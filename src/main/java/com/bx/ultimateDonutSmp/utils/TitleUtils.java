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
    private static final ThreadLocal<Boolean> SENDING_OWN_TITLE = new ThreadLocal<>();

    /**
     * False until the outgoing title listener is registered. Without it a restore cannot tell our
     * packets from someone else's, and sending one anyway is what cuts their animation off.
     */
    private static volatile boolean watchingForeignTitles;

    private TitleUtils() {
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, DEFAULT_FADE_IN, DEFAULT_STAY, DEFAULT_FADE_OUT);
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        cancelPendingReset(uuid);

        String colorTitle = ColorUtils.colorize(title);
        String colorSubtitle = ColorUtils.colorize(subtitle);

        whileSendingOwnTitle(() -> player.sendTitle(colorTitle, colorSubtitle, fadeIn, stay, fadeOut));

        if (fadeIn != DEFAULT_FADE_IN || stay != DEFAULT_STAY || fadeOut != DEFAULT_FADE_OUT) {
            int totalTicks = Math.max(1, fadeIn + stay + fadeOut);
            // Own the timings only after this send returns. A title packet observed during the
            // send is ours; one that arrives later belongs to whoever sent it.
            int token = TitleTimingGuard.own(uuid);
            scheduleDefaultTimesReset(player, totalTicks, token);
        }
    }

    public static void resetTitleTimes(Player player) {
        if (player != null && player.isOnline()) {
            whileSendingOwnTitle(() ->
                    player.sendTitle(null, null, DEFAULT_FADE_IN, DEFAULT_STAY, DEFAULT_FADE_OUT));
        }
    }

    public static void clearTitle(Player player) {
        if (player != null) {
            cancelPendingReset(player.getUniqueId());
            whileSendingOwnTitle(player::resetTitle);
        }
    }

    public static void watchForeignTitles() {
        watchingForeignTitles = true;
    }

    /**
     * Another plugin sent a title, subtitle, times, or clear packet. Drop the pending restore so
     * it does not change the timings of the title they are showing.
     */
    public static void noteForeignTitlePacket(UUID playerUuid) {
        if (playerUuid == null || Boolean.TRUE.equals(SENDING_OWN_TITLE.get())) {
            return;
        }
        cancelPendingReset(playerUuid);
    }

    public static void cancelPendingReset(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        TitleTimingGuard.release(playerUuid);
        BukkitTask task = PENDING_RESETS.remove(playerUuid);
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception ignored) {
            }
        }
    }

    static void whileSendingOwnTitle(Runnable action) {
        SENDING_OWN_TITLE.set(Boolean.TRUE);
        try {
            action.run();
        } finally {
            SENDING_OWN_TITLE.remove();
        }
    }

    private static void scheduleDefaultTimesReset(Player player, int delayTicks, int token) {
        UltimateDonutSmp plugin = UltimateDonutSmp.getInstance();
        UUID uuid = player.getUniqueId();
        if (!watchingForeignTitles || plugin == null || plugin.getSpigotScheduler() == null) {
            TitleTimingGuard.release(uuid);
            return;
        }

        BukkitTask task = plugin.getSpigotScheduler().runEntityLater(player, () -> {
            PENDING_RESETS.remove(uuid);
            if (!TitleTimingGuard.mayRestore(uuid, token)) {
                return;
            }
            resetTitleTimes(player);
            TitleTimingGuard.release(uuid);
        }, delayTicks);

        if (task != null) {
            PENDING_RESETS.put(uuid, task);
        } else {
            TitleTimingGuard.release(uuid);
        }
    }
}
