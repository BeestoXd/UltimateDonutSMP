package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseManagerKnownPlayersTest {

    @Test
    void knownPlayerNamesAreSortedAndBlankNamesAreSkipped() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE players (uuid TEXT PRIMARY KEY, username TEXT)");
            statement.execute("INSERT INTO players VALUES ('1', 'Zed')");
            statement.execute("INSERT INTO players VALUES ('2', '')");
            statement.execute("INSERT INTO players VALUES ('3', NULL)");
            statement.execute("INSERT INTO players VALUES ('4', 'alice')");

            DatabaseManager manager = new DatabaseManager(null);
            Field connectionField = DatabaseManager.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            connectionField.set(manager, connection);

            assertEquals(List.of("alice", "Zed"), manager.loadKnownPlayerNames());
        }
    }

    @Test
    void offlinePlayerLookupDoesNotCloseConnectionAndLoadsPlayer() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE players ("
                    + "uuid TEXT PRIMARY KEY, username TEXT, money REAL DEFAULT 0, shards INTEGER DEFAULT 0, "
                    + "kills INTEGER DEFAULT 0, deaths INTEGER DEFAULT 0, playtime_seconds INTEGER DEFAULT 0, "
                    + "blocks_placed INTEGER DEFAULT 0, blocks_broken INTEGER DEFAULT 0, mobs_killed INTEGER DEFAULT 0, "
                    + "kill_streak INTEGER DEFAULT 0, highest_kill_streak INTEGER DEFAULT 0, money_spent REAL DEFAULT 0, "
                    + "money_made REAL DEFAULT 0, tpauto INTEGER DEFAULT 0, phantom_enabled INTEGER DEFAULT 1, "
                    + "payments_enabled INTEGER DEFAULT 1, scoreboard_visible INTEGER DEFAULT 1, pay_alerts_enabled INTEGER DEFAULT 1, "
                    + "hotbar_messages_enabled INTEGER DEFAULT 1, worth_display_enabled INTEGER DEFAULT 1, "
                    + "money_nametags_enabled INTEGER DEFAULT 0, "
                    + "clear_entities_messages_enabled INTEGER DEFAULT 1, bounty_alerts_enabled INTEGER DEFAULT 1, "
                    + "tpa_confirm_menu_enabled INTEGER DEFAULT 1, chainmail_on_respawn_enabled INTEGER DEFAULT 1, "
                    + "lunar_teammates_enabled INTEGER DEFAULT 1, tpa_requests_enabled INTEGER DEFAULT 1, "
                    + "auto_tpahere_enabled INTEGER DEFAULT 0, tpahere_requests_enabled INTEGER DEFAULT 1, "
                    + "team_invites_enabled INTEGER DEFAULT 1, mob_spawn_enabled INTEGER DEFAULT 1, "
                    + "pay_confirm_menu_enabled INTEGER DEFAULT 1, totem_particles_enabled INTEGER DEFAULT 1, "
                    + "fast_crystals_enabled INTEGER DEFAULT 1, amethyst_break_messages_enabled INTEGER DEFAULT 1, "
                    + "private_messages_enabled INTEGER DEFAULT 1, keyall_notifications_enabled INTEGER DEFAULT 1, "
                    + "duel_requests_enabled INTEGER DEFAULT 1, public_chat_enabled INTEGER DEFAULT 1, "
                    + "server_broadcasts_enabled INTEGER DEFAULT 1, auction_notifications_enabled INTEGER DEFAULT 1, "
                    + "explosion_particles_enabled INTEGER DEFAULT 1, hide_all_players_enabled INTEGER DEFAULT 0, "
                    + "notification_sounds_enabled INTEGER DEFAULT 1, rtp_coordinates_enabled INTEGER DEFAULT 1, "
                    + "order_notifications_enabled INTEGER DEFAULT 1, team_chat_visible INTEGER DEFAULT 1, "
                    + "duel_music_enabled INTEGER DEFAULT 1, quiet_spawn_enabled INTEGER DEFAULT 0, "
                    + "night_vision_enabled INTEGER DEFAULT 0, keyall_remaining_seconds INTEGER DEFAULT -1, "
                    + "shard_booster_expiry INTEGER DEFAULT 0, mob_spawn_disabled_until BIGINT DEFAULT 0, "
                    + "phantom_disabled_until BIGINT DEFAULT 0, destroy_pearl_on_death INTEGER DEFAULT 1, "
                    + "randomized_coords INTEGER DEFAULT 0, death_messages_choice INTEGER DEFAULT 1, "
                    + "advancement_messages_choice INTEGER DEFAULT 1, join_leave_messages_choice INTEGER DEFAULT 1, "
                    + "teleport_alerts_enabled INTEGER DEFAULT 1, follow_alerts_enabled INTEGER DEFAULT 1, "
                    + "explosion_sounds_enabled INTEGER DEFAULT 1, display_donutplus_enabled INTEGER DEFAULT 1, "
                    + "voice_chat_consent INTEGER DEFAULT 0)");

            java.util.UUID testUuid = java.util.UUID.randomUUID();
            statement.execute("INSERT INTO players (uuid, username, kills, money) VALUES ('" + testUuid + "', 'Bob', 42, 500.0)");

            DatabaseManager manager = new DatabaseManager(null);
            Field connectionField = DatabaseManager.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            connectionField.set(manager, connection);

            // Lookup offline player UUID by username
            java.util.UUID foundUuid = manager.findPlayerUuidByUsername("Bob");
            assertEquals(testUuid, foundUuid);

            // Verify connection is NOT closed and loadPlayer succeeds
            com.bx.ultimateDonutSmp.models.PlayerData loaded = manager.loadPlayer(foundUuid);
            org.junit.jupiter.api.Assertions.assertNotNull(loaded);
            assertEquals("Bob", loaded.getUsername());
            assertEquals(42, loaded.getKills());
            assertEquals(500.0, loaded.getMoney());

            // Lookup last known username again to ensure repeated calls work
            assertEquals("Bob", manager.getLastKnownUsername(foundUuid));
        }
    }
}
