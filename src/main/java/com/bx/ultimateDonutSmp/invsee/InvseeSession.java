package com.bx.ultimateDonutSmp.invsee;

import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class InvseeSession {

    private final UUID viewerUuid;
    private final UUID targetUuid;
    private final Inventory inventory;
    private final String targetName;
    private boolean editable;
    private boolean frozen;
    private boolean writeBackScheduled;
    private long lastSyncMillis;

    public InvseeSession(UUID viewerUuid, UUID targetUuid, Inventory inventory, String targetName, boolean editable) {
        this.viewerUuid = viewerUuid;
        this.targetUuid = targetUuid;
        this.inventory = inventory;
        this.targetName = targetName;
        this.editable = editable;
        this.frozen = false;
        this.writeBackScheduled = false;
        this.lastSyncMillis = System.currentTimeMillis();
    }

    public UUID getViewerUuid() {
        return viewerUuid;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public String getTargetName() {
        return targetName;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public boolean isWriteBackScheduled() {
        return writeBackScheduled;
    }

    public long getLastSyncMillis() {
        return lastSyncMillis;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void markSynced() {
        this.lastSyncMillis = System.currentTimeMillis();
    }

    public void scheduleWriteBack() {
        this.writeBackScheduled = true;
    }

    public void completeWriteBack() {
        this.writeBackScheduled = false;
    }

    public void freeze() {
        this.frozen = true;
        this.writeBackScheduled = false;
    }
}
