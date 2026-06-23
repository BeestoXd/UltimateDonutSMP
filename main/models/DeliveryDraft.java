package com.bx.ultimateDonutSmp.models;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public record DeliveryDraft(
        UUID playerUuid,
        long orderId,
        List<ItemStack> acceptedItems,
        int quantity,
        double payout,
        long createdAt
) {
    public DeliveryDraft {
        acceptedItems = copy(acceptedItems);
    }

    private static List<ItemStack> copy(List<ItemStack> items) {
        return items == null ? List.of() : items.stream()
                .filter(java.util.Objects::nonNull)
                .map(ItemStack::clone)
                .toList();
    }
}
