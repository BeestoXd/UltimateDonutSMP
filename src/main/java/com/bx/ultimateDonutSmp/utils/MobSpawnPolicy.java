package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Collection;
import java.util.function.Function;

public final class MobSpawnPolicy {

    private MobSpawnPolicy() {
    }

    public static boolean isHostileMob(LivingEntity entity) {
        return entity instanceof Monster
                || entity instanceof org.bukkit.entity.Slime
                || entity instanceof org.bukkit.entity.Ghast
                || entity instanceof org.bukkit.entity.Hoglin;
    }

    /**
     * Bosses stay outside the toggle. They come from one-off world features or player rituals rather
     * than the ambient spawn cycle the toggle is meant to thin out, so cancelling them would delete
     * content that never comes back (an elder guardian cancelled at chunk gen leaves the monument
     * empty forever).
     */
    public static boolean isBoss(EntityType type) {
        if (type == null) {
            return false;
        }
        return switch (type) {
            case WITHER, ENDER_DRAGON, ELDER_GUARDIAN, WARDEN -> true;
            default -> false;
        };
    }

    /**
     * Whether the toggle is allowed to cancel a spawn that came from {@code reason}.
     *
     * <p>Trial spawners sit outside the toggle by default. A trial spawner counts the mobs it
     * released and starts ejecting its rewards once none of them are left alive, so cancelling the
     * spawn does not just keep the room quiet — it hands the player the loot without the fight.
     * Servers that would rather keep the quiet room can set
     * {@code SETTINGS.MOB-SPAWN-TOGGLE-BLOCKS-TRIAL-SPAWNERS} to true and take that trade.
     */
    public static boolean isPreventableSpawnReason(
            CreatureSpawnEvent.SpawnReason reason,
            boolean trialSpawnersBlocked
    ) {
        if (reason == null) {
            return false;
        }
        if (reason == CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER) {
            return trialSpawnersBlocked;
        }
        return switch (reason) {
            case CUSTOM, SPAWNER_EGG, BUILD_WITHER, BREEDING -> false;
            default -> true;
        };
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
}
