package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class SkinsRestorerTablistRefreshBridge implements Listener {

    private static final long[] REFRESH_DELAY_TICKS = {1L, 5L, 20L, 40L, 100L};

    private final UltimateDonutSmp plugin;
    private final Map<UUID, List<ScheduledTask>> pendingRefreshes = new ConcurrentHashMap<>();

    private Object skinsRestorerEventBus;
    private Class<?> registeredEventClass;
    private boolean registeredAsBukkit;
    private ScheduledTask pollTask;

    public SkinsRestorerTablistRefreshBridge(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void start() {
        shutdown();
        if (!isSkinsRestorerAvailable()) {
            return;
        }

        if (registerApiEventBus()) {
            plugin.getLogger().info("SkinsRestorer tablist skin head refresh bridge enabled via API EventBus.");
            return;
        }

        if (registerBukkitEvent()) {
            plugin.getLogger().info("SkinsRestorer tablist skin head refresh bridge enabled via Bukkit Event.");
            return;
        }

        startTextureWatcher();
        plugin.getLogger().info("SkinsRestorer tablist skin head fallback watcher enabled.");
    }

    public void shutdown() {
        if (registeredAsBukkit) {
            HandlerList.unregisterAll(this);
            registeredAsBukkit = false;
        }

        if (pollTask != null) {
            pollTask.cancel();
            pollTask = null;
        }

        unregisterApiEventBus();
        skinsRestorerEventBus = null;
        registeredEventClass = null;

        for (List<ScheduledTask> tasks : pendingRefreshes.values()) {
            if (tasks == null) {
                continue;
            }
            for (ScheduledTask task : tasks) {
                if (task != null) {
                    task.cancel();
                }
            }
        }
        pendingRefreshes.clear();
    }

    private boolean isSkinsRestorerAvailable() {
        Plugin skinsRestorer = findSkinsRestorerPlugin();
        if (skinsRestorer == null || !skinsRestorer.isEnabled()) {
            return false;
        }

        try {
            findSkinsRestorerClass("net.skinsrestorer.api.SkinsRestorerProvider");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private void startTextureWatcher() {
        pollTask = plugin.getFoliaScheduler().runGlobalTimer(this::pollOnlinePlayers, 20L, 20L);
    }

    private void pollOnlinePlayers() {
        if (plugin.getTablistManager() == null) {
            return;
        }

        plugin.getFoliaScheduler().forEachOnlinePlayer(player -> {
            if (plugin.getTablistManager() != null) {
                plugin.getTablistManager().refreshStoredSkinTexture(player);
            }
        });
    }

    private boolean registerApiEventBus() {
        try {
            Class<?> providerClass = findSkinsRestorerClass("net.skinsrestorer.api.SkinsRestorerProvider");
            Object skinsRestorer = providerClass.getMethod("get").invoke(null);

            Class<?> apiClass = findSkinsRestorerClass("net.skinsrestorer.api.SkinsRestorer");
            Method getEventBus = apiClass.getMethod("getEventBus");
            getEventBus.setAccessible(true);
            skinsRestorerEventBus = getEventBus.invoke(skinsRestorer);

            registeredEventClass = findSkinsRestorerClass("net.skinsrestorer.api.event.SkinApplyEvent");
            return subscribeApiEventBus();
        } catch (Throwable ignored) {
            skinsRestorerEventBus = null;
            registeredEventClass = null;
            return false;
        }
    }

    private boolean registerBukkitEvent() {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) findSkinsRestorerClass(
                    "net.skinsrestorer.bukkit.event.SkinApplyBukkitEvent"
            );
            Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    this,
                    EventPriority.MONITOR,
                    (listener, event) -> onSkinApplyEvent(event),
                    plugin,
                    true
            );
            registeredAsBukkit = true;
            return true;
        } catch (Throwable error) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "Failed to register SkinsRestorer event listener. Skin changes might not auto-refresh tablist.",
                    error
            );
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean subscribeApiEventBus() {
        if (skinsRestorerEventBus == null || registeredEventClass == null) {
            return false;
        }

        Consumer<Object> consumer = this::onSkinApplyEvent;
        for (Method method : skinsRestorerEventBus.getClass().getMethods()) {
            if (!method.getName().equals("subscribe")) {
                continue;
            }

            Object[] args = buildSubscribeArgs(method.getParameterTypes(), consumer);
            if (args == null) {
                continue;
            }

            try {
                method.setAccessible(true);
                method.invoke(skinsRestorerEventBus, args);
                return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private Object[] buildSubscribeArgs(Class<?>[] parameterTypes, Consumer<Object> consumer) {
        Object[] args = new Object[parameterTypes.length];
        boolean eventClassUsed = false;
        boolean consumerUsed = false;
        boolean ownerUsed = false;

        for (int index = 0; index < parameterTypes.length; index++) {
            Class<?> parameterType = parameterTypes[index];
            if (!eventClassUsed && Class.class.isAssignableFrom(parameterType)) {
                args[index] = registeredEventClass;
                eventClassUsed = true;
                continue;
            }
            if (!consumerUsed && Consumer.class.isAssignableFrom(parameterType)) {
                args[index] = consumer;
                consumerUsed = true;
                continue;
            }
            if (!ownerUsed && parameterType.isAssignableFrom(plugin.getClass())) {
                args[index] = plugin;
                ownerUsed = true;
                continue;
            }
            if (!ownerUsed && parameterType == Object.class) {
                args[index] = plugin;
                ownerUsed = true;
                continue;
            }
            return null;
        }

        return eventClassUsed && consumerUsed ? args : null;
    }

    private void onSkinApplyEvent(Object event) {
        if (isCancelled(event)) {
            return;
        }

        UUID playerId = extractPlayerUUID(event);
        if (playerId == null) {
            return;
        }

        Object property = extractSkinProperty(event);
        String value = readStringNoArg(property, "getValue", "value", "getTexture", "texture");
        String signature = readStringNoArg(property, "getSignature", "signature");
        boolean hasProperty = value != null && !value.isBlank();

        plugin.getFoliaScheduler().runGlobal(() -> {
            if (plugin.getTablistManager() == null) {
                return;
            }

            plugin.getTablistManager().invalidateSkinCache(playerId);
            if (hasProperty) {
                plugin.getTablistManager().updateSkinTexture(playerId, value, signature);
            }
            scheduleCascadeRefresh(playerId, !hasProperty);
        });
    }

    private void scheduleCascadeRefresh(UUID playerId, boolean forceTextureLookup) {
        cancelPendingRefreshes(playerId);

        List<ScheduledTask> tasks = new ArrayList<>(REFRESH_DELAY_TICKS.length);
        for (long delayTicks : REFRESH_DELAY_TICKS) {
            ScheduledTask task = plugin.getFoliaScheduler().runGlobalLater(
                    () -> refreshOnlinePlayer(playerId, forceTextureLookup),
                    delayTicks
            );
            tasks.add(task);
        }
        pendingRefreshes.put(playerId, tasks);
    }

    private void cancelPendingRefreshes(UUID playerId) {
        List<ScheduledTask> existing = pendingRefreshes.remove(playerId);
        if (existing == null) {
            return;
        }
        for (ScheduledTask task : existing) {
            if (task != null) {
                task.cancel();
            }
        }
    }

    private void refreshOnlinePlayer(UUID playerId, boolean forceTextureLookup) {
        pendingRefreshes.remove(playerId);

        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            plugin.getFoliaScheduler().runEntity(player, () -> refreshPlayer(player, forceTextureLookup));
        }
    }

    private void refreshPlayer(Player player, boolean forceTextureLookup) {
        if (player == null || !player.isOnline() || plugin.getTablistManager() == null) {
            return;
        }

        plugin.getTablistManager().refreshTablistEntry(player, forceTextureLookup);
    }

    private boolean isCancelled(Object event) {
        Object cancelled = invokeNoArgQuietly(event, "isCancelled", "cancelled");
        return Boolean.TRUE.equals(cancelled);
    }

    private Object extractSkinProperty(Object event) {
        for (String methodName : List.of("getProperty", "property", "getSkinProperty", "skinProperty")) {
            Object property = invokeNoArgQuietly(event, methodName);
            if (property != null) {
                return property;
            }
        }
        return null;
    }

    private UUID extractPlayerUUID(Object event) {
        if (event == null) {
            return null;
        }

        try {
            Method getPlayer = event.getClass().getMethod("getPlayer", Class.class);
            getPlayer.setAccessible(true);
            Object player = getPlayer.invoke(event, Player.class);
            if (player instanceof Player bukkitPlayer) {
                return bukkitPlayer.getUniqueId();
            }
        } catch (Throwable ignored) {
        }

        Object player = invokeNoArgQuietly(event, "getPlayer", "player");
        UUID playerId = extractUuid(player);
        if (playerId != null) {
            return playerId;
        }

        Object uuid = invokeNoArgQuietly(event, "getPlayerUUID", "getUniqueId", "uuid", "uniqueId");
        if (uuid instanceof UUID id) {
            return id;
        }

        Object name = invokeNoArgQuietly(event, "getPlayerName", "playerName", "name");
        if (name instanceof String playerName) {
            Player online = Bukkit.getPlayer(playerName);
            return online != null ? online.getUniqueId() : null;
        }

        return null;
    }

    private UUID extractUuid(Object value) {
        if (value instanceof Player player) {
            return player.getUniqueId();
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        Object uuid = invokeNoArgQuietly(value, "getUniqueId", "getUniqueID", "uniqueId", "uuid");
        return uuid instanceof UUID id ? id : null;
    }

    private String readStringNoArg(Object target, String... methodNames) {
        Object value = invokeNoArgQuietly(target, methodNames);
        return value instanceof String string ? string : null;
    }

    private Object invokeNoArgQuietly(Object target, String... methodNames) {
        if (target == null) {
            return null;
        }

        for (String methodName : methodNames) {
            try {
                Method method = findNoArgMethod(target.getClass(), methodName);
                if (method == null) {
                    continue;
                }
                method.setAccessible(true);
                return method.invoke(target);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private Method findNoArgMethod(Class<?> type, String methodName) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                    return method;
                }
            }
        }

        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                return method;
            }
        }
        return null;
    }

    private Class<?> findSkinsRestorerClass(String className) throws ClassNotFoundException {
        ClassNotFoundException missing = null;
        for (ClassLoader loader : skinsRestorerClassLoaders()) {
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException exception) {
                missing = exception;
            }
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw missing == null ? exception : missing;
        }
    }

    private List<ClassLoader> skinsRestorerClassLoaders() {
        List<ClassLoader> loaders = new ArrayList<>();
        addClassLoader(loaders, Thread.currentThread().getContextClassLoader());
        addClassLoader(loaders, getClass().getClassLoader());
        addClassLoader(loaders, Bukkit.class.getClassLoader());

        Plugin skinsRestorer = findSkinsRestorerPlugin();
        if (skinsRestorer != null) {
            addClassLoader(loaders, skinsRestorer.getClass().getClassLoader());
        }
        return loaders;
    }

    private Plugin findSkinsRestorerPlugin() {
        Plugin pluginByName = Bukkit.getPluginManager().getPlugin("SkinsRestorer");
        if (pluginByName != null) {
            return pluginByName;
        }

        for (Plugin candidate : Bukkit.getPluginManager().getPlugins()) {
            if (candidate != null && "SkinsRestorer".equalsIgnoreCase(candidate.getName())) {
                return candidate;
            }
        }
        return null;
    }

    private void addClassLoader(List<ClassLoader> loaders, ClassLoader loader) {
        if (loader != null && !loaders.contains(loader)) {
            loaders.add(loader);
        }
    }

    private void unregisterApiEventBus() {
        if (skinsRestorerEventBus == null || registeredEventClass == null) {
            return;
        }

        for (String methodName : List.of("unsubscribe", "unregister")) {
            for (Method method : skinsRestorerEventBus.getClass().getMethods()) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }

                try {
                    method.setAccessible(true);
                    if (method.getParameterCount() == 1) {
                        method.invoke(skinsRestorerEventBus, plugin);
                        return;
                    }
                    if (method.getParameterCount() == 2) {
                        method.invoke(skinsRestorerEventBus, plugin, registeredEventClass);
                        return;
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
