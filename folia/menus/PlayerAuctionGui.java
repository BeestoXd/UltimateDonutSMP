package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.AuctionClaim;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerAuctionGui extends BaseMenu {

    private int page;
    private final Map<Integer, Object> slotMappings = new HashMap<>();

    public PlayerAuctionGui(UltimateDonutSmp plugin, int page) {
        super(plugin, "&6Your Items", 54);
        this.page = Math.max(1, page);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        slotMappings.clear();

        // 1. Fetch active listings
        List<AuctionListing> activeListings = plugin.getAuctionHouseManager().getActiveListingsForSeller(
                player.getUniqueId(),
                plugin.getAuctionHouseManager().getDefaultSort()
        );

        // 2. Fetch unclaimed claims
        List<AuctionClaim> unclaimedClaims = plugin.getAuctionHouseManager().getUnclaimedClaims(player.getUniqueId());

        // 3. Combine them into a single list
        List<Object> entries = new ArrayList<>();
        entries.addAll(activeListings);
        entries.addAll(unclaimedClaims);

        int itemsPerPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) itemsPerPage));
        page = Math.min(page, totalPages);

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(entries.size(), startIndex + itemsPerPage);

        long now = System.currentTimeMillis();

        for (int i = 0; i < itemsPerPage && (startIndex + i) < endIndex; i++) {
            int slot = i;
            Object entry = entries.get(startIndex + i);
            ItemStack displayItem = null;

            if (entry instanceof AuctionListing listing) {
                displayItem = listing.item().clone();
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    lore.add("");
                    lore.add(ColorUtils.colorize("&7Price: &e$" + NumberUtils.format(listing.price())));
                    lore.add(ColorUtils.colorize("&7Time Remaining: &f" + plugin.getAuctionHouseManager().formatRemaining(listing.secondsRemaining(now) * 1000L)));
                    lore.add(ColorUtils.colorize("&7Status: &fActive"));
                    lore.add(ColorUtils.colorize("&aClick to cancel auction."));
                    meta.setLore(lore);
                    displayItem.setItemMeta(meta);
                }
                slotMappings.put(slot, listing);
            } else if (entry instanceof AuctionClaim claim) {
                if (claim.moneyClaim()) {
                    AuctionListing associatedListing = plugin.getAuctionHouseManager().getListing(claim.sourceListingId());
                    displayItem = (associatedListing != null) ? associatedListing.item().clone() : new ItemStack(Material.GOLD_INGOT);
                    ItemMeta meta = displayItem.getItemMeta();
                    if (meta != null) {
                        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                        lore.add("");
                        lore.add(ColorUtils.colorize("&7Proceeds: &a$" + NumberUtils.format(claim.moneyAmount())));
                        lore.add(ColorUtils.colorize("&7Status: &aSold"));
                        lore.add(ColorUtils.colorize("&aClick to mark proceeds collected."));
                        meta.setLore(lore);
                        displayItem.setItemMeta(meta);
                    }
                } else {
                    displayItem = claim.item().clone();
                    ItemMeta meta = displayItem.getItemMeta();
                    if (meta != null) {
                        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                        lore.add("");

                        // Try to find if cancelled or expired
                        String statusName = "Expired";
                        AuctionListing associatedListing = plugin.getAuctionHouseManager().getListing(claim.sourceListingId());
                        if (associatedListing != null && associatedListing.cancelled()) {
                            statusName = "Cancelled";
                        }

                        lore.add(ColorUtils.colorize("&7Status: &c" + statusName));
                        lore.add(ColorUtils.colorize("&aClick to reclaim item."));
                        meta.setLore(lore);
                        displayItem.setItemMeta(meta);
                    }
                }
                slotMappings.put(slot, claim);
            }

            if (displayItem != null) {
                set(slot, displayItem);
            }
        }

        // Slot 45: Previous Page
        set(45, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&fPrevious Page", List.of("&7Go back"))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));

        // Slot 49: Back to Auction
        set(49, ItemUtils.createItem(
                Material.CHEST,
                "&fBack to Auction",
                List.of("&7Return to the auction browser")
        ));

        // Slot 53: Next Page
        set(53, page < totalPages
                ? ItemUtils.createItem(Material.ARROW, "&fNext Page", List.of("&7Open next page"))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot == 45) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                page--;
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                new PlayerAuctionGui(plugin, page).open(player);
            }
            return;
        }
        if (slot == 49) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new AuctionHouseBrowseMenu(plugin, 1, plugin.getAuctionHouseManager().getDefaultSort()).open(player);
            return;
        }
        if (slot == 53) {
            // Count total entries to see if we actually can go next
            List<AuctionListing> activeListings = plugin.getAuctionHouseManager().getActiveListingsForSeller(
                    player.getUniqueId(),
                    plugin.getAuctionHouseManager().getDefaultSort()
            );
            List<AuctionClaim> unclaimedClaims = plugin.getAuctionHouseManager().getUnclaimedClaims(player.getUniqueId());
            int totalEntries = activeListings.size() + unclaimedClaims.size();
            int totalPages = Math.max(1, (int) Math.ceil(totalEntries / 45.0));

            if (page < totalPages) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                page++;
                plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
                new PlayerAuctionGui(plugin, page).open(player);
            }
            return;
        }

        Object clickedEntry = slotMappings.get(slot);
        if (clickedEntry == null) return;

        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));

        if (clickedEntry instanceof AuctionListing listing) {
            // Cancel active listing
            var result = plugin.getAuctionHouseManager().cancelListing(player, listing.id());
            if (result.success()) {
                String msg = plugin.getConfigManager().getMessage(
                        "AUCTION_HOUSE.LISTING_CANCELLED",
                        "{listing_id}", String.valueOf(listing.id()),
                        "{item}", plugin.getAuctionHouseManager().describeItem(listing.item())
                );
                player.sendMessage(ColorUtils.toComponent(msg));
            } else {
                String errorMsgKey = switch (result.reason()) {
                    case DISABLED -> "AUCTION_HOUSE.DISABLED";
                    case LISTING_NOT_FOUND -> "AUCTION_HOUSE.LISTING_NOT_FOUND";
                    case NOT_OWNER -> "AUCTION_HOUSE.NOT_YOUR_LISTING";
                    case NOT_ACTIVE -> "AUCTION_HOUSE.LISTING_NOT_ACTIVE";
                    default -> "AUCTION_HOUSE.LISTING_NOT_FOUND";
                };
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(errorMsgKey)));
            }
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new PlayerAuctionGui(plugin, page).open(player);

        } else if (clickedEntry instanceof AuctionClaim claim) {
            // Claim proceeds or item
            var result = plugin.getAuctionHouseManager().claim(player, claim.id());
            if (result.success()) {
                if (claim.moneyClaim()) {
                    String msg = plugin.getConfigManager().getMessage(
                            "AUCTION_HOUSE.CLAIMED_MONEY",
                            "{amount}", NumberUtils.format(claim.moneyAmount())
                    );
                    player.sendMessage(ColorUtils.toComponent(msg));
                } else {
                    String msg = plugin.getConfigManager().getMessage(
                            "AUCTION_HOUSE.CLAIMED_ITEM",
                            "{item}", plugin.getAuctionHouseManager().describeItem(claim.item())
                    );
                    player.sendMessage(ColorUtils.toComponent(msg));
                }
            } else {
                String errorMsgKey = switch (result.reason()) {
                    case DISABLED -> "AUCTION_HOUSE.DISABLED";
                    case CLAIM_NOT_FOUND -> "AUCTION_HOUSE.CLAIM_NOT_FOUND";
                    case NOT_OWNER -> "AUCTION_HOUSE.NOT_YOUR_CLAIM";
                    case ALREADY_CLAIMED -> "AUCTION_HOUSE.CLAIM_ALREADY_CLAIMED";
                    case INVENTORY_FULL -> "AUCTION_HOUSE.CLAIM_INVENTORY_FULL";
                    default -> "AUCTION_HOUSE.CLAIM_NOT_FOUND";
                };
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(errorMsgKey)));
            }
            plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
            new PlayerAuctionGui(plugin, page).open(player);
        }
    }

    @Override
    public void onClose(Player player) {
        if (!plugin.getAuctionHouseManager().stopNavigating(player.getUniqueId())) {
            plugin.getAuctionHouseManager().clearSearchQuery(player.getUniqueId());
        }
    }
}
