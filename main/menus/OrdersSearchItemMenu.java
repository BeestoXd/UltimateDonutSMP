package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.OrderAlphaSort;
import com.bx.ultimateDonutSmp.models.OrderCatalogEntry;
import com.bx.ultimateDonutSmp.models.OrderUiState;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersSearchItemMenu extends BaseMenu {

    private final String query;
    private final int page;
    private final long editOrderId;
    private final OrdersManager.OrderEditNavigation editNavigation;

    public OrdersSearchItemMenu(UltimateDonutSmp plugin, String query, int page) {
        this(plugin, query, page, 0L, null);
    }

    public OrdersSearchItemMenu(
            UltimateDonutSmp plugin,
            String query,
            int page,
            long editOrderId,
            OrdersManager.OrderEditNavigation editNavigation
    ) {
        super(plugin, plugin.getOrdersManager().getSearchItemTitle(sanitizeQuery(query)),
                plugin.getOrdersManager().getSearchItemSize());
        this.query = sanitizeQuery(query);
        this.page = Math.max(1, page);
        this.editOrderId = editOrderId;
        this.editNavigation = editNavigation;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        OrderUiState state = plugin.getOrdersManager().getUiState(player.getUniqueId());
        state.itemSearch(query);
        state.itemPage(page - 1);
        List<OrderCatalogEntry> results = results(state);
        int itemsPerPage = plugin.getOrdersManager().getSearchItemItemsPerPage();
        int start = (page - 1) * itemsPerPage;
        int end = Math.min(results.size(), start + itemsPerPage);

        for (int slot = 0; slot < itemsPerPage && start + slot < end; slot++) {
            OrderCatalogEntry entry = results.get(start + slot);
            set(slot, OrdersMenuSupport.decorateItem(
                    plugin,
                    entry.createPreviewItem(),
                    "&b" + entry.displayName(),
                    List.of(
                            "&7" + OrdersMenuSupport.text(plugin, "ORDERS.GUI.ITEM.CATEGORY", "Category")
                                    + ": &f" + plugin.getOrdersManager().prettifyCategory(entry.categoryKey()),
                            "",
                            OrdersMenuSupport.text(plugin, "ORDERS.GUI.ITEM.SELECT", "&eᴄʟɪᴄᴋ ᴛᴏ ѕᴇʟᴇᴄᴛ")
                    ),
                    true
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, ItemUtils.createItem(Material.COMPASS,
                OrdersMenuSupport.text(plugin, "ORDERS.GUI.BACK.NAME", "&bʙᴀᴄᴋ"),
                List.of(OrdersMenuSupport.text(plugin, "ORDERS.GUI.BACK.LORE", "&7ʀᴇᴛᴜʀɴ"))));
        set(lastRow + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ", List.of("&7ᴘᴀɢᴇ " + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 2, ItemUtils.createItem(Material.SPYGLASS, "&bɴᴇᴡ ѕᴇᴀʀᴄʜ",
                List.of("&7ѕᴇᴀʀᴄʜ ᴀɴᴏᴛʜᴇʀ ɪᴛᴇᴍ ᴏʀ ᴄᴀᴛᴇɢᴏʀʏ")));
        set(lastRow + 3, ItemUtils.createItem(Material.HOPPER,
                "&eѕᴏʀᴛ: &f" + state.itemSort().name().replace('_', '-'),
                List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ ᴀ-ᴢ / ᴢ-ᴀ")));
        set(lastRow + 5, ItemUtils.createItem(Material.BOOK,
                "&eᴘᴀɢᴇ " + page + "&7/&e" + totalPages(results.size(), itemsPerPage),
                List.of("&7ᴍᴀᴛᴄʜᴇѕ: &f" + results.size(), "&7ǫᴜᴇʀʏ: &f" + query)));
        set(lastRow + 7, page < totalPages(results.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ", List.of("&7ᴘᴀɢᴇ " + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));

        if (results.isEmpty()) {
            set(Math.min(itemsPerPage - 1, itemsPerPage / 2),
                    ItemUtils.createItem(Material.BARRIER, "&cɴᴏ ʀᴇѕᴜʟᴛѕ",
                            List.of("&7ɴᴏ ᴄᴀᴛᴀʟᴏɢ ɪᴛᴇᴍѕ ᴍᴀᴛᴄʜᴇᴅ &f" + query + "&7.")));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        OrderUiState state = plugin.getOrdersManager().getUiState(player.getUniqueId());
        List<OrderCatalogEntry> results = results(state);
        int itemsPerPage = plugin.getOrdersManager().getSearchItemItemsPerPage();
        int lastRow = inventory.getSize() - 9;

        if (slot == lastRow) {
            if (isEditMode()) {
                openEditMenu(player);
            } else {
                new OrdersSelectItemMenu(plugin, 1, "ALL").open(player);
            }
            return;
        }
        if (slot == lastRow + 1 && page > 1) {
            openPage(player, page - 1);
            return;
        }
        if (slot == lastRow + 2) {
            if (isEditMode()) {
                plugin.getOrdersManager().promptEditOrderSearchInput(player, editOrderId, editNavigation);
            } else {
                plugin.getOrdersManager().promptOrderSearchInput(player);
            }
            return;
        }
        if (slot == lastRow + 3) {
            state.itemSort(state.itemSort() == OrderAlphaSort.A_Z ? OrderAlphaSort.Z_A : OrderAlphaSort.A_Z);
            plugin.getOrdersManager().saveUiState(state);
            openPage(player, 1);
            return;
        }
        if (slot == lastRow + 7 && page < totalPages(results.size(), itemsPerPage)) {
            openPage(player, page + 1);
            return;
        }
        if (slot < 0 || slot >= itemsPerPage) {
            return;
        }

        int index = ((page - 1) * itemsPerPage) + slot;
        if (index >= results.size()) {
            return;
        }
        OrderCatalogEntry selected = results.get(index);
        boolean accepted = plugin.getOrdersManager().selectOrderItem(
                player, selected.createPreviewItem(), selected.categoryKey(), editOrderId, editNavigation
        );
        SoundUtils.play(player, plugin.getConfigManager().getSound(
                accepted ? "MENUS.BUTTON-CLICK" : "ORDERS.FAIL"
        ));
    }

    private List<OrderCatalogEntry> results(OrderUiState state) {
        return plugin.getOrdersManager().getCatalogEntries("ALL", state.itemSort(), query);
    }

    private void openPage(Player player, int targetPage) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
        new OrdersSearchItemMenu(plugin, query, targetPage, editOrderId, editNavigation).open(player);
    }

    private boolean isEditMode() {
        return editOrderId > 0L;
    }

    private void openEditMenu(Player player) {
        new OrdersEditMenu(
                plugin,
                editOrderId,
                editNavigation != null && editNavigation.backToMyOrders(),
                editNavigation == null ? 1 : editNavigation.originPage(),
                editNavigation == null ? plugin.getOrdersManager().getDefaultSort() : editNavigation.sortMode(),
                editNavigation == null ? "ALL" : editNavigation.categoryFilter()
        ).open(player);
    }

    private int totalPages(int total, int perPage) {
        return Math.max(1, (int) Math.ceil(total / (double) perPage));
    }

    private static String sanitizeQuery(String query) {
        return query == null ? "" : query.replace('&', ' ').trim();
    }
}
