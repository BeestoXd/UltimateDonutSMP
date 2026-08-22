package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.ChatLogsMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatLogCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "ultimatedonutsmp.admin.chatlog";

    private final UltimateDonutSmp plugin;

    public ChatLogCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (!viewer.hasPermission(PERMISSION)) {
            viewer.sendMessage(ColorUtils.toComponent("&cYou do not have permission to use this command."));
            return true;
        }

        if (args.length > 1) {
            viewer.sendMessage(ColorUtils.toComponent("&cUsage: /chatlog [player]"));
            return true;
        }

        if (args.length == 0) {
            new ChatLogsMenu(plugin, null, null).open(viewer);
            return true;
        }

        String input = args[0].trim();
        Player onlineTarget = findOnlineTarget(input);
        UUID targetUuid = onlineTarget == null
                ? plugin.getDatabaseManager().findPlayerUuidByUsername(input)
                : onlineTarget.getUniqueId();
        if (targetUuid == null) {
            viewer.sendMessage(ColorUtils.toComponent("&cPlayer not found."));
            return true;
        }

        String targetName = onlineTarget == null
                ? plugin.getDatabaseManager().getLastKnownUsername(targetUuid)
                : onlineTarget.getName();
        if (targetName == null || targetName.isBlank()) {
            targetName = input;
        }

        new ChatLogsMenu(plugin, targetUuid, targetName).open(viewer);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION) || args.length != 1) {
            return Collections.emptyList();
        }

        String input = args[0].toLowerCase(Locale.ROOT);
        List<String> suggestions = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase(Locale.ROOT).startsWith(input)) {
                suggestions.add(player.getName());
            }
        }
        return suggestions;
    }

    private Player findOnlineTarget(String username) {
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
}
