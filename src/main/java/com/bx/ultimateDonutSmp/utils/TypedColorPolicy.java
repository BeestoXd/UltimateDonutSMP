package com.bx.ultimateDonutSmp.utils;

import org.bukkit.permissions.Permissible;

/**
 * Decides whether the colour codes a player typed into their own message survive into the line it
 * is printed as.
 *
 * <p>Public chat never colours what a player typed, so it needs none of this. Private messages and
 * staff chat both drop the message into their format and colour the whole line afterwards, which
 * handed anyone able to use them a way to colour their own text, hide it behind {@code &k}, or wipe
 * the format's own colours from that point on with {@code &r}. Those codes now come out of the
 * message unless the sender holds {@link #PERMISSION}.
 *
 * <p>Staff chat crosses servers, so this runs where a message is sent rather than where it is
 * printed: the sender's permissions only exist on the server they are actually on.
 */
public final class TypedColorPolicy {

    public static final String PERMISSION = "ultimatedonutsmp.chat.color";

    private TypedColorPolicy() {
    }

    /**
     * Returns the message as it should travel onward: unchanged for a sender allowed to colour
     * their own text, and with every colour code and tag taken out for anyone else.
     */
    public static String apply(Permissible sender, String message) {
        if (message == null || message.isEmpty() || mayColour(sender)) {
            return message;
        }
        return ColorUtils.strip(message);
    }

    public static boolean mayColour(Permissible sender) {
        return PermissionUtils.has(sender, PERMISSION);
    }
}
