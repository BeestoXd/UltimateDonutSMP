import 'dotenv/config';
import { Client, Events, GatewayIntentBits, MessageFlags } from 'discord.js';
import { createPlayerRepository, leaderboardTypes } from './database.js';
import { createPvpRepository, tierLeaderboardTypes } from './pvp.js';
import { createLinkStore } from './links.js';
import { createLeaderboardEmbed, createStatsEmbed } from './embeds.js';
import { createCompetitiveStatsEmbed, createTierLeaderboardEmbed } from './pvp-embeds.js';
import { createMatchWatcher } from './match-watcher.js';
import { registerCommands } from './register-commands.js';

const token = process.env.DISCORD_TOKEN;

if (!token) {
  throw new Error('DISCORD_TOKEN is required.');
}

const repository = await createPlayerRepository();
const pvp = await createPvpRepository();
const links = createLinkStore();
const client = new Client({ intents: [GatewayIntentBits.Guilds] });
const matchWatcher = createMatchWatcher({ client, repository: pvp, links });

await registerCommands();

client.once(Events.ClientReady, async readyClient => {
  console.log(`Logged in as ${readyClient.user.tag}.`);
  await matchWatcher.start();
});

client.on(Events.InteractionCreate, async interaction => {
  if (!interaction.isChatInputCommand()) {
    return;
  }

  try {
    switch (interaction.commandName) {
      case 'stats':
        await handleStats(interaction);
        return;
      case 'leaderboard':
        await handleLeaderboard(interaction);
        return;
      case 'mystats':
        await handleMyStats(interaction);
        return;
      case 'sync':
        await handleSync(interaction);
        return;
      case 'unsync':
        await handleUnsync(interaction);
        return;
      case 'tier':
        await handleTier(interaction);
        return;
      default:
    }
  } catch (error) {
    console.error(error);
    const message = 'Failed to read the UltimateDonutSmp database.';
    if (interaction.deferred || interaction.replied) {
      await interaction.editReply({ content: message, embeds: [] });
    } else {
      await interaction.reply({ content: message, flags: MessageFlags.Ephemeral });
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

// ── Ranked arena ─────────────────────────────────────────────────────────────

async function handleMyStats(interaction) {
  await interaction.deferReply();

  const link = links.getLink(interaction.user.id);
  if (!link) {
    await interaction.editReply(
      'Your Discord account is not linked yet. Run `/pvp sync` in game, then `/sync` here with the code it gives you.'
    );
    return;
  }

  await replyWithCompetitiveStats(interaction, link.player_uuid, link.player_name);
}

async function handleTier(interaction) {
  const subcommand = interaction.options.getSubcommand();
  if (subcommand === 'stats') {
    await interaction.deferReply();
    await replyWithCompetitiveStats(interaction, interaction.options.getString('player', true));
    return;
  }

  const typeKey = interaction.options.getString('type', true);
  const limit = interaction.options.getInteger('limit') ?? 10;
  await interaction.deferReply();

  if (!tierLeaderboardTypes[typeKey]) {
    await interaction.editReply(`Unknown leaderboard type: \`${typeKey}\`.`);
    return;
  }

  const entries = (await pvp.getTierLeaderboard(typeKey, limit)).filter(Boolean);
  if (entries.length === 0) {
    await interaction.editReply('Nobody has fought a ranked match yet.');
    return;
  }

  await interaction.editReply({
    embeds: [createTierLeaderboardEmbed(typeKey, entries, interaction.user.username)]
  });
}

async function replyWithCompetitiveStats(interaction, lookup, fallbackName) {
  const stats = await pvp.findStats(lookup);
  if (!stats) {
    await interaction.editReply(`No ranked record found for \`${fallbackName ?? lookup}\`.`);
    return;
  }

  const record = await pvp.getRecord(stats.uuid);
  await interaction.editReply({
    embeds: [createCompetitiveStatsEmbed(stats, record, interaction.user.username)]
  });
}

/**
 * Claims a Minecraft account with a code issued in game.
 *
 * The reply is ephemeral throughout: a code that is still valid should not stay readable in a
 * public channel, and neither should the failure that tells somebody a code was wrong.
 */
async function handleSync(interaction) {
  const code = interaction.options.getString('code', true).trim().toLowerCase();
  await interaction.deferReply({ flags: MessageFlags.Ephemeral });

  const entry = await pvp.findSyncCode(code);
  if (!entry) {
    await interaction.editReply('That sync code is not valid. Run `/pvp sync` in game for a new one.');
    return;
  }
  if (entry.expiresAt > 0 && entry.expiresAt < Date.now()) {
    await interaction.editReply('That sync code has expired. Run `/pvp sync` in game for a new one.');
    return;
  }
  if (links.isCodeUsed(code)) {
    await interaction.editReply('That sync code has already been used. Run `/pvp sync` in game for a new one.');
    return;
  }

  const taken = links.getLinkByUuid(entry.uuid);
  if (taken && taken.discord_id !== interaction.user.id) {
    await interaction.editReply(
      'That Minecraft account is already linked to another Discord user. They need to `/unsync` first.'
    );
    return;
  }

  const name = entry.name ?? (await pvp.findStats(entry.uuid))?.username ?? 'your account';
  links.link(interaction.user.id, entry.uuid, name, code);
  await interaction.editReply(`Sync successful! You are now linked with **${name}**.`);
}

async function handleUnsync(interaction) {
  await interaction.deferReply({ flags: MessageFlags.Ephemeral });

  const link = links.getLink(interaction.user.id);
  if (!link) {
    await interaction.editReply('Your Discord account is not linked to a Minecraft account.');
    return;
  }

  links.unlink(interaction.user.id);
  await interaction.editReply(`Unlinked from **${link.player_name ?? link.player_uuid}**.`);
}

function shutdown() {
  matchWatcher.stop();
  repository.close?.();
  pvp.close?.();
  links.close?.();
  client.destroy();
}

process.once('SIGINT', shutdown);
process.once('SIGTERM', shutdown);

await client.login(token);
