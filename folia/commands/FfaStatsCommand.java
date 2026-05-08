package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FfaStatsCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public FfaStatsCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        UUID targetUuid;
        String targetName;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Player only.");
                return true;
            }
            targetUuid = player.getUniqueId();
            targetName = player.getName();
        } else {
            Player online = Bukkit.getPlayerExact(args[0]);
            if (online != null) {
                targetUuid = online.getUniqueId();
                targetName = online.getName();
            } else {
                targetUuid = plugin.getDatabaseManager().findPlayerUuidByUsername(args[0]);
                if (targetUuid == null) {
                    sender.sendMessage(ColorUtils.toComponent("&cThat player was not found."));
                    return true;
                }
                OfflinePlayer offline = Bukkit.getOfflinePlayer(targetUuid);
                String lastKnown = plugin.getDatabaseManager().getLastKnownUsername(targetUuid);
                targetName = lastKnown == null || lastKnown.isBlank()
                        ? (offline.getName() == null ? args[0] : offline.getName())
                        : lastKnown;
            }
        }

        sender.sendMessage(ColorUtils.toComponent("&6FFA Info &7for &f" + targetName));
        sender.sendMessage(ColorUtils.toComponent("&7FFA saat ini tidak memakai sistem victory, defeat, draw, atau streak."));
        return true;
    }
}
