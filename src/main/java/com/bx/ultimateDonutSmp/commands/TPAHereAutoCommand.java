package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPAHereAutoCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public TPAHereAutoCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only."); return true; }
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) return true;

        data.setAutoTpaHereEnabled(!data.isAutoTpaHereEnabled());
        if (data.isAutoTpaHereEnabled()) {
            plugin.getTPAManager().processQueuedAutoRequests(player.getUniqueId());
        }

        String msg = data.isAutoTpaHereEnabled()
                ? plugin.getConfigManager().getMessage("TPAHEREAUTO.ENABLED")
                : plugin.getConfigManager().getMessage("TPAHEREAUTO.DISABLED");
        player.sendMessage(ColorUtils.toComponent(msg));
        return true;
    }
}
