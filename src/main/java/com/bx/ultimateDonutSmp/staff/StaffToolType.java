package com.bx.ultimateDonutSmp.staff;

import java.util.Locale;

public enum StaffToolType {
    VANISH("VANISH"),
    FREEZE("FREEZE"),
    STAFF_LIST("STAFF_LIST"),
    BETTER_VIEW("BETTER_VIEW"),
    RANDOM_TELEPORT("RANDOM_TELEPORT");

    private final String configKey;

    StaffToolType(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    public static StaffToolType fromPersistentId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return StaffToolType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
