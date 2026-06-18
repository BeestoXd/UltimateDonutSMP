package com.bx.ultimateDonutSmp.enderchest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class EnderChestInspectionHolder implements InventoryHolder {

    private final UUID viewerUuid;
    private final UUID targetUuid;
    private Inventory inventory;

    public EnderChestInspectionHolder(UUID viewerUuid, UUID targetUuid) {
        this.viewerUuid = viewerUuid;
        this.targetUuid = targetUuid;
    }

    public UUID getViewerUuid() {
        return viewerUuid;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
