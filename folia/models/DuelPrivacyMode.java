package com.bx.ultimateDonutSmp.models;

import java.util.Locale;

public enum DuelPrivacyMode {
    INVITE_ONLY,
    FRIENDS_ONLY;

    public static DuelPrivacyMode fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return INVITE_ONLY;
        }

        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        if (normalized.equals("FRIENDS") || normalized.equals("FRIEND") || normalized.equals("ONLY_FRIENDS")) {
            return FRIENDS_ONLY;
        }
        if (normalized.equals("INVITE") || normalized.equals("PRIVATE") || normalized.equals("ONLY_INVITE")) {
            return INVITE_ONLY;
        }

        try {
            return DuelPrivacyMode.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return INVITE_ONLY;
        }
    }

    public String displayName() {
        return switch (this) {
            case FRIENDS_ONLY -> "Friends Only";
            case INVITE_ONLY -> "Invite Only";
        };
    }
}
