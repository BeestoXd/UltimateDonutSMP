package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersDeleteConfirmMenu extends BaseMenu {

    private final long orderId;
    private final boolean backToMyOrders;
    private final int originPage;
    private final OrderSort sortMode;
    private final String categoryFilter;

    public OrdersDeleteConfirmMenu(
            UltimateDonutSmp plugin,
            long orderId,
            boolean backToMyOrders,
            int originPage,
            OrderSort sortMode,
            String categoryFilter
    ) {
        super(plugin, OrdersMenuSupport.text(plugin, "ORDERS.GUI.DELETE.TITLE", "&8ᴏʀᴅᴇʀѕ -> ᴅᴇʟᴇᴛᴇ ᴏʀᴅᴇʀ"), 27);
        this.orderId = orderId;
        this.backToMyOrders = backToMyOrders;
        this.originPage = Math.max(1, originPage);
        this.sortMode = sortMode == null ? plugin.getOrdersManager().getDefaultSort() : sortMode;
        this.categoryFilter = plugin.getOrdersManager().normalizeCategory(categoryFilter);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        Order order = plugin.getOrdersManager().getOrder(orderId);
        if (order != null) {
            set(13, OrdersMenuSupport.createOrderDisplay(plugin, plugin.getOrdersManager(), order, true));
        }
        set(10, OrdersMenuSupport.button(
                plugin, "GUI.DELETE.BUTTONS.BACK", "ORDERS.GUI.DELETE.BACK",
                Material.RED_STAINED_GLASS_PANE, "&cʙᴀᴄᴋ", List.of("&fʀᴇᴛᴜʀɴ ᴡɪᴛʜᴏᴜᴛ ᴄᴀɴᴄᴇʟʟɪɴɢ")
        ));
        set(16, OrdersMenuSupport.button(
                plugin, "GUI.DELETE.BUTTONS.CONFIRM", "ORDERS.GUI.DELETE.CONFIRM",
                Material.LIME_STAINED_GLASS_PANE, "&aᴄᴏɴꜰɪʀᴍ",
                List.of("&fᴄᴀɴᴄᴇʟ ᴛʜɪѕ ᴏʀᴅᴇʀ", "&7ᴜɴᴜѕᴇᴅ ᴇѕᴄʀᴏᴡ ʙᴇᴄᴏᴍᴇѕ ᴀ ᴄᴏʟʟᴇᴄᴛᴀʙʟᴇ ʀᴇꜰᴜɴᴅ")
        ));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == 10) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            openEdit(player);
            return;
        }
        if (slot != 16) {
            return;
        }

        OrdersManager manager = plugin.getOrdersManager();
        if (!manager.beginAction(player.getUniqueId())) {
            return;
        }
        try {
            manager.updateClickCooldown(player.getUniqueId());
            OrdersManager.CancelOrderResult result = manager.cancelOrder(player, orderId);
            if (!result.success()) {
                player.sendMessage(ColorUtils.toComponent(OrdersMenuSupport.text(
                        plugin, "ORDERS.ORDER_NOT_ACTIVE", "&cᴛʜᴀᴛ ᴏʀᴅᴇʀ ᴄᴀɴ ɴᴏ ʟᴏɴɢᴇʀ ʙᴇ ᴄᴀɴᴄᴇʟʟᴇᴅ."
                )));
                SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.FAIL"));
                openEdit(player);
                return;
            }
            player.sendMessage(ColorUtils.toComponent(OrdersMenuSupport.text(
                    plugin,
                    "ORDERS.CANCELLED",
                    "&eᴏʀᴅᴇʀ #{order_id} ᴡᴀѕ ᴄᴀɴᴄᴇʟʟᴇᴅ. ʀᴇᴍᴀɪɴɪɴɢ ᴇѕᴄʀᴏᴡ ɪѕ ʀᴇᴀᴅʏ ᴛᴏ ᴄᴏʟʟᴇᴄᴛ.",
                    "{order_id}", String.valueOf(orderId)
            )));
            SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.SUCCESS"));
            new OrdersMyOrdersMenu(plugin, 1, sortMode).open(player);
        } finally {
            manager.endAction(player.getUniqueId());
        }
    }

    private void openEdit(Player player) {
        new OrdersEditMenu(
                plugin, orderId, backToMyOrders, originPage, sortMode, categoryFilter
        ).open(player);
    }
}
