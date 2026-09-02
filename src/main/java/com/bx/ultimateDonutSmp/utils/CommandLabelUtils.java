package com.bx.ultimateDonutSmp.utils;

import org.bukkit.command.Command;

import java.util.Locale;

public final class CommandLabelUtils {

    private CommandLabelUtils() {
    }

    /**
     * Returns the label a command was invoked under, without the plugin namespace.
     *
     * <p>Bukkit registers every command under {@code plugin:name} as well as its plain name and hands
     * whichever form was typed to {@code onCommand}, so a class that decides which command it is by
     * comparing the raw label matches nothing when the namespaced form is used.</p>
     */
    public static String normalizeLabel(String label, Command command) {
        String normalized = label == null || label.isBlank()
                ? (command == null ? "" : command.getName())
                : label;
        if (normalized == null) {
            return "";
        }

        int namespaceSeparator = normalized.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator + 1 < normalized.length()) {
            normalized = normalized.substring(namespaceSeparator + 1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }
}
