package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnerAutoDropGroupingTest {

    private static final int ITEMS_PER_PAGE = 45;

    // Material.getMaxStackSize() walks the item registry, and org.bukkit.Registry never finishes
    // initialising while Bukkit has no server. The registry proxy is built on first use because
    // building it needs the server to already be in place.
    @BeforeAll
    static void installServer() throws Exception {
        Field serverField = org.bukkit.Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        if (serverField.get(null) != null) {
            return;
        }

        Object[] registry = new Object[1];
        org.bukkit.Server server = (org.bukkit.Server) Proxy.newProxyInstance(
                org.bukkit.Server.class.getClassLoader(),
                new Class<?>[]{org.bukkit.Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getRegistry" -> {
                        if (registry[0] == null) {
                            Class<?> registryClass = Class.forName("org.bukkit.Registry");
                            registry[0] = Proxy.newProxyInstance(
                                    registryClass.getClassLoader(),
                                    new Class<?>[]{registryClass},
                                    (registryProxy, registryMethod, registryArgs) -> defaultValue(registryMethod));
                        }
                        yield registry[0];
                    }
                    default -> defaultValue(method);
                });

        serverField.set(null, server);
    }

    private static Object defaultValue(Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        return null;
    }

    @Test
    void alternatingDropsKeepEachMaterialInOneRun() {
        SpawnerInstance instance = spawner();

        for (int round = 0; round < 4; round++) {
            instance.addAutoMobDrop(Material.ARROW, 64L, 0L);
            instance.addAutoMobDrop(Material.BONE, 64L, 0L);
        }

        assertEquals(
                List.of(
                        Material.ARROW, Material.ARROW, Material.ARROW, Material.ARROW,
                        Material.BONE, Material.BONE, Material.BONE, Material.BONE
                ),
                materials(instance.getPageLootEntries(1, ITEMS_PER_PAGE))
        );
    }

    @Test
    void theNextDropTidiesStorageThatIsAlreadyInterleaved() {
        SpawnerInstance instance = spawner();
        instance.setStoredLootEntries(List.of(
                slot(0, Material.ARROW, 64),
                slot(1, Material.BONE, 64),
                slot(2, Material.ARROW, 64),
                slot(3, Material.BONE, 64)
        ));

        instance.addAutoMobDrop(Material.ARROW, 64L, 0L);

        assertEquals(
                List.of(Material.ARROW, Material.ARROW, Material.ARROW, Material.BONE, Material.BONE),
                materials(instance.getPageLootEntries(1, ITEMS_PER_PAGE))
        );
    }

    @Test
    void groupingClosesTheGapsThatSpreadStorageOverExtraPages() {
        SpawnerInstance instance = spawner();
        instance.setStoredLootEntries(List.of(
                slot(0, Material.ARROW, 64),
                slot(90, Material.BONE, 32)
        ));

        instance.addAutoMobDrop(Material.ARROW, 10L, 0L);

        assertEquals(
                List.of(Material.ARROW, Material.ARROW, Material.BONE),
                materials(instance.getPageLootEntries(1, ITEMS_PER_PAGE))
        );
        assertTrue(instance.getPageLootEntries(2, ITEMS_PER_PAGE).isEmpty());
        assertTrue(instance.getPageLootEntries(3, ITEMS_PER_PAGE).isEmpty());
        assertEquals(106L, instance.getTotalStoredItems());
    }

    @Test
    void groupingLeavesTheStoredAmountsAlone() {
        SpawnerInstance instance = spawner();

        instance.addAutoMobDrop(Material.ARROW, 100L, 0L);
        instance.addAutoMobDrop(Material.BONE, 30L, 0L);
        instance.addAutoMobDrop(Material.ARROW, 30L, 0L);

        assertEquals(160L, instance.getTotalStoredItems());
        assertEquals(
                List.of(Material.ARROW, Material.ARROW, Material.ARROW, Material.BONE),
                materials(instance.getPageLootEntries(1, ITEMS_PER_PAGE))
        );
        // the second arrow stack was topped up to a full 64 before the leftover 2 opened a third
        assertEquals(List.of(64L, 64L, 2L, 30L), amounts(instance.getPageLootEntries(1, ITEMS_PER_PAGE)));
    }

    @Test
    void groupingDoesNotLetAMaterialPastItsStorageCap() {
        SpawnerInstance instance = spawner();

        instance.addAutoMobDrop(Material.ARROW, 100L, 64L);
        instance.addAutoMobDrop(Material.ARROW, 100L, 64L);

        assertEquals(64L, instance.getTotalStoredItems());
    }

    private static List<Material> materials(List<SpawnerLootEntry> entries) {
        return entries.stream().map(SpawnerLootEntry::getMaterial).toList();
    }

    private static List<Long> amounts(List<SpawnerLootEntry> entries) {
        return entries.stream().map(SpawnerLootEntry::getAmount).toList();
    }

    private static SpawnerLootEntry slot(int slotIndex, Material material, long amount) {
        return new SpawnerLootEntry("SLOT_" + slotIndex, material, amount);
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
                "SKELETON",
                1L,
                SpawnerInstance.AccessMode.OWNER_ONLY,
                0L,
                0L,
                0L
        );
    }
}
