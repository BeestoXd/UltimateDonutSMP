package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.PunishmentHistoryMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PunishmentHistoryCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public PunishmentHistoryCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.PLAYER-ONLY",
                    "&cOnly players can use this command."
            ));
            return true;
        }

        if (!plugin.getPunishmentManager().canView(player)) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.NO-PERMISSION",
                    "&cYou do not have permission to view punishment history."
            )));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.USAGE",
                    "&cUsage: /" + label + " <player>"
            )));
            return true;
        }

        UUID targetUuid = plugin.getPunishmentManager().resolveTargetUuid(args[0], true).orElse(null);
        if (targetUuid == null) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "PUNISHMENTS.NOT-FOUND",
                    "&cPlayer not found."
            )));
            return true;
        }

        new PunishmentHistoryMenu(plugin, targetUuid, false).open(player);
        return true;
    }
}
