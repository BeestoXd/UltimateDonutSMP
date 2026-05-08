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
            sender.sendMessage("Player only.");
            return true;
        }

        AuctionHouseManager manager = plugin.getAuctionHouseManager();
        String subcommand = args.length == 0 ? "" : args[0].toLowerCase();
        if (subcommand.equals("reload")) {
            if (!player.hasPermission("ultimatedonutsmp.admin.auctionhouse")) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                        "AUCTION_HOUSE.NO_ADMIN_PERMISSION",
                        "&cYou do not have permission to reload Auction House settings."
                )));
                return true;
            }

            plugin.getConfigManager().reloadAuctionHouse();
            plugin.getAuctionHouseManager().reload();
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.RELOADED",
                    "&aAuction House config reloaded."
            )));
            return true;
        }

        if (!manager.isEnabled()) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.DISABLED",
                    "&cAuction House is currently disabled."
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
                            "&cUsage: /ah sell <price>"
                    )));
                    return true;
                }

                double price;
                try {
                    price = NumberUtils.parse(args[1]);
                } catch (NumberFormatException exception) {
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                            "AUCTION_HOUSE.INVALID_PRICE",
                            "&cInvalid price format."
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
                            "&cUsage: /ah cancel <listingId>"
                    )));
                    return true;
                }

                long listingId;
                try {
                    listingId = Long.parseLong(args[1]);
                } catch (NumberFormatException exception) {
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                            "AUCTION_HOUSE.INVALID_LISTING_ID",
                            "&cInvalid listing id."
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
            case NO_PLAYER_DATA -> "&cYour player data could not be loaded.";
            case NO_ITEM -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.NO_ITEM_IN_HAND",
                    "&cHold the item you want to list in your main hand."
            );
            case INVALID_ITEM -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.ITEM_BLOCKED",
                    "&cThat item cannot be listed in the Auction House."
            );
            case INVALID_PRICE -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.PRICE_OUT_OF_RANGE",
                    "&cThat price is outside the allowed range."
            );
            case NO_MONEY -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.NO_MONEY_FOR_FEE",
                    "&cYou do not have enough money to pay the listing fee."
            );
            case MAX_LISTINGS_REACHED -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.MAX_LISTINGS_REACHED",
                    "&cYou have reached your active listing limit."
            );
            case DATABASE_ERROR -> "&cAuction House could not save your listing. Try again.";
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
