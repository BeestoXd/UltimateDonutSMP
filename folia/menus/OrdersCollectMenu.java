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
        set(lastRow, ItemUtils.createItem(Material.COMPASS, "&bBack to Board", List.of("&7Return to active orders")));
        set(lastRow + 1, page > 1
                ? ItemUtils.createItem(Material.ARROW, "&aPrevious Page", List.of("&7Go to page &f" + (page - 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 2, ItemUtils.createItem(Material.WRITABLE_BOOK, "&bMy Orders", List.of("&7View your orders")));
        set(lastRow + 3, ItemUtils.createItem(Material.CLOCK, "&eRefresh", List.of("&7Reload your collect queue")));
        set(lastRow + 5, ItemUtils.createItem(
                Material.BOOK,
                "&ePage " + page + "&7/&e" + getTotalPages(claims.size(), itemsPerPage),
                List.of("&7Pending claims: &f" + claims.size())
        ));
        set(lastRow + 7, hasNextPage(claims.size(), itemsPerPage)
                ? ItemUtils.createItem(Material.ARROW, "&aNext Page", List.of("&7Go to page &f" + (page + 1)))
                : ItemUtils.createPlaceholder(Material.BLACK_STAINED_GLASS_PANE));
        set(lastRow + 8, ItemUtils.createItem(Material.BARRIER, "&cClose", List.of("&7Close Orders")));

        if (claims.isEmpty()) {
            set(inventory.getSize() / 2, ItemUtils.createItem(
                    Material.BARRIER,
                    "&cNothing to Collect",
                    List.of("&7Delivered items and refunds will appear here.")
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
            player.sendMessage(ColorUtils.toComponent("&cOrders is still processing your previous action."));
            return;
        }

        try {
            if (manager.isOnClickCooldown(player.getUniqueId())) {
                player.sendMessage(ColorUtils.toComponent("&cSlow down for a moment."));
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
                        "&aClaimed escrow refund of &a${amount}&a.",
                        "{amount}", NumberUtils.format(claim.moneyAmount())
                )));
            } else {
                player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                        "ORDERS.CLAIMED_ITEM",
                        "&aClaimed delivered item: &f{item}&a.",
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
            case DISABLED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.DISABLED", "&cOrders is currently disabled.");
            case CLAIM_NOT_FOUND -> plugin.getConfigManager().getMessageOrDefault("ORDERS.CLAIM_NOT_FOUND", "&cThat claim no longer exists.");
            case NOT_OWNER -> plugin.getConfigManager().getMessageOrDefault("ORDERS.NOT_YOUR_CLAIM", "&cThat claim does not belong to you.");
            case ALREADY_CLAIMED -> plugin.getConfigManager().getMessageOrDefault("ORDERS.CLAIM_ALREADY_CLAIMED", "&cThat claim was already collected.");
            case INVENTORY_FULL -> plugin.getConfigManager().getMessageOrDefault("ORDERS.CLAIM_INVENTORY_FULL", "&cYou need a free inventory slot to claim that item.");
            case NO_PLAYER_DATA -> "&cYour player data could not be loaded.";
            case DATABASE_ERROR -> "&cOrders could not complete that claim right now.";
        };
    }
}
