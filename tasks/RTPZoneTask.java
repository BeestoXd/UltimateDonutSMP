package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.entity.Player;

public class RTPZoneTask implements Runnable {

    private final UltimateDonutSmp plugin;

    public RTPZoneTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getPaperScheduler().forEachOnlinePlayer((Player player) -> plugin.getRtpZoneManager().tick(player));
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getPaperScheduler().runGlobalTimer(new RTPZoneTask(plugin), 20L, 20L);
    }
}
