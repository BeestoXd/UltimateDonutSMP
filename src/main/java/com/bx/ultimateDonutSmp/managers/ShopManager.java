package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.amethyst.AmethystToolType;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.SellCategory;
import com.bx.ultimateDonutSmp.models.ShopPreference;
import com.bx.ultimateDonutSmp.storage.ShopPreferenceRepository;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemSerializationUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.logging.Level;

public class ShopManager {

    private static final String PRICE_TAG_PREFIX = "[PRICE] ";
    private static final Set<String> CATEGORY_META_KEYS = Set.of("MENU-TITLE", "MENU-SIZE", "ORDER");
    private static final Set<String> MENU_META_KEYS = Set.of(
            "TITLE",
            "SIZE",
            "CURRENCY",
            "BACK-BUTTON-SLOT",
            "FIRST-PAGE-SLOT",
            "PREVIOUS-PAGE-SLOT",
            "PAGE-INFO-SLOT",
            "NEXT-PAGE-SLOT",
            "LAST-PAGE-SLOT",
            "ITEMS-PER-PAGE",
            "ITEMS"
    );
    private static final Set<Material> FISH_CATEGORY_OVERRIDES = EnumSet.of(
            Material.COD,
            Material.COOKED_COD,
            Material.SALMON,
            Material.COOKED_SALMON,
            Material.TROPICAL_FISH,
            Material.PUFFERFISH,
            Material.COD_BUCKET,
            Material.SALMON_BUCKET,
            Material.TROPICAL_FISH_BUCKET,
            Material.PUFFERFISH_BUCKET,
            Material.AXOLOTL_BUCKET,
            Material.TADPOLE_BUCKET,
            Material.FISHING_ROD,
            Material.NAME_TAG,
            Material.NAUTILUS_SHELL,
            Material.LILY_PAD,
            Material.HEART_OF_THE_SEA
    );
    private static final Set<Material> POTION_CATEGORY_OVERRIDES = EnumSet.of(
            Material.POTION,
            Material.SPLASH_POTION,
            Material.LINGERING_POTION,
            Material.TIPPED_ARROW,
            Material.BREWING_STAND,
            Material.BLAZE_POWDER,
            Material.BLAZE_ROD,
            Material.FERMENTED_SPIDER_EYE,
            Material.GLASS_BOTTLE,
            Material.GLISTERING_MELON_SLICE,
            Material.GHAST_TEAR,
            Material.MAGMA_CREAM,
            Material.RABBIT_FOOT,
            Material.SPIDER_EYE,
            Material.SUGAR,
            Material.GOLDEN_CARROT,
            Material.PHANTOM_MEMBRANE
    );
    private static final int MAX_MULTIPLIER_BAR_SEGMENTS = 10;

    public enum Currency { MONEY, SHARD }

    public enum PurchaseFailureReason {
        INVALID_ITEM,
        INVALID_QUANTITY,
        NO_PLAYER_DATA,
        NO_PERMISSION,
        NO_MONEY,
        NO_SHARDS,
        INVENTORY_FULL,
        REWARD_FAILED
    }

    public record ShopCategory(
            String key,
            String menuSection,
            Material material,
            String displayName,
            List<String> lore,
            int slot
    ) {}

    public record ShopRestriction(
            int minQuantity,
            int maxQuantity,
            int defaultQuantity,
            boolean hideQuantityButtons
    ) {
        public ShopRestriction {
            minQuantity = Math.max(1, minQuantity);
            maxQuantity = Math.max(minQuantity, maxQuantity);
            defaultQuantity = Math.max(minQuantity, Math.min(maxQuantity, defaultQuantity));
        }

        public int clamp(int value) {
            return Math.max(minQuantity, Math.min(maxQuantity, value));
        }

        public boolean adjustable() {
            return !hideQuantityButtons && maxQuantity > minQuantity;
        }
    }

    public record SellProgressInfo(
            SellCategory category,
            double earned,
            int completedLevels,
            double currentMultiplier,
            String nextMultiplierDisplay,
            double previousGoal,
            double nextGoal,
            int percentage,
            String progressBar,
            boolean maxed
    ) {}

    public enum SellStatus {
        SUCCESS,
        NO_SELLABLE_ITEMS,
        TRANSACTION_FAILED
    }

    public record SellResult(SellStatus status, double totalPayout, Set<SellCategory> leveledUpCategories) {
        public boolean hasSales() {
            return status == SellStatus.SUCCESS;
        }

        public boolean transactionFailed() {
            return status == SellStatus.TRANSACTION_FAILED;
        }
    }

    private record PendingSellHistory(
            Material material,
            int amount,
            double payout
    ) {}

    private static final class PendingSale {
        private final Map<SellCategory, Double> currentProgress = new EnumMap<>(SellCategory.class);
        private final EnumMap<SellCategory, Double> earnedByCategory = new EnumMap<>(SellCategory.class);
        private final List<PendingSellHistory> history = new ArrayList<>();
        private final List<Integer> soldSlots = new ArrayList<>();
        private double totalPayout;
    }

    public record PurchaseResult(
            boolean success,
            PurchaseFailureReason reason,
            ShopItem item,
            int quantity,
            double totalPrice,
            Currency currency
    ) {
        public boolean insufficientMoney() {
            return reason == PurchaseFailureReason.NO_MONEY;
        }

        public boolean insufficientShards() {
            return reason == PurchaseFailureReason.NO_SHARDS;
        }

        public boolean inventoryFull() {
            return reason == PurchaseFailureReason.INVENTORY_FULL;
        }
    }

    private record ManagedSpawnerReward(String typeKey) {}

    private record RewardDeliveryResult(boolean success, String message, Throwable throwable) {
        static RewardDeliveryResult ok() {
            return new RewardDeliveryResult(true, "", null);
        }

        static RewardDeliveryResult failure(String message) {
            return new RewardDeliveryResult(false, message == null ? "" : message, null);
        }

        static RewardDeliveryResult failure(String message, Throwable throwable) {
            return new RewardDeliveryResult(false, message == null ? "" : message, throwable);
        }
    }

    public record ShopItem(
            String key,
            String menuSection,
            Material material,
            String displayName,
            List<String> lore,
            int slot,
            double pricePerUnit,
            Currency currency,
            String command,
            boolean giveItem,
            String permission,
            int minQuantity,
            int maxQuantity,
            int defaultQuantity,
            Boolean hideQuantityButtons,
            AmethystToolType amethystToolType,
            long amethystDurationSeconds,
            List<String> enchantments,
            Boolean glint,
            String serializedItemData
    ) {
        public ShopItem(
                String key,
                String menuSection,
                Material material,
                String displayName,
                List<String> lore,
                int slot,
                double pricePerUnit,
                Currency currency,
                String command,
                boolean giveItem,
                String permission,
                int minQuantity,
                int maxQuantity,
                int defaultQuantity,
                Boolean hideQuantityButtons,
                AmethystToolType amethystToolType,
                long amethystDurationSeconds
        ) {
            this(
                    key, menuSection, material, displayName, lore, slot, pricePerUnit,
                    currency, command, giveItem, permission, minQuantity, maxQuantity,
                    defaultQuantity, hideQuantityButtons, amethystToolType, amethystDurationSeconds,
                    List.of(), null, null
            );
        }

        /** Kept so callers that predate custom item data keep compiling. */
        public ShopItem(
                String key,
                String menuSection,
                Material material,
                String displayName,
                List<String> lore,
                int slot,
                double pricePerUnit,
                Currency currency,
                String command,
                boolean giveItem,
                String permission,
                int minQuantity,
                int maxQuantity,
                int defaultQuantity,
                Boolean hideQuantityButtons,
                AmethystToolType amethystToolType,
                long amethystDurationSeconds,
                List<String> enchantments,
                Boolean glint
        ) {
            this(
                    key, menuSection, material, displayName, lore, slot, pricePerUnit,
                    currency, command, giveItem, permission, minQuantity, maxQuantity,
                    defaultQuantity, hideQuantityButtons, amethystToolType, amethystDurationSeconds,
                    enchantments, glint, null
            );
        }

        public boolean hasCustomItemData() {
            return serializedItemData != null && !serializedItemData.isBlank();
        }

        public boolean isAmethystToolReward() {
            return amethystToolType != null;
        }
    }

    public record AuctionQuote(AuctionListing listing, double unitPrice) {}

    private final UltimateDonutSmp plugin;
    private final ShopPreferenceRepository preferenceRepository;
    private final Map<UUID, ShopPreference> preferenceCache = new ConcurrentHashMap<>();
    private final Map<String, ItemStack> customItemCache = new ConcurrentHashMap<>();

    public ShopManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.preferenceRepository = new ShopPreferenceRepository(plugin);
        this.preferenceRepository.initialize().join();
        reload();
    }

    public void reload() {
        customItemCache.clear();
        validateShopConfiguration();
    }

    public void shutdown() {
        preferenceRepository.shutdown();
        preferenceCache.clear();
    }

    public CompletableFuture<ShopPreference> loadPreference(UUID playerId) {
        return preferenceRepository.load(playerId).thenApply(preference -> {
            Player online = Bukkit.getPlayer(playerId);
            if (online != null && online.isOnline()) {
                preferenceCache.put(playerId, preference);
            }
            return preference;
        }).exceptionally(throwable -> {
            plugin.getLogger().log(Level.WARNING, "Failed to load shop preference for " + playerId, throwable);
            return getPreference(playerId);
        });
    }

    public ShopPreference getPreference(UUID playerId) {
        return preferenceCache.computeIfAbsent(
                playerId,
                ignored -> new ShopPreference(playerId, Set.of())
        );
    }

    public boolean areFavoritesEnabled() {
        return plugin.getConfigManager().getShop()
                .getBoolean("SHOP-GUI.FAVORITES.ENABLED", true);
    }

    public boolean toggleFavorite(UUID playerId, ShopItem item) {
        if (!areFavoritesEnabled()) {
            return false;
        }
        String favoriteId = favoriteId(item);
        ShopPreference updated = preferenceCache.compute(playerId, (ignored, current) -> {
            ShopPreference base = current == null
                    ? new ShopPreference(playerId, Set.of())
                    : current;
            return base.withFavorite(favoriteId, !base.favorites().contains(favoriteId));
        });
        boolean favorite = updated.favorites().contains(favoriteId);
        preferenceRepository.setFavorite(playerId, favoriteId, favorite).exceptionally(throwable -> {
            plugin.getLogger().log(Level.WARNING, "Failed to save shop favorite for " + playerId, throwable);
            return null;
        });
        return favorite;
    }

    public boolean isFavorite(UUID playerId, ShopItem item) {
        return getPreference(playerId).favorites().contains(favoriteId(item));
    }

    public List<ShopItem> loadFavoriteItems(UUID playerId) {
        Set<String> favorites = getPreference(playerId).favorites();
        if (favorites.isEmpty()) {
            return List.of();
        }
        return loadAllItems().stream()
                .filter(item -> favorites.contains(favoriteId(item)))
                .toList();
    }

    public List<ShopItem> loadAllItems() {
        Map<String, ShopItem> items = new java.util.LinkedHashMap<>();
        for (ShopCategory category : loadCategories()) {
            for (ShopItem item : loadMenuItems(category.menuSection())) {
                items.putIfAbsent(favoriteId(item), item);
            }
        }
        return List.copyOf(items.values());
    }

    public String favoriteId(ShopItem item) {
        if (item == null) {
            return "";
        }
        return item.menuSection().trim().toUpperCase(Locale.ROOT)
                + ":"
                + item.key().trim().toUpperCase(Locale.ROOT);
    }

    public AuctionQuote findBestAuctionQuote(Player buyer, ShopItem item) {
        if (buyer == null
                || item == null
                || !shouldDeliverConfiguredItem(item)
                || item.isAmethystToolReward()
                || !isAuctionPriceEnabled()
                || plugin.getAuctionHouseManager() == null
                || !plugin.getAuctionHouseManager().isEnabled()) {
            return null;
        }
        ItemStack desiredItem = createPurchasedItem(item, 1);
        return findBestAuctionQuote(
                plugin.getAuctionHouseManager().getActiveListings(AuctionHouseManager.AuctionSort.NEWEST),
                buyer.getUniqueId(),
                desiredItem,
                System.currentTimeMillis()
        );
    }

    public static AuctionQuote findBestAuctionQuote(
            List<AuctionListing> listings,
            UUID buyerId,
            ItemStack desiredItem,
            long now
    ) {
        return findBestAuctionQuote(listings, buyerId, desiredItem, now, ItemStack::isSimilar);
    }

    public static AuctionQuote findBestAuctionQuote(
            List<AuctionListing> listings,
            UUID buyerId,
            ItemStack desiredItem,
            long now,
            BiPredicate<ItemStack, ItemStack> similarity
    ) {
        if (listings == null || desiredItem == null || isAir(desiredItem.getType())) {
            return null;
        }
        BiPredicate<ItemStack, ItemStack> matcher = similarity == null ? ItemStack::isSimilar : similarity;
        return listings.stream()
                .filter(Objects::nonNull)
                .filter(AuctionListing::active)
                .filter(listing -> listing.expiresAt() > now)
                .filter(listing -> buyerId == null || !buyerId.equals(listing.sellerUuid()))
                .filter(listing -> listing.item() != null
                        && !isAir(listing.item().getType())
                        && listing.item().getAmount() > 0
                        && matcher.test(listing.item(), desiredItem))
                .map(listing -> new AuctionQuote(
                        listing,
                        listing.price() / Math.max(1, listing.item().getAmount())
                ))
                .filter(quote -> Double.isFinite(quote.unitPrice()) && quote.unitPrice() >= 0D)
                .min(Comparator.comparingDouble(AuctionQuote::unitPrice)
                        .thenComparingDouble(quote -> quote.listing().price())
                        .thenComparingLong(quote -> quote.listing().createdAt()))
                .orElse(null);
    }

    private static boolean isAir(Material material) {
        return material == null
                || material == Material.AIR
                || material == Material.CAVE_AIR
                || material == Material.VOID_AIR;
    }

    public boolean isAuctionPriceEnabled() {
        return plugin.getConfigManager().getShop()
                .getBoolean("SHOP-GUI.SHOW-AUCTION-PRICE", true);
    }

    public void cleanupPlayer(UUID playerId) {
        preferenceCache.remove(playerId);
    }

    public List<ShopCategory> loadCategories() {
        ConfigurationSection categoriesSection = plugin.getConfigManager().getShop()
                .getConfigurationSection("CATEGORIES");
        if (categoriesSection == null) {
            return List.of();
        }

        List<String> orderedKeys = new ArrayList<>();
        for (String configuredKey : categoriesSection.getStringList("ORDER")) {
            if (configuredKey == null || configuredKey.isBlank()) {
                continue;
            }

            String normalized = configuredKey.trim().toUpperCase(Locale.US);
            if (!orderedKeys.contains(normalized)) {
                orderedKeys.add(normalized);
            }
        }

        for (String key : categoriesSection.getKeys(false)) {
            String normalized = key.toUpperCase(Locale.US);
            if (CATEGORY_META_KEYS.contains(normalized) || orderedKeys.contains(normalized)) {
                continue;
            }
            orderedKeys.add(normalized);
        }

        List<ShopCategory> categories = new ArrayList<>();
        for (String key : orderedKeys) {
            ConfigurationSection section = categoriesSection.getConfigurationSection(key);
            if (section == null || !section.getBoolean("ENABLED", true)) {
                continue;
            }

            categories.add(new ShopCategory(
                    key,
                    normalizeMenuSection(section.getString("OPEN-MENU"), key),
                    ItemUtils.parseMaterial(section.getString("MATERIAL", "STONE")),
                    section.getString("DISPLAY-NAME", key),
                    section.getStringList("LORE"),
                    section.getInt("SLOT", 0)
            ));
        }

        return List.copyOf(categories);
    }

    public List<ShopItem> loadMenuItems(String menuSection) {
        List<ShopItem> items = new ArrayList<>();
        ConfigurationSection menuConfig = plugin.getConfigManager().getShop()
                .getConfigurationSection(menuSection);
        if (menuConfig == null) {
            return items;
        }

        ConfigurationSection itemsSection = menuConfig.getConfigurationSection("ITEMS");
        ConfigurationSection sourceSection = itemsSection != null ? itemsSection : menuConfig;
        Currency defaultCurrency = parseCurrency(menuConfig.getString("CURRENCY", "MONEY"));

        for (String key : sourceSection.getKeys(false)) {
            if (sourceSection == menuConfig && MENU_META_KEYS.contains(key.toUpperCase(Locale.US))) {
                continue;
            }

            ConfigurationSection itemSec = sourceSection.getConfigurationSection(key);
            if (itemSec == null || !itemSec.getBoolean("ENABLED", true) || !itemSec.contains("MATERIAL")) {
                continue;
            }

            Material material = ItemUtils.parseMaterial(itemSec.getString("MATERIAL", "STONE"));

            List<String> enchantments = new ArrayList<>();
            if (itemSec.isConfigurationSection("ENCHANTMENTS")) {
                ConfigurationSection enchSec = itemSec.getConfigurationSection("ENCHANTMENTS");
                if (enchSec != null) {
                    for (String enchKey : enchSec.getKeys(false)) {
                        int level = enchSec.getInt(enchKey, 1);
                        enchantments.add(enchKey + ":" + level);
                    }
                }
            } else if (itemSec.isList("ENCHANTMENTS")) {
                enchantments.addAll(itemSec.getStringList("ENCHANTMENTS"));
            }

            Boolean glint = itemSec.contains("GLINT") ? itemSec.getBoolean("GLINT") : null;

            items.add(new ShopItem(
                    key,
                    menuSection,
                    material,
                    itemSec.getString("DISPLAY-NAME", key),
                    itemSec.getStringList("LORE"),
                    itemSec.getInt("SLOT", 0),
                    itemSec.getDouble("PRICE-PER-UNIT", 0),
                    parseCurrency(itemSec.getString("CURRENCY", defaultCurrency.name())),
                    itemSec.getString("COMMAND", ""),
                    itemSec.getBoolean("GIVE-ITEM", true),
                    itemSec.getString("PERMISSION", ""),
                    itemSec.contains("MIN-QUANTITY") ? itemSec.getInt("MIN-QUANTITY") : -1,
                    itemSec.contains("MAX-QUANTITY") ? itemSec.getInt("MAX-QUANTITY") : -1,
                    itemSec.contains("DEFAULT-QUANTITY") ? itemSec.getInt("DEFAULT-QUANTITY") : -1,
                    itemSec.contains("HIDE-QUANTITY-BUTTONS") ? itemSec.getBoolean("HIDE-QUANTITY-BUTTONS") : null,
                    null,
                    -1L,
                    enchantments,
                    glint,
                    itemSec.getString("ITEM-DATA")
            ));
        }

        if ("SHARD-MENU".equalsIgnoreCase(menuSection)) {
            items.addAll(loadAmethystShardShopItems(menuSection));
        }

        items.sort(Comparator.comparingInt(ShopItem::slot).thenComparing(ShopItem::key, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(items);
    }

    private List<ShopItem> loadAmethystShardShopItems(String menuSection) {
        if (plugin.getAmethystToolsManager() == null || !plugin.getAmethystToolsManager().isEnabled()) {
            return List.of();
        }

        List<ShopItem> items = new ArrayList<>();
        for (AmethystToolType type : AmethystToolType.values()) {
            ConfigurationSection toolSection = plugin.getAmethystToolsManager().getToolSection(type);
            ConfigurationSection shopSection = toolSection == null
                    ? null
                    : toolSection.getConfigurationSection("SHARD-SHOP");
            if (toolSection == null || shopSection == null || !shopSection.getBoolean("ENABLED", false)) {
                continue;
            }

            long configuredDuration = shopSection.getLong(
                    "DURATION",
                    toolSection.getLong("DURATION", 86400L)
            );
            long duration = Math.max(1L, configuredDuration);
            double price = shopSection.getDouble("PRICE-PER-UNIT", 0D);
            if (!Double.isFinite(price) || price <= 0D) {
                continue;
            }
            List<String> lore = toolSection.getStringList("LORE").stream()
                    .map(line -> line.replace("{time}", NumberUtils.formatTimeLong(duration)))
                    .toList();

            items.add(new ShopItem(
                    "AMETHYST-" + type.getConfigKey(),
                    menuSection,
                    ItemUtils.parseMaterial(toolSection.getString("MATERIAL", "STONE")),
                    toolSection.getString("NAME", type.getDisplayName()),
                    lore,
                    shopSection.getInt("SLOT", 0),
                    price,
                    parseCurrency(shopSection.getString("CURRENCY", Currency.SHARD.name())),
                    "",
                    true,
                    shopSection.getString("PERMISSION", ""),
                    shopSection.getInt("MIN-QUANTITY", 1),
                    shopSection.getInt("MAX-QUANTITY", 1),
                    shopSection.getInt("DEFAULT-QUANTITY", 1),
                    shopSection.getBoolean("HIDE-QUANTITY-BUTTONS", true),
                    type,
                    duration
            ));
        }
        return items;
    }

    public ShopRestriction getPurchaseRestriction(ShopItem item) {
        ConfigurationSection restrictions = plugin.getConfigManager().getMenus()
                .getConfigurationSection("PURCHASE-SHOP-MENU.RESTRICTIONS");

        int minQuantity = 1;
        int maxQuantity = 64;
        int defaultQuantity = 1;
        boolean hideQuantityButtons = false;

        if (restrictions != null) {
            ConfigurationSection defaultSection = restrictions.getConfigurationSection("DEFAULT");
            if (defaultSection != null) {
                minQuantity = defaultSection.getInt("MIN_QUANTITY", minQuantity);
                maxQuantity = defaultSection.getInt("MAX_QUANTITY", maxQuantity);
                defaultQuantity = defaultSection.getInt("DEFAULT_QUANTITY", defaultQuantity);
                hideQuantityButtons = defaultSection.getBoolean("HIDE_QUANTITY_BUTTONS", hideQuantityButtons);
            }

            if (item != null) {
                ConfigurationSection materialSection = restrictions.getConfigurationSection(item.material().name());
                if (materialSection != null) {
                    minQuantity = materialSection.getInt("MIN_QUANTITY", minQuantity);
                    maxQuantity = materialSection.getInt("MAX_QUANTITY", maxQuantity);
                    defaultQuantity = materialSection.getInt("DEFAULT_QUANTITY", defaultQuantity);
                    hideQuantityButtons = materialSection.getBoolean("HIDE_QUANTITY_BUTTONS", hideQuantityButtons);
                }
            }
        }

        if (item != null) {
            if (item.minQuantity() > 0) {
                minQuantity = item.minQuantity();
            }
            if (item.maxQuantity() > 0) {
                maxQuantity = item.maxQuantity();
            }
            if (item.defaultQuantity() > 0) {
                defaultQuantity = item.defaultQuantity();
            }
            if (item.hideQuantityButtons() != null) {
                hideQuantityButtons = item.hideQuantityButtons();
            }
        }

        return new ShopRestriction(minQuantity, maxQuantity, defaultQuantity, hideQuantityButtons);
    }

    public PurchaseResult previewPurchase(Player player, ShopItem item, int amount) {
        return validatePurchase(player, item, amount);
    }

    /**
     * Process a shop purchase and return a rich result for menu feedback.
     */
    public PurchaseResult purchase(Player player, ShopItem item, int amount) {
        PurchaseResult preview = validatePurchase(player, item, amount);
        if (!preview.success()) {
            return preview;
        }

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            return failPurchase(item, amount, preview.totalPrice(), PurchaseFailureReason.NO_PLAYER_DATA);
        }

        long shardCost = preview.currency() == Currency.SHARD ? Math.round(preview.totalPrice()) : 0L;
        if (preview.currency() == Currency.SHARD) {
            if (!data.removeShards(shardCost)) {
                return failPurchase(item, amount, preview.totalPrice(), PurchaseFailureReason.NO_SHARDS);
            }
        } else {
            var withdrawResult = plugin.getEconomyManager().withdraw(player, preview.totalPrice(), EconomyReason.SHOP_PURCHASE);
            if (!withdrawResult.success()) {
                return failPurchase(item, amount, preview.totalPrice(), PurchaseFailureReason.NO_MONEY);
            }
        }

        RewardDeliveryResult commandReward = deliverCommandReward(player, item, preview.quantity());
        if (!commandReward.success()) {
            refundPurchase(player, data, preview, shardCost);
            logRewardFailure(player, item, preview, commandReward);
            return failPurchase(item, preview.quantity(), preview.totalPrice(), PurchaseFailureReason.REWARD_FAILED);
        }

        RewardDeliveryResult itemReward = deliverItemReward(player, item, preview.quantity());
        if (!itemReward.success()) {
            refundPurchase(player, data, preview, shardCost);
            logRewardFailure(player, item, preview, itemReward);
            return failPurchase(item, preview.quantity(), preview.totalPrice(), PurchaseFailureReason.REWARD_FAILED);
        }

        if (preview.currency() != Currency.SHARD) {
            data.addMoneySpent(preview.totalPrice());
        }

        String itemName = item.displayName() != null && !item.displayName().isBlank()
                ? ColorUtils.strip(item.displayName())
                : plugin.getWorthManager().prettifyMaterial(item.material());
        String formattedPrice = preview.currency() == Currency.SHARD
                ? preview.totalPrice() + " Shards"
                : plugin.getCurrencyManager().formatMoney(preview.totalPrice());
        plugin.getPlayerLogsManager().log(
                player.getUniqueId(),
                player.getName(),
                "Shop",
                "SHOP_BUY",
                "Bought " + itemName + " x" + preview.quantity() + " for " + formattedPrice
        );

        return preview;
    }

    private RewardDeliveryResult deliverItemReward(Player player, ShopItem item, int quantity) {
        if (!shouldDeliverConfiguredItem(item)) {
            return RewardDeliveryResult.ok();
        }

        if (item.isAmethystToolReward()) {
            if (!isAmethystRewardAvailable(item)) {
                return RewardDeliveryResult.failure("amethyst tools is disabled or the configured tool is unavailable.");
            }

            List<ItemStack> rewards = new ArrayList<>();
            for (int index = 0; index < quantity; index++) {
                ItemStack reward = plugin.getAmethystToolsManager().createTool(
                        item.amethystToolType(),
                        player.getUniqueId(),
                        item.amethystDurationSeconds()
                );
                if (reward == null) {
                    return RewardDeliveryResult.failure(
                            "failed to create amethyst tool reward: " + item.amethystToolType().name()
                    );
                }
                rewards.add(reward);
            }
            plugin.getWorthManager().stripStorageWorthDisplayForNativePickup(player);
            for (ItemStack reward : rewards) {
                player.getInventory().addItem(reward).values().forEach(left ->
                        player.getWorld().dropItemNaturally(player.getLocation(), left));
            }
        } else {
            plugin.getWorthManager().stripStorageWorthDisplayForNativePickup(player);
            ItemStack stack = createPurchasedItem(item, quantity);
            player.getInventory().addItem(stack).values().forEach(left ->
                    player.getWorld().dropItemNaturally(player.getLocation(), left));
        }

        plugin.getWorthManager().syncWorthDisplay(player);
        player.updateInventory();
        return RewardDeliveryResult.ok();
    }

    private RewardDeliveryResult deliverCommandReward(Player player, ShopItem item, int quantity) {
        if (item == null || item.command() == null || item.command().isBlank()) {
            return RewardDeliveryResult.ok();
        }

        ManagedSpawnerReward spawnerReward = parseManagedSpawnerReward(item.command());
        if (spawnerReward != null) {
            try {
                var result = plugin.getSpawnerManager().giveSpawner(player, spawnerReward.typeKey(), quantity);
                return result.success()
                        ? RewardDeliveryResult.ok()
                        : RewardDeliveryResult.failure(result.message());
            } catch (RuntimeException exception) {
                return RewardDeliveryResult.failure(
                        "managed spawner reward threw an exception: " + item.command(),
                        exception
                );
            }
        }

        String command = resolveShopCommand(player, item.command(), quantity);
        try {
            boolean dispatched = plugin.getSpigotScheduler().dispatchConsoleCommand(command);
            return dispatched
                    ? RewardDeliveryResult.ok()
                    : RewardDeliveryResult.failure("command returned false: " + command);
        } catch (RuntimeException exception) {
            return RewardDeliveryResult.failure("command threw an exception: " + command, exception);
        }
    }

    private static ManagedSpawnerReward parseManagedSpawnerReward(String command) {
        if (command == null || command.isBlank()) {
            return null;
        }

        String normalized = command.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1).trim();
        }

        String[] parts = normalized.split("\\s+");
        if (parts.length != 5
                || !parts[0].equalsIgnoreCase("spawner")
                || !parts[1].equalsIgnoreCase("give")
                || !isPlayerPlaceholder(parts[2])
                || !isAmountPlaceholder(parts[4])) {
            return null;
        }

        return new ManagedSpawnerReward(parts[3]);
    }

    static boolean isManagedSpawnerRewardCommand(String command) {
        return parseManagedSpawnerReward(command) != null;
    }

    static boolean shouldDeliverConfiguredItem(ShopItem item) {
        return item != null
                && item.giveItem()
                && !isManagedSpawnerRewardCommand(item.command());
    }

    private static boolean isPlayerPlaceholder(String token) {
        return token != null
                && (token.equalsIgnoreCase("{username}") || token.equalsIgnoreCase("{player}"));
    }

    private static boolean isAmountPlaceholder(String token) {
        return token != null && token.equalsIgnoreCase("{amount}");
    }

    private String resolveShopCommand(Player player, String command, int quantity) {
        return command
                .replace("{username}", player.getName())
                .replace("{player}", player.getName())
                .replace("{amount}", String.valueOf(quantity));
    }

    private void refundPurchase(Player player, PlayerData data, PurchaseResult purchase, long shardCost) {
        if (purchase.currency() == Currency.SHARD) {
            data.addShards(shardCost);
            return;
        }

        var refundResult = plugin.getEconomyManager().deposit(player, purchase.totalPrice(), EconomyReason.SHOP_REFUND);
        if (!refundResult.success()) {
            plugin.getLogger().warning("[ShopManager] Failed to refund shop purchase for "
                    + player.getName() + " after reward delivery failed.");
        }
    }

    private void logRewardFailure(
            Player player,
            ShopItem item,
            PurchaseResult purchase,
            RewardDeliveryResult result
    ) {
        String message = "[shopmanager] failed to deliver shop reward for "
                + player.getName()
                + " item=" + (item == null ? "unknown" : item.key())
                + " quantity=" + purchase.quantity()
                + " price=" + purchase.totalPrice()
                + " currency=" + purchase.currency()
                + " reason=" + ColorUtils.strip(result.message());
        if (result.throwable() != null) {
            plugin.getLogger().log(Level.WARNING, message, result.throwable());
        } else {
            plugin.getLogger().warning(message);
        }
    }

    private PurchaseResult validatePurchase(Player player, ShopItem item, int amount) {
        if (player == null || item == null) {
            return failPurchase(item, amount, 0, PurchaseFailureReason.INVALID_ITEM);
        }

        ShopRestriction restriction = getPurchaseRestriction(item);
        if (amount < restriction.minQuantity() || amount > restriction.maxQuantity()) {
            return failPurchase(item, amount, item.pricePerUnit() * Math.max(1, amount), PurchaseFailureReason.INVALID_QUANTITY);
        }

        if (item.permission() != null && !item.permission().isBlank() && !PermissionUtils.has(player, item.permission())) {
            return failPurchase(item, amount, item.pricePerUnit() * amount, PurchaseFailureReason.NO_PERMISSION);
        }

        if (item.pricePerUnit() < 0) {
            return failPurchase(item, amount, 0, PurchaseFailureReason.INVALID_ITEM);
        }

        if (item.isAmethystToolReward() && !isAmethystRewardAvailable(item)) {
            return failPurchase(item, amount, item.pricePerUnit() * amount, PurchaseFailureReason.INVALID_ITEM);
        }

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            return failPurchase(item, amount, item.pricePerUnit() * amount, PurchaseFailureReason.NO_PLAYER_DATA);
        }

        double totalPrice = item.pricePerUnit() * amount;
        if (item.currency() == Currency.SHARD) {
            long shardCost = Math.round(totalPrice);
            if (!data.hasShards(shardCost)) {
                return failPurchase(item, amount, totalPrice, PurchaseFailureReason.NO_SHARDS);
            }
        } else if (!plugin.getEconomyManager().has(player, totalPrice)) {
            return failPurchase(item, amount, totalPrice, PurchaseFailureReason.NO_MONEY);
        }

        if (shouldDeliverConfiguredItem(item) && !canFitPurchasedItem(player, item, amount)) {
            return failPurchase(item, amount, totalPrice, PurchaseFailureReason.INVENTORY_FULL);
        }

        return new PurchaseResult(true, null, item, amount, totalPrice, item.currency());
    }

    private PurchaseResult failPurchase(ShopItem item, int quantity, double totalPrice, PurchaseFailureReason reason) {
        return new PurchaseResult(
                false,
                reason,
                item,
                Math.max(0, quantity),
                Math.max(0, totalPrice),
                item == null ? Currency.MONEY : item.currency()
        );
    }

    static Currency parseCurrency(String currencyName) {
        if (currencyName == null || currencyName.isBlank()) {
            return Currency.MONEY;
        }

        return "SHARD".equalsIgnoreCase(currencyName) || "SHARDS".equalsIgnoreCase(currencyName)
                ? Currency.SHARD
                : Currency.MONEY;
    }

    private String normalizeMenuSection(String rawMenuSection, String fallbackKey) {
        String raw = rawMenuSection == null || rawMenuSection.isBlank()
                ? fallbackKey + "-MENU"
                : rawMenuSection;
        return raw.replace("{", "").replace("}", "").trim().toUpperCase(Locale.US);
    }

    private ItemStack createPurchasedItem(ShopItem item, int amount) {
        ItemStack custom = createCustomItem(item);
        if (custom != null) {
            custom.setAmount(Math.max(1, amount));
            return custom;
        }

        ItemStack stack = new ItemStack(item.material(), Math.max(1, amount));
        if (item.enchantments() != null && !item.enchantments().isEmpty()) {
            ItemUtils.addEnchantments(stack, item.enchantments());
        }
        if (item.glint() != null) {
            ItemUtils.setGlint(stack, item.glint());
        }
        return stack;
    }

    /**
     * The exact item an admin stored through the shop editor, or null when the entry is a plain
     * MATERIAL one. Menus use this so the icon matches what the buyer actually receives.
     */
    public ItemStack createCustomItem(ShopItem item) {
        if (item == null || !item.hasCustomItemData()) {
            return null;
        }

        ItemStack cached = customItemCache.get(item.serializedItemData());
        if (cached != null) {
            return cached.clone();
        }

        try {
            ItemStack stored = ItemSerializationUtils.deserialize(item.serializedItemData());
            if (stored == null || stored.getType().isAir()) {
                return null;
            }
            customItemCache.put(item.serializedItemData(), stored.clone());
            return stored;
        } catch (IOException | ClassNotFoundException | IllegalArgumentException exception) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to read ITEM-DATA for shop item " + item.key() + " in " + item.menuSection(), exception);
            return null;
        }
    }

    // ── Shop editor ─────────────────────────────────────────────────────────────

    /** Outcome of an edit made through the shop editor menu. */
    public record EditResult(boolean success, String message) {}

    /** An item with its shop price, once a {@code [PRICE] <amount>} rename has been read off it. */
    public record PricedItem(ItemStack item, Double price) {}

    /**
     * Reads a {@code [PRICE] <amount>} rename off an item and hands back the item with that rename
     * removed, so the price tag never ends up baked into what the buyer receives. Items without the
     * tag come back untouched and with a null price.
     */
    public PricedItem readPriceTag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return new PricedItem(item, null);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return new PricedItem(item, null);
        }

        Double parsed = parsePriceTag(ColorUtils.strip(meta.getDisplayName()));
        if (parsed == null) {
            return new PricedItem(item, null);
        }

        ItemStack cleaned = item.clone();
        ItemMeta cleanedMeta = cleaned.getItemMeta();
        if (cleanedMeta != null) {
            cleanedMeta.setDisplayName(null);
            cleaned.setItemMeta(cleanedMeta);
        }
        return new PricedItem(cleaned, parsed);
    }

    /**
     * Pulls the amount out of a {@code [PRICE] 250} rename, or null when the name is not a price tag
     * or the amount will not do as a price.
     */
    public static Double parsePriceTag(String displayName) {
        if (displayName == null) {
            return null;
        }

        String trimmed = displayName.trim();
        if (!trimmed.toUpperCase(Locale.US).startsWith(PRICE_TAG_PREFIX)) {
            return null;
        }

        double parsed;
        try {
            parsed = Double.parseDouble(trimmed.substring(PRICE_TAG_PREFIX.length()).trim().replace(",", ""));
        } catch (NumberFormatException exception) {
            return null;
        }

        return parsed > 0 && Double.isFinite(parsed) ? parsed : null;
    }

    /** Menu sections an admin can open in the editor, in the order the categories menu shows them. */
    public List<String> getEditableMenuSections() {
        List<String> sections = new ArrayList<>();
        for (ShopCategory category : loadCategories()) {
            if (!sections.contains(category.menuSection())) {
                sections.add(category.menuSection());
            }
        }
        return List.copyOf(sections);
    }

    /** Turns what an admin typed ("end", "end-menu", "{end-menu}") into a real shop.yml section. */
    public String resolveMenuSection(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normalized = input.replace("{", "").replace("}", "").trim().toUpperCase(Locale.US);
        List<String> sections = getEditableMenuSections();
        if (sections.contains(normalized)) {
            return normalized;
        }

        String suffixed = normalized.endsWith("-MENU") ? normalized : normalized + "-MENU";
        return sections.contains(suffixed) ? suffixed : null;
    }

    public int getMenuSize(String menuSection) {
        ConfigurationSection menuConfig = plugin.getConfigManager().getShop()
                .getConfigurationSection(menuSection);
        int configured = menuConfig == null ? 27 : menuConfig.getInt("SIZE", 27);
        int bounded = Math.max(9, Math.min(54, configured));
        return bounded % 9 == 0 ? bounded : ((bounded / 9) + 1) * 9;
    }

    /** Where a shop menu puts its back button, which the editor reuses for its close button. */
    public int getBackButtonSlot(String menuSection) {
        ConfigurationSection menuConfig = plugin.getConfigManager().getShop()
                .getConfigurationSection(menuSection);
        int size = getMenuSize(menuSection);
        return menuConfig == null ? size - 9 : menuConfig.getInt("BACK-BUTTON-SLOT", size - 9);
    }

    /**
     * Slots a shop menu keeps for its own back and paging buttons. An item parked on one of these is
     * what makes ShopMenu give up on configured slots and fall back to paging, so the editor refuses
     * to place there.
     */
    public Set<Integer> getReservedSlots(String menuSection) {
        ConfigurationSection menuConfig = plugin.getConfigManager().getShop()
                .getConfigurationSection(menuSection);
        int size = getMenuSize(menuSection);
        if (menuConfig == null) {
            return Set.of();
        }

        Set<Integer> reserved = new HashSet<>();
        reserved.add(getBackButtonSlot(menuSection));
        reserved.add(menuConfig.getInt("FIRST-PAGE-SLOT", size - 8));
        reserved.add(menuConfig.getInt("PREVIOUS-PAGE-SLOT", size - 7));
        reserved.add(menuConfig.getInt("PAGE-INFO-SLOT", size - 5));
        reserved.add(menuConfig.getInt("NEXT-PAGE-SLOT", size - 3));
        reserved.add(menuConfig.getInt("LAST-PAGE-SLOT", size - 2));
        return Set.copyOf(reserved);
    }

    /**
     * Stores {@code item} in {@code slot} of {@code menuSection}, replacing whatever was there. The
     * whole item is written as ITEM-DATA so enchantments and every other bit of item data survive the
     * round trip, with MATERIAL and friends written alongside so the entry stays readable in shop.yml.
     */
    public EditResult upsertMenuItem(String menuSection, int slot, ItemStack item, Double price) {
        if (item == null || item.getType().isAir()) {
            return new EditResult(false, "&cSelect an item from your inventory first.");
        }

        ConfigurationSection menuConfig = plugin.getConfigManager().getOriginalShop()
                .getConfigurationSection(menuSection);
        if (menuConfig == null) {
            return new EditResult(false, "&cThat shop menu no longer exists.");
        }

        if (slot < 0 || slot >= getMenuSize(menuSection)) {
            return new EditResult(false, "&cThat slot is outside this shop menu.");
        }

        if (getReservedSlots(menuSection).contains(slot)) {
            return new EditResult(false, "&cThat slot belongs to the menu buttons. Pick another one.");
        }

        ItemStack storedItem = item.clone();
        storedItem.setAmount(1);

        CrashProtectionManager.ValidationResult safetyResult = plugin.getCrashProtectionManager()
                .validateForStorage(storedItem, CrashProtectionManager.Context.SHOP);
        if (!safetyResult.allowed()) {
            plugin.getCrashProtectionManager().logBlockedItem(
                    "shop " + menuSection + " slot " + slot,
                    storedItem,
                    CrashProtectionManager.Context.SHOP,
                    safetyResult
            );
            return new EditResult(false, plugin.getConfigManager().getMessageOrDefault(
                    "CRASH_PROTECTION.ITEM_BLOCKED",
                    "&cThat item cannot be used here because its data looks unsafe. &7context: &f{context}&7. reason: &f{reason}",
                    "{context}", CrashProtectionManager.Context.SHOP.displayName(),
                    "{reason}", safetyResult.reason()
            ));
        }

        double resolvedPrice = price != null ? price : getWorth(storedItem);
        if (resolvedPrice <= 0) {
            return new EditResult(false, "&cThat item has no worth entry, so it needs a price. "
                    + "Rename it to &f[PRICE] 250&c and place it again.");
        }

        String serializedItemData;
        try {
            serializedItemData = ItemSerializationUtils.serialize(storedItem);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to serialize shop item for " + menuSection + " slot " + slot, exception);
            return new EditResult(false, "&cFailed to store that item in the shop.");
        }

        ConfigurationSection itemsSection = menuConfig.getConfigurationSection("ITEMS");
        String basePath = menuSection + (itemsSection != null ? ".ITEMS." : ".");
        String existingKey = findItemKeyBySlot(menuSection, slot);
        boolean editing = existingKey != null;
        String itemKey = editing ? existingKey : generateItemKey(menuSection, storedItem.getType(), slot);
        String path = basePath + itemKey;

        FileConfiguration shopConfig = plugin.getConfigManager().getOriginalShop();
        ItemMeta meta = storedItem.getItemMeta();
        String displayName = editorDisplayName(meta, storedItem.getType());

        shopConfig.set(path + ".MATERIAL", storedItem.getType().name());
        shopConfig.set(path + ".DISPLAY-NAME", displayName);
        shopConfig.set(path + ".SLOT", slot);
        shopConfig.set(path + ".PRICE-PER-UNIT", resolvedPrice);
        shopConfig.set(path + ".ENCHANTMENTS", editorEnchantments(storedItem));
        shopConfig.set(path + ".ITEM-DATA", serializedItemData);

        if (!plugin.getConfigManager().saveShop()) {
            return new EditResult(false, "&cFailed to save shop.yml while updating that slot.");
        }

        reload();
        return new EditResult(true, "&aStored &f" + ColorUtils.strip(displayName)
                + "&a in slot &f" + slot + "&a at &f" + NumberUtils.format(resolvedPrice) + "&a.");
    }

    /** Drops whatever item sits in {@code slot} of {@code menuSection} out of shop.yml. */
    public EditResult removeMenuItem(String menuSection, int slot) {
        ConfigurationSection menuConfig = plugin.getConfigManager().getOriginalShop()
                .getConfigurationSection(menuSection);
        if (menuConfig == null) {
            return new EditResult(false, "&cThat shop menu no longer exists.");
        }

        String itemKey = findItemKeyBySlot(menuSection, slot);
        if (itemKey == null) {
            return new EditResult(false, "&cThere is nothing in slot &f" + slot + "&c.");
        }

        ConfigurationSection itemsSection = menuConfig.getConfigurationSection("ITEMS");
        String path = menuSection + (itemsSection != null ? ".ITEMS." : ".") + itemKey;
        plugin.getConfigManager().getOriginalShop().set(path, null);

        if (!plugin.getConfigManager().saveShop()) {
            return new EditResult(false, "&cFailed to save shop.yml while clearing that slot.");
        }

        reload();
        return new EditResult(true, "&aCleared slot &f" + slot + "&a.");
    }

    private String findItemKeyBySlot(String menuSection, int slot) {
        ConfigurationSection menuConfig = plugin.getConfigManager().getOriginalShop()
                .getConfigurationSection(menuSection);
        if (menuConfig == null) {
            return null;
        }

        ConfigurationSection itemsSection = menuConfig.getConfigurationSection("ITEMS");
        ConfigurationSection sourceSection = itemsSection != null ? itemsSection : menuConfig;
        for (String key : sourceSection.getKeys(false)) {
            if (sourceSection == menuConfig && MENU_META_KEYS.contains(key.toUpperCase(Locale.US))) {
                continue;
            }

            ConfigurationSection itemSec = sourceSection.getConfigurationSection(key);
            if (itemSec != null && itemSec.contains("MATERIAL") && itemSec.getInt("SLOT", -1) == slot) {
                return key;
            }
        }
        return null;
    }

    private String generateItemKey(String menuSection, Material material, int slot) {
        ConfigurationSection menuConfig = plugin.getConfigManager().getOriginalShop()
                .getConfigurationSection(menuSection);
        ConfigurationSection itemsSection = menuConfig == null ? null : menuConfig.getConfigurationSection("ITEMS");
        ConfigurationSection sourceSection = itemsSection != null ? itemsSection : menuConfig;

        String base = material.name().replace('_', '-') + "-ITEM";
        if (sourceSection == null || !sourceSection.contains(base)) {
            return base;
        }
        return base + "-" + slot;
    }

    private String editorDisplayName(ItemMeta meta, Material material) {
        if (meta == null || !meta.hasDisplayName()) {
            return "&f" + plugin.getWorthManager().prettifyMaterial(material);
        }
        return meta.getDisplayName().replace('\u00A7', '&');
    }

    private List<String> editorEnchantments(ItemStack item) {
        if (item == null || item.getEnchantments().isEmpty()) {
            return List.of();
        }

        List<String> enchantments = new ArrayList<>();
        for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            enchantments.add(entry.getKey().getKey().getKey() + ":" + entry.getValue());
        }
        return enchantments;
    }

    private boolean canFitPurchasedItem(Player player, ShopItem item, int amount) {
        if (item.isAmethystToolReward()) {
            int emptySlots = 0;
            for (ItemStack current : player.getInventory().getStorageContents()) {
                if (current == null || current.getType().isAir()) {
                    emptySlots++;
                }
            }
            return emptySlots >= amount;
        }

        ItemStack simulated = createPurchasedItem(item, amount);
        ItemStack[] storage = player.getInventory().getStorageContents();
        int remaining = simulated.getAmount();
        ItemStack singleItem = simulated.clone();
        singleItem.setAmount(1);

        for (ItemStack current : storage) {
            if (current == null || current.getType().isAir()) {
                remaining -= simulated.getMaxStackSize();
            } else if (canStack(plugin.getWorthManager().stripWorthDisplay(current), singleItem)) {
                remaining -= Math.max(0, current.getMaxStackSize() - current.getAmount());
            }

            if (remaining <= 0) {
                return true;
            }
        }
        return remaining <= 0;
    }

    private boolean isAmethystRewardAvailable(ShopItem item) {
        return item != null
                && item.amethystToolType() != null
                && plugin.getAmethystToolsManager() != null
                && plugin.getAmethystToolsManager().isEnabled()
                && plugin.getAmethystToolsManager().getToolSection(item.amethystToolType()) != null;
    }

    private boolean canStack(ItemStack first, ItemStack second) {
        return first.isSimilar(second) && first.getAmount() < first.getMaxStackSize();
    }

    private void validateShopConfiguration() {
        validateAmethystShardShopConfiguration();
        ConfigurationSection shopConfig = plugin.getConfigManager().getShop();
        ConfigurationSection categoriesSection = shopConfig.getConfigurationSection("CATEGORIES");
        if (categoriesSection == null) {
            plugin.getLogger().warning("shop.yml is missing the CATEGORIES section.");
            return;
        }

        Set<Integer> usedCategorySlots = new HashSet<>();
        for (ShopCategory category : loadCategories()) {
            if (!usedCategorySlots.add(category.slot())) {
                plugin.getLogger().warning("Duplicate shop category slot detected at " + category.slot()
                        + " for category " + category.key() + ".");
            }

            if (shopConfig.getConfigurationSection(category.menuSection()) == null) {
                plugin.getLogger().warning("Shop category " + category.key()
                        + " points to missing menu section " + category.menuSection() + ".");
            }
        }

        for (ShopCategory category : loadCategories()) {
            Set<Integer> usedItemSlots = new HashSet<>();
            for (ShopItem item : loadMenuItems(category.menuSection())) {
                if (item.pricePerUnit() < 0) {
                    plugin.getLogger().warning("Shop item " + item.key() + " in " + category.menuSection()
                            + " has a negative price-per-unit.");
                }

                if (item.slot() >= 0 && !usedItemSlots.add(item.slot())) {
                    plugin.getLogger().warning("Duplicate shop item slot detected at " + item.slot()
                            + " in " + category.menuSection() + ".");
                }

                if (item.currency() == Currency.MONEY) {
                    double worth = getWorth(item.material());
                    if (worth > item.pricePerUnit()) {
                        plugin.getLogger().warning("Potential shop arbitrage detected for " + item.material().name()
                                + " in " + category.menuSection() + ": buy " + plugin.getCurrencyManager().formatMoney(item.pricePerUnit())
                                + " but worth " + plugin.getCurrencyManager().formatMoney(worth) + ".");
                    }
                }
            }
        }
    }

    private void validateAmethystShardShopConfiguration() {
        if (plugin.getAmethystToolsManager() == null) {
            return;
        }
        for (AmethystToolType type : AmethystToolType.values()) {
            ConfigurationSection toolSection = plugin.getAmethystToolsManager().getToolSection(type);
            ConfigurationSection shopSection = toolSection == null
                    ? null
                    : toolSection.getConfigurationSection("SHARD-SHOP");
            if (shopSection == null || !shopSection.getBoolean("ENABLED", false)) {
                continue;
            }
            double price = shopSection.getDouble("PRICE-PER-UNIT", 0D);
            if (!Double.isFinite(price) || price <= 0D) {
                plugin.getLogger().warning("Amethyst tool " + type.name()
                        + " is enabled for /shardshop but price-per-unit is not greater than zero.");
            }
        }
    }

    public double getWorth(Material material) {
        return plugin.getWorthManager().getWorth(material);
    }

    public double getWorth(ItemStack item) {
        return plugin.getWorthManager().getWorth(item);
    }

    public void syncWorthDisplay(Player player) {
        plugin.getWorthManager().syncWorthDisplay(player);
    }

    public void clearWorthDisplay(Player player) {
        plugin.getWorthManager().clearWorthDisplay(player);
    }

    public void sanitizeInventory(Inventory inventory) {
        plugin.getWorthManager().sanitizeInventory(inventory);
    }

    public ItemStack stripWorthDisplay(ItemStack item) {
        return plugin.getWorthManager().stripWorthDisplay(item);
    }

    public String getWorthLoreLine(ItemStack item) {
        return plugin.getWorthManager().getWorthLoreLine(item);
    }

    private double findWorthRecursively(ConfigurationSection section, ItemStack item) {
        if (section == null || item == null || item.getType().isAir()) {
            return -1;
        }

        double directWorth = getDirectWorth(section, item);
        if (directWorth >= 0) {
            return directWorth;
        }

        for (String key : section.getKeys(false)) {
            if (!section.isConfigurationSection(key)) {
                continue;
            }

            double nestedWorth = findWorthRecursively(section.getConfigurationSection(key), item);
            if (nestedWorth >= 0) {
                return nestedWorth;
            }
        }
        return -1;
    }

    private double getDirectWorth(ConfigurationSection section, ItemStack item) {
        double specificWorth = resolveSpecificItemWorth(section, item);
        if (specificWorth >= 0) {
            return specificWorth;
        }

        String materialKey = item.getType().name();
        if (section.contains(materialKey) && !section.isConfigurationSection(materialKey)) {
            return section.getDouble(materialKey, -1);
        }
        return -1;
    }

    private double resolveSpecificItemWorth(ConfigurationSection section, ItemStack item) {
        if (item.getType() == Material.ENCHANTED_BOOK) {
            return resolveEnchantedBookWorth(section, item);
        }

        if (item.getItemMeta() instanceof PotionMeta meta) {
            return resolvePotionWorth(section, item.getType(), meta);
        }

        return -1;
    }

    private double resolveEnchantedBookWorth(ConfigurationSection section, ItemStack item) {
        if (!(item.getItemMeta() instanceof EnchantmentStorageMeta meta) || meta.getStoredEnchants().isEmpty()) {
            return -1;
        }

        double total = 0;
        boolean matched = false;
        for (var enchantmentEntry : meta.getStoredEnchants().entrySet()) {
            String enchantmentKey = enchantmentEntry.getKey().getKey().getKey()
                    .toUpperCase(Locale.US)
                    .replace('-', '_');
            String worthKey = item.getType().name() + ":" + enchantmentKey + ":" + enchantmentEntry.getValue();
            if (section.contains(worthKey) && !section.isConfigurationSection(worthKey)) {
                total += section.getDouble(worthKey, 0);
                matched = true;
            }
        }

        return matched ? total : -1;
    }

    private double resolvePotionWorth(ConfigurationSection section, Material material, PotionMeta meta) {
        PotionType potionType = meta.getBasePotionType();
        if (potionType == null) {
            return -1;
        }

        String potionKey = buildPotionWorthKey(material, potionType);
        if (potionKey == null || !section.contains(potionKey) || section.isConfigurationSection(potionKey)) {
            return -1;
        }

        return section.getDouble(potionKey, -1);
    }

    private String buildPotionWorthKey(Material material, PotionType potionType) {
        String potionName = potionType.name().toUpperCase(Locale.US);
        boolean upgraded = false;

        if (potionName.startsWith("STRONG_")) {
            potionName = potionName.substring("STRONG_".length());
            upgraded = true;
        } else if (potionName.startsWith("LONG_")) {
            potionName = potionName.substring("LONG_".length());
        }

        StringBuilder builder = new StringBuilder(material.name()).append(':').append(potionName);
        if (upgraded) {
            builder.append(":2");
        }
        return builder.toString();
    }

    public SellCategory getSellCategory(ItemStack item) {
        return plugin.getWorthManager().getSellCategory(item);
    }

    public Map<SellCategory, Double> getSellProgress(java.util.UUID uuid) {
        return plugin.getDatabaseManager().getSellProgress(uuid);
    }

    public SellProgressInfo getSellProgressInfo(java.util.UUID uuid, SellCategory category) {
        return getSellProgressInfo(getSellProgress(uuid), category);
    }

    public SellProgressInfo getSellProgressInfo(Map<SellCategory, Double> progress, SellCategory category) {
        List<Long> levels = getSellProgressLevels();
        double earned = progress.getOrDefault(category, 0D);
        int completedLevels = getCompletedLevels(earned, levels);
        boolean maxed = completedLevels >= levels.size();
        double currentMultiplier = 1.0 + (completedLevels * 0.1);
        double previousGoal = completedLevels <= 0 ? 0 : levels.get(completedLevels - 1);
        double nextGoal = maxed ? levels.get(levels.size() - 1) : levels.get(completedLevels);
        int percentage = calculateProgressPercentage(earned, previousGoal, nextGoal, maxed);

        return new SellProgressInfo(
                category,
                earned,
                completedLevels,
                currentMultiplier,
                maxed ? "MAX" : formatMultiplier(1.0 + ((completedLevels + 1) * 0.1)),
                previousGoal,
                nextGoal,
                percentage,
                buildProgressBar(percentage),
                maxed
        );
    }

    public double getCurrentSellMultiplier(Map<SellCategory, Double> progress, SellCategory category) {
        return 1.0 + (getCompletedLevels(progress.getOrDefault(category, 0D), getSellProgressLevels()) * 0.1);
    }

    public SellResult sellInventoryContents(Player player, Inventory inventory, int startInclusive, int endExclusive) {
        return sellInventoryContents(
                player,
                inventory,
                startInclusive,
                endExclusive,
                EconomyReason.SELL_PAYOUT,
                true
        );
    }

    public SellResult sellInventoryContents(
            Player player,
            Inventory inventory,
            int startInclusive,
            int endExclusive,
            EconomyReason reason,
            boolean sendFeedback
    ) {
        if (player == null || inventory == null) {
            return emptySale();
        }

        PendingSale sale = createPendingSale(player);
        int firstSlot = Math.max(0, startInclusive);
        int lastSlot = Math.min(inventory.getSize(), Math.max(firstSlot, endExclusive));

        for (int slot = firstSlot; slot < lastSlot; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }

            double payout = collectSellWorthEntries(item, sale);
            if (payout <= 0) {
                continue;
            }

            sale.totalPayout += payout;
            sale.soldSlots.add(slot);
        }

        return executeSale(player, sale, reason, sendFeedback, () -> {
            for (int slot : sale.soldSlots) {
                inventory.setItem(slot, null);
            }
        });
    }

    private PendingSale createPendingSale(Player player) {
        PendingSale sale = new PendingSale();
        sale.currentProgress.putAll(getSellProgress(player.getUniqueId()));
        return sale;
    }

    private SellResult emptySale() {
        return new SellResult(SellStatus.NO_SELLABLE_ITEMS, 0, Set.of());
    }

    private SellResult failedSale() {
        return new SellResult(SellStatus.TRANSACTION_FAILED, 0, Set.of());
    }

    private SellResult executeSale(
            Player player,
            PendingSale sale,
            EconomyReason reason,
            boolean sendFeedback,
            Runnable removeSoldItems
    ) {
        if (sale.totalPayout <= 0) {
            return emptySale();
        }

        EconomyReason resolvedReason = reason == null ? EconomyReason.SELL_PAYOUT : reason;
        var depositResult = plugin.getEconomyManager().deposit(player, sale.totalPayout, resolvedReason);
        if (!depositResult.success()) {
            return failedSale();
        }

        removeSoldItems.run();
        return commitSale(player, sale, sendFeedback);
    }

    public double sellInventory(Player player, boolean handOnly) {
        PendingSale sale = createPendingSale(player);
        ItemStack[] contents = handOnly
                ? new ItemStack[]{player.getInventory().getItemInMainHand()}
                : player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType().isAir()) {
                continue;
            }

            double payout = collectSellWorthEntries(item, sale);
            if (payout <= 0) {
                continue;
            }

            sale.totalPayout += payout;
            sale.soldSlots.add(i);
        }

        SellResult result = executeSale(player, sale, EconomyReason.SELL_PAYOUT, true, () -> {
            if (handOnly) {
                player.getInventory().setItemInMainHand(null);
            } else {
                for (int slot : sale.soldSlots) {
                    player.getInventory().setItem(slot, null);
                }
            }
        });

        return result.hasSales() ? result.totalPayout() : 0;
    }

    private double collectSellWorthEntries(ItemStack item, PendingSale sale) {
        List<WorthManager.SellWorthEntry> entries = plugin.getWorthManager().resolveSellWorthEntries(item);
        if (entries.isEmpty()) {
            return 0;
        }

        double totalPayout = 0;
        for (WorthManager.SellWorthEntry entry : entries) {
            double payout = entry.totalWorth() * getCurrentSellMultiplier(sale.currentProgress, entry.category());
            if (payout <= 0) {
                continue;
            }

            totalPayout += payout;
            sale.earnedByCategory.merge(entry.category(), entry.totalWorth(), Double::sum);
            sale.history.add(new PendingSellHistory(entry.material(), entry.amount(), payout));
        }
        return totalPayout;
    }

    private SellResult commitSale(Player player, PendingSale sale, boolean sendFeedback) {
        EnumSet<SellCategory> leveledUpCategories = EnumSet.noneOf(SellCategory.class);

        List<DatabaseManager.SellHistoryRecord> historyBatch = new ArrayList<>();
        List<DatabaseManager.PlayerLogRecord> logBatch = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (PendingSellHistory historyEntry : sale.history) {
            historyBatch.add(new DatabaseManager.SellHistoryRecord(
                    player.getUniqueId(),
                    historyEntry.material().name(),
                    historyEntry.amount(),
                    historyEntry.payout(),
                    now
            ));
            String prettyName = plugin.getWorthManager().prettifyMaterial(historyEntry.material());
            logBatch.add(new DatabaseManager.PlayerLogRecord(
                    player.getUniqueId(),
                    player.getName(),
                    "Shop",
                    "SHOP_SELL",
                    "Sold " + prettyName + " x" + historyEntry.amount() + " for " + plugin.getCurrencyManager().formatMoney(historyEntry.payout()),
                    now
            ));
        }

        Map<SellCategory, Double> earnedCopy = new EnumMap<>(sale.earnedByCategory);
        for (var entry : sale.earnedByCategory.entrySet()) {
            SellCategory category = entry.getKey();
            double before = sale.currentProgress.getOrDefault(category, 0D);
            double after = before + entry.getValue();
            if (getCompletedLevels(after, getSellProgressLevels())
                    > getCompletedLevels(before, getSellProgressLevels())) {
                leveledUpCategories.add(category);
            }

            sale.currentProgress.put(category, after);
        }

        plugin.getDatabaseManager().executeAsync(() -> {
            plugin.getDatabaseManager().addSellHistoryBatch(historyBatch);
            plugin.getDatabaseManager().addPlayerLogBatch(logBatch);
            for (var entry : earnedCopy.entrySet()) {
                plugin.getDatabaseManager().addSellProgress(player.getUniqueId(), entry.getKey(), entry.getValue());
            }
        });

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data != null) {
            data.addMoneyMade(sale.totalPayout);
        }

        if (sendFeedback) {
            sendSellFeedback(player, sale.totalPayout);
        }
        if (sale.totalPayout > 0) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("SELL.SUCCESS"));
        }
        if (!leveledUpCategories.isEmpty()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("SELL.LEVEL-UP"));
        }

        return new SellResult(
                SellStatus.SUCCESS,
                sale.totalPayout,
                leveledUpCategories.isEmpty() ? Set.of() : EnumSet.copyOf(leveledUpCategories)
        );
    }

    private void sendSellFeedback(Player player, double totalPayout) {
        String sellMsg = plugin.getConfigManager().getConfig()
                .getString("SETTINGS.SELL-MESSAGE", "&a+{price_formatted}")
                .replace("%price%", plugin.getCurrencyManager().formatCompactAmount(CurrencyManager.CurrencyType.MONEY, totalPayout))
                .replace("%price_formatted%", plugin.getCurrencyManager().formatMoney(totalPayout))
                .replace("{price}", plugin.getCurrencyManager().formatCompactAmount(CurrencyManager.CurrencyType.MONEY, totalPayout))
                .replace("{price_formatted}", plugin.getCurrencyManager().formatMoney(totalPayout));
        PlayerSettingUtils.sendActionBar(plugin, player, sellMsg);
    }

    private List<Long> getSellProgressLevels() {
        List<Long> levels = plugin.getConfigManager().getMenus().getLongList("PROGRESS-MENU.LEVEL");
        if (!levels.isEmpty()) {
            return levels;
        }

        return List.of(25_000L, 150_000L, 500_000L, 1_000_000L);
    }

    private int getCompletedLevels(double earned, List<Long> levels) {
        int completed = 0;
        for (Long level : levels) {
            if (earned >= level) {
                completed++;
            } else {
                break;
            }
        }
        return completed;
    }

    private int calculateProgressPercentage(double earned, double previousGoal, double nextGoal, boolean maxed) {
        if (maxed || nextGoal <= previousGoal) {
            return 100;
        }

        double progress = (earned - previousGoal) / (nextGoal - previousGoal);
        return (int) Math.max(0, Math.min(100, Math.round(progress * 100)));
    }

    private String formatMultiplier(double multiplier) {
        return String.format(Locale.US, "%.1fx", multiplier);
    }

    private String buildProgressBar(int percentage) {
        String symbol = plugin.getConfigManager().getMenus()
                .getString("PROGRESS-MENU.PROGRESS-BAR", "\u25A0");
        int filledSegments = (int) Math.round((percentage / 100.0) * MAX_MULTIPLIER_BAR_SEGMENTS);
        filledSegments = Math.max(0, Math.min(MAX_MULTIPLIER_BAR_SEGMENTS, filledSegments));

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < MAX_MULTIPLIER_BAR_SEGMENTS; i++) {
            builder.append(i < filledSegments ? "&#6BF18D" : "&7").append(symbol);
        }
        return builder.toString();
    }
}
