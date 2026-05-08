package com.bx.ultimateDonutSmp.models;

import java.util.Arrays;
import java.util.Optional;

public enum SellCategory {
    CROPS("CROPS", "CROPS", "CROPS-BUTTON"),
    ORES("ORES", "ORES", "ORES-BUTTON"),
    MOBS("MOBS", "MOBS", "MOBS-BUTTON"),
    NATURAL("NATURAL", "NATURAL", "NATURAL-BUTTON"),
    ARMOR_AND_TOOLS("ARMOR_AND_TOOLS", "ARMOR_AND_TOOLS", "ARMOR-AND-TOOLS-BUTTON"),
    FISH("FISH", "FISH", "FISH-BUTTON"),
    BOOK("BOOK", "BOOK", "BOOK-BUTTON"),
    POTIONS("POTIONS", "POTION", "POTIONS-BUTTON"),
    BLOCKS("BLOCKS", "BLOCKS", "BLOCKS-BUTTON");

    private final String configKey;
    private final String worthSectionKey;
    private final String sellMenuButtonKey;

    SellCategory(String configKey, String worthSectionKey, String sellMenuButtonKey) {
        this.configKey = configKey;
        this.worthSectionKey = worthSectionKey;
        this.sellMenuButtonKey = sellMenuButtonKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getWorthSectionKey() {
        return worthSectionKey;
    }

    public String getSellMenuButtonKey() {
        return sellMenuButtonKey;
    }

    public static Optional<SellCategory> fromConfigKey(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(category -> category.configKey.equalsIgnoreCase(key)
                        || category.name().equalsIgnoreCase(key)
                        || category.worthSectionKey.equalsIgnoreCase(key))
                .findFirst();
    }
}
