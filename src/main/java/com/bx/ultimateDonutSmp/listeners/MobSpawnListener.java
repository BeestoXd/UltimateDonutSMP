package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.MobSpawnPolicy;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobSpawnListener implements Listener {

    private final UltimateDonutSmp plugin;

    public MobSpawnListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (MobSpawnPolicy.hasCustomName(entity)) return;

        if (event.getEntityType() == EntityType.PHANTOM) {
            if (isPreventableSpawnReason(event.getSpawnReason()) && shouldCancelPhantomSpawn(entity.getLocation())) {
                event.setCancelled(true);
            }
            return;
        }

        if (!MobSpawnPolicy.isHostileMob(entity)) return;

        if (isPreventableSpawnReason(event.getSpawnReason())) {
            if (shouldCancelMobSpawn(entity.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }

        if (MobSpawnPolicy.isVanillaSpawnerSpawn(event.getSpawnReason())) {
            MobSpawnPolicy.markVanillaSpawnerMob(plugin, entity);
        }
    }

    private boolean shouldCancelPhantomSpawn(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        double radius = plugin.getConfigManager().getConfig().getDouble("SETTINGS.PHANTOM-SPAWN-RADIUS", 40);
        return MobSpawnPolicy.shouldCancelPhantomSpawn(
                location,
                location.getWorld().getPlayers(),
                radius,
                p -> plugin.getPlayerDataManager().get(p)
        );
    }

    private boolean shouldCancelMobSpawn(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        double radius = plugin.getConfigManager().getConfig().getDouble("SETTINGS.MOB-SPAWN-RADIUS", 50);
        return MobSpawnPolicy.shouldCancelMobSpawn(
                location,
                location.getWorld().getPlayers(),
                radius,
                p -> plugin.getPlayerDataManager().get(p)
        );
    }

    private boolean isPreventableSpawnReason(CreatureSpawnEvent.SpawnReason reason) {
        if (reason == null) return false;
        return switch (reason) {
            case CUSTOM, SPAWNER_EGG, BUILD_WITHER, BREEDING -> false;
            default -> true;
        };
    }

    private void startCleanupTask() {
        plugin.getSpigotScheduler().runGlobalTimer(this::cleanupNearbyHostileMobs, 20L, 20L);
    }

    private void cleanupNearbyHostileMobs() {
        double radius = plugin.getConfigManager().getConfig().getDouble("SETTINGS.MOB-SPAWN-RADIUS", 50);
        double radiusSquared = radius * radius;

        plugin.getSpigotScheduler().forEachOnlinePlayer(player -> {
            PlayerData data = plugin.getPlayerDataManager().get(player);
            if (data == null || data.isMobSpawnEnabled()) {
                return;
            }

            removeNearbyHostiles(player, radius, radiusSquared);
        });
    }

    private void removeNearbyHostiles(Player player, double radius, double radiusSquared) {
        Location playerLocation = player.getLocation();

        for (org.bukkit.entity.Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
            if (!(nearby instanceof LivingEntity entity) || !isRemovableHostileMob(entity)) {
                continue;
            }
            if (entity.getLocation().distanceSquared(playerLocation) > radiusSquared) {
                continue;
            }

            entity.remove();
        }
    }

    private boolean isRemovableHostileMob(LivingEntity entity) {
        return MobSpawnPolicy.shouldRemoveFromPeriodicCleanup(plugin, entity);
    }
}
