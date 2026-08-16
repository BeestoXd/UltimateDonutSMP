package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentState;
import com.bx.ultimateDonutSmp.models.PunishmentType;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shared rendering for punishment records so the per-player history and the server-wide list show the
 * same fields, colours and date format.
 */
final class PunishmentItemRenderer {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d yyyy, HH:mm:ss", Locale.US);

    private PunishmentItemRenderer() {}

    static String applyRecord(String value,
                              PunishmentRecord record,
                              PunishmentState state,
                              String displayType,
                              String playerName) {
        if (value == null) {
            return "";
        }

        String removedBy = safeText(record.getRemovedByNameSnapshot());
        String removalReason = safeText(record.getRemovalReason());
        String removedAt = formatTimestamp(record.getRemovedAt(), "N/A");

        if (state == PunishmentState.EXPIRED) {
            if (removedBy.equals("N/A")) {
                removedBy = "System";
            }
            if (removalReason.equals("N/A")) {
                removalReason = "Expired";
            }
            if (removedAt.equals("N/A")) {
                removedAt = formatTimestamp(record.getExpiresAt(), "N/A");
            }
        }

        return value
                .replace("{player}", playerName == null ? "" : playerName)
                .replace("{status_color}", statusColor(record, state))
                .replace("{type}", displayType)
                .replace("{reason}", record.getReason())
                .replace("{issuer}", safeText(record.getIssuerNameSnapshot()))
                .replace("{issued_at}", formatTimestamp(record.getIssuedAt(), "unknown"))
                .replace("{expires_at}", formatTimestamp(record.getExpiresAt(), "Never"))
                .replace("{eXpires_at}", formatTimestamp(record.getExpiresAt(), "Never"))
                .replace("{status}", state.getDisplayName())
                .replace("{removed_by}", removedBy)
                .replace("{removal_reason}", removalReason)
                .replace("{removed_at}", removedAt)
                .replace("{id}", String.valueOf(record.getId()))
                .replace("{scope}", record.getScope().name())
                .replace("{source_server}", record.getSourceServer());
    }

    static List<String> applyRecord(List<String> lines,
                                    PunishmentRecord record,
                                    PunishmentState state,
                                    String displayType,
                                    String playerName) {
        List<String> replaced = new ArrayList<>();
        for (String line : lines) {
            replaced.add(applyRecord(line, record, state, displayType, playerName));
        }
        return replaced;
    }

    static String defaultMaterial(PunishmentType type) {
        return switch (type) {
            case BAN -> "IRON_BARS";
            case MUTE -> "PAPER";
            case WARN -> "YELLOW_DYE";
            case KICK -> "LEATHER_BOOTS";
            case BLACKLIST -> "BARRIER";
        };
    }

    static String statusColor(PunishmentRecord record, PunishmentState state) {
        if (state == PunishmentState.EXPIRED) {
            return "&6";
        }
        if (state == PunishmentState.REMOVED) {
            return "&7";
        }

        return switch (record.getType()) {
            case BAN, BLACKLIST -> "&c";
            case MUTE -> "&d";
            case WARN -> "&e";
            case KICK -> "&6";
        };
    }

    static String formatTimestamp(Long timestamp, String fallback) {
        if (timestamp == null || timestamp <= 0L) {
            return toSmallCaps(fallback);
        }
        return toSmallCaps(DATE_FORMATTER.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())));
    }

    static String safeText(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    static String toSmallCaps(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (Character.toLowerCase(c)) {
                case 'a': sb.append('ᴀ'); break;
                case 'b': sb.append('ʙ'); break;
                case 'c': sb.append('ᴄ'); break;
                case 'd': sb.append('ᴅ'); break;
                case 'e': sb.append('ᴇ'); break;
                case 'f': sb.append('ꜰ'); break;
                case 'g': sb.append('ɢ'); break;
                case 'h': sb.append('ʜ'); break;
                case 'i': sb.append('ɪ'); break;
                case 'j': sb.append('ᴊ'); break;
                case 'k': sb.append('ᴋ'); break;
                case 'l': sb.append('ʟ'); break;
                case 'm': sb.append('ᴍ'); break;
                case 'n': sb.append('ɴ'); break;
                case 'o': sb.append('ᴏ'); break;
                case 'p': sb.append('ᴘ'); break;
                case 'q': sb.append('ǫ'); break;
                case 'r': sb.append('ʀ'); break;
                case 's': sb.append('ѕ'); break;
                case 't': sb.append('ᴛ'); break;
                case 'u': sb.append('ᴜ'); break;
                case 'v': sb.append('ᴠ'); break;
                case 'w': sb.append('ᴡ'); break;
                case 'x': sb.append('x'); break;
                case 'y': sb.append('ʏ'); break;
                case 'z': sb.append('ᴢ'); break;
                default: sb.append(c); break;
            }
        }
        return sb.toString();
    }
}
