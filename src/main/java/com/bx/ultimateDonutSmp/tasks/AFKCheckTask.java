package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ShardManager;
import org.bukkit.entity.Player;

public class AFKCheckTask implements Runnable {

    private final UltimateDonutSmp plugin;

    public AFKCheckTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getAFKManager().isEnabled()) {
            return;
        }

        plugin.getSpigotScheduler().forEachOnlinePlayer(this::tickPlayer);
    }

    private void tickPlayer(Player player) {
        if (plugin.getAFKManager().isAfk(player.getUniqueId())) {
            return;
        }

        ShardManager.ShardCuboidConfig active = plugin.getShardManager().findMatchingShardCuboid(player);
        if (active != null) {
            return;
        }

        if (plugin.getAFKManager().isInSpawnCuboid(player)
                && plugin.getAFKManager().shouldGoAfk(player.getUniqueId())) {
            plugin.getAFKManager().sendToAfk(player);
        }
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getSpigotScheduler().runGlobalTimer(new AFKCheckTask(plugin), 200L, 200L);
    }
}
