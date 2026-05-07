package com.bx.ultimateDonutSmp.models;

public enum PunishmentState {
    ACTIVE("Active", true),
    EXPIRED("Expired", false),
    REMOVED("Removed", false);

    private final String displayName;
    private final boolean active;

    PunishmentState(String displayName, boolean active) {
        this.displayName = displayName;
        this.active = active;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return active;
    }
}
