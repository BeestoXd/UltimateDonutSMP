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

    @Test
    void isWaterReturnsFalseForNull() {
        assertFalse(AmethystToolsListener.isWater(null));
    }

    @Test
    void isWaterIdentifiesWaterMaterial() {
        org.bukkit.block.Block block = (org.bukkit.block.Block) java.lang.reflect.Proxy.newProxyInstance(
                org.bukkit.block.Block.class.getClassLoader(),
                new Class<?>[]{org.bukkit.block.Block.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getType")) {
                        return org.bukkit.Material.WATER;
                    }
                    return null;
                }
        );
        assertTrue(AmethystToolsListener.isWater(block));
    }

    @Test
    void isWaterIdentifiesNonWaterMaterial() {
        org.bukkit.block.Block block = (org.bukkit.block.Block) java.lang.reflect.Proxy.newProxyInstance(
                org.bukkit.block.Block.class.getClassLoader(),
                new Class<?>[]{org.bukkit.block.Block.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getType")) {
                        return org.bukkit.Material.STONE;
                    }
                    if (method.getName().equals("getBlockData")) {
                        return null;
                    }
                    return null;
                }
        );
        assertFalse(AmethystToolsListener.isWater(block));
    }

    @Test
    void isWaterIdentifiesWaterloggedBlock() {
        org.bukkit.block.data.Waterlogged waterlogged = (org.bukkit.block.data.Waterlogged) java.lang.reflect.Proxy.newProxyInstance(
                org.bukkit.block.data.Waterlogged.class.getClassLoader(),
                new Class<?>[]{org.bukkit.block.data.Waterlogged.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("isWaterlogged")) {
                        return true;
                    }
                    return null;
                }
        );
        org.bukkit.block.Block block = (org.bukkit.block.Block) java.lang.reflect.Proxy.newProxyInstance(
                org.bukkit.block.Block.class.getClassLoader(),
                new Class<?>[]{org.bukkit.block.Block.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getType")) {
                        return org.bukkit.Material.OAK_STAIRS;
                    }
                    if (method.getName().equals("getBlockData")) {
                        return waterlogged;
                    }
                    return null;
                }
        );
        assertTrue(AmethystToolsListener.isWater(block));
    }
}
