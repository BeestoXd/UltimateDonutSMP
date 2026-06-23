package com.bx.ultimateDonutSmp.models;

import org.bukkit.inventory.ItemStack;

public record OrderCatalogEntry(
        String categoryKey,
        ItemStack previewItem,
        String displayName,
        String searchText
) {

    public OrderCatalogEntry(String categoryKey, org.bukkit.Material material) {
        this(
                categoryKey,
                material == null ? null : new ItemStack(material),
                material == null ? "" : ItemKey.niceName(material),
                material == null ? "" : material.name().toLowerCase(java.util.Locale.ROOT)
        );
    }

    public OrderCatalogEntry {
        previewItem = previewItem == null ? null : previewItem.clone();
        if (previewItem != null) {
            previewItem.setAmount(1);
        }
        displayName = displayName == null ? "" : displayName;
        searchText = searchText == null ? "" : searchText;
    }

    public org.bukkit.Material material() {
        return previewItem == null ? org.bukkit.Material.AIR : previewItem.getType();
    }

    public ItemStack createPreviewItem() {
        return previewItem == null ? null : previewItem.clone();
    }
}
