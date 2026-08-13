package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.MobSpawnPolicy;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.function.Function;

public class MobSpawnListener implements Listener {

    private final UltimateDonutSmp plugin;
    private final Function<Player, PlayerData> dataProvider;

    private volatile FileConfiguration cachedConfig;
    private volatile double mobSpawnRadius = 50.0D;
    private volatile double phantomSpawnRadius = 40.0D;

    public MobSpawnListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.dataProvider = p -> plugin.getPlayerDataManager().get(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (MobSpawnPolicy.hasCustomName(entity)) return;

        refreshSettingsIfNeeded();

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

    /**
     * Clears hostile mobs that were already loaded around a player who joins with the toggle off.
     * This is a single scan per join, not a repeating server-wide entity sweep.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getSpigotScheduler().runEntityLater(player, () -> {
            if (!player.isOnline()) {
                return;
            }
            PlayerData data = dataProvider.apply(player);
            if (data == null || data.isMobSpawnEnabled()) {
                return;
            }
            refreshSettingsIfNeeded();
            MobSpawnPolicy.clearNearbyHostileMobs(plugin, player, mobSpawnRadius);
        }, 20L);
    }

    /**
     * Re-reads the radii only when {@link com.bx.ultimateDonutSmp.managers.ConfigManager} swapped in a
     * new {@link FileConfiguration}, so the hot path costs a reference compare instead of a YAML path
     * lookup per spawn attempt.
     */
    private void refreshSettingsIfNeeded() {
        FileConfiguration current = plugin.getConfigManager().getConfig();
        if (current == cachedConfig || current == null) {
            return;
        }
        mobSpawnRadius = Math.max(0.0D, current.getDouble("SETTINGS.MOB-SPAWN-RADIUS", 50));
        phantomSpawnRadius = Math.max(0.0D, current.getDouble("SETTINGS.PHANTOM-SPAWN-RADIUS", 40));
        cachedConfig = current;
    }

    private boolean shouldCancelPhantomSpawn(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        return MobSpawnPolicy.shouldCancelPhantomSpawn(
                location,
                location.getWorld().getPlayers(),
                phantomSpawnRadius,
                dataProvider
        );
    }

    private boolean shouldCancelMobSpawn(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        return MobSpawnPolicy.shouldCancelMobSpawn(
                location,
                location.getWorld().getPlayers(),
                mobSpawnRadius,
                dataProvider
        );
    }

    private boolean isPreventableSpawnReason(CreatureSpawnEvent.SpawnReason reason) {
        if (reason == null) return false;
        return switch (reason) {
            case CUSTOM, SPAWNER_EGG, BUILD_WITHER, BREEDING -> false;
            default -> true;
        };
    }
}
