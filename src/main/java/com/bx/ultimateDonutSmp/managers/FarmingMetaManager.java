package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Keeps one item from the configured rotation marked as the farming meta. The meta item keeps
 * its normal worth.yml price as a base and sells for a multiple of it until the rotation moves
 * on to the next item.
 */
public class FarmingMetaManager {

    private final UltimateDonutSmp plugin;

    private File dataFile;
    private YamlConfiguration dataConfig;

    // worth is resolved from packet and region threads, so the rotation has to be visible
    // to them the moment the timer thread moves it
    private volatile Material currentItem;
    private volatile long nextRotationMillis;

    public FarmingMetaManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "farming-meta-data.yml");
        dataConfig = dataFile.exists()
                ? YamlConfiguration.loadConfiguration(dataFile)
                : new YamlConfiguration();

        List<Material> items = getRotationItems();
        currentItem = resolveCurrentItem(dataConfig.getString("current-item"), items);

        long persisted = dataConfig.getLong("next-rotation-millis", -1L);
        if (persisted > 0L) {
            nextRotationMillis = persisted;
            return;
        }

        nextRotationMillis = System.currentTimeMillis() + getRotationIntervalMillis();
        // the countdown has to survive restarts, otherwise a server that reboots daily never
        // reaches the end of a two week rotation. It only starts once the rotation is switched
        // on, so turning it on later does not begin with an overdue rotation.
        if (isEnabled() && currentItem != null) {
            save();
        }
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getWorth().getBoolean("META.ENABLED", false);
    }

    public boolean isActive() {
        return isEnabled() && currentItem != null;
    }

    public Material getCurrentItem() {
        return isEnabled() ? currentItem : null;
    }

    public double getMultiplier() {
        double configured = plugin.getConfigManager().getWorth().getDouble("META.MULTIPLIER", 1.15D);
        if (!Double.isFinite(configured) || configured <= 0D) {
            return 1.0D;
        }
        return configured;
    }

    /**
     * The factor to apply to a worth price. This runs for every item a player looks at or sells,
     * so it answers with a plain field comparison before it reads any configuration.
     */
    public double getMultiplierFor(Material material) {
        if (material == null || material != currentItem) {
            return 1.0D;
        }
        if (!isEnabled()) {
            return 1.0D;
        }
        return getMultiplier();
    }

    public boolean isTimeToRotate() {
        return isActive() && System.currentTimeMillis() >= nextRotationMillis;
    }

    public void rotate() {
        List<Material> items = getRotationItems();
        if (items.isEmpty()) {
            return;
        }

        int currentIndex = items.indexOf(currentItem);
        currentItem = items.get((currentIndex + 1) % items.size());
        nextRotationMillis = System.currentTimeMillis() + getRotationIntervalMillis();
        save();

        // browser prices are cached per item, so they have to be rebuilt against the new meta
        plugin.getWorthManager().reload();

        announceRotation();
    }

    public long getRemainingSeconds() {
        return Math.max(0L, (nextRotationMillis - System.currentTimeMillis()) / 1000L);
    }

    public String getFormattedCountdown() {
        return NumberUtils.formatTimeLong(getRemainingSeconds());
    }

    /**
     * The rotation in configuration order, without duplicates and without items that have no
     * price to multiply.
     */
    public List<Material> getRotationItems() {
        List<Material> items = new ArrayList<>();
        WorthManager worthManager = plugin.getWorthManager();

        for (String configured : plugin.getConfigManager().getWorth().getStringList("META.ITEMS")) {
            Material material = parseRotationItem(configured);
            if (material == null || material.isAir() || items.contains(material)) {
                continue;
            }
            if (worthManager != null && worthManager.getBaseWorth(material) <= 0D) {
                continue;
            }
            items.add(material);
        }

        return items;
    }

    private Material resolveCurrentItem(String persisted, List<Material> items) {
        if (items.isEmpty()) {
            return null;
        }

        Material stored = parseRotationItem(persisted);
        return stored != null && items.contains(stored) ? stored : items.get(0);
    }

    static Material parseRotationItem(String configured) {
        if (configured == null || configured.isBlank()) {
            return null;
        }

        String name = configured.trim();
        int namespace = name.indexOf(':');
        if (namespace >= 0) {
            name = name.substring(namespace + 1);
        }

        return Material.getMaterial(name.toUpperCase(Locale.US).replace(' ', '_'));
    }

    /**
     * Fills a message with the meta item, what it is worth before and after the multiplier, and
     * how long the rotation has left.
     */
    public String formatMetaMessage(String messagePath) {
        WorthManager worthManager = plugin.getWorthManager();
        double baseWorth = worthManager.getBaseWorth(currentItem);
        double metaWorth = worthManager.getWorth(currentItem);

        return plugin.getConfigManager().getMessage(
                messagePath,
                "{item}", worthManager.prettifyMaterial(currentItem),
                "{multiplier}", NumberUtils.format(getMultiplier()),
                "{base}", NumberUtils.format(baseWorth),
                "{base_formatted}", plugin.getCurrencyManager().formatMoney(baseWorth),
                "{price}", NumberUtils.format(metaWorth),
                "{price_formatted}", plugin.getCurrencyManager().formatMoney(metaWorth),
                "{countdown}", getFormattedCountdown()
        );
    }

    private void announceRotation() {
        if (!plugin.getConfigManager().getWorth().getBoolean("META.ANNOUNCE_ON_ROTATE", true)) {
            return;
        }

        String message = formatMetaMessage("WORTH.META-ROTATED");

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!PlayerSettingUtils.notificationEnabled(
                    plugin,
                    player,
                    PlayerSettingUtils.NotificationChannel.SERVER_BROADCAST
            )) {
                return;
            }
            player.sendMessage(ColorUtils.toComponent(message));
        });
    }

    private void save() {
        if (dataConfig == null || dataFile == null) {
            return;
        }

        dataConfig.set("current-item", currentItem == null ? null : currentItem.name());
        dataConfig.set("next-rotation-millis", nextRotationMillis);

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save farming-meta-data.yml", e);
        }
    }

    private long getRotationIntervalMillis() {
        int days = Math.max(0, plugin.getConfigManager().getWorth().getInt("META.INTERVAL_DAYS", 14));
        int hours = Math.max(0, plugin.getConfigManager().getWorth().getInt("META.INTERVAL_HOURS", 0));

        Duration interval = Duration.ofDays(days).plusHours(hours);
        return interval.isZero() ? Duration.ofDays(14).toMillis() : interval.toMillis();
    }
}
