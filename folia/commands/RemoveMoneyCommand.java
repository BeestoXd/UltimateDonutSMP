package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveMoneyCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.admin.removemoney";

    private final UltimateDonutSmp plugin;

    public RemoveMoneyCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cNo permission."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /removemoney <player> <amount>"));
            return true;
        }

        double amount;
        try {
            amount = NumberUtils.parse(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.ADMIN.INVALID-AMOUNT")));
            return true;
        }

        if (amount <= 0D) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.ADMIN.MUST-BE-POSITIVE")));
            return true;
        }

        var account = plugin.getEconomyManager().resolveAccount(args[0]);
        if (account == null) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.ADMIN.PLAYER-NOT-FOUND")));
            return true;
        }

        var result = plugin.getEconomyManager().withdraw(account, amount, EconomyReason.ADMIN_REMOVE);
        if (!result.success()) {
            String key = result.insufficientFunds()
                    ? "BALANCE.ADMIN.TARGET-NOT-ENOUGH-MONEY"
                    : "BALANCE.ADMIN.PLAYER-NOT-FOUND";
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                    key,
                    "{player}", result.displayName(),
                    "{balance}", NumberUtils.format(result.beforeBalance())
            )));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                "BALANCE.ADMIN.REMOVE-MONEY-SUCCESS",
                "{player}", result.displayName(),
                "{amount}", NumberUtils.format(result.amount()),
                "{balance}", NumberUtils.format(result.afterBalance())
        )));

        Player targetPlayer = Bukkit.getPlayer(result.targetUuid());
        if (targetPlayer != null && !targetPlayer.equals(sender)) {
            targetPlayer.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                    "BALANCE.ADMIN.REMOVE-MONEY-RECEIVED",
                    "{admin}", sender.getName(),
                    "{amount}", NumberUtils.format(result.amount()),
                    "{balance}", NumberUtils.format(result.afterBalance())
            )));
        }

        return true;
    }
}
