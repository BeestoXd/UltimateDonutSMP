package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.function.Function;

public final class MobSpawnPolicy {

    private static final String VANILLA_SPAWNER_MOB_KEY = "vanilla_spawner_mob";
    private static final byte TRUE = 1;

    private MobSpawnPolicy() {
    }

    public static boolean isVanillaSpawnerSpawn(CreatureSpawnEvent.SpawnReason reason) {
        return reason == CreatureSpawnEvent.SpawnReason.SPAWNER;
    }

    public static void markVanillaSpawnerMob(UltimateDonutSmp plugin, LivingEntity entity) {
        if (plugin == null || entity == null) {
            return;
        }
        entity.getPersistentDataContainer().set(
                plugin.getKey(VANILLA_SPAWNER_MOB_KEY),
                PersistentDataType.BYTE,
                TRUE
        );
    }

    public static boolean isVanillaSpawnerMob(UltimateDonutSmp plugin, LivingEntity entity) {
        if (plugin == null || entity == null) {
            return false;
        }
        return entity.getPersistentDataContainer().getOrDefault(
                plugin.getKey(VANILLA_SPAWNER_MOB_KEY),
                PersistentDataType.BYTE,
                (byte) 0
        ) == TRUE;
    }

    public static boolean isHostileMob(LivingEntity entity) {
        return entity instanceof Monster
                || entity instanceof org.bukkit.entity.Slime
                || entity instanceof org.bukkit.entity.Ghast
                || entity instanceof org.bukkit.entity.Hoglin;
    }

    public static boolean hasCustomName(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        return entity.getCustomName() != null && !entity.getCustomName().isEmpty();
    }

    public static boolean shouldCancelMobSpawn(
            Location spawnLocation,
            Collection<? extends Player> players,
            double radius,
            Function<Player, PlayerData> dataProvider
    ) {
        if (spawnLocation == null || players == null || players.isEmpty()) {
            return false;
        }
        double radiusSquared = radius * radius;
        for (Player player : players) {
            if (player == null) continue;
            PlayerData data = dataProvider != null ? dataProvider.apply(player) : null;
            if (data == null || data.isMobSpawnEnabled()) {
                continue;
            }
            Location playerLoc = player.getLocation();
            if (playerLoc == null) {
                continue;
            }
            if (playerLoc.getWorld() != null && spawnLocation.getWorld() != null
                    && !playerLoc.getWorld().equals(spawnLocation.getWorld())) {
                continue;
            }
            double dx = playerLoc.getX() - spawnLocation.getX();
            double dy = playerLoc.getY() - spawnLocation.getY();
            double dz = playerLoc.getZ() - spawnLocation.getZ();
            if (dx * dx + dy * dy + dz * dz <= radiusSquared) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldCancelPhantomSpawn(
            Location spawnLocation,
            Collection<? extends Player> players,
            double radius,
            Function<Player, PlayerData> dataProvider
    ) {
        if (spawnLocation == null || players == null || players.isEmpty()) {
            return false;
        }
        double radiusSquared = radius * radius;
        for (Player player : players) {
            if (player == null) continue;
            PlayerData data = dataProvider != null ? dataProvider.apply(player) : null;
            if (data == null || data.isPhantomEnabled()) {
                continue;
            }
            Location playerLoc = player.getLocation();
            if (playerLoc == null) {
                continue;
            }
            if (playerLoc.getWorld() != null && spawnLocation.getWorld() != null
                    && !playerLoc.getWorld().equals(spawnLocation.getWorld())) {
                continue;
            }
            double dx = playerLoc.getX() - spawnLocation.getX();
            double dz = playerLoc.getZ() - spawnLocation.getZ();
            if (dx * dx + dz * dz <= radiusSquared) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldRemoveFromPeriodicCleanup(
            boolean monster,
            EntityType type,
            boolean vanillaSpawnerMob
    ) {
        return shouldRemoveFromPeriodicCleanup(monster, type, vanillaSpawnerMob, false);
    }

    public static boolean shouldRemoveFromPeriodicCleanup(
            boolean monster,
            EntityType type,
            boolean vanillaSpawnerMob,
            boolean hasCustomName
    ) {
        if (!monster || type == null || vanillaSpawnerMob || hasCustomName) {
            return false;
        }
        return switch (type) {
            case PHANTOM, WITHER, ENDER_DRAGON, ELDER_GUARDIAN, WARDEN -> false;
            default -> true;
        };
    }

    public static boolean shouldRemoveFromPeriodicCleanup(
            UltimateDonutSmp plugin,
            LivingEntity entity
    ) {
        return entity != null && shouldRemoveFromPeriodicCleanup(
                isHostileMob(entity),
                entity.getType(),
                isVanillaSpawnerMob(plugin, entity),
                hasCustomName(entity)
        );
    }
}
