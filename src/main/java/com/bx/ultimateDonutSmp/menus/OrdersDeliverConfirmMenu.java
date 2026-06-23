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
                OrdersMenuSupport.text(plugin, "ORDERS.GUI.CONFIRM.TITLE", "&8ᴏʀᴅᴇʀѕ -> ᴄᴏɴꜰɪʀᴍ ᴅᴇʟɪᴠᴇʀʏ"),
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
            set(13, ItemUtils.createItem(Material.BARRIER, "&cᴏʀᴅᴇʀ ᴜɴᴀᴠᴀɪʟᴀʙʟᴇ", List.of("&7ᴄʟᴏѕᴇ ᴛʜɪѕ ᴍᴇɴᴜ ᴛᴏ ʀᴇᴛᴜʀɴ.")));
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
                Material.RED_STAINED_GLASS_PANE, "&cᴄᴀɴᴄᴇʟ", List.of("&fʀᴇᴛᴜʀɴ ᴡɪᴛʜᴏᴜᴛ ᴅᴇʟɪᴠᴇʀɪɴɢ")
        ));
        set(13, OrdersMenuSupport.decorateItem(
                plugin,
                order.requestedItem(),
                OrdersMenuSupport.text(plugin, "ORDERS.GUI.CONFIRM.SUMMARY.NAME", "&f{owner}'ѕ ᴏʀᴅᴇʀ",
                        "{owner}", order.ownerName()),
                OrdersMenuSupport.list(
                        plugin,
                        "ORDERS.GUI.CONFIRM.SUMMARY.LORE",
                        List.of(
                                "&7ɪᴛᴇᴍ: &f{item}",
                                "&7ᴀᴍᴏᴜɴᴛ: &f{amount}",
                                "&7ʀᴇᴄᴇɪᴠᴇ: &a{receive}",
                                "&7ʀᴇᴍᴀɪɴɪɴɢ ᴀꜰᴛᴇʀ: &f{remaining}"
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
                quantity > 0 ? "&aᴄᴏɴꜰɪʀᴍ" : "&cᴄᴀɴɴᴏᴛ ᴅᴇʟɪᴠᴇʀ",
                quantity > 0
                        ? List.of("&fᴅᴇʟɪᴠᴇʀ {amount} ɪᴛᴇᴍѕ", "&7ʏᴏᴜ ʀᴇᴄᴇɪᴠᴇ {receive}")
                        : List.of("&7ɴᴏ ᴅᴇʟɪᴠᴇʀᴀʙʟᴇ ɪᴛᴇᴍѕ ᴀʀᴇ ᴀᴠᴀɪʟᴀʙʟᴇ."),
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
                        "&aᴅᴇʟɪᴠᴇʀᴇᴅ &e{quantity} {item}&a ᴀɴᴅ ʀᴇᴄᴇɪᴠᴇᴅ &a{payout}&a.",
                        "{quantity}", String.valueOf(result.deliveredQuantity()),
                        "{item}", result.order() == null ? "ɪᴛᴇᴍѕ" : manager.describeItem(result.order().requestedItem()),
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
            return OrdersMenuSupport.text(plugin, "ORDERS.DELIVERY_FAILED", "&cᴅᴇʟɪᴠᴇʀʏ ꜰᴀɪʟᴇᴅ.");
        }
        return switch (result.reason()) {
            case OWN_ORDER -> OrdersMenuSupport.text(plugin, "ORDERS.CANNOT_DELIVER_OWN", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴅᴇʟɪᴠᴇʀ ᴛᴏ ʏᴏᴜʀ ᴏᴡɴ ᴏʀᴅᴇʀ.");
            case NO_MATCHING_ITEMS -> OrdersMenuSupport.text(plugin, "ORDERS.NO_MATCHING_ITEMS", "&cᴛʜᴇ ᴅᴇᴘᴏѕɪᴛᴇᴅ ɪᴛᴇᴍѕ ɴᴏ ʟᴏɴɢᴇʀ ᴍᴀᴛᴄʜ.");
            case ORDER_FULL -> OrdersMenuSupport.text(plugin, "ORDERS.ORDER_FULL", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ɪѕ ᴀʟʀᴇᴀᴅʏ ꜰᴜʟʟ.");
            case PAYOUT_ERROR -> OrdersMenuSupport.text(plugin, "ORDERS.DELIVERY_FAILED_ECONOMY", "&cᴛʜᴇ ᴘᴀʏᴏᴜᴛ ᴛʀᴀɴѕᴀᴄᴛɪᴏɴ ꜰᴀɪʟᴇᴅ.");
            default -> OrdersMenuSupport.text(plugin, "ORDERS.ORDER_NOT_ACTIVE", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ.");
        };
    }
}
