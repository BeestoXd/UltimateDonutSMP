package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersNewMenu extends BaseMenu {

    public OrdersNewMenu(UltimateDonutSmp plugin) {
        super(plugin, plugin.getOrdersManager().getNewOrderTitle(), plugin.getOrdersManager().getNewOrderSize());
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        OrdersManager.PendingOrderCreationSnapshot pending = plugin.getOrdersManager().getPendingCreation(player.getUniqueId());
        set(18, ItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "&cBack", List.of("&7Return to item selection")));

        if (pending == null) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo Pending Order",
                    List.of("&7Pick an item first to create a new order.")
            ));
            return;
        }

        set(11, ItemUtils.createItem(
                Material.PAPER,
                "&bOrder Details",
                List.of(
                        "&7Item: &f" + plugin.getOrdersManager().describeMaterial(pending.entry().material()),
                        "&7Category: &f" + plugin.getOrdersManager().prettifyCategory(pending.entry().categoryKey()),
                        "&7Quantity: &e" + pending.quantity(),
                        "&7Price Each: &a$" + NumberUtils.format(pending.priceEach()),
                        "&7Total Budget: &a$" + NumberUtils.format(pending.totalBudget())
                )
        ));
        set(13, ItemUtils.createItem(
                pending.entry().material(),
                "&b" + plugin.getOrdersManager().describeMaterial(pending.entry().material()),
                List.of("&7This is the item other players will deliver.")
        ));
        set(15, ItemUtils.createItem(
                Material.SUNFLOWER,
                "&eBalance Check",
                List.of(
                        "&7Current Balance: &a$" + NumberUtils.format(plugin.getEconomyManager().getBalance(player)),
                        "&7Creation Fee: &a$" + NumberUtils.format(plugin.getConfigManager().getOrders().getDouble("PRICING.ORDER_CREATION_FEE", 0D)),
                        "&7Required: &a$" + NumberUtils.format(
                                pending.totalBudget() + plugin.getConfigManager().getOrders().getDouble("PRICING.ORDER_CREATION_FEE", 0D)
                        )
                )
        ));
        set(23, ItemUtils.createItem(
                Material.LIME_DYE,
                "&aConfirm Order",
                List.of(
                        "&7This will lock your budget in escrow.",
                        "",
                        "&eClick to create"
                )
        ));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == 18) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersSelectItemMenu(plugin, 1, "ALL").open(player);
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

            OrdersManager.CreateOrderResult result = manager.createOrder(player);
            if (!result.success()) {
                player.sendMessage(ColorUtils.toComponent(resolveFailureMessage(result)));
                SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.FAIL"));
                return;
            }

            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "ORDERS.CREATED",
                    "&aOrder created! &7#{order_id} &ffor &e{quantity} {item}&7 at &a${price_each} &7each. Budget locked: &a${budget}&7.",
                    "{order_id}", String.valueOf(result.order().id()),
                    "{quantity}", String.valueOf(result.order().requestedQuantity()),
                    "{item}", manager.describeItem(result.order().requestedItem()),
                    "{price_each}", NumberUtils.format(result.order().priceEach()),
                    "{budget}", NumberUtils.format(result.order().totalBudget())
            )));
            SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.SUCCESS"));
            new OrdersMyOrdersMenu(plugin, 1, manager.getDefaultSort()).open(player);
        } finally {
            manager.endAction(player.getUniqueId());
        }
    }

    private String resolveFailureMessage(OrdersManager.CreateOrderResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.DISABLED", "&cOrders is currently disabled.");
            case NO_PENDING_ORDER -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NO_PENDING_ORDER", "&cThere is no pending order draft to confirm.");
            case NO_PLAYER_DATA -> "&cYour player data could not be loaded.";
            case INVALID_ITEM -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ITEM_BLOCKED", "&cThat item cannot be ordered.");
            case INVALID_QUANTITY -> plugin.getConfigManager().getMessageOrDefault("ORDERS.INVALID_QUANTITY", "&cInvalid quantity.");
            case INVALID_PRICE -> plugin.getConfigManager().getMessageOrDefault("ORDERS.INVALID_PRICE", "&cInvalid price.");
            case TOTAL_TOO_HIGH -> plugin.getConfigManager().getMessageOrDefault("ORDERS.TOTAL_TOO_HIGH", "&cThat total order budget is too high.");
            case NO_MONEY -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NOT_ENOUGH_MONEY", "&cYou do not have enough money for that order.");
            case MAX_ORDERS_REACHED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.MAX_ACTIVE_REACHED", "&cYou have reached your active order limit.");
            case DATABASE_ERROR -> "&cOrders could not save your order right now. Try again.";
        };
    }
}
