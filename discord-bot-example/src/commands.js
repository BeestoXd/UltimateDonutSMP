import { SlashCommandBuilder } from 'discord.js';
import { leaderboardTypes } from './database.js';

const leaderboardChoices = Object.entries(leaderboardTypes).map(([value, definition]) => ({
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
    )
].map(command => command.toJSON());
