package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class SpawnerInstance {

    public enum AccessMode {
        OWNER_ONLY,
        OWNER_AND_TEAM,
        PUBLIC;

        public static AccessMode fromString(String raw, AccessMode fallback) {
            if (raw == null || raw.isBlank()) {
                return fallback == null ? OWNER_ONLY : fallback;
            }

            String normalized = raw.trim().toUpperCase(Locale.US).replace('-', '_').replace(' ', '_');
            for (AccessMode value : values()) {
                if (value.name().equals(normalized)) {
                    return value;
                }
            }
            return fallback == null ? OWNER_ONLY : fallback;
        }
    }

    private long id;
    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private final UUID ownerUuid;
    private final String ownerNameSnapshot;
    private final String mobTypeKey;
    private long stackAmount;
    private AccessMode accessMode;
    private long lastProcessedAt;
    private final long createdAt;
    private long updatedAt;
    private final Map<String, SpawnerLootEntry> storedLoot = new LinkedHashMap<>();

    public SpawnerInstance(
            long id,
            String world,
            int x,
            int y,
            int z,
            UUID ownerUuid,
            String ownerNameSnapshot,
            String mobTypeKey,
            long stackAmount,
            AccessMode accessMode,
            long lastProcessedAt,
            long createdAt,
            long updatedAt
    ) {
        this.id = id;
        this.world = world == null ? "" : world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.ownerUuid = ownerUuid;
        this.ownerNameSnapshot = ownerNameSnapshot == null ? "" : ownerNameSnapshot;
        this.mobTypeKey = mobTypeKey == null ? "" : mobTypeKey.trim().toUpperCase(Locale.US);
        this.stackAmount = Math.max(1L, stackAmount);
        this.accessMode = accessMode == null ? AccessMode.OWNER_ONLY : accessMode;
        this.lastProcessedAt = Math.max(0L, lastProcessedAt);
        this.createdAt = Math.max(0L, createdAt);
        this.updatedAt = Math.max(0L, updatedAt);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerNameSnapshot() {
        return ownerNameSnapshot;
    }

    public String getMobTypeKey() {
        return mobTypeKey;
    }

    public long getStackAmount() {
        return stackAmount;
    }

    public void setStackAmount(long stackAmount) {
        this.stackAmount = Math.max(1L, stackAmount);
    }

    public void addStackAmount(long amount) {
        if (amount <= 0L) {
            return;
        }
        setStackAmount(this.stackAmount + amount);
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode accessMode) {
        this.accessMode = accessMode == null ? AccessMode.OWNER_ONLY : accessMode;
    }

    public long getLastProcessedAt() {
        return lastProcessedAt;
    }

    public void setLastProcessedAt(long lastProcessedAt) {
        this.lastProcessedAt = Math.max(0L, lastProcessedAt);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = Math.max(0L, updatedAt);
    }

    public List<SpawnerLootEntry> getStoredLootEntries() {
        return new ArrayList<>(storedLoot.values());
    }

    public SpawnerLootEntry getStoredLoot(String key) {
        if (key == null) {
            return null;
        }
        return storedLoot.get(key.toUpperCase(Locale.US));
    }

    public void setStoredLootEntries(Collection<SpawnerLootEntry> entries) {
        storedLoot.clear();
        if (entries == null) {
            return;
        }

        for (SpawnerLootEntry entry : entries) {
            if (entry == null || entry.getAmount() <= 0L) {
                continue;
            }
            storedLoot.put(entry.getKey().toUpperCase(Locale.US), entry);
        }
    }

    public void addStoredLoot(String key, Material material, long amount, long capPerKey) {
        if (key == null || material == null || amount <= 0L) {
            return;
        }

        String normalizedKey = key.toUpperCase(Locale.US);
        SpawnerLootEntry entry = storedLoot.computeIfAbsent(normalizedKey, ignored -> new SpawnerLootEntry(normalizedKey, material, 0L));
        long targetAmount = entry.getAmount() + amount;
        if (capPerKey > 0L) {
            targetAmount = Math.min(capPerKey, targetAmount);
        }
        entry.setAmount(targetAmount);
    }

    public long removeStoredLoot(String key, long amount) {
        SpawnerLootEntry entry = getStoredLoot(key);
        if (entry == null) {
            return 0L;
        }

        long removed = entry.removeAmount(amount);
        if (entry.getAmount() <= 0L) {
            storedLoot.remove(entry.getKey().toUpperCase(Locale.US));
        }
        return removed;
    }

    public void clearStoredLoot() {
        storedLoot.clear();
    }

    public long getTotalStoredItems() {
        long total = 0L;
        for (SpawnerLootEntry entry : storedLoot.values()) {
            total += entry.getAmount();
        }
        return total;
    }

    public String getLocationKey() {
        return buildLocationKey(world, x, y, z);
    }

    public static String buildLocationKey(String world, int x, int y, int z) {
        String worldName = world == null ? "" : world.toLowerCase(Locale.US);
        return worldName + ":" + x + ":" + y + ":" + z;
    }
}
