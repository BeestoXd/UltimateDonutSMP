package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.DuelClaimMenu;
import com.bx.ultimateDonutSmp.menus.DuelCreateMenu;
import com.bx.ultimateDonutSmp.menus.DuelQueueMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public DuelCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (!plugin.getDuelManager().isEnabled() && (args.length == 0 || !"reload".equalsIgnoreCase(args[0]))) {
            player.sendMessage(ColorUtils.toComponent("&cDuels are currently disabled."));
            return true;
        }

        if (args.length == 0) {
            new DuelQueueMenu(plugin).open(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        if (subcommand.equals("claims")) {
            new DuelClaimMenu(plugin, 1).open(player);
            return true;
        }
        if (subcommand.equals("reload")) {
            if (!player.hasPermission("ultimatedonutsmp.admin.duels")) {
                player.sendMessage(ColorUtils.toComponent("&cYou do not have permission to reload duels."));
                return true;
            }
            plugin.getConfigManager().reloadDuels();
            plugin.getDuelManager().reload();
            player.sendMessage(ColorUtils.toComponent("&aDuels config reloaded."));
            return true;
        }
        if (subcommand.equals("accept")) {
            plugin.getDuelManager().acceptChallenge(player, args.length > 1 ? args[1] : null);
            return true;
        }
        if (subcommand.equals("deny")) {
            plugin.getDuelManager().denyChallenge(player, args.length > 1 ? args[1] : null);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cThat player is not online."));
            return true;
        }

        new DuelCreateMenu(plugin, target.getUniqueId()).open(player);
        return true;
    }
}
