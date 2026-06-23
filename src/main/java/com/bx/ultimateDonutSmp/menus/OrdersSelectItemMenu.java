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

import java.util.Comparator;
import java.util.List;

public class OrdersSelectItemMenu extends BaseMenu {

    private final int page;
    private final String categoryFilter;
    private final long editOrderId;
    private final OrdersManager.OrderEditNavigation editNavigation;

    public OrdersSelectItemMenu(UltimateDonutSmp plugin, int page, String categoryFilter) {
        this(plugin, page, categoryFilter, 0L, null);
    }

    public OrdersSelectItemMenu(
            UltimateDonutSmp plugin,
            int page,
            String categoryFilter,
            long editOrderId,
            OrdersManager.OrderEditNavigation editNavigation
    ) {
        super(plugin, plugin.getOrdersManager().getSelectItemTitle(),
                plugin.getOrdersManager().getSelectItemSize());
        this.page = Math.max(1, page);
        this.categoryFilter = plugin.getOrdersManager().normalizeSelectItemCategory(categoryFilter);
        this.editOrderId = editOrderId;
        this.editNavigation = editNavigation;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        OrderUiState state = plugin.getOrdersManager().getUiState(player.getUniqueId());
        state.itemPage(page - 1);
        List<OrderCatalogEntry> entries = entries(state);
        int itemsPerPage = plugin.getOrdersManager().getSelectItemItemsPerPage();
        int start = (page - 1) * itemsPerPage;
        int end = Math.min(entries.size(), start + itemsPerPage);

        for (int slot = 0; slot < itemsPerPage && start + slot < end; slot++) {
            OrderCatalogEntry entry = entries.get(start + slot);
            set(slot, OrdersMenuSupport.decorateItem(
                    plugin,
                    entry.createPreviewItem(),
                    "&b" + entry.displayName(),
                    List.of(
                            "&7ᴄᴀᴛᴇɢᴏʀʏ: &f" + plugin.getOrdersManager().prettifyCategory(entry.categoryKey()),
                            "",
                            OrdersMenuSupport.text(plugin, "ORDERS.GUI.ITEM.SELECT", "&eᴄʟɪᴄᴋ ᴛᴏ ѕᴇʟᴇᴄᴛ")
                    ),
                    true
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&bʙᴀᴄᴋ", List.of("&7ʀᴇᴛᴜʀɴ")));
        set(lastRow + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ", List.of("&7ᴘᴀɢᴇ " + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 2, ItemUtils.createItem(Material.CHEST,
                "&bꜰɪʟᴛᴇʀ: &f" + plugin.getOrdersManager().prettifyCategory(categoryFilter),
                List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ᴄʏᴄʟᴇ ᴄᴀᴛᴇɢᴏʀʏ")));
        set(lastRow + 3, ItemUtils.createItem(Material.HOPPER,
                "&eѕᴏʀᴛ: &f" + state.itemSort().name().replace('_', '-'),
                List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ ᴀ-ᴢ / ᴢ-ᴀ")));
        set(lastRow + 4, ItemUtils.createItem(Material.SPYGLASS, "&bѕᴇᴀʀᴄʜ",
                List.of("&7ѕᴇᴀʀᴄʜ ɪᴛᴇᴍ ɴᴀᴍᴇѕ ᴀɴᴅ ᴄᴀᴛᴇɢᴏʀɪᴇѕ")));
        set(lastRow + 5, ItemUtils.createItem(Material.BOOK,
                "&eᴘᴀɢᴇ " + page + "&7/&e" + totalPages(entries.size(), itemsPerPage),
                List.of("&7ᴀᴠᴀɪʟᴀʙʟᴇ ɪᴛᴇᴍѕ: &f" + entries.size())));
        set(lastRow + 7, page < totalPages(entries.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ", List.of("&7ᴘᴀɢᴇ " + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));

        if (entries.isEmpty()) {
            set(Math.min(itemsPerPage - 1, itemsPerPage / 2),
                    ItemUtils.createItem(Material.BARRIER, "&cɴᴏ ᴀᴠᴀɪʟᴀʙʟᴇ ɪᴛᴇᴍѕ",
                            List.of("&7ɴᴏ ᴏʀᴅᴇʀᴀʙʟᴇ ɪᴛᴇᴍѕ ᴇxɪѕᴛ ɪɴ ᴛʜɪѕ ᴄᴀᴛᴇɢᴏʀʏ.")));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        OrderUiState state = plugin.getOrdersManager().getUiState(player.getUniqueId());
        List<OrderCatalogEntry> entries = entries(state);
        int itemsPerPage = plugin.getOrdersManager().getSelectItemItemsPerPage();
        int lastRow = inventory.getSize() - 9;

        if (slot == lastRow) {
            if (isEditMode()) {
                openEditMenu(player);
            } else {
                new OrdersNewMenu(plugin).open(player);
            }
            return;
        }
        if (slot == lastRow + 1 && page > 1) {
            openPage(player, page - 1, categoryFilter);
            return;
        }
        if (slot == lastRow + 2) {
            openPage(player, 1, plugin.getOrdersManager().nextSelectItemCategory(categoryFilter));
            return;
        }
        if (slot == lastRow + 3) {
            state.itemSort(state.itemSort() == OrderAlphaSort.A_Z ? OrderAlphaSort.Z_A : OrderAlphaSort.A_Z);
            plugin.getOrdersManager().saveUiState(state);
            openPage(player, 1, categoryFilter);
            return;
        }
        if (slot == lastRow + 4) {
            if (isEditMode()) {
                plugin.getOrdersManager().promptEditOrderSearchInput(player, editOrderId, editNavigation);
            } else {
                plugin.getOrdersManager().promptOrderSearchInput(player);
            }
            return;
        }
        if (slot == lastRow + 7 && page < totalPages(entries.size(), itemsPerPage)) {
            openPage(player, page + 1, categoryFilter);
            return;
        }
        if (slot < 0 || slot >= itemsPerPage) {
            return;
        }

        int index = ((page - 1) * itemsPerPage) + slot;
        if (index >= entries.size()) {
            return;
        }
        OrderCatalogEntry entry = entries.get(index);
        boolean selected = plugin.getOrdersManager().selectOrderItem(
                player, entry.createPreviewItem(), entry.categoryKey(), editOrderId, editNavigation
        );
        SoundUtils.play(player, plugin.getConfigManager().getSound(
                selected ? "MENUS.BUTTON-CLICK" : "ORDERS.FAIL"
        ));
    }

    private List<OrderCatalogEntry> entries(OrderUiState state) {
        OrdersManager manager = plugin.getOrdersManager();
        if (manager.getSelectItemSource() != OrdersManager.SelectItemSource.SERVER_MATERIALS) {
            return manager.getCatalogEntries(categoryFilter, state.itemSort(), "");
        }
        Comparator<OrderCatalogEntry> comparator = Comparator.comparing(
                OrderCatalogEntry::displayName, String.CASE_INSENSITIVE_ORDER
        );
        if (state.itemSort() == OrderAlphaSort.Z_A) {
            comparator = comparator.reversed();
        }
        return manager.getSelectItemCatalogEntries(categoryFilter).stream().sorted(comparator).toList();
    }

    private void openPage(Player player, int targetPage, String targetCategory) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
        new OrdersSelectItemMenu(
                plugin, targetPage, targetCategory, editOrderId, editNavigation
        ).open(player);
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
}
