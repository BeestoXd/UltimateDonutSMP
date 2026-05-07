package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.WorthManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class WorthMenu extends BaseMenu {

    private enum SortMode {
        CATEGORY(
                "Category Order",
                Material.BOOK,
                null
        ),
        PRICE_HIGH_TO_LOW(
                "Price High to Low",
                Material.GOLD_INGOT,
                Comparator.comparingDouble(WorthManager.WorthBrowserEntry::unitWorth)
                        .reversed()
                        .thenComparing(entry -> entry.material().name())
        ),
        PRICE_LOW_TO_HIGH(
                "Price Low to High",
                Material.IRON_INGOT,
                Comparator.comparingDouble(WorthManager.WorthBrowserEntry::unitWorth)
                        .thenComparing(entry -> entry.material().name())
        ),
        NAME_A_TO_Z(
                "Name A to Z",
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

    public WorthMenu(UltimateDonutSmp plugin, int page) {
        this(plugin, page, SortMode.fromConfig(
                plugin.getConfigManager().getWorth().getString("BROWSER.DEFAULT-SORT", "CATEGORY")
        ));
    }

    public WorthMenu(UltimateDonutSmp plugin, int page, SortMode sortMode) {
        super(plugin, plugin.getWorthManager().getBrowserTitle(), plugin.getWorthManager().getBrowserSize());
        this.page = Math.max(1, page);
        this.itemsPerPage = plugin.getWorthManager().getBrowserItemsPerPage();
        this.sortMode = sortMode == null ? SortMode.CATEGORY : sortMode;
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
            ItemStack displayItem = ItemUtils.createItem(
                    entry.material(),
                    "&b" + plugin.getWorthManager().prettifyMaterial(entry.material()),
                    List.of(
                            "&7Category: &f" + formatCategory(entry.categoryKey()),
                            "&7Worth: &a$" + NumberUtils.formatNice(entry.unitWorth()),
                            "&7Stack x64: &a$" + NumberUtils.formatNice(entry.unitWorth() * 64),
                            "",
                            "&eClick to send worth info in chat"
                    )
            );
            set(inventorySlot, displayItem);
        }

        int lastRowStart = inventory.getSize() - 9;
        set(lastRowStart, ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRowStart + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aPrevious Page", List.of("&7Go to page &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRowStart + 3, ItemUtils.createItem(
                Material.BOOK,
                "&bWorth Browser",
                List.of(
                        "&7Page: &f" + page + "&7/&f" + getTotalPages(entries.size()),
                        "&7Entries: &f" + entries.size()
                )
        ));
        set(lastRowStart + 4, ItemUtils.createItem(
                sortMode.icon(),
                "&eSort: &f" + sortMode.displayName(),
                List.of(
                        "&7Left click: &fNext sort",
                        "&7Right click: &fPrevious sort"
                )
        ));
        set(lastRowStart + 7, hasNextPage(entries.size())
                ? ItemUtils.createItem(Material.ARROW, "&aNext Page", List.of("&7Go to page &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRowStart + 8, ItemUtils.createItem(Material.BARRIER, "&cClose", List.of("&7Close this menu")));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        int lastRowStart = inventory.getSize() - 9;
        List<WorthManager.WorthBrowserEntry> entries = getSortedEntries();

        if (slot == lastRowStart + 1 && page > 1) {
            new WorthMenu(plugin, page - 1, sortMode).open(player);
            return;
        }

        if (slot == lastRowStart + 4) {
            SortMode targetSort = clickType.isRightClick() ? sortMode.previous() : sortMode.next();
            new WorthMenu(plugin, 1, targetSort).open(player);
            return;
        }

        if (slot == lastRowStart + 7 && hasNextPage(entries.size())) {
            new WorthMenu(plugin, page + 1, sortMode).open(player);
            return;
        }

        if (slot == lastRowStart + 8) {
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

        WorthManager.WorthBrowserEntry entry = entries.get(entryIndex);
        player.sendMessage(ColorUtils.toComponent(
                "&7" + plugin.getWorthManager().prettifyMaterial(entry.material())
                        + " &7is worth &a$" + NumberUtils.formatNice(entry.unitWorth())
                        + " &8(" + formatCategory(entry.categoryKey()) + "&8)"
        ));
    }

    private List<WorthManager.WorthBrowserEntry> getSortedEntries() {
        return sortMode.sort(plugin.getWorthManager().getBrowserEntries());
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
}
