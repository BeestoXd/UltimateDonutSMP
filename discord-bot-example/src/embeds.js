import { EmbedBuilder } from 'discord.js';
import { leaderboardTypes } from './database.js';
import { formatKdr, formatMoney, formatNumber, formatPlaytime, formatValue } from './format.js';

const accentColor = 0x22d3ee;

export function createStatsEmbed(player, requestedBy) {
  return new EmbedBuilder()
    .setColor(accentColor)
    .setAuthor({ name: 'Stats System', iconURL: skinFaceUrl(player.uuid) })
    .setTitle(`${player.username}'s Stats`)
    .setDescription(`> Viewed by **${requestedBy}**`)
    .setThumbnail(skinBustUrl(player.uuid))
    .addFields(
      {
        name: ':moneybag: Economy',
        value: [
          `Money: **${formatMoney(player.money)}**`,
          `Shards: **${formatNumber(player.shards)}**`
        ].join('\n'),
        inline: true
      },
      {
        name: ':crossed_swords: Combat',
        value: [
          `Kills: **${formatNumber(player.kills)}**`,
          `Deaths: **${formatNumber(player.deaths)}**`,
          `K/D: **${formatKdr(player.kills, player.deaths)}**`
        ].join('\n'),
        inline: true
      },
      {
        name: ':hourglass_flowing_sand: Activity',
        value: `Playtime: **${formatPlaytime(player.playtimeSeconds)}**`,
        inline: false
      }
    )
    .setFooter({ text: 'UltimateDonutSmp stats' })
    .setTimestamp();
}

export function createLeaderboardEmbed(typeKey, entries, requestedBy) {
  const type = leaderboardTypes[typeKey];
  const topPlayer = entries[0];
  const lines = entries.map((player, index) => {
    return `${rankIcon(index + 1)} **${escapeMarkdown(player.username)}** \`${formatValue(type, player)}\``;
  });

  return new EmbedBuilder()
    .setColor(0x38bdf8)
    .setAuthor({ name: 'Leaderboard System', iconURL: skinFaceUrl(topPlayer.uuid) })
    .setTitle(`${type.label} Leaderboard`)
    .setDescription(`> Top player: **${escapeMarkdown(topPlayer.username)}** with \`${formatValue(type, topPlayer)}\``)
    .setThumbnail(skinBustUrl(topPlayer.uuid))
    .addFields(
      {
        name: ':trophy: Top 10',
        value: lines.join('\n'),
        inline: false
      },
      {
        name: ':mag: Snapshot',
        value: [
          `Type: **${type.label}**`,
          `Requested by: **${requestedBy}**`
        ].join('\n'),
        inline: false
      }
    )
    .setFooter({ text: 'UltimateDonutSmp leaderboard' })
    .setTimestamp();
}

function skinBustUrl(uuid) {
  return renderUrl(process.env.SKIN_BUST_URL || 'https://visage.surgeplay.com/bust/384/%uuid_no_dash%', uuid);
}

function skinFaceUrl(uuid) {
  return renderUrl(process.env.SKIN_FACE_URL || 'https://visage.surgeplay.com/face/128/%uuid_no_dash%', uuid);
}

function renderUrl(template, uuid) {
  const uuidNoDash = String(uuid).replaceAll('-', '');
  return template
    .replaceAll('%uuid%', String(uuid))
    .replaceAll('%uuid_no_dash%', uuidNoDash);
}

function rankIcon(position) {
  if (position === 1) {
    return ':first_place:';
  }
  if (position === 2) {
    return ':second_place:';
  }
  if (position === 3) {
    return ':third_place:';
  }
  return ':small_blue_diamond:';
}

function escapeMarkdown(value) {
  return String(value).replaceAll('`', "'");
}
