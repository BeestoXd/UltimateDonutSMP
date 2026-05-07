package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;

public class OrdersExpiryTask implements Runnable {

    private final UltimateDonutSmp plugin;

    private OrdersExpiryTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public static void start(UltimateDonutSmp plugin) {
        long configuredSeconds = plugin.getConfigManager().getOrders()
                .getLong("SETTINGS.EXPIRE_CHECK_SECONDS", 30L);
        long periodTicks = Math.max(20L, configuredSeconds * 20L);
        plugin.getPaperScheduler().runGlobalTimer(new OrdersExpiryTask(plugin), periodTicks, periodTicks);
    }

    @Override
    public void run() {
        if (plugin.getOrdersManager() != null) {
            plugin.getOrdersManager().expireOrders();
        }
    }
}
