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
    void anEmptyLobbyServerCountsAsNoDestination() {
        assertTrue(MaintenanceManager.isLobbyServerSet("lobby"));
        assertFalse(MaintenanceManager.isLobbyServerSet(""));
        assertFalse(MaintenanceManager.isLobbyServerSet("   "));
        assertFalse(MaintenanceManager.isLobbyServerSet(null));
    }

    @Test
    void clearingTheStoredLobbyLeavesNothingBehindInTheStateFile() {
        assertNull(MaintenanceManager.normalizeLobbyServer(null));
        assertNull(MaintenanceManager.normalizeLobbyServer(""));
        assertNull(MaintenanceManager.normalizeLobbyServer("   "));
        assertEquals("hub", MaintenanceManager.normalizeLobbyServer("hub"));
    }
}
