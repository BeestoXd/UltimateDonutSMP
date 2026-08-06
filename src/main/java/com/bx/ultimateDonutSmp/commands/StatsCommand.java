package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.StatsMenu;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StatsCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public StatsCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " [player]"));
            return true;
        }

        UUID targetUuid;
        String targetName;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <player>"));
                return true;
            }
            targetUuid = player.getUniqueId();
            targetName = plugin.getHideManager().publicName(player);
        } else {
            Player online = plugin.getHideManager().findOnlinePlayer(sender, args[0]);
            if (online != null) {
                targetUuid = online.getUniqueId();
                targetName = plugin.getHideManager().publicName(online);
            } else {
                targetUuid = plugin.getHideManager().findKnownPlayerUuid(sender, args[0]);
                if (targetUuid == null) {
                    sender.sendMessage(ColorUtils.toComponent("&cPlayer not found."));
                    return true;
                }
                OfflinePlayer offline = Bukkit.getOfflinePlayer(targetUuid);
                String lastKnown = plugin.getDatabaseManager().getLastKnownUsername(targetUuid);
                String fallback = lastKnown == null || lastKnown.isBlank()
                        ? (offline.getName() == null ? args[0] : offline.getName())
                        : lastKnown;
                targetName = plugin.getHideManager().publicName(targetUuid, fallback);
            }
        }

        if (sender instanceof Player player) {
            new StatsMenu(plugin, targetUuid, targetName).open(player);
        } else {
            PlayerData data = plugin.getPlayerDataManager().get(targetUuid);
            if (data == null) data = plugin.getDatabaseManager().loadPlayer(targetUuid);
            if (data == null) {
                sender.sendMessage(ColorUtils.toComponent("&cPlayer not found."));
                return true;
            }
            sender.sendMessage(ColorUtils.toComponent("&7&m------------------"));
            sender.sendMessage(ColorUtils.toComponent("&b" + targetName + "&7's Stats:"));
            sender.sendMessage(ColorUtils.toComponent("Money: " + plugin.getCurrencyManager().formatMoneyCompact(data.getMoney())));
            sender.sendMessage(ColorUtils.toComponent("Shards: " + plugin.getCurrencyManager().formatShardsCompact(data.getShards())));
            sender.sendMessage(ColorUtils.toComponent("Kills: " + data.getKills()));
            sender.sendMessage(ColorUtils.toComponent("Deaths: " + data.getDeaths()));
            sender.sendMessage(ColorUtils.toComponent("&7&m------------------"));
        }

        return true;
    }
}
