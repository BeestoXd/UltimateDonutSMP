package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.AuctionHouseManager;
import com.bx.ultimateDonutSmp.models.AuctionClaim;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class AuctionHouseClaimsMenu extends BaseMenu {

    private final int page;

    public AuctionHouseClaimsMenu(UltimateDonutSmp plugin, int page) {
        super(plugin, plugin.getAuctionHouseManager().getClaimsTitle(), plugin.getAuctionHouseManager().getClaimsSize());
        this.page = Math.max(1, page);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<AuctionClaim> claims = plugin.getAuctionHouseManager().getUnclaimedClaims(player.getUniqueId());
        int itemsPerPage = plugin.getAuctionHouseManager().getClaimsItemsPerPage();
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(claims.size(), startIndex + itemsPerPage);

        for (int slot = 0; slot < itemsPerPage && slot < inventory.getSize() - 9; slot++) {
            int claimIndex = startIndex + slot;
            if (claimIndex >= endIndex) {
                break;
            }
            set(slot, AuctionHouseMenuSupport.createClaimDisplay(
                    plugin,
                    plugin.getAuctionHouseManager(),
                    claims.get(claimIndex)
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&bʙᴀᴄᴋ ᴛᴏ ᴍᴀʀᴋᴇᴛ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ")));
        set(lastRow + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 2, ItemUtils.createItem(Material.CHEST, "&bᴍʏ ʟɪѕᴛɪɴɢѕ", List.of("&7ᴍᴀɴᴀɢᴇ ʏᴏᴜʀ ᴀᴄᴛɪᴠᴇ ʟɪѕᴛɪɴɢѕ")));
        set(lastRow + 3, ItemUtils.createItem(Material.CLOCK, "&eʀᴇꜰʀᴇѕʜ", List.of("&7ʀᴇʟᴏᴀᴅ ʏᴏᴜʀ ᴄʟᴀɪᴍ ǫᴜᴇᴜᴇ")));
        set(lastRow + 5, ItemUtils.createItem(
                Material.BOOK,
                "&eᴘᴀɢᴇ " + page + "&7/&e" + getTotalPages(claims.size(), itemsPerPage),
                List.of("&7ᴘᴇɴᴅɪɴɢ ᴄʟᴀɪᴍѕ: &f" + claims.size())
        ));
        set(lastRow + 7, hasNextPage(claims.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ", List.of("&7ᴄʟᴏѕᴇ ᴛʜɪѕ ᴍᴇɴᴜ")));

        if (claims.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cɴᴏ ᴘᴇɴᴅɪɴɢ ᴄʟᴀɪᴍѕ",
                    List.of("&7ѕᴏʟᴅ ᴘᴀʏᴏᴜᴛѕ ᴀɴᴅ ʀᴇᴛᴜʀɴᴇᴅ ɪᴛᴇᴍѕ ᴡɪʟʟ ѕʜᴏᴡ ᴜᴘ ʜᴇʀᴇ.")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        int lastRow = inventory.getSize() - 9;
        List<AuctionClaim> claims = plugin.getAuctionHouseManager().getUnclaimedClaims(player.getUniqueId());
        int itemsPerPage = plugin.getAuctionHouseManager().getClaimsItemsPerPage();

        if (slot == lastRow) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new AuctionHouseBrowseMenu(plugin, 1, plugin.getAuctionHouseManager().getDefaultSort()).open(player);
            return;
        }
        if (slot == lastRow + 1) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new AuctionHouseClaimsMenu(plugin, page - 1).open(player);
            }
            return;
        }
        if (slot == lastRow + 2) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new AuctionHouseMyListingsMenu(plugin, 1, plugin.getAuctionHouseManager().getDefaultSort()).open(player);
            return;
        }
        if (slot == lastRow + 3) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new AuctionHouseClaimsMenu(plugin, page).open(player);
            return;
        }
        if (slot == lastRow + 7) {
            if (hasNextPage(claims.size(), itemsPerPage)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new AuctionHouseClaimsMenu(plugin, page + 1).open(player);
            }
            return;
        }
        if (slot == lastRow + 8) {
            player.closeInventory();
            return;
        }

        if (slot < 0 || slot >= itemsPerPage) {
            return;
        }

        int claimIndex = ((page - 1) * itemsPerPage) + slot;
        if (claimIndex >= claims.size()) {
            return;
        }

        AuctionHouseManager manager = plugin.getAuctionHouseManager();
        if (!manager.beginAction(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ɪѕ ѕᴛɪʟʟ ᴘʀᴏᴄᴇѕѕɪɴɢ ʏᴏᴜʀ ᴘʀᴇᴠɪᴏᴜѕ ᴀᴄᴛɪᴏɴ."));
            return;
        }

        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent("&cѕʟᴏᴡ ᴅᴏᴡɴ ꜰᴏʀ ᴀ ᴍᴏᴍᴇɴᴛ."));
                return;
            }
            manager.updateClickCooldown(player.getUniqueId());

            AuctionClaim claim = claims.get(claimIndex);
            AuctionHouseManager.ClaimResult result = manager.claim(player, claim.id());
            if (!result.success()) {
                player.sendMessage(ColorUtils.toComponent(resolveFailureMessage(result)));
                SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.FAIL"));
                return;
            }

            if (claim.moneyClaim()) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                        "AUCTION_HOUSE.CLAIMED_MONEY",
                        "{amount}", NumberUtils.format(claim.moneyAmount()),
                        "{amount_formatted}", plugin.getCurrencyManager().formatMoney(claim.moneyAmount())
                )));
            } else {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage(
                        "AUCTION_HOUSE.CLAIMED_ITEM",
                        "{item}", plugin.getAuctionHouseManager().describeItem(claim.item())
                )));
            }
            SoundUtils.play(player, plugin.getConfigManager().getSound("AUCTION_HOUSE.SUCCESS"));
            new AuctionHouseClaimsMenu(plugin, page).open(player);
        } finally {
            manager.endAction(player.getUniqueId());
        }
    }

    private int getTotalPages(int totalItems, int itemsPerPage) {
        return Math.max(1, (int) Math.ceil(totalItems / (double) itemsPerPage));
    }

    private boolean hasNextPage(int totalItems, int itemsPerPage) {
        return page < getTotalPages(totalItems, itemsPerPage);
    }

    private String resolveFailureMessage(AuctionHouseManager.ClaimResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessage("AUCTION_HOUSE.DISABLED");
            case CLAIM_NOT_FOUND -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.CLAIM_NOT_FOUND",
                    "&cᴛʜᴀᴛ ᴄʟᴀɪᴍ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ."
            );
            case NOT_OWNER -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.NOT_YOUR_CLAIM",
                    "&cᴛʜᴀᴛ ᴄʟᴀɪᴍ ᴅᴏᴇѕ ɴᴏᴛ ʙᴇʟᴏɴɢ ᴛᴏ ʏᴏᴜ."
            );
            case ALREADY_CLAIMED -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.CLAIM_ALREADY_CLAIMED",
                    "&cᴛʜᴀᴛ ᴄʟᴀɪᴍ ᴡᴀѕ ᴀʟʀᴇᴀᴅʏ ᴄᴏʟʟᴇᴄᴛᴇᴅ."
            );
            case INVENTORY_FULL -> plugin.getConfigManager().getMessage(
                    "AUCTION_HOUSE.CLAIM_INVENTORY_FULL",
                    "&cʏᴏᴜ ɴᴇᴇᴅ ᴀ ꜰʀᴇᴇ ɪɴᴠᴇɴᴛᴏʀʏ ѕʟᴏᴛ ᴛᴏ ᴄʟᴀɪᴍ ᴛʜᴀᴛ ɪᴛᴇᴍ."
            );
            case NO_PLAYER_DATA -> "&cʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ʟᴏᴀᴅᴇᴅ.";
            case DATABASE_ERROR -> "&cᴀᴜᴄᴛɪᴏɴ ʜᴏᴜѕᴇ ᴄᴏᴜʟᴅ ɴᴏᴛ ᴄᴏᴍᴘʟᴇᴛᴇ ᴛʜᴀᴛ ᴄʟᴀɪᴍ ʀɪɢʜᴛ ɴᴏᴡ.";
        };
    }
}
