package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.PlayerSettingDefaults;
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
        PlayerData data = cache.get(uuid);
        if (data != null) {
            data.setUsername(player.getName());
            data.setSessionStartMillis(System.currentTimeMillis());
            return data;
        }

        data = plugin.getDatabaseManager().loadPlayer(uuid);
        if (data == null) {
            data = new PlayerData(uuid, player.getName());
            PlayerSettingDefaults.applyDefaults(plugin, data);
            double startMoney = plugin.getConfigManager().getConfig()
                    .getDouble("SETTINGS.MONEY-PER-DEFAULT", 1000.0);
            data.setMoney(startMoney);
            plugin.getDatabaseManager().savePlayer(data);
        } else {
            data.setUsername(player.getName());
        }
        PlayerSettingDefaults.applyDisabledOptions(plugin, data);
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

    /**
     * Drops one player's cached copy without saving it. A restore has just rewritten their row, so
     * writing the in-memory totals back would undo it before they have even noticed.
     */
    public void discardWithoutSaving(UUID uuid) {
        cache.remove(uuid);
    }

    public void discardAllForServerWipe() {
        cache.clear();
    }

    public boolean isLoaded(UUID uuid) {
        return cache.containsKey(uuid);
    }

    /** Re-pins options removed with ENABLED: false after menus.yml was reloaded. */
    public void refreshRemovedSettingOptions() {
        for (PlayerData data : cache.values()) {
            PlayerSettingDefaults.applyDisabledOptions(plugin, data);
        }
    }

    /** Periodic dirty-save without committing sessions */
    public void autoSaveDirty() {
        if (plugin.getStatsWipeManager() != null && plugin.getStatsWipeManager().isWipeInProgress()) {
            return;
        }
        for (PlayerData data : cache.values()) {
            if (data.isDirty()) {
                plugin.getDatabaseManager().savePlayerAsync(data);
            }
        }
    }
}
