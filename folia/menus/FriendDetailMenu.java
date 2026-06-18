package com.bx.ultimateDonutSmp.menus;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.FollowEntry;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendDetailMenu extends BaseMenu {

    private final UUID targetUuid;
    private final String targetName;
    private final int parentPage;
    private final String parentSearch;
    private final FriendsMenu.FilterType parentFilter;

    public FriendDetailMenu(UltimateDonutSmp plugin, UUID targetUuid, String targetName, int parentPage, String parentSearch, FriendsMenu.FilterType parentFilter) {
        super(plugin, "Friends -> " + targetName, 27);
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.parentPage = parentPage;
        this.parentSearch = parentSearch;
        this.parentFilter = parentFilter;
    }

    @Override
    public void build(Player player) {
        clear();
        fill(Material.BLACK_STAINED_GLASS_PANE);

        UUID playerUuid = player.getUniqueId();
        boolean isFollowing = plugin.getFriendsManager().isFollowing(playerUuid, targetUuid);
        boolean isFollower = plugin.getFriendsManager().isFollower(playerUuid, targetUuid);

        boolean isOnline = Bukkit.getPlayer(targetUuid) != null && Bukkit.getPlayer(targetUuid).isOnline();
        String status = isOnline ? "&aOnline" : "&cOffline";

        String rel;
        if (isFollowing && isFollower) rel = "&dFriend";
        else if (isFollowing) rel = "&9Following";
        else if (isFollower) rel = "&bFollower";
        else rel = "&7None";

        // Head (Slot 10)
        List<String> headLore = List.of(
                "&7Status: " + status,
                "&7Relationship: " + rel
        );
        ItemStack head = ItemUtils.createPlayerHead(
                Bukkit.getOfflinePlayer(targetUuid),
                "&e&l" + targetName,
                headLore
        );
        set(10, head);

        // Action Button (Slot 11)
        ItemStack actionItem;
        if (isFollowing) {
            actionItem = ItemUtils.createItem(
                    Material.LAVA_BUCKET,
                    "&cRemove Friend",
                    List.of("&7Click to remove friend")
            );
        } else if (isFollower) {
            actionItem = ItemUtils.createItem(
                    Material.LIME_DYE,
                    "&aFollow Back",
                    List.of("&7Click to follow back " + targetName)
            );
        } else {
            actionItem = ItemUtils.createItem(
                    Material.GRAY_DYE,
                    "&aFollow Player",
                    List.of("&7Click to follow " + targetName)
            );
        }
        set(11, actionItem);

        // Settings Toggles (Slots 13, 14, 15, 16, 22, 23)
        FollowEntry entry = plugin.getFriendsManager().getFollowEntry(playerUuid, targetUuid);

        boolean transactions = entry != null && entry.transactionsEnabled();
        boolean messages = entry == null || entry.messagesEnabled();
        boolean payments = entry == null || entry.paymentsEnabled();
        boolean activity = entry == null || entry.activityEnabled();
        boolean tpaAuto = entry != null && entry.tpaAutoAcceptEnabled();
        boolean teleport = entry == null || entry.teleportRequestsEnabled();

        // 13. Transactions
        set(13, ItemUtils.createItem(
                transactions ? Material.LIME_DYE : Material.GRAY_DYE,
                "&eTransactions",
                List.of(
                        "&7Currently: " + (transactions ? "&aOn" : "&cOff"),
                        "&7Click to toggle " + targetName + "'s transaction messages"
                )
        ));

        // 14. Messages
        set(14, ItemUtils.createItem(
                messages ? Material.LIME_DYE : Material.GRAY_DYE,
                "&eMessages",
                List.of(
                        "&7Currently: " + (messages ? "&aOn" : "&cOff"),
                        "&7Click to toggle " + targetName + "'s private messages"
                )
        ));

        // 15. Payments
        set(15, ItemUtils.createItem(
                payments ? Material.LIME_DYE : Material.GRAY_DYE,
                "&ePayments",
                List.of(
                        "&7Currently: " + (payments ? "&aOn" : "&cOff"),
                        "&7Click to toggle " + targetName + "'s payments"
                )
        ));

        // 16. Activity
        set(16, ItemUtils.createItem(
                activity ? Material.LIME_DYE : Material.GRAY_DYE,
                "&eActivity",
                List.of(
                        "&7Currently: " + (activity ? "&aOn" : "&cOff"),
                        "&7Click to toggle " + targetName + "'s activity messages"
                )
        ));

        // 22. TPA Auto Accept
        set(22, ItemUtils.createItem(
                tpaAuto ? Material.LIME_DYE : Material.GRAY_DYE,
                "&eTPA Auto Accept",
                List.of(
                        "&7Currently: " + (tpaAuto ? "&aOn" : "&cOff"),
                        "&7Click to toggle auto accepting " + targetName + "'s TPA requests"
                )
        ));

        // 23. Teleport Requests
        set(23, ItemUtils.createItem(
                teleport ? Material.LIME_DYE : Material.GRAY_DYE,
                "&eTeleport Requests",
                List.of(
                        "&7Currently: " + (teleport ? "&aOn" : "&cOff"),
                        "&7Click to toggle " + targetName + "'s teleport requests"
                )
        ));

        // Back Button (Slot 18)
        set(18, ItemUtils.createItem(
                Material.ARROW,
                "&cBack",
                List.of("&7Return to friends list")
        ));
    }

    @Override
    public void handleClick(int slot, Player player, ClickType clickType) {
        UUID playerUuid = player.getUniqueId();

        if (slot == 18) {
            new FriendsMenu(plugin, parentPage, parentSearch, parentFilter).open(player);
            return;
        }

        boolean isFollowing = plugin.getFriendsManager().isFollowing(playerUuid, targetUuid);

        if (slot == 11) {
            if (isFollowing) {
                plugin.getFriendsManager().unfollowPlayer(player, targetUuid);
                player.sendMessage(ColorUtils.toComponent("&7You unfollowed &f" + targetName + "&7."));
            } else {
                plugin.getFriendsManager().followPlayer(player, targetUuid, targetName);
                player.sendMessage(ColorUtils.toComponent("&7You are now following &f" + targetName + "&7."));
            }
            build(player);
            return;
        }

        // Toggles require following relationship
        if (slot == 13 || slot == 14 || slot == 15 || slot == 16 || slot == 22 || slot == 23) {
            if (!isFollowing) {
                player.sendMessage(ColorUtils.toComponent("&cYou must follow " + targetName + " first to change settings."));
                return;
            }

            FollowEntry entry = plugin.getFriendsManager().getFollowEntry(playerUuid, targetUuid);
            if (entry == null) return;

            boolean transactions = entry.transactionsEnabled();
            boolean messages = entry.messagesEnabled();
            boolean payments = entry.paymentsEnabled();
            boolean activity = entry.activityEnabled();
            boolean tpaAuto = entry.tpaAutoAcceptEnabled();
            boolean teleport = entry.teleportRequestsEnabled();

            switch (slot) {
                case 13 -> transactions = !transactions;
                case 14 -> messages = !messages;
                case 15 -> payments = !payments;
                case 16 -> activity = !activity;
                case 22 -> tpaAuto = !tpaAuto;
                case 23 -> teleport = !teleport;
            }

            plugin.getFriendsManager().updateFollowSettings(
                    playerUuid, targetUuid, transactions, messages, payments, activity, tpaAuto, teleport
            );
            build(player);
        }
    }
}
