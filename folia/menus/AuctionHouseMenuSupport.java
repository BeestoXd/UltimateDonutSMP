package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
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
        List<String> extraLore = new ArrayList<>();
        extraLore.add("");
        extraLore.add("&7ѕᴇʟʟᴇʀ: &f" + listing.sellerName());
        extraLore.add("&7ᴘʀɪᴄᴇ: &a$" + NumberUtils.format(listing.price()));
        extraLore.add("&7ʏᴏᴜ ʀᴇᴄᴇɪᴠᴇ: &a$" + NumberUtils.format(listing.sellerPayout()));
        extraLore.add("&7ᴛɪᴍᴇ ʟᴇꜰᴛ: &f" + manager.formatRemaining(listing.secondsRemaining(System.currentTimeMillis())));
        extraLore.add("&7ʟɪѕᴛɪɴɢ ɪᴅ: &f#" + listing.id());
        extraLore.add("");
        extraLore.add(ownedByViewer ? "&eᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ʟɪѕᴛɪɴɢ" : "&eᴄʟɪᴄᴋ ᴛᴏ ʙᴜʏ");
        return decorateItem(plugin, listing.item(), manager.describeItem(listing.item()), extraLore);
    }

    static ItemStack createClaimDisplay(
            UltimateDonutSmp plugin,
            AuctionHouseManager manager,
            AuctionClaim claim
    ) {
        if (claim.moneyClaim()) {
            return ItemUtils.createItem(
                    Material.SUNFLOWER,
                    "&aᴍᴏɴᴇʏ ᴄʟᴀɪᴍ",
                    List.of(
                            "&7ᴀᴍᴏᴜɴᴛ: &a$" + NumberUtils.format(claim.moneyAmount()),
                            "&7ѕᴏᴜʀᴄᴇ ʟɪѕᴛɪɴɢ: &f#" + claim.sourceListingId(),
                            "",
                            "&eᴄʟɪᴄᴋ ᴛᴏ ᴄʟᴀɪᴍ"
                    )
            );
        }

        List<String> extraLore = new ArrayList<>();
        extraLore.add("");
        extraLore.add("&7ᴄʟᴀɪᴍ ᴛʏᴘᴇ: &fʀᴇᴛᴜʀɴᴇᴅ ɪᴛᴇᴍ");
        extraLore.add("&7ѕᴏᴜʀᴄᴇ ʟɪѕᴛɪɴɢ: &f#" + claim.sourceListingId());
        extraLore.add("&7ᴄʀᴇᴀᴛᴇᴅ: &f" + NumberUtils.formatTimeLong(Math.max(0L,
                (System.currentTimeMillis() - claim.createdAt()) / 1000L)));
        extraLore.add("");
        extraLore.add("&eᴄʟɪᴄᴋ ᴛᴏ ᴄʟᴀɪᴍ");
        return decorateItem(plugin, claim.item(), manager.describeItem(claim.item()), extraLore);
    }

    static ItemStack decorateItem(
            UltimateDonutSmp plugin,
            ItemStack source,
            String fallbackDisplayName,
            List<String> extraLore
    ) {
        if (source == null || source.getType().isAir()) {
            return ItemUtils.createItem(Material.BARRIER, "&cᴍɪѕѕɪɴɢ ɪᴛᴇᴍ", List.of("&7ᴛʜɪѕ ᴇɴᴛʀʏ ʜᴀѕ ɴᴏ ɪᴛᴇᴍ ᴅᴀᴛᴀ."));
        }

        ItemStack display = source.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) {
            return display;
        }

        List<String> combinedLore = new ArrayList<>();
        if (meta.hasLore() && meta.lore() != null) {
            for (net.kyori.adventure.text.Component line : meta.lore()) {
                combinedLore.add(ColorUtils.toLegacyString(line));
            }
        }
        combinedLore.addAll(extraLore);

        if (!meta.hasDisplayName() && fallbackDisplayName != null && !fallbackDisplayName.isBlank()) {
            meta.displayName(ColorUtils.toComponent("&b" + fallbackDisplayName));
        }
        meta.lore(ColorUtils.toComponentList(combinedLore));
        display.setItemMeta(meta);
        return display;
    }
}
