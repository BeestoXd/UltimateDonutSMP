package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.DeliveryDraft;
import com.bx.ultimateDonutSmp.models.DeliveryRequest;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersDeliverConfirmMenu extends BaseMenu {

    private final long orderId;
    private final DeliveryDraft draft;
    private final int originPage;
    private final OrderSort sortMode;
    private final String categoryFilter;
    private boolean finalized;

    public OrdersDeliverConfirmMenu(
            UltimateDonutSmp plugin,
            long orderId,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {
        this(plugin, null, orderId, originPage, sortMode, categoryFilter);
    }

    public OrdersDeliverConfirmMenu(
            UltimateDonutSmp plugin,
            DeliveryDraft draft,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {
        this(plugin, draft, draft == null ? 0L : draft.orderId(), originPage, sortMode, categoryFilter);
    }

    private OrdersDeliverConfirmMenu(
            UltimateDonutSmp plugin,
            DeliveryDraft draft,
            long orderId,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {
        super(
                plugin,
                OrdersMenuSupport.text(plugin, "ORDERS.GUI.CONFIRM.TITLE", "&8Orders -> Confirm delivery"),
                27
        );
        this.draft = draft;
        this.orderId = orderId;
        this.originPage = Math.max(1, originPage);
        this.sortMode = sortMode == null ? plugin.getOrdersManager().getDefaultSort() : sortMode;
        this.categoryFilter = plugin.getOrdersManager().normalizeCategory(categoryFilter);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        OrdersManager manager = plugin.getOrdersManager();
        Order order = manager.getOrder(orderId);
        if (order == null) {
            returnHeldItems(player);
            finalized = true;
            set(13, ItemUtils.createItem(Material.BARRIER, "&cOrder unavailable", List.of("&7Close this menu to return.")));
            return;
        }

        int quantity;
        double payout;
        if (draft != null) {
            quantity = draft.quantity();
            payout = draft.payout();
        } else {
            OrdersManager.DeliveryPreview preview = manager.getDeliveryPreview(player, orderId);
            quantity = preview.success() ? preview.deliverQuantity() : 0;
            payout = preview.success() ? preview.payout() : 0D;
        }

        set(11, OrdersMenuSupport.button(
                plugin, "GUI.DELIVERY_CONFIRM.BUTTONS.CANCEL", "ORDERS.GUI.CONFIRM.CANCEL",
                Material.RED_STAINED_GLASS_PANE, "&cCancel", List.of("&fReturn without delivering")
        ));
        set(13, OrdersMenuSupport.decorateItem(
                plugin,
                order.requestedItem(),
                OrdersMenuSupport.text(plugin, "ORDERS.GUI.CONFIRM.SUMMARY.NAME", "&f{owner}'s order",
                        "{owner}", order.ownerName()),
                OrdersMenuSupport.list(
                        plugin,
                        "ORDERS.GUI.CONFIRM.SUMMARY.LORE",
                        List.of(
                                "&7Item: &f{item}",
                                "&7Amount: &f{amount}",
                                "&7Receive: &a{receive}",
                                "&7Remaining after: &f{remaining}"
                        ),
                        "{item}", manager.describeItem(order.requestedItem()),
                        "{amount}", String.valueOf(quantity),
                        "{receive}", plugin.getCurrencyManager().formatMoney(payout),
                        "{remaining}", String.valueOf(Math.max(0, order.remainingQuantity() - quantity))
                ),
                false
        ));
        set(15, OrdersMenuSupport.button(
                plugin, "GUI.DELIVERY_CONFIRM.BUTTONS.CONFIRM", "ORDERS.GUI.CONFIRM.CONFIRM",
                quantity > 0 ? Material.LIME_STAINED_GLASS_PANE : Material.BARRIER,
                quantity > 0 ? "&aConfirm" : "&cCannot deliver",
                quantity > 0
                        ? List.of("&fDeliver {amount} items", "&7You receive {receive}")
                        : List.of("&7No deliverable items are available."),
                "{amount}", String.valueOf(quantity),
                "{receive}", plugin.getCurrencyManager().formatMoney(payout)
        ));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (finalized) {
            return;
        }
        if (slot == 11) {
            finalized = true;
            returnHeldItems(player);
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (draft != null) {
                new OrdersDepositMenu(plugin, orderId, originPage, sortMode, categoryFilter).open(player);
            } else {
                new OrdersEditMenu(plugin, orderId, false, originPage, sortMode, categoryFilter).open(player);
            }
            return;
        }
        if (slot != 15) {
            return;
        }

        OrdersManager manager = plugin.getOrdersManager();
        if (!manager.beginAction(player.getUniqueId())) {
            return;
        }
        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                return;
            }
            manager.updateClickCooldown(player.getUniqueId());
            finalized = true;
            Order order = manager.getOrder(orderId);
            OrdersManager.DeliverOrderResult result;
            if (draft != null && order != null) {
                double expectedPriceEach = draft.quantity() <= 0
                        ? 0D
                        : manager.roundCurrency(draft.payout() / draft.quantity());
                result = manager.deliverOrder(
                        player,
                        new DeliveryRequest(orderId, draft.acceptedItems(), draft.quantity(), expectedPriceEach)
                );
            } else {
                result = manager.deliverOrder(player, orderId);
            }

            if (!result.success()) {
                returnHeldItems(player);
                player.sendMessage(ColorUtils.toComponent(resolveFailure(result)));
                SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.FAIL"));
            } else {
                player.sendMessage(ColorUtils.toComponent(OrdersMenuSupport.text(
                        plugin,
                        "ORDERS.DELIVERY_SUCCESS",
                        "&aDelivered &e{quantity} {item}&a and received &a{payout}&a.",
                        "{quantity}", String.valueOf(result.deliveredQuantity()),
                        "{item}", result.order() == null ? "Items" : manager.describeItem(result.order().requestedItem()),
                        "{payout}", plugin.getCurrencyManager().formatMoney(result.payout())
                )));
                SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.SUCCESS"));
            }
            new OrdersBrowseMenu(plugin, originPage, sortMode, categoryFilter).open(player);
        } finally {
            manager.endAction(player.getUniqueId());
        }
    }

    @Override
    public void onClose(Player player) {
        if (!finalized) {
            finalized = true;
            returnHeldItems(player);
        }
    }

    private void returnHeldItems(Player player) {
        if (draft != null && !draft.acceptedItems().isEmpty()) {
            plugin.getOrdersManager().giveOrDrop(player, draft.acceptedItems());
        }
    }

    private String resolveFailure(OrdersManager.DeliverOrderResult result) {
        if (result == null || result.reason() == null) {
            return OrdersMenuSupport.text(plugin, "ORDERS.DELIVERY_FAILED", "&cDelivery failed.");
        }
        return switch (result.reason()) {
            case OWN_ORDER -> OrdersMenuSupport.text(plugin, "ORDERS.CANNOT_DELIVER_OWN", "&cYou cannot deliver to your own order.");
            case NO_MATCHING_ITEMS -> OrdersMenuSupport.text(plugin, "ORDERS.NO_MATCHING_ITEMS", "&cThe deposited items no longer match.");
            case ORDER_FULL -> OrdersMenuSupport.text(plugin, "ORDERS.ORDER_FULL", "&cThat order is already full.");
            case PAYOUT_ERROR -> OrdersMenuSupport.text(plugin, "ORDERS.DELIVERY_FAILED_ECONOMY", "&cThe payout transaction failed.");
            default -> OrdersMenuSupport.text(plugin, "ORDERS.ORDER_NOT_ACTIVE", "&cThat order is no longer active.");
        };
    }
}
