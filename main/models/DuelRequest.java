package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public record DuelRequest(
        UUID challengerUuid,
        String challengerName,
        UUID targetUuid,
        String targetName,
        DuelMapSelection mapSelection,
        DuelPrivacyMode privacyMode,
        long expiresAt
) {
    public boolean isExpired(long now) {
        return now >= expiresAt;
    }
}
