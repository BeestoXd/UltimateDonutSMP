package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.VoiceChatConsentManager;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * The punishment message text, and the delivery of it, shared by the direct punishment commands and
 * by {@code /offend}. Both used to carry their own copy, so every new punishment type had to be
 * added in two places, and the switch in {@link #applyRuntimeEffect} is a statement rather than an
 * expression: the compiler never checked that the two copies handled the same set of types.
 *
 * <p>The two callers differ in one respect. The direct commands close the message with a line of
 * advice ("appeal at", "you may reconnect"); {@code /offend} leaves it off. That is what
 * {@code closingAdvice} selects between.
 */
class PunishmentMessages {

    private final UltimateDonutSmp plugin;
    private final boolean closingAdvice;

    PunishmentMessages(UltimateDonutSmp plugin, boolean closingAdvice) {
        this.plugin = plugin;
        this.closingAdvice = closingAdvice;
    }

    void applyRuntimeEffect(PunishmentType type, Player onlineTarget, PunishmentRecord record) {
        if (onlineTarget == null) {
            return;
        }

        switch (type) {
            case BAN, BLACKLIST, KICK -> onlineTarget.kickPlayer(ColorUtils.toComponent(build(record)));
            case WARN -> onlineTarget.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "PUNISHMENTS.WARN-RECEIVED",
                            "&cWarning: &f{reason}",
                            "{reason}", record.getReason()
                    )
            ));
            case MUTE -> onlineTarget.sendMessage(ColorUtils.toComponent(build(record)));
            case VOICE_MUTE -> {
                refreshVoiceMute(onlineTarget.getUniqueId(), onlineTarget.getName());
                onlineTarget.sendMessage(ColorUtils.toComponent(build(record)));
            }
        }
    }

    /**
     * The microphone gate reads a cache rather than the database, so a voice mute only takes hold
     * once that cache has been told about the record that was just written or removed.
     */
    void refreshVoiceMute(UUID targetUuid, String targetName) {
        VoiceChatConsentManager manager = plugin.getVoiceChatConsentManager();
        if (manager != null) {
            manager.refreshVoiceMute(targetUuid, targetName);
        }
    }

    String build(PunishmentRecord record) {
        return plugin.getConfigManager().getMessageOrDefault(
                messagePath(record.getType()),
                defaultMessage(record.getType()),
                placeholders(record)
        );
    }

    static String messagePath(PunishmentType type) {
        return switch (type) {
            case BAN -> "PUNISHMENTS.BAN";
            case KICK -> "PUNISHMENTS.KICK";
            case MUTE -> "PUNISHMENTS.MUTE";
            case VOICE_MUTE -> "PUNISHMENTS.VOICE-MUTE";
            case BLACKLIST -> "PUNISHMENTS.BLACKLIST";
            case WARN -> "PUNISHMENTS.WARN-RECEIVED";
        };
    }

    String defaultMessage(PunishmentType type) {
        String advice = closingAdvice ? closingAdvice(type) : "";
        return advice.isEmpty() ? messageBody(type) : messageBody(type) + "\n" + advice;
    }

    /** The message without the closing line of advice. */
    static String messageBody(PunishmentType type) {
        return switch (type) {
            case BAN -> "&c&lyou have been banned!\n&8&m----------------------------\n&7reason: &f%reason%\n&7expires: &f%nicest_expiration%\n&7banned by: &f%issuer%\n&8&m----------------------------";
            case KICK -> "&c&lyou have been kicked!\n&8&m----------------------------\n&7reason: &f%reason%\n&7kicked by: &f%issuer%\n&8&m----------------------------";
            case VOICE_MUTE -> "&c&lyou have been voice muted!\n&8&m----------------------------\n&7reason: &f%reason%\n&7expires: &f%nicest_expiration%\n&7muted by: &f%issuer%\n&8&m----------------------------";
            case MUTE -> "&c&lyou have been muted!\n&8&m----------------------------\n&7reason: &f%reason%\n&7expires: &f%nicest_expiration%\n&7muted by: &f%issuer%\n&8&m----------------------------";
            case BLACKLIST -> "&4&lyou have been blacklisted!\n&8&m----------------------------\n&7reason: &f%reason%\n&7blacklisted by: &f%issuer%\n&8&m----------------------------";
            case WARN -> "&cwarning: &f{reason}";
        };
    }

    /** The closing line the direct commands add. A warning carries none. */
    static String closingAdvice(PunishmentType type) {
        return switch (type) {
            case BAN -> "&7appeal at: &fdiscord.example.space";
            case KICK -> "&7you may reconnect";
            case VOICE_MUTE -> "&7you cannot speak in voice chat";
            case MUTE -> "&7you cannot speak in chat";
            case BLACKLIST -> "&4you cannot join the server";
            case WARN -> "";
        };
    }

    String[] placeholders(PunishmentRecord record) {
        String expires = formatExpires(record);
        String issuer = formatIssuer(record);
        String reason = record == null || record.getReason() == null ? "" : record.getReason();
        String player = record == null || record.getTargetNameSnapshot() == null ? "" : record.getTargetNameSnapshot();
        String id = record == null ? "" : String.valueOf(record.getId());
        String type = record == null || record.getType() == null ? "" : record.getType().name();

        return new String[]{
                "%reason%", reason,
                "{reason}", reason,

                "%nicest_expiration%", expires,
                "{nicest_expiration}", expires,
                "%expires%", expires,
                "{expires}", expires,
                "%expires_at%", expires,
                "{expires_at}", expires,
                "%expiration%", expires,
                "{expiration}", expires,
                "%expiry%", expires,
                "{expiry}", expires,
                "%duration%", expires,
                "{duration}", expires,

                "%issuer%", issuer,
                "{issuer}", issuer,
                "%staff%", issuer,
                "{staff}", issuer,
                "%by%", issuer,
                "{by}", issuer,

                "%player%", player,
                "{player}", player,
                "%target%", player,
                "{target}", player,

                "%id%", id,
                "{id}", id,

                "%type%", type,
                "{type}", type
        };
    }

    private String formatExpires(PunishmentRecord record) {
        if (record == null || record.getExpiresAt() == null) {
            return "Permanent";
        }
        long remainingSeconds = Math.max(0L, (record.getExpiresAt() - System.currentTimeMillis()) / 1000L);
        if (remainingSeconds <= 0L) {
            return "Expired";
        }
        if (plugin != null && plugin.getLanguageManager() != null) {
            return plugin.getLanguageManager().formatDuration(remainingSeconds, true);
        }
        return NumberUtils.formatCountdown(remainingSeconds);
    }

    private static String formatIssuer(PunishmentRecord record) {
        if (record == null) return "unknown";
        String issuer = record.getIssuerNameSnapshot();
        return issuer == null || issuer.isBlank() ? "unknown" : issuer;
    }
}
