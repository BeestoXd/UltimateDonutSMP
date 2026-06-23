package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class OrdersItemStackModelTest {

    @Test
    void catalogAndDeliveryModelsDefensivelyCopyStacks() {
        ItemStack source = new ItemStack(Material.DIAMOND);
        source.setAmount(32);
        OrderCatalogEntry entry = new OrderCatalogEntry("items", source, "Diamond", "diamond items");
        DeliveryDraft draft = new DeliveryDraft(
                UUID.randomUUID(), 42L, List.of(source), 32, 320D, 1L
        );

        source.setAmount(1);
        ItemStack preview = entry.createPreviewItem();
        ItemStack delivered = draft.acceptedItems().getFirst();

        assertEquals(1, preview.getAmount());
        assertEquals(32, delivered.getAmount());
        assertNotSame(source, preview);
        assertNotSame(source, delivered);
    }
}
