package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.OrdersBrowseMenu;
import com.bx.ultimateDonutSmp.menus.OrdersCollectMenu;
import com.bx.ultimateDonutSmp.menus.OrdersMyOrdersMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OrdersCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public OrdersCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }

        String subcommand = args.length == 0 ? "" : args[0].toLowerCase();
        if (subcommand.equals("reload")) {
            if (!PermissionUtils.has(player, "ultimatedonutsmp.admin.orders")) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                        "ORDERS.NO_ADMIN_PERMISSION",
                        "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʀᴇʟᴏᴀᴅ ᴏʀᴅᴇʀѕ ѕᴇᴛᴛɪɴɢѕ."
                )));
                return true;
            }

            plugin.getConfigManager().reloadOrders();
            plugin.getOrdersManager().reload();
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.RELOADED",
                    "&aᴏʀᴅᴇʀѕ ᴄᴏɴꜰɪɢ ʀᴇʟᴏᴀᴅᴇᴅ."
            )));
            return true;
        }

        if (!plugin.getOrdersManager().isEnabled()) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.DISABLED",
                    "&cᴏʀᴅᴇʀѕ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."
            )));
            return true;
        }

        if (args.length == 0) {
            new OrdersBrowseMenu(plugin, 1, plugin.getOrdersManager().getDefaultSort(), "ALL").open(player);
            return true;
        }

        switch (subcommand) {
            case "my" -> new OrdersMyOrdersMenu(plugin, 1, plugin.getOrdersManager().getDefaultSort()).open(player);
            case "collect" -> {
                if (!plugin.getOrdersManager().isClaimsEnabled()) {
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                            "ORDERS.CLAIMS_DISABLED",
                            "&cᴏʀᴅᴇʀѕ ᴄʟᴀɪᴍѕ ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."
                    )));
                    return true;
                }
                new OrdersCollectMenu(plugin, 1).open(player);
            }
            default -> new OrdersBrowseMenu(plugin, 1, plugin.getOrdersManager().getDefaultSort(), "ALL").open(player);
        }

        return true;
    }
}
