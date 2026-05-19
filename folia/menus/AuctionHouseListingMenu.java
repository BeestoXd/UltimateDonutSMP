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
        super(plugin, "&8ᴀᴜᴄᴛɪᴏɴ #" + listingId, 27);
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
        set(18, ItemUtils.createItem(Material.RED_STAINED_GLASS_PANE, "&cʙᴀᴄᴋ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴛʜᴇ ᴘʀᴇᴠɪᴏᴜѕ ᴍᴇɴᴜ")));

        if (listing == null) {
            set(13, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cʟɪѕᴛɪɴɢ ɴᴏᴛ ꜰᴏᴜɴᴅ",
                    List.of("&7ᴛʜɪѕ ʟɪѕᴛɪɴɢ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.")
            ));
            return;
        }

        boolean owner = listing.sellerUuid().equals(player.getUniqueId());
        set(11, ItemUtils.createItem(
                Material.PAPER,
                "&bʟɪѕᴛɪɴɢ ɪɴꜰᴏ",
                List.of(
                        "&7ɪᴅ: &f#" + listing.id(),
                        "&7ѕᴇʟʟᴇʀ: &f" + listing.sellerName(),
                        "&7ѕᴛᴀᴛᴜѕ: &f" + listing.status().name(),
                        "&7ᴘʀɪᴄᴇ: &a$" + NumberUtils.format(listing.price())
                )
        ));
        set(13, AuctionHouseMenuSupport.createListingDisplay(plugin, plugin.getAuctionHouseManager(), listing, owner));
        set(15, ItemUtils.createItem(
                Material.CLOCK,
                "&eᴛɪᴍɪɴɢ",
                List.of(
                        "&7ᴄʀᴇᴀᴛᴇᴅ: &f" + NumberUtils.formatTimeLong(Math.max(0L,
                                (System.currentTimeMillis() - listing.createdAt()) / 1000L)),
                        "&7ᴛɪᴍᴇ ʟᴇꜰᴛ: &f" + plugin.getAuctionHouseManager()
                                .formatRemaining(listing.secondsRemaining(System.currentTimeMillis())),
                        "&7ѕᴇʟʟᴇʀ ᴘᴀʏᴏᴜᴛ: &a$" + NumberUtils.format(listing.sellerPayout())
                )
        ));

        if (!listing.active()) {
            set(23, ItemUtils.createItem(Material.BARRIER, "&cʟɪѕᴛɪɴɢ ᴜɴᴀᴠᴀɪʟᴀʙʟᴇ", List.of("&7ᴛʜɪѕ ʟɪѕᴛɪɴɢ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ.")));
            return;
        }

        if (owner) {
            set(23, ItemUtils.createItem(
                    Material.REDSTONE,
                    "&cᴄᴀɴᴄᴇʟ ʟɪѕᴛɪɴɢ",
                    List.of(
                            "&7ᴍᴏᴠᴇ ᴛʜɪѕ ʟɪѕᴛɪɴɢ ɪɴᴛᴏ ʏᴏᴜʀ ᴄʟᴀɪᴍ ǫᴜᴇᴜᴇ.",
                            "",
                            "&eᴄʟɪᴄᴋ ᴛᴏ ᴄᴀɴᴄᴇʟ"
                    )
            ));
            return;
        }

        set(23, ItemUtils.createItem(
                Material.EMERALD,
                "&aʙᴜʏ ʟɪѕᴛɪɴɢ",
                List.of(
                        "&7ᴘʀɪᴄᴇ: &a$" + NumberUtils.format(listing.price()),
                        "&7ɪᴛᴇᴍ: &f" + plugin.getAuctionHouseManager().describeItem(listing.item()),
                        "",
                        "&eᴄʟɪᴄᴋ ᴛᴏ ᴘᴜʀᴄʜᴀѕᴇ"
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
            player.sendMessage(ColorUtils.toComponent("&cᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ɪѕ ѕᴛɪʟʟ ᴘʀᴏᴄᴇѕѕɪɴɢ ʏᴏᴜʀ ᴘʀᴇᴠɪᴏᴜѕ ᴀᴄᴛɪᴏɴ."));
            return;
        }

        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent("&cѕʟᴏᴡ ᴅᴏᴡɴ ꜰᴏʀ ᴀ ᴍᴏᴍᴇɴᴛ."));
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
            case NO_PLAYER_DATA -> "&cʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ʟᴏᴀᴅᴇᴅ.";
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
                    "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴇɴᴏᴜɢʜ ᴍᴏɴᴇʏ."
            );
            case INVENTORY_FULL -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.FULL_INVENTORY",
                    "&cʏᴏᴜ ɴᴇᴇᴅ ꜰʀᴇᴇ ɪɴᴠᴇɴᴛᴏʀʏ ѕᴘᴀᴄᴇ ᴛᴏ ʙᴜʏ ᴛʜᴀᴛ ɪᴛᴇᴍ."
            );
            case DATABASE_ERROR -> "&cᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ᴄᴏᴜʟᴅ ɴᴏᴛ ᴄᴏᴍᴘʟᴇᴛᴇ ᴛʜᴀᴛ ᴘᴜʀᴄʜᴀѕᴇ ʀɪɢʜᴛ ɴᴏᴡ.";
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
            case DATABASE_ERROR -> "&cᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ᴄᴏᴜʟᴅ ɴᴏᴛ ᴄᴀɴᴄᴇʟ ᴛʜᴀᴛ ʟɪѕᴛɪɴɢ ʀɪɢʜᴛ ɴᴏᴡ.";
        };
    }
}
