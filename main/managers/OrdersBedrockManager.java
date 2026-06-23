package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderCatalogEntry;
import com.bx.ultimateDonutSmp.models.OrderCollectionClaim;
import com.bx.ultimateDonutSmp.models.OrderUiState;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.List;
import java.util.Locale;

public final class OrdersBedrockManager {

    private final UltimateDonutSmp plugin;

    public OrdersBedrockManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isBedrockPlayer(Player player) {
        if (player == null || !enabled()) {
            return false;
        }
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (Throwable throwable) {
            return false;
        }
    }

    public boolean openMain(Player player) {
        return openMain(player, "");
    }

    public boolean openMain(Player player, String query) {
        if (!isBedrockPlayer(player)) {
            return false;
        }
        OrdersManager manager = plugin.getOrdersManager();
        OrderUiState state = manager.getUiState(player.getUniqueId());
        String normalizedQuery = query == null ? "" : query.trim();
        List<Order> orders = manager.getActiveOrders(state.sort(), state.filter()).stream()
                .filter(order -> matches(order, normalizedQuery))
                .limit(maxResults())
                .toList();

        SimpleForm.Builder form = SimpleForm.builder()
                .title(text("ORDERS.BEDROCK.MAIN.TITLE", "ᴏʀᴅᴇʀѕ"))
                .content(normalizedQuery.isBlank()
                        ? text("ORDERS.BEDROCK.MAIN.CONTENT", "Browse open orders.")
                        : text("ORDERS.BEDROCK.MAIN.SEARCHING", "Search: {query}", "{query}", normalizedQuery))
                .button(text("ORDERS.BEDROCK.BUTTON.SEARCH", "Search"))
                .button(text("ORDERS.BEDROCK.BUTTON.NEW", "New Order"))
                .button(text("ORDERS.BEDROCK.BUTTON.MY", "My Orders"))
                .button(text("ORDERS.BEDROCK.BUTTON.COLLECT", "Collect"));
        for (Order order : orders) {
            form.button(orderButton(order));
        }
        form.validResultHandler(response -> schedule(player, () -> {
            int button = response.clickedButtonId();
            if (button == 0) {
                openSearch(player, normalizedQuery);
            } else if (button == 1) {
                openItemSelection(player, "");
            } else if (button == 2) {
                openMyOrders(player);
            } else if (button == 3) {
                openCollect(player);
            } else {
                int orderIndex = button - 4;
                if (orderIndex >= 0 && orderIndex < orders.size()) {
                    openOrder(player, orders.get(orderIndex));
                }
            }
        }));
        return send(player, form.build());
    }

    public boolean openMyOrders(Player player) {
        if (!isBedrockPlayer(player)) {
            return false;
        }
        OrderUiState state = plugin.getOrdersManager().getUiState(player.getUniqueId());
        List<Order> orders = plugin.getOrdersManager()
                .getOrdersForOwner(player.getUniqueId(), state.sort()).stream()
                .limit(maxResults())
                .toList();
        SimpleForm.Builder form = SimpleForm.builder()
                .title(text("ORDERS.BEDROCK.MY.TITLE", "ᴍʏ ᴏʀᴅᴇʀѕ"))
                .content(text("ORDERS.BEDROCK.MY.CONTENT", "Select an order to manage it."))
                .button(text("ORDERS.BEDROCK.BUTTON.BACK", "Back"))
                .button(text("ORDERS.BEDROCK.BUTTON.NEW", "New Order"));
        for (Order order : orders) {
            form.button(orderButton(order));
        }
        form.validResultHandler(response -> schedule(player, () -> {
            int button = response.clickedButtonId();
            if (button == 0) {
                openMain(player);
            } else if (button == 1) {
                openItemSelection(player, "");
            } else {
                int index = button - 2;
                if (index >= 0 && index < orders.size()) {
                    openOwnerActions(player, orders.get(index));
                }
            }
        }));
        return send(player, form.build());
    }

    public boolean openCollect(Player player) {
        if (!isBedrockPlayer(player)) {
            return false;
        }
        List<OrderCollectionClaim> claims = plugin.getOrdersManager()
                .getUnclaimedClaims(player.getUniqueId()).stream()
                .limit(maxResults())
                .toList();
        SimpleForm.Builder form = SimpleForm.builder()
                .title(text("ORDERS.BEDROCK.COLLECT.TITLE", "ᴄᴏʟʟᴇᴄᴛ"))
                .content(text("ORDERS.BEDROCK.COLLECT.CONTENT", "Pending claims: {count}",
                        "{count}", String.valueOf(claims.size())))
                .button(text("ORDERS.BEDROCK.BUTTON.BACK", "Back"))
                .button(text("ORDERS.BEDROCK.BUTTON.COLLECT_ALL", "Collect All"))
                .button(text("ORDERS.BEDROCK.BUTTON.DROP_ITEMS", "Drop Item Claims"));
        for (OrderCollectionClaim claim : claims) {
            form.button(claim.refundClaim()
                    ? text("ORDERS.BEDROCK.CLAIM.REFUND", "Refund #{order}: {amount}",
                    "{order}", String.valueOf(claim.orderId()),
                    "{amount}", plugin.getCurrencyManager().formatMoney(claim.moneyAmount()))
                    : text("ORDERS.BEDROCK.CLAIM.ITEM", "Order #{order}: {item}",
                    "{order}", String.valueOf(claim.orderId()),
                    "{item}", plugin.getOrdersManager().describeItem(claim.item())));
        }
        form.validResultHandler(response -> schedule(player, () -> {
            int button = response.clickedButtonId();
            if (button == 0) {
                openMain(player);
            } else if (button == 1) {
                plugin.getOrdersManager().claimBatch(player, 0L, false);
                openCollect(player);
            } else if (button == 2) {
                plugin.getOrdersManager().claimBatch(player, 0L, true);
                openCollect(player);
            } else {
                int index = button - 3;
                if (index >= 0 && index < claims.size()) {
                    plugin.getOrdersManager().claim(player, claims.get(index).id());
                    openCollect(player);
                }
            }
        }));
        return send(player, form.build());
    }

    private void openSearch(Player player, String currentQuery) {
        CustomForm.Builder form = CustomForm.builder()
                .title(text("ORDERS.BEDROCK.SEARCH.TITLE", "ѕᴇᴀʀᴄʜ ᴏʀᴅᴇʀѕ"))
                .input(text("ORDERS.BEDROCK.SEARCH.INPUT", "Item, owner, or category"), "", currentQuery);
        form.validResultHandler(response ->
                schedule(player, () -> openMain(player, response.asInput(0))));
        form.closedResultHandler(() -> schedule(player, () -> openMain(player, currentQuery)));
        send(player, form.build());
    }

    private void openItemSelection(Player player, String query) {
        List<OrderCatalogEntry> entries = plugin.getOrdersManager()
                .searchCatalogEntries(query).stream()
                .limit(maxResults())
                .toList();
        SimpleForm.Builder form = SimpleForm.builder()
                .title(text("ORDERS.BEDROCK.ITEMS.TITLE", "select item"))
                .content(query.isBlank()
                        ? text("ORDERS.BEDROCK.ITEMS.CONTENT", "Choose the item to order.")
                        : text("ORDERS.BEDROCK.MAIN.SEARCHING", "Search: {query}", "{query}", query))
                .button(text("ORDERS.BEDROCK.BUTTON.BACK", "Back"))
                .button(text("ORDERS.BEDROCK.BUTTON.SEARCH", "Search Items"));
        for (OrderCatalogEntry entry : entries) {
            form.button(entry.displayName() + "\n" + plugin.getOrdersManager().prettifyCategory(entry.categoryKey()));
        }
        form.validResultHandler(response -> schedule(player, () -> {
            int button = response.clickedButtonId();
            if (button == 0) {
                openMain(player);
            } else if (button == 1) {
                openItemSearch(player, query);
            } else {
                int index = button - 2;
                if (index >= 0 && index < entries.size()) {
                    openNewOrder(player, entries.get(index));
                }
            }
        }));
        send(player, form.build());
    }

    private void openItemSearch(Player player, String currentQuery) {
        CustomForm.Builder form = CustomForm.builder()
                .title(text("ORDERS.BEDROCK.ITEM_SEARCH.TITLE", "ѕᴇᴀʀᴄʜ ɪᴛᴇᴍѕ"))
                .input(text("ORDERS.BEDROCK.SEARCH.INPUT", "Item or category"), "", currentQuery);
        form.validResultHandler(response ->
                schedule(player, () -> openItemSelection(player, response.asInput(0))));
        form.closedResultHandler(() -> schedule(player, () -> openItemSelection(player, currentQuery)));
        send(player, form.build());
    }

    private void openNewOrder(Player player, OrderCatalogEntry entry) {
        CustomForm.Builder form = CustomForm.builder()
                .title(text("ORDERS.BEDROCK.NEW.TITLE", "ɴᴇᴡ ᴏʀᴅᴇʀ"))
                .input(text("ORDERS.BEDROCK.NEW.AMOUNT", "Amount"), "64", "")
                .input(text("ORDERS.BEDROCK.NEW.PRICE", "Price each"), "100", "");
        form.validResultHandler(response -> schedule(player, () -> {
            try {
                int amount = Integer.parseInt(response.asInput(0).trim());
                double price = Double.parseDouble(response.asInput(1).trim());
                OrdersManager.NewOrderSession session = plugin.getOrdersManager()
                        .getOrCreateNewOrderSession(player.getUniqueId());
                session.setChosenItem(entry.createPreviewItem());
                session.setCategoryKey(entry.categoryKey());
                session.setAmount(amount);
                session.setPriceEach(price);
                OrdersManager.CreateOrderResult result = plugin.getOrdersManager().createOrder(player);
                sendResult(player, result.success(),
                        result.success() ? "ORDERS.BEDROCK.NEW.SUCCESS" : "ORDERS.BEDROCK.NEW.FAILED",
                        result.success() ? "order created." : "could not create order: {reason}",
                        "{reason}", result.reason() == null ? "unknown" : result.reason().name());
                openMyOrders(player);
            } catch (NumberFormatException exception) {
                sendResult(player, false, "ORDERS.BEDROCK.INVALID_NUMBER",
                        "amount and price must be valid numbers.");
                openNewOrder(player, entry);
            }
        }));
        form.closedResultHandler(() -> schedule(player, () -> openItemSelection(player, "")));
        send(player, form.build());
    }

    private void openOrder(Player player, Order order) {
        if (order.ownerUuid().equals(player.getUniqueId())) {
            openOwnerActions(player, order);
        } else {
            openDelivery(player, order);
        }
    }

    private void openDelivery(Player player, Order order) {
        CustomForm.Builder form = CustomForm.builder()
                .title(text("ORDERS.BEDROCK.DELIVERY.TITLE", "ᴅᴇʟɪᴠᴇʀ ɪᴛᴇᴍѕ"))
                .input(text("ORDERS.BEDROCK.DELIVERY.AMOUNT", "Amount (max {max})",
                        "{max}", String.valueOf(order.remainingQuantity())), "64", "");
        form.validResultHandler(response -> schedule(player, () -> {
            try {
                int amount = Integer.parseInt(response.asInput(0).trim());
                OrdersManager.DeliverOrderResult result = plugin.getOrdersManager()
                        .deliverFromInventory(player, order.id(), amount);
                sendResult(player, result.success(),
                        result.success() ? "ORDERS.BEDROCK.DELIVERY.SUCCESS" : "ORDERS.BEDROCK.DELIVERY.FAILED",
                        result.success()
                                ? "delivered {amount} items for {payout}."
                                : "delivery failed: {reason}",
                        "{amount}", String.valueOf(result.deliveredQuantity()),
                        "{payout}", plugin.getCurrencyManager().formatMoney(result.payout()),
                        "{reason}", result.reason() == null ? "unknown" : result.reason().name());
                openMain(player);
            } catch (NumberFormatException exception) {
                sendResult(player, false, "ORDERS.BEDROCK.INVALID_NUMBER", "enter a valid amount.");
                openDelivery(player, order);
            }
        }));
        form.closedResultHandler(() -> schedule(player, () -> openMain(player)));
        send(player, form.build());
    }

    private void openOwnerActions(Player player, Order order) {
        if (order == null) {
            openMyOrders(player);
            return;
        }
        SimpleForm.Builder form = SimpleForm.builder()
                .title(text("ORDERS.BEDROCK.ORDER.TITLE", "ᴏʀᴅᴇʀ #{id}",
                        "{id}", String.valueOf(order.id())))
                .content(orderButton(order))
                .button(text("ORDERS.BEDROCK.BUTTON.BACK", "Back"))
                .button(text("ORDERS.BEDROCK.BUTTON.COLLECT_ORDER", "Collect This Order"))
                .button(text("ORDERS.BEDROCK.BUTTON.CANCEL_ORDER", "Cancel Order"));
        form.validResultHandler(response -> schedule(player, () -> {
            if (response.clickedButtonId() == 0) {
                openMyOrders(player);
            } else if (response.clickedButtonId() == 1) {
                plugin.getOrdersManager().claimBatch(player, order.id(), false);
                openOwnerActions(player, plugin.getOrdersManager().getOrder(order.id()));
            } else if (response.clickedButtonId() == 2) {
                openCancelConfirmation(player, order);
            }
        }));
        send(player, form.build());
    }

    private void openCancelConfirmation(Player player, Order order) {
        ModalForm.Builder form = ModalForm.builder()
                .title(text("ORDERS.BEDROCK.CANCEL.TITLE", "ᴄᴀɴᴄᴇʟ ᴏʀᴅᴇʀ"))
                .content(text("ORDERS.BEDROCK.CANCEL.CONTENT",
                        "Cancel order #{id}? Remaining escrow will become a refund claim.",
                        "{id}", String.valueOf(order.id())))
                .button1(text("ORDERS.BEDROCK.BUTTON.CONFIRM", "Confirm"))
                .button2(text("ORDERS.BEDROCK.BUTTON.BACK", "Back"));
        form.validResultHandler(response -> schedule(player, () -> {
            if (response.clickedFirst()) {
                OrdersManager.CancelOrderResult result = plugin.getOrdersManager().cancelOrder(player, order.id());
                sendResult(player, result.success(),
                        result.success() ? "ORDERS.BEDROCK.CANCEL.SUCCESS" : "ORDERS.BEDROCK.CANCEL.FAILED",
                        result.success() ? "order cancelled." : "order could not be cancelled.");
                openMyOrders(player);
            } else {
                openOwnerActions(player, order);
            }
        }));
        send(player, form.build());
    }

    private boolean matches(Order order, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String search = query.toLowerCase(Locale.ROOT);
        return order.ownerName().toLowerCase(Locale.ROOT).contains(search)
                || order.categoryKey().toLowerCase(Locale.ROOT).contains(search)
                || plugin.getOrdersManager().describeItem(order.requestedItem())
                .toLowerCase(Locale.ROOT).contains(search);
    }

    private String orderButton(Order order) {
        return text("ORDERS.BEDROCK.ORDER.BUTTON",
                "#{id} {item} x{remaining}\n{price} each - {owner}",
                "{id}", String.valueOf(order.id()),
                "{item}", plugin.getOrdersManager().describeItem(order.requestedItem()),
                "{remaining}", String.valueOf(order.remainingQuantity()),
                "{price}", plugin.getCurrencyManager().formatMoney(order.priceEach()),
                "{owner}", order.ownerName());
    }

    private boolean send(Player player, Form form) {
        try {
            return FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
        } catch (Throwable throwable) {
            plugin.getLogger().warning("Could not send Orders Bedrock form to " + player.getName()
                    + ": " + throwable.getMessage());
            return false;
        }
    }

    private void schedule(Player player, Runnable runnable) {
        plugin.getSpigotScheduler().runEntity(player, runnable);
    }

    private void sendResult(Player player, boolean success, String path, String fallback, String... placeholders) {
        player.sendMessage(ColorUtils.toComponent((success ? "&a" : "&c") + text(path, fallback, placeholders)));
    }

    private String text(String path, String fallback, String... placeholders) {
        return plain(plugin.getLanguageManager().text(path, null, fallback, placeholders));
    }

    private String plain(String value) {
        return value == null ? "" : value.replaceAll("(?i)[&§][0-9A-FK-ORX]", "");
    }

    private int maxResults() {
        return Math.max(5, Math.min(50,
                plugin.getConfigManager().getOrders().getInt("BEDROCK.MAX_RESULTS", 20)));
    }

    private boolean enabled() {
        return plugin.getConfigManager().getOrders().getBoolean("BEDROCK.ENABLED", true)
                && plugin.getServer().getPluginManager().isPluginEnabled("floodgate");
    }
}
