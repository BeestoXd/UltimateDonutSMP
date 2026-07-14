package com.bx.ultimateDonutSmp.models;

public enum ThreeChoice {
    OFF,
    ANYONE,
    FRIENDS_FOLLOWED;

    public static ThreeChoice fromInt(int val) {
        if (val < 0 || val >= values().length) {
            return ANYONE; // default fallback
        }
        return values()[val];
    }
}
