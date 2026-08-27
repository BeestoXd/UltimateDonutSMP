package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages player ping resolution and updates, specifically ensuring Bedrock (Geyser/Floodgate)
 * players have their ping correctly measured and updated instead of remaining stuck at 0.
 */
public final class PingManager implements Listener {

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Integer> pingCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> pendingKeepAlives = new ConcurrentHashMap<>();
    private boolean protocolLibEnabled = false;

    private volatile boolean geyserAbsent;
    private volatile Method geyserApiMethod;
    private volatile Method geyserConnectionMethod;

    public PingManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        initProtocolLibListener();
        startPeriodicPingTask();
    }

    private void initProtocolLibListener() {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            return;
        }

        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.addPacketListener(new PacketAdapter(
                    plugin,
                    ListenerPriority.MONITOR,
                    PacketType.Play.Client.KEEP_ALIVE,
                    PacketType.Play.Server.KEEP_ALIVE
            ) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if (event.isCancelled() || event.getPlayer() == null) return;
                    Player player = event.getPlayer();
                    try {
                        PacketContainer packet = event.getPacket();
                        if (packet.getLongs().size() > 0) {
                            long now = System.currentTimeMillis();
                            pendingKeepAlives.put(player.getUniqueId(), now);
                        }
                    } catch (Throwable ignored) {}
                }

                @Override
                public void onPacketReceiving(PacketEvent event) {
                    if (event.isCancelled() || event.getPlayer() == null) return;
                    Player player = event.getPlayer();
                    UUID uuid = player.getUniqueId();
                    Long sentTime = pendingKeepAlives.remove(uuid);
                    if (sentTime != null) {
                        long elapsed = System.currentTimeMillis() - sentTime;
                        if (elapsed >= 0 && elapsed < 10000) {
                            int ping = (int) elapsed;
                            pingCache.put(uuid, ping);
                            setNmsLatency(player, ping);
                        }
                    }
                }
            });
            protocolLibEnabled = true;
        } catch (Throwable t) {
            plugin.getLogger().log(Level.FINE, "ProtocolLib keep-alive ping measurement not available.", t);
        }
    }

    private void startPeriodicPingTask() {
        plugin.getSpigotScheduler().runGlobalTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                refreshPlayerPing(player);
            }
        }, 100L, 100L); // Every 5 seconds
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Immediately attempt resolution, then retry after a short delay for Bedrock handshake completion
        refreshPlayerPing(player);
        plugin.getSpigotScheduler().runEntityLater(player, () -> refreshPlayerPing(player), 20L);
        plugin.getSpigotScheduler().runEntityLater(player, () -> refreshPlayerPing(player), 60L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        pingCache.remove(uuid);
        pendingKeepAlives.remove(uuid);
    }

    public void refreshPlayerPing(Player player) {
        if (player == null || !player.isOnline()) return;
        getPing(player);
    }

    /**
     * Resolves the real ping for a player (including Bedrock players).
     */
    public int getPing(Player player) {
        if (player == null || !player.isOnline()) {
            return 0;
        }
        UUID uuid = player.getUniqueId();

        // 1. Check Geyser API (highest accuracy for Bedrock players connected via Geyser)
        int geyserPing = getGeyserPing(uuid);
        if (geyserPing > 0) {
            pingCache.put(uuid, geyserPing);
            setNmsLatency(player, geyserPing);
            return geyserPing;
        }

        // 2. Check Bukkit getPing()
        int bukkitPing = player.getPing();
        if (bukkitPing > 0) {
            pingCache.put(uuid, bukkitPing);
            return bukkitPing;
        }

        // 3. Check measured/cached ping
        Integer cached = pingCache.get(uuid);
        if (cached != null && cached > 0) {
            setNmsLatency(player, cached);
            return cached;
        }

        // 4. Fallback ping (1ms) while initial calculation is pending
        int fallback = 1;
        setNmsLatency(player, fallback);
        return fallback;
    }

    /**
     * Checks GeyserApi via reflection if Geyser is installed on the server.
     */
    private int getGeyserPing(UUID uuid) {
        // Without Geyser installed this lookup walks the whole plugin classloader graph and builds a
        // ClassNotFoundException, and it used to run on every ping refresh. The answer cannot change
        // while the server is up.
        if (geyserAbsent) {
            return -1;
        }

        try {
            Method apiMethod = geyserApiMethod;
            Method connectionMethod = geyserConnectionMethod;
            if (apiMethod == null || connectionMethod == null) {
                Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
                apiMethod = geyserApiClass.getMethod("api");
                connectionMethod = geyserApiClass.getMethod("connectionByUuid", UUID.class);
                geyserApiMethod = apiMethod;
                geyserConnectionMethod = connectionMethod;
            }

            Object apiInstance = apiMethod.invoke(null);
            if (apiInstance != null) {
                Object connection = connectionMethod.invoke(apiInstance, uuid);
                if (connection != null) {
                    Method pingMethod = connection.getClass().getMethod("ping");
                    Object result = pingMethod.invoke(connection);
                    if (result instanceof Number number) {
                        int ping = number.intValue();
                        if (ping >= 0) {
                            return ping;
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | LinkageError absent) {
            // Geyser is not installed. Stop asking.
            geyserAbsent = true;
        } catch (Throwable ignored) {
            // Geyser is present but this player is not a Bedrock connection, or the call failed.
        }
        return -1;
    }

    /**
     * Updates the underlying NMS ServerPlayer.latency field so player.getPing() and
     * server tablist packets reflect the updated ping value across all server systems.
     */
    private void setNmsLatency(Player player, int latency) {
        if (player == null) return;
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(player);
            if (handle == null) return;

            Class<?> clazz = handle.getClass();
            while (clazz != null && clazz != Object.class) {
                try {
                    Field f = clazz.getDeclaredField("latency");
                    f.setAccessible(true);
                    f.set(handle, latency);
                    return;
                } catch (NoSuchFieldException ignored) {
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Throwable ignored) {
            // Quiet fallback if NMS field is not accessible or named differently
        }
    }

    public boolean isProtocolLibEnabled() {
        return protocolLibEnabled;
    }
}
