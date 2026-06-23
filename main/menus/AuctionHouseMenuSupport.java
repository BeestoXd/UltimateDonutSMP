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
import org.bukkit.configuration.file.FileConfiguration;
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
                        "&7ѕᴇʟʟᴇʀ: &f{seller}",
                        "&7ᴘʀɪᴄᴇ: {price}",
                        "&7ʏᴏᴜ ʀᴇᴄᴇɪᴠᴇ: {payout}",
                        "&7ᴛɪᴍᴇ ʟᴇꜰᴛ: &f{time}",
                        "&7ʟɪѕᴛɪɴɢ ɪᴅ: &f#{id}",
                        ""
                ),
                "{seller}", plugin.getHideManager().publicName(listing.sellerUuid(), listing.sellerName()),
                "{price}", plugin.getCurrencyManager().formatMoney(listing.price()),
                "{payout}", plugin.getCurrencyManager().formatMoney(listing.sellerPayout()),
                "{time}", manager.formatRemaining(listing.secondsRemaining(System.currentTimeMillis())),
                "{id}", String.valueOf(listing.id())
        ));
        extraLore.add(ownedByViewer
                ? language.menu("AUCTION_HOUSE.ENTRY.MANAGE", "&eᴄʟɪᴄᴋ ᴛᴏ ᴍᴀɴᴀɢᴇ ʟɪѕᴛɪɴɢ")
                : language.menu("AUCTION_HOUSE.ENTRY.BUY", "&eᴄʟɪᴄᴋ ᴛᴏ ʙᴜʏ"));
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
                            "{money_color}{money_name} ᴄʟᴀɪᴍ",
                            "{money_color}", plugin.getCurrencyManager().color(CurrencyManager.CurrencyType.MONEY),
                            "{money_name}", plugin.getCurrencyManager().singular(CurrencyManager.CurrencyType.MONEY)
                    ),
                    language.menuList(
                            "AUCTION_HOUSE.ENTRY.MONEY_CLAIM_LORE",
                            List.of("&7ᴀᴍᴏᴜɴᴛ: {amount}", "&7ѕᴏᴜʀᴄᴇ ʟɪѕᴛɪɴɢ: &f#{id}", "", "&eᴄʟɪᴄᴋ ᴛᴏ ᴄʟᴀɪᴍ"),
                            "{amount}", plugin.getCurrencyManager().formatMoney(claim.moneyAmount()),
                            "{id}", String.valueOf(claim.sourceListingId())
                    )
            );
        }

        List<String> extraLore = language.menuList(
                "AUCTION_HOUSE.ENTRY.ITEM_CLAIM_LORE",
                List.of(
                        "",
                        "&7ᴄʟᴀɪᴍ ᴛʏᴘᴇ: &fʀᴇᴛᴜʀɴᴇᴅ ɪᴛᴇᴍ",
                        "&7ѕᴏᴜʀᴄᴇ ʟɪѕᴛɪɴɢ: &f#{id}",
                        "&7ᴄʀᴇᴀᴛᴇᴅ: &f{created}",
                        "",
                        "&eᴄʟɪᴄᴋ ᴛᴏ ᴄʟᴀɪᴍ"
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
                    plugin.getLanguageManager().menu("AUCTION_HOUSE.ENTRY.MISSING_NAME", "&cᴍɪѕѕɪɴɢ ɪᴛᴇᴍ"),
                    plugin.getLanguageManager().menuList("AUCTION_HOUSE.ENTRY.MISSING_LORE",
                            List.of("&7ᴛʜɪѕ ᴇɴᴛʀʏ ʜᴀѕ ɴᴏ ɪᴛᴇᴍ ᴅᴀᴛᴀ."))
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

    static int slot(UltimateDonutSmp plugin, String path, int fallback) {
        return plugin.getConfigManager().getAuctionHouse().getInt(path + ".SLOT", fallback);
    }

    static ItemStack control(
            UltimateDonutSmp plugin,
            String path,
            Material fallbackMaterial,
            String fallbackName,
            List<String> fallbackLore,
            String... replacements
    ) {
        FileConfiguration config = plugin.getConfigManager().getAuctionHouse();
        Material material = fallbackMaterial;
        String configuredMaterial = config.getString(path + ".MATERIAL", fallbackMaterial.name());
        if (configuredMaterial != null) {
            try {
                material = Material.valueOf(configuredMaterial.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        String name = replace(config.getString(path + ".NAME", fallbackName), replacements);
        List<String> configuredLore = config.getStringList(path + ".LORE");
        List<String> lore = (configuredLore.isEmpty() ? fallbackLore : configuredLore).stream()
                .map(line -> replace(line, replacements))
                .toList();
        return ItemUtils.createItem(material, name, lore);
    }

    static String configText(
            UltimateDonutSmp plugin,
            String path,
            String fallback,
            String... replacements
    ) {
        return replace(plugin.getConfigManager().getAuctionHouse().getString(path, fallback), replacements);
    }

    static List<String> configList(
            UltimateDonutSmp plugin,
            String path,
            List<String> fallback,
            String... replacements
    ) {
        List<String> configured = plugin.getConfigManager().getAuctionHouse().getStringList(path);
        return (configured.isEmpty() ? fallback : configured).stream()
                .map(line -> replace(line, replacements))
                .toList();
    }

    private static String replace(String value, String... replacements) {
        String result = value == null ? "" : value;
        for (int index = 0; index + 1 < replacements.length; index += 2) {
            result = result.replace(replacements[index], replacements[index + 1]);
        }
        return result;
    }
}
