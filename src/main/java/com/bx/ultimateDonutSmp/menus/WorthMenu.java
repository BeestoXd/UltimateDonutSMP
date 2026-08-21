package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.WorthManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import com.bx.ultimateDonutSmp.models.SellCategory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class WorthMenu extends BaseMenu {

    public enum SortMode {
        CATEGORY(
                "Category order",
                Material.BOOK,
                null
        ),
        PRICE_HIGH_TO_LOW(
                "Price high to low",
                Material.GOLD_INGOT,
                Comparator.comparingDouble(WorthManager.WorthBrowserEntry::unitWorth)
                        .reversed()
                        .thenComparing(entry -> entry.material().name())
        ),
        PRICE_LOW_TO_HIGH(
                "Price low to high",
                Material.IRON_INGOT,
                Comparator.comparingDouble(WorthManager.WorthBrowserEntry::unitWorth)
                        .thenComparing(entry -> entry.material().name())
        ),
        NAME_A_TO_Z(
                "Name a to z",
                Material.NAME_TAG,
                Comparator.comparing(entry -> entry.material().name())
        );

        private final String displayName;
        private final Material icon;
        private final Comparator<WorthManager.WorthBrowserEntry> comparator;

        SortMode(String displayName, Material icon, Comparator<WorthManager.WorthBrowserEntry> comparator) {
            this.displayName = displayName;
            this.icon = icon;
            this.comparator = comparator;
        }

        public String displayName() {
            return displayName;
        }

        public Material icon() {
            return icon;
        }

        public List<WorthManager.WorthBrowserEntry> sort(List<WorthManager.WorthBrowserEntry> entries) {
            List<WorthManager.WorthBrowserEntry> sorted = new ArrayList<>(entries);
            if (comparator != null) {
                sorted.sort(comparator);
            }
            return sorted;
        }

        public SortMode next() {
            SortMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        public SortMode previous() {
            SortMode[] values = values();
            return values[(ordinal() - 1 + values.length) % values.length];
        }

        public static SortMode fromConfig(String raw) {
            if (raw == null || raw.isBlank()) {
                return CATEGORY;
            }

            String normalized = raw.trim().toUpperCase(Locale.US)
                    .replace(' ', '_')
                    .replace('-', '_');
            for (SortMode mode : values()) {
                if (mode.name().equals(normalized)) {
                    return mode;
                }
            }
            return CATEGORY;
        }
    }

    private final int page;
    private final int itemsPerPage;
    private final SortMode sortMode;
    private final SellCategory categoryFilter;
    private final BaseMenu parentMenu;

    public WorthMenu(UltimateDonutSmp plugin, int page) {
        this(plugin, page, SortMode.fromConfig(
                plugin.getConfigManager().getWorth().getString("BROWSER.DEFAULT-SORT", "CATEGORY")
        ));
    }

    public WorthMenu(UltimateDonutSmp plugin, int page, SortMode sortMode) {
        this(plugin, page, sortMode, null, null);
    }

    public WorthMenu(UltimateDonutSmp plugin, int page, SortMode sortMode, SellCategory categoryFilter, BaseMenu parentMenu) {
        super(plugin,
                categoryFilter != null
                        ? ColorUtils.toComponent("&8" + categoryFilter.name().replace('_', ' ') + " ITEMS")
                        : plugin.getWorthManager().getBrowserTitle(),
                plugin.getWorthManager().getBrowserSize());
        this.page = Math.max(1, page);
        this.itemsPerPage = plugin.getWorthManager().getBrowserItemsPerPage();
        this.sortMode = sortMode == null ? SortMode.CATEGORY : sortMode;
        this.categoryFilter = categoryFilter;
        this.parentMenu = parentMenu;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<WorthManager.WorthBrowserEntry> entries = getSortedEntries();
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(entries.size(), startIndex + itemsPerPage);

        for (int inventorySlot = 0; inventorySlot < itemsPerPage; inventorySlot++) {
            int entryIndex = startIndex + inventorySlot;
            if (entryIndex >= endIndex || inventorySlot >= inventory.getSize() - 9) {
                break;
            }

            WorthManager.WorthBrowserEntry entry = entries.get(entryIndex);
            int stackSize = Math.max(1, entry.material().getMaxStackSize());
            ItemStack displayItem = ItemUtils.createItem(
                    entry.material(),
                    replaceItemPlaceholders(
                            plugin.getConfigManager().getWorth().getString("BROWSER.ITEM.NAME", "&b{item}"),
                            entry,
                            stackSize
                    ),
                    plugin.getConfigManager().getWorth().getStringList("BROWSER.ITEM.LORE").stream()
                            .map(line -> replaceItemPlaceholders(line, entry, stackSize))
                            .toList()
            );
            set(inventorySlot, displayItem);
        }

        int lastRowStart = inventory.getSize() - 9;
        set(lastRowStart, ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRowStart + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aPrevious page", List.of("&7Go to page &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRowStart + 3, ItemUtils.createItem(
                Material.BOOK,
                "&bWorth browser",
                List.of(
                        "&7Page: &f" + page + "&7/&f" + getTotalPages(entries.size()),
                        "&7Entries: &f" + entries.size()
                )
        ));
        set(lastRowStart + 4, ItemUtils.createItem(
                sortMode.icon(),
                "&eSort: &f" + sortMode.displayName(),
                List.of(
                        "&7Left click: &fnext sort",
                        "&7Right click: &fprevious sort"
                )
        ));
        set(lastRowStart + 7, hasNextPage(entries.size())
                ? ItemUtils.createItem(Material.ARROW, "&aNext page", List.of("&7Go to page &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRowStart + 8, parentMenu != null
                ? ItemUtils.createItem(Material.BARRIER, "&cBack", List.of("&7Go back to previous menu"))
                : ItemUtils.createItem(Material.BARRIER, "&cClose", List.of("&7Close this menu")));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        int lastRowStart = inventory.getSize() - 9;
        List<WorthManager.WorthBrowserEntry> entries = getSortedEntries();

        if (slot == lastRowStart + 1 && page > 1) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            new WorthMenu(plugin, page - 1, sortMode, categoryFilter, parentMenu).open(player);
            return;
        }

        if (slot == lastRowStart + 4) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            SortMode targetSort = clickType.isRightClick() ? sortMode.previous() : sortMode.next();
            new WorthMenu(plugin, 1, targetSort, categoryFilter, parentMenu).open(player);
            return;
        }

        if (slot == lastRowStart + 7 && hasNextPage(entries.size())) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            new WorthMenu(plugin, page + 1, sortMode, categoryFilter, parentMenu).open(player);
            return;
        }

        if (slot == lastRowStart + 8) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (parentMenu != null) {
                parentMenu.open(player);
            } else {
                player.closeInventory();
            }
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
        WorthManager.WorthBrowserEntry entry = entries.get(entryIndex);
        player.sendMessage(ColorUtils.toComponent(
                "&7" + plugin.getWorthManager().prettifyMaterial(entry.material())
                        + " &7is worth " + plugin.getCurrencyManager().formatMoneyCompact(entry.unitWorth())
                        + " &8(" + formatCategory(entry.categoryKey()) + "&8)"
        ));
    }

    private List<WorthManager.WorthBrowserEntry> getSortedEntries() {
        List<WorthManager.WorthBrowserEntry> entries = plugin.getWorthManager().getBrowserEntries();
        if (categoryFilter != null) {
            entries = entries.stream()
                    .filter(entry -> isEntryInCategory(entry, categoryFilter))
                    .toList();
        }
        return sortMode.sort(entries);
    }

    private boolean isEntryInCategory(WorthManager.WorthBrowserEntry entry, SellCategory category) {
        if (entry.categoryKey() != null && entry.categoryKey().equalsIgnoreCase(category.getWorthSectionKey())) {
            return true;
        }
        SellCategory entryCategory = SellCategory.fromConfigKey(entry.categoryKey()).orElse(null);
        if (entryCategory == category) {
            return true;
        }
        return plugin.getShopManager().getSellCategory(new ItemStack(entry.material())) == category;
    }

    private boolean hasNextPage(int totalEntries) {
        return page < getTotalPages(totalEntries);
    }

    private int getTotalPages(int totalEntries) {
        return Math.max(1, (int) Math.ceil(totalEntries / (double) itemsPerPage));
    }

    private String formatCategory(String categoryKey) {
        if (categoryKey == null || categoryKey.isBlank()) {
            return "General";
        }

        String[] tokens = categoryKey.toLowerCase(Locale.US).split("_");
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(token.charAt(0))).append(token.substring(1));
        }
        return builder.toString();
    }

    private String replaceItemPlaceholders(
            String text,
            WorthManager.WorthBrowserEntry entry,
            int stackSize
    ) {
        double stackPrice = calculateStackPrice(entry.unitWorth(), stackSize);
        return (text == null ? "" : text)
                .replace("{item}", plugin.getWorthManager().prettifyMaterial(entry.material()))
                .replace("{category}", formatCategory(entry.categoryKey()))
                .replace("{unit_price}", NumberUtils.format(entry.unitWorth()))
                .replace("{unit_price_formatted}", plugin.getCurrencyManager().formatMoney(entry.unitWorth()))
                .replace("{unit_price_compact}", plugin.getCurrencyManager().formatMoneyCompact(entry.unitWorth()))
                .replace("{stack_size}", String.valueOf(stackSize))
                .replace("{stack_price}", NumberUtils.format(stackPrice))
                .replace("{stack_price_formatted}", plugin.getCurrencyManager().formatMoney(stackPrice))
                .replace("{stack_price_compact}", plugin.getCurrencyManager().formatMoneyCompact(stackPrice));
    }

    public static double calculateStackPrice(double unitPrice, int stackSize) {
        return Math.max(0D, unitPrice) * Math.max(1, stackSize);
    }
}
