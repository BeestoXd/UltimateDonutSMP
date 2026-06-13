package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.managers.LanguageManager;
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
        LanguageManager language = plugin.getLanguageManager();
        String sortLabel = language.display(
                "SORTS",
                sortMode.name(),
                sortMode.name().replace('_', ' ')
        );
        set(lastRow, page > 1
                ? ItemUtils.createItem(
                        Material.ARROW,
                        language.menu("COMMON.PREVIOUS.NAME", "&aPrevious Page"),
                        language.menuList("COMMON.PREVIOUS.LORE", List.of("&7Go to page &f{page}"),
                                "{page}", String.valueOf(page - 1))
                )
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 1, ItemUtils.createItem(
                Material.CLOCK,
                language.menu("COMMON.REFRESH.NAME", "&eRefresh"),
                language.menuList("AUCTION_HOUSE.BROWSE.REFRESH_LORE", List.of("&7Reload active listings"))
        ));
        set(lastRow + 2, ItemUtils.createItem(
                Material.HOPPER,
                language.menu("AUCTION_HOUSE.BROWSE.SORT_NAME", "&aSort: &f{sort}",
                        "{sort}", sortLabel),
                language.menuList("AUCTION_HOUSE.BROWSE.SORT_LORE",
                        List.of("&7Click to cycle sorting mode"))
        ));
        set(lastRow + 3, ItemUtils.createItem(
                Material.CHEST,
                language.menu("AUCTION_HOUSE.BROWSE.MY_LISTINGS_NAME", "&bMy Listings"),
                language.menuList("AUCTION_HOUSE.BROWSE.MY_LISTINGS_LORE",
                        List.of("&7View your active listings"))
        ));
        set(lastRow + 4, ItemUtils.createItem(
                Material.ENDER_CHEST,
                language.menu("AUCTION_HOUSE.BROWSE.CLAIMS_NAME", "&dClaims"),
                language.menuList("AUCTION_HOUSE.BROWSE.CLAIMS_LORE",
                        List.of("&7Claim sold money and returned items"))
        ));
        set(lastRow + 5, ItemUtils.createItem(
                Material.BOOK,
                language.menu("COMMON.PAGE.NAME", "&ePage {page}&7/&e{pages}",
                        "{page}", String.valueOf(page),
                        "{pages}", String.valueOf(getTotalPages(listings.size(), itemsPerPage))),
                language.menuList("AUCTION_HOUSE.BROWSE.PAGE_LORE",
                        List.of("&7Active listings: &f{count}"),
                        "{count}", String.valueOf(listings.size()))
        ));
        set(lastRow + 7, hasNextPage(listings.size(), itemsPerPage)
                ? ItemUtils.createItem(
                        Material.ARROW,
                        language.menu("COMMON.NEXT.NAME", "&aNext Page"),
                        language.menuList("COMMON.NEXT.LORE", List.of("&7Go to page &f{page}"),
                                "{page}", String.valueOf(page + 1))
                )
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(
                Material.BARRIER,
                language.menu("COMMON.CLOSE.NAME", "&cClose"),
                language.menuList("AUCTION_HOUSE.BROWSE.CLOSE_LORE", List.of("&7Close the Auction House"))
        ));

        if (listings.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    language.menu("AUCTION_HOUSE.BROWSE.EMPTY_NAME", "&cNo Active Listings"),
                    language.menuList("AUCTION_HOUSE.BROWSE.EMPTY_LORE",
                            List.of("&7Use &f/ah sell <price> &7to create one."))
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
