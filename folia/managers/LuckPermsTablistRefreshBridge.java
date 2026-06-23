package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class LuckPermsTablistRefreshBridge {

    /**
     * LuckPerms commands/events can arrive before Spigot's permission cache and
     * PlaceholderAPI output settle. Use one delayed refresh, not a recurring poll.
     */
    private static final long REFRESH_DELAY_TICKS = 10L;

    private final UltimateDonutSmp plugin;
    private final Map<UUID, ScheduledTask> pendingRefreshes = new ConcurrentHashMap<>();
    private final Map<UUID, String> permissionSnapshots = new ConcurrentHashMap<>();

    private Object recalculateSubscription;
    private Object nodeMutateSubscription;
    private Listener commandListener;
    private ScheduledTask pendingGlobalRefresh;

    public LuckPermsTablistRefreshBridge(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void start() {
        shutdown();
        try {
            if (findLuckPermsPlugin() == null) {
                return;
            }

            Class<?> providerClass = findLuckPermsClass("net.luckperms.api.LuckPermsProvider");
            Object luckPerms = providerClass.getMethod("get").invoke(null);

            Class<?> apiClass = findLuckPermsClass("net.luckperms.api.LuckPerms");
            Object eventBus = apiClass.getMethod("getEventBus").invoke(luckPerms);

            Class<?> eventBusClass = findLuckPermsClass("net.luckperms.api.event.EventBus");
            Method subscribe = eventBusClass.getMethod(
                    "subscribe",
                    Object.class,
                    Class.class,
                    Consumer.class
            );

            Class<?> recalculationEventClass = findLuckPermsClass("net.luckperms.api.event.user.UserDataRecalculateEvent");
            recalculateSubscription = subscribe.invoke(
                    eventBus,
                    plugin,
                    recalculationEventClass,
                    (Consumer<Object>) this::onUserDataRecalculate
            );

            try {
                Class<?> nodeMutateEventClass = findLuckPermsClass("net.luckperms.api.event.node.NodeMutateEvent");
                nodeMutateSubscription = subscribe.invoke(
                        eventBus,
                        plugin,
                        nodeMutateEventClass,
                        (Consumer<Object>) this::onNodeMutate
                );
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                nodeMutateSubscription = null;
            }

            registerCommandListener();
            plugin.getLogger().info("LuckPerms tablist refresh bridge enabled (Spigot one-shot refresh v4).");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            recalculateSubscription = null;
            nodeMutateSubscription = null;
            plugin.getLogger().log(
                    Level.WARNING,
                    "LuckPerms API is unavailable; tablist permission refresh bridge is disabled.",
                    exception
            );
        }
    }

    public void shutdown() {
        closeSubscription(recalculateSubscription);
        recalculateSubscription = null;
        closeSubscription(nodeMutateSubscription);
        nodeMutateSubscription = null;

        if (commandListener != null) {
            HandlerList.unregisterAll(commandListener);
            commandListener = null;
        }

        for (ScheduledTask task : pendingRefreshes.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        pendingRefreshes.clear();
        cancelPendingGlobalRefreshes();
        permissionSnapshots.clear();
    }

    private void closeSubscription(Object subscription) {
        if (subscription == null) {
            return;
        }

        try {
            Method close = AutoCloseable.class.getMethod("close");
            close.invoke(subscription);
        } catch (Throwable exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to close LuckPerms tablist refresh bridge subscription.", exception);
        }
    }

    private void onUserDataRecalculate(Object event) {
        UUID playerId = extractPlayerId(event);
        if (playerId != null) {
            scheduleCascadeRefresh(playerId, true);
        }
    }

    private void onNodeMutate(Object event) {
        UUID playerId = extractTargetUserId(event);
        if (playerId != null) {
            scheduleCascadeRefresh(playerId, true);
        } else if (isGroupTarget(event)) {
            scheduleGlobalRefresh(true);
        }
    }

    private void scheduleCascadeRefresh(UUID playerId, boolean fullEntryRefresh) {
        cancelPendingRefreshes(playerId);

        ScheduledTask task = plugin.getFoliaScheduler().runGlobalLater(
                () -> refreshOnlinePlayer(playerId, fullEntryRefresh),
                REFRESH_DELAY_TICKS
        );
        pendingRefreshes.put(playerId, task);
    }

    private void scheduleGlobalRefresh(boolean fullEntryRefresh) {
        cancelPendingGlobalRefreshes();
        pendingGlobalRefresh = plugin.getFoliaScheduler().runGlobalLater(
                () -> {
                    pendingGlobalRefresh = null;
                    refreshAllOnlinePlayers(fullEntryRefresh);
                },
                REFRESH_DELAY_TICKS
        );
    }

    private void cancelPendingGlobalRefreshes() {
        if (pendingGlobalRefresh != null) {
            pendingGlobalRefresh.cancel();
            pendingGlobalRefresh = null;
        }
    }

    private void cancelPendingRefreshes(UUID playerId) {
        ScheduledTask existing = pendingRefreshes.remove(playerId);
        if (existing != null) {
            existing.cancel();
        }
    }

    private UUID extractPlayerId(Object event) {
        try {
            ClassLoader classLoader = event.getClass().getClassLoader();
            Class<?> eventClass = Class.forName("net.luckperms.api.event.user.UserDataRecalculateEvent", false, classLoader);
            Object user = eventClass.getMethod("getUser").invoke(event);
            if (user == null) {
                return null;
            }
            Class<?> userClass = Class.forName("net.luckperms.api.model.user.User", false, classLoader);
            Object uniqueId = userClass.getMethod("getUniqueId").invoke(user);
            return uniqueId instanceof UUID uuid ? uuid : null;
        } catch (Throwable exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to read LuckPerms recalculation event.", exception);
            return null;
        }
    }

    private UUID extractTargetUserId(Object event) {
        try {
            ClassLoader classLoader = event.getClass().getClassLoader();
            Class<?> eventClass = Class.forName("net.luckperms.api.event.node.NodeMutateEvent", false, classLoader);
            Object target = eventClass.getMethod("getTarget").invoke(event);
            if (target == null) {
                return null;
            }

            Class<?> userClass = Class.forName("net.luckperms.api.model.user.User", false, classLoader);
            if (userClass.isInstance(target)) {
                Object uniqueId = userClass.getMethod("getUniqueId").invoke(target);
                return uniqueId instanceof UUID uuid ? uuid : null;
            }
        } catch (Throwable exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to read LuckPerms NodeMutateEvent target user.", exception);
        }
        return null;
    }

    private boolean isGroupTarget(Object event) {
        try {
            ClassLoader classLoader = event.getClass().getClassLoader();
            Class<?> eventClass = Class.forName("net.luckperms.api.event.node.NodeMutateEvent", false, classLoader);
            Object target = eventClass.getMethod("getTarget").invoke(event);
            if (target == null) {
                return false;
            }

            Class<?> groupClass = Class.forName("net.luckperms.api.model.group.Group", false, classLoader);
            return groupClass.isInstance(target);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void refreshOnlinePlayer(UUID playerId, boolean fullEntryRefresh) {
        pendingRefreshes.remove(playerId);

        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            if (fullEntryRefresh) {
                reloadLuckPermsUserThenRefresh(player, fullEntryRefresh);
            } else {
                plugin.getFoliaScheduler().runEntity(player, () -> refreshPlayer(player, false, false));
            }
        }
    }

    private void refreshPlayer(Player player, boolean refreshAllOnlinePlayers) {
        refreshPlayer(player, refreshAllOnlinePlayers, false);
    }

    private void refreshPlayer(Player player, boolean refreshAllOnlinePlayers, boolean fullEntryRefresh) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        String previousSnapshot = permissionSnapshots.get(playerId);

        if (plugin.getTablistManager() != null) {
            plugin.getTablistManager().invalidateLuckPermsCachedData(player);
        }
        player.recalculatePermissions();
        if (plugin.getHideManager() != null) {
            plugin.getHideManager().enforcePermission(
                    player,
                    message -> player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(message))
            );
        }

        try {
            player.updateCommands();
        } catch (RuntimeException | LinkageError ignored) {
        }

        if (plugin.getTablistManager() == null) {
            return;
        }

        String currentSnapshot = createPermissionSnapshot(player);
        if (fullEntryRefresh && previousSnapshot != null && previousSnapshot.equals(currentSnapshot)) {
            permissionSnapshots.put(playerId, currentSnapshot);
            return;
        }

        if (fullEntryRefresh) {
            plugin.getTablistManager().forceRefreshPermissionTablistEntry(player);
        } else {
            plugin.getTablistManager().updateTablistName(player);
            plugin.getTablistManager().update(player);
        }
        permissionSnapshots.put(playerId, currentSnapshot);
        if (refreshAllOnlinePlayers) {
            refreshAllOnlinePlayers(fullEntryRefresh);
        }
    }

    private void refreshAllOnlinePlayers() {
        refreshAllOnlinePlayers(false);
    }

    private void refreshAllOnlinePlayers(boolean fullEntryRefresh) {
        if (plugin.getTablistManager() == null) {
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.isOnline()) {
                if (fullEntryRefresh) {
                    reloadLuckPermsUserThenRefresh(online, true);
                    continue;
                }
                plugin.getTablistManager().invalidateLuckPermsCachedData(online);
                online.recalculatePermissions();
                plugin.getTablistManager().updateTablistName(online);
                plugin.getTablistManager().update(online);
                permissionSnapshots.put(online.getUniqueId(), createPermissionSnapshot(online));
            }
        }
    }

    private void reloadLuckPermsUserThenRefresh(Player player, boolean fullEntryRefresh) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        String playerName = player.getName();
        try {
            Class<?> providerClass = findLuckPermsClass("net.luckperms.api.LuckPermsProvider");
            Object luckPerms = providerClass.getMethod("get").invoke(null);
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object future = invokeCompatible(userManager, "loadUser", playerId, playerName);
            if (future == null) {
                future = invokeCompatible(userManager, "loadUser", playerId);
            }

            if (future instanceof CompletableFuture<?> completableFuture) {
                completableFuture.whenComplete((ignoredUser, ignoredError) -> plugin.getFoliaScheduler().runGlobalLater(
                        () -> refreshOnlinePlayerAfterReload(playerId, fullEntryRefresh),
                        1L
                ));
                return;
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
        }

        plugin.getFoliaScheduler().runEntity(player, () -> refreshPlayer(player, false, fullEntryRefresh));
    }

    private void refreshOnlinePlayerAfterReload(UUID playerId, boolean fullEntryRefresh) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }

        plugin.getFoliaScheduler().runEntity(player, () -> refreshPlayer(player, false, fullEntryRefresh));
    }

    private void registerCommandListener() {
        commandListener = new Listener() {
            @EventHandler
            public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
                if (event == null || event.isCancelled() || !isLuckPermsCommand(event.getMessage())) {
                    return;
                }
                scheduleCommandRefresh(event.getMessage());
            }

            @EventHandler
            public void onServerCommand(ServerCommandEvent event) {
                if (event == null || !isLuckPermsCommand(event.getCommand())) {
                    return;
                }
                scheduleCommandRefresh(event.getCommand());
            }
        };
        Bukkit.getPluginManager().registerEvents(commandListener, plugin);
    }

    private void scheduleCommandRefresh(String command) {
        Player target = findCommandTargetPlayer(command);
        if (target != null && isLikelyUserLuckPermsMutation(command)) {
            rememberCommandPermissionOverride(command, target);
            scheduleCascadeRefresh(target.getUniqueId(), true);
            return;
        }

        if (isLikelyGlobalLuckPermsMutation(command)) {
            scheduleGlobalRefresh(true);
        }
    }

    private boolean isLuckPermsCommand(String command) {
        String[] parts = splitCommandParts(command);
        if (parts.length == 0) {
            return false;
        }

        String root = parts[0].toLowerCase(java.util.Locale.ROOT);
        return root.equals("lp")
                || root.equals("luckperms")
                || root.equals("perm")
                || root.equals("perms")
                || root.equals("permission")
                || root.equals("permissions");
    }

    private void rememberCommandPermissionOverride(String command, Player target) {
        if (plugin.getTablistManager() == null || command == null || command.isBlank()) {
            return;
        }

        String[] parts = splitCommandParts(command);
        if (parts.length < 6 || !parts[1].equalsIgnoreCase("user")) {
            return;
        }

        String actionRoot = parts[3].toLowerCase(java.util.Locale.ROOT);
        if (!actionRoot.equals("permission") && !actionRoot.equals("perm") && !actionRoot.equals("permissions")) {
            return;
        }

        String operation = parts[4].toLowerCase(java.util.Locale.ROOT);
        String permission = parts[5];
        Boolean value = switch (operation) {
            case "set", "add", "settemp", "addtemp" -> parts.length <= 6 || !parts[6].equalsIgnoreCase("false");
            case "unset", "remove", "unsettemp", "removetemp" -> false;
            default -> null;
        };
        if (value == null || permission.isBlank()) {
            return;
        }

        if (target == null) {
            return;
        }

        plugin.getTablistManager().rememberLuckPermsPermissionOverride(target.getUniqueId(), permission, value);
        plugin.getLogger().fine("[Tablist] LuckPerms permission override applied for "
                + target.getName() + ": " + permission + "=" + value + ".");
    }

    private Player findCommandTargetPlayer(String command) {
        String[] parts = splitCommandParts(command);
        if (parts.length < 3 || !parts[1].equalsIgnoreCase("user")) {
            return null;
        }
        return findOnlinePlayer(parts[2]);
    }

    private boolean isLikelyUserLuckPermsMutation(String command) {
        String[] parts = splitCommandParts(command);
        if (parts.length < 4 || !parts[1].equalsIgnoreCase("user")) {
            return false;
        }

        String subjectAction = parts[3].toLowerCase(java.util.Locale.ROOT);
        if (subjectAction.equals("permission")
                || subjectAction.equals("perm")
                || subjectAction.equals("permissions")
                || subjectAction.equals("parent")
                || subjectAction.equals("meta")) {
            return containsMutationVerb(parts, 4);
        }

        return subjectAction.equals("clear")
                || subjectAction.equals("promote")
                || subjectAction.equals("demote")
                || subjectAction.equals("setprimarygroup")
                || subjectAction.equals("switchprimarygroup");
    }

    private boolean isLikelyGlobalLuckPermsMutation(String command) {
        String[] parts = splitCommandParts(command);
        if (parts.length < 2) {
            return false;
        }

        String subject = parts[1].toLowerCase(java.util.Locale.ROOT);
        if (!subject.equals("group") && !subject.equals("track")) {
            return false;
        }

        return containsMutationVerb(parts, 2);
    }

    private boolean containsMutationVerb(String[] parts, int startIndex) {
        for (int index = Math.max(0, startIndex); index < parts.length; index++) {
            if (isMutationVerb(parts[index])) {
                return true;
            }
        }
        return false;
    }

    private boolean isMutationVerb(String value) {
        if (value == null) {
            return false;
        }

        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "set", "unset", "add", "remove", "delete", "clear",
                    "settemp", "unsettemp", "addtemp", "removetemp",
                    "addprefix", "addsuffix", "removeprefix", "removesuffix",
                    "promote", "demote", "setprimarygroup", "switchprimarygroup",
                    "permission", "parent", "meta" -> true;
            default -> false;
        };
    }

    private String[] splitCommandParts(String command) {
        if (command == null || command.isBlank()) {
            return new String[0];
        }

        String normalizedCommand = command.strip();
        if (normalizedCommand.startsWith("/")) {
            normalizedCommand = normalizedCommand.substring(1);
        }

        String[] parts = normalizedCommand.split("\\s+");
        if (parts.length > 0) {
            int namespace = parts[0].indexOf(':');
            if (namespace >= 0 && namespace + 1 < parts[0].length()) {
                parts[0] = parts[0].substring(namespace + 1);
            }
        }
        return parts;
    }

    private Player findOnlinePlayer(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        Player exact = Bukkit.getPlayerExact(name);
        if (exact != null && exact.isOnline()) {
            return exact;
        }

        try {
            Player byId = Bukkit.getPlayer(UUID.fromString(name));
            if (byId != null && byId.isOnline()) {
                return byId;
            }
        } catch (IllegalArgumentException ignored) {
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && player.isOnline() && player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    private String createPermissionSnapshot(Player player) {
        if (player == null || plugin.getTablistManager() == null) {
            return "";
        }

        try {
            return plugin.getTablistManager().createPermissionRefreshSnapshot(player);
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private Class<?> findLuckPermsClass(String className) throws ClassNotFoundException {
        ClassNotFoundException missing = null;
        for (ClassLoader loader : luckPermsClassLoaders()) {
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

    private Object invokeCompatible(Object target, String methodName, Object... args) throws ReflectiveOperationException {
        if (target == null) {
            return null;
        }

        Method method = findCompatibleMethod(target, methodName, args);
        if (method == null) {
            return null;
        }

        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private Method findCompatibleMethod(Object target, String methodName, Object... args) {
        if (target == null) {
            return null;
        }

        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != args.length) {
                continue;
            }
            if (canAccept(method.getParameterTypes(), args)) {
                return method;
            }
        }

        for (Class<?> current = target.getClass(); current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != args.length) {
                    continue;
                }
                if (canAccept(method.getParameterTypes(), args)) {
                    return method;
                }
            }
        }
        return null;
    }

    private boolean canAccept(Class<?>[] parameterTypes, Object[] args) {
        for (int index = 0; index < parameterTypes.length; index++) {
            Object arg = args[index];
            if (arg == null) {
                continue;
            }

            Class<?> parameterType = wrapPrimitive(parameterTypes[index]);
            if (!parameterType.isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }

    private Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }

        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }

    private List<ClassLoader> luckPermsClassLoaders() {
        List<ClassLoader> loaders = new ArrayList<>();
        addClassLoader(loaders, Thread.currentThread().getContextClassLoader());
        addClassLoader(loaders, getClass().getClassLoader());
        addClassLoader(loaders, Bukkit.class.getClassLoader());

        Plugin luckPerms = findLuckPermsPlugin();
        if (luckPerms != null) {
            addClassLoader(loaders, luckPerms.getClass().getClassLoader());
        }
        return loaders;
    }

    private Plugin findLuckPermsPlugin() {
        Plugin pluginByName = Bukkit.getPluginManager().getPlugin("LuckPerms");
        if (pluginByName != null && pluginByName.isEnabled()) {
            return pluginByName;
        }

        for (Plugin candidate : Bukkit.getPluginManager().getPlugins()) {
            if (candidate != null && candidate.isEnabled() && "LuckPerms".equalsIgnoreCase(candidate.getName())) {
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
}
