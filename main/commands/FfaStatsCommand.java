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
                sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
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
                    sender.sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ᴘʟᴀʏᴇʀ ᴡᴀѕ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
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

        sender.sendMessage(ColorUtils.toComponent("&6ꜰꜰᴀ ɪɴꜰᴏ &7ꜰᴏʀ &f" + targetName));
        sender.sendMessage(ColorUtils.toComponent("&7ꜰꜰᴀ ѕᴀᴀᴛ ɪɴɪ ᴛɪᴅᴀᴋ ᴍᴇᴍᴀᴋᴀɪ ѕɪѕᴛᴇᴍ ᴠɪᴄᴛᴏʀʏ, ᴅᴇꜰᴇᴀᴛ, ᴅʀᴀᴡ, ᴀᴛᴀᴜ ѕᴛʀᴇᴀᴋ."));
        return true;
    }
}
