package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.DuelQueueMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public QueueCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (!plugin.getDuelManager().isEnabled()) {
            player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent("&cDuels are currently disabled."));
            return true;
        }

        if (args.length == 0) {
            new DuelQueueMenu(plugin).open(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "join" -> plugin.getDuelManager().joinQueue(player);
            case "leave" -> plugin.getDuelManager().leaveState(player);
            default -> new DuelQueueMenu(plugin).open(player);
        }
        return true;
    }
}
