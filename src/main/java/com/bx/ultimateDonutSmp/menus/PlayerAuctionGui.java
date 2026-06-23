package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.AuctionClaim;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.models.AuctionPlayerEntries;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
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

public final class PlayerAuctionGui extends BaseMenu {

    private static final int REFRESH_SLOT = 48;
    private static final int BACK_SLOT = 49;
    private static final int PAGE_SLOT = 50;

    private int page;
    private final Map<Integer, Object> entriesBySlot = new HashMap<>();
    private int totalPages = 1;

    public PlayerAuctionGui(UltimateDonutSmp plugin, int page) {
        super(plugin, plugin.getAuctionHouseManager().getMyListingsTitle(), 54);
        this.page = Math.max(1, page);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);
        entriesBySlot.clear();

        List<Object> entries = AuctionPlayerEntries.combine(
                plugin.getAuctionHouseManager().getPlayerListings(player.getUniqueId()),
                plugin.getAuctionHouseManager().getUnclaimedClaims(player.getUniqueId()),
                plugin.getAuctionHouseManager().isClaimsEnabled()
        );

        int perPage = plugin.getAuctionHouseManager().getMyListingsItemsPerPage();
        totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) perPage));
        page = Math.min(page, totalPages);
        int from = (page - 1) * perPage;
        int to = Math.min(entries.size(), from + perPage);
        for (int index = from; index < to; index++) {
            int slot = index - from;
            Object entry = entries.get(index);
            ItemStack display = entry instanceof AuctionListing listing
                    ? listingDisplay(listing)
                    : claimDisplay((AuctionClaim) entry);
            set(slot, display);
            entriesBySlot.put(slot, entry);
        }

        int previousSlot = AuctionHouseMenuSupport.slot(plugin, "GUI.PLAYER_ITEMS.CONTROLS.PREVIOUS", 45);
        int nextSlot = AuctionHouseMenuSupport.slot(plugin, "GUI.PLAYER_ITEMS.CONTROLS.NEXT", 53);
        set(previousSlot, page > 1
                ? control("PREVIOUS", Material.ARROW, "&fᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ",
                List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f{page}"), "{page}", String.valueOf(page - 1))
                : control("FILLER", Material.BLACK_STAINED_GLASS_PANE, "&7 ", List.of()));
        set(BACK_SLOT, control(
                "BACK",
                Material.CHEST,
                "&fʙᴀᴄᴋ ᴛᴏ ᴀᴜᴄᴛɪᴏɴ",
                List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴛʜᴇ ᴀᴜᴄᴛɪᴏɴ ʙʀᴏᴡѕᴇʀ")
        ));
        set(REFRESH_SLOT, control(
                "REFRESH",
                Material.ANVIL,
                "&fʀᴇꜰʀᴇѕʜ",
                List.of("&7ʀᴇʟᴏᴀᴅ ʏᴏᴜʀ ᴀᴜᴄᴛɪᴏɴ ʜɪѕᴛᴏʀʏ")
        ));
        set(PAGE_SLOT, control(
                "PAGE",
                Material.BOOK,
                "&fᴘᴀɢᴇ {page}/{pages}",
                List.of("&7ᴇɴᴛʀɪᴇѕ: &f{count}"),
                "{page}", String.valueOf(page),
                "{pages}", String.valueOf(totalPages),
                "{count}", String.valueOf(entries.size())
        ));
        set(nextSlot, page < totalPages
                ? control("NEXT", Material.ARROW, "&fɴᴇxᴛ ᴘᴀɢᴇ",
                List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f{page}"), "{page}", String.valueOf(page + 1))
                : control("FILLER", Material.BLACK_STAINED_GLASS_PANE, "&7 ", List.of()));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        if (slot == AuctionHouseMenuSupport.slot(plugin, "GUI.PLAYER_ITEMS.CONTROLS.PREVIOUS", 45)
                && page > 1) {
            navigate(player, () -> new PlayerAuctionGui(plugin, page - 1).open(player));
            return;
        }
        if (slot == BACK_SLOT) {
            var request = plugin.getAuctionHouseManager().session(player.getUniqueId()).request();
            navigate(player, () -> new AuctionHouseBrowseMenu(
                    plugin,
                    request.page(),
                    request.sort(),
                    request.category().name()
            ).open(player));
            return;
        }
        if (slot == REFRESH_SLOT) {
            plugin.getAuctionHouseManager().refreshCache().thenRun(() ->
                    plugin.getSpigotScheduler().runEntity(
                            player,
                            () -> navigate(player, () -> new PlayerAuctionGui(plugin, page).open(player))
                    )
            );
            return;
        }
        if (slot == AuctionHouseMenuSupport.slot(plugin, "GUI.PLAYER_ITEMS.CONTROLS.NEXT", 53)
                && page < totalPages) {
            navigate(player, () -> new PlayerAuctionGui(plugin, page + 1).open(player));
            return;
        }

        Object entry = entriesBySlot.get(slot);
        if (entry instanceof AuctionListing listing && listing.active()) {
            plugin.getAuctionHouseManager().cancelListing(player, listing.id())
                    .thenAccept(result -> plugin.getSpigotScheduler().runEntity(player, () -> {
                        if (result.success()) {
                            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                                    "AUCTION_HOUSE.LISTING_CANCELLED",
                                    "{listing_id}", String.valueOf(listing.id()),
                                    "{item}", plugin.getAuctionHouseManager().describeItem(listing.item())
                            )));
                            SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.SUCCESS"));
                        } else {
                            String key = switch (result.reason()) {
                                case DISABLED -> "AUCTION_HOUSE.DISABLED";
                                case NO_PERMISSION -> "AUCTION_HOUSE.NO_PERMISSION";
                                case LISTING_NOT_FOUND -> "AUCTION_HOUSE.LISTING_NOT_FOUND";
                                case NOT_OWNER -> "AUCTION_HOUSE.NOT_YOUR_LISTING";
                                case NOT_ACTIVE -> "AUCTION_HOUSE.LISTING_NOT_ACTIVE";
                                case DATABASE_ERROR -> "AUCTION_HOUSE.CANCEL_DATABASE_ERROR";
                            };
                            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(key)));
                            SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.FAIL"));
                        }
                        navigate(player, () -> new PlayerAuctionGui(plugin, page).open(player));
                    }));
            return;
        }
        if (entry instanceof AuctionClaim claim && plugin.getAuctionHouseManager().isClaimsEnabled()) {
            plugin.getAuctionHouseManager().claim(player, claim.id())
                    .thenAccept(result -> plugin.getSpigotScheduler().runEntity(player, () -> {
                        if (result.success()) {
                            String key = claim.moneyClaim()
                                    ? "AUCTION_HOUSE.CLAIMED_MONEY"
                                    : "AUCTION_HOUSE.CLAIMED_ITEM";
                            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                                    key,
                                    "{amount}", NumberUtils.format(claim.moneyAmount()),
                                    "{amount_formatted}", plugin.getCurrencyManager().formatMoney(claim.moneyAmount()),
                                    "{item}", plugin.getAuctionHouseManager().describeItem(claim.item())
                            )));
                            SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.SUCCESS"));
                        } else {
                            String key = switch (result.reason()) {
                                case DISABLED -> "AUCTION_HOUSE.DISABLED";
                                case NO_PERMISSION -> "AUCTION_HOUSE.NO_PERMISSION";
                                case CLAIM_NOT_FOUND -> "AUCTION_HOUSE.CLAIM_NOT_FOUND";
                                case NOT_OWNER -> "AUCTION_HOUSE.NOT_YOUR_CLAIM";
                                case ALREADY_CLAIMED -> "AUCTION_HOUSE.CLAIM_ALREADY_CLAIMED";
                                case INVENTORY_FULL -> "AUCTION_HOUSE.CLAIM_INVENTORY_FULL";
                                case NO_PLAYER_DATA, DATABASE_ERROR -> "AUCTION_HOUSE.CLAIM_DATABASE_ERROR";
                            };
                            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(key)));
                            SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.FAIL"));
                        }
                        navigate(player, () -> new PlayerAuctionGui(plugin, page).open(player));
                    }));
        }
    }

    private ItemStack listingDisplay(AuctionListing listing) {
        ItemStack display = listing.item().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) {
            return display;
        }
        List<String> lore = meta.hasLore() && meta.getLore() != null
                ? new ArrayList<>(meta.getLore())
                : new ArrayList<>();
        String status = AuctionHouseMenuSupport.configText(
                plugin,
                "GUI.PLAYER_ITEMS.STATUS." + listing.status().name() + ".NAME",
                listing.status().name()
        );
        lore.addAll(AuctionHouseMenuSupport.configList(
                plugin,
                "GUI.PLAYER_ITEMS.LISTING.BASE_LORE",
                List.of("", "&7ᴘʀɪᴄᴇ: {price}", "&7ѕᴛᴀᴛᴜѕ: &f{status}"),
                "{price}", plugin.getCurrencyManager().formatMoney(listing.price()),
                "{status}", status
        ).stream().map(ColorUtils::colorize).toList());
        if (listing.active()) {
            lore.addAll(AuctionHouseMenuSupport.configList(
                    plugin,
                    "GUI.PLAYER_ITEMS.LISTING.ACTIVE_LORE",
                    List.of("&7ᴛɪᴍᴇ ʀᴇᴍᴀɪɴɪɴɢ: &f{time}", "&eᴄʟɪᴄᴋ ᴛᴏ ᴄᴀɴᴄᴇʟ ᴛʜɪѕ ʟɪѕᴛɪɴɢ."),
                    "{time}", plugin.getAuctionHouseManager().formatRemaining(
                            listing.secondsRemaining(System.currentTimeMillis())
                    )
            ).stream().map(ColorUtils::colorize).toList());
        } else if (listing.sold()) {
            lore.addAll(AuctionHouseMenuSupport.configList(
                    plugin,
                    "GUI.PLAYER_ITEMS.LISTING.SOLD_LORE",
                    List.of("&aѕᴏʟᴅ ᴛᴏ ᴀɴᴏᴛʜᴇʀ ᴘʟᴀʏᴇʀ.")
            ).stream().map(ColorUtils::colorize).toList());
        } else if (listing.cancelled()) {
            lore.addAll(AuctionHouseMenuSupport.configList(
                    plugin,
                    "GUI.PLAYER_ITEMS.LISTING.CANCELLED_LORE",
                    List.of("&cᴄᴀɴᴄᴇʟʟᴇᴅ.")
            ).stream().map(ColorUtils::colorize).toList());
        } else {
            lore.addAll(AuctionHouseMenuSupport.configList(
                    plugin,
                    "GUI.PLAYER_ITEMS.LISTING.EXPIRED_LORE",
                    List.of("&cᴇxᴘɪʀᴇᴅ.")
            ).stream().map(ColorUtils::colorize).toList());
        }
        meta.setLore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private ItemStack claimDisplay(AuctionClaim claim) {
        return AuctionHouseMenuSupport.createClaimDisplay(
                plugin,
                plugin.getAuctionHouseManager(),
                claim
        );
    }

    private ItemStack control(
            String key,
            Material material,
            String name,
            List<String> lore,
            String... replacements
    ) {
        return AuctionHouseMenuSupport.control(
                plugin,
                "GUI.PLAYER_ITEMS.CONTROLS." + key,
                material,
                name,
                lore,
                replacements
        );
    }

    private void navigate(Player player, Runnable action) {
        SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
        plugin.getAuctionHouseManager().startNavigating(player.getUniqueId());
        action.run();
    }

    @Override
    public void onClose(Player player) {
        plugin.getAuctionHouseManager().stopNavigating(player.getUniqueId());
    }
}
