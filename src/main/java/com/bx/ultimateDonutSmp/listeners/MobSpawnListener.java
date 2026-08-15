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

import java.util.function.Function;

/**
 * Enforces the per-player mob spawn toggle purely by cancelling {@link CreatureSpawnEvent}. The
 * toggle governs the spawn cycle only: mobs that are already alive stay alive when a player turns it
 * off, and turning it back on lets the next natural spawn attempt through immediately. Nothing here
 * removes entities, so there is no sweep of the loaded world at any point.
 */
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

        if (!isPreventableSpawnReason(event.getSpawnReason())) return;

        refreshSettingsIfNeeded();

        if (event.getEntityType() == EntityType.PHANTOM) {
            if (shouldCancelPhantomSpawn(entity.getLocation())) {
                event.setCancelled(true);
            }
            return;
        }

        if (!MobSpawnPolicy.isHostileMob(entity)) return;

        if (MobSpawnPolicy.isBoss(event.getEntityType())) return;

        if (shouldCancelMobSpawn(entity.getLocation())) {
            event.setCancelled(true);
        }
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
