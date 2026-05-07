package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ReportCommand implements TabExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.report";

    private final UltimateDonutSmp plugin;

    public ReportCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player reporter)) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "REPORT.PLAYER_ONLY",
                    "&cOnly players can use report."
            )));
            return true;
        }

        if (!reporter.hasPermission(PERMISSION)) {
            reporter.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "REPORT.NO_PERMISSION",
                    "&cYou do not have permission to report players."
            )));
            return true;
        }

        if (args.length < 2) {
            reporter.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "REPORT.USAGE",
                    "&cUsage: /report <player> <reason>"
            )));
            return true;
        }

        Player reported = plugin.getServer().getPlayerExact(args[0]);
        if (reported == null) {
            reported = findOnlinePlayer(args[0]);
        }

        if (reported == null) {
            reporter.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "REPORT.PLAYER_NOT_FOUND",
                    "&cPlayer not found."
            )));
            return true;
        }

        plugin.getNetworkStaffAlertManager().sendReport(reporter, reported, joinArgs(args, 1));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1 || !sender.hasPermission(PERMISSION)) {
            return List.of();
        }

        String input = args[0].toLowerCase();
        List<String> suggestions = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (sender instanceof Player reporter && reporter.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            if (player.getName().toLowerCase().startsWith(input)) {
                suggestions.add(player.getName());
            }
        }
        return suggestions;
    }

    private Player findOnlinePlayer(String input) {
        String lowerInput = input.toLowerCase();
        Player match = null;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.getName().toLowerCase().startsWith(lowerInput)) {
                continue;
            }
            if (match != null) {
                return null;
            }
            match = player;
        }
        return match;
    }

    private String joinArgs(String[] args, int startIndex) {
        StringBuilder builder = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }
}
