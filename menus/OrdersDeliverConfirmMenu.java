package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersDeliverConfirmMenu extends BaseMenu {

    private final long orderId;
    private final int originPage;
    private final OrderSort sortMode;
    private final String categoryFilter;

    public OrdersDeliverConfirmMenu(
            UltimateDonutSmp plugin,
            long orderId,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {
        super(plugin, plugin.getOrdersManager().getDeliverTitle(orderId), plugin.getOrdersManager().getDeliverSize());
        this.orderId = orderId;
        this.originPage = Math.max(1, originPage);
        this.sortMode = sortMode == null ? plugin.getOrdersManager().getDefaultSort() : sortMode;
        this.categoryFilter = categoryFilter == null ? "ALL" : categoryFilter;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        OrdersManager manager = plugin.getOrdersManager();
        OrdersManager.DeliveryPreview preview = manager.getDeliveryPreview(player, orderId);
        set(18, ItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "&cBack", List.of("&7Return to order details")));

        if (preview.order() == null) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cOrder Not Found",
                    List.of("&7This order no longer exists.")
            ));
            return;
        }

        Order order = preview.order();
        set(13, OrdersMenuSupport.createOrderDisplay(plugin, manager, order, false));

        if (!preview.success()) {
            set(11, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cCannot Deliver",
                    List.of(resolveFailureMessage(preview))
            ));
            return;
        }

        set(11, ItemUtils.createItem(
                Material.PAPER,
                "&bDelivery Preview",
                List.of(
                        "&7Deliver Quantity: &e" + preview.deliverQuantity(),
                        "&7Payout: &a$" + NumberUtils.format(preview.payout()),
                        "&7Remaining After This: &e" + Math.max(0, order.remainingQuantity() - preview.deliverQuantity())
                )
        ));
        set(15, ItemUtils.createItem(
                Material.CHEST,
                "&eMatching Items Found",
                List.of(
                        "&7Orders will remove matching items from your inventory.",
                        "&7Requested Item: &f" + manager.describeItem(order.requestedItem())
                )
        ));
        set(23, ItemUtils.createItem(
                Material.LIME_DYE,
                "&aConfirm Delivery",
                List.of(
                        "&7You will be paid instantly on success.",
                        "",
                        "&eClick to deliver"
                )
        ));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == 18) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersEditMenu(plugin, orderId, false, originPage, sortMode, categoryFilter).open(player);
            return;
        }

        if (slot != 23) {
            return;
        }

        OrdersManager manager = plugin.getOrdersManager();
        if (!manager.beginAction(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cOrders is still processing your previous action."));
            return;
        }

        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent("&cSlow down for a moment."));
                return;
            }
            manager.updateClickCooldown(player.getUniqueId());

            OrdersManager.DeliverOrderResult result = manager.deliverOrder(player, orderId);
            if (!result.success()) {
                player.sendMessage(ColorUtils.toComponent(resolveDeliverFailure(result)));
                SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.FAIL"));
                new OrdersEditMenu(plugin, orderId, false, originPage, sortMode, categoryFilter).open(player);
                return;
            }

            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.DELIVERY_SUCCESS",
                    "&aDelivered &e{quantity} {item}&a and received &a${payout}&a.",
                    "{quantity}", String.valueOf(result.deliveredQuantity()),
                    "{item}", manager.describeItem(result.order().requestedItem()),
                    "{payout}", NumberUtils.format(result.payout())
            )));
            SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.SUCCESS"));
            new OrdersBrowseMenu(plugin, originPage, sortMode, categoryFilter).open(player);
        } finally {
            manager.endAction(player.getUniqueId());
        }
    }

    private String resolveFailureMessage(OrdersManager.DeliveryPreview preview) {
        if (preview.reason() == null) {
            return "&7Ready.";
        }
        return switch (preview.reason()) {
            case DISABLED -> "&7Orders is disabled.";
            case NO_PLAYER_DATA -> "&7Your player data is unavailable.";
            case ORDER_NOT_FOUND -> "&7This order no longer exists.";
            case NOT_ACTIVE -> "&7This order is no longer active.";
            case OWN_ORDER -> "&7You cannot deliver to your own order.";
            case NO_MATCHING_ITEMS -> "&7You do not have matching items to deliver.";
            case ORDER_FULL -> "&7This order is already fulfilled.";
            case PAYOUT_ERROR -> "&7The payout could not be calculated.";
            case DATABASE_ERROR -> "&7Orders is busy right now.";
        };
    }

    private String resolveDeliverFailure(OrdersManager.DeliverOrderResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.DISABLED", "&cOrders is currently disabled.");
            case NO_PLAYER_DATA -> "&cYour player data could not be loaded.";
            case ORDER_NOT_FOUND -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_NOT_FOUND", "&cThat order no longer exists.");
            case NOT_ACTIVE -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_NOT_ACTIVE", "&cThat order is no longer active.");
            case OWN_ORDER -> plugin.getConfigManager().getMessageOrDefault("ORDERS.CANNOT_DELIVER_OWN", "&cYou cannot deliver to your own order.");
            case NO_MATCHING_ITEMS -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NO_MATCHING_ITEMS", "&cYou do not have the required items to deliver.");
            case ORDER_FULL -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_FULL", "&cThat order is already full.");
            case PAYOUT_ERROR -> "&cOrders could not process the payout right now.";
            case DATABASE_ERROR -> "&cOrders could not complete that delivery right now.";
        };
    }
}
