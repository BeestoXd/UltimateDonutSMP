package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.SellCategory;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.logging.Level;

public class ShopManager {

    private static final String NULL_LORE = "__NULL__";
    private static final String LORE_SEPARATOR = ";";
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
            Boolean hideQuantityButtons
    ) {}

    private final UltimateDonutSmp plugin;
    private final NamespacedKey worthDisplayAppliedKey;
    private final NamespacedKey worthDisplayOriginalLoreKey;

    public ShopManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.worthDisplayAppliedKey = new NamespacedKey(plugin, "worth_display_applied");
        this.worthDisplayOriginalLoreKey = new NamespacedKey(plugin, "worth_display_original_lore");
        reload();
    }

    public void reload() {
        validateShopConfiguration();
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
            if (itemSec == null || !itemSec.getBoolean("ENABLED", true)) {
                continue;
            }

            Material material = ItemUtils.parseMaterial(itemSec.getString("MATERIAL", "STONE"));
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
                    itemSec.contains("HIDE-QUANTITY-BUTTONS") ? itemSec.getBoolean("HIDE-QUANTITY-BUTTONS") : null
            ));
        }

        items.sort(Comparator.comparingInt(ShopItem::slot).thenComparing(ShopItem::key, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(items);
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

        if (item.giveItem()) {
            plugin.getWorthManager().stripStorageWorthDisplayForNativePickup(player);
            ItemStack stack = createPurchasedItem(item, preview.quantity());
            player.getInventory().addItem(stack).values().forEach(left ->
                    player.getWorld().dropItemNaturally(player.getLocation(), left));
            plugin.getWorthManager().syncWorthDisplay(player);
            player.updateInventory();
        }

        if (preview.currency() != Currency.SHARD) {
            data.addMoneySpent(preview.totalPrice());
        }

        return preview;
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
                        "Managed spawner reward threw an exception: " + item.command(),
                        exception
                );
            }
        }

        String command = resolveShopCommand(player, item.command(), quantity);
        try {
            boolean dispatched = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return dispatched
                    ? RewardDeliveryResult.ok()
                    : RewardDeliveryResult.failure("Command returned false: " + command);
        } catch (RuntimeException exception) {
            return RewardDeliveryResult.failure("Command threw an exception: " + command, exception);
        }
    }

    private ManagedSpawnerReward parseManagedSpawnerReward(String command) {
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

    private boolean isPlayerPlaceholder(String token) {
        return token != null
                && (token.equalsIgnoreCase("{username}") || token.equalsIgnoreCase("{player}"));
    }

    private boolean isAmountPlaceholder(String token) {
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
        String message = "[ShopManager] Failed to deliver shop reward for "
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

        if (item.giveItem() && !canFitPurchasedItem(player, item, amount)) {
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

    private Currency parseCurrency(String currencyName) {
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
        return new ItemStack(item.material(), Math.max(1, amount));
    }

    private boolean canFitPurchasedItem(Player player, ShopItem item, int amount) {
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

    private boolean canStack(ItemStack first, ItemStack second) {
        return first.isSimilar(second) && first.getAmount() < first.getMaxStackSize();
    }

    private void validateShopConfiguration() {
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
                            + " has a negative PRICE-PER-UNIT.");
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

    private boolean isWorthDisplayEnabled(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        return data != null && data.isWorthDisplayEnabled();
    }

    private ItemStack updateWorthDisplay(ItemStack item, boolean enabled) {
        if (item == null || item.getType().isAir()) {
            return item;
        }

        String loreLine = enabled ? getWorthLoreLine(item) : null;
        if (loreLine == null || loreLine.isBlank()) {
            return stripWorthDisplay(item);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        List<String> currentLore = meta.getLore();
        List<String> originalLore = readOriginalLore(meta);
        if (originalLore == null) {
            originalLore = currentLore;
        }
        originalLore = stripExistingWorthLore(originalLore);

        List<String> desiredLore = originalLore == null ? new ArrayList<>() : new ArrayList<>(originalLore);
        desiredLore.add(ColorUtils.toComponent(loreLine));

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String serializedOriginalLore = serializeLore(originalLore);
        String storedOriginalLore = container.get(worthDisplayOriginalLoreKey, PersistentDataType.STRING);
        boolean alreadyApplied = container.has(worthDisplayAppliedKey, PersistentDataType.BYTE);
        if (alreadyApplied
                && Objects.equals(storedOriginalLore, serializedOriginalLore)
                && Objects.equals(currentLore, desiredLore)) {
            return item;
        }

        ItemStack updated = item.clone();
        ItemMeta updatedMeta = updated.getItemMeta();
        if (updatedMeta == null) {
            return item;
        }

        PersistentDataContainer updatedContainer = updatedMeta.getPersistentDataContainer();
        updatedContainer.set(worthDisplayAppliedKey, PersistentDataType.BYTE, (byte) 1);
        updatedContainer.set(worthDisplayOriginalLoreKey, PersistentDataType.STRING, serializedOriginalLore);
        updatedMeta.setLore(desiredLore);
        updated.setItemMeta(updatedMeta);
        return updated;
    }

    private List<String> readOriginalLore(ItemMeta meta) {
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String stored = container.get(worthDisplayOriginalLoreKey, PersistentDataType.STRING);
        if (stored != null) {
            return deserializeLore(stored);
        }

        if (!container.has(worthDisplayAppliedKey, PersistentDataType.BYTE)) {
            return meta.getLore();
        }

        List<String> currentLore = meta.getLore();
        if (currentLore == null || currentLore.isEmpty()) {
            return null;
        }

        return new ArrayList<>(currentLore.subList(0, currentLore.size() - 1));
    }

    private String serializeLore(List<String> lore) {
        if (lore == null) {
            return NULL_LORE;
        }
        if (lore.isEmpty()) {
            return "";
        }

        List<String> encoded = new ArrayList<>();
        for (String line : lore) {
            encoded.add(Base64.getEncoder().encodeToString((line == null ? "" : line).getBytes(StandardCharsets.UTF_8)));
        }
        return String.join(LORE_SEPARATOR, encoded);
    }

    private List<String> deserializeLore(String serialized) {
        if (serialized == null) {
            return null;
        }
        if (NULL_LORE.equals(serialized)) {
            return null;
        }
        if (serialized.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> lore = new ArrayList<>();
        for (String token : serialized.split(LORE_SEPARATOR, -1)) {
            lore.add(new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8));
        }
        return lore;
    }

    private String replaceWorthPlaceholders(
            String format,
            String totalPrice,
            String unitPrice,
            String amount,
            String itemName
    ) {
        String resolved = replacePlaceholderVariants(format, "price", totalPrice);
        resolved = replacePlaceholderVariants(resolved, "unit_price", unitPrice);
        resolved = replacePlaceholderVariants(resolved, "amount", amount);
        return replacePlaceholderVariants(resolved, "item", itemName);
    }

    private String replacePlaceholderVariants(String text, String key, String value) {
        return text
                .replace("%" + key + "%", value)
                .replace("${" + key + "}", value)
                .replace("{" + key + "}", value);
    }

    private List<String> stripExistingWorthLore(List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            return lore;
        }

        Pattern worthLorePattern = buildWorthLorePattern();
        List<String> sanitized = new ArrayList<>();
        boolean changed = false;
        for (String line : lore) {
            if (isWorthLoreLine(line, worthLorePattern)) {
                changed = true;
                continue;
            }
            sanitized.add(line);
        }
        return changed ? sanitized : lore;
    }

    private boolean isWorthLoreLine(String line, Pattern worthLorePattern) {
        String plain = ColorUtils.strip(line);
        if (containsBrokenWorthPlaceholder(plain)) {
            return true;
        }

        return worthLorePattern.matcher(plain).matches();
    }

    private boolean containsBrokenWorthPlaceholder(String plain) {
        return plain.contains("${price}")
                || plain.contains("%price%")
                || plain.contains("{price}")
                || plain.contains("${unit_price}")
                || plain.contains("%unit_price%")
                || plain.contains("{unit_price}");
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

    private String getWorthLoreFormat() {
        FileConfiguration worthConfig = plugin.getConfigManager().getWorth();

        String format = firstNonBlank(
                worthConfig.getString("WORTH-LORE.FORMAT"),
                worthConfig.getString("DISPLAY.FORMAT"),
                worthConfig.getString("FORMAT")
        );
        if (format != null) {
            return normalizeWorthLoreFormat(format);
        }

        return normalizeWorthLoreFormat(
                plugin.getConfigManager().getConfig()
                        .getString("WORTH-LORE.FORMAT", "&7ᴡᴏʀᴛʜ: &a{price_formatted}")
        );
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return null;
    }

    private String normalizeWorthLoreFormat(String format) {
        if (format == null || format.isBlank()) {
            return "&7ᴡᴏʀᴛʜ: &a{price_formatted}";
        }

        return format;
    }

    private Pattern buildWorthLorePattern() {
        String format = stripColorCodes(getWorthLoreFormat());
        List<String> placeholders = List.of(
                "%unit_price%", "${unit_price}", "{unit_price}",
                "%unit_price_formatted%", "${unit_price_formatted}", "{unit_price_formatted}",
                "%price%", "${price}", "{price}",
                "%price_formatted%", "${price_formatted}", "{price_formatted}",
                "%amount%", "${amount}", "{amount}",
                "%item%", "${item}", "{item}"
        );

        StringBuilder regex = new StringBuilder("^");
        int index = 0;
        while (index < format.length()) {
            int nextPlaceholderIndex = -1;
            String nextPlaceholder = null;
            for (String placeholder : placeholders) {
                int candidateIndex = format.indexOf(placeholder, index);
                if (candidateIndex < 0) {
                    continue;
                }
                if (nextPlaceholderIndex < 0 || candidateIndex < nextPlaceholderIndex) {
                    nextPlaceholderIndex = candidateIndex;
                    nextPlaceholder = placeholder;
                }
            }

            if (nextPlaceholderIndex < 0) {
                regex.append(Pattern.quote(format.substring(index)));
                break;
            }

            String staticPart = format.substring(index, nextPlaceholderIndex);
            if (isPricePlaceholder(nextPlaceholder) && staticPart.endsWith("$")) {
                staticPart = staticPart.substring(0, staticPart.length() - 1);
                regex.append(Pattern.quote(staticPart));
                regex.append("\\$?");
            } else {
                regex.append(Pattern.quote(staticPart));
            }

            regex.append(".+?");
            index = nextPlaceholderIndex + nextPlaceholder.length();
        }

        regex.append("$");
        return Pattern.compile(regex.toString());
    }

    private String stripColorCodes(String text) {
        return text
                .replaceAll("(?i)&#[0-9a-f]{6}", "")
                .replaceAll("(?i)&[0-9a-fk-or]", "");
    }

    private boolean isPricePlaceholder(String placeholder) {
        return placeholder.contains("price");
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

        for (PendingSellHistory historyEntry : sale.history) {
            plugin.getDatabaseManager().addSellHistory(
                    player.getUniqueId(),
                    historyEntry.material().name(),
                    historyEntry.amount(),
                    historyEntry.payout()
            );
        }

        for (var entry : sale.earnedByCategory.entrySet()) {
            SellCategory category = entry.getKey();
            double before = sale.currentProgress.getOrDefault(category, 0D);
            double after = before + entry.getValue();
            if (getCompletedLevels(after, getSellProgressLevels())
                    > getCompletedLevels(before, getSellProgressLevels())) {
                leveledUpCategories.add(category);
            }

            sale.currentProgress.put(category, after);
            plugin.getDatabaseManager().addSellProgress(player.getUniqueId(), category, entry.getValue());
        }

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data != null) {
            data.addMoneyMade(sale.totalPayout);
        }

        if (sendFeedback) {
            sendSellFeedback(player, sale.totalPayout);
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
