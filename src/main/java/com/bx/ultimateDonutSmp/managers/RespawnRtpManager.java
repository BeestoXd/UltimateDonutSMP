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
 * Sends players back out into the world at a random location after they die, instead of
 * leaving them standing on spawn. The search reuses the RTP engine directly, so a respawn
 * is never blocked by RTP cooldowns, playtime requirements, or the RTP queue.
 *
 * <p>The teleport is started only once the player has already landed on the normal respawn
 * location, so a failed or slow search simply leaves them at spawn.
 */
public class RespawnRtpManager {

    private final UltimateDonutSmp plugin;
    private final Set<UUID> pendingTeleports = ConcurrentHashMap.newKeySet();

    private boolean enabled;
    private String searchingMessage;
    private String successMessage;
    private String failedMessage;

    public RespawnRtpManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reloadSettings();
    }

    public void reloadSettings() {
        enabled = plugin.getConfigManager().getConfig().getBoolean("RESPAWN-RTP.ENABLED", false);
        searchingMessage = plugin.getConfigManager().getConfig().getString("RESPAWN-RTP.SEARCHING-MESSAGE", "");
        successMessage = plugin.getConfigManager().getConfig().getString("RESPAWN-RTP.SUCCESS-MESSAGE", "");
        failedMessage = plugin.getConfigManager().getConfig().getString("RESPAWN-RTP.FAILED-MESSAGE", "");
        pendingTeleports.clear();
    }

    public boolean isEnabled() {
        return enabled
                && plugin.getRtpManager() != null
                && plugin.getRtpManager().isEnabled();
    }

    /**
     * Handles a player who has just respawned after dying.
     *
     * @param player          the player that respawned
     * @param deathWorldName  the world the player died in, used when no world is configured
     * @return true when the random teleport search was started
     */
    public boolean handleRespawn(Player player, String deathWorldName) {
        if (player == null || !isEnabled()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (!pendingTeleports.add(uuid)) {
            return true;
        }

        RTPManager.SearchSettings settings = plugin.getRtpManager().getRespawnSearchSettings(deathWorldName);
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
                    sendMessage(player, failedMessage, null);
                    return;
                }
                plugin.getSpigotScheduler().teleport(player, destination).thenAccept(success ->
                        plugin.getSpigotScheduler().runEntity(player, () -> {
                            if (!player.isOnline()) {
                                return;
                            }
                            if (!Boolean.TRUE.equals(success)) {
                                sendMessage(player, failedMessage, null);
                                return;
                            }
                            SoundUtils.play(player, plugin.getConfigManager().getSound("TELEPORT.SUCCESS"));
                            sendMessage(player, successMessage, destination);
                        }));
            });
        });
        return true;
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
