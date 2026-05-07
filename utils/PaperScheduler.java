package com.bx.ultimateDonutSmp.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class PaperScheduler {

    private static final long MILLIS_PER_TICK = 50L;

    private final Plugin plugin;

    public PaperScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    public ScheduledTask runGlobal(Runnable runnable) {
        return Bukkit.getGlobalRegionScheduler().run(plugin, task -> runnable.run());
    }

    public ScheduledTask runGlobalLater(Runnable runnable, long delayTicks) {
        if (delayTicks <= 0L) {
            return runGlobal(runnable);
        }
        return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), delayTicks);
    }

    public ScheduledTask runGlobalTimer(Runnable runnable, long initialDelayTicks, long periodTicks) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                plugin,
                task -> runnable.run(),
                Math.max(1L, initialDelayTicks),
                Math.max(1L, periodTicks)
        );
    }

    public ScheduledTask runAsync(Runnable runnable) {
        return Bukkit.getAsyncScheduler().runNow(plugin, task -> runnable.run());
    }

    public ScheduledTask runAsyncLater(Runnable runnable, long delayTicks) {
        return Bukkit.getAsyncScheduler().runDelayed(
                plugin,
                task -> runnable.run(),
                ticksToMillis(delayTicks),
                TimeUnit.MILLISECONDS
        );
    }

    public ScheduledTask runAsyncTimer(Runnable runnable, long initialDelayTicks, long periodTicks) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                task -> runnable.run(),
                ticksToMillis(initialDelayTicks),
                Math.max(1L, ticksToMillis(periodTicks)),
                TimeUnit.MILLISECONDS
        );
    }

    public ScheduledTask runEntity(Entity entity, Runnable runnable) {
        if (entity == null) {
            return null;
        }
        return entity.getScheduler().run(plugin, task -> runnable.run(), null);
    }

    public ScheduledTask runEntityLater(Entity entity, Runnable runnable, long delayTicks) {
        if (entity == null) {
            return null;
        }
        if (delayTicks <= 0L) {
            return runEntity(entity, runnable);
        }
        return entity.getScheduler().runDelayed(plugin, task -> runnable.run(), null, delayTicks);
    }

    public ScheduledTask runEntityTimer(Entity entity, Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (entity == null) {
            return null;
        }
        return entity.getScheduler().runAtFixedRate(
                plugin,
                task -> runnable.run(),
                null,
                Math.max(1L, initialDelayTicks),
                Math.max(1L, periodTicks)
        );
    }

    public ScheduledTask runRegion(Location location, Runnable runnable) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return Bukkit.getRegionScheduler().run(plugin, location, task -> runnable.run());
    }

    public ScheduledTask runRegionLater(Location location, Runnable runnable, long delayTicks) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        if (delayTicks <= 0L) {
            return runRegion(location, runnable);
        }
        return Bukkit.getRegionScheduler().runDelayed(plugin, location, task -> runnable.run(), delayTicks);
    }

    public ScheduledTask runRegion(World world, int chunkX, int chunkZ, Runnable runnable) {
        if (world == null) {
            return null;
        }
        return Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, task -> runnable.run());
    }

    public void forEachOnlinePlayer(Consumer<Player> consumer) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            runEntity(player, () -> {
                if (player.isOnline()) {
                    consumer.accept(player);
                }
            });
        }
    }

    public CompletableFuture<Boolean> teleport(Entity entity, Location location) {
        return teleport(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public CompletableFuture<Boolean> teleport(
            Entity entity,
            Location location,
            PlayerTeleportEvent.TeleportCause cause
    ) {
        if (entity == null || location == null || location.getWorld() == null) {
            return CompletableFuture.completedFuture(false);
        }
        return entity.teleportAsync(location, cause);
    }

    private long ticksToMillis(long ticks) {
        return Math.max(0L, ticks) * MILLIS_PER_TICK;
    }
}
