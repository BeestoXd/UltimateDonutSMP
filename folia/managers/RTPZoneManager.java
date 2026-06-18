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
        enabled = plugin.getFeatureManager().isEnabled(FeatureManager.Feature.RTP_ZONE)
                && plugin.getConfigManager().getConfig().getBoolean("RTP-ZONE.ENABLED", true);
        cuboidName = plugin.getConfigManager().getConfig().getString("RTP-ZONE.CUBOID", "");
        countdownSeconds = Math.max(1, plugin.getConfigManager().getConfig().getInt("RTP-ZONE.EVERY", 30));
        titleTemplate = plugin.getConfigManager().getConfig().getString("RTP-ZONE.TITLE", "&c&lʀᴛᴘ ᴢᴏɴᴇ");
        subtitleTemplate = plugin.getConfigManager().getConfig().getString("RTP-ZONE.SUB-TITLE", "&fᴛᴇʟᴇᴘᴏʀᴛɪɴɢ ɪɴ %countdown%");
        cancelledMessage = plugin.getConfigManager().getConfig().getString(
                "RTP-ZONE.CANCELLED-MESSAGE",
                "&cʀᴛᴘ ᴄᴀɴᴄᴇʟʟᴇᴅ ʙᴇᴄᴀᴜѕᴇ ʏᴏᴜ ʟᴇꜰᴛ ᴛʜᴇ ᴢᴏɴᴇ."
        );
        failedMessage = plugin.getConfigManager().getConfig().getString(
                "RTP-ZONE.FAILED-MESSAGE",
                "&cᴄᴏᴜʟᴅ ɴᴏᴛ ꜰɪɴᴅ ᴀ ѕᴀꜰᴇ ʀᴛᴘ ᴢᴏɴᴇ ʟᴏᴄᴀᴛɪᴏɴ."
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
                TitleUtils.clearTitle(player);
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

    private void teleportPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (!pendingTeleports.add(uuid)) {
            return;
        }

        plugin.getRtpManager().findSafeLocationAsync(player, searchSettings).whenComplete((destination, throwable) -> {
            pendingTeleports.remove(uuid);
            plugin.getFoliaScheduler().runEntity(player, () -> {
                if (throwable != null || destination == null) {
                    if (failedMessage != null && !failedMessage.isBlank()) {
                        player.sendMessage(ColorUtils.toComponent(failedMessage));
                    }
                    return;
                }
                plugin.getFoliaScheduler().teleport(player, destination).thenAccept(success ->
                        plugin.getFoliaScheduler().runEntity(player, () -> {
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
}
