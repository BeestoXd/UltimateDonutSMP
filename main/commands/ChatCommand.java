package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class ChatCommand implements CommandExecutor {

    private static final String BASE_PERMISSION = "ultimatedonutsmp.staff.chat.use";
    private static final String MUTE_PERMISSION = "ultimatedonutsmp.staff.chat.mute";
    private static final String UNMUTE_PERMISSION = "ultimatedonutsmp.staff.chat.unmute";
    private static final String DELAY_PERMISSION = "ultimatedonutsmp.staff.chat.delay";
    private static final String CLEAR_PERMISSION = "ultimatedonutsmp.staff.chat.clear";

    private final UltimateDonutSmp plugin;

    public ChatCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("CHAT")) {
            send(sender, message("DISABLED", "&cᴄʜᴀᴛ ᴄᴏᴍᴍᴀɴᴅ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."));
            return true;
        }

        if (!hasAccess(sender, BASE_PERMISSION, MUTE_PERMISSION, UNMUTE_PERMISSION, DELAY_PERMISSION, CLEAR_PERMISSION)) {
            send(sender, message("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "mute" -> handleMute(sender);
            case "unmute" -> handleUnmute(sender);
            case "delay" -> handleDelay(sender, args, label);
            case "clear" -> handleClear(sender);
            default -> {
                sendHelp(sender);
                yield true;
            }
        };
    }

    private boolean handleMute(CommandSender sender) {
        if (!hasAccess(sender, MUTE_PERMISSION)) {
            send(sender, message("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        plugin.getChatManager().setGlobalChatMuted(true, true);
        broadcast(message("MUTED", "&aɢʟᴏʙᴀʟ ᴄʜᴀᴛ ɪѕ ɴᴏᴡ ᴍᴜᴛᴇᴅ."), sender);
        return true;
    }

    private boolean handleUnmute(CommandSender sender) {
        if (!hasAccess(sender, UNMUTE_PERMISSION)) {
            send(sender, message("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        plugin.getChatManager().setGlobalChatMuted(false, true);
        broadcast(message("UNMUTED", "&aɢʟᴏʙᴀʟ ᴄʜᴀᴛ ɪѕ ɴᴏᴡ ᴜɴᴍᴜᴛᴇᴅ."), sender);
        return true;
    }

    private boolean handleDelay(CommandSender sender, String[] args, String label) {
        if (!hasAccess(sender, DELAY_PERMISSION)) {
            send(sender, message("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        if (args.length < 2) {
            send(sender, "&cᴜѕᴀɢᴇ: /" + label + " delay <seconds|off>");
            return true;
        }

        int delaySeconds;
        if (args[1].equalsIgnoreCase("off")) {
            delaySeconds = 0;
        } else {
            try {
                delaySeconds = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                send(sender, message("INVALID-DELAY", "&cɪɴᴠᴀʟɪᴅ ᴅᴇʟᴀʏ."));
                return true;
            }
        }

        int maxDelay = plugin.getChatManager().getMaxDelaySeconds();
        if (delaySeconds < 0 || delaySeconds > maxDelay) {
            send(sender, message("INVALID-DELAY", "&cɪɴᴠᴀʟɪᴅ ᴅᴇʟᴀʏ. ᴜѕᴇ ᴀ ɴᴜᴍʙᴇʀ ʙᴇᴛᴡᴇᴇɴ 0 ᴀɴᴅ {max}.")
                    .replace("{max}", String.valueOf(maxDelay))
                    .replace("%max%", String.valueOf(maxDelay)));
            return true;
        }

        boolean enabled = delaySeconds > 0;
        plugin.getChatManager().setGlobalDelay(delaySeconds, enabled, true);

        String status = enabled
                ? message("STATUS-ENABLED", "ᴇɴᴀʙʟᴇᴅ")
                : message("STATUS-DISABLED", "ᴅɪѕᴀʙʟᴇᴅ");
        String response = message("DELAY", "&7ᴄʜᴀᴛ ɪѕ ɴᴏᴡ ᴅᴇʟᴀʏᴇᴅ &a%delay% &7ѕᴇᴄᴏɴᴅѕ ᴀɴᴅ ᴅᴇʟᴀʏ ɪѕ &a%status%")
                .replace("%delay%", String.valueOf(delaySeconds))
                .replace("%status%", status)
                .replace("{delay}", String.valueOf(delaySeconds))
                .replace("{status}", status);
        broadcast(response, sender);
        return true;
    }

    private boolean handleClear(CommandSender sender) {
        if (!hasAccess(sender, CLEAR_PERMISSION)) {
            send(sender, message("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        plugin.getChatManager().clearChatForAllPlayers();
        broadcast(message("CLEARED", "&aɢʟᴏʙᴀʟ ᴄʜᴀᴛ ɪѕ ᴄʟᴇᴀʀᴇᴅ."), sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        List<String> lines = plugin.getConfigManager().getMessages().getStringList("CHAT-MANAGER.HELP");
        if (lines.isEmpty()) {
            lines = List.of(
                    "",
                    "&b&lᴄʜᴀᴛ ᴍᴀɴᴀɢᴇʀ &7(Commands)",
                    "",
                    "&f/chat mute &7- ᴛᴏ ᴍᴜᴛᴇ ɢʟᴏʙᴀʟ ᴄʜᴀᴛ.",
                    "&f/chat unmute &7- ᴛᴏ ᴜɴᴍᴜᴛᴇ ɢʟᴏʙᴀʟ ᴄʜᴀᴛ.",
                    "&f/chat delay (time) &7- ᴛᴏ ᴀᴅᴅ ᴅᴇʟᴀʏ ᴛᴏ ɢʟᴏʙᴀʟ ᴄʜᴀᴛ.",
                    "&f/chat clear &7- ᴛᴏ ᴄʟᴇᴀʀ ɢʟᴏʙᴀʟ ᴄʜᴀᴛ.",
                    ""
            );
        }

        for (String line : lines) {
            send(sender, line);
        }
    }

    private void broadcast(String message, CommandSender sender) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(ColorUtils.toComponent(message, player)));

        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize(message));
        }
    }

    private void send(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            player.sendMessage(ColorUtils.toComponent(message, player));
            return;
        }
        sender.sendMessage(ColorUtils.colorize(message));
    }

    private boolean hasAccess(CommandSender sender, String... permissions) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        if (PermissionUtils.has(player, BASE_PERMISSION)) {
            return true;
        }

        for (String permission : permissions) {
            if (PermissionUtils.has(player, permission)) {
                return true;
            }
        }
        return false;
    }

    private String message(String key, String fallback) {
        return plugin.getConfigManager().getMessages().getString("CHAT-MANAGER." + key, fallback);
    }
}
