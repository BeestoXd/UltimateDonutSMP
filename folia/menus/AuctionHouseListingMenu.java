package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class AuctionHouseListingMenu extends BaseMenu {

    private final long listingId;
    private final boolean backToMyListings;
    private final int originPage;
    private final AuctionHouseManager.AuctionSort sortMode;

    public AuctionHouseListingMenu(
            UltimateDonutSmp plugin,
            long listingId,
            boolean backToMyListings,
            int originPage,
            AuctionHouseManager.AuctionSort sortMode
    ) {
        super(plugin, "&8Auction #" + listingId, 27);
        this.listingId = listingId;
        this.backToMyListings = backToMyListings;
        this.originPage = Math.max(1, originPage);
        this.sortMode = sortMode == null ? plugin.getAuctionHouseManager().getDefaultSort() : sortMode;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        AuctionListing listing = plugin.getAuctionHouseManager().getListing(listingId);
        set(18, ItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "&cBack", List.of("&7Return to the previous menu")));

        if (listing == null) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cListing Not Found",
                    List.of("&7This listing no longer exists.")
            ));
            return;
        }

        boolean owner = listing.sellerUuid().equals(player.getUniqueId());
        set(11, ItemUtils.createItem(
                Material.PAPER,
                "&bListing Info",
                List.of(
                        "&7ID: &f#" + listing.id(),
                        "&7Seller: &f" + listing.sellerName(),
                        "&7Status: &f" + listing.status().name(),
                        "&7Price: &a$" + NumberUtils.format(listing.price())
                )
        ));
        set(13, AuctionHouseMenuSupport.createListingDisplay(plugin, plugin.getAuctionHouseManager(), listing, owner));
        set(15, ItemUtils.createItem(
                Material.CLOCK,
                "&eTiming",
                List.of(
                        "&7Created: &f" + NumberUtils.formatTimeLong(Math.max(0L,
                                (System.currentTimeMillis() - listing.createdAt()) / 1000L)),
                        "&7Time Left: &f" + plugin.getAuctionHouseManager()
                                .formatRemaining(listing.secondsRemaining(System.currentTimeMillis())),
                        "&7Seller Payout: &a$" + NumberUtils.format(listing.sellerPayout())
                )
        ));

        if (!listing.active()) {
            set(23, ItemUtils.createItem(Material.BARRIER, "&cListing Unavailable", List.of("&7This listing is no longer active.")));
            return;
        }

        if (owner) {
            set(23, ItemUtils.createItem(
                    Material.REDSTONE,
                    "&cCancel Listing",
                    List.of(
                            "&7Move this listing into your claim queue.",
                            "",
                            "&eClick to cancel"
                    )
            ));
            return;
        }

        set(23, ItemUtils.createItem(
                Material.EMERALD,
                "&aBuy Listing",
                List.of(
                        "&7Price: &a$" + NumberUtils.format(listing.price()),
                        "&7Item: &f" + plugin.getAuctionHouseManager().describeItem(listing.item()),
                        "",
                        "&eClick to purchase"
                )
        ));
    }

    @Override
    public void handleClick(int slot, Player player) {
        if (slot == 18) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            if (backToMyListings) {
                new AuctionHouseMyListingsMenu(plugin, originPage, sortMode).open(player);
            } else {
                new AuctionHouseBrowseMenu(plugin, originPage, sortMode).open(player);
            }
            return;
        }

        if (slot != 23) {
            return;
        }

        AuctionHouseManager manager = plugin.getAuctionHouseManager();
        AuctionListing listing = manager.getListing(listingId);
        if (listing == null) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.LISTING_NOT_FOUND",
                    "&cThat listing no longer exists."
            )));
            return;
        }

        if (!manager.beginAction(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cAuction House is still processing your previous action."));
            return;
        }

        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent("&cSlow down for a moment."));
                return;
            }
            manager.updateClickCooldown(player.getUniqueId());

            if (listing.sellerUuid().equals(player.getUniqueId())) {
                AuctionHouseManager.CancelListingResult result = manager.cancelListing(player, listing.id());
                if (!result.success()) {
                    player.sendMessage(ColorUtils.toComponent(resolveCancelFailure(result)));
                    SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.FAIL"));
                    return;
                }

                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                        "AUCTION_HOUSE.LISTING_CANCELLED",
                        "{listing_id}", String.valueOf(listing.id()),
                        "{item}", manager.describeItem(listing.item())
                )));
                SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.SUCCESS"));
                new AuctionHouseClaimsMenu(plugin, 1).open(player);
                return;
            }

            AuctionHouseManager.PurchaseListingResult result = manager.purchaseListing(player, listing.id());
            if (!result.success()) {
                player.sendMessage(ColorUtils.toComponent(resolvePurchaseFailure(result)));
                SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.FAIL"));
                new AuctionHouseBrowseMenu(plugin, originPage, sortMode).open(player);
                return;
            }

            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.PURCHASE_SUCCESS",
                    "{item}", manager.describeItem(listing.item()),
                    "{price}", NumberUtils.format(listing.price()),
                    "{seller}", listing.sellerName()
            )));
            SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.SUCCESS"));
            new AuctionHouseBrowseMenu(plugin, originPage, sortMode).open(player);
        } finally {
            manager.endAction(player.getUniqueId());
        }
    }

    private String resolvePurchaseFailure(AuctionHouseManager.PurchaseListingResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessage("AUCTION_HOUSE.DISABLED");
            case NO_PLAYER_DATA -> "&cYour player data could not be loaded.";
            case LISTING_NOT_FOUND -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.LISTING_NOT_FOUND",
                    "&cThat listing no longer exists."
            );
            case NOT_ACTIVE -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.LISTING_NOT_ACTIVE",
                    "&cThat listing is no longer active."
            );
            case OWN_LISTING -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.CANNOT_BUY_OWN",
                    "&cYou cannot buy your own listing."
            );
            case NO_MONEY -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.NOT_ENOUGH_MONEY",
                    "&cYou do not have enough money."
            );
            case INVENTORY_FULL -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.FULL_INVENTORY",
                    "&cYou need free inventory space to buy that item."
            );
            case DATABASE_ERROR -> "&cAuction House could not complete that purchase right now.";
        };
    }

    private String resolveCancelFailure(AuctionHouseManager.CancelListingResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessage("AUCTION_HOUSE.DISABLED");
            case LISTING_NOT_FOUND -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.LISTING_NOT_FOUND",
                    "&cThat listing no longer exists."
            );
            case NOT_OWNER -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.NOT_YOUR_LISTING",
                    "&cThat listing does not belong to you."
            );
            case NOT_ACTIVE -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.LISTING_NOT_ACTIVE",
                    "&cThat listing is no longer active."
            );
            case DATABASE_ERROR -> "&cAuction House could not cancel that listing right now.";
        };
    }
}
