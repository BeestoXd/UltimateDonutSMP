package com.bx.ultimateDonutSmp.models;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public record DuelClaim(
        long matchId,
        UUID playerUuid,
        String defeatedName,
        List<ItemStack> items,
        long createdAt
) {
    public int itemCount() {
        return items == null ? 0 : items.size();
    }
}
