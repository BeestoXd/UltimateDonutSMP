package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class PlayerStatsListener implements Listener {

    private final UltimateDonutSmp plugin;

    public PlayerStatsListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getFfaManager() != null && plugin.getFfaManager().shouldSuppressGlobalStats(event.getPlayer().getUniqueId())) {
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(event.getPlayer());
        if (data != null) {
            data.addBlocksPlaced(1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getFfaManager() != null && plugin.getFfaManager().shouldSuppressGlobalStats(event.getPlayer().getUniqueId())) {
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(event.getPlayer());
        if (data != null) {
            data.addBlocksBroken(1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || event.getEntity() instanceof Player) {
            return;
        }
        if (plugin.getFfaManager() != null && plugin.getFfaManager().shouldSuppressGlobalStats(killer.getUniqueId())) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().get(killer);
        if (data != null) {
            data.addMobKill();
        }
    }
}
