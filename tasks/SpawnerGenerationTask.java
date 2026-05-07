package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

public class SpawnerGenerationTask implements Runnable {

    private final UltimateDonutSmp plugin;

    private SpawnerGenerationTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public static void start(UltimateDonutSmp plugin) {
        long configuredSeconds = plugin.getConfigManager().getSpawners()
                .getLong("SETTINGS.GENERATION_INTERVAL_SECONDS", 5L);
        long periodTicks = Math.max(20L, configuredSeconds * 20L);
        plugin.getPaperScheduler().runGlobalTimer(new SpawnerGenerationTask(plugin), periodTicks, periodTicks);
    }

    @Override
    public void run() {
        if (plugin.getSpawnerManager() != null) {
            plugin.getSpawnerManager().processGeneration();
        }
    }
}
