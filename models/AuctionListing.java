package com.bx.ultimateDonutSmp.models;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record AuctionListing(
        long id,
        UUID sellerUuid,
        String sellerName,
        UUID buyerUuid,
        Status status,
        double price,
        double tax,
        ItemStack item,
        long createdAt,
        long expiresAt,
        long soldAt,
        long cancelledAt,
        long expiredAt
) {

    public enum Status {
        ACTIVE,
        SOLD,
        EXPIRED,
        CANCELLED;

        public static Status fromDatabase(String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                return ACTIVE;
            }

            try {
                return Status.valueOf(rawValue.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return ACTIVE;
            }
        }
    }

    public boolean active() {
        return status == Status.ACTIVE;
    }

    public boolean sold() {
        return status == Status.SOLD;
    }

    public boolean expired() {
        return status == Status.EXPIRED;
    }

    public boolean cancelled() {
        return status == Status.CANCELLED;
    }

    public double sellerPayout() {
        return Math.max(0D, price - tax);
    }

    public long secondsRemaining(long nowMillis) {
        return Math.max(0L, (expiresAt - nowMillis) / 1000L);
    }
}
