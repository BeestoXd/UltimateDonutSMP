package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TitleUtils {

    public static final int DEFAULT_FADE_IN = 10;
    public static final int DEFAULT_STAY = 70;
    public static final int DEFAULT_FADE_OUT = 20;

    private static final Map<UUID, BukkitTask> PENDING_RESETS = new ConcurrentHashMap<>();

    private TitleUtils() {
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, DEFAULT_FADE_IN, DEFAULT_STAY, DEFAULT_FADE_OUT);
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null || !player.isOnline()) {
            return;
        }

        cancelPendingReset(player.getUniqueId());

        String colorTitle = ColorUtils.colorize(title);
        String colorSubtitle = ColorUtils.colorize(subtitle);

        player.sendTitle(colorTitle, colorSubtitle, fadeIn, stay, fadeOut);

        if (fadeIn != DEFAULT_FADE_IN || stay != DEFAULT_STAY || fadeOut != DEFAULT_FADE_OUT) {
            int totalTicks = Math.max(1, fadeIn + stay + fadeOut);
            scheduleDefaultTimesReset(player, totalTicks);
        }
    }

    public static void resetTitleTimes(Player player) {
        if (player != null && player.isOnline()) {
            player.sendTitle(null, null, DEFAULT_FADE_IN, DEFAULT_STAY, DEFAULT_FADE_OUT);
        }
    }

    public static void clearTitle(Player player) {
        if (player != null) {
            cancelPendingReset(player.getUniqueId());
            player.resetTitle();
        }
    }

    public static void cancelPendingReset(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        BukkitTask task = PENDING_RESETS.remove(playerUuid);
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception ignored) {
            }
        }
    }

    private static void scheduleDefaultTimesReset(Player player, int delayTicks) {
        UltimateDonutSmp plugin = UltimateDonutSmp.getInstance();
        if (plugin == null || plugin.getSpigotScheduler() == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        BukkitTask task = plugin.getSpigotScheduler().runEntityLater(player, () -> {
            PENDING_RESETS.remove(uuid);
            resetTitleTimes(player);
        }, delayTicks);

        if (task != null) {
            PENDING_RESETS.put(uuid, task);
        }
    }
}

