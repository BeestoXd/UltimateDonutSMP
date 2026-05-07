package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddMoneyCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.admin.addmoney";

    private final UltimateDonutSmp plugin;

    public AddMoneyCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cNo permission."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /addmoney <player> <amount>"));
            return true;
        }

        double amount;
        try {
            amount = NumberUtils.parse(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.ADMIN.INVALID-AMOUNT")));
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.ADMIN.MUST-BE-POSITIVE")));
            return true;
        }

        var account = plugin.getEconomyManager().resolveAccount(args[0]);
        if (account == null) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.ADMIN.PLAYER-NOT-FOUND")));
            return true;
        }

        var result = plugin.getEconomyManager().deposit(account, amount, com.bx.ultimateDonutSmp.models.EconomyReason.ADMIN_ADD);
        if (!result.success()) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.ADMIN.PLAYER-NOT-FOUND")));
            return true;
        }

        String success = plugin.getConfigManager().getMessage("BALANCE.ADMIN.ADD-MONEY-SUCCESS",
                "{player}", result.displayName(),
                "{amount}", NumberUtils.format(result.amount()),
                "{balance}", NumberUtils.format(result.afterBalance()));
        sender.sendMessage(ColorUtils.toComponent(success));

        Player targetPlayer = result.targetUuid() != null ? org.bukkit.Bukkit.getPlayer(result.targetUuid()) : null;
        if (targetPlayer != null && !targetPlayer.equals(sender)) {
            String received = plugin.getConfigManager().getMessage("BALANCE.ADMIN.ADD-MONEY-RECEIVED",
                    "{admin}", sender.getName(),
                    "{amount}", NumberUtils.format(result.amount()),
                    "{balance}", NumberUtils.format(result.afterBalance()));
            targetPlayer.sendMessage(ColorUtils.toComponent(received));
        }

        return true;
    }
}
