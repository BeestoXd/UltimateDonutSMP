package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnerLootFilterKeyTest {

    @Test
    void filteringAMaterialNeverMatchesTheSlotKeyStoredLootIsKeyedBy() {
        SpawnerInstance instance = spawner();
        instance.setStoredLootEntries(List.of(new SpawnerLootEntry("SLOT_0", Material.ROTTEN_FLESH, 64)));
        instance.setLootDisabled(Material.ROTTEN_FLESH.name(), true);

        SpawnerLootEntry stored = instance.getSlotLoot(0);
        assertTrue(instance.isLootDisabled(stored.getMaterial().name()));
        assertFalse(instance.isLootDisabled(stored.getKey()));
    }

    @Test
    void filteringADropDefinitionKeyNeverMatchesTheSlotKeyEither() {
        SpawnerInstance instance = spawner();
        instance.setStoredLootEntries(List.of(new SpawnerLootEntry("SLOT_7", Material.BONE, 32)));
        instance.setLootDisabled("BONE_DROP", true);

        assertTrue(instance.isLootDisabled("BONE_DROP"));
        assertFalse(instance.isLootDisabled("SLOT_7"));
    }

    @Test
    void disablingAndReEnablingOnlyEverTouchesTheKeyItWasGiven() {
        SpawnerInstance instance = spawner();

        instance.setLootDisabled(Material.ARROW.name(), true);
        assertTrue(instance.isLootDisabled("arrow"));
        assertTrue(instance.isLootDisabled(Material.ARROW.name()));

        instance.setLootDisabled(Material.ARROW.name(), false);
        assertFalse(instance.isLootDisabled(Material.ARROW.name()));
    }

    private static SpawnerInstance spawner() {
        return new SpawnerInstance(
                1L,
                "world",
                0,
                64,
                0,
                UUID.randomUUID(),
                "BeestoXd",
                "ZOMBIE",
                1L,
                SpawnerInstance.AccessMode.OWNER_ONLY,
                0L,
                0L,
                0L
        );
    }
}
