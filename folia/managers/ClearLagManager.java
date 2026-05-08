package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClearLagManager {

    private final UltimateDonutSmp plugin;
    private int countdown;
    private boolean running = false;

    public ClearLagManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.ENABLED", true);
    }

    public int getIntervalMinutes() {
        return plugin.getConfigManager().getConfig().getInt("CLEAR-LAG.EVERY", 5);
    }

    public boolean clearAnimals() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.ANIMALS", true);
    }

    public boolean clearMonsters() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.MONSTERS", true);
    }

    public boolean clearDroppedItems() {
        return plugin.getConfigManager().getConfig().getBoolean("CLEAR-LAG.DROPPED-ITEMS", true);
    }

    public List<String> getExcludedWorlds() {
        return plugin.getConfigManager().getConfig().getStringList("CLEAR-LAG.EXCLUDED-WORLDS");
    }

    public int getScanRadius() {
        return Math.max(16, plugin.getConfigManager().getConfig().getInt("CLEAR-LAG.SCAN-RADIUS", 96));
    }

    public CompletableFuture<Integer> clearEntities() {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        ClearRules rules = new ClearRules(
                List.copyOf(getExcludedWorlds()),
                clearAnimals(),
                clearMonsters(),
                clearDroppedItems(),
                getScanRadius()
        );

        plugin.getFoliaScheduler().runGlobal(() -> {
            Collection<? extends Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (onlinePlayers.isEmpty()) {
                result.complete(0);
                return;
            }

            Map<UUID, Entity> candidates = new ConcurrentHashMap<>();
            AtomicInteger pendingScans = new AtomicInteger(onlinePlayers.size());

            for (Player player : onlinePlayers) {
                if (plugin.getFoliaScheduler().runEntity(player, () -> {
                    try {
                        scanNearbyEntities(player, rules, candidates);
                    } finally {
                        if (pendingScans.decrementAndGet() == 0) {
                            removeCandidates(candidates.values(), rules, result);
                        }
                    }
                }) == null && pendingScans.decrementAndGet() == 0) {
                    removeCandidates(candidates.values(), rules, result);
                }
            }
        });

        return result;
    }

    public void broadcastCountdown(int seconds) {
        String msg = plugin.getConfigManager().getMessage("CLEAR-LAG.COUNTDOWN",
                "{seconds}", String.valueOf(seconds));
        broadcastToSubscribedPlayers(msg);
    }

    public void broadcastSuccess(int total) {
        String msg = plugin.getConfigManager().getMessage("CLEAR-LAG.SUCCESS",
                "{total}", String.valueOf(total));
        broadcastToSubscribedPlayers(msg);
    }

    private void broadcastToSubscribedPlayers(String message) {
        plugin.getFoliaScheduler().forEachOnlinePlayer(player -> {
            PlayerData data = plugin.getPlayerDataManager().get(player);
            if (data != null && !data.isClearEntitiesMessagesEnabled()) {
                return;
            }

            player.sendMessage(ColorUtils.toComponent(message));
        });
    }

    private void scanNearbyEntities(Player player, ClearRules rules, Map<UUID, Entity> candidates) {
        if (player == null || !player.isOnline() || player.getWorld() == null) {
            return;
        }
        if (rules.excludedWorlds().contains(player.getWorld().getName())) {
            return;
        }

        int radius = rules.scanRadius();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (shouldClear(entity, rules)) {
                candidates.putIfAbsent(entity.getUniqueId(), entity);
            }
        }
    }

    private void removeCandidates(
            Collection<Entity> candidates,
            ClearRules rules,
            CompletableFuture<Integer> result
    ) {
        if (candidates.isEmpty()) {
            result.complete(0);
            return;
        }

        AtomicInteger removed = new AtomicInteger();
        AtomicInteger pendingRemovals = new AtomicInteger(candidates.size());

        for (Entity entity : candidates) {
            if (plugin.getFoliaScheduler().runEntity(entity, () -> {
                try {
                    if (entity.isValid() && shouldClear(entity, rules)) {
                        entity.remove();
                        removed.incrementAndGet();
                    }
                } finally {
                    if (pendingRemovals.decrementAndGet() == 0) {
                        result.complete(removed.get());
                    }
                }
            }) == null && pendingRemovals.decrementAndGet() == 0) {
                result.complete(removed.get());
            }
        }
    }

    private boolean shouldClear(Entity entity, ClearRules rules) {
        if (entity == null || entity instanceof Player || entity.getWorld() == null) {
            return false;
        }
        if (rules.excludedWorlds().contains(entity.getWorld().getName())) {
            return false;
        }
        return (rules.clearDroppedItems() && entity instanceof Item)
                || (rules.clearAnimals() && entity instanceof Animals)
                || (rules.clearMonsters() && entity instanceof Monster);
    }

    private record ClearRules(
            List<String> excludedWorlds,
            boolean clearAnimals,
            boolean clearMonsters,
            boolean clearDroppedItems,
            int scanRadius
    ) {
    }
}
