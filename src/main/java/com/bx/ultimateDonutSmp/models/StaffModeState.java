package com.bx.ultimateDonutSmp.models;

import org.bukkit.GameMode;

import java.util.UUID;

public final class StaffModeState {

    private final UUID staffUuid;
    private final String staffNameSnapshot;
    private final long enabledAt;
    private final String sourceServer;
    private final boolean vanishActive;
    private final boolean betterViewActive;
    private final boolean snapshotPresent;
    private final boolean previousAllowFlight;
    private final boolean previousFlying;
    private final int previousSelectedSlot;
    private final boolean nightVisionOwned;
    private final GameMode previousGameMode;

    public StaffModeState(UUID staffUuid,
                          String staffNameSnapshot,
                          long enabledAt,
                          String sourceServer,
                          boolean vanishActive,
                          boolean betterViewActive,
                          boolean snapshotPresent,
                          boolean previousAllowFlight,
                          boolean previousFlying,
                          int previousSelectedSlot,
                          boolean nightVisionOwned,
                          GameMode previousGameMode) {
        this.staffUuid = staffUuid;
        this.staffNameSnapshot = staffNameSnapshot == null ? "" : staffNameSnapshot;
        this.enabledAt = enabledAt;
        this.sourceServer = sourceServer == null || sourceServer.isBlank() ? "local" : sourceServer;
        this.vanishActive = vanishActive;
        this.betterViewActive = betterViewActive;
        this.snapshotPresent = snapshotPresent;
        this.previousAllowFlight = previousAllowFlight;
        this.previousFlying = previousFlying;
        this.previousSelectedSlot = Math.max(0, Math.min(8, previousSelectedSlot));
        this.nightVisionOwned = nightVisionOwned;
        this.previousGameMode = previousGameMode == null ? GameMode.SURVIVAL : previousGameMode;
    }

    public UUID getStaffUuid() {
        return staffUuid;
    }

    public String getStaffNameSnapshot() {
        return staffNameSnapshot;
    }

    public long getEnabledAt() {
        return enabledAt;
    }

    public String getSourceServer() {
        return sourceServer;
    }

    public boolean isVanishActive() {
        return vanishActive;
    }

    public boolean isBetterViewActive() {
        return betterViewActive;
    }

    public boolean isSnapshotPresent() {
        return snapshotPresent;
    }

    public boolean isPreviousAllowFlight() {
        return previousAllowFlight;
    }

    public boolean isPreviousFlying() {
        return previousFlying;
    }

    public int getPreviousSelectedSlot() {
        return previousSelectedSlot;
    }

    public boolean isNightVisionOwned() {
        return nightVisionOwned;
    }

    public GameMode getPreviousGameMode() {
        return previousGameMode;
    }

    public StaffModeState withStaffNameSnapshot(String nameSnapshot) {
        return new StaffModeState(
                staffUuid,
                nameSnapshot,
                enabledAt,
                sourceServer,
                vanishActive,
                betterViewActive,
                snapshotPresent,
                previousAllowFlight,
                previousFlying,
                previousSelectedSlot,
                nightVisionOwned,
                previousGameMode
        );
    }

    public StaffModeState withVanishActive(boolean active) {
        return new StaffModeState(
                staffUuid,
                staffNameSnapshot,
                enabledAt,
                sourceServer,
                active,
                betterViewActive,
                snapshotPresent,
                previousAllowFlight,
                previousFlying,
                previousSelectedSlot,
                nightVisionOwned,
                previousGameMode
        );
    }

    public StaffModeState withBetterView(boolean active, boolean ownsNightVision) {
        return new StaffModeState(
                staffUuid,
                staffNameSnapshot,
                enabledAt,
                sourceServer,
                vanishActive,
                active,
                snapshotPresent,
                previousAllowFlight,
                previousFlying,
                previousSelectedSlot,
                ownsNightVision,
                previousGameMode
        );
    }
}
