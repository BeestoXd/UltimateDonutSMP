package com.bx.ultimateDonutSmp.enderchest;

import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class EnderChestInspectionSession {

    private final UUID viewerUuid;
    private final UUID targetUuid;
    private final String targetName;
    private final Inventory inventory;
    private long lastSyncMillis;

    public EnderChestInspectionSession(UUID viewerUuid, UUID targetUuid, String targetName, Inventory inventory) {
        this.viewerUuid = viewerUuid;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.inventory = inventory;
        this.lastSyncMillis = System.currentTimeMillis();
    }

    public UUID getViewerUuid() {
        return viewerUuid;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public long getLastSyncMillis() {
        return lastSyncMillis;
    }

    public void markSynced() {
        this.lastSyncMillis = System.currentTimeMillis();
    }
}
