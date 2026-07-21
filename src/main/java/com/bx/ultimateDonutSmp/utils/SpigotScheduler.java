package com.bx.ultimateDonutSmp.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class SpigotScheduler {

    private static final long MILLIS_PER_TICK = 50L;

    private final Plugin plugin;
    private final FoliaBridge foliaBridge;

    public SpigotScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.foliaBridge = FoliaBridge.create(plugin);
    }

    public boolean isFolia() {
        return foliaBridge.available();
    }

    public BukkitTask runGlobal(Runnable runnable) {
        if (isFolia()) {
            return foliaBridge.runGlobal(runnable);
        }
        return Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public BukkitTask runGlobalLater(Runnable runnable, long delayTicks) {
        if (delayTicks <= 0L) {
            return runGlobal(runnable);
        }
        if (isFolia()) {
            return foliaBridge.runGlobalLater(runnable, delayTicks);
        }
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }

    public BukkitTask runGlobalTimer(Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (isFolia()) {
            return foliaBridge.runGlobalTimer(runnable, initialDelayTicks, periodTicks);
        }
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, Math.max(1L, initialDelayTicks), Math.max(1L, periodTicks));
    }

    public BukkitTask runAsync(Runnable runnable) {
        if (isFolia()) {
            return foliaBridge.runAsync(runnable);
        }
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public BukkitTask runAsyncLater(Runnable runnable, long delayTicks) {
        if (isFolia()) {
            return foliaBridge.runAsyncLater(runnable, delayTicks);
        }
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, Math.max(0L, delayTicks));
    }

    public BukkitTask runAsyncTimer(Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (isFolia()) {
            return foliaBridge.runAsyncTimer(runnable, initialDelayTicks, periodTicks);
        }
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, Math.max(1L, initialDelayTicks), Math.max(1L, periodTicks));
    }

    public BukkitTask runEntity(Entity entity, Runnable runnable) {
        if (entity == null) {
            return null;
        }
        if (isFolia()) {
            return foliaBridge.runEntity(entity, runnable);
        }
        return runGlobal(runnable);
    }

    public BukkitTask runEntityLater(Entity entity, Runnable runnable, long delayTicks) {
        if (entity == null) {
            return null;
        }
        if (delayTicks <= 0L) {
            return runEntity(entity, runnable);
        }
        if (isFolia()) {
            return foliaBridge.runEntityLater(entity, runnable, delayTicks);
        }
        return runGlobalLater(runnable, delayTicks);
    }

    public BukkitTask runEntityTimer(Entity entity, Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (entity == null) {
            return null;
        }
        if (isFolia()) {
            return foliaBridge.runEntityTimer(entity, runnable, initialDelayTicks, periodTicks);
        }
        return runGlobalTimer(runnable, initialDelayTicks, periodTicks);
    }

    public BukkitTask runRegion(Location location, Runnable runnable) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        if (isFolia()) {
            return foliaBridge.runRegion(location, runnable);
        }
        return runGlobal(runnable);
    }

    public BukkitTask runRegionLater(Location location, Runnable runnable, long delayTicks) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        if (delayTicks <= 0L) {
            return runRegion(location, runnable);
        }
        if (isFolia()) {
            return foliaBridge.runRegionLater(location, runnable, delayTicks);
        }
        return runGlobalLater(runnable, delayTicks);
    }

    public BukkitTask runRegion(World world, int chunkX, int chunkZ, Runnable runnable) {
        if (world == null) {
            return null;
        }
        if (isFolia()) {
            return foliaBridge.runRegion(world, chunkX, chunkZ, runnable);
        }
        return runGlobal(runnable);
    }

    public void forEachOnlinePlayer(Consumer<Player> consumer) {
        Runnable iteration = () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                runEntity(player, () -> {
                    if (player.isOnline()) {
                        consumer.accept(player);
                    }
                });
            }
        };

        if (isFolia()) {
            runGlobal(iteration);
            return;
        }

        iteration.run();
    }

    public CompletableFuture<Boolean> teleport(Entity entity, Location location) {
        return teleport(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @SuppressWarnings("unchecked")
    public CompletableFuture<Boolean> teleport(
            Entity entity,
            Location location,
            PlayerTeleportEvent.TeleportCause cause
    ) {
        if (entity == null || location == null || location.getWorld() == null) {
            return CompletableFuture.completedFuture(false);
        }
        if (isFolia()) {
            return foliaBridge.teleport(entity, location, cause);
        }
        try {
            Class<?> flagClass = Class.forName("io.papermc.paper.entity.TeleportFlag");
            Class<?> flagArrayClass = java.lang.reflect.Array.newInstance(flagClass, 0).getClass();
            Method method = entity.getClass().getMethod("teleportAsync", Location.class, PlayerTeleportEvent.TeleportCause.class, flagArrayClass);
            if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
                Object emptyFlags = java.lang.reflect.Array.newInstance(flagClass, 0);
                return (CompletableFuture<Boolean>) method.invoke(entity, location, cause, emptyFlags);
            }
        } catch (Exception ignored) {
        }
        try {
            Class<?> flagClass = Class.forName("io.papermc.paper.entity.TeleportFlag");
            Class<?> flagArrayClass = java.lang.reflect.Array.newInstance(flagClass, 0).getClass();
            Method method = entity.getClass().getMethod("teleportAsync", Location.class, flagArrayClass);
            if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
                Object emptyFlags = java.lang.reflect.Array.newInstance(flagClass, 0);
                return (CompletableFuture<Boolean>) method.invoke(entity, location, emptyFlags);
            }
        } catch (Exception ignored) {
        }
        try {
            Method method = entity.getClass().getMethod("teleportAsync", Location.class, PlayerTeleportEvent.TeleportCause.class);
            if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
                return (CompletableFuture<Boolean>) method.invoke(entity, location, cause);
            }
        } catch (Exception ignored) {
        }
        try {
            Method method = entity.getClass().getMethod("teleportAsync", Location.class);
            if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
                return (CompletableFuture<Boolean>) method.invoke(entity, location);
            }
        } catch (Exception ignored) {
        }
        return CompletableFuture.completedFuture(entity.teleport(location, cause));
    }

    public boolean isOwnedByCurrentRegion(Entity entity) {
        return !isFolia() || foliaBridge.isOwnedByCurrentRegion(entity);
    }

    public boolean isOwnedByCurrentRegion(Location location) {
        return !isFolia() || foliaBridge.isOwnedByCurrentRegion(location);
    }

    private static long ticksToMillis(long ticks) {
        return Math.max(0L, ticks) * MILLIS_PER_TICK;
    }

    private static final class FoliaBridge {

        private static final AtomicInteger TASK_IDS = new AtomicInteger(-1);

        private final Plugin plugin;
        private final boolean available;
        private final Object globalScheduler;
        private final Object asyncScheduler;
        private final Object regionScheduler;
        private final Method globalRun;
        private final Method globalRunDelayed;
        private final Method globalRunAtFixedRate;
        private final Method asyncRunNow;
        private final Method asyncRunDelayed;
        private final Method asyncRunAtFixedRate;
        private final Method regionRunLocation;
        private final Method regionRunDelayedLocation;
        private final Method regionRunChunk;
        private final Method entityGetScheduler;
        private final Method entityRun;
        private final Method entityRunDelayed;
        private final Method entityRunAtFixedRate;
        private final Method entityTeleportAsync;
        private final Method ownedEntity;
        private final Method ownedLocation;

        private FoliaBridge(
                Plugin plugin,
                boolean available,
                Object globalScheduler,
                Object asyncScheduler,
                Object regionScheduler,
                Method globalRun,
                Method globalRunDelayed,
                Method globalRunAtFixedRate,
                Method asyncRunNow,
                Method asyncRunDelayed,
                Method asyncRunAtFixedRate,
                Method regionRunLocation,
                Method regionRunDelayedLocation,
                Method regionRunChunk,
                Method entityGetScheduler,
                Method entityRun,
                Method entityRunDelayed,
                Method entityRunAtFixedRate,
                Method entityTeleportAsync,
                Method ownedEntity,
                Method ownedLocation
        ) {
            this.plugin = plugin;
            this.available = available;
            this.globalScheduler = globalScheduler;
            this.asyncScheduler = asyncScheduler;
            this.regionScheduler = regionScheduler;
            this.globalRun = globalRun;
            this.globalRunDelayed = globalRunDelayed;
            this.globalRunAtFixedRate = globalRunAtFixedRate;
            this.asyncRunNow = asyncRunNow;
            this.asyncRunDelayed = asyncRunDelayed;
            this.asyncRunAtFixedRate = asyncRunAtFixedRate;
            this.regionRunLocation = regionRunLocation;
            this.regionRunDelayedLocation = regionRunDelayedLocation;
            this.regionRunChunk = regionRunChunk;
            this.entityGetScheduler = entityGetScheduler;
            this.entityRun = entityRun;
            this.entityRunDelayed = entityRunDelayed;
            this.entityRunAtFixedRate = entityRunAtFixedRate;
            this.entityTeleportAsync = entityTeleportAsync;
            this.ownedEntity = ownedEntity;
            this.ownedLocation = ownedLocation;
        }

        static FoliaBridge create(Plugin plugin) {
            if (!looksLikeFolia()) {
                return disabled(plugin);
            }

            try {
                Method getGlobalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler");
                Method getAsyncScheduler = Bukkit.class.getMethod("getAsyncScheduler");
                Method getRegionScheduler = Bukkit.class.getMethod("getRegionScheduler");

                Object globalScheduler = getGlobalRegionScheduler.invoke(null);
                Object asyncScheduler = getAsyncScheduler.invoke(null);
                Object regionScheduler = getRegionScheduler.invoke(null);

                ClassLoader classLoader = Bukkit.class.getClassLoader();
                Class<?> globalSchedulerType = Class.forName(
                        "io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler",
                        false,
                        classLoader
                );
                Class<?> asyncSchedulerType = Class.forName(
                        "io.papermc.paper.threadedregions.scheduler.AsyncScheduler",
                        false,
                        classLoader
                );
                Class<?> regionSchedulerType = Class.forName(
                        "io.papermc.paper.threadedregions.scheduler.RegionScheduler",
                        false,
                        classLoader
                );
                Class<?> entitySchedulerType = Class.forName(
                        "io.papermc.paper.threadedregions.scheduler.EntityScheduler",
                        false,
                        classLoader
                );

                Method entityGetScheduler = Entity.class.getMethod("getScheduler");
                Method entityTeleportAsync = Entity.class.getMethod(
                        "teleportAsync",
                        Location.class,
                        PlayerTeleportEvent.TeleportCause.class
                );

                return new FoliaBridge(
                        plugin,
                        true,
                        globalScheduler,
                        asyncScheduler,
                        regionScheduler,
                        globalSchedulerType.getMethod("run", Plugin.class, Consumer.class),
                        globalSchedulerType.getMethod("runDelayed", Plugin.class, Consumer.class, long.class),
                        globalSchedulerType.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class),
                        asyncSchedulerType.getMethod("runNow", Plugin.class, Consumer.class),
                        asyncSchedulerType.getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class),
                        asyncSchedulerType.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class),
                        regionSchedulerType.getMethod("run", Plugin.class, Location.class, Consumer.class),
                        regionSchedulerType.getMethod("runDelayed", Plugin.class, Location.class, Consumer.class, long.class),
                        regionSchedulerType.getMethod("run", Plugin.class, World.class, int.class, int.class, Consumer.class),
                        entityGetScheduler,
                        entitySchedulerType.getMethod("run", Plugin.class, Consumer.class, Runnable.class),
                        entitySchedulerType.getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class),
                        entitySchedulerType.getMethod("runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class),
                        entityTeleportAsync,
                        Bukkit.class.getMethod("isOwnedByCurrentRegion", Entity.class),
                        Bukkit.class.getMethod("isOwnedByCurrentRegion", Location.class)
                );
            } catch (ReflectiveOperationException | RuntimeException exception) {
                plugin.getLogger().log(Level.SEVERE, "Folia was detected but the Folia scheduler API could not be initialized.", exception);
                return disabled(plugin);
            }
        }

        boolean available() {
            return available;
        }

        BukkitTask runGlobal(Runnable runnable) {
            try {
                return wrap(globalRun.invoke(globalScheduler, plugin, taskConsumer(runnable)), true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("global task", exception);
            }
        }

        BukkitTask runGlobalLater(Runnable runnable, long delayTicks) {
            try {
                return wrap(globalRunDelayed.invoke(globalScheduler, plugin, taskConsumer(runnable), Math.max(1L, delayTicks)), true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("delayed global task", exception);
            }
        }

        BukkitTask runGlobalTimer(Runnable runnable, long initialDelayTicks, long periodTicks) {
            try {
                return wrap(globalRunAtFixedRate.invoke(
                        globalScheduler,
                        plugin,
                        taskConsumer(runnable),
                        Math.max(1L, initialDelayTicks),
                        Math.max(1L, periodTicks)
                ), true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("global timer", exception);
            }
        }

        BukkitTask runAsync(Runnable runnable) {
            try {
                return wrap(asyncRunNow.invoke(asyncScheduler, plugin, taskConsumer(runnable)), false);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("async task", exception);
            }
        }

        BukkitTask runAsyncLater(Runnable runnable, long delayTicks) {
            try {
                return wrap(asyncRunDelayed.invoke(
                        asyncScheduler,
                        plugin,
                        taskConsumer(runnable),
                        ticksToMillis(delayTicks),
                        TimeUnit.MILLISECONDS
                ), false);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("delayed async task", exception);
            }
        }

        BukkitTask runAsyncTimer(Runnable runnable, long initialDelayTicks, long periodTicks) {
            try {
                return wrap(asyncRunAtFixedRate.invoke(
                        asyncScheduler,
                        plugin,
                        taskConsumer(runnable),
                        ticksToMillis(initialDelayTicks),
                        Math.max(1L, ticksToMillis(periodTicks)),
                        TimeUnit.MILLISECONDS
                ), false);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("async timer", exception);
            }
        }

        BukkitTask runEntity(Entity entity, Runnable runnable) {
            try {
                Object scheduler = entityGetScheduler.invoke(entity);
                return wrap(entityRun.invoke(scheduler, plugin, taskConsumer(runnable), null), true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("entity task", exception);
            }
        }

        BukkitTask runEntityLater(Entity entity, Runnable runnable, long delayTicks) {
            try {
                Object scheduler = entityGetScheduler.invoke(entity);
                return wrap(entityRunDelayed.invoke(scheduler, plugin, taskConsumer(runnable), null, Math.max(1L, delayTicks)), true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("delayed entity task", exception);
            }
        }

        BukkitTask runEntityTimer(Entity entity, Runnable runnable, long initialDelayTicks, long periodTicks) {
            try {
                Object scheduler = entityGetScheduler.invoke(entity);
                return wrap(entityRunAtFixedRate.invoke(
                        scheduler,
                        plugin,
                        taskConsumer(runnable),
                        null,
                        Math.max(1L, initialDelayTicks),
                        Math.max(1L, periodTicks)
                ), true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("entity timer", exception);
            }
        }

        BukkitTask runRegion(Location location, Runnable runnable) {
            try {
                return wrap(regionRunLocation.invoke(regionScheduler, plugin, location, taskConsumer(runnable)), true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("region task", exception);
            }
        }

        BukkitTask runRegionLater(Location location, Runnable runnable, long delayTicks) {
            try {
                return wrap(regionRunDelayedLocation.invoke(regionScheduler, plugin, location, taskConsumer(runnable), Math.max(1L, delayTicks)), true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("delayed region task", exception);
            }
        }

        BukkitTask runRegion(World world, int chunkX, int chunkZ, Runnable runnable) {
            try {
                return wrap(regionRunChunk.invoke(regionScheduler, plugin, world, chunkX, chunkZ, taskConsumer(runnable)), true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return failed("chunk region task", exception);
            }
        }

        @SuppressWarnings("unchecked")
        CompletableFuture<Boolean> teleport(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
            try {
                return (CompletableFuture<Boolean>) entityTeleportAsync.invoke(entity, location, cause);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                plugin.getLogger().log(Level.SEVERE, "Failed to teleport entity through Folia teleportAsync.", exception);
                return CompletableFuture.completedFuture(false);
            }
        }

        boolean isOwnedByCurrentRegion(Entity entity) {
            if (entity == null) {
                return false;
            }
            try {
                return Boolean.TRUE.equals(ownedEntity.invoke(null, entity));
            } catch (ReflectiveOperationException | RuntimeException exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to query Folia entity region ownership.", exception);
                return false;
            }
        }

        boolean isOwnedByCurrentRegion(Location location) {
            if (location == null || location.getWorld() == null) {
                return false;
            }
            try {
                return Boolean.TRUE.equals(ownedLocation.invoke(null, location));
            } catch (ReflectiveOperationException | RuntimeException exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to query Folia location region ownership.", exception);
                return false;
            }
        }

        private Consumer<Object> taskConsumer(Runnable runnable) {
            return ignored -> runnable.run();
        }

        private BukkitTask wrap(Object task, boolean sync) {
            if (task == null) {
                return new NoopTask(plugin, sync);
            }
            return new FoliaBukkitTask(plugin, task, TASK_IDS.getAndDecrement(), sync);
        }

        private BukkitTask failed(String action, Exception exception) {
            Throwable root = exception;
            if (exception instanceof java.lang.reflect.InvocationTargetException && exception.getCause() != null) {
                root = exception.getCause();
            }
            if (root.getClass().getName().contains("IllegalPluginAccessException") || !plugin.isEnabled()) {
                plugin.getLogger().log(Level.FINE, "Could not schedule Folia " + action + " because the plugin is disabled or disabling.");
                return new NoopTask(plugin, true);
            }
            plugin.getLogger().log(Level.SEVERE, "Failed to schedule Folia " + action + ".", exception);
            return new NoopTask(plugin, true);
        }

        private static boolean looksLikeFolia() {
            try {
                if ("Folia".equalsIgnoreCase(Bukkit.getServer().getName())) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                return false;
            }

            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer", false, Bukkit.class.getClassLoader());
                return true;
            } catch (ClassNotFoundException ignored) {
                return false;
            }
        }

        private static FoliaBridge disabled(Plugin plugin) {
            return new FoliaBridge(
                    plugin,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    private static final class FoliaBukkitTask implements BukkitTask {

        private final Plugin plugin;
        private final Object task;
        private final int taskId;
        private final boolean sync;
        private volatile boolean cancelled;

        private FoliaBukkitTask(Plugin plugin, Object task, int taskId, boolean sync) {
            this.plugin = plugin;
            this.task = task;
            this.taskId = taskId;
            this.sync = sync;
        }

        @Override
        public int getTaskId() {
            return taskId;
        }

        @Override
        public Plugin getOwner() {
            return plugin;
        }

        @Override
        public boolean isSync() {
            return sync;
        }

        @Override
        public boolean isCancelled() {
            if (cancelled) {
                return true;
            }
            try {
                Method isCancelled = scheduledTaskMethod("isCancelled");
                return Boolean.TRUE.equals(isCancelled.invoke(task));
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                return false;
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
            try {
                Method cancel = scheduledTaskMethod("cancel");
                cancel.invoke(task);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to cancel Folia task " + taskId + ".", exception);
            }
        }

        private Method scheduledTaskMethod(String name) throws ReflectiveOperationException {
            Class<?> scheduledTaskType = Class.forName(
                    "io.papermc.paper.threadedregions.scheduler.ScheduledTask",
                    false,
                    Bukkit.class.getClassLoader()
            );
            return scheduledTaskType.getMethod(name);
        }
    }

    private static final class NoopTask implements BukkitTask {

        private static final AtomicInteger TASK_IDS = new AtomicInteger(Integer.MIN_VALUE);

        private final Plugin plugin;
        private final int taskId = TASK_IDS.getAndIncrement();
        private final boolean sync;

        private NoopTask(Plugin plugin, boolean sync) {
            this.plugin = plugin;
            this.sync = sync;
        }

        @Override
        public int getTaskId() {
            return taskId;
        }

        @Override
        public Plugin getOwner() {
            return plugin;
        }

        @Override
        public boolean isSync() {
            return sync;
        }

        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public void cancel() {
        }
    }
}
