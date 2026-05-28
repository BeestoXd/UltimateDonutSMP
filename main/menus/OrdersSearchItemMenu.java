package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class OrdersSearchItemMenu extends BaseMenu {

    private final String query;
    private final int page;
    private final long editOrderId;
    private final OrdersManager.OrderEditNavigation editNavigation;

    public OrdersSearchItemMenu(UltimateDonutSmp plugin, String query, int page) {
        super(plugin, plugin.getOrdersManager().getSearchItemTitle(sanitizeQuery(query)), plugin.getOrdersManager().getSearchItemSize());
        this.query = sanitizeQuery(query);
        this.page = Math.max(1, page);
        this.editOrderId = 0L;
        this.editNavigation = null;
    }

    public OrdersSearchItemMenu(
            UltimateDonutSmp plugin,
            String query,
            int page,
            long editOrderId,
            OrdersManager.OrderEditNavigation editNavigation
    ) {
        super(plugin, plugin.getOrdersManager().getSearchItemTitle(sanitizeQuery(query)), plugin.getOrdersManager().getSearchItemSize());
        this.query = sanitizeQuery(query);
        this.page = Math.max(1, page);
        this.editOrderId = editOrderId;
        this.editNavigation = editNavigation;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<Material> results = plugin.getOrdersManager().searchOrderMaterials(query);
        int itemsPerPage = plugin.getOrdersManager().getSearchItemItemsPerPage();
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(results.size(), startIndex + itemsPerPage);

        for (int slot = 0; slot < itemsPerPage && slot < inventory.getSize() - 9; slot++) {
            int resultIndex = startIndex + slot;
            if (resultIndex >= endIndex) {
                break;
            }

            Material material = results.get(resultIndex);
            set(slot, ItemUtils.createItem(
                    material,
                    "&b" + plugin.getOrdersManager().describeMaterial(material),
                    List.of(
                            "&7ѕᴇᴀʀᴄʜ: &f" + query,
                            "&7ᴄᴀᴛᴇɢᴏʀʏ: &f" + plugin.getOrdersManager().prettifyCategory(plugin.getOrdersManager().resolveCategoryForMaterial(material)),
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
        set(lastRow + 2, ItemUtils.createItem(Material.SPYGLASS, "&bɴᴇᴡ ѕᴇᴀʀᴄʜ", List.of("&7ᴛʏᴘᴇ ᴀɴᴏᴛʜᴇʀ ɪᴛᴇᴍ ᴏʀ ᴄᴀᴛᴇɢᴏʀʏ")));
        set(lastRow + 3, ItemUtils.createItem(Material.CLOCK, "&eʀᴇꜰʀᴇѕʜ", List.of("&7ʀᴇʟᴏᴀᴅ ѕᴇᴀʀᴄʜ ʀᴇѕᴜʟᴛѕ")));
        set(lastRow + 4, ItemUtils.createItem(
                Material.NAME_TAG,
                "&eǫᴜᴇʀʏ: &f" + query,
                List.of("&7ᴀᴠᴀɪʟᴀʙʟᴇ ɪᴛᴇᴍѕ: &f" + results.size())
        ));
        set(lastRow + 5, ItemUtils.createItem(
                Material.BOOK,
                "&eᴘᴀɢᴇ " + page + "&7/&e" + getTotalPages(results.size(), itemsPerPage),
                List.of("&7ᴍᴀᴛᴄʜᴇѕ: &f" + results.size())
        ));
        set(lastRow + 7, hasNextPage(results.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ", List.of("&7ᴄʟᴏѕᴇ ᴏʀᴅᴇʀѕ")));

        if (results.isEmpty()) {
            int emptySlot = Math.max(0, Math.min(itemsPerPage - 1, itemsPerPage / 2));
            set(emptySlot, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cɴᴏ ʀᴇѕᴜʟᴛѕ",
                    List.of(
                            "&7ɴᴏ ᴏʀᴅᴇʀᴀʙʟᴇ ɪᴛᴇᴍѕ ᴍᴀᴛᴄʜᴇᴅ &f" + query + "&7.",
                            "&7ᴛʀʏ ᴀ ɴᴀᴍᴇ ʟɪᴋᴇ &fѕʜᴜʟᴋᴇʀ&7, &fᴏᴀᴋ&7, ᴏʀ &fʙʟᴏᴄᴋѕ&7."
                    )
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        int lastRow = inventory.getSize() - 9;
        List<Material> results = plugin.getOrdersManager().searchOrderMaterials(query);
        int itemsPerPage = plugin.getOrdersManager().getSearchItemItemsPerPage();

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
                new OrdersSearchItemMenu(plugin, query, page - 1, editOrderId, editNavigation).open(player);
            }
            return;
        }
        if (slot == lastRow + 2) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (isEditMode()) {
                plugin.getOrdersManager().promptEditOrderSearchInput(player, editOrderId, editNavigation);
            } else {
                plugin.getOrdersManager().promptOrderSearchInput(player);
            }
            return;
        }
        if (slot == lastRow + 3) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersSearchItemMenu(plugin, query, page, editOrderId, editNavigation).open(player);
            return;
        }
        if (slot == lastRow + 7) {
            if (hasNextPage(results.size(), itemsPerPage)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersSearchItemMenu(plugin, query, page + 1, editOrderId, editNavigation).open(player);
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

        int resultIndex = ((page - 1) * itemsPerPage) + slot;
        if (resultIndex >= results.size()) {
            return;
        }

        Material selected = results.get(resultIndex);
        boolean started = plugin.getOrdersManager().selectOrderItem(
                player,
                new ItemStack(selected),
                plugin.getOrdersManager().resolveCategoryForMaterial(selected),
                editOrderId,
                editNavigation
        );
        SoundUtils.play(player, plugin.getConfigManager().getSound(started ? "MENUS.BUTTON-CLICK" : "ORDERS.FAIL"));
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

    private static String sanitizeQuery(String query) {
        return query == null ? "" : query.replace('&', ' ').trim();
    }

    private int getTotalPages(int totalItems, int itemsPerPage) {
        return Math.max(1, (int) Math.ceil(totalItems / (double) itemsPerPage));
    }

    private boolean hasNextPage(int totalItems, int itemsPerPage) {
        return page < getTotalPages(totalItems, itemsPerPage);
    }
}
