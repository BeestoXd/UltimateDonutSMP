package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ShopManager;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShopMenu extends BaseMenu {

    private final boolean mainMenu;
    private final String menuSection;
    private final Map<Integer, ShopManager.ShopCategory> slotCategories = new HashMap<>();
    private final Map<Integer, ShopManager.ShopItem> slotItems = new HashMap<>();
    private int page;
    private int totalPages = 1;
    private boolean hasPreviousPage;
    private boolean hasNextPage;

    public ShopMenu(UltimateDonutSmp plugin) {
        this(plugin, null, 0);
    }

    public ShopMenu(UltimateDonutSmp plugin, String menuSection) {
        this(plugin, menuSection, 0);
    }

    public ShopMenu(UltimateDonutSmp plugin, String menuSection, int page) {
        super(
                plugin,
                menuSection == null
                        ? plugin.getConfigManager().getShop().getString("CATEGORIES.MENU-TITLE", "&8Shop")
                        : plugin.getConfigManager().getShop().getString(menuSection + ".TITLE", "&8Shop"),
                normalizeSize(menuSection == null
                        ? plugin.getConfigManager().getShop().getInt("CATEGORIES.MENU-SIZE", 27)
                        : plugin.getConfigManager().getShop().getInt(menuSection + ".SIZE", 27))
        );
        this.mainMenu = menuSection == null;
        this.menuSection = menuSection;
        this.page = Math.max(0, page);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        slotCategories.clear();
        slotItems.clear();

        if (mainMenu) {
            buildMainMenu();
        } else {
            buildCategoryMenu();
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (mainMenu) {
            ShopManager.ShopCategory category = slotCategories.get(slot);
            if (category == null) {
                return;
            }

            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new ShopMenu(plugin, category.menuSection(), 0).open(player);
            return;
        }

        if (slot == getBackSlot()) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new ShopMenu(plugin).open(player);
            return;
        }

        if (slot == getFirstPageSlot() && hasPreviousPage) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            page = 0;
            build(player);
            return;
        }

        if (slot == getPreviousPageSlot() && hasPreviousPage) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            page--;
            build(player);
            return;
        }

        if (slot == getNextPageSlot() && hasNextPage) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            page++;
            build(player);
            return;
        }

        if (slot == getLastPageSlot() && hasNextPage) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
            page = totalPages - 1;
            build(player);
            return;
        }

        ShopManager.ShopItem item = slotItems.get(slot);
        if (item == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        new PurchaseShopMenu(plugin, item, menuSection, page).open(player);
    }

    private void buildMainMenu() {
        List<ShopManager.ShopCategory> categories = plugin.getShopManager().loadCategories();
        for (ShopManager.ShopCategory category : categories) {
            set(category.slot(), ItemUtils.createItem(
                    category.material(),
                    category.displayName(),
                    category.lore()
            ));
            slotCategories.put(category.slot(), category);
        }

        if (categories.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo shop categories",
                    List.of("&7Belum ada kategori shop yang aktif.")
            ));
        }
    }

    private void buildCategoryMenu() {
        List<ShopManager.ShopItem> items = plugin.getShopManager().loadMenuItems(menuSection);
        buildBackButton();

        if (items.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo items in this category",
                    List.of("&7Belum ada item shop yang aktif di kategori ini.")
            ));
            return;
        }

        int[] contentSlots = getContentSlots();
        if (contentSlots.length == 0) {
            return;
        }

        int itemsPerPage = Math.max(1, Math.min(contentSlots.length, getConfiguredItemsPerPage(contentSlots.length)));
        totalPages = Math.max(1, (int) Math.ceil(items.size() / (double) itemsPerPage));
        if (page >= totalPages) {
            page = totalPages - 1;
        }

        hasPreviousPage = page > 0;
        hasNextPage = page < totalPages - 1;

        if (canUseConfiguredSlots(items, contentSlots)) {
            for (ShopManager.ShopItem item : items) {
                set(item.slot(), createShopItem(item));
                slotItems.put(item.slot(), item);
            }
        } else {
            int startIndex = page * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, items.size());
            for (int index = startIndex; index < endIndex; index++) {
                int slot = contentSlots[index - startIndex];
                ShopManager.ShopItem item = items.get(index);
                set(slot, createShopItem(item));
                slotItems.put(slot, item);
            }
        }

        buildPageButtons(items.size());
    }

    private ItemStack createShopItem(ShopManager.ShopItem item) {
        List<String> lore = new ArrayList<>(item.lore());
        if (!lore.isEmpty()) {
            lore.add("");
        }

        String currencyLabel = item.currency() == ShopManager.Currency.SHARD ? "&5Shards" : "&aMoney";
        lore.add("&7Currency: " + currencyLabel);
        lore.add("&eClick to choose quantity");

        return ItemUtils.createItem(item.material(), item.displayName(), lore);
    }

    private void buildBackButton() {
        ConfigurationSection backButton = plugin.getConfigManager().getShop()
                .getConfigurationSection("BACK-BUTTON");
        if (backButton == null) {
            return;
        }

        set(getBackSlot(), ItemUtils.createItem(
                ItemUtils.parseMaterial(backButton.getString("MATERIAL", "RED_STAINED_GLASS_PANE")),
                backButton.getString("DISPLAY-NAME", "&cBack"),
                backButton.getStringList("LORE")
        ));
    }

    private void buildPageButtons(int totalItems) {
        FileConfiguration menus = plugin.getConfigManager().getMenus();
        Material arrowMaterial = ItemUtils.parseMaterial(menus.getString("GLOBAL.PAGE-MENU.MATERIAL", "ARROW"));

        if (hasPreviousPage) {
            set(getFirstPageSlot(), ItemUtils.createItem(
                    arrowMaterial,
                    menus.getString("GLOBAL.PAGE-MENU.FIRST-PAGE-BUTTON", "&aFirst Page"),
                    menus.getStringList("GLOBAL.PAGE-MENU.FIRST-PAGE-LORE")
            ));
            set(getPreviousPageSlot(), ItemUtils.createItem(
                    arrowMaterial,
                    menus.getString("GLOBAL.PAGE-MENU.BACK-BUTTON", "&aBack"),
                    menus.getStringList("GLOBAL.PAGE-MENU.BACK-LORE")
            ));
        }

        set(getPageInfoSlot(), ItemUtils.createItem(
                Material.BOOK,
                "&ePage " + (page + 1) + "&7/&e" + totalPages,
                List.of("&fItems: &7" + totalItems)
        ));

        if (hasNextPage) {
            set(getNextPageSlot(), ItemUtils.createItem(
                    arrowMaterial,
                    menus.getString("GLOBAL.PAGE-MENU.NEXT-BUTTON", "&aNext"),
                    menus.getStringList("GLOBAL.PAGE-MENU.NEXT-LORE")
            ));
            set(getLastPageSlot(), ItemUtils.createItem(
                    arrowMaterial,
                    menus.getString("GLOBAL.PAGE-MENU.LAST-PAGE-BUTTON", "&aLast Page"),
                    menus.getStringList("GLOBAL.PAGE-MENU.LAST-PAGE-LORE")
            ));
        }
    }

    private boolean canUseConfiguredSlots(List<ShopManager.ShopItem> items, int[] contentSlots) {
        if (totalPages > 1 || items.isEmpty()) {
            return false;
        }

        Set<Integer> validSlots = new HashSet<>();
        for (int slot : contentSlots) {
            validSlots.add(slot);
        }

        Set<Integer> usedSlots = new HashSet<>();
        for (ShopManager.ShopItem item : items) {
            if (!validSlots.contains(item.slot()) || !usedSlots.add(item.slot())) {
                return false;
            }
        }
        return true;
    }

    private int getConfiguredItemsPerPage(int contentSlotCount) {
        return plugin.getConfigManager().getShop()
                .getInt(menuSection + ".ITEMS-PER-PAGE", contentSlotCount);
    }

    private int[] getContentSlots() {
        List<Integer> slots = new ArrayList<>();
        Set<Integer> reserved = new HashSet<>();
        reserved.add(getBackSlot());
        reserved.add(getFirstPageSlot());
        reserved.add(getPreviousPageSlot());
        reserved.add(getPageInfoSlot());
        reserved.add(getNextPageSlot());
        reserved.add(getLastPageSlot());

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (!reserved.contains(slot)) {
                slots.add(slot);
            }
        }

        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private int getBackSlot() {
        return plugin.getConfigManager().getShop().getInt(menuSection + ".BACK-BUTTON-SLOT", inventory.getSize() - 9);
    }

    private int getFirstPageSlot() {
        return plugin.getConfigManager().getShop().getInt(menuSection + ".FIRST-PAGE-SLOT", inventory.getSize() - 8);
    }

    private int getPreviousPageSlot() {
        return plugin.getConfigManager().getShop().getInt(menuSection + ".PREVIOUS-PAGE-SLOT", inventory.getSize() - 7);
    }

    private int getPageInfoSlot() {
        return plugin.getConfigManager().getShop().getInt(menuSection + ".PAGE-INFO-SLOT", inventory.getSize() - 5);
    }

    private int getNextPageSlot() {
        return plugin.getConfigManager().getShop().getInt(menuSection + ".NEXT-PAGE-SLOT", inventory.getSize() - 3);
    }

    private int getLastPageSlot() {
        return plugin.getConfigManager().getShop().getInt(menuSection + ".LAST-PAGE-SLOT", inventory.getSize() - 2);
    }

    private static int normalizeSize(int configuredSize) {
        if (configuredSize < 9) {
            return 27;
        }
        if (configuredSize > 54) {
            return 54;
        }
        return configuredSize - (configuredSize % 9);
    }
}
