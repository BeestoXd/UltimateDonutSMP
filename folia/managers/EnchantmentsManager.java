package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class EnchantmentsManager {

    public static final class EnchantOption {
        public final String key;
        public final Enchantment ench;
        public final int level;
        public final int slot;
        public final int page;
        public final String category;

        public EnchantOption(String key, Enchantment ench, int level, int slot, int page, String category) {
            this.key = key;
            this.ench = ench;
            this.level = level;
            this.slot = slot;
            this.page = page;
            this.category = category;
        }
    }

    private final UltimateDonutSmp plugin;
    private final Map<String, List<EnchantOption>> byCategory = new LinkedHashMap<>();
    private final Map<Material, String> materialCategory = new HashMap<>();

    public EnchantmentsManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        byCategory.clear();
        mapMaterials();

        FileConfiguration config = plugin.getConfigManager().getEnchantments();
        if (config == null) return;

        List<String> categoriesToLoad = List.of(
                "helmet", "chestplate", "leggings", "boots", "elytra",
                "bow", "crossbow", "fishing_rod", "shovel", "pickaxe", "axe", "hoe", "shield", "sword", "trident"
        );

        for (String cat : categoriesToLoad) {
            ConfigurationSection sec = config.getConfigurationSection(cat);
            if (sec == null) continue;
            List<EnchantOption> options = new ArrayList<>();
            for (String key : sec.getKeys(false)) {
                ConfigurationSection optSec = sec.getConfigurationSection(key);
                if (optSec == null) continue;
                String enchRaw = optSec.getString("enchantment");
                if (enchRaw == null) continue;
                String[] parts = enchRaw.split(";");
                if (parts.length != 2) continue;
                Enchantment enchantment = findByKey(parts[0]);
                if (enchantment == null) continue;
                int level = Integer.parseInt(parts[1]);
                int slot = optSec.getInt("slot");
                int page = optSec.getInt("page", 1);
                options.add(new EnchantOption(key, enchantment, level, slot, page, cat));
            }
            byCategory.put(cat, options);
        }
    }

    public boolean hasOptionsFor(Material material) {
        String cat = materialCategory.get(material);
        return cat != null && byCategory.containsKey(cat) && !byCategory.get(cat).isEmpty();
    }

    public List<EnchantOption> optionsFor(Material material) {
        String cat = materialCategory.get(material);
        if (cat == null) return Collections.emptyList();
        return byCategory.getOrDefault(cat, Collections.emptyList());
    }

    public int maxPage(List<EnchantOption> options) {
        int max = 1;
        for (EnchantOption opt : options) {
            if (opt.page > max) {
                max = opt.page;
            }
        }
        return max;
    }

    public String getGuiTitle() {
        FileConfiguration config = plugin.getConfigManager().getEnchantments();
        return config != null ? config.getString("gui.title", "&8Pick Enchantments") : "&8Pick Enchantments";
    }

    public int getGuiRows() {
        FileConfiguration config = plugin.getConfigManager().getEnchantments();
        return config != null ? config.getInt("gui.rows", 6) : 6;
    }

    public int getCancelSlot() {
        FileConfiguration config = plugin.getConfigManager().getEnchantments();
        return config != null ? config.getInt("gui.slots.cancel", 46) : 46;
    }

    public int getPrevSlot() {
        FileConfiguration config = plugin.getConfigManager().getEnchantments();
        return config != null ? config.getInt("gui.slots.prev", 45) : 45;
    }

    public int getNextSlot() {
        FileConfiguration config = plugin.getConfigManager().getEnchantments();
        return config != null ? config.getInt("gui.slots.next", 53) : 53;
    }

    public int getConfirmSlot() {
        FileConfiguration config = plugin.getConfigManager().getEnchantments();
        return config != null ? config.getInt("gui.slots.confirm", 52) : 52;
    }

    public int getItemSlot() {
        FileConfiguration config = plugin.getConfigManager().getEnchantments();
        return config != null ? config.getInt("gui.slots.item", 0) : 0;
    }

    public String getMessageSelect() {
        FileConfiguration config = plugin.getConfigManager().getEnchantments();
        return config != null ? config.getString("messages.select", "&fClick to select") : "&fClick to select";
    }

    public String getMessageSelected() {
        FileConfiguration config = plugin.getConfigManager().getEnchantments();
        return config != null ? config.getString("messages.selected", "&aSelected") : "&aSelected";
    }

    public String getMessageCannot() {
        FileConfiguration config = plugin.getConfigManager().getEnchantments();
        return config != null ? config.getString("messages.cannot", "&fCannot add this enchantment") : "&fCannot add this enchantment";
    }

    private void mapMaterials() {
        materialCategory.clear();
        for (Material material : Material.values()) {
            if (!material.isItem()) {
                continue;
            }
            String name = material.name();
            if (name.endsWith("_SWORD")) {
                materialCategory.put(material, "sword");
            } else if (name.endsWith("_PICKAXE")) {
                materialCategory.put(material, "pickaxe");
            } else if (name.endsWith("_AXE")) {
                materialCategory.put(material, "axe");
            } else if (name.endsWith("_SHOVEL")) {
                materialCategory.put(material, "shovel");
            } else if (name.endsWith("_HOE")) {
                materialCategory.put(material, "hoe");
            } else if (name.endsWith("_HELMET")) {
                materialCategory.put(material, "helmet");
            } else if (name.endsWith("_CHESTPLATE")) {
                materialCategory.put(material, "chestplate");
            } else if (name.endsWith("_LEGGINGS")) {
                materialCategory.put(material, "leggings");
            } else if (name.endsWith("_BOOTS")) {
                materialCategory.put(material, "boots");
            } else if (name.equals("BOW")) {
                materialCategory.put(material, "bow");
            } else if (name.equals("CROSSBOW")) {
                materialCategory.put(material, "crossbow");
            } else if (name.equals("TRIDENT")) {
                materialCategory.put(material, "trident");
            } else if (name.equals("SHIELD")) {
                materialCategory.put(material, "shield");
            } else if (name.equals("FISHING_ROD")) {
                materialCategory.put(material, "fishing_rod");
            } else if (name.equals("ELYTRA")) {
                materialCategory.put(material, "elytra");
            }
        }
    }

    private static Enchantment findByKey(String key) {
        if (key == null) return null;
        String cleanKey = key.toLowerCase(Locale.ENGLISH).replace("minecraft:", "");
        try {
            NamespacedKey nsk = NamespacedKey.minecraft(cleanKey);
            Enchantment ench = Enchantment.getByKey(nsk);
            if (ench != null) return ench;
        } catch (Exception ignored) {}
        for (Enchantment ench : Enchantment.values()) {
            if (ench.getName().equalsIgnoreCase(cleanKey) || ench.getKey().getKey().equalsIgnoreCase(cleanKey)) {
                return ench;
            }
        }
        return null;
    }

    private static String keyOf(Enchantment ench) {
        if (ench == null) return "";
        return ench.getKey().getKey().toLowerCase(Locale.ENGLISH);
    }
}
