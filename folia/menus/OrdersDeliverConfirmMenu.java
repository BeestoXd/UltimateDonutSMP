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
        set(18, ItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "&cʙᴀᴄᴋ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴏʀᴅᴇʀ ᴅᴇᴛᴀɪʟѕ")));

        if (preview.order() == null) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cᴏʀᴅᴇʀ ɴᴏᴛ ꜰᴏᴜɴᴅ",
                    List.of("&7ᴛʜɪѕ ᴏʀᴅᴇʀ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.")
            ));
            return;
        }

        Order order = preview.order();
        set(13, OrdersMenuSupport.createOrderDisplay(plugin, manager, order, false));

        if (!preview.success()) {
            set(11, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cᴄᴀɴɴᴏᴛ ᴅᴇʟɪᴠᴇʀ",
                    List.of(resolveFailureMessage(preview))
            ));
            return;
        }

        set(11, ItemUtils.createItem(
                Material.PAPER,
                "&bᴅᴇʟɪᴠᴇʀʏ ᴘʀᴇᴠɪᴇᴡ",
                List.of(
                        "&7ᴅᴇʟɪᴠᴇʀ ǫᴜᴀɴᴛɪᴛʏ: &e" + preview.deliverQuantity(),
                        "&7ᴘᴀʏᴏᴜᴛ: &a$" + NumberUtils.format(preview.payout()),
                        "&7ʀᴇᴍᴀɪɴɪɴɢ ᴀꜰᴛᴇʀ ᴛʜɪѕ: &e" + Math.max(0, order.remainingQuantity() - preview.deliverQuantity())
                )
        ));
        set(15, ItemUtils.createItem(
                Material.CHEST,
                "&eᴍᴀᴛᴄʜɪɴɢ ɪᴛᴇᴍѕ ꜰᴏᴜɴᴅ",
                List.of(
                        "&7ᴏʀᴅᴇʀѕ ᴡɪʟʟ ʀᴇᴍᴏᴠᴇ ᴍᴀᴛᴄʜɪɴɢ ɪᴛᴇᴍѕ ꜰʀᴏᴍ ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ.",
                        "&7ʀᴇǫᴜᴇѕᴛᴇᴅ ɪᴛᴇᴍ: &f" + manager.describeItem(order.requestedItem())
                )
        ));
        set(23, ItemUtils.createItem(
                Material.LIME_DYE,
                "&aᴄᴏɴꜰɪʀᴍ ᴅᴇʟɪᴠᴇʀʏ",
                List.of(
                        "&7ʏᴏᴜ ᴡɪʟʟ ʙᴇ ᴘᴀɪᴅ ɪɴѕᴛᴀɴᴛʟʏ ᴏɴ ѕᴜᴄᴄᴇѕѕ.",
                        "",
                        "&eᴄʟɪᴄᴋ ᴛᴏ ᴅᴇʟɪᴠᴇʀ"
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
            player.sendMessage(ColorUtils.toComponent("&cᴏʀᴅᴇʀѕ ɪѕ ѕᴛɪʟʟ ᴘʀᴏᴄᴇѕѕɪɴɢ ʏᴏᴜʀ ᴘʀᴇᴠɪᴏᴜѕ ᴀᴄᴛɪᴏɴ."));
            return;
        }

        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent("&cѕʟᴏᴡ ᴅᴏᴡɴ ꜰᴏʀ ᴀ ᴍᴏᴍᴇɴᴛ."));
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
                    "&aᴅᴇʟɪᴠᴇʀᴇᴅ &e{quantity} {item}&a ᴀɴᴅ ʀᴇᴄᴇɪᴠᴇᴅ &a${payout}&a.",
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
            return "&7ʀᴇᴀᴅʏ.";
        }
        return switch (preview.reason()) {
            case DISABLED -> "&7ᴏʀᴅᴇʀѕ ɪѕ ᴅɪѕᴀʙʟᴇᴅ.";
            case NO_PLAYER_DATA -> "&7ʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ɪѕ ᴜɴᴀᴠᴀɪʟᴀʙʟᴇ.";
            case ORDER_NOT_FOUND -> "&7ᴛʜɪѕ ᴏʀᴅᴇʀ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.";
            case NOT_ACTIVE -> "&7ᴛʜɪѕ ᴏʀᴅᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ.";
            case OWN_ORDER -> "&7ʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴅᴇʟɪᴠᴇʀ ᴛᴏ ʏᴏᴜʀ ᴏᴡɴ ᴏʀᴅᴇʀ.";
            case NO_MATCHING_ITEMS -> "&7ʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴍᴀᴛᴄʜɪɴɢ ɪᴛᴇᴍѕ ᴛᴏ ᴅᴇʟɪᴠᴇʀ.";
            case ORDER_FULL -> "&7ᴛʜɪѕ ᴏʀᴅᴇʀ ɪѕ ᴀʟʀᴇᴀᴅʏ ꜰᴜʟꜰɪʟʟᴇᴅ.";
            case PAYOUT_ERROR -> "&7ᴛʜᴇ ᴘᴀʏᴏᴜᴛ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ᴄᴀʟᴄᴜʟᴀᴛᴇᴅ.";
            case DATABASE_ERROR -> "&7ᴏʀᴅᴇʀѕ ɪѕ ʙᴜѕʏ ʀɪɢʜᴛ ɴᴏᴡ.";
        };
    }

    private String resolveDeliverFailure(OrdersManager.DeliverOrderResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.DISABLED", "&cᴏʀᴅᴇʀѕ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ.");
            case NO_PLAYER_DATA -> "&cʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ʟᴏᴀᴅᴇᴅ.";
            case ORDER_NOT_FOUND -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_NOT_FOUND", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.");
            case NOT_ACTIVE -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_NOT_ACTIVE", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ.");
            case OWN_ORDER -> plugin.getConfigManager().getMessageOrDefault("ORDERS.CANNOT_DELIVER_OWN", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴅᴇʟɪᴠᴇʀ ᴛᴏ ʏᴏᴜʀ ᴏᴡɴ ᴏʀᴅᴇʀ.");
            case NO_MATCHING_ITEMS -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NO_MATCHING_ITEMS", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴛʜᴇ ʀᴇǫᴜɪʀᴇᴅ ɪᴛᴇᴍѕ ᴛᴏ ᴅᴇʟɪᴠᴇʀ.");
            case ORDER_FULL -> plugin.getConfigManager().getMessageOrDefault("ORDERS.ORDER_FULL", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ɪѕ ᴀʟʀᴇᴀᴅʏ ꜰᴜʟʟ.");
            case PAYOUT_ERROR -> "&cᴏʀᴅᴇʀѕ ᴄᴏᴜʟᴅ ɴᴏᴛ ᴘʀᴏᴄᴇѕѕ ᴛʜᴇ ᴘᴀʏᴏᴜᴛ ʀɪɢʜᴛ ɴᴏᴡ.";
            case DATABASE_ERROR -> "&cᴏʀᴅᴇʀѕ ᴄᴏᴜʟᴅ ɴᴏᴛ ᴄᴏᴍᴘʟᴇᴛᴇ ᴛʜᴀᴛ ᴅᴇʟɪᴠᴇʀʏ ʀɪɢʜᴛ ɴᴏᴡ.";
        };
    }
}
