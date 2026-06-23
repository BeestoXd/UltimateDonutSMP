package com.bx.ultimateDonutSmp.models;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public record DeliveryRequest(
        long orderId,
        List<ItemStack> items,
        int expectedQuantity,
        double expectedPriceEach
) {
    public DeliveryRequest {
        items = items == null ? List.of() : items.stream()
                .filter(java.util.Objects::nonNull)
                .map(ItemStack::clone)
                .toList();
    }
}
