package com.bx.ultimateDonutSmp.managers;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class FakePlayerSession {

    private final long id;
    private final int entityId;
    private final UUID fakeUuid;
    private final Object profile;
    private final UUID creatorUuid;
    private final String creatorName;
    private final String profileName;
    private final String displayName;
    private final Location location;
    private Location visualLocation;
    private Vector visualVelocity = new Vector(0D, 0D, 0D);
    private boolean visualOnGround;
    private final long createdAtMillis;
    private final long expiresAtMillis;
    private final Set<UUID> viewers = new HashSet<>();
    private final Map<UUID, Integer> aimCounts = new HashMap<>();
    private final Map<UUID, Long> alertCooldowns = new HashMap<>();
    private final Map<UUID, Long> hitCooldowns = new HashMap<>();
    private final Map<UUID, Long> hardPositionLockTimes = new HashMap<>();
    private long positionLockPausedUntilMillis;
    private long visualMotionSequence;

    FakePlayerSession(
            long id,
            int entityId,
            UUID fakeUuid,
            Object profile,
            UUID creatorUuid,
            String creatorName,
            String profileName,
            String displayName,
            Location location,
            long createdAtMillis,
            long expiresAtMillis
    ) {
        this.id = id;
        this.entityId = entityId;
        this.fakeUuid = fakeUuid;
        this.profile = profile;
        this.creatorUuid = creatorUuid;
        this.creatorName = creatorName;
        this.profileName = profileName;
        this.displayName = displayName;
        this.location = location.clone();
        this.visualLocation = location.clone();
        this.createdAtMillis = createdAtMillis;
        this.expiresAtMillis = expiresAtMillis;
    }

    long id() {
        return id;
    }

    int entityId() {
        return entityId;
    }

    UUID fakeUuid() {
        return fakeUuid;
    }

    Object profile() {
        return profile;
    }

    UUID creatorUuid() {
        return creatorUuid;
    }

    String creatorName() {
        return creatorName;
    }

    String profileName() {
        return profileName;
    }

    String displayName() {
        return displayName;
    }

    Location location() {
        return location.clone();
    }

    Location eyeLocation() {
        Location eye = location.clone();
        eye.add(0D, 1.62D, 0D);
        return eye;
    }

    Location visualLocation() {
        return visualLocation.clone();
    }

    Location visualEyeLocation() {
        Location eye = visualLocation.clone();
        eye.add(0D, 1.62D, 0D);
        return eye;
    }

    void setVisualLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        visualLocation = location.clone();
    }

    void resetVisualLocation() {
        visualLocation = location.clone();
        visualVelocity = new Vector(0D, 0D, 0D);
        visualOnGround = false;
    }

    Vector visualVelocity() {
        return visualVelocity.clone();
    }

    void setVisualVelocity(Vector velocity) {
        if (velocity == null) {
            visualVelocity = new Vector(0D, 0D, 0D);
            return;
        }
        visualVelocity = velocity.clone();
    }

    void addVisualVelocity(Vector velocity) {
        if (velocity == null) {
            return;
        }
        visualVelocity.add(velocity);
    }

    boolean visualOnGround() {
        return visualOnGround;
    }

    void setVisualOnGround(boolean visualOnGround) {
        this.visualOnGround = visualOnGround;
    }

    long nextVisualMotionSequence() {
        return ++visualMotionSequence;
    }

    boolean isVisualMotionSequence(long sequence) {
        return visualMotionSequence == sequence;
    }

    long createdAtMillis() {
        return createdAtMillis;
    }

    long expiresAtMillis() {
        return expiresAtMillis;
    }

    void pausePositionLock(long ticks) {
        if (ticks <= 0L) {
            return;
        }

        long pauseUntil = System.currentTimeMillis() + ticks * 50L;
        positionLockPausedUntilMillis = Math.max(positionLockPausedUntilMillis, pauseUntil);
    }

    boolean isPositionLockPaused(long nowMillis) {
        return nowMillis < positionLockPausedUntilMillis;
    }

    boolean shouldHardPositionLock(UUID viewerId, long nowMillis, long intervalTicks) {
        if (viewerId == null || intervalTicks <= 0L) {
            return false;
        }

        long intervalMillis = intervalTicks * 50L;
        Long previous = hardPositionLockTimes.get(viewerId);
        if (previous != null && nowMillis - previous < intervalMillis) {
            return false;
        }

        hardPositionLockTimes.put(viewerId, nowMillis);
        return true;
    }

    Set<UUID> viewers() {
        return viewers;
    }

    Map<UUID, Integer> aimCounts() {
        return aimCounts;
    }

    Map<UUID, Long> alertCooldowns() {
        return alertCooldowns;
    }

    Map<UUID, Long> hitCooldowns() {
        return hitCooldowns;
    }

    Map<UUID, Long> hardPositionLockTimes() {
        return hardPositionLockTimes;
    }
}
