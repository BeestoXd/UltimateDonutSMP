package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Locale;

public class MessageCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.message";

    private final UltimateDonutSmp plugin;

    public MessageCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("MESSAGE")) {
            send(sender, message("DISABLED", "&cᴘʀɪᴠᴀᴛᴇ ᴍᴇѕѕᴀɢᴇѕ ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."));
            return true;
        }

        if (sender instanceof Player player && !PermissionUtils.has(player, PERMISSION)) {
            send(player, message("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        if (label.equalsIgnoreCase("reply") || label.equalsIgnoreCase("r")) {
            return handleReply(sender, args, label);
        }

        if (args.length < 2) {
            send(sender, message("USAGE", "&cᴜѕᴀɢᴇ: /msg <player> <message>"));
            return true;
        }

        Player target = plugin.getHideManager().findOnlinePlayer(sender, args[0]);
        if (target == null) {
            send(sender, message("PLAYER-NOT-ONLINE", "&cᴘʟᴀʏᴇʀ ɴᴏᴛ ᴏɴʟɪɴᴇ."));
            return true;
        }

        String privateMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.getPrivateMessageManager().sendPrivateMessage(sender, target, privateMessage);
        return true;
    }

    private boolean handleReply(CommandSender sender, String[] args, String label) {
        if (!(sender instanceof Player player)) {
            send(sender, message("PLAYER-ONLY-REPLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ /" + label + "."));
            return true;
        }

        if (args.length == 0) {
            send(player, message("REPLY-USAGE", "&cᴜѕᴀɢᴇ: /reply <message>"));
            return true;
        }

        plugin.getPrivateMessageManager().reply(player, String.join(" ", args));
        return true;
    }

    private void send(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            player.sendMessage(ColorUtils.toComponent(message, player));
            return;
        }
        sender.sendMessage(ColorUtils.colorize(message));
    }

    private String message(String key, String fallback) {
        String configured = switch (key) {
            case "USAGE" -> configuredMessage("MESSAGES.USAGE", "PRIVATE-MESSAGE.USAGE");
            case "REPLY-USAGE" -> configuredMessage("MESSAGES.REPLY_USAGE", "PRIVATE-MESSAGE.REPLY-USAGE");
            case "PLAYER-ONLY-REPLY" -> configuredMessage("MESSAGES.PLAYER_ONLY_REPLY", "PRIVATE-MESSAGE.PLAYER-ONLY-REPLY");
            case "NO-PERMISSION" -> configuredMessage("MESSAGES.NO_PERMISSION", "PRIVATE-MESSAGE.NO-PERMISSION");
            case "DISABLED" -> configuredMessage("MESSAGES.DISABLED", "PRIVATE-MESSAGE.DISABLED");
            case "PLAYER-NOT-ONLINE" -> configuredMessage("MESSAGES.PLAYER_NOT_ONLINE", "PRIVATE-MESSAGE.PLAYER-NOT-ONLINE");
            default -> plugin.getConfigManager().getMessages().getString("PRIVATE-MESSAGE." + key);
        };
        return configured == null ? fallback : configured;
    }

    private String configuredMessage(String path, String fallbackPath) {
        String value = plugin.getConfigManager().getMessages().getString(path);
        if (value != null) {
            return value;
        }
        return plugin.getConfigManager().getMessages().getString(fallbackPath);
    }
}
