import { REST, Routes } from 'discord.js';
import { commands } from './commands.js';

export async function registerCommands() {
  const token = process.env.DISCORD_TOKEN;
  const clientId = normalizeSnowflake('DISCORD_CLIENT_ID', process.env.DISCORD_CLIENT_ID);
  const guildId = normalizeOptionalSnowflake('DISCORD_GUILD_ID', process.env.DISCORD_GUILD_ID);

  if (!token || !clientId) {
    throw new Error('DISCORD_TOKEN and DISCORD_CLIENT_ID are required.');
  }

  const rest = new REST({ version: '10' }).setToken(token);
  const route = guildId
    ? Routes.applicationGuildCommands(clientId, guildId)
    : Routes.applicationCommands(clientId);

  console.log(`Refreshing ${commands.length} slash commands ${guildId ? `for guild ${guildId}` : 'globally'}...`);
  await rest.put(route, { body: commands });
  console.log('Slash commands are up to date.');
}

function normalizeSnowflake(name, value) {
  const normalized = String(value || '').trim();
  if (!normalized) {
    return '';
  }
  if (!isSnowflake(normalized)) {
    throw new Error(`${name} must be a Discord snowflake ID, but got "${normalized}".`);
  }
  return normalized;
}

function normalizeOptionalSnowflake(name, value) {
  const normalized = String(value || '').trim();
  if (!normalized || isPlaceholder(normalized)) {
    return '';
  }
  return normalizeSnowflake(name, normalized);
}

function isSnowflake(value) {
  return /^\d{17,20}$/.test(value);
}

function isPlaceholder(value) {
  return [
    'your_test_server_id',
    'your_guild_id',
    'guild_id_here'
  ].includes(value.toLowerCase());
}
