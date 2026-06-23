package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import org.bukkit.entity.Player;

public final class AuctionHouseListingMenu extends BaseMenu {

    private final long listingId;

    public AuctionHouseListingMenu(
            UltimateDonutSmp plugin,
            long listingId,
            boolean ignoredBackToMyListings,
            int ignoredOriginPage,
            AuctionHouseManager.AuctionSort ignoredSort
    ) {
        super(plugin, AuctionHouseMenuSupport.configText(
                plugin,
                "GUI.LISTING.TITLE",
                "&8ᴀᴜᴄᴛɪᴏɴ #{id}",
                "{id}", String.valueOf(listingId)
        ), 27);
        this.listingId = listingId;
    }

    @Override
    public void build(Player player) {
    }

    @Override
    public void open(Player player) {
        AuctionListing listing = plugin.getAuctionHouseManager().getListing(listingId);
        if (listing == null || listing.sellerUuid().equals(player.getUniqueId())) {
            new PlayerAuctionGui(plugin, 1).open(player);
            return;
        }
        new ConfirmPurchaseGui(
                plugin,
                listing,
                plugin.getAuctionHouseManager().session(player.getUniqueId()).request()
        ).open(player);
    }
}
