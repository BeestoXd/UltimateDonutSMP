package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ConfirmPurchaseGui extends BaseMenu {

    private final AuctionListing listing;

    public ConfirmPurchaseGui(UltimateDonutSmp plugin, AuctionListing listing) {
        super(plugin, "&6Confirm Purchase", 27);
        this.listing = listing;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        // Slot 11: Cloned item with price/seller appended
        ItemStack displayItem = listing.item().clone();
        ItemMeta meta = displayItem.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add(ColorUtils.colorize("&6Price: &e$" + NumberUtils.format(listing.price())));
            lore.add(ColorUtils.colorize("&7Seller: &f" + listing.sellerName()));
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }
        set(11, displayItem);

        // Slot 15: Lime Pane (Confirm Purchase)
        set(15, ItemUtils.createItem(
                Material.LIME_STAINED_GLASS_PANE,
                "&aConfirm Purchase",
                List.of(
                        "&7Click to buy this item",
                        "&6Price: &e$" + NumberUtils.format(listing.price())
                )
        ));

        // Slot 22: Red Pane (Cancel)
        set(22, ItemUtils.createItem(
                Material.RED_STAINED_GLASS_PANE,
                "&cCancel",
                List.of(
                        "&7Go back to the auction house"
                )
        ));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot == 15) {
            // Confirm Purchase
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

            // Register navigating so search query is not wiped when browse menu reopens
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
            // Reopen Browse Menu
            new AuctionHouseBrowseMenu(plugin, 1, plugin.getAuctionHouseManager().getDefaultSort()).open(player);
        } else if (slot == 22) {
            // Cancel
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new AuctionHouseBrowseMenu(plugin, 1, plugin.getAuctionHouseManager().getDefaultSort()).open(player);
        }
    }

    @Override
    public void onClose(Player player) {
        if (!plugin.getAuctionHouseManager().stopNavigating(player.getUniqueId())) {
            plugin.getAuctionHouseManager().clearSearchQuery(player.getUniqueId());
        }
    }
}
