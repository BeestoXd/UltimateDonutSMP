package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.FollowEntry;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.menus.FriendsMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FriendsManager {

    private final UltimateDonutSmp plugin;

    // followerUuid -> (followedUuid -> FollowEntry)
    private final Map<UUID, Map<UUID, FollowEntry>> followingMap = new ConcurrentHashMap<>();
    // followedUuid -> (followerUuid -> FollowEntry)
    private final Map<UUID, Map<UUID, FollowEntry>> followersMap = new ConcurrentHashMap<>();

    // Chat search pending state
    private final Map<UUID, PendingSearch> pendingSearchInputs = new ConcurrentHashMap<>();
    private final Map<UUID, String> activeSearchQueries = new ConcurrentHashMap<>();

    public record PendingSearch(int page, String filter) {}

    public FriendsManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void loadPlayer(UUID playerUuid) {
        if (playerUuid == null) return;

        // Load who this player is following
        Map<UUID, FollowEntry> following = new ConcurrentHashMap<>();
        for (FollowEntry entry : plugin.getDatabaseManager().loadFollowsByFollower(playerUuid)) {
            following.put(entry.followedUuid(), entry);
        }
        followingMap.put(playerUuid, following);

        // Load who is following this player
        Map<UUID, FollowEntry> followers = new ConcurrentHashMap<>();
        for (FollowEntry entry : plugin.getDatabaseManager().loadFollowsByFollowed(playerUuid)) {
            followers.put(entry.followerUuid(), entry);
        }
        followersMap.put(playerUuid, followers);
    }

    public void unloadPlayer(UUID playerUuid) {
        if (playerUuid == null) return;
        followingMap.remove(playerUuid);
        followersMap.remove(playerUuid);
    }

    public void clear() {
        followingMap.clear();
        followersMap.clear();
        pendingSearchInputs.clear();
        activeSearchQueries.clear();
    }

    public boolean followPlayer(Player follower, UUID followedUuid, String followedName) {
        if (follower == null || followedUuid == null) return false;
        UUID followerUuid = follower.getUniqueId();
        if (followerUuid.equals(followedUuid)) return false;

        long now = System.currentTimeMillis();
        boolean saved = plugin.getDatabaseManager().addFollow(followerUuid, followedUuid, followedName, now);
        if (!saved) return false;

        FollowEntry entry = new FollowEntry(
                followerUuid,
                followedUuid,
                followedName,
                false, // transactionsEnabled
                true,  // messagesEnabled
                true,  // paymentsEnabled
                true,  // activityEnabled
                false, // tpaAutoAcceptEnabled
                true,  // teleportRequestsEnabled
                now
        );

        getFollowingMap(followerUuid).put(followedUuid, entry);
        getFollowersMap(followedUuid).put(followerUuid, entry);
        return true;
    }

    public boolean unfollowPlayer(Player follower, UUID followedUuid) {
        if (follower == null || followedUuid == null) return false;
        UUID followerUuid = follower.getUniqueId();

        boolean removed = plugin.getDatabaseManager().removeFollow(followerUuid, followedUuid);
        if (!removed) return false;

        getFollowingMap(followerUuid).remove(followedUuid);
        getFollowersMap(followedUuid).remove(followerUuid);
        return true;
    }

    public boolean updateFollowSettings(UUID followerUuid, UUID followedUuid, boolean transactions, boolean messages, boolean payments, boolean activity, boolean tpaAutoAccept, boolean teleportRequests) {
        if (followerUuid == null || followedUuid == null) return false;

        boolean updated = plugin.getDatabaseManager().updateFollowSettings(
                followerUuid, followedUuid, transactions, messages, payments, activity, tpaAutoAccept, teleportRequests
        );
        if (!updated) return false;

        FollowEntry oldEntry = getFollowingMap(followerUuid).get(followedUuid);
        FollowEntry entry = new FollowEntry(
                followerUuid,
                followedUuid,
                oldEntry != null ? oldEntry.followedNameSnapshot() : "",
                transactions,
                messages,
                payments,
                activity,
                tpaAutoAccept,
                teleportRequests,
                oldEntry != null ? oldEntry.createdAt() : System.currentTimeMillis()
        );

        getFollowingMap(followerUuid).put(followedUuid, entry);
        getFollowersMap(followedUuid).put(followerUuid, entry);
        return true;
    }

    public boolean isFollowing(UUID followerUuid, UUID followedUuid) {
        if (followerUuid == null || followedUuid == null) return false;
        return getFollowingMap(followerUuid).containsKey(followedUuid);
    }

    public boolean isFollower(UUID followedUuid, UUID followerUuid) {
        if (followedUuid == null || followerUuid == null) return false;
        return getFollowersMap(followedUuid).containsKey(followerUuid);
    }

    public boolean isFriend(UUID uuid1, UUID uuid2) {
        return isFollowing(uuid1, uuid2) && isFollowing(uuid2, uuid1);
    }

    public FollowEntry getFollowEntry(UUID followerUuid, UUID followedUuid) {
        if (followerUuid == null || followedUuid == null) return null;
        return getFollowingMap(followerUuid).get(followedUuid);
    }

    public Collection<FollowEntry> getFollowing(UUID followerUuid) {
        if (followerUuid == null) return Collections.emptyList();
        return getFollowingMap(followerUuid).values();
    }

    public Collection<FollowEntry> getFollowers(UUID followedUuid) {
        if (followedUuid == null) return Collections.emptyList();
        return getFollowersMap(followedUuid).values();
    }

    // Interceptions
    public boolean isMessageBlocked(UUID senderUuid, UUID recipientUuid) {
        FollowEntry entry = getFollowEntry(recipientUuid, senderUuid); // recipient follows sender?
        return entry != null && !entry.messagesEnabled();
    }

    public boolean isPaymentBlocked(UUID senderUuid, UUID recipientUuid) {
        FollowEntry entry = getFollowEntry(recipientUuid, senderUuid); // recipient follows sender?
        return entry != null && !entry.paymentsEnabled();
    }

    public boolean isTeleportRequestBlocked(UUID requesterUuid, UUID targetUuid) {
        FollowEntry entry = getFollowEntry(targetUuid, requesterUuid); // target follows requester?
        return entry != null && !entry.teleportRequestsEnabled();
    }

    public boolean isTpaAutoAcceptEnabled(UUID requesterUuid, UUID targetUuid) {
        FollowEntry entry = getFollowEntry(targetUuid, requesterUuid); // target follows requester?
        return entry != null && entry.tpaAutoAcceptEnabled();
    }

    public boolean isTransactionMessageBlocked(UUID actorUuid, UUID recipientUuid) {
        FollowEntry entry = getFollowEntry(recipientUuid, actorUuid); // recipient follows actor?
        return entry != null && !entry.transactionsEnabled();
    }

    // Activity Broadcasting
    public void handleJoin(Player player) {
        UUID playerUuid = player.getUniqueId();
        loadPlayer(playerUuid);

        // Notify online followers
        Map<UUID, FollowEntry> followers = getFollowersMap(playerUuid);
        String joinMsg = "&a&lFriends &8» &f" + plugin.getHideManager().publicName(player) + " &7joined the game.";
        for (FollowEntry entry : followers.values()) {
            if (entry.activityEnabled()) {
                Player followerPlayer = Bukkit.getPlayer(entry.followerUuid());
                if (followerPlayer != null && followerPlayer.isOnline()) {
                    followerPlayer.sendMessage(ColorUtils.toComponent(joinMsg, followerPlayer));
                }
            }
        }

        // List online followed players to joining player
        Map<UUID, FollowEntry> following = getFollowingMap(playerUuid);
        List<String> onlineFollowed = new ArrayList<>();
        for (FollowEntry entry : following.values()) {
            Player followedPlayer = Bukkit.getPlayer(entry.followedUuid());
            if (followedPlayer != null && followedPlayer.isOnline()) {
                onlineFollowed.add(plugin.getHideManager().publicName(followedPlayer));
            }
        }

        plugin.getFoliaScheduler().runEntityLater(player, () -> {
            if (!player.isOnline()) return;
            if (onlineFollowed.isEmpty()) {
                player.sendMessage(ColorUtils.toComponent("&a&lFriends &8» &7None of the players you follow are currently online."));
            } else {
                String names = String.join("&7, &f", onlineFollowed);
                player.sendMessage(ColorUtils.toComponent("&a&lFriends &8» &7Online followed players: &f" + names));
            }
        }, 20L); // 1 second delay
    }

    public void handleQuit(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Notify online followers
        Map<UUID, FollowEntry> followers = getFollowersMap(playerUuid);
        String quitMsg = "&a&lFriends &8» &f" + plugin.getHideManager().publicName(player) + " &7left the game.";
        for (FollowEntry entry : followers.values()) {
            if (entry.activityEnabled()) {
                Player followerPlayer = Bukkit.getPlayer(entry.followerUuid());
                if (followerPlayer != null && followerPlayer.isOnline()) {
                    followerPlayer.sendMessage(ColorUtils.toComponent(quitMsg, followerPlayer));
                }
            }
        }

        unloadPlayer(playerUuid);
    }

    // Search query chat input
    public void promptFriendsSearch(Player player, int page, String filter) {
        if (player == null) return;
        pendingSearchInputs.put(player.getUniqueId(), new PendingSearch(page, filter));
        player.closeInventory();
        player.sendMessage(ColorUtils.toComponent("&7ᴛʏᴘᴇ ᴀ ᴘʟᴀʏᴇʀ ɴᴀᴍᴇ ɪɴ ᴄʜᴀᴛ ᴛᴏ ѕᴇᴀʀᴄʜ. ᴛʏᴘᴇ &ccancel &7ᴛᴏ ᴀʙᴏʀᴛ."));
    }

    public boolean hasPendingSearchInput(UUID uuid) {
        return pendingSearchInputs.containsKey(uuid);
    }

    public void handlePendingSearchInput(Player player, String rawInput) {
        if (player == null) return;
        PendingSearch pending = pendingSearchInputs.remove(player.getUniqueId());
        if (pending == null) return;

        String input = rawInput == null ? "" : rawInput.trim();
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(ColorUtils.toComponent("&7Friends search cancelled."));
            new FriendsMenu(plugin, pending.page(), pending.filter(), FriendsMenu.FilterType.ALL).open(player);
            return;
        }

        if (input.equalsIgnoreCase("clear")) {
            player.sendMessage(ColorUtils.toComponent("&7Friends search filter cleared."));
            new FriendsMenu(plugin, 0, null, FriendsMenu.FilterType.ALL).open(player);
            return;
        }

        if (input.isBlank()) {
            player.sendMessage(ColorUtils.toComponent("&cSearch query cannot be empty."));
            promptFriendsSearch(player, pending.page(), pending.filter());
            return;
        }

        player.sendMessage(ColorUtils.toComponent("&7Searching friends for &f" + input + "&7."));
        new FriendsMenu(plugin, 0, input, FriendsMenu.FilterType.ALL).open(player);
    }

    private Map<UUID, FollowEntry> getFollowingMap(UUID followerUuid) {
        return followingMap.computeIfAbsent(followerUuid, id -> {
            Map<UUID, FollowEntry> m = new ConcurrentHashMap<>();
            for (FollowEntry entry : plugin.getDatabaseManager().loadFollowsByFollower(id)) {
                m.put(entry.followedUuid(), entry);
            }
            return m;
        });
    }

    private Map<UUID, FollowEntry> getFollowersMap(UUID followedUuid) {
        return followersMap.computeIfAbsent(followedUuid, id -> {
            Map<UUID, FollowEntry> m = new ConcurrentHashMap<>();
            for (FollowEntry entry : plugin.getDatabaseManager().loadFollowsByFollowed(id)) {
                m.put(entry.followerUuid(), entry);
            }
            return m;
        });
    }
}
