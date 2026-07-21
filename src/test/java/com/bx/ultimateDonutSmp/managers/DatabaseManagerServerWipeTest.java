package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerServerWipeTest {

    @TempDir
    Path tempDirectory;

    @Test
    void previewBackupAndResetPreserveModerationAndPreferences() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseManager manager = managerWithConnection(connection);
            createSchema(connection, true);
            seedData(connection);

            DatabaseManager.ServerWipePreview preview = manager.previewServerWipe(Set.of("resource"));
            assertEquals(1, preview.count("players"));
            assertEquals(1, preview.count("homes"));
            assertEquals(1, preview.count("spawners"));
            assertEquals(1, preview.count("crate_blocks"));
            assertEquals(500D, queryDouble(connection, "SELECT money FROM players"), 0.001D);

            Path backup = tempDirectory.resolve("database.sql");
            manager.writePortableBackup(backup);
            String sql = Files.readString(backup);
            assertTrue(sql.contains("CREATE TABLE players"));
            assertTrue(sql.contains("INSERT INTO \"punishments\""));
            assertTrue(sql.contains("'keep-this-ban'"));

            manager.resetForServerWipe(1000D, Set.of("resource"), "wipe-1");

            assertEquals(1000D, queryDouble(connection, "SELECT money FROM players"), 0.001D);
            assertEquals(0, queryInt(connection, "SELECT shards FROM players"));
            assertEquals(0, queryInt(connection, "SELECT kills FROM players"));
            assertEquals(1, queryInt(connection, "SELECT scoreboard_visible FROM players"));
            assertEquals(1, count(connection, "punishments"));
            assertEquals(1, count(connection, "player_ignores"));
            assertEquals(1, count(connection, "warps"));
            assertEquals(0, count(connection, "homes"));
            assertEquals(0, count(connection, "player_crate_keys"));
            assertEquals(0, countWhere(connection, "spawners", "world = 'resource'"));
            assertEquals(1, countWhere(connection, "spawners", "world = 'survival'"));
            assertEquals(0, countWhere(connection, "crate_blocks", "world = 'resource'"));
            assertEquals(1, countWhere(connection, "crate_blocks", "world = 'survival'"));
            assertTrue(manager.isServerWipeCommitted("wipe-1"));
        }
    }

    @Test
    void resetRollsBackWhenCommitMarkerCannotBeWritten() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseManager manager = managerWithConnection(connection);
            createSchema(connection, false);
            seedData(connection);

            assertThrows(
                    java.sql.SQLException.class,
                    () -> manager.resetForServerWipe(1000D, Set.of("resource"), "wipe-failure")
            );

            assertEquals(500D, queryDouble(connection, "SELECT money FROM players"), 0.001D);
            assertEquals(25, queryInt(connection, "SELECT shards FROM players"));
            assertEquals(1, count(connection, "homes"));
            assertEquals(1, countWhere(connection, "spawners", "world = 'resource'"));
        }
    }

    private DatabaseManager managerWithConnection(Connection connection) throws Exception {
        DatabaseManager manager = new DatabaseManager(null);
        Field connectionField = DatabaseManager.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(manager, connection);
        return manager;
    }

    private void createSchema(Connection connection, boolean includeCommitTable) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE players (
                        uuid TEXT PRIMARY KEY,
                        username TEXT,
                        money REAL,
                        shards INTEGER,
                        kills INTEGER,
                        deaths INTEGER,
                        playtime_seconds INTEGER,
                        blocks_placed INTEGER,
                        blocks_broken INTEGER,
                        mobs_killed INTEGER,
                        kill_streak INTEGER,
                        highest_kill_streak INTEGER,
                        money_spent REAL,
                        money_made REAL,
                        scoreboard_visible INTEGER,
                        keyall_remaining_seconds INTEGER,
                        shard_booster_expiry INTEGER
                    )
                    """);
            statement.execute("CREATE TABLE homes (owner_uuid TEXT, name TEXT)");
            statement.execute("CREATE TABLE teams (id TEXT)");
            statement.execute("CREATE TABLE team_members (team_id TEXT, player_uuid TEXT)");
            statement.execute("CREATE TABLE bounties (target_uuid TEXT)");
            statement.execute("CREATE TABLE sell_history (player_uuid TEXT)");
            statement.execute("CREATE TABLE sell_progress (player_uuid TEXT)");
            statement.execute("CREATE TABLE player_crate_keys (player_uuid TEXT)");
            statement.execute("CREATE TABLE ender_chest_profiles (player_uuid TEXT)");
            statement.execute("CREATE TABLE ender_chest_items (player_uuid TEXT)");
            statement.execute("CREATE TABLE punishments (id TEXT, reason TEXT)");
            statement.execute("CREATE TABLE player_ignores (owner_uuid TEXT, ignored_uuid TEXT)");
            statement.execute("CREATE TABLE warps (name TEXT)");
            statement.execute("CREATE TABLE spawners (id INTEGER PRIMARY KEY, world TEXT)");
            statement.execute("CREATE TABLE spawner_loot (spawner_id INTEGER)");
            statement.execute("CREATE TABLE crate_blocks (world TEXT)");
            if (includeCommitTable) {
                statement.execute("""
                        CREATE TABLE server_wipe_commits (
                            wipe_id TEXT PRIMARY KEY,
                            committed_at INTEGER NOT NULL
                        )
                        """);
            }
        }
    }

    private void seedData(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO players VALUES (
                        'player-1', 'Player', 500, 25, 9, 3, 900, 12, 13, 14, 4, 8, 200, 300, 1, 60, 12345
                    )
                    """);
            statement.executeUpdate("INSERT INTO homes VALUES ('player-1', 'base')");
            statement.executeUpdate("INSERT INTO player_crate_keys VALUES ('player-1')");
            statement.executeUpdate("INSERT INTO punishments VALUES ('punishment-1', 'keep-this-ban')");
            statement.executeUpdate("INSERT INTO player_ignores VALUES ('player-1', 'player-2')");
            statement.executeUpdate("INSERT INTO warps VALUES ('spawn')");
            statement.executeUpdate("INSERT INTO spawners VALUES (1, 'resource')");
            statement.executeUpdate("INSERT INTO spawners VALUES (2, 'survival')");
            statement.executeUpdate("INSERT INTO spawner_loot VALUES (1)");
            statement.executeUpdate("INSERT INTO spawner_loot VALUES (2)");
            statement.executeUpdate("INSERT INTO crate_blocks VALUES ('resource')");
            statement.executeUpdate("INSERT INTO crate_blocks VALUES ('survival')");
        }
    }

    private int count(Connection connection, String table) throws Exception {
        return queryInt(connection, "SELECT COUNT(*) FROM " + table);
    }

    private int countWhere(Connection connection, String table, String predicate) throws Exception {
        return queryInt(connection, "SELECT COUNT(*) FROM " + table + " WHERE " + predicate);
    }

    private int queryInt(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private double queryDouble(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getDouble(1) : 0D;
        }
    }
}
