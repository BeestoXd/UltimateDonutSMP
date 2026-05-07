package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.listeners.PlayerRespawnListener;
import com.bx.ultimateDonutSmp.models.DuelArena;
import com.bx.ultimateDonutSmp.models.DuelClaim;
import com.bx.ultimateDonutSmp.models.DuelMatch;
import com.bx.ultimateDonutSmp.models.DuelRequest;
import com.bx.ultimateDonutSmp.models.DuelStats;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.LocationUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.title.Title;

public class DuelManager {

    public enum ArenaSetting {
        NO_HUNGER("No Hunger"),
        NO_WEATHER("No Weather"),
        ALWAYS_MORNING("Always Morning"),
        NO_FALL_DAMAGE("No Fall Damage");

        private final String displayName;

        ArenaSetting(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final UltimateDonutSmp plugin;
    private final Map<String, DuelArena> arenas = new HashMap<>();
    private final Map<UUID, DuelRequest> requestsByTarget = new HashMap<>();
    private final LinkedHashSet<UUID> queue = new LinkedHashSet<>();
    private final Map<Long, DuelMatch> activeMatches = new HashMap<>();
    private final Map<UUID, Long> activeMatchIds = new HashMap<>();
    private final Set<String> reservedArenaIds = new HashSet<>();
    private final Map<UUID, PendingRespawnState> pendingRespawns = new HashMap<>();
    private final Map<UUID, DuelStats> statsCache = new HashMap<>();
    private final Map<Long, ArenaSnapshot> arenaSnapshots = new HashMap<>();
    private final Set<UUID> transitioningPlayers = new HashSet<>();
    private final Map<UUID, TransitionPlayerState> transitionStates = new HashMap<>();
    private final Map<UUID, TransitionTitleState> transitionTitles = new HashMap<>();
    private long tickCounter = 0L;

    public DuelManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        ensureTables();
        reload();
    }

    public void reload() {
        loadArenas();
        syncArenaRulesForAllOccupants();
    }

    public void refreshArenaAvailability() {
        reload();
    }

    public void shutdown() {
        requestsByTarget.clear();
        queue.clear();
        activeMatches.clear();
        activeMatchIds.clear();
        reservedArenaIds.clear();
        pendingRespawns.clear();
        arenaSnapshots.clear();
        transitioningPlayers.clear();
        transitionStates.clear();
        transitionTitles.clear();
        showAllVanishedPlayers();
    }

    public boolean isEnabled() {
        return config().getBoolean("SETTINGS.ENABLED", true);
    }

    public String getQueueTitle() {
        return config().getString("GUI.QUEUE.TITLE", "&8Casual Queue");
    }

    public int getQueueSize() {
        return normalizeSize(config().getInt("GUI.QUEUE.SIZE", 27));
    }

    public String getCreateTitle(Player target) {
        String name = target == null ? "Player" : target.getName();
        return config().getString("GUI.CREATE.TITLE", "&8Create Duel -> {player}")
                .replace("{player}", name);
    }

    public int getCreateSize() {
        return normalizeSize(config().getInt("GUI.CREATE.SIZE", 27));
    }

    public String getClaimsTitle() {
        return config().getString("GUI.CLAIMS.TITLE", "&8Duel Claims");
    }

    public int getClaimsSize() {
        return normalizeSize(config().getInt("GUI.CLAIMS.SIZE", 54));
    }

    public int getClaimsItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.CLAIMS.ITEMS_PER_PAGE", 45)));
    }

    public int getCountdownSeconds() {
        int configured = Math.max(0, config().getInt("SETTINGS.COUNTDOWN_SECONDS", 5));
        ConfigurationSection section = config().getConfigurationSection("START-COUNTDOWN");
        if (section == null || !section.getBoolean("ENABLED", true)) {
            return configured;
        }

        int maxKey = -1;
        maxKey = Math.max(maxKey, getMaxNumericKey(section.getConfigurationSection("TITLES")));
        maxKey = Math.max(maxKey, getMaxNumericKey(section.getConfigurationSection("MESSAGES")));
        return maxKey >= 0 ? maxKey : configured;
    }

    public int getMatchDurationSeconds() {
        return Math.max(30, config().getInt("SETTINGS.MATCH_DURATION_SECONDS", 900));
    }

    public int getRequestTimeoutSeconds() {
        return Math.max(5, config().getInt("SETTINGS.REQUEST_TIMEOUT_SECONDS", 30));
    }

    public int getDrawTimeoutSeconds() {
        return Math.max(5, config().getInt("SETTINGS.DRAW_REQUEST_TIMEOUT_SECONDS", 15));
    }

    public int getReturnDelayTicks() {
        int seconds = config().getInt("SETTINGS.RETURN_DELAY_SECONDS",
                config().getInt("SETTINGS.WINNER_RETURN_DELAY_SECONDS", 3));
        return Math.max(0, seconds) * 20;
    }

    public int getRollbackHorizontalPadding() {
        return Math.max(0, config().getInt("SETTINGS.ROLLBACK_PADDING_HORIZONTAL", 8));
    }

    public int getRollbackVerticalPadding() {
        return Math.max(0, config().getInt("SETTINGS.ROLLBACK_PADDING_VERTICAL", 6));
    }

    public int getQueueSizeCount() {
        return queue.size();
    }

    public boolean isInQueue(UUID uuid) {
        return uuid != null && queue.contains(uuid);
    }

    public boolean isInDuel(UUID uuid) {
        return uuid != null && activeMatchIds.containsKey(uuid);
    }

    public boolean isInCountdown(UUID uuid) {
        DuelMatch match = getActiveMatch(uuid);
        return match != null && match.getStatus() == DuelMatch.MatchStatus.COUNTDOWN;
    }

    public boolean isTransitioning(UUID uuid) {
        return uuid != null && transitioningPlayers.contains(uuid);
    }

    public boolean isMatchActive(UUID uuid) {
        DuelMatch match = getActiveMatch(uuid);
        return match != null && match.getStatus() == DuelMatch.MatchStatus.ACTIVE;
    }

    public boolean areOpponents(UUID first, UUID second) {
        DuelMatch match = getActiveMatch(first);
        return match != null && match.getStatus() != DuelMatch.MatchStatus.FINISHED
                && match.isParticipant(second);
    }

    public boolean canModifyArena(Player player) {
        if (player == null) {
            return false;
        }

        DuelMatch match = getActiveMatch(player.getUniqueId());
        return match != null
                && match.getStatus() == DuelMatch.MatchStatus.ACTIVE
                && match.getArena().hasRollbackRegion()
                && arenaSnapshots.containsKey(match.getId());
    }

    public boolean shouldBypassGlobalCombat(Player attacker, Player victim) {
        if (victim != null && isInDuel(victim.getUniqueId())) {
            return true;
        }
        return attacker != null && isInDuel(attacker.getUniqueId());
    }

    public DuelStats getStats(UUID uuid) {
        if (uuid == null) {
            return DuelStats.empty();
        }
        return statsCache.computeIfAbsent(uuid, this::loadStats);
    }

    public List<DuelClaim> getClaims(UUID uuid) {
        List<DuelClaim> claims = new ArrayList<>();
        if (uuid == null || connection() == null) {
            return claims;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT match_id, defeated_name, item_data, created_at FROM duel_claims WHERE player_uuid = ? ORDER BY created_at DESC, id ASC")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                Map<Long, ClaimAccumulator> groupedClaims = new LinkedHashMap<>();
                while (rs.next()) {
                    long matchId = rs.getLong("match_id");
                    String defeatedName = rs.getString("defeated_name");
                    long createdAt = rs.getLong("created_at");
                    ItemStack item = deserializeItem(rs.getString("item_data"));
                    if (item == null) {
                        continue;
                    }
                    ClaimAccumulator accumulator = groupedClaims.computeIfAbsent(
                            matchId,
                            ignored -> new ClaimAccumulator(defeatedName, createdAt)
                    );
                    accumulator.items().add(item);
                    accumulator.updateMetadata(defeatedName, createdAt);
                }

                for (Map.Entry<Long, ClaimAccumulator> entry : groupedClaims.entrySet()) {
                    ClaimAccumulator accumulator = entry.getValue();
                    claims.add(new DuelClaim(
                            entry.getKey(),
                            uuid,
                            accumulator.defeatedName(),
                            new ArrayList<>(accumulator.items()),
                            accumulator.createdAt()
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load duel claims for " + uuid, e);
        }

        return claims;
    }

    public DuelClaim getClaim(UUID playerUuid, long matchId) {
        if (playerUuid == null || matchId <= 0L) {
            return null;
        }

        for (DuelClaim claim : getClaims(playerUuid)) {
            if (claim.matchId() == matchId) {
                return claim;
            }
        }
        return null;
    }

    public boolean claim(Player player, long matchId) {
        if (player == null || matchId <= 0L || connection() == null) {
            return false;
        }

        DuelClaim claim = getClaim(player.getUniqueId(), matchId);
        if (claim == null || claim.items() == null || claim.items().isEmpty()) {
            send(player, "&cThat duel claim no longer exists.");
            return false;
        }

        List<ClaimItemRow> claimRows = loadClaimItemRows(player.getUniqueId(), matchId);
        if (claimRows.isEmpty()) {
            send(player, "&cThat duel claim no longer exists.");
            return false;
        }

        List<Long> claimedRowIds = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        int remainingCount = 0;
        for (ClaimItemRow row : claimRows) {
            ItemStack item = row.item();
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }

            if (!canFullyFit(inventory, item)) {
                remainingCount++;
                continue;
            }

            inventory.addItem(item.clone());
            claimedRowIds.add(row.id());
        }

        if (claimedRowIds.isEmpty()) {
            send(player, "&cMake room in your inventory before claiming that loot.");
            return false;
        }

        try (PreparedStatement deleteOne = connection().prepareStatement(
                "DELETE FROM duel_claims WHERE id = ? AND player_uuid = ?")) {
            for (Long rowId : claimedRowIds) {
                deleteOne.setLong(1, rowId);
                deleteOne.setString(2, player.getUniqueId().toString());
                deleteOne.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete duel claim package " + matchId, e);
            send(player, "&cCould not claim that loot right now.");
            return false;
        }

        play(player, "DUELS.CLAIM");
        String defeatedName = claim.defeatedName() == null || claim.defeatedName().isBlank()
                ? "Unknown"
                : claim.defeatedName();
        if (remainingCount > 0) {
            send(player, "&eClaimed some duel loot from &f" + defeatedName + "&e. "
                    + "&7Some items are still waiting in Claims.");
        } else {
            send(player, "&aClaimed duel loot from &f" + defeatedName + "&a.");
        }
        return true;
    }

    public boolean deleteClaim(Player player, long matchId) {
        if (player == null || matchId <= 0L || connection() == null) {
            return false;
        }

        DuelClaim claim = getClaim(player.getUniqueId(), matchId);
        if (claim == null) {
            send(player, "&cThat duel claim no longer exists.");
            return false;
        }

        int deletedRows;
        try (PreparedStatement deletePackage = connection().prepareStatement(
                "DELETE FROM duel_claims WHERE match_id = ? AND player_uuid = ?")) {
            deletePackage.setLong(1, matchId);
            deletePackage.setString(2, player.getUniqueId().toString());
            deletedRows = deletePackage.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete duel claim package " + matchId, e);
            send(player, "&cCould not delete that duel claim right now.");
            return false;
        }

        if (deletedRows <= 0) {
            send(player, "&cThat duel claim no longer exists.");
            return false;
        }

        String defeatedName = claim.defeatedName() == null || claim.defeatedName().isBlank()
                ? "Unknown"
                : claim.defeatedName();
        send(player, "&cDeleted duel loot claim from &f" + defeatedName + "&c.");
        return true;
    }

    public List<DuelArena> getArenas() {
        List<DuelArena> values = new ArrayList<>(arenas.values());
        values.sort(Comparator.comparing(DuelArena::getId, String.CASE_INSENSITIVE_ORDER));
        return values;
    }

    public DuelArena getArena(String id) {
        if (id == null) {
            return null;
        }
        return arenas.get(normalizeArenaId(id));
    }

    public DuelArena getSessionArena(UUID uuid) {
        DuelMatch match = getActiveMatch(uuid);
        return match == null ? null : match.getArena();
    }

    public boolean hasArenaSetting(UUID uuid, ArenaSetting setting) {
        if (setting == null) {
            return false;
        }

        DuelArena arena = getSessionArena(uuid);
        if (arena == null) {
            return false;
        }

        return switch (setting) {
            case NO_HUNGER -> arena.isNoHunger();
            case NO_WEATHER -> arena.isNoWeather();
            case ALWAYS_MORNING -> arena.isAlwaysMorning();
            case NO_FALL_DAMAGE -> arena.isNoFallDamage();
        };
    }

    public List<DuelArena> getReadyEnabledArenas() {
        List<DuelArena> result = new ArrayList<>();
        for (DuelArena arena : getArenas()) {
            if (arena.isEnabled() && arena.isReady()) {
                result.add(arena);
            }
        }
        return result;
    }

    public List<DuelArena> getReadyQueueArenas() {
        List<DuelArena> result = new ArrayList<>();
        for (DuelArena arena : getArenas()) {
            if (arena.isEnabled() && arena.isQueueEnabled() && arena.isReady()) {
                result.add(arena);
            }
        }
        return result;
    }

    public boolean createArena(String id) {
        String normalized = normalizeArenaId(id);
        if (normalized == null || arenas.containsKey(normalized)) {
            return false;
        }

        DuelArena arena = new DuelArena(
                normalized,
                prettifyId(normalized),
                null,
                null,
                null,
                null,
                null,
                true,
                true,
                false,
                false,
                false,
                false
        );
        arenas.put(normalized, arena);
        saveArena(arena);
        synchronizeArenaSettingsConfig();
        return true;
    }

    public boolean deleteArena(String id) {
        DuelArena arena = getArena(id);
        if (arena == null || reservedArenaIds.contains(arena.getId())) {
            return false;
        }

        arenas.remove(arena.getId());
        if (connection() == null) {
            return true;
        }

        try (PreparedStatement ps = connection().prepareStatement("DELETE FROM duel_arenas WHERE id = ?")) {
            ps.setString(1, arena.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete duel arena " + arena.getId(), e);
            return false;
        }
    }

    public boolean setArenaSpawn(String id, int spawnIndex, Location location) {
        DuelArena arena = getArena(id);
        if (arena == null || location == null || location.getWorld() == null) {
            return false;
        }

        if (spawnIndex == 1) {
            arena.setSpawn1(location);
        } else if (spawnIndex == 2) {
            arena.setSpawn2(location);
        } else {
            return false;
        }

        saveArena(arena);
        return true;
    }

    public boolean setArenaReturn(String id, Location location) {
        DuelArena arena = getArena(id);
        if (arena == null || location == null || location.getWorld() == null) {
            return false;
        }

        arena.setReturnLocation(location);
        saveArena(arena);
        return true;
    }

    public boolean setArenaRegionPos(String id, int posIndex, Location location) {
        DuelArena arena = getArena(id);
        if (arena == null || location == null || location.getWorld() == null) {
            return false;
        }

        Location blockLocation = location.getBlock().getLocation();
        if (posIndex == 1) {
            arena.setSpawn1(location);
            arena.setRegionPos1(blockLocation);
        } else if (posIndex == 2) {
            arena.setSpawn2(location);
            arena.setRegionPos2(blockLocation);
        } else {
            return false;
        }

        saveArena(arena);
        return true;
    }

    public boolean setArenaDisplayName(String id, String displayName) {
        DuelArena arena = getArena(id);
        if (arena == null || displayName == null || displayName.isBlank()) {
            return false;
        }

        arena.setDisplayName(displayName.trim());
        saveArena(arena);
        return true;
    }

    public boolean setArenaEnabled(String id, boolean enabled) {
        DuelArena arena = getArena(id);
        if (arena == null) {
            return false;
        }

        arena.setEnabled(enabled);
        saveArena(arena);
        return true;
    }

    public boolean setArenaQueueEnabled(String id, boolean enabled) {
        DuelArena arena = getArena(id);
        if (arena == null) {
            return false;
        }

        arena.setQueueEnabled(enabled);
        saveArena(arena);
        return true;
    }

    public boolean sendChallenge(Player challenger, Player target, String arenaId) {
        if (!isEnabled()) {
            send(challenger, "&cDuels are currently disabled.");
            return false;
        }
        if (challenger == null || target == null) {
            return false;
        }
        if (challenger.getUniqueId().equals(target.getUniqueId())) {
            send(challenger, "&cYou cannot duel yourself.");
            return false;
        }
        if (!canEnterDuel(challenger, true) || !canEnterDuel(target, false)) {
            return false;
        }

        String preferredArenaId = normalizeArenaId(arenaId);
        if (preferredArenaId != null) {
            DuelArena arena = getArena(preferredArenaId);
            if (arena == null || !arena.isEnabled() || !arena.isReady()) {
                send(challenger, "&cThat arena is not available.");
                return false;
            }
        } else if (getReadyEnabledArenas().isEmpty()) {
            send(challenger, "&cThere are no duel arenas ready yet.");
            return false;
        }

        removeRequestsFor(challenger.getUniqueId(), false);
        removeRequestsFor(target.getUniqueId(), false);

        long expiresAt = System.currentTimeMillis() + (getRequestTimeoutSeconds() * 1000L);
        DuelRequest request = new DuelRequest(
                challenger.getUniqueId(),
                challenger.getName(),
                target.getUniqueId(),
                target.getName(),
                preferredArenaId,
                expiresAt
        );
        requestsByTarget.put(target.getUniqueId(), request);

        send(challenger, "&aSent a duel request to &f" + target.getName() + "&a.");
        send(target, "&e" + challenger.getName() + " &fhas challenged you to a duel.");
        send(target, "&7Use &f/duel accept " + challenger.getName() + " &7or &f/duel deny " + challenger.getName() + "&7.");
        play(challenger, "DUELS.REQUEST-SENT");
        play(target, "DUELS.REQUEST-RECEIVED");
        return true;
    }

    public boolean acceptChallenge(Player target, String challengerName) {
        DuelRequest request = requestsByTarget.get(target.getUniqueId());
        if (request == null) {
            send(target, "&cYou have no pending duel request.");
            return false;
        }
        if (request.isExpired(System.currentTimeMillis())) {
            requestsByTarget.remove(target.getUniqueId());
            send(target, "&cThat duel request has expired.");
            return false;
        }
        if (challengerName != null && !challengerName.isBlank()
                && !request.challengerName().equalsIgnoreCase(challengerName.trim())) {
            send(target, "&cYour pending duel request is from &f" + request.challengerName() + "&c.");
            return false;
        }

        Player challenger = Bukkit.getPlayer(request.challengerUuid());
        if (challenger == null || !challenger.isOnline()) {
            requestsByTarget.remove(target.getUniqueId());
            send(target, "&cThat challenger is no longer online.");
            return false;
        }

        requestsByTarget.remove(target.getUniqueId());
        if (!canEnterDuel(challenger, true) || !canEnterDuel(target, false)) {
            return false;
        }

        DuelArena arena = findAvailableArena(request.arenaId(), false);
        if (arena == null) {
            send(target, "&cNo duel arena is available right now.");
            send(challenger, "&cYour duel request could not start because no arena is available.");
            return false;
        }

        startMatch(challenger, target, arena, DuelMatch.MatchType.DIRECT);
        return true;
    }

    public boolean denyChallenge(Player target, String challengerName) {
        DuelRequest request = requestsByTarget.get(target.getUniqueId());
        if (request == null) {
            send(target, "&cYou have no pending duel request.");
            return false;
        }
        if (challengerName != null && !challengerName.isBlank()
                && !request.challengerName().equalsIgnoreCase(challengerName.trim())) {
            send(target, "&cYour pending duel request is from &f" + request.challengerName() + "&c.");
            return false;
        }

        requestsByTarget.remove(target.getUniqueId());
        Player challenger = Bukkit.getPlayer(request.challengerUuid());
        if (challenger != null) {
            send(challenger, "&c" + target.getName() + " denied your duel request.");
        }
        send(target, "&eDenied duel request from &f" + request.challengerName() + "&e.");
        return true;
    }

    public boolean joinQueue(Player player) {
        if (!isEnabled()) {
            send(player, "&cDuels are currently disabled.");
            return false;
        }
        if (getReadyQueueArenas().isEmpty()) {
            send(player, buildQueueUnavailableMessage());
            return false;
        }
        if (!canEnterDuel(player, true)) {
            return false;
        }
        if (queue.contains(player.getUniqueId())) {
            send(player, "&eYou are already in the casual duel queue.");
            return false;
        }

        removeRequestsFor(player.getUniqueId(), false);
        queue.add(player.getUniqueId());
        send(player, "&aJoined the casual duel queue.");
        play(player, "DUELS.QUEUE-JOIN");
        attemptQueueMatchmaking();
        return true;
    }

    public boolean leaveState(Player player) {
        if (player == null) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (queue.remove(uuid)) {
            send(player, "&eYou left the casual duel queue.");
            return true;
        }

        DuelRequest incoming = requestsByTarget.remove(uuid);
        if (incoming != null) {
            send(player, "&eYour pending duel request was cleared.");
            return true;
        }

        if (removeOutgoingRequest(uuid)) {
            send(player, "&eYour outgoing duel request was cancelled.");
            return true;
        }

        DuelMatch match = getActiveMatch(uuid);
        if (match != null) {
            boolean active = match.getStatus() == DuelMatch.MatchStatus.ACTIVE;
            handleForfeit(player, active ? "FORFEIT" : "COUNTDOWN_LEAVE", active);
            return true;
        }

        send(player, "&cYou are not in a duel or queue.");
        return false;
    }

    public boolean requestDraw(Player player) {
        DuelMatch match = getActiveMatch(player.getUniqueId());
        if (match == null || match.getStatus() != DuelMatch.MatchStatus.ACTIVE) {
            send(player, "&cYou can only request a draw during an active duel.");
            return false;
        }

        UUID requester = player.getUniqueId();
        UUID opponentUuid = match.getOpponent(requester);
        Player opponent = Bukkit.getPlayer(opponentUuid);
        if (opponent == null || !opponent.isOnline()) {
            send(player, "&cYour opponent is no longer online.");
            return false;
        }

        if (match.getDrawRequester() == null) {
            match.setDrawRequester(requester);
            match.setDrawRequestExpiresAt(System.currentTimeMillis() + (getDrawTimeoutSeconds() * 1000L));
            send(player, "&eDraw request sent to &f" + opponent.getName() + "&e.");
            send(opponent, "&e" + player.getName() + " &fhas requested a draw. Use &f/draw &fto accept.");
            return true;
        }

        if (requester.equals(match.getDrawRequester())) {
            send(player, "&eYou already requested a draw.");
            return false;
        }

        finishMatch(match, null, null, "DRAW", false, List.of(), true);
        return true;
    }

    public void tick() {
        tickCounter++;
        boolean secondPulse = tickCounter % 20L == 0L;
        if (secondPulse) {
            expireRequests();
            cleanupQueue();
            attemptQueueMatchmaking();
        }

        long now = System.currentTimeMillis();
        List<DuelMatch> matches = new ArrayList<>(activeMatches.values());
        for (DuelMatch match : matches) {
            if (match.getStatus() == DuelMatch.MatchStatus.COUNTDOWN) {
                if (secondPulse) {
                    tickCountdown(match);
                }
                continue;
            }

            if (match.getStatus() != DuelMatch.MatchStatus.ACTIVE) {
                continue;
            }

            if (match.getDrawRequester() != null && now >= match.getDrawRequestExpiresAt()) {
                UUID requester = match.getDrawRequester();
                match.setDrawRequester(null);
                match.setDrawRequestExpiresAt(0L);
                Player requesterPlayer = Bukkit.getPlayer(requester);
                if (requesterPlayer != null) {
                    send(requesterPlayer, "&cYour draw request expired.");
                }
            }

            long remaining = Math.max(0L, (match.getEndsAt() - now + 999L) / 1000L);
            sendMatchActionBar(match, remaining);
            if (now >= match.getEndsAt()) {
                finishMatch(match, null, null, "TIMEOUT", false, List.of(), true);
            }
        }
    }

    public boolean handleDuelDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        DuelMatch match = getActiveMatch(victim.getUniqueId());
        if (match == null) {
            return false;
        }

        UUID victimUuid = victim.getUniqueId();
        UUID winnerUuid = match.getOpponent(victimUuid);
        Player winner = winnerUuid == null ? null : Bukkit.getPlayer(winnerUuid);
        List<ItemStack> loot = copyLoot(event.getDrops());

        event.deathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);

        finishMatch(match, winnerUuid, victimUuid, "DEATH", false, loot, true);

        if (winner != null) {
            send(winner, "&aYou defeated &f" + victim.getName() + "&a.");
        }
        send(victim, "&cYou lost the duel against &f" + match.getOpponentName(victimUuid) + "&c.");
        return true;
    }

    public boolean handleLethalPvPHit(Player attacker, Player victim) {
        if (attacker == null || victim == null) {
            return false;
        }

        DuelMatch match = getActiveMatch(victim.getUniqueId());
        if (match == null || match.getStatus() != DuelMatch.MatchStatus.ACTIVE) {
            return false;
        }
        if (!match.isParticipant(attacker.getUniqueId())
                || !attacker.getUniqueId().equals(match.getOpponent(victim.getUniqueId()))) {
            return false;
        }

        List<ItemStack> loot = extractInventory(victim);
        finishMatch(match, attacker.getUniqueId(), victim.getUniqueId(), "PVP_KILL", false, loot, true);
        PlayerRespawnListener.scheduleChainmailKit(plugin, victim, getReturnDelayTicks() + 2L);

        send(attacker, "&aYou defeated &f" + victim.getName() + "&a.");
        send(victim, "&cYou lost the duel against &f" + attacker.getName() + "&c.");
        return true;
    }

    public boolean consumeRespawn(Player player, org.bukkit.event.player.PlayerRespawnEvent event) {
        PendingRespawnState state = pendingRespawns.remove(player.getUniqueId());
        if (state == null || state.respawnLocation() == null || state.respawnLocation().getWorld() == null) {
            return false;
        }

        event.setRespawnLocation(state.respawnLocation());
        plugin.getPaperScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }
            applyTransitionState(player);
            showStoredTransitionTitle(player);
            teleportAfterDelay(player.getUniqueId(), state.returnLocation(), state.delayTicks(), true);
        });
        return true;
    }

    public void handleJoin(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (isTransitioning(uuid) || transitionStates.containsKey(uuid)) {
            restoreTransitionState(player);
        }

        DuelArena arena = findArenaContainingLocation(player.getLocation());
        if (arena == null) {
            return;
        }

        Location fallbackLocation = resolveArenaJoinFallbackLocation(arena, player.getLocation());
        if (fallbackLocation == null || fallbackLocation.getWorld() == null) {
            return;
        }

        plugin.getPaperScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }

            DuelArena currentArena = findArenaContainingLocation(player.getLocation());
            if (currentArena == null) {
                return;
            }

            Location destination = resolveArenaJoinFallbackLocation(currentArena, player.getLocation());
            if (destination == null || destination.getWorld() == null) {
                return;
            }

            String arenaName = currentArena.getDisplayName();
            plugin.getPaperScheduler().teleport(player, destination).thenAccept(success ->
                    plugin.getPaperScheduler().runEntity(player, () -> {
                        if (!Boolean.TRUE.equals(success) || !player.isOnline()) {
                            return;
                        }
                        player.resetPlayerTime();
                        player.resetPlayerWeather();
                        player.setNoDamageTicks(60);
                        player.setFallDistance(0F);
                        player.setFireTicks(0);
                        send(player, "&eYou were moved out of duel arena &f" + arenaName + "&e after reconnecting.");
                    }));
        });
    }

    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }

        if (isTransitioning(player.getUniqueId()) || transitionStates.containsKey(player.getUniqueId())) {
            restoreTransitionState(player);
        }

        pendingRespawns.remove(player.getUniqueId());
        queue.remove(player.getUniqueId());
        requestsByTarget.remove(player.getUniqueId());
        removeOutgoingRequest(player.getUniqueId());

        DuelMatch match = getActiveMatch(player.getUniqueId());
        if (match == null) {
            return;
        }

        boolean active = match.getStatus() == DuelMatch.MatchStatus.ACTIVE;
        handleForfeit(player, active ? "QUIT" : "COUNTDOWN_QUIT", active);
    }

    private void tickCountdown(DuelMatch match) {
        Player first = Bukkit.getPlayer(match.getPlayerOneUuid());
        Player second = Bukkit.getPlayer(match.getPlayerTwoUuid());
        if (first == null || second == null) {
            finishMatch(match, null, null, "COUNTDOWN_FAIL", true, List.of(), false);
            return;
        }

        int remaining = match.getCountdownSecondsRemaining();
        if (remaining > 0) {
            sendCountdownTick(first, remaining);
            sendCountdownTick(second, remaining);
            match.decrementCountdown();
            return;
        }

        match.setStatus(DuelMatch.MatchStatus.ACTIVE);
        long now = System.currentTimeMillis();
        match.setStartedAt(now);
        match.setEndsAt(now + (getMatchDurationSeconds() * 1000L));
        sendCountdownStart(first);
        sendCountdownStart(second);
        send(first, "&aDuel started against &f" + second.getName() + "&a.");
        send(second, "&aDuel started against &f" + first.getName() + "&a.");
        playCountdownStartSound(first);
        playCountdownStartSound(second);
    }

    private void sendCountdownTick(Player player, int remaining) {
        if (player == null || !player.isOnline()) {
            return;
        }

        ConfigurationSection countdown = config().getConfigurationSection("START-COUNTDOWN");
        if (countdown == null || !countdown.getBoolean("ENABLED", true)) {
            player.showTitle(Title.title(
                    ColorUtils.toComponent("&e" + remaining),
                    ColorUtils.toComponent("&7Duel starts soon"),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(750), Duration.ofMillis(250))
            ));
            return;
        }

        ConfigurationSection titles = countdown.getConfigurationSection("TITLES");
        String title = titles == null ? null : titles.getString(String.valueOf(remaining));
        if (title == null) {
            title = "&e" + remaining;
        }

        player.showTitle(Title.title(
                ColorUtils.toComponent(title),
                ColorUtils.toComponent(""),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1000), Duration.ZERO)
        ));

        ConfigurationSection messages = countdown.getConfigurationSection("MESSAGES");
        String message = messages == null ? null : messages.getString(String.valueOf(remaining));
        if (message != null && !message.isBlank()) {
            send(player, message);
        }

        playCountdownTickSound(player, remaining);
    }

    private void sendCountdownStart(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        ConfigurationSection countdown = config().getConfigurationSection("START-COUNTDOWN");
        if (countdown == null || !countdown.getBoolean("ENABLED", true)) {
            player.showTitle(Title.title(
                    ColorUtils.toComponent("&aFight!"),
                    ColorUtils.toComponent("&7Defeat your opponent"),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(1250), Duration.ofMillis(500))
            ));
            return;
        }

        ConfigurationSection titles = countdown.getConfigurationSection("TITLES");
        String title = titles == null ? null : titles.getString("0");
        if (title == null) {
            title = "&a&lFight!";
        }

        player.showTitle(Title.title(
                ColorUtils.toComponent(title),
                ColorUtils.toComponent(""),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ofMillis(150))
        ));

        String startMessage = countdown.getString("START-MESSAGE", "");
        if (!startMessage.isBlank()) {
            send(player, startMessage);
        }
    }

    private void playCountdownTickSound(Player player, int remaining) {
        ConfigurationSection countdown = config().getConfigurationSection("START-COUNTDOWN");
        if (player == null || countdown == null || !countdown.getBoolean("SOUNDS.ENABLED", true)) {
            return;
        }

        String sound = plugin.getConfigManager().getSound("DUELS.START-COUNTDOWN.PER-SECOND." + remaining);
        if (sound != null && !sound.isBlank()) {
            SoundUtils.play(player, sound);
        }
    }

    private void playCountdownStartSound(Player player) {
        ConfigurationSection countdown = config().getConfigurationSection("START-COUNTDOWN");
        if (player == null) {
            return;
        }

        if (countdown != null && countdown.getBoolean("SOUNDS.ENABLED", true)) {
            String sound = plugin.getConfigManager().getSound("DUELS.START-COUNTDOWN.START-SOUND");
            if (sound != null && !sound.isBlank()) {
                SoundUtils.play(player, sound);
                return;
            }
        }

        play(player, "DUELS.MATCH-START");
    }

    private void sendMatchActionBar(DuelMatch match, long remainingSeconds) {
        Player first = Bukkit.getPlayer(match.getPlayerOneUuid());
        Player second = Bukkit.getPlayer(match.getPlayerTwoUuid());
        if (first != null && first.isOnline()) {
            String text = "&eOpponent: &f" + match.getPlayerTwoName() + " &8| &eTime Left: &f" + formatDuration(remainingSeconds);
            if (match.getDrawRequester() != null && !match.getDrawRequester().equals(first.getUniqueId())) {
                text += " &8| &a/draw to accept";
            }
            first.sendActionBar(ColorUtils.toComponent(text));
        }
        if (second != null && second.isOnline()) {
            String text = "&eOpponent: &f" + match.getPlayerOneName() + " &8| &eTime Left: &f" + formatDuration(remainingSeconds);
            if (match.getDrawRequester() != null && !match.getDrawRequester().equals(second.getUniqueId())) {
                text += " &8| &a/draw to accept";
            }
            second.sendActionBar(ColorUtils.toComponent(text));
        }
    }

    private void handleForfeit(Player loser, String reason, boolean transferLoot) {
        if (loser == null) {
            return;
        }

        DuelMatch match = getActiveMatch(loser.getUniqueId());
        if (match == null) {
            return;
        }

        UUID loserUuid = loser.getUniqueId();
        UUID winnerUuid = match.getOpponent(loserUuid);
        List<ItemStack> loot = transferLoot ? extractInventory(loser) : List.of();
        finishMatch(match, winnerUuid, loserUuid, reason, false, loot, true);
    }

    private void finishMatch(DuelMatch match,
                             UUID winnerUuid,
                             UUID loserUuid,
                             String endReason,
                             boolean cancelled,
                             List<ItemStack> loot,
                             boolean recordStats) {
        if (match == null || match.getStatus() == DuelMatch.MatchStatus.FINISHED) {
            return;
        }

        match.setStatus(DuelMatch.MatchStatus.FINISHED);
        activeMatches.remove(match.getId());
        activeMatchIds.remove(match.getPlayerOneUuid());
        activeMatchIds.remove(match.getPlayerTwoUuid());
        reservedArenaIds.remove(match.getArena().getId());

        Player winner = winnerUuid == null ? null : Bukkit.getPlayer(winnerUuid);
        Player loser = loserUuid == null ? null : Bukkit.getPlayer(loserUuid);

        if (recordStats) {
            if (winnerUuid != null && loserUuid != null) {
                updateStatsAfterWin(winnerUuid, loserUuid);
            } else if (!cancelled) {
                updateStatsAfterDraw(match.getPlayerOneUuid(), match.getPlayerTwoUuid());
            }
        }

        updateMatchRecord(match, winnerUuid, loserUuid, endReason);

        if (winnerUuid != null && !loot.isEmpty()) {
            storeLootClaimPackage(winnerUuid, winner, match.getId(), resolveParticipantName(match, loserUuid), loot);
        }

        if (winner != null && loser != null) {
            storeTransitionTitle(
                    winner.getUniqueId(),
                    formatResultTitle("victory", winner.getName(), loser.getName(), "&e&lVICTORY!"),
                    formatResultSubtitle("victory", winner.getName(), loser.getName(), "&e" + winner.getName() + " &fwon the Match!")
            );
            storeTransitionTitle(
                    loser.getUniqueId(),
                    formatResultTitle("defeat", loser.getName(), winner.getName(), "&c&lDEFEAT!"),
                    formatResultSubtitle("defeat", loser.getName(), winner.getName(), "&c" + winner.getName() + " &fwon this Match!")
            );
            play(winner, "DUELS.VICTORY");
            play(loser, "DUELS.DEFEAT");
        } else if (!cancelled) {
            Player first = Bukkit.getPlayer(match.getPlayerOneUuid());
            Player second = Bukkit.getPlayer(match.getPlayerTwoUuid());
            String drawTitle = formatResultTitle("draw", null, null, "&e&lDRAW!");
            String drawSubtitle = formatResultSubtitle(
                    "draw",
                    null,
                    null,
                    "TIMEOUT".equalsIgnoreCase(endReason) ? "&fTime's up - no winner." : "&7No one won this duel"
            );
            if (first != null) {
                storeTransitionTitle(first.getUniqueId(), drawTitle, drawSubtitle);
            }
            if (second != null) {
                storeTransitionTitle(second.getUniqueId(), drawTitle, drawSubtitle);
            }

            if ("TIMEOUT".equalsIgnoreCase(endReason)) {
                String timeoutMessage = formatResultMessage(
                        "draw",
                        null,
                        null,
                        "&e[Timer] &fTime limit reached! Match ended as a &eDRAW &f- streaks unchanged."
                );
                if (first != null && !timeoutMessage.isBlank()) {
                    send(first, timeoutMessage);
                }
                if (second != null && !timeoutMessage.isBlank()) {
                    send(second, timeoutMessage);
                }
            }
        }

        scheduleTransitionAndReturn(match, winnerUuid, loserUuid, endReason);
        plugin.getCombatManager().clearTag(match.getPlayerOneUuid());
        plugin.getCombatManager().clearTag(match.getPlayerTwoUuid());
    }

    private void scheduleTransitionAndReturn(DuelMatch match, UUID winnerUuid, UUID loserUuid, String endReason) {
        int delayTicks = getReturnDelayTicks();

        prepareTransition(match.getPlayerOneUuid());
        prepareTransition(match.getPlayerTwoUuid());

        if ("DEATH".equalsIgnoreCase(endReason) && loserUuid != null) {
            pendingRespawns.put(loserUuid, new PendingRespawnState(
                    resolveArenaStayLocation(match, loserUuid),
                    resolveReturnLocation(match, loserUuid),
                    delayTicks
            ));
        }

        if (loserUuid != null && shouldGrantPostReturnRespawnKit(endReason)) {
            Player loser = Bukkit.getPlayer(loserUuid);
            if (loser != null && loser.isOnline()) {
                PlayerRespawnListener.scheduleChainmailKit(plugin, loser, delayTicks + 2L);
            }
        }

        if (winnerUuid != null) {
            teleportAfterDelay(winnerUuid, resolveReturnLocation(match, winnerUuid), delayTicks, true);
        }

        if (loserUuid != null && !"DEATH".equalsIgnoreCase(endReason)) {
            teleportAfterDelay(loserUuid, resolveReturnLocation(match, loserUuid), delayTicks, true);
        }

        if (winnerUuid == null && loserUuid == null) {
            teleportAfterDelay(match.getPlayerOneUuid(), resolveReturnLocation(match, match.getPlayerOneUuid()), delayTicks, true);
            teleportAfterDelay(match.getPlayerTwoUuid(), resolveReturnLocation(match, match.getPlayerTwoUuid()), delayTicks, true);
        }

        plugin.getPaperScheduler().runGlobalLater(() -> rollbackArena(match.getId()), delayTicks + 1L);
    }

    private void teleportAfterDelay(UUID uuid, Location location, long delayTicks, boolean clearTransition) {
        if (uuid == null || location == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }

        plugin.getPaperScheduler().runEntityLater(player, () -> {
            if (player.isOnline()) {
                plugin.getPaperScheduler().teleport(player, location).thenAccept(success ->
                        plugin.getPaperScheduler().runEntity(player, () -> {
                            if (!Boolean.TRUE.equals(success) || !player.isOnline()) {
                                return;
                            }
                            healPlayer(player);
                            if (clearTransition) {
                                restoreTransitionState(player);
                            }
                        }));
            }
        }, delayTicks);
    }

    private boolean shouldGrantPostReturnRespawnKit(String endReason) {
        if (endReason == null || endReason.isBlank()) {
            return false;
        }

        return endReason.equalsIgnoreCase("FORFEIT")
                || endReason.equalsIgnoreCase("QUIT");
    }

    private void healPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        }
        player.setFoodLevel(20);
        player.setSaturation(20F);
        player.setFireTicks(0);
        player.setFallDistance(0F);
    }

    private void storeTransitionTitle(UUID uuid, String title, String subtitle) {
        if (uuid == null) {
            return;
        }

        transitionTitles.put(uuid, new TransitionTitleState(title, subtitle));
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            showStoredTransitionTitle(player);
        }
    }

    private void showStoredTransitionTitle(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        TransitionTitleState state = transitionTitles.get(player.getUniqueId());
        if (state == null) {
            return;
        }

        long delayMillis = Math.max(1000L, getReturnDelayTicks() * 50L);
        player.showTitle(Title.title(
                ColorUtils.toComponent(state.title()),
                ColorUtils.toComponent(state.subtitle()),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(delayMillis), Duration.ZERO)
        ));
    }

    private String formatResultTitle(String key, String playerName, String opponentName, String fallback) {
        String raw = config().getString("RESULT-TITLES." + key + ".title", fallback);
        return applyResultPlaceholders(raw, playerName, opponentName);
    }

    private String formatResultSubtitle(String key, String playerName, String opponentName, String fallback) {
        String raw = config().getString("RESULT-TITLES." + key + ".subtitle", fallback);
        return applyResultPlaceholders(raw, playerName, opponentName);
    }

    private String formatResultMessage(String key, String playerName, String opponentName, String fallback) {
        String raw = config().getString("RESULT-TITLES." + key + ".message", fallback);
        return applyResultPlaceholders(raw, playerName, opponentName);
    }

    private String applyResultPlaceholders(String text, String playerName, String opponentName) {
        String resolved = text == null ? "" : text;
        resolved = resolved.replace("<player>", playerName == null ? "Player" : playerName);
        resolved = resolved.replace("<opponent>", opponentName == null ? "Opponent" : opponentName);
        return resolved;
    }

    private void updateStatsAfterWin(UUID winnerUuid, UUID loserUuid) {
        DuelStats winnerStats = getStats(winnerUuid).recordWin();
        DuelStats loserStats = getStats(loserUuid).recordLoss();
        statsCache.put(winnerUuid, winnerStats);
        statsCache.put(loserUuid, loserStats);
        saveStats(winnerUuid, winnerStats);
        saveStats(loserUuid, loserStats);
    }

    private void updateStatsAfterDraw(UUID firstUuid, UUID secondUuid) {
        DuelStats firstStats = getStats(firstUuid).recordDraw();
        DuelStats secondStats = getStats(secondUuid).recordDraw();
        statsCache.put(firstUuid, firstStats);
        statsCache.put(secondUuid, secondStats);
        saveStats(firstUuid, firstStats);
        saveStats(secondUuid, secondStats);
    }

    private void storeLootClaimPackage(UUID winnerUuid, Player winner, long matchId, String defeatedName, List<ItemStack> loot) {
        if (winnerUuid == null || loot == null || loot.isEmpty()) {
            return;
        }

        for (ItemStack item : loot) {
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }
            createClaim(winnerUuid, matchId, defeatedName, item.clone());
        }

        if (winner != null) {
            String name = defeatedName == null || defeatedName.isBlank() ? "your opponent" : defeatedName;
            send(winner, "&eLoot from &f" + name + " &ehas been sent to your duel Claims.");
        }
    }

    private void createClaim(UUID playerUuid, long matchId, String defeatedName, ItemStack item) {
        if (playerUuid == null || item == null || item.getType().isAir() || connection() == null) {
            return;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "INSERT INTO duel_claims (player_uuid, match_id, defeated_name, item_data, created_at) VALUES (?,?,?,?,?)")) {
            ps.setString(1, playerUuid.toString());
            ps.setLong(2, matchId);
            ps.setString(3, defeatedName == null ? "" : defeatedName);
            ps.setString(4, serializeItem(item));
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create duel claim for " + playerUuid, e);
        }
    }

    private void attemptQueueMatchmaking() {
        if (queue.size() < 2) {
            return;
        }

        List<UUID> queuedPlayers = new ArrayList<>(queue);
        for (UUID uuid : queuedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || isInDuel(uuid)) {
                queue.remove(uuid);
            }
        }

        while (queue.size() >= 2) {
            DuelArena arena = findAvailableArena(null, true);
            if (arena == null) {
                return;
            }

            List<UUID> available = new ArrayList<>(queue);
            if (available.size() < 2) {
                return;
            }

            UUID firstUuid = available.get(0);
            UUID secondUuid = available.get(1);
            Player first = Bukkit.getPlayer(firstUuid);
            Player second = Bukkit.getPlayer(secondUuid);
            if (first == null || second == null || !first.isOnline() || !second.isOnline()) {
                queue.remove(firstUuid);
                queue.remove(secondUuid);
                continue;
            }

            queue.remove(firstUuid);
            queue.remove(secondUuid);
            startMatch(first, second, arena, DuelMatch.MatchType.QUEUE);
        }
    }

    private void startMatch(Player first, Player second, DuelArena arena, DuelMatch.MatchType type) {
        if (first == null || second == null || arena == null) {
            return;
        }

        long matchId = insertMatch(type, arena, first.getUniqueId(), second.getUniqueId());
        if (matchId <= 0L) {
            send(first, "&cCould not start the duel right now.");
            send(second, "&cCould not start the duel right now.");
            return;
        }

        DuelMatch match = new DuelMatch(
                matchId,
                type,
                arena,
                first.getUniqueId(),
                first.getName(),
                second.getUniqueId(),
                second.getName(),
                getCountdownSeconds()
        );
        match.setReturnLocation(first.getUniqueId(), first.getLocation());
        match.setReturnLocation(second.getUniqueId(), second.getLocation());

        activeMatches.put(matchId, match);
        activeMatchIds.put(first.getUniqueId(), matchId);
        activeMatchIds.put(second.getUniqueId(), matchId);
        reservedArenaIds.add(arena.getId());
        transitioningPlayers.remove(first.getUniqueId());
        transitioningPlayers.remove(second.getUniqueId());

        if (arena.hasRollbackRegion()) {
            ArenaSnapshot snapshot = captureArenaSnapshot(arena);
            if (snapshot != null) {
                arenaSnapshots.put(matchId, snapshot);
            } else {
                plugin.getLogger().warning("Failed to capture rollback snapshot for duel arena " + arena.getId());
            }
        }

        preparePlayerForMatch(first, arena.getSpawn1(), arena);
        preparePlayerForMatch(second, arena.getSpawn2(), arena);

        send(first, "&aDuel found against &f" + second.getName() + "&a on arena &f" + arena.getDisplayName() + "&a.");
        send(second, "&aDuel found against &f" + first.getName() + "&a on arena &f" + arena.getDisplayName() + "&a.");
        play(first, "DUELS.MATCH-FOUND");
        play(second, "DUELS.MATCH-FOUND");
    }

    private void preparePlayerForMatch(Player player, Location teleportLocation, DuelArena arena) {
        player.closeInventory();
        restoreTransitionState(player);
        player.setGameMode(GameMode.SURVIVAL);
        healPlayer(player);
        applyArenaRules(player, arena);
        if (teleportLocation != null) {
            plugin.getPaperScheduler().teleport(player, teleportLocation);
        }
    }

    private void applyArenaRules(Player player, DuelArena arena) {
        if (player == null) {
            return;
        }

        if (arena == null) {
            player.resetPlayerTime();
            player.resetPlayerWeather();
            return;
        }

        if (arena.isNoHunger()) {
            player.setFoodLevel(20);
            player.setSaturation(20F);
            player.setExhaustion(0F);
        }

        if (arena.isAlwaysMorning()) {
            player.setPlayerTime(1000L, false);
        } else {
            player.resetPlayerTime();
        }

        if (arena.isNoWeather()) {
            player.setPlayerWeather(WeatherType.CLEAR);
        } else {
            player.resetPlayerWeather();
        }
    }

    private void syncArenaRulesForOccupants(DuelArena arena) {
        if (arena == null) {
            return;
        }

        Set<UUID> participantUuids = new HashSet<>();
        for (DuelMatch match : activeMatches.values()) {
            if (match == null || match.getArena() == null || !arena.getId().equalsIgnoreCase(match.getArena().getId())) {
                continue;
            }
            participantUuids.add(match.getPlayerOneUuid());
            participantUuids.add(match.getPlayerTwoUuid());
        }

        for (UUID uuid : participantUuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                applyArenaRules(player, arena);
            }
        }
    }

    private void syncArenaRulesForAllOccupants() {
        Set<String> syncedArenaIds = new HashSet<>();
        for (DuelArena arena : arenas.values()) {
            if (arena == null || !syncedArenaIds.add(arena.getId())) {
                continue;
            }
            syncArenaRulesForOccupants(arena);
        }
    }

    private boolean canEnterDuel(Player player, boolean selfFeedback) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (isInDuel(uuid)) {
            if (selfFeedback) {
                send(player, "&cYou are already in a duel.");
            }
            return false;
        }
        if (isInQueue(uuid)) {
            if (selfFeedback) {
                send(player, "&cYou are already in the queue.");
            }
            return false;
        }
        if (requestsByTarget.containsKey(uuid)) {
            if (selfFeedback) {
                send(player, "&cYou already have a pending duel request.");
            }
            return false;
        }
        if (plugin.getFfaManager() != null && plugin.getFfaManager().isBusy(uuid)) {
            if (selfFeedback) {
                send(player, "&cYou cannot use duels while inside the FFA system.");
            }
            return false;
        }
        return true;
    }

    private DuelArena findAvailableArena(String preferredArenaId, boolean queueArena) {
        if (preferredArenaId != null) {
            DuelArena arena = getArena(preferredArenaId);
            if (arena == null || !arena.isEnabled() || !arena.isReady()) {
                return null;
            }
            if (queueArena && !arena.isQueueEnabled()) {
                return null;
            }
            return reservedArenaIds.contains(arena.getId()) ? null : arena;
        }

        List<DuelArena> arenasToSearch = queueArena ? getReadyQueueArenas() : getReadyEnabledArenas();
        for (DuelArena arena : arenasToSearch) {
            if (!reservedArenaIds.contains(arena.getId())) {
                return arena;
            }
        }
        return null;
    }

    private void expireRequests() {
        long now = System.currentTimeMillis();
        List<UUID> expiredTargets = new ArrayList<>();
        for (Map.Entry<UUID, DuelRequest> entry : requestsByTarget.entrySet()) {
            if (entry.getValue().isExpired(now)) {
                expiredTargets.add(entry.getKey());
            }
        }

        for (UUID targetUuid : expiredTargets) {
            DuelRequest request = requestsByTarget.remove(targetUuid);
            if (request == null) {
                continue;
            }

            Player challenger = Bukkit.getPlayer(request.challengerUuid());
            Player target = Bukkit.getPlayer(targetUuid);
            if (challenger != null) {
                send(challenger, "&cYour duel request to &f" + request.targetName() + " &cexpired.");
            }
            if (target != null) {
                send(target, "&cYour duel request from &f" + request.challengerName() + " &cexpired.");
            }
        }
    }

    private void cleanupQueue() {
        List<UUID> toRemove = new ArrayList<>();
        for (UUID uuid : queue) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || isInDuel(uuid)) {
                toRemove.add(uuid);
            }
        }
        queue.removeAll(toRemove);
    }

    private boolean removeOutgoingRequest(UUID challengerUuid) {
        if (challengerUuid == null) {
            return false;
        }

        UUID matchKey = null;
        for (Map.Entry<UUID, DuelRequest> entry : requestsByTarget.entrySet()) {
            if (entry.getValue().challengerUuid().equals(challengerUuid)) {
                matchKey = entry.getKey();
                break;
            }
        }

        if (matchKey != null) {
            requestsByTarget.remove(matchKey);
            return true;
        }
        return false;
    }

    private void removeRequestsFor(UUID playerUuid, boolean notifyPlayers) {
        if (playerUuid == null) {
            return;
        }

        DuelRequest removedIncoming = requestsByTarget.remove(playerUuid);
        if (notifyPlayers && removedIncoming != null) {
            Player challenger = Bukkit.getPlayer(removedIncoming.challengerUuid());
            if (challenger != null) {
                send(challenger, "&cYour duel request was cleared.");
            }
        }

        UUID outgoingTarget = null;
        DuelRequest outgoing = null;
        for (Map.Entry<UUID, DuelRequest> entry : requestsByTarget.entrySet()) {
            if (entry.getValue().challengerUuid().equals(playerUuid)) {
                outgoingTarget = entry.getKey();
                outgoing = entry.getValue();
                break;
            }
        }

        if (outgoingTarget != null) {
            requestsByTarget.remove(outgoingTarget);
            if (notifyPlayers) {
                Player target = Bukkit.getPlayer(outgoingTarget);
                if (target != null && outgoing != null) {
                    send(target, "&cThat duel request was cancelled.");
                }
            }
        }
    }

    private DuelMatch getActiveMatch(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        Long matchId = activeMatchIds.get(uuid);
        return matchId == null ? null : activeMatches.get(matchId);
    }

    private Location resolveReturnLocation(DuelMatch match, UUID uuid) {
        Location location = match.getReturnLocation(uuid);
        if (location != null && location.getWorld() != null) {
            return location;
        }

        Location arenaReturn = match.getArena().getReturnLocation();
        if (arenaReturn != null && arenaReturn.getWorld() != null) {
            return arenaReturn;
        }

        return plugin.getSpawnManager().hasSpawn() ? plugin.getSpawnManager().getSpawnLocation() : null;
    }

    private Location resolveArenaJoinFallbackLocation(DuelArena arena, Location currentLocation) {
        if (arena != null) {
            Location arenaReturn = arena.getReturnLocation();
            if (arenaReturn != null && arenaReturn.getWorld() != null) {
                return arenaReturn;
            }
        }
        if (plugin.getSpawnManager().hasSpawn()) {
            Location spawn = plugin.getSpawnManager().getSpawnLocation();
            if (spawn != null && spawn.getWorld() != null) {
                return spawn;
            }
        }
        if (currentLocation != null && currentLocation.getWorld() != null) {
            Location worldSpawn = currentLocation.getWorld().getSpawnLocation();
            if (worldSpawn != null && worldSpawn.getWorld() != null) {
                return worldSpawn;
            }
        }
        if (!Bukkit.getWorlds().isEmpty()) {
            Location worldSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
            if (worldSpawn != null && worldSpawn.getWorld() != null) {
                return worldSpawn;
            }
        }
        return null;
    }

    private Location resolveArenaStayLocation(DuelMatch match, UUID uuid) {
        if (match == null || uuid == null) {
            return null;
        }

        if (uuid.equals(match.getPlayerOneUuid())) {
            return match.getArena().getSpawn1();
        }
        if (uuid.equals(match.getPlayerTwoUuid())) {
            return match.getArena().getSpawn2();
        }
        return match.getArena().getSpawn1();
    }

    private String resolveParticipantName(DuelMatch match, UUID uuid) {
        if (match == null || uuid == null) {
            return "Unknown";
        }
        if (uuid.equals(match.getPlayerOneUuid())) {
            return match.getPlayerOneName();
        }
        if (uuid.equals(match.getPlayerTwoUuid())) {
            return match.getPlayerTwoName();
        }
        return "Unknown";
    }

    private boolean canFullyFit(PlayerInventory inventory, ItemStack item) {
        if (inventory == null || item == null || item.getType().isAir() || item.getAmount() <= 0) {
            return false;
        }

        int remaining = item.getAmount();
        for (ItemStack existing : inventory.getStorageContents()) {
            if (existing == null || existing.getType().isAir()) {
                remaining -= item.getMaxStackSize();
            } else if (existing.isSimilar(item)) {
                remaining -= Math.max(0, existing.getMaxStackSize() - existing.getAmount());
            }

            if (remaining <= 0) {
                return true;
            }
        }

        return remaining <= 0;
    }

    private List<ClaimItemRow> loadClaimItemRows(UUID playerUuid, long matchId) {
        List<ClaimItemRow> rows = new ArrayList<>();
        if (playerUuid == null || matchId <= 0L || connection() == null) {
            return rows;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT id, defeated_name, item_data, created_at FROM duel_claims WHERE player_uuid = ? AND match_id = ? ORDER BY id ASC")) {
            ps.setString(1, playerUuid.toString());
            ps.setLong(2, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemStack item = deserializeItem(rs.getString("item_data"));
                    if (item == null) {
                        continue;
                    }
                    rows.add(new ClaimItemRow(
                            rs.getLong("id"),
                            rs.getString("defeated_name"),
                            item,
                            rs.getLong("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load duel claim rows for match " + matchId, e);
        }

        return rows;
    }

    private List<ItemStack> extractInventory(Player player) {
        List<ItemStack> loot = new ArrayList<>();
        if (player == null) {
            return loot;
        }

        PlayerInventory inventory = player.getInventory();
        collectItems(loot, inventory.getStorageContents());
        collectItems(loot, inventory.getArmorContents());
        collectItems(loot, new ItemStack[]{inventory.getItemInOffHand()});

        inventory.clear();
        inventory.setArmorContents(null);
        inventory.setItemInOffHand(null);
        player.updateInventory();
        return loot;
    }

    private List<ItemStack> copyLoot(List<ItemStack> drops) {
        List<ItemStack> loot = new ArrayList<>();
        if (drops == null) {
            return loot;
        }
        for (ItemStack item : drops) {
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }
            loot.add(item.clone());
        }
        return loot;
    }

    private void collectItems(List<ItemStack> destination, ItemStack[] items) {
        if (items == null) {
            return;
        }
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }
            destination.add(item.clone());
        }
    }

    public boolean shouldHandleAsCustomLethalPvP(Player attacker, Player victim, double finalDamage) {
        if (attacker == null || victim == null || finalDamage <= 0D) {
            return false;
        }
        if (!areOpponents(attacker.getUniqueId(), victim.getUniqueId())
                || !isMatchActive(attacker.getUniqueId())) {
            return false;
        }
        if (hasUsableTotem(victim)) {
            return false;
        }

        double effectiveHealth = victim.getHealth() + Math.max(0D, victim.getAbsorptionAmount());
        return finalDamage >= effectiveHealth;
    }

    private boolean hasUsableTotem(Player player) {
        if (player == null) {
            return false;
        }

        return player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
                || player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING;
    }

    private ArenaSnapshot captureArenaSnapshot(DuelArena arena) {
        if (arena == null || !arena.hasRollbackRegion()) {
            return null;
        }

        Location pos1 = arena.getRegionPos1();
        Location pos2 = arena.getRegionPos2();
        if (pos1 == null || pos2 == null || pos1.getWorld() == null || pos2.getWorld() == null) {
            return null;
        }

        World world = pos1.getWorld();
        if (!world.getName().equalsIgnoreCase(pos2.getWorld().getName())) {
            return null;
        }

        int horizontalPadding = getRollbackHorizontalPadding();
        int verticalPadding = getRollbackVerticalPadding();
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX()) - horizontalPadding;
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX()) + horizontalPadding;
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY()) - verticalPadding;
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY()) + verticalPadding;
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ()) - horizontalPadding;
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ()) + horizontalPadding;

        List<BlockSnapshot> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blocks.add(new BlockSnapshot(x, y, z, block.getBlockData().getAsString()));
                }
            }
        }

        return new ArenaSnapshot(world.getName(), minX, maxX, minY, maxY, minZ, maxZ, blocks);
    }

    private void rollbackArena(long matchId) {
        ArenaSnapshot snapshot = arenaSnapshots.remove(matchId);
        if (snapshot == null) {
            return;
        }

        World world = Bukkit.getWorld(snapshot.worldName());
        if (world == null) {
            return;
        }

        for (BlockSnapshot blockSnapshot : snapshot.blocks()) {
            Block block = world.getBlockAt(blockSnapshot.x(), blockSnapshot.y(), blockSnapshot.z());
            try {
                BlockData data = Bukkit.createBlockData(blockSnapshot.blockDataString());
                block.setBlockData(data, false);
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to restore duel arena block at "
                                + blockSnapshot.x() + "," + blockSnapshot.y() + "," + blockSnapshot.z(),
                        exception);
            }
        }

        cleanupTransientEntities(snapshot, world);
    }

    private void cleanupTransientEntities(ArenaSnapshot snapshot, World world) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player) {
                continue;
            }
            Location location = entity.getLocation();
            if (!snapshot.contains(location)) {
                continue;
            }

            String typeName = entity.getType().name();
            if (typeName.equals("ITEM")
                    || typeName.equals("EXPERIENCE_ORB")
                    || typeName.equals("ARROW")
                    || typeName.equals("SPECTRAL_ARROW")
                    || typeName.equals("TRIDENT")
                    || typeName.equals("EGG")
                    || typeName.equals("ENDER_PEARL")
                    || typeName.equals("SNOWBALL")
                    || typeName.equals("POTION")
                    || typeName.equals("SPLASH_POTION")
                    || typeName.equals("THROWN_POTION")
                    || typeName.equals("LINGERING_POTION")
                    || typeName.equals("AREA_EFFECT_CLOUD")
                    || typeName.equals("FALLING_BLOCK")
                    || typeName.equals("PRIMED_TNT")
                    || typeName.equals("TNT")) {
                entity.remove();
            }
        }
    }

    private void prepareTransition(UUID uuid) {
        if (uuid == null) {
            return;
        }

        transitioningPlayers.add(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline() && !player.isDead()) {
            applyTransitionState(player);
        }
    }

    private void applyTransitionState(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        transitioningPlayers.add(player.getUniqueId());
        transitionStates.putIfAbsent(player.getUniqueId(), new TransitionPlayerState(
                player.getGameMode(),
                player.getAllowFlight(),
                player.isFlying(),
                player.isInvulnerable(),
                player.isCollidable()
        ));
        applyTemporaryVanish(player);
        if (player.getGameMode() != GameMode.ADVENTURE) {
            player.setGameMode(GameMode.ADVENTURE);
        }
        healPlayer(player);
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        showStoredTransitionTitle(player);
    }

    private void restoreTransitionState(Player player) {
        if (player == null) {
            return;
        }

        TransitionPlayerState state = transitionStates.remove(player.getUniqueId());
        if (state == null) {
            state = new TransitionPlayerState(GameMode.SURVIVAL, false, false, false, true);
        }
        if (player.getGameMode() != state.gameMode()) {
            player.setGameMode(state.gameMode());
        }
        player.setAllowFlight(state.allowFlight());
        player.setFlying(state.allowFlight() && state.flying());
        player.setInvulnerable(state.invulnerable());
        player.setCollidable(state.collidable());
        player.resetPlayerTime();
        player.resetPlayerWeather();
        transitionTitles.remove(player.getUniqueId());
        player.clearTitle();
        clearTemporaryVanish(player);
        transitioningPlayers.remove(player.getUniqueId());
    }

    private void applyTemporaryVanish(Player hidden) {
        if (hidden == null) {
            return;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(hidden.getUniqueId())) {
                continue;
            }
            viewer.hidePlayer(plugin, hidden);
        }
    }

    private void clearTemporaryVanish(Player shown) {
        if (shown == null) {
            return;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(shown.getUniqueId())) {
                continue;
            }
            viewer.showPlayer(plugin, shown);
        }
    }

    private void showAllVanishedPlayers() {
        for (Player shown : Bukkit.getOnlinePlayers()) {
            clearTemporaryVanish(shown);
        }
    }

    private String formatDuration(long totalSeconds) {
        long safeSeconds = Math.max(0L, totalSeconds);
        long minutes = safeSeconds / 60L;
        long seconds = safeSeconds % 60L;
        return minutes + "m " + seconds + "s";
    }

    private void ensureTables() {
        Connection connection = connection();
        if (connection == null) {
            return;
        }

        try (Statement st = connection.createStatement()) {
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS duel_arenas (
                      id TEXT PRIMARY KEY,
                      display_name TEXT NOT NULL,
                      spawn1_data TEXT,
                      spawn2_data TEXT,
                      return_data TEXT,
                      region_pos1_data TEXT,
                      region_pos2_data TEXT,
                      enabled INTEGER DEFAULT 1,
                      queue_enabled INTEGER DEFAULT 1
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS duel_stats (
                      player_uuid TEXT PRIMARY KEY,
                      wins INTEGER DEFAULT 0,
                      losses INTEGER DEFAULT 0,
                      draws INTEGER DEFAULT 0,
                      current_streak INTEGER DEFAULT 0,
                      best_streak INTEGER DEFAULT 0
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS duel_matches (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      match_type TEXT NOT NULL,
                      arena_id TEXT NOT NULL,
                      player_one_uuid TEXT NOT NULL,
                      player_two_uuid TEXT NOT NULL,
                      winner_uuid TEXT,
                      loser_uuid TEXT,
                      status TEXT NOT NULL,
                      end_reason TEXT,
                      started_at INTEGER DEFAULT 0,
                      ended_at INTEGER DEFAULT 0,
                      duration_seconds INTEGER DEFAULT 0
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS duel_claims (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      player_uuid TEXT NOT NULL,
                      match_id INTEGER NOT NULL,
                      defeated_name TEXT DEFAULT '',
                      item_data TEXT NOT NULL,
                      created_at INTEGER NOT NULL
                    )
                    """);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize duel tables", e);
        }

        ensureArenaColumn("region_pos1_data", "TEXT");
        ensureArenaColumn("region_pos2_data", "TEXT");
        ensureClaimColumn("defeated_name", "TEXT DEFAULT ''");
    }

    private void ensureArenaColumn(String columnName, String definition) {
        if (connection() == null) {
            return;
        }

        try {
            if (plugin.getDatabaseManager().hasColumn("duel_arenas", columnName)) {
                return;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to inspect duel_arenas schema", e);
            return;
        }

        try (Statement st = connection().createStatement()) {
            plugin.getDatabaseManager().executeSchema(st, "ALTER TABLE duel_arenas ADD COLUMN " + columnName + " " + definition);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add duel_arenas column " + columnName, e);
        }
    }

    private void ensureClaimColumn(String columnName, String definition) {
        if (connection() == null) {
            return;
        }

        try {
            if (plugin.getDatabaseManager().hasColumn("duel_claims", columnName)) {
                return;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to inspect duel_claims schema", e);
            return;
        }

        try (Statement st = connection().createStatement()) {
            plugin.getDatabaseManager().executeSchema(st, "ALTER TABLE duel_claims ADD COLUMN " + columnName + " " + definition);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add duel_claims column " + columnName, e);
        }
    }

    private int getMaxNumericKey(ConfigurationSection section) {
        if (section == null) {
            return -1;
        }

        int max = -1;
        for (String key : section.getKeys(false)) {
            try {
                max = Math.max(max, Integer.parseInt(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return max;
    }

    private void loadArenas() {
        Map<String, DuelArena> previousArenas = new HashMap<>(arenas);
        arenas.clear();
        if (connection() == null) {
            return;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT id, display_name, spawn1_data, spawn2_data, return_data, region_pos1_data, region_pos2_data, enabled, queue_enabled FROM duel_arenas");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String arenaId = rs.getString("id");
                DuelArena loadedArena = new DuelArena(
                        arenaId,
                        rs.getString("display_name"),
                        LocationUtils.parse(rs.getString("spawn1_data")),
                        LocationUtils.parse(rs.getString("spawn2_data")),
                        LocationUtils.parse(rs.getString("return_data")),
                        LocationUtils.parse(rs.getString("region_pos1_data")),
                        LocationUtils.parse(rs.getString("region_pos2_data")),
                        rs.getInt("enabled") == 1,
                        rs.getInt("queue_enabled") == 1,
                        false,
                        false,
                        false,
                        false
                );
                DuelArena arena = previousArenas.get(arenaId);
                if (arena != null) {
                    updateLoadedArenaState(arena, loadedArena);
                } else {
                    arena = loadedArena;
                }
                arenas.put(arena.getId(), arena);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load duel arenas", e);
        }

        synchronizeArenaSettingsConfig();
    }

    private void updateLoadedArenaState(DuelArena target, DuelArena loaded) {
        if (target == null || loaded == null) {
            return;
        }

        target.setDisplayName(loaded.getDisplayName());
        target.setSpawn1(loaded.getSpawn1());
        target.setSpawn2(loaded.getSpawn2());
        target.setReturnLocation(loaded.getReturnLocation());
        target.setRegionPos1(loaded.getRegionPos1());
        target.setRegionPos2(loaded.getRegionPos2());
        target.setEnabled(loaded.isEnabled());
        target.setQueueEnabled(loaded.isQueueEnabled());
        target.setNoHunger(loaded.isNoHunger());
        target.setNoWeather(loaded.isNoWeather());
        target.setAlwaysMorning(loaded.isAlwaysMorning());
        target.setNoFallDamage(loaded.isNoFallDamage());
    }

    private void saveArena(DuelArena arena) {
        if (arena == null || connection() == null) {
            return;
        }

        StoredArenaLocationData existingData = loadStoredArenaLocationData(arena.getId());
        String spawn1Data = preserveExistingLocationData(LocationUtils.serialize(arena.getSpawn1()),
                existingData == null ? null : existingData.spawn1Data());
        String spawn2Data = preserveExistingLocationData(LocationUtils.serialize(arena.getSpawn2()),
                existingData == null ? null : existingData.spawn2Data());
        String returnData = preserveExistingLocationData(LocationUtils.serialize(arena.getReturnLocation()),
                existingData == null ? null : existingData.returnData());
        String regionPos1Data = preserveExistingLocationData(LocationUtils.serialize(arena.getRegionPos1()),
                existingData == null ? null : existingData.regionPos1Data());
        String regionPos2Data = preserveExistingLocationData(LocationUtils.serialize(arena.getRegionPos2()),
                existingData == null ? null : existingData.regionPos2Data());

        try (PreparedStatement ps = connection().prepareStatement(
                "REPLACE INTO duel_arenas (id, display_name, spawn1_data, spawn2_data, return_data, region_pos1_data, region_pos2_data, enabled, queue_enabled) " +
                        "VALUES (?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, arena.getId());
            ps.setString(2, arena.getDisplayName());
            ps.setString(3, spawn1Data);
            ps.setString(4, spawn2Data);
            ps.setString(5, returnData);
            ps.setString(6, regionPos1Data);
            ps.setString(7, regionPos2Data);
            ps.setInt(8, arena.isEnabled() ? 1 : 0);
            ps.setInt(9, arena.isQueueEnabled() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save duel arena " + arena.getId(), e);
        }
    }

    private StoredArenaLocationData loadStoredArenaLocationData(String arenaId) {
        if (arenaId == null || arenaId.isBlank() || connection() == null) {
            return null;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT spawn1_data, spawn2_data, return_data, region_pos1_data, region_pos2_data FROM duel_arenas WHERE id = ?")) {
            ps.setString(1, arenaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new StoredArenaLocationData(
                        rs.getString("spawn1_data"),
                        rs.getString("spawn2_data"),
                        rs.getString("return_data"),
                        rs.getString("region_pos1_data"),
                        rs.getString("region_pos2_data")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to inspect existing duel arena data for " + arenaId, e);
            return null;
        }
    }

    private String preserveExistingLocationData(String currentSerialized, String existingSerialized) {
        if (currentSerialized != null && !currentSerialized.isBlank()) {
            return currentSerialized;
        }
        return existingSerialized == null ? "" : existingSerialized;
    }

    private DuelStats loadStats(UUID uuid) {
        if (uuid == null || connection() == null) {
            return DuelStats.empty();
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT wins, losses, draws, current_streak, best_streak FROM duel_stats WHERE player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DuelStats(
                            rs.getInt("wins"),
                            rs.getInt("losses"),
                            rs.getInt("draws"),
                            rs.getInt("current_streak"),
                            rs.getInt("best_streak")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load duel stats for " + uuid, e);
        }

        return DuelStats.empty();
    }

    private void saveStats(UUID uuid, DuelStats stats) {
        if (uuid == null || stats == null || connection() == null) {
            return;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "REPLACE INTO duel_stats (player_uuid, wins, losses, draws, current_streak, best_streak) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, stats.getWins());
            ps.setInt(3, stats.getLosses());
            ps.setInt(4, stats.getDraws());
            ps.setInt(5, stats.getCurrentStreak());
            ps.setInt(6, stats.getBestStreak());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save duel stats for " + uuid, e);
        }
    }

    private long insertMatch(DuelMatch.MatchType type, DuelArena arena, UUID firstUuid, UUID secondUuid) {
        if (connection() == null) {
            return -1L;
        }

        try (PreparedStatement ps = connection().prepareStatement("""
                INSERT INTO duel_matches (match_type, arena_id, player_one_uuid, player_two_uuid, status, started_at)
                VALUES (?,?,?,?,?,?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type.name());
            ps.setString(2, arena.getId());
            ps.setString(3, firstUuid.toString());
            ps.setString(4, secondUuid.toString());
            ps.setString(5, DuelMatch.MatchStatus.COUNTDOWN.name());
            ps.setLong(6, 0L);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to insert duel match record", e);
        }
        return -1L;
    }

    private void updateMatchRecord(DuelMatch match, UUID winnerUuid, UUID loserUuid, String endReason) {
        if (match == null || connection() == null) {
            return;
        }

        long endedAt = System.currentTimeMillis();
        long durationSeconds = match.getStartedAt() <= 0L ? 0L : Math.max(0L, (endedAt - match.getStartedAt()) / 1000L);

        try (PreparedStatement ps = connection().prepareStatement("""
                UPDATE duel_matches
                SET winner_uuid = ?, loser_uuid = ?, status = ?, end_reason = ?, started_at = ?, ended_at = ?, duration_seconds = ?
                WHERE id = ?
                """)) {
            if (winnerUuid == null) {
                ps.setNull(1, java.sql.Types.VARCHAR);
            } else {
                ps.setString(1, winnerUuid.toString());
            }
            if (loserUuid == null) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, loserUuid.toString());
            }
            ps.setString(3, DuelMatch.MatchStatus.FINISHED.name());
            ps.setString(4, endReason);
            ps.setLong(5, match.getStartedAt());
            ps.setLong(6, endedAt);
            ps.setLong(7, durationSeconds);
            ps.setLong(8, match.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update duel match record " + match.getId(), e);
        }
    }

    private String serializeItem(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    private ItemStack deserializeItem(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }

        try {
            return ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to deserialize duel item", e);
            return null;
        }
    }

    private String normalizeArenaId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return id.trim().toLowerCase(Locale.ROOT);
    }

    private String prettifyId(String id) {
        if (id == null || id.isBlank()) {
            return "Arena";
        }
        String[] parts = id.replace('-', ' ').replace('_', ' ').split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return builder.isEmpty() ? "Arena" : builder.toString();
    }

    private String buildQueueUnavailableMessage() {
        List<String> queueEnabledNotReady = new ArrayList<>();
        for (DuelArena arena : getArenas()) {
            if (arena.isEnabled() && arena.isQueueEnabled() && !arena.isReady()) {
                queueEnabledNotReady.add(arena.getId());
            }
        }

        if (!queueEnabledNotReady.isEmpty()) {
            return "&cQueue arenas exist but are not ready yet. "
                    + "&7Use &f/arena setpos1 <id> &7and &f/arena setpos2 <id> "
                    + "&7for: &f" + String.join("&7, &f", queueEnabledNotReady) + "&7.";
        }

        return "&cNo ready queue arenas are configured yet. "
                + "&7Enable queue with &f/arena queue <id> true&7, then set &fpos1 &7and &fpos2&7.";
    }

    private void synchronizeArenaSettingsConfig() {
        FileConfiguration duelConfig = config();
        if (duelConfig == null) {
            return;
        }

        boolean changed = false;
        for (DuelArena arena : arenas.values()) {
            if (arena == null) {
                continue;
            }
            changed |= ensureArenaSettingsEntry(duelConfig, arena);
            applyConfiguredArenaSettings(arena, duelConfig);
        }

        if (changed) {
            plugin.getConfigManager().saveDuels();
        }
    }

    private boolean ensureArenaSettingsEntry(FileConfiguration duelConfig, DuelArena arena) {
        if (duelConfig == null || arena == null) {
            return false;
        }

        boolean changed = false;
        for (ArenaSetting setting : ArenaSetting.values()) {
            String path = getArenaSettingPath(arena.getId(), setting);
            if (!duelConfig.contains(path)) {
                duelConfig.set(path, false);
                changed = true;
            }
        }
        return changed;
    }

    private void applyConfiguredArenaSettings(DuelArena arena, FileConfiguration duelConfig) {
        if (arena == null || duelConfig == null) {
            return;
        }

        arena.setNoHunger(duelConfig.getBoolean(getArenaSettingPath(arena.getId(), ArenaSetting.NO_HUNGER), false));
        arena.setNoWeather(duelConfig.getBoolean(getArenaSettingPath(arena.getId(), ArenaSetting.NO_WEATHER), false));
        arena.setAlwaysMorning(duelConfig.getBoolean(getArenaSettingPath(arena.getId(), ArenaSetting.ALWAYS_MORNING), false));
        arena.setNoFallDamage(duelConfig.getBoolean(getArenaSettingPath(arena.getId(), ArenaSetting.NO_FALL_DAMAGE), false));
    }

    private String getArenaSettingPath(String arenaId, ArenaSetting setting) {
        return "ARENA_SETTINGS." + normalizeArenaId(arenaId) + "." + switch (setting) {
            case NO_HUNGER -> "NO_HUNGER";
            case NO_WEATHER -> "NO_WEATHER";
            case ALWAYS_MORNING -> "ALWAYS_MORNING";
            case NO_FALL_DAMAGE -> "NO_FALL_DAMAGE";
        };
    }

    private ArenaRegionBounds resolveArenaRegionBounds(DuelArena arena) {
        if (arena == null) {
            return null;
        }

        Location pos1 = arena.getRegionPos1();
        Location pos2 = arena.getRegionPos2();
        if ((pos1 == null || pos1.getWorld() == null || pos2 == null || pos2.getWorld() == null) && arena.isReady()) {
            pos1 = arena.getSpawn1();
            pos2 = arena.getSpawn2();
        }
        if (pos1 == null || pos1.getWorld() == null || pos2 == null || pos2.getWorld() == null) {
            return null;
        }

        World world = pos1.getWorld();
        if (!world.getName().equalsIgnoreCase(pos2.getWorld().getName())) {
            return null;
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX()) - getRollbackHorizontalPadding();
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX()) + getRollbackHorizontalPadding();
        int minY = Math.max(world.getMinHeight(), Math.min(pos1.getBlockY(), pos2.getBlockY()) - getRollbackVerticalPadding());
        int maxY = Math.min(world.getMaxHeight() - 1, Math.max(pos1.getBlockY(), pos2.getBlockY()) + getRollbackVerticalPadding());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ()) - getRollbackHorizontalPadding();
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ()) + getRollbackHorizontalPadding();
        if (maxY < minY) {
            return null;
        }

        return new ArenaRegionBounds(world, minX, maxX, minY, maxY, minZ, maxZ);
    }

    private boolean isWithinArenaBounds(Location location, ArenaRegionBounds bounds) {
        if (location == null || bounds == null || location.getWorld() == null) {
            return false;
        }

        return bounds.world().getName().equalsIgnoreCase(location.getWorld().getName())
                && location.getBlockX() >= bounds.minX()
                && location.getBlockX() <= bounds.maxX()
                && location.getBlockY() >= bounds.minY()
                && location.getBlockY() <= bounds.maxY()
                && location.getBlockZ() >= bounds.minZ()
                && location.getBlockZ() <= bounds.maxZ();
    }

    private DuelArena findArenaContainingLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        for (DuelArena arena : getArenas()) {
            ArenaRegionBounds bounds = resolveArenaRegionBounds(arena);
            if (bounds != null && isWithinArenaBounds(location, bounds)) {
                return arena;
            }
        }
        return null;
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getDuels();
    }

    private Connection connection() {
        return plugin.getDatabaseManager().getConnection();
    }

    private int normalizeSize(int size) {
        int normalized = Math.max(9, size);
        normalized = ((normalized + 8) / 9) * 9;
        return Math.min(54, normalized);
    }

    private void send(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isBlank()) {
            sender.sendMessage(ColorUtils.toComponent(message));
        }
    }

    private void play(Player player, String path) {
        if (player == null) {
            return;
        }
        SoundUtils.play(player, plugin.getConfigManager().getSound(path));
    }

    private record PendingRespawnState(Location respawnLocation, Location returnLocation, long delayTicks) {
    }

    private static final class ClaimAccumulator {
        private String defeatedName;
        private long createdAt;
        private final List<ItemStack> items = new ArrayList<>();

        private ClaimAccumulator(String defeatedName, long createdAt) {
            this.defeatedName = defeatedName;
            this.createdAt = createdAt;
        }

        private void updateMetadata(String defeatedName, long createdAt) {
            if ((this.defeatedName == null || this.defeatedName.isBlank())
                    && defeatedName != null && !defeatedName.isBlank()) {
                this.defeatedName = defeatedName;
            }
            this.createdAt = Math.min(this.createdAt, createdAt);
        }

        private String defeatedName() {
            return defeatedName == null || defeatedName.isBlank() ? "Unknown" : defeatedName;
        }

        private long createdAt() {
            return createdAt;
        }

        private List<ItemStack> items() {
            return items;
        }
    }

    private record ClaimItemRow(long id, String defeatedName, ItemStack item, long createdAt) {
    }

    private record TransitionPlayerState(
            GameMode gameMode,
            boolean allowFlight,
            boolean flying,
            boolean invulnerable,
            boolean collidable
    ) {
    }

    private record TransitionTitleState(String title, String subtitle) {
    }

    private record StoredArenaLocationData(
            String spawn1Data,
            String spawn2Data,
            String returnData,
            String regionPos1Data,
            String regionPos2Data
    ) {
    }

    private record BlockSnapshot(int x, int y, int z, String blockDataString) {
    }

    private record ArenaSnapshot(
            String worldName,
            int minX,
            int maxX,
            int minY,
            int maxY,
            int minZ,
            int maxZ,
            List<BlockSnapshot> blocks
    ) {
        private boolean contains(Location location) {
            if (location == null || location.getWorld() == null) {
                return false;
            }
            if (!worldName.equalsIgnoreCase(location.getWorld().getName())) {
                return false;
            }
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            return x >= minX && x <= maxX
                    && y >= minY && y <= maxY
                    && z >= minZ && z <= maxZ;
        }
    }

    private record ArenaRegionBounds(
            World world,
            int minX,
            int maxX,
            int minY,
            int maxY,
            int minZ,
            int maxZ
    ) {
    }
}
