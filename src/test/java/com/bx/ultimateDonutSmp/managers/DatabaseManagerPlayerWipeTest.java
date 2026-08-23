package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseManagerPlayerWipeTest {

    private static final UUID TARGET = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BYSTANDER = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void wipeClearsOneOwnerAndLeavesEverybodyElseAlone() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseManager manager = managerWithConnection(connection);
            createSchema(connection, true);
            seedData(connection);

            DatabaseManager.PlayerWipePreview preview = manager.previewPlayerWipe(TARGET);
            assertEquals(1, preview.count("stats"));
            assertEquals(1, preview.count("homes"));
            assertEquals(2, preview.count("ender_chest"));
            assertEquals(3, preview.count("sell_records"));
            assertEquals(2, preview.count("bounties"));
            assertEquals(2, preview.count("duels"));

            DatabaseManager.PlayerWipeResult result = manager.resetForPlayerWipe(TARGET, 1000D);
            assertEquals(preview.total(), result.total());

            assertEquals(1000D, money(connection, TARGET), 0.001D);
            assertEquals(0, queryInt(connection, "SELECT kills FROM players WHERE uuid = '" + TARGET + "'"));
            assertEquals(0, queryInt(connection, "SELECT shards FROM players WHERE uuid = '" + TARGET + "'"));
            assertEquals(-1, queryInt(connection,
                    "SELECT keyall_remaining_seconds FROM players WHERE uuid = '" + TARGET + "'"));
            assertEquals(1, queryInt(connection,
                    "SELECT scoreboard_visible FROM players WHERE uuid = '" + TARGET + "'"));

            assertEquals(0, countWhere(connection, "homes", "player_uuid = '" + TARGET + "'"));
            assertEquals(0, countWhere(connection, "shop_favorites", "player_uuid = '" + TARGET + "'"));
            assertEquals(0, count(connection, "ender_chest_items"));
            assertEquals(0, count(connection, "sell_history"));
            assertEquals(0, count(connection, "player_logs"));
            assertEquals(0, count(connection, "bounties"));
            assertEquals(0, count(connection, "player_friends"));
            assertEquals(0, count(connection, "player_ignores"));
            assertEquals(0, count(connection, "duel_matches"));
            assertEquals(0, count(connection, "ffa_matches"));

            // The bystander keeps every one of their own rows.
            assertEquals(500D, money(connection, BYSTANDER), 0.001D);
            assertEquals(9, queryInt(connection, "SELECT kills FROM players WHERE uuid = '" + BYSTANDER + "'"));
            assertEquals(1, countWhere(connection, "homes", "player_uuid = '" + BYSTANDER + "'"));
            assertEquals(1, countWhere(connection, "shop_favorites", "player_uuid = '" + BYSTANDER + "'"));

            // Moderation records and world objects are not player progress.
            assertEquals(1, count(connection, "punishments"));
            assertEquals(1, count(connection, "player_ip_history"));
            assertEquals(1, count(connection, "spawners"));
        }
    }

    @Test
    void wipeRollsBackWhenATableCannotBeCleared() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseManager manager = managerWithConnection(connection);
            createSchema(connection, false);
            seedData(connection);

            assertThrows(
                    java.sql.SQLException.class,
                    () -> manager.resetForPlayerWipe(TARGET, 1000D)
            );

            assertEquals(250D, money(connection, TARGET), 0.001D);
            assertEquals(7, queryInt(connection, "SELECT kills FROM players WHERE uuid = '" + TARGET + "'"));
            assertEquals(1, countWhere(connection, "shop_favorites", "player_uuid = '" + TARGET + "'"));
        }
    }

    private DatabaseManager managerWithConnection(Connection connection) throws Exception {
        DatabaseManager manager = new DatabaseManager(null);
        Field connectionField = DatabaseManager.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(manager, connection);
        return manager;
    }

    private void createSchema(Connection connection, boolean homesAreWipeable) throws Exception {
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
            statement.execute("CREATE TABLE team_members (player_uuid TEXT, team_name TEXT)");
            statement.execute("CREATE TABLE ender_chest_profiles (player_uuid TEXT)");
            statement.execute("CREATE TABLE ender_chest_items (player_uuid TEXT, slot INTEGER)");
            statement.execute("CREATE TABLE player_crate_keys (player_uuid TEXT)");
            statement.execute("CREATE TABLE sell_history (player_uuid TEXT)");
            statement.execute("CREATE TABLE sell_progress (player_uuid TEXT)");
            statement.execute("CREATE TABLE sell_summary_players (player_uuid TEXT)");
            statement.execute("CREATE TABLE shop_favorites (player_uuid TEXT, favorite_id TEXT)");
            statement.execute("CREATE TABLE player_logs (player_uuid TEXT)");
            statement.execute("CREATE TABLE bounties (target_uuid TEXT, placer_uuid TEXT)");
            statement.execute("CREATE TABLE player_friends (follower_uuid TEXT, followed_uuid TEXT)");
            statement.execute("CREATE TABLE player_ignores (owner_uuid TEXT, ignored_uuid TEXT)");
            statement.execute("CREATE TABLE duel_stats (player_uuid TEXT)");
            statement.execute("CREATE TABLE duel_matches (player_one_uuid TEXT, player_two_uuid TEXT)");
            statement.execute("CREATE TABLE ffa_matches (player_one_uuid TEXT, player_two_uuid TEXT)");
            statement.execute("CREATE TABLE punishments (target_uuid TEXT, reason TEXT)");
            statement.execute("CREATE TABLE player_ip_history (player_uuid TEXT, ip_address TEXT)");
            statement.execute("CREATE TABLE spawners (id INTEGER PRIMARY KEY, owner_uuid TEXT)");

            if (!homesAreWipeable) {
                // Homes are cleared first, so blocking them proves the players update that ran
                // before it is rolled back too.
                statement.execute("""
                        CREATE TRIGGER block_home_delete BEFORE DELETE ON homes
                        BEGIN
                            SELECT RAISE(ABORT, 'homes are locked');
                        END
                        """);
            }
        }
    }

    private void seedData(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(playerRow(TARGET, "Target", 250, 40, 7));
            statement.executeUpdate(playerRow(BYSTANDER, "Bystander", 500, 25, 9));

            statement.executeUpdate("INSERT INTO homes VALUES ('" + TARGET + "', 'base')");
            statement.executeUpdate("INSERT INTO homes VALUES ('" + BYSTANDER + "', 'base')");
            statement.executeUpdate("INSERT INTO ender_chest_profiles VALUES ('" + TARGET + "')");
            statement.executeUpdate("INSERT INTO ender_chest_items VALUES ('" + TARGET + "', 0)");
            statement.executeUpdate("INSERT INTO sell_history VALUES ('" + TARGET + "')");
            statement.executeUpdate("INSERT INTO sell_progress VALUES ('" + TARGET + "')");
            statement.executeUpdate("INSERT INTO sell_summary_players VALUES ('" + TARGET + "')");
            statement.executeUpdate("INSERT INTO shop_favorites VALUES ('" + TARGET + "', 'diamond')");
            statement.executeUpdate("INSERT INTO shop_favorites VALUES ('" + BYSTANDER + "', 'diamond')");
            statement.executeUpdate("INSERT INTO player_logs VALUES ('" + TARGET + "')");

            // Rows that reference the target through either of two owner columns.
            statement.executeUpdate("INSERT INTO bounties VALUES ('" + TARGET + "', '" + BYSTANDER + "')");
            statement.executeUpdate("INSERT INTO bounties VALUES ('" + BYSTANDER + "', '" + TARGET + "')");
            statement.executeUpdate("INSERT INTO player_friends VALUES ('" + TARGET + "', '" + BYSTANDER + "')");
            statement.executeUpdate("INSERT INTO player_friends VALUES ('" + BYSTANDER + "', '" + TARGET + "')");
            statement.executeUpdate("INSERT INTO player_ignores VALUES ('" + TARGET + "', '" + BYSTANDER + "')");
            statement.executeUpdate("INSERT INTO duel_stats VALUES ('" + TARGET + "')");
            statement.executeUpdate("INSERT INTO duel_matches VALUES ('" + BYSTANDER + "', '" + TARGET + "')");
            statement.executeUpdate("INSERT INTO ffa_matches VALUES ('" + TARGET + "', '" + BYSTANDER + "')");

            statement.executeUpdate("INSERT INTO punishments VALUES ('" + TARGET + "', 'keep-this-ban')");
            statement.executeUpdate("INSERT INTO player_ip_history VALUES ('" + TARGET + "', '127.0.0.1')");
            statement.executeUpdate("INSERT INTO spawners VALUES (1, '" + TARGET + "')");
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
}
