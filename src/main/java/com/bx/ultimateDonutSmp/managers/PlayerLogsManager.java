package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import java.util.UUID;

public class PlayerLogsManager {

    private final UltimateDonutSmp plugin;

    public PlayerLogsManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void log(UUID uuid, String name, String category, String type, String details) {
        if (uuid == null) {
            return;
        }
        long timestamp = System.currentTimeMillis();
        plugin.getSpigotScheduler().runAsync(() -> {
            plugin.getDatabaseManager().addPlayerLog(uuid, name, category, type, details, timestamp);
        });
    }
}
