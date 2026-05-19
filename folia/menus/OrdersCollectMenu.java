package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OrdersManager;
import com.bx.ultimateDonutSmp.models.OrderCollectionClaim;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OrdersCollectMenu extends BaseMenu {

    private final int page;

    public OrdersCollectMenu(UltimateDonutSmp plugin, int page) {
        super(plugin, plugin.getOrdersManager().getCollectTitle(), plugin.getOrdersManager().getCollectSize());
        this.page = Math.max(1, page);
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.GRAY_STAINED_GLASS_PANE);

        List<OrderCollectionClaim> claims = plugin.getOrdersManager().getUnclaimedClaims(player.getUniqueId());
        int itemsPerPage = plugin.getOrdersManager().getCollectItemsPerPage();
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(claims.size(), startIndex + itemsPerPage);

        for (int slot = 0; slot < itemsPerPage && slot < inventory.getSize() - 9; slot++) {
            int claimIndex = startIndex + slot;
            if (claimIndex >= endIndex) {
                break;
            }
            set(slot, OrdersMenuSupport.createClaimDisplay(
                    plugin,
                    plugin.getOrdersManager(),
                    claims.get(claimIndex)
            ));
        }

        int lastRow = inventory.getSize() - 9;
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&bʙᴀᴄᴋ ᴛᴏ ʙᴏᴀʀᴅ", List.of("&7ʀᴇᴛᴜʀɴ ᴛᴏ ᴀᴄᴛɪᴠᴇ ᴏʀᴅᴇʀѕ")));
        set(lastRow + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aᴘʀᴇᴠɪᴏᴜѕ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 2, ItemUtils.createItem(Material.WRITABLE_BOOK, "&bᴍʏ ᴏʀᴅᴇʀѕ", List.of("&7ᴠɪᴇᴡ ʏᴏᴜʀ ᴏʀᴅᴇʀѕ")));
        set(lastRow + 3, ItemUtils.createItem(Material.CLOCK, "&eʀᴇꜰʀᴇѕʜ", List.of("&7ʀᴇʟᴏᴀᴅ ʏᴏᴜʀ ᴄᴏʟʟᴇᴄᴛ ǫᴜᴇᴜᴇ")));
        set(lastRow + 5, ItemUtils.createItem(
                Material.BOOK,
                "&eᴘᴀɢᴇ " + page + "&7/&e" + getTotalPages(claims.size(), itemsPerPage),
                List.of("&7ᴘᴇɴᴅɪɴɢ ᴄʟᴀɪᴍѕ: &f" + claims.size())
        ));
        set(lastRow + 7, hasNextPage(claims.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aɴᴇxᴛ ᴘᴀɢᴇ", List.of("&7ɢᴏ ᴛᴏ ᴘᴀɢᴇ &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cᴄʟᴏѕᴇ", List.of("&7ᴄʟᴏѕᴇ ᴏʀᴅᴇʀѕ")));

        if (claims.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cɴᴏᴛʜɪɴɢ ᴛᴏ ᴄᴏʟʟᴇᴄᴛ",
                    List.of("&7ᴅᴇʟɪᴠᴇʀᴇᴅ ɪᴛᴇᴍѕ ᴀɴᴅ ʀᴇꜰᴜɴᴅѕ ᴡɪʟʟ ᴀᴘᴘᴇᴀʀ ʜᴇʀᴇ.")
            ));
        }
    }

    @Override
    public void handleClick(int slot, Player player) {
        int lastRow = inventory.getSize() - 9;
        List<OrderCollectionClaim> claims = plugin.getOrdersManager().getUnclaimedClaims(player.getUniqueId());
        int itemsPerPage = plugin.getOrdersManager().getCollectItemsPerPage();

        if (slot == lastRow) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersBrowseMenu(plugin, 1, plugin.getOrdersManager().getDefaultSort(), "ALL").open(player);
            return;
        }
        if (slot == lastRow + 1) {
            if (page > 1) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersCollectMenu(plugin, page - 1).open(player);
            }
            return;
        }
        if (slot == lastRow + 2) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersMyOrdersMenu(plugin, 1, plugin.getOrdersManager().getDefaultSort()).open(player);
            return;
        }
        if (slot == lastRow + 3) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.BUTTON-CLICK"));
            new OrdersCollectMenu(plugin, page).open(player);
            return;
        }
        if (slot == lastRow + 7) {
            if (hasNextPage(claims.size(), itemsPerPage)) {
                SoundUtils.play(player, plugin.getConfigManager().getSound("MENUS.PAGE-TURN"));
                new OrdersCollectMenu(plugin, page + 1).open(player);
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

        OrdersManager manager = plugin.getOrdersManager();
        if (!manager.beginAction(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cᴏʀᴅᴇʀѕ ɪѕ ѕᴛɪʟʟ ᴘʀᴏᴄᴇѕѕɪɴɢ ʏᴏᴜʀ ᴘʀᴇᴠɪᴏᴜѕ ᴀᴄᴛɪᴏɴ."));
            return;
        }

        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent("&cѕʟᴏᴡ ᴅᴏᴡɴ ꜰᴏʀ ᴀ ᴍᴏᴍᴇɴᴛ."));
                return;
            }
            manager.updateClickCooldown(player.getUniqueId());

            OrderCollectionClaim claim = claims.get(claimIndex);
            OrdersManager.ClaimResult result = manager.claim(player, claim.id());
            if (!result.success()) {
                player.sendMessage(ColorUtils.toComponent(resolveFailureMessage(result)));
                SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.FAIL"));
                return;
            }

            if (claim.refundClaim()) {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                        "ORDERS.CLAIMED_REFUND",
                        "&aᴄʟᴀɪᴍᴇᴅ ᴇѕᴄʀᴏᴡ ʀᴇꜰᴜɴᴅ ᴏꜰ &a${amount}&a.",
                        "{amount}", NumberUtils.format(claim.moneyAmount())
                )));
            } else {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                        "ORDERS.CLAIMED_ITEM",
                        "&aᴄʟᴀɪᴍᴇᴅ ᴅᴇʟɪᴠᴇʀᴇᴅ ɪᴛᴇᴍ: &f{item}&a.",
                        "{item}", manager.describeItem(claim.item())
                )));
            }
            SoundUtils.play(player, plugin.getConfigManager().getSound("ORDERS.SUCCESS"));
            new OrdersCollectMenu(plugin, page).open(player);
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

    private String resolveFailureMessage(OrdersManager.ClaimResult result) {
        return switch (result.reason()) {
            case DISABLED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.DISABLED", "&cᴏʀᴅᴇʀѕ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ.");
            case CLAIM_NOT_FOUND -> plugin.getConfigManager().getMessageOrDefault("ORDERS.CLAIM_NOT_FOUND", "&cᴛʜᴀᴛ ᴄʟᴀɪᴍ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.");
            case NOT_OWNER -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NOT_YOUR_CLAIM", "&cᴛʜᴀᴛ ᴄʟᴀɪᴍ ᴅᴏᴇѕ ɴᴏᴛ ʙᴇʟᴏɴɢ ᴛᴏ ʏᴏᴜ.");
            case ALREADY_CLAIMED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.CLAIM_ALREADY_CLAIMED", "&cᴛʜᴀᴛ ᴄʟᴀɪᴍ ᴡᴀѕ ᴀʟʀᴇᴀᴅʏ ᴄᴏʟʟᴇᴄᴛᴇᴅ.");
            case INVENTORY_FULL -> plugin.getConfigManager().getMessageOrDefault("ORDERS.CLAIM_INVENTORY_FULL", "&cʏᴏᴜ ɴᴇᴇᴅ ᴀ ꜰʀᴇᴇ ɪɴᴠᴇɴᴛᴏʀʏ ѕʟᴏᴛ ᴛᴏ ᴄʟᴀɪᴍ ᴛʜᴀᴛ ɪᴛᴇᴍ.");
            case NO_PLAYER_DATA -> "&cʏᴏᴜʀ ᴘʟᴀʏᴇʀ ᴅᴀᴛᴀ ᴄᴏᴜʟᴅ ɴᴏᴛ ʙᴇ ʟᴏᴀᴅᴇᴅ.";
            case DATABASE_ERROR -> "&cᴏʀᴅᴇʀѕ ᴄᴏᴜʟᴅ ɴᴏᴛ ᴄᴏᴍᴘʟᴇᴛᴇ ᴛʜᴀᴛ ᴄʟᴀɪᴍ ʀɪɢʜᴛ ɴᴏᴡ.";
        };
    }
}
