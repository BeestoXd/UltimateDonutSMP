package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.OrderCatalogEntry;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersSelectItemMenu extends BaseMenu {

    private final int page;
    private final String categoryFilter;
    private final long editOrderId;
    private final OrdersManager.OrderEditNavigation editNavigation;

    public OrdersSelectItemMenu(UltimateDonutSmp plugin, int page, String categoryFilter) {
        super(plugin, plugin.getOrdersManager().getSelectItemTitle(), plugin.getOrdersManager().getSelectItemSize());
        this.page = Math.max(1, page);
        this.categoryFilter = plugin.getOrdersManager().normalizeSelectItemCategory(categoryFilter);
        this.editOrderId = 0L;
        this.editNavigation = null;
    }

    public OrdersSelectItemMenu(
            UltimateDonutSmp plugin,
            int page,
            String categoryFilter,
            long editOrderId,
            OrdersManager.OrderEditNavigation editNavigation
    ) {
        super(plugin, plugin.getOrdersManager().getSelectItemTitle(), plugin.getOrdersManager().getSelectItemSize());
        this.page = Math.max(1, page);
        this.categoryFilter = plugin.getOrdersManager().normalizeSelectItemCategory(categoryFilter);
        this.editOrderId = editOrderId;
        this.editNavigation = editNavigation;
    }

    @Override
    public void build(Player player) {
        if (plugin.getOrdersManager().getSelectItemSource() == OrdersManager.SelectItemSource.SERVER_MATERIALS) {
            buildServerMaterialCatalog();
            return;
        }

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
                            "&7ᴄᴀᴛᴇɢᴏʀʏ: &f" + plugin.getOrdersManager().prettifyCategory(entry.categoryKey()),
                            "",
                            "&eᴄʟɪᴄᴋ ᴛᴏ ѕᴇʟᴇᴄᴛ"
                    )
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&bʙᴀᴄᴋ ᴛᴏ ᴏʀᴅᴇʀѕ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴛʜᴇ ᴏʀᴅᴇʀ ʙᴏᴀʀᴅ")));
        set(lastRow + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 2, ItemUtils.createItem(
                Material.CHEST,
                "&bꜰɪʟᴛᴇʀ: &f" + plugin.getOrdersManager().prettifyCategory(categoryFilter),
                List.of("&7ᴄʟɪᴄᴋ ᴛᴏ ᴄʏᴄʟᴇ ᴄᴀᴛᴇɢᴏʀʏ ꜰɪʟᴛᴇʀ")
        ));
        set(lastRow + 3, ItemUtils.createItem(Material.CLOCK, "&eʀᴇꜰʀᴇѕʜ", List.of("&7ʀᴇʟᴏᴀᴅ ᴛʜᴇ ɪᴛᴇᴍ ᴄᴀᴛᴀʟᴏɢ")));
        set(lastRow + 5, ItemUtils.createItem(
                Material.BOOK,
                "&eᴘᴀɢᴇ " + page + "&7/&e" + getTotalPages(entries.size(), itemsPerPage),
                List.of("&7ᴀᴠᴀɪʟᴀʙʟᴇ ɪᴛᴇᴍѕ: &f" + entries.size())
        ));
        set(lastRow + 7, hasNextPage(entries.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ", List.of("&7ᴄʟᴏѕᴇ ᴏʀᴅᴇʀѕ")));

        if (entries.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cɴᴏ ᴀᴠᴀɪʟᴀʙʟᴇ ɪᴛᴇᴍѕ",
                    List.of("&7ᴛʜɪѕ ᴄᴀᴛᴇɢᴏʀʏ ᴅᴏᴇѕ ɴᴏᴛ ʜᴀᴠᴇ ᴀɴʏ ᴏʀᴅᴇʀᴀʙʟᴇ ɪᴛᴇᴍѕ.")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (plugin.getOrdersManager().getSelectItemSource() == OrdersManager.SelectItemSource.SERVER_MATERIALS) {
            handleServerMaterialCatalogClick(slot, player);
            return;
        }

        int lastRow = inventory.getSize() - 9;
        List<OrderCatalogEntry> entries = plugin.getOrdersManager().getCatalogEntries(categoryFilter);
        int itemsPerPage = plugin.getOrdersManager().getSelectItemItemsPerPage();

        if (slot == lastRow) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (isEditMode()) {
                openEditMenu(player);
            } else {
                new OrdersBrowseMenu(plugin, 1, plugin.getOrdersManager().getDefaultSort(), "ALL").open(player);
            }
            return;
        }
        if (slot == lastRow + 1) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersSelectItemMenu(plugin, page - 1, categoryFilter, editOrderId, editNavigation).open(player);
            }
            return;
        }
        if (slot == lastRow + 2) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersSelectItemMenu(plugin, 1, plugin.getOrdersManager().nextSelectItemCategory(categoryFilter), editOrderId, editNavigation).open(player);
            return;
        }
        if (slot == lastRow + 3) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersSelectItemMenu(plugin, page, categoryFilter, editOrderId, editNavigation).open(player);
            return;
        }
        if (slot == lastRow + 7) {
            if (hasNextPage(entries.size(), itemsPerPage)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersSelectItemMenu(plugin, page + 1, categoryFilter, editOrderId, editNavigation).open(player);
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
        OrderCatalogEntry entry = entries.get(entryIndex);
        boolean selected = plugin.getOrdersManager().selectOrderItem(
                player,
                entry.createPreviewItem(),
                entry.categoryKey(),
                editOrderId,
                editNavigation
        );
        if (!selected) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.FAIL"));
        }
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

    private void buildServerMaterialCatalog() {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        OrdersManager manager = plugin.getOrdersManager();
        List<String> categories = manager.getSelectItemCategories();
        for (int index = 0; index < categories.size() && index < 9; index++) {
            String category = categories.get(index);
            boolean selected = category.equals(categoryFilter);
            set(index, ItemUtils.createItem(
                    manager.getSelectItemCategoryIcon(category),
                    (selected ? "&a" : "&b") + manager.prettifySelectItemCategory(category),
                    List.of(
                            selected ? "&7ᴄᴜʀʀᴇɴᴛ ᴄᴀᴛᴇɢᴏʀʏ" : "&7ᴄʟɪᴄᴋ ᴛᴏ ᴏᴘᴇɴ ᴛʜɪѕ ᴄᴀᴛᴇɢᴏʀʏ",
                            "&7ѕᴏᴜʀᴄᴇ: &fѕᴇʀᴠᴇʀ ᴍᴀᴛᴇʀɪᴀʟѕ"
                    )
            ));
        }

        List<OrderCatalogEntry> entries = manager.getSelectItemCatalogEntries(categoryFilter);
        int itemsPerPage = getServerMaterialsItemsPerPage();
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(entries.size(), startIndex + itemsPerPage);
        int slot = getServerMaterialsFirstResultSlot();
        int lastRow = inventory.getSize() - 9;

        for (int entryIndex = startIndex; entryIndex < endIndex && slot < lastRow; entryIndex++, slot++) {
            OrderCatalogEntry entry = entries.get(entryIndex);
            set(slot, ItemUtils.createItem(
                            entry.material(),
                            "&b" + manager.describeMaterial(entry.material()),
                            List.of(
                            "&7ᴄᴀᴛᴇɢᴏʀʏ: &f" + manager.prettifySelectItemCategory(categoryFilter),
                            "&7ѕᴏᴜʀᴄᴇ: &fѕᴇʀᴠᴇʀ.ᴊᴀʀ",
                            "",
                            "&eᴄʟɪᴄᴋ ᴛᴏ ѕᴇʟᴇᴄᴛ"
                    )
            ));
        }

        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&bʙᴀᴄᴋ ᴛᴏ ᴏʀᴅᴇʀѕ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴛʜᴇ ᴏʀᴅᴇʀ ʙᴏᴀʀᴅ")));
        set(lastRow + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 3, ItemUtils.createItem(Material.CLOCK, "&eʀᴇꜰʀᴇѕʜ", List.of("&7ʀᴇʟᴏᴀᴅ ѕᴇʀᴠᴇʀ ᴍᴀᴛᴇʀɪᴀʟѕ")));
        set(lastRow + 5, ItemUtils.createItem(
                Material.BOOK,
                "&eᴘᴀɢᴇ " + page + "&7/&e" + getTotalPages(entries.size(), itemsPerPage),
                List.of(
                        "&7ᴄᴀᴛᴇɢᴏʀʏ: &f" + manager.prettifySelectItemCategory(categoryFilter),
                        "&7ᴀᴠᴀɪʟᴀʙʟᴇ ɪᴛᴇᴍѕ: &f" + entries.size()
                )
        ));
        set(lastRow + 7, hasNextPage(entries.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ", List.of("&7ᴄʟᴏѕᴇ ᴏʀᴅᴇʀѕ")));

        if (entries.isEmpty()) {
            set(Math.min(lastRow - 1, getServerMaterialsFirstResultSlot() + Math.max(0, getServerMaterialsItemsPerPage() / 2)), ItemUtils.createItem(
                    Material.BARRIER,
                    "&cɴᴏ ᴀᴠᴀɪʟᴀʙʟᴇ ɪᴛᴇᴍѕ",
                    List.of("&7ᴛʜɪѕ ᴄᴀᴛᴇɢᴏʀʏ ʜᴀѕ ɴᴏ ᴏʀᴅᴇʀᴀʙʟᴇ ѕᴇʀᴠᴇʀ ᴍᴀᴛᴇʀɪᴀʟѕ.")
            ));
        }
    }

    private void handleServerMaterialCatalogClick(int slot, Player player) {
        OrdersManager manager = plugin.getOrdersManager();
        List<String> categories = manager.getSelectItemCategories();
        if (slot >= 0 && slot < categories.size() && slot < 9) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersSelectItemMenu(plugin, 1, categories.get(slot), editOrderId, editNavigation).open(player);
            return;
        }

        int lastRow = inventory.getSize() - 9;
        List<OrderCatalogEntry> entries = manager.getSelectItemCatalogEntries(categoryFilter);
        int itemsPerPage = getServerMaterialsItemsPerPage();

        if (slot == lastRow) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (isEditMode()) {
                openEditMenu(player);
            } else {
                new OrdersBrowseMenu(plugin, 1, manager.getDefaultSort(), "ALL").open(player);
            }
            return;
        }
        if (slot == lastRow + 1) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersSelectItemMenu(plugin, page - 1, categoryFilter, editOrderId, editNavigation).open(player);
            }
            return;
        }
        if (slot == lastRow + 3) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersSelectItemMenu(plugin, page, categoryFilter, editOrderId, editNavigation).open(player);
            return;
        }
        if (slot == lastRow + 7) {
            if (hasNextPage(entries.size(), itemsPerPage)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersSelectItemMenu(plugin, page + 1, categoryFilter, editOrderId, editNavigation).open(player);
            }
            return;
        }
        if (slot == lastRow + 8) {
            player.closeInventory();
            return;
        }

        int firstResultSlot = getServerMaterialsFirstResultSlot();
        if (slot < firstResultSlot || slot >= lastRow) {
            return;
        }

        int entryIndex = ((page - 1) * itemsPerPage) + (slot - firstResultSlot);
        if (entryIndex >= entries.size()) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        OrderCatalogEntry entry = entries.get(entryIndex);
        boolean selected = manager.selectOrderItem(
                player,
                entry.createPreviewItem(),
                entry.categoryKey(),
                editOrderId,
                editNavigation
        );
        if (!selected) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.FAIL"));
        }
    }

    private int getServerMaterialsFirstResultSlot() {
        return Math.min(9, inventory.getSize());
    }

    private int getServerMaterialsItemsPerPage() {
        int resultSlots = Math.max(1, (inventory.getSize() - 9) - getServerMaterialsFirstResultSlot());
        return Math.max(1, Math.min(resultSlots, plugin.getOrdersManager().getSelectItemItemsPerPage()));
    }

    private int getTotalPages(int totalItems, int itemsPerPage) {
        return Math.max(1, (int) Math.ceil(totalItems / (double) itemsPerPage));
    }

    private boolean hasNextPage(int totalItems, int itemsPerPage) {
        return page < getTotalPages(totalItems, itemsPerPage);
    }
}
