package com.bx.ultimateDonutSmp.utils;

import org.bukkit.entity.Player;

public final class TitleUtils {

    private TitleUtils() {
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) {
            return;
        }
        player.sendTitle(ColorUtils.colorize(title), ColorUtils.colorize(subtitle), fadeIn, stay, fadeOut);
    }

    public static void clearTitle(Player player) {
        if (player != null) {
            player.resetTitle();
        }
    }
}
