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
                "ᴄᴀᴛᴇɢᴏʀʏ ᴏʀᴅᴇʀ",
                Material.BOOK,
                null
        ),
        PRICE_HIGH_TO_LOW(
                "ᴘʀɪᴄᴇ ʜɪɢʜ ᴛᴏ ʟᴏᴡ",
                Material.GOLD_INGOT,
                Comparator.comparingDouble(WorthManager.WorthBrowserEntry::unitWorth)
                        .reversed()
                        .thenComparing(entry -> entry.material().name())
        ),
        PRICE_LOW_TO_HIGH(
                "ᴘʀɪᴄᴇ ʟᴏᴡ ᴛᴏ ʜɪɢʜ",
                Material.IRON_INGOT,
                Comparator.comparingDouble(WorthManager.WorthBrowserEntry::unitWorth)
                        .thenComparing(entry -> entry.material().name())
        ),
        NAME_A_TO_Z(
                "ɴᴀᴍᴇ ᴀ ᴛᴏ ᴢ",
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
                            "&7ᴄᴀᴛᴇɢᴏʀʏ: &f" + formatCategory(entry.categoryKey()),
                            "&7ᴡᴏʀᴛʜ: &a$" + NumberUtils.formatNice(entry.unitWorth()),
                            "&7ѕᴛᴀᴄᴋ x64: &a$" + NumberUtils.formatNice(entry.unitWorth() * 64),
                            "",
                            "&eᴄʟɪᴄᴋ ᴛᴏ ѕᴇɴᴅ ᴡᴏʀᴛʜ ɪɴꜰᴏ ɪɴ ᴄʜᴀᴛ"
                    )
            );
            set(inventorySlot, displayItem);
        }

        int lastRowStart = inventory.getSize() - 9;
        set(lastRowStart, ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRowStart + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRowStart + 3, ItemUtils.createItem(
                Material.BOOK,
                "&bᴡᴏʀᴛʜ ʙʀᴏᴡѕᴇʀ",
                List.of(
                        "&7ᴘᴀɢᴇ: &f" + page + "&7/&f" + getTotalPages(entries.size()),
                        "&7ᴇɴᴛʀɪᴇѕ: &f" + entries.size()
                )
        ));
        set(lastRowStart + 4, ItemUtils.createItem(
                sortMode.icon(),
                "&eѕᴏʀᴛ: &f" + sortMode.displayName(),
                List.of(
                        "&7ʟᴇꜰᴛ ᴄʟɪᴄᴋ: &fɴᴇxᴛ ѕᴏʀᴛ",
                        "&7ʀɪɢʜᴛ ᴄʟɪᴄᴋ: &fᴘʀᴇᴠɪᴏᴜѕ ѕᴏʀᴛ"
                )
        ));
        set(lastRowStart + 7, hasNextPage(entries.size())
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRowStart + 8, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ", List.of("&7ᴄʟᴏѕᴇ ᴛʜɪѕ ᴍᴇɴᴜ")));
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
                        + " &7ɪѕ ᴡᴏʀᴛʜ &a$" + NumberUtils.formatNice(entry.unitWorth())
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
