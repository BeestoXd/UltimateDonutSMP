package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.ThreeChoice;
import com.bx.ultimateDonutSmp.models.TwoChoice;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerPlayerSettingsTest {

    private static final List<String> NEW_COLUMNS = List.of(
            "public_chat_enabled",
            "server_broadcasts_enabled",
            "auction_notifications_enabled",
            "explosion_particles_enabled",
            "hide_all_players_enabled",
            "notification_sounds_enabled",
            "rtp_coordinates_enabled",
            "order_notifications_enabled",
            "team_chat_visible",
            "duel_music_enabled",
            "quiet_spawn_enabled",
            "night_vision_enabled",
            "destroy_pearl_on_death",
            "randomized_coords",
            "death_messages_choice",
            "advancement_messages_choice",
            "join_leave_messages_choice",
            "teleport_alerts_enabled",
            "follow_alerts_enabled",
            "explosion_sounds_enabled",
            "display_donutplus_enabled"
    );

    @Test
    void newPlayerSettingsRoundTripThroughSqlite() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseManager manager = managerWithSchema(connection);
            UUID uuid = UUID.randomUUID();
            PlayerData original = new PlayerData(uuid, "SettingsTester");
            original.setPublicChatEnabled(false);
            original.setServerBroadcastsEnabled(false);
            original.setAuctionNotificationsEnabled(false);
            original.setExplosionParticlesEnabled(false);
            original.setHideAllPlayersEnabled(true);
            original.setNotificationSoundsEnabled(false);
            original.setRtpCoordinatesEnabled(false);
            original.setOrderNotificationsEnabled(false);
            original.setTeamChatVisible(false);
            original.setDuelMusicEnabled(false);
            original.setQuietSpawnEnabled(true);
            original.setNightVisionEnabled(true);

            // Enum / 3 choice settings
            original.setPrivateMessagesChoice(ThreeChoice.FRIENDS_FOLLOWED);
            original.setTpaRequestsChoice(ThreeChoice.OFF);
            original.setTpaHereRequestsChoice(ThreeChoice.FRIENDS_FOLLOWED);
            original.setPaymentsChoice(ThreeChoice.OFF);

            // New settings
            original.setDestroyPearlOnDeath(false);
            original.setRandomizedCoords(true);
            original.setDeathMessagesChoice(TwoChoice.OFF);
            original.setAdvancementMessagesChoice(ThreeChoice.FRIENDS_FOLLOWED);
            original.setJoinLeaveMessagesChoice(ThreeChoice.OFF);
            original.setTeleportAlertsEnabled(false);
            original.setFollowAlertsEnabled(false);
            original.setExplosionSoundsEnabled(false);
            original.setDisplayDonutPlusEnabled(false);

            manager.savePlayer(original);
            PlayerData loaded = manager.loadPlayer(uuid);

            assertNotNull(loaded);
            assertFalse(loaded.isPublicChatEnabled());
            assertFalse(loaded.isServerBroadcastsEnabled());
            assertFalse(loaded.isAuctionNotificationsEnabled());
            assertFalse(loaded.isExplosionParticlesEnabled());
            assertTrue(loaded.isHideAllPlayersEnabled());
            assertFalse(loaded.isNotificationSoundsEnabled());
            assertFalse(loaded.isRtpCoordinatesEnabled());
            assertFalse(loaded.isOrderNotificationsEnabled());
            assertFalse(loaded.isTeamChatVisible());
            assertFalse(loaded.isDuelMusicEnabled());
            assertTrue(loaded.isQuietSpawnEnabled());
            assertTrue(loaded.isNightVisionEnabled());

            // Enum choices assertions
            assertEquals(ThreeChoice.FRIENDS_FOLLOWED, loaded.getPrivateMessagesChoice());
            assertEquals(ThreeChoice.OFF, loaded.getTpaRequestsChoice());
            assertEquals(ThreeChoice.FRIENDS_FOLLOWED, loaded.getTpaHereRequestsChoice());
            assertEquals(ThreeChoice.OFF, loaded.getPaymentsChoice());

            // New settings assertions
            assertFalse(loaded.isDestroyPearlOnDeath());
            assertTrue(loaded.isRandomizedCoords());
            assertEquals(TwoChoice.OFF, loaded.getDeathMessagesChoice());
            assertEquals(ThreeChoice.FRIENDS_FOLLOWED, loaded.getAdvancementMessagesChoice());
            assertEquals(ThreeChoice.OFF, loaded.getJoinLeaveMessagesChoice());
            assertFalse(loaded.isTeleportAlertsEnabled());
            assertFalse(loaded.isFollowAlertsEnabled());
            assertFalse(loaded.isExplosionSoundsEnabled());
            assertFalse(loaded.isDisplayDonutPlusEnabled());
        }
    }

    @Test
    void oldPlayersTableReceivesNewColumnsWithCompatibleDefaults() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseManager manager = managerWithSchema(connection);
            try (Statement statement = connection.createStatement()) {
                for (String column : NEW_COLUMNS) {
                    statement.execute("ALTER TABLE players DROP COLUMN " + column);
                }
            }

            invoke(manager, "ensurePlayerColumns");

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                        "INSERT INTO players (uuid, username) VALUES ('00000000-0000-0000-0000-000000000001', 'Legacy')");
                try (ResultSet result = statement.executeQuery("SELECT * FROM players WHERE username = 'Legacy'")) {
                    assertTrue(result.next());
                    assertTrue(result.getInt("public_chat_enabled") != 0);
                    assertTrue(result.getInt("server_broadcasts_enabled") != 0);
                    assertTrue(result.getInt("auction_notifications_enabled") != 0);
                    assertTrue(result.getInt("explosion_particles_enabled") != 0);
                    assertFalse(result.getInt("hide_all_players_enabled") != 0);
                    assertTrue(result.getInt("notification_sounds_enabled") != 0);
                    assertTrue(result.getInt("rtp_coordinates_enabled") != 0);
                    assertTrue(result.getInt("order_notifications_enabled") != 0);
                    assertTrue(result.getInt("team_chat_visible") != 0);
                    assertTrue(result.getInt("duel_music_enabled") != 0);
                    assertFalse(result.getInt("quiet_spawn_enabled") != 0);
                    assertFalse(result.getInt("night_vision_enabled") != 0);

                    // New settings column defaults assertions
                    assertTrue(result.getInt("destroy_pearl_on_death") != 0);
                    assertFalse(result.getInt("randomized_coords") != 0);
                    assertEquals(1, result.getInt("death_messages_choice"));
                    assertEquals(1, result.getInt("advancement_messages_choice"));
                    assertEquals(1, result.getInt("join_leave_messages_choice"));
                    assertTrue(result.getInt("teleport_alerts_enabled") != 0);
                    assertTrue(result.getInt("follow_alerts_enabled") != 0);
                    assertTrue(result.getInt("explosion_sounds_enabled") != 0);
                    assertTrue(result.getInt("display_donutplus_enabled") != 0);
                }
            }
        }
    }

    private static DatabaseManager managerWithSchema(Connection connection) throws Exception {
        DatabaseManager manager = new DatabaseManager(null);
        Field connectionField = DatabaseManager.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(manager, connection);
        invoke(manager, "createTables");
        invoke(manager, "ensurePlayerColumns");
        return manager;
    }

    private static void invoke(DatabaseManager manager, String methodName) throws Exception {
        Method method = DatabaseManager.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(manager);
    }
}
