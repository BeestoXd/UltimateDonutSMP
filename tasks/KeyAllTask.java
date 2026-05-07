package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

/**
 * Checks every second if it's time for key-all.
 */
public class KeyAllTask implements Runnable {

    private final UltimateDonutSmp plugin;

    public KeyAllTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getKeyAllManager().isEnabled()) return;
        plugin.getKeyAllManager().tickOnlinePlayers();
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getPaperScheduler().runGlobalTimer(new KeyAllTask(plugin), 20L, 20L);
    }
}
