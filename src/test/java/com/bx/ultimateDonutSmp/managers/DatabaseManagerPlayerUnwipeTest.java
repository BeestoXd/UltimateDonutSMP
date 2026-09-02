package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PlayerWipeArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerPlayerUnwipeTest {

    private static final UUID TARGET = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BYSTANDER = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void restoringABackupPutsTheWipedPlayerBackExactlyAsTheyWere() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseManager manager = managerWithConnection(connection);
            createSchema(connection);
            seedData(connection);

            PlayerWipeArchive archive = manager.capturePlayerWipeArchive(TARGET, "Target", "Moderator");
            manager.resetForPlayerWipe(TARGET, 1000D);

            assertEquals(1000D, money(connection, TARGET), 0.001D);
            assertEquals(0, countWhere(connection, "homes", "player_uuid = '" + TARGET + "'"));

            DatabaseManager.PlayerWipeResult restored = manager.restorePlayerWipeArchive(archive);

            assertEquals(250D, money(connection, TARGET), 0.001D);
            assertEquals(7, queryInt(connection, "SELECT kills FROM players WHERE uuid = '" + TARGET + "'"));
            assertEquals(40, queryInt(connection, "SELECT shards FROM players WHERE uuid = '" + TARGET + "'"));
            assertEquals(60, queryInt(connection,
                    "SELECT keyall_remaining_seconds FROM players WHERE uuid = '" + TARGET + "'"));

            assertEquals("base", queryString(connection,
                    "SELECT home_name FROM homes WHERE player_uuid = '" + TARGET + "'"));
            assertEquals(1, countWhere(connection, "shop_favorites", "player_uuid = '" + TARGET + "'"));
            assertEquals(2, countWhere(connection, "ender_chest_items", "player_uuid = '" + TARGET + "'"));
            assertEquals("mending-book", queryString(connection,
                    "SELECT item_data FROM ender_chest_items WHERE player_uuid = '" + TARGET + "' AND slot = 0"));

            // Rows the player owns through either of two columns come back on both sides.
            assertEquals(2, count(connection, "bounties"));
            assertEquals(2, count(connection, "player_friends"));

            assertTrue(restored.total() > 0);
            assertEquals(1, restored.affected("stats"));
            assertEquals(1, restored.affected("homes"));
            assertEquals(2, restored.affected("ender_chest"));

            // The bystander is untouched by both halves of the round trip.
            assertEquals(500D, money(connection, BYSTANDER), 0.001D);
            assertEquals(1, countWhere(connection, "homes", "player_uuid = '" + BYSTANDER + "'"));
        }
    }

    @Test
    void restoringDropsAnythingEarnedAfterTheWipe() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseManager manager = managerWithConnection(connection);
            createSchema(connection);
            seedData(connection);

            PlayerWipeArchive archive = manager.capturePlayerWipeArchive(TARGET, "Target", "Moderator");
            manager.resetForPlayerWipe(TARGET, 1000D);

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO homes VALUES ('" + TARGET + "', 'after-the-wipe')");
            }

            manager.restorePlayerWipeArchive(archive);

            assertEquals(1, countWhere(connection, "homes", "player_uuid = '" + TARGET + "'"));
            assertEquals("base", queryString(connection,
                    "SELECT home_name FROM homes WHERE player_uuid = '" + TARGET + "'"));
        }
    }

    @Test
    void aMembershipOfADisbandedTeamIsNotRestored() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseManager manager = managerWithConnection(connection);
            createSchema(connection);
            seedData(connection);

            PlayerWipeArchive archive = manager.capturePlayerWipeArchive(TARGET, "Target", "Moderator");
            manager.resetForPlayerWipe(TARGET, 1000D);

            // Wiping the leader disbands the team, so the team row is gone by restore time.
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM teams WHERE name = 'donuts'");
            }

            manager.restorePlayerWipeArchive(archive);

            assertEquals(0, count(connection, "team_members"));
        }
    }

    @Test
    void aBackupSurvivesBeingWrittenToDiskAndReadBack(@TempDir Path directory) throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseManager manager = managerWithConnection(connection);
            createSchema(connection);
            seedData(connection);

            PlayerWipeArchive archive = manager.capturePlayerWipeArchive(TARGET, "Target_1", "Moderator");
            File file = directory.resolve("Target_1_" + TARGET + "_2026-09-02_10-00-00.yml").toFile();
            archive.save(file);

            PlayerWipeArchive reloaded = PlayerWipeArchive.load(file);
            assertEquals(TARGET, reloaded.playerUuid());
            assertEquals("Target_1", reloaded.playerName());
            assertEquals("Moderator", reloaded.wipedBy());
            assertEquals(archive.wipedAt(), reloaded.wipedAt());
            assertEquals(archive.rowCount(), reloaded.rowCount());

            // A null column has to survive the trip, or the restore would silently invent a value.
            List<Object> firstLog = reloaded.tables().get("player_logs").rows().get(0);
            assertNull(firstLog.get(reloaded.tables().get("player_logs").columns().indexOf("details")));

            manager.resetForPlayerWipe(TARGET, 1000D);
            manager.restorePlayerWipeArchive(reloaded);

            assertEquals(250D, money(connection, TARGET), 0.001D);
            assertEquals("mending-book", queryString(connection,
                    "SELECT item_data FROM ender_chest_items WHERE player_uuid = '" + TARGET + "' AND slot = 0"));
            assertNull(queryString(connection, "SELECT details FROM player_logs WHERE player_uuid = '" + TARGET + "'"));
        }
    }

    private DatabaseManager managerWithConnection(Connection connection) throws Exception {
        DatabaseManager manager = new DatabaseManager(null);
        Field connectionField = DatabaseManager.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(manager, connection);
        return manager;
    }

    private void createSchema(Connection connection) throws Exception {
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
                        shard_booster_expiry INTEGER,
                        mob_spawn_disabled_until BIGINT,
                        phantom_disabled_until BIGINT
                    )
                    """);
            statement.execute("CREATE TABLE homes (player_uuid TEXT, home_name TEXT)");
            statement.execute("CREATE TABLE teams (name TEXT PRIMARY KEY, leader_uuid TEXT)");
            statement.execute("CREATE TABLE team_members (player_uuid TEXT PRIMARY KEY, team_name TEXT)");
            statement.execute("CREATE TABLE ender_chest_profiles (player_uuid TEXT)");
            statement.execute("CREATE TABLE ender_chest_items (player_uuid TEXT, slot INTEGER, item_data TEXT)");
            statement.execute("CREATE TABLE player_crate_keys (player_uuid TEXT)");
            statement.execute("CREATE TABLE sell_history (player_uuid TEXT)");
            statement.execute("CREATE TABLE sell_progress (player_uuid TEXT)");
            statement.execute("CREATE TABLE sell_summary_players (player_uuid TEXT)");
            statement.execute("CREATE TABLE shop_favorites (player_uuid TEXT, favorite_id TEXT)");
            statement.execute("CREATE TABLE player_logs (player_uuid TEXT, details TEXT)");
            statement.execute("CREATE TABLE bounties (target_uuid TEXT, placer_uuid TEXT)");
            statement.execute("CREATE TABLE player_friends (follower_uuid TEXT, followed_uuid TEXT)");
            statement.execute("CREATE TABLE player_ignores (owner_uuid TEXT, ignored_uuid TEXT)");
            statement.execute("CREATE TABLE duel_stats (player_uuid TEXT)");
            statement.execute("CREATE TABLE duel_matches (player_one_uuid TEXT, player_two_uuid TEXT)");
            statement.execute("CREATE TABLE ffa_matches (player_one_uuid TEXT, player_two_uuid TEXT)");
        }
    }

    private void seedData(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(playerRow(TARGET, "Target", 250, 40, 7));
            statement.executeUpdate(playerRow(BYSTANDER, "Bystander", 500, 25, 9));

            statement.executeUpdate("INSERT INTO homes VALUES ('" + TARGET + "', 'base')");
            statement.executeUpdate("INSERT INTO homes VALUES ('" + BYSTANDER + "', 'base')");
            statement.executeUpdate("INSERT INTO teams VALUES ('donuts', '" + TARGET + "')");
            statement.executeUpdate("INSERT INTO team_members VALUES ('" + TARGET + "', 'donuts')");
            statement.executeUpdate("INSERT INTO ender_chest_items VALUES ('" + TARGET + "', 0, 'mending-book')");
            statement.executeUpdate("INSERT INTO ender_chest_items VALUES ('" + TARGET + "', 1, 'netherite-pick')");
            statement.executeUpdate("INSERT INTO shop_favorites VALUES ('" + TARGET + "', 'diamond')");
            statement.executeUpdate("INSERT INTO player_logs VALUES ('" + TARGET + "', NULL)");
            statement.executeUpdate("INSERT INTO bounties VALUES ('" + TARGET + "', '" + BYSTANDER + "')");
            statement.executeUpdate("INSERT INTO bounties VALUES ('" + BYSTANDER + "', '" + TARGET + "')");
            statement.executeUpdate("INSERT INTO player_friends VALUES ('" + TARGET + "', '" + BYSTANDER + "')");
            statement.executeUpdate("INSERT INTO player_friends VALUES ('" + BYSTANDER + "', '" + TARGET + "')");
        }
    }

    private String playerRow(UUID uuid, String username, int money, int shards, int kills) {
        return "INSERT INTO players VALUES ('" + uuid + "', '" + username + "', " + money + ", " + shards + ", "
                + kills + ", 3, 900, 12, 13, 14, 4, 8, 200, 300, 1, 60, 12345, 0, 0)";
    }

    private double money(Connection connection, UUID uuid) throws Exception {
        return queryDouble(connection, "SELECT money FROM players WHERE uuid = '" + uuid + "'");
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

    private String queryString(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getString(1) : null;
        }
    }
}
