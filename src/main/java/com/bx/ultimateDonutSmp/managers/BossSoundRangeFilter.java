package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.BossSoundRangePolicy;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps the wither spawn and ender dragon death sounds from reaching the whole server.
 *
 * <p>Both are sent as global level events, which carry a position but not a world, so the world is
 * taken from the boss itself as it spawns or dies and held for a short while. A wither only makes
 * its noise once its charge-up finishes, which is why the window has to outlive that; the dragon
 * fires on the tick after it dies. When there is no world to match against, the distance alone
 * decides, which is still the part the reporter cares about.
 */
public final class BossSoundRangeFilter implements Listener {

    private static final long ORIGIN_LIFETIME_MS = 60_000L;

    private final UltimateDonutSmp plugin;
    private final Map<Integer, TrackedOrigin> origins = new ConcurrentHashMap<>();
    private ProtocolManager protocolManager;
    private PacketListener listener;
    private boolean available;

    public BossSoundRangeFilter(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        initialize();
    }

    public boolean isAvailable() {
        return available;
    }

    public void shutdown() {
        if (protocolManager != null && listener != null) {
            protocolManager.removePacketListener(listener);
        }
        listener = null;
        protocolManager = null;
        available = false;
        origins.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Wither) {
            remember(BossSoundRangePolicy.WITHER_SPAWN_EFFECT_ID, event.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            remember(BossSoundRangePolicy.ENDER_DRAGON_DEATH_EFFECT_ID, event.getEntity().getLocation());
        }
    }

    private void remember(int effectId, Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        origins.put(effectId, new TrackedOrigin(
                location.getWorld().getName(),
                System.currentTimeMillis() + ORIGIN_LIFETIME_MS
        ));
    }

    private String originWorld(int effectId) {
        TrackedOrigin tracked = origins.get(effectId);
        if (tracked == null) {
            return null;
        }
        if (System.currentTimeMillis() > tracked.expiresAt()) {
            origins.remove(effectId, tracked);
            return null;
        }
        return tracked.world();
    }

    private void initialize() {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            return;
        }
        try {
            protocolManager = ProtocolLibrary.getProtocolManager();
            listener = new PacketAdapter(
                    plugin,
                    ListenerPriority.NORMAL,
                    PacketType.Play.Server.WORLD_EVENT
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    BossSoundRangeFilter.this.handle(event);
                }
            };
            protocolManager.addPacketListener(listener);
            available = true;
        } catch (Throwable error) {
            plugin.getLogger().warning("Boss sound range limiting is unavailable: " + error.getMessage());
            shutdown();
        }
    }

    private void handle(PacketEvent event) {
        Player viewer = event.getPlayer();
        if (viewer == null) {
            return;
        }
        Integer effectId = event.getPacket().getIntegers().readSafely(0);
        if (effectId == null) {
            return;
        }
        BossSoundRangePolicy policy = BossSoundRangePolicy.fromConfig(plugin.getConfig());
        if (!policy.filters(effectId)) {
            return;
        }
        BlockPosition source = event.getPacket().getBlockPositionModifier().readSafely(0);
        if (source == null) {
            return;
        }
        Location at = viewer.getLocation();
        BossSoundRangePolicy.Position origin = new BossSoundRangePolicy.Position(
                originWorld(effectId),
                source.getX() + 0.5D,
                source.getY() + 0.5D,
                source.getZ() + 0.5D
        );
        BossSoundRangePolicy.Position listenerAt = new BossSoundRangePolicy.Position(
                at.getWorld() == null ? null : at.getWorld().getName(),
                at.getX(),
                at.getY(),
                at.getZ()
        );
        if (!policy.canHear(origin, listenerAt)) {
            event.setCancelled(true);
        }
    }

    private record TrackedOrigin(String world, long expiresAt) {
    }
}
