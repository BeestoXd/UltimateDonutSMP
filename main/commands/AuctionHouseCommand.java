package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.models.PlayerPreference;
import com.bx.ultimateDonutSmp.menus.AuctionHouseBrowseMenu;
import com.bx.ultimateDonutSmp.menus.PlayerAuctionGui;
import com.bx.ultimateDonutSmp.menus.SellGui;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionHouseCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public AuctionHouseCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }

        AuctionHouseManager manager = plugin.getAuctionHouseManager();
        String subcommand = args.length == 0 ? "" : args[0].toLowerCase();

        if (subcommand.equals("reload")) {
            if (!PermissionUtils.has(player, "ultimatedonutsmp.admin.auctionhouse")) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                        "AUCTION_HOUSE.NO_ADMIN_PERMISSION",
                        "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʀᴇʟᴏᴀᴅ ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ѕᴇᴛᴛɪɴɢѕ."
                )));
                return true;
            }

            plugin.getConfigManager().reloadAuctionHouse();
            plugin.getAuctionHouseManager().reload();
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.RELOADED",
                    "&aᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ᴄᴏɴꜰɪɢ ʀᴇʟᴏᴀᴅᴇᴅ."
            )));
            return true;
        }

        if (!manager.isEnabled()) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.DISABLED",
                    "&cᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."
            )));
            return true;
        }

        if (args.length == 0) {
            new AuctionHouseBrowseMenu(plugin, 1, manager.getDefaultSort()).open(player);
            return true;
        }

        switch (subcommand) {
            case "sell" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                            "AUCTION_HOUSE.SELL_USAGE",
                            "&cᴜѕᴀɢᴇ: /ah sell <price>"
                    )));
                    return true;
                }

                double price;
                try {
                    price = NumberUtils.parse(args[1]);
                } catch (NumberFormatException exception) {
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                            "AUCTION_HOUSE.INVALID_PRICE",
                            "&cɪɴᴠᴀʟɪᴅ ᴘʀɪᴄᴇ ꜰᴏʀᴍᴀᴛ."
                    )));
                    return true;
                }

                PlayerPreference pref = manager.getPreference(player.getUniqueId());
                boolean fastSell = (pref != null && pref.fastSellEnabled() &&
                        (player.hasPermission("ultimatedonutsmp.auctionhouse.fastsell") || player.hasPermission("donutauction.fastsell")));

                if (fastSell) {
                    // Instant listing
                    int duration = pref.lastDurationHours();
                    String category = pref.lastCategory();
                    AuctionHouseManager.CreateListingResult result = manager.createListing(player, price, category, duration);

                    if (!result.success()) {
                        player.sendMessage(ColorUtils.toComponent(resolveCreateFailure(result)));
                        SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.FAIL"));
                        return true;
                    }

                    pref.lastPrice(price);
                    manager.savePreference(pref);

                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                            "AUCTION_HOUSE.LISTING_CREATED",
                            "{listing_id}", String.valueOf(result.listing().id()),
                            "{item}", manager.describeItem(result.listing().item()),
                            "{price}", NumberUtils.format(result.listing().price()),
                            "{price_formatted}", plugin.getCurrencyManager().formatMoney(result.listing().price()),
                            "{fee}", NumberUtils.format(result.listingFee()),
                            "{fee_formatted}", plugin.getCurrencyManager().formatMoney(result.listingFee()),
                            "{expires}", manager.formatRemaining(result.listing().secondsRemaining(System.currentTimeMillis()) * 1000L)
                    )));
                    SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.SUCCESS"));
                    new PlayerAuctionGui(plugin, 1).open(player);
                } else {
                    // GUI-based listing flow (with anti-duplication holding)
                    ItemStack handItem = player.getInventory().getItemInMainHand();
                    if (handItem == null || handItem.getType().isAir()) {
                        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                                "AUCTION_HOUSE.NO_ITEM_IN_HAND",
                                "&cʜᴏʟᴅ ᴛʜᴇ ɪᴛᴇᴍ ʏᴏᴜ ᴡᴀɴᴛ ᴛᴏ ʟɪѕᴛ ɪɴ ʏᴏᴜʀ ᴍᴀɪɴ ʜᴀɴᴅ."
                        )));
                        return true;
                    }

                    ItemStack toSell = handItem.clone();
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    player.updateInventory();

                    new SellGui(plugin, toSell, price).open(player);
                }
            }
            case "my", "claims", "cancel" -> {
                new PlayerAuctionGui(plugin, 1).open(player);
            }
            case "limit" -> {
                int activeCount = manager.countActiveListings(player.getUniqueId());
                int maxListings = manager.getMaxActiveListings(player);
                player.sendMessage(ColorUtils.colorize("&6Listing Limit: &e" + activeCount + "&7/&e" + maxListings));
            }
            case "fastbuy" -> {
                PlayerPreference pref = manager.getPreference(player.getUniqueId());
                if (pref != null) {
                    pref.fastBuyEnabled(!pref.fastBuyEnabled());
                    manager.savePreference(pref);
                    player.sendMessage(ColorUtils.colorize("&6&lAuctionHouse &8» &eFast Buy has been " + (pref.fastBuyEnabled() ? "&aenabled" : "&cdisabled") + "&e."));
                }
            }
            case "fastsell" -> {
                PlayerPreference pref = manager.getPreference(player.getUniqueId());
                if (pref != null) {
                    pref.fastSellEnabled(!pref.fastSellEnabled());
                    manager.savePreference(pref);
                    player.sendMessage(ColorUtils.colorize("&6&lAuctionHouse &8» &eFast Sell has been " + (pref.fastSellEnabled() ? "&aenabled" : "&cdisabled") + "&e."));
                }
            }
            default -> new AuctionHouseBrowseMenu(plugin, 1, manager.getDefaultSort()).open(player);
        }

        return true;
    }

    private String resolveCreateFailure(AuctionHouseManager.CreateListingResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessage("AUCTION_HOUSE.DISABLED");
            case NO_PLAYER_DATA -> "&cʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ʟᴏᴀᴅᴇᴅ.";
            case NO_ITEM -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.NO_ITEM_IN_HAND",
                    "&cʜᴏʟᴅ ᴛʜᴇ ɪᴛᴇᴍ ʏᴏᴜ ᴡᴀɴᴛ ᴛᴏ ʟɪѕᴛ ɪɴ ʏᴏᴜʀ ᴍᴀɪɴ ʜᴀɴᴅ."
            );
            case INVALID_ITEM -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.ITEM_BLOCKED",
                    "&cᴛʜᴀᴛ ɪᴛᴇᴍ ᴄᴀɴɴᴏᴛ ʙᴇ ʟɪѕᴛᴇᴅ ɪɴ ᴛʜᴇ ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ."
            );
            case UNSAFE_ITEM -> plugin.getConfigManager().getMessageOrDefault(
                    "CRASH_PROTECTION.ITEM_BLOCKED",
                    "&cThat item cannot be used here because its data looks unsafe. &7Context: &f{context}&7. Reason: &f{reason}",
                    "{context}", "Auction House",
                    "{reason}", result.safetyResult() == null ? "unsafe item data" : result.safetyResult().reason()
            );
            case INVALID_PRICE -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.PRICE_OUT_OF_RANGE",
                    "&cᴛʜᴀᴛ ᴘʀɪᴄᴇ ɪѕ ᴏᴜᴛѕɪᴅᴇ ᴛʜᴇ ᴀʟʟᴏᴡᴇᴅ ʀᴀɴɢᴇ."
            );
            case NO_MONEY -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.NO_MONEY_FOR_FEE",
                    "{fee}", NumberUtils.format(result.listingFee()),
                    "{fee_formatted}", plugin.getCurrencyManager().formatMoney(result.listingFee())
            );
            case MAX_LISTINGS_REACHED -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.MAX_LISTINGS_REACHED",
                    "&cʏᴏᴜ ʜᴀᴠᴇ ʀᴇᴀᴄʜᴇᴅ ʏᴏᴜʀ ᴀᴄᴛɪᴠᴇ ʟɪѕᴛɪɴɢ ʟɪᴍɪᴛ."
            );
            case DATABASE_ERROR -> "&cᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴀᴠᴇ ʏᴏᴜʀ ʟɪѕᴛɪɴɢ. ᴛʀʏ ᴀɢᴀɪɴ.";
        };
    }
}
