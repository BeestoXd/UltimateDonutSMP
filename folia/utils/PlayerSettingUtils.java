package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
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
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, ColorUtils.toBaseComponents(text, player));
    }

    public static void sendActionBar(UltimateDonutSmp plugin, Player player, BaseComponent... component) {
        if (!hotbarMessagesEnabled(plugin, player)) return;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    public static void clearActionBar(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, ColorUtils.toBaseComponents(""));
    }
}
