package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaintenanceLobbyDestinationTest {

    @Test
    void bundledConfigShipsTheMaintenanceSection() {
        YamlConfiguration network = YamlConfiguration.loadConfiguration(new File("src/main/resources/network.yml"));
        assertTrue(network.contains("MAINTENANCE.LOBBY_SERVER"));
        assertTrue(network.getBoolean("MAINTENANCE.USE_PROXY"));
        assertEquals("lobby", network.getString("MAINTENANCE.LOBBY_SERVER"));
        assertEquals("WORLD", network.getString("MAINTENANCE.LOBBY_WORLD"));
        assertEquals(5, network.getInt("MAINTENANCE.RECONNECT_DELAY_SECONDS"));
        assertEquals(
                "ULTIMATEDONUTSMP.ADMIN.MAINTENANCE.BYPASS",
                network.getString("MAINTENANCE.BYPASS_PERMISSION")
        );
    }

    @Test
    void bundledConfigShipsTheMaintenanceMessages() {
        YamlConfiguration network = YamlConfiguration.loadConfiguration(new File("src/main/resources/network.yml"));

        assertEquals(
                "&cThis server is in maintenance and no lobby is available.",
                network.getString("MAINTENANCE.MESSAGES.KICK_FALLBACK")
        );
        assertEquals(
                "&d[Maintenance] &7you joined while maintenance mode is active.",
                network.getString("MAINTENANCE.MESSAGES.BYPASS_JOIN")
        );
        assertEquals("&a&lServer online", network.getString("MAINTENANCE.MESSAGES.RECONNECTING_TITLE"));
        assertTrue(network.getString("MAINTENANCE.MESSAGES.RECONNECTING_SUBTITLE", "").contains("%seconds%"));
        assertTrue(network.contains("MAINTENANCE.MESSAGES.ENTERING"));
        assertTrue(network.contains("MAINTENANCE.MESSAGES.NOT_ALLOWED"));
    }

    @Test
    void anEmptyLobbyServerCountsAsNoDestination() {
        assertTrue(MaintenanceManager.isLobbyServerSet("lobby"));
        assertFalse(MaintenanceManager.isLobbyServerSet(""));
        assertFalse(MaintenanceManager.isLobbyServerSet("   "));
        assertFalse(MaintenanceManager.isLobbyServerSet(null));
    }

    @Test
    void anEmptyLobbyWorldCountsAsNoDestinationEither() {
        assertTrue(MaintenanceManager.isLobbyDestinationSet("WORLD"));
        assertFalse(MaintenanceManager.isLobbyDestinationSet(""));
        assertFalse(MaintenanceManager.isLobbyDestinationSet("   "));
        assertFalse(MaintenanceManager.isLobbyDestinationSet(null));
    }

    @Test
    void withNowhereToPutThemEveryoneOnlineIsKickedInsteadOfLeftPlaying() {
        assertTrue(MaintenanceManager.kicksLeftoverPlayers(false, false));
        assertFalse(MaintenanceManager.kicksLeftoverPlayers(false, true));
        assertTrue(MaintenanceManager.kicksLeftoverPlayers(true, false));
        assertTrue(MaintenanceManager.kicksLeftoverPlayers(true, true));
    }

    @Test
    void clearingTheStoredLobbyLeavesNothingBehindInTheStateFile() {
        assertNull(MaintenanceManager.normalizeLobbyServer(null));
        assertNull(MaintenanceManager.normalizeLobbyServer(""));
        assertNull(MaintenanceManager.normalizeLobbyServer("   "));
        assertEquals("hub", MaintenanceManager.normalizeLobbyServer("hub"));
    }
}
