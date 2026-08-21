package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.DeliveryDraft;
import com.bx.ultimateDonutSmp.models.DeliveryQuote;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OrdersDepositMenu extends BaseMenu {

    private final long orderId;
    private final int originPage;
    private final OrderSort sortMode;
    private final String categoryFilter;
    private boolean finalized;

    public OrdersDepositMenu(
            UltimateDonutSmp plugin,
            long orderId,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {
        super(
                plugin,
                OrdersMenuSupport.text(plugin, "ORDERS.GUI.DEPOSIT.TITLE", "&8Orders -> Deliver items"),
                36
        );
        this.orderId = orderId;
        this.originPage = Math.max(1, originPage);
        this.sortMode = sortMode == null ? plugin.getOrdersManager().getDefaultSort() : sortMode;
        this.categoryFilter = plugin.getOrdersManager().normalizeCategory(categoryFilter);
    }

    @Override
    public void build(Player player) {
        clear();
        Order order = plugin.getOrdersManager().getOrder(orderId);
        if (order == null || !order.active() || order.ownerUuid().equals(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(OrdersMenuSupport.text(
                    plugin, "ORDERS.ORDER_NOT_ACTIVE", "&cThat order is no longer available."
            )));
            plugin.getSpigotScheduler().runEntityLater(player, () ->
                    new OrdersBrowseMenu(plugin, originPage, sortMode, categoryFilter).open(player), 1L);
            return;
        }

        int confirmSlot = confirmSlot();
        set(confirmSlot, OrdersMenuSupport.button(
                plugin,
                "GUI.DELIVERY_DEPOSIT.BUTTONS.CONFIRM",
                "ORDERS.GUI.DEPOSIT.CONFIRM",
                Material.LIME_STAINED_GLASS_PANE,
                "&aConfirm delivery",
                List.of(
                        "&fPlace matching items in this menu.",
                        "&7Remaining: &f{remaining}",
                        "&7Price each: &f{price_each}",
                        "",
                        "&eClick to review"
                ),
                "{remaining}", String.valueOf(order.remainingQuantity()),
                "{price_each}", plugin.getCurrencyManager().formatMoney(order.priceEach()),
                "{item}", plugin.getOrdersManager().describeItem(order.requestedItem())
        ));
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }
        int rawSlot = event.getRawSlot();
        if (rawSlot == confirmSlot()) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
            confirm(player);
            return;
        }
        if (rawSlot >= 0 && rawSlot < inventory.getSize() && rawSlot != confirmSlot()) {
            event.setCancelled(false);
            return;
        }
        event.setCancelled(false);
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        if (event.getRawSlots().contains(confirmSlot())) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        } else {
            event.setCancelled(false);
        }
    }

    @Override
    public void onClose(Player player) {
        if (finalized) {
            return;
        }
        finalized = true;
        plugin.getOrdersManager().giveOrDrop(player, takeDepositedItems());
    }

    private void confirm(Player player) {
        OrdersManager manager = plugin.getOrdersManager();
        if (finalized || !manager.beginAction(player.getUniqueId())) {
            return;
        }
        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                return;
            }
            manager.updateClickCooldown(player.getUniqueId());
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

            List<ItemStack> deposited = takeDepositedItems();
            DeliveryQuote quote = manager.quoteDelivery(player, orderId, deposited);
            finalized = true;
            manager.giveOrDrop(player, quote.returnedItems());
            if (!quote.success()) {
                player.sendMessage(ColorUtils.toComponent(resolveFailure(quote.failureCode())));
                new OrdersDepositMenu(plugin, orderId, originPage, sortMode, categoryFilter).open(player);
                return;
            }

            DeliveryDraft draft = manager.createDeliveryDraft(player, quote);
            new OrdersDeliverConfirmMenu(
                    plugin,
                    draft,
                    originPage,
                    sortMode,
                    categoryFilter
            ).open(player);
        } finally {
            manager.endAction(player.getUniqueId());
        }
    }

    private List<ItemStack> takeDepositedItems() {
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot == confirmSlot()) {
                continue;
            }
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                items.add(item.clone());
                inventory.setItem(slot, null);
            }
        }
        return items;
    }

    private int confirmSlot() {
        return OrdersMenuSupport.slot(plugin, "GUI.DELIVERY_DEPOSIT.BUTTONS.CONFIRM.SLOT", 35);
    }

    private String resolveFailure(String failureCode) {
        return switch (failureCode == null ? "" : failureCode) {
            case "OWN_ORDER" -> OrdersMenuSupport.text(plugin, "ORDERS.CANNOT_DELIVER_OWN", "&cYou cannot deliver to your own order.");
            case "NO_MATCHING_ITEMS" -> OrdersMenuSupport.text(plugin, "ORDERS.NO_MATCHING_ITEMS", "&cNo matching items were deposited.");
            case "ORDER_FULL" -> OrdersMenuSupport.text(plugin, "ORDERS.ORDER_FULL", "&cThat order is already full.");
            case "PAYOUT_ERROR" -> OrdersMenuSupport.text(plugin, "ORDERS.DELIVERY_FAILED_ECONOMY", "&cThe order escrow could not cover this delivery.");
            default -> OrdersMenuSupport.text(plugin, "ORDERS.ORDER_NOT_ACTIVE", "&cThat order is no longer active.");
        };
    }
}
