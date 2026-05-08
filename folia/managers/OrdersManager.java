package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.OrdersBrowseMenu;
import com.bx.ultimateDonutSmp.menus.OrdersNewMenu;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderCatalogEntry;
import com.bx.ultimateDonutSmp.models.OrderCollectionClaim;
import com.bx.ultimateDonutSmp.models.OrderDelivery;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.models.OrderStatus;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

    public record PendingOrderCreationSnapshot(
            OrderCatalogEntry entry,
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

    private final UltimateDonutSmp plugin;
    private final Set<UUID> activeTransactions = new HashSet<>();
    private final Map<UUID, Long> lastClickTimes = new HashMap<>();
    private final Map<UUID, PendingOrderCreation> pendingCreations = new HashMap<>();
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
        return config().getBoolean("SETTINGS.ENABLED", true);
    }

    public String getBrowseTitle() {
        return config().getString("GUI.MAIN.TITLE", "&8Orders");
    }

    public int getBrowseSize() {
        return normalizeSize(config().getInt("GUI.MAIN.SIZE", 54));
    }

    public int getBrowseItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.MAIN.ITEMS_PER_PAGE", 45)));
    }

    public String getMyOrdersTitle() {
        return config().getString("GUI.MY_ORDERS.TITLE", "&8Orders -> My Orders");
    }

    public int getMyOrdersSize() {
        return normalizeSize(config().getInt("GUI.MY_ORDERS.SIZE", 54));
    }

    public int getMyOrdersItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.MY_ORDERS.ITEMS_PER_PAGE", 45)));
    }

    public String getCollectTitle() {
        return config().getString("GUI.COLLECT.TITLE", "&8Orders -> Collect");
    }

    public int getCollectSize() {
        return normalizeSize(config().getInt("GUI.COLLECT.SIZE", 54));
    }

    public int getCollectItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.COLLECT.ITEMS_PER_PAGE", 45)));
    }

    public String getSelectItemTitle() {
        return config().getString("GUI.SELECT_ITEM.TITLE", "&8Orders -> Select Item");
    }

    public int getSelectItemSize() {
        return normalizeSize(config().getInt("GUI.SELECT_ITEM.SIZE", 54));
    }

    public int getSelectItemItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.SELECT_ITEM.ITEMS_PER_PAGE", 45)));
    }

    public String getNewOrderTitle() {
        return config().getString("GUI.NEW_ORDER.TITLE", "&8Orders -> New Order");
    }

    public int getNewOrderSize() {
        return normalizeSize(config().getInt("GUI.NEW_ORDER.SIZE", 27));
    }

    public String getEditOrderTitle(long orderId) {
        return config().getString("GUI.EDIT_ORDER.TITLE", "&8Orders -> Edit Order")
                .replace("{order_id}", String.valueOf(orderId));
    }

    public int getEditOrderSize() {
        return normalizeSize(config().getInt("GUI.EDIT_ORDER.SIZE", 27));
    }

    public String getDeliverTitle(long orderId) {
        return config().getString("GUI.DELIVER_CONFIRM.TITLE", "&8Orders -> Deliver")
                .replace("{order_id}", String.valueOf(orderId));
    }

    public int getDeliverSize() {
        return normalizeSize(config().getInt("GUI.DELIVER_CONFIRM.SIZE", 27));
    }

    public OrderSort getDefaultSort() {
        return OrderSort.fromConfig(config().getString("SORTING.DEFAULT", "MOST_PAID"));
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

        pendingCreations.put(player.getUniqueId(), PendingOrderCreation.start(entry));
        player.closeInventory();
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_QUANTITY",
                "&7Enter the order quantity for &f{item}&7 in chat. Type &ccancel&7 to abort.",
                "{item}", describeMaterial(entry.material())
        )));
    }

    public boolean hasPendingInput(UUID uuid) {
        PendingOrderCreation pending = pendingCreations.get(uuid);
        return pending != null && !pending.readyForConfirmation();
    }

    public PendingOrderCreationSnapshot getPendingCreation(UUID uuid) {
        PendingOrderCreation pending = pendingCreations.get(uuid);
        if (pending == null || !pending.readyForConfirmation()) {
            return null;
        }

        return new PendingOrderCreationSnapshot(
                pending.entry(),
                pending.quantity(),
                pending.priceEach(),
                roundCurrency(pending.quantity() * pending.priceEach())
        );
    }

    public void clearPendingCreation(UUID uuid) {
        if (uuid == null) {
            return;
        }
        pendingCreations.remove(uuid);
    }

    public void handlePendingInput(Player player, String rawInput) {
        if (player == null) {
            return;
        }

        PendingOrderCreation pending = pendingCreations.get(player.getUniqueId());
        if (pending == null) {
            return;
        }

        String input = rawInput == null ? "" : rawInput.trim();
        if (input.equalsIgnoreCase("cancel")) {
            pendingCreations.remove(player.getUniqueId());
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.INPUT_CANCELLED",
                    "&7Order creation cancelled."
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

        if (!isOrderable(pending.entry().material())) {
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
                new ItemStack(pending.entry().material()),
                pending.entry().categoryKey(),
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

    public String describeMaterial(Material material) {
        return plugin.getWorthManager().prettifyMaterial(material);
    }

    public String describeItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "Unknown Item";
        }

        return describeMaterial(item.getType());
    }

    public String formatRemaining(long seconds) {
        if (seconds <= 0) {
            return "Expired";
        }
        return NumberUtils.formatTimeLong(seconds);
    }

    private void handleQuantityInput(Player player, PendingOrderCreation pending, String input) {
        int quantity;
        try {
            quantity = Math.toIntExact(NumberUtils.parseLong(input));
        } catch (RuntimeException exception) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.INVALID_QUANTITY",
                    "&cInvalid quantity. Use a whole number greater than 0."
            )));
            resendQuantityPrompt(player, pending.entry());
            return;
        }

        if (quantity <= 0 || quantity > getMaxQuantityPerOrder()) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.QUANTITY_OUT_OF_RANGE",
                    "&cQuantity must be between 1 and {max}.",
                    "{max}", String.valueOf(getMaxQuantityPerOrder())
            )));
            resendQuantityPrompt(player, pending.entry());
            return;
        }

        pendingCreations.put(player.getUniqueId(), pending.withQuantity(quantity));
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_PRICE",
                "&7Enter the price each for &f{item}&7 in chat. Type &ccancel&7 to abort.",
                "{item}", describeMaterial(pending.entry().material())
        )));
    }

    private void handlePriceInput(Player player, PendingOrderCreation pending, String input) {
        double priceEach;
        try {
            priceEach = NumberUtils.parse(input);
        } catch (NumberFormatException exception) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.INVALID_PRICE",
                    "&cInvalid price format. Use numbers like 100, 5K, or 1.5M."
            )));
            resendPricePrompt(player, pending.entry());
            return;
        }

        double normalizedPrice = roundCurrency(priceEach);
        double totalBudget = roundCurrency(normalizedPrice * pending.quantity());
        if (normalizedPrice < getMinPriceEach() || normalizedPrice > getMaxPriceEach()) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.PRICE_OUT_OF_RANGE",
                    "&cPrice each must be between &f${min}&c and &f${max}&c.",
                    "{min}", NumberUtils.format(getMinPriceEach()),
                    "{max}", NumberUtils.format(getMaxPriceEach())
            )));
            resendPricePrompt(player, pending.entry());
            return;
        }

        if (totalBudget <= 0D || totalBudget > getMaxTotalBudget()) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.TOTAL_TOO_HIGH",
                    "&cTotal order budget cannot exceed &f${max}&c.",
                    "{max}", NumberUtils.format(getMaxTotalBudget())
            )));
            resendPricePrompt(player, pending.entry());
            return;
        }

        pendingCreations.put(player.getUniqueId(), pending.withPriceEach(normalizedPrice));
        new OrdersNewMenu(plugin).open(player);
    }

    private void resendQuantityPrompt(Player player, OrderCatalogEntry entry) {
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_QUANTITY",
                "&7Enter the order quantity for &f{item}&7 in chat. Type &ccancel&7 to abort.",
                "{item}", describeMaterial(entry.material())
        )));
    }

    private void resendPricePrompt(Player player, OrderCatalogEntry entry) {
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "ORDERS.PROMPT_PRICE",
                "&7Enter the price each for &f{item}&7 in chat. Type &ccancel&7 to abort.",
                "{item}", describeMaterial(entry.material())
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
        ItemStack deliveredStack = requestedItem.clone();
        deliveredStack.setAmount(preview.deliverQuantity());

        if (!removeMatchingItems(player.getInventory(), requestedItem, preview.deliverQuantity())) {
            return new DeliverOrderResult(false, DeliveryFailureReason.NO_MATCHING_ITEMS, preview.order(), 0, 0D);
        }

        long now = System.currentTimeMillis();
        boolean updated = applyDelivery(preview.order(), player, deliveredStack, preview.deliverQuantity(), preview.payout(), now);
        if (!updated) {
            player.getInventory().addItem(deliveredStack);
            player.updateInventory();
            return new DeliverOrderResult(false, DeliveryFailureReason.DATABASE_ERROR, preview.order(), 0, 0D);
        }

        var payoutResult = plugin.getEconomyManager().deposit(player, preview.payout(), EconomyReason.ORDER_DELIVERY_PAYOUT);
        if (!payoutResult.success()) {
            revertDelivery(preview.order().id(), player.getUniqueId(), deliveredStack, preview.deliverQuantity(), preview.payout(), now);
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
            if (!player.hasPermission(permission)) {
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
        return Math.max(getMinPriceEach(), config().getDouble("PRICING.MAX_PRICE_EACH", 1_000_000D));
    }

    private double getMaxTotalBudget() {
        return Math.max(getMinPriceEach(), config().getDouble("PRICING.MAX_TOTAL_BUDGET", 250_000_000D));
    }

    private double getCreationFee() {
        return Math.max(0D, config().getDouble("PRICING.ORDER_CREATION_FEE", 0D));
    }

    private boolean isOrderable(Material material) {
        if (material == null || material.isAir() || !material.isItem()) {
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

    private Material parseMaterial(String rawMaterial) {
        if (rawMaterial == null || rawMaterial.isBlank()) {
            return null;
        }

        try {
            return Material.valueOf(rawMaterial.trim().toUpperCase(Locale.US));
        } catch (IllegalArgumentException exception) {
            return null;
        }
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

    private boolean removeMatchingItems(PlayerInventory inventory, ItemStack requestedItem, int quantity) {
        if (quantity <= 0) {
            return true;
        }

        ItemStack[] original = inventory.getStorageContents();
        ItemStack[] working = new ItemStack[original.length];
        for (int i = 0; i < original.length; i++) {
            working[i] = original[i] == null ? null : original[i].clone();
        }

        int remaining = quantity;
        for (int slot = 0; slot < working.length && remaining > 0; slot++) {
            ItemStack stack = working[slot];
            if (!matchesRequestedItem(stack, requestedItem)) {
                continue;
            }

            int removed = Math.min(remaining, stack.getAmount());
            stack.setAmount(stack.getAmount() - removed);
            remaining -= removed;
            if (stack.getAmount() <= 0) {
                working[slot] = null;
            }
        }

        if (remaining > 0) {
            return false;
        }

        inventory.setStorageContents(working);
        return true;
    }

    private boolean matchesRequestedItem(ItemStack stack, ItemStack requestedItem) {
        if (stack == null || requestedItem == null || stack.getType().isAir()) {
            return false;
        }
        return stack.isSimilar(requestedItem);
    }

    private boolean canFitItem(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return true;
        }

        int remaining = item.getAmount();
        int maxStack = item.getMaxStackSize();
        ItemStack comparison = item.clone();
        comparison.setAmount(1);

        for (ItemStack current : player.getInventory().getStorageContents()) {
            if (current == null || current.getType().isAir()) {
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
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    private ItemStack deserializeItem(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }

        try {
            return ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded));
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to deserialize order item", exception);
            return null;
        }
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        String materialKey = rs.getString("requested_material_key");
        Material material = parseMaterial(materialKey);
        ItemStack requestedItem = deserializeItem(rs.getString("requested_item_data"));
        if (requestedItem == null && material != null) {
            requestedItem = new ItemStack(material);
        }
        if (requestedItem == null) {
            return null;
        }

        return new Order(
                rs.getLong("id"),
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
        return new OrderCollectionClaim(
                rs.getLong("id"),
                UUID.fromString(rs.getString("owner_uuid")),
                rs.getLong("order_id"),
                OrderCollectionClaim.ClaimType.fromDatabase(rs.getString("claim_type")),
                deserializeItem(rs.getString("item_data")),
                rs.getDouble("money_amount"),
                rs.getLong("created_at"),
                rs.getLong("claimed_at")
        );
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

    private enum PendingStep {
        QUANTITY,
        PRICE,
        READY
    }

    private record PendingOrderCreation(
            OrderCatalogEntry entry,
            int quantity,
            double priceEach,
            PendingStep step
    ) {
        private static PendingOrderCreation start(OrderCatalogEntry entry) {
            return new PendingOrderCreation(entry, 0, 0D, PendingStep.QUANTITY);
        }

        private PendingOrderCreation withQuantity(int quantity) {
            return new PendingOrderCreation(entry, quantity, 0D, PendingStep.PRICE);
        }

        private PendingOrderCreation withPriceEach(double priceEach) {
            return new PendingOrderCreation(entry, quantity, priceEach, PendingStep.READY);
        }

        private boolean readyForConfirmation() {
            return step == PendingStep.READY && quantity > 0 && priceEach > 0D;
        }
    }
}
