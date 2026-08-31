import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import Database from 'better-sqlite3';
import { MongoClient } from 'mongodb';
import mysql from 'mysql2/promise';

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const defaultSqliteFile = '../plugins/UltimateDonutSmp/data/data.db';

/**
 * The ranked boards. `column` types sort a column of `pvp_stats`; `derived` types are counted out
 * of `pvp_matches`, because a win is a property of a match rather than of a player row.
 */
export const tierLeaderboardTypes = {
  elo: { label: 'ELO', column: 'elo' },
  level: { label: 'Level', column: 'pvp_level' },
  kills: { label: 'Kills', column: 'kills' },
  deaths: { label: 'Deaths', column: 'deaths' },
  streak: { label: 'Streak', column: 'streak' },
  killstreak: { label: 'Highest Kill Streak', column: 'best_streak' },
  wins: { label: 'Wins', derived: 'wins' },
  losses: { label: 'Losses', derived: 'losses' }
};

const statsColumns = `
  s.player_uuid AS uuid,
  p.username AS username,
  s.elo AS elo,
  s.pvp_level AS pvp_level,
  s.pvp_xp AS pvp_xp,
  s.kills AS kills,
  s.deaths AS deaths,
  s.streak AS streak,
  s.best_streak AS best_streak,
  s.arena_joins AS arena_joins,
  s.updated_at AS updated_at
`;

const matchColumns = `
  id, player_one_uuid, player_one_name, player_two_uuid, player_two_name, kit_id,
  winner_uuid, result, started_at, ended_at, one_hits, two_hits, one_crystals, two_crystals,
  one_elo_before, two_elo_before, one_elo_delta, two_elo_delta,
  one_final_health, two_final_health, max_health
`;

export async function createPvpRepository() {
  const type = (process.env.DB_TYPE || 'sqlite').toLowerCase();
  if (type === 'mysql') {
    return createMysqlPvpRepository();
  }
  if (type === 'mongodb' || type === 'mongo') {
    return createMongoPvpRepository();
  }
  return createSqlitePvpRepository();
}

// ── SQLite ───────────────────────────────────────────────────────────────────

function createSqlitePvpRepository() {
  const configured = process.env.SQLITE_FILE || defaultSqliteFile;
  const file = resolveSqlitePath(configured);
  if (!fs.existsSync(file)) {
    throw new Error(
      [
        `SQLite database file was not found: ${file}`,
        `SQLITE_FILE is currently set to: ${configured}`,
        'Point SQLITE_FILE at the plugin database, usually plugins/UltimateDonutSmp/data/data.db.'
      ].join('\n')
    );
  }

  const database = new Database(file, { readonly: true, fileMustExist: true });
  const has = table => tableExists(() =>
    database.prepare("SELECT name FROM sqlite_master WHERE type='table' AND name = ?").get(table)
  );

  return {
    ready: has('pvp_stats'),

    async findStats(input) {
      if (!has('pvp_stats')) {
        return null;
      }
      return normalizeStats(database.prepare(`
        SELECT ${statsColumns}
        FROM pvp_stats s LEFT JOIN players p ON p.uuid = s.player_uuid
        WHERE LOWER(p.username) = LOWER(?) OR s.player_uuid = ?
        LIMIT 1
      `).get(input, input));
    },

    async getRecord(uuid) {
      if (!has('pvp_matches')) {
        return emptyRecord();
      }
      const row = database.prepare(`
        SELECT
          SUM(CASE WHEN result = 'DECIDED' AND winner_uuid = ? THEN 1 ELSE 0 END) AS wins,
          SUM(CASE WHEN result = 'DECIDED' AND winner_uuid IS NOT NULL AND winner_uuid <> ? THEN 1 ELSE 0 END) AS losses,
          SUM(CASE WHEN result <> 'DECIDED' THEN 1 ELSE 0 END) AS draws
        FROM pvp_matches
        WHERE player_one_uuid = ? OR player_two_uuid = ?
      `).get(uuid, uuid, uuid, uuid);
      return normalizeRecord(row);
    },

    async getTierLeaderboard(typeKey, limit) {
      const type = tierLeaderboardTypes[typeKey];
      if (!type) {
        return [];
      }
      if (type.derived) {
        if (!has('pvp_matches')) {
          return [];
        }
        return database.prepare(derivedLeaderboardSql(type.derived)).all(limit).map(normalizeEntry);
      }
      if (!has('pvp_stats')) {
        return [];
      }
      return database.prepare(`
        SELECT s.player_uuid AS uuid, p.username AS username, s.${type.column} AS value
        FROM pvp_stats s LEFT JOIN players p ON p.uuid = s.player_uuid
        ORDER BY value DESC, LOWER(COALESCE(p.username, s.player_uuid)) ASC
        LIMIT ?
      `).all(limit).map(normalizeEntry);
    },

    async getLatestMatchId() {
      if (!has('pvp_matches')) {
        return 0;
      }
      const row = database.prepare('SELECT MAX(id) AS id FROM pvp_matches').get();
      return Number(row?.id || 0);
    },

    async getMatchesAfter(afterId, limit) {
      if (!has('pvp_matches')) {
        return [];
      }
      return database.prepare(`
        SELECT ${matchColumns} FROM pvp_matches WHERE id > ? ORDER BY id ASC LIMIT ?
      `).all(afterId, limit).map(normalizeMatch);
    },

    async findSyncCode(code) {
      if (!has('pvp_sync_codes')) {
        return null;
      }
      const row = database.prepare(`
        SELECT code, player_uuid, player_name, expires_at FROM pvp_sync_codes WHERE LOWER(code) = LOWER(?)
      `).get(code);
      return normalizeSyncCode(row);
    },

    close() {
      database.close();
    }
  };
}

function resolveSqlitePath(configured) {
  const value = String(configured || defaultSqliteFile).trim();
  if (value.startsWith('file://')) {
    return fileURLToPath(value);
  }
  return path.isAbsolute(value) ? value : path.resolve(projectRoot, value);
}

function tableExists(lookup) {
  try {
    return Boolean(lookup());
  } catch {
    return false;
  }
}

/**
 * Wins and losses counted over matches.
 *
 * A loss is any decided match a player was in and did not win, which needs both sides of the row
 * flattened into one column first. That is what the union is doing.
 */
function derivedLeaderboardSql(kind) {
  if (kind === 'wins') {
    return `
      SELECT m.winner_uuid AS uuid, p.username AS username, COUNT(*) AS value
      FROM pvp_matches m LEFT JOIN players p ON p.uuid = m.winner_uuid
      WHERE m.result = 'DECIDED' AND m.winner_uuid IS NOT NULL
      GROUP BY m.winner_uuid, p.username
      ORDER BY value DESC
      LIMIT ?
    `;
  }

  return `
    SELECT t.uuid AS uuid, p.username AS username, COUNT(*) AS value
    FROM (
      SELECT player_one_uuid AS uuid, winner_uuid, result FROM pvp_matches
      UNION ALL
      SELECT player_two_uuid AS uuid, winner_uuid, result FROM pvp_matches
    ) t
    LEFT JOIN players p ON p.uuid = t.uuid
    WHERE t.result = 'DECIDED' AND t.winner_uuid IS NOT NULL AND t.uuid <> t.winner_uuid
    GROUP BY t.uuid, p.username
    ORDER BY value DESC
    LIMIT ?
  `;
}

// ── MySQL ────────────────────────────────────────────────────────────────────

async function createMysqlPvpRepository() {
  const pool = mysql.createPool({
    host: process.env.MYSQL_HOST || 'localhost',
    port: Number(process.env.MYSQL_PORT || 3306),
    database: process.env.MYSQL_DATABASE || 'ultimatedonutsmp',
    user: process.env.MYSQL_USER || 'root',
    password: process.env.MYSQL_PASSWORD || '',
    waitForConnections: true,
    connectionLimit: 5
  });

  const query = async (sql, params = []) => {
    try {
      const [rows] = await pool.execute(sql, params);
      return rows;
    } catch (error) {
      if (error?.code === 'ER_NO_SUCH_TABLE') {
        return [];
      }
      throw error;
    }
  };

  return {
    ready: true,

    async findStats(input) {
      const rows = await query(`
        SELECT ${statsColumns}
        FROM pvp_stats s LEFT JOIN players p ON p.uuid = s.player_uuid
        WHERE LOWER(p.username) = LOWER(?) OR s.player_uuid = ?
        LIMIT 1
      `, [input, input]);
      return normalizeStats(rows[0]);
    },

    async getRecord(uuid) {
      const rows = await query(`
        SELECT
          SUM(CASE WHEN result = 'DECIDED' AND winner_uuid = ? THEN 1 ELSE 0 END) AS wins,
          SUM(CASE WHEN result = 'DECIDED' AND winner_uuid IS NOT NULL AND winner_uuid <> ? THEN 1 ELSE 0 END) AS losses,
          SUM(CASE WHEN result <> 'DECIDED' THEN 1 ELSE 0 END) AS draws
        FROM pvp_matches
        WHERE player_one_uuid = ? OR player_two_uuid = ?
      `, [uuid, uuid, uuid, uuid]);
      return normalizeRecord(rows[0]);
    },

    async getTierLeaderboard(typeKey, limit) {
      const type = tierLeaderboardTypes[typeKey];
      if (!type) {
        return [];
      }
      if (type.derived) {
        return (await query(derivedLeaderboardSql(type.derived), [limit])).map(normalizeEntry);
      }
      const rows = await query(`
        SELECT s.player_uuid AS uuid, p.username AS username, s.${type.column} AS value
        FROM pvp_stats s LEFT JOIN players p ON p.uuid = s.player_uuid
        ORDER BY value DESC, LOWER(COALESCE(p.username, s.player_uuid)) ASC
        LIMIT ?
      `, [limit]);
      return rows.map(normalizeEntry);
    },

    async getLatestMatchId() {
      const rows = await query('SELECT MAX(id) AS id FROM pvp_matches');
      return Number(rows[0]?.id || 0);
    },

    async getMatchesAfter(afterId, limit) {
      const rows = await query(
        `SELECT ${matchColumns} FROM pvp_matches WHERE id > ? ORDER BY id ASC LIMIT ?`,
        [afterId, limit]
      );
      return rows.map(normalizeMatch);
    },

    async findSyncCode(code) {
      const rows = await query(
        'SELECT code, player_uuid, player_name, expires_at FROM pvp_sync_codes WHERE LOWER(code) = LOWER(?)',
        [code]
      );
      return normalizeSyncCode(rows[0]);
    },

    close() {
      return pool.end();
    }
  };
}

// ── MongoDB ──────────────────────────────────────────────────────────────────

async function createMongoPvpRepository() {
  const client = new MongoClient(process.env.MONGODB_URI || 'mongodb://localhost:27017');
  await client.connect();

  const database = client.db(process.env.MONGODB_DATABASE || 'ultimatedonutsmp');
  const stats = database.collection('pvp_stats');
  const matches = database.collection('pvp_matches');
  const codes = database.collection('pvp_sync_codes');
  const players = database.collection(process.env.MONGODB_PLAYERS_COLLECTION || 'players');

  const usernameFor = async uuid => {
    const player = await players.findOne({ uuid }, { projection: { username: 1 } });
    return player?.username ?? null;
  };

  const withUsernames = async rows => {
    return Promise.all(rows.map(async row => ({
      ...row,
      username: row.username ?? await usernameFor(row.uuid)
    })));
  };

  return {
    ready: true,

    async findStats(input) {
      const player = await players.findOne(
        { username: { $regex: `^${escapeRegex(input)}$`, $options: 'i' } },
        { projection: { uuid: 1, username: 1 } }
      );
      const uuid = player?.uuid ?? input;
      const row = await stats.findOne({ player_uuid: uuid });
      if (!row) {
        return null;
      }
      return normalizeStats({ ...row, uuid: row.player_uuid, username: player?.username ?? await usernameFor(uuid) });
    },

    async getRecord(uuid) {
      const rows = await matches
        .find({ $or: [{ player_one_uuid: uuid }, { player_two_uuid: uuid }] })
        .project({ result: 1, winner_uuid: 1 })
        .toArray();

      let wins = 0;
      let losses = 0;
      let draws = 0;
      for (const row of rows) {
        if (row.result !== 'DECIDED') {
          draws += 1;
        } else if (row.winner_uuid === uuid) {
          wins += 1;
        } else if (row.winner_uuid) {
          losses += 1;
        }
      }
      return { wins, losses, draws };
    },

    async getTierLeaderboard(typeKey, limit) {
      const type = tierLeaderboardTypes[typeKey];
      if (!type) {
        return [];
      }

      if (type.derived) {
        const pipeline = type.derived === 'wins'
          ? [
            { $match: { result: 'DECIDED', winner_uuid: { $ne: null } } },
            { $group: { _id: '$winner_uuid', value: { $sum: 1 } } }
          ]
          : [
            { $match: { result: 'DECIDED', winner_uuid: { $ne: null } } },
            { $project: { uuid: ['$player_one_uuid', '$player_two_uuid'], winner_uuid: 1 } },
            { $unwind: '$uuid' },
            { $match: { $expr: { $ne: ['$uuid', '$winner_uuid'] } } },
            { $group: { _id: '$uuid', value: { $sum: 1 } } }
          ];
        pipeline.push({ $sort: { value: -1 } }, { $limit: limit });

        const rows = await matches.aggregate(pipeline).toArray();
        return withUsernames(rows.map(row => ({ uuid: row._id, value: row.value }))).then(all => all.map(normalizeEntry));
      }

      const rows = await stats
        .find({})
        .sort({ [type.column]: -1 })
        .limit(limit)
        .toArray();
      return withUsernames(rows.map(row => ({ uuid: row.player_uuid, value: row[type.column] })))
        .then(all => all.map(normalizeEntry));
    },

    async getLatestMatchId() {
      const row = await matches.find({}).sort({ id: -1 }).limit(1).next();
      return Number(row?.id || 0);
    },

    async getMatchesAfter(afterId, limit) {
      const rows = await matches.find({ id: { $gt: afterId } }).sort({ id: 1 }).limit(limit).toArray();
      return rows.map(normalizeMatch);
    },

    async findSyncCode(code) {
      const row = await codes.findOne({ code: String(code).toLowerCase() });
      return normalizeSyncCode(row);
    },

    close() {
      return client.close();
    }
  };
}

function escapeRegex(value) {
  return String(value).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

// ── Row shaping ──────────────────────────────────────────────────────────────

function normalizeStats(row) {
  if (!row) {
    return null;
  }
  return {
    uuid: String(row.uuid ?? row.player_uuid),
    username: row.username || 'Unknown',
    elo: Number(row.elo || 0),
    level: Number(row.pvp_level || 1),
    xp: Number(row.pvp_xp || 0),
    kills: Number(row.kills || 0),
    deaths: Number(row.deaths || 0),
    streak: Number(row.streak || 0),
    bestStreak: Number(row.best_streak || 0),
    arenaJoins: Number(row.arena_joins || 0),
    updatedAt: Number(row.updated_at || 0)
  };
}

function normalizeEntry(row) {
  if (!row) {
    return null;
  }
  return {
    uuid: String(row.uuid),
    username: row.username || String(row.uuid).slice(0, 8),
    value: Number(row.value || 0)
  };
}

function normalizeRecord(row) {
  if (!row) {
    return emptyRecord();
  }
  return {
    wins: Number(row.wins || 0),
    losses: Number(row.losses || 0),
    draws: Number(row.draws || 0)
  };
}

function emptyRecord() {
  return { wins: 0, losses: 0, draws: 0 };
}

function normalizeSyncCode(row) {
  if (!row) {
    return null;
  }
  return {
    code: String(row.code).toLowerCase(),
    uuid: String(row.player_uuid),
    name: row.player_name || null,
    expiresAt: Number(row.expires_at || 0)
  };
}

function normalizeMatch(row) {
  if (!row) {
    return null;
  }
  const maxHealth = Number(row.max_health || 20);
  return {
    id: Number(row.id || 0),
    kitId: row.kit_id || null,
    result: row.result || 'ABORTED',
    winnerUuid: row.winner_uuid || null,
    startedAt: Number(row.started_at || 0),
    endedAt: Number(row.ended_at || 0),
    durationSeconds: Math.max(0, Math.floor((Number(row.ended_at || 0) - Number(row.started_at || 0)) / 1000)),
    maxHealth,
    first: {
      uuid: String(row.player_one_uuid),
      name: row.player_one_name || 'Unknown',
      hits: Number(row.one_hits || 0),
      crystals: Number(row.one_crystals || 0),
      eloBefore: Number(row.one_elo_before || 0),
      eloDelta: Number(row.one_elo_delta || 0),
      finalHealth: Number(row.one_final_health || 0)
    },
    second: {
      uuid: String(row.player_two_uuid),
      name: row.player_two_name || 'Unknown',
      hits: Number(row.two_hits || 0),
      crystals: Number(row.two_crystals || 0),
      eloBefore: Number(row.two_elo_before || 0),
      eloDelta: Number(row.two_elo_delta || 0),
      finalHealth: Number(row.two_final_health || 0)
    }
  };
}
