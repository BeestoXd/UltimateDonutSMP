package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.SellCategory;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
        INVENTORY_FULL
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

    public record SellResult(double totalPayout, Set<SellCategory> leveledUpCategories) {
        public boolean hasSales() {
            return totalPayout > 0;
        }
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

        if (preview.currency() == Currency.SHARD) {
            long shardCost = Math.round(preview.totalPrice());
            if (!data.removeShards(shardCost)) {
                return failPurchase(item, amount, preview.totalPrice(), PurchaseFailureReason.NO_SHARDS);
            }
        } else {
            var withdrawResult = plugin.getEconomyManager().withdraw(player, preview.totalPrice(), EconomyReason.SHOP_PURCHASE);
            if (!withdrawResult.success()) {
                return failPurchase(item, amount, preview.totalPrice(), PurchaseFailureReason.NO_MONEY);
            }
            data.addMoneySpent(preview.totalPrice());
        }

        if (item.command() != null && !item.command().isBlank()) {
            String cmd = item.command()
                    .replace("{username}", player.getName())
                    .replace("{player}", player.getName())
                    .replace("{amount}", String.valueOf(preview.quantity()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        if (item.giveItem()) {
            ItemStack stack = createPurchasedItem(item, preview.quantity());
            player.getInventory().addItem(stack).values().forEach(left ->
                    player.getWorld().dropItemNaturally(player.getLocation(), left));
        }

        return preview;
    }

    private PurchaseResult validatePurchase(Player player, ShopItem item, int amount) {
        if (player == null || item == null) {
            return failPurchase(item, amount, 0, PurchaseFailureReason.INVALID_ITEM);
        }

        ShopRestriction restriction = getPurchaseRestriction(item);
        if (amount < restriction.minQuantity() || amount > restriction.maxQuantity()) {
            return failPurchase(item, amount, item.pricePerUnit() * Math.max(1, amount), PurchaseFailureReason.INVALID_QUANTITY);
        }

        if (item.permission() != null && !item.permission().isBlank() && !player.hasPermission(item.permission())) {
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
            } else if (canStack(current, singleItem)) {
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
                                + " in " + category.menuSection() + ": buy $" + NumberUtils.format(item.pricePerUnit())
                                + " but worth $" + NumberUtils.format(worth) + ".");
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

        List<Component> currentLore = meta.lore();
        List<Component> originalLore = readOriginalLore(meta);
        if (originalLore == null) {
            originalLore = currentLore;
        }
        originalLore = stripExistingWorthLore(originalLore);

        List<Component> desiredLore = originalLore == null ? new ArrayList<>() : new ArrayList<>(originalLore);
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
        updatedMeta.lore(desiredLore);
        updated.setItemMeta(updatedMeta);
        return updated;
    }

    private List<Component> readOriginalLore(ItemMeta meta) {
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String stored = container.get(worthDisplayOriginalLoreKey, PersistentDataType.STRING);
        if (stored != null) {
            return deserializeLore(stored);
        }

        if (!container.has(worthDisplayAppliedKey, PersistentDataType.BYTE)) {
            return meta.lore();
        }

        List<Component> currentLore = meta.lore();
        if (currentLore == null || currentLore.isEmpty()) {
            return null;
        }

        return new ArrayList<>(currentLore.subList(0, currentLore.size() - 1));
    }

    private String serializeLore(List<Component> lore) {
        if (lore == null) {
            return NULL_LORE;
        }
        if (lore.isEmpty()) {
            return "";
        }

        List<String> encoded = new ArrayList<>();
        for (Component component : lore) {
            String json = GsonComponentSerializer.gson().serialize(component);
            encoded.add(Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)));
        }
        return String.join(LORE_SEPARATOR, encoded);
    }

    private List<Component> deserializeLore(String serialized) {
        if (serialized == null) {
            return null;
        }
        if (NULL_LORE.equals(serialized)) {
            return null;
        }
        if (serialized.isEmpty()) {
            return new ArrayList<>();
        }

        List<Component> lore = new ArrayList<>();
        for (String token : serialized.split(LORE_SEPARATOR, -1)) {
            String json = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            lore.add(GsonComponentSerializer.gson().deserialize(json));
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

    private List<Component> stripExistingWorthLore(List<Component> lore) {
        if (lore == null || lore.isEmpty()) {
            return lore;
        }

        Pattern worthLorePattern = buildWorthLorePattern();
        List<Component> sanitized = new ArrayList<>();
        boolean changed = false;
        for (Component line : lore) {
            if (isWorthLoreLine(line, worthLorePattern)) {
                changed = true;
                continue;
            }
            sanitized.add(line);
        }
        return changed ? sanitized : lore;
    }

    private boolean isWorthLoreLine(Component line, Pattern worthLorePattern) {
        String plain = PlainTextComponentSerializer.plainText().serialize(line);
        if (containsBrokenWorthPlaceholder(plain)) {
            return true;
        }

        return worthLorePattern.matcher(plain).matches();
    }

    private boolean containsBrokenWorthPlaceholder(Component line) {
        return containsBrokenWorthPlaceholder(PlainTextComponentSerializer.plainText().serialize(line));
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
        if (inventory == null) {
            return new SellResult(0, Set.of());
        }

        Map<SellCategory, Double> progress = new EnumMap<>(SellCategory.class);
        progress.putAll(getSellProgress(player.getUniqueId()));

        EnumMap<SellCategory, Double> earnedByCategory = new EnumMap<>(SellCategory.class);
        List<Integer> soldSlots = new ArrayList<>();
        double totalPayout = 0;

        for (int slot = startInclusive; slot < endExclusive; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }

            double unitWorth = getWorth(item);
            if (unitWorth < 0) {
                continue;
            }

            SellCategory category = getSellCategory(item);
            if (category == null) {
                continue;
            }

            double baseTotal = unitWorth * item.getAmount();
            double payout = baseTotal * getCurrentSellMultiplier(progress, category);
            totalPayout += payout;
            earnedByCategory.merge(category, baseTotal, Double::sum);
            soldSlots.add(slot);

            plugin.getDatabaseManager().addSellHistory(
                    player.getUniqueId(),
                    item.getType().name(),
                    item.getAmount(),
                    payout
            );
        }

        if (totalPayout <= 0) {
            return new SellResult(0, Set.of());
        }

        for (int slot : soldSlots) {
            inventory.setItem(slot, null);
        }

        return finishSale(player, progress, earnedByCategory, totalPayout);
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
                        .getString("WORTH-LORE.FORMAT", "&7Worth: &a$%price%")
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
            return "&7Worth: &a$%price%";
        }
        if (format.contains("$")) {
            return format;
        }

        for (String placeholder : List.of("%price%", "${price}", "{price}")) {
            int index = format.indexOf(placeholder);
            if (index >= 0) {
                return format.substring(0, index) + "$" + format.substring(index);
            }
        }

        return format;
    }

    private Pattern buildWorthLorePattern() {
        String format = stripColorCodes(getWorthLoreFormat());
        List<String> placeholders = List.of(
                "%unit_price%", "${unit_price}", "{unit_price}",
                "%price%", "${price}", "{price}",
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
        Map<SellCategory, Double> progress = new EnumMap<>(SellCategory.class);
        progress.putAll(getSellProgress(player.getUniqueId()));

        EnumMap<SellCategory, Double> earnedByCategory = new EnumMap<>(SellCategory.class);
        double totalPayout = 0;
        ItemStack[] contents = handOnly
                ? new ItemStack[]{player.getInventory().getItemInMainHand()}
                : player.getInventory().getContents();
        List<Integer> toRemove = new ArrayList<>();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType().isAir()) {
                continue;
            }

            double unitWorth = getWorth(item);
            if (unitWorth < 0) {
                continue;
            }

            SellCategory category = getSellCategory(item);
            if (category == null) {
                continue;
            }

            double baseTotal = unitWorth * item.getAmount();
            double payout = baseTotal * getCurrentSellMultiplier(progress, category);
            totalPayout += payout;
            earnedByCategory.merge(category, baseTotal, Double::sum);

            plugin.getDatabaseManager().addSellHistory(
                    player.getUniqueId(),
                    item.getType().name(),
                    item.getAmount(),
                    payout
            );
            toRemove.add(i);
        }

        if (totalPayout > 0) {
            if (handOnly) {
                player.getInventory().setItemInMainHand(null);
            } else {
                for (int slot : toRemove) {
                    player.getInventory().setItem(slot, null);
                }
            }

            finishSale(player, progress, earnedByCategory, totalPayout);
        }

        return totalPayout;
    }

    private SellResult finishSale(
            Player player,
            Map<SellCategory, Double> currentProgress,
            Map<SellCategory, Double> earnedByCategory,
            double totalPayout
    ) {
        EnumSet<SellCategory> leveledUpCategories = EnumSet.noneOf(SellCategory.class);

        for (var entry : earnedByCategory.entrySet()) {
            SellCategory category = entry.getKey();
            double before = currentProgress.getOrDefault(category, 0D);
            double after = before + entry.getValue();
            if (getCompletedLevels(after, getSellProgressLevels())
                    > getCompletedLevels(before, getSellProgressLevels())) {
                leveledUpCategories.add(category);
            }

            currentProgress.put(category, after);
            plugin.getDatabaseManager().addSellProgress(player.getUniqueId(), category, entry.getValue());
        }

        PlayerData data = plugin.getPlayerDataManager().get(player);
        var depositResult = plugin.getEconomyManager().deposit(player, totalPayout, EconomyReason.SELL_PAYOUT);
        if (data != null && depositResult.success()) {
            data.addMoneyMade(totalPayout);
        }

        sendSellFeedback(player, totalPayout);
        if (!leveledUpCategories.isEmpty()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("SELL.LEVEL-UP"));
        }

        return new SellResult(
                totalPayout,
                leveledUpCategories.isEmpty() ? Set.of() : EnumSet.copyOf(leveledUpCategories)
        );
    }

    private void sendSellFeedback(Player player, double totalPayout) {
        String sellMsg = plugin.getConfigManager().getConfig()
                .getString("SETTINGS.SELL-MESSAGE", "&a+$%price%")
                .replace("%price%", NumberUtils.formatNice(totalPayout));
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
