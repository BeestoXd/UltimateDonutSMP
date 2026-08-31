import path from 'node:path';
import fs from 'node:fs';
import { fileURLToPath } from 'node:url';
import Database from 'better-sqlite3';

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');

/**
 * Where the bot keeps what it owns: Discord account links and how far the match watcher has read.
 *
 * This is deliberately the bot's own SQLite file rather than a second writer on the plugin
 * database. The plugin issues sync codes and never has to know a Discord id, so the bot can stay
 * a read-only reader of the server's data and still own the half of the relationship that is
 * genuinely its own.
 */
export function createLinkStore() {
  const configured = process.env.LINKS_FILE || 'data/links.db';
  const file = path.isAbsolute(configured) ? configured : path.resolve(projectRoot, configured);
  fs.mkdirSync(path.dirname(file), { recursive: true });

  const database = new Database(file);
  database.pragma('journal_mode = WAL');
  database.exec(`
    CREATE TABLE IF NOT EXISTS links (
      discord_id TEXT PRIMARY KEY,
      player_uuid TEXT NOT NULL UNIQUE,
      player_name TEXT,
      code TEXT,
      linked_at INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS state (
      key TEXT PRIMARY KEY,
      value TEXT
    );
    CREATE UNIQUE INDEX IF NOT EXISTS links_code ON links(code);
  `);

  return {
    getLink(discordId) {
      return database
        .prepare('SELECT discord_id, player_uuid, player_name, linked_at FROM links WHERE discord_id = ?')
        .get(String(discordId)) ?? null;
    },

    getLinkByUuid(uuid) {
      return database
        .prepare('SELECT discord_id, player_uuid, player_name, linked_at FROM links WHERE player_uuid = ?')
        .get(String(uuid)) ?? null;
    },

    /** True once a code has been spent, so the same one cannot be claimed twice. */
    isCodeUsed(code) {
      return Boolean(
        database.prepare('SELECT 1 FROM links WHERE code = ?').get(String(code).toLowerCase())
      );
    },

    /**
     * Claims an account for a Discord user.
     *
     * One row per Discord id and one per Minecraft account, so relinking either side replaces the
     * old pair instead of leaving two rows that disagree about who owns what.
     */
    link(discordId, uuid, name, code) {
      const now = Date.now();
      const write = database.transaction(() => {
        database.prepare('DELETE FROM links WHERE discord_id = ? OR player_uuid = ?')
          .run(String(discordId), String(uuid));
        database.prepare(`
          INSERT INTO links (discord_id, player_uuid, player_name, code, linked_at)
          VALUES (?, ?, ?, ?, ?)
        `).run(String(discordId), String(uuid), name ?? null, String(code).toLowerCase(), now);
      });
      write();
      return { discord_id: String(discordId), player_uuid: String(uuid), player_name: name, linked_at: now };
    },

    unlink(discordId) {
      return database.prepare('DELETE FROM links WHERE discord_id = ?').run(String(discordId)).changes > 0;
    },

    getState(key, fallback = null) {
      const row = database.prepare('SELECT value FROM state WHERE key = ?').get(String(key));
      return row ? row.value : fallback;
    },

    setState(key, value) {
      database.prepare(`
        INSERT INTO state (key, value) VALUES (?, ?)
        ON CONFLICT(key) DO UPDATE SET value = excluded.value
      `).run(String(key), String(value));
    },

    close() {
      database.close();
    }
  };
}
