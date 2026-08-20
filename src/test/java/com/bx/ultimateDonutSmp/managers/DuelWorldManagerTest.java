package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DuelWorldManagerTest {

    private static final String VANILLA_POOL = "MAP_SOURCES.RANDOM_BIOMES.VANILLA_POOL.";

    private static YamlConfiguration bundledDuels() {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/duels.yml"));
    }

    @Test
    void bundledConfigDefinesWaterLimitDefaults() {
        YamlConfiguration duels = bundledDuels();
        assertEquals(40, duels.getInt(VANILLA_POOL + "MAX_WATER_PERCENT"));
        assertEquals(5, duels.getInt(VANILLA_POOL + "MAX_TERRAIN_ATTEMPTS"));
    }

    @Test
    void waterLimitIsClampedToAPercentage() {
        assertEquals(0, DuelWorldManager.normalizeMaxWaterPercent(-20));
        assertEquals(40, DuelWorldManager.normalizeMaxWaterPercent(40));
        assertEquals(100, DuelWorldManager.normalizeMaxWaterPercent(250));
    }

    @Test
    void terrainAttemptsAlwaysAllowAtLeastOneGeneration() {
        assertEquals(1, DuelWorldManager.normalizeMaxTerrainAttempts(-3));
        assertEquals(1, DuelWorldManager.normalizeMaxTerrainAttempts(0));
        assertEquals(5, DuelWorldManager.normalizeMaxTerrainAttempts(5));
        assertEquals(20, DuelWorldManager.normalizeMaxTerrainAttempts(500));
    }

    @Test
    void waterPercentRoundsToTheNearestWholePercent() {
        assertEquals(0, DuelWorldManager.waterPercent(0, 0));
        assertEquals(0, DuelWorldManager.waterPercent(0, 625));
        assertEquals(50, DuelWorldManager.waterPercent(312, 625));
        assertEquals(100, DuelWorldManager.waterPercent(625, 625));
    }

    @Test
    void oceanArenasAreRejectedWhileShallowPondsAreKept() {
        assertTrue(DuelWorldManager.isArenaTooWatery(95, 40));
        assertTrue(DuelWorldManager.isArenaTooWatery(41, 40));
        assertFalse(DuelWorldManager.isArenaTooWatery(40, 40));
        assertFalse(DuelWorldManager.isArenaTooWatery(12, 40));
    }

    @Test
    void aFullyPermissiveLimitNeverRejectsAnArena() {
        assertFalse(DuelWorldManager.isArenaTooWatery(100, 100));
    }

    @Test
    void sampleStepGrowsWithTheArenaSoLargeArenasStayCheapToScan() {
        assertEquals(4, DuelWorldManager.arenaSampleStep(16));
        assertEquals(4, DuelWorldManager.arenaSampleStep(48));
        assertEquals(8, DuelWorldManager.arenaSampleStep(128));
        assertEquals(16, DuelWorldManager.arenaSampleStep(256));
    }
}
