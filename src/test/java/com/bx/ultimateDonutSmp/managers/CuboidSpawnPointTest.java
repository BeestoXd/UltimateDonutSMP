package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.managers.CuboidManager.Cuboid;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuboidSpawnPointTest {

    private static final Cuboid SPAWN_ZONE = new Cuboid("world", 10, 60, 10, -10, 80, -10);

    @Test
    void aPointWithinTheCornersBelongsToTheCuboid() {
        assertTrue(CuboidManager.isInside(SPAWN_ZONE, "world", 0, 70, 0));
        assertTrue(CuboidManager.isInside(SPAWN_ZONE, "world", -10, 60, 10));
        assertTrue(CuboidManager.isInside(SPAWN_ZONE, "world", 10, 80, -10));
    }

    @Test
    void theCornersAreReadInEitherOrder() {
        Cuboid flipped = new Cuboid("world", -10, 80, -10, 10, 60, 10);
        assertTrue(CuboidManager.isInside(flipped, "world", 0, 70, 0));
        assertFalse(CuboidManager.isInside(flipped, "world", 0, 59, 0));
    }

    @Test
    void aPointOutsideTheCornersOrInAnotherWorldDoesNot() {
        assertFalse(CuboidManager.isInside(SPAWN_ZONE, "world", 11, 70, 0));
        assertFalse(CuboidManager.isInside(SPAWN_ZONE, "world", 0, 81, 0));
        assertFalse(CuboidManager.isInside(SPAWN_ZONE, "world_nether", 0, 70, 0));
        assertFalse(CuboidManager.isInside(SPAWN_ZONE, null, 0, 70, 0));
        assertFalse(CuboidManager.isInside(null, "world", 0, 70, 0));
    }

    @Test
    void worldNamesAreMatchedWithoutRegardForCase() {
        assertTrue(CuboidManager.isInside(SPAWN_ZONE, "WORLD", 0, 70, 0));
    }

    @Test
    void aSavedSpawnIsJudgedByTheBlockItStandsOn() {
        assertTrue(CuboidManager.spawnFitsCuboid(SPAWN_ZONE, "world,0.5,70.0,0.5,90.0,0.0"));
        assertTrue(CuboidManager.spawnFitsCuboid(SPAWN_ZONE, "world,10.9,60.0,10.9,0.0,0.0"));
        assertFalse(CuboidManager.spawnFitsCuboid(SPAWN_ZONE, "world,11.0,70.0,0.5,0.0,0.0"));
        assertFalse(CuboidManager.spawnFitsCuboid(SPAWN_ZONE, "world,-10.5,70.0,0.5,0.0,0.0"));
        assertFalse(CuboidManager.spawnFitsCuboid(SPAWN_ZONE, "world_nether,0.5,70.0,0.5,0.0,0.0"));
    }

    @Test
    void anUnreadableSavedSpawnCountsAsNoSpawnAtAll() {
        assertFalse(CuboidManager.spawnFitsCuboid(SPAWN_ZONE, null));
        assertFalse(CuboidManager.spawnFitsCuboid(SPAWN_ZONE, ""));
        assertFalse(CuboidManager.spawnFitsCuboid(SPAWN_ZONE, "world,0.5,70.0"));
        assertFalse(CuboidManager.spawnFitsCuboid(SPAWN_ZONE, "world,here,70.0,0.5"));
        assertFalse(CuboidManager.spawnFitsCuboid(null, "world,0.5,70.0,0.5,0.0,0.0"));
    }

    @Test
    void spawnPointsAreStoredUnderTheLowercaseCuboidName() {
        assertEquals("CUBOID-SPAWNS.spawn_zone", CuboidManager.spawnPath("Spawn_Zone"));
        assertEquals("CUBOID-SPAWNS.spawn_zone", CuboidManager.spawnPath("spawn_zone"));
    }

    @Test
    void pluginMetadataAdvertisesBothSubcommands() {
        YamlConfiguration pluginYaml = YamlConfiguration.loadConfiguration(new File("src/main/resources/plugin.yml"));

        String usage = pluginYaml.getString("commands.cuboid.usage", "");
        assertTrue(usage.contains("setspawn"), "usage should mention setspawn: " + usage);
        assertTrue(usage.contains("delspawn"), "usage should mention delspawn: " + usage);
    }
}
