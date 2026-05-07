package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OptimizationManager;

/**
 * Updates tablist header/footer and entry names for all players every 40 ticks (2s).
 */
public class TablistTask implements Runnable {

    private final UltimateDonutSmp plugin;

    public TablistTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getOptimizationManager() != null
                && !plugin.getOptimizationManager().shouldRun(OptimizationManager.OptimizedTask.TABLIST)) {
            return;
        }
        plugin.getTablistManager().updateAll();
        plugin.getTablistManager().updateNamesAll();
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getPaperScheduler().runGlobalTimer(new TablistTask(plugin), 40L, 40L);
    }
}
