package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.managers.LanguageManager;
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
        super(plugin, resolveTitle(plugin, listingId), 27);
        this.listingId = listingId;
        this.backToMyListings = backToMyListings;
        this.originPage = Math.max(1, originPage);
        this.sortMode = sortMode == null ? plugin.getAuctionHouseManager().getDefaultSort() : sortMode;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        LanguageManager language = plugin.getLanguageManager();
        AuctionListing listing = plugin.getAuctionHouseManager().getListing(listingId);
        set(18, ItemUtils.createItem(
                Material.RED_STAINED_GLASS_PANE,
                language.menu("AUCTION_HOUSE.LISTING.BACK_NAME", "&cBack"),
                language.menuList("AUCTION_HOUSE.LISTING.BACK_LORE",
                        List.of("&7Return to the previous menu"))
        ));

        if (listing == null) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    language.menu("AUCTION_HOUSE.LISTING.NOT_FOUND_NAME", "&cListing Not Found"),
                    language.menuList("AUCTION_HOUSE.LISTING.NOT_FOUND_LORE",
                            List.of("&7This listing no longer exists."))
            ));
            return;
        }

        boolean owner = listing.sellerUuid().equals(player.getUniqueId());
        set(11, ItemUtils.createItem(
                Material.PAPER,
                language.menu("AUCTION_HOUSE.LISTING.INFO_NAME", "&bListing Info"),
                language.menuList(
                        "AUCTION_HOUSE.LISTING.INFO_LORE",
                        List.of(
                                "&7ID: &f#{id}",
                                "&7Seller: &f{seller}",
                                "&7Status: &f{status}",
                                "&7Price: {price}"
                        ),
                        "{id}", String.valueOf(listing.id()),
                        "{seller}", plugin.getHideManager().publicName(listing.sellerUuid(), listing.sellerName()),
                        "{status}", language.display("STATUSES", listing.status().name(), listing.status().name()),
                        "{price}", plugin.getCurrencyManager().formatMoney(listing.price())
                )
        ));
        set(13, AuctionHouseMenuSupport.createListingDisplay(plugin, plugin.getAuctionHouseManager(), listing, owner));
        set(15, ItemUtils.createItem(
                Material.CLOCK,
                language.menu("AUCTION_HOUSE.LISTING.TIMING_NAME", "&eTiming"),
                language.menuList(
                        "AUCTION_HOUSE.LISTING.TIMING_LORE",
                        List.of(
                                "&7Created: &f{created}",
                                "&7Time left: &f{time}",
                                "&7Seller payout: {payout}"
                        ),
                        "{created}", NumberUtils.formatTimeLong(Math.max(0L,
                                (System.currentTimeMillis() - listing.createdAt()) / 1000L)),
                        "{time}", plugin.getAuctionHouseManager()
                                .formatRemaining(listing.secondsRemaining(System.currentTimeMillis())),
                        "{payout}", plugin.getCurrencyManager().formatMoney(listing.sellerPayout())
                )
        ));

        if (!listing.active()) {
            set(23, ItemUtils.createItem(
                    Material.BARRIER,
                    language.menu("AUCTION_HOUSE.LISTING.UNAVAILABLE_NAME", "&cListing Unavailable"),
                    language.menuList("AUCTION_HOUSE.LISTING.UNAVAILABLE_LORE",
                            List.of("&7This listing is no longer active."))
            ));
            return;
        }

        if (owner) {
            set(23, ItemUtils.createItem(
                    Material.REDSTONE,
                    language.menu("AUCTION_HOUSE.LISTING.CANCEL_NAME", "&cCancel Listing"),
                    language.menuList("AUCTION_HOUSE.LISTING.CANCEL_LORE",
                            List.of("&7Move this listing into your claim queue.", "", "&eClick to cancel"))
            ));
            return;
        }

        set(23, ItemUtils.createItem(
                Material.EMERALD,
                language.menu("AUCTION_HOUSE.LISTING.BUY_NAME", "&aBuy Listing"),
                language.menuList(
                        "AUCTION_HOUSE.LISTING.BUY_LORE",
                        List.of("&7Price: {price}", "&7Item: &f{item}", "", "&eClick to purchase"),
                        "{price}", plugin.getCurrencyManager().formatMoney(listing.price()),
                        "{item}", plugin.getAuctionHouseManager().describeItem(listing.item())
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
                    "&cᴛʜᴀᴛ ʟɪѕᴛɪɴɢ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ."
            )));
            return;
        }

        if (!manager.beginAction(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(plugin.getLanguageManager().message(
                    "AUCTION_HOUSE.ACTION_IN_PROGRESS",
                    "&cAuction House is still processing your previous action."
            )));
            return;
        }

        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent(plugin.getLanguageManager().message(
                        "AUCTION_HOUSE.CLICK_COOLDOWN",
                        "&cSlow down for a moment."
                )));
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
                    "{price_formatted}", plugin.getCurrencyManager().formatMoney(listing.price()),
                    "{seller}", plugin.getHideManager().publicName(listing.sellerUuid(), listing.sellerName())
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
            case NO_PLAYER_DATA -> plugin.getLanguageManager().message(
                    "AUCTION_HOUSE.PLAYER_DATA_UNAVAILABLE",
                    "&cYour player data could not be loaded."
            );
            case LISTING_NOT_FOUND -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.LISTING_NOT_FOUND",
                    "&cᴛʜᴀᴛ ʟɪѕᴛɪɴɢ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ."
            );
            case NOT_ACTIVE -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.LISTING_NOT_ACTIVE",
                    "&cᴛʜᴀᴛ ʟɪѕᴛɪɴɢ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ."
            );
            case OWN_LISTING -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.CANNOT_BUY_OWN",
                    "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ʙᴜʏ ʏᴏᴜʀ ᴏᴡɴ ʟɪѕᴛɪɴɢ."
            );
            case NO_MONEY -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.NOT_ENOUGH_MONEY",
                    "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴇɴᴏᴜɢʜ "
                            + plugin.getCurrencyManager().plural(com.bx.ultimateDonutSmp.managers.CurrencyManager.CurrencyType.MONEY)
                            + "."
            );
            case INVENTORY_FULL -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.FULL_INVENTORY",
                    "&cʏᴏᴜ ɴᴇᴇᴅ ꜰʀᴇᴇ ɪɴᴠᴇɴᴛᴏʀʏ ѕᴘᴀᴄᴇ ᴛᴏ ʙᴜʏ ᴛʜᴀᴛ ɪᴛᴇᴍ."
            );
            case DATABASE_ERROR -> plugin.getLanguageManager().message(
                    "AUCTION_HOUSE.PURCHASE_DATABASE_ERROR",
                    "&cAuction House could not complete that purchase right now."
            );
        };
    }

    private String resolveCancelFailure(AuctionHouseManager.CancelListingResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessage("AUCTION_HOUSE.DISABLED");
            case LISTING_NOT_FOUND -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.LISTING_NOT_FOUND",
                    "&cᴛʜᴀᴛ ʟɪѕᴛɪɴɢ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ."
            );
            case NOT_OWNER -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.NOT_YOUR_LISTING",
                    "&cᴛʜᴀᴛ ʟɪѕᴛɪɴɢ ᴅᴏᴇѕ ɴᴏᴛ ʙᴇʟᴏɴɢ ᴛᴏ ʏᴏᴜ."
            );
            case NOT_ACTIVE -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.LISTING_NOT_ACTIVE",
                    "&cᴛʜᴀᴛ ʟɪѕᴛɪɴɢ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ."
            );
            case DATABASE_ERROR -> plugin.getLanguageManager().message(
                    "AUCTION_HOUSE.CANCEL_DATABASE_ERROR",
                    "&cAuction House could not cancel that listing right now."
            );
        };
    }

    private static String resolveTitle(UltimateDonutSmp plugin, long listingId) {
        LanguageManager language = plugin.getLanguageManager();
        if (language == null) {
            return "&8Auction #" + listingId;
        }
        return language.menu("AUCTION_HOUSE.LISTING.TITLE", "&8Auction #{id}",
                "{id}", String.valueOf(listingId));
    }
}
