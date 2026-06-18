package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.models.PlayerPreference;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import com.bx.ultimateDonutSmp.utils.ShulkerBoxSupport;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionHouseBrowseMenu extends BaseMenu {

    private final int page;
    private final AuctionHouseManager.AuctionSort sortMode;
    private final String categoryFilter;
    private final Map<Integer, AuctionListing> slotMappings = new HashMap<>();

    public AuctionHouseBrowseMenu(UltimateDonutSmp plugin, int page, AuctionHouseManager.AuctionSort sortMode) {
        this(plugin, page, sortMode, "ALL");
    }

    public AuctionHouseBrowseMenu(UltimateDonutSmp plugin, int page, AuctionHouseManager.AuctionSort sortMode, String categoryFilter) {
        super(plugin, plugin.getAuctionHouseManager().getBrowseTitle(), 54);
        this.page = Math.max(1, page);
        this.sortMode = sortMode == null ? plugin.getAuctionHouseManager().getDefaultSort() : sortMode;
        this.categoryFilter = categoryFilter == null ? "ALL" : categoryFilter;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        slotMappings.clear();

        String searchQuery = plugin.getAuctionHouseManager().getSearchQuery(player.getUniqueId());
        List<AuctionListing> listings = plugin.getAuctionHouseManager().getActiveListings(sortMode, categoryFilter, searchQuery);

        int itemsPerPage = 45;
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(listings.size(), startIndex + itemsPerPage);

        long now = System.currentTimeMillis();

        for (int slot = 0; slot < itemsPerPage && (startIndex + slot) < endIndex; slot++) {
            AuctionListing listing = listings.get(startIndex + slot);
            ItemStack displayItem = listing.item().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                lore.add("");
                lore.add(ColorUtils.colorize("&7Price: &e$" + NumberUtils.format(listing.price())));
                lore.add(ColorUtils.colorize("&7Seller: &f" + listing.sellerName()));
                lore.add(ColorUtils.colorize("&7Time Remaining: &f" + plugin.getAuctionHouseManager().formatRemaining(listing.secondsRemaining(now) * 1000L)));

                if (ShulkerBoxSupport.isShulkerBox(displayItem)) {
                    int count = ShulkerBoxSupport.getItemCount(displayItem);
                    lore.add(ColorUtils.colorize("&bItems Inside: &3" + count));
                    lore.add(ColorUtils.colorize("&8Right-click to preview"));
                }

                lore.add(ColorUtils.colorize("&aClick to purchase."));
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            set(slot, displayItem);
            slotMappings.put(slot, listing);
        }

        // Slot 45: Previous Page
        set(45, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&fPrevious Page", List.of("&7Go to page " + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));

        // Slot 47: Sort Mode (Cauldron)
        String sortLabel = sortMode.name().replace('_', ' ').toLowerCase();
        sortLabel = Character.toUpperCase(sortLabel.charAt(0)) + sortLabel.substring(1);
        set(47, ItemUtils.createItem(
                Material.CAULDRON,
                "&fPrice Sort",
                List.of(
                        "&7Current: &e" + sortLabel,
                        "&8Click to cycle sorting"
                )
        ));

        // Slot 48: Category Filter (Hopper)
        String catLabel = categoryFilter.substring(0, 1) + categoryFilter.substring(1).toLowerCase();
        set(48, ItemUtils.createItem(
                Material.HOPPER,
                "&fFilter",
                List.of(
                        "&7Category: &e" + catLabel,
                        "&8Click to change category"
                )
        ));

        // Slot 49: Refresh (Anvil)
        set(49, ItemUtils.createItem(
                Material.ANVIL,
                "&fAuction",
                List.of("&7Refresh the auction house")
        ));

        // Slot 50: Search (Oak Sign)
        set(50, ItemUtils.createItem(
                Material.OAK_SIGN,
                "&fSearch",
                List.of(
                        "&7Current: &e" + (searchQuery.isEmpty() ? "none" : searchQuery),
                        "&8Left-click to search",
                        "&8Right-click to clear"
                )
        ));

        // Slot 51: Your Items (Chest)
        set(51, ItemUtils.createItem(
                Material.CHEST,
                "&fYour Items",
                List.of("&7View active, sold, and expired listings")
        ));

        // Slot 53: Next Page
        int totalPages = Math.max(1, (int) Math.ceil(listings.size() / (double) itemsPerPage));
        set(53, page < totalPages
                ? ItemUtils.createItem(Material.ARROW, "&fNext Page", List.of("&7Open the next page"))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot >= 0 && slot < 45) {
            AuctionListing listing = slotMappings.get(slot);
            if (listing == null) return;

            // Check if right click and shulker
            if (clickType.isRightClick() && ShulkerBoxSupport.isShulkerBox(listing.item())) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                new ShulkerPreviewGui(plugin, listing.item()).open(player);
                return;
            }

            // Left click to purchase
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

            PlayerPreference pref = plugin.getAuctionHouseManager().getPreference(player.getUniqueId());
            boolean fastBuy = (pref != null && pref.fastBuyEnabled() &&
                    (player.hasPermission("ultimatedonutsmp.auctionhouse.fastbuy") || player.hasPermission("donutauction.fastbuy")));

            if (fastBuy) {
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                var result = plugin.getAuctionHouseManager().purchaseListing(player, listing.id());
                if (result.success()) {
                    String successMsg = plugin.getConfigManager().getMessage(
                            "AUCTION_HOUSE.PURCHASE_SUCCESS",
                            "{item}", plugin.getAuctionHouseManager().describeItem(listing.item()),
                            "{price}", NumberUtils.format(listing.price()),
                            "{seller}", listing.sellerName()
                    );
                    player.sendMessage(ColorUtils.toComponent(successMsg));
                } else {
                    String errorMsgKey = switch (result.reason()) {
                        case DISABLED -> "AUCTION_HOUSE.DISABLED";
                        case LISTING_NOT_FOUND -> "AUCTION_HOUSE.LISTING_NOT_FOUND";
                        case NOT_ACTIVE -> "AUCTION_HOUSE.LISTING_NOT_ACTIVE";
                        case OWN_LISTING -> "AUCTION_HOUSE.CANNOT_BUY_OWN";
                        case NO_MONEY -> "AUCTION_HOUSE.NOT_ENOUGH_MONEY";
                        case INVENTORY_FULL -> "AUCTION_HOUSE.FULL_INVENTORY";
                        default -> "AUCTION_HOUSE.LISTING_NOT_FOUND";
                    };
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(errorMsgKey)));
                }
                new AuctionHouseBrowseMenu(plugin, page, sortMode, categoryFilter).open(player);
            } else {
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                new ConfirmPurchaseGui(plugin, listing).open(player);
            }
            return;
        }

        if (slot == 45) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                new AuctionHouseBrowseMenu(plugin, page - 1, sortMode, categoryFilter).open(player);
            }
            return;
        }

        if (slot == 47) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new AuctionHouseBrowseMenu(plugin, 1, nextSort(sortMode), categoryFilter).open(player);
            return;
        }

        if (slot == 48) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new FilterGui(plugin, sortMode, categoryFilter).open(player);
            return;
        }

        if (slot == 49) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new AuctionHouseBrowseMenu(plugin, page, sortMode, categoryFilter).open(player);
            return;
        }

        if (slot == 50) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (clickType.isRightClick()) {
                plugin.getAuctionHouseManager().clearSearchQuery(player.getUniqueId());
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                new AuctionHouseBrowseMenu(plugin, 1, sortMode, categoryFilter).open(player);
            } else {
                org.bukkit.configuration.ConfigurationSection config = plugin.getConfigManager().getAuctionHouse()
                        .getConfigurationSection("GUI.BROWSE.SEARCH_SIGN");
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                com.bx.ultimateDonutSmp.utils.SignInputUtil.openFromConfig(plugin, player, config, text -> {
                    if (text == null || text.isBlank() || text.equalsIgnoreCase("cancel")) {
                        new AuctionHouseBrowseMenu(plugin, page, sortMode, categoryFilter).open(player);
                    } else {
                        plugin.getAuctionHouseManager().setSearchQuery(player.getUniqueId(), text.trim());
                        new AuctionHouseBrowseMenu(plugin, 1, sortMode, categoryFilter).open(player);
                    }
                });
            }
            return;
        }

        if (slot == 51) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new PlayerAuctionGui(plugin, 1).open(player);
            return;
        }

        if (slot == 53) {
            String searchQuery = plugin.getAuctionHouseManager().getSearchQuery(player.getUniqueId());
            List<AuctionListing> listings = plugin.getAuctionHouseManager().getActiveListings(sortMode, categoryFilter, searchQuery);
            int totalPages = Math.max(1, (int) Math.ceil(listings.size() / 45.0));

            if (page < totalPages) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                new AuctionHouseBrowseMenu(plugin, page + 1, sortMode, categoryFilter).open(player);
            }
        }
    }

    private AuctionHouseManager.AuctionSort nextSort(AuctionHouseManager.AuctionSort current) {
        List<AuctionHouseManager.AuctionSort> sorts = plugin.getAuctionHouseManager().getAllowedSorts();
        int index = sorts.indexOf(current);
        if (index < 0) {
            return plugin.getAuctionHouseManager().getDefaultSort();
        }
        return sorts.get((index + 1) % sorts.size());
    }

    @Override
    public void onClose(Player player) {
        if (!plugin.getAuctionHouseManager().stopNavigating(player.getUniqueId())) {
            plugin.getAuctionHouseManager().clearSearchQuery(player.getUniqueId());
        }
    }
}
