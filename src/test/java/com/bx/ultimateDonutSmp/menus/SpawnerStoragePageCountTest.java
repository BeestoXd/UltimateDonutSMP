package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerLootEntry;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpawnerStoragePageCountTest {

    private static final int ITEMS_PER_PAGE = 45;

    @Test
    void anEmptySpawnerStillHasOnePage() {
        assertEquals(1, SpawnerStorageMenu.countStoredPages(spawnerWith(), ITEMS_PER_PAGE));
    }

    @Test
    void pageCountFollowsTheHighestOccupiedSlot() {
        assertEquals(1, SpawnerStorageMenu.countStoredPages(spawnerWith(slot(0)), ITEMS_PER_PAGE));
        assertEquals(1, SpawnerStorageMenu.countStoredPages(spawnerWith(slot(44)), ITEMS_PER_PAGE));
        assertEquals(2, SpawnerStorageMenu.countStoredPages(spawnerWith(slot(45)), ITEMS_PER_PAGE));
        assertEquals(2, SpawnerStorageMenu.countStoredPages(spawnerWith(slot(89)), ITEMS_PER_PAGE));
        assertEquals(3, SpawnerStorageMenu.countStoredPages(spawnerWith(slot(90)), ITEMS_PER_PAGE));
    }

    @Test
    void gapsBelowTheHighestSlotStillCount() {
        assertEquals(3, SpawnerStorageMenu.countStoredPages(spawnerWith(slot(0), slot(90)), ITEMS_PER_PAGE));
    }

    @Test
    void smallerPagesSplitTheSameStorageIntoMorePages() {
        assertEquals(3, SpawnerStorageMenu.countStoredPages(spawnerWith(slot(18)), 9));
        assertEquals(1, SpawnerStorageMenu.countStoredPages(spawnerWith(slot(8)), 9));
    }

    @Test
    void missingInstanceOrPageSizeFallsBackToOnePage() {
        assertEquals(1, SpawnerStorageMenu.countStoredPages(null, ITEMS_PER_PAGE));
        assertEquals(1, SpawnerStorageMenu.countStoredPages(spawnerWith(slot(90)), 0));
    }

    private static SpawnerLootEntry slot(int slotIndex) {
        return new SpawnerLootEntry("SLOT_" + slotIndex, Material.ROTTEN_FLESH, 1);
    }

    private static SpawnerInstance spawnerWith(SpawnerLootEntry... entries) {
        SpawnerInstance instance = new SpawnerInstance(
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
        instance.setStoredLootEntries(List.of(entries));
        return instance;
    }
}
