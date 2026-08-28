package com.bx.ultimateDonutSmp.models;

public enum VoiceChatConsent {
    UNDECIDED,
    ACCEPTED,
    DECLINED;

    public static VoiceChatConsent fromInt(int val) {
        if (val < 0 || val >= values().length) {
            return UNDECIDED; // default fallback
        }
        return values()[val];
    }
}
