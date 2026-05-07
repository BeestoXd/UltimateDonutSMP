package com.bx.ultimateDonutSmp.utils;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {

    public static ItemStack createItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (displayName != null && !displayName.isEmpty()) {
            meta.displayName(ColorUtils.toComponent(displayName));
        } else {
            meta.displayName(Component.empty());
        }

        if (lore != null && !lore.isEmpty()) {
            meta.lore(ColorUtils.toComponentList(lore));
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItem(Material material, String displayName) {
        return createItem(material, displayName, null);
    }

    public static ItemStack createPlayerHead(OfflinePlayer player, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta rawMeta = item.getItemMeta();
        if (!(rawMeta instanceof SkullMeta meta)) {
            return createItem(Material.PLAYER_HEAD, displayName, lore);
        }

        meta.setOwningPlayer(player);
        if (displayName != null && !displayName.isEmpty()) {
            meta.displayName(ColorUtils.toComponent(displayName));
        } else {
            meta.displayName(Component.empty());
        }

        if (lore != null && !lore.isEmpty()) {
            meta.lore(ColorUtils.toComponentList(lore));
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createPlaceholder(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createGlassPane() {
        return createPlaceholder(Material.GRAY_STAINED_GLASS_PANE);
    }

    public static ItemStack createGlassPane(Material material) {
        return createPlaceholder(material);
    }

    public static ItemStack fillWith(Material material, int size) {
        return createPlaceholder(material);
    }

    public static Material parseMaterial(String name) {
        if (name == null || name.isEmpty()) return Material.STONE;
        try {
            return Material.valueOf(name.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }

    public static ItemStack addEnchantments(ItemStack item, List<String> enchantmentStrings) {
        if (enchantmentStrings == null) return item;
        for (String entry : enchantmentStrings) {
            String[] parts = entry.split(":");
            if (parts.length < 2) continue;
            String name = parts[0].trim().toLowerCase();
            int level;
            try {
                level = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                continue;
            }
            Enchantment ench = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ENCHANTMENT)
                    .get(NamespacedKey.minecraft(name));
            if (ench != null) {
                item.addUnsafeEnchantment(ench, level);
            }
        }
        return item;
    }

    public static void fillInventory(org.bukkit.inventory.Inventory inventory, Material material) {
        ItemStack filler = createPlaceholder(material);
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    public static void fillInventory(org.bukkit.inventory.Inventory inventory) {
        fillInventory(inventory, Material.GRAY_STAINED_GLASS_PANE);
    }
}
