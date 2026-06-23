package com.bx.ultimateDonutSmp.models;

import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionBrowseEngineTest {

    private static final long NOW = 1_000_000L;

    @Test
    void sortsFiltersSearchesAndPaginatesActiveListings() {
        AuctionListing sword = listing(1, Material.DIAMOND_SWORD, 300D, NOW - 100, NOW + 5_000);
        AuctionListing block = listing(2, Material.GRASS_BLOCK, 100D, NOW - 300, NOW + 3_000);
        AuctionListing food = listing(3, Material.GOLDEN_CARROT, 200D, NOW - 200, NOW + 4_000);
        AuctionListing expired = listing(4, Material.DIAMOND_PICKAXE, 50D, NOW - 400, NOW - 1);
        List<AuctionListing> source = List.of(sword, block, food, expired);

        AuctionPage cheapest = AuctionBrowseEngine.page(
                source,
                new AuctionBrowseRequest(
                        1,
                        AuctionHouseManager.AuctionSort.PRICE_LOWEST,
                        AuctionCategory.ALL,
                        ""
                ),
                2,
                NOW,
                listing -> listing.item().getType().name()
        );
        assertEquals(List.of(block, food), cheapest.listings());
        assertEquals(2, cheapest.totalPages());
        assertEquals(3, cheapest.totalListings());

        List<AuctionListing> combat = AuctionBrowseEngine.filter(
                source,
                new AuctionBrowseRequest(
                        1,
                        AuctionHouseManager.AuctionSort.NEWEST,
                        AuctionCategory.COMBAT,
                        "diamond sword"
                ),
                NOW,
                listing -> listing.item().getType().name().replace('_', ' ')
        );
        assertEquals(List.of(sword), combat);
    }

    @Test
    void validatesCategoriesPriceDurationAndClaimVisibility() {
        assertTrue(AuctionCategory.BLOCKS.matches(Material.STONE, true, false));
        assertTrue(AuctionCategory.POTIONS.matches(Material.SPLASH_POTION, false, false));
        assertFalse(AuctionCategory.FOOD.matches(Material.DIAMOND_SWORD, false, false));

        assertTrue(AuctionHouseManager.isPriceAllowed(100D, 100D, 1_000D));
        assertFalse(AuctionHouseManager.isPriceAllowed(Double.NaN, 100D, 1_000D));
        assertFalse(AuctionHouseManager.isPriceAllowed(1_001D, 100D, 1_000D));
        assertTrue(AuctionHouseManager.isDurationAllowed(48, List.of(24, 48, 72)));
        assertFalse(AuctionHouseManager.isDurationAllowed(12, List.of(24, 48, 72)));

        AuctionListing listing = listing(5, Material.CHEST, 100D, NOW, NOW + 10_000);
        AuctionClaim claim = new AuctionClaim(
                9,
                listing.sellerUuid(),
                AuctionClaim.ClaimType.ITEM,
                listing.id(),
                0D,
                new ItemStack(Material.CHEST),
                NOW,
                0L
        );
        assertEquals(2, AuctionPlayerEntries.combine(List.of(listing), List.of(claim), true).size());
        assertEquals(List.of(listing), AuctionPlayerEntries.combine(List.of(listing), List.of(claim), false));
    }

    @Test
    void alignsMyAuctionControlsToTheDonutLayout() {
        YamlConfiguration legacy = new YamlConfiguration();
        legacy.set("GUI.PLAYER_ITEMS.CONTROLS.BACK.SLOT", 49);
        legacy.set("GUI.PLAYER_ITEMS.CONTROLS.REFRESH.SLOT", 50);
        legacy.set("GUI.PLAYER_ITEMS.CONTROLS.PAGE.SLOT", 51);

        assertTrue(AuctionHouseManager.migrateMyAuctionControlSlots(legacy));
        assertEquals(49, legacy.getInt("GUI.PLAYER_ITEMS.CONTROLS.BACK.SLOT"));
        assertEquals(48, legacy.getInt("GUI.PLAYER_ITEMS.CONTROLS.REFRESH.SLOT"));
        assertEquals(50, legacy.getInt("GUI.PLAYER_ITEMS.CONTROLS.PAGE.SLOT"));

        assertFalse(AuctionHouseManager.migrateMyAuctionControlSlots(legacy));
    }

    private static AuctionListing listing(
            long id,
            Material material,
            double price,
            long createdAt,
            long expiresAt
    ) {
        return new AuctionListing(
                id,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Seller",
                null,
                AuctionListing.Status.ACTIVE,
                price,
                0D,
                new ItemStack(material),
                createdAt,
                expiresAt,
                0L,
                0L,
                0L,
                AuctionCategory.ALL.name()
        );
    }
}
