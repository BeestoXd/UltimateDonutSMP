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

    @Test
    void bfsWaterReturnsEmptyOnNullStart() {
        AmethystToolsListener listener = new AmethystToolsListener(null);
        assertTrue(listener.bfsWater(null, 1, 27).isEmpty());
    }

    @Test
    void bfsWaterDrainsTwentySevenWaterBlocksInGrid() {
        AmethystToolsListener listener = new AmethystToolsListener(null);
        org.bukkit.World[] worldHolder = new org.bukkit.World[1];

        java.util.Map<String, org.bukkit.block.Block> blockCache = new java.util.HashMap<>();
        java.util.concurrent.atomic.AtomicReference<java.util.function.Function<int[], org.bukkit.block.Block>> makeBlockRef =
                new java.util.concurrent.atomic.AtomicReference<>();
        java.util.function.Function<int[], org.bukkit.block.Block> makeBlockImpl = coords -> {
            int x = coords[0];
            int y = coords[1];
            int z = coords[2];
            String key = x + "," + y + "," + z;
            return blockCache.computeIfAbsent(key, k -> (org.bukkit.block.Block) java.lang.reflect.Proxy.newProxyInstance(
                    org.bukkit.block.Block.class.getClassLoader(),
                    new Class<?>[]{org.bukkit.block.Block.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getX" -> x;
                        case "getY" -> y;
                        case "getZ" -> z;
                        case "getWorld" -> worldHolder[0];
                        case "getType" -> (Math.abs(x) <= 1 && Math.abs(y) <= 1 && Math.abs(z) <= 1)
                                ? org.bukkit.Material.WATER
                                : org.bukkit.Material.AIR;
                        case "getBlockData" -> null;
                        case "getLocation" -> new org.bukkit.Location(worldHolder[0], x, y, z);
                        case "getRelative" -> {
                            org.bukkit.block.BlockFace face = (org.bukkit.block.BlockFace) args[0];
                            yield makeBlockRef.get().apply(new int[]{x + face.getModX(), y + face.getModY(), z + face.getModZ()});
                        }
                        case "hashCode" -> key.hashCode();
                        case "equals" -> proxy == (args != null && args.length > 0 ? args[0] : null);
                        default -> null;
                    }
            ));
        };
        makeBlockRef.set(makeBlockImpl);

        org.bukkit.World world = (org.bukkit.World) java.lang.reflect.Proxy.newProxyInstance(
                org.bukkit.World.class.getClassLoader(),
                new Class<?>[]{org.bukkit.World.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getBlockAt")) {
                        int x = ((Number) args[0]).intValue();
                        int y = ((Number) args[1]).intValue();
                        int z = ((Number) args[2]).intValue();
                        return makeBlockRef.get().apply(new int[]{x, y, z});
                    }
                    if (method.getName().equals("hashCode")) {
                        return 1;
                    }
                    if (method.getName().equals("equals")) {
                        return proxy == (args != null && args.length > 0 ? args[0] : null);
                    }
                    return null;
                }
        );
        worldHolder[0] = world;

        org.bukkit.block.Block start = makeBlockRef.get().apply(new int[]{0, 0, 0});
        java.util.List<org.bukkit.block.Block> drained = listener.bfsWater(start, 1, 27);
        assertEquals(27, drained.size(), "Amethyst Bucket must drain exactly 27 water blocks in a 3x3x3 grid");
    }
}
