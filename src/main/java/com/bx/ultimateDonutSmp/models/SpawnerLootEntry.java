package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;

public class SpawnerLootEntry {

    private final String key;
    private final Material material;
    private long amount;

    public SpawnerLootEntry(String key, Material material, long amount) {
        this.key = key == null ? "" : key.trim();
        this.material = material == null ? Material.STONE : material;
        this.amount = Math.max(0L, amount);
    }

    public String getKey() {
        return key;
    }

    public Material getMaterial() {
        return material;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = Math.max(0L, amount);
    }

    public void addAmount(long amount) {
        if (amount <= 0L) {
            return;
        }
        setAmount(this.amount + amount);
    }

    public long removeAmount(long amount) {
        if (amount <= 0L || this.amount <= 0L) {
            return 0L;
        }

        long removed = Math.min(this.amount, amount);
        this.amount -= removed;
        return removed;
    }
}
