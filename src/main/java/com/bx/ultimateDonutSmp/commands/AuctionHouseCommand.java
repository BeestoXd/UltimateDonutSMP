package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.models.PlayerPreference;
import com.bx.ultimateDonutSmp.menus.AuctionHouseBrowseMenu;
import com.bx.ultimateDonutSmp.menus.PlayerAuctionGui;
import com.bx.ultimateDonutSmp.menus.SellGui;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AuctionHouseCommand implements CommandExecutor, TabCompleter {

    private final UltimateDonutSmp plugin;

    public AuctionHouseCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLanguageManager().message(
                    "AUCTION_HOUSE.PLAYERS_ONLY",
                    "ᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."
            ));
            return true;
        }

        AuctionHouseManager manager = plugin.getAuctionHouseManager();
        String subcommand = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        if ("reload".equals(subcommand)) {
            handleReload(player);
            return true;
        }
        if (!manager.isEnabled()) {
            send(player, "AUCTION_HOUSE.DISABLED", "&cᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ.");
            return true;
        }

        switch (subcommand) {
            case "" -> {
                if (requirePermission(player, "use")) {
                    openBrowse(player);
                }
            }
            case "sell" -> {
                if (requirePermission(player, "sell")) {
                    handleSell(player, args);
                }
            }
            case "my" -> {
                if (requirePermission(player, "my")) {
                    openPlayerItems(player);
                }
            }
            case "claims" -> {
                if (!requirePermission(player, "claims")) {
                    return true;
                }
                if (!manager.isClaimsEnabled()) {
                    manager.processAutoClaims(player);
                    send(player, "AUCTION_HOUSE.CLAIMS_AUTOMATIC", "&eᴄʟᴀɪᴍѕ ᴀʀᴇ ᴄᴏʟʟᴇᴄᴛᴇᴅ ᴀᴜᴛᴏᴍᴀᴛɪᴄᴀʟʟʏ.");
                } else {
                    openPlayerItems(player);
                }
            }
            case "cancel" -> {
                if (requirePermission(player, "cancel")) {
                    handleCancel(player, args);
                }
            }
            case "limit" -> {
                if (requirePermission(player, "limit")) {
                    handleLimit(player);
                }
            }
            case "fastbuy" -> togglePreference(player, true);
            case "fastsell" -> togglePreference(player, false);
            default -> {
                if (requirePermission(player, "use")) {
                    openBrowse(player);
                }
            }
        }
        return true;
    }

    private void handleReload(Player player) {
        if (!PermissionUtils.has(player, "ultimatedonutsmp.admin.auctionhouse")) {
            send(player, "AUCTION_HOUSE.NO_ADMIN_PERMISSION", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ʀᴇʟᴏᴀᴅ ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ѕᴇᴛᴛɪɴɢѕ.");
            return;
        }
        plugin.getConfigManager().reloadAuctionHouse();
        plugin.getAuctionHouseManager().reload();
        send(player, "AUCTION_HOUSE.RELOADED", "&aᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ѕᴇᴛᴛɪɴɢѕ ʀᴇʟᴏᴀᴅᴇᴅ.");
    }

    private void handleSell(Player player, String[] args) {
        if (args.length < 2) {
            send(player, "AUCTION_HOUSE.SELL_USAGE", "&cᴜѕᴀɢᴇ: /ah ѕᴇʟʟ <price>");
            return;
        }

        double price;
        try {
            price = NumberUtils.parse(args[1]);
        } catch (NumberFormatException exception) {
            send(player, "AUCTION_HOUSE.INVALID_PRICE", "&cɪɴᴠᴀʟɪᴅ ᴘʀɪᴄᴇ.");
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType().isAir()) {
            send(player, "AUCTION_HOUSE.NO_ITEM_IN_HAND", "&cʜᴏʟᴅ ᴛʜᴇ ɪᴛᴇᴍ ʏᴏᴜ ᴡᴀɴᴛ ᴛᴏ ʟɪѕᴛ.");
            return;
        }

        AuctionHouseManager manager = plugin.getAuctionHouseManager();
        ItemStack escrow = hand.clone();
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        player.updateInventory();
        manager.getPreferenceAsync(player.getUniqueId()).whenComplete((preference, throwable) -> {
            if (!player.isOnline()) {
                manager.returnEscrow(player, escrow);
                return;
            }
            plugin.getSpigotScheduler().runEntity(player, () -> {
                    if (throwable != null || preference == null) {
                        manager.returnEscrow(player, escrow);
                        send(player, "AUCTION_HOUSE.CREATE_DATABASE_ERROR",
                                "&cᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ᴄᴏᴜʟᴅ ɴᴏᴛ ʟᴏᴀᴅ ʏᴏᴜʀ ʟɪѕᴛɪɴɢ ѕᴇᴛᴛɪɴɢѕ.");
                        return;
                    }
                    boolean fastSell = preference.fastSellEnabled()
                            && hasEither(player, "ultimatedonutsmp.auctionhouse.fastsell", "donutauction.fastsell");
                    if (fastSell) {
                        com.bx.ultimateDonutSmp.models.AuctionCategory category;
                        boolean autoDetect = plugin.getConfigManager().getAuctionHouse()
                                .getBoolean("SETTINGS.AUTO_DETECT_CATEGORY", false);
                        if (autoDetect) {
                            category = com.bx.ultimateDonutSmp.models.AuctionCategory.findCategoryForItem(escrow);
                        } else {
                            category = com.bx.ultimateDonutSmp.models.AuctionCategory.from(preference.lastCategory());
                        }

                        manager.createListingFromItem(
                                player,
                                escrow,
                                price,
                                preference.lastDurationHours(),
                                category
                        ).thenAccept(result -> plugin.getSpigotScheduler().runEntity(player, () ->
                                handleCreateResult(player, preference, price, result)
                        ));
                        return;
                    }

                    new SellGui(plugin, escrow, price, preference).open(player);
                });
        });
    }

    private void handleCreateResult(
            Player player,
            PlayerPreference preference,
            double price,
            AuctionHouseManager.CreateListingResult result
    ) {
        if (!result.success()) {
            player.sendMessage(ColorUtils.toComponent(resolveCreateFailure(result)));
            SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.FAIL"));
            return;
        }
        preference.lastPrice(price);
        plugin.getAuctionHouseManager().savePreference(preference);
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                "AUCTION_HOUSE.LISTING_CREATED",
                "{listing_id}", String.valueOf(result.listing().id()),
                "{item}", plugin.getAuctionHouseManager().describeItem(result.listing().item()),
                "{price}", NumberUtils.format(result.listing().price()),
                "{price_formatted}", plugin.getCurrencyManager().formatMoney(result.listing().price()),
                "{fee}", NumberUtils.format(result.listingFee()),
                "{fee_formatted}", plugin.getCurrencyManager().formatMoney(result.listingFee()),
                "{expires}", plugin.getAuctionHouseManager().formatRemaining(
                        result.listing().secondsRemaining(System.currentTimeMillis())
                )
        )));
        SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.SUCCESS"));
        openPlayerItems(player);
    }

    private void handleCancel(Player player, String[] args) {
        if (args.length < 2) {
            send(player, "AUCTION_HOUSE.CANCEL_USAGE", "&cᴜѕᴀɢᴇ: /ah ᴄᴀɴᴄᴇʟ <listingId>");
            return;
        }
        long listingId;
        try {
            listingId = Long.parseLong(args[1]);
        } catch (NumberFormatException exception) {
            send(player, "AUCTION_HOUSE.INVALID_LISTING_ID", "&cɪɴᴠᴀʟɪᴅ ʟɪѕᴛɪɴɢ ɪᴅ.");
            return;
        }

        plugin.getAuctionHouseManager().cancelListing(player, listingId)
                .thenAccept(result -> plugin.getSpigotScheduler().runEntity(player, () -> {
                    if (!result.success()) {
                        String key = switch (result.reason()) {
                            case DISABLED -> "AUCTION_HOUSE.DISABLED";
                            case NO_PERMISSION -> "AUCTION_HOUSE.NO_PERMISSION";
                            case LISTING_NOT_FOUND -> "AUCTION_HOUSE.LISTING_NOT_FOUND";
                            case NOT_OWNER -> "AUCTION_HOUSE.NOT_YOUR_LISTING";
                            case NOT_ACTIVE -> "AUCTION_HOUSE.LISTING_NOT_ACTIVE";
                            case DATABASE_ERROR -> "AUCTION_HOUSE.CANCEL_DATABASE_ERROR";
                        };
                        send(player, key, "&cᴛʜᴇ ʟɪѕᴛɪɴɢ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ᴄᴀɴᴄᴇʟʟᴇᴅ.");
                        return;
                    }
                    player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                            "AUCTION_HOUSE.LISTING_CANCELLED",
                            "{listing_id}", String.valueOf(result.listing().id()),
                            "{item}", plugin.getAuctionHouseManager().describeItem(result.listing().item())
                    )));
                    openPlayerItems(player);
                }));
    }

    private void handleLimit(Player player) {
        int active = plugin.getAuctionHouseManager().countActiveListings(player.getUniqueId());
        int limit = plugin.getAuctionHouseManager().getMaxActiveListings(player);
        String displayLimit = limit == Integer.MAX_VALUE
                ? plugin.getAuctionHouseManager().getText("UNLIMITED", "ᴜɴʟɪᴍɪᴛᴇᴅ")
                : String.valueOf(limit);
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                "AUCTION_HOUSE.LIMIT",
                "&6ʟɪѕᴛɪɴɢ ʟɪᴍɪᴛ: &e{active}&7/&e{limit}",
                "{active}", String.valueOf(active),
                "{limit}", displayLimit
        )));
    }

    private void togglePreference(Player player, boolean fastBuy) {
        String permission = fastBuy ? "fastbuy" : "fastsell";
        if (!canUse(player, permission)) {
            send(player, "AUCTION_HOUSE.NO_FAST_PERMISSION", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴜѕᴇ ᴛʜᴀᴛ ꜰᴀѕᴛ-ᴀᴄᴛɪᴏɴ ѕᴇᴛᴛɪɴɢ.");
            return;
        }

        plugin.getAuctionHouseManager().getPreferenceAsync(player.getUniqueId()).thenAccept(preference -> {
            boolean enabled;
            if (fastBuy) {
                preference.fastBuyEnabled(!preference.fastBuyEnabled());
                enabled = preference.fastBuyEnabled();
            } else {
                preference.fastSellEnabled(!preference.fastSellEnabled());
                enabled = preference.fastSellEnabled();
            }
            plugin.getAuctionHouseManager().savePreference(preference);
            boolean finalEnabled = enabled;
            plugin.getSpigotScheduler().runEntity(player, () -> player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            fastBuy ? "AUCTION_HOUSE.FAST_BUY_TOGGLED" : "AUCTION_HOUSE.FAST_SELL_TOGGLED",
                            "&6&lᴀᴜᴄᴛɪᴏɴʜᴏᴜѕᴇ &8» &e{setting} ɪѕ ɴᴏᴡ {state}&e.",
                            "{setting}", fastBuy
                                    ? plugin.getAuctionHouseManager().getText("FAST_BUY", "ꜰᴀѕᴛ ʙᴜʏ")
                                    : plugin.getAuctionHouseManager().getText("FAST_SELL", "ꜰᴀѕᴛ ѕᴇʟʟ"),
                            "{state}", finalEnabled
                                    ? plugin.getAuctionHouseManager().getText("ENABLED", "&aᴇɴᴀʙʟᴇᴅ")
                                    : plugin.getAuctionHouseManager().getText("DISABLED", "&cᴅɪѕᴀʙʟᴇᴅ")
                    )
            )));
        });
    }

    private void openBrowse(Player player) {
        plugin.getAuctionHouseManager().processAutoClaims(player);
        String category = "ALL";
        boolean autoFilter = plugin.getConfigManager().getAuctionHouse()
                .getBoolean("SETTINGS.AUTO_FILTER_BY_HAND", false);
        if (autoFilter) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand != null && hand.getType() != org.bukkit.Material.AIR) {
                category = com.bx.ultimateDonutSmp.models.AuctionCategory.findCategoryForItem(hand).name();
            }
        }
        new AuctionHouseBrowseMenu(
                plugin,
                1,
                plugin.getAuctionHouseManager().getDefaultSort(),
                category
        ).open(player);
    }

    private void openPlayerItems(Player player) {
        plugin.getAuctionHouseManager().processAutoClaims(player);
        new PlayerAuctionGui(plugin, 1).open(player);
    }

    private boolean hasEither(Player player, String first, String second) {
        return PermissionUtils.has(player, first) || PermissionUtils.has(player, second);
    }

    private boolean requirePermission(Player player, String action) {
        if (canUse(player, action)) {
            return true;
        }
        send(player, "AUCTION_HOUSE.NO_PERMISSION", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴜѕᴇ ᴛʜᴀᴛ ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ᴀᴄᴛɪᴏɴ.");
        return false;
    }

    private boolean canUse(Player player, String action) {
        return hasEither(
                player,
                "ultimatedonutsmp.auctionhouse." + action,
                "donutauction." + action
        ) || PermissionUtils.has(player, "ultimatedonutsmp.admin.auctionhouse");
    }

    private void send(Player player, String key, String fallback) {
        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(key, fallback)));
    }

    private String resolveCreateFailure(AuctionHouseManager.CreateListingResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessage("AUCTION_HOUSE.DISABLED");
            case NO_PERMISSION -> plugin.getConfigManager().getMessage("AUCTION_HOUSE.NO_PERMISSION");
            case NO_PLAYER_DATA -> plugin.getLanguageManager().message(
                    "AUCTION_HOUSE.PLAYER_DATA_UNAVAILABLE",
                    "&cʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ʟᴏᴀᴅᴇᴅ."
            );
            case NO_ITEM -> plugin.getConfigManager().getMessage("AUCTION_HOUSE.NO_ITEM_IN_HAND");
            case INVALID_ITEM -> plugin.getConfigManager().getMessage("AUCTION_HOUSE.ITEM_BLOCKED");
            case UNSAFE_ITEM -> plugin.getConfigManager().getMessageOrDefault(
                    "CRASH_PROTECTION.ITEM_BLOCKED",
                    "&cᴛʜᴀᴛ ɪᴛᴇᴍ ᴄᴀɴɴᴏᴛ ʙᴇ ᴜѕᴇᴅ ʜᴇʀᴇ. &7ʀᴇᴀѕᴏɴ: &f{reason}",
                    "{context}", "ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ",
                    "{reason}", result.safetyResult() == null ? "ᴜɴѕᴀꜰᴇ ɪᴛᴇᴍ ᴅᴀᴛᴀ" : result.safetyResult().reason()
            );
            case INVALID_PRICE -> plugin.getConfigManager().getMessage("AUCTION_HOUSE.PRICE_OUT_OF_RANGE");
            case INVALID_DURATION -> plugin.getConfigManager().getMessageOrDefault(
                    "AUCTION_HOUSE.INVALID_DURATION",
                    "&cᴛʜᴀᴛ ʟɪѕᴛɪɴɢ ᴅᴜʀᴀᴛɪᴏɴ ɪѕ ɴᴏᴛ ᴀʟʟᴏᴡᴇᴅ."
            );
            case NO_MONEY -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.NO_MONEY_FOR_FEE",
                    "{fee}", NumberUtils.format(result.listingFee()),
                    "{fee_formatted}", plugin.getCurrencyManager().formatMoney(result.listingFee())
            );
            case MAX_LISTINGS_REACHED -> plugin.getConfigManager().getMessage("AUCTION_HOUSE.MAX_LISTINGS_REACHED");
            case DATABASE_ERROR -> plugin.getLanguageManager().message(
                    "AUCTION_HOUSE.CREATE_DATABASE_ERROR",
                    "&cᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴀᴠᴇ ʏᴏᴜʀ ʟɪѕᴛɪɴɢ."
            );
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || args.length != 1) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        if (canUse(player, "sell")) {
            values.add("sell");
        }
        if (canUse(player, "my")) {
            values.add("my");
        }
        if (canUse(player, "limit")) {
            values.add("limit");
        }
        if (canUse(player, "fastbuy")) {
            values.add("fastbuy");
        }
        if (canUse(player, "fastsell")) {
            values.add("fastsell");
        }
        if (plugin.getAuctionHouseManager().isClaimsEnabled()
                && canUse(player, "claims")) {
            values.add("claims");
        }
        if (canUse(player, "cancel")) {
            values.add("cancel");
        }
        if (PermissionUtils.has(player, "ultimatedonutsmp.admin.auctionhouse")) {
            values.add("reload");
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.startsWith(prefix)).toList();
    }
}
