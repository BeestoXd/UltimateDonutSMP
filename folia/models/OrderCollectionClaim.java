package com.bx.ultimateDonutSmp.models;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record OrderCollectionClaim(
        long id,
        UUID ownerUuid,
        long orderId,
        ClaimType claimType,
        ItemStack item,
        double moneyAmount,
        long createdAt,
        long claimedAt
) {

    public enum ClaimType {
        ITEM,
        REFUND;

        public static ClaimType fromDatabase(String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                return ITEM;
            }

            try {
                return ClaimType.valueOf(rawValue.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return ITEM;
            }
        }
    }

    public boolean itemClaim() {
        return claimType == ClaimType.ITEM;
    }

    public boolean refundClaim() {
        return claimType == ClaimType.REFUND;
    }

    public boolean claimed() {
        return claimedAt > 0L;
    }
}
