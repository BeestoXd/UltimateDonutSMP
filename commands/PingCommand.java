package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class PingCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public PingCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " [player]"));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <player>"));
                return true;
            }

            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "PING.SELF",
                            "&7Your ping is &b%ping%ms",
                            "%ping%",
                            String.valueOf(player.getPing())
                    ),
                    player
            ));
            return true;
        }

        Player target = findOnlinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ColorUtils.toComponent("&cPlayer not online."));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault(
                        "PING.OTHER",
                        "&e%player%'s &7ping is &b%ping%ms",
                        "%player%",
                        target.getName(),
                        "%ping%",
                        String.valueOf(target.getPing())
                )
        ));
        return true;
    }

    private Player findOnlinePlayer(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        Player exact = Bukkit.getPlayerExact(input);
        if (exact != null) {
            return exact;
        }

        String expected = input.toLowerCase(Locale.ROOT);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase(Locale.ROOT).equals(expected)) {
                return player;
            }
        }
        return null;
    }
}
