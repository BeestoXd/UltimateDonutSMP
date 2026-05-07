package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

public class DuelMatchTask implements Runnable {

    private final UltimateDonutSmp plugin;

    private DuelMatchTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getPaperScheduler().runGlobalTimer(new DuelMatchTask(plugin), 1L, 1L);
    }

    @Override
    public void run() {
        if (plugin.getDuelManager() != null && plugin.getDuelManager().isEnabled()) {
            plugin.getDuelManager().tick();
        }
    }
}
