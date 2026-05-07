package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class AuctionHouseMyListingsMenu extends BaseMenu {

    private final int page;
    private final AuctionHouseManager.AuctionSort sortMode;

    public AuctionHouseMyListingsMenu(UltimateDonutSmp plugin, int page, AuctionHouseManager.AuctionSort sortMode) {
        super(plugin, plugin.getAuctionHouseManager().getMyListingsTitle(), plugin.getAuctionHouseManager().getMyListingsSize());
        this.page = Math.max(1, page);
        this.sortMode = sortMode == null ? plugin.getAuctionHouseManager().getDefaultSort() : sortMode;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<AuctionListing> listings = plugin.getAuctionHouseManager()
                .getActiveListingsForSeller(player.getUniqueId(), sortMode);
        int itemsPerPage = plugin.getAuctionHouseManager().getMyListingsItemsPerPage();
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
                    true
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&bBack to Market", List.of("&7Return to the main market")));
        set(lastRow + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aPrevious Page", List.of("&7Go to page &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 2, ItemUtils.createItem(Material.CLOCK, "&eRefresh", List.of("&7Reload your active listings")));
        set(lastRow + 3, ItemUtils.createItem(
                Material.HOPPER,
                "&aSort: &f" + sortMode.name().replace('_', ' '),
                List.of("&7Click to cycle sorting mode")
        ));
        set(lastRow + 4, ItemUtils.createItem(Material.ENDER_CHEST, "&dClaims", List.of("&7Open your claim queue")));
        set(lastRow + 5, ItemUtils.createItem(
                Material.BOOK,
                "&ePage " + page + "&7/&e" + getTotalPages(listings.size(), itemsPerPage),
                List.of("&7Your active listings: &f" + listings.size())
        ));
        set(lastRow + 7, hasNextPage(listings.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aNext Page", List.of("&7Go to page &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cClose", List.of("&7Close this menu")));

        if (listings.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNo Active Listings",
                    List.of("&7Use &f/ah sell <price> &7to create your first listing.")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        int lastRow = inventory.getSize() - 9;
        List<AuctionListing> listings = plugin.getAuctionHouseManager()
                .getActiveListingsForSeller(player.getUniqueId(), sortMode);
        int itemsPerPage = plugin.getAuctionHouseManager().getMyListingsItemsPerPage();

        if (slot == lastRow) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new AuctionHouseBrowseMenu(plugin, 1, sortMode).open(player);
            return;
        }
        if (slot == lastRow + 1) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new AuctionHouseMyListingsMenu(plugin, page - 1, sortMode).open(player);
            }
            return;
        }
        if (slot == lastRow + 2) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new AuctionHouseMyListingsMenu(plugin, page, sortMode).open(player);
            return;
        }
        if (slot == lastRow + 3) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new AuctionHouseMyListingsMenu(plugin, 1, nextSort(sortMode)).open(player);
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
                new AuctionHouseMyListingsMenu(plugin, page + 1, sortMode).open(player);
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
        new AuctionHouseListingMenu(plugin, listings.get(listingIndex).id(), true, page, sortMode).open(player);
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
