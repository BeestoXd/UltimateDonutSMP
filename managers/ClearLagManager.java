package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.List;

public class ClearLagManager {

    private final UltimateDonutSmp plugin;
    private int countdown;
    private boolean running = false;

    public ClearLagManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.ENABLED", true);
    }

    public int getIntervalMinutes() {
        return plugin.getConfigManager().getConfig().getInt("CLEAR-LAG.EVERY", 5);
    }

    public boolean clearAnimals() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.ANIMALS", true);
    }

    public boolean clearMonsters() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.MONSTERS", true);
    }

    public boolean clearDroppedItems() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.DROPPED-ITEMS", true);
    }

    public List<String> getExcludedWorlds() {
        return plugin.getConfigManager().getConfig().getStringList("CLEAR-LAG.EXCLUDED-WORLDS");
    }

    public int clearEntities() {
        List<String> excluded = getExcludedWorlds();
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            if (excluded.contains(world.getName())) continue;
            for (Entity entity : world.getEntities()) {
                boolean remove = false;
                if (clearDroppedItems() && entity instanceof Item) remove = true;
                if (clearAnimals() && entity instanceof Animals) remove = true;
                if (clearMonsters() && entity instanceof Monster) remove = true;
                if (remove && !(entity instanceof Player)) {
                    entity.remove();
                    count++;
                }
            }
        }
        return count;
    }

    public void broadcastCountdown(int seconds) {
        String msg = plugin.getConfigManager().getMessage("CLEAR-LAG.COUNTDOWN",
                "{seconds}", String.valueOf(seconds));
        broadcastToSubscribedPlayers(msg);
    }

    public void broadcastSuccess(int total) {
        String msg = plugin.getConfigManager().getMessage("CLEAR-LAG.SUCCESS",
                "{total}", String.valueOf(total));
        broadcastToSubscribedPlayers(msg);
    }

    private void broadcastToSubscribedPlayers(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerDataManager().get(player);
            if (data != null && !data.isClearEntitiesMessagesEnabled()) {
                continue;
            }

            player.sendMessage(ColorUtils.toComponent(message));
        }
    }
}
