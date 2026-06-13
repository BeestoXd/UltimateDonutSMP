package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public record HideState(
        UUID playerUuid,
        String realNameSnapshot,
        HideMode mode,
        String alias,
        String aliasNormalized,
        String skinKey,
        String skinUsername,
        String textureValue,
        String textureSignature,
        long createdAt,
        long updatedAt
) {
    public HideState {
        realNameSnapshot = safe(realNameSnapshot);
        alias = safe(alias);
        aliasNormalized = safe(aliasNormalized);
        skinKey = safe(skinKey);
        skinUsername = safe(skinUsername);
        textureValue = safe(textureValue);
        textureSignature = safe(textureSignature);
    }

    public boolean hasTexture() {
        return !textureValue.isBlank();
    }

    public HideState withTexture(String value, String signature, long timestamp) {
        return new HideState(
                playerUuid,
                realNameSnapshot,
                mode,
                alias,
                aliasNormalized,
                skinKey,
                skinUsername,
                value,
                signature,
                createdAt,
                timestamp
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
