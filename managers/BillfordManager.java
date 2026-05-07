package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.BillfordMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class BillfordManager {

    public record BillfordSlot(int slot, Material material, int quantity) {}

    public record BillfordTrade(
            int id,
            String displayName,
            int tradeLimit,
            long shardBonus,
            double moneyBonus,
            List<BillfordSlot> requiredItems,
            Material rewardMaterial,
            int rewardQuantity
    ) {}

    private final UltimateDonutSmp plugin;
    private final Random random = new Random();

    private int currentTradeId;
    private long nextAdvanceMillis;

    private final Map<UUID, Integer> playerTradeCounts = new HashMap<>();
    private final Map<UUID, Long> lastClickTime = new HashMap<>();
    private final Set<UUID> processingTrades = new HashSet<>();

    private File dataFile;
    private YamlConfiguration dataConfig;

    public BillfordManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "billford-data.yml");
        dataConfig = dataFile.exists()
                ? YamlConfiguration.loadConfiguration(dataFile)
                : new YamlConfiguration();
        playerTradeCounts.clear();

        currentTradeId = dataConfig.getInt(
                "current-trade",
                plugin.getConfigManager().getBillford().getInt("CURRENT", 1)
        );

        boolean countdownEnabled = plugin.getConfigManager().getBillford()
                .getBoolean("COUNTDOWN.ENABLED", true);

        if (countdownEnabled) {
            long persisted = dataConfig.getLong("next-advance-millis", -1L);
            if (persisted > 0L) {
                nextAdvanceMillis = persisted;
            } else {
                String startDate = plugin.getConfigManager().getBillford()
                        .getString("COUNTDOWN.START_DATE", "2026-01-31 00:00:00");
                nextAdvanceMillis = computeNext(startDate);
            }
        } else {
            nextAdvanceMillis = Long.MAX_VALUE;
        }

        ConfigurationSection countsSection = dataConfig.getConfigurationSection("player-counts");
        if (countsSection != null) {
            for (String uuidStr : countsSection.getKeys(false)) {
                try {
                    playerTradeCounts.put(
                            UUID.fromString(uuidStr),
                            countsSection.getInt(uuidStr, 0)
                    );
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public boolean isTimeToAdvance() {
        return nextAdvanceMillis != Long.MAX_VALUE
                && System.currentTimeMillis() >= nextAdvanceMillis;
    }

    public synchronized boolean beginTrade(UUID uuid) {
        return processingTrades.add(uuid);
    }

    public synchronized void endTrade(UUID uuid) {
        processingTrades.remove(uuid);
    }

    public void advanceTrade() {
        int total = getTradeCount();
        if (total <= 0) {
            return;
        }

        String mode = plugin.getConfigManager().getBillford()
                .getString("ROTATION_MODE", "SEQUENTIAL")
                .toUpperCase();

        if ("RANDOM".equals(mode)) {
            int next;
            do {
                next = random.nextInt(total) + 1;
            } while (next == currentTradeId && total > 1);
            currentTradeId = next;
        } else {
            currentTradeId = (currentTradeId % total) + 1;
        }

        playerTradeCounts.clear();
        nextAdvanceMillis = System.currentTimeMillis() + getRotationIntervalMillis();
        saveData();

        boolean announce = plugin.getConfigManager().getBillford()
                .getBoolean("ANNOUNCE_ON_ROTATE", true);
        if (announce) {
            String raw = plugin.getConfigManager().getBillford()
                    .getString(
                            "ANNOUNCE_MESSAGE",
                            "&6&l[Billford] &eTrade rotated! &7Next change in &b{countdown}&7."
                    )
                    .replace("{trade_id}", String.valueOf(currentTradeId))
                    .replace("{countdown}", getFormattedCountdown());

            Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage(ColorUtils.toComponent(raw));
                SoundUtils.play(player, plugin.getConfigManager().getSound("BILLFORD.ROTATE"));
            });
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof BillfordMenu) {
                player.closeInventory();
            }
        });
    }

    private void saveData() {
        dataConfig.set("current-trade", currentTradeId);
        dataConfig.set("next-advance-millis", nextAdvanceMillis);
        dataConfig.set("player-counts", null);
        playerTradeCounts.forEach((uuid, count) ->
                dataConfig.set("player-counts." + uuid, count));

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save billford-data.yml", e);
        }
    }

    public BillfordTrade getCurrentTrade() {
        return getTrade(currentTradeId);
    }

    public int getTradeCount() {
        ConfigurationSection section = plugin.getConfigManager().getBillford()
                .getConfigurationSection("BILLFORD");
        return section == null ? 0 : section.getKeys(false).size();
    }

    public BillfordTrade getTrade(int id) {
        ConfigurationSection section = plugin.getConfigManager().getBillford()
                .getConfigurationSection("BILLFORD." + id);
        if (section == null) {
            return null;
        }

        String displayName = section.getString("DISPLAY_NAME", "Trade #" + id);
        int tradeLimit = section.getInt("LIMIT", section.getInt("LIMIT_PER_PLAYER", 0));

        ConfigurationSection rewardSection = section.getConfigurationSection("REWARD");
        long shardBonus = section.getLong(
                "SHARD_BONUS",
                rewardSection != null ? rewardSection.getLong("SHARD_BONUS", 0L) : 0L
        );
        double moneyBonus = section.getDouble(
                "MONEY_BONUS",
                rewardSection != null ? rewardSection.getDouble("MONEY_BONUS", 0D) : 0D
        );

        List<BillfordSlot> requiredItems = readRequiredItems(section);
        Material rewardMaterial = rewardSection != null
                ? ItemUtils.parseMaterial(rewardSection.getString("MATERIAL",
                rewardSection.getString("ITEM", "STONE")))
                : Material.STONE;
        int rewardQuantity = rewardSection != null
                ? Math.max(1, rewardSection.getInt("QUANTITY", rewardSection.getInt("AMOUNT", 1)))
                : 1;

        return new BillfordTrade(
                id,
                displayName,
                tradeLimit,
                shardBonus,
                moneyBonus,
                requiredItems,
                rewardMaterial,
                rewardQuantity
        );
    }

    private List<BillfordSlot> readRequiredItems(ConfigurationSection tradeSection) {
        List<BillfordSlot> slots = new ArrayList<>();

        ConfigurationSection inputsSection = tradeSection.getConfigurationSection("INPUTS");
        if (inputsSection != null && !inputsSection.getKeys(false).isEmpty()) {
            populateInputSlots(inputsSection, slots);
        } else {
            for (String key : tradeSection.getKeys(false)) {
                if (!key.toUpperCase().startsWith("ITEM_")) {
                    continue;
                }
                ConfigurationSection itemSection = tradeSection.getConfigurationSection(key);
                if (itemSection == null) {
                    continue;
                }
                populateInputSlot(itemSection, slots.size() + 1, slots);
            }
        }

        slots.sort(Comparator.comparingInt(BillfordSlot::slot));
        return slots;
    }

    private void populateInputSlots(ConfigurationSection inputsSection, List<BillfordSlot> slots) {
        for (String key : inputsSection.getKeys(false)) {
            ConfigurationSection itemSection = inputsSection.getConfigurationSection(key);
            if (itemSection == null) {
                continue;
            }
            populateInputSlot(itemSection, slots.size() + 1, slots);
        }
    }

    private void populateInputSlot(
            ConfigurationSection itemSection,
            int fallbackSlot,
            List<BillfordSlot> slots
    ) {
        int slot = Math.max(1, Math.min(54, itemSection.getInt("SLOT", fallbackSlot)));
        Material material = ItemUtils.parseMaterial(itemSection.getString("MATERIAL", "STONE"));
        int quantity = Math.max(1, itemSection.getInt("QUANTITY", itemSection.getInt("AMOUNT", 1)));
        slots.add(new BillfordSlot(slot, material, quantity));
    }

    public int getPlayerTradeCount(UUID uuid) {
        return playerTradeCounts.getOrDefault(uuid, 0);
    }

    public boolean hasReachedLimit(UUID uuid) {
        BillfordTrade trade = getCurrentTrade();
        if (trade == null || trade.tradeLimit() <= 0) {
            return false;
        }
        return getPlayerTradeCount(uuid) >= trade.tradeLimit();
    }

    public void incrementPlayerTradeCount(UUID uuid) {
        playerTradeCounts.merge(uuid, 1, Integer::sum);
        saveData();
    }

    public boolean isOnCooldown(UUID uuid) {
        long cooldownMs = plugin.getConfigManager().getBillford()
                .getLong("SETTINGS.CLICK_COOLDOWN_MS",
                        plugin.getConfigManager().getBillford().getLong("CLICK_COOLDOWN_MS", 1000L));
        Long lastClick = lastClickTime.get(uuid);
        return lastClick != null && (System.currentTimeMillis() - lastClick) < cooldownMs;
    }

    public void updateCooldown(UUID uuid) {
        lastClickTime.put(uuid, System.currentTimeMillis());
    }

    public int getCurrentTradeId() {
        return currentTradeId;
    }

    public long getRemainingSeconds() {
        return Math.max(0L, (nextAdvanceMillis - System.currentTimeMillis()) / 1000L);
    }

    public String getFormattedCountdown() {
        long totalSeconds = getRemainingSeconds();
        long days = totalSeconds / 86_400L;
        long hours = (totalSeconds % 86_400L) / 3_600L;
        long minutes = (totalSeconds % 3_600L) / 60L;
        long seconds = totalSeconds % 60L;

        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        }
        return minutes + "m " + seconds + "s";
    }

    private long computeNext(String startStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ZoneId zoneId = getRotationZone();
            ZonedDateTime start = LocalDateTime.parse(startStr, formatter).atZone(zoneId);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            Duration interval = getRotationInterval();

            ZonedDateTime next = start;
            while (!next.isAfter(now)) {
                next = next.plus(interval);
            }

            return next.toInstant().toEpochMilli();
        } catch (Exception exception) {
            return System.currentTimeMillis() + getRotationIntervalMillis();
        }
    }

    private ZoneId getRotationZone() {
        String configured = plugin.getConfigManager().getBillford()
                .getString("COUNTDOWN.TIME_ZONE", "SYSTEM");
        if (configured == null || configured.isBlank() || configured.equalsIgnoreCase("SYSTEM")) {
            return ZoneId.systemDefault();
        }

        try {
            return ZoneId.of(configured);
        } catch (Exception ignored) {
            return ZoneId.systemDefault();
        }
    }

    private Duration getRotationInterval() {
        int days = Math.max(0, plugin.getConfigManager().getBillford()
                .getInt("COUNTDOWN.INTERVAL_DAYS", 3));
        int hours = Math.max(0, plugin.getConfigManager().getBillford()
                .getInt("COUNTDOWN.INTERVAL_HOURS", 0));

        Duration interval = Duration.ZERO;
        if (days > 0) {
            interval = interval.plusDays(days);
        }
        if (hours > 0) {
            interval = interval.plusHours(hours);
        }

        if (interval.isZero()) {
            return Duration.ofDays(3);
        }
        return interval;
    }

    private long getRotationIntervalMillis() {
        return getRotationInterval().toMillis();
    }
}
