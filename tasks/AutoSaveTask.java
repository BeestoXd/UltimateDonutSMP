package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

/**
 * Auto-saves dirty player data every 5 minutes.
 */
public class AutoSaveTask implements Runnable {

    private final UltimateDonutSmp plugin;

    public AutoSaveTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getPlayerDataManager().autoSaveDirty();
        if (plugin.getConfigManager().getDatabase().getBoolean("DATABASE.MONGODB.SYNC-ON-AUTOSAVE", true)) {
            plugin.getDatabaseManager().flush();
        }
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getPaperScheduler().runAsyncTimer(new AutoSaveTask(plugin), 5 * 60 * 20L, 5 * 60 * 20L);
    }
}
