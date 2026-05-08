package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PrivateMessageManager {

    private static final String BYPASS_DISABLED_PERMISSION = "ultimatedonutsmp.message.bypass-disabled";

    private final UltimateDonutSmp plugin;
    private final Map<UUID, UUID> replyTargets = new ConcurrentHashMap<>();

    public PrivateMessageManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean sendPrivateMessage(CommandSender sender, Player target, String message) {
        if (sender == null || target == null || message == null || message.isBlank()) {
            return false;
        }

        String senderName = sender instanceof Player player ? player.getName() : "Console";
        if (sender instanceof Player player) {
            if (player.getUniqueId().equals(target.getUniqueId())) {
                send(sender, configuredMessage(
                        "MESSAGES.CANNOT_MESSAGE_SELF",
                        "PRIVATE-MESSAGE.CANNOT-MESSAGE-SELF",
                        "&cYou cannot message yourself!"
                ));
                return false;
            }

            if (plugin.getIgnoreManager().isIgnoring(target.getUniqueId(), player.getUniqueId())
                    && !plugin.getIgnoreManager().canBypassIgnore(sender)) {
                send(sender, applyPlaceholders(configuredMessage(
                        "MESSAGES.PLAYER_BLOCKED",
                        "IGNORE.MESSAGE-BLOCKED-SENDER",
                        "&c%player% has blocked you."
                ), target.getName(), message));
                return false;
            }
        }

        if (!targetPrivateMessagesEnabled(target) && !sender.hasPermission(BYPASS_DISABLED_PERMISSION)) {
            send(sender, applyPlaceholders(configuredMessage(
                    "MESSAGES.PMS_DISABLED",
                    null,
                    "&c%player% has private messages disabled."
            ), target.getName(), message));
            return false;
        }

        String sentFormat = configuredMessage(
                "MESSAGES.SENDER_FORMAT",
                "PRIVATE-MESSAGE.SENT",
                "&d(To &a%player%&d) %message%"
        );
        String receivedFormat = configuredMessage(
                "MESSAGES.RECEIVER_FORMAT",
                "PRIVATE-MESSAGE.RECEIVED",
                "&d(From &a%player%&d) %message%"
        );

        send(sender, applyPlaceholders(sentFormat, target.getName(), message));
        target.sendMessage(ColorUtils.toComponent(applyPlaceholders(receivedFormat, senderName, message), target));

        if (sender instanceof Player player) {
            replyTargets.put(player.getUniqueId(), target.getUniqueId());
            replyTargets.put(target.getUniqueId(), player.getUniqueId());
        }
        return true;
    }

    public boolean reply(Player sender, String message) {
        if (sender == null || message == null || message.isBlank()) {
            return false;
        }

        UUID targetUuid = replyTargets.get(sender.getUniqueId());
        if (targetUuid == null) {
            sendNoConversation(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            replyTargets.remove(sender.getUniqueId());
            sendNoConversation(sender);
            return true;
        }

        sendPrivateMessage(sender, target, message);
        return true;
    }

    public void clearPlayer(UUID uuid) {
        if (uuid == null) {
            return;
        }

        replyTargets.remove(uuid);
        replyTargets.entrySet().removeIf(entry -> uuid.equals(entry.getValue()));
    }

    public void clear() {
        replyTargets.clear();
    }

    private String applyPlaceholders(String format, String playerName, String message) {
        return format
                .replace("%player%", playerName)
                .replace("{player}", playerName)
                .replace("%message%", message)
                .replace("{message}", message);
    }

    private void send(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            player.sendMessage(ColorUtils.toComponent(message, player));
            return;
        }
        sender.sendMessage(ColorUtils.colorize(message));
    }

    private boolean targetPrivateMessagesEnabled(Player target) {
        PlayerData data = plugin.getPlayerDataManager().get(target);
        if (data == null) {
            data = plugin.getPlayerDataManager().loadOrCreate(target);
        }
        return data == null || data.isPrivateMessagesEnabled();
    }

    private void sendNoConversation(Player sender) {
        sender.sendMessage(ColorUtils.toComponent(configuredMessage(
                "MESSAGES.NO_CONVERSATION",
                "PRIVATE-MESSAGE.NO-REPLY-TARGET",
                "&cYou are currently not in conversation with anyone or the player is offline."
        ), sender));
    }

    private String configuredMessage(String path, String fallbackPath, String fallback) {
        String value = plugin.getConfigManager().getMessages().getString(path);
        if (value != null) {
            return value;
        }
        if (fallbackPath != null) {
            value = plugin.getConfigManager().getMessages().getString(fallbackPath);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }
}
