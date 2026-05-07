package com.bx.ultimateDonutSmp.models;

import java.util.Locale;

public enum PunishmentType {
    BAN,
    MUTE,
    WARN,
    KICK,
    BLACKLIST;

    public static PunishmentType fromString(String value, PunishmentType fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        try {
            return PunishmentType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    public static PunishmentType nextFilter(PunishmentType current) {
        if (current == null) {
            return BAN;
        }

        PunishmentType[] values = values();
        int nextIndex = current.ordinal() + 1;
        return nextIndex >= values.length ? null : values[nextIndex];
    }
}
