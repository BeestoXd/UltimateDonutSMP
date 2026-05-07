package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TeleportManager {

    private final UltimateDonutSmp plugin;
    private final Map<UUID, ScheduledTask> pendingTasks = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingTypes = new ConcurrentHashMap<>();
    private final Map<UUID, Location> startLocations = new ConcurrentHashMap<>();

    public TeleportManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void queue(Player player, Location destination, String type,
                      Consumer<Player> onSuccess) {
        int cooldownSecs = getCooldown(type);
        String normalizedType = normalizeType(type);
        cancel(player.getUniqueId());

        if (cooldownSecs <= 0) {
            teleportNow(player, destination, onSuccess);
            return;
        }

        startLocations.put(player.getUniqueId(), player.getLocation().clone());

        sendCountdownFeedback(player, cooldownSecs);
        if (!"RTP".equals(normalizedType)) {
            sendMovementWarning(player, cooldownSecs);
        }

        final int[] remaining = {cooldownSecs};
        ScheduledTask task = plugin.getPaperScheduler().runEntityTimer(player, () -> {
            remaining[0]--;

            if (!player.isOnline()) {
                cancel(player.getUniqueId());
                return;
            }

            if (remaining[0] <= 0) {
                cancel(player.getUniqueId());
                PlayerSettingUtils.clearActionBar(player);
                teleportNow(player, destination, onSuccess);
            } else {
                sendCountdownFeedback(player, remaining[0]);
            }
        }, 20L, 20L);

        if (task != null) {
            pendingTasks.put(player.getUniqueId(), task);
        }
        pendingTypes.put(player.getUniqueId(), normalizedType);
    }

    private void sendActionBar(Player player, int seconds) {
        String template = plugin.getConfigManager().getMessage("TELEPORT.COUNTDOWN",
                "{seconds}", String.valueOf(seconds));
        PlayerSettingUtils.sendActionBar(plugin, player, ColorUtils.toComponent(template));
    }

    private void sendCountdownFeedback(Player player, int seconds) {
        sendActionBar(player, seconds);
        SoundUtils.play(player, plugin.getConfigManager().getSound("TELEPORT.COUNTDOWN"));
    }

    private void sendMovementWarning(Player player, int seconds) {
        String warning = plugin.getConfigManager().getMessage(
                "TELEPORT.WARNING",
                "{seconds}", String.valueOf(seconds)
        );
        if (warning == null || warning.isBlank()) {
            return;
        }
        player.sendMessage(ColorUtils.toComponent(warning));
    }

    private void teleportNow(Player player, Location destination, Consumer<Player> onSuccess) {
        plugin.getPaperScheduler().teleport(player, destination).whenComplete((success, throwable) ->
                plugin.getPaperScheduler().runEntity(player, () -> {
                    if (!player.isOnline()) {
                        return;
                    }
                    if (throwable != null || !Boolean.TRUE.equals(success)) {
                        String failed = plugin.getConfigManager().getMessageOrDefault(
                                "TELEPORT.FAILED",
                                "&cTeleport failed. Please try again."
                        );
                        player.sendMessage(ColorUtils.toComponent(failed));
                        return;
                    }

                    String msg = plugin.getConfigManager().getMessage("TELEPORT.SUCCESS");
                    player.sendMessage(ColorUtils.toComponent(msg));
                    SoundUtils.play(player, plugin.getConfigManager().getSound("TELEPORT.SUCCESS"));
                    if (onSuccess != null) {
                        onSuccess.accept(player);
                    }
                }));
    }

    public void cancel(UUID uuid) {
        ScheduledTask task = pendingTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        pendingTypes.remove(uuid);
        startLocations.remove(uuid);
    }

    public boolean hasPending(UUID uuid) {
        return pendingTasks.containsKey(uuid);
    }

    public boolean hasPendingType(UUID uuid, String type) {
        String pendingType = pendingTypes.get(uuid);
        return pendingType != null && pendingType.equals(normalizeType(type));
    }

    public int countPendingByType(String type) {
        String normalizedType = normalizeType(type);
        int count = 0;
        for (String pendingType : pendingTypes.values()) {
            if (normalizedType.equals(pendingType)) {
                count++;
            }
        }
        return count;
    }

    public void checkMovement(Player player) {
        Location start = startLocations.get(player.getUniqueId());
        if (start == null) {
            return;
        }

        Location now = player.getLocation();
        if (Math.abs(now.getX() - start.getX()) > 0.5
                || Math.abs(now.getZ() - start.getZ()) > 0.5) {
            cancel(player.getUniqueId());
            PlayerSettingUtils.clearActionBar(player);
            String msg = plugin.getConfigManager().getMessage("TELEPORT.CANCELED");
            player.sendMessage(ColorUtils.toComponent(msg));
            SoundUtils.play(player, plugin.getConfigManager().getSound("TELEPORT.CANCELLED"));
        }
    }

    private int getCooldown(String type) {
        return plugin.getConfigManager().getConfig()
                .getInt("TELEPORT-COOLDOWN." + type.toUpperCase(Locale.ROOT), 5);
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
    }
}
