package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnderPearlManager implements Listener {

    private final UltimateDonutSmp plugin;
    private final Map<UUID, UUID> trackedPearls = new ConcurrentHashMap<>();
    private final Map<UUID, Location> pendingTeleports = new ConcurrentHashMap<>();

    public EnderPearlManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void handlePlayerDeath(Player victim) {
        if (victim == null) {
            return;
        }

        World world = victim.getWorld();
        if (world == null) {
            return;
        }

        for (EnderPearl pearl : world.getEntitiesByClass(EnderPearl.class)) {
            if (victim.equals(pearl.getShooter())) {
                pearl.setShooter(null);
                trackedPearls.put(pearl.getUniqueId(), victim.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl pearl)) {
            return;
        }

        UUID victimUuid = trackedPearls.remove(pearl.getUniqueId());
        if (victimUuid == null) {
            return;
        }

        Player victim = Bukkit.getPlayer(victimUuid);
        if (victim == null || !victim.isOnline()) {
            return;
        }

        Location landLoc = pearl.getLocation().clone();
        Location currentLoc = victim.getLocation();
        if (currentLoc != null) {
            landLoc.setYaw(currentLoc.getYaw());
            landLoc.setPitch(currentLoc.getPitch());
        }

        if (victim.isDead()) {
            pendingTeleports.put(victimUuid, landLoc);
        } else {
            plugin.getSpigotScheduler().teleport(victim, landLoc);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() != null) {
            pendingTeleports.remove(event.getPlayer().getUniqueId());
        }
    }

    public Location consumePendingTeleport(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }
        return pendingTeleports.remove(playerUuid);
    }

    public boolean isPearlTracked(UUID pearlUuid) {
        return trackedPearls.containsKey(pearlUuid);
    }

    public boolean hasPendingTeleport(UUID playerUuid) {
        return pendingTeleports.containsKey(playerUuid);
    }

    public void trackPearlDirectly(UUID pearlUuid, UUID playerUuid) {
        trackedPearls.put(pearlUuid, playerUuid);
    }

    public void setPendingTeleportDirectly(UUID playerUuid, Location location) {
        pendingTeleports.put(playerUuid, location);
    }

    public void clear() {
        trackedPearls.clear();
        pendingTeleports.clear();
    }
}
