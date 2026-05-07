package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersMyOrdersMenu extends BaseMenu {

    private final int page;
    private final OrderSort sortMode;

    public OrdersMyOrdersMenu(UltimateDonutSmp plugin, int page, OrderSort sortMode) {
        super(plugin, plugin.getOrdersManager().getMyOrdersTitle(), plugin.getOrdersManager().getMyOrdersSize());
        this.page = Math.max(1, page);
        this.sortMode = sortMode == null ? plugin.getOrdersManager().getDefaultSort() : sortMode;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<Order> orders = plugin.getOrdersManager().getOrdersForOwner(player.getUniqueId(), sortMode);
        int itemsPerPage = plugin.getOrdersManager().getMyOrdersItemsPerPage();
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(orders.size(), startIndex + itemsPerPage);

        for (int slot = 0; slot < itemsPerPage && slot < inventory.getSize() - 9; slot++) {
            int orderIndex = startIndex + slot;
            if (orderIndex >= endIndex) {
                break;
            }

            set(slot, OrdersMenuSupport.createOrderDisplay(
                    plugin,
                    plugin.getOrdersManager(),
                    orders.get(orderIndex),
                    true
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&bBack to Board", List.of("&7Return to active orders")));
        set(lastRow + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aPrevious Page", List.of("&7Go to page &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 2, ItemUtils.createItem(
                Material.HOPPER,
                "&aSort: &f" + sortMode.displayName(),
                List.of("&7Click to cycle sorting mode")
        ));
        set(lastRow + 3, ItemUtils.createItem(Material.CLOCK, "&eRefresh", List.of("&7Reload your orders")));
        set(lastRow + 4, ItemUtils.createItem(Material.EMERALD, "&aNew Order", List.of("&7Create a new buy order")));
        set(lastRow + 5, ItemUtils.createItem(Material.ENDER_CHEST, "&dCollect", List.of("&7Collect delivered items and refunds")));
        set(lastRow + 7, hasNextPage(orders.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aNext Page", List.of("&7Go to page &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cClose", List.of("&7Close Orders")));

        if (orders.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo Orders Yet",
                    List.of("&7Create your first buy order from the board.")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        int lastRow = inventory.getSize() - 9;
        List<Order> orders = plugin.getOrdersManager().getOrdersForOwner(player.getUniqueId(), sortMode);
        int itemsPerPage = plugin.getOrdersManager().getMyOrdersItemsPerPage();

        if (slot == lastRow) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersBrowseMenu(plugin, 1, plugin.getOrdersManager().getDefaultSort(), "ALL").open(player);
            return;
        }
        if (slot == lastRow + 1) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersMyOrdersMenu(plugin, page - 1, sortMode).open(player);
            }
            return;
        }
        if (slot == lastRow + 2) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersMyOrdersMenu(plugin, 1, nextSort(sortMode)).open(player);
            return;
        }
        if (slot == lastRow + 3) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersMyOrdersMenu(plugin, page, sortMode).open(player);
            return;
        }
        if (slot == lastRow + 4) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersSelectItemMenu(plugin, 1, "ALL").open(player);
            return;
        }
        if (slot == lastRow + 5) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersCollectMenu(plugin, 1).open(player);
            return;
        }
        if (slot == lastRow + 7) {
            if (hasNextPage(orders.size(), itemsPerPage)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersMyOrdersMenu(plugin, page + 1, sortMode).open(player);
            }
            return;
        }
        if (slot == lastRow + 8) {
            player.closeInventory();
            return;
        }

        if (slot < 0 || slot >= itemsPerPage) {
            return;
        }

        int orderIndex = ((page - 1) * itemsPerPage) + slot;
        if (orderIndex >= orders.size()) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        new OrdersEditMenu(plugin, orders.get(orderIndex).id(), true, page, sortMode, "ALL").open(player);
    }

    private int getTotalPages(int totalItems, int itemsPerPage) {
        return Math.max(1, (int) Math.ceil(totalItems / (double) itemsPerPage));
    }

    private boolean hasNextPage(int totalItems, int itemsPerPage) {
        return page < getTotalPages(totalItems, itemsPerPage);
    }

    private OrderSort nextSort(OrderSort current) {
        List<OrderSort> sorts = plugin.getOrdersManager().getAllowedSorts();
        int index = sorts.indexOf(current);
        if (index < 0) {
            return plugin.getOrdersManager().getDefaultSort();
        }
        return sorts.get((index + 1) % sorts.size());
    }
}
