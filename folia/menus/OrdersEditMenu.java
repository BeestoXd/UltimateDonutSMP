package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderDelivery;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OrdersEditMenu extends BaseMenu {

    private final long orderId;
    private final boolean backToMyOrders;
    private final int originPage;
    private final OrderSort sortMode;
    private final String categoryFilter;

    public OrdersEditMenu(
            UltimateDonutSmp plugin,
            long orderId,
            boolean backToMyOrders,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {
        super(plugin, plugin.getOrdersManager().getEditOrderTitle(orderId), plugin.getOrdersManager().getEditOrderSize());
        this.orderId = orderId;
        this.backToMyOrders = backToMyOrders;
        this.originPage = Math.max(1, originPage);
        this.sortMode = sortMode == null ? plugin.getOrdersManager().getDefaultSort() : sortMode;
        this.categoryFilter = categoryFilter == null ? "ALL" : categoryFilter;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        OrdersManager manager = plugin.getOrdersManager();
        Order order = manager.getOrder(orderId);
        set(18, ItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "&cBack", List.of("&7Return to the previous menu")));

        if (order == null) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cOrder Not Found",
                    List.of("&7This order no longer exists.")
            ));
            return;
        }

        boolean owner = order.ownerUuid().equals(player.getUniqueId());
        set(13, OrdersMenuSupport.createOrderDisplay(plugin, manager, order, owner));
        set(10, ItemUtils.createItem(
                Material.PAPER,
                "&bOrder Info",
                List.of(
                        "&7ID: &f#" + order.id(),
                        "&7Owner: &f" + order.ownerName(),
                        "&7Status: &f" + order.status().name(),
                        "&7Category: &f" + manager.prettifyCategory(order.categoryKey())
                )
        ));
        set(16, ItemUtils.createItem(
                Material.CLOCK,
                "&eProgress",
                List.of(
                        "&7Delivered: &e" + order.deliveredQuantity() + "&7/&e" + order.requestedQuantity(),
                        "&7Collected: &e" + order.collectedQuantity() + "&7/&e" + order.deliveredQuantity(),
                        "&7Paid: &a$" + NumberUtils.format(order.paidAmount()),
                        "&7Escrow Left: &a$" + NumberUtils.format(order.escrowRemaining()),
                        "&7Time Left: &f" + manager.formatRemaining(order.secondsRemaining(System.currentTimeMillis()))
                )
        ));
        set(14, buildDeliveryHistory(order.id()));

        if (owner) {
            set(21, ItemUtils.createItem(Material.ENDER_CHEST, "&dCollect", List.of("&7Open your collect queue")));
            if (order.active()) {
                set(23, ItemUtils.createItem(
                        Material.REDSTONE,
                        "&cCancel Order",
                        List.of(
                                "&7Close this order and queue the remaining escrow refund.",
                                "",
                                "&eClick to cancel"
                        )
                ));
            } else {
                set(23, ItemUtils.createItem(Material.BARRIER, "&cOrder Closed", List.of("&7This order can no longer be changed.")));
            }
            return;
        }

        if (!order.active()) {
            set(23, ItemUtils.createItem(Material.BARRIER, "&cOrder Unavailable", List.of("&7This order is no longer active.")));
            return;
        }

        OrdersManager.DeliveryPreview preview = manager.getDeliveryPreview(player, order.id());
        List<String> deliverLore = new ArrayList<>();
        if (preview.success()) {
            deliverLore.add("&7Deliver Quantity: &e" + preview.deliverQuantity());
            deliverLore.add("&7Payout: &a$" + NumberUtils.format(preview.payout()));
            deliverLore.add("");
            deliverLore.add("&eClick to deliver");
            set(23, ItemUtils.createItem(Material.EMERALD, "&aDeliver Items", deliverLore));
        } else {
            deliverLore.add(resolvePreviewMessage(preview));
            set(23, ItemUtils.createItem(Material.BARRIER, "&cCannot Deliver", deliverLore));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == 18) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (backToMyOrders) {
                new OrdersMyOrdersMenu(plugin, originPage, sortMode).open(player);
            } else {
                new OrdersBrowseMenu(plugin, originPage, sortMode, categoryFilter).open(player);
            }
            return;
        }

        Order order = plugin.getOrdersManager().getOrder(orderId);
        if (order == null) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.ORDER_NOT_FOUND",
                    "&cThat order no longer exists."
            )));
            return;
        }

        boolean owner = order.ownerUuid().equals(player.getUniqueId());
        if (owner && slot == 21) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersCollectMenu(plugin, 1).open(player);
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

            if (owner) {
                OrdersManager.CancelOrderResult result = manager.cancelOrder(player, order.id());
                if (!result.success()) {
                    player.sendMessage(ColorUtils.toComponent(resolveCancelFailure(result)));
                    SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.FAIL"));
                    return;
                }

                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                        "ORDERS.CANCELLED",
                        "&eOrder #{order_id} &ehas been closed. Remaining escrow was moved to your collect queue.",
                        "{order_id}", String.valueOf(order.id())
                )));
                SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.SUCCESS"));
                new OrdersCollectMenu(plugin, 1).open(player);
                return;
            }

            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersDeliverConfirmMenu(plugin, order.id(), originPage, sortMode, categoryFilter).open(player);
        } finally {
            manager.endAction(player.getUniqueId());
        }
    }

    private ItemStack buildDeliveryHistory(long orderId) {
        List<String> lore = new ArrayList<>();
        List<OrderDelivery> deliveries = plugin.getOrdersManager().getRecentDeliveries(orderId, 3);
        if (deliveries.isEmpty()) {
            lore.add("&7No deliveries yet.");
        } else {
            for (OrderDelivery delivery : deliveries) {
                lore.add("&f" + delivery.delivererName() + " &7-> &e" + delivery.quantity()
                        + " &7for &a$" + NumberUtils.format(delivery.payout()));
            }
        }
        return ItemUtils.createItem(Material.BOOK, "&bRecent Deliveries", lore);
    }

    private String resolvePreviewMessage(OrdersManager.DeliveryPreview preview) {
        if (preview == null) {
            return "&7Delivery preview unavailable.";
        }
        if (preview.reason() == null) {
            return "&7Ready to deliver.";
        }
        return switch (preview.reason()) {
            case DISABLED -> "&7Orders is disabled.";
            case NO_PLAYER_DATA -> "&7Your player data is unavailable.";
            case ORDER_NOT_FOUND -> "&7This order no longer exists.";
            case NOT_ACTIVE -> "&7This order is no longer active.";
            case OWN_ORDER -> "&7You cannot deliver to your own order.";
            case NO_MATCHING_ITEMS -> "&7You do not have matching items to deliver.";
            case ORDER_FULL -> "&7This order has already been fulfilled.";
            case PAYOUT_ERROR -> "&7The payout could not be calculated.";
            case DATABASE_ERROR -> "&7Orders is busy right now.";
        };
    }

    private String resolveCancelFailure(OrdersManager.CancelOrderResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.DISABLED", "&cOrders is currently disabled.");
            case ORDER_NOT_FOUND -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_NOT_FOUND", "&cThat order no longer exists.");
            case NOT_OWNER -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NOT_YOUR_ORDER", "&cThat order does not belong to you.");
            case NOT_ACTIVE -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_NOT_ACTIVE", "&cThat order is no longer active.");
            case DATABASE_ERROR -> "&cOrders could not cancel that order right now.";
        };
    }
}
