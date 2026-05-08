package com.bx.ultimateDonutSmp.staff;

import java.util.UUID;

public final class StaffModeSession {

    private final UUID staffUuid;
    private UUID lastRandomTeleportTarget;

    public StaffModeSession(UUID staffUuid) {
        this.staffUuid = staffUuid;
    }

    public UUID getStaffUuid() {
        return staffUuid;
    }

    public UUID getLastRandomTeleportTarget() {
        return lastRandomTeleportTarget;
    }

    public void setLastRandomTeleportTarget(UUID lastRandomTeleportTarget) {
        this.lastRandomTeleportTarget = lastRandomTeleportTarget;
    }
}
