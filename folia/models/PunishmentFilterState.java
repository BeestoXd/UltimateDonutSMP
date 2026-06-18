package com.bx.ultimateDonutSmp.models;

public enum PunishmentFilterState {
    ALL("All"),
    ACTIVE("Active"),
    INACTIVE("ɪɴᴀᴄᴛɪᴠᴇ");

    private final String displayName;

    PunishmentFilterState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PunishmentFilterState next() {
        return switch (this) {
            case ALL -> ACTIVE;
            case ACTIVE -> INACTIVE;
            case INACTIVE -> ALL;
        };
    }
}
