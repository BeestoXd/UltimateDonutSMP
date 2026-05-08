package com.bx.ultimateDonutSmp.models;

public enum PunishmentSortOrder {
    NEWEST("Newest"),
    OLDEST("Oldest");

    private final String displayName;

    PunishmentSortOrder(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PunishmentSortOrder next() {
        return this == NEWEST ? OLDEST : NEWEST;
    }
}
