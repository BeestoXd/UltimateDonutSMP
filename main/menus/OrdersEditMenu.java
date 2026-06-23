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
        set(18, ItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "&cʙᴀᴄᴋ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴛʜᴇ ᴘʀᴇᴠɪᴏᴜѕ ᴍᴇɴᴜ")));

        if (order == null) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cᴏʀᴅᴇʀ ɴᴏᴛ ꜰᴏᴜɴᴅ",
                    List.of("&7ᴛʜɪѕ ᴏʀᴅᴇʀ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.")
            ));
            return;
        }

        boolean owner = order.ownerUuid().equals(player.getUniqueId());
        set(13, OrdersMenuSupport.createOrderDisplay(plugin, manager, order, owner));
        set(10, ItemUtils.createItem(
                Material.PAPER,
                "&bᴏʀᴅᴇʀ ɪɴꜰᴏ",
                List.of(
                        "&7ɪᴅ: &f#" + order.id(),
                        "&7ᴏᴡɴᴇʀ: &f" + order.ownerName(),
                        OrdersMenuSupport.tr("&7ѕᴛᴀᴛᴜѕ: &f") + plugin.getLanguageManager().display(
                                "ORDER_STATUSES",
                                order.status().name(),
                                order.status().name()
                        ),
                        "&7ᴄᴀᴛᴇɢᴏʀʏ: &f" + manager.prettifyCategory(order.categoryKey())
                )
        ));
        set(16, ItemUtils.createItem(
                Material.CLOCK,
                "&eᴘʀᴏɢʀᴇѕѕ",
                List.of(
                        "&7ᴅᴇʟɪᴠᴇʀᴇᴅ: &e" + order.deliveredQuantity() + "&7/&e" + order.requestedQuantity(),
                        "&7ᴄᴏʟʟᴇᴄᴛᴇᴅ: &e" + order.collectedQuantity() + "&7/&e" + order.deliveredQuantity(),
                        "&7ᴘᴀɪᴅ: " + plugin.getCurrencyManager().formatMoney(order.paidAmount()),
                        "&7ᴇѕᴄʀᴏᴡ ʟᴇꜰᴛ: " + plugin.getCurrencyManager().formatMoney(order.escrowRemaining()),
                        "&7ᴛɪᴍᴇ ʟᴇꜰᴛ: &f" + manager.formatRemaining(order.secondsRemaining(System.currentTimeMillis()))
                )
        ));
        set(14, buildDeliveryHistory(order.id()));

        if (owner) {
            boolean editable = order.active() && order.deliveredQuantity() == 0;
            if (editable) {
                set(19, ItemUtils.createItem(
                        Material.NAME_TAG,
                        "&bᴄʜᴀɴɢᴇ ɪᴛᴇᴍ",
                        List.of("&7ᴜѕᴇ ᴛʜᴇ ᴄᴏɴꜰɪɢᴜʀᴇᴅ ɪᴛᴇᴍ ѕᴇʟᴇᴄᴛɪᴏɴ ᴍᴏᴅᴇ.", "", "&eᴄʟɪᴄᴋ ᴛᴏ ᴇᴅɪᴛ")
                ));
                set(20, ItemUtils.createItem(
                        Material.WRITABLE_BOOK,
                        "&eᴄʜᴀɴɢᴇ ǫᴜᴀɴᴛɪᴛʏ",
                        List.of("&7ᴄᴜʀʀᴇɴᴛ: &e" + order.requestedQuantity(), "", "&eᴄʟɪᴄᴋ ᴛᴏ ᴇᴅɪᴛ")
                ));
                set(22, ItemUtils.createItem(
                        Material.GOLD_INGOT,
                        "&eᴄʜᴀɴɢᴇ ᴘʀɪᴄᴇ",
                        List.of("&7ᴄᴜʀʀᴇɴᴛ: " + plugin.getCurrencyManager().formatMoney(order.priceEach()), "", "&eᴄʟɪᴄᴋ ᴛᴏ ᴇᴅɪᴛ")
                ));
            } else {
                List<String> lockedLore = order.active()
                        ? List.of("&7ᴛʜɪѕ ᴏʀᴅᴇʀ ᴀʟʀᴇᴀᴅʏ ʜᴀѕ ᴅᴇʟɪᴠᴇʀɪᴇѕ.")
                        : List.of("&7ᴛʜɪѕ ᴏʀᴅᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ.");
                set(19, ItemUtils.createItem(Material.GRAY_DYE, "&cᴇᴅɪᴛ ʟᴏᴄᴋᴇᴅ", lockedLore));
                set(20, ItemUtils.createItem(Material.GRAY_DYE, "&cᴇᴅɪᴛ ʟᴏᴄᴋᴇᴅ", lockedLore));
                set(22, ItemUtils.createItem(Material.GRAY_DYE, "&cᴇᴅɪᴛ ʟᴏᴄᴋᴇᴅ", lockedLore));
            }
            set(21, ItemUtils.createItem(Material.ENDER_CHEST, "&dᴄᴏʟʟᴇᴄᴛ", List.of("&7ᴏᴘᴇɴ ʏᴏᴜʀ ᴄᴏʟʟᴇᴄᴛ ǫᴜᴇᴜᴇ")));
            if (order.active()) {
                set(23, ItemUtils.createItem(
                        Material.REDSTONE,
                        "&cᴄᴀɴᴄᴇʟ ᴏʀᴅᴇʀ",
                        List.of(
                                "&7ᴄʟᴏѕᴇ ᴛʜɪѕ ᴏʀᴅᴇʀ ᴀɴᴅ ǫᴜᴇᴜᴇ ᴛʜᴇ ʀᴇᴍᴀɪɴɪɴɢ ᴇѕᴄʀᴏᴡ ʀᴇꜰᴜɴᴅ.",
                                "",
                                "&eᴄʟɪᴄᴋ ᴛᴏ ᴄᴀɴᴄᴇʟ"
                        )
                ));
            } else {
                set(23, ItemUtils.createItem(Material.BARRIER, "&cᴏʀᴅᴇʀ ᴄʟᴏѕᴇᴅ", List.of("&7ᴛʜɪѕ ᴏʀᴅᴇʀ ᴄᴀɴ ɴᴏ ʟᴏɴɢᴇʀ ʙᴇ ᴄʜᴀɴɢᴇᴅ.")));
            }
            return;
        }

        if (!order.active()) {
            set(23, ItemUtils.createItem(Material.BARRIER, "&cᴏʀᴅᴇʀ ᴜɴᴀᴠᴀɪʟᴀʙʟᴇ", List.of("&7ᴛʜɪѕ ᴏʀᴅᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ.")));
            return;
        }

        OrdersManager.DeliveryPreview preview = manager.getDeliveryPreview(player, order.id());
        List<String> deliverLore = new ArrayList<>();
        if (preview.success()) {
            deliverLore.add("&7ᴅᴇʟɪᴠᴇʀ ǫᴜᴀɴᴛɪᴛʏ: &e" + preview.deliverQuantity());
            deliverLore.add("&7ᴘᴀʏᴏᴜᴛ: " + plugin.getCurrencyManager().formatMoney(preview.payout()));
            deliverLore.add("");
            deliverLore.add("&eᴄʟɪᴄᴋ ᴛᴏ ᴅᴇʟɪᴠᴇʀ");
            set(23, ItemUtils.createItem(Material.EMERALD, "&aᴅᴇʟɪᴠᴇʀ ɪᴛᴇᴍѕ", deliverLore));
        } else {
            deliverLore.add(resolvePreviewMessage(preview));
            set(23, ItemUtils.createItem(Material.BARRIER, "&cᴄᴀɴɴᴏᴛ ᴅᴇʟɪᴠᴇʀ", deliverLore));
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
                    "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ."
            )));
            return;
        }

        boolean owner = order.ownerUuid().equals(player.getUniqueId());
        if (owner && slot == 19) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getOrdersManager().openEditOrderItemSelection(player, order.id(), backToMyOrders, originPage, sortMode, categoryFilter);
            return;
        }

        if (owner && slot == 20) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getOrdersManager().promptEditOrderQuantityInput(player, order.id(), backToMyOrders, originPage, sortMode, categoryFilter);
            return;
        }

        if (owner && slot == 21) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersCollectMenu(plugin, 1, order.id()).open(player);
            return;
        }

        if (owner && slot == 22) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getOrdersManager().promptEditOrderPriceInput(player, order.id(), backToMyOrders, originPage, sortMode, categoryFilter);
            return;
        }

        if (slot != 23) {
            return;
        }

        if (owner) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersDeleteConfirmMenu(
                    plugin, order.id(), backToMyOrders, originPage, sortMode, categoryFilter
            ).open(player);
            return;
        }
        if (plugin.getOrdersManager().getDeliveryMode() == OrdersManager.DeliveryMode.DEPOSIT_GUI) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersDepositMenu(plugin, order.id(), originPage, sortMode, categoryFilter).open(player);
            return;
        }

        OrdersManager manager = plugin.getOrdersManager();
        if (!manager.beginAction(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cᴏʀᴅᴇʀѕ ɪѕ ѕᴛɪʟʟ ᴘʀᴏᴄᴇѕѕɪɴɢ ʏᴏᴜʀ ᴘʀᴇᴠɪᴏᴜѕ ᴀᴄᴛɪᴏɴ."));
            return;
        }

        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent("&cѕʟᴏᴡ ᴅᴏᴡɴ ꜰᴏʀ ᴀ ᴍᴏᴍᴇɴᴛ."));
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
                        "&eᴏʀᴅᴇʀ #{order_id} &eʜᴀѕ ʙᴇᴇɴ ᴄʟᴏѕᴇᴅ. ʀᴇᴍᴀɪɴɪɴɢ ᴇѕᴄʀᴏᴡ ᴡᴀѕ ᴍᴏᴠᴇᴅ ᴛᴏ ʏᴏᴜʀ ᴄᴏʟʟᴇᴄᴛ ǫᴜᴇᴜᴇ.",
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
            lore.add("&7ɴᴏ ᴅᴇʟɪᴠᴇʀɪᴇѕ ʏᴇᴛ.");
        } else {
            for (OrderDelivery delivery : deliveries) {
                lore.add("&f" + delivery.delivererName() + " &7-> &e" + delivery.quantity()
                        + " &7ꜰᴏʀ " + plugin.getCurrencyManager().formatMoney(delivery.payout()));
            }
        }
        return ItemUtils.createItem(Material.BOOK, "&bʀᴇᴄᴇɴᴛ ᴅᴇʟɪᴠᴇʀɪᴇѕ", lore);
    }

    private String resolvePreviewMessage(OrdersManager.DeliveryPreview preview) {
        if (preview == null) {
            return "&7ᴅᴇʟɪᴠᴇʀʏ ᴘʀᴇᴠɪᴇᴡ ᴜɴᴀᴠᴀɪʟᴀʙʟᴇ.";
        }
        if (preview.reason() == null) {
            return "&7ʀᴇᴀᴅʏ ᴛᴏ ᴅᴇʟɪᴠᴇʀ.";
        }
        return switch (preview.reason()) {
            case DISABLED -> "&7ᴏʀᴅᴇʀѕ ɪѕ ᴅɪѕᴀʙʟᴇᴅ.";
            case NO_PLAYER_DATA -> "&7ʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ɪѕ ᴜɴᴀᴠᴀɪʟᴀʙʟᴇ.";
            case ORDER_NOT_FOUND -> "&7ᴛʜɪѕ ᴏʀᴅᴇʀ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.";
            case NOT_ACTIVE -> "&7ᴛʜɪѕ ᴏʀᴅᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ.";
            case OWN_ORDER -> "&7ʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴅᴇʟɪᴠᴇʀ ᴛᴏ ʏᴏᴜʀ ᴏᴡɴ ᴏʀᴅᴇʀ.";
            case NO_MATCHING_ITEMS -> "&7ʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴍᴀᴛᴄʜɪɴɢ ɪᴛᴇᴍѕ ᴛᴏ ᴅᴇʟɪᴠᴇʀ.";
            case ORDER_FULL -> "&7ᴛʜɪѕ ᴏʀᴅᴇʀ ʜᴀѕ ᴀʟʀᴇᴀᴅʏ ʙᴇᴇɴ ꜰᴜʟꜰɪʟʟᴇᴅ.";
            case PAYOUT_ERROR -> "&7ᴛʜᴇ ᴘᴀʏᴏᴜᴛ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ᴄᴀʟᴄᴜʟᴀᴛᴇᴅ.";
            case DATABASE_ERROR -> "&7ᴏʀᴅᴇʀѕ ɪѕ ʙᴜѕʏ ʀɪɢʜᴛ ɴᴏᴡ.";
        };
    }

    private String resolveCancelFailure(OrdersManager.CancelOrderResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.DISABLED", "&cᴏʀᴅᴇʀѕ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ.");
            case ORDER_NOT_FOUND -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_NOT_FOUND", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.");
            case NOT_OWNER -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NOT_YOUR_ORDER", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ᴅᴏᴇѕ ɴᴏᴛ ʙᴇʟᴏɴɢ ᴛᴏ ʏᴏᴜ.");
            case NOT_ACTIVE -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_NOT_ACTIVE", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ.");
            case DATABASE_ERROR -> "&cᴏʀᴅᴇʀѕ ᴄᴏᴜʟᴅ ɴᴏᴛ ᴄᴀɴᴄᴇʟ ᴛʜᴀᴛ ᴏʀᴅᴇʀ ʀɪɢʜᴛ ɴᴏᴡ.";
        };
    }
}
