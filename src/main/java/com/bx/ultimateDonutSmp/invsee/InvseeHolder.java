package com.bx.ultimateDonutSmp.invsee;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class InvseeHolder implements InventoryHolder {

    private final UUID viewerUuid;
    private final UUID targetUuid;
    private Inventory inventory;

    public InvseeHolder(UUID viewerUuid, UUID targetUuid) {
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
