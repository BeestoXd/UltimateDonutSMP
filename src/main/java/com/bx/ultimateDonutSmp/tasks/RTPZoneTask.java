package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.entity.Player;

/**
 * The once-a-second sweep behind both RTP cuboids: the countdown zone that teleports whoever
 * stands still in it, and the matchmaking cuboid that puts whoever walks in on the RTP queue.
 * They share one pass over the player list rather than running a timer each.
 */
public class RTPZoneTask implements Runnable {

    private final UltimateDonutSmp plugin;

    public RTPZoneTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        boolean countdownZone = plugin.getRtpZoneManager() != null && plugin.getRtpZoneManager().isEnabled();
        boolean queueZone = plugin.getRtpQueueManager() != null && plugin.getRtpQueueManager().isZoneActive();
        if (!countdownZone && !queueZone) {
            return;
        }

        plugin.getSpigotScheduler().forEachOnlinePlayer((Player player) -> {
            if (countdownZone) {
                plugin.getRtpZoneManager().tick(player);
            }
            if (queueZone) {
                plugin.getRtpQueueManager().tickZone(player);
            }
        });
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getSpigotScheduler().runGlobalTimer(new RTPZoneTask(plugin), 20L, 20L);
    }
}
