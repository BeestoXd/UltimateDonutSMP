package com.bx.ultimateDonutSmp.utils;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakePlayerSpawnTargetTest {

    @Test
    void bundledStaffModeSpawnsAtLookAndSneaks() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.load(Path.of("src/main/resources", "staff-mode.yml").toFile());

        assertTrue(config.getBoolean("FAKE-PLAYER.SNEAK", false),
                "staff-mode.yml must ship SNEAK as true so a config restore keeps the DonutSMP crouch");
        assertTrue(config.getBoolean("FAKE-PLAYER.SPAWN-AT-LOOK-TARGET", false),
                "staff-mode.yml must ship SPAWN-AT-LOOK-TARGET as true so a config restore keeps spawning on the crosshair");
        assertEquals(32.0D, config.getDouble("FAKE-PLAYER.LOOK-RANGE", 0.0D), 0.0001D);
    }

    @Test
    void feetStayWhenLookTargetIsOff() {
        Location standing = new Location(null, 10.0D, 64.0D, 10.0D, 90.0F, 45.0F);
        Location spawn = FakePlayerSpawnTarget.resolve(
                standing,
                standing.clone().add(0D, 1.62D, 0D),
                new Vector(1D, 0D, 0D),
                new Vector(20.0D, 70.0D, 20.0D),
                new Vector(0D, 1D, 0D),
                false,
                8.0D
        );

        assertEquals(10.0D, spawn.getX(), 0.0001D);
        assertEquals(64.0D, spawn.getY(), 0.0001D);
        assertEquals(10.0D, spawn.getZ(), 0.0001D);
        assertEquals(90.0F, spawn.getYaw(), 0.0001F);
        assertEquals(45.0F, spawn.getPitch(), 0.0001F);
    }

    @Test
    void topFaceKeepsTheLookedAtPointAndStaffYaw() {
        Location standing = new Location(null, 10.0D, 64.0D, 10.0D, 180.0F, 12.0F);
        Location spawn = FakePlayerSpawnTarget.resolve(
                standing,
                standing.clone().add(0D, 1.62D, 0D),
                new Vector(1D, -0.4D, 0D),
                new Vector(22.5D, 70.0D, 18.25D),
                new Vector(0D, 1D, 0D),
                true,
                8.0D
        );

        assertEquals(22.5D, spawn.getX(), 0.0001D);
        assertEquals(70.0D + FakePlayerSpawnTarget.TOP_SURFACE_LIFT, spawn.getY(), 0.0001D);
        assertEquals(18.25D, spawn.getZ(), 0.0001D);
        assertEquals(180.0F, spawn.getYaw(), 0.0001F);
        assertEquals(0.0F, spawn.getPitch(), 0.0001F);
    }

    @Test
    void sideFacePushesTheBaitOutOfTheBlock() {
        Location standing = new Location(null, 0.0D, 64.0D, 0.0D, 0.0F, 0.0F);
        Location spawn = FakePlayerSpawnTarget.resolve(
                standing,
                standing.clone().add(0D, 1.62D, 0D),
                new Vector(0D, 0D, 1D),
                new Vector(5.0D, 66.0D, 12.0D),
                new Vector(0D, 0D, -1D),
                true,
                8.0D
        );

        assertEquals(5.0D, spawn.getX(), 0.0001D);
        assertEquals(66.0D, spawn.getY(), 0.0001D);
        assertEquals(12.0D - FakePlayerSpawnTarget.FACE_PUSH, spawn.getZ(), 0.0001D);
    }

    @Test
    void aMissPlacesTheBaitAlongTheLookInsteadOfAtTheFeet() {
        Location standing = new Location(null, 10.0D, 64.0D, 10.0D, 45.0F, -20.0F);
        Location eye = new Location(null, 10.0D, 65.62D, 10.0D);
        Location spawn = FakePlayerSpawnTarget.resolve(
                standing,
                eye,
                new Vector(1D, 0D, 0D),
                null,
                null,
                true,
                8.0D
        );

        assertEquals(18.0D, spawn.getX(), 0.0001D);
        assertEquals(65.62D, spawn.getY(), 0.0001D);
        assertEquals(10.0D, spawn.getZ(), 0.0001D);
        assertEquals(45.0F, spawn.getYaw(), 0.0001F);
        assertEquals(0.0F, spawn.getPitch(), 0.0001F);
    }
}
