package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

/**
 * Keeps every money nametag under its owner and up to date with their balance. It runs every tick
 * so the lines move in step with the players they belong to; the manager decides for itself how
 * much work each tick is worth.
 */
public class MoneyNametagTask implements Runnable {

    private final UltimateDonutSmp plugin;

    public MoneyNametagTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getMoneyNametagManager().tick();
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getMoneyNametagManager().purgeOrphanedDisplays();
        plugin.getSpigotScheduler().runGlobalTimer(new MoneyNametagTask(plugin), 1L, 1L);
    }
}
