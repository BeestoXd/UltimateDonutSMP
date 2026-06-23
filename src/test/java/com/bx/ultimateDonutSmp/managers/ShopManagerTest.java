package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.AuctionListing;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopManagerTest {

    private static final long NOW = 1_000_000L;
    private static final UUID BUYER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SELLER = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void selectsLowestMatchingAuctionUnitPrice() {
        AuctionListing single = listing(1, SELLER, Material.DIAMOND, 1, 90D, NOW + 5_000L);
        AuctionListing stack = listing(2, SELLER, Material.DIAMOND, 10, 500D, NOW + 5_000L);
        AuctionListing different = listing(3, SELLER, Material.EMERALD, 64, 64D, NOW + 5_000L);

        ShopManager.AuctionQuote quote = ShopManager.findBestAuctionQuote(
                List.of(single, stack, different),
                BUYER,
                new ItemStack(Material.DIAMOND),
                NOW,
                (candidate, desired) -> candidate.getType() == desired.getType()
        );

        assertEquals(2L, quote.listing().id());
        assertEquals(50D, quote.unitPrice());
    }

    @Test
    void ignoresOwnExpiredInactiveAndDifferentListings() {
        AuctionListing own = listing(1, BUYER, Material.DIAMOND, 64, 64D, NOW + 5_000L);
        AuctionListing expired = listing(2, SELLER, Material.DIAMOND, 64, 64D, NOW - 1L);
        AuctionListing sold = new AuctionListing(
                3,
                SELLER,
                "Seller",
                BUYER,
                AuctionListing.Status.SOLD,
                64D,
                0D,
                new ItemStack(Material.DIAMOND, 64),
                NOW - 100L,
                NOW + 5_000L,
                NOW,
                0L,
                0L,
                "ALL"
        );
        AuctionListing different = listing(4, SELLER, Material.EMERALD, 64, 64D, NOW + 5_000L);

        assertNull(ShopManager.findBestAuctionQuote(
                List.of(own, expired, sold, different),
                BUYER,
                new ItemStack(Material.DIAMOND),
                NOW,
                (candidate, desired) -> candidate.getType() == desired.getType()
        ));
    }

    @Test
    void recognizesManagedSpawnerRewardCommands() {
        assertTrue(ShopManager.isManagedSpawnerRewardCommand(
                "spawner give {username} pig {amount}"
        ));
        assertTrue(ShopManager.isManagedSpawnerRewardCommand(
                "/SPAWNER GIVE {player} iron_golem {amount}"
        ));
        assertFalse(ShopManager.isManagedSpawnerRewardCommand(
                "give {username} spawner {amount}"
        ));
    }

    @Test
    void managedSpawnerRewardNeverAlsoDeliversConfiguredVanillaItem() {
        ShopManager.ShopItem managedSpawner = new ShopManager.ShopItem(
                "PIG-SPAWNER-ITEM",
                "SHARD-MENU",
                Material.SPAWNER,
                "Pig Spawner",
                List.of(),
                9,
                250D,
                ShopManager.Currency.SHARD,
                "spawner give {username} pig {amount}",
                true,
                "",
                1,
                64,
                1,
                false,
                null,
                -1L
        );

        assertFalse(ShopManager.shouldDeliverConfiguredItem(managedSpawner));
    }

    private AuctionListing listing(
            long id,
            UUID seller,
            Material material,
            int amount,
            double price,
            long expiresAt
    ) {
        return new AuctionListing(
                id,
                seller,
                "Seller",
                null,
                AuctionListing.Status.ACTIVE,
                price,
                0D,
                new ItemStack(material, amount),
                NOW - id,
                expiresAt,
                0L,
                0L,
                0L,
                "ALL"
        );
    }
}
