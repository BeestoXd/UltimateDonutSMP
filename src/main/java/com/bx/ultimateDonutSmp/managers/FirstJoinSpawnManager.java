package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drops brand new players at a random location the first time they join instead of
 * leaving them on the vanilla world spawn. The search reuses the RTP engine directly,
 * so first joins are never blocked by RTP cooldowns, playtime requirements, or the
 * RTP queue.
 */
public class FirstJoinSpawnManager {

    private final UltimateDonutSmp plugin;
    private final Set<UUID> pendingTeleports = ConcurrentHashMap.newKeySet();

    private boolean enabled;
    private boolean fallbackToSpawn;
    private String searchingMessage;
    private String successMessage;
    private String failedMessage;

    public FirstJoinSpawnManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reloadSettings();
    }

    public void reloadSettings() {
        enabled = plugin.getConfigManager().getConfig().getBoolean("FIRST-JOIN-RTP.ENABLED", false);
        fallbackToSpawn = plugin.getConfigManager().getConfig().getBoolean("FIRST-JOIN-RTP.FALLBACK-TO-SPAWN", true);
        searchingMessage = plugin.getConfigManager().getConfig().getString("FIRST-JOIN-RTP.SEARCHING-MESSAGE", "");
        successMessage = plugin.getConfigManager().getConfig().getString("FIRST-JOIN-RTP.SUCCESS-MESSAGE", "");
        failedMessage = plugin.getConfigManager().getConfig().getString("FIRST-JOIN-RTP.FAILED-MESSAGE", "");
        pendingTeleports.clear();
    }

    public boolean isEnabled() {
        return enabled
                && plugin.getRtpManager() != null
                && plugin.getRtpManager().isEnabled();
    }

    /**
     * Handles a player joining the server for the very first time.
     *
     * @return true when the random spawn search was started and the caller must not run
     *         the regular spawn teleport, false when the join should fall through to it
     */
    public boolean handleFirstJoin(Player player) {
        if (player == null || !isEnabled()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (!pendingTeleports.add(uuid)) {
            return true;
        }

        RTPManager.SearchSettings settings =
                plugin.getRtpManager().getFirstJoinSearchSettings(player.getWorld().getName());
        if (settings == null) {
            pendingTeleports.remove(uuid);
            return false;
        }

        sendMessage(player, searchingMessage, null);
        plugin.getRtpManager().findSafeLocationAsync(settings).whenComplete((destination, throwable) -> {
            pendingTeleports.remove(uuid);
            plugin.getSpigotScheduler().runEntity(player, () -> {
                if (!player.isOnline()) {
                    return;
                }
                if (throwable != null || destination == null) {
                    failFirstJoinTeleport(player);
                    return;
                }
                plugin.getSpigotScheduler().teleport(player, destination).thenAccept(success ->
                        plugin.getSpigotScheduler().runEntity(player, () -> {
                            if (!player.isOnline()) {
                                return;
                            }
                            if (!Boolean.TRUE.equals(success)) {
                                failFirstJoinTeleport(player);
                                return;
                            }
                            SoundUtils.play(player, plugin.getConfigManager().getSound("TELEPORT.SUCCESS"));
                            sendMessage(player, successMessage, destination);
                        }));
            });
        });
        return true;
    }

    private void failFirstJoinTeleport(Player player) {
        sendMessage(player, failedMessage, null);
        if (fallbackToSpawn) {
            teleportToSpawn(player);
        }
    }

    private void teleportToSpawn(Player player) {
        if (plugin.getSpawnManager() == null || !plugin.getSpawnManager().hasSpawn()) {
            return;
        }
        Location spawn = plugin.getSpawnManager().getSpawnLocation();
        if (spawn != null) {
            plugin.getSpigotScheduler().teleport(player, spawn);
        }
    }

    private void sendMessage(Player player, String message, Location location) {
        if (message == null || message.isBlank()) {
            return;
        }
        player.sendMessage(ColorUtils.toComponent(applyPlaceholders(message, location)));
    }

    static String applyPlaceholders(String message, Location location) {
        if (message == null || message.isBlank()) {
            return "";
        }
        if (location == null) {
            return message;
        }
        return message
                .replace("{world}", location.getWorld() == null ? "" : location.getWorld().getName())
                .replace("{x}", String.valueOf(location.getBlockX()))
                .replace("{y}", String.valueOf(location.getBlockY()))
                .replace("{z}", String.valueOf(location.getBlockZ()));
    }
}
