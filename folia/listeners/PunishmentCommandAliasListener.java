package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Locale;
import java.util.Map;

public class PunishmentCommandAliasListener implements Listener {

    private static final Map<String, String> PUNISHMENT_COMMANDS = Map.ofEntries(
            Map.entry("ban", "ban"),
            Map.entry("tempban", "tempban"),
            Map.entry("mute", "mute"),
            Map.entry("tempmute", "tempmute"),
            Map.entry("warn", "warn"),
            Map.entry("kick", "kick"),
            Map.entry("blacklist", "blacklist"),
            Map.entry("unban", "unban"),
            Map.entry("pardon", "unban"),
            Map.entry("unmute", "unmute"),
            Map.entry("unblacklist", "unblacklist"),
            Map.entry("punishments", "punishments"),
            Map.entry("phistory", "punishments")
    );

    private final String namespace;

    public PunishmentCommandAliasListener(UltimateDonutSmp plugin) {
        this.namespace = plugin.getName().toLowerCase(Locale.ROOT);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String rewritten = rewrite(event.getMessage(), true);
        if (rewritten != null) {
            event.setMessage(rewritten);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent event) {
        String rewritten = rewrite(event.getCommand(), false);
        if (rewritten != null) {
            event.setCommand(rewritten);
        }
    }

    private String rewrite(String commandLine, boolean slashPrefixed) {
        if (commandLine == null || commandLine.isBlank()) {
            return null;
        }

        String trimmed = commandLine.trim();
        if (slashPrefixed) {
            if (!trimmed.startsWith("/")) {
                return null;
            }
            trimmed = trimmed.substring(1);
        }

        if (trimmed.isBlank()) {
            return null;
        }

        int separator = firstWhitespaceIndex(trimmed);
        String label = separator == -1 ? trimmed : trimmed.substring(0, separator);
        if (label.contains(":")) {
            return null;
        }

        String targetCommand = PUNISHMENT_COMMANDS.get(label.toLowerCase(Locale.ROOT));
        if (targetCommand == null) {
            return null;
        }

        String arguments = separator == -1 ? "" : trimmed.substring(separator);
        String rewritten = namespace + ":" + targetCommand + arguments;
        return slashPrefixed ? "/" + rewritten : rewritten;
    }

    private int firstWhitespaceIndex(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (Character.isWhitespace(value.charAt(index))) {
                return index;
            }
        }
        return -1;
    }
}
