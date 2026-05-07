package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OptimizationManager;

/**
 * Updates all player scoreboards every 2 ticks (~10x/sec).
 */
public class ScoreboardTask implements Runnable {

    private final UltimateDonutSmp plugin;

    public ScoreboardTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getOptimizationManager() != null
                && !plugin.getOptimizationManager().shouldRun(OptimizationManager.OptimizedTask.SCOREBOARD)) {
            return;
        }
        plugin.getScoreboardManager().updateAll();
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getPaperScheduler().runGlobalTimer(new ScoreboardTask(plugin), 2L, 2L);
    }
}
