package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.managers.SpawnerManager;
import com.bx.ultimateDonutSmp.models.SpawnerTypeDefinition;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpawnerMainMenuCapacityTest {

    @Test
    void emptySpawnerStorageYieldsZeroPercent() {
        assertEquals(0.0, SpawnerMainMenu.calculateFillPercentage(0L, 1000L));
        assertEquals("0.0", SpawnerMainMenu.formatFillPercentage(0.0));
    }

    @Test
    void halfFullStorageYieldsFiftyPercent() {
        assertEquals(50.0, SpawnerMainMenu.calculateFillPercentage(500L, 1000L));
        assertEquals("50.0", SpawnerMainMenu.formatFillPercentage(50.0));
    }

    @Test
    void completelyFullStorageDisplaysHundredPercent() {
        assertEquals(100.0, SpawnerMainMenu.calculateFillPercentage(1000L, 1000L));
        assertEquals("100.0", SpawnerMainMenu.formatFillPercentage(100.0));
    }

    @Test
    void overCapacityStorageIsClampedAtHundredPercent() {
        assertEquals(100.0, SpawnerMainMenu.calculateFillPercentage(2500L, 1000L));
        assertEquals("100.0", SpawnerMainMenu.formatFillPercentage(100.0));
    }

    @Test
    void zeroOrNegativeCapacitySafelyYieldsZeroPercent() {
        assertEquals(0.0, SpawnerMainMenu.calculateFillPercentage(100L, 0L));
        assertEquals(0.0, SpawnerMainMenu.calculateFillPercentage(100L, -50L));
        assertEquals(0.0, SpawnerMainMenu.calculateFillPercentage(-10L, 100L));
    }

    @Test
    void capacityCalculationAccountsForEveryEnabledDrop() {
        SpawnerTypeDefinition ironGolem = new SpawnerTypeDefinition(
                "IRON_GOLEM",
                "Iron Golem",
                EntityType.IRON_GOLEM,
                Material.IRON_INGOT,
                1L,
                3.7,
                null,
                List.of(
                        new SpawnerTypeDefinition.DropDefinition("IRON_INGOT", Material.IRON_INGOT, 3, 5, 1.0),
                        new SpawnerTypeDefinition.DropDefinition("POPPY", Material.POPPY, 0, 2, 0.45)
                )
        );

        // 2 active drops with 10,000 cap per loot key = 20,000 total capacity
        long twoDropsCapacity = SpawnerManager.calculateTotalStorageCapacity(ironGolem, Set.of(), 10_000L);
        assertEquals(20_000L, twoDropsCapacity);

        // When storage reaches 20,000 items, fill percentage is 100.0%
        double fillPercentage = SpawnerMainMenu.calculateFillPercentage(20_000L, twoDropsCapacity);
        assertEquals(100.0, fillPercentage);
        assertEquals("100.0", SpawnerMainMenu.formatFillPercentage(fillPercentage));
    }

    @Test
    void disabledDropsReduceTotalCapacityAccordingly() {
        SpawnerTypeDefinition ironGolem = new SpawnerTypeDefinition(
                "IRON_GOLEM",
                "Iron Golem",
                EntityType.IRON_GOLEM,
                Material.IRON_INGOT,
                1L,
                3.7,
                null,
                List.of(
                        new SpawnerTypeDefinition.DropDefinition("IRON_INGOT", Material.IRON_INGOT, 3, 5, 1.0),
                        new SpawnerTypeDefinition.DropDefinition("POPPY", Material.POPPY, 0, 2, 0.45)
                )
        );

        // Poppy disabled -> only Iron Ingot counts towards capacity (10,000 items)
        long singleDropCapacity = SpawnerManager.calculateTotalStorageCapacity(ironGolem, Set.of("POPPY"), 10_000L);
        assertEquals(10_000L, singleDropCapacity);

        // Once Iron Ingot fills 10,000 items, it hits 100.0%
        double fillPercentage = SpawnerMainMenu.calculateFillPercentage(10_000L, singleDropCapacity);
        assertEquals(100.0, fillPercentage);
        assertEquals("100.0", SpawnerMainMenu.formatFillPercentage(fillPercentage));
    }

    @Test
    void missingDefinitionOrAllDisabledDropsFallBackSafely() {
        long fallback = SpawnerManager.calculateTotalStorageCapacity(null, Set.of(), 5_000L);
        assertEquals(5_000L, fallback);

        SpawnerTypeDefinition singleDrop = new SpawnerTypeDefinition(
                "BLAZE",
                "Blaze",
                EntityType.BLAZE,
                Material.BLAZE_ROD,
                1L,
                3.7,
                null,
                List.of(new SpawnerTypeDefinition.DropDefinition("BLAZE_ROD", Material.BLAZE_ROD, 1, 1, 1.0))
        );

        // Even if all drops are disabled in filter, capacity falls back to definition size rather than zero
        long allDisabled = SpawnerManager.calculateTotalStorageCapacity(singleDrop, Set.of("BLAZE_ROD"), 5_000L);
        assertEquals(5_000L, allDisabled);
    }
}