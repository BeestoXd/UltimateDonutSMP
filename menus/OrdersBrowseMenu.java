package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersBrowseMenu extends BaseMenu {

    private final int page;
    private final OrderSort sortMode;
    private final String categoryFilter;

    public OrdersBrowseMenu(UltimateDonutSmp plugin, int page, OrderSort sortMode, String categoryFilter) {
        super(plugin, plugin.getOrdersManager().getBrowseTitle(), plugin.getOrdersManager().getBrowseSize());
        this.page = Math.max(1, page);
        this.sortMode = sortMode == null ? plugin.getOrdersManager().getDefaultSort() : sortMode;
        this.categoryFilter = plugin.getOrdersManager().normalizeCategory(categoryFilter);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<Order> orders = plugin.getOrdersManager().getActiveOrders(sortMode, categoryFilter);
        int itemsPerPage = plugin.getOrdersManager().getBrowseItemsPerPage();
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(orders.size(), startIndex + itemsPerPage);

        for (int slot = 0; slot < itemsPerPage && slot < inventory.getSize() - 9; slot++) {
            int orderIndex = startIndex + slot;
            if (orderIndex >= endIndex) {
                break;
            }

            Order order = orders.get(orderIndex);
            set(slot, OrdersMenuSupport.createOrderDisplay(
                    plugin,
                    plugin.getOrdersManager(),
                    order,
                    order.ownerUuid().equals(player.getUniqueId())
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aPrevious Page", List.of("&7Go to page &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 1, ItemUtils.createItem(Material.CLOCK, "&eRefresh", List.of("&7Reload active orders")));
        set(lastRow + 2, ItemUtils.createItem(
                Material.HOPPER,
                "&aSort: &f" + sortMode.displayName(),
                List.of("&7Click to cycle sorting mode")
        ));
        set(lastRow + 3, ItemUtils.createItem(
                Material.CHEST,
                "&bFilter: &f" + plugin.getOrdersManager().prettifyCategory(categoryFilter),
                List.of("&7Click to cycle category filter")
        ));
        set(lastRow + 4, ItemUtils.createItem(Material.EMERALD, "&aNew Order", List.of("&7Create a new buy order")));
        set(lastRow + 5, ItemUtils.createItem(Material.WRITABLE_BOOK, "&bMy Orders", List.of("&7View your orders")));
        set(lastRow + 6, ItemUtils.createItem(Material.ENDER_CHEST, "&dCollect", List.of("&7Collect delivered items and refunds")));
        set(lastRow + 7, hasNextPage(orders.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aNext Page", List.of("&7Go to page &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cClose", List.of("&7Close Orders")));

        if (orders.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo Active Orders",
                    List.of("&7Create one with the &aNew Order &7button.")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        int lastRow = inventory.getSize() - 9;
        List<Order> orders = plugin.getOrdersManager().getActiveOrders(sortMode, categoryFilter);
        int itemsPerPage = plugin.getOrdersManager().getBrowseItemsPerPage();

        if (slot == lastRow) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersBrowseMenu(plugin, page - 1, sortMode, categoryFilter).open(player);
            }
            return;
        }
        if (slot == lastRow + 1) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersBrowseMenu(plugin, page, sortMode, categoryFilter).open(player);
            return;
        }
        if (slot == lastRow + 2) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersBrowseMenu(plugin, 1, nextSort(sortMode), categoryFilter).open(player);
            return;
        }
        if (slot == lastRow + 3) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersBrowseMenu(plugin, 1, sortMode, plugin.getOrdersManager().nextCategory(categoryFilter)).open(player);
            return;
        }
        if (slot == lastRow + 4) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersSelectItemMenu(plugin, 1, "ALL").open(player);
            return;
        }
        if (slot == lastRow + 5) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersMyOrdersMenu(plugin, 1, sortMode).open(player);
            return;
        }
        if (slot == lastRow + 6) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersCollectMenu(plugin, 1).open(player);
            return;
        }
        if (slot == lastRow + 7) {
            if (hasNextPage(orders.size(), itemsPerPage)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersBrowseMenu(plugin, page + 1, sortMode, categoryFilter).open(player);
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
        new OrdersEditMenu(plugin, orders.get(orderIndex).id(), false, page, sortMode, categoryFilter).open(player);
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
