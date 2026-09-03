package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.models.Home;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeMenuSlotResolutionTest {

    private final UUID playerUuid = UUID.randomUUID();

    @Test
    void parseHomeSlotHandlesStandardAliases() {
        assertEquals(0, HomeMenu.parseHomeSlot("home"));
        assertEquals(0, HomeMenu.parseHomeSlot("Home"));
        assertEquals(0, HomeMenu.parseHomeSlot("HOME"));
        assertEquals(0, HomeMenu.parseHomeSlot("home1"));
        assertEquals(0, HomeMenu.parseHomeSlot("Home1"));
        assertEquals(0, HomeMenu.parseHomeSlot("1"));

        assertEquals(1, HomeMenu.parseHomeSlot("home2"));
        assertEquals(1, HomeMenu.parseHomeSlot("Home2"));
        assertEquals(1, HomeMenu.parseHomeSlot("2"));

        assertEquals(2, HomeMenu.parseHomeSlot("home3"));
        assertEquals(4, HomeMenu.parseHomeSlot("home5"));
        assertEquals(9, HomeMenu.parseHomeSlot("home10"));
    }

    @Test
    void parseHomeSlotRejectsNonNumberedOrInvalidNames() {
        assertEquals(-1, HomeMenu.parseHomeSlot("base"));
        assertEquals(-1, HomeMenu.parseHomeSlot("farm"));
        assertEquals(-1, HomeMenu.parseHomeSlot("homestead"));
        assertEquals(-1, HomeMenu.parseHomeSlot("home0"));
        assertEquals(-1, HomeMenu.parseHomeSlot("0"));
        assertEquals(-1, HomeMenu.parseHomeSlot("-1"));
        assertEquals(-1, HomeMenu.parseHomeSlot(null));
        assertEquals(-1, HomeMenu.parseHomeSlot("   "));
    }

    @Test
    void emptyHomesReturnsEmptyMap() {
        Map<Integer, Home> resolved = HomeMenu.resolveHomeSlots(List.of());
        assertTrue(resolved.isEmpty());
    }

    @Test
    void sequentialHomesMapToSequentialSlots() {
        Home h1 = new Home(playerUuid, "home", null, 100L);
        Home h2 = new Home(playerUuid, "home2", null, 200L);
        Home h3 = new Home(playerUuid, "home3", null, 300L);

        Map<Integer, Home> resolved = HomeMenu.resolveHomeSlots(List.of(h1, h2, h3));

        assertEquals(h1, resolved.get(0));
        assertEquals(h2, resolved.get(1));
        assertEquals(h3, resolved.get(2));
    }

    @Test
    void creatingHome2WithoutHome1StaysInSlot2() {
        Home h2 = new Home(playerUuid, "home2", null, 100L);

        Map<Integer, Home> resolved = HomeMenu.resolveHomeSlots(List.of(h2));

        assertNull(resolved.get(0), "Slot 1 should remain empty");
        assertEquals(h2, resolved.get(1), "Home 2 should be in slot index 1");
        assertNull(resolved.get(2), "Slot 3 should remain empty");
    }

    @Test
    void deletingMiddleHomeLeavesSlotEmptyWithoutMovingSubsequentHomes() {
        Home h1 = new Home(playerUuid, "home", null, 100L);
        Home h3 = new Home(playerUuid, "home3", null, 300L);

        Map<Integer, Home> resolved = HomeMenu.resolveHomeSlots(List.of(h1, h3));

        assertEquals(h1, resolved.get(0), "Home 1 should remain in slot index 0");
        assertNull(resolved.get(1), "Slot index 1 (Home 2) should be empty");
        assertEquals(h3, resolved.get(2), "Home 3 should stay in slot index 2");
    }

    @Test
    void customNamedHomesFillLowestUnclaimedSlots() {
        Home farm = new Home(playerUuid, "farm", null, 100L);
        Home base = new Home(playerUuid, "base", null, 200L);

        Map<Integer, Home> resolved = HomeMenu.resolveHomeSlots(List.of(farm, base));

        assertEquals(farm, resolved.get(0));
        assertEquals(base, resolved.get(1));
    }

    @Test
    void mixedCustomAndNumberedHomesPreserveExplicitSlots() {
        Home h1 = new Home(playerUuid, "home", null, 100L);
        Home h3 = new Home(playerUuid, "home3", null, 200L);
        Home farm = new Home(playerUuid, "farm", null, 300L);

        Map<Integer, Home> resolved = HomeMenu.resolveHomeSlots(List.of(h1, h3, farm));

        assertEquals(h1, resolved.get(0), "Home 1 should be in slot 0");
        assertEquals(farm, resolved.get(1), "Farm should take the lowest unclaimed slot (slot 1)");
        assertEquals(h3, resolved.get(2), "Home 3 should stay in slot 2");
    }

    @Test
    void multipleHomesTargetingSameSlotDoNotCollideOrOverwrite() {
        Home h2Named = new Home(playerUuid, "home2", null, 100L);
        Home h2Number = new Home(playerUuid, "2", null, 200L);

        Map<Integer, Home> resolved = HomeMenu.resolveHomeSlots(List.of(h2Named, h2Number));

        assertEquals(2, resolved.size());
        assertEquals(h2Named, resolved.get(1));
        assertEquals(h2Number, resolved.get(0), "Second home should be placed into next free slot");
    }
}
