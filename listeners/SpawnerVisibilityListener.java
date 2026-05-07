package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class SpawnerVisibilityListener implements Listener {

    private final UltimateDonutSmp plugin;

    public SpawnerVisibilityListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        scheduleRefresh(event.getPlayer(), 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getAntiEspManager().clearPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        scheduleRefresh(event.getPlayer(), 2L);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        scheduleRefresh(event.getPlayer(), 2L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }
        if (sameBlockAndChunk(event.getFrom(), event.getTo())) {
            return;
        }

        plugin.getAntiEspManager().updatePlayer(event.getPlayer());
    }

    private void scheduleRefresh(Player player, long delayTicks) {
        plugin.getPaperScheduler().runEntityLater(player, () -> {
            if (player.isOnline()) {
                plugin.getAntiEspManager().updatePlayer(player);
            }
        }, delayTicks);
    }

    private boolean sameBlockAndChunk(org.bukkit.Location from, org.bukkit.Location to) {
        return from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()
                && (from.getBlockX() >> 4) == (to.getBlockX() >> 4)
                && (from.getBlockZ() >> 4) == (to.getBlockZ() >> 4);
    }
}
