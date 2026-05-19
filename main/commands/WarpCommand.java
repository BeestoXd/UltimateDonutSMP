package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WarpCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public WarpCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendWarpList(sender);
            return true;
        }

        if (args.length > 1) {
            sendMessage(sender, message("WARP.USAGE", "&cᴜѕᴀɢᴇ: /warp [name]"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sendMessage(sender, message("WARP.PLAYER-ONLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴡᴀʀᴘ ᴄᴏᴍᴍᴀɴᴅ."));
            return true;
        }

        String requestedName = args[0];
        Location warp = plugin.getWarpManager().getWarp(requestedName);
        if (warp == null) {
            sendWarpNotFound(sender, requestedName);
            return true;
        }

        plugin.getTeleportManager().queue(player, warp, "WARP", null);
        return true;
    }

    private void sendWarpList(CommandSender sender) {
        List<String> names = plugin.getWarpManager().getSortedWarpNames();
        if (names.isEmpty()) {
            sendMessage(sender, message("WARP.LIST-EMPTY", "&cɴᴏ ᴡᴀʀᴘѕ ᴀᴠᴀɪʟᴀʙʟᴇ."));
            return;
        }

        sendMessage(sender, message("WARP.LIST-HEADER",
                "&8&m---------------- &bᴡᴀʀᴘѕ &7({count}) &8&m----------------",
                "{count}", String.valueOf(names.size())));
        for (String name : names) {
            sendMessage(sender, message("WARP.LIST-ENTRY", "&7- &b{name}", "{name}", name));
        }
    }

    private void sendWarpNotFound(CommandSender sender, String requestedName) {
        sendMessage(sender, message("WARP.NOT-FOUND", "&cᴡᴀʀᴘ '&e{name}&c' ɴᴏᴛ ꜰᴏᴜɴᴅ.", "{name}", requestedName));

        List<String> suggestions = plugin.getWarpManager().findWarpSuggestions(requestedName);
        if (!suggestions.isEmpty()) {
            sendMessage(sender, message("WARP.NOT-FOUND-SUGGESTION",
                    "&7ᴅɪᴅ ʏᴏᴜ ᴍᴇᴀɴ: &b{suggestions}",
                    "{suggestions}", String.join("&7, &b", suggestions)));
        }
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.colorize(message));
    }

    private String message(String path, String fallback, String... placeholders) {
        return plugin.getConfigManager().getMessageOrDefault(path, fallback, placeholders);
    }
}
