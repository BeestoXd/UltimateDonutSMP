package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

public class AuctionHouseExpiryTask implements Runnable {

    private final UltimateDonutSmp plugin;

    private AuctionHouseExpiryTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public static void start(UltimateDonutSmp plugin) {
        long configuredSeconds = plugin.getConfigManager().getAuctionHouse()
                .getLong("SETTINGS.EXPIRE_CHECK_SECONDS", 30L);
        long periodTicks = Math.max(20L, configuredSeconds * 20L);
        plugin.getPaperScheduler().runGlobalTimer(new AuctionHouseExpiryTask(plugin), periodTicks, periodTicks);
    }

    @Override
    public void run() {
        if (plugin.getAuctionHouseManager() != null) {
            plugin.getAuctionHouseManager().expireListings();
        }
    }
}
