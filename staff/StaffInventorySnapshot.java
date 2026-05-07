package com.bx.ultimateDonutSmp.staff;

import org.bukkit.inventory.ItemStack;

public final class StaffInventorySnapshot {

    private final ItemStack[] storageContents;
    private final ItemStack[] armorContents;
    private final ItemStack offhandItem;

    public StaffInventorySnapshot(ItemStack[] storageContents, ItemStack[] armorContents, ItemStack offhandItem) {
        this.storageContents = cloneArray(storageContents, 36);
        this.armorContents = cloneArray(armorContents, 4);
        this.offhandItem = cloneItem(offhandItem);
    }

    public ItemStack[] getStorageContents() {
        return cloneArray(storageContents, 36);
    }

    public ItemStack[] getArmorContents() {
        return cloneArray(armorContents, 4);
    }

    public ItemStack getOffhandItem() {
        return cloneItem(offhandItem);
    }

    private static ItemStack cloneItem(ItemStack item) {
        return item == null ? null : item.clone();
    }

    private static ItemStack[] cloneArray(ItemStack[] source, int size) {
        ItemStack[] cloned = new ItemStack[size];
        if (source == null) {
            return cloned;
        }

        for (int index = 0; index < Math.min(size, source.length); index++) {
            cloned[index] = cloneItem(source[index]);
        }
        return cloned;
    }
}
