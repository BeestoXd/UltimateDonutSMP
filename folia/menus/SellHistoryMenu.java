package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.DatabaseManager;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class SellHistoryMenu extends BaseMenu {

    private static final int FIRST_PAGE_SLOT = 45;
    private static final int PREVIOUS_PAGE_SLOT = 46;
    private static final int PAGE_INFO_SLOT = 50;
    private static final int NEXT_PAGE_SLOT = 52;
    private static final int LAST_PAGE_SLOT = 53;

    private int page;
    private boolean sortByPrice;
    private boolean hasPreviousPage;
    private boolean hasNextPage;
    private int totalPages = 1;
    private int totalItems;

    public SellHistoryMenu(UltimateDonutSmp plugin) {
        super(
                plugin,
                plugin.getConfigManager().getMenus().getString("SELL-HISTORY-MENU.TITLE", "&8sell history"),
                plugin.getConfigManager().getMenus().getInt("SELL-HISTORY-MENU.SIZE", 54)
        );
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        FileConfiguration menus = plugin.getConfigManager().getMenus();
        int maxItems = menus.getInt("SELL-HISTORY-MENU.MAX-ITEMS-PER-PAGE", 45);
        totalItems = plugin.getDatabaseManager().countSellHistory(player.getUniqueId());
        totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) maxItems));
        if (page >= totalPages) {
            page = totalPages - 1;
        }

        int offset = page * maxItems;
        hasPreviousPage = page > 0;
        hasNextPage = offset + maxItems < totalItems;

        List<DatabaseManager.SellHistoryEntry> entries = plugin.getDatabaseManager()
                .getSellHistoryEntries(player.getUniqueId(), maxItems, offset, sortByPrice);

        for (int index = 0; index < entries.size() && index < 45; index++) {
            DatabaseManager.SellHistoryEntry entry = entries.get(index);
            set(index, createHistoryItem(menus, entry));
        }

        buildSortButton(menus);
        buildPageButtons(menus);
    }

    @Override
    public void handleClick(int slot, Player player) {
        int sortSlot = plugin.getConfigManager().getMenus().getInt("SELL-HISTORY-MENU.BUTTONS.SORT.SLOT", 49);
        if (slot == sortSlot) {
            sortByPrice = !sortByPrice;
            page = 0;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            build(player);
            return;
        }

        if (slot == FIRST_PAGE_SLOT && hasPreviousPage) {
            page = 0;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == PREVIOUS_PAGE_SLOT && hasPreviousPage) {
            page--;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == NEXT_PAGE_SLOT && hasNextPage) {
            page++;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
            return;
        }

        if (slot == LAST_PAGE_SLOT && hasNextPage) {
            page = totalPages - 1;
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            build(player);
        }
    }

    private void buildSortButton(FileConfiguration menus) {
        String path = "SELL-HISTORY-MENU.BUTTONS.SORT";
        Material material = ItemUtils.parseMaterial(menus.getString(path + ".MATERIAL", "ANVIL"));
        String name = menus.getString(path + ".NAME", "&aSort");
        String sortState = sortByPrice ? "Highest Price" : "Newest";
        List<String> lore = menus.getStringList(path + ".LORE").stream()
                .map(line -> line.replace("{sort_state}", sortState))
                .toList();
        set(menus.getInt(path + ".SLOT", 49), ItemUtils.createItem(material, name, lore));
    }

    private void buildPageButtons(FileConfiguration menus) {
        Material material = ItemUtils.parseMaterial(menus.getString("GLOBAL.PAGE-MENU.MATERIAL", "ARROW"));

        if (hasPreviousPage) {
            set(FIRST_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus.getString("GLOBAL.PAGE-MENU.FIRST-PAGE-BUTTON", "&aFirst Page"),
                    menus.getStringList("GLOBAL.PAGE-MENU.FIRST-PAGE-LORE")
            ));
            set(PREVIOUS_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus.getString("GLOBAL.PAGE-MENU.BACK-BUTTON", "&cBack"),
                    menus.getStringList("GLOBAL.PAGE-MENU.BACK-LORE")
            ));
        }

        set(PAGE_INFO_SLOT, ItemUtils.createItem(
                Material.BOOK,
                "&ePage " + (page + 1) + "&7/&e" + totalPages,
                List.of(
                        "&fEntries: &7" + NumberUtils.format(totalItems),
                        "&fSort: &7" + (sortByPrice ? "Highest Price" : "Newest")
                )
        ));

        if (hasNextPage) {
            set(NEXT_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus.getString("GLOBAL.PAGE-MENU.NEXT-BUTTON", "&aNext"),
                    menus.getStringList("GLOBAL.PAGE-MENU.NEXT-LORE")
            ));
            set(LAST_PAGE_SLOT, ItemUtils.createItem(
                    material,
                    menus.getString("GLOBAL.PAGE-MENU.LAST-PAGE-BUTTON", "&aLast Page"),
                    menus.getStringList("GLOBAL.PAGE-MENU.LAST-PAGE-LORE")
            ));
        }
    }

    private org.bukkit.inventory.ItemStack createHistoryItem(
            FileConfiguration menus,
            DatabaseManager.SellHistoryEntry entry
    ) {
        Material material = ItemUtils.parseMaterial(entry.itemName());
        String displayName = "&f" + toDisplayName(entry.itemName());
        List<String> lore = menus.getStringList("SELL-HISTORY-MENU.BUTTONS.MATERIAL-ITEM.LORE").stream()
                .map(line -> line
                        .replace("{price}", "$" + NumberUtils.formatNice(entry.price()))
                        .replace("{amount}", NumberUtils.format(entry.amount())))
                .toList();

        return ItemUtils.createItem(material, displayName, lore);
    }

    private String toDisplayName(String value) {
        String[] words = value.toLowerCase(Locale.US).split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }
}
