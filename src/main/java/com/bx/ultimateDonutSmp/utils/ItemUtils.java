package com.bx.ultimateDonutSmp.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class ItemUtils {

    public static ItemStack createItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(ColorUtils.colorize(displayName));
        } else {
            meta.setDisplayName("");
        }

        if (lore != null && !lore.isEmpty()) {
            meta.setLore(ColorUtils.colorizeList(lore));
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItem(Material material, String displayName) {
        return createItem(material, displayName, null);
    }

    public static ItemStack createPlayerHead(OfflinePlayer player, String displayName, List<String> lore) {
        return createPlayerHead(player, null, displayName, lore);
    }

    public static ItemStack createPlayerHead(
            OfflinePlayer player,
            String textureValue,
            String displayName,
            List<String> lore
    ) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta rawMeta = item.getItemMeta();
        if (!(rawMeta instanceof SkullMeta meta)) {
            return createItem(Material.PLAYER_HEAD, displayName, lore);
        }

        if (!applyTextureProfile(meta, player, textureValue)) {
            meta.setOwningPlayer(player);
        }
        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(ColorUtils.colorize(displayName));
        } else {
            meta.setDisplayName("");
        }

        if (lore != null && !lore.isEmpty()) {
            meta.setLore(ColorUtils.colorizeList(lore));
        }

        item.setItemMeta(meta);
        return item;
    }

    private static boolean applyTextureProfile(SkullMeta meta, OfflinePlayer fallback, String textureValue) {
        TextureProfileData data = decodeTextureProfile(textureValue);
        if (data == null) {
            return false;
        }

        try {
            UUID profileId = data.profileId() != null
                    ? data.profileId()
                    : UUID.nameUUIDFromBytes(("uds-head:" + textureValue).getBytes(StandardCharsets.UTF_8));
            String profileName = data.profileName();
            if (profileName == null || profileName.isBlank() || profileName.length() > 16) {
                profileName = fallback == null ? null : fallback.getName();
            }

            PlayerProfile profile = Bukkit.createPlayerProfile(profileId, profileName);
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(data.skinUrl());
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
            return true;
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static TextureProfileData decodeTextureProfile(String textureValue) {
        if (textureValue == null || textureValue.isBlank()) {
            return null;
        }

        try {
            byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(textureValue);
            } catch (IllegalArgumentException ignored) {
                decoded = Base64.getUrlDecoder().decode(textureValue);
            }
            JsonObject root = JsonParser.parseString(new String(decoded, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            JsonObject textures = root.getAsJsonObject("textures");
            JsonObject skin = textures == null ? null : textures.getAsJsonObject("SKIN");
            if (skin == null || !skin.has("url")) {
                return null;
            }

            URL skinUrl = URI.create(skin.get("url").getAsString()).toURL();
            UUID profileId = parseProfileUuid(root.has("profileId") ? root.get("profileId").getAsString() : null);
            String profileName = root.has("profileName") ? root.get("profileName").getAsString() : null;
            return new TextureProfileData(profileId, profileName, skinUrl);
        } catch (Exception | LinkageError ignored) {
            return null;
        }
    }

    private static UUID parseProfileUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String compact = value.replace("-", "");
        if (compact.length() != 32) {
            return null;
        }
        try {
            return UUID.fromString(compact.substring(0, 8)
                    + "-" + compact.substring(8, 12)
                    + "-" + compact.substring(12, 16)
                    + "-" + compact.substring(16, 20)
                    + "-" + compact.substring(20));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private record TextureProfileData(UUID profileId, String profileName, URL skinUrl) {
    }

    public static ItemStack createPlaceholder(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("");
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
            Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(name));
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
