package com.bx.ultimateDonutSmp.utils;

import org.bukkit.command.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Owns the unprefixed labels of a feature command when that feature is live, and gives them back
 * when {@code DISABLED_COMMAND_ACTION} is {@code UNREGISTER}.
 *
 * <p>The old map edit dropped {@code enderchest} and {@code ec} without asking who currently held
 * those keys. Another plugin that had already registered them lost the unprefixed command, and
 * players got vanilla unknown-command even though the other plugin was still on the server.</p>
 */
public final class CommandMapFeatureSync {

    private CommandMapFeatureSync() {
    }

    public static void apply(
            Command command,
            Map<String, Command> knownCommands,
            String fallbackPrefix,
            boolean claim
    ) {
        if (command == null || knownCommands == null || fallbackPrefix == null) {
            return;
        }

        String prefix = key(fallbackPrefix);
        String name = key(command.getName());
        if (prefix.isEmpty() || name.isEmpty()) {
            return;
        }

        List<String> labels = new ArrayList<>();
        labels.add(name);
        List<String> aliases = command.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                String aliasKey = key(alias);
                if (!aliasKey.isEmpty() && !aliasKey.equals(name)) {
                    labels.add(aliasKey);
                }
            }
        }

        if (claim) {
            for (String label : labels) {
                knownCommands.put(label, command);
                knownCommands.put(prefix + ":" + label, command);
            }
            return;
        }

        for (String label : labels) {
            yieldLabel(knownCommands, label, command, true);
            yieldLabel(knownCommands, prefix + ":" + label, command, false);
        }
    }

    private static void yieldLabel(
            Map<String, Command> knownCommands,
            String label,
            Command ours,
            boolean restoreForeign
    ) {
        Command current = knownCommands.get(label);
        if (current == ours) {
            knownCommands.remove(label);
            current = null;
        } else if (current != null) {
            return;
        }
        if (!restoreForeign || current != null) {
            return;
        }
        Command replacement = findReplacement(knownCommands, label, ours);
        if (replacement != null) {
            knownCommands.put(label, replacement);
        }
    }

    static Command findReplacement(Map<String, Command> knownCommands, String label, Command ours) {
        String needle = key(label);
        if (needle.isEmpty()) {
            return null;
        }

        Command aliasMatch = null;
        for (Command candidate : knownCommands.values()) {
            if (candidate == null || candidate == ours) {
                continue;
            }
            if (needle.equals(key(candidate.getName()))) {
                return candidate;
            }
            if (aliasMatch != null) {
                continue;
            }
            List<String> aliases = candidate.getAliases();
            if (aliases == null) {
                continue;
            }
            for (String alias : aliases) {
                if (needle.equals(key(alias))) {
                    aliasMatch = candidate;
                    break;
                }
            }
        }
        return aliasMatch;
    }

    private static String key(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ENGLISH).trim();
    }
}
