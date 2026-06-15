package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final UltimateDonutSmp plugin;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public PlayerDataManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public PlayerData loadOrCreate(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getDatabaseManager().loadPlayer(uuid);
        if (data == null) {
            data = new PlayerData(uuid, player.getName());
            double startMoney = plugin.getConfigManager().getConfig()
                    .getDouble("SETTINGS.MONEY-PER-DEFAULT", 1000.0);
            data.setMoney(startMoney);
            plugin.getDatabaseManager().savePlayer(data);
        } else {
            data.setUsername(player.getName());
        }
        data.setSessionStartMillis(System.currentTimeMillis());
        cache.put(uuid, data);
        return data;
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            data.commitSession();
            plugin.getDatabaseManager().savePlayer(data);
            cache.remove(uuid);
        }
    }

    public void saveAll() {
        for (PlayerData data : cache.values()) {
            data.commitSession();
            plugin.getDatabaseManager().savePlayer(data);
        }
    }

    public PlayerData get(UUID uuid) {
        return cache.get(uuid);
    }

    public PlayerData get(Player player) {
        return cache.get(player.getUniqueId());
    }

    public Collection<PlayerData> getAll() {
        return cache.values();
    }

    public boolean isLoaded(UUID uuid) {
        return cache.containsKey(uuid);
    }

    /** Periodic dirty-save without committing sessions */
    public void autoSaveDirty() {
        if (plugin.getStatsWipeManager() != null && plugin.getStatsWipeManager().isWipeInProgress()) {
            return;
        }
        for (PlayerData data : cache.values()) {
            if (data.isDirty()) {
                plugin.getDatabaseManager().savePlayer(data);
            }
        }
    }
}
