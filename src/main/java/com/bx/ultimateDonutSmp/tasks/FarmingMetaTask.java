package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

/**
 * Checks every 30 seconds whether the farming meta rotation timer has expired and, if so,
 * moves the meta on to the next item in the rotation.
 */
public class FarmingMetaTask implements Runnable {

    private final UltimateDonutSmp plugin;

    private FarmingMetaTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public static void start(UltimateDonutSmp plugin) {
        // 600 ticks = 30 seconds; first check after 30 s as well
        plugin.getSpigotScheduler().runGlobalTimer(new FarmingMetaTask(plugin), 600L, 600L);
    }

    @Override
    public void run() {
        if (!plugin.getFeatureManager().isEnabled(com.bx.ultimateDonutSmp.managers.FeatureManager.Feature.WORTH)) {
            return;
        }
        if (plugin.getFarmingMetaManager().isTimeToRotate()) {
            plugin.getFarmingMetaManager().rotate();
        }
    }
}
