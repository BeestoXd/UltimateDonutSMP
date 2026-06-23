package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.models.OrderSort;
import com.bx.ultimateDonutSmp.models.OrderUiState;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersBrowseMenu extends BaseMenu {

    private final int page;
    private final OrderSort sortMode;
    private final String categoryFilter;
    private final String query;

    public OrdersBrowseMenu(UltimateDonutSmp plugin, int page, OrderSort sortMode, String categoryFilter) {
        this(plugin, page, sortMode, categoryFilter, "");
    }

    public OrdersBrowseMenu(UltimateDonutSmp plugin, int page, OrderSort sortMode, String categoryFilter, String query) {
        super(plugin, plugin.getOrdersManager().getBrowseTitle(), 54);
        this.page = Math.max(1, page);
        this.sortMode = sortMode == null ? plugin.getOrdersManager().getDefaultSort() : sortMode;
        this.categoryFilter = plugin.getOrdersManager().normalizeCategory(categoryFilter);
        this.query = query == null ? "" : query.trim();
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        OrdersManager manager = plugin.getOrdersManager();
        OrderUiState state = manager.getUiState(player.getUniqueId());
        state.page(page - 1);
        state.sort(sortMode);
        state.filter(categoryFilter);
        state.search(query);

        List<Order> orders = visibleOrders();
        int itemsPerPage = Math.min(45, manager.getBrowseItemsPerPage());
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(orders.size(), startIndex + itemsPerPage);
        for (int slot = 0; slot < itemsPerPage; slot++) {
            int orderIndex = startIndex + slot;
            if (orderIndex >= endIndex) {
                break;
            }
            Order order = orders.get(orderIndex);
            set(slot, OrdersMenuSupport.createOrderDisplay(
                    plugin,
                    manager,
                    order,
                    order.ownerUuid().equals(player.getUniqueId())
            ));
        }

        int prevSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.PREV.SLOT", 45);
        int sortSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.SORT.SLOT", 47);
        int filterSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.FILTER.SLOT", 48);
        int refreshSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.REFRESH.SLOT", 49);
        int searchSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.SEARCH.SLOT", 50);
        int myOrdersSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.MY_ORDERS.SLOT", 51);
        int collectSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.COLLECT.SLOT", 52);
        int nextSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.NEXT.SLOT", 53);

        if (page > 1) {
            set(prevSlot, OrdersMenuSupport.button(
                    plugin, "GUI.MAIN.BUTTONS.PREV", "ORDERS.GUI.MAIN.PREV",
                    Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ", List.of("&fᴄʟɪᴄᴋ ᴛᴏ ᴏᴘᴇɴ ᴘᴀɢᴇ {page}"),
                    "{page}", String.valueOf(page - 1)
            ));
        }
        if (hasNextPage(orders.size(), itemsPerPage)) {
            set(nextSlot, OrdersMenuSupport.button(
                    plugin, "GUI.MAIN.BUTTONS.NEXT", "ORDERS.GUI.MAIN.NEXT",
                    Material.ARROW, "&aɴᴇxᴛ", List.of("&fᴄʟɪᴄᴋ ᴛᴏ ᴏᴘᴇɴ ᴘᴀɢᴇ {page}"),
                    "{page}", String.valueOf(page + 1)
            ));
        }

        List<String> sortLore = new ArrayList<>();
        for (OrderSort value : manager.getAllowedSorts()) {
            String prefix = value == sortMode ? "&a• " : "&f• ";
            sortLore.add(prefix + plugin.getLanguageManager().display(
                    "ORDER_SORTS", value.name(), value.displayName()
            ));
        }
        set(sortSlot, ItemUtils.createItem(
                OrdersMenuSupport.material(plugin, "GUI.MAIN.BUTTONS.SORT.MATERIAL", Material.CAULDRON),
                OrdersMenuSupport.text(plugin, "ORDERS.GUI.MAIN.SORT.NAME", "&aѕᴏʀᴛ"),
                sortLore
        ));

        List<String> filterLore = new ArrayList<>();
        for (String category : manager.getAvailableCategories()) {
            filterLore.add((category.equals(categoryFilter) ? "&a• " : "&f• ")
                    + manager.prettifyCategory(category));
        }
        set(filterSlot, ItemUtils.createItem(
                OrdersMenuSupport.material(plugin, "GUI.MAIN.BUTTONS.FILTER.MATERIAL", Material.HOPPER),
                OrdersMenuSupport.text(plugin, "ORDERS.GUI.MAIN.FILTER.NAME", "&aꜰɪʟᴛᴇʀ"),
                filterLore
        ));
        set(refreshSlot, OrdersMenuSupport.button(
                plugin, "GUI.MAIN.BUTTONS.REFRESH", "ORDERS.GUI.MAIN.REFRESH",
                Material.MAP, "&aʀᴇꜰʀᴇѕʜ", List.of("&fᴄʟɪᴄᴋ ᴛᴏ ʀᴇꜰʀᴇѕʜ")
        ));
        set(searchSlot, OrdersMenuSupport.button(
                plugin, "GUI.MAIN.BUTTONS.SEARCH", "ORDERS.GUI.MAIN.SEARCH",
                Material.OAK_SIGN, "&aѕᴇᴀʀᴄʜ",
                List.of("&7ᴄᴜʀʀᴇɴᴛ: &f{query}", "", "&eʟᴇꜰᴛ-ᴄʟɪᴄᴋ &fᴛᴏ ѕᴇᴀʀᴄʜ", "&eʀɪɢʜᴛ-ᴄʟɪᴄᴋ &fᴛᴏ ᴄʟᴇᴀʀ"),
                "{query}", query.isBlank() ? "none" : query
        ));
        set(myOrdersSlot, OrdersMenuSupport.button(
                plugin, "GUI.MAIN.BUTTONS.MY_ORDERS", "ORDERS.GUI.MAIN.MY_ORDERS",
                Material.CHEST, "&aʏᴏᴜʀ ᴏʀᴅᴇʀѕ", List.of("&fᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ʏᴏᴜʀ ᴏʀᴅᴇʀѕ")
        ));
        if (manager.isClaimsEnabled()) {
            set(collectSlot, OrdersMenuSupport.button(
                    plugin, "GUI.MAIN.BUTTONS.COLLECT", "ORDERS.GUI.MAIN.COLLECT",
                    Material.DROPPER, "&aᴄᴏʟʟᴇᴄᴛ", List.of("&fᴄᴏʟʟᴇᴄᴛ ᴅᴇʟɪᴠᴇʀᴇᴅ ɪᴛᴇᴍѕ ᴀɴᴅ ʀᴇꜰᴜɴᴅѕ")
            ));
        }

        if (orders.isEmpty()) {
            set(22, ItemUtils.createItem(
                    Material.BARRIER,
                    OrdersMenuSupport.text(plugin, "ORDERS.GUI.MAIN.EMPTY.NAME", "&cɴᴏ ᴀᴄᴛɪᴠᴇ ᴏʀᴅᴇʀѕ"),
                    OrdersMenuSupport.list(plugin, "ORDERS.GUI.MAIN.EMPTY.LORE", List.of("&7ɴᴏ ᴏʀᴅᴇʀѕ ᴍᴀᴛᴄʜ ᴛʜɪѕ ᴠɪᴇᴡ."))
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        OrdersManager manager = plugin.getOrdersManager();
        List<Order> orders = visibleOrders();
        int itemsPerPage = Math.min(45, manager.getBrowseItemsPerPage());

        int prevSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.PREV.SLOT", 45);
        int sortSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.SORT.SLOT", 47);
        int filterSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.FILTER.SLOT", 48);
        int refreshSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.REFRESH.SLOT", 49);
        int searchSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.SEARCH.SLOT", 50);
        int myOrdersSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.MY_ORDERS.SLOT", 51);
        int collectSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.COLLECT.SLOT", 52);
        int nextSlot = OrdersMenuSupport.slot(plugin, "GUI.MAIN.BUTTONS.NEXT.SLOT", 53);

        if (slot == prevSlot && page > 1) {
            turnPage(player, page - 1, sortMode, categoryFilter, query);
            return;
        }
        if (slot == nextSlot && hasNextPage(orders.size(), itemsPerPage)) {
            turnPage(player, page + 1, sortMode, categoryFilter, query);
            return;
        }
        if (slot == sortSlot) {
            click(player);
            OrderSort next = nextSort(sortMode);
            manager.getUiState(player.getUniqueId()).sort(next);
            new OrdersBrowseMenu(plugin, 1, next, categoryFilter, query).open(player);
            return;
        }
        if (slot == filterSlot) {
            click(player);
            String next = manager.nextCategory(categoryFilter);
            manager.getUiState(player.getUniqueId()).filter(next);
            new OrdersBrowseMenu(plugin, 1, sortMode, next, query).open(player);
            return;
        }
        if (slot == refreshSlot) {
            click(player);
            new OrdersBrowseMenu(plugin, page, sortMode, categoryFilter, query).open(player);
            return;
        }
        if (slot == searchSlot) {
            click(player);
            if (clickType.isRightClick()) {
                new OrdersBrowseMenu(plugin, 1, sortMode, categoryFilter, "").open(player);
            } else {
                manager.promptOrdersMenuSearch(player, sortMode, categoryFilter, false);
            }
            return;
        }
        if (slot == myOrdersSlot) {
            click(player);
            new OrdersMyOrdersMenu(plugin, 1, sortMode, query).open(player);
            return;
        }
        if (slot == collectSlot && manager.isClaimsEnabled()) {
            click(player);
            new OrdersCollectMenu(plugin, 1).open(player);
            return;
        }
        if (slot < 0 || slot >= itemsPerPage) {
            return;
        }

        int orderIndex = ((page - 1) * itemsPerPage) + slot;
        if (orderIndex >= orders.size()) {
            return;
        }
        Order selected = orders.get(orderIndex);
        click(player);
        if (selected.ownerUuid().equals(player.getUniqueId())) {
            new OrdersEditMenu(plugin, selected.id(), false, page, sortMode, categoryFilter).open(player);
        } else if (manager.getDeliveryMode() == OrdersManager.DeliveryMode.DEPOSIT_GUI) {
            new OrdersDepositMenu(plugin, selected.id(), page, sortMode, categoryFilter).open(player);
        } else {
            new OrdersDeliverConfirmMenu(plugin, selected.id(), page, sortMode, categoryFilter).open(player);
        }
    }

    private List<Order> visibleOrders() {
        List<Order> orders = plugin.getOrdersManager().getActiveOrders(sortMode, categoryFilter);
        if (query.isBlank()) {
            return orders;
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        return orders.stream()
                .filter(order -> plugin.getOrdersManager().describeItem(order.requestedItem())
                        .toLowerCase(Locale.ROOT).contains(normalized)
                        || order.requestedMaterialKey().toLowerCase(Locale.ROOT).contains(normalized)
                        || order.ownerName().toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
    }

    private void click(Player player) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
    }

    private void turnPage(Player player, int target, OrderSort sort, String filter, String search) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
        new OrdersBrowseMenu(plugin, target, sort, filter, search).open(player);
    }

    private boolean hasNextPage(int totalItems, int itemsPerPage) {
        return page < Math.max(1, (int) Math.ceil(totalItems / (double) itemsPerPage));
    }

    private OrderSort nextSort(OrderSort current) {
        List<OrderSort> sorts = plugin.getOrdersManager().getAllowedSorts();
        int index = sorts.indexOf(current);
        return index < 0 ? plugin.getOrdersManager().getDefaultSort() : sorts.get((index + 1) % sorts.size());
    }
}
