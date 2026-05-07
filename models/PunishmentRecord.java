package com.bx.ultimateDonutSmp.models;

import java.util.UUID;

public final class PunishmentRecord {

    private final long id;
    private final UUID targetUuid;
    private final String targetNameSnapshot;
    private final PunishmentType type;
    private final String reason;
    private final UUID issuerUuid;
    private final String issuerNameSnapshot;
    private final long issuedAt;
    private final Long expiresAt;
    private final UUID removedByUuid;
    private final String removedByNameSnapshot;
    private final Long removedAt;
    private final String removalReason;
    private final String sourceServer;
    private final PunishmentScope scope;

    public PunishmentRecord(long id,
                            UUID targetUuid,
                            String targetNameSnapshot,
                            PunishmentType type,
                            String reason,
                            UUID issuerUuid,
                            String issuerNameSnapshot,
                            long issuedAt,
                            Long expiresAt,
                            UUID removedByUuid,
                            String removedByNameSnapshot,
                            Long removedAt,
                            String removalReason,
                            String sourceServer,
                            PunishmentScope scope) {
        this.id = id;
        this.targetUuid = targetUuid;
        this.targetNameSnapshot = targetNameSnapshot == null ? "" : targetNameSnapshot;
        this.type = type == null ? PunishmentType.WARN : type;
        this.reason = reason == null || reason.isBlank() ? "No reason specified" : reason;
        this.issuerUuid = issuerUuid;
        this.issuerNameSnapshot = issuerNameSnapshot == null ? "" : issuerNameSnapshot;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.removedByUuid = removedByUuid;
        this.removedByNameSnapshot = removedByNameSnapshot == null ? "" : removedByNameSnapshot;
        this.removedAt = removedAt;
        this.removalReason = removalReason == null ? "" : removalReason;
        this.sourceServer = sourceServer == null || sourceServer.isBlank() ? "local" : sourceServer;
        this.scope = scope == null ? PunishmentScope.SERVER : scope;
    }

    public long getId() {
        return id;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetNameSnapshot() {
        return targetNameSnapshot;
    }

    public PunishmentType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public UUID getIssuerUuid() {
        return issuerUuid;
    }

    public String getIssuerNameSnapshot() {
        return issuerNameSnapshot;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public UUID getRemovedByUuid() {
        return removedByUuid;
    }

    public String getRemovedByNameSnapshot() {
        return removedByNameSnapshot;
    }

    public Long getRemovedAt() {
        return removedAt;
    }

    public String getRemovalReason() {
        return removalReason;
    }

    public String getSourceServer() {
        return sourceServer;
    }

    public PunishmentScope getScope() {
        return scope;
    }

    public boolean hasExpiry() {
        return expiresAt != null && expiresAt > 0L;
    }

    public boolean isTemporary() {
        return hasExpiry();
    }

    public boolean isRemoved() {
        return removedAt != null && removedAt > 0L;
    }
}
