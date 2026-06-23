package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;
import org.bukkit.potion.PotionType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrdersModelTest {

    @Test
    void uiStateNormalizesPersistedAndSessionValues() {
        UUID playerId = UUID.randomUUID();
        OrderUiState state = new OrderUiState(playerId, OrderSort.RECENTLY_LISTED, " food ", OrderAlphaSort.Z_A);

        state.page(-4);
        state.itemPage(-2);
        state.search("  diamond sword ");
        state.itemSearch("  potion ");

        assertEquals(playerId, state.playerUuid());
        assertEquals(OrderSort.RECENTLY_LISTED, state.sort());
        assertEquals("FOOD", state.filter());
        assertEquals(OrderAlphaSort.Z_A, state.itemSort());
        assertEquals(0, state.page());
        assertEquals(0, state.itemPage());
        assertEquals("diamond sword", state.search());
        assertEquals("potion", state.itemSearch());
    }

    @Test
    void potionVariantRoundTripsWithoutLosingIdentity() {
        ItemKey key = ItemKey.potion(Material.SPLASH_POTION, PotionType.STRONG_HEALING);
        ItemKey restored = ItemKey.deserialize(key.serialize());

        assertEquals(Material.SPLASH_POTION, restored.material);
        assertEquals(PotionType.STRONG_HEALING, restored.potionType);
        assertEquals(key.serialize(), restored.serialize());
    }

    @Test
    void orderRemainingQuantityNeverBecomesNegative() {
        Order order = new Order(
                1L,
                UUID.randomUUID(),
                "Owner",
                null,
                "STONE",
                "BLOCKS",
                OrderStatus.FILLED,
                10,
                12,
                10,
                5D,
                50D,
                50D,
                0D,
                1L,
                2L,
                3L
        );

        assertEquals(0, order.remainingQuantity());
    }
}
