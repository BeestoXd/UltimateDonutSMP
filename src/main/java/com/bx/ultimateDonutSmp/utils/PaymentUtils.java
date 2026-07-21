package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CurrencyManager;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.entity.Player;

public final class PaymentUtils {

    private PaymentUtils() {
    }

    public static boolean transferMoney(UltimateDonutSmp plugin, Player sender, String targetName, double amount) {
        if (targetName.equalsIgnoreCase(sender.getName())) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.PAY.CANT-PAY-SELF")));
            return false;
        }
        if (amount <= 0) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.PAY.MUST-BE-POSITIVE")));
            return false;
        }

        Player target = plugin.getHideManager().findOnlinePlayer(sender, targetName);
        if (target == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴘʟᴀʏᴇʀ ɴᴏᴛ ᴏɴʟɪɴᴇ."));
            return false;
        }

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.PAY.CANT-PAY-SELF")));
            return false;
        }
        PlayerData senderData = plugin.getPlayerDataManager().get(sender);
        PlayerData targetData = plugin.getPlayerDataManager().get(target);
        if (targetData == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴛᴀʀɢᴇᴛ ᴘʀᴏꜰɪʟᴇ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
            return false;
        }
        if (!targetData.isPaymentsEnabled()) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.PAY.TARGET-DISABLED-PAYMENTS")));
            return false;
        }
        if (plugin.getFriendsManager() != null && plugin.getFriendsManager().isPaymentBlocked(sender.getUniqueId(), target.getUniqueId())) {
            sender.sendMessage(ColorUtils.toComponent("&c" + target.getName() + " has disabled payments from you."));
            return false;
        }
        if (senderData == null) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.PAY.NOT-ENOUGH-MONEY")));
            return false;
        }
        if (!plugin.getEconomyManager().has(sender, amount)) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.PAY.NOT-ENOUGH-MONEY")));
            return false;
        }

        var transferResult = plugin.getEconomyManager().transfer(sender, target, amount, EconomyReason.PLAYER_PAY);
        if (!transferResult.success()) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.PAY.TRANSACTION-ERROR")));
            return false;
        }

        sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                "BALANCE.PAY.SUCCESS-SENDER",
                "{player}", plugin.getHideManager().publicName(target),
                "{amount}", compactMoneyAmount(plugin, amount),
                "{amount_full}", fullMoneyAmount(plugin, amount),
                "{money}", plugin.getCurrencyManager().formatMoneyCompact(amount),
                "{money_full}", fullMoney(plugin, amount))));
        if (targetData.isPayAlertsEnabled()) {
            target.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                    "BALANCE.PAY.SUCCESS-RECEIVER",
                    "{player}", plugin.getHideManager().publicName(sender),
                    "{amount}", compactMoneyAmount(plugin, amount),
                    "{amount_full}", fullMoneyAmount(plugin, amount),
                    "{money}", plugin.getCurrencyManager().formatMoneyCompact(amount),
                    "{money_full}", fullMoney(plugin, amount))));
        }
        return true;
    }

    public static boolean transferShards(UltimateDonutSmp plugin, Player sender, String targetName, long amount) {
        if (targetName.equalsIgnoreCase(sender.getName())) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("SHARD_PAY.CANT-PAY-SELF")));
            return false;
        }
        if (amount <= 0) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("SHARD_PAY.MUST-BE-POSITIVE")));
            return false;
        }

        Player target = plugin.getHideManager().findOnlinePlayer(sender, targetName);
        if (target == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴘʟᴀʏᴇʀ ɴᴏᴛ ᴏɴʟɪɴᴇ."));
            return false;
        }

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("SHARD_PAY.CANT-PAY-SELF")));
            return false;
        }
        PlayerData senderData = plugin.getPlayerDataManager().get(sender);
        PlayerData targetData = plugin.getPlayerDataManager().get(target);
        if (targetData == null) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("SHARD_PAY.TARGET-PROFILE-NOT-FOUND")));
            return false;
        }
        if (!targetData.isPaymentsEnabled()) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("SHARD_PAY.TARGET-DISABLED-PAYMENTS")));
            return false;
        }
        if (plugin.getFriendsManager() != null && plugin.getFriendsManager().isPaymentBlocked(sender.getUniqueId(), target.getUniqueId())) {
            sender.sendMessage(ColorUtils.toComponent("&c" + target.getName() + " has disabled payments from you."));
            return false;
        }
        if (senderData == null || !senderData.hasShards(amount)) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("SHARD_PAY.NOT-ENOUGH-SHARDS")));
            return false;
        }

        senderData.removeShards(amount);
        targetData.addShards(amount);

        sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                "SHARD_PAY.SUCCESS-SENDER",
                "{player}", plugin.getHideManager().publicName(target),
                "{amount}", String.valueOf(amount),
                "{shards}", plugin.getCurrencyManager().formatShards(amount))));
        if (targetData.isPayAlertsEnabled()) {
            target.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                    "SHARD_PAY.SUCCESS-RECEIVER",
                    "{player}", plugin.getHideManager().publicName(sender),
                    "{amount}", String.valueOf(amount),
                    "{shards}", plugin.getCurrencyManager().formatShards(amount))));
        }
        return true;
    }

    private static String compactMoneyAmount(UltimateDonutSmp plugin, double amount) {
        return plugin.getCurrencyManager().formatCompactAmount(CurrencyManager.CurrencyType.MONEY, amount);
    }

    private static String fullMoneyAmount(UltimateDonutSmp plugin, double amount) {
        return plugin.getCurrencyManager().formatAmount(CurrencyManager.CurrencyType.MONEY, amount);
    }

    private static String fullMoney(UltimateDonutSmp plugin, double amount) {
        return plugin.getCurrencyManager().format(CurrencyManager.CurrencyType.MONEY, amount, false);
    }
}
