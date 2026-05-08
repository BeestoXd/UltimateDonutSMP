package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ChatManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import net.kyori.adventure.text.Component;
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
            send(sender, message("DISABLED", "&cChat command is currently disabled."));
            return true;
        }

        if (!hasAccess(sender, BASE_PERMISSION, MUTE_PERMISSION, UNMUTE_PERMISSION, DELAY_PERMISSION, CLEAR_PERMISSION)) {
            send(sender, message("NO-PERMISSION", "&cYou do not have permission."));
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
            send(sender, message("NO-PERMISSION", "&cYou do not have permission."));
            return true;
        }

        plugin.getChatManager().setGlobalChatMuted(true, true);
        broadcast(message("MUTED", "&aGlobal chat is now muted."), sender);
        return true;
    }

    private boolean handleUnmute(CommandSender sender) {
        if (!hasAccess(sender, UNMUTE_PERMISSION)) {
            send(sender, message("NO-PERMISSION", "&cYou do not have permission."));
            return true;
        }

        plugin.getChatManager().setGlobalChatMuted(false, true);
        broadcast(message("UNMUTED", "&aGlobal chat is now unmuted."), sender);
        return true;
    }

    private boolean handleDelay(CommandSender sender, String[] args, String label) {
        if (!hasAccess(sender, DELAY_PERMISSION)) {
            send(sender, message("NO-PERMISSION", "&cYou do not have permission."));
            return true;
        }

        if (args.length < 2) {
            send(sender, "&cUsage: /" + label + " delay <seconds|off>");
            return true;
        }

        int delaySeconds;
        if (args[1].equalsIgnoreCase("off")) {
            delaySeconds = 0;
        } else {
            try {
                delaySeconds = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                send(sender, message("INVALID-DELAY", "&cInvalid delay."));
                return true;
            }
        }

        int maxDelay = plugin.getChatManager().getMaxDelaySeconds();
        if (delaySeconds < 0 || delaySeconds > maxDelay) {
            send(sender, message("INVALID-DELAY", "&cInvalid delay. Use a number between 0 and {max}.")
                    .replace("{max}", String.valueOf(maxDelay))
                    .replace("%max%", String.valueOf(maxDelay)));
            return true;
        }

        boolean enabled = delaySeconds > 0;
        plugin.getChatManager().setGlobalDelay(delaySeconds, enabled, true);

        String status = enabled
                ? message("STATUS-ENABLED", "enabled")
                : message("STATUS-DISABLED", "disabled");
        String response = message("DELAY", "&7Chat is now delayed &a%delay% &7seconds and delay is &a%status%")
                .replace("%delay%", String.valueOf(delaySeconds))
                .replace("%status%", status)
                .replace("{delay}", String.valueOf(delaySeconds))
                .replace("{status}", status);
        broadcast(response, sender);
        return true;
    }

    private boolean handleClear(CommandSender sender) {
        if (!hasAccess(sender, CLEAR_PERMISSION)) {
            send(sender, message("NO-PERMISSION", "&cYou do not have permission."));
            return true;
        }

        plugin.getChatManager().clearChatForAllPlayers();
        broadcast(message("CLEARED", "&aGlobal chat is cleared."), sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        List<String> lines = plugin.getConfigManager().getMessages().getStringList("CHAT-MANAGER.HELP");
        if (lines.isEmpty()) {
            lines = List.of(
                    "",
                    "&b&lChat Manager &7(Commands)",
                    "",
                    "&f/chat mute &7- To mute global chat.",
                    "&f/chat unmute &7- To unmute global chat.",
                    "&f/chat delay (time) &7- To add delay to global chat.",
                    "&f/chat clear &7- To clear global chat.",
                    ""
            );
        }

        for (String line : lines) {
            send(sender, line);
        }
    }

    private void broadcast(String message, CommandSender sender) {
        Component component = ColorUtils.toComponent(message);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(component));

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
        if (player.hasPermission(BASE_PERMISSION)) {
            return true;
        }

        for (String permission : permissions) {
            if (player.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    private String message(String key, String fallback) {
        return plugin.getConfigManager().getMessages().getString("CHAT-MANAGER." + key, fallback);
    }
}
