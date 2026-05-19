package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.menus.AuctionHouseBrowseMenu;
import com.bx.ultimateDonutSmp.menus.AuctionHouseClaimsMenu;
import com.bx.ultimateDonutSmp.menus.AuctionHouseMyListingsMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            if (!player.hasPermission("ultimatedonutsmp.admin.auctionhouse")) {
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

                AuctionHouseManager.CreateListingResult result = manager.createListing(player, price);
                if (!result.success()) {
                    player.sendMessage(ColorUtils.toComponent(resolveCreateFailure(result)));
                    SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.FAIL"));
                    return true;
                }

                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                        "AUCTION_HOUSE.LISTING_CREATED",
                        "{listing_id}", String.valueOf(result.listing().id()),
                        "{item}", manager.describeItem(result.listing().item()),
                        "{price}", NumberUtils.format(result.listing().price()),
                        "{fee}", NumberUtils.format(result.listingFee()),
                        "{expires}", manager.formatRemaining(result.listing().secondsRemaining(System.currentTimeMillis()))
                )));
                SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.SUCCESS"));
                new AuctionHouseMyListingsMenu(plugin, 1, manager.getDefaultSort()).open(player);
            }
            case "my" -> new AuctionHouseMyListingsMenu(plugin, 1, manager.getDefaultSort()).open(player);
            case "claims" -> new AuctionHouseClaimsMenu(plugin, 1).open(player);
            case "cancel" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                            "AUCTION_HOUSE.CANCEL_USAGE",
                            "&cᴜѕᴀɢᴇ: /ah cancel <listingid>"
                    )));
                    return true;
                }

                long listingId;
                try {
                    listingId = Long.parseLong(args[1]);
                } catch (NumberFormatException exception) {
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                            "AUCTION_HOUSE.INVALID_LISTING_ID",
                            "&cɪɴᴠᴀʟɪᴅ ʟɪѕᴛɪɴɢ ɪᴅ."
                    )));
                    return true;
                }

                AuctionHouseManager.CancelListingResult result = manager.cancelListing(player, listingId);
                if (!result.success()) {
                    player.sendMessage(ColorUtils.toComponent(resolveCancelFailure(result)));
                    SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.FAIL"));
                    return true;
                }

                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                        "AUCTION_HOUSE.LISTING_CANCELLED",
                        "{listing_id}", String.valueOf(listingId),
                        "{item}", manager.describeItem(result.listing().item())
                )));
                SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.SUCCESS"));
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
            case INVALID_PRICE -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.PRICE_OUT_OF_RANGE",
                    "&cᴛʜᴀᴛ ᴘʀɪᴄᴇ ɪѕ ᴏᴜᴛѕɪᴅᴇ ᴛʜᴇ ᴀʟʟᴏᴡᴇᴅ ʀᴀɴɢᴇ."
            );
            case NO_MONEY -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.NO_MONEY_FOR_FEE",
                    "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴇɴᴏᴜɢʜ ᴍᴏɴᴇʏ ᴛᴏ ᴘᴀʏ ᴛʜᴇ ʟɪѕᴛɪɴɢ ꜰᴇᴇ."
            );
            case MAX_LISTINGS_REACHED -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.MAX_LISTINGS_REACHED",
                    "&cʏᴏᴜ ʜᴀᴠᴇ ʀᴇᴀᴄʜᴇᴅ ʏᴏᴜʀ ᴀᴄᴛɪᴠᴇ ʟɪѕᴛɪɴɢ ʟɪᴍɪᴛ."
            );
            case DATABASE_ERROR -> "&cᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴀᴠᴇ ʏᴏᴜʀ ʟɪѕᴛɪɴɢ. ᴛʀʏ ᴀɢᴀɪɴ.";
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
