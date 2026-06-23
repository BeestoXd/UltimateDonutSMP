package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CurrencyManager;
import com.bx.ultimateDonutSmp.managers.ShopManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ShopMenu extends BaseMenu {

    private final boolean mainMenu;
    private final boolean favoritesMenu;
    private final String menuSection;
    private final Map<Integer, ShopManager.ShopCategory> slotCategories = new HashMap<>();
    private final Map<Integer, ShopManager.ShopItem> slotItems = new HashMap<>();
    private final Map<Integer, ShopManager.AuctionQuote> slotQuotes = new HashMap<>();
    private int page;
    private int totalPages = 1;
    private boolean hasPreviousPage;
    private boolean hasNextPage;

    public ShopMenu(UltimateDonutSmp plugin) {
        this(plugin, null, 0, false);
    }

    public ShopMenu(UltimateDonutSmp plugin, String menuSection) {
        this(plugin, menuSection, 0, false);
    }

    public ShopMenu(UltimateDonutSmp plugin, String menuSection, int page) {
        this(plugin, menuSection, page, false);
    }

    public ShopMenu(UltimateDonutSmp plugin, boolean favoritesMenu, int page) {
        this(plugin, null, page, favoritesMenu);
    }

    private ShopMenu(UltimateDonutSmp plugin, String menuSection, int page, boolean favoritesMenu) {
        super(
                plugin,
                resolveTitle(plugin, menuSection, favoritesMenu),
                resolveSize(plugin, menuSection, favoritesMenu)
        );
        this.mainMenu = menuSection == null && !favoritesMenu;
        this.favoritesMenu = favoritesMenu;
        this.menuSection = menuSection;
        this.page = Math.max(0, page);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        slotCategories.clear();
        slotItems.clear();
        slotQuotes.clear();

        if (mainMenu) {
            buildMainMenu(player);
        } else {
            buildItemsMenu(player);
        }
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (mainMenu) {
            handleMainClick(slot, player);
            return;
        }

        if (slot == getBackSlot()) {
            click(player);
            new ShopMenu(plugin).open(player);
            return;
        }

        if (handlePageClick(slot, player)) {
            return;
        }

        ShopManager.ShopItem item = slotItems.get(slot);
        if (item == null) {
            return;
        }

        if (favoritesEnabled() && clickType.isShiftClick()) {
            boolean favorite = plugin.getShopManager().toggleFavorite(player.getUniqueId(), item);
            player.sendMessage(ColorUtils.toComponent(text(
                    favorite ? "SHOP-GUI.MESSAGES.FAVORITE-ADDED" : "SHOP-GUI.MESSAGES.FAVORITE-REMOVED",
                    favorite ? "&aᴀᴅᴅᴇᴅ &f{item}&a ᴛᴏ ѕʜᴏᴘ ꜰᴀᴠᴏʀɪᴛᴇѕ." : "&eʀᴇᴍᴏᴠᴇᴅ &f{item}&e ꜰʀᴏᴍ ѕʜᴏᴘ ꜰᴀᴠᴏʀɪᴛᴇѕ.",
                    "{item}", itemName(item)
            )));
            click(player);
            build(player);
            return;
        }

        ShopManager.AuctionQuote quote = slotQuotes.get(slot);
        if (clickType.isRightClick() && quote != null) {
            click(player);
            new ConfirmPurchaseGui(
                    plugin,
                    quote.listing(),
                    ignored -> reopenCurrent(player)
            ).open(player);
            return;
        }

        click(player);
        if (favoritesMenu) {
            new PurchaseShopMenu(plugin, item, item.menuSection(), page, true).open(player);
        } else {
            new PurchaseShopMenu(plugin, item, menuSection, page).open(player);
        }
    }

    private void handleMainClick(int slot, Player player) {
        int favoritesSlot = config().getInt("SHOP-GUI.FAVORITES.BUTTON.SLOT", 26);
        if (favoritesEnabled() && slot == favoritesSlot) {
            click(player);
            new ShopMenu(plugin, true, 0).open(player);
            return;
        }

        ShopManager.ShopCategory category = slotCategories.get(slot);
        if (category != null) {
            click(player);
            new ShopMenu(plugin, category.menuSection(), 0).open(player);
        }
    }

    private boolean handlePageClick(int slot, Player player) {
        if (slot == getFirstPageSlot() && hasPreviousPage) {
            click(player);
            page = 0;
            build(player);
            return true;
        }
        if (slot == getPreviousPageSlot() && hasPreviousPage) {
            click(player);
            page--;
            build(player);
            return true;
        }
        if (slot == getNextPageSlot() && hasNextPage) {
            click(player);
            page++;
            build(player);
            return true;
        }
        if (slot == getLastPageSlot() && hasNextPage) {
            click(player);
            page = totalPages - 1;
            build(player);
            return true;
        }
        return false;
    }

    private void buildMainMenu(Player player) {
        List<ShopManager.ShopCategory> categories = plugin.getShopManager().loadCategories();
        for (ShopManager.ShopCategory category : categories) {
            set(category.slot(), ItemUtils.createItem(
                    category.material(),
                    plugin.getCurrencyManager().applyStaticPlaceholders(category.displayName()),
                    plugin.getCurrencyManager().applyStaticPlaceholders(category.lore())
            ));
            slotCategories.put(category.slot(), category);
        }

        if (categories.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    text("SHOP-GUI.EMPTY.CATEGORIES.NAME", "&cɴᴏ ѕʜᴏᴘ ᴄᴀᴛᴇɢᴏʀɪᴇѕ"),
                    textList("SHOP-GUI.EMPTY.CATEGORIES.LORE", List.of("&7ɴᴏ ᴇɴᴀʙʟᴇᴅ ᴄᴀᴛᴇɢᴏʀɪᴇѕ ᴀʀᴇ ᴄᴏɴꜰɪɢᴜʀᴇᴅ."))
            ));
        }

        if (favoritesEnabled()) {
            buildFavoritesControl(player);
        }
    }

    private void buildFavoritesControl(Player player) {
        String path = "SHOP-GUI.FAVORITES.BUTTON";
        int count = plugin.getShopManager().getPreference(player.getUniqueId()).favorites().size();
        set(config().getInt(path + ".SLOT", 26), ItemUtils.createItem(
                ItemUtils.parseMaterial(config().getString(path + ".MATERIAL", "NETHER_STAR")),
                replace(config().getString(path + ".NAME", "&dꜰᴀᴠᴏʀɪᴛᴇ ɪᴛᴇᴍѕ"), "{count}", String.valueOf(count)),
                replace(config().getStringList(path + ".LORE"), "{count}", String.valueOf(count))
        ));
    }

    private void buildItemsMenu(Player player) {
        List<ShopManager.ShopItem> items = favoritesMenu
                ? plugin.getShopManager().loadFavoriteItems(player.getUniqueId())
                : plugin.getShopManager().loadMenuItems(menuSection);
        buildBackButton();

        if (items.isEmpty()) {
            String emptyPath = favoritesMenu ? "SHOP-GUI.EMPTY.FAVORITES" : "SHOP-GUI.EMPTY.ITEMS";
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    config().getString(emptyPath + ".NAME", favoritesMenu ? "&cɴᴏ ꜰᴀᴠᴏʀɪᴛᴇ ɪᴛᴇᴍѕ" : "&cɴᴏ ѕʜᴏᴘ ɪᴛᴇᴍѕ"),
                    config().getStringList(emptyPath + ".LORE")
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

        if (!favoritesMenu && canUseConfiguredSlots(items, contentSlots)) {
            for (ShopManager.ShopItem item : items) {
                renderItem(player, item.slot(), item);
            }
        } else {
            int startIndex = page * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, items.size());
            for (int index = startIndex; index < endIndex; index++) {
                renderItem(player, contentSlots[index - startIndex], items.get(index));
            }
        }

        buildPageButtons(items.size());
    }

    private void renderItem(Player player, int slot, ShopManager.ShopItem item) {
        ShopManager.AuctionQuote quote = plugin.getShopManager().findBestAuctionQuote(player, item);
        set(slot, createShopItem(player, item, quote));
        slotItems.put(slot, item);
        if (quote != null) {
            slotQuotes.put(slot, quote);
        }
    }

    private ItemStack createShopItem(
            Player player,
            ShopManager.ShopItem item,
            ShopManager.AuctionQuote quote
    ) {
        List<String> lore = item.lore().stream()
                .map(line -> replaceShopItemCurrencyPlaceholders(line, item))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (!lore.isEmpty()) {
            lore.add("");
        }

        boolean favoritesEnabled = favoritesEnabled();
        boolean favorite = favoritesEnabled
                && plugin.getShopManager().isFavorite(player.getUniqueId(), item);
        for (String line : config().getStringList("SHOP-GUI.ITEM.LORE")) {
            if (!favoritesEnabled
                    && (line.contains("{favorite_line}") || line.contains("{favorite_action}"))) {
                continue;
            }
            lore.add(replaceShopGuiPlaceholders(line, item, quote, favorite));
        }

        return ItemUtils.createItem(
                item.material(),
                plugin.getCurrencyManager().applyStaticPlaceholders(item.displayName()),
                plugin.getCurrencyManager().applyStaticPlaceholders(lore)
        );
    }

    private String replaceShopGuiPlaceholders(
            String line,
            ShopManager.ShopItem item,
            ShopManager.AuctionQuote quote,
            boolean favorite
    ) {
        CurrencyManager.CurrencyType currencyType = currencyType(item);
        double shopPrice = item.currency() == ShopManager.Currency.SHARD
                ? Math.round(item.pricePerUnit())
                : item.pricePerUnit();
        String auctionLine = quote == null
                ? config().getString("SHOP-GUI.ITEM.NO-AUCTION", "&8ʙᴇѕᴛ ᴀᴜᴄᴛɪᴏɴ: ᴜɴᴀᴠᴀɪʟᴀʙʟᴇ")
                : replace(
                        config().getString(
                                "SHOP-GUI.ITEM.AUCTION",
                                "&7ʙᴇѕᴛ ᴀᴜᴄᴛɪᴏɴ: {auction_price} &8({auction_amount} ɪᴛᴇᴍѕ)"
                        ),
                        "{auction_price}", plugin.getCurrencyManager().formatMoney(quote.listing().price()),
                        "{auction_unit_price}", plugin.getCurrencyManager().formatMoney(quote.unitPrice()),
                        "{auction_amount}", String.valueOf(quote.listing().item().getAmount())
                );
        String favoriteLine = config().getString(
                favorite ? "SHOP-GUI.ITEM.FAVORITE-ON" : "SHOP-GUI.ITEM.FAVORITE-OFF",
                favorite ? "&d★ ꜰᴀᴠᴏʀɪᴛᴇ" : "&7☆ ɴᴏᴛ ꜰᴀᴠᴏʀɪᴛᴇᴅ"
        );
        String favoriteAction = favoritesEnabled()
                ? config().getString("SHOP-GUI.ITEM.FAVORITE-ACTION", "&dѕʜɪꜰᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴛᴏɢɢʟᴇ ꜰᴀᴠᴏʀɪᴛᴇ")
                : "";
        String auctionAction = config().getString(
                quote == null ? "SHOP-GUI.ITEM.NO-AUCTION-ACTION" : "SHOP-GUI.ITEM.AUCTION-ACTION",
                quote == null ? "" : "&bʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ ʙᴜʏ ᴛʜᴇ ʙᴇѕᴛ ᴀᴜᴄᴛɪᴏɴ"
        );
        return replace(
                line,
                "{shop_price}", plugin.getCurrencyManager().format(currencyType, shopPrice),
                "{shop_unit_price}", plugin.getCurrencyManager().format(currencyType, shopPrice),
                "{auction_line}", auctionLine,
                "{auction_action}", auctionAction,
                "{favorite_line}", favoriteLine,
                "{favorite_action}", favoriteAction,
                "{item}", itemName(item)
        );
    }

    private CurrencyManager.CurrencyType currencyType(ShopManager.ShopItem item) {
        return item.currency() == ShopManager.Currency.SHARD
                ? CurrencyManager.CurrencyType.SHARDS
                : CurrencyManager.CurrencyType.MONEY;
    }

    private String replaceShopItemCurrencyPlaceholders(String line, ShopManager.ShopItem item) {
        CurrencyManager.CurrencyType currencyType = currencyType(item);
        double price = item.currency() == ShopManager.Currency.SHARD
                ? Math.round(item.pricePerUnit())
                : item.pricePerUnit();
        String formattedPrice = plugin.getCurrencyManager().format(currencyType, price);
        String rawPrice = plugin.getCurrencyManager().formatAmount(currencyType, price);
        String result = plugin.getCurrencyManager().applyStaticPlaceholders(line)
                .replace("{price_formatted}", formattedPrice)
                .replace("%price_formatted%", formattedPrice)
                .replace("${price_formatted}", formattedPrice)
                .replace("${price}", formattedPrice)
                .replace("{price}", rawPrice)
                .replace("%price%", rawPrice);

        String normalizedLine = normalizePriceLabel(line);
        if ((normalizedLine.contains("buy price:") || normalizedLine.contains("harga beli:")) && !line.contains("{price")) {
            int colonIndex = line.indexOf(':');
            if (colonIndex >= 0) {
                return line.substring(0, colonIndex + 1) + " " + formattedPrice;
            }
        }
        return result;
    }

    private String normalizePriceLabel(String value) {
        return (value == null ? "" : value.toLowerCase(Locale.ROOT))
                .replace('b', 'b')
                .replace('u', 'u')
                .replace('y', 'y')
                .replace('p', 'p')
                .replace('r', 'r')
                .replace('i', 'i')
                .replace('c', 'c')
                .replace('e', 'e')
                .replace('h', 'h')
                .replace('a', 'a')
                .replace('g', 'g')
                .replace('l', 'l');
    }

    private void buildBackButton() {
        ConfigurationSection backButton = config().getConfigurationSection("BACK-BUTTON");
        if (backButton == null) {
            return;
        }
        set(getBackSlot(), ItemUtils.createItem(
                ItemUtils.parseMaterial(backButton.getString("MATERIAL", "RED_STAINED_GLASS_PANE")),
                backButton.getString("DISPLAY-NAME", "&cʙᴀᴄᴋ"),
                backButton.getStringList("LORE")
        ));
    }

    private void buildPageButtons(int totalItems) {
        FileConfiguration menus = plugin.getConfigManager().getMenus();
        Material arrowMaterial = ItemUtils.parseMaterial(menus.getString("GLOBAL.PAGE-MENU.MATERIAL", "ARROW"));

        if (hasPreviousPage) {
            set(getFirstPageSlot(), ItemUtils.createItem(
                    arrowMaterial,
                    menus.getString("GLOBAL.PAGE-MENU.FIRST-PAGE-BUTTON", "&aꜰɪʀѕᴛ ᴘᴀɢᴇ"),
                    menus.getStringList("GLOBAL.PAGE-MENU.FIRST-PAGE-LORE")
            ));
            set(getPreviousPageSlot(), ItemUtils.createItem(
                    arrowMaterial,
                    menus.getString("GLOBAL.PAGE-MENU.BACK-BUTTON", "&aʙᴀᴄᴋ"),
                    menus.getStringList("GLOBAL.PAGE-MENU.BACK-LORE")
            ));
        }

        set(getPageInfoSlot(), ItemUtils.createItem(
                Material.BOOK,
                text("SHOP-GUI.PAGE.NAME", "&eᴘᴀɢᴇ {page}&7/&e{pages}",
                        "{page}", String.valueOf(page + 1),
                        "{pages}", String.valueOf(totalPages)),
                List.of(text("SHOP-GUI.PAGE.ITEMS", "&fɪᴛᴇᴍѕ: &7{items}",
                        "{items}", String.valueOf(totalItems)))
        ));

        if (hasNextPage) {
            set(getNextPageSlot(), ItemUtils.createItem(
                    arrowMaterial,
                    menus.getString("GLOBAL.PAGE-MENU.NEXT-BUTTON", "&aɴᴇxᴛ"),
                    menus.getStringList("GLOBAL.PAGE-MENU.NEXT-LORE")
            ));
            set(getLastPageSlot(), ItemUtils.createItem(
                    arrowMaterial,
                    menus.getString("GLOBAL.PAGE-MENU.LAST-PAGE-BUTTON", "&aʟᴀѕᴛ ᴘᴀɢᴇ"),
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
        return config().getInt(menuPath() + ".ITEMS-PER-PAGE", contentSlotCount);
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

    private String menuPath() {
        return favoritesMenu ? "SHOP-GUI.FAVORITES.MENU" : menuSection;
    }

    private int getBackSlot() {
        return config().getInt(menuPath() + ".BACK-BUTTON-SLOT", inventory.getSize() - 9);
    }

    private int getFirstPageSlot() {
        return config().getInt(menuPath() + ".FIRST-PAGE-SLOT", inventory.getSize() - 8);
    }

    private int getPreviousPageSlot() {
        return config().getInt(menuPath() + ".PREVIOUS-PAGE-SLOT", inventory.getSize() - 7);
    }

    private int getPageInfoSlot() {
        return config().getInt(menuPath() + ".PAGE-INFO-SLOT", inventory.getSize() - 5);
    }

    private int getNextPageSlot() {
        return config().getInt(menuPath() + ".NEXT-PAGE-SLOT", inventory.getSize() - 3);
    }

    private int getLastPageSlot() {
        return config().getInt(menuPath() + ".LAST-PAGE-SLOT", inventory.getSize() - 2);
    }

    private void reopenCurrent(Player player) {
        if (favoritesMenu) {
            new ShopMenu(plugin, true, page).open(player);
        } else {
            new ShopMenu(plugin, menuSection, page).open(player);
        }
    }

    private void click(Player player) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
    }

    private String itemName(ShopManager.ShopItem item) {
        String display = ColorUtils.strip(item.displayName());
        return display == null || display.isBlank()
                ? plugin.getWorthManager().prettifyMaterial(item.material())
                : display;
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getShop();
    }

    private boolean favoritesEnabled() {
        return plugin.getShopManager().areFavoritesEnabled();
    }

    private String text(String path, String fallback, String... replacements) {
        String value = config().getString(path, fallback);
        return replace(value, replacements);
    }

    private List<String> textList(String path, List<String> fallback, String... replacements) {
        List<String> value = config().isList(path) ? config().getStringList(path) : fallback;
        return replace(value, replacements);
    }

    private String replace(String value, String... replacements) {
        String resolved = value == null ? "" : value;
        for (int index = 0; index + 1 < replacements.length; index += 2) {
            resolved = resolved.replace(replacements[index], replacements[index + 1]);
        }
        return resolved;
    }

    private List<String> replace(List<String> values, String... replacements) {
        List<String> resolved = new ArrayList<>();
        for (String value : values) {
            resolved.add(replace(value, replacements));
        }
        return resolved;
    }

    private static String resolveTitle(UltimateDonutSmp plugin, String menuSection, boolean favoritesMenu) {
        FileConfiguration config = plugin.getConfigManager().getShop();
        if (favoritesMenu) {
            return config.getString("SHOP-GUI.FAVORITES.MENU.TITLE", "&8ꜰᴀᴠᴏʀɪᴛᴇ ѕʜᴏᴘ ɪᴛᴇᴍѕ");
        }
        if (menuSection == null) {
            return config.getString("CATEGORIES.MENU-TITLE", "&8ѕʜᴏᴘ");
        }
        return config.getString(menuSection + ".TITLE", "&8ѕʜᴏᴘ");
    }

    private static int resolveSize(UltimateDonutSmp plugin, String menuSection, boolean favoritesMenu) {
        FileConfiguration config = plugin.getConfigManager().getShop();
        int configured = favoritesMenu
                ? config.getInt("SHOP-GUI.FAVORITES.MENU.SIZE", 54)
                : menuSection == null
                ? config.getInt("CATEGORIES.MENU-SIZE", 27)
                : config.getInt(menuSection + ".SIZE", 27);
        return normalizeSize(configured);
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
