package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.OrderCatalogEntry;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersSelectItemMenu extends BaseMenu {

    private final int page;
    private final String categoryFilter;

    public OrdersSelectItemMenu(UltimateDonutSmp plugin, int page, String categoryFilter) {
        super(plugin, plugin.getOrdersManager().getSelectItemTitle(), plugin.getOrdersManager().getSelectItemSize());
        this.page = Math.max(1, page);
        this.categoryFilter = plugin.getOrdersManager().normalizeCategory(categoryFilter);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<OrderCatalogEntry> entries = plugin.getOrdersManager().getCatalogEntries(categoryFilter);
        int itemsPerPage = plugin.getOrdersManager().getSelectItemItemsPerPage();
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(entries.size(), startIndex + itemsPerPage);

        for (int slot = 0; slot < itemsPerPage && slot < inventory.getSize() - 9; slot++) {
            int entryIndex = startIndex + slot;
            if (entryIndex >= endIndex) {
                break;
            }

            OrderCatalogEntry entry = entries.get(entryIndex);
            set(slot, ItemUtils.createItem(
                    entry.material(),
                    "&b" + plugin.getOrdersManager().describeMaterial(entry.material()),
                    List.of(
                            "&7Category: &f" + plugin.getOrdersManager().prettifyCategory(entry.categoryKey()),
                            "",
                            "&eClick to select"
                    )
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&bBack to Orders", List.of("&7Return to the order board")));
        set(lastRow + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aPrevious Page", List.of("&7Go to page &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 2, ItemUtils.createItem(
                Material.CHEST,
                "&bFilter: &f" + plugin.getOrdersManager().prettifyCategory(categoryFilter),
                List.of("&7Click to cycle category filter")
        ));
        set(lastRow + 3, ItemUtils.createItem(Material.CLOCK, "&eRefresh", List.of("&7Reload the item catalog")));
        set(lastRow + 5, ItemUtils.createItem(
                Material.BOOK,
                "&ePage " + page + "&7/&e" + getTotalPages(entries.size(), itemsPerPage),
                List.of("&7Available items: &f" + entries.size())
        ));
        set(lastRow + 7, hasNextPage(entries.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aNext Page", List.of("&7Go to page &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cClose", List.of("&7Close Orders")));

        if (entries.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo Available Items",
                    List.of("&7This category does not have any orderable items.")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        int lastRow = inventory.getSize() - 9;
        List<OrderCatalogEntry> entries = plugin.getOrdersManager().getCatalogEntries(categoryFilter);
        int itemsPerPage = plugin.getOrdersManager().getSelectItemItemsPerPage();

        if (slot == lastRow) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersBrowseMenu(plugin, 1, plugin.getOrdersManager().getDefaultSort(), "ALL").open(player);
            return;
        }
        if (slot == lastRow + 1) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersSelectItemMenu(plugin, page - 1, categoryFilter).open(player);
            }
            return;
        }
        if (slot == lastRow + 2) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersSelectItemMenu(plugin, 1, plugin.getOrdersManager().nextCategory(categoryFilter)).open(player);
            return;
        }
        if (slot == lastRow + 3) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersSelectItemMenu(plugin, page, categoryFilter).open(player);
            return;
        }
        if (slot == lastRow + 7) {
            if (hasNextPage(entries.size(), itemsPerPage)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersSelectItemMenu(plugin, page + 1, categoryFilter).open(player);
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

        int entryIndex = ((page - 1) * itemsPerPage) + slot;
        if (entryIndex >= entries.size()) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        plugin.getOrdersManager().promptOrderQuantityInput(player, entries.get(entryIndex));
    }

    private int getTotalPages(int totalItems, int itemsPerPage) {
        return Math.max(1, (int) Math.ceil(totalItems / (double) itemsPerPage));
    }

    private boolean hasNextPage(int totalItems, int itemsPerPage) {
        return page < getTotalPages(totalItems, itemsPerPage);
    }
}
