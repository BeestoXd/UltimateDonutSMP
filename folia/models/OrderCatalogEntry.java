package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record OrderCatalogEntry(
        String categoryKey,
        Material material
) {

    public ItemStack createPreviewItem() {
        return new ItemStack(material);
    }
}
