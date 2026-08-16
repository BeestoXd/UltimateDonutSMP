package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

public class RTPCacheTask implements Runnable {

    private final UltimateDonutSmp plugin;

    public RTPCacheTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getRtpManager() == null || !plugin.getRtpManager().isEnabled()) {
            return;
        }
        plugin.getRtpManager().refillPreCacheAllWorlds();
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getSpigotScheduler().runGlobalTimer(new RTPCacheTask(plugin), 100L, 20L);
    }
}
