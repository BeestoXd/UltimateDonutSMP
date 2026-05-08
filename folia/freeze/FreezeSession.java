package com.bx.ultimateDonutSmp.freeze;

import org.bukkit.Location;

import java.util.UUID;

public final class FreezeSession {

    private final UUID targetUuid;
    private Location anchorLocation;
    private long lastAlertSentAt;
    private long lastReminderSentAt;

    public FreezeSession(UUID targetUuid, Location anchorLocation) {
        this.targetUuid = targetUuid;
        this.anchorLocation = anchorLocation == null ? null : anchorLocation.clone();
        this.lastAlertSentAt = 0L;
        this.lastReminderSentAt = 0L;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public Location getAnchorLocation() {
        return anchorLocation == null ? null : anchorLocation.clone();
    }

    public void setAnchorLocation(Location anchorLocation) {
        this.anchorLocation = anchorLocation == null ? null : anchorLocation.clone();
    }

    public boolean shouldSendAlert(long now, long cooldownMillis) {
        return cooldownMillis <= 0L || now - lastAlertSentAt >= cooldownMillis;
    }

    public void markAlertSent(long now) {
        this.lastAlertSentAt = now;
    }

    public boolean shouldSendReminder(long now, long cooldownMillis) {
        return cooldownMillis <= 0L || now - lastReminderSentAt >= cooldownMillis;
    }

    public void markReminderSent(long now) {
        this.lastReminderSentAt = now;
    }
}
