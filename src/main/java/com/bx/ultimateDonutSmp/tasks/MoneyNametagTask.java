package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

/**
 * Keeps every money nametag under its owner and up to date with their balance.
 */
public class MoneyNametagTask implements Runnable {

    private final UltimateDonutSmp plugin;

    public MoneyNametagTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getMoneyNametagManager().updateAll();
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getMoneyNametagManager().purgeOrphanedDisplays();
        long interval = plugin.getMoneyNametagManager().getUpdateIntervalTicks();
        plugin.getSpigotScheduler().runGlobalTimer(new MoneyNametagTask(plugin), interval, interval);
    }
}
