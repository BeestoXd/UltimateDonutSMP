package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.models.Order;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Builds and sends the server-wide announcements configured under SERVER-NOTIFICATIONS.
 *
 * <p>Join, leave and first-join text is handed back to PlayerJoinQuitListener rather than
 * broadcast from here, because that listener already decides who is allowed to see a join
 * line and the configured text has to travel the same route as the server's own.</p>
 */
public class ServerNotificationManager {

    private static final String ROOT = "SERVER-NOTIFICATIONS.";

    private final UltimateDonutSmp plugin;

    public ServerNotificationManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    /**
     * The join line for a player, or null when the server's own message should be relayed
     * instead. A brand new player gets the first-join text when that is switched on, so
     * nobody is announced twice.
     */
    public String joinAnnouncement(Player player, boolean firstJoin) {
        if (firstJoin && isEnabled("FIRST-JOIN")) {
            return format(message("FIRST-JOIN", player), "{player}", publicName(player));
        }
        if (!isEnabled("JOIN")) {
            return null;
        }
        return format(message("JOIN", player), "{player}", publicName(player));
    }

    /** The leave line for a player, or null to relay the server's own message. */
    public String leaveAnnouncement(Player player) {
        if (!isEnabled("LEAVE")) {
            return null;
        }
        return format(message("LEAVE", player), "{player}", publicName(player));
    }

    public void announceAuctionListing(Player seller, AuctionListing listing) {
        if (listing == null || !isEnabled("AUCTION-HOUSE") || !isEnabled("AUCTION-HOUSE.LISTING")) {
            return;
        }
        broadcast(
                format(
                        message("AUCTION-HOUSE.LISTING"),
                        "{player}", publicName(seller),
                        "{item}", describeAuctionItem(listing.item()),
                        "{amount}", String.valueOf(amountOf(listing.item())),
                        "{price}", NumberUtils.format(listing.price()),
                        "{price_formatted}", plugin.getCurrencyManager().formatMoney(listing.price()),
                        "{category}", listing.category()
                ),
                PlayerSettingUtils.NotificationChannel.AUCTION
        );
    }

    public void announceAuctionPurchase(Player buyer, AuctionListing listing) {
        if (listing == null || !isEnabled("AUCTION-HOUSE") || !isEnabled("AUCTION-HOUSE.PURCHASE")) {
            return;
        }
        broadcast(
                format(
                        message("AUCTION-HOUSE.PURCHASE"),
                        "{player}", publicName(buyer),
                        "{seller}", sellerName(listing),
                        "{item}", describeAuctionItem(listing.item()),
                        "{amount}", String.valueOf(amountOf(listing.item())),
                        "{price}", NumberUtils.format(listing.price()),
                        "{price_formatted}", plugin.getCurrencyManager().formatMoney(listing.price())
                ),
                PlayerSettingUtils.NotificationChannel.AUCTION
        );
    }

    public void announceOrderCreated(Player owner, Order order) {
        if (order == null || !isEnabled("ORDERS") || !isEnabled("ORDERS.CREATE")) {
            return;
        }
        broadcast(
                format(
                        message("ORDERS.CREATE"),
                        "{player}", publicName(owner),
                        "{item}", describeOrderItem(order.requestedItem()),
                        "{amount}", String.valueOf(order.requestedQuantity()),
                        "{price}", NumberUtils.format(order.priceEach()),
                        "{price_formatted}", plugin.getCurrencyManager().formatMoney(order.priceEach()),
                        "{total}", NumberUtils.format(order.totalBudget()),
                        "{total_formatted}", plugin.getCurrencyManager().formatMoney(order.totalBudget())
                ),
                PlayerSettingUtils.NotificationChannel.ORDER
        );
    }

    public void announceOrderCompleted(Player deliverer, Order order) {
        if (order == null || !isEnabled("ORDERS") || !isEnabled("ORDERS.COMPLETE")) {
            return;
        }
        broadcast(
                format(
                        message("ORDERS.COMPLETE"),
                        "{player}", publicName(deliverer),
                        "{owner}", ownerName(order),
                        "{item}", describeOrderItem(order.requestedItem()),
                        "{amount}", String.valueOf(order.requestedQuantity()),
                        "{price}", NumberUtils.format(order.priceEach()),
                        "{price_formatted}", plugin.getCurrencyManager().formatMoney(order.priceEach()),
                        "{total}", NumberUtils.format(order.totalBudget()),
                        "{total_formatted}", plugin.getCurrencyManager().formatMoney(order.totalBudget())
                ),
                PlayerSettingUtils.NotificationChannel.ORDER
        );
    }

    private void broadcast(String message, PlayerSettingUtils.NotificationChannel channel) {
        if (message == null || message.isBlank()) {
            return;
        }
        plugin.getSpigotScheduler().forEachOnlinePlayer(viewer -> {
            if (!PlayerSettingUtils.notificationEnabled(
                    plugin,
                    viewer,
                    PlayerSettingUtils.NotificationChannel.SERVER_BROADCAST
            )) {
                return;
            }
            if (!PlayerSettingUtils.notificationEnabled(plugin, viewer, channel)) {
                return;
            }
            viewer.sendMessage(ColorUtils.toComponent(message));
        });
    }

    /** Fills the placeholders in, treating a missing value as an empty one. */
    static String format(String message, String... replacements) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String result = message;
        for (int index = 0; index + 1 < replacements.length; index += 2) {
            String value = replacements[index + 1];
            result = result.replace(replacements[index], value == null ? "" : value);
        }
        return result;
    }

    private String publicName(Player player) {
        if (player == null) {
            return "";
        }
        return plugin.getHideManager() == null ? player.getName() : plugin.getHideManager().publicName(player);
    }

    private String sellerName(AuctionListing listing) {
        String fallback = listing.sellerName() == null ? "" : listing.sellerName();
        return plugin.getHideManager() == null
                ? fallback
                : plugin.getHideManager().publicName(listing.sellerUuid(), fallback);
    }

    private String ownerName(Order order) {
        String fallback = order.ownerName() == null ? "" : order.ownerName();
        return plugin.getHideManager() == null
                ? fallback
                : plugin.getHideManager().publicName(order.ownerUuid(), fallback);
    }

    private String describeAuctionItem(ItemStack item) {
        return plugin.getAuctionHouseManager() == null
                ? fallbackItemName(item)
                : plugin.getAuctionHouseManager().describeItem(item);
    }

    private String describeOrderItem(ItemStack item) {
        return plugin.getOrdersManager() == null
                ? fallbackItemName(item)
                : plugin.getOrdersManager().describeItem(item);
    }

    private String fallbackItemName(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "Unknown Item";
        }
        return plugin.getWorthManager().prettifyMaterial(item.getType());
    }

    private static int amountOf(ItemStack item) {
        return item == null ? 0 : Math.max(0, item.getAmount());
    }

    private boolean isEnabled(String path) {
        return config().getBoolean(ROOT + path + ".ENABLED", false);
    }

    private String message(String path) {
        return config().getString(ROOT + path + ".MESSAGE", "");
    }

    /**
     * The wording for one announcement, taking the player's rank into account.
     *
     * <p>An announcement may carry a BY-PERMISSION map of permission node to wording. The first
     * node the player holds wins, so a server lists its highest rank first, and a player holding
     * none of them falls back to the plain MESSAGE.</p>
     */
    private String message(String path, Player player) {
        return resolveByPermission(
                config().getConfigurationSection(ROOT + path + ".BY-PERMISSION"),
                message(path),
                permission -> PermissionUtils.hasExact(player, permission)
        );
    }

    /**
     * Picks the wording for the first permission in the section that the player holds, falling
     * back to {@code fallback} when the section is absent, empty, or matches nothing.
     *
     * <p>The section's own order decides the winner because there is no way to rank two pieces of
     * wording against each other the way a home limit picks its highest number. Config order is
     * the one ordering the admin can see and change, and Bukkit hands the keys back in the order
     * the file lists them.</p>
     *
     * <p>The values are read deeply, exactly as the home limits are, because a permission node
     * carries dots and Bukkit reads those as a path. A node written as one quoted key arrives as a
     * tree of sections, so only a deep read hands back the whole node again; the sections it walks
     * through on the way are not wording and are skipped.</p>
     *
     * <p>Matching is deliberately on the exact node rather than the usual inherited check: these
     * maps are keyed by a server's own ranks, and a wildcard would otherwise match every entry and
     * hand the top rank's wording to anybody holding it.</p>
     */
    static String resolveByPermission(
            ConfigurationSection byPermission,
            String fallback,
            Predicate<String> holdsPermission
    ) {
        if (byPermission == null || holdsPermission == null) {
            return fallback;
        }
        for (Map.Entry<String, Object> entry : byPermission.getValues(true).entrySet()) {
            if (!(entry.getValue() instanceof String wording) || wording.isBlank()) {
                continue;
            }
            if (holdsPermission.test(entry.getKey())) {
                return wording;
            }
        }
        return fallback;
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getConfig();
    }
}
