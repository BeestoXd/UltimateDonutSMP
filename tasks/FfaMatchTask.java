package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

public class FfaMatchTask implements Runnable {

    private final UltimateDonutSmp plugin;

    private FfaMatchTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getPaperScheduler().runGlobalTimer(new FfaMatchTask(plugin), 1L, 1L);
    }

    @Override
    public void run() {
        if (plugin.getFfaManager() != null && plugin.getFfaManager().isEnabled()) {
            plugin.getFfaManager().tick();
        }
    }
}
