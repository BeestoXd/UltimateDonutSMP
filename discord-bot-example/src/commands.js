import { SlashCommandBuilder } from 'discord.js';
import { leaderboardTypes } from './database.js';
import { tierLeaderboardTypes } from './pvp.js';

const leaderboardChoices = Object.entries(leaderboardTypes).map(([value, definition]) => ({
  name: definition.label,
  value
}));

const tierChoices = Object.entries(tierLeaderboardTypes).map(([value, definition]) => ({
  name: definition.label,
  value
}));

export const commands = [
  new SlashCommandBuilder()
    .setName('stats')
    .setDescription('Show player stats from the UltimateDonutSmp database.')
    .addStringOption(option =>
      option
        .setName('player')
        .setDescription('Minecraft username or UUID')
        .setRequired(true)
    ),
  new SlashCommandBuilder()
    .setName('leaderboard')
    .setDescription('Show a top-player leaderboard from the UltimateDonutSmp database.')
    .addStringOption(option =>
      option
        .setName('type')
        .setDescription('Leaderboard type')
        .setRequired(true)
        .addChoices(...leaderboardChoices)
    )
    .addIntegerOption(option =>
      option
        .setName('limit')
        .setDescription('Number of entries to show')
        .setMinValue(1)
        .setMaxValue(10)
    ),
  new SlashCommandBuilder()
    .setName('mystats')
    .setDescription('View your own competitive statistics.'),
  new SlashCommandBuilder()
    .setName('sync')
    .setDescription('Sync your Minecraft account with Discord.')
    .addStringOption(option =>
      option
        .setName('code')
        .setDescription('The code /pvp sync gave you in game')
        .setRequired(true)
    ),
  new SlashCommandBuilder()
    .setName('unsync')
    .setDescription('Unlink your Minecraft account.'),
  new SlashCommandBuilder()
    .setName('tier')
    .setDescription('Ranked arena commands.')
    .addSubcommand(subcommand =>
      subcommand
        .setName('leaderboard')
        .setDescription('View the top players.')
        .addStringOption(option =>
          option
            .setName('type')
            .setDescription('Leaderboard type')
            .setRequired(true)
            .addChoices(...tierChoices)
        )
        .addIntegerOption(option =>
          option
            .setName('limit')
            .setDescription('Number of entries to show')
            .setMinValue(1)
            .setMaxValue(10)
        )
    )
    .addSubcommand(subcommand =>
      subcommand
        .setName('stats')
        .setDescription('View the competitive statistics of another player.')
        .addStringOption(option =>
          option
            .setName('player')
            .setDescription('Minecraft username or UUID')
            .setRequired(true)
        )
    )
].map(command => command.toJSON());
