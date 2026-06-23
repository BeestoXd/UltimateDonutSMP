package com.bx.ultimateDonutSmp.models;

import java.util.Locale;

public enum OrderAlphaSort {
    A_Z,
    Z_A;

    public OrderAlphaSort next() {
        return this == A_Z ? Z_A : A_Z;
    }

    public static OrderAlphaSort fromDatabase(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return A_Z;
        }
        try {
            return valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return A_Z;
        }
    }
}
