package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

/**
 * Keeps the balance under every player's name up to date. The client draws the line itself, so
 * there is no position to maintain and this only has to keep up with money changing hands.
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
