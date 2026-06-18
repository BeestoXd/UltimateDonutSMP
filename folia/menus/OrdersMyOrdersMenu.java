package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

public class OrdersMyOrdersMenu extends BaseMenu {

    private final int page;
    private final OrderSort sortMode;
    private final String query;

    public OrdersMyOrdersMenu(UltimateDonutSmp plugin, int page, OrderSort sortMode) {
        this(plugin, page, sortMode, "");
    }

    public OrdersMyOrdersMenu(UltimateDonutSmp plugin, int page, OrderSort sortMode, String query) {
        super(plugin, plugin.getOrdersManager().getMyOrdersTitle(), plugin.getOrdersManager().getMyOrdersSize());
        this.page = Math.max(1, page);
        this.sortMode = sortMode == null ? plugin.getOrdersManager().getDefaultSort() : sortMode;
        this.query = query == null ? "" : query;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<Order> orders = plugin.getOrdersManager().getOrdersForOwner(player.getUniqueId(), sortMode);
        if (!query.isEmpty()) {
            String q = query.toLowerCase();
            orders = orders.stream()
                    .filter(order -> {
                        String name = order.requestedItem() != null && order.requestedItem().getItemMeta() != null
                                && order.requestedItem().getItemMeta().hasDisplayName()
                                ? order.requestedItem().getItemMeta().getDisplayName() : "";
                        return name.toLowerCase().contains(q)
                                || order.requestedMaterialKey().toLowerCase().contains(q)
                                || order.ownerName().toLowerCase().contains(q);
                    })
                    .toList();
        }
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
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&bʙᴀᴄᴋ ᴛᴏ ʙᴏᴀʀᴅ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴀᴄᴛɪᴠᴇ ᴏʀᴅᴇʀѕ")));
        set(lastRow + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));

        set(lastRow + 2, ItemUtils.createItem(
                Material.OAK_SIGN,
                "&bSearch",
                List.of(
                        "&7Current: &e" + (query.isEmpty() ? "none" : query),
                        "",
                        "&eLeft-Click &7to search",
                        "&eRight-Click &7to clear"
                )
        ));

        set(lastRow + 3, ItemUtils.createItem(Material.CLOCK, "&eʀᴇꜰʀᴇѕʜ", List.of("&7ʀᴇʟᴏᴀᴅ ʏᴏᴜʀ ᴏʀᴅᴇʀѕ")));

        List<String> sortLore = new ArrayList<>();
        sortLore.add("&7Click to change");
        sortLore.add("");
        for (OrderSort val : plugin.getOrdersManager().getAllowedSorts()) {
            boolean active = (val == sortMode);
            String bullet = active ? "&f• " : "&7• ";
            String textColor = active ? "&f" : "&7";
            sortLore.add(bullet + textColor + getSortOptionName(val));
        }

        set(lastRow + 4, ItemUtils.createItem(
                Material.HOPPER,
                OrdersMenuSupport.tr("&aѕᴏʀᴛ: &f") + plugin.getLanguageManager().display(
                        "ORDER_SORTS",
                        sortMode.name(),
                        sortMode.displayName()
                ),
                sortLore
        ));

        set(lastRow + 5, ItemUtils.createItem(Material.EMERALD, "&aɴᴇᴡ ᴏʀᴅᴇʀ", List.of("&7ᴄʀᴇᴀᴛᴇ ᴀ ɴᴇᴡ ʙᴜʏ ᴏʀᴅᴇʀ")));

        if (plugin.getOrdersManager().isClaimsEnabled()) {
            set(lastRow + 6, ItemUtils.createItem(Material.ENDER_CHEST, "&dᴄᴏʟʟᴇᴄᴛ", List.of("&7ᴄᴏʟʟᴇᴄᴛ ᴅᴇʟɪᴠᴇʀᴇᴅ ɪᴛᴇᴍѕ ᴀɴᴅ ʀᴇꜰᴜɴᴅѕ")));
        } else {
            set(lastRow + 6, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));
        }

        set(lastRow + 7, hasNextPage(orders.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));

        set(lastRow + 8, ItemUtils.createPlaceholder(Material.GRAY_STAINED_GLASS_PANE));

        if (orders.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cɴᴏ ᴏʀᴅᴇʀѕ ʏᴇᴛ",
                    List.of("&7ᴄʀᴇᴀᴛᴇ ʏᴏᴜʀ ꜰɪʀѕᴛ ʙᴜʏ ᴏʀᴅᴇʀ ꜰʀᴏᴍ ᴛʜᴇ ʙᴏᴀʀᴅ.")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        int lastRow = inventory.getSize() - 9;
        List<Order> orders = plugin.getOrdersManager().getOrdersForOwner(player.getUniqueId(), sortMode);
        int itemsPerPage = plugin.getOrdersManager().getMyOrdersItemsPerPage();

        if (slot == lastRow) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersBrowseMenu(plugin, 1, plugin.getOrdersManager().getDefaultSort(), "ALL", query).open(player);
            return;
        }
        if (slot == lastRow + 1) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersMyOrdersMenu(plugin, page - 1, sortMode, query).open(player);
            }
            return;
        }
        if (slot == lastRow + 2) {
            if (clickType.isRightClick()) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                new OrdersMyOrdersMenu(plugin, 1, sortMode, "").open(player);
            } else {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                plugin.getOrdersManager().promptOrdersMenuSearch(player, sortMode, "ALL", true);
            }
            return;
        }
        if (slot == lastRow + 3) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersMyOrdersMenu(plugin, page, sortMode, query).open(player);
            return;
        }
        if (slot == lastRow + 4) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersMyOrdersMenu(plugin, 1, nextSort(sortMode), query).open(player);
            return;
        }
        if (slot == lastRow + 5) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getOrdersManager().openNewOrderMenu(player);
            return;
        }
        if (slot == lastRow + 6) {
            if (!plugin.getOrdersManager().isClaimsEnabled()) {
                return;
            }
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersCollectMenu(plugin, 1).open(player);
            return;
        }
        if (slot == lastRow + 7) {
            if (hasNextPage(orders.size(), itemsPerPage)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersMyOrdersMenu(plugin, page + 1, sortMode, query).open(player);
            }
            return;
        }
        if (slot == lastRow + 8) {
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

    private String getSortOptionName(OrderSort sort) {
        return switch (sort) {
            case MOST_MONEY_PER_ITEM -> "Most Per Item";
            case MOST_PAID -> "Most Paid";
            case RECENTLY_LISTED -> "Recently Listed";
            case MOST_DELIVERED -> "Most Delivered";
        };
    }
}
