package com.bx.ultimateDonutSmp.enderchest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class EnderChestHolder implements InventoryHolder {

    private final UUID ownerUuid;
    private final int rows;
    private Inventory inventory;

    public EnderChestHolder(UUID ownerUuid, int rows) {
        this.ownerUuid = ownerUuid;
        this.rows = rows;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public int getRows() {
        return rows;
    }

    public void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
