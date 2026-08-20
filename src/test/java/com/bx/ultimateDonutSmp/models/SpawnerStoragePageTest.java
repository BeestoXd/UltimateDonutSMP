package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnerStoragePageTest {

    private static final int ITEMS_PER_PAGE = 45;

    @Test
    void pageEntriesOnlyCoverTheSlotsShownOnThatPage() {
        SpawnerInstance instance = spawner();
        instance.setSlotLoot(0, Material.ROTTEN_FLESH, 64);
        instance.setSlotLoot(44, Material.BONE, 32);
        instance.setSlotLoot(45, Material.ARROW, 16);
        instance.setSlotLoot(89, Material.STRING, 8);
        instance.setSlotLoot(90, Material.GUNPOWDER, 4);

        List<SpawnerLootEntry> firstPage = instance.getPageLootEntries(1, ITEMS_PER_PAGE);
        assertEquals(List.of("SLOT_0", "SLOT_44"), keys(firstPage));

        List<SpawnerLootEntry> secondPage = instance.getPageLootEntries(2, ITEMS_PER_PAGE);
        assertEquals(List.of("SLOT_45", "SLOT_89"), keys(secondPage));

        List<SpawnerLootEntry> thirdPage = instance.getPageLootEntries(3, ITEMS_PER_PAGE);
        assertEquals(List.of("SLOT_90"), keys(thirdPage));
    }

    @Test
    void pageEntriesAreOrderedBySlotRatherThanInsertion() {
        SpawnerInstance instance = spawner();
        instance.setSlotLoot(30, Material.BONE, 12);
        instance.setSlotLoot(2, Material.ARROW, 5);
        instance.setSlotLoot(17, Material.STRING, 9);

        assertEquals(List.of("SLOT_2", "SLOT_17", "SLOT_30"), keys(instance.getPageLootEntries(1, ITEMS_PER_PAGE)));
    }

    @Test
    void droppingOnePageLeavesEveryOtherPageUntouched() {
        SpawnerInstance instance = spawner();
        instance.setSlotLoot(0, Material.ROTTEN_FLESH, 64);
        instance.setSlotLoot(45, Material.ARROW, 16);
        instance.setSlotLoot(90, Material.GUNPOWDER, 4);

        for (SpawnerLootEntry entry : instance.getPageLootEntries(2, ITEMS_PER_PAGE)) {
            instance.removeStoredLoot(entry.getKey(), entry.getAmount());
        }

        assertTrue(instance.getPageLootEntries(2, ITEMS_PER_PAGE).isEmpty());
        assertEquals(List.of("SLOT_0"), keys(instance.getPageLootEntries(1, ITEMS_PER_PAGE)));
        assertEquals(List.of("SLOT_90"), keys(instance.getPageLootEntries(3, ITEMS_PER_PAGE)));
        assertEquals(68L, instance.getTotalStoredItems());
    }

    @Test
    void emptyAndOutOfRangePagesYieldNothing() {
        SpawnerInstance instance = spawner();
        instance.setSlotLoot(0, Material.ROTTEN_FLESH, 64);

        assertTrue(instance.getPageLootEntries(2, ITEMS_PER_PAGE).isEmpty());
        assertTrue(instance.getPageLootEntries(500, ITEMS_PER_PAGE).isEmpty());
        assertTrue(instance.getPageLootEntries(1, 0).isEmpty());
        assertEquals(List.of("SLOT_0"), keys(instance.getPageLootEntries(0, ITEMS_PER_PAGE)));
    }

    @Test
    void smallerPagesSplitTheSameStorageIntoMorePages() {
        SpawnerInstance instance = spawner();
        instance.setSlotLoot(0, Material.ROTTEN_FLESH, 64);
        instance.setSlotLoot(9, Material.BONE, 32);
        instance.setSlotLoot(18, Material.ARROW, 16);

        assertEquals(List.of("SLOT_0"), keys(instance.getPageLootEntries(1, 9)));
        assertEquals(List.of("SLOT_9"), keys(instance.getPageLootEntries(2, 9)));
        assertEquals(List.of("SLOT_18"), keys(instance.getPageLootEntries(3, 9)));
    }

    private static List<String> keys(List<SpawnerLootEntry> entries) {
        return entries.stream().map(SpawnerLootEntry::getKey).toList();
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
