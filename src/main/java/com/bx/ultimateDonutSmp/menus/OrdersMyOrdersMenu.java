package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.Locale;

public class OrdersMyOrdersMenu extends BaseMenu {

    private final OrderSort sortMode;
    private final String query;

    public OrdersMyOrdersMenu(UltimateDonutSmp plugin, int ignoredPage, OrderSort sortMode) {
        this(plugin, ignoredPage, sortMode, "");
    }

    public OrdersMyOrdersMenu(UltimateDonutSmp plugin, int ignoredPage, OrderSort sortMode, String query) {
        super(plugin, plugin.getOrdersManager().getMyOrdersTitle(), 27);
        this.sortMode = sortMode == null ? plugin.getOrdersManager().getDefaultSort() : sortMode;
        this.query = query == null ? "" : query.trim();
    }

    @Override
    public void build(Player player) {
        clear();
        List<Order> orders = visibleOrders(player);
        int backSlot = OrdersMenuSupport.slot(plugin, "GUI.MY_ORDERS.BUTTONS.BACK.SLOT", 25);
        int newSlot = OrdersMenuSupport.slot(plugin, "GUI.MY_ORDERS.BUTTONS.NEW.SLOT", 26);

        int displayIndex = 0;
        for (Order order : orders) {
            while (displayIndex == backSlot || displayIndex == newSlot) {
                displayIndex++;
            }
            if (displayIndex >= inventory.getSize()) {
                break;
            }
            set(displayIndex++, OrdersMenuSupport.createOrderDisplay(
                    plugin,
                    plugin.getOrdersManager(),
                    order,
                    true
            ));
        }

        set(backSlot, OrdersMenuSupport.button(
                plugin, "GUI.MY_ORDERS.BUTTONS.BACK", "ORDERS.GUI.MY_ORDERS.BACK",
                Material.ARROW, "&cʙᴀᴄᴋ", List.of("&fʀᴇᴛᴜʀɴ ᴛᴏ ᴀᴄᴛɪᴠᴇ ᴏʀᴅᴇʀѕ")
        ));
        set(newSlot, OrdersMenuSupport.button(
                plugin, "GUI.MY_ORDERS.BUTTONS.NEW", "ORDERS.GUI.MY_ORDERS.NEW",
                Material.MAP, "&aɴᴇᴡ ᴏʀᴅᴇʀ", List.of("&fᴄʟɪᴄᴋ ᴛᴏ ᴄʀᴇᴀᴛᴇ ᴀ ɴᴇᴡ ᴏʀᴅᴇʀ")
        ));

        if (orders.isEmpty()) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    OrdersMenuSupport.text(plugin, "ORDERS.GUI.MY_ORDERS.EMPTY.NAME", "&cɴᴏ ᴏʀᴅᴇʀѕ ʏᴇᴛ"),
                    OrdersMenuSupport.list(plugin, "ORDERS.GUI.MY_ORDERS.EMPTY.LORE", List.of("&7ᴄʀᴇᴀᴛᴇ ʏᴏᴜʀ ꜰɪʀѕᴛ ʙᴜʏ ᴏʀᴅᴇʀ."))
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        int backSlot = OrdersMenuSupport.slot(plugin, "GUI.MY_ORDERS.BUTTONS.BACK.SLOT", 25);
        int newSlot = OrdersMenuSupport.slot(plugin, "GUI.MY_ORDERS.BUTTONS.NEW.SLOT", 26);
        if (slot == backSlot) {
            click(player);
            var state = plugin.getOrdersManager().getUiState(player.getUniqueId());
            new OrdersBrowseMenu(plugin, state.page() + 1, state.sort(), state.filter(), state.search()).open(player);
            return;
        }
        if (slot == newSlot) {
            click(player);
            plugin.getOrdersManager().openNewOrderMenu(player);
            return;
        }

        List<Order> orders = visibleOrders(player);
        int displayIndex = 0;
        for (Order order : orders) {
            while (displayIndex == backSlot || displayIndex == newSlot) {
                displayIndex++;
            }
            if (displayIndex == slot) {
                click(player);
                new OrdersEditMenu(plugin, order.id(), true, 1, sortMode, "ALL").open(player);
                return;
            }
            displayIndex++;
        }
    }

    private List<Order> visibleOrders(Player player) {
        List<Order> orders = plugin.getOrdersManager().getOrdersForOwner(player.getUniqueId(), sortMode);
        if (query.isBlank()) {
            return orders;
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        return orders.stream()
                .filter(order -> plugin.getOrdersManager().describeItem(order.requestedItem())
                        .toLowerCase(Locale.ROOT).contains(normalized)
                        || order.requestedMaterialKey().toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
    }

    private void click(Player player) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
    }
}
