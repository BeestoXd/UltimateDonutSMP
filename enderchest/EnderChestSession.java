package com.bx.ultimateDonutSmp.enderchest;

import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class EnderChestSession {

    private final UUID ownerUuid;
    private final Inventory inventory;
    private final int rows;
    private boolean dirty;
    private long lastSaveMillis;

    public EnderChestSession(UUID ownerUuid, Inventory inventory, int rows) {
        this.ownerUuid = ownerUuid;
        this.inventory = inventory;
        this.rows = rows;
        this.dirty = false;
        this.lastSaveMillis = System.currentTimeMillis();
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getRows() {
        return rows;
    }

    public boolean isDirty() {
        return dirty;
    }

    public long getLastSaveMillis() {
        return lastSaveMillis;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void markSaved() {
        this.dirty = false;
        this.lastSaveMillis = System.currentTimeMillis();
    }
}
