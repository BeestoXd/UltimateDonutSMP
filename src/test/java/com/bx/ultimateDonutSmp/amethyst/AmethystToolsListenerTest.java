package com.bx.ultimateDonutSmp.amethyst;

import org.bukkit.GameMode;
import org.junit.jupiter.api.Test;

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
}
