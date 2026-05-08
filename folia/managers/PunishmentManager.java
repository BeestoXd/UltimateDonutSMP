package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PunishmentQuery;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentScope;
import com.bx.ultimateDonutSmp.models.PunishmentState;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class PunishmentManager {

    public static final String VIEW_PERMISSION = "ultimatedonutsmp.staff.punishments.view";
    public static final String DELETE_PERMISSION = "ultimatedonutsmp.staff.punishments.delete";
    private static final Pattern MINECRAFT_USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    private final UltimateDonutSmp plugin;

    public PunishmentManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean canView(Player viewer) {
        return viewer != null && viewer.hasPermission(VIEW_PERMISSION);
    }

    public Optional<UUID> resolveTargetUuid(String username) {
        return resolveTargetUuid(username, false);
    }

    public Optional<UUID> resolveTargetUuid(String username, boolean allowOfflineFallback) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        UUID parsedUuid = parseUuid(username);
        if (parsedUuid != null) {
            return Optional.of(parsedUuid);
        }

        Player online = findOnlinePlayer(username);
        if (online != null) {
            return Optional.of(online.getUniqueId());
        }

        UUID playerUuid = plugin.getDatabaseManager().findPlayerUuidByUsername(username);
        if (playerUuid != null) {
            return Optional.of(playerUuid);
        }

        UUID punishedUuid = plugin.getDatabaseManager().findPunishmentTargetUuidByName(username);
        if (punishedUuid != null) {
            return Optional.of(punishedUuid);
        }

        if (!allowOfflineFallback || !isValidUsername(username)) {
            return Optional.empty();
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(username);
        return Optional.ofNullable(offline.getUniqueId());
    }

    public String resolveTargetName(UUID uuid) {
        if (uuid == null) {
            return "Unknown";
        }

        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            return online.getName();
        }

        String knownName = plugin.getDatabaseManager().getLastKnownUsername(uuid);
        if (knownName != null && !knownName.isBlank()) {
            return knownName;
        }

        String punishmentName = plugin.getDatabaseManager().getLatestPunishmentTargetName(uuid);
        if (punishmentName != null && !punishmentName.isBlank()) {
            return punishmentName;
        }

        return uuid.toString().substring(0, 8);
    }

    public String resolveTargetName(UUID uuid, String fallbackName) {
        String resolvedName = resolveTargetName(uuid);
        if (uuid == null || !resolvedName.equals(uuid.toString().substring(0, 8))) {
            return resolvedName;
        }
        return isValidUsername(fallbackName) ? fallbackName : resolvedName;
    }

    public Optional<PunishmentRecord> getRecord(long punishmentId) {
        return Optional.ofNullable(plugin.getDatabaseManager().loadPunishmentRecord(punishmentId));
    }

    public int countHistory(UUID targetUuid, PunishmentQuery query) {
        return plugin.getDatabaseManager().countPunishmentHistory(
                targetUuid,
                query == null ? PunishmentQuery.defaultQuery() : query,
                System.currentTimeMillis()
        );
    }

    public List<PunishmentRecord> getHistory(UUID targetUuid, int limit, int offset, PunishmentQuery query) {
        return plugin.getDatabaseManager().loadPunishmentHistory(
                targetUuid,
                query == null ? PunishmentQuery.defaultQuery() : query,
                Math.max(1, limit),
                Math.max(0, offset),
                System.currentTimeMillis()
        );
    }

    public Optional<PunishmentRecord> getActiveRecord(UUID targetUuid, PunishmentType type) {
        if (targetUuid == null || type == null) {
            return Optional.empty();
        }

        List<PunishmentRecord> records = getHistory(
                targetUuid,
                1,
                0,
                new PunishmentQuery(type, com.bx.ultimateDonutSmp.models.PunishmentFilterState.ACTIVE, null)
        );
        return records.stream().findFirst();
    }

    public Optional<PunishmentRecord> getActiveRecord(UUID targetUuid, PunishmentType... types) {
        if (targetUuid == null || types == null || types.length == 0) {
            return Optional.empty();
        }

        PunishmentRecord newest = null;
        for (PunishmentType type : types) {
            PunishmentRecord record = getActiveRecord(targetUuid, type).orElse(null);
            if (record != null && (newest == null || record.getIssuedAt() > newest.getIssuedAt())) {
                newest = record;
            }
        }
        return Optional.ofNullable(newest);
    }

    public boolean markActiveRecordsRemoved(UUID targetUuid, PunishmentType type, PunishmentRemovalRequest request) {
        if (targetUuid == null || type == null || request == null) {
            return false;
        }

        List<PunishmentRecord> activeRecords = getHistory(
                targetUuid,
                100,
                0,
                new PunishmentQuery(type, com.bx.ultimateDonutSmp.models.PunishmentFilterState.ACTIVE, null)
        );
        boolean changed = false;
        for (PunishmentRecord record : activeRecords) {
            changed |= markRemoved(record.getId(), request);
        }
        return changed;
    }

    public PunishmentRecord createRecord(PunishmentCreateRequest request) {
        if (request == null || request.targetUuid() == null) {
            return null;
        }

        String targetName = resolveNameSnapshot(request.targetUuid(), request.targetNameSnapshot(), false);
        String issuerName = resolveNameSnapshot(request.issuerUuid(), request.issuerNameSnapshot(), true);
        long issuedAt = request.issuedAt() > 0L ? request.issuedAt() : System.currentTimeMillis();
        Long expiresAt = normalizeTimestamp(request.expiresAt());

        PunishmentRecord unsaved = new PunishmentRecord(
                0L,
                request.targetUuid(),
                targetName,
                request.type() == null ? PunishmentType.WARN : request.type(),
                request.reason(),
                request.issuerUuid(),
                issuerName,
                issuedAt,
                expiresAt,
                null,
                "",
                null,
                "",
                request.sourceServer() == null || request.sourceServer().isBlank() ? "local" : request.sourceServer(),
                request.scope() == null ? PunishmentScope.SERVER : request.scope()
        );

        long id = plugin.getDatabaseManager().createPunishmentRecord(unsaved);
        if (id <= 0L) {
            return null;
        }

        return new PunishmentRecord(
                id,
                unsaved.getTargetUuid(),
                unsaved.getTargetNameSnapshot(),
                unsaved.getType(),
                unsaved.getReason(),
                unsaved.getIssuerUuid(),
                unsaved.getIssuerNameSnapshot(),
                unsaved.getIssuedAt(),
                unsaved.getExpiresAt(),
                null,
                "",
                null,
                "",
                unsaved.getSourceServer(),
                unsaved.getScope()
        );
    }

    public boolean markRemoved(long punishmentId, PunishmentRemovalRequest request) {
        if (punishmentId <= 0L || request == null) {
            return false;
        }

        PunishmentRecord existing = plugin.getDatabaseManager().loadPunishmentRecord(punishmentId);
        if (existing == null) {
            return false;
        }

        String removedByName = resolveNameSnapshot(request.removedByUuid(), request.removedByNameSnapshot(), true);
        long removedAt = request.removedAt() > 0L ? request.removedAt() : System.currentTimeMillis();
        String removalReason = request.removalReason();
        if (removalReason == null || removalReason.isBlank()) {
            removalReason = getState(existing) == PunishmentState.EXPIRED ? "Expired" : "Removed";
        }

        return plugin.getDatabaseManager().markPunishmentRemoved(
                punishmentId,
                request.removedByUuid(),
                removedByName,
                removedAt,
                removalReason
        );
    }

    public boolean deleteRecord(long punishmentId) {
        return plugin.getDatabaseManager().deletePunishmentRecord(punishmentId);
    }

    public PunishmentState getState(PunishmentRecord record) {
        if (record == null) {
            return PunishmentState.REMOVED;
        }
        if (record.isRemoved()) {
            return PunishmentState.REMOVED;
        }
        if (record.hasExpiry() && record.getExpiresAt() != null && record.getExpiresAt() <= System.currentTimeMillis()) {
            return PunishmentState.EXPIRED;
        }
        return PunishmentState.ACTIVE;
    }

    public boolean isActive(PunishmentRecord record) {
        return getState(record).isActive();
    }

    public String getDisplayType(PunishmentRecord record) {
        if (record == null) {
            return "UNKNOWN";
        }
        if (!record.isTemporary()) {
            return record.getType().name();
        }
        return switch (record.getType()) {
            case BAN -> "TEMPBAN";
            case MUTE -> "TEMPMUTE";
            default -> record.getType().name();
        };
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean isValidUsername(String username) {
        return username != null && MINECRAFT_USERNAME.matcher(username).matches();
    }

    private String resolveNameSnapshot(UUID uuid, String providedSnapshot, boolean fallbackToConsole) {
        if (providedSnapshot != null && !providedSnapshot.isBlank()) {
            return providedSnapshot;
        }

        if (uuid != null) {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                return online.getName();
            }

            String knownName = plugin.getDatabaseManager().getLastKnownUsername(uuid);
            if (knownName != null && !knownName.isBlank()) {
                return knownName;
            }

            String punishmentName = plugin.getDatabaseManager().getLatestPunishmentTargetName(uuid);
            if (punishmentName != null && !punishmentName.isBlank()) {
                return punishmentName;
            }

            return uuid.toString().substring(0, 8);
        }

        return fallbackToConsole ? "Console" : "Unknown";
    }

    private Long normalizeTimestamp(Long timestamp) {
        return timestamp == null || timestamp <= 0L ? null : timestamp;
    }

    private Player findOnlinePlayer(String username) {
        Player exact = Bukkit.getPlayerExact(username);
        if (exact != null) {
            return exact;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }

    public record PunishmentCreateRequest(
            UUID targetUuid,
            String targetNameSnapshot,
            PunishmentType type,
            String reason,
            UUID issuerUuid,
            String issuerNameSnapshot,
            long issuedAt,
            Long expiresAt,
            String sourceServer,
            PunishmentScope scope
    ) {
    }

    public record PunishmentRemovalRequest(
            UUID removedByUuid,
            String removedByNameSnapshot,
            long removedAt,
            String removalReason
    ) {
    }
}
