package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import java.util.Collection;
import java.util.UUID;

public class PlayerLogsManager {

    /** Category shared by every chat entry, so the logs menu can pick an icon for them. */
    public static final String CHAT_CATEGORY = "chat";
    /** Log type written for a public chat message, and the key the chat log browser reads back. */
    public static final String PUBLIC_CHAT_TYPE = "CHAT_PUBLIC";

    private final UltimateDonutSmp plugin;

    public PlayerLogsManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void log(UUID uuid, String name, String category, String type, String details) {
        if (uuid == null) {
            return;
        }
        long timestamp = System.currentTimeMillis();
        plugin.getDatabaseManager().executeAsync(() -> {
            plugin.getDatabaseManager().addPlayerLog(uuid, name, category, type, details, timestamp);
        });
    }

    public void logBatch(Collection<DatabaseManager.PlayerLogRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        plugin.getDatabaseManager().executeAsync(() -> {
            plugin.getDatabaseManager().addPlayerLogBatch(records);
        });
    }
}
