package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public LeaveCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (plugin.getFfaManager() != null) {
            if (plugin.getFfaManager().isInSession(player.getUniqueId())) {
                plugin.getFfaManager().leaveState(player);
                return true;
            }
        }

        if (plugin.getDuelManager() != null) {
            if (plugin.getDuelManager().isInQueue(player.getUniqueId())
                    || plugin.getDuelManager().isInDuel(player.getUniqueId())
                    || plugin.getDuelManager().isTransitioning(player.getUniqueId())) {
                plugin.getDuelManager().leaveState(player);
                return true;
            }
        }

        player.sendMessage(ColorUtils.toComponent("&cYou are not in a duel or FFA arena/match."));
        return true;
    }
}
