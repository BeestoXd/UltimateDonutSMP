package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerMoveListener implements Listener {

    private final UltimateDonutSmp plugin;

    public PlayerMoveListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // only care about block-level movement
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
            return;
        }

        // reset afk timer
        plugin.getAFKManager().recordMovement(player.getUniqueId());
        plugin.getShardManager().recordMovement(
                player.getUniqueId(),
                event.getFrom(),
                event.getTo(),
                event instanceof PlayerTeleportEvent
        );

        // check pending teleport
        if (plugin.getTeleportManager().hasPending(player.getUniqueId())) {
            plugin.getTeleportManager().checkMovement(player);
        }
    }
}
