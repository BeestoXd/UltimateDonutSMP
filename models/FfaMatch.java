package com.bx.ultimateDonutSmp.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FfaMatch {

    public enum MatchStatus {
        ACTIVE,
        POST_MATCH,
        FINISHED
    }

    private final long id;
    private final FfaArena arena;
    private final UUID playerOneUuid;
    private final UUID playerTwoUuid;
    private final String playerOneName;
    private final String playerTwoName;
    private final Map<UUID, FfaPlayerSnapshot> snapshots = new HashMap<>();
    private MatchStatus status;
    private long startedAt;
    private long lastCombatAt;

    public FfaMatch(long id,
                    FfaArena arena,
                    UUID playerOneUuid,
                    String playerOneName,
                    UUID playerTwoUuid,
                    String playerTwoName) {
        this.id = id;
        this.arena = arena;
        this.playerOneUuid = playerOneUuid;
        this.playerOneName = playerOneName;
        this.playerTwoUuid = playerTwoUuid;
        this.playerTwoName = playerTwoName;
        this.status = MatchStatus.ACTIVE;
    }

    public long getId() {
        return id;
    }

    public FfaArena getArena() {
        return arena;
    }

    public UUID getPlayerOneUuid() {
        return playerOneUuid;
    }

    public UUID getPlayerTwoUuid() {
        return playerTwoUuid;
    }

    public String getPlayerOneName() {
        return playerOneName;
    }

    public String getPlayerTwoName() {
        return playerTwoName;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getLastCombatAt() {
        return lastCombatAt;
    }

    public void markCombat() {
        this.lastCombatAt = System.currentTimeMillis();
    }

    public boolean hasCombatStarted() {
        return lastCombatAt > 0L;
    }

    public void putSnapshot(UUID uuid, FfaPlayerSnapshot snapshot) {
        if (uuid != null && snapshot != null) {
            snapshots.put(uuid, snapshot);
        }
    }

    public FfaPlayerSnapshot getSnapshot(UUID uuid) {
        return snapshots.get(uuid);
    }

    public boolean isParticipant(UUID uuid) {
        return playerOneUuid.equals(uuid) || playerTwoUuid.equals(uuid);
    }

    public UUID getOpponent(UUID uuid) {
        if (playerOneUuid.equals(uuid)) {
            return playerTwoUuid;
        }
        if (playerTwoUuid.equals(uuid)) {
            return playerOneUuid;
        }
        return null;
    }

    public String getOpponentName(UUID uuid) {
        if (playerOneUuid.equals(uuid)) {
            return playerTwoName;
        }
        if (playerTwoUuid.equals(uuid)) {
            return playerOneName;
        }
        return "Unknown";
    }
}
