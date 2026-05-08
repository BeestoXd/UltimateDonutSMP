package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClearLagCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public ClearLagCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("ultimatedonutsmp.admin.clearlag")) {
            sender.sendMessage(ColorUtils.toComponent("&cNo permission."));
            return true;
        }
        int cleared = plugin.getClearLagManager().clearEntities();
        plugin.getClearLagManager().broadcastSuccess(cleared);
        return true;
    }
}
