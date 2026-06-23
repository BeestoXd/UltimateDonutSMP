package com.bx.ultimateDonutSmp.models;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record Order(
        long id,
        UUID ownerUuid,
        String ownerName,
        ItemStack requestedItem,
        String requestedMaterialKey,
        String categoryKey,
        OrderStatus status,
        int requestedQuantity,
        int deliveredQuantity,
        int collectedQuantity,
        double priceEach,
        double totalBudget,
        double paidAmount,
        double escrowRemaining,
        long createdAt,
        long expiresAt,
        long closedAt
) {

    public boolean active() {
        return status == OrderStatus.ACTIVE;
    }

    public boolean filled() {
        return status == OrderStatus.FILLED;
    }

    public boolean expired() {
        return status == OrderStatus.EXPIRED;
    }

    public boolean cancelled() {
        return status == OrderStatus.CANCELLED;
    }

    public boolean closed() {
        return status != OrderStatus.ACTIVE;
    }

    public int remainingQuantity() {
        return Math.max(0, requestedQuantity - deliveredQuantity);
    }

    public long secondsRemaining(long nowMillis) {
        return Math.max(0L, (expiresAt - nowMillis) / 1000L);
    }

    public double progressPercent() {
        if (requestedQuantity <= 0) {
            return 0D;
        }
        return Math.min(100D, (deliveredQuantity * 100D) / requestedQuantity);
    }
}
