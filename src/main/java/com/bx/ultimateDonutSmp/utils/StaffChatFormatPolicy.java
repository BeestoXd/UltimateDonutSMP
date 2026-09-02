package com.bx.ultimateDonutSmp.utils;

import java.util.function.UnaryOperator;

/**
 * Builds the line a staff chat message is printed as.
 *
 * <p>Two things decide the order of the work here. Placeholders written into the format belong to
 * the staff member who sent the message rather than to whoever is reading it, so they are resolved
 * once against the sender instead of per recipient; resolving them per recipient would give every
 * reader their own rank in front of somebody else's name. The message goes in after that, because
 * it is typed by a player: a placeholder somebody types into staff chat stays the text they typed.
 */
public final class StaffChatFormatPolicy {

    private StaffChatFormatPolicy() {
    }

    /**
     * Fills in one staff chat or staff notice format.
     *
     * @param format             the configured format, before any substitution
     * @param senderPlaceholders resolves placeholders against the sender, or null to skip that pass
     * @param serverName         display name of the server the message came from
     * @param senderName         name of the staff member who sent it
     * @param message            the message text, or the status word on a server status notice
     */
    public static String render(
            String format,
            UnaryOperator<String> senderPlaceholders,
            String serverName,
            String senderName,
            String message
    ) {
        String resolved = safe(format);
        if (senderPlaceholders != null) {
            resolved = safe(senderPlaceholders.apply(resolved));
        }

        // %message% goes last so that neither it nor the sender's name can be substituted into a
        // second time by whatever the player happened to type.
        return resolved
                .replace("%server%", safe(serverName))
                .replace("%player%", safe(senderName))
                .replace("%status%", safe(message))
                .replace("%message%", safe(message));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
