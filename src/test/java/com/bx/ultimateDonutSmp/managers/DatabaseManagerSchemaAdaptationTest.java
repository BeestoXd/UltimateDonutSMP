package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerSchemaAdaptationTest {

    @Test
    void adaptSchemaSqlEscapesRowsAndConvertsTypesInMySqlMode() throws Exception {
        DatabaseManager manager = new DatabaseManager(null);
        Field typeField = DatabaseManager.class.getDeclaredField("databaseType");
        typeField.setAccessible(true);
        typeField.set(manager, DatabaseManager.DatabaseType.MYSQL);

        String rawSql = "CREATE TABLE IF NOT EXISTS ender_chest_profiles (" +
                " player_uuid TEXT PRIMARY KEY," +
                " rows INTEGER DEFAULT 6," +
                " updated_at INTEGER DEFAULT 0" +
                ")";

        String adapted = manager.adaptSchemaSql(rawSql);
        assertTrue(adapted.contains("`rows`"), "adapted SQL should escape 'rows' keyword with backticks");
        assertTrue(adapted.contains("BIGINT"), "adapted SQL should convert INTEGER to BIGINT");
        assertTrue(adapted.contains("VARCHAR(191)"), "adapted SQL should convert TEXT to VARCHAR(191)");
    }

    @Test
    void adaptSchemaSqlConvertsWipeIdAndLogTypeToVarcharAndPreservesBlacklistedTextFields() throws Exception {
        DatabaseManager manager = new DatabaseManager(null);
        Field typeField = DatabaseManager.class.getDeclaredField("databaseType");
        typeField.setAccessible(true);
        typeField.set(manager, DatabaseManager.DatabaseType.MYSQL);

        String wipeSql = "CREATE TABLE IF NOT EXISTS server_wipe_commits (" +
                " wipe_id TEXT PRIMARY KEY," +
                " committed_at INTEGER NOT NULL" +
                ")";
        String adaptedWipe = manager.adaptSchemaSql(wipeSql);
        assertTrue(adaptedWipe.contains("wipe_id VARCHAR(191) PRIMARY KEY"), "adapted SQL should convert wipe_id TEXT to VARCHAR(191)");

        String logSql = "CREATE TABLE IF NOT EXISTS player_logs (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " player_uuid TEXT NOT NULL," +
                " player_name TEXT NOT NULL," +
                " category TEXT NOT NULL," +
                " log_type TEXT NOT NULL," +
                " details TEXT NOT NULL," +
                " timestamp INTEGER NOT NULL" +
                ")";
        String adaptedLog = manager.adaptSchemaSql(logSql);
        assertTrue(adaptedLog.contains("log_type VARCHAR(191) NOT NULL"), "adapted SQL should convert log_type TEXT to VARCHAR(191)");
        assertTrue(adaptedLog.contains("details TEXT NOT NULL"), "adapted SQL should preserve details as TEXT");
        assertTrue(adaptedLog.contains("player_uuid VARCHAR(191) NOT NULL"), "adapted SQL should convert player_uuid TEXT to VARCHAR(191)");

        String spawnerSql = "CREATE TABLE IF NOT EXISTS spawners (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " world TEXT NOT NULL," +
                " disabled_loot_keys TEXT DEFAULT ''" +
                ")";
        String adaptedSpawner = manager.adaptSchemaSql(spawnerSql);
        assertTrue(adaptedSpawner.contains("world VARCHAR(191) NOT NULL"), "adapted SQL should convert world TEXT to VARCHAR(191)");
        assertTrue(adaptedSpawner.contains("disabled_loot_keys TEXT DEFAULT ''"), "adapted SQL should preserve disabled_loot_keys as TEXT");

        String backtickSql = "CREATE TABLE IF NOT EXISTS demo (`wipe_id` TEXT PRIMARY KEY)";
        String adaptedBacktick = manager.adaptSchemaSql(backtickSql);
        assertTrue(adaptedBacktick.contains("`wipe_id` VARCHAR(191) PRIMARY KEY"), "adapted SQL should handle backticked identifiers");
    }

    @Test
    void enderChestProfilesCanBeCreatedAndQueriedInDatabase() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {

            DatabaseManager manager = new DatabaseManager(null);
            Field connectionField = DatabaseManager.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            connectionField.set(manager, connection);

            statement.execute("CREATE TABLE IF NOT EXISTS ender_chest_profiles (" +
                    " player_uuid TEXT PRIMARY KEY," +
                    " `rows` INTEGER DEFAULT 6," +
                    " updated_at INTEGER DEFAULT 0" +
                    ")");
            statement.execute("CREATE TABLE IF NOT EXISTS ender_chest_items (" +
                    " player_uuid TEXT," +
                    " slot INTEGER," +
                    " item_data TEXT," +
                    " PRIMARY KEY (player_uuid, slot)" +
                    ")");

            UUID uuid = UUID.randomUUID();
            assertEquals(6, manager.loadEnderChestRows(uuid, 6));

            boolean saved = manager.saveEnderChest(uuid, 4, new org.bukkit.inventory.ItemStack[0]);
            assertTrue(saved);
            assertEquals(4, manager.loadEnderChestRows(uuid, 6));
        }
    }
}

