package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrivateMessageToggleCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.message.toggle";

    private final UltimateDonutSmp plugin;

    public PrivateMessageToggleCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("MESSAGE")) {
            send(sender, message("MESSAGES.DISABLED", "PRIVATE-MESSAGE.DISABLED",
                    "&cPrivate messages are currently disabled."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            send(sender, message("MESSAGES.PLAYER_ONLY", null, "&cOnly players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            send(player, message("MESSAGES.NO_PERMISSION", "PRIVATE-MESSAGE.NO-PERMISSION",
                    "&cYou do not have permission."));
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            data = plugin.getPlayerDataManager().loadOrCreate(player);
        }

        boolean enabled = !data.isPrivateMessagesEnabled();
        data.setPrivateMessagesEnabled(enabled);
        plugin.getDatabaseManager().savePlayer(data);

        send(player, message(
                enabled ? "PRIVATE_MESSAGES.PM_ENABLED" : "PRIVATE_MESSAGES.PM_DISABLED",
                enabled ? "PRIVATE-MESSAGE.PM-ENABLED" : "PRIVATE-MESSAGE.PM-DISABLED",
                enabled ? "&aPrivate messages are now enabled" : "&cPrivate messages are now disabled"
        ));
        return true;
    }

    private void send(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            player.sendMessage(ColorUtils.toComponent(message, player));
            return;
        }
        sender.sendMessage(ColorUtils.colorize(message));
    }

    private String message(String path, String fallbackPath, String fallback) {
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
