package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.managers.CurrencyManager;
import com.bx.ultimateDonutSmp.managers.LanguageManager;
import com.bx.ultimateDonutSmp.models.AuctionClaim;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

final class AuctionHouseMenuSupport {

    private AuctionHouseMenuSupport() {}

    static ItemStack createListingDisplay(
            UltimateDonutSmp plugin,
            AuctionHouseManager manager,
            AuctionListing listing,
            boolean ownedByViewer
    ) {
        LanguageManager language = plugin.getLanguageManager();
        List<String> extraLore = new ArrayList<>(language.menuList(
                "AUCTION_HOUSE.ENTRY.LISTING_LORE",
                List.of(
                        "",
                        "&7Seller: &f{seller}",
                        "&7Price: {price}",
                        "&7You receive: {payout}",
                        "&7Time left: &f{time}",
                        "&7Listing ID: &f#{id}",
                        ""
                ),
                "{seller}", plugin.getHideManager().publicName(listing.sellerUuid(), listing.sellerName()),
                "{price}", plugin.getCurrencyManager().formatMoney(listing.price()),
                "{payout}", plugin.getCurrencyManager().formatMoney(listing.sellerPayout()),
                "{time}", manager.formatRemaining(listing.secondsRemaining(System.currentTimeMillis())),
                "{id}", String.valueOf(listing.id())
        ));
        extraLore.add(ownedByViewer
                ? language.menu("AUCTION_HOUSE.ENTRY.MANAGE", "&eClick to manage listing")
                : language.menu("AUCTION_HOUSE.ENTRY.BUY", "&eClick to buy"));
        return decorateItem(plugin, listing.item(), manager.describeItem(listing.item()), extraLore);
    }

    static ItemStack createClaimDisplay(
            UltimateDonutSmp plugin,
            AuctionHouseManager manager,
            AuctionClaim claim
    ) {
        LanguageManager language = plugin.getLanguageManager();
        if (claim.moneyClaim()) {
            return ItemUtils.createItem(
                    Material.SUNFLOWER,
                    language.menu(
                            "AUCTION_HOUSE.ENTRY.MONEY_CLAIM_NAME",
                            "{money_color}{money_name} Claim",
                            "{money_color}", plugin.getCurrencyManager().color(CurrencyManager.CurrencyType.MONEY),
                            "{money_name}", plugin.getCurrencyManager().singular(CurrencyManager.CurrencyType.MONEY)
                    ),
                    language.menuList(
                            "AUCTION_HOUSE.ENTRY.MONEY_CLAIM_LORE",
                            List.of("&7Amount: {amount}", "&7Source listing: &f#{id}", "", "&eClick to claim"),
                            "{amount}", plugin.getCurrencyManager().formatMoney(claim.moneyAmount()),
                            "{id}", String.valueOf(claim.sourceListingId())
                    )
            );
        }

        List<String> extraLore = language.menuList(
                "AUCTION_HOUSE.ENTRY.ITEM_CLAIM_LORE",
                List.of(
                        "",
                        "&7Claim type: &fReturned Item",
                        "&7Source listing: &f#{id}",
                        "&7Created: &f{created}",
                        "",
                        "&eClick to claim"
                ),
                "{id}", String.valueOf(claim.sourceListingId()),
                "{created}", NumberUtils.formatTimeLong(Math.max(0L,
                        (System.currentTimeMillis() - claim.createdAt()) / 1000L))
        );
        return decorateItem(plugin, claim.item(), manager.describeItem(claim.item()), extraLore);
    }

    static ItemStack decorateItem(
            UltimateDonutSmp plugin,
            ItemStack source,
            String fallbackDisplayName,
        List<String> extraLore
    ) {
        if (source == null || source.getType().isAir()) {
            return ItemUtils.createItem(
                    Material.BARRIER,
                    plugin.getLanguageManager().menu("AUCTION_HOUSE.ENTRY.MISSING_NAME", "&cMissing Item"),
                    plugin.getLanguageManager().menuList("AUCTION_HOUSE.ENTRY.MISSING_LORE",
                            List.of("&7This entry has no item data."))
            );
        }

        ItemStack display = source.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) {
            return display;
        }

        List<String> combinedLore = new ArrayList<>();
        if (meta.hasLore() && meta.getLore() != null) {
            for (String line : meta.getLore()) {
                combinedLore.add(ColorUtils.toLegacyString(line));
            }
        }
        combinedLore.addAll(extraLore);

        if (!meta.hasDisplayName() && fallbackDisplayName != null && !fallbackDisplayName.isBlank()) {
            meta.setDisplayName(ColorUtils.toComponent("&b" + fallbackDisplayName));
        }
        meta.setLore(ColorUtils.toComponentList(combinedLore));
        display.setItemMeta(meta);
        return display;
    }
}
