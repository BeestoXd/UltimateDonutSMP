package com.bx.ultimateDonutSmp.models;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelMatch {

    public enum MatchType {
        DIRECT,
        QUEUE
    }

    public enum MatchStatus {
        COUNTDOWN,
        ACTIVE,
        FINISHED
    }

    private final long id;
    private final MatchType type;
    private final DuelArena arena;
    private final UUID playerOneUuid;
    private final UUID playerTwoUuid;
    private final String playerOneName;
    private final String playerTwoName;
    private final Map<UUID, Location> returnLocations = new HashMap<>();
    private MatchStatus status;
    private int countdownSecondsRemaining;
    private long startedAt;
    private long endsAt;
    private UUID drawRequester;
    private long drawRequestExpiresAt;

    public DuelMatch(long id, MatchType type, DuelArena arena,
                     UUID playerOneUuid, String playerOneName,
                     UUID playerTwoUuid, String playerTwoName,
                     int countdownSecondsRemaining) {
        this.id = id;
        this.type = type;
        this.arena = arena;
        this.playerOneUuid = playerOneUuid;
        this.playerOneName = playerOneName;
        this.playerTwoUuid = playerTwoUuid;
        this.playerTwoName = playerTwoName;
        this.status = MatchStatus.COUNTDOWN;
        this.countdownSecondsRemaining = Math.max(0, countdownSecondsRemaining);
        this.startedAt = 0L;
        this.endsAt = 0L;
        this.drawRequester = null;
        this.drawRequestExpiresAt = 0L;
    }

    public long getId() {
        return id;
    }

    public MatchType getType() {
        return type;
    }

    public DuelArena getArena() {
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

    public int getCountdownSecondsRemaining() {
        return countdownSecondsRemaining;
    }

    public void decrementCountdown() {
        this.countdownSecondsRemaining = Math.max(0, countdownSecondsRemaining - 1);
    }

    public void setCountdownSecondsRemaining(int countdownSecondsRemaining) {
        this.countdownSecondsRemaining = Math.max(0, countdownSecondsRemaining);
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(long endsAt) {
        this.endsAt = endsAt;
    }

    public UUID getDrawRequester() {
        return drawRequester;
    }

    public void setDrawRequester(UUID drawRequester) {
        this.drawRequester = drawRequester;
    }

    public long getDrawRequestExpiresAt() {
        return drawRequestExpiresAt;
    }

    public void setDrawRequestExpiresAt(long drawRequestExpiresAt) {
        this.drawRequestExpiresAt = drawRequestExpiresAt;
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

    public void setReturnLocation(UUID uuid, Location location) {
        if (uuid == null || location == null) {
            return;
        }
        returnLocations.put(uuid, location.clone());
    }

    public Location getReturnLocation(UUID uuid) {
        Location location = returnLocations.get(uuid);
        return location == null ? null : location.clone();
    }
}
