package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PlayerLogEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatLogsTest {

    private static final UUID ALEX = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID STEVE = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

    @Test
    void theServerWideViewReadsPublicChatFromEveryPlayerNewestFirst() throws Exception {
        try (Connection connection = openLogTable()) {
            insert(connection, ALEX, "Alex", "chat", "CHAT_PUBLIC", "first", 1_000L);
            insert(connection, STEVE, "Steve", "chat", "CHAT_PUBLIC", "second", 2_000L);
            insert(connection, ALEX, "Alex", "messages", "MSG_SENT", "To Steve: hidden", 3_000L);
            insert(connection, ALEX, "Alex", "shop", "SHOP_BUY", "ignored", 4_000L);

            DatabaseManager manager = managerOn(connection);

            List<PlayerLogEntry> logs = manager.getLogsByType(null, PlayerLogsManager.PUBLIC_CHAT_TYPE, 45, 0);

            assertEquals(2, manager.getLogsByTypeCount(null, PlayerLogsManager.PUBLIC_CHAT_TYPE));
            assertEquals(List.of("second", "first"), logs.stream().map(PlayerLogEntry::details).toList());
            assertEquals(List.of("Steve", "Alex"), logs.stream().map(PlayerLogEntry::playerName).toList());
        }
    }

    @Test
    void askingForOnePlayerLeavesTheRestOfTheServerOut() throws Exception {
        try (Connection connection = openLogTable()) {
            insert(connection, ALEX, "Alex", "chat", "CHAT_PUBLIC", "alex talking", 1_000L);
            insert(connection, STEVE, "Steve", "chat", "CHAT_PUBLIC", "steve talking", 2_000L);

            DatabaseManager manager = managerOn(connection);

            List<PlayerLogEntry> logs = manager.getLogsByType(ALEX, PlayerLogsManager.PUBLIC_CHAT_TYPE, 45, 0);

            assertEquals(1, manager.getLogsByTypeCount(ALEX, PlayerLogsManager.PUBLIC_CHAT_TYPE));
            assertEquals(List.of("alex talking"), logs.stream().map(PlayerLogEntry::details).toList());
        }
    }

    @Test
    void pagingWalksBackThroughOlderMessages() throws Exception {
        try (Connection connection = openLogTable()) {
            for (int i = 1; i <= 5; i++) {
                insert(connection, ALEX, "Alex", "chat", "CHAT_PUBLIC", "message " + i, i * 1_000L);
            }

            DatabaseManager manager = managerOn(connection);

            assertEquals(
                    List.of("message 5", "message 4"),
                    manager.getLogsByType(null, PlayerLogsManager.PUBLIC_CHAT_TYPE, 2, 0)
                            .stream().map(PlayerLogEntry::details).toList()
            );
            assertEquals(
                    List.of("message 3", "message 2"),
                    manager.getLogsByType(null, PlayerLogsManager.PUBLIC_CHAT_TYPE, 2, 2)
                            .stream().map(PlayerLogEntry::details).toList()
            );
            assertEquals(
                    List.of("message 1"),
                    manager.getLogsByType(null, PlayerLogsManager.PUBLIC_CHAT_TYPE, 2, 4)
                            .stream().map(PlayerLogEntry::details).toList()
            );
        }
    }

    @Test
    void chatLoggingShipsOnForBothPublicAndPrivateMessages() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.load(Path.of("src/main/resources", "config.yml").toFile());
        ConfigurationSection logging = config.getConfigurationSection("CHAT.LOGGING");

        assertNotNull(logging, "config.yml has no CHAT.LOGGING section");
        assertTrue(logging.getBoolean("ENABLED"));
        assertTrue(logging.getBoolean("PUBLIC-MESSAGES"));
        assertTrue(logging.getBoolean("PRIVATE-MESSAGES"));
    }

    private static Connection openLogTable() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE player_logs ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, player_uuid TEXT NOT NULL, "
                    + "player_name TEXT NOT NULL, category TEXT NOT NULL, log_type TEXT NOT NULL, "
                    + "details TEXT NOT NULL, timestamp INTEGER NOT NULL)");
        }
        return connection;
    }

    private static DatabaseManager managerOn(Connection connection) throws Exception {
        DatabaseManager manager = new DatabaseManager(null);
        Field connectionField = DatabaseManager.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(manager, connection);
        return manager;
    }

    private static void insert(Connection connection, UUID uuid, String name, String category,
                               String type, String details, long timestamp) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO player_logs (player_uuid, player_name, category, log_type, details, timestamp) "
                        + "VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setString(3, category);
            ps.setString(4, type);
            ps.setString(5, details);
            ps.setLong(6, timestamp);
            ps.executeUpdate();
        }
    }
}
