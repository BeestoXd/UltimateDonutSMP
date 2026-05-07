package com.bx.ultimateDonutSmp.models;

import java.util.Locale;

public enum OrderSort {
    MOST_PAID,
    MOST_DELIVERED,
    RECENTLY_LISTED,
    MOST_MONEY_PER_ITEM;

    public static OrderSort fromConfig(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return MOST_PAID;
        }

        try {
            return OrderSort.valueOf(rawValue.trim().toUpperCase(Locale.US));
        } catch (IllegalArgumentException ignored) {
            return MOST_PAID;
        }
    }

    public String displayName() {
        return name().replace('_', ' ');
    }
}
