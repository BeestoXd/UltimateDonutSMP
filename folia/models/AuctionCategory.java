package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public enum AuctionCategory {
    ALL("All", Material.COMPASS),
    BLOCKS("Blocks", Material.GRASS_BLOCK),
    TOOLS("Tools", Material.DIAMOND_PICKAXE),
    FOOD("Food", Material.GOLDEN_CARROT),
    COMBAT("Combat", Material.DIAMOND_SWORD),
    POTIONS("Potions", Material.POTION),
    BOOKS("Books", Material.ENCHANTED_BOOK),
    INGREDIENTS("Ingredients", Material.BLAZE_POWDER),
    UTILITIES("Utilities", Material.ENDER_CHEST);

    private final String displayName;
    private final Material icon;

    AuctionCategory(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String defaultDisplayName() {
        return displayName;
    }

    public Material defaultIcon() {
        return icon;
    }

    public boolean matches(ItemStack item) {
        if (this == ALL) {
            return true;
        }
        if (item == null) {
            return false;
        }

        Material type = item.getType();
        if (type == Material.AIR || type.name().endsWith("_AIR")) {
            return false;
        }
        return switch (this) {
            case BLOCKS -> matches(type, type.isBlock(), false);
            case FOOD -> matches(type, false, type.isEdible());
            default -> matches(type, false, false);
        };
    }

    public boolean matches(Material type, boolean block, boolean edible) {
        if (this == ALL) {
            return true;
        }
        if (type == null || type == Material.AIR || type.name().endsWith("_AIR")) {
            return false;
        }
        String name = type.name();
        return switch (this) {
            case ALL -> true;
            case BLOCKS -> block;
            case TOOLS -> name.endsWith("_AXE")
                    || name.endsWith("_PICKAXE")
                    || name.endsWith("_SHOVEL")
                    || name.endsWith("_HOE")
                    || type == Material.SHEARS
                    || type == Material.FLINT_AND_STEEL
                    || type == Material.FISHING_ROD;
            case FOOD -> edible;
            case COMBAT -> name.endsWith("_SWORD")
                    || name.endsWith("_AXE")
                    || name.endsWith("_HELMET")
                    || name.endsWith("_CHESTPLATE")
                    || name.endsWith("_LEGGINGS")
                    || name.endsWith("_BOOTS")
                    || name.endsWith("_BOW")
                    || type == Material.CROSSBOW
                    || type == Material.TRIDENT
                    || type == Material.SHIELD;
            case POTIONS -> type == Material.POTION
                    || type == Material.SPLASH_POTION
                    || type == Material.LINGERING_POTION
                    || type == Material.TIPPED_ARROW;
            case BOOKS -> type == Material.BOOK
                    || type == Material.WRITABLE_BOOK
                    || type == Material.WRITTEN_BOOK
                    || type == Material.ENCHANTED_BOOK
                    || type == Material.KNOWLEDGE_BOOK;
            case INGREDIENTS -> type == Material.BLAZE_POWDER
                    || type == Material.BLAZE_ROD
                    || type == Material.GUNPOWDER
                    || type == Material.STRING
                    || type == Material.SPIDER_EYE
                    || type == Material.FERMENTED_SPIDER_EYE
                    || type == Material.GLISTERING_MELON_SLICE
                    || type == Material.GHAST_TEAR
                    || type == Material.MAGMA_CREAM
                    || type == Material.RABBIT_FOOT
                    || type == Material.PHANTOM_MEMBRANE
                    || type == Material.SUGAR
                    || type == Material.REDSTONE
                    || type == Material.GLOWSTONE_DUST
                    || type == Material.NETHER_WART;
            case UTILITIES -> type == Material.ENDER_CHEST
                    || type == Material.CHEST
                    || type == Material.BARREL
                    || type == Material.SHULKER_BOX
                    || name.endsWith("_SHULKER_BOX")
                    || type == Material.ELYTRA
                    || type == Material.LEAD
                    || type == Material.NAME_TAG
                    || type == Material.COMPASS
                    || type == Material.RECOVERY_COMPASS
                    || type == Material.CLOCK;
        };
    }

    public static AuctionCategory from(String raw) {
        if (raw == null || raw.isBlank()) {
            return ALL;
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return ALL;
        }
    }
}
