package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * Matchmaking in front of RTP. Players run /rtpq to wait for other players, and once enough of
 * them are waiting the whole group is dropped at one shared random location rather than at a
 * random location each, which is what /rtp does. Everybody lands within a few blocks of
 * everybody else, so a fight can start without anyone swapping coordinates first.
 *
 * <p>The search reuses the RTP engine through {@link RTPManager#findSafeLocationAsync}, so a
 * match is never blocked by RTP cooldowns, playtime requirements, or the RTP slot queue. The
 * waiting list only lives in memory, so leaving the server or reloading the config drops the
 * player from it.
 */
public class RTPQueueManager {

    private static final int DEFAULT_MATCH_SIZE = 2;
    private static final int MIN_MATCH_SIZE = 2;
    private static final int MAX_MATCH_SIZE = 32;
    private static final int DEFAULT_SPREAD_RADIUS = 16;
    private static final int MAX_SPREAD_RADIUS = 512;

    private final UltimateDonutSmp plugin;
    private final LinkedHashSet<UUID> waiting = new LinkedHashSet<>();

    public RTPQueueManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        clear();
    }

    public boolean isEnabled() {
        FileConfiguration rtp = rtpConfig();
        return rtp != null
                && plugin.getRtpManager() != null
                && plugin.getRtpManager().isEnabled()
                && rtp.getBoolean("QUEUE.ENABLED", true);
    }

    public int getMatchSize() {
        FileConfiguration rtp = rtpConfig();
        int configured = rtp == null ? DEFAULT_MATCH_SIZE : rtp.getInt("QUEUE.MATCH-SIZE", DEFAULT_MATCH_SIZE);
        return Math.max(MIN_MATCH_SIZE, Math.min(MAX_MATCH_SIZE, configured));
    }

    public int getSpreadRadius() {
        FileConfiguration rtp = rtpConfig();
        int configured = rtp == null ? DEFAULT_SPREAD_RADIUS : rtp.getInt("QUEUE.SPREAD-RADIUS", DEFAULT_SPREAD_RADIUS);
        return Math.max(0, Math.min(MAX_SPREAD_RADIUS, configured));
    }

    public String getWorldName() {
        FileConfiguration rtp = rtpConfig();
        String configured = rtp == null ? null : rtp.getString("QUEUE.WORLD", "world");
        if (configured == null || configured.isBlank() || plugin.getRtpManager() == null) {
            return null;
        }
        return plugin.getRtpManager().resolveWorldSelector(configured);
    }

    public boolean isInQueue(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        synchronized (waiting) {
            return waiting.contains(playerId);
        }
    }

    public int getQueuePosition(UUID playerId) {
        if (playerId == null) {
            return -1;
        }
        synchronized (waiting) {
            int position = 1;
            for (UUID queued : waiting) {
                if (queued.equals(playerId)) {
                    return position;
                }
                position++;
            }
        }
        return -1;
    }

    public int getQueueSize() {
        synchronized (waiting) {
            return waiting.size();
        }
    }

    public void clear() {
        synchronized (waiting) {
            waiting.clear();
        }
    }

    /**
     * Puts a player in the waiting list, and starts the match as soon as that fills it.
     *
     * @return true when the player is waiting in the queue once this returns
     */
    public boolean join(Player player) {
        if (player == null) {
            return false;
        }

        if (!isEnabled()) {
            sendMessage(player, "DISABLED", "&cThe RTP queue is currently disabled.");
            return false;
        }

        UUID playerId = player.getUniqueId();
        if (isInQueue(playerId)) {
            sendMessage(
                    player,
                    "ALREADY-QUEUED",
                    "&cYou are already in the RTP queue at position #{position}.",
                    "{position}", String.valueOf(getQueuePosition(playerId))
            );
            return false;
        }

        if (getSearchSettings() == null) {
            sendMessage(player, "UNAVAILABLE", "&cThe RTP queue is not available right now.");
            return false;
        }

        if (plugin.getRtpManager().hasActiveRtpFlow(playerId)
                || plugin.getTeleportManager().hasPendingType(playerId, "RTP")) {
            sendMessage(player, "BUSY", "&cFinish the teleport you already started before joining the RTP queue.");
            return false;
        }

        int needed = getMatchSize();
        int waitingCount;
        synchronized (waiting) {
            waiting.add(playerId);
            waitingCount = waiting.size();
        }

        broadcastToQueue(
                playerId,
                "PLAYER-JOINED",
                "&e[RTP Queue] &f{player} has joined the queue. &7({waiting}/{needed})",
                "{player}", player.getName(),
                "{waiting}", String.valueOf(waitingCount),
                "{needed}", String.valueOf(needed)
        );
        sendMessage(
                player,
                "JOINED",
                "&e[RTP Queue] &fYou have joined the queue. Waiting for another player... &7({waiting}/{needed})",
                "{waiting}", String.valueOf(waitingCount),
                "{needed}", String.valueOf(needed)
        );
        SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-START"));

        startMatchIfReady();
        return true;
    }

    /**
     * Takes a player back out of the waiting list at their own request.
     *
     * @return true when the player was queued and has now been removed
     */
    public boolean leave(Player player) {
        if (player == null) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        if (!remove(playerId)) {
            sendMessage(player, "NOT-QUEUED", "&cYou are not in the RTP queue.");
            return false;
        }

        sendMessage(player, "LEFT", "&e[RTP Queue] &fYou have left the queue.");
        broadcastToQueue(
                playerId,
                "PLAYER-LEFT",
                "&e[RTP Queue] &f{player} has left the queue. &7({waiting}/{needed})",
                "{player}", player.getName(),
                "{waiting}", String.valueOf(getQueueSize()),
                "{needed}", String.valueOf(getMatchSize())
        );
        return true;
    }

    /**
     * Drops a player who has disconnected, without telling the rest of the queue they left.
     */
    public void handleQuit(UUID playerId) {
        remove(playerId);
    }

    public boolean remove(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        synchronized (waiting) {
            return waiting.remove(playerId);
        }
    }

    /**
     * Pulls the players for one match off the front of the waiting list, longest wait first,
     * and leaves the list untouched while it is still too short to fill a match.
     */
    List<UUID> pollMatchParty() {
        int needed = getMatchSize();
        synchronized (waiting) {
            if (waiting.size() < needed) {
                return List.of();
            }
            List<UUID> party = new ArrayList<>(needed);
            for (UUID queued : waiting) {
                party.add(queued);
                if (party.size() == needed) {
                    break;
                }
            }
            waiting.removeAll(party);
            return party;
        }
    }

    private void startMatchIfReady() {
        List<UUID> party = pollMatchParty();
        if (party.isEmpty()) {
            return;
        }

        List<Player> matched = onlinePlayers(party);
        if (matched.size() < MIN_MATCH_SIZE) {
            requeue(
                    matched,
                    "MATCH-ABANDONED",
                    "&c[RTP Queue] &fThe other players left before the match started, so you are back in the queue."
            );
            return;
        }

        RTPManager.SearchSettings settings = getSearchSettings();
        if (settings == null) {
            requeue(matched, "UNAVAILABLE", "&cThe RTP queue is not available right now.");
            return;
        }

        for (Player player : matched) {
            sendMessage(player, "MATCH-FOUND", "&a[RTP Queue] &fMatch found! Teleporting you to the same area...");
            SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-FOUND"));
        }

        plugin.getRtpManager().findSafeLocationAsync(settings).whenComplete((meetingPoint, throwable) -> {
            if (throwable != null || meetingPoint == null) {
                requeue(
                        matched,
                        "MATCH-FAILED",
                        "&c[RTP Queue] &fNo safe location was found for the match, so you are back in the queue."
                );
                return;
            }
            teleportParty(matched, meetingPoint, settings);
        });
    }

    /**
     * Sends the whole party to the meeting point. Everybody after the first gets their own
     * search within SPREAD-RADIUS of it, so the group lands together without stacking up on a
     * single block, and anybody the spread search cannot place lands on the point itself.
     *
     * <p>The teleports deliberately skip the usual RTP stand-still countdown: one player moving
     * would otherwise cancel their half of the match and strand the rest out in the wild alone.
     */
    private void teleportParty(List<Player> matched, Location meetingPoint, RTPManager.SearchSettings settings) {
        int spreadRadius = getSpreadRadius();
        for (int index = 0; index < matched.size(); index++) {
            Player player = matched.get(index);
            if (index == 0 || spreadRadius <= 0) {
                teleport(player, meetingPoint);
                continue;
            }

            RTPManager.SearchSettings spread = new RTPManager.SearchSettings(
                    settings.worldName(),
                    0,
                    spreadRadius,
                    meetingPoint.getBlockX(),
                    meetingPoint.getBlockZ(),
                    settings.maxAttempts(),
                    settings.maxChunkSamples(),
                    settings.attemptIntervalTicks()
            );
            plugin.getRtpManager().findSafeLocationAsync(spread).whenComplete((nearby, throwable) ->
                    teleport(player, throwable == null && nearby != null ? nearby : meetingPoint));
        }
    }

    private void teleport(Player player, Location destination) {
        plugin.getSpigotScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }
            plugin.getSpigotScheduler().teleport(player, destination).thenAccept(success ->
                    plugin.getSpigotScheduler().runEntity(player, () -> {
                        if (!player.isOnline()) {
                            return;
                        }
                        if (!Boolean.TRUE.equals(success)) {
                            sendMessage(
                                    player,
                                    "MATCH-FAILED",
                                    "&c[RTP Queue] &fNo safe location was found for the match, so you are back in the queue."
                            );
                            rejoin(player.getUniqueId());
                            return;
                        }
                        SoundUtils.play(player, plugin.getConfigManager().getSound("TELEPORT.SUCCESS"));
                    }));
        });
    }

    private void requeue(List<Player> matched, String messageKey, String fallback) {
        for (Player player : matched) {
            if (!player.isOnline()) {
                continue;
            }
            rejoin(player.getUniqueId());
            sendMessage(player, messageKey, fallback);
        }
    }

    private void rejoin(UUID playerId) {
        synchronized (waiting) {
            waiting.add(playerId);
        }
    }

    private List<Player> onlinePlayers(List<UUID> party) {
        List<Player> matched = new ArrayList<>(party.size());
        for (UUID playerId : party) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                matched.add(player);
            }
        }
        return matched;
    }

    private RTPManager.SearchSettings getSearchSettings() {
        String worldName = getWorldName();
        if (worldName == null || worldName.isBlank()) {
            return null;
        }
        return plugin.getRtpManager().getWorldSearchSettings(worldName);
    }

    private void broadcastToQueue(UUID excluded, String messageKey, String fallback, String... replacements) {
        List<UUID> queued;
        synchronized (waiting) {
            queued = new ArrayList<>(waiting);
        }
        for (UUID playerId : queued) {
            if (playerId.equals(excluded)) {
                continue;
            }
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                sendMessage(player, messageKey, fallback, replacements);
            }
        }
    }

    private void sendMessage(Player player, String messageKey, String fallback, String... replacements) {
        FileConfiguration rtp = rtpConfig();
        String message = rtp == null ? fallback : rtp.getString("QUEUE.MESSAGES." + messageKey, fallback);
        if (message == null || message.isBlank()) {
            return;
        }
        for (int index = 0; index + 1 < replacements.length; index += 2) {
            message = message.replace(replacements[index], replacements[index + 1]);
        }
        player.sendMessage(ColorUtils.toComponent(message));
    }

    private FileConfiguration rtpConfig() {
        if (plugin == null || plugin.getConfigManager() == null) {
            return null;
        }
        return plugin.getConfigManager().getRtp();
    }
}
