package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import com.bx.ultimateDonutSmp.utils.TitleUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RTPZoneManager {

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Integer> countdowns = new HashMap<>();
    private final Set<UUID> pendingTeleports = ConcurrentHashMap.newKeySet();

    private boolean enabled;
    private String cuboidName;
    private int countdownSeconds;
    private int titleFadeOutTicks;
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
        hardClearActiveTitles();
        enabled = plugin.getFeatureManager().isEnabled(FeatureManager.Feature.RTP_ZONE)
                && plugin.getConfigManager().getConfig().getBoolean("RTP-ZONE.ENABLED", true);
        cuboidName = plugin.getConfigManager().getConfig().getString("RTP-ZONE.CUBOID", "");
        countdownSeconds = Math.max(1, plugin.getConfigManager().getConfig().getInt("RTP-ZONE.EVERY", 30));
        titleFadeOutTicks = normalizeTitleFadeOutTicks(
                plugin.getConfigManager().getConfig().getInt("RTP-ZONE.TITLE-FADE-OUT-TICKS", 10)
        );
        titleTemplate = plugin.getConfigManager().getConfig().getString("RTP-ZONE.TITLE", "&c&lrtp zone");
        subtitleTemplate = plugin.getConfigManager().getConfig().getString("RTP-ZONE.SUB-TITLE", "&fteleporting in %countdown%");
        cancelledMessage = plugin.getConfigManager().getConfig().getString(
                "RTP-ZONE.CANCELLED-MESSAGE",
                "&crtp cancelled because you left the zone."
        );
        failedMessage = plugin.getConfigManager().getConfig().getString(
                "RTP-ZONE.FAILED-MESSAGE",
                "&ccould not find a safe rtp zone location."
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
        pendingTeleports.clear();
    }

    public void tick(Player player) {
        if (!plugin.getFeatureManager().isEnabled(FeatureManager.Feature.RTP)
                || !plugin.getFeatureManager().isEnabled(FeatureManager.Feature.RTP_ZONE)) {
            clearState(player);
            return;
        }

        if (plugin.getDuelManager() != null) {
            UUID uuid = player.getUniqueId();
            if (plugin.getDuelManager().isInDuel(uuid) || plugin.getDuelManager().isTransitioning(uuid)) {
                clearState(player);
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
            Integer remaining = countdowns.get(uuid);
            if (remaining != null) {
                fadeOutTitle(player, remaining);
                clearState(uuid);
                SoundUtils.play(player, cancelledSound);
                if (cancelledMessage != null && !cancelledMessage.isBlank()) {
                    player.sendMessage(ColorUtils.toComponent(cancelledMessage));
                }
            }
            return;
        }

        if (pendingTeleports.contains(uuid)) {
            return;
        }

        int remaining = countdowns.getOrDefault(uuid, countdownSeconds + 1) - 1;
        if (remaining <= 0) {
            TitleUtils.clearTitle(player);
            clearState(uuid);
            teleportPlayer(player);
            return;
        }

        countdowns.put(uuid, remaining);
        showCountdown(player, remaining);
    }

    public void clearState(Player player) {
        if (player != null) {
            TitleUtils.clearTitle(player);
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

    static int normalizeTitleFadeOutTicks(int configuredTicks) {
        return Math.max(1, configuredTicks);
    }

    private void teleportPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (!pendingTeleports.add(uuid)) {
            return;
        }

        plugin.getRtpManager().findSafeLocationAsync(player, searchSettings).whenComplete((destination, throwable) -> {
            pendingTeleports.remove(uuid);
            plugin.getSpigotScheduler().runEntity(player, () -> {
                if (throwable != null || destination == null) {
                    if (failedMessage != null && !failedMessage.isBlank()) {
                        player.sendMessage(ColorUtils.toComponent(failedMessage));
                    }
                    return;
                }
                plugin.getSpigotScheduler().teleport(player, destination).thenAccept(success ->
                        plugin.getSpigotScheduler().runEntity(player, () -> {
                            if (!Boolean.TRUE.equals(success) || !player.isOnline()) {
                                return;
                            }
                            SoundUtils.play(player, plugin.getConfigManager().getSound("TELEPORT.SUCCESS"));
                            if (successMessage != null && !successMessage.isBlank()) {
                                player.sendMessage(ColorUtils.toComponent(successMessage));
                            }
                        }));
            });
        });
    }

    private void showCountdown(Player player, int remaining) {
        String countdown = String.valueOf(remaining);
        String title = titleTemplate.replace("%countdown%", countdown);
        String subtitle = subtitleTemplate.replace("%countdown%", countdown);

        TitleUtils.sendTitle(player, ColorUtils.colorize(title, player), ColorUtils.colorize(subtitle, player), 0, 24, 3);
        SoundUtils.play(player, countdownSound);
    }

    private void fadeOutTitle(Player player, int remaining) {
        String countdown = String.valueOf(remaining);
        String title = titleTemplate.replace("%countdown%", countdown);
        String subtitle = subtitleTemplate.replace("%countdown%", countdown);
        TitleUtils.sendTitle(
                player,
                ColorUtils.colorize(title, player),
                ColorUtils.colorize(subtitle, player),
                0,
                0,
                titleFadeOutTicks
        );
    }

    private void hardClearActiveTitles() {
        for (UUID uuid : Set.copyOf(countdowns.keySet())) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                TitleUtils.clearTitle(player);
            }
        }
    }
}
