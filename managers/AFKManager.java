package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AFKManager {

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Long> lastMovement = new HashMap<>();
    private final Set<UUID> afkPlayers = new HashSet<>();

    public AFKManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getConfig()
                .getBoolean("AFK-SYSTEM.ENABLED", true);
    }

    public int getTimeoutSeconds() {
        return plugin.getConfigManager().getConfig()
                .getInt("AFK-SYSTEM.TIME", 180);
    }

    public String getSpawnCuboidName() {
        return plugin.getConfigManager().getConfig()
                .getString("AFK-SYSTEM.SPAWN-CUBOID-NAME", "spawn");
    }

    public Set<String> getTrackedSpawnCuboidNames() {
        LinkedHashSet<String> cuboidNames = new LinkedHashSet<>(
                plugin.getSpawnManager().getAreaCuboidNames(SpawnManager.AreaType.SPAWN)
        );
        if (!cuboidNames.isEmpty()) {
            return cuboidNames;
        }

        String legacy = getSpawnCuboidName();
        if (legacy != null && !legacy.isBlank()) {
            cuboidNames.add(legacy.toLowerCase());
        }
        return cuboidNames;
    }

    public void recordMovement(UUID uuid) {
        lastMovement.put(uuid, System.currentTimeMillis());
        afkPlayers.remove(uuid);
    }

    public void trackPlayer(Player player) {
        lastMovement.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void removePlayer(UUID uuid) {
        lastMovement.remove(uuid);
        afkPlayers.remove(uuid);
    }

    public boolean isAfk(UUID uuid) {
        return afkPlayers.contains(uuid);
    }

    public boolean shouldGoAfk(UUID uuid) {
        return shouldGoAfk(uuid, getTimeoutSeconds());
    }

    public boolean shouldGoAfk(UUID uuid, int timeoutSeconds) {
        Long last = lastMovement.get(uuid);
        if (last == null) {
            return false;
        }
        return (System.currentTimeMillis() - last) >= (timeoutSeconds * 1000L);
    }

    public boolean hasRecentMovement(UUID uuid, int windowSeconds) {
        if (windowSeconds <= 0) {
            return true;
        }

        Long last = lastMovement.get(uuid);
        if (last == null) {
            return false;
        }
        return (System.currentTimeMillis() - last) <= (windowSeconds * 1000L);
    }

    public long getSecondsSinceLastMovement(UUID uuid) {
        Long last = lastMovement.get(uuid);
        if (last == null) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, (System.currentTimeMillis() - last) / 1000L);
    }

    public void sendToAfk(Player player) {
        sendToAfk(player, resolveAutomaticAfkLocation(), plugin.getConfigManager().getConfig()
                .getString("AFK-SYSTEM.MESSAGE",
                        "&7You have been moved to the AFK area for being inactive in the spawn."));
    }

    public Location resolveAutomaticAfkLocation() {
        Location areaDestination = plugin.getSpawnManager().getRandomAreaDestination(SpawnManager.AreaType.AFK);
        if (areaDestination != null) {
            return areaDestination;
        }
        return plugin.getSpawnManager().getAfkLocation();
    }

    public void sendToAfk(Player player, Location afkLoc, String message) {
        afkPlayers.add(player.getUniqueId());
        if (afkLoc != null) {
            plugin.getPaperScheduler().teleport(player, afkLoc).thenRun(() ->
                    plugin.getPaperScheduler().runEntity(player, () -> sendAfkMessage(player, message)));
            return;
        }
        sendAfkMessage(player, message);
    }

    private void sendAfkMessage(Player player, String message) {
        if (message != null && !message.isBlank()) {
            player.sendMessage(ColorUtils.toComponent(message));
        }
    }

    public boolean isInSpawnCuboid(Player player) {
        Set<String> cuboidNames = getTrackedSpawnCuboidNames();
        if (cuboidNames.isEmpty()) {
            return false;
        }
        return plugin.getCuboidManager().isInAnyCuboid(player, cuboidNames.toArray(String[]::new));
    }
}
