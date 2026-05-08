package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public final class FreezeState {

    private final UUID targetUuid;
    private final String targetNameSnapshot;
    private final UUID frozenByUuid;
    private final String frozenByNameSnapshot;
    private final long frozenAt;
    private final String sourceServer;

    public FreezeState(UUID targetUuid,
                       String targetNameSnapshot,
                       UUID frozenByUuid,
                       String frozenByNameSnapshot,
                       long frozenAt,
                       String sourceServer) {
        this.targetUuid = targetUuid;
        this.targetNameSnapshot = targetNameSnapshot == null ? "" : targetNameSnapshot;
        this.frozenByUuid = frozenByUuid;
        this.frozenByNameSnapshot = frozenByNameSnapshot == null ? "" : frozenByNameSnapshot;
        this.frozenAt = frozenAt;
        this.sourceServer = sourceServer == null || sourceServer.isBlank() ? "local" : sourceServer;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetNameSnapshot() {
        return targetNameSnapshot;
    }

    public UUID getFrozenByUuid() {
        return frozenByUuid;
    }

    public String getFrozenByNameSnapshot() {
        return frozenByNameSnapshot;
    }

    public long getFrozenAt() {
        return frozenAt;
    }

    public String getSourceServer() {
        return sourceServer;
    }
}
