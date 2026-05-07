package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RTPZoneManager {

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Integer> countdowns = new HashMap<>();

    private boolean enabled;
    private String cuboidName;
    private int countdownSeconds;
    private String titleTemplate;
    private String subtitleTemplate;
    private String cancelledMessage;
    private String failedMessage;
    private String successMessage;
    private String countdownSound;
    private String cancelledSound;
    private RTPManager.SearchSettings searchSettings;

    public RTPZoneManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reloadSettings();
    }

    public void reloadSettings() {
        enabled = plugin.getConfigManager().getConfig().getBoolean("RTP-ZONE.ENABLED", true);
        cuboidName = plugin.getConfigManager().getConfig().getString("RTP-ZONE.CUBOID", "");
        countdownSeconds = Math.max(1, plugin.getConfigManager().getConfig().getInt("RTP-ZONE.EVERY", 30));
        titleTemplate = plugin.getConfigManager().getConfig().getString("RTP-ZONE.TITLE", "&c&lRTP Zone");
        subtitleTemplate = plugin.getConfigManager().getConfig().getString("RTP-ZONE.SUB-TITLE", "&fTeleporting in %countdown%");
        cancelledMessage = plugin.getConfigManager().getConfig().getString(
                "RTP-ZONE.CANCELLED-MESSAGE",
                "&cRTP cancelled because you left the zone."
        );
        failedMessage = plugin.getConfigManager().getConfig().getString(
                "RTP-ZONE.FAILED-MESSAGE",
                "&cCould not find a safe RTP zone location."
        );
        successMessage = plugin.getConfigManager().getConfig().getString("RTP-ZONE.SUCCESS-MESSAGE", "");
        countdownSound = plugin.getConfigManager().getSound("RTP-ZONE.COUNTDOWN");
        if (countdownSound == null || countdownSound.isBlank()) {
            countdownSound = plugin.getConfigManager().getSound("TELEPORT.COUNTDOWN");
        }
        cancelledSound = plugin.getConfigManager().getSound("RTP-ZONE.CANCELLED");
        if (cancelledSound == null || cancelledSound.isBlank()) {
            cancelledSound = plugin.getConfigManager().getSound("TELEPORT.CANCELLED");
        }
        searchSettings = plugin.getRtpManager().getZoneSearchSettings();
        countdowns.clear();
    }

    public void tick(Player player) {
        if (plugin.getDuelManager() != null) {
            UUID uuid = player.getUniqueId();
            if (plugin.getDuelManager().isInDuel(uuid) || plugin.getDuelManager().isTransitioning(uuid)) {
                clearState(uuid);
                return;
            }
        }

        if (!enabled || cuboidName == null || cuboidName.isBlank()) {
            clearState(player);
            return;
        }

        CuboidManager.Cuboid cuboid = plugin.getCuboidManager().getCuboid(cuboidName);
        if (cuboid == null) {
            clearState(player);
            return;
        }

        UUID uuid = player.getUniqueId();
        if (!cuboid.contains(player.getLocation())) {
            if (countdowns.containsKey(uuid)) {
                player.clearTitle();
                clearState(uuid);
                SoundUtils.play(player, cancelledSound);
                if (cancelledMessage != null && !cancelledMessage.isBlank()) {
                    player.sendMessage(ColorUtils.toComponent(cancelledMessage));
                }
            }
            return;
        }

        int remaining = countdowns.getOrDefault(uuid, countdownSeconds + 1) - 1;
        if (remaining <= 0) {
            player.clearTitle();
            clearState(uuid);
            teleportPlayer(player);
            return;
        }

        countdowns.put(uuid, remaining);
        showCountdown(player, remaining);
    }

    public void clearState(Player player) {
        if (player != null) {
            player.clearTitle();
            clearState(player.getUniqueId());
        }
    }

    public boolean isInZone(Player player) {
        if (player == null || !enabled || cuboidName == null || cuboidName.isBlank()) {
            return false;
        }

        CuboidManager.Cuboid cuboid = plugin.getCuboidManager().getCuboid(cuboidName);
        return cuboid != null && cuboid.contains(player.getLocation());
    }

    public void clearState(UUID uuid) {
        countdowns.remove(uuid);
    }

    private void teleportPlayer(Player player) {
        Location destination = plugin.getRtpManager().findSafeLocation(searchSettings);
        if (destination == null) {
            if (failedMessage != null && !failedMessage.isBlank()) {
                player.sendMessage(ColorUtils.toComponent(failedMessage));
            }
            return;
        }

        plugin.getPaperScheduler().teleport(player, destination).thenAccept(success ->
                plugin.getPaperScheduler().runEntity(player, () -> {
                    if (!Boolean.TRUE.equals(success) || !player.isOnline()) {
                        return;
                    }
                    SoundUtils.play(player, plugin.getConfigManager().getSound("TELEPORT.SUCCESS"));
                    if (successMessage != null && !successMessage.isBlank()) {
                        player.sendMessage(ColorUtils.toComponent(successMessage));
                    }
                }));
    }

    private void showCountdown(Player player, int remaining) {
        String countdown = String.valueOf(remaining);
        String title = titleTemplate.replace("%countdown%", countdown);
        String subtitle = subtitleTemplate.replace("%countdown%", countdown);

        player.showTitle(Title.title(
                ColorUtils.toComponent(title, player),
                ColorUtils.toComponent(subtitle, player),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ofMillis(150))
        ));
        SoundUtils.play(player, countdownSound);
    }
}
