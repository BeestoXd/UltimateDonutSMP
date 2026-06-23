package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EcseeCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public EcseeCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage(plugin.getEnderChestManager().getMessage(
                    "ECSEE-PLAYER-ONLY",
                    "ᴘʟᴀʏᴇʀ ᴏɴʟʏ."
            ));
            return true;
        }

        if (!plugin.getEnderChestManager().isInspectionEnabled()) {
            viewer.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().getMessage(
                            "ECSEE-DISABLED",
                            "&cᴛʜᴇ /ecsee ᴄᴏᴍᴍᴀɴᴅ ɪѕ ᴅɪѕᴀʙʟᴇᴅ."
                    )
            ));
            return true;
        }

        if (!plugin.getEnderChestManager().canInspect(viewer)) {
            viewer.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().getMessage(
                            "ECSEE-NO-PERMISSION",
                            "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."
                    )
            ));
            return true;
        }

        if (args.length != 1) {
            viewer.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().getMessage(
                            "ECSEE-USAGE",
                            "&cᴜѕᴀɢᴇ: /ecsee <player>"
                    )
            ));
            return true;
        }

        String input = args[0].trim();
        Player onlineTarget = findOnlineTarget(input);
        UUID targetUuid = onlineTarget == null
                ? plugin.getDatabaseManager().findPlayerUuidByUsername(input)
                : onlineTarget.getUniqueId();
        if (targetUuid == null) {
            viewer.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().formatMessage(
                            "ECSEE-PLAYER-NOT-FOUND",
                            "&cᴘʟᴀʏᴇʀ ɴᴏᴛ ꜰᴏᴜɴᴅ.",
                            "{player}", input,
                            "{target}", input
                    )
            ));
            return true;
        }

        String targetName = onlineTarget == null
                ? plugin.getDatabaseManager().getLastKnownUsername(targetUuid)
                : onlineTarget.getName();
        if (targetName == null || targetName.isBlank()) {
            targetName = input;
        }

        plugin.getEnderChestManager().openInspection(viewer, targetUuid, targetName);
        return true;
    }

    private Player findOnlineTarget(String username) {
        Player exact = Bukkit.getPlayerExact(username);
        if (exact != null) {
            return exact;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }
}
