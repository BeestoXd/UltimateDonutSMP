package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CurrencyManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public BalanceCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ."); return true; }

        if (args.length == 0) {
            double balance = plugin.getEconomyManager().getBalance(player);
            String msg = plugin.getConfigManager().getMessage("BALANCE.YOUR-BALANCE",
                    "{amount}", compactAmount(balance),
                    "{amount_full}", fullAmount(balance),
                    "{money}", plugin.getCurrencyManager().formatMoneyCompact(balance),
                    "{money_full}", fullMoney(balance));
            player.sendMessage(ColorUtils.toComponent(msg));
        } else {
            var account = plugin.getEconomyManager().resolveAccount(args[0]);
            if (account == null) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.ADMIN.PLAYER-NOT-FOUND")));
                return true;
            }
            double balance = plugin.getEconomyManager().getBalance(account.uuid());
            String msg = plugin.getConfigManager().getMessage("BALANCE.OTHER-BALANCE",
                    "{player}", account.displayName(),
                    "{amount}", compactAmount(balance),
                    "{amount_full}", fullAmount(balance),
                    "{money}", plugin.getCurrencyManager().formatMoneyCompact(balance),
                    "{money_full}", fullMoney(balance));
            player.sendMessage(ColorUtils.toComponent(msg));
        }
        return true;
    }

    private String compactAmount(double amount) {
        return plugin.getCurrencyManager().formatCompactAmount(CurrencyManager.CurrencyType.MONEY, amount);
    }

    private String fullAmount(double amount) {
        return plugin.getCurrencyManager().formatAmount(CurrencyManager.CurrencyType.MONEY, amount);
    }

    private String fullMoney(double amount) {
        return plugin.getCurrencyManager().format(CurrencyManager.CurrencyType.MONEY, amount, false);
    }
}
