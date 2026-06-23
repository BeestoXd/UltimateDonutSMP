import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import Database from 'better-sqlite3';
import { MongoClient } from 'mongodb';
import mysql from 'mysql2/promise';

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const defaultSqliteFile = '../plugins/UltimateDonutSmp/data/data.db';

export const leaderboardTypes = {
  money: { label: 'Money', column: 'money', formatter: 'money' },
  shards: { label: 'Shards', column: 'shards', formatter: 'integer' },
  kills: { label: 'Kills', column: 'kills', formatter: 'integer' },
  deaths: { label: 'Deaths', column: 'deaths', formatter: 'integer' },
  playtime: { label: 'Playtime', column: 'playtime_seconds', formatter: 'time' },
  blocksplaced: { label: 'Blocks Placed', column: 'blocks_placed', formatter: 'integer' },
  blocksbroken: { label: 'Blocks Broken', column: 'blocks_broken', formatter: 'integer' },
  mobskilled: { label: 'Mobs Killed', column: 'mobs_killed', formatter: 'integer' },
  killstreak: { label: 'Kill Streak', column: 'kill_streak', formatter: 'integer' },
  highestkillstreak: { label: 'Highest Kill Streak', column: 'highest_kill_streak', formatter: 'integer' },
  moneyspent: { label: 'Money Spent', column: 'money_spent', formatter: 'money' },
  moneymade: { label: 'Money Made', column: 'money_made', formatter: 'money' }
};

const playerColumns = `
  uuid,
  username,
  money,
  shards,
  kills,
  deaths,
  playtime_seconds,
  blocks_placed,
  blocks_broken,
  mobs_killed,
  kill_streak,
  highest_kill_streak,
  money_spent,
  money_made
`;

export async function createPlayerRepository() {
  const type = (process.env.DB_TYPE || 'sqlite').toLowerCase();
  if (type === 'mysql') {
    return createMysqlRepository();
  }
  if (type === 'mongodb' || type === 'mongo') {
    return createMongoRepository();
  }
  return createSqliteRepository();
}

function createSqliteRepository() {
  const configuredPath = process.env.SQLITE_FILE || defaultSqliteFile;
  const databasePath = resolveSqlitePath(configuredPath);
  assertReadableSqliteFile(databasePath, configuredPath);
  const database = new Database(databasePath, { readonly: true, fileMustExist: true });

  return {
    async findPlayer(input) {
      return normalizePlayer(database.prepare(`
        SELECT ${playerColumns}
        FROM players
        WHERE LOWER(username) = LOWER(?) OR uuid = ?
        LIMIT 1
      `).get(input, input));
    },

    async getLeaderboard(typeKey, limit) {
      const type = leaderboardTypes[typeKey];
      if (!type) {
        return [];
      }

      return database.prepare(`
        SELECT ${playerColumns}
        FROM players
        WHERE username IS NOT NULL AND username <> ''
        ORDER BY ${type.column} DESC, LOWER(username) ASC
        LIMIT ?
      `).all(limit).map(normalizePlayer);
    },

    close() {
      database.close();
    }
  };
}

function resolveSqlitePath(configuredPath) {
  const value = String(configuredPath || defaultSqliteFile).trim();
  if (value.startsWith('file://')) {
    return fileURLToPath(value);
  }
  if (path.isAbsolute(value)) {
    return value;
  }
  return path.resolve(projectRoot, value);
}

function assertReadableSqliteFile(databasePath, configuredPath) {
  if (!fs.existsSync(databasePath)) {
    throw new Error(
      [
        `SQLite database file was not found: ${databasePath}`,
        `SQLITE_FILE is currently set to: ${configuredPath}`,
        'Point SQLITE_FILE to the real UltimateDonutSmp database, usually plugins/UltimateDonutSmp/data/data.db, or copy that data.db file into this bot project.'
      ].join('\n')
    );
  }

  const stats = fs.statSync(databasePath);
  if (!stats.isFile()) {
    throw new Error(`SQLite path is not a file: ${databasePath}`);
  }
}

async function createMysqlRepository() {
  const pool = mysql.createPool({
    host: process.env.MYSQL_HOST || 'localhost',
    port: Number(process.env.MYSQL_PORT || 3306),
    database: process.env.MYSQL_DATABASE || 'ultimatedonutsmp',
    user: process.env.MYSQL_USER || 'root',
    password: process.env.MYSQL_PASSWORD || '',
    waitForConnections: true,
    connectionLimit: 5
  });

  return {
    async findPlayer(input) {
      const [rows] = await pool.execute(`
        SELECT ${playerColumns}
        FROM players
        WHERE LOWER(username) = LOWER(?) OR uuid = ?
        LIMIT 1
      `, [input, input]);
      return normalizePlayer(rows[0]);
    },

    async getLeaderboard(typeKey, limit) {
      const type = leaderboardTypes[typeKey];
      if (!type) {
        return [];
      }

      const [rows] = await pool.execute(`
        SELECT ${playerColumns}
        FROM players
        WHERE username IS NOT NULL AND username <> ''
        ORDER BY ${type.column} DESC, LOWER(username) ASC
        LIMIT ?
      `, [limit]);
      return rows.map(normalizePlayer);
    },

    close() {
      return pool.end();
    }
  };
}

async function createMongoRepository() {
  const uri = process.env.MONGODB_URI || 'mongodb://localhost:27017';
  const databaseName = process.env.MONGODB_DATABASE || 'ultimatedonutsmp';
  const playersCollectionName = process.env.MONGODB_PLAYERS_COLLECTION || 'players';
  const client = new MongoClient(uri);

  await client.connect();
  const collection = client.db(databaseName).collection(playersCollectionName);

  return {
    async findPlayer(input) {
      const query = {
        $or: [
          { username: { $regex: `^${escapeRegex(input)}$`, $options: 'i' } },
          { uuid: input }
        ]
      };
      return normalizePlayer(await collection.findOne(query, { projection: mongoProjection() }));
    },

    async getLeaderboard(typeKey, limit) {
      const type = leaderboardTypes[typeKey];
      if (!type) {
        return [];
      }

      return collection
        .find(
          { username: { $exists: true, $ne: '' } },
          { projection: mongoProjection() }
        )
        .sort({ [type.column]: -1, username: 1 })
        .limit(limit)
        .toArray()
        .then(rows => rows.map(normalizePlayer));
    },

    close() {
      return client.close();
    }
  };
}

function mongoProjection() {
  return Object.fromEntries(
    playerColumns
      .split(',')
      .map(column => column.trim())
      .filter(Boolean)
      .map(column => [column, 1])
  );
}

function escapeRegex(value) {
  return String(value).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function normalizePlayer(row) {
  if (!row) {
    return null;
  }

  return {
    uuid: String(row.uuid),
    username: row.username || 'Unknown',
    money: Number(row.money || 0),
    shards: Number(row.shards || 0),
    kills: Number(row.kills || 0),
    deaths: Number(row.deaths || 0),
    playtimeSeconds: Number(row.playtime_seconds || 0),
    blocksPlaced: Number(row.blocks_placed || 0),
    blocksBroken: Number(row.blocks_broken || 0),
    mobsKilled: Number(row.mobs_killed || 0),
    killStreak: Number(row.kill_streak || 0),
    highestKillStreak: Number(row.highest_kill_streak || 0),
    moneySpent: Number(row.money_spent || 0),
    moneyMade: Number(row.money_made || 0)
  };
}
