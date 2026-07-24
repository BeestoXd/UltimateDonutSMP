package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.SellAllConfirmMenu;
import com.bx.ultimateDonutSmp.menus.SellHistoryMenu;
import com.bx.ultimateDonutSmp.menus.SellMenu;
import com.bx.ultimateDonutSmp.menus.SellStatsAdminMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public SellCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ."); return true; }

        if (label.equalsIgnoreCase("topsell") || label.equalsIgnoreCase("sellstats")) {
            return new SellStatsCommand(plugin).onCommand(sender, command, label, args);
        }

        if (args.length > 0 && (args[0].equalsIgnoreCase("admin") || args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("top"))) {
            return new SellStatsCommand(plugin).onCommand(sender, command, label, args);
        }

        switch (label.toLowerCase()) {
            case "sell"        -> new SellMenu(plugin).open(player);
            case "sellhand"    -> {
                double total = plugin.getShopManager().sellInventory(player, true);
                if (total <= 0) player.sendMessage(ColorUtils.toComponent(
                        plugin.getConfigManager().getMessage("WORTH.NO-SELLABLE")));
            }
            case "sellall"     -> new SellAllConfirmMenu(plugin).open(player);
            case "sellhistory" -> {
                if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
                    new SellStatsAdminMenu(plugin).open(player);
                } else {
                    new SellHistoryMenu(plugin).open(player);
                }
            }
        }
        return true;
    }
}
