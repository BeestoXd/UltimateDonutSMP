package com.bx.ultimateDonutSmp.models;

public record ServerStatusSnapshot(
        String serverId,
        String displayName,
        boolean online,
        int playerCount,
        String softwareLabel,
        String performanceLabel,
        long lastUpdatedAt,
        long latencyMs
) {

    public ServerStatusSnapshot {
        serverId = serverId == null ? "" : serverId;
        displayName = displayName == null || displayName.isBlank() ? serverId : displayName;
        playerCount = Math.max(0, playerCount);
        softwareLabel = normalizeLabel(softwareLabel);
        performanceLabel = normalizeLabel(performanceLabel);
        lastUpdatedAt = Math.max(0L, lastUpdatedAt);
        latencyMs = Math.max(0L, latencyMs);
    }

    public static ServerStatusSnapshot offline(String serverId, String displayName) {
        return new ServerStatusSnapshot(
                serverId,
                displayName,
                false,
                0,
                "N/A",
                "N/A",
                0L,
                0L
        );
    }

    public ServerStatusSnapshot withIdentity(String newServerId, String newDisplayName) {
        return new ServerStatusSnapshot(
                newServerId,
                newDisplayName,
                online,
                playerCount,
                softwareLabel,
                performanceLabel,
                lastUpdatedAt,
                latencyMs
        );
    }

    private static String normalizeLabel(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }
}
