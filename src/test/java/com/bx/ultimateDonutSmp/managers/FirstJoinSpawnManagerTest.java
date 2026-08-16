package com.bx.ultimateDonutSmp.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstJoinSpawnManagerTest {

    private static YamlConfiguration bundledConfig() {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));
    }

    @Test
    void bundledConfigShipsFirstJoinRtpDisabledByDefault() {
        YamlConfiguration config = bundledConfig();
        assertTrue(config.contains("FIRST-JOIN-RTP.ENABLED"));
        assertFalse(config.getBoolean("FIRST-JOIN-RTP.ENABLED"));
        assertTrue(config.getBoolean("FIRST-JOIN-RTP.FALLBACK-TO-SPAWN"));
        assertEquals("", config.getString("FIRST-JOIN-RTP.WORLD.NAME"));
        assertEquals(500, config.getInt("FIRST-JOIN-RTP.WORLD.MIN-RADIUS"));
        assertEquals(5000, config.getInt("FIRST-JOIN-RTP.WORLD.MAX-RADIUS"));
    }

    @Test
    void bundledConfigDocumentsTheLegacySpawnOnFirstJoinToggle() {
        YamlConfiguration config = bundledConfig();
        assertTrue(config.contains("SETTINGS.TELEPORT-SPAWN-ON-FIRST-JOIN"));
        assertTrue(config.getBoolean("SETTINGS.TELEPORT-SPAWN-ON-FIRST-JOIN"));
    }

    @Test
    void placeholdersUseBlockCoordinatesOfTheDestination() {
        Location destination = new Location(null, 128.9, 71.0, -64.2);
        assertEquals(
                "dropped at X:128 Y:71 Z:-65 in ",
                FirstJoinSpawnManager.applyPlaceholders("dropped at X:{x} Y:{y} Z:{z} in {world}", destination)
        );
    }

    @Test
    void placeholdersAreLeftUntouchedWithoutADestination() {
        assertEquals("still searching {x}", FirstJoinSpawnManager.applyPlaceholders("still searching {x}", null));
        assertEquals("", FirstJoinSpawnManager.applyPlaceholders("", null));
        assertEquals("", FirstJoinSpawnManager.applyPlaceholders(null, null));
    }
}
