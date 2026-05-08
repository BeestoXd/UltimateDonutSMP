package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.WorthMenu;
import com.bx.ultimateDonutSmp.models.WorthResult;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WorthCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public WorthCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean pricesAlias = label.equalsIgnoreCase("prices");

        if (args.length > 0) {
            String subcommand = args[0].toLowerCase();
            if (subcommand.equals("browse") || subcommand.equals("prices")) {
                openBrowser(sender);
                return true;
            }

            if (subcommand.equals("reload")) {
                if (!sender.hasPermission("ultimatedonutsmp.admin.worth")) {
                    sender.sendMessage(ColorUtils.colorize(
                            plugin.getConfigManager().getMessages().getString(
                                    "WORTH.NO-ADMIN-PERMISSION",
                                    "&cYou do not have permission to reload worth settings."
                            )));
                    return true;
                }

                plugin.getConfigManager().reloadWorth();
                plugin.getWorthManager().reload();
                sender.sendMessage(ColorUtils.colorize(
                        plugin.getConfigManager().getMessages().getString(
                                "WORTH.RELOADED",
                                "&aWorth config reloaded."
                        )));
                return true;
            }

            if (!subcommand.equals("hand")
                    && !subcommand.equals("held")
                    && !subcommand.equals("item")
                    && !subcommand.equals("check")) {
                openBrowser(sender);
                return true;
            }
        }

        if (pricesAlias || args.length == 0) {
            openBrowser(sender);
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(ColorUtils.toComponent("&cHold an item to check its worth."));
            return true;
        }

        WorthResult worthResult = plugin.getWorthManager().resolveWorth(item);
        if (!worthResult.sellable()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessage("WORTH.NO-SELLABLE")));
            return true;
        }

        String name = plugin.getWorthManager().prettifyMaterial(item.getType()).toLowerCase();
        String msg = item.getAmount() == 1
                ? plugin.getConfigManager().getMessage("WORTH.DEFAULT",
                    "{item}", name, "{price}", NumberUtils.format(worthResult.totalWorth()))
                : plugin.getConfigManager().getMessage("WORTH.HAND-ITEM",
                    "{amount}", String.valueOf(item.getAmount()),
                    "{item}", name,
                    "{total}", NumberUtils.format(worthResult.totalWorth()));
        player.sendMessage(ColorUtils.toComponent(msg));

        if (worthResult.container() && worthResult.hasContainerContentsWorth()) {
            String breakdown = plugin.getConfigManager().getMessages().getString(
                    "WORTH.CONTAINER-BREAKDOWN",
                    "&7Base: &f${base} &8| &7Contents: &f${contents}"
            );
            breakdown = breakdown
                    .replace("{base}", NumberUtils.formatNice(worthResult.baseWorth()))
                    .replace("{contents}", NumberUtils.formatNice(worthResult.containerContentsWorth()));
            player.sendMessage(ColorUtils.toComponent(breakdown));
        }
        return true;
    }

    private void openBrowser(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return;
        }

        new WorthMenu(plugin, 1).open(player);
    }
}
