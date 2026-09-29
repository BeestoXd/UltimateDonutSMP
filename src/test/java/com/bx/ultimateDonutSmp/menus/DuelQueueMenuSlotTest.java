package com.bx.ultimateDonutSmp.menus;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DuelQueueMenuSlotTest {

    @Test
    void size27WithSelectorFitsWithinBoundsAndSlotsDoNotOverlap() {
        int size = 27;
        int queueSlot = DuelQueueMenu.resolveQueueSlot(size);
        int statsSlot = DuelQueueMenu.resolveStatsSlot(size);
        int selectSlot = DuelQueueMenu.resolveSelectSlot(size);
        int claimsSlot = DuelQueueMenu.resolveClaimsSlot(size, true);
        int closeSlot = size - 1;

        assertEquals(11, queueSlot);
        assertEquals(13, statsSlot);
        assertEquals(15, selectSlot);
        assertEquals(22, claimsSlot);
        assertEquals(26, closeSlot);

        List<Integer> slots = List.of(queueSlot, statsSlot, selectSlot, claimsSlot, closeSlot);
        for (int slot : slots) {
            assertTrue(slot >= 0 && slot < size, "Slot " + slot + " must be within bounds [0, " + size + ")");
        }

        Set<Integer> uniqueSlots = new HashSet<>(slots);
        assertEquals(slots.size(), uniqueSlots.size(), "All active slots must be distinct");
    }

    @Test
    void size27WithoutSelectorFitsWithinBoundsAndSlotsDoNotOverlap() {
        int size = 27;
        int queueSlot = DuelQueueMenu.resolveQueueSlot(size);
        int statsSlot = DuelQueueMenu.resolveStatsSlot(size);
        int claimsSlot = DuelQueueMenu.resolveClaimsSlot(size, false);
        int closeSlot = size - 1;

        assertEquals(11, queueSlot);
        assertEquals(13, statsSlot);
        assertEquals(15, claimsSlot);
        assertEquals(26, closeSlot);

        List<Integer> slots = List.of(queueSlot, statsSlot, claimsSlot, closeSlot);
        for (int slot : slots) {
            assertTrue(slot >= 0 && slot < size, "Slot " + slot + " must be within bounds [0, " + size + ")");
        }

        Set<Integer> uniqueSlots = new HashSet<>(slots);
        assertEquals(slots.size(), uniqueSlots.size(), "All active slots must be distinct");
    }

    @Test
    void size36And54PreserveLegacySlotLayout() {
        for (int size : List.of(36, 54)) {
            int queueSlot = DuelQueueMenu.resolveQueueSlot(size);
            int statsSlot = DuelQueueMenu.resolveStatsSlot(size);
            int selectSlot = DuelQueueMenu.resolveSelectSlot(size);
            int claimsWithSelector = DuelQueueMenu.resolveClaimsSlot(size, true);
            int claimsWithoutSelector = DuelQueueMenu.resolveClaimsSlot(size, false);

            assertEquals(20, queueSlot);
            assertEquals(22, statsSlot);
            assertEquals(24, selectSlot);
            assertEquals(31, claimsWithSelector);
            assertEquals(24, claimsWithoutSelector);

            assertTrue(claimsWithSelector < size);
            assertTrue(claimsWithoutSelector < size);
        }
    }

    @Test
    void allSupportedSizesFitWithinBounds() {
        for (int size : List.of(9, 18, 27, 36, 45, 54)) {
            for (boolean showSelector : List.of(true, false)) {
                int queueSlot = DuelQueueMenu.resolveQueueSlot(size);
                int statsSlot = DuelQueueMenu.resolveStatsSlot(size);
                int claimsSlot = DuelQueueMenu.resolveClaimsSlot(size, showSelector);
                int closeSlot = size - 1;

                assertTrue(queueSlot >= 0 && queueSlot < size);
                assertTrue(statsSlot >= 0 && statsSlot < size);
                assertTrue(claimsSlot >= 0 && claimsSlot < size);
                assertTrue(closeSlot >= 0 && closeSlot < size);

                if (showSelector) {
                    int selectSlot = DuelQueueMenu.resolveSelectSlot(size);
                    assertTrue(selectSlot >= 0 && selectSlot < size);
                    Set<Integer> unique = new HashSet<>(List.of(queueSlot, statsSlot, selectSlot, claimsSlot, closeSlot));
                    assertEquals(5, unique.size(), "All 5 slots must be distinct for size " + size);
                } else {
                    Set<Integer> unique = new HashSet<>(List.of(queueSlot, statsSlot, claimsSlot, closeSlot));
                    assertEquals(4, unique.size(), "All 4 slots must be distinct for size " + size);
                }
            }
        }
    }
}