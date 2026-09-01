package com.bx.ultimateDonutSmp.amethyst;

import org.bukkit.GameMode;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmethystToolsListenerTest {

    @Test
    void inventoryUpkeepStaysOffInCreative() {
        assertFalse(AmethystToolsListener.shouldManageInventory(GameMode.CREATIVE));
        assertTrue(AmethystToolsListener.shouldManageInventory(GameMode.SURVIVAL));
        assertTrue(AmethystToolsListener.shouldManageInventory(GameMode.ADVENTURE));
        assertTrue(AmethystToolsListener.shouldManageInventory(GameMode.SPECTATOR));
    }

    @Test
    void areaBreaksDropLootOutsideCreativeOnly() {
        assertFalse(AmethystToolsListener.shouldDropAoeLoot(GameMode.CREATIVE));
        assertTrue(AmethystToolsListener.shouldDropAoeLoot(GameMode.SURVIVAL));
        assertTrue(AmethystToolsListener.shouldDropAoeLoot(GameMode.ADVENTURE));
    }

    @Test
    void aDrunkBoosterComesFromTheHandThatDrankIt() {
        // Slot 40 is the off hand. Clearing the held slot instead would delete whatever the player
        // happened to be holding while they drank.
        assertEquals(40, AmethystToolsListener.consumedSlot(EquipmentSlot.OFF_HAND, 3));
        assertEquals(3, AmethystToolsListener.consumedSlot(EquipmentSlot.HAND, 3));
        assertEquals(8, AmethystToolsListener.consumedSlot(EquipmentSlot.HAND, 8));
    }

    @Test
    void anAbsentHandFallsBackToTheHeldSlot() {
        assertEquals(5, AmethystToolsListener.consumedSlot(null, 5));
    }
}
