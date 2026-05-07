package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class AuctionHouseBrowseMenu extends BaseMenu {

    private final int page;
    private final AuctionHouseManager.AuctionSort sortMode;

    public AuctionHouseBrowseMenu(UltimateDonutSmp plugin, int page, AuctionHouseManager.AuctionSort sortMode) {
        super(plugin, plugin.getAuctionHouseManager().getBrowseTitle(), plugin.getAuctionHouseManager().getBrowseSize());
        this.page = Math.max(1, page);
        this.sortMode = sortMode == null ? plugin.getAuctionHouseManager().getDefaultSort() : sortMode;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<AuctionListing> listings = plugin.getAuctionHouseManager().getActiveListings(sortMode);
        int itemsPerPage = plugin.getAuctionHouseManager().getBrowseItemsPerPage();
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(listings.size(), startIndex + itemsPerPage);

        for (int slot = 0; slot < itemsPerPage && slot < inventory.getSize() - 9; slot++) {
            int listingIndex = startIndex + slot;
            if (listingIndex >= endIndex) {
                break;
            }

            AuctionListing listing = listings.get(listingIndex);
            set(slot, AuctionHouseMenuSupport.createListingDisplay(
                    plugin,
                    plugin.getAuctionHouseManager(),
                    listing,
                    listing.sellerUuid().equals(player.getUniqueId())
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aPrevious Page", List.of("&7Go to page &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 1, ItemUtils.createItem(Material.CLOCK, "&eRefresh", List.of("&7Reload active listings")));
        set(lastRow + 2, ItemUtils.createItem(
                Material.HOPPER,
                "&aSort: &f" + sortMode.name().replace('_', ' '),
                List.of("&7Click to cycle sorting mode")
        ));
        set(lastRow + 3, ItemUtils.createItem(Material.CHEST, "&bMy Listings", List.of("&7View your active listings")));
        set(lastRow + 4, ItemUtils.createItem(Material.ENDER_CHEST, "&dClaims", List.of("&7Claim sold money and returned items")));
        set(lastRow + 5, ItemUtils.createItem(
                Material.BOOK,
                "&ePage " + page + "&7/&e" + getTotalPages(listings.size(), itemsPerPage),
                List.of("&7Active listings: &f" + listings.size())
        ));
        set(lastRow + 7, hasNextPage(listings.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aNext Page", List.of("&7Go to page &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cClose", List.of("&7Close the Auction House")));

        if (listings.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo Active Listings",
                    List.of("&7Use &f/ah sell <price> &7to create one.")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        int lastRow = inventory.getSize() - 9;
        List<AuctionListing> listings = plugin.getAuctionHouseManager().getActiveListings(sortMode);
        int itemsPerPage = plugin.getAuctionHouseManager().getBrowseItemsPerPage();

        if (slot == lastRow) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new AuctionHouseBrowseMenu(plugin, page - 1, sortMode).open(player);
            }
            return;
        }
        if (slot == lastRow + 1) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new AuctionHouseBrowseMenu(plugin, page, sortMode).open(player);
            return;
        }
        if (slot == lastRow + 2) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new AuctionHouseBrowseMenu(plugin, 1, nextSort(sortMode)).open(player);
            return;
        }
        if (slot == lastRow + 3) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new AuctionHouseMyListingsMenu(plugin, 1, sortMode).open(player);
            return;
        }
        if (slot == lastRow + 4) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new AuctionHouseClaimsMenu(plugin, 1).open(player);
            return;
        }
        if (slot == lastRow + 7) {
            if (hasNextPage(listings.size(), itemsPerPage)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new AuctionHouseBrowseMenu(plugin, page + 1, sortMode).open(player);
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

        int listingIndex = ((page - 1) * itemsPerPage) + slot;
        if (listingIndex >= listings.size()) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        new AuctionHouseListingMenu(plugin, listings.get(listingIndex).id(), false, page, sortMode).open(player);
    }

    private int getTotalPages(int totalItems, int itemsPerPage) {
        return Math.max(1, (int) Math.ceil(totalItems / (double) itemsPerPage));
    }

    private boolean hasNextPage(int totalItems, int itemsPerPage) {
        return page < getTotalPages(totalItems, itemsPerPage);
    }

    private AuctionHouseManager.AuctionSort nextSort(AuctionHouseManager.AuctionSort current) {
        List<AuctionHouseManager.AuctionSort> sorts = plugin.getAuctionHouseManager().getAllowedSorts();
        int index = sorts.indexOf(current);
        if (index < 0) {
            return plugin.getAuctionHouseManager().getDefaultSort();
        }
        return sorts.get((index + 1) % sorts.size());
    }
}
