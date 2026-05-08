package com.bx.ultimateDonutSmp.models;

import java.util.Locale;

public enum PunishmentScope {
    SERVER,
    NETWORK;

    public static PunishmentScope fromString(String value, PunishmentScope fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        try {
            return PunishmentScope.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
