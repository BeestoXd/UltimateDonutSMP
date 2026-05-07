package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
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
        if (!isNaturalSpawn(event.getSpawnReason())) return;

        if (event.getEntityType() == EntityType.PHANTOM) {
            if (shouldCancelPhantomSpawn(entity.getLocation())) {
                event.setCancelled(true);
            }
            return;
        }

        if (!(entity instanceof Monster)) return;
        if (shouldCancelMobSpawn(entity.getLocation())) {
            event.setCancelled(true);
        }
    }

    private boolean shouldCancelPhantomSpawn(Location location) {
        Player nearestPlayer = getNearestPlayer(
                location,
                plugin.getConfigManager().getConfig().getDouble("SETTINGS.PHANTOM-SPAWN-RADIUS", 40)
        );
        if (nearestPlayer == null) {
            return false;
        }

        PlayerData data = plugin.getPlayerDataManager().get(nearestPlayer);
        return data != null && !data.isPhantomEnabled();
    }

    private boolean shouldCancelMobSpawn(Location location) {
        Player nearestPlayer = getNearestPlayer(
                location,
                plugin.getConfigManager().getConfig().getDouble("SETTINGS.MOB-SPAWN-RADIUS", 50)
        );
        if (nearestPlayer == null) {
            return false;
        }

        PlayerData data = plugin.getPlayerDataManager().get(nearestPlayer);
        return data != null && !data.isMobSpawnEnabled();
    }

    private boolean isNaturalSpawn(CreatureSpawnEvent.SpawnReason reason) {
        return switch (reason) {
            case NATURAL, REINFORCEMENTS, PATROL, JOCKEY, CHUNK_GEN, NETHER_PORTAL, OCELOT_BABY, SLIME_SPLIT -> true;
            default -> false;
        };
    }

    private Player getNearestPlayer(Location location, double radius) {
        double radiusSquared = radius * radius;
        Player nearestPlayer = null;
        double nearestDistance = radiusSquared;

        for (Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distanceSquared(location);
            if (distance > radiusSquared || distance >= nearestDistance) {
                continue;
            }

            nearestPlayer = player;
            nearestDistance = distance;
        }

        return nearestPlayer;
    }

    private void startCleanupTask() {
        plugin.getPaperScheduler().runGlobalTimer(this::cleanupNearbyHostileMobs, 20L, 20L);
    }

    private void cleanupNearbyHostileMobs() {
        double radius = plugin.getConfigManager().getConfig().getDouble("SETTINGS.MOB-SPAWN-RADIUS", 50);
        double radiusSquared = radius * radius;

        plugin.getPaperScheduler().forEachOnlinePlayer(player -> {
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
        if (!(entity instanceof Monster)) {
            return false;
        }

        return switch (entity.getType()) {
            case PHANTOM, WITHER, ENDER_DRAGON, ELDER_GUARDIAN, WARDEN -> false;
            default -> true;
        };
    }
}
