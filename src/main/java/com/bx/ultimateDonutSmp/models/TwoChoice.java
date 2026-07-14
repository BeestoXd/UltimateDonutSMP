package com.bx.ultimateDonutSmp.models;

public enum TwoChoice {
    OFF,
    FRIENDS_FOLLOWED;

    public static TwoChoice fromInt(int val) {
        if (val < 0 || val >= values().length) {
            return FRIENDS_FOLLOWED; // default fallback
        }
        return values()[val];
    }
}
