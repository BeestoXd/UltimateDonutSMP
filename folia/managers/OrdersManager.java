package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.OrdersBrowseMenu;
import com.bx.ultimateDonutSmp.menus.OrdersEditMenu;
import com.bx.ultimateDonutSmp.menus.OrdersInventoryItemMenu;
import com.bx.ultimateDonutSmp.menus.OrdersNewMenu;
import com.bx.ultimateDonutSmp.menus.OrdersSearchItemMenu;
import com.bx.ultimateDonutSmp.menus.OrdersSelectItemMenu;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.EconomyTransactionResult;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderCatalogEntry;
import com.bx.ultimateDonutSmp.models.OrderCollectionClaim;
import com.bx.ultimateDonutSmp.models.OrderDelivery;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.models.OrderStatus;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemSerializationUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class OrdersManager {

    private static final double EPSILON = 0.0000001D;
    private static final List<String> DEFAULT_CATEGORY_ORDER = List.of(
            "ALL",
            "BLOCKS",
            "TOOLS",
            "FOOD",
            "COMBAT",
            "POTIONS",
            "BOOKS",
            "INGREDIENTS",
            "UTILITIES"
    );
    private static final List<ServerCatalogCategory> SERVER_CATALOG_CATEGORIES = List.of(
            new ServerCatalogCategory("ALL", Material.COMPASS),
            new ServerCatalogCategory("BLOCKS", Material.GRASS_BLOCK),
            new ServerCatalogCategory("ITEMS", Material.CHEST),
            new ServerCatalogCategory("FOOD", Material.APPLE),
            new ServerCatalogCategory("TOOLS", Material.DIAMOND_PICKAXE),
            new ServerCatalogCategory("SWORDS", Material.DIAMOND_SWORD),
            new ServerCatalogCategory("ARMOR", Material.DIAMOND_CHESTPLATE),
            new ServerCatalogCategory("COMBAT", Material.BOW),
            new ServerCatalogCategory("REDSTONE", Material.REDSTONE)
    );

    public enum ItemSelectionMode {
        SELECT_ITEM,
        INVENTORY_ITEM,
        SEARCH_ITEM;

        private static ItemSelectionMode fromConfig(String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                return SELECT_ITEM;
            }

            try {
                return ItemSelectionMode.valueOf(rawValue.trim().toUpperCase(Locale.US));
            } catch (IllegalArgumentException ignored) {
                return SELECT_ITEM;
            }
        }
    }

    public enum SelectItemSource {
        CATEGORY_FILTERS,
        SERVER_MATERIALS;

        private static SelectItemSource fromConfig(String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                return CATEGORY_FILTERS;
            }

            try {
                return SelectItemSource.valueOf(rawValue.trim().toUpperCase(Locale.US));
            } catch (IllegalArgumentException ignored) {
                return CATEGORY_FILTERS;
            }
        }
    }

    public enum CreateFailureReason {
        DISABLED,
        NO_PENDING_ORDER,
        NO_PLAYER_DATA,
        INVALID_ITEM,
        INVALID_QUANTITY,
        INVALID_PRICE,
        TOTAL_TOO_HIGH,
        NO_MONEY,
        MAX_ORDERS_REACHED,
        DATABASE_ERROR
    }

    public enum DeliveryFailureReason {
        DISABLED,
        NO_PLAYER_DATA,
        ORDER_NOT_FOUND,
        NOT_ACTIVE,
        OWN_ORDER,
        NO_MATCHING_ITEMS,
        ORDER_FULL,
        PAYOUT_ERROR,
        DATABASE_ERROR
    }

    public enum CancelFailureReason {
        DISABLED,
        ORDER_NOT_FOUND,
        NOT_OWNER,
        NOT_ACTIVE,
        DATABASE_ERROR
    }

    public enum ClaimFailureReason {
        DISABLED,
        CLAIM_NOT_FOUND,
        NOT_OWNER,
        ALREADY_CLAIMED,
        INVENTORY_FULL,
        NO_PLAYER_DATA,
        DATABASE_ERROR
    }

    public enum EditFailureReason {
        DISABLED,
        ORDER_NOT_FOUND,
        NOT_OWNER,
        NOT_ACTIVE,
        ALREADY_DELIVERED,
        INVALID_ITEM,
        INVALID_QUANTITY,
        INVALID_PRICE,
        PRICE_OUT_OF_RANGE,
        TOTAL_TOO_HIGH,
        NO_MONEY,
        NO_PLAYER_DATA,
        DATABASE_ERROR
    }

    public record OrderEditNavigation(
            boolean backToMyOrders,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {}

    public record PendingOrderCreationSnapshot(
            ItemStack requestedItem,
            String categoryKey,
            int quantity,
            double priceEach,
            double totalBudget
    ) {}

    public record CreateOrderResult(
            boolean success,
            CreateFailureReason reason,
            Order order,
            double creationFee
    ) {}

    public record DeliveryPreview(
            boolean success,
            DeliveryFailureReason reason,
            Order order,
            int deliverQuantity,
            double payout
    ) {}

    public record DeliverOrderResult(
            boolean success,
            DeliveryFailureReason reason,
            Order order,
            int deliveredQuantity,
            double payout
    ) {}

    public record CancelOrderResult(
            boolean success,
            CancelFailureReason reason,
            Order order
    ) {}

    public record ClaimResult(
            boolean success,
            ClaimFailureReason reason,
            OrderCollectionClaim claim
    ) {}

    public record EditOrderResult(
            boolean success,
            EditFailureReason reason,
            Order order,
            double balanceDelta
    ) {}

    private final UltimateDonutSmp plugin;
    private final Set<UUID> activeTransactions = new HashSet<>();
    private final Map<UUID, Long> lastClickTimes = new HashMap<>();
    private final Map<UUID, PendingOrderCreation> pendingCreations = new HashMap<>();
    private final Map<UUID, PendingSearchInput> pendingSearchInputs = new HashMap<>();
    private final Map<UUID, PendingOrderEdit> pendingEdits = new HashMap<>();
    private final Map<String, List<OrderCatalogEntry>> catalogByCategory = new LinkedHashMap<>();
    private final List<String> categoryOrder = new ArrayList<>();

    public OrdersManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        ensureTables();
        reload();
    }

    public void reload() {
        rebuildCatalog();
        validateConfiguration();
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.ORDERS)
                && config().getBoolean("SETTINGS.ENABLED", true);
    }

    public String getBrowseTitle() {
        return config().getString("GUI.MAIN.TITLE", "&8ᴏʀᴅᴇʀѕ");
    }

    public int getBrowseSize() {
        return normalizeSize(config().getInt("GUI.MAIN.SIZE", 54));
    }

    public int getBrowseItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.MAIN.ITEMS_PER_PAGE", 45)));
    }

    public String getMyOrdersTitle() {
        return config().getString("GUI.MY_ORDERS.TITLE", "&8ᴏʀᴅᴇʀѕ -> ᴍʏ ᴏʀᴅᴇʀѕ");
    }

    public int getMyOrdersSize() {
        return normalizeSize(config().getInt("GUI.MY_ORDERS.SIZE", 54));
    }

    public int getMyOrdersItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.MY_ORDERS.ITEMS_PER_PAGE", 45)));
    }

    public String getCollectTitle() {
        return config().getString("GUI.COLLECT.TITLE", "&8ᴏʀᴅᴇʀѕ -> ᴄᴏʟʟᴇᴄᴛ");
    }

    public int getCollectSize() {
        return normalizeSize(config().getInt("GUI.COLLECT.SIZE", 54));
    }

    public int getCollectItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.COLLECT.ITEMS_PER_PAGE", 45)));
    }

    public String getSelectItemTitle() {
        return config().getString("GUI.SELECT_ITEM.TITLE", "&8ᴏʀᴅᴇʀѕ -> ѕᴇʟᴇᴄᴛ ɪᴛᴇᴍ");
    }

    public int getSelectItemSize() {
        int normalized = normalizeSize(config().getInt("GUI.SELECT_ITEM.SIZE", 54));
        return getSelectItemSource() == SelectItemSource.SERVER_MATERIALS ? Math.max(27, normalized) : normalized;
    }

    public int getSelectItemItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.SELECT_ITEM.ITEMS_PER_PAGE", 45)));
    }

    public String getSearchItemTitle(String query) {
        String safeQuery = query == null ? "" : query;
        return config().getString("GUI.SEARCH_ITEM.TITLE", "&8ᴏʀᴅᴇʀѕ -> ѕᴇᴀʀᴄʜ ɪᴛᴇᴍ")
                .replace("{query}", safeQuery);
    }

    public int getSearchItemSize() {
        return Math.max(18, normalizeSize(config().getInt("GUI.SEARCH_ITEM.SIZE", 54)));
    }

    public int getSearchItemItemsPerPage() {
        int maxResultSlots = Math.max(1, getSearchItemSize() - 9);
        return Math.max(1, Math.min(maxResultSlots, config().getInt("GUI.SEARCH_ITEM.ITEMS_PER_PAGE", 45)));
    }

    public String getInventoryItemTitle() {
        return config().getString("GUI.INVENTORY_ITEM.TITLE", "&8ᴏʀᴅᴇʀѕ -> ᴄʜᴏᴏѕᴇ ɪᴛᴇᴍ");
    }

    public int getInventoryItemSize() {
        return Math.max(27, normalizeSize(config().getInt("GUI.INVENTORY_ITEM.SIZE", 27)));
    }

    public String getNewOrderTitle() {
        return config().getString("GUI.NEW_ORDER.TITLE", "&8ᴏʀᴅᴇʀѕ -> ɴᴇᴡ ᴏʀᴅᴇʀ");
    }

    public int getNewOrderSize() {
        return normalizeSize(config().getInt("GUI.NEW_ORDER.SIZE", 27));
    }

    public String getEditOrderTitle(long orderId) {
        return config().getString("GUI.EDIT_ORDER.TITLE", "&8ᴏʀᴅᴇʀѕ -> ᴇᴅɪᴛ ᴏʀᴅᴇʀ")
                .replace("{order_id}", String.valueOf(orderId));
    }

    public int getEditOrderSize() {
        return normalizeSize(config().getInt("GUI.EDIT_ORDER.SIZE", 27));
    }

    public String getDeliverTitle(long orderId) {
        return config().getString("GUI.DELIVER_CONFIRM.TITLE", "&8ᴏʀᴅᴇʀѕ -> ᴅᴇʟɪᴠᴇʀ")
                .replace("{order_id}", String.valueOf(orderId));
    }

    public int getDeliverSize() {
        return normalizeSize(config().getInt("GUI.DELIVER_CONFIRM.SIZE", 27));
    }

    public OrderSort getDefaultSort() {
        return OrderSort.fromConfig(config().getString("SORTING.DEFAULT", "MOST_PAID"));
    }

    public ItemSelectionMode getItemSelectionMode() {
        return ItemSelectionMode.fromConfig(config().getString("SETTINGS.ITEM_SELECTION_MODE", "SELECT_ITEM"));
    }

    public SelectItemSource getSelectItemSource() {
        return SelectItemSource.fromConfig(config().getString("SETTINGS.SELECT_ITEM_SOURCE", "CATEGORY_FILTERS"));
    }

    public void openNewOrderItemSelection(Player player) {
        if (player == null) {
            return;
        }

        pendingEdits.remove(player.getUniqueId());
        ItemSelectionMode mode = getItemSelectionMode();
        if (mode == ItemSelectionMode.INVENTORY_ITEM) {
            new OrdersInventoryItemMenu(plugin).open(player);
            return;
        }

        if (mode == ItemSelectionMode.SEARCH_ITEM) {
            promptOrderSearchInput(player);
            return;
        }

        new OrdersSelectItemMenu(plugin, 1, "ALL").open(player);
    }

    public void openEditOrderItemSelection(
            Player player,
            long orderId,
            boolean backToMyOrders,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {
        if (player == null) {
            return;
        }

        OrderEditNavigation navigation = normalizeNavigation(backToMyOrders, originPage, sortMode, categoryFilter);
        EditOrderResult validation = validateEditableOrder(player, orderId);
        if (!validation.success()) {
            player.sendMessage(ColorUtils.toComponent(resolveEditFailureMessage(validation)));
            openEditOrderMenu(player, orderId, navigation);
            return;
        }

        pendingCreations.remove(player.getUniqueId());
        pendingEdits.remove(player.getUniqueId());
        ItemSelectionMode mode = getItemSelectionMode();
        if (mode == ItemSelectionMode.INVENTORY_ITEM) {
            new OrdersInventoryItemMenu(plugin, orderId, navigation).open(player);
            return;
        }

        if (mode == ItemSelectionMode.SEARCH_ITEM) {
            promptEditOrderSearchInput(player, orderId, navigation);
            return;
        }

        new OrdersSelectItemMenu(plugin, 1, "ALL", orderId, navigation).open(player);
    }

    public List<OrderSort> getAllowedSorts() {
        List<OrderSort> sorts = new ArrayList<>();
        for (String rawValue : config().getStringList("SORTING.ALLOWED")) {
            OrderSort sort = OrderSort.fromConfig(rawValue);
            if (!sorts.contains(sort)) {
                sorts.add(sort);
            }
        }
        if (sorts.isEmpty()) {
            sorts.addAll(List.of(
                    OrderSort.MOST_PAID,
                    OrderSort.MOST_DELIVERED,
                    OrderSort.RECENTLY_LISTED,
                    OrderSort.MOST_MONEY_PER_ITEM
            ));
        }
        return List.copyOf(sorts);
    }

    public List<String> getAvailableCategories() {
        return List.copyOf(categoryOrder);
    }

    public String normalizeCategory(String rawCategory) {
        if (rawCategory == null || rawCategory.isBlank()) {
            return "ALL";
        }

        String normalized = rawCategory.trim().toUpperCase(Locale.US);
        return categoryOrder.contains(normalized) ? normalized : "ALL";
    }

    public String nextCategory(String currentCategory) {
        String normalized = normalizeCategory(currentCategory);
        int currentIndex = categoryOrder.indexOf(normalized);
        if (currentIndex < 0) {
            return "ALL";
        }
        return categoryOrder.get((currentIndex + 1) % categoryOrder.size());
    }

    public String prettifyCategory(String categoryKey) {
        return normalizeCategory(categoryKey).replace('_', ' ');
    }

    public String prettifySelectItemCategory(String categoryKey) {
        if (getSelectItemSource() == SelectItemSource.SERVER_MATERIALS) {
            return normalizeServerCatalogCategory(categoryKey).replace('_', ' ');
        }

        return prettifyCategory(categoryKey);
    }

    public String resolveCategoryForMaterial(Material material) {
        if (!isModernMaterial(material) || material.isAir()) {
            return "ALL";
        }

        for (String categoryKey : categoryOrder) {
            if (categoryKey.equals("ALL")) {
                continue;
            }

            for (OrderCatalogEntry entry : catalogByCategory.getOrDefault(categoryKey, List.of())) {
                if (entry.material() == material) {
                    return categoryKey;
                }
            }
        }

        return "ALL";
    }

    public List<OrderCatalogEntry> getCatalogEntries(String categoryKey) {
        String normalized = normalizeCategory(categoryKey);
        if (normalized.equals("ALL")) {
            Map<Material, OrderCatalogEntry> unique = new LinkedHashMap<>();
            for (List<OrderCatalogEntry> entries : catalogByCategory.values()) {
                for (OrderCatalogEntry entry : entries) {
                    unique.putIfAbsent(entry.material(), entry);
                }
            }
            return List.copyOf(unique.values());
        }

        return List.copyOf(catalogByCategory.getOrDefault(normalized, List.of()));
    }

    public List<OrderCatalogEntry> getSelectItemCatalogEntries(String categoryKey) {
        if (getSelectItemSource() == SelectItemSource.SERVER_MATERIALS) {
            return getServerMaterialCatalogEntries(categoryKey);
        }

        return getCatalogEntries(categoryKey);
    }

    public List<String> getSelectItemCategories() {
        if (getSelectItemSource() == SelectItemSource.SERVER_MATERIALS) {
            return SERVER_CATALOG_CATEGORIES.stream()
                    .map(ServerCatalogCategory::key)
                    .toList();
        }

        return getAvailableCategories();
    }

    public String normalizeSelectItemCategory(String rawCategory) {
        if (getSelectItemSource() == SelectItemSource.SERVER_MATERIALS) {
            return normalizeServerCatalogCategory(rawCategory);
        }

        return normalizeCategory(rawCategory);
    }

    public String nextSelectItemCategory(String currentCategory) {
        if (getSelectItemSource() == SelectItemSource.SERVER_MATERIALS) {
            List<String> categories = getSelectItemCategories();
            String normalized = normalizeServerCatalogCategory(currentCategory);
            int currentIndex = categories.indexOf(normalized);
            if (currentIndex < 0) {
                return "ALL";
            }
            return categories.get((currentIndex + 1) % categories.size());
        }

        return nextCategory(currentCategory);
    }

    public Material getSelectItemCategoryIcon(String categoryKey) {
        if (getSelectItemSource() == SelectItemSource.SERVER_MATERIALS) {
            return serverCategory(normalizeServerCatalogCategory(categoryKey)).icon();
        }

        return Material.CHEST;
    }

    public boolean beginAction(UUID uuid) {
        return activeTransactions.add(uuid);
    }

    public void endAction(UUID uuid) {
        activeTransactions.remove(uuid);
    }

    public boolean isOnClickCooldown(UUID uuid) {
        long lastClick = lastClickTimes.getOrDefault(uuid, 0L);
        return System.currentTimeMillis() - lastClick < getClickCooldownMillis();
    }

    public void updateClickCooldown(UUID uuid) {
        lastClickTimes.put(uuid, System.currentTimeMillis());
    }

    public void promptOrderQuantityInput(Player player, OrderCatalogEntry entry) {
        if (player == null || entry == null) {
            return;
        }

        pendingSearchInputs.remove(player.getUniqueId());
        pendingEdits.remove(player.getUniqueId());
        pendingCreations.put(player.getUniqueId(), PendingOrderCreation.start(entry));
        player.closeInventory();
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_QUANTITY",
                "&7ᴇɴᴛᴇʀ ᴛʜᴇ ᴏʀᴅᴇʀ ǫᴜᴀɴᴛɪᴛʏ ꜰᴏʀ &f{item}&7 ɪɴ ᴄʜᴀᴛ. ᴛʏᴘᴇ &cᴄᴀɴᴄᴇʟ&7 ᴛᴏ ᴀʙᴏʀᴛ.",
                "{item}", describeMaterial(entry.material())
        )));
    }

    public boolean promptOrderQuantityInput(Player player, ItemStack item) {
        return promptOrderQuantityInput(player, item, resolveCategoryForMaterial(item == null ? null : item.getType()));
    }

    public boolean promptOrderQuantityInput(Player player, ItemStack item, String categoryKey) {
        if (player == null) {
            return false;
        }

        pendingSearchInputs.remove(player.getUniqueId());
        pendingEdits.remove(player.getUniqueId());
        ItemStack requestedItem = prepareRequestedItem(item);
        if (requestedItem == null || !isOrderable(requestedItem.getType())) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.ITEM_BLOCKED",
                    "&cᴛʜᴀᴛ ɪᴛᴇᴍ ᴄᴀɴɴᴏᴛ ʙᴇ ᴏʀᴅᴇʀᴇᴅ."
            )));
            return false;
        }

        pendingCreations.put(player.getUniqueId(), PendingOrderCreation.start(requestedItem, normalizeCategory(categoryKey)));
        player.closeInventory();
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_QUANTITY",
                "&7ᴇɴᴛᴇʀ ᴛʜᴇ ᴏʀᴅᴇʀ ǫᴜᴀɴᴛɪᴛʏ ꜰᴏʀ &f{item}&7 ɪɴ ᴄʜᴀᴛ. ᴛʏᴘᴇ &cᴄᴀɴᴄᴇʟ&7 ᴛᴏ ᴀʙᴏʀᴛ.",
                "{item}", describeItem(requestedItem)
        )));
        return true;
    }

    public void promptOrderSearchInput(Player player) {
        if (player == null) {
            return;
        }

        pendingCreations.remove(player.getUniqueId());
        pendingEdits.remove(player.getUniqueId());
        pendingSearchInputs.put(player.getUniqueId(), PendingSearchInput.newOrder());
        player.closeInventory();
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_SEARCH",
                "&7ᴇɴᴛᴇʀ ᴀɴ ɪᴛᴇᴍ ᴏʀ ᴄᴀᴛᴇɢᴏʀʏ ѕᴇᴀʀᴄʜ ɪɴ ᴄʜᴀᴛ. ᴛʏᴘᴇ &cᴄᴀɴᴄᴇʟ&7 ᴛᴏ ᴀʙᴏʀᴛ."
        )));
    }

    public void promptEditOrderSearchInput(Player player, long orderId, OrderEditNavigation navigation) {
        if (player == null) {
            return;
        }

        EditOrderResult validation = validateEditableOrder(player, orderId);
        if (!validation.success()) {
            player.sendMessage(ColorUtils.toComponent(resolveEditFailureMessage(validation)));
            openEditOrderMenu(player, orderId, navigation);
            return;
        }

        pendingCreations.remove(player.getUniqueId());
        pendingEdits.remove(player.getUniqueId());
        pendingSearchInputs.put(player.getUniqueId(), PendingSearchInput.editOrder(orderId, navigation));
        player.closeInventory();
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_SEARCH",
                "&7á´‡É´á´›á´‡Ê€ á´€É´ Éªá´›á´‡á´ á´Ê€ á´„á´€á´›á´‡É¢á´Ê€Ê Ñ•á´‡á´€Ê€á´„Êœ ÉªÉ´ á´„Êœá´€á´›. á´›Êá´˜á´‡ &cá´„á´€É´á´„á´‡ÊŸ&7 á´›á´ á´€Ê™á´Ê€á´›."
        )));
    }

    public boolean hasPendingInput(UUID uuid) {
        if (pendingSearchInputs.containsKey(uuid)) {
            return true;
        }

        if (pendingEdits.containsKey(uuid)) {
            return true;
        }

        PendingOrderCreation pending = pendingCreations.get(uuid);
        return pending != null && !pending.readyForConfirmation();
    }

    public PendingOrderCreationSnapshot getPendingCreation(UUID uuid) {
        PendingOrderCreation pending = pendingCreations.get(uuid);
        if (pending == null || !pending.readyForConfirmation()) {
            return null;
        }

        return new PendingOrderCreationSnapshot(
                pending.requestedItem().clone(),
                pending.categoryKey(),
                pending.quantity(),
                pending.priceEach(),
                roundCurrency(pending.quantity() * pending.priceEach())
        );
    }

    public void clearPendingCreation(UUID uuid) {
        if (uuid == null) {
            return;
        }
        pendingSearchInputs.remove(uuid);
        pendingEdits.remove(uuid);
        pendingCreations.remove(uuid);
    }

    public void handlePendingInput(Player player, String rawInput) {
        if (player == null) {
            return;
        }

        String input = rawInput == null ? "" : rawInput.trim();
        if (pendingSearchInputs.containsKey(player.getUniqueId())) {
            handleSearchInput(player, input);
            return;
        }

        PendingOrderEdit pendingEdit = pendingEdits.get(player.getUniqueId());
        if (pendingEdit != null) {
            handleEditInput(player, pendingEdit, input);
            return;
        }

        PendingOrderCreation pending = pendingCreations.get(player.getUniqueId());
        if (pending == null) {
            return;
        }

        if (input.equalsIgnoreCase("cancel")) {
            pendingCreations.remove(player.getUniqueId());
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.INPUT_CANCELLED",
                    "&7ᴏʀᴅᴇʀ ᴄʀᴇᴀᴛɪᴏɴ ᴄᴀɴᴄᴇʟʟᴇᴅ."
            )));
            new OrdersBrowseMenu(plugin, 1, getDefaultSort(), "ALL").open(player);
            return;
        }

        if (pending.step() == PendingStep.QUANTITY) {
            handleQuantityInput(player, pending, input);
            return;
        }

        if (pending.step() == PendingStep.PRICE) {
            handlePriceInput(player, pending, input);
        }
    }

    public synchronized CreateOrderResult createOrder(Player player) {
        if (!isEnabled()) {
            return new CreateOrderResult(false, CreateFailureReason.DISABLED, null, 0D);
        }

        PendingOrderCreation pending = pendingCreations.get(player.getUniqueId());
        if (pending == null || !pending.readyForConfirmation()) {
            return new CreateOrderResult(false, CreateFailureReason.NO_PENDING_ORDER, null, 0D);
        }

        ItemStack requestedItem = prepareRequestedItem(pending.requestedItem());
        if (requestedItem == null || !isOrderable(requestedItem.getType())) {
            return new CreateOrderResult(false, CreateFailureReason.INVALID_ITEM, null, 0D);
        }

        if (pending.quantity() <= 0) {
            return new CreateOrderResult(false, CreateFailureReason.INVALID_QUANTITY, null, 0D);
        }

        if (pending.priceEach() < getMinPriceEach() || pending.priceEach() > getMaxPriceEach()) {
            return new CreateOrderResult(false, CreateFailureReason.INVALID_PRICE, null, 0D);
        }

        double totalBudget = roundCurrency(pending.quantity() * pending.priceEach());
        if (totalBudget <= 0D || totalBudget > getMaxTotalBudget()) {
            return new CreateOrderResult(false, CreateFailureReason.TOTAL_TOO_HIGH, null, 0D);
        }

        if (countActiveOrders(player.getUniqueId()) >= getMaxActiveOrders(player)) {
            return new CreateOrderResult(false, CreateFailureReason.MAX_ORDERS_REACHED, null, getCreationFee());
        }

        PlayerData ownerData = getPlayerData(player);
        if (ownerData == null) {
            return new CreateOrderResult(false, CreateFailureReason.NO_PLAYER_DATA, null, getCreationFee());
        }

        double creationFee = getCreationFee();
        double requiredBalance = totalBudget + creationFee;
        if (!plugin.getEconomyManager().has(player, requiredBalance)) {
            return new CreateOrderResult(false, CreateFailureReason.NO_MONEY, null, creationFee);
        }

        var escrowWithdraw = plugin.getEconomyManager().withdraw(player, totalBudget, EconomyReason.ORDER_CREATE_ESCROW);
        if (!escrowWithdraw.success()) {
            return new CreateOrderResult(false, CreateFailureReason.NO_MONEY, null, creationFee);
        }

        boolean feeWithdrawn = false;
        if (creationFee > 0D) {
            var feeResult = plugin.getEconomyManager().withdraw(player, creationFee, EconomyReason.ORDER_CREATE_FEE);
            if (!feeResult.success()) {
                plugin.getEconomyManager().deposit(player, totalBudget, EconomyReason.ORDER_REFUND);
                return new CreateOrderResult(false, CreateFailureReason.NO_MONEY, null, creationFee);
            }
            feeWithdrawn = true;
        }

        long now = System.currentTimeMillis();
        long expiresAt = now + getOrderDurationMillis();
        long orderId = insertOrder(
                player.getUniqueId(),
                player.getName(),
                requestedItem,
                pending.categoryKey(),
                pending.quantity(),
                pending.priceEach(),
                totalBudget,
                now,
                expiresAt
        );

        if (orderId <= 0L) {
            plugin.getEconomyManager().deposit(player, totalBudget, EconomyReason.ORDER_REFUND);
            if (feeWithdrawn) {
                plugin.getEconomyManager().deposit(player, creationFee, EconomyReason.ORDER_REFUND);
            }
            return new CreateOrderResult(false, CreateFailureReason.DATABASE_ERROR, null, creationFee);
        }

        ownerData.addMoneySpent(totalBudget + creationFee);
        plugin.getDatabaseManager().savePlayer(ownerData);
        pendingCreations.remove(player.getUniqueId());
        return new CreateOrderResult(true, null, getOrder(orderId), creationFee);
    }

    public boolean selectOrderItem(
            Player player,
            ItemStack item,
            String categoryKey,
            long editOrderId,
            OrderEditNavigation navigation
    ) {
        if (editOrderId <= 0L) {
            return promptOrderQuantityInput(player, item, categoryKey);
        }

        EditOrderResult result = updateOrderItem(player, editOrderId, item, categoryKey);
        if (result.success()) {
            ItemStack displayItem = result.order() == null ? item : result.order().requestedItem();
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.EDIT_ITEM_UPDATED",
                    "&aᴏʀᴅᴇʀ #{order_id} ɪᴛᴇᴍ ᴜᴘᴅᴀᴛᴇᴅ ᴛᴏ &f{item}&a.",
                    "{order_id}", String.valueOf(editOrderId),
                    "{item}", describeItem(displayItem)
            )));
        } else {
            player.sendMessage(ColorUtils.toComponent(resolveEditFailureMessage(result)));
        }
        openEditOrderMenu(player, editOrderId, navigation);
        return result.success();
    }

    public void promptEditOrderQuantityInput(
            Player player,
            long orderId,
            boolean backToMyOrders,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {
        OrderEditNavigation navigation = normalizeNavigation(backToMyOrders, originPage, sortMode, categoryFilter);
        EditOrderResult validation = validateEditableOrder(player, orderId);
        if (!validation.success()) {
            player.sendMessage(ColorUtils.toComponent(resolveEditFailureMessage(validation)));
            openEditOrderMenu(player, orderId, navigation);
            return;
        }

        pendingCreations.remove(player.getUniqueId());
        pendingSearchInputs.remove(player.getUniqueId());
        pendingEdits.put(player.getUniqueId(), new PendingOrderEdit(orderId, EditField.QUANTITY, navigation));
        player.closeInventory();
        resendEditQuantityPrompt(player, validation.order());
    }

    public void promptEditOrderPriceInput(
            Player player,
            long orderId,
            boolean backToMyOrders,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {
        OrderEditNavigation navigation = normalizeNavigation(backToMyOrders, originPage, sortMode, categoryFilter);
        EditOrderResult validation = validateEditableOrder(player, orderId);
        if (!validation.success()) {
            player.sendMessage(ColorUtils.toComponent(resolveEditFailureMessage(validation)));
            openEditOrderMenu(player, orderId, navigation);
            return;
        }

        pendingCreations.remove(player.getUniqueId());
        pendingSearchInputs.remove(player.getUniqueId());
        pendingEdits.put(player.getUniqueId(), new PendingOrderEdit(orderId, EditField.PRICE, navigation));
        player.closeInventory();
        resendEditPricePrompt(player, validation.order());
    }

    public synchronized EditOrderResult updateOrderItem(Player player, long orderId, ItemStack item, String categoryKey) {
        EditOrderResult validation = validateEditableOrder(player, orderId);
        if (!validation.success()) {
            return validation;
        }

        ItemStack requestedItem = prepareRequestedItem(item);
        if (requestedItem == null || !isOrderable(requestedItem.getType())) {
            return new EditOrderResult(false, EditFailureReason.INVALID_ITEM, validation.order(), 0D);
        }

        String serializedItem = serializeItem(requestedItem);
        if (serializedItem.isBlank()) {
            return new EditOrderResult(false, EditFailureReason.DATABASE_ERROR, validation.order(), 0D);
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "UPDATE orders SET requested_item_data = ?, requested_material_key = ?, category_key = ? " +
                        "WHERE id = ? AND owner_uuid = ? AND status = ? AND delivered_quantity = 0")) {
            ps.setString(1, serializedItem);
            ps.setString(2, requestedItem.getType().name());
            ps.setString(3, normalizeCategory(categoryKey));
            ps.setLong(4, orderId);
            ps.setString(5, player.getUniqueId().toString());
            ps.setString(6, OrderStatus.ACTIVE.name());
            if (ps.executeUpdate() != 1) {
                return new EditOrderResult(false, EditFailureReason.ALREADY_DELIVERED, validation.order(), 0D);
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to update order item " + orderId, exception);
            return new EditOrderResult(false, EditFailureReason.DATABASE_ERROR, validation.order(), 0D);
        }

        return new EditOrderResult(true, null, getOrder(orderId), 0D);
    }

    public synchronized EditOrderResult updateOrderQuantity(Player player, long orderId, int quantity) {
        EditOrderResult validation = validateEditableOrder(player, orderId);
        if (!validation.success()) {
            return validation;
        }
        return updateOrderBudget(player, validation.order(), quantity, validation.order().priceEach());
    }

    public synchronized EditOrderResult updateOrderPrice(Player player, long orderId, double priceEach) {
        EditOrderResult validation = validateEditableOrder(player, orderId);
        if (!validation.success()) {
            return validation;
        }
        return updateOrderBudget(player, validation.order(), validation.order().requestedQuantity(), priceEach);
    }

    public EditOrderResult validateEditableOrder(Player player, long orderId) {
        if (!isEnabled()) {
            return new EditOrderResult(false, EditFailureReason.DISABLED, null, 0D);
        }
        if (player == null) {
            return new EditOrderResult(false, EditFailureReason.NO_PLAYER_DATA, null, 0D);
        }

        Order order = getOrder(orderId);
        if (order == null) {
            return new EditOrderResult(false, EditFailureReason.ORDER_NOT_FOUND, null, 0D);
        }
        if (!order.ownerUuid().equals(player.getUniqueId())) {
            return new EditOrderResult(false, EditFailureReason.NOT_OWNER, order, 0D);
        }
        if (!order.active() || order.expiresAt() <= System.currentTimeMillis()) {
            if (order.active() && order.expiresAt() <= System.currentTimeMillis()) {
                expireOrder(order);
                order = getOrder(orderId);
            }
            return new EditOrderResult(false, EditFailureReason.NOT_ACTIVE, order, 0D);
        }
        if (order.deliveredQuantity() > 0) {
            return new EditOrderResult(false, EditFailureReason.ALREADY_DELIVERED, order, 0D);
        }

        return new EditOrderResult(true, null, order, 0D);
    }

    public String resolveEditFailureMessage(EditOrderResult result) {
        EditFailureReason reason = result == null ? EditFailureReason.DATABASE_ERROR : result.reason();
        return switch (reason) {
            case DISABLED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.DISABLED", "&cᴏʀᴅᴇʀѕ ɪѕ ᴅɪѕᴀʙʟᴇᴅ.");
            case ORDER_NOT_FOUND -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_NOT_FOUND", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.");
            case NOT_OWNER -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NOT_YOUR_ORDER", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ᴅᴏᴇѕ ɴᴏᴛ ʙᴇʟᴏɴɢ ᴛᴏ ʏᴏᴜ.");
            case NOT_ACTIVE -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_NOT_ACTIVE", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ.");
            case ALREADY_DELIVERED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.EDIT_LOCKED", "&cᴛʜɪѕ ᴏʀᴅᴇʀ ᴀʟʀᴇᴀᴅʏ ʜᴀѕ ᴅᴇʟɪᴠᴇʀɪᴇѕ, ѕᴏ ɪᴛ ᴄᴀɴɴᴏᴛ ʙᴇ ᴇᴅɪᴛᴇᴅ.");
            case INVALID_ITEM -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ITEM_BLOCKED", "&cᴛʜᴀᴛ ɪᴛᴇᴍ ᴄᴀɴɴᴏᴛ ʙᴇ ᴏʀᴅᴇʀᴇᴅ.");
            case INVALID_QUANTITY -> plugin.getConfigManager().getMessageOrDefault("ORDERS.INVALID_QUANTITY", "&cɪɴᴠᴀʟɪᴅ ǫᴜᴀɴᴛɪᴛʏ.");
            case INVALID_PRICE -> plugin.getConfigManager().getMessageOrDefault("ORDERS.INVALID_PRICE", "&cɪɴᴠᴀʟɪᴅ ᴘʀɪᴄᴇ.");
            case PRICE_OUT_OF_RANGE -> plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.PRICE_OUT_OF_RANGE",
                    "&cᴘʀɪᴄᴇ ᴇᴀᴄʜ ᴍᴜѕᴛ ʙᴇ ʙᴇᴛᴡᴇᴇɴ &f{min_formatted}&c ᴀɴᴅ &f{max_formatted}&c.",
                    "{min}", NumberUtils.format(getMinPriceEach()),
                    "{min_formatted}", plugin.getCurrencyManager().formatMoney(getMinPriceEach()),
                    "{max}", NumberUtils.format(getMaxPriceEach()),
                    "{max_formatted}", plugin.getCurrencyManager().formatMoney(getMaxPriceEach())
            );
            case TOTAL_TOO_HIGH -> plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.TOTAL_TOO_HIGH",
                    "&cᴛᴏᴛᴀʟ ᴏʀᴅᴇʀ ʙᴜᴅɢᴇᴛ ᴄᴀɴɴᴏᴛ ᴇxᴄᴇᴇᴅ &f{max_formatted}&c.",
                    "{max_formatted}", plugin.getCurrencyManager().formatMoney(getMaxTotalBudget())
            );
            case NO_MONEY -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NOT_ENOUGH_MONEY", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴇɴᴏᴜɢʜ ᴍᴏɴᴇʏ ꜰᴏʀ ᴛʜᴀᴛ ᴄʜᴀɴɢᴇ.");
            case NO_PLAYER_DATA -> "&cʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ʟᴏᴀᴅᴇᴅ.";
            case DATABASE_ERROR -> "&cᴏʀᴅᴇʀѕ ᴄᴏᴜʟᴅ ɴᴏᴛ ᴜᴘᴅᴀᴛᴇ ᴛʜᴀᴛ ᴏʀᴅᴇʀ ʀɪɢʜᴛ ɴᴏᴡ.";
        };
    }

    public String describeMaterial(Material material) {
        if (!isModernMaterial(material)) {
            return "unknown item";
        }
        return plugin.getWorthManager().prettifyMaterial(material);
    }

    public String describeItem(ItemStack item) {
        if (isMissingItem(item)) {
            return "ᴜɴᴋɴᴏᴡɴ ɪᴛᴇᴍ";
        }

        return describeMaterial(item.getType());
    }

    public String formatRemaining(long seconds) {
        if (seconds <= 0) {
            return "Expired";
        }
        return NumberUtils.formatTimeLong(seconds);
    }

    public List<Material> searchOrderMaterials(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.trim();
        String normalizedQuery = normalizeSearchText(query);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }

        SearchCategoryMatch categoryMatch = resolveSearchCategory(normalizedQuery);
        List<SearchCandidate> candidates = new ArrayList<>();
        for (Material material : Material.values()) {
            if (!isOrderable(material)) {
                continue;
            }

            SearchCandidate candidate = scoreSearchMaterial(material, normalizedQuery, categoryMatch);
            if (candidate != null) {
                candidates.add(candidate);
            }
        }

        boolean hasDirectMaterialMatch = candidates.stream()
                .anyMatch(candidate -> candidate.tier() == SearchMatchTier.DIRECT_MATERIAL);
        boolean hasExactCategoryMatch = candidates.stream()
                .anyMatch(candidate -> candidate.tier() == SearchMatchTier.EXACT_CATEGORY);
        if (hasDirectMaterialMatch || hasExactCategoryMatch) {
            candidates.removeIf(candidate -> candidate.tier() != SearchMatchTier.EXACT_CATEGORY
                    && candidate.tier() != SearchMatchTier.DIRECT_MATERIAL);
        }

        candidates.sort(Comparator
                .comparingInt(SearchCandidate::score)
                .reversed()
                .thenComparing(candidate -> candidate.material().name()));
        return candidates.stream()
                .map(SearchCandidate::material)
                .toList();
    }

    private void handleSearchInput(Player player, String input) {
        UUID uuid = player.getUniqueId();
        PendingSearchInput pendingSearch = pendingSearchInputs.get(uuid);
        if (input.equalsIgnoreCase("cancel")) {
            pendingSearchInputs.remove(uuid);
            pendingCreations.remove(uuid);
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.INPUT_CANCELLED",
                    "&7ᴏʀᴅᴇʀ ᴄʀᴇᴀᴛɪᴏɴ ᴄᴀɴᴄᴇʟʟᴇᴅ."
            )));
            if (pendingSearch != null && pendingSearch.editOrderId() > 0L) {
                openEditOrderMenu(player, pendingSearch.editOrderId(), pendingSearch.navigation());
            } else {
                new OrdersBrowseMenu(plugin, 1, getDefaultSort(), "ALL").open(player);
            }
            return;
        }

        if (input.isBlank()) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.SEARCH_EMPTY",
                    "&cᴛʏᴘᴇ ᴀɴ ɪᴛᴇᴍ ᴏʀ ᴄᴀᴛᴇɢᴏʀʏ ɴᴀᴍᴇ ᴛᴏ ѕᴇᴀʀᴄʜ."
            )));
            if (pendingSearch != null && pendingSearch.editOrderId() > 0L) {
                promptEditOrderSearchInput(player, pendingSearch.editOrderId(), pendingSearch.navigation());
            } else {
                promptOrderSearchInput(player);
            }
            return;
        }

        pendingSearchInputs.remove(uuid);
        if (pendingSearch != null && pendingSearch.editOrderId() > 0L) {
            new OrdersSearchItemMenu(plugin, input, 1, pendingSearch.editOrderId(), pendingSearch.navigation()).open(player);
        } else {
            new OrdersSearchItemMenu(plugin, input, 1).open(player);
        }
    }

    private void handleEditInput(Player player, PendingOrderEdit pendingEdit, String input) {
        if (input.equalsIgnoreCase("cancel")) {
            pendingEdits.remove(player.getUniqueId());
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.EDIT_CANCELLED",
                    "&7ᴏʀᴅᴇʀ ᴇᴅɪᴛ ᴄᴀɴᴄᴇʟʟᴇᴅ."
            )));
            openEditOrderMenu(player, pendingEdit.orderId(), pendingEdit.navigation());
            return;
        }

        if (pendingEdit.field() == EditField.QUANTITY) {
            handleEditQuantityInput(player, pendingEdit, input);
            return;
        }

        if (pendingEdit.field() == EditField.PRICE) {
            handleEditPriceInput(player, pendingEdit, input);
        }
    }

    private void handleEditQuantityInput(Player player, PendingOrderEdit pendingEdit, String input) {
        int quantity;
        try {
            quantity = Math.toIntExact(NumberUtils.parseLong(input));
        } catch (RuntimeException exception) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.INVALID_QUANTITY",
                    "&cɪɴᴠᴀʟɪᴅ ǫᴜᴀɴᴛɪᴛʏ. ᴜѕᴇ ᴀ ᴡʜᴏʟᴇ ɴᴜᴍʙᴇʀ ɢʀᴇᴀᴛᴇʀ ᴛʜᴀɴ 0."
            )));
            Order order = getOrder(pendingEdit.orderId());
            if (order != null) {
                resendEditQuantityPrompt(player, order);
            }
            return;
        }

        EditOrderResult result = updateOrderQuantity(player, pendingEdit.orderId(), quantity);
        handleEditUpdateResult(player, pendingEdit, result);
    }

    private void handleEditPriceInput(Player player, PendingOrderEdit pendingEdit, String input) {
        double priceEach;
        try {
            priceEach = NumberUtils.parse(input);
        } catch (NumberFormatException exception) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.INVALID_PRICE",
                    "&cɪɴᴠᴀʟɪᴅ ᴘʀɪᴄᴇ ꜰᴏʀᴍᴀᴛ. ᴜѕᴇ ɴᴜᴍʙᴇʀѕ ʟɪᴋᴇ 100, 5ᴋ, ᴏʀ 1.5ᴍ."
            )));
            Order order = getOrder(pendingEdit.orderId());
            if (order != null) {
                resendEditPricePrompt(player, order);
            }
            return;
        }

        EditOrderResult result = updateOrderPrice(player, pendingEdit.orderId(), priceEach);
        handleEditUpdateResult(player, pendingEdit, result);
    }

    private void handleEditUpdateResult(Player player, PendingOrderEdit pendingEdit, EditOrderResult result) {
        if (result.success()) {
            Order updatedOrder = result.order() == null ? getOrder(pendingEdit.orderId()) : result.order();
            if (updatedOrder == null) {
                pendingEdits.remove(player.getUniqueId());
                player.sendMessage(ColorUtils.toComponent(resolveEditFailureMessage(
                        new EditOrderResult(false, EditFailureReason.DATABASE_ERROR, null, 0D)
                )));
                return;
            }
            pendingEdits.remove(player.getUniqueId());
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.EDIT_UPDATED",
                    "&aᴏʀᴅᴇʀ #{order_id} ᴜᴘᴅᴀᴛᴇᴅ. ɴᴇᴡ ʙᴜᴅɢᴇᴛ: {budget_formatted}.",
                    "{order_id}", String.valueOf(updatedOrder.id()),
                    "{budget}", NumberUtils.format(updatedOrder.totalBudget()),
                    "{budget_formatted}", plugin.getCurrencyManager().formatMoney(updatedOrder.totalBudget())
            )));
            openEditOrderMenu(player, updatedOrder.id(), pendingEdit.navigation());
            return;
        }

        player.sendMessage(ColorUtils.toComponent(resolveEditFailureMessage(result)));
        Order order = result.order() != null ? result.order() : getOrder(pendingEdit.orderId());
        if (order == null || !canRetryEditInput(result.reason())) {
            pendingEdits.remove(player.getUniqueId());
            openEditOrderMenu(player, pendingEdit.orderId(), pendingEdit.navigation());
            return;
        }

        if (pendingEdit.field() == EditField.QUANTITY) {
            resendEditQuantityPrompt(player, order);
        } else {
            resendEditPricePrompt(player, order);
        }
    }

    private boolean canRetryEditInput(EditFailureReason reason) {
        return reason == EditFailureReason.INVALID_QUANTITY
                || reason == EditFailureReason.INVALID_PRICE
                || reason == EditFailureReason.PRICE_OUT_OF_RANGE
                || reason == EditFailureReason.TOTAL_TOO_HIGH
                || reason == EditFailureReason.NO_MONEY;
    }

    private SearchCandidate scoreSearchMaterial(Material material, String normalizedQuery, SearchCategoryMatch categoryMatch) {
        int bestScore = 0;
        SearchMatchTier bestTier = null;
        if (categoryMatch != null && matchesSearchCategory(material, categoryMatch.category())) {
            bestScore = Math.max(bestScore, categoryMatch.score());
            bestTier = categoryMatch.exact() ? SearchMatchTier.EXACT_CATEGORY : SearchMatchTier.FUZZY_CATEGORY;
        }

        String normalizedName = normalizeSearchText(material.name());
        if (normalizedName.equals(normalizedQuery)) {
            return new SearchCandidate(material, Math.max(bestScore, 9_500), SearchMatchTier.DIRECT_MATERIAL);
        }

        int containsIndex = normalizedName.indexOf(normalizedQuery);
        if (containsIndex >= 0) {
            bestScore = Math.max(bestScore, 9_000 - Math.min(500, containsIndex));
            bestTier = SearchMatchTier.DIRECT_MATERIAL;
        }

        int bestPrefixScore = 0;
        for (String token : searchTokens(material)) {
            if (token.equals(normalizedQuery)) {
                bestPrefixScore = Math.max(bestPrefixScore, 8_750);
                continue;
            }
            if (token.startsWith(normalizedQuery)) {
                bestPrefixScore = Math.max(bestPrefixScore, 8_500 - Math.min(250, token.length() - normalizedQuery.length()));
                continue;
            }
            if (normalizedQuery.startsWith(token)) {
                bestPrefixScore = Math.max(bestPrefixScore, 8_250 - Math.min(250, normalizedQuery.length() - token.length()));
            }
        }
        if (bestPrefixScore > 0) {
            bestScore = Math.max(bestScore, bestPrefixScore);
            bestTier = SearchMatchTier.DIRECT_MATERIAL;
        }

        int threshold = fuzzyThreshold(normalizedQuery);
        if (threshold <= 0) {
            return bestScore > 0 ? new SearchCandidate(material, bestScore, bestTier) : null;
        }

        int bestDistance = Integer.MAX_VALUE;
        for (String token : searchTokens(material)) {
            if (Math.abs(token.length() - normalizedQuery.length()) > threshold + 1) {
                continue;
            }
            int distance = damerauLevenshtein(normalizedQuery, token);
            if (distance <= threshold) {
                bestDistance = Math.min(bestDistance, distance);
            }
        }

        if (bestDistance != Integer.MAX_VALUE) {
            int fuzzyScore = 7_000 - (bestDistance * 250);
            if (fuzzyScore > bestScore) {
                bestScore = fuzzyScore;
                bestTier = SearchMatchTier.FUZZY_MATERIAL;
            }
        }

        return bestScore > 0 ? new SearchCandidate(material, bestScore, bestTier) : null;
    }

    private List<String> searchTokens(Material material) {
        List<String> tokens = new ArrayList<>();
        for (String rawToken : material.name().split("_")) {
            String token = normalizeSearchText(rawToken);
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private SearchCategoryMatch resolveSearchCategory(String normalizedQuery) {
        for (SearchCategory category : SearchCategory.values()) {
            for (String alias : searchCategoryAliases(category)) {
                if (alias.equals(normalizedQuery)) {
                    return new SearchCategoryMatch(category, 10_000, true);
                }
            }
        }

        int threshold = fuzzyThreshold(normalizedQuery);
        if (threshold <= 0) {
            return null;
        }

        for (SearchCategory category : SearchCategory.values()) {
            for (String alias : searchCategoryAliases(category)) {
                if (Math.abs(alias.length() - normalizedQuery.length()) > threshold + 1) {
                    continue;
                }
                int distance = damerauLevenshtein(normalizedQuery, alias);
                if (distance <= threshold) {
                    return new SearchCategoryMatch(category, 6_500 - (distance * 250), false);
                }
            }
        }

        return null;
    }

    private List<String> searchCategoryAliases(SearchCategory category) {
        return switch (category) {
            case BLOCKS -> List.of("block", "blocks");
            case ITEMS -> List.of("item", "items");
            case SWORDS -> List.of("sword", "swords");
            case ARMOR -> List.of("armor");
            case FOOD -> List.of("food", "foods");
            case WOOD -> List.of("wood", "woods");
        };
    }

    private boolean matchesSearchCategory(Material material, SearchCategory category) {
        if (!isModernMaterial(material)) {
            return false;
        }

        return switch (category) {
            case BLOCKS -> material.isBlock();
            case ITEMS -> !material.isBlock();
            case SWORDS -> material.name().endsWith("_SWORD");
            case ARMOR -> isArmorMaterial(material);
            case FOOD -> material.isEdible();
            case WOOD -> isWoodFamilyMaterial(material);
        };
    }

    private List<OrderCatalogEntry> getServerMaterialCatalogEntries(String categoryKey) {
        String normalizedCategory = normalizeServerCatalogCategory(categoryKey);
        List<OrderCatalogEntry> entries = new ArrayList<>();
        for (Material material : Material.values()) {
            if (!isOrderable(material) || !matchesServerCatalogCategory(material, normalizedCategory)) {
                continue;
            }
            entries.add(new OrderCatalogEntry(normalizedCategory, material));
        }
        entries.sort(Comparator.comparing(entry -> entry.material().name()));
        return List.copyOf(entries);
    }

    private String normalizeServerCatalogCategory(String rawCategory) {
        if (rawCategory == null || rawCategory.isBlank()) {
            return "ALL";
        }

        String normalized = rawCategory.trim().toUpperCase(Locale.US);
        for (ServerCatalogCategory category : SERVER_CATALOG_CATEGORIES) {
            if (category.key().equals(normalized)) {
                return normalized;
            }
        }
        return "ALL";
    }

    private ServerCatalogCategory serverCategory(String categoryKey) {
        String normalized = normalizeServerCatalogCategory(categoryKey);
        for (ServerCatalogCategory category : SERVER_CATALOG_CATEGORIES) {
            if (category.key().equals(normalized)) {
                return category;
            }
        }
        return SERVER_CATALOG_CATEGORIES.get(0);
    }

    private boolean matchesServerCatalogCategory(Material material, String categoryKey) {
        if (!isModernMaterial(material) || material.isAir() || !material.isItem()) {
            return false;
        }

        return switch (normalizeServerCatalogCategory(categoryKey)) {
            case "ALL" -> true;
            case "BLOCKS" -> material.isBlock();
            case "ITEMS" -> !material.isBlock() && !isFoodMaterial(material) && !isToolMaterial(material)
                    && !isSwordMaterial(material) && !isArmorMaterial(material)
                    && !isCombatMaterial(material) && !isRedstoneMaterial(material);
            case "FOOD" -> isFoodMaterial(material);
            case "TOOLS" -> isToolMaterial(material);
            case "SWORDS" -> isSwordMaterial(material);
            case "ARMOR" -> isArmorMaterial(material);
            case "COMBAT" -> isCombatMaterial(material);
            case "REDSTONE" -> isRedstoneMaterial(material);
            default -> true;
        };
    }

    private boolean isFoodMaterial(Material material) {
        return isModernMaterial(material) && material.isEdible();
    }

    private boolean isSwordMaterial(Material material) {
        return isModernMaterial(material) && material.name().endsWith("_SWORD");
    }

    private boolean isArmorMaterial(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS")
                || name.endsWith("_BOOTS")
                || material == Material.TURTLE_HELMET
                || material == Material.ELYTRA;
    }

    private boolean isToolMaterial(Material material) {
        if (!isModernMaterial(material)) {
            return false;
        }

        String name = material.name();
        return name.endsWith("_PICKAXE")
                || name.endsWith("_AXE")
                || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE")
                || material == Material.SHEARS
                || material == Material.FLINT_AND_STEEL
                || material == Material.FISHING_ROD
                || material == Material.BRUSH
                || material == Material.COMPASS
                || material == Material.CLOCK
                || material == Material.LEAD
                || material == Material.NAME_TAG;
    }

    private boolean isCombatMaterial(Material material) {
        if (!isModernMaterial(material)) {
            return false;
        }

        String name = material.name();
        return isSwordMaterial(material)
                || isArmorMaterial(material)
                || name.contains("BOW")
                || name.contains("ARROW")
                || name.contains("SHIELD")
                || material == Material.TRIDENT
                || material == Material.MACE
                || material == Material.TOTEM_OF_UNDYING;
    }

    private boolean isRedstoneMaterial(Material material) {
        if (!isModernMaterial(material)) {
            return false;
        }

        String name = material.name();
        return name.contains("REDSTONE")
                || name.contains("PISTON")
                || name.contains("OBSERVER")
                || name.contains("DISPENSER")
                || name.contains("DROPPER")
                || name.contains("HOPPER")
                || name.contains("REPEATER")
                || name.contains("COMPARATOR")
                || name.contains("DAYLIGHT_DETECTOR")
                || name.contains("PRESSURE_PLATE")
                || name.contains("BUTTON")
                || name.contains("LEVER")
                || name.contains("TRIPWIRE")
                || name.contains("SCULK_SENSOR")
                || material == Material.TARGET
                || material == Material.NOTE_BLOCK
                || material == Material.TNT
                || material == Material.CRAFTER;
    }

    private boolean isWoodFamilyMaterial(Material material) {
        String name = material.name();
        return name.contains("WOOD")
                || name.contains("LOG")
                || name.contains("PLANKS")
                || name.contains("STEM")
                || name.contains("HYPHAE");
    }

    private String normalizeSearchText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        String lower = rawText.toLowerCase(Locale.US);
        StringBuilder normalized = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char character = lower.charAt(i);
            if (character == ' ' || character == '_' || character == '-') {
                continue;
            }
            if (Character.isLetterOrDigit(character)) {
                normalized.append(character);
            }
        }
        return normalized.toString();
    }

    private int fuzzyThreshold(String normalizedQuery) {
        int length = normalizedQuery.length();
        if (length <= 2) {
            return 0;
        }
        if (length <= 4) {
            return 1;
        }
        if (length <= 7) {
            return 2;
        }
        return 3;
    }

    private int damerauLevenshtein(String left, String right) {
        int[][] distances = new int[left.length() + 1][right.length() + 1];
        for (int i = 0; i <= left.length(); i++) {
            distances[i][0] = i;
        }
        for (int j = 0; j <= right.length(); j++) {
            distances[0][j] = j;
        }

        for (int i = 1; i <= left.length(); i++) {
            for (int j = 1; j <= right.length(); j++) {
                int substitutionCost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                int distance = Math.min(
                        Math.min(distances[i - 1][j] + 1, distances[i][j - 1] + 1),
                        distances[i - 1][j - 1] + substitutionCost
                );

                if (i > 1
                        && j > 1
                        && left.charAt(i - 1) == right.charAt(j - 2)
                        && left.charAt(i - 2) == right.charAt(j - 1)) {
                    distance = Math.min(distance, distances[i - 2][j - 2] + 1);
                }

                distances[i][j] = distance;
            }
        }

        return distances[left.length()][right.length()];
    }

    private EditOrderResult updateOrderBudget(Player player, Order order, int requestedQuantity, double priceEach) {
        if (requestedQuantity <= 0 || requestedQuantity > getMaxQuantityPerOrder()) {
            return new EditOrderResult(false, EditFailureReason.INVALID_QUANTITY, order, 0D);
        }

        double normalizedPrice = roundCurrency(priceEach);
        if (!Double.isFinite(normalizedPrice)) {
            return new EditOrderResult(false, EditFailureReason.INVALID_PRICE, order, 0D);
        }
        if (normalizedPrice < getMinPriceEach() || normalizedPrice > getMaxPriceEach()) {
            return new EditOrderResult(false, EditFailureReason.PRICE_OUT_OF_RANGE, order, 0D);
        }

        double totalBudget = roundCurrency(requestedQuantity * normalizedPrice);
        if (totalBudget <= 0D || totalBudget > getMaxTotalBudget()) {
            return new EditOrderResult(false, EditFailureReason.TOTAL_TOO_HIGH, order, 0D);
        }

        double balanceDelta = roundCurrency(totalBudget - order.escrowRemaining());
        boolean withdrewDelta = false;
        if (balanceDelta > EPSILON) {
            if (!plugin.getEconomyManager().has(player, balanceDelta)) {
                return new EditOrderResult(false, EditFailureReason.NO_MONEY, order, balanceDelta);
            }

            EconomyTransactionResult withdraw = plugin.getEconomyManager().withdraw(player, balanceDelta, EconomyReason.ORDER_CREATE_ESCROW);
            if (!withdraw.success()) {
                return new EditOrderResult(false, economyFailureToEditFailure(withdraw), order, balanceDelta);
            }
            withdrewDelta = true;
        }

        boolean updated = updateOrderBudgetRow(player, order.id(), requestedQuantity, normalizedPrice, totalBudget);
        if (!updated) {
            if (withdrewDelta) {
                plugin.getEconomyManager().deposit(player, balanceDelta, EconomyReason.ORDER_REFUND);
            }
            return new EditOrderResult(false, EditFailureReason.DATABASE_ERROR, order, balanceDelta);
        }

        if (balanceDelta < -EPSILON) {
            double refund = roundCurrency(Math.abs(balanceDelta));
            EconomyTransactionResult deposit = plugin.getEconomyManager().deposit(player, refund, EconomyReason.ORDER_REFUND);
            if (!deposit.success()) {
                updateOrderBudgetRow(player, order.id(), order.requestedQuantity(), order.priceEach(), order.totalBudget());
                return new EditOrderResult(false, economyFailureToEditFailure(deposit), order, balanceDelta);
            }
        }

        return new EditOrderResult(true, null, getOrder(order.id()), balanceDelta);
    }

    private boolean updateOrderBudgetRow(Player player, long orderId, int requestedQuantity, double priceEach, double totalBudget) {
        try (PreparedStatement ps = connection().prepareStatement(
                "UPDATE orders SET requested_quantity = ?, price_each = ?, total_budget = ?, escrow_remaining = ? " +
                        "WHERE id = ? AND owner_uuid = ? AND status = ? AND delivered_quantity = 0")) {
            ps.setInt(1, requestedQuantity);
            ps.setDouble(2, roundCurrency(priceEach));
            ps.setDouble(3, roundCurrency(totalBudget));
            ps.setDouble(4, roundCurrency(totalBudget));
            ps.setLong(5, orderId);
            ps.setString(6, player.getUniqueId().toString());
            ps.setString(7, OrderStatus.ACTIVE.name());
            return ps.executeUpdate() == 1;
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to update order budget " + orderId, exception);
            return false;
        }
    }

    private EditFailureReason economyFailureToEditFailure(EconomyTransactionResult result) {
        if (result == null) {
            return EditFailureReason.DATABASE_ERROR;
        }
        if (result.insufficientFunds()) {
            return EditFailureReason.NO_MONEY;
        }
        if (result.noPlayerData() || result.playerNotFound()) {
            return EditFailureReason.NO_PLAYER_DATA;
        }
        return EditFailureReason.DATABASE_ERROR;
    }

    private void resendEditQuantityPrompt(Player player, Order order) {
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_EDIT_QUANTITY",
                "&7ᴇɴᴛᴇʀ ᴛʜᴇ ɴᴇᴡ ǫᴜᴀɴᴛɪᴛʏ ꜰᴏʀ ᴏʀᴅᴇʀ &f#{order_id}&7. ᴄᴜʀʀᴇɴᴛ: &e{quantity}&7. ᴛʏᴘᴇ &cᴄᴀɴᴄᴇʟ&7 ᴛᴏ ᴀʙᴏʀᴛ.",
                "{order_id}", String.valueOf(order.id()),
                "{quantity}", String.valueOf(order.requestedQuantity())
        )));
    }

    private void resendEditPricePrompt(Player player, Order order) {
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_EDIT_PRICE",
                "&7ᴇɴᴛᴇʀ ᴛʜᴇ ɴᴇᴡ ᴘʀɪᴄᴇ ᴇᴀᴄʜ ꜰᴏʀ ᴏʀᴅᴇʀ &f#{order_id}&7. ᴄᴜʀʀᴇɴᴛ: {price_formatted}&7. ᴛʏᴘᴇ &cᴄᴀɴᴄᴇʟ&7 ᴛᴏ ᴀʙᴏʀᴛ.",
                "{order_id}", String.valueOf(order.id()),
                "{price}", NumberUtils.format(order.priceEach()),
                "{price_formatted}", plugin.getCurrencyManager().formatMoney(order.priceEach())
        )));
    }

    private OrderEditNavigation normalizeNavigation(
            boolean backToMyOrders,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {
        return new OrderEditNavigation(
                backToMyOrders,
                Math.max(1, originPage),
                sortMode == null ? getDefaultSort() : sortMode,
                categoryFilter == null ? "ALL" : categoryFilter
        );
    }

    private void openEditOrderMenu(Player player, long orderId, OrderEditNavigation navigation) {
        OrderEditNavigation effectiveNavigation = navigation == null
                ? normalizeNavigation(true, 1, getDefaultSort(), "ALL")
                : navigation;
        new OrdersEditMenu(
                plugin,
                orderId,
                effectiveNavigation.backToMyOrders(),
                effectiveNavigation.originPage(),
                effectiveNavigation.sortMode(),
                effectiveNavigation.categoryFilter()
        ).open(player);
    }

    private void handleQuantityInput(Player player, PendingOrderCreation pending, String input) {
        int quantity;
        try {
            quantity = Math.toIntExact(NumberUtils.parseLong(input));
        } catch (RuntimeException exception) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.INVALID_QUANTITY",
                    "&cɪɴᴠᴀʟɪᴅ ǫᴜᴀɴᴛɪᴛʏ. ᴜѕᴇ ᴀ ᴡʜᴏʟᴇ ɴᴜᴍʙᴇʀ ɢʀᴇᴀᴛᴇʀ ᴛʜᴀɴ 0."
            )));
            resendQuantityPrompt(player, pending.requestedItem());
            return;
        }

        if (quantity <= 0 || quantity > getMaxQuantityPerOrder()) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.QUANTITY_OUT_OF_RANGE",
                    "&cǫᴜᴀɴᴛɪᴛʏ ᴍᴜѕᴛ ʙᴇ ʙᴇᴛᴡᴇᴇɴ 1 ᴀɴᴅ {max}.",
                    "{max}", String.valueOf(getMaxQuantityPerOrder())
            )));
            resendQuantityPrompt(player, pending.requestedItem());
            return;
        }

        pendingCreations.put(player.getUniqueId(), pending.withQuantity(quantity));
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_PRICE",
                "&7ᴇɴᴛᴇʀ ᴛʜᴇ ᴘʀɪᴄᴇ ᴇᴀᴄʜ ꜰᴏʀ &f{item}&7 ɪɴ ᴄʜᴀᴛ. ᴛʏᴘᴇ &cᴄᴀɴᴄᴇʟ&7 ᴛᴏ ᴀʙᴏʀᴛ.",
                "{item}", describeItem(pending.requestedItem())
        )));
    }

    private void handlePriceInput(Player player, PendingOrderCreation pending, String input) {
        double priceEach;
        try {
            priceEach = NumberUtils.parse(input);
        } catch (NumberFormatException exception) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.INVALID_PRICE",
                    "&cɪɴᴠᴀʟɪᴅ ᴘʀɪᴄᴇ ꜰᴏʀᴍᴀᴛ. ᴜѕᴇ ɴᴜᴍʙᴇʀѕ ʟɪᴋᴇ 100, 5ᴋ, ᴏʀ 1.5ᴍ."
            )));
            resendPricePrompt(player, pending.requestedItem());
            return;
        }

        double normalizedPrice = roundCurrency(priceEach);
        double totalBudget = roundCurrency(normalizedPrice * pending.quantity());
        if (normalizedPrice < getMinPriceEach() || normalizedPrice > getMaxPriceEach()) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.PRICE_OUT_OF_RANGE",
                    "&cᴘʀɪᴄᴇ ᴇᴀᴄʜ ᴍᴜѕᴛ ʙᴇ ʙᴇᴛᴡᴇᴇɴ &f{min_formatted}&c ᴀɴᴅ &f{max_formatted}&c.",
                    "{min}", NumberUtils.format(getMinPriceEach()),
                    "{min_formatted}", plugin.getCurrencyManager().formatMoney(getMinPriceEach()),
                    "{max}", NumberUtils.format(getMaxPriceEach()),
                    "{max_formatted}", plugin.getCurrencyManager().formatMoney(getMaxPriceEach())
            )));
            resendPricePrompt(player, pending.requestedItem());
            return;
        }

        if (totalBudget <= 0D || totalBudget > getMaxTotalBudget()) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.TOTAL_TOO_HIGH",
                    "&cᴛᴏᴛᴀʟ ᴏʀᴅᴇʀ ʙᴜᴅɢᴇᴛ ᴄᴀɴɴᴏᴛ ᴇxᴄᴇᴇᴅ &f{max_formatted}&c.",
                    "{max}", NumberUtils.format(getMaxTotalBudget()),
                    "{max_formatted}", plugin.getCurrencyManager().formatMoney(getMaxTotalBudget())
            )));
            resendPricePrompt(player, pending.requestedItem());
            return;
        }

        pendingCreations.put(player.getUniqueId(), pending.withPriceEach(normalizedPrice));
        new OrdersNewMenu(plugin).open(player);
    }

    private void resendQuantityPrompt(Player player, ItemStack item) {
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_QUANTITY",
                "&7ᴇɴᴛᴇʀ ᴛʜᴇ ᴏʀᴅᴇʀ ǫᴜᴀɴᴛɪᴛʏ ꜰᴏʀ &f{item}&7 ɪɴ ᴄʜᴀᴛ. ᴛʏᴘᴇ &cᴄᴀɴᴄᴇʟ&7 ᴛᴏ ᴀʙᴏʀᴛ.",
                "{item}", describeItem(item)
        )));
    }

    private void resendPricePrompt(Player player, ItemStack item) {
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_PRICE",
                "&7ᴇɴᴛᴇʀ ᴛʜᴇ ᴘʀɪᴄᴇ ᴇᴀᴄʜ ꜰᴏʀ &f{item}&7 ɪɴ ᴄʜᴀᴛ. ᴛʏᴘᴇ &cᴄᴀɴᴄᴇʟ&7 ᴛᴏ ᴀʙᴏʀᴛ.",
                "{item}", describeItem(item)
        )));
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getOrders();
    }

    private Connection connection() {
        return plugin.getDatabaseManager().getConnection();
    }

    public List<Order> getActiveOrders(OrderSort sortMode, String categoryKey) {
        List<Order> orders = new ArrayList<>();
        long now = System.currentTimeMillis();
        String normalizedCategory = normalizeCategory(categoryKey);

        String sql = "SELECT * FROM orders WHERE status = ? AND expires_at > ?"
                + (normalizedCategory.equals("ALL") ? "" : " AND category_key = ?");
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, OrderStatus.ACTIVE.name());
            ps.setLong(2, now);
            if (!normalizedCategory.equals("ALL")) {
                ps.setString(3, normalizedCategory);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    if (order != null) {
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load active orders", e);
        }

        orders.sort(resolveComparator(sortMode));
        return orders;
    }

    public List<Order> getOrdersForOwner(UUID ownerUuid, OrderSort sortMode) {
        List<Order> orders = new ArrayList<>();

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM orders WHERE owner_uuid = ? ORDER BY created_at DESC, id DESC")) {
            ps.setString(1, ownerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    if (order != null) {
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load owner orders", e);
        }

        orders.sort(resolveComparator(sortMode));
        return orders;
    }

    public List<OrderCollectionClaim> getUnclaimedClaims(UUID ownerUuid) {
        List<OrderCollectionClaim> claims = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM order_claims WHERE owner_uuid = ? AND claimed_at = 0 ORDER BY created_at DESC, id DESC")) {
            ps.setString(1, ownerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderCollectionClaim claim = mapClaim(rs);
                    if (claim != null) {
                        claims.add(claim);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load order claims", e);
        }
        return claims;
    }

    public List<OrderDelivery> getRecentDeliveries(long orderId, int limit) {
        List<OrderDelivery> deliveries = new ArrayList<>();
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM order_deliveries WHERE order_id = ? ORDER BY created_at DESC, id DESC LIMIT ?")) {
            ps.setLong(1, orderId);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    deliveries.add(new OrderDelivery(
                            rs.getLong("id"),
                            rs.getLong("order_id"),
                            UUID.fromString(rs.getString("deliverer_uuid")),
                            rs.getString("deliverer_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("payout"),
                            rs.getLong("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load order deliveries", e);
        }
        return deliveries;
    }

    public Order getOrder(long orderId) {
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM orders WHERE id = ? LIMIT 1")) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load order " + orderId, e);
        }
        return null;
    }

    public OrderCollectionClaim getClaim(long claimId) {
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM order_claims WHERE id = ? LIMIT 1")) {
            ps.setLong(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapClaim(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load order claim " + claimId, e);
        }
        return null;
    }

    public DeliveryPreview getDeliveryPreview(Player player, long orderId) {
        if (!isEnabled()) {
            return new DeliveryPreview(false, DeliveryFailureReason.DISABLED, null, 0, 0D);
        }

        Order order = getOrder(orderId);
        if (order == null) {
            return new DeliveryPreview(false, DeliveryFailureReason.ORDER_NOT_FOUND, null, 0, 0D);
        }

        if (!order.active() || order.expiresAt() <= System.currentTimeMillis()) {
            if (order.active() && order.expiresAt() <= System.currentTimeMillis()) {
                expireOrder(order);
                order = getOrder(orderId);
            }
            return new DeliveryPreview(false, DeliveryFailureReason.NOT_ACTIVE, order, 0, 0D);
        }

        if (order.ownerUuid().equals(player.getUniqueId())) {
            return new DeliveryPreview(false, DeliveryFailureReason.OWN_ORDER, order, 0, 0D);
        }

        if (order.remainingQuantity() <= 0) {
            return new DeliveryPreview(false, DeliveryFailureReason.ORDER_FULL, order, 0, 0D);
        }

        ItemStack requestedItem = order.requestedItem().clone();
        requestedItem.setAmount(1);
        int matching = countMatchingItems(player, requestedItem);
        int deliverCap = Math.max(1, Math.min(getMaxDeliverPerClick(), requestedItem.getMaxStackSize()));
        int deliverQuantity = Math.min(order.remainingQuantity(), Math.min(matching, deliverCap));
        if (deliverQuantity <= 0) {
            return new DeliveryPreview(false, DeliveryFailureReason.NO_MATCHING_ITEMS, order, 0, 0D);
        }

        double payout = roundCurrency(deliverQuantity * order.priceEach());
        if (payout <= 0D || payout - order.escrowRemaining() > EPSILON) {
            return new DeliveryPreview(false, DeliveryFailureReason.PAYOUT_ERROR, order, 0, 0D);
        }

        return new DeliveryPreview(true, null, order, deliverQuantity, payout);
    }

    public synchronized DeliverOrderResult deliverOrder(Player player, long orderId) {
        if (!isEnabled()) {
            return new DeliverOrderResult(false, DeliveryFailureReason.DISABLED, null, 0, 0D);
        }

        PlayerData delivererData = getPlayerData(player);
        if (delivererData == null) {
            return new DeliverOrderResult(false, DeliveryFailureReason.NO_PLAYER_DATA, null, 0, 0D);
        }

        DeliveryPreview preview = getDeliveryPreview(player, orderId);
        if (!preview.success()) {
            return new DeliverOrderResult(false, preview.reason(), preview.order(), 0, 0D);
        }

        ItemStack requestedItem = preview.order().requestedItem().clone();
        requestedItem.setAmount(1);
        RemovedOrderItems removedItems = removeMatchingItems(player.getInventory(), requestedItem, preview.deliverQuantity());

        if (!removedItems.success() || isMissingItem(removedItems.deliveredStack())) {
            return new DeliverOrderResult(false, DeliveryFailureReason.NO_MATCHING_ITEMS, preview.order(), 0, 0D);
        }

        ItemStack deliveredStack = removedItems.deliveredStack();
        long now = System.currentTimeMillis();
        boolean updated = applyDelivery(preview.order(), player, deliveredStack, removedItems.quantity(), preview.payout(), now);
        if (!updated) {
            player.getInventory().addItem(deliveredStack);
            player.updateInventory();
            return new DeliverOrderResult(false, DeliveryFailureReason.DATABASE_ERROR, preview.order(), 0, 0D);
        }

        var payoutResult = plugin.getEconomyManager().deposit(player, preview.payout(), EconomyReason.ORDER_DELIVERY_PAYOUT);
        if (!payoutResult.success()) {
            revertDelivery(preview.order().id(), player.getUniqueId(), deliveredStack, removedItems.quantity(), preview.payout(), now);
            player.getInventory().addItem(deliveredStack);
            player.updateInventory();
            return new DeliverOrderResult(false, DeliveryFailureReason.PAYOUT_ERROR, preview.order(), 0, 0D);
        }

        delivererData.addMoneyMade(preview.payout());
        plugin.getDatabaseManager().savePlayer(delivererData);
        player.updateInventory();
        return new DeliverOrderResult(true, null, getOrder(orderId), preview.deliverQuantity(), preview.payout());
    }

    public synchronized CancelOrderResult cancelOrder(Player player, long orderId) {
        if (!isEnabled()) {
            return new CancelOrderResult(false, CancelFailureReason.DISABLED, null);
        }

        Order order = getOrder(orderId);
        if (order == null) {
            return new CancelOrderResult(false, CancelFailureReason.ORDER_NOT_FOUND, null);
        }

        if (!order.ownerUuid().equals(player.getUniqueId())) {
            return new CancelOrderResult(false, CancelFailureReason.NOT_OWNER, order);
        }

        if (!order.active()) {
            return new CancelOrderResult(false, CancelFailureReason.NOT_ACTIVE, order);
        }

        if (!closeOrderAndCreateRefundClaim(order, OrderStatus.CANCELLED, System.currentTimeMillis())) {
            return new CancelOrderResult(false, CancelFailureReason.DATABASE_ERROR, order);
        }

        return new CancelOrderResult(true, null, getOrder(orderId));
    }

    public synchronized ClaimResult claim(Player player, long claimId) {
        if (!isEnabled()) {
            return new ClaimResult(false, ClaimFailureReason.DISABLED, null);
        }

        OrderCollectionClaim claim = getClaim(claimId);
        if (claim == null) {
            return new ClaimResult(false, ClaimFailureReason.CLAIM_NOT_FOUND, null);
        }

        if (!claim.ownerUuid().equals(player.getUniqueId())) {
            return new ClaimResult(false, ClaimFailureReason.NOT_OWNER, claim);
        }

        if (claim.claimed()) {
            return new ClaimResult(false, ClaimFailureReason.ALREADY_CLAIMED, claim);
        }

        if (claim.refundClaim()) {
            PlayerData ownerData = getPlayerData(player);
            if (ownerData == null) {
                return new ClaimResult(false, ClaimFailureReason.NO_PLAYER_DATA, claim);
            }

            if (!markClaimClaimed(claim.id(), player.getUniqueId(), System.currentTimeMillis())) {
                return new ClaimResult(false, ClaimFailureReason.DATABASE_ERROR, claim);
            }

            var depositResult = plugin.getEconomyManager().deposit(player, claim.moneyAmount(), EconomyReason.ORDER_REFUND);
            if (!depositResult.success()) {
                reopenClaim(claim.id());
                return new ClaimResult(false, ClaimFailureReason.NO_PLAYER_DATA, claim);
            }

            clearEscrowRemaining(claim.orderId());
            ownerData.setMoneySpent(Math.max(0D, ownerData.getMoneySpent() - claim.moneyAmount()));
            plugin.getDatabaseManager().savePlayer(ownerData);
            return new ClaimResult(true, null, getClaim(claim.id()));
        }

        if (isMissingItem(claim.item())) {
            plugin.getLogger().warning("Order claim #" + claim.id() + " has unreadable item data; refusing item claim.");
            return new ClaimResult(false, ClaimFailureReason.DATABASE_ERROR, claim);
        }

        if (!canFitItem(player, claim.item())) {
            return new ClaimResult(false, ClaimFailureReason.INVENTORY_FULL, claim);
        }

        if (!markClaimClaimed(claim.id(), player.getUniqueId(), System.currentTimeMillis())) {
            return new ClaimResult(false, ClaimFailureReason.DATABASE_ERROR, claim);
        }

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(claim.item().clone());
        if (!leftovers.isEmpty()) {
            reopenClaim(claim.id());
            return new ClaimResult(false, ClaimFailureReason.INVENTORY_FULL, claim);
        }

        incrementCollectedQuantity(claim.orderId(), claim.item() == null ? 0 : claim.item().getAmount());
        player.updateInventory();
        return new ClaimResult(true, null, getClaim(claim.id()));
    }

    public synchronized int expireOrders() {
        int expired = 0;
        long now = System.currentTimeMillis();
        List<Order> orders = new ArrayList<>();

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT * FROM orders WHERE status = ? AND expires_at <= ?")) {
            ps.setString(1, OrderStatus.ACTIVE.name());
            ps.setLong(2, now);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrder(rs);
                    if (order != null) {
                        orders.add(order);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to scan expired orders", e);
        }

        for (Order order : orders) {
            if (expireOrder(order)) {
                expired++;
            }
        }

        return expired;
    }

    private void rebuildCatalog() {
        catalogByCategory.clear();
        categoryOrder.clear();
        categoryOrder.add("ALL");

        ConfigurationSection categoriesSection = config().getConfigurationSection("CATEGORY_FILTERS");
        if (categoriesSection == null) {
            catalogByCategory.put("BLOCKS", List.of(new OrderCatalogEntry("BLOCKS", Material.STONE)));
            categoryOrder.add("BLOCKS");
            return;
        }

        Set<Material> globallySeen = EnumSet.noneOf(Material.class);
        for (String preferredCategory : DEFAULT_CATEGORY_ORDER) {
            if (preferredCategory.equals("ALL") || !categoriesSection.contains(preferredCategory)) {
                continue;
            }
            List<OrderCatalogEntry> entries = parseCategoryEntries(preferredCategory, categoriesSection, globallySeen);
            if (!entries.isEmpty()) {
                catalogByCategory.put(preferredCategory, entries);
                categoryOrder.add(preferredCategory);
            }
        }

        for (String key : categoriesSection.getKeys(false)) {
            String normalized = key.trim().toUpperCase(Locale.US);
            if (normalized.equals("ALL") || categoryOrder.contains(normalized)) {
                continue;
            }

            List<OrderCatalogEntry> entries = parseCategoryEntries(normalized, categoriesSection, globallySeen);
            if (!entries.isEmpty()) {
                catalogByCategory.put(normalized, entries);
                categoryOrder.add(normalized);
            }
        }

        if (catalogByCategory.isEmpty()) {
            catalogByCategory.put("BLOCKS", List.of(new OrderCatalogEntry("BLOCKS", Material.STONE)));
            categoryOrder.add("BLOCKS");
        }
    }

    private List<OrderCatalogEntry> parseCategoryEntries(
            String categoryKey,
            ConfigurationSection categoriesSection,
            Set<Material> globallySeen
    ) {
        List<OrderCatalogEntry> entries = new ArrayList<>();
        for (String rawMaterial : categoriesSection.getStringList(categoryKey)) {
            Material material = parseMaterial(rawMaterial);
            if (material == null || !isOrderable(material) || !globallySeen.add(material)) {
                continue;
            }
            entries.add(new OrderCatalogEntry(categoryKey, material));
        }
        return entries;
    }

    private int normalizeSize(int configured) {
        int bounded = Math.max(9, Math.min(54, configured));
        int remainder = bounded % 9;
        return remainder == 0 ? bounded : bounded + (9 - remainder);
    }

    private long getOrderDurationMillis() {
        long hours = Math.max(1L, config().getLong("SETTINGS.ORDER_DURATION_HOURS", 168L));
        return hours * 60L * 60L * 1000L;
    }

    private int getMaxActiveOrders(Player player) {
        int defaultValue = Math.max(1, config().getInt("SETTINGS.MAX_ACTIVE_ORDERS_DEFAULT", 5));
        ConfigurationSection limitsSection = config().getConfigurationSection("SETTINGS.MAX_ACTIVE_ORDERS_BY_PERMISSION");
        if (limitsSection == null || player == null) {
            return defaultValue;
        }

        int resolved = defaultValue;
        for (String permission : limitsSection.getKeys(false)) {
            if (!PermissionUtils.has(player, permission)) {
                continue;
            }
            resolved = Math.max(resolved, limitsSection.getInt(permission, defaultValue));
        }
        return resolved;
    }

    private long getClickCooldownMillis() {
        return Math.max(250L, config().getLong("SETTINGS.CLICK_COOLDOWN_MS", 750L));
    }

    private int getMaxDeliverPerClick() {
        return Math.max(1, config().getInt("DELIVERY.MAX_DELIVER_PER_CLICK", 64));
    }

    private int getMaxQuantityPerOrder() {
        return Math.max(1, config().getInt("SETTINGS.MAX_QUANTITY_PER_ORDER", 2304));
    }

    private double getMinPriceEach() {
        return Math.max(0.01D, config().getDouble("PRICING.MIN_PRICE_EACH", 10D));
    }

    private double getMaxPriceEach() {
        return Math.max(getMinPriceEach(), config().getDouble("PRICING.MAX_PRICE_EACH", 250_000_000D));
    }

    private double getMaxTotalBudget() {
        return Math.max(getMinPriceEach(), config().getDouble("PRICING.MAX_TOTAL_BUDGET", 250_000_000D));
    }

    private double getCreationFee() {
        return Math.max(0D, config().getDouble("PRICING.ORDER_CREATION_FEE", 0D));
    }

    private boolean isOrderable(Material material) {
        if (!isModernMaterial(material) || material.isAir() || !material.isItem()) {
            return false;
        }

        Set<Material> blocked = EnumSet.noneOf(Material.class);
        for (String rawMaterial : config().getStringList("MATCHING.BLOCKED_MATERIALS")) {
            Material blockedMaterial = parseMaterial(rawMaterial);
            if (blockedMaterial != null) {
                blocked.add(blockedMaterial);
            }
        }
        return !blocked.contains(material);
    }

    private ItemStack prepareRequestedItem(ItemStack item) {
        if (isMissingItem(item)) {
            return null;
        }

        ItemStack prepared = item.clone();
        prepared = plugin.getWorthManager().stripWorthDisplay(prepared);
        if (isMissingItem(prepared)) {
            return null;
        }

        prepared.setAmount(1);
        return prepared;
    }

    private Material parseMaterial(String rawMaterial) {
        if (rawMaterial == null || rawMaterial.isBlank()) {
            return null;
        }

        try {
            Material material = Material.valueOf(rawMaterial.trim().toUpperCase(Locale.US));
            return isModernMaterial(material) ? material : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean isModernMaterial(Material material) {
        return material != null
                && !material.name().startsWith("LEGACY_")
                && !material.isLegacy();
    }

    private boolean isMissingItem(ItemStack item) {
        if (item == null) {
            return true;
        }

        Material material = item.getType();
        return !isModernMaterial(material) || material.isAir();
    }

    private int countActiveOrders(UUID ownerUuid) {
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT COUNT(*) FROM orders WHERE owner_uuid = ? AND status = ? AND expires_at > ?")) {
            ps.setString(1, ownerUuid.toString());
            ps.setString(2, OrderStatus.ACTIVE.name());
            ps.setLong(3, System.currentTimeMillis());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to count active orders", e);
        }
        return 0;
    }

    private long insertOrder(
            UUID ownerUuid,
            String ownerName,
            ItemStack requestedItem,
            String categoryKey,
            int requestedQuantity,
            double priceEach,
            double totalBudget,
            long createdAt,
            long expiresAt
    ) {
        try (PreparedStatement ps = connection().prepareStatement(
                "INSERT INTO orders " +
                        "(owner_uuid, owner_name, requested_item_data, requested_material_key, category_key, status, requested_quantity, delivered_quantity, collected_quantity, " +
                        "price_each, total_budget, paid_amount, escrow_remaining, created_at, expires_at, closed_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, ownerUuid.toString());
            ps.setString(2, ownerName);
            ps.setString(3, serializeItem(requestedItem));
            ps.setString(4, requestedItem.getType().name());
            ps.setString(5, normalizeCategory(categoryKey));
            ps.setString(6, OrderStatus.ACTIVE.name());
            ps.setInt(7, requestedQuantity);
            ps.setInt(8, 0);
            ps.setInt(9, 0);
            ps.setDouble(10, roundCurrency(priceEach));
            ps.setDouble(11, totalBudget);
            ps.setDouble(12, 0D);
            ps.setDouble(13, totalBudget);
            ps.setLong(14, createdAt);
            ps.setLong(15, expiresAt);
            ps.setLong(16, 0L);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to insert order", e);
        }
        return -1L;
    }

    private boolean applyDelivery(
            Order order,
            Player deliverer,
            ItemStack deliveredStack,
            int deliveredQuantity,
            double payout,
            long createdAt
    ) {
        try {
            Connection conn = connection();
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Order liveOrder = getOrder(order.id());
                if (liveOrder == null || !liveOrder.active() || liveOrder.remainingQuantity() <= 0) {
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return false;
                }

                int actualQuantity = Math.min(deliveredQuantity, liveOrder.remainingQuantity());
                if (actualQuantity <= 0) {
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return false;
                }

                double actualPayout = roundCurrency(actualQuantity * liveOrder.priceEach());
                if (actualPayout <= 0D || actualPayout - liveOrder.escrowRemaining() > EPSILON) {
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return false;
                }

                int newDeliveredQuantity = liveOrder.deliveredQuantity() + actualQuantity;
                double newPaidAmount = roundCurrency(liveOrder.paidAmount() + actualPayout);
                double newEscrow = roundCurrency(Math.max(0D, liveOrder.escrowRemaining() - actualPayout));
                OrderStatus nextStatus = newDeliveredQuantity >= liveOrder.requestedQuantity()
                        ? OrderStatus.FILLED
                        : OrderStatus.ACTIVE;
                long closedAt = nextStatus == OrderStatus.FILLED ? createdAt : 0L;

                try (PreparedStatement updateOrder = conn.prepareStatement(
                        "UPDATE orders SET status = ?, delivered_quantity = ?, paid_amount = ?, escrow_remaining = ?, closed_at = ? " +
                                "WHERE id = ? AND status = ?")) {
                    updateOrder.setString(1, nextStatus.name());
                    updateOrder.setInt(2, newDeliveredQuantity);
                    updateOrder.setDouble(3, newPaidAmount);
                    updateOrder.setDouble(4, newEscrow);
                    updateOrder.setLong(5, closedAt);
                    updateOrder.setLong(6, liveOrder.id());
                    updateOrder.setString(7, OrderStatus.ACTIVE.name());
                    if (updateOrder.executeUpdate() != 1) {
                        conn.rollback();
                        conn.setAutoCommit(originalAutoCommit);
                        return false;
                    }
                }

                try (PreparedStatement insertDelivery = conn.prepareStatement(
                        "INSERT INTO order_deliveries (order_id, deliverer_uuid, deliverer_name, quantity, payout, created_at) " +
                                "VALUES (?,?,?,?,?,?)")) {
                    insertDelivery.setLong(1, liveOrder.id());
                    insertDelivery.setString(2, deliverer.getUniqueId().toString());
                    insertDelivery.setString(3, deliverer.getName());
                    insertDelivery.setInt(4, actualQuantity);
                    insertDelivery.setDouble(5, actualPayout);
                    insertDelivery.setLong(6, createdAt);
                    insertDelivery.executeUpdate();
                }

                ItemStack storedClaim = deliveredStack.clone();
                storedClaim.setAmount(actualQuantity);
                try (PreparedStatement insertClaim = conn.prepareStatement(
                        "INSERT INTO order_claims (owner_uuid, order_id, claim_type, item_data, money_amount, created_at, claimed_at) " +
                                "VALUES (?,?,?,?,?,?,?)")) {
                    insertClaim.setString(1, liveOrder.ownerUuid().toString());
                    insertClaim.setLong(2, liveOrder.id());
                    insertClaim.setString(3, OrderCollectionClaim.ClaimType.ITEM.name());
                    insertClaim.setString(4, serializeItem(storedClaim));
                    insertClaim.setDouble(5, 0D);
                    insertClaim.setLong(6, createdAt);
                    insertClaim.setLong(7, 0L);
                    insertClaim.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                throw e;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to apply order delivery", e);
            return false;
        }
    }

    private void revertDelivery(
            long orderId,
            UUID delivererUuid,
            ItemStack deliveredStack,
            int deliveredQuantity,
            double payout,
            long createdAt
    ) {
        try {
            Connection conn = connection();
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Order order = getOrder(orderId);
                if (order == null) {
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }

                int restoredDelivered = Math.max(0, order.deliveredQuantity() - deliveredQuantity);
                double restoredPaid = roundCurrency(Math.max(0D, order.paidAmount() - payout));
                double restoredEscrow = roundCurrency(order.escrowRemaining() + payout);
                OrderStatus restoredStatus = restoredDelivered >= order.requestedQuantity()
                        ? OrderStatus.FILLED
                        : OrderStatus.ACTIVE;
                long restoredClosedAt = restoredStatus == OrderStatus.ACTIVE ? 0L : order.closedAt();

                try (PreparedStatement updateOrder = conn.prepareStatement(
                        "UPDATE orders SET status = ?, delivered_quantity = ?, paid_amount = ?, escrow_remaining = ?, closed_at = ? WHERE id = ?")) {
                    updateOrder.setString(1, restoredStatus.name());
                    updateOrder.setInt(2, restoredDelivered);
                    updateOrder.setDouble(3, restoredPaid);
                    updateOrder.setDouble(4, restoredEscrow);
                    updateOrder.setLong(5, restoredClosedAt);
                    updateOrder.setLong(6, orderId);
                    updateOrder.executeUpdate();
                }

                try (PreparedStatement deleteDelivery = conn.prepareStatement(
                        "DELETE FROM order_deliveries WHERE order_id = ? AND deliverer_uuid = ? AND quantity = ? AND payout = ? AND created_at = ?")) {
                    deleteDelivery.setLong(1, orderId);
                    deleteDelivery.setString(2, delivererUuid.toString());
                    deleteDelivery.setInt(3, deliveredQuantity);
                    deleteDelivery.setDouble(4, payout);
                    deleteDelivery.setLong(5, createdAt);
                    deleteDelivery.executeUpdate();
                }

                try (PreparedStatement deleteClaim = conn.prepareStatement(
                        "DELETE FROM order_claims WHERE order_id = ? AND claim_type = ? AND item_data = ? AND created_at = ? AND claimed_at = 0")) {
                    deleteClaim.setLong(1, orderId);
                    deleteClaim.setString(2, OrderCollectionClaim.ClaimType.ITEM.name());
                    deleteClaim.setString(3, serializeItem(deliveredStack));
                    deleteClaim.setLong(4, createdAt);
                    deleteClaim.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                throw e;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to revert order delivery " + orderId, e);
        }
    }

    private boolean closeOrderAndCreateRefundClaim(Order order, OrderStatus nextStatus, long closedAt) {
        try {
            Connection conn = connection();
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement updateOrder = conn.prepareStatement(
                        "UPDATE orders SET status = ?, closed_at = ? WHERE id = ? AND status = ?")) {
                    updateOrder.setString(1, nextStatus.name());
                    updateOrder.setLong(2, closedAt);
                    updateOrder.setLong(3, order.id());
                    updateOrder.setString(4, OrderStatus.ACTIVE.name());
                    if (updateOrder.executeUpdate() != 1) {
                        conn.rollback();
                        conn.setAutoCommit(originalAutoCommit);
                        return false;
                    }
                }

                if (order.escrowRemaining() > EPSILON) {
                    try (PreparedStatement insertClaim = conn.prepareStatement(
                            "INSERT INTO order_claims (owner_uuid, order_id, claim_type, item_data, money_amount, created_at, claimed_at) " +
                                    "VALUES (?,?,?,?,?,?,?)")) {
                        insertClaim.setString(1, order.ownerUuid().toString());
                        insertClaim.setLong(2, order.id());
                        insertClaim.setString(3, OrderCollectionClaim.ClaimType.REFUND.name());
                        insertClaim.setNull(4, Types.VARCHAR);
                        insertClaim.setDouble(5, roundCurrency(order.escrowRemaining()));
                        insertClaim.setLong(6, closedAt);
                        insertClaim.setLong(7, 0L);
                        insertClaim.executeUpdate();
                    }
                }

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                throw e;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to close order " + order.id(), e);
            return false;
        }
    }

    private boolean markClaimClaimed(long claimId, UUID ownerUuid, long claimedAt) {
        try (PreparedStatement ps = connection().prepareStatement(
                "UPDATE order_claims SET claimed_at = ? WHERE id = ? AND owner_uuid = ? AND claimed_at = 0")) {
            ps.setLong(1, claimedAt);
            ps.setLong(2, claimId);
            ps.setString(3, ownerUuid.toString());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to mark order claim as claimed", e);
            return false;
        }
    }

    private void reopenClaim(long claimId) {
        try (PreparedStatement ps = connection().prepareStatement(
                "UPDATE order_claims SET claimed_at = 0 WHERE id = ?")) {
            ps.setLong(1, claimId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reopen order claim " + claimId, e);
        }
    }

    private void incrementCollectedQuantity(long orderId, int amount) {
        if (amount <= 0) {
            return;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "UPDATE orders SET collected_quantity = collected_quantity + ? WHERE id = ?")) {
            ps.setInt(1, amount);
            ps.setLong(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to increment collected quantity for order " + orderId, e);
        }
    }

    private void clearEscrowRemaining(long orderId) {
        try (PreparedStatement ps = connection().prepareStatement(
                "UPDATE orders SET escrow_remaining = 0 WHERE id = ?")) {
            ps.setLong(1, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to clear escrow remaining for order " + orderId, e);
        }
    }

    private boolean expireOrder(Order order) {
        return closeOrderAndCreateRefundClaim(order, OrderStatus.EXPIRED, System.currentTimeMillis());
    }

    private Comparator<Order> resolveComparator(OrderSort sortMode) {
        OrderSort effectiveSort = sortMode == null ? getDefaultSort() : sortMode;
        return switch (effectiveSort) {
            case MOST_DELIVERED -> Comparator.comparingInt(Order::deliveredQuantity).reversed()
                    .thenComparing(Comparator.comparingLong(Order::createdAt).reversed());
            case RECENTLY_LISTED -> Comparator.comparingLong(Order::createdAt).reversed()
                    .thenComparing(Comparator.comparingLong(Order::id).reversed());
            case MOST_MONEY_PER_ITEM -> Comparator.comparingDouble(Order::priceEach).reversed()
                    .thenComparing(Comparator.comparingLong(Order::createdAt).reversed());
            case MOST_PAID -> Comparator.comparingDouble(Order::paidAmount).reversed()
                    .thenComparing(Comparator.comparingLong(Order::createdAt).reversed());
        };
    }

    private PlayerData getPlayerData(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            data = plugin.getPlayerDataManager().loadOrCreate(player);
        }
        return data;
    }

    private int countMatchingItems(Player player, ItemStack requestedItem) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (matchesRequestedItem(stack, requestedItem)) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    private RemovedOrderItems removeMatchingItems(PlayerInventory inventory, ItemStack requestedItem, int quantity) {
        if (quantity <= 0) {
            return new RemovedOrderItems(true, null, 0);
        }

        ItemStack[] original = inventory.getStorageContents();
        ItemStack[] working = new ItemStack[original.length];
        for (int i = 0; i < original.length; i++) {
            working[i] = original[i] == null ? null : original[i].clone();
        }

        int remaining = quantity;
        ItemStack deliveredStack = null;
        for (int slot = 0; slot < working.length && remaining > 0; slot++) {
            ItemStack originalStack = working[slot];
            if (!matchesRequestedItem(originalStack, requestedItem)) {
                continue;
            }

            ItemStack stack = normalizeForOrderMatch(originalStack);
            if (isMissingItem(stack)) {
                continue;
            }

            working[slot] = stack;
            int removed = Math.min(remaining, stack.getAmount());
            if (deliveredStack == null) {
                deliveredStack = stack.clone();
                deliveredStack.setAmount(removed);
            } else {
                deliveredStack.setAmount(deliveredStack.getAmount() + removed);
            }
            stack.setAmount(stack.getAmount() - removed);
            remaining -= removed;
            if (stack.getAmount() <= 0) {
                working[slot] = null;
            }
        }

        if (remaining > 0) {
            return new RemovedOrderItems(false, null, 0);
        }

        inventory.setStorageContents(working);
        return new RemovedOrderItems(true, deliveredStack, quantity);
    }

    private boolean matchesRequestedItem(ItemStack stack, ItemStack requestedItem) {
        if (isMissingItem(stack) || isMissingItem(requestedItem)) {
            return false;
        }

        ItemStack normalizedStack = normalizeForOrderMatch(stack);
        ItemStack normalizedRequested = normalizeForOrderMatch(requestedItem);
        if (isMissingItem(normalizedStack) || isMissingItem(normalizedRequested)) {
            return false;
        }

        normalizedStack.setAmount(1);
        normalizedRequested.setAmount(1);
        try {
            return ItemSerializationUtils.serialize(normalizedStack).equals(ItemSerializationUtils.serialize(normalizedRequested));
        } catch (java.io.IOException exception) {
            plugin.getLogger().warning("Failed to compare order item data: " + summarizeException(exception));
            return false;
        }
    }

    private ItemStack normalizeForOrderMatch(ItemStack item) {
        ItemStack normalized = plugin.getWorthManager().stripWorthDisplay(item);
        return normalized == null ? null : normalized.clone();
    }

    private boolean canFitItem(Player player, ItemStack item) {
        if (isMissingItem(item)) {
            return false;
        }

        int remaining = item.getAmount();
        int maxStack = item.getMaxStackSize();
        ItemStack comparison = item.clone();
        comparison.setAmount(1);

        for (ItemStack current : player.getInventory().getStorageContents()) {
            if (isMissingItem(current)) {
                remaining -= maxStack;
            } else if (current.isSimilar(comparison) && current.getAmount() < current.getMaxStackSize()) {
                remaining -= Math.max(0, current.getMaxStackSize() - current.getAmount());
            }

            if (remaining <= 0) {
                return true;
            }
        }
        return remaining <= 0;
    }

    private String serializeItem(ItemStack item) {
        try {
            return ItemSerializationUtils.serialize(item);
        } catch (java.io.IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to serialize order item", exception);
            return "";
        }
    }

    private DeserializedItem deserializeItem(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return new DeserializedItem(null, true, "missing item data");
        }

        if (serializedItemLooksLegacy(encoded)) {
            return new DeserializedItem(null, true, "legacy serialized material data");
        }

        try {
            ItemStack item = ItemSerializationUtils.deserialize(encoded);
            if (isMissingItem(item)) {
                return new DeserializedItem(null, true, "serialized item is missing or legacy");
            }
            return new DeserializedItem(item, false, "");
        } catch (IllegalArgumentException | java.io.IOException | ClassNotFoundException exception) {
            return new DeserializedItem(null, true, summarizeException(exception));
        }
    }

    private DeserializedItem deserializeOrderItem(String encoded, Material fallbackMaterial) {
        if (fallbackMaterial != null
                && !ItemSerializationUtils.isByteSerialized(encoded)
                && serializedItemMissingMaterialKey(encoded, fallbackMaterial)) {
            return new DeserializedItem(null, true, "legacy serialized data missing " + fallbackMaterial.name());
        }

        return deserializeItem(encoded);
    }

    private boolean serializedItemLooksLegacy(String encoded) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encoded);
            return new String(decoded, StandardCharsets.ISO_8859_1).contains("LEGACY_");
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private boolean serializedItemMissingMaterialKey(String encoded, Material material) {
        if (encoded == null || encoded.isBlank() || material == null) {
            return false;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encoded);
            String rawData = new String(decoded, StandardCharsets.ISO_8859_1);
            return rawData.contains("org.bukkit.inventory.ItemStack") && !rawData.contains(material.name());
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private String summarizeException(Exception exception) {
        String message = exception.getMessage();
        if ((message == null || message.isBlank()) && exception.getCause() != null) {
            message = exception.getCause().getMessage();
        }
        return exception.getClass().getSimpleName()
                + (message == null || message.isBlank() ? "" : ": " + message);
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        long orderId = rs.getLong("id");
        String materialKey = rs.getString("requested_material_key");
        Material material = parseMaterial(materialKey);
        DeserializedItem deserialized = deserializeOrderItem(rs.getString("requested_item_data"), material);
        ItemStack requestedItem = deserialized.item();
        if (requestedItem == null && material != null) {
            requestedItem = new ItemStack(material);
            requestedItem.setAmount(1);
            repairOrderItemData(orderId, requestedItem, deserialized.failureReason());
        } else if (requestedItem != null) {
            requestedItem.setAmount(1);
        }
        if (requestedItem == null) {
            plugin.getLogger().warning("Order #" + orderId + " has unreadable item data and invalid fallback material: " + materialKey);
            return null;
        }

        return new Order(
                orderId,
                UUID.fromString(rs.getString("owner_uuid")),
                rs.getString("owner_name"),
                requestedItem,
                materialKey,
                normalizeCategory(rs.getString("category_key")),
                OrderStatus.fromDatabase(rs.getString("status")),
                rs.getInt("requested_quantity"),
                rs.getInt("delivered_quantity"),
                rs.getInt("collected_quantity"),
                rs.getDouble("price_each"),
                rs.getDouble("total_budget"),
                rs.getDouble("paid_amount"),
                rs.getDouble("escrow_remaining"),
                rs.getLong("created_at"),
                rs.getLong("expires_at"),
                rs.getLong("closed_at")
        );
    }

    private OrderCollectionClaim mapClaim(ResultSet rs) throws SQLException {
        long claimId = rs.getLong("id");
        OrderCollectionClaim.ClaimType claimType = OrderCollectionClaim.ClaimType.fromDatabase(rs.getString("claim_type"));
        DeserializedItem deserialized = claimType == OrderCollectionClaim.ClaimType.ITEM
                ? deserializeItem(rs.getString("item_data"))
                : new DeserializedItem(null, false, "");
        if (claimType == OrderCollectionClaim.ClaimType.ITEM && deserialized.needsRepair()) {
            plugin.getLogger().warning("Order claim #" + claimId + " has unreadable item data: " + deserialized.failureReason());
        }

        return new OrderCollectionClaim(
                claimId,
                UUID.fromString(rs.getString("owner_uuid")),
                rs.getLong("order_id"),
                claimType,
                deserialized.item(),
                rs.getDouble("money_amount"),
                rs.getLong("created_at"),
                rs.getLong("claimed_at")
        );
    }

    private void repairOrderItemData(long orderId, ItemStack fallbackItem, String reason) {
        String serializedFallback = serializeItem(fallbackItem);
        if (serializedFallback.isBlank()) {
            return;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "UPDATE orders SET requested_item_data = ?, requested_material_key = ? WHERE id = ?")) {
            ps.setString(1, serializedFallback);
            ps.setString(2, fallbackItem.getType().name());
            ps.setLong(3, orderId);
            ps.executeUpdate();
            plugin.getLogger().warning("Repaired order #" + orderId + " item data with " + fallbackItem.getType().name()
                    + " fallback" + (reason == null || reason.isBlank() ? "." : " (" + reason + ")."));
        } catch (SQLException exception) {
            plugin.getLogger().warning("Failed to repair order #" + orderId + " item data: " + summarizeException(exception));
        }
    }

    private double roundCurrency(double amount) {
        return Math.round(amount * 100D) / 100D;
    }

    private void ensureTables() {
        try (Statement statement = connection().createStatement()) {
            plugin.getDatabaseManager().executeSchema(statement, """
                    CREATE TABLE IF NOT EXISTS orders (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      owner_uuid TEXT NOT NULL,
                      owner_name TEXT NOT NULL,
                      requested_item_data TEXT NOT NULL,
                      requested_material_key TEXT NOT NULL,
                      category_key TEXT NOT NULL,
                      status TEXT NOT NULL,
                      requested_quantity INTEGER NOT NULL,
                      delivered_quantity INTEGER NOT NULL DEFAULT 0,
                      collected_quantity INTEGER NOT NULL DEFAULT 0,
                      price_each REAL NOT NULL,
                      total_budget REAL NOT NULL,
                      paid_amount REAL NOT NULL DEFAULT 0,
                      escrow_remaining REAL NOT NULL,
                      created_at INTEGER NOT NULL,
                      expires_at INTEGER NOT NULL,
                      closed_at INTEGER DEFAULT 0
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(statement, """
                    CREATE TABLE IF NOT EXISTS order_deliveries (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      order_id INTEGER NOT NULL,
                      deliverer_uuid TEXT NOT NULL,
                      deliverer_name TEXT NOT NULL,
                      quantity INTEGER NOT NULL,
                      payout REAL NOT NULL,
                      created_at INTEGER NOT NULL
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(statement, """
                    CREATE TABLE IF NOT EXISTS order_claims (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      owner_uuid TEXT NOT NULL,
                      order_id INTEGER NOT NULL,
                      claim_type TEXT NOT NULL,
                      item_data TEXT,
                      money_amount REAL DEFAULT 0,
                      created_at INTEGER NOT NULL,
                      claimed_at INTEGER DEFAULT 0
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(statement, "CREATE INDEX IF NOT EXISTS idx_orders_status_expires ON orders(status, expires_at)");
            plugin.getDatabaseManager().executeSchema(statement, "CREATE INDEX IF NOT EXISTS idx_orders_owner_status ON orders(owner_uuid, status)");
            plugin.getDatabaseManager().executeSchema(statement, "CREATE INDEX IF NOT EXISTS idx_order_claims_owner_claimed ON order_claims(owner_uuid, claimed_at)");
            plugin.getDatabaseManager().executeSchema(statement, "CREATE INDEX IF NOT EXISTS idx_order_deliveries_order_created ON order_deliveries(order_id, created_at)");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create orders tables", e);
        }
    }

    private void validateConfiguration() {
        if (getMinPriceEach() > getMaxPriceEach()) {
            plugin.getLogger().warning("orders.yml has PRICING.MIN_PRICE_EACH greater than PRICING.MAX_PRICE_EACH.");
        }

        if (catalogByCategory.isEmpty()) {
            plugin.getLogger().warning("orders.yml has no valid CATEGORY_FILTERS entries; fallback catalog will be used.");
        }

        for (String rawMaterial : config().getStringList("MATCHING.BLOCKED_MATERIALS")) {
            if (rawMaterial == null || rawMaterial.isBlank()) {
                continue;
            }
            if (parseMaterial(rawMaterial) == null) {
                plugin.getLogger().warning("Invalid Orders blocked material: " + rawMaterial);
            }
        }
    }

    private enum SearchCategory {
        BLOCKS,
        ITEMS,
        SWORDS,
        ARMOR,
        FOOD,
        WOOD
    }

    private enum SearchMatchTier {
        EXACT_CATEGORY,
        DIRECT_MATERIAL,
        FUZZY_MATERIAL,
        FUZZY_CATEGORY
    }

    private record SearchCandidate(Material material, int score, SearchMatchTier tier) {}

    private record SearchCategoryMatch(SearchCategory category, int score, boolean exact) {}

    private record DeserializedItem(ItemStack item, boolean needsRepair, String failureReason) {}

    private record RemovedOrderItems(boolean success, ItemStack deliveredStack, int quantity) {}

    private record ServerCatalogCategory(String key, Material icon) {}

    private enum EditField {
        QUANTITY,
        PRICE
    }

    private record PendingSearchInput(long editOrderId, OrderEditNavigation navigation) {
        private static PendingSearchInput newOrder() {
            return new PendingSearchInput(0L, null);
        }

        private static PendingSearchInput editOrder(long orderId, OrderEditNavigation navigation) {
            return new PendingSearchInput(orderId, navigation);
        }
    }

    private record PendingOrderEdit(long orderId, EditField field, OrderEditNavigation navigation) {}

    private enum PendingStep {
        QUANTITY,
        PRICE,
        READY
    }

    private record PendingOrderCreation(
            ItemStack requestedItem,
            String categoryKey,
            int quantity,
            double priceEach,
            PendingStep step
    ) {
        private static PendingOrderCreation start(OrderCatalogEntry entry) {
            return start(entry.createPreviewItem(), entry.categoryKey());
        }

        private static PendingOrderCreation start(ItemStack requestedItem, String categoryKey) {
            ItemStack storedItem = requestedItem.clone();
            storedItem.setAmount(1);
            return new PendingOrderCreation(storedItem, categoryKey, 0, 0D, PendingStep.QUANTITY);
        }

        private PendingOrderCreation withQuantity(int quantity) {
            return new PendingOrderCreation(requestedItem.clone(), categoryKey, quantity, 0D, PendingStep.PRICE);
        }

        private PendingOrderCreation withPriceEach(double priceEach) {
            return new PendingOrderCreation(requestedItem.clone(), categoryKey, quantity, priceEach, PendingStep.READY);
        }

        private boolean readyForConfirmation() {
            return step == PendingStep.READY && quantity > 0 && priceEach > 0D;
        }
    }
}
