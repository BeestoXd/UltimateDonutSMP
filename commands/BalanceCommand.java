package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
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
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only."); return true; }

        if (args.length == 0) {
            String msg = plugin.getConfigManager().getMessage("BALANCE.YOUR-BALANCE",
                    "{amount}", NumberUtils.format(plugin.getEconomyManager().getBalance(player)));
            player.sendMessage(ColorUtils.toComponent(msg));
        } else {
            var account = plugin.getEconomyManager().resolveAccount(args[0]);
            if (account == null) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.ADMIN.PLAYER-NOT-FOUND")));
                return true;
            }
            String msg = plugin.getConfigManager().getMessage("BALANCE.OTHER-BALANCE",
                    "{player}", account.displayName(),
                    "{amount}", NumberUtils.format(plugin.getEconomyManager().getBalance(account.uuid())));
            player.sendMessage(ColorUtils.toComponent(msg));
        }
        return true;
    }
}
