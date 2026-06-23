import 'dotenv/config';
import { Client, Events, GatewayIntentBits } from 'discord.js';
import { createPlayerRepository, leaderboardTypes } from './database.js';
import { createLeaderboardEmbed, createStatsEmbed } from './embeds.js';
import { registerCommands } from './register-commands.js';

const token = process.env.DISCORD_TOKEN;

if (!token) {
  throw new Error('DISCORD_TOKEN is required.');
}

const repository = await createPlayerRepository();
const client = new Client({ intents: [GatewayIntentBits.Guilds] });

await registerCommands();

client.once(Events.ClientReady, readyClient => {
  console.log(`Logged in as ${readyClient.user.tag}.`);
});

client.on(Events.InteractionCreate, async interaction => {
  if (!interaction.isChatInputCommand()) {
    return;
  }

  try {
    if (interaction.commandName === 'stats') {
      await handleStats(interaction);
      return;
    }

    if (interaction.commandName === 'leaderboard') {
      await handleLeaderboard(interaction);
    }
  } catch (error) {
    console.error(error);
    const message = 'Failed to read the UltimateDonutSmp database.';
    if (interaction.deferred || interaction.replied) {
      await interaction.editReply({ content: message, embeds: [] });
    } else {
      await interaction.reply({ content: message });
    }
  }
});

async function handleStats(interaction) {
  const playerInput = interaction.options.getString('player', true);
  await interaction.deferReply();

  const player = await repository.findPlayer(playerInput);
  if (!player) {
    await interaction.editReply(`No player found for \`${playerInput}\`.`);
    return;
  }

  await interaction.editReply({ embeds: [createStatsEmbed(player, interaction.user.username)] });
}

async function handleLeaderboard(interaction) {
  const typeKey = interaction.options.getString('type', true);
  const limit = interaction.options.getInteger('limit') ?? 10;
  const type = leaderboardTypes[typeKey];

  await interaction.deferReply();

  if (!type) {
    await interaction.editReply(`Unknown leaderboard type: \`${typeKey}\`.`);
    return;
  }

  const entries = await repository.getLeaderboard(typeKey, limit);
  if (entries.length === 0) {
    await interaction.editReply('No leaderboard data found.');
    return;
  }

  await interaction.editReply({ embeds: [createLeaderboardEmbed(typeKey, entries, interaction.user.username)] });
}

function shutdown() {
  repository.close?.();
  client.destroy();
}

process.once('SIGINT', shutdown);
process.once('SIGTERM', shutdown);

await client.login(token);
