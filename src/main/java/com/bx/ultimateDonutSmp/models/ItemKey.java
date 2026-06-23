package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.stream.Collectors;

public final class ItemKey {
    public final Material material;
    public final PotionType potionType;
    public final Map<String, Integer> enchants;

    private ItemKey(Material material, PotionType potionType, Map<String, Integer> enchants) {
        this.material = material != null ? material : Material.AIR;
        this.potionType = potionType;
        this.enchants = enchants != null ? Collections.unmodifiableMap(new LinkedHashMap<>(enchants)) : Collections.emptyMap();
    }

    public static ItemKey of(Material material) {
        return new ItemKey(material, null, null);
    }

    public static ItemKey potion(Material material, PotionType potionType) {
        return new ItemKey(material, potionType, null);
    }

    public static ItemKey book(Map<Enchantment, Integer> enchants) {
        Map<String, Integer> stringEnchants = new LinkedHashMap<>();
        if (enchants != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                stringEnchants.put(keyOf(entry.getKey()), entry.getValue());
            }
        }
        return new ItemKey(Material.ENCHANTED_BOOK, null, stringEnchants);
    }

    public static ItemKey fromStack(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return of(Material.AIR);
        }
        Material mat = item.getType();
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;

        if (mat == Material.ENCHANTED_BOOK) {
            Map<String, Integer> enchants = new LinkedHashMap<>();
            if (meta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta esm) {
                for (Map.Entry<Enchantment, Integer> entry : esm.getStoredEnchants().entrySet()) {
                    enchants.put(keyOf(entry.getKey()), entry.getValue());
                }
            }
            return new ItemKey(mat, null, enchants);
        }

        if (isPotionLike(mat)) {
            PotionType pType = null;
            if (meta instanceof PotionMeta pm) {
                try {
                    pType = pm.getBasePotionType();
                } catch (NoSuchMethodError | Exception e) {
                    // Fallback for older Spigot versions if getBasePotionType doesn't exist
                    try {
                        pType = pm.getBasePotionData().getType();
                    } catch (Exception ignored) {}
                }
            }
            return new ItemKey(mat, pType, null);
        }

        Map<String, Integer> enchants = new LinkedHashMap<>();
        if (meta != null && meta.hasEnchants()) {
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                enchants.put(keyOf(entry.getKey()), entry.getValue());
            }
        }
        return new ItemKey(mat, null, enchants);
    }

    public boolean matches(ItemStack item) {
        if (item == null || item.getType() != material) {
            return false;
        }

        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;

        if (material == Material.ENCHANTED_BOOK) {
            if (!(meta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta esm)) {
                return false;
            }
            Map<Enchantment, Integer> itemEnchants = esm.getStoredEnchants();
            // Verify that all required enchantments match exactly
            for (Map.Entry<String, Integer> reqEntry : enchants.entrySet()) {
                Enchantment reqEnch = findByKey(reqEntry.getKey());
                if (reqEnch == null) {
                    return false;
                }
                Integer itemLvl = itemEnchants.get(reqEnch);
                if (itemLvl == null || !itemLvl.equals(reqEntry.getValue())) {
                    return false;
                }
            }
            return true;
        }

        if (isPotionLike(material)) {
            if (!(meta instanceof PotionMeta pm)) {
                return false;
            }
            PotionType itemPType = null;
            try {
                itemPType = pm.getBasePotionType();
            } catch (NoSuchMethodError | Exception e) {
                try {
                    itemPType = pm.getBasePotionData().getType();
                } catch (Exception ignored) {}
            }
            return Objects.equals(potionType, itemPType);
        }

        // Check required enchantments on regular items
        if (!enchants.isEmpty()) {
            if (meta == null) {
                return false;
            }
            Map<Enchantment, Integer> itemEnchants = meta.getEnchants();
            for (Map.Entry<String, Integer> reqEntry : enchants.entrySet()) {
                Enchantment reqEnch = findByKey(reqEntry.getKey());
                if (reqEnch == null) {
                    return false;
                }
                Integer itemLvl = itemEnchants.get(reqEnch);
                if (itemLvl == null || !itemLvl.equals(reqEntry.getValue())) {
                    return false;
                }
            }
        }

        // Damage check: items must not be damaged!
        if (meta instanceof Damageable dmg) {
            if (dmg.hasDamage() && dmg.getDamage() > 0) {
                return false;
            }
        }

        return true;
    }

    public boolean isVariant() {
        return !enchants.isEmpty() || (isPotionLike(material) && potionType != null);
    }

    public static boolean isPotionLike(Material material) {
        return material == Material.POTION
                || material == Material.SPLASH_POTION
                || material == Material.LINGERING_POTION
                || material == Material.TIPPED_ARROW;
    }

    public ItemStack buildIcon() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        if (material == Material.ENCHANTED_BOOK) {
            org.bukkit.inventory.meta.EnchantmentStorageMeta esm = (org.bukkit.inventory.meta.EnchantmentStorageMeta) meta;
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                Enchantment ench = findByKey(entry.getKey());
                if (ench != null) {
                    esm.addStoredEnchant(ench, entry.getValue(), true);
                }
            }
        } else if (isPotionLike(material) && potionType != null) {
            PotionMeta pm = (PotionMeta) meta;
            try {
                pm.setBasePotionType(potionType);
            } catch (NoSuchMethodError | Exception e) {
                try {
                    pm.setBasePotionData(new org.bukkit.potion.PotionData(potionType));
                } catch (Exception ignored) {}
            }
        } else {
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                Enchantment ench = findByKey(entry.getKey());
                if (ench != null) {
                    meta.addEnchant(ench, entry.getValue(), true);
                }
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    public String displayName() {
        if (material == Material.ENCHANTED_BOOK && !enchants.isEmpty()) {
            if (enchants.size() == 1) {
                Map.Entry<String, Integer> entry = enchants.entrySet().iterator().next();
                return bookEnchantLabel(entry.getKey(), entry.getValue());
            } else {
                String label = enchants.entrySet().stream()
                        .limit(3)
                        .map(entry -> bookEnchantLabel(entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining(", "));
                if (enchants.size() > 3) {
                    label += ", ...";
                }
                return label;
            }
        }

        if (isPotionLike(material) && potionType != null) {
            String effectName = potionEffectName(potionType);
            boolean strong = potionType.name().startsWith("STRONG_");
            boolean longDuration = potionType.name().startsWith("LONG_");

            String prefix;
            switch (material) {
                case POTION -> prefix = "Potion of ";
                case SPLASH_POTION -> prefix = "Splash Potion of ";
                case LINGERING_POTION -> prefix = "Lingering Potion of ";
                case TIPPED_ARROW -> prefix = "Tipped Arrow of ";
                default -> prefix = "";
            }

            if (strong) {
                return prefix + effectName + " II";
            } else if (longDuration) {
                return prefix + "Long " + effectName;
            } else {
                return prefix + effectName;
            }
        }

        return niceName(material);
    }

    public List<String> enchantLoreLines(String highlightPrefix) {
        if (enchants.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            Enchantment ench = findByKey(entry.getKey());
            String name = ench != null ? title(ench.getKey().getKey().replace('_', ' ')) : title(entry.getKey().replace('_', ' '));
            lines.add(highlightPrefix + name + " " + roman(entry.getValue()));
        }
        return lines;
    }

    private static String bookEnchantLabel(String name, int level) {
        Enchantment ench = findByKey(name);
        String labelName = name;
        if (name.contains(":")) {
            labelName = name.substring(name.indexOf(':') + 1);
        }
        labelName = title(labelName.replace('_', ' '));

        int maxLvl = 1;
        if (ench != null) {
            maxLvl = Math.max(1, ench.getMaxLevel());
        }

        if (maxLvl <= 1 || level <= 1) {
            return labelName;
        }

        return labelName + " " + roman(level);
    }

    private static String potionEffectName(PotionType type) {
        String name = type.name().replaceFirst("^(LONG_|STRONG_)", "");
        return title(name.replace('_', ' '));
    }

    private static String title(String s) {
        if (s == null || s.isEmpty()) return "";
        return Arrays.stream(s.split(" "))
                .map(word -> word.isEmpty() ? "" : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase(Locale.ENGLISH))
                .collect(Collectors.joining(" "));
    }

    public static String niceName(Material material) {
        if (material == null) return "";
        return title(material.name().replace('_', ' '));
    }

    private static String roman(int n) {
        if (n <= 0) return "";
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (n >= values[i]) {
                n -= values[i];
                sb.append(symbols[i]);
            }
        }
        return sb.toString();
    }

    private static String keyOf(Enchantment ench) {
        if (ench == null) return "";
        return ench.getKey().getKey().toLowerCase(Locale.ENGLISH);
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

    public String serialize() {
        if (material == Material.ENCHANTED_BOOK && !enchants.isEmpty()) {
            String serialized = enchants.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining(","));
            return "BOOK|" + serialized;
        }

        if (isPotionLike(material) && potionType != null) {
            return material.name() + "|POTION:" + potionType.name();
        }

        if (!enchants.isEmpty()) {
            String serialized = enchants.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining(","));
            return material.name() + "|ENCH:" + serialized;
        }

        return material.name();
    }

    public static ItemKey deserialize(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }

        if (s.startsWith("BOOK|")) {
            String data = s.substring("BOOK|".length());
            Map<String, Integer> map = new LinkedHashMap<>();
            if (!data.isEmpty()) {
                for (String part : data.split(",")) {
                    String[] split = part.split("=");
                    if (split.length == 2) {
                        try {
                            map.put(split[0], Integer.parseInt(split[1]));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            return new ItemKey(Material.ENCHANTED_BOOK, null, map);
        }

        if (s.contains("|POTION:")) {
            String[] split = s.split("\\|POTION:");
            Material mat = Material.matchMaterial(split[0]);
            PotionType pType = PotionType.valueOf(split[1]);
            return new ItemKey(mat, pType, null);
        }

        if (s.contains("|ENCH:")) {
            String[] split = s.split("\\|ENCH:");
            Material mat = Material.matchMaterial(split[0]);
            Map<String, Integer> map = new LinkedHashMap<>();
            if (split.length > 1 && !split[1].isEmpty()) {
                for (String part : split[1].split(",")) {
                    String[] partSplit = part.split("=");
                    if (partSplit.length == 2) {
                        try {
                            map.put(partSplit[0], Integer.parseInt(partSplit[1]));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            return new ItemKey(mat, null, map);
        }

        Material mat = Material.matchMaterial(s);
        return new ItemKey(mat, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemKey itemKey = (ItemKey) o;
        return material == itemKey.material &&
                potionType == itemKey.potionType &&
                Objects.equals(enchants, itemKey.enchants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, potionType, enchants);
    }

    @Override
    public String toString() {
        return serialize();
    }
}
