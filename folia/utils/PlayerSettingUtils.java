package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class PlayerSettingUtils {

    private PlayerSettingUtils() {
    }

    public static boolean hotbarMessagesEnabled(UltimateDonutSmp plugin, Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        return data == null || data.isHotbarMessagesEnabled();
    }

    public static void sendActionBar(UltimateDonutSmp plugin, Player player, String text) {
        if (!hotbarMessagesEnabled(plugin, player)) return;
        player.sendActionBar(ColorUtils.toComponent(text, player));
    }

    public static void sendActionBar(UltimateDonutSmp plugin, Player player, Component component) {
        if (!hotbarMessagesEnabled(plugin, player)) return;
        player.sendActionBar(component);
    }

    public static void clearActionBar(Player player) {
        player.sendActionBar(Component.empty());
    }
}
