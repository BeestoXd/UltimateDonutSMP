package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

/**
 * Checks every 30 seconds whether the Billford trade rotation timer has
 * expired and, if so, advances to the next trade.
 */
public class BillfordTask implements Runnable {

    private final UltimateDonutSmp plugin;

    private BillfordTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public static void start(UltimateDonutSmp plugin) {
        // 600 ticks = 30 seconds; first check after 30 s as well
        plugin.getSpigotScheduler().runGlobalTimer(new BillfordTask(plugin), 600L, 600L);
    }

    @Override
    public void run() {
        if (plugin.getBillfordManager().isTimeToAdvance()) {
            plugin.getBillfordManager().advanceTrade();
        }
    }
}
