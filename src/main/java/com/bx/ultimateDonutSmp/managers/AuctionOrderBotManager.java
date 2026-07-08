package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.AuctionCategory;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public final class AuctionOrderBotManager {

    private final UltimateDonutSmp plugin;

    // Auction Bot Settings
    private boolean auctionEnabled;
    private long auctionMinInterval;
    private long auctionMaxInterval;
    private double auctionChance;
    private int maxBotListings;
    private int auctionMinDuration;
    private int auctionMaxDuration;
    private List<String> auctionBotNames = new ArrayList<>();
    private List<BotItemConfig> auctionItems = new ArrayList<>();
    private long nextAuctionCheckTime;

    // Order Bot Settings
    private boolean ordersEnabled;
    private long ordersMinInterval;
    private long ordersMaxInterval;
    private double ordersChance;
    private int maxBotOrders;
    private int ordersMinDuration;
    private int ordersMaxDuration;
    private List<String> ordersBotNames = new ArrayList<>();
    private List<BotItemConfig> ordersItems = new ArrayList<>();
    private long nextOrdersCheckTime;

    public AuctionOrderBotManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        loadAuctionSettings();
        loadOrdersSettings();
    }

    private void loadAuctionSettings() {
        FileConfiguration config = plugin.getConfigManager().getAuctionHouse();
        ConfigurationSection section = config.getConfigurationSection("BOTS");
        if (section == null) {
            auctionEnabled = false;
            return;
        }

        auctionEnabled = section.getBoolean("ENABLED", false);
        auctionMinInterval = section.getLong("MIN_CHECK_INTERVAL_SECONDS", 60) * 1000L;
        auctionMaxInterval = section.getLong("MAX_CHECK_INTERVAL_SECONDS", 300) * 1000L;
        auctionChance = section.getDouble("CHANCE", 0.5);
        maxBotListings = section.getInt("MAX_ACTIVE_BOT_LISTINGS", 10);
        auctionMinDuration = section.getInt("MIN_DURATION_HOURS", 12);
        auctionMaxDuration = section.getInt("MAX_DURATION_HOURS", 48);
        auctionBotNames = section.getStringList("BOT_NAMES");
        if (auctionBotNames == null) {
            auctionBotNames = new ArrayList<>();
        }

        auctionItems = new ArrayList<>();
        List<Map<?, ?>> itemsList = config.getMapList("ITEMS");
        if (itemsList == null || itemsList.isEmpty()) {
            itemsList = section.getMapList("ITEMS");
        }
        if (itemsList != null) {
            for (Map<?, ?> map : itemsList) {
                BotItemConfig itemConfig = BotItemConfig.fromMap(map);
                if (itemConfig != null) {
                    auctionItems.add(itemConfig);
                }
            }
        }

        // Schedule first check
        nextAuctionCheckTime = System.currentTimeMillis() + getRandomInterval(auctionMinInterval, auctionMaxInterval);
    }

    private void loadOrdersSettings() {
        FileConfiguration config = plugin.getConfigManager().getOrders();
        ConfigurationSection section = config.getConfigurationSection("BOTS");
        if (section == null) {
            ordersEnabled = false;
            return;
        }

        ordersEnabled = section.getBoolean("ENABLED", false);
        ordersMinInterval = section.getLong("MIN_CHECK_INTERVAL_SECONDS", 60) * 1000L;
        ordersMaxInterval = section.getLong("MAX_CHECK_INTERVAL_SECONDS", 300) * 1000L;
        ordersChance = section.getDouble("CHANCE", 0.5);
        maxBotOrders = section.getInt("MAX_ACTIVE_BOT_ORDERS", 10);
        ordersMinDuration = section.getInt("MIN_DURATION_HOURS", 24);
        ordersMaxDuration = section.getInt("MAX_DURATION_HOURS", 72);
        ordersBotNames = section.getStringList("BOT_NAMES");
        if (ordersBotNames == null) {
            ordersBotNames = new ArrayList<>();
        }

        ordersItems = new ArrayList<>();
        List<Map<?, ?>> itemsList = config.getMapList("ITEMS");
        if (itemsList == null || itemsList.isEmpty()) {
            itemsList = section.getMapList("ITEMS");
        }
        if (itemsList != null) {
            for (Map<?, ?> map : itemsList) {
                BotItemConfig itemConfig = BotItemConfig.fromMap(map);
                if (itemConfig != null) {
                    ordersItems.add(itemConfig);
                }
            }
        }

        // Schedule first check
        nextOrdersCheckTime = System.currentTimeMillis() + getRandomInterval(ordersMinInterval, ordersMaxInterval);
    }

    private long getRandomInterval(long min, long max) {
        if (min >= max) {
            return min;
        }
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    public void tick() {
        long now = System.currentTimeMillis();

        if (auctionEnabled && !auctionBotNames.isEmpty() && !auctionItems.isEmpty()) {
            if (now >= nextAuctionCheckTime) {
                try {
                    checkAndRunAuctionBot();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error checking/running Auction bots", e);
                }
                nextAuctionCheckTime = now + getRandomInterval(auctionMinInterval, auctionMaxInterval);
            }
        }

        if (ordersEnabled && !ordersBotNames.isEmpty() && !ordersItems.isEmpty()) {
            if (now >= nextOrdersCheckTime) {
                try {
                    checkAndRunOrderBot();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error checking/running Order bots", e);
                }
                nextOrdersCheckTime = now + getRandomInterval(ordersMinInterval, ordersMaxInterval);
            }
        }
    }

    private void checkAndRunAuctionBot() {
        if (ThreadLocalRandom.current().nextDouble() > auctionChance) {
            return;
        }

        AuctionHouseManager ahManager = plugin.getAuctionHouseManager();
        if (ahManager == null || !ahManager.isEnabled()) {
            return;
        }

        int currentBotListings = ahManager.countActiveBotListings(auctionBotNames);
        if (currentBotListings >= maxBotListings) {
            return;
        }

        // List a bot item!
        String botName = auctionBotNames.get(ThreadLocalRandom.current().nextInt(auctionBotNames.size()));
        UUID botUuid = UUID.nameUUIDFromBytes(("UDSAuctionBot:" + botName).getBytes(StandardCharsets.UTF_8));

        BotItemConfig itemConfig = auctionItems.get(ThreadLocalRandom.current().nextInt(auctionItems.size()));
        ItemStack itemStack = itemConfig.createItemStack();
        if (itemStack == null || itemStack.getType().isAir()) {
            return;
        }

        double price = itemConfig.getRandomPrice();
        int duration = ThreadLocalRandom.current().nextInt(auctionMinDuration, auctionMaxDuration + 1);
        String category = determineCategory(itemStack);

        ahManager.createBotListingDirect(
                botUuid,
                botName,
                itemStack,
                price,
                duration,
                category
        );
    }

    private String determineCategory(ItemStack item) {
        for (AuctionCategory cat : AuctionCategory.values()) {
            if (cat == AuctionCategory.ALL) continue;
            if (cat.matches(item)) {
                return cat.name();
            }
        }
        return AuctionCategory.ALL.name();
    }

    private void checkAndRunOrderBot() {
        if (ThreadLocalRandom.current().nextDouble() > ordersChance) {
            return;
        }

        OrdersManager ordersManager = plugin.getOrdersManager();
        if (ordersManager == null || !ordersManager.isEnabled()) {
            return;
        }

        int currentBotOrders = ordersManager.countActiveBotOrders(ordersBotNames);
        if (currentBotOrders >= maxBotOrders) {
            return;
        }

        // Place a bot order!
        String botName = ordersBotNames.get(ThreadLocalRandom.current().nextInt(ordersBotNames.size()));
        UUID botUuid = UUID.nameUUIDFromBytes(("UDSAuctionBot:" + botName).getBytes(StandardCharsets.UTF_8));

        BotItemConfig itemConfig = ordersItems.get(ThreadLocalRandom.current().nextInt(ordersItems.size()));
        ItemStack itemStack = itemConfig.createItemStack();
        if (itemStack == null || itemStack.getType().isAir()) {
            return;
        }

        double priceEach = itemConfig.getRandomPrice();
        int quantity = itemStack.getAmount();
        int duration = ThreadLocalRandom.current().nextInt(ordersMinDuration, ordersMaxDuration + 1);
        String category = ordersManager.resolveCategoryForMaterial(itemStack.getType());

        ItemStack templateItem = itemStack.clone();
        templateItem.setAmount(1);

        ordersManager.createBotOrderDirect(
                botUuid,
                botName,
                templateItem,
                category,
                quantity,
                priceEach,
                duration
        );
    }

    private static final class BotItemConfig {
        private final Material material;
        private final int minAmount;
        private final int maxAmount;
        private final double minPrice;
        private final double maxPrice;
        private final List<String> enchants;

        private BotItemConfig(Material material, int minAmount, int maxAmount, double minPrice, double maxPrice, List<String> enchants) {
            this.material = material;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.enchants = enchants;
        }

        public static BotItemConfig fromMap(Map<?, ?> map) {
            if (map == null) return null;
            Object matObj = map.get("MATERIAL");
            if (matObj == null) return null;
            Material material = ItemUtils.parseMaterial(matObj.toString());
            if (material == null || material.isAir()) return null;

            int minAmount = 1;
            if (map.containsKey("MIN_AMOUNT")) {
                minAmount = ((Number) map.get("MIN_AMOUNT")).intValue();
            } else if (map.containsKey("AMOUNT")) {
                minAmount = ((Number) map.get("AMOUNT")).intValue();
            }

            int maxAmount = minAmount;
            if (map.containsKey("MAX_AMOUNT")) {
                maxAmount = ((Number) map.get("MAX_AMOUNT")).intValue();
            }

            double minPrice = 10;
            if (map.containsKey("MIN_PRICE")) {
                minPrice = ((Number) map.get("MIN_PRICE")).doubleValue();
            } else if (map.containsKey("PRICE")) {
                minPrice = ((Number) map.get("PRICE")).doubleValue();
            } else if (map.containsKey("MIN_PRICE_EACH")) {
                minPrice = ((Number) map.get("MIN_PRICE_EACH")).doubleValue();
            }

            double maxPrice = minPrice;
            if (map.containsKey("MAX_PRICE")) {
                maxPrice = ((Number) map.get("MAX_PRICE")).doubleValue();
            } else if (map.containsKey("MAX_PRICE_EACH")) {
                maxPrice = ((Number) map.get("MAX_PRICE_EACH")).doubleValue();
            }

            List<String> enchants = null;
            if (map.containsKey("ENCHANTS")) {
                Object enchObj = map.get("ENCHANTS");
                if (enchObj instanceof List<?>) {
                    enchants = new ArrayList<>();
                    for (Object o : (List<?>) enchObj) {
                        enchants.add(o.toString());
                    }
                }
            }

            return new BotItemConfig(material, minAmount, maxAmount, minPrice, maxPrice, enchants);
        }

        public ItemStack createItemStack() {
            int amount = minAmount >= maxAmount ? minAmount : ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1);
            ItemStack item = new ItemStack(material, amount);
            if (enchants != null && !enchants.isEmpty()) {
                item = ItemUtils.addEnchantments(item, enchants);
            }
            return item;
        }

        public double getRandomPrice() {
            if (minPrice >= maxPrice) {
                return minPrice;
            }
            double randomVal = ThreadLocalRandom.current().nextDouble(minPrice, maxPrice);
            return Math.round(randomVal * 100.0) / 100.0;
        }
    }
}
