package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

/**
 * What the plugin remembers about a player while they are inside the ranked arena.
 *
 * <p>Deliberately no inventory snapshot: the arena is expected to be its own world so that
 * Multiverse-Inventories - or whatever else already separates inventories - keeps owning the
 * survival one. Storing a copy here would fight that plugin for the same job.</p>
 */
public class PvpSession {

    private final UUID playerUuid;
    private final long joinedAt;

    private String kitId;
    private long spawnedAt;
    private long respawnAt;
    private boolean awaitingKit;

    public PvpSession(UUID playerUuid, long joinedAt) {
        this.playerUuid = playerUuid;
        this.joinedAt = joinedAt;
        this.awaitingKit = true;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public String getKitId() {
        return kitId;
    }

    public void setKitId(String kitId) {
        this.kitId = kitId;
    }

    public long getSpawnedAt() {
        return spawnedAt;
    }

    public void setSpawnedAt(long spawnedAt) {
        this.spawnedAt = spawnedAt;
    }

    /** Epoch millis the respawn countdown ends, or 0 when the player is not waiting to respawn. */
    public long getRespawnAt() {
        return respawnAt;
    }

    public void setRespawnAt(long respawnAt) {
        this.respawnAt = respawnAt;
    }

    /** True while the player still owes the arena a kit choice, so they must not be fought yet. */
    public boolean isAwaitingKit() {
        return awaitingKit;
    }

    public void setAwaitingKit(boolean awaitingKit) {
        this.awaitingKit = awaitingKit;
    }

    /** True while the player is inside the configured spawn protection window. */
    public boolean isSpawnProtected(long now, int protectionSeconds) {
        return protectionSeconds > 0
                && spawnedAt > 0
                && now - spawnedAt < protectionSeconds * 1000L;
    }
}
