package com.bx.ultimateDonutSmp.listeners;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstJoinSpawnTeleportTest {

    @Test
    void bundledConfigShipsTheFirstJoinSpawnDelay() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));
        assertTrue(config.contains("SETTINGS.FIRST-JOIN-SPAWN-DELAY-TICKS"));
        assertEquals(
                PlayerJoinQuitListener.DEFAULT_FIRST_JOIN_SPAWN_DELAY_TICKS,
                config.getLong("SETTINGS.FIRST-JOIN-SPAWN-DELAY-TICKS")
        );
    }

    @Test
    void theTeleportNeverRunsInsideTheJoinEvent() {
        assertEquals(1L, PlayerJoinQuitListener.firstJoinSpawnDelayTicks(0L));
        assertEquals(1L, PlayerJoinQuitListener.firstJoinSpawnDelayTicks(-40L));
        assertEquals(1L, PlayerJoinQuitListener.firstJoinSpawnDelayTicks(1L));
    }

    @Test
    void aConfiguredDelayIsKeptUpToTheCap() {
        assertEquals(20L, PlayerJoinQuitListener.firstJoinSpawnDelayTicks(20L));
        assertEquals(100L, PlayerJoinQuitListener.firstJoinSpawnDelayTicks(100L));
        assertEquals(
                PlayerJoinQuitListener.MAX_FIRST_JOIN_SPAWN_DELAY_TICKS,
                PlayerJoinQuitListener.firstJoinSpawnDelayTicks(999_999L)
        );
    }
}
