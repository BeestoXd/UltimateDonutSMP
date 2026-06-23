package com.bx.ultimateDonutSmp.models;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public record DeliveryQuote(
        boolean success,
        String failureCode,
        Order order,
        List<ItemStack> acceptedItems,
        List<ItemStack> returnedItems,
        int quantity,
        double payout
) {
    public DeliveryQuote {
        acceptedItems = copy(acceptedItems);
        returnedItems = copy(returnedItems);
    }

    private static List<ItemStack> copy(List<ItemStack> items) {
        return items == null ? List.of() : items.stream()
                .filter(java.util.Objects::nonNull)
                .map(ItemStack::clone)
                .toList();
    }
}
