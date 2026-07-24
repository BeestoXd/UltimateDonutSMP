package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CurrencyManager;
import com.bx.ultimateDonutSmp.menus.WorthMenu;
import com.bx.ultimateDonutSmp.models.WorthResult;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WorthCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public WorthCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean pricesAlias = label.equalsIgnoreCase("prices");

        if (pricesAlias || args.length == 0) {
            openBrowser(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        if (subcommand.equals("browse") || subcommand.equals("prices")) {
            openBrowser(sender);
            return true;
        }

        if (subcommand.equals("reload")) {
            if (!PermissionUtils.has(sender, "ultimatedonutsmp.admin.worth")) {
                sendMessage(sender, plugin.getConfigManager().getMessages().getString(
                        "WORTH.NO-ADMIN-PERMISSION",
                        "&cyou do not have permission to reload worth settings."
                ));
                return true;
            }

            plugin.getConfigManager().reloadWorth();
            plugin.getWorthManager().reload();
            sendMessage(sender, plugin.getConfigManager().getMessages().getString(
                    "WORTH.RELOADED",
                    "&aᴡᴏʀᴛʜ ᴄᴏɴꜰɪɢ ʀᴇʟᴏᴀᴅᴇᴅ."
            ));
            return true;
        }

        if (subcommand.equals("hand")
                || subcommand.equals("held")
                || subcommand.equals("item")
                || subcommand.equals("check")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                sendMessage(player, "&cʜᴏʟᴅ ᴀɴ ɪᴛᴇᴍ ᴛᴏ ᴄʜᴇᴄᴋ ɪᴛѕ ᴡᴏʀᴛʜ.");
                return true;
            }

            sendWorthMessage(sender, item, item.getAmount());
            return true;
        }

        int amount = 1;
        int itemIndex = 0;
        try {
            int parsedAmount = Integer.parseInt(args[0]);
            if (parsedAmount > 0) {
                amount = parsedAmount;
                itemIndex = 1;
            }
        } catch (NumberFormatException ignored) {
            // Not a number, itemIndex stays 0
        }

        if (itemIndex >= args.length) {
            sendMessage(sender, plugin.getConfigManager().getMessages().getString(
                    "WORTH.USAGE",
                    "&cᴜѕᴀɢᴇ: /ᴡᴏʀᴛʜ [ᴀᴍᴏᴜɴᴛ] <ɪᴛᴇᴍ> or /ᴡᴏʀᴛʜ ʜᴀɴᴅ"
            ));
            return true;
        }

        String itemInput = String.join(" ", Arrays.copyOfRange(args, itemIndex, args.length));
        Material material = plugin.getWorthManager().findMaterial(itemInput);

        if (material == null || material.isAir()) {
            String msg = plugin.getConfigManager().getMessages().getString(
                    "WORTH.UNKNOWN-ITEM",
                    "&cᴜɴᴋɴᴏᴡɴ ɪᴛᴇᴍ: {item}"
            ).replace("{item}", itemInput);
            sendMessage(sender, msg);
            return true;
        }

        ItemStack item = new ItemStack(material, amount);
        sendWorthMessage(sender, item, amount);
        return true;
    }

    private void sendWorthMessage(CommandSender sender, ItemStack item, int displayAmount) {
        WorthResult worthResult = plugin.getWorthManager().resolveWorth(item);
        if (!worthResult.sellable()) {
            sendMessage(sender, plugin.getConfigManager().getMessage("WORTH.NO-SELLABLE"));
            return;
        }

        String name = plugin.getWorthManager().prettifyMaterial(item.getType());
        String msg = displayAmount == 1
                ? plugin.getConfigManager().getMessage("WORTH.DEFAULT",
                    "{item}", name,
                    "{price}", NumberUtils.format(worthResult.totalWorth()),
                    "{price_formatted}", plugin.getCurrencyManager().formatMoney(worthResult.totalWorth()))
                : plugin.getConfigManager().getMessage("WORTH.HAND-ITEM",
                    "{amount}", String.valueOf(displayAmount),
                    "{item}", name,
                    "{total}", NumberUtils.format(worthResult.totalWorth()),
                    "{total_formatted}", plugin.getCurrencyManager().formatMoney(worthResult.totalWorth()));
        sendMessage(sender, msg);

        if (worthResult.container() && worthResult.hasContainerContentsWorth()) {
            String breakdown = plugin.getConfigManager().getMessages().getString(
                    "WORTH.CONTAINER-BREAKDOWN",
                    "&7base: &f{base_formatted} &8| &7contents: &f{contents_formatted}"
            );
            breakdown = breakdown
                    .replace("{base}", plugin.getCurrencyManager().formatCompactAmount(CurrencyManager.CurrencyType.MONEY, worthResult.baseWorth()))
                    .replace("{base_formatted}", plugin.getCurrencyManager().formatMoney(worthResult.baseWorth()))
                    .replace("{contents}", plugin.getCurrencyManager().formatCompactAmount(CurrencyManager.CurrencyType.MONEY, worthResult.containerContentsWorth()))
                    .replace("{contents_formatted}", plugin.getCurrencyManager().formatMoney(worthResult.containerContentsWorth()));
            sendMessage(sender, breakdown);
        }
    }

    private void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            PlayerSettingUtils.sendActionBar(plugin, player, message);
        } else {
            sender.sendMessage(ColorUtils.toComponent(message));
        }
    }

    private void openBrowser(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return;
        }

        new WorthMenu(plugin, 1).open(player);
    }
}
