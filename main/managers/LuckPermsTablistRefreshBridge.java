package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class LuckPermsTablistRefreshBridge {

    private static final long REFRESH_DELAY_TICKS = 2L;

    private final UltimateDonutSmp plugin;
    private final Map<UUID, BukkitTask> pendingRefreshes = new ConcurrentHashMap<>();

    private Object subscription;

    public LuckPermsTablistRefreshBridge(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void start() {
        shutdown();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider", false, classLoader);
            Class<?> recalculationEventClass = Class.forName(
                    "net.luckperms.api.event.user.UserDataRecalculateEvent",
                    false,
                    classLoader
            );
            Object luckPerms = providerClass.getMethod("get").invoke(null);
            Object eventBus = luckPerms.getClass().getMethod("getEventBus").invoke(luckPerms);
            Method subscribe = eventBus.getClass().getMethod(
                    "subscribe",
                    Object.class,
                    Class.class,
                    Consumer.class
            );
            subscription = subscribe.invoke(
                    eventBus,
                    plugin,
                    recalculationEventClass,
                    (Consumer<Object>) this::onUserDataRecalculate
            );
            plugin.getLogger().info("LuckPerms tablist refresh bridge enabled.");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            subscription = null;
            plugin.getLogger().log(
                    Level.WARNING,
                    "LuckPerms API is unavailable; tablist permission refresh bridge is disabled.",
                    exception
            );
        }
    }

    public void shutdown() {
        if (subscription != null) {
            try {
                subscription.getClass().getMethod("close").invoke(subscription);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to close LuckPerms tablist refresh bridge.", exception);
            } finally {
                subscription = null;
            }
        }

        for (BukkitTask task : pendingRefreshes.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        pendingRefreshes.clear();
    }

    private void onUserDataRecalculate(Object event) {
        UUID playerId = extractPlayerId(event);
        if (playerId == null) {
            return;
        }

        pendingRefreshes.computeIfAbsent(playerId, id -> plugin.getSpigotScheduler().runGlobalLater(
                () -> refreshOnlinePlayer(id),
                REFRESH_DELAY_TICKS
        ));
    }

    private UUID extractPlayerId(Object event) {
        try {
            Object user = event.getClass().getMethod("getUser").invoke(event);
            if (user == null) {
                return null;
            }
            Object uniqueId = user.getClass().getMethod("getUniqueId").invoke(user);
            return uniqueId instanceof UUID uuid ? uuid : null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to read LuckPerms recalculation event.", exception);
            return null;
        }
    }

    private void refreshOnlinePlayer(UUID playerId) {
        pendingRefreshes.remove(playerId);

        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }

        plugin.getSpigotScheduler().runEntity(player, () -> refreshPlayer(player));
    }

    private void refreshPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        player.recalculatePermissions();
        try {
            player.updateCommands();
        } catch (RuntimeException | LinkageError ignored) {
        }

        if (plugin.getTablistManager() == null) {
            return;
        }

        plugin.getTablistManager().update(player);
        plugin.getTablistManager().updateTablistName(player);
        plugin.getTablistManager().refreshSkinHeads(player);
    }
}
