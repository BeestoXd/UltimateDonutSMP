package com.bx.ultimateDonutSmp.models;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record AuctionClaim(
        long id,
        UUID ownerUuid,
        ClaimType claimType,
        long sourceListingId,
        double moneyAmount,
        ItemStack item,
        long createdAt,
        long claimedAt
) {

    public enum ClaimType {
        MONEY,
        ITEM;

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

    public boolean claimed() {
        return claimedAt > 0L;
    }

    public boolean moneyClaim() {
        return claimType == ClaimType.MONEY;
    }

    public boolean itemClaim() {
        return claimType == ClaimType.ITEM;
    }
}
