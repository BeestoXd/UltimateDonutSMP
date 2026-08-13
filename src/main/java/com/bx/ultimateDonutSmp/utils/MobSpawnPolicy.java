package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
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
        if (spawnLocation == null || radius <= 0.0D || players == null || players.isEmpty()) {
            return false;
        }
        double radiusSquared = radius * radius;
        int chunkRadius = chunkRadius(radius);
        int spawnChunkX = spawnLocation.getBlockX() >> 4;
        int spawnChunkZ = spawnLocation.getBlockZ() >> 4;
        Location buffer = new Location(null, 0.0D, 0.0D, 0.0D);
        for (Player player : players) {
            if (player == null) continue;
            PlayerData data = dataProvider != null ? dataProvider.apply(player) : null;
            if (data == null || data.isMobSpawnEnabled()) {
                continue;
            }
            Location playerLoc = locationOf(player, buffer);
            if (playerLoc == null) {
                continue;
            }
            if (playerLoc.getWorld() != null && spawnLocation.getWorld() != null
                    && !playerLoc.getWorld().equals(spawnLocation.getWorld())) {
                continue;
            }
            if (isOutsideChunkRadius(playerLoc, spawnChunkX, spawnChunkZ, chunkRadius)) {
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
        if (spawnLocation == null || radius <= 0.0D || players == null || players.isEmpty()) {
            return false;
        }
        double radiusSquared = radius * radius;
        int chunkRadius = chunkRadius(radius);
        int spawnChunkX = spawnLocation.getBlockX() >> 4;
        int spawnChunkZ = spawnLocation.getBlockZ() >> 4;
        Location buffer = new Location(null, 0.0D, 0.0D, 0.0D);
        for (Player player : players) {
            if (player == null) continue;
            PlayerData data = dataProvider != null ? dataProvider.apply(player) : null;
            if (data == null || data.isPhantomEnabled()) {
                continue;
            }
            Location playerLoc = locationOf(player, buffer);
            if (playerLoc == null) {
                continue;
            }
            if (playerLoc.getWorld() != null && spawnLocation.getWorld() != null
                    && !playerLoc.getWorld().equals(spawnLocation.getWorld())) {
                continue;
            }
            if (isOutsideChunkRadius(playerLoc, spawnChunkX, spawnChunkZ, chunkRadius)) {
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

    /**
     * Reads the player position into {@code buffer} so a spawn check over N players allocates one
     * {@link Location} instead of N. Falls back to {@link Player#getLocation()} when the buffered
     * overload is unavailable.
     */
    private static Location locationOf(Player player, Location buffer) {
        Location buffered = player.getLocation(buffer);
        return buffered != null ? buffered : player.getLocation();
    }

    /**
     * Chunk span that fully contains {@code radius}, used as an integer-only pre-filter before the
     * squared-distance math.
     */
    private static int chunkRadius(double radius) {
        return (((int) Math.ceil(radius)) >> 4) + 1;
    }

    private static boolean isOutsideChunkRadius(
            Location playerLocation,
            int spawnChunkX,
            int spawnChunkZ,
            int chunkRadius
    ) {
        int playerChunkX = playerLocation.getBlockX() >> 4;
        int playerChunkZ = playerLocation.getBlockZ() >> 4;
        return Math.abs(playerChunkX - spawnChunkX) > chunkRadius
                || Math.abs(playerChunkZ - spawnChunkZ) > chunkRadius;
    }

    /**
     * One-shot removal of the hostile mobs already loaded around {@code player}. Used when a player
     * turns the toggle off and when they join with it already off, so the toggle stays meaningful
     * without a repeating server-wide entity scan.
     */
    public static void clearNearbyHostileMobs(UltimateDonutSmp plugin, Player player, double radius) {
        if (plugin == null || player == null || radius <= 0.0D) {
            return;
        }
        double radiusSquared = radius * radius;
        Location playerLocation = player.getLocation();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (!shouldRemoveFromPeriodicCleanup(plugin, living)) {
                continue;
            }
            if (living.getLocation().distanceSquared(playerLocation) > radiusSquared) {
                continue;
            }
            living.remove();
        }
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
