// Builds a throwaway database with the plugin's real schema and drives every query the bot makes
// against it. A column renamed on the plugin side fails here rather than in a live channel.
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import test, { after, before } from 'node:test';
import Database from 'better-sqlite3';

const ALICE = '11111111-1111-1111-1111-111111111111';
const BOB = '22222222-2222-2222-2222-222222222222';

let scratch;
let pvp;
let links;
let modules;

before(async () => {
  scratch = fs.mkdtempSync(path.join(os.tmpdir(), 'uds-bot-test-'));
  const databaseFile = path.join(scratch, 'data.db');
  seedDatabase(databaseFile);

  process.env.DB_TYPE = 'sqlite';
  process.env.SQLITE_FILE = databaseFile;
  process.env.LINKS_FILE = path.join(scratch, 'links.db');
  delete process.env.PVP_RANKS;

  modules = {
    pvp: await import('../src/pvp.js'),
    links: await import('../src/links.js'),
    ranks: await import('../src/ranks.js'),
    embeds: await import('../src/pvp-embeds.js')
  };

  pvp = await modules.pvp.createPvpRepository();
  links = modules.links.createLinkStore();
});

after(() => {
  pvp?.close?.();
  links?.close?.();
  fs.rmSync(scratch, { recursive: true, force: true });
});

function seedDatabase(file) {
  const database = new Database(file);
  database.exec(`
    CREATE TABLE players (uuid TEXT PRIMARY KEY, username TEXT);
    CREATE TABLE pvp_stats (player_uuid TEXT PRIMARY KEY, elo INTEGER DEFAULT 0,
      pvp_level INTEGER DEFAULT 1, pvp_xp INTEGER DEFAULT 0, kills INTEGER DEFAULT 0,
      deaths INTEGER DEFAULT 0, streak INTEGER DEFAULT 0, best_streak INTEGER DEFAULT 0,
      arena_joins INTEGER DEFAULT 0, updated_at INTEGER DEFAULT 0);
    CREATE TABLE pvp_matches (id INTEGER PRIMARY KEY AUTOINCREMENT, player_one_uuid TEXT NOT NULL,
      player_one_name TEXT, player_two_uuid TEXT NOT NULL, player_two_name TEXT, kit_id TEXT,
      winner_uuid TEXT, result TEXT, started_at INTEGER DEFAULT 0, ended_at INTEGER DEFAULT 0,
      one_hits INTEGER DEFAULT 0, two_hits INTEGER DEFAULT 0, one_crystals INTEGER DEFAULT 0,
      two_crystals INTEGER DEFAULT 0, one_elo_before INTEGER DEFAULT 0, two_elo_before INTEGER DEFAULT 0,
      one_elo_delta INTEGER DEFAULT 0, two_elo_delta INTEGER DEFAULT 0, one_final_health REAL DEFAULT 0,
      two_final_health REAL DEFAULT 0, max_health REAL DEFAULT 20);
    CREATE TABLE pvp_sync_codes (code TEXT PRIMARY KEY, player_uuid TEXT NOT NULL, player_name TEXT,
      created_at INTEGER DEFAULT 0, expires_at INTEGER DEFAULT 0);
  `);

  const now = Date.now();
  database.prepare('INSERT INTO players (uuid, username) VALUES (?,?)').run(ALICE, 'SamirSpider');
  database.prepare('INSERT INTO players (uuid, username) VALUES (?,?)').run(BOB, 'Nexf');
  const insertStats = database.prepare(`
    INSERT INTO pvp_stats (player_uuid, elo, pvp_level, kills, deaths, streak, best_streak, arena_joins, updated_at)
    VALUES (?,?,?,?,?,?,?,?,?)
  `);
  insertStats.run(ALICE, 970, 3, 4, 9, 0, 2, 11, now);
  insertStats.run(BOB, 1040, 7, 9, 4, 3, 5, 14, now);
  database.prepare(`
    INSERT INTO pvp_matches (player_one_uuid, player_one_name, player_two_uuid, player_two_name, kit_id,
      winner_uuid, result, started_at, ended_at, one_hits, two_hits, one_crystals, two_crystals,
      one_elo_before, two_elo_before, one_elo_delta, two_elo_delta, one_final_health, two_final_health, max_health)
    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
  `).run(ALICE, 'SamirSpider', BOB, 'Nexf', 'warrior', BOB, 'DECIDED', 1000, 9000,
    0, 5, 0, 0, 985, 1020, -15, 20, 0, 7, 20);
  database.prepare(`
    INSERT INTO pvp_sync_codes (code, player_uuid, player_name, created_at, expires_at)
    VALUES (?,?,?,?,?)
  `).run('ouw5rp', BOB, 'Nexf', now, now + 600_000);
  database.close();
}

test('stats resolve by username and by uuid', async () => {
  const byName = await pvp.findStats('nexf');
  assert.equal(byName.username, 'Nexf');
  assert.equal(byName.elo, 1040);
  assert.equal(byName.level, 7);

  assert.equal((await pvp.findStats(ALICE)).username, 'SamirSpider');
  assert.equal(await pvp.findStats('nobody'), null);
});

test('the record counts wins and losses from the match rows', async () => {
  const winner = await pvp.getRecord(BOB);
  assert.deepEqual(winner, { wins: 1, losses: 0, draws: 0 });

  const loser = await pvp.getRecord(ALICE);
  assert.deepEqual(loser, { wins: 0, losses: 1, draws: 0 });
});

test('every leaderboard type runs against the real schema', async () => {
  for (const typeKey of Object.keys(modules.pvp.tierLeaderboardTypes)) {
    const rows = await pvp.getTierLeaderboard(typeKey, 10);
    assert.ok(Array.isArray(rows), `${typeKey} did not return rows`);
  }
});

test('leaderboards are ordered and name the right player', async () => {
  assert.equal((await pvp.getTierLeaderboard('elo', 10))[0].username, 'Nexf');
  assert.equal((await pvp.getTierLeaderboard('wins', 10))[0].username, 'Nexf');
  assert.equal((await pvp.getTierLeaderboard('losses', 10))[0].username, 'SamirSpider');
});

test('the match watcher reads forward from an id and stops', async () => {
  assert.equal(await pvp.getLatestMatchId(), 1);
  assert.equal((await pvp.getMatchesAfter(0, 10)).length, 1);
  assert.equal((await pvp.getMatchesAfter(1, 10)).length, 0);
});

test('a match row keeps both sides apart', async () => {
  const [match] = await pvp.getMatchesAfter(0, 10);

  assert.equal(match.durationSeconds, 8);
  assert.equal(match.first.eloDelta, -15);
  assert.equal(match.second.eloDelta, 20);
  assert.equal(match.second.hits, 5);
  assert.equal(match.first.finalHealth, 0);
  assert.equal(match.second.finalHealth, 7);
  assert.equal(match.maxHealth, 20);
});

test('sync codes are looked up without case mattering', async () => {
  const entry = await pvp.findSyncCode('OUW5RP');
  assert.equal(entry.uuid, BOB);
  assert.equal(entry.name, 'Nexf');
  assert.ok(entry.expiresAt > Date.now());

  assert.equal(await pvp.findSyncCode('zzzzzz'), null);
});

test('a rank is the highest one the elo reaches', () => {
  const { rankName } = modules.ranks;

  assert.equal(rankName(0), 'LT5');
  assert.equal(rankName(-10), 'LT5', 'below the floor still ranks');
  assert.equal(rankName(399), 'LT2');
  assert.equal(rankName(400), 'LT1');
  assert.equal(rankName(970), 'HT4');
  assert.equal(rankName(1040), 'HT3');
  assert.equal(rankName(99_999), 'HT1');
});

test('links are one per Discord user and one per account', () => {
  assert.equal(links.getLink('discord-1'), null);

  links.link('discord-1', BOB, 'Nexf', 'ouw5rp');
  assert.equal(links.getLink('discord-1').player_uuid, BOB);
  assert.equal(links.isCodeUsed('OUW5RP'), true, 'a spent code cannot be claimed again');
  assert.equal(links.isCodeUsed('abc123'), false);

  links.link('discord-2', BOB, 'Nexf', 'newcode');
  assert.equal(links.getLink('discord-1'), null, 'relinking moves the account rather than duplicating it');
  assert.equal(links.getLink('discord-2').player_uuid, BOB);

  assert.equal(links.unlink('discord-2'), true);
  assert.equal(links.unlink('discord-2'), false);
});

test('watcher state survives a read back', () => {
  links.setState('last_match_id', '7');
  assert.equal(links.getState('last_match_id'), '7');
  assert.equal(links.getState('never_set', '0'), '0');
});

test('the embeds carry what the screenshots show', async () => {
  const stats = await pvp.findStats('nexf');
  const record = await pvp.getRecord(BOB);
  const [match] = await pvp.getMatchesAfter(0, 10);
  const board = await pvp.getTierLeaderboard('elo', 10);

  const statsEmbed = modules.embeds.createCompetitiveStatsEmbed(stats, record, 'Hugster').toJSON();
  assert.equal(statsEmbed.title, "Nexf's Stats");
  assert.match(JSON.stringify(statsEmbed.fields), /HT3/);

  const boardEmbed = modules.embeds.createTierLeaderboardEmbed('elo', board, 'Hugster').toJSON();
  assert.match(boardEmbed.description, /Nexf/);
  assert.match(boardEmbed.description, /SamirSpider/);

  const matchEmbed = modules.embeds.createMatchResultEmbed(match).toJSON();
  assert.match(matchEmbed.title, /SamirSpider vs Nexf/);
  assert.match(matchEmbed.fields[0].value, /Nexf/);
  assert.match(matchEmbed.fields[1].value, /8s/);
  assert.match(JSON.stringify(matchEmbed.fields), /7\.0 \/ 20\.0/);
  assert.match(JSON.stringify(matchEmbed.fields), /\(\+20\)/);
});

test('durations read as clock time', () => {
  assert.equal(modules.embeds.formatDuration(8), '8s');
  assert.equal(modules.embeds.formatDuration(65), '1m 5s');
  assert.equal(modules.embeds.formatDuration(3725), '1h 2m 5s');
  assert.equal(modules.embeds.formatDuration(-3), '0s');
});
