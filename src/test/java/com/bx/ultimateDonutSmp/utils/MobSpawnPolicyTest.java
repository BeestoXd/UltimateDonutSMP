package com.bx.ultimateDonutSmp.utils;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobSpawnPolicyTest {

    @Test
    void onlyVanillaSpawnerReasonReceivesPersistentExemption() {
        assertTrue(MobSpawnPolicy.isVanillaSpawnerSpawn(CreatureSpawnEvent.SpawnReason.SPAWNER));
        assertFalse(MobSpawnPolicy.isVanillaSpawnerSpawn(CreatureSpawnEvent.SpawnReason.NATURAL));
        assertFalse(MobSpawnPolicy.isVanillaSpawnerSpawn(CreatureSpawnEvent.SpawnReason.CUSTOM));
    }

    @Test
    void periodicCleanupPreservesSpawnerMobsAndExistingExcludedTypes() {
        assertTrue(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.ZOMBIE, false));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.ZOMBIE, true));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(false, EntityType.ZOMBIE, false));

        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.PHANTOM, false));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.WITHER, false));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.ENDER_DRAGON, false));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.ELDER_GUARDIAN, false));
        assertFalse(MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(true, EntityType.WARDEN, false));
    }
}
