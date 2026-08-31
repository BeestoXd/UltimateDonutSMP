import { EmbedBuilder } from 'discord.js';
import { tierLeaderboardTypes } from './pvp.js';
import { rankName } from './ranks.js';
import { formatKdr, formatNumber } from './format.js';

const competitiveColor = 0x5865f2;
const winColor = 0x22c55e;
const drawColor = 0xeab308;

export function createCompetitiveStatsEmbed(stats, record, requestedBy) {
  return new EmbedBuilder()
    .setColor(competitiveColor)
    .setTitle(`${escape(stats.username)}'s Stats`)
    .setDescription(`> Viewed by **${escape(requestedBy)}**`)
    .setThumbnail(skinBustUrl(stats.uuid))
    .addFields(
      {
        name: ':trophy: Competitive',
        value: [
          `Rank: **${rankName(stats.elo)}**`,
          `ELO: **${formatNumber(stats.elo)}**`,
          `Level: **${formatNumber(stats.level)}**`
        ].join('\n'),
        inline: false
      },
      {
        name: ':crossed_swords: Stats',
        value: [
          `Kills: **${formatNumber(stats.kills)}**`,
          `Deaths: **${formatNumber(stats.deaths)}**`,
          `K/D: **${formatKdr(stats.kills, stats.deaths)}**`,
          `Kill Streak: **${formatNumber(stats.streak)}**`,
          `Highest Kill Streak: **${formatNumber(stats.bestStreak)}**`
        ].join('\n'),
        inline: false
      },
      {
        name: ':scroll: Record',
        value: `Wins: **${formatNumber(record.wins)}** | Losses: **${formatNumber(record.losses)}** | Draws: **${formatNumber(record.draws)}**`,
        inline: false
      },
      {
        name: ':information_source: Information',
        value: [
          `Arena joins: **${formatNumber(stats.arenaJoins)}**`,
          `Last active: **${formatRelative(stats.updatedAt)}**`
        ].join('\n'),
        inline: false
      }
    )
    .setFooter({ text: 'UltimateDonutSmp ranked stats' })
    .setTimestamp();
}

export function createTierLeaderboardEmbed(typeKey, entries, requestedBy) {
  const type = tierLeaderboardTypes[typeKey];
  const top = entries[0];
  const showRank = typeKey === 'elo';

  const lines = entries.map((entry, index) => {
    const suffix = showRank ? ` (${rankName(entry.value)})` : '';
    return `**#${index + 1}** ${escape(entry.username)} » \`${formatNumber(entry.value)}\`${suffix}`;
  });

  return new EmbedBuilder()
    .setColor(competitiveColor)
    .setTitle(`:trophy: Top ${type.label}`)
    .setDescription(lines.join('\n'))
    .setThumbnail(skinBustUrl(top.uuid))
    .setFooter({ text: `UltimateDonutSmp leaderboards · requested by ${requestedBy}` })
    .setTimestamp();
}

/**
 * The snapshot posted after a ranked match ends.
 *
 * Both fighters get an inline column so the two sides sit side by side and can be compared without
 * scrolling, which is the whole point of posting the result rather than just the winner.
 */
export function createMatchResultEmbed(match) {
  const decided = match.result === 'DECIDED' && match.winnerUuid;
  const winnerName = decided
    ? (match.winnerUuid === match.first.uuid ? match.first.name : match.second.name)
    : null;

  return new EmbedBuilder()
    .setColor(decided ? winColor : drawColor)
    .setTitle(`:crossed_swords: Match Result: ${escape(match.first.name)} vs ${escape(match.second.name)}`)
    .setThumbnail(skinBustUrl(decided ? match.winnerUuid : match.first.uuid))
    .addFields(
      {
        name: 'Winner',
        value: decided ? `**${escape(winnerName)}**` : `**${match.result === 'DRAW' ? 'Draw' : 'No result'}**`,
        inline: true
      },
      {
        name: 'Duration',
        value: `**${formatDuration(match.durationSeconds)}**`,
        inline: true
      },
      { name: '​', value: '​', inline: true },
      fighterField(match, match.first),
      fighterField(match, match.second),
      { name: '​', value: '​', inline: true }
    )
    .setFooter({ text: match.kitId ? `Match snapshot · ${match.kitId} kit` : 'Match snapshot' })
    .setTimestamp(match.endedAt > 0 ? new Date(match.endedAt) : new Date());
}

function fighterField(match, fighter) {
  return {
    name: `${escape(fighter.name)} Stats`,
    value: [
      `Rank: **${rankName(fighter.eloBefore + fighter.eloDelta)}**`,
      `ELO: **${formatNumber(fighter.eloBefore + fighter.eloDelta)}** ${formatDelta(fighter.eloDelta)}`,
      '',
      `Hits: **${formatNumber(fighter.hits)}**`,
      `Crystals: **${formatNumber(fighter.crystals)}**`,
      `Final HP: **${fighter.finalHealth.toFixed(1)} / ${match.maxHealth.toFixed(1)}**`
    ].join('\n'),
    inline: true
  };
}

function formatDelta(delta) {
  if (delta > 0) {
    return `(+${delta})`;
  }
  return delta < 0 ? `(${delta})` : '(0)';
}

export function formatDuration(totalSeconds) {
  const seconds = Math.max(0, Math.floor(Number(totalSeconds || 0)));
  if (seconds < 60) {
    return `${seconds}s`;
  }
  const minutes = Math.floor(seconds / 60);
  const remainder = seconds % 60;
  if (minutes < 60) {
    return `${minutes}m ${remainder}s`;
  }
  return `${Math.floor(minutes / 60)}h ${minutes % 60}m ${remainder}s`;
}

/** Discord renders this itself, so the reader always sees it in their own timezone. */
function formatRelative(epochMillis) {
  const value = Number(epochMillis || 0);
  if (value <= 0) {
    return 'never';
  }
  return `<t:${Math.floor(value / 1000)}:R>`;
}

function skinBustUrl(uuid) {
  const template = process.env.SKIN_BUST_URL || 'https://visage.surgeplay.com/bust/384/%uuid_no_dash%';
  return template
    .replaceAll('%uuid%', String(uuid))
    .replaceAll('%uuid_no_dash%', String(uuid).replaceAll('-', ''));
}

/** Player names are server-controlled text, so they are neutralised before going into markdown. */
function escape(value) {
  return String(value ?? '').replace(/[\\`*_~|>]/g, character => `\\${character}`);
}
