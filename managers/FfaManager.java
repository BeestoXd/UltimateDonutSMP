package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.FfaArena;
import com.bx.ultimateDonutSmp.models.FfaMatch;
import com.bx.ultimateDonutSmp.models.FfaPlayerSnapshot;
import com.bx.ultimateDonutSmp.models.FfaStats;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.LocationUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import net.kyori.adventure.title.Title;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class FfaManager {

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

    private static final int MIN_SINGLE_POINT_ROLLBACK_HORIZONTAL_PADDING = 48;
    private static final int MIN_SINGLE_POINT_ROLLBACK_VERTICAL_PADDING = 20;
    private static final int MAX_SNAPSHOT_REPAIR_PASSES = 16;
    private static final int MAX_SNAPSHOT_REPAIR_RADIUS = 6;
    private static final double DEFAULT_ARENA_SPAWN_DISTANCE_SQUARED = 36.0D;
    private static final double JOIN_PAIR_SPAWN_DISTANCE_SQUARED = 4.0D;

    private final UltimateDonutSmp plugin;
    private final Map<String, FfaArena> arenas = new HashMap<>();
    private final LinkedHashSet<UUID> waitingPlayers = new LinkedHashSet<>();
    private final Map<UUID, WaitingArenaEntry> waitingArenaEntries = new HashMap<>();
    private final Map<Long, FfaMatch> activeMatches = new HashMap<>();
    private final Map<UUID, Long> activeMatchIds = new HashMap<>();
    private final Set<String> reservedArenaIds = new HashSet<>();
    private final Map<String, ArenaSnapshot> arenaBaselineSnapshots = new HashMap<>();
    private final Map<Long, ArenaSnapshot> arenaSnapshots = new HashMap<>();
    private final Map<UUID, FfaStats> statsCache = new HashMap<>();
    private final Map<UUID, FfaPlayerSnapshot> pendingJoinSnapshots = new HashMap<>();
    private final Map<UUID, Location> pendingJoinLocations = new HashMap<>();
    private final Map<String, Integer> nextRegionCorners = new HashMap<>();
    private final Map<Long, FfaMatch> pendingResetMatches = new HashMap<>();
    private final Map<Long, Long> pendingResetEarliestAt = new HashMap<>();
    private final Set<UUID> combatLockedPlayers = new HashSet<>();
    private final Set<UUID> transitioningPlayers = new HashSet<>();
    private final Map<UUID, TransitionPlayerState> transitionStates = new HashMap<>();
    private final Map<UUID, TransitionTitleState> transitionTitles = new HashMap<>();
    private final Map<UUID, PendingRespawnState> pendingRespawns = new HashMap<>();
    private long tickCounter = 0L;
    private int autoRepairArenaCursor = 0;

    public FfaManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        ensureTables();
        reload();
    }

    public void reload() {
        loadArenas();
        syncArenaRulesForAllOccupants();
    }

    public void refreshArenaAvailability() {
        loadArenas();
    }

    public void shutdown() {
        for (FfaMatch match : new ArrayList<>(activeMatches.values())) {
            restoreParticipant(match, match.getPlayerOneUuid());
            restoreParticipant(match, match.getPlayerTwoUuid());
        }

        for (UUID uuid : new ArrayList<>(waitingPlayers)) {
            cancelWaitingEntry(uuid, null, true);
        }

        waitingPlayers.clear();
        waitingArenaEntries.clear();
        activeMatches.clear();
        activeMatchIds.clear();
        reservedArenaIds.clear();
        arenaBaselineSnapshots.clear();
        arenaSnapshots.clear();
        pendingJoinSnapshots.clear();
        pendingJoinLocations.clear();
        nextRegionCorners.clear();
        pendingResetMatches.clear();
        pendingResetEarliestAt.clear();
        combatLockedPlayers.clear();
        transitionTitles.clear();
        pendingRespawns.clear();
        showAllVanishedPlayers();

        for (UUID uuid : new ArrayList<>(transitionStates.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                restoreTransitionState(player);
            }
        }
        transitionStates.clear();
        transitioningPlayers.clear();
    }

    public boolean isEnabled() {
        return config().getBoolean("SETTINGS.ENABLED", true);
    }

    public boolean shouldBlockCommands() {
        return config().getBoolean("RULES.BLOCK_COMMANDS", true);
    }

    public boolean shouldCountTowardGlobalStats() {
        return config().getBoolean("RULES.COUNT_TOWARD_GLOBAL_STATS", false);
    }

    public int getCountdownSeconds() {
        return 0;
    }

    public int getReturnDelayTicks() {
        return Math.max(0, config().getInt("SETTINGS.RETURN_DELAY_SECONDS", 3)) * 20;
    }

    public int getRollbackHorizontalPadding() {
        return Math.max(0, config().getInt("ROLLBACK.PADDING_HORIZONTAL", 8));
    }

    public int getRollbackVerticalPadding() {
        return Math.max(0, config().getInt("ROLLBACK.PADDING_VERTICAL", 6));
    }

    public boolean shouldRollbackArena() {
        return config().getBoolean("ROLLBACK.ENABLED", true);
    }

    public boolean isInQueue(UUID uuid) {
        return uuid != null && waitingPlayers.contains(uuid);
    }

    public boolean isInMatch(UUID uuid) {
        return uuid != null && activeMatchIds.containsKey(uuid);
    }

    public boolean isInCountdown(UUID uuid) {
        return false;
    }

    public boolean isMatchActive(UUID uuid) {
        FfaMatch match = getActiveMatch(uuid);
        return match != null && match.getStatus() == FfaMatch.MatchStatus.ACTIVE;
    }

    public boolean isTransitioning(UUID uuid) {
        return uuid != null && transitioningPlayers.contains(uuid);
    }

    public boolean isCombatLocked(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        if (isInMatch(uuid) || isTransitioning(uuid)) {
            return true;
        }
        if (!combatLockedPlayers.contains(uuid)) {
            return false;
        }
        if (isCombatTagged(uuid)) {
            return true;
        }

        combatLockedPlayers.remove(uuid);
        return false;
    }

    public boolean isBusy(UUID uuid) {
        return isInQueue(uuid) || isCombatLocked(uuid);
    }

    public boolean isInSession(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return waitingPlayers.contains(uuid)
                || waitingArenaEntries.containsKey(uuid)
                || activeMatchIds.containsKey(uuid)
                || transitioningPlayers.contains(uuid)
                || transitionStates.containsKey(uuid);
    }

    public boolean shouldSuppressGlobalStats(UUID uuid) {
        return !shouldCountTowardGlobalStats() && (isInQueue(uuid) || isInMatch(uuid) || isTransitioning(uuid));
    }

    public boolean areOpponents(UUID first, UUID second) {
        FfaMatch match = getActiveMatch(first);
        return match != null
                && match.getStatus() != FfaMatch.MatchStatus.FINISHED
                && match.isParticipant(second);
    }

    public boolean isActiveOpponentPair(UUID first, UUID second) {
        if (first == null || second == null) {
            return false;
        }

        Long firstMatchId = activeMatchIds.get(first);
        Long secondMatchId = activeMatchIds.get(second);
        if (firstMatchId == null || !firstMatchId.equals(secondMatchId)) {
            return false;
        }

        FfaMatch match = activeMatches.get(firstMatchId);
        return match != null
                && match.getStatus() == FfaMatch.MatchStatus.ACTIVE
                && match.isParticipant(first)
                && match.isParticipant(second);
    }

    public boolean shouldBypassGlobalCombat(Player attacker, Player victim) {
        if (victim != null && (isInQueue(victim.getUniqueId())
                || isInMatch(victim.getUniqueId())
                || isTransitioning(victim.getUniqueId()))) {
            return true;
        }
        return attacker != null && (isInQueue(attacker.getUniqueId())
                || isInMatch(attacker.getUniqueId())
                || isTransitioning(attacker.getUniqueId()));
    }

    public boolean canModifyArena(Player player) {
        if (player == null) {
            return false;
        }

        FfaMatch match = getActiveMatch(player.getUniqueId());
        return match != null
                && match.getStatus() == FfaMatch.MatchStatus.ACTIVE
                && match.getArena().hasRollbackRegion()
                && arenaSnapshots.containsKey(match.getId());
    }

    public FfaStats getStats(UUID uuid) {
        if (uuid == null) {
            return FfaStats.empty();
        }
        return statsCache.computeIfAbsent(uuid, this::loadStats);
    }

    public List<FfaArena> getArenas() {
        List<FfaArena> values = new ArrayList<>(arenas.values());
        values.sort(Comparator.comparing(FfaArena::getId, String.CASE_INSENSITIVE_ORDER));
        return values;
    }

    public FfaArena getArena(String id) {
        if (id == null) {
            return null;
        }
        return arenas.get(normalizeArenaId(id));
    }

    public FfaArena getSessionArena(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        WaitingArenaEntry waitingEntry = waitingArenaEntries.get(uuid);
        if (waitingEntry != null) {
            return waitingEntry.arena();
        }

        FfaMatch match = getActiveMatch(uuid);
        return match == null ? null : match.getArena();
    }

    public boolean hasArenaSetting(UUID uuid, ArenaSetting setting) {
        if (setting == null) {
            return false;
        }

        FfaArena arena = getSessionArena(uuid);
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

    public List<FfaArena> getReadyQueueArenas() {
        List<FfaArena> result = new ArrayList<>();
        for (FfaArena arena : getArenas()) {
            if (arena.isReady()) {
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

        FfaArena arena = new FfaArena(
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
        nextRegionCorners.put(normalized, 1);
        saveArena(arena);
        synchronizeArenaSettingsConfig();
        return true;
    }

    public boolean deleteArena(String id) {
        FfaArena arena = getArena(id);
        if (arena == null || reservedArenaIds.contains(arena.getId())) {
            return false;
        }

        arenas.remove(arena.getId());
        nextRegionCorners.remove(arena.getId());
        invalidateArenaBaseline(arena.getId());
        if (connection() == null) {
            return true;
        }

        try (PreparedStatement ps = connection().prepareStatement("DELETE FROM ffa_arenas WHERE id = ?")) {
            ps.setString(1, arena.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete FFA arena " + arena.getId(), e);
            return false;
        }
    }

    public boolean setArenaSpawn(String id, int spawnIndex, Location location) {
        FfaArena arena = getArena(id);
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
        FfaArena arena = getArena(id);
        if (arena == null || location == null || location.getWorld() == null) {
            return false;
        }

        arena.setReturnLocation(location);
        saveArena(arena);
        return true;
    }

    public boolean setArenaRegionPos(String id, Location location) {
        FfaArena arena = getArena(id);
        if (arena == null || location == null || location.getWorld() == null) {
            return false;
        }

        Location exactLocation = cloneArenaRegionLocation(location);
        arena.setRegionPos1(exactLocation);
        arena.setRegionPos2(exactLocation);
        nextRegionCorners.put(arena.getId(), 1);
        invalidateArenaBaseline(arena.getId());
        saveArena(arena);
        return true;
    }

    public boolean setArenaRegionPos(String id, int posIndex, Location location) {
        FfaArena arena = getArena(id);
        if (arena == null || location == null || location.getWorld() == null) {
            return false;
        }

        Location exactLocation = cloneArenaRegionLocation(location);
        if (posIndex == 1) {
            arena.setRegionPos1(exactLocation);
        } else if (posIndex == 2) {
            arena.setRegionPos2(exactLocation);
        } else {
            return false;
        }

        invalidateArenaBaseline(arena.getId());
        saveArena(arena);
        nextRegionCorners.put(arena.getId(), posIndex == 1 ? 2 : 1);
        return true;
    }

    private Location cloneArenaRegionLocation(Location location) {
        return location == null ? null : location.clone();
    }

    public boolean setArenaDisplayName(String id, String displayName) {
        FfaArena arena = getArena(id);
        if (arena == null || displayName == null || displayName.isBlank()) {
            return false;
        }

        arena.setDisplayName(displayName.trim());
        saveArena(arena);
        return true;
    }

    public boolean setArenaSetting(String id, ArenaSetting setting, boolean enabled) {
        FfaArena arena = getArena(id);
        FileConfiguration ffaConfig = config();
        if (arena == null || setting == null || ffaConfig == null) {
            return false;
        }

        ensureArenaSettingsEntry(ffaConfig, arena);
        ffaConfig.set(getArenaSettingPath(arena.getId(), setting), enabled);
        boolean saved = plugin.getConfigManager().saveFfa();
        applyConfiguredArenaSettings(arena, ffaConfig);
        syncArenaRulesForOccupants(arena);
        return saved;
    }

    public boolean setArenaEnabled(String id, boolean enabled) {
        FfaArena arena = getArena(id);
        if (arena == null) {
            return false;
        }

        arena.setEnabled(enabled);
        if (enabled && arena.getState() == FfaArena.ArenaState.DISABLED) {
            arena.setState(FfaArena.ArenaState.READY);
        }
        saveArena(arena);
        return true;
    }

    public boolean joinArena(Player player) {
        if (!isEnabled()) {
            send(player, "&cFFA is currently disabled.");
            return false;
        }
        if (!canEnterFfa(player, true)) {
            return false;
        }

        FfaArena arena = findJoinableArena();
        if (arena == null) {
            send(player, buildQueueUnavailableMessage());
            return false;
        }

        return placePlayerIntoWaitingArena(player, arena);
    }

    public boolean joinQueue(Player player) {
        return joinArena(player);
    }

    public boolean leaveState(Player player) {
        if (player == null) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (waitingPlayers.contains(uuid) || waitingArenaEntries.containsKey(uuid)) {
            cancelWaitingEntry(uuid, "&eYou left the FFA arena.", true);
            return true;
        }

        FfaMatch match = getActiveMatch(uuid);
        if (match != null) {
            handleForfeit(player, "SURRENDER");
            return true;
        }

        if (transitioningPlayers.contains(uuid) || transitionStates.containsKey(uuid)) {
            restoreTransitionState(player);
            send(player, "&eYour FFA session is already ending.");
            return true;
        }

        send(player, "&cYou are not inside an FFA arena or match.");
        return false;
    }

    public void tick() {
        tickCounter++;
        boolean secondPulse = tickCounter % 20L == 0L;
        if (secondPulse) {
            cleanupWaitingPlayers();
        }

        processPendingResets();

        for (FfaMatch match : new ArrayList<>(activeMatches.values())) {
            if (match.getStatus() != FfaMatch.MatchStatus.ACTIVE || !secondPulse) {
                continue;
            }

             if (match.hasCombatStarted()
                    && !isCombatTagged(match.getPlayerOneUuid())
                    && !isCombatTagged(match.getPlayerTwoUuid())) {
                finishMatch(match, "COMBAT_EXPIRED", null);
                continue;
            }
        }

        if (secondPulse) {
            processAutoArenaRepairs();
        }
    }

    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (isTransitioning(uuid) || transitionStates.containsKey(uuid)) {
            restoreTransitionState(player);
        }

        if (waitingPlayers.contains(uuid)) {
            cancelWaitingEntry(uuid, null, true);
            return;
        }

        FfaMatch match = getActiveMatch(uuid);
        if (match == null) {
            return;
        }

        handleForfeit(player, "QUIT");
    }

    public void handleJoin(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (transitionStates.containsKey(uuid) || isTransitioning(uuid)) {
            restoreTransitionState(player);
        }

        FfaPlayerSnapshot snapshot = pendingJoinSnapshots.remove(uuid);
        if (snapshot != null) {
            restoreSnapshot(player, snapshot);
            send(player, "&eYour FFA state was restored after reconnecting.");
        }

        Location pendingLocation = pendingJoinLocations.remove(uuid);
        if (pendingLocation != null && pendingLocation.getWorld() != null) {
            plugin.getPaperScheduler().runEntity(player, () -> {
                if (!player.isOnline()) {
                    return;
                }
                teleportPlayer(player, pendingLocation, () -> player.setNoDamageTicks(60));
            });
            return;
        }

        if (snapshot != null) {
            return;
        }

        FfaArena arena = findArenaContainingLocation(player.getLocation());
        if (arena == null) {
            return;
        }

        Location safeJoinLocation = resolveServerJoinFallbackLocation(player.getLocation());
        if (safeJoinLocation == null || safeJoinLocation.getWorld() == null) {
            return;
        }

        plugin.getPaperScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }

            FfaArena currentArena = findArenaContainingLocation(player.getLocation());
            if (currentArena == null) {
                return;
            }

            Location orientedLocation = applyPlayerOrientation(safeJoinLocation, player);
            teleportPlayer(player, orientedLocation, () -> {
                player.setNoDamageTicks(60);
                player.setFallDistance(0F);
                player.setFireTicks(0);
                send(player, "&eYou were moved out of FFA arena &f" + currentArena.getDisplayName() + "&e after reconnecting.");
            });
        });
    }

    public boolean shouldHandleAsCustomLethalPvP(Player attacker, Player victim, double finalDamage) {
        return false;
    }

    public boolean shouldHandleAsCustomLethalDamage(Player victim, double finalDamage) {
        return false;
    }

    public void refreshCombatTag(Player first, Player second) {
        if (plugin.getCombatManager() == null || !plugin.getCombatManager().isEnabled()) {
            return;
        }
        if (first != null && second != null) {
            FfaMatch match = getActiveMatch(first.getUniqueId());
            if (match != null
                    && match.getStatus() == FfaMatch.MatchStatus.ACTIVE
                    && match.isParticipant(second.getUniqueId())) {
                match.markCombat();
            }
        }
        if (first != null) {
            plugin.getCombatManager().tag(first);
        }
        if (second != null) {
            plugin.getCombatManager().tag(second);
        }
    }

    public boolean tryStartCombatPair(Player first, Player second) {
        if (first == null || second == null) {
            return false;
        }

        UUID firstUuid = first.getUniqueId();
        UUID secondUuid = second.getUniqueId();
        if (firstUuid.equals(secondUuid) || isInMatch(firstUuid) || isInMatch(secondUuid)) {
            return false;
        }

        WaitingArenaEntry firstEntry = waitingArenaEntries.get(firstUuid);
        WaitingArenaEntry secondEntry = waitingArenaEntries.get(secondUuid);
        if (!canWaitingPlayerStay(first, firstEntry) || !canWaitingPlayerStay(second, secondEntry)) {
            return false;
        }
        if (firstEntry == null
                || secondEntry == null
                || firstEntry.arena() == null
                || secondEntry.arena() == null
                || !firstEntry.arena().getId().equalsIgnoreCase(secondEntry.arena().getId())) {
            return false;
        }

        String arenaId = firstEntry.arena().getId();
        if (hasPendingResetForArena(arenaId) || hasActiveMatchForArena(arenaId)) {
            return false;
        }

        long now = System.currentTimeMillis();
        long matchId = insertMatch(firstEntry.arena(), firstUuid, secondUuid, now);
        if (matchId <= 0L) {
            return false;
        }

        waitingPlayers.remove(firstUuid);
        waitingPlayers.remove(secondUuid);
        waitingArenaEntries.remove(firstUuid);
        waitingArenaEntries.remove(secondUuid);

        FfaMatch match = new FfaMatch(
                matchId,
                firstEntry.arena(),
                firstUuid,
                firstEntry.playerName(),
                secondUuid,
                secondEntry.playerName()
        );
        match.putSnapshot(firstUuid, firstEntry.snapshot());
        match.putSnapshot(secondUuid, secondEntry.snapshot());
        match.setStartedAt(now);

        activeMatches.put(matchId, match);
        activeMatchIds.put(firstUuid, matchId);
        activeMatchIds.put(secondUuid, matchId);
        pendingJoinSnapshots.remove(firstUuid);
        pendingJoinSnapshots.remove(secondUuid);
        pendingJoinLocations.remove(firstUuid);
        pendingJoinLocations.remove(secondUuid);

        ArenaSnapshot arenaSnapshot = firstEntry.arenaSnapshot() != null
                ? firstEntry.arenaSnapshot()
                : getArenaBaselineSnapshot(firstEntry.arena());
        if (arenaSnapshot != null) {
            arenaSnapshots.put(matchId, arenaSnapshot);
        }

        send(first, "&aCombat started against &f" + second.getName() + "&a.");
        send(second, "&aCombat started against &f" + first.getName() + "&a.");
        return true;
    }

    public boolean handleLethalPvPHit(Player attacker, Player victim) {
        return false;
    }

    public boolean handleLethalDamage(Player victim, String endReason) {
        return false;
    }

    public boolean handleDeath(PlayerDeathEvent event) {
        if (event == null) {
            return false;
        }

        Player victim = event.getEntity();
        if (victim == null) {
            return false;
        }

        FfaMatch match = getActiveMatch(victim.getUniqueId());
        if (match == null || match.getStatus() != FfaMatch.MatchStatus.ACTIVE) {
            return false;
        }

        event.getDrops().clear();
        event.setDroppedExp(0);
        finishParticipantMatch(match, victim.getUniqueId(), "DEATH", true);
        return true;
    }

    public boolean consumeRespawn(Player player, org.bukkit.event.player.PlayerRespawnEvent event) {
        if (player == null || event == null) {
            return false;
        }

        PendingRespawnState state = pendingRespawns.remove(player.getUniqueId());
        if (state == null || state.respawnLocation() == null || state.respawnLocation().getWorld() == null) {
            return false;
        }

        event.setRespawnLocation(state.respawnLocation());
        plugin.getPaperScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }
            restoreTransitionState(player);
            restoreSnapshot(player, state.snapshot());
            player.setNoDamageTicks(60);
            player.setFallDistance(0F);
            player.setFireTicks(0);
        });
        return true;
    }

    private void cleanupWaitingPlayers() {
        for (UUID uuid : new ArrayList<>(waitingPlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            WaitingArenaEntry entry = waitingArenaEntries.get(uuid);
            if (!canWaitingPlayerStay(player, entry)) {
                cancelWaitingEntry(uuid, null, true);
            }
        }
    }

    private boolean canWaitingPlayerStay(Player player, WaitingArenaEntry entry) {
        if (player == null || !player.isOnline() || entry == null || entry.arena() == null) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (!waitingPlayers.contains(uuid) || isInMatch(uuid)) {
            return false;
        }
        if (plugin.getDuelManager() != null
                && (plugin.getDuelManager().isInDuel(uuid)
                || plugin.getDuelManager().isInQueue(uuid)
                || plugin.getDuelManager().isTransitioning(uuid))) {
            return false;
        }
        if (plugin.getTeleportManager() != null && plugin.getTeleportManager().hasPending(uuid)) {
            return false;
        }
        return reservedArenaIds.contains(entry.arena().getId());
    }

    private WaitingArenaEntry findWaitingEntryForMatch() {
        for (UUID uuid : new ArrayList<>(waitingPlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            WaitingArenaEntry entry = waitingArenaEntries.get(uuid);
            if (!canWaitingPlayerStay(player, entry)) {
                cancelWaitingEntry(uuid, null, true);
                continue;
            }
            if (entry != null && entry.arena() != null && hasPendingResetForArena(entry.arena().getId())) {
                continue;
            }
            return entry;
        }
        return null;
    }

    private boolean placePlayerIntoWaitingArena(Player player, FfaArena arena) {
        if (player == null || arena == null) {
            return false;
        }

        FfaPlayerSnapshot snapshot = FfaPlayerSnapshot.capture(player);
        if (snapshot == null) {
            send(player, "&cCould not enter FFA right now.");
            return false;
        }

        ArenaSnapshot arenaSnapshot = null;
        if (shouldRollbackArena()) {
            arenaSnapshot = getArenaBaselineSnapshot(arena);
            if (arenaSnapshot == null) {
                arena.setEnabled(false);
                arena.setState(FfaArena.ArenaState.DISABLED);
                saveArena(arena);
                send(player, "&cThis FFA arena could not snapshot correctly and has been disabled.");
                return false;
            }
        }

        Location waitingSpawn = findArenaJoinSpawn(arena);
        if (waitingSpawn == null) {
            arena.setEnabled(false);
            arena.setState(FfaArena.ArenaState.DISABLED);
            saveArena(arena);
            send(player, "&cThis FFA arena does not have a safe combat spot and has been disabled.");
            return false;
        }

        UUID uuid = player.getUniqueId();
        waitingPlayers.add(uuid);
        waitingArenaEntries.put(uuid, new WaitingArenaEntry(
                arena,
                player.getName(),
                snapshot,
                arenaSnapshot,
                waitingSpawn
        ));
        reservedArenaIds.add(arena.getId());
        arena.setState(FfaArena.ArenaState.RESERVED);
        pendingJoinSnapshots.remove(uuid);
        pendingJoinLocations.remove(uuid);

        preparePlayerForMatch(player, waitingSpawn);
        send(player, "&aTeleported to FFA arena &f" + arena.getDisplayName() + "&a.");
        send(player, "&7Use &f/leave &7to leave the arena.");
        return true;
    }

    private boolean startMatchWithWaitingPlayer(Player second, WaitingArenaEntry entry) {
        if (second == null || entry == null) {
            return false;
        }

        UUID firstUuid = findWaitingPlayerUuid(entry);
        Player first = firstUuid == null ? null : Bukkit.getPlayer(firstUuid);
        if (!canWaitingPlayerStay(first, entry)) {
            if (firstUuid != null) {
                cancelWaitingEntry(firstUuid, null, true);
            }
            return false;
        }

        FfaPlayerSnapshot secondSnapshot = FfaPlayerSnapshot.capture(second);
        if (secondSnapshot == null) {
            send(second, "&cCould not start FFA right now.");
            return false;
        }

        boolean keepFirstCurrentPosition = isWaitingParticipantInsideArena(first, entry);
        Location firstSpawn = keepFirstCurrentPosition ? null : resolveWaitingParticipantMatchStartLocation(first, entry);
        Location secondSpawn = findArenaJoinSpawn(entry.arena());
        if ((!keepFirstCurrentPosition && firstSpawn == null) || secondSpawn == null) {
            cancelWaitingEntry(firstUuid, "&cThis FFA arena no longer has a second safe combat spot.", true);
            entry.arena().setEnabled(false);
            entry.arena().setState(FfaArena.ArenaState.DISABLED);
            saveArena(entry.arena());
            send(second, "&cThis FFA arena no longer has two safe combat spots and has been disabled.");
            return false;
        }

        long now = System.currentTimeMillis();
        long matchId = insertMatch(entry.arena(), first.getUniqueId(), second.getUniqueId(), now);
        if (matchId <= 0L) {
            send(second, "&cCould not start FFA right now.");
            return false;
        }

        waitingPlayers.remove(firstUuid);
        waitingArenaEntries.remove(firstUuid);

        FfaMatch match = new FfaMatch(
                matchId,
                entry.arena(),
                first.getUniqueId(),
                entry.playerName(),
                second.getUniqueId(),
                second.getName()
        );
        match.putSnapshot(first.getUniqueId(), entry.snapshot());
        match.putSnapshot(second.getUniqueId(), secondSnapshot);
        match.setStartedAt(now);

        activeMatches.put(matchId, match);
        activeMatchIds.put(first.getUniqueId(), matchId);
        activeMatchIds.put(second.getUniqueId(), matchId);
        pendingJoinSnapshots.remove(first.getUniqueId());
        pendingJoinSnapshots.remove(second.getUniqueId());
        pendingJoinLocations.remove(first.getUniqueId());
        pendingJoinLocations.remove(second.getUniqueId());

        if (entry.arenaSnapshot() != null) {
            arenaSnapshots.put(matchId, entry.arenaSnapshot());
        }

        preparePlayerForMatch(first, firstSpawn);
        preparePlayerForMatch(second, secondSpawn);
        clearMatchSpawnProtection(first.getUniqueId());
        clearMatchSpawnProtection(second.getUniqueId());
        plugin.getPaperScheduler().runGlobalLater(() -> {
            clearMatchSpawnProtection(first.getUniqueId());
            clearMatchSpawnProtection(second.getUniqueId());
        }, 1L);

        send(first, "&aFFA started against &f" + second.getName() + "&a on arena &f" + entry.arena().getDisplayName() + "&a.");
        send(second, "&aFFA started against &f" + entry.playerName() + "&a on arena &f" + entry.arena().getDisplayName() + "&a.");
        send(first, "&7Use &f/leave &7to surrender.");
        send(second, "&7Use &f/leave &7to surrender.");
        return true;
    }

    private boolean startMatchBetweenWaitingPlayers(UUID firstUuid,
                                                    WaitingArenaEntry firstEntry,
                                                    UUID secondUuid,
                                                    WaitingArenaEntry secondEntry) {
        return startMatchBetweenWaitingPlayers(firstUuid, firstEntry, secondUuid, secondEntry, false);
    }

    private boolean startMatchBetweenWaitingPlayers(UUID firstUuid,
                                                    WaitingArenaEntry firstEntry,
                                                    UUID secondUuid,
                                                    WaitingArenaEntry secondEntry,
                                                    boolean keepCurrentPositions) {
        if (firstUuid == null || secondUuid == null || firstEntry == null || secondEntry == null) {
            return false;
        }

        Player first = Bukkit.getPlayer(firstUuid);
        Player second = Bukkit.getPlayer(secondUuid);
        if (!canWaitingPlayerStay(first, firstEntry)) {
            cancelWaitingEntry(firstUuid, null, true);
            return false;
        }
        if (!canWaitingPlayerStay(second, secondEntry)) {
            cancelWaitingEntry(secondUuid, null, true);
            return false;
        }
        if (firstEntry.arena() == null
                || secondEntry.arena() == null
                || !firstEntry.arena().getId().equalsIgnoreCase(secondEntry.arena().getId())) {
            return false;
        }

        FfaArena arena = firstEntry.arena();
        Location firstSpawn = keepCurrentPositions ? null : resolveWaitingReentryLocation(firstEntry, null);
        Location secondSpawn = keepCurrentPositions ? null : resolveWaitingReentryLocation(secondEntry, firstSpawn);
        if (!keepCurrentPositions && (firstSpawn == null || secondSpawn == null)) {
            cancelWaitingEntry(firstUuid, "&cThis FFA arena no longer has two safe combat spots.", true);
            cancelWaitingEntry(secondUuid, "&cThis FFA arena no longer has two safe combat spots.", true);
            arena.setEnabled(false);
            arena.setState(FfaArena.ArenaState.DISABLED);
            saveArena(arena);
            return false;
        }

        long now = System.currentTimeMillis();
        long matchId = insertMatch(arena, firstUuid, secondUuid, now);
        if (matchId <= 0L) {
            send(first, "&cCould not restart FFA right now.");
            send(second, "&cCould not restart FFA right now.");
            return false;
        }

        waitingPlayers.remove(firstUuid);
        waitingPlayers.remove(secondUuid);
        waitingArenaEntries.remove(firstUuid);
        waitingArenaEntries.remove(secondUuid);

        FfaMatch match = new FfaMatch(
                matchId,
                arena,
                firstUuid,
                firstEntry.playerName(),
                secondUuid,
                secondEntry.playerName()
        );
        match.putSnapshot(firstUuid, firstEntry.snapshot());
        match.putSnapshot(secondUuid, secondEntry.snapshot());
        match.setStartedAt(now);

        activeMatches.put(matchId, match);
        activeMatchIds.put(firstUuid, matchId);
        activeMatchIds.put(secondUuid, matchId);
        pendingJoinSnapshots.remove(firstUuid);
        pendingJoinSnapshots.remove(secondUuid);
        pendingJoinLocations.remove(firstUuid);
        pendingJoinLocations.remove(secondUuid);

        ArenaSnapshot arenaSnapshot = firstEntry.arenaSnapshot() != null
                ? firstEntry.arenaSnapshot()
                : getArenaBaselineSnapshot(arena);
        if (arenaSnapshot != null) {
            arenaSnapshots.put(matchId, arenaSnapshot);
        }

        preparePlayerForMatch(first, firstSpawn);
        preparePlayerForMatch(second, secondSpawn);
        clearMatchSpawnProtection(firstUuid);
        clearMatchSpawnProtection(secondUuid);
        plugin.getPaperScheduler().runGlobalLater(() -> {
            clearMatchSpawnProtection(firstUuid);
            clearMatchSpawnProtection(secondUuid);
        }, 1L);

        String restartMessage = keepCurrentPositions
                ? "&aFFA resumed against &f"
                : "&aFFA restarted against &f";
        send(first, restartMessage + secondEntry.playerName() + "&a on arena &f" + arena.getDisplayName() + "&a.");
        send(second, restartMessage + firstEntry.playerName() + "&a on arena &f" + arena.getDisplayName() + "&a.");
        send(first, "&7Use &f/leave &7to surrender.");
        send(second, "&7Use &f/leave &7to surrender.");
        return true;
    }

    private WaitingMatchPair findWaitingMatchPairForArena(FfaArena arena) {
        if (arena == null) {
            return null;
        }

        UUID firstUuid = null;
        WaitingArenaEntry firstEntry = null;
        for (UUID uuid : new ArrayList<>(waitingPlayers)) {
            WaitingArenaEntry entry = waitingArenaEntries.get(uuid);
            if (entry == null
                    || entry.arena() == null
                    || !arena.getId().equalsIgnoreCase(entry.arena().getId())) {
                continue;
            }

            Player player = Bukkit.getPlayer(uuid);
            if (!canWaitingPlayerStay(player, entry)) {
                cancelWaitingEntry(uuid, null, true);
                continue;
            }

            if (firstUuid == null) {
                firstUuid = uuid;
                firstEntry = entry;
                continue;
            }

            return new WaitingMatchPair(firstUuid, firstEntry, uuid, entry);
        }

        return null;
    }

    private boolean hasWaitingEntriesForArena(String arenaId) {
        if (arenaId == null || arenaId.isBlank()) {
            return false;
        }

        for (WaitingArenaEntry entry : waitingArenaEntries.values()) {
            if (entry != null && entry.arena() != null && arenaId.equalsIgnoreCase(entry.arena().getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameWaitingPair(WaitingMatchPair pair, FfaMatch match) {
        if (pair == null || match == null) {
            return false;
        }

        UUID firstUuid = pair.firstUuid();
        UUID secondUuid = pair.secondUuid();
        return (firstUuid.equals(match.getPlayerOneUuid()) && secondUuid.equals(match.getPlayerTwoUuid()))
                || (firstUuid.equals(match.getPlayerTwoUuid()) && secondUuid.equals(match.getPlayerOneUuid()));
    }

    private UUID findWaitingPlayerUuid(WaitingArenaEntry target) {
        for (Map.Entry<UUID, WaitingArenaEntry> entry : waitingArenaEntries.entrySet()) {
            if (entry.getValue() == target) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void preparePlayerForMatch(Player player, Location teleportLocation) {
        if (player == null) {
            return;
        }

        player.closeInventory();
        restoreTransitionState(player);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setInvulnerable(false);
        player.setCollidable(true);
        clearPotionEffects(player);
        healPlayerForMatch(player);
        player.setAbsorptionAmount(0D);
        player.setFireTicks(0);
        player.setFallDistance(0F);
        FfaArena sessionArena = getSessionArena(player.getUniqueId());
        if (teleportLocation != null && teleportLocation.getWorld() != null) {
            teleportPlayer(player, teleportLocation, () -> {
                player.setNoDamageTicks(0);
                applyArenaRules(player, sessionArena);
            });
            return;
        }
        player.setNoDamageTicks(0);
        applyArenaRules(player, sessionArena);
    }

    private Location findArenaSpawn(FfaArena arena, Location avoidLocation) {
        ArenaRegionBounds bounds = resolveArenaRegionBounds(arena);
        if (bounds == null) {
            return null;
        }
        World world = bounds.world();
        int minY = bounds.minY();
        int maxY = bounds.maxY();
        int searchMinX = bounds.maxX() - bounds.minX() >= 2 ? bounds.minX() + 1 : bounds.minX();
        int searchMaxX = bounds.maxX() - bounds.minX() >= 2 ? bounds.maxX() - 1 : bounds.maxX();
        int searchMinZ = bounds.maxZ() - bounds.minZ() >= 2 ? bounds.minZ() + 1 : bounds.minZ();
        int searchMaxZ = bounds.maxZ() - bounds.minZ() >= 2 ? bounds.maxZ() - 1 : bounds.maxZ();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 64; attempt++) {
            int x = random.nextInt(searchMinX, searchMaxX + 1);
            int z = random.nextInt(searchMinZ, searchMaxZ + 1);
            Location candidate = findSafeStandingLocation(world, x, z, minY, maxY);
            if (candidate != null && isValidArenaSpawn(candidate, avoidLocation)) {
                return candidate;
            }
        }

        Location fallback = null;
        for (int x = searchMinX; x <= searchMaxX; x++) {
            for (int z = searchMinZ; z <= searchMaxZ; z++) {
                Location candidate = findSafeStandingLocation(world, x, z, minY, maxY);
                if (candidate == null) {
                    continue;
                }
                if (isValidArenaSpawn(candidate, avoidLocation)) {
                    return candidate;
                }
                if (fallback == null && !isSameBlock(candidate, avoidLocation)) {
                    fallback = candidate;
                }
            }
        }
        return fallback;
    }

    private Location findArenaJoinSpawn(FfaArena arena) {
        if (arena == null) {
            return null;
        }

        ArenaRegionBounds bounds = resolveArenaRegionBounds(arena);
        Location anchor = arena.getRegionPos1();
        if (bounds != null
                && anchor != null
                && anchor.getWorld() != null
                && bounds.world().getName().equalsIgnoreCase(anchor.getWorld().getName())) {
            Location exactAnchor = resolveArenaJoinAnchorSpawn(bounds, anchor);
            if (exactAnchor != null) {
                return exactAnchor;
            }
        }

        return null;
    }

    private Location findArenaJoinPartnerSpawn(FfaArena arena, Location avoidLocation) {
        if (avoidLocation != null && avoidLocation.getWorld() != null) {
            return avoidLocation.clone();
        }

        if (arena == null) {
            return null;
        }

        Location anchorSpawn = findArenaJoinSpawn(arena);
        if (anchorSpawn != null) {
            return anchorSpawn;
        }

        return findArenaSpawn(arena, null);
    }

    private boolean isWaitingParticipantInsideArena(Player player, WaitingArenaEntry entry) {
        if (player == null || entry == null || entry.arena() == null) {
            return false;
        }

        ArenaRegionBounds bounds = resolveArenaRegionBounds(entry.arena());
        Location current = player.getLocation();
        return bounds != null
                && current.getWorld() != null
                && bounds.world().getName().equalsIgnoreCase(current.getWorld().getName())
                && isWithinArenaBounds(current, bounds);
    }

    private Location resolveWaitingParticipantMatchStartLocation(Player player, WaitingArenaEntry entry) {
        if (player == null || entry == null || entry.arena() == null) {
            return null;
        }

        return resolveWaitingReentryLocation(entry, null);
    }

    private Location resolveArenaJoinAnchorSpawn(ArenaRegionBounds bounds, Location anchor) {
        if (bounds == null || anchor == null || anchor.getWorld() == null) {
            return null;
        }

        int baseX = anchor.getBlockX();
        int baseZ = anchor.getBlockZ();
        if (baseX < bounds.minX() || baseX > bounds.maxX() || baseZ < bounds.minZ() || baseZ > bounds.maxZ()) {
            return null;
        }

        Location normalizedAnchor = normalizeArenaJoinAnchor(anchor);
        if (isSafeExactStandingLocation(normalizedAnchor, bounds)) {
            return normalizedAnchor;
        }

        Location standingSpot = findSafeStandingLocation(bounds.world(), baseX, baseZ, bounds.minY(), bounds.maxY());
        if (standingSpot == null || !isWithinArenaBounds(standingSpot, bounds)) {
            return null;
        }

        Location anchoredStandingSpot = normalizedAnchor.clone();
        anchoredStandingSpot.setY(standingSpot.getY());
        if (isSafeExactStandingLocation(anchoredStandingSpot, bounds)) {
            return anchoredStandingSpot;
        }

        return standingSpot;
    }

    private Location normalizeArenaJoinAnchor(Location anchor) {
        if (anchor == null) {
            return null;
        }

        Location normalized = anchor.clone();
        if (isWholeBlockCoordinate(normalized.getX())) {
            normalized.setX(normalized.getX() + 0.5D);
        }
        if (isWholeBlockCoordinate(normalized.getZ())) {
            normalized.setZ(normalized.getZ() + 0.5D);
        }
        return normalized;
    }

    private boolean isSafeExactStandingLocation(Location location, ArenaRegionBounds bounds) {
        if (location == null || bounds == null || location.getWorld() == null) {
            return false;
        }
        if (!bounds.world().getName().equalsIgnoreCase(location.getWorld().getName())
                || !isWithinArenaBounds(location, bounds)) {
            return false;
        }

        int groundY = location.getBlockY() - 1;
        return groundY >= bounds.minY()
                && groundY <= bounds.maxY()
                && isSafeStandingSpot(location.getWorld(), location.getBlockX(), groundY, location.getBlockZ());
    }

    private boolean isWholeBlockCoordinate(double value) {
        return Math.abs(value - Math.rint(value)) < 1.0E-6D;
    }

    private Location findSafeLocationNear(ArenaRegionBounds bounds,
                                          int baseX,
                                          int baseZ,
                                          Location avoidLocation,
                                          int maxRadius) {
        return findSafeLocationNear(bounds, baseX, baseZ, avoidLocation, maxRadius, DEFAULT_ARENA_SPAWN_DISTANCE_SQUARED);
    }

    private Location findSafeLocationNear(ArenaRegionBounds bounds,
                                          int baseX,
                                          int baseZ,
                                          Location avoidLocation,
                                          int maxRadius,
                                          double minimumDistanceSquared) {
        if (bounds == null) {
            return null;
        }

        int safeRadius = Math.max(0, maxRadius);
        Location fallback = null;
        for (int radius = 0; radius <= safeRadius; radius++) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                    int x = baseX + offsetX;
                    int z = baseZ + offsetZ;
                    if (x < bounds.minX() || x > bounds.maxX() || z < bounds.minZ() || z > bounds.maxZ()) {
                        continue;
                    }

                    Location candidate = findSafeStandingLocation(bounds.world(), x, z, bounds.minY(), bounds.maxY());
                    if (candidate == null) {
                        continue;
                    }
                    if (isValidArenaSpawn(candidate, avoidLocation, minimumDistanceSquared)) {
                        return candidate;
                    }
                    if (fallback == null && !isSameBlock(candidate, avoidLocation)) {
                        fallback = candidate;
                    }
                }
            }
        }

        return fallback;
    }

    private Location findSafePartnerLocationNear(ArenaRegionBounds bounds,
                                                 int baseX,
                                                 int baseZ,
                                                 Location avoidLocation,
                                                 int maxRadius) {
        if (bounds == null) {
            return null;
        }

        int safeRadius = Math.max(0, maxRadius);
        for (int radius = 0; radius <= safeRadius; radius++) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                    int x = baseX + offsetX;
                    int z = baseZ + offsetZ;
                    if (x < bounds.minX() || x > bounds.maxX() || z < bounds.minZ() || z > bounds.maxZ()) {
                        continue;
                    }

                    Location candidate = findSafeStandingLocation(bounds.world(), x, z, bounds.minY(), bounds.maxY());
                    if (isValidArenaPartnerSpawn(candidate, avoidLocation)) {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    private Location findArenaPartnerSpawn(FfaArena arena, Location avoidLocation) {
        ArenaRegionBounds bounds = resolveArenaRegionBounds(arena);
        if (bounds == null) {
            return null;
        }

        World world = bounds.world();
        int minY = bounds.minY();
        int maxY = bounds.maxY();
        int searchMinX = bounds.maxX() - bounds.minX() >= 2 ? bounds.minX() + 1 : bounds.minX();
        int searchMaxX = bounds.maxX() - bounds.minX() >= 2 ? bounds.maxX() - 1 : bounds.maxX();
        int searchMinZ = bounds.maxZ() - bounds.minZ() >= 2 ? bounds.minZ() + 1 : bounds.minZ();
        int searchMaxZ = bounds.maxZ() - bounds.minZ() >= 2 ? bounds.maxZ() - 1 : bounds.maxZ();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 64; attempt++) {
            int x = random.nextInt(searchMinX, searchMaxX + 1);
            int z = random.nextInt(searchMinZ, searchMaxZ + 1);
            Location candidate = findSafeStandingLocation(world, x, z, minY, maxY);
            if (isValidArenaPartnerSpawn(candidate, avoidLocation)) {
                return candidate;
            }
        }

        for (int x = searchMinX; x <= searchMaxX; x++) {
            for (int z = searchMinZ; z <= searchMaxZ; z++) {
                Location candidate = findSafeStandingLocation(world, x, z, minY, maxY);
                if (isValidArenaPartnerSpawn(candidate, avoidLocation)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private Location findSafeStandingLocation(World world, int x, int z, int minY, int maxY) {
        for (int groundY = maxY - 2; groundY >= minY; groundY--) {
            if (isSafeStandingSpot(world, x, groundY, z)) {
                return new Location(world, x + 0.5D, groundY + 1.0D, z + 0.5D);
            }
        }
        return null;
    }

    private boolean isValidArenaSpawn(Location candidate, Location avoidLocation) {
        return isValidArenaSpawn(candidate, avoidLocation, DEFAULT_ARENA_SPAWN_DISTANCE_SQUARED);
    }

    private boolean isValidArenaPartnerSpawn(Location candidate, Location avoidLocation) {
        return candidate != null
                && !isSameBlock(candidate, avoidLocation)
                && !isSameColumn(candidate, avoidLocation)
                && isValidArenaSpawn(candidate, avoidLocation, JOIN_PAIR_SPAWN_DISTANCE_SQUARED);
    }

    private boolean isValidArenaSpawn(Location candidate, Location avoidLocation, double minimumDistanceSquared) {
        if (candidate == null) {
            return false;
        }
        if (avoidLocation == null || avoidLocation.getWorld() == null) {
            return true;
        }
        if (candidate.getWorld() == null || !candidate.getWorld().getName().equalsIgnoreCase(avoidLocation.getWorld().getName())) {
            return true;
        }
        return candidate.distanceSquared(avoidLocation) >= Math.max(0.0D, minimumDistanceSquared);
    }

    private boolean isSameBlock(Location first, Location second) {
        if (first == null || second == null || first.getWorld() == null || second.getWorld() == null) {
            return false;
        }
        return first.getWorld().getName().equalsIgnoreCase(second.getWorld().getName())
                && first.getBlockX() == second.getBlockX()
                && first.getBlockY() == second.getBlockY()
                && first.getBlockZ() == second.getBlockZ();
    }

    private boolean isSameColumn(Location first, Location second) {
        if (first == null || second == null || first.getWorld() == null || second.getWorld() == null) {
            return false;
        }
        return first.getWorld().getName().equalsIgnoreCase(second.getWorld().getName())
                && first.getBlockX() == second.getBlockX()
                && first.getBlockZ() == second.getBlockZ();
    }

    private boolean isSafeStandingSpot(World world, int x, int groundY, int z) {
        if (world == null || groundY < world.getMinHeight() || groundY + 2 >= world.getMaxHeight()) {
            return false;
        }

        Block ground = world.getBlockAt(x, groundY, z);
        Block feet = world.getBlockAt(x, groundY + 1, z);
        Block head = world.getBlockAt(x, groundY + 2, z);
        return ground.getType().isSolid()
                && feet.isPassable()
                && head.isPassable()
                && !isHazardous(ground.getType())
                && !isHazardous(feet.getType())
                && !isHazardous(head.getType());
    }

    private boolean isHazardous(Material material) {
        if (material == null) {
            return true;
        }
        String name = material.name();
        return name.contains("LAVA")
                || name.contains("WATER")
                || name.contains("FIRE")
                || name.contains("CACTUS")
                || name.contains("MAGMA")
                || name.contains("CAMPFIRE")
                || name.contains("POWDER_SNOW")
                || name.contains("SWEET_BERRY_BUSH")
                || name.contains("VOID");
    }

    private void sendMatchActionBar(FfaMatch match, long remainingSeconds) {
    }

    private void handleForfeit(Player loser, String endReason) {
        if (loser == null) {
            return;
        }

        FfaMatch match = getActiveMatch(loser.getUniqueId());
        if (match == null) {
            return;
        }

        finishParticipantMatch(match, loser.getUniqueId(), endReason, false);
    }

    private void cancelWaitingEntry(UUID uuid, String feedbackMessage, boolean restorePlayerState) {
        if (uuid == null) {
            return;
        }

        waitingPlayers.remove(uuid);
        WaitingArenaEntry entry = waitingArenaEntries.remove(uuid);
        if (entry == null) {
            return;
        }

        FfaArena arena = entry.arena();
        if (arena != null
                && !hasArenaOccupants(arena.getId())
                && !hasPendingResetForArena(arena.getId())) {
            arena.setState(FfaArena.ArenaState.RESETTING);
        }
        finishWaitingEntry(uuid, entry, feedbackMessage, restorePlayerState, true,
                () -> resetArenaAfterWaitingExit(arena, entry.arenaSnapshot()));
    }

    private void resetArenaAfterWaitingExit(FfaArena arena, ArenaSnapshot arenaSnapshot) {
        if (arena == null) {
            return;
        }

        plugin.getPaperScheduler().runGlobal(() -> {
            if (!hasArenaOccupants(arena.getId()) && !hasPendingResetForArena(arena.getId())) {
                resetArena(arena, arenaSnapshot, null);
            }
        });
    }

    private void finishParticipantMatch(FfaMatch match, UUID exitingUuid, String endReason, boolean respawnParticipant) {
        if (match == null
                || exitingUuid == null
                || !match.isParticipant(exitingUuid)
                || match.getStatus() == FfaMatch.MatchStatus.POST_MATCH
                || match.getStatus() == FfaMatch.MatchStatus.FINISHED) {
            return;
        }

        UUID remainingUuid = match.getOpponent(exitingUuid);
        ArenaSnapshot arenaSnapshot = arenaSnapshots.get(match.getId());

        match.setStatus(FfaMatch.MatchStatus.POST_MATCH);
        updateMatchRecord(match, null, null, "FINISHED", endReason);

        activeMatches.remove(match.getId());
        activeMatchIds.remove(match.getPlayerOneUuid());
        activeMatchIds.remove(match.getPlayerTwoUuid());
        match.setStatus(FfaMatch.MatchStatus.FINISHED);

        clearParticipantCombatState(exitingUuid);

        int delayTicks = getReturnDelayTicks();
        Location exitingReturn = resolveExitLocation(match, exitingUuid, endReason);
        finishParticipant(match, exitingUuid, respawnParticipant ? exitingUuid : null, delayTicks, exitingReturn);

        if (!preserveRemainingParticipant(match, remainingUuid, arenaSnapshot, null, "&eYou remain in the FFA arena.") && remainingUuid != null) {
            finishParticipant(match, remainingUuid, null, delayTicks);
        }
        queuePendingReset(match, System.currentTimeMillis());
    }

    private void finishMatch(FfaMatch match, String endReason, UUID respawnParticipantUuid) {
        if (match == null
                || match.getStatus() == FfaMatch.MatchStatus.POST_MATCH
                || match.getStatus() == FfaMatch.MatchStatus.FINISHED) {
            return;
        }

        match.setStatus(FfaMatch.MatchStatus.POST_MATCH);
        updateMatchRecord(match, null, null, "FINISHED", endReason);

        activeMatches.remove(match.getId());
        activeMatchIds.remove(match.getPlayerOneUuid());
        activeMatchIds.remove(match.getPlayerTwoUuid());
        match.setStatus(FfaMatch.MatchStatus.FINISHED);

        clearParticipantCombatState(match.getPlayerOneUuid());
        clearParticipantCombatState(match.getPlayerTwoUuid());

        int delayTicks = getReturnDelayTicks();
        if ("COMBAT_EXPIRED".equalsIgnoreCase(endReason)) {
            ArenaSnapshot arenaSnapshot = arenaSnapshots.get(match.getId());
            boolean preservedFirst = preserveRemainingParticipant(
                    match,
                    match.getPlayerOneUuid(),
                    arenaSnapshot,
                    null,
                    "&eCombat ended. Wait while the arena resets."
            );
            boolean preservedSecond = preserveRemainingParticipant(
                    match,
                    match.getPlayerTwoUuid(),
                    arenaSnapshot,
                    null,
                    "&eCombat ended. Wait while the arena resets."
            );
            if (!preservedFirst) {
                finishParticipant(match, match.getPlayerOneUuid(), respawnParticipantUuid, delayTicks);
            }
            if (!preservedSecond) {
                finishParticipant(match, match.getPlayerTwoUuid(), respawnParticipantUuid, delayTicks);
            }
        } else {
            finishParticipant(match, match.getPlayerOneUuid(), respawnParticipantUuid, delayTicks);
            finishParticipant(match, match.getPlayerTwoUuid(), respawnParticipantUuid, delayTicks);
        }
        queuePendingReset(match, System.currentTimeMillis());
    }

    private void finishParticipant(FfaMatch match, UUID uuid, UUID respawnParticipantUuid, int delayTicks) {
        finishParticipant(match, uuid, respawnParticipantUuid, delayTicks, null);
    }

    private void finishParticipant(FfaMatch match,
                                   UUID uuid,
                                   UUID respawnParticipantUuid,
                                   int delayTicks,
                                   Location explicitReturnLocation) {
        if (match == null || uuid == null) {
            return;
        }

        if (respawnParticipantUuid != null && respawnParticipantUuid.equals(uuid)) {
            queueRespawnRestore(match, uuid);
            return;
        }

        restoreParticipant(match, uuid);
        prepareTransition(uuid);
        Location returnLocation = explicitReturnLocation != null ? explicitReturnLocation : resolveReturnLocation(match, uuid);
        scheduleReturn(uuid, returnLocation, delayTicks);
    }

    private void queueRespawnRestore(FfaMatch match, UUID uuid) {
        if (match == null || uuid == null) {
            return;
        }

        FfaPlayerSnapshot snapshot = match.getSnapshot(uuid);
        if (snapshot == null) {
            return;
        }

        Location respawnLocation = resolveLobbyRespawnLocation(match, uuid);
        if (respawnLocation == null || respawnLocation.getWorld() == null) {
            return;
        }

        pendingRespawns.put(uuid, new PendingRespawnState(snapshot, respawnLocation));
    }

    private boolean preserveRemainingParticipant(FfaMatch match,
                                                 UUID uuid,
                                                 ArenaSnapshot arenaSnapshot,
                                                 Location holdLocation,
                                                 String feedbackMessage) {
        if (match == null || uuid == null) {
            return false;
        }

        FfaPlayerSnapshot snapshot = match.getSnapshot(uuid);
        if (snapshot == null) {
            return false;
        }

        FfaArena arena = match.getArena();
        if (arena == null) {
            return false;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return false;
        }

        restoreTransitionState(player);
        Location waitingLocation = holdLocation;
        if (waitingLocation != null && waitingLocation.getWorld() != null) {
            teleportPlayer(player, waitingLocation, null);
        } else {
            waitingLocation = player.getLocation().clone();
        }
        waitingPlayers.add(uuid);
        waitingArenaEntries.put(uuid, new WaitingArenaEntry(
                arena,
                player.getName(),
                snapshot,
                arenaSnapshot,
                waitingLocation.clone()
        ));
        reservedArenaIds.add(arena.getId());
        arena.setState(FfaArena.ArenaState.RESERVED);
        pendingJoinSnapshots.remove(uuid);
        pendingJoinLocations.remove(uuid);
        player.setNoDamageTicks(60);
        player.setFallDistance(0F);
        player.setFireTicks(0);
        applyArenaRules(player, arena);
        if (feedbackMessage != null && !feedbackMessage.isBlank()) {
            send(player, feedbackMessage);
        }
        return true;
    }

    private void restoreParticipant(FfaMatch match, UUID uuid) {
        if (match == null || uuid == null) {
            return;
        }

        FfaPlayerSnapshot snapshot = match.getSnapshot(uuid);
        if (snapshot == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            pendingJoinSnapshots.put(uuid, snapshot);
            return;
        }

        restoreSnapshot(player, snapshot);
    }

    private void restoreSnapshot(Player player, FfaPlayerSnapshot snapshot) {
        if (player == null || snapshot == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setStorageContents(snapshot.getStorageContents());
        inventory.setArmorContents(snapshot.getArmorContents());
        inventory.setItemInOffHand(snapshot.getOffHand());

        clearPotionEffects(player);
        for (PotionEffect effect : snapshot.getPotionEffects()) {
            player.addPotionEffect(effect);
        }

        player.setGameMode(snapshot.getGameMode());
        player.setAllowFlight(snapshot.isAllowFlight());
        player.setFlying(snapshot.isAllowFlight() && snapshot.isFlying());
        player.setFoodLevel(snapshot.getFoodLevel());
        player.setSaturation(snapshot.getSaturation());
        player.setExhaustion(snapshot.getExhaustion());
        player.setFireTicks(snapshot.getFireTicks());
        player.setFallDistance(0F);
        player.setLevel(snapshot.getLevel());
        player.setExp(snapshot.getExperienceProgress());
        player.setTotalExperience(snapshot.getTotalExperience());
        player.setAbsorptionAmount(snapshot.getAbsorptionAmount());
        player.resetPlayerTime();
        player.resetPlayerWeather();

        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH) == null
                ? 20D
                : player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, Math.max(1D, snapshot.getHealth())));
        player.updateInventory();
    }

    private void queuePendingReset(FfaMatch match, long earliestResetAt) {
        if (match == null) {
            return;
        }

        pendingResetMatches.put(match.getId(), match);
        pendingResetEarliestAt.put(match.getId(), Math.max(System.currentTimeMillis(), earliestResetAt));
        updateCombatLock(match.getPlayerOneUuid());
        updateCombatLock(match.getPlayerTwoUuid());
    }

    private void processPendingResets() {
        if (pendingResetMatches.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        for (FfaMatch match : new ArrayList<>(pendingResetMatches.values())) {
            if (match == null) {
                continue;
            }

            long earliestResetAt = pendingResetEarliestAt.getOrDefault(match.getId(), 0L);
            updateCombatLock(match.getPlayerOneUuid());
            updateCombatLock(match.getPlayerTwoUuid());
            if (now < earliestResetAt) {
                continue;
            }
            if (pendingRespawns.containsKey(match.getPlayerOneUuid()) || pendingRespawns.containsKey(match.getPlayerTwoUuid())) {
                continue;
            }
            if (isTransitioning(match.getPlayerOneUuid()) || isTransitioning(match.getPlayerTwoUuid())) {
                continue;
            }
            if (isCombatTagged(match.getPlayerOneUuid()) || isCombatTagged(match.getPlayerTwoUuid())) {
                continue;
            }

            finalizePendingReset(match);
        }
    }

    private void processAutoArenaRepairs() {
        if (!shouldRollbackArena() || arenas.isEmpty()) {
            return;
        }

        List<FfaArena> arenaList = new ArrayList<>(getArenas());
        if (arenaList.isEmpty()) {
            return;
        }

        if (autoRepairArenaCursor < 0 || autoRepairArenaCursor >= arenaList.size()) {
            autoRepairArenaCursor = 0;
        }

        for (int checked = 0; checked < arenaList.size(); checked++) {
            FfaArena arena = arenaList.get(autoRepairArenaCursor);
            autoRepairArenaCursor = (autoRepairArenaCursor + 1) % arenaList.size();
            if (!canAutoRepairArena(arena)) {
                continue;
            }

            ArenaSnapshot baselineSnapshot = getArenaBaselineSnapshot(arena);
            List<BlockSnapshot> changedBlocks = collectChangedArenaBlocks(baselineSnapshot);
            if (changedBlocks.isEmpty()) {
                changedBlocks = collectSurfaceHoleRepairBlocks(arena);
            }
            if (baselineSnapshot == null || changedBlocks.isEmpty()) {
                continue;
            }

            performAutoArenaRepair(arena, baselineSnapshot, changedBlocks);
            break;
        }
    }

    private boolean canAutoRepairArena(FfaArena arena) {
        if (arena == null
                || !arena.isEnabled()
                || !arena.isConfigured()
                || arena.getState() == FfaArena.ArenaState.RESETTING
                || hasPendingResetForArena(arena.getId())) {
            return false;
        }

        ArenaRegionBounds bounds = resolveArenaRegionBounds(arena);
        if (bounds == null) {
            return false;
        }

        for (Player player : bounds.world().getPlayers()) {
            if (player == null || !player.isOnline() || !isWithinArenaBounds(player.getLocation(), bounds)) {
                continue;
            }

            UUID uuid = player.getUniqueId();
            if (isCombatTagged(uuid) || isTransitioning(uuid) || pendingRespawns.containsKey(uuid)) {
                return false;
            }
        }

        return true;
    }

    private List<BlockSnapshot> collectChangedArenaBlocks(ArenaSnapshot snapshot) {
        List<BlockSnapshot> changedBlocks = new ArrayList<>();
        if (snapshot == null) {
            return changedBlocks;
        }

        World world = Bukkit.getWorld(snapshot.worldName());
        if (world == null) {
            return changedBlocks;
        }

        for (BlockSnapshot blockSnapshot : snapshot.blocks()) {
            String currentBlockData = world.getBlockAt(blockSnapshot.x(), blockSnapshot.y(), blockSnapshot.z())
                    .getBlockData()
                    .getAsString();
            if (!blockSnapshot.blockDataString().equals(currentBlockData)) {
                changedBlocks.add(blockSnapshot);
            }
        }

        return changedBlocks;
    }

    private List<BlockSnapshot> collectSurfaceHoleRepairBlocks(FfaArena arena) {
        List<BlockSnapshot> repairBlocks = new ArrayList<>();
        if (arena == null) {
            return repairBlocks;
        }

        ArenaRegionBounds bounds = resolveArenaRegionBounds(arena);
        if (bounds == null) {
            return repairBlocks;
        }

        World world = bounds.world();
        int width = bounds.maxX() - bounds.minX() + 1;
        int depth = bounds.maxZ() - bounds.minZ() + 1;
        SnapshotSurface[][] surfaceMap = new SnapshotSurface[width][depth];
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                surfaceMap[x - bounds.minX()][z - bounds.minZ()] = findWorldSurface(world, bounds.minY(), bounds.maxY(), x, z);
            }
        }

        Map<Long, BlockSnapshot> uniqueRepairBlocks = new HashMap<>();
        Map<String, Material> materialCache = new HashMap<>();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                SnapshotSurface currentSurface = surfaceMap[x - bounds.minX()][z - bounds.minZ()];
                int currentSurfaceY = currentSurface == null ? bounds.minY() - 1 : currentSurface.y();
                SnapshotRepairTarget repairTarget = resolveWorldSurfaceRepairTarget(bounds, surfaceMap, x, z, currentSurfaceY);
                if (repairTarget == null || repairTarget.surfaceY() <= currentSurfaceY) {
                    continue;
                }

                String fillerBlockData = currentSurface != null && isSnapshotSolid(resolveSnapshotMaterial(currentSurface.blockDataString(), materialCache))
                        ? currentSurface.blockDataString()
                        : deriveSubsurfaceBlockData(repairTarget.topBlockData(), materialCache);
                for (int y = currentSurfaceY + 1; y <= repairTarget.surfaceY(); y++) {
                    String targetBlockData = y == repairTarget.surfaceY()
                            ? repairTarget.topBlockData()
                            : fillerBlockData;
                    String currentBlockData = world.getBlockAt(x, y, z).getBlockData().getAsString();
                    if (currentBlockData.equals(targetBlockData)) {
                        continue;
                    }

                    uniqueRepairBlocks.put(blockKey(x, y, z), new BlockSnapshot(x, y, z, targetBlockData));
                }
            }
        }

        repairBlocks.addAll(uniqueRepairBlocks.values());
        return repairBlocks;
    }

    private void performAutoArenaRepair(FfaArena arena,
                                        ArenaSnapshot baselineSnapshot,
                                        List<BlockSnapshot> changedBlocks) {
        if (arena == null || baselineSnapshot == null || changedBlocks == null || changedBlocks.isEmpty()) {
            return;
        }

        ArenaSnapshot changedAreaSnapshot = createChangedAreaSnapshot(baselineSnapshot, changedBlocks);
        if (changedAreaSnapshot == null) {
            return;
        }

        FfaArena.ArenaState previousState = arena.getState();
        arena.setState(FfaArena.ArenaState.RESETTING);
        boolean success = rollbackArena(changedAreaSnapshot, null, arena, changedBlocks);
        if (!success) {
            arena.setEnabled(false);
            arena.setState(FfaArena.ArenaState.DISABLED);
            saveArena(arena);
            plugin.getLogger().warning("Disabled FFA arena " + arena.getId() + " because auto-repair failed.");
            return;
        }

        mergeArenaBaselineBlocks(arena.getId(), baselineSnapshot, changedBlocks);
        restoreArenaStateAfterRepair(arena, previousState);
    }

    private ArenaSnapshot createChangedAreaSnapshot(ArenaSnapshot baselineSnapshot, List<BlockSnapshot> changedBlocks) {
        if (baselineSnapshot == null || changedBlocks == null || changedBlocks.isEmpty()) {
            return null;
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockSnapshot blockSnapshot : changedBlocks) {
            minX = Math.min(minX, blockSnapshot.x());
            maxX = Math.max(maxX, blockSnapshot.x());
            minY = Math.min(minY, blockSnapshot.y());
            maxY = Math.max(maxY, blockSnapshot.y());
            minZ = Math.min(minZ, blockSnapshot.z());
            maxZ = Math.max(maxZ, blockSnapshot.z());
        }

        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return null;
        }

        int padding = 0;
        minX = Math.max(baselineSnapshot.minX(), minX - padding);
        maxX = Math.min(baselineSnapshot.maxX(), maxX + padding);
        minY = Math.max(baselineSnapshot.minY(), minY - padding);
        maxY = Math.min(baselineSnapshot.maxY(), maxY + padding);
        minZ = Math.max(baselineSnapshot.minZ(), minZ - padding);
        maxZ = Math.min(baselineSnapshot.maxZ(), maxZ + padding);

        return new ArenaSnapshot(
                baselineSnapshot.worldName(),
                minX,
                maxX,
                minY,
                maxY,
                minZ,
                maxZ,
                new ArrayList<>(changedBlocks)
        );
    }

    private boolean hasPlayersInsideSnapshot(ArenaSnapshot snapshot) {
        if (snapshot == null) {
            return false;
        }

        World world = Bukkit.getWorld(snapshot.worldName());
        if (world == null) {
            return false;
        }

        for (Player player : world.getPlayers()) {
            if (player != null && player.isOnline() && snapshot.contains(player.getLocation())) {
                return true;
            }
        }

        return false;
    }

    private SnapshotSurface findWorldSurface(World world, int minY, int maxY, int x, int z) {
        if (world == null) {
            return null;
        }

        for (int y = maxY; y >= minY; y--) {
            Block block = world.getBlockAt(x, y, z);
            if (isSnapshotSolid(block.getType())) {
                return new SnapshotSurface(y, block.getBlockData().getAsString());
            }
        }

        return null;
    }

    private SnapshotRepairTarget resolveWorldSurfaceRepairTarget(ArenaRegionBounds bounds,
                                                                 SnapshotSurface[][] surfaceMap,
                                                                 int x,
                                                                 int z,
                                                                 int currentSurfaceY) {
        if (bounds == null || surfaceMap == null) {
            return null;
        }

        for (int radius = 1; radius <= MAX_SNAPSHOT_REPAIR_RADIUS; radius++) {
            List<SnapshotSurface> neighbors = collectWorldNeighborSurfaces(bounds, surfaceMap, x, z, radius);
            if (neighbors.size() < Math.max(3, radius + 2)) {
                continue;
            }

            List<SnapshotSurface> higherNeighbors = new ArrayList<>();
            for (SnapshotSurface neighbor : neighbors) {
                if (neighbor.y() >= currentSurfaceY + 1) {
                    higherNeighbors.add(neighbor);
                }
            }

            int requiredHigherNeighbors = Math.max(3, (int) Math.ceil(neighbors.size() * 0.55D));
            if (higherNeighbors.size() < requiredHigherNeighbors) {
                continue;
            }

            List<Integer> higherNeighborHeights = new ArrayList<>(higherNeighbors.size());
            for (SnapshotSurface higherNeighbor : higherNeighbors) {
                higherNeighborHeights.add(higherNeighbor.y());
            }
            higherNeighborHeights.sort(Integer::compareTo);
            int targetSurfaceY = higherNeighborHeights.get(higherNeighborHeights.size() / 2);
            if (targetSurfaceY <= currentSurfaceY) {
                continue;
            }

            return new SnapshotRepairTarget(
                    targetSurfaceY,
                    resolveDominantNeighborSurfaceData(higherNeighbors)
            );
        }

        return null;
    }

    private List<SnapshotSurface> collectWorldNeighborSurfaces(ArenaRegionBounds bounds,
                                                               SnapshotSurface[][] surfaceMap,
                                                               int x,
                                                               int z,
                                                               int radius) {
        List<SnapshotSurface> neighbors = new ArrayList<>();
        if (bounds == null || surfaceMap == null || radius < 1) {
            return neighbors;
        }

        for (int offsetX = -radius; offsetX <= radius; offsetX++) {
            for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                if (offsetX == 0 && offsetZ == 0) {
                    continue;
                }

                int targetX = x + offsetX;
                int targetZ = z + offsetZ;
                if (targetX < bounds.minX()
                        || targetX > bounds.maxX()
                        || targetZ < bounds.minZ()
                        || targetZ > bounds.maxZ()) {
                    continue;
                }

                SnapshotSurface neighbor = surfaceMap[targetX - bounds.minX()][targetZ - bounds.minZ()];
                if (neighbor != null) {
                    neighbors.add(neighbor);
                }
            }
        }

        return neighbors;
    }

    private void restoreArenaStateAfterRepair(FfaArena arena, FfaArena.ArenaState previousState) {
        if (arena == null) {
            return;
        }

        if (!arena.isEnabled()) {
            arena.setState(FfaArena.ArenaState.DISABLED);
            return;
        }

        if (hasPendingResetForArena(arena.getId())) {
            arena.setState(FfaArena.ArenaState.RESETTING);
            return;
        }

        if (previousState == FfaArena.ArenaState.RESERVED
                || reservedArenaIds.contains(arena.getId())
                || hasArenaOccupants(arena.getId())) {
            arena.setState(FfaArena.ArenaState.RESERVED);
            return;
        }

        arena.setState(FfaArena.ArenaState.READY);
    }

    private void finalizePendingReset(FfaMatch match) {
        if (match == null) {
            return;
        }

        pendingResetMatches.remove(match.getId());
        pendingResetEarliestAt.remove(match.getId());
        combatLockedPlayers.remove(match.getPlayerOneUuid());
        combatLockedPlayers.remove(match.getPlayerTwoUuid());
        resetArena(match);
        resumeWaitingArena(match.getArena(), match);
    }

    private void scheduleReturn(UUID uuid, Location location, long delayTicks) {
        if (uuid == null || location == null || location.getWorld() == null) {
            return;
        }

        plugin.getPaperScheduler().runGlobalLater(() -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                pendingJoinLocations.put(uuid, location.clone());
                transitionTitles.remove(uuid);
                transitionStates.remove(uuid);
                transitioningPlayers.remove(uuid);
                return;
            }

            plugin.getPaperScheduler().runEntity(player, () -> {
                if (!player.isOnline()) {
                    pendingJoinLocations.put(uuid, location.clone());
                    transitionTitles.remove(uuid);
                    transitionStates.remove(uuid);
                    transitioningPlayers.remove(uuid);
                    return;
                }

                teleportPlayer(player, location, () -> {
                    player.setNoDamageTicks(60);
                    restoreTransitionState(player);
                    updateCombatLock(uuid);
                });
            });
        }, delayTicks);
    }

    private void resetArena(FfaMatch match) {
        if (match == null) {
            return;
        }

        ArenaSnapshot snapshot = arenaSnapshots.remove(match.getId());
        resetArena(match.getArena(), snapshot, match);
    }

    private void resetArena(FfaArena arena, ArenaSnapshot snapshot, FfaMatch match) {
        if (arena == null) {
            return;
        }

        arena.setState(FfaArena.ArenaState.RESETTING);
        ArenaSnapshot effectiveSnapshot = snapshot;
        if (effectiveSnapshot == null && shouldRollbackArena()) {
            effectiveSnapshot = getArenaBaselineSnapshot(arena);
        }
        boolean success = rollbackArena(effectiveSnapshot, match, arena);
        reservedArenaIds.remove(arena.getId());

        if (success) {
            arena.setState(arena.isEnabled() ? FfaArena.ArenaState.READY : FfaArena.ArenaState.DISABLED);
            return;
        }

        arena.setEnabled(false);
        arena.setState(FfaArena.ArenaState.DISABLED);
        saveArena(arena);
        plugin.getLogger().warning("Disabled FFA arena " + arena.getId() + " because reset failed.");
    }

    private void resumeWaitingArena(FfaArena arena, FfaMatch recentlyResetMatch) {
        if (arena == null) {
            return;
        }

        if (!arena.isEnabled()) {
            reservedArenaIds.remove(arena.getId());
            arena.setState(FfaArena.ArenaState.DISABLED);
            releaseWaitingEntriesForArena(arena);
            return;
        }

        reservedArenaIds.add(arena.getId());
        arena.setState(FfaArena.ArenaState.RESERVED);
        restoreWaitingPlayersToArena(arena);

        if (!hasWaitingEntriesForArena(arena.getId())) {
            reservedArenaIds.remove(arena.getId());
            arena.setState(arena.isEnabled() ? FfaArena.ArenaState.READY : FfaArena.ArenaState.DISABLED);
        }
    }

    private void restoreWaitingPlayersToArena(FfaArena arena) {
        if (arena == null) {
            return;
        }

        Location avoidLocation = null;
        for (Map.Entry<UUID, WaitingArenaEntry> storedEntry : new ArrayList<>(waitingArenaEntries.entrySet())) {
            UUID uuid = storedEntry.getKey();
            WaitingArenaEntry entry = storedEntry.getValue();
            if (entry == null
                    || entry.arena() == null
                    || !arena.getId().equalsIgnoreCase(entry.arena().getId())) {
                continue;
            }

            Player player = Bukkit.getPlayer(uuid);
            if (!canWaitingPlayerStay(player, entry)) {
                continue;
            }

            Location destination = resolveWaitingRestoreLocation(entry, avoidLocation);
            if (destination == null || destination.getWorld() == null) {
                continue;
            }

            if (!isSameBlock(player.getLocation(), destination)) {
                boolean wasSneaking = player.isSneaking();
                boolean wasSprinting = player.isSprinting();
                Location orientedDestination = applyPlayerOrientation(destination, player);
                teleportPlayer(player, orientedDestination, () -> {
                    restorePlayerResetPose(player, orientedDestination, wasSneaking, wasSprinting);
                    player.setNoDamageTicks(60);
                    player.setFallDistance(0F);
                    player.setFireTicks(0);
                });
                destination = orientedDestination;
            }

            waitingArenaEntries.put(uuid, new WaitingArenaEntry(
                    entry.arena(),
                    entry.playerName(),
                    entry.snapshot(),
                    entry.arenaSnapshot(),
                    destination.clone()
            ));
            applyArenaRules(player, arena);
            avoidLocation = destination;
        }
    }

    private void applyArenaRules(Player player, FfaArena arena) {
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

    private void syncArenaRulesForOccupants(FfaArena arena) {
        if (arena == null) {
            return;
        }

        Set<UUID> occupantUuids = new HashSet<>();
        for (Map.Entry<UUID, WaitingArenaEntry> entry : waitingArenaEntries.entrySet()) {
            if (entry.getValue() != null
                    && entry.getValue().arena() != null
                    && arena.getId().equalsIgnoreCase(entry.getValue().arena().getId())) {
                occupantUuids.add(entry.getKey());
            }
        }

        for (FfaMatch match : activeMatches.values()) {
            if (match == null || match.getArena() == null || !arena.getId().equalsIgnoreCase(match.getArena().getId())) {
                continue;
            }
            occupantUuids.add(match.getPlayerOneUuid());
            occupantUuids.add(match.getPlayerTwoUuid());
        }

        for (UUID uuid : occupantUuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                applyArenaRules(player, arena);
            }
        }
    }

    private void syncArenaRulesForAllOccupants() {
        Set<String> syncedArenaIds = new HashSet<>();
        for (FfaArena arena : arenas.values()) {
            if (arena == null || !syncedArenaIds.add(arena.getId())) {
                continue;
            }
            syncArenaRulesForOccupants(arena);
        }
    }

    private Location resolveWaitingRestoreLocation(WaitingArenaEntry entry, Location occupiedLocation) {
        if (entry == null || entry.arena() == null || entry.waitingLocation() == null) {
            return null;
        }

        Location exactColumn = findSameColumnWaitingRestoreLocation(entry);
        if (exactColumn != null && isValidWaitingRestoreLocation(exactColumn, occupiedLocation)) {
            return exactColumn;
        }

        Location nearby = findNearbyWaitingRestoreLocation(entry, occupiedLocation);
        if (nearby != null) {
            return nearby;
        }

        return resolveWaitingReentryLocation(entry, null);
    }

    private Location findSameColumnWaitingRestoreLocation(WaitingArenaEntry entry) {
        if (entry == null || entry.arena() == null || entry.waitingLocation() == null) {
            return null;
        }

        ArenaRegionBounds bounds = resolveArenaRegionBounds(entry.arena());
        Location waitingLocation = entry.waitingLocation();
        if (bounds == null
                || waitingLocation.getWorld() == null
                || !bounds.world().getName().equalsIgnoreCase(waitingLocation.getWorld().getName())) {
            return null;
        }

        int baseX = waitingLocation.getBlockX();
        int baseZ = waitingLocation.getBlockZ();
        if (baseX < bounds.minX() || baseX > bounds.maxX() || baseZ < bounds.minZ() || baseZ > bounds.maxZ()) {
            return null;
        }

        Location standingSpot = findSafeStandingLocation(bounds.world(), baseX, baseZ, bounds.minY(), bounds.maxY());
        if (standingSpot == null || !isWithinArenaBounds(standingSpot, bounds)) {
            return null;
        }

        Location exact = waitingLocation.clone();
        exact.setY(standingSpot.getY());
        return exact;
    }

    private Location findNearbyWaitingRestoreLocation(WaitingArenaEntry entry, Location occupiedLocation) {
        if (entry == null || entry.arena() == null || entry.waitingLocation() == null) {
            return null;
        }

        ArenaRegionBounds bounds = resolveArenaRegionBounds(entry.arena());
        Location waitingLocation = entry.waitingLocation();
        if (bounds == null
                || waitingLocation.getWorld() == null
                || !bounds.world().getName().equalsIgnoreCase(waitingLocation.getWorld().getName())) {
            return null;
        }

        int baseX = waitingLocation.getBlockX();
        int baseZ = waitingLocation.getBlockZ();
        for (int radius = 1; radius <= 3; radius++) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                    int x = baseX + offsetX;
                    int z = baseZ + offsetZ;
                    if (x < bounds.minX() || x > bounds.maxX() || z < bounds.minZ() || z > bounds.maxZ()) {
                        continue;
                    }

                    Location candidate = findSafeStandingLocation(bounds.world(), x, z, bounds.minY(), bounds.maxY());
                    if (candidate == null
                            || !isWithinArenaBounds(candidate, bounds)
                            || !isValidWaitingRestoreLocation(candidate, occupiedLocation)) {
                        continue;
                    }

                    Location restored = waitingLocation.clone();
                    restored.setX(candidate.getX());
                    restored.setY(candidate.getY());
                    restored.setZ(candidate.getZ());
                    return restored;
                }
            }
        }

        return null;
    }

    private boolean isValidWaitingRestoreLocation(Location candidate, Location occupiedLocation) {
        return candidate != null && (occupiedLocation == null || !isSameBlock(candidate, occupiedLocation));
    }

    private void prepareTransition(UUID uuid) {
        if (uuid == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        transitioningPlayers.add(uuid);
        transitionStates.putIfAbsent(uuid, new TransitionPlayerState(
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
            transitionTitles.remove(player.getUniqueId());
            transitioningPlayers.remove(player.getUniqueId());
            player.clearTitle();
            clearTemporaryVanish(player);
            return;
        }

        if (player.getGameMode() != state.gameMode()) {
            player.setGameMode(state.gameMode());
        }
        player.setAllowFlight(state.allowFlight());
        player.setFlying(state.allowFlight() && state.flying());
        player.setInvulnerable(state.invulnerable());
        player.setCollidable(state.collidable());
        transitionTitles.remove(player.getUniqueId());
        transitioningPlayers.remove(player.getUniqueId());
        player.clearTitle();
        clearTemporaryVanish(player);
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

        long stayMillis = Math.max(1000L, getReturnDelayTicks() * 50L);
        player.showTitle(Title.title(
                ColorUtils.toComponent(state.title()),
                ColorUtils.toComponent(state.subtitle()),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(stayMillis), Duration.ZERO)
        ));
    }

    private void applyTemporaryVanish(Player hidden) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.getUniqueId().equals(hidden.getUniqueId())) {
                viewer.hidePlayer(plugin, hidden);
            }
        }
    }

    private void clearTemporaryVanish(Player shown) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.getUniqueId().equals(shown.getUniqueId())) {
                viewer.showPlayer(plugin, shown);
            }
        }
    }

    private void showAllVanishedPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            clearTemporaryVanish(player);
        }
    }

    private void clearPotionEffects(Player player) {
        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }
    }

    private void healPlayerForMatch(Player player) {
        if (player == null) {
            return;
        }

        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH) == null
                ? 20D
                : player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(maxHealth);
        player.setFoodLevel(20);
        player.setSaturation(20F);
        player.setExhaustion(0F);
        player.setFireTicks(0);
        player.setFallDistance(0F);
    }

    private boolean canEnterFfa(Player player, boolean selfFeedback) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (isInMatch(uuid)) {
            if (selfFeedback) {
                send(player, "&cYou are already in FFA.");
            }
            return false;
        }
        if (isInQueue(uuid)) {
            if (selfFeedback) {
                send(player, "&cYou are already inside an FFA arena.");
            }
            return false;
        }
        if (isTransitioning(uuid)) {
            if (selfFeedback) {
                send(player, "&cYou are still finishing your previous FFA session.");
            }
            return false;
        }
        if (plugin.getDuelManager() != null
                && (plugin.getDuelManager().isInDuel(uuid)
                || plugin.getDuelManager().isInQueue(uuid)
                || plugin.getDuelManager().isTransitioning(uuid))) {
            if (selfFeedback) {
                send(player, "&cYou cannot join FFA while using the duel system.");
            }
            return false;
        }
        if (plugin.getCombatManager() != null && plugin.getCombatManager().isInCombat(uuid)) {
            if (selfFeedback) {
                send(player, "&cYou cannot join FFA while combat tagged.");
            }
            return false;
        }
        if (plugin.getTeleportManager() != null && plugin.getTeleportManager().hasPending(uuid)) {
            if (selfFeedback) {
                send(player, "&cYou cannot join FFA while another teleport is pending.");
            }
            return false;
        }
        return true;
    }

    private FfaArena findJoinableArena() {
        FfaArena fallbackArena = null;
        for (FfaArena arena : getArenas()) {
            if (arena == null
                    || !arena.isEnabled()
                    || !arena.isConfigured()
                    || arena.getState() == FfaArena.ArenaState.DISABLED
                    || arena.getState() == FfaArena.ArenaState.RESETTING
                    || hasPendingResetForArena(arena.getId())) {
                continue;
            }

            if (hasArenaOccupants(arena.getId()) || reservedArenaIds.contains(arena.getId()) || arena.getState() == FfaArena.ArenaState.RESERVED) {
                return arena;
            }

            if (fallbackArena == null) {
                fallbackArena = arena;
            }
        }
        return fallbackArena;
    }

    private FfaMatch getActiveMatch(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        Long matchId = activeMatchIds.get(uuid);
        return matchId == null ? null : activeMatches.get(matchId);
    }

    private Location resolveReturnLocation(FfaMatch match, UUID uuid) {
        FfaPlayerSnapshot snapshot = match == null ? null : match.getSnapshot(uuid);
        FfaArena arena = match == null ? null : match.getArena();
        return resolveSafeFfaExitLocation(arena, snapshot, null);
    }

    private Location resolveLobbyRespawnLocation(FfaMatch match, UUID uuid) {
        return resolveReturnLocation(match, uuid);
    }

    private Location resolveExitLocation(FfaMatch match, UUID uuid, String endReason) {
        if ("SURRENDER".equalsIgnoreCase(endReason) || "QUIT".equalsIgnoreCase(endReason)) {
            return resolveLobbyRespawnLocation(match, uuid);
        }
        return resolveReturnLocation(match, uuid);
    }

    private void clearMatchSpawnProtection(UUID uuid) {
        if (uuid == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        player.setInvulnerable(false);
        player.setCollidable(true);
        player.setNoDamageTicks(0);
        player.setFallDistance(0F);
    }

    private Location resolveWaitingExitLocation(WaitingArenaEntry entry) {
        FfaArena arena = entry == null ? null : entry.arena();
        FfaPlayerSnapshot snapshot = entry == null ? null : entry.snapshot();
        return resolveSafeFfaExitLocation(arena, snapshot, null);
    }

    private Location resolveSafeFfaExitLocation(FfaArena arena, FfaPlayerSnapshot snapshot, Location contextLocation) {
        Location pluginSpawn = plugin.getSpawnManager().hasSpawn() ? plugin.getSpawnManager().getSpawnLocation() : null;
        if (isValidExitLocation(pluginSpawn)) {
            return pluginSpawn.clone();
        }

        Location arenaReturn = arena == null ? null : arena.getReturnLocation();
        if (isValidExitLocation(arenaReturn) && !isInsideAnyFfaArena(arenaReturn)) {
            return arenaReturn.clone();
        }

        Location snapshotReturn = snapshot == null ? null : snapshot.getReturnLocation();
        if (isValidExitLocation(snapshotReturn) && !isInsideAnyFfaArena(snapshotReturn)) {
            return snapshotReturn.clone();
        }

        Location contextWorldSpawn = resolveWorldSpawn(contextLocation);
        if (isValidExitLocation(contextWorldSpawn)) {
            return contextWorldSpawn.clone();
        }

        Location arenaWorldSpawn = resolveArenaWorldSpawn(arena);
        if (isValidExitLocation(arenaWorldSpawn)) {
            return arenaWorldSpawn.clone();
        }

        if (!Bukkit.getWorlds().isEmpty()) {
            Location firstWorldSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
            if (isValidExitLocation(firstWorldSpawn)) {
                return firstWorldSpawn.clone();
            }
        }
        return null;
    }

    private boolean isValidExitLocation(Location location) {
        return location != null && location.getWorld() != null;
    }

    private Location resolveWorldSpawn(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return location.getWorld().getSpawnLocation();
    }

    private Location resolveArenaWorldSpawn(FfaArena arena) {
        if (arena == null) {
            return null;
        }

        Location returnLocation = arena.getReturnLocation();
        if (returnLocation != null && returnLocation.getWorld() != null) {
            return returnLocation.getWorld().getSpawnLocation();
        }

        Location regionPos = arena.getRegionPos1();
        if (regionPos != null && regionPos.getWorld() != null) {
            return regionPos.getWorld().getSpawnLocation();
        }

        Location spawn = arena.getSpawn1();
        if (spawn != null && spawn.getWorld() != null) {
            return spawn.getWorld().getSpawnLocation();
        }
        return null;
    }

    private boolean isInsideAnyFfaArena(Location location) {
        return findArenaContainingLocation(location) != null;
    }

    private Location resolveServerJoinFallbackLocation(Location currentLocation) {
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

    private ArenaSnapshot getArenaBaselineSnapshot(FfaArena arena) {
        if (arena == null) {
            return null;
        }

        ArenaSnapshot snapshot = arenaBaselineSnapshots.get(arena.getId());
        if (snapshot != null) {
            return snapshot;
        }

        snapshot = captureArenaSnapshot(arena);
        if (snapshot != null) {
            snapshot = repairArenaSnapshot(snapshot);
            arenaBaselineSnapshots.put(arena.getId(), snapshot);
        }
        return snapshot;
    }

    private void invalidateArenaBaseline(String arenaId) {
        if (arenaId != null && !arenaId.isBlank()) {
            arenaBaselineSnapshots.remove(arenaId);
        }
    }

    private void mergeArenaBaselineBlocks(String arenaId,
                                          ArenaSnapshot baselineSnapshot,
                                          List<BlockSnapshot> updatedBlocks) {
        if (arenaId == null
                || arenaId.isBlank()
                || baselineSnapshot == null
                || updatedBlocks == null
                || updatedBlocks.isEmpty()) {
            return;
        }

        Map<Long, String> blockDataByPosition = new HashMap<>(baselineSnapshot.blocks().size() * 2);
        for (BlockSnapshot blockSnapshot : baselineSnapshot.blocks()) {
            blockDataByPosition.put(blockKey(blockSnapshot.x(), blockSnapshot.y(), blockSnapshot.z()), blockSnapshot.blockDataString());
        }
        for (BlockSnapshot blockSnapshot : updatedBlocks) {
            blockDataByPosition.put(blockKey(blockSnapshot.x(), blockSnapshot.y(), blockSnapshot.z()), blockSnapshot.blockDataString());
        }

        List<BlockSnapshot> mergedBlocks = new ArrayList<>(baselineSnapshot.blocks().size());
        for (BlockSnapshot blockSnapshot : baselineSnapshot.blocks()) {
            mergedBlocks.add(new BlockSnapshot(
                    blockSnapshot.x(),
                    blockSnapshot.y(),
                    blockSnapshot.z(),
                    blockDataByPosition.getOrDefault(
                            blockKey(blockSnapshot.x(), blockSnapshot.y(), blockSnapshot.z()),
                            blockSnapshot.blockDataString()
                    )
            ));
        }

        arenaBaselineSnapshots.put(arenaId, new ArenaSnapshot(
                baselineSnapshot.worldName(),
                baselineSnapshot.minX(),
                baselineSnapshot.maxX(),
                baselineSnapshot.minY(),
                baselineSnapshot.maxY(),
                baselineSnapshot.minZ(),
                baselineSnapshot.maxZ(),
                mergedBlocks
        ));
    }

    private Location resolveWaitingReentryLocation(WaitingArenaEntry entry, Location avoidLocation) {
        if (entry == null || entry.arena() == null) {
            return null;
        }

        Location exact = findExactWaitingReentryLocation(entry, avoidLocation);
        if (exact != null) {
            return exact;
        }

        Location preferred = findSafeWaitingReentryLocation(entry, avoidLocation);
        if (preferred != null) {
            return preferred;
        }

        return findArenaSpawn(entry.arena(), avoidLocation);
    }

    private Location findExactWaitingReentryLocation(WaitingArenaEntry entry, Location avoidLocation) {
        if (entry == null || entry.arena() == null || entry.waitingLocation() == null) {
            return null;
        }

        ArenaRegionBounds bounds = resolveArenaRegionBounds(entry.arena());
        Location waitingLocation = entry.waitingLocation();
        if (bounds == null
                || waitingLocation.getWorld() == null
                || !bounds.world().getName().equalsIgnoreCase(waitingLocation.getWorld().getName())) {
            return null;
        }

        Location normalizedWaiting = normalizeArenaJoinAnchor(waitingLocation);
        if (isSafeExactStandingLocation(normalizedWaiting, bounds)
                && isValidArenaSpawn(normalizedWaiting, avoidLocation)) {
            return normalizedWaiting;
        }

        int baseX = waitingLocation.getBlockX();
        int baseZ = waitingLocation.getBlockZ();
        if (baseX < bounds.minX() || baseX > bounds.maxX() || baseZ < bounds.minZ() || baseZ > bounds.maxZ()) {
            return null;
        }

        int preferredGroundY = waitingLocation.getBlockY() - 1;
        int minY = Math.max(bounds.minY(), preferredGroundY - 3);
        int maxY = Math.min(bounds.maxY(), preferredGroundY + 3);
        if (maxY < minY) {
            return null;
        }

        Location standingSpot = findSafeStandingLocation(bounds.world(), baseX, baseZ, minY, maxY);
        if (standingSpot == null || !isWithinArenaBounds(standingSpot, bounds)) {
            return null;
        }

        Location exactColumn = normalizedWaiting.clone();
        exactColumn.setY(standingSpot.getY());
        if (isSafeExactStandingLocation(exactColumn, bounds)
                && isValidArenaSpawn(exactColumn, avoidLocation)) {
            return exactColumn;
        }

        return null;
    }

    private Location findSafeWaitingReentryLocation(WaitingArenaEntry entry, Location avoidLocation) {
        if (entry == null || entry.arena() == null || entry.waitingLocation() == null) {
            return null;
        }

        ArenaRegionBounds bounds = resolveArenaRegionBounds(entry.arena());
        Location waitingLocation = entry.waitingLocation();
        if (bounds == null
                || waitingLocation.getWorld() == null
                || !bounds.world().getName().equalsIgnoreCase(waitingLocation.getWorld().getName())) {
            return null;
        }

        int baseX = waitingLocation.getBlockX();
        int baseZ = waitingLocation.getBlockZ();
        int preferredGroundY = waitingLocation.getBlockY() - 1;
        int minY = Math.max(bounds.minY(), preferredGroundY - 3);
        int maxY = Math.min(bounds.maxY(), preferredGroundY + 3);
        if (maxY < minY) {
            return null;
        }

        for (int radius = 0; radius <= 3; radius++) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                    int x = baseX + offsetX;
                    int z = baseZ + offsetZ;
                    if (x < bounds.minX() || x > bounds.maxX() || z < bounds.minZ() || z > bounds.maxZ()) {
                        continue;
                    }

                    Location candidate = findSafeStandingLocation(bounds.world(), x, z, minY, maxY);
                    if (candidate != null
                            && isWithinArenaBounds(candidate, bounds)
                            && isValidArenaSpawn(candidate, avoidLocation)) {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    private Location findArenaHoldLocation(FfaArena arena, ArenaSnapshot snapshot, Location avoidLocation) {
        Location hold = findArenaSpawn(arena, avoidLocation);
        if (hold != null && hold.getWorld() != null) {
            return hold;
        }
        if (snapshot == null) {
            return null;
        }
        World world = Bukkit.getWorld(snapshot.worldName());
        if (world == null) {
            return null;
        }
        double centerX = ((double) snapshot.minX() + snapshot.maxX()) / 2.0D + 0.5D;
        double centerZ = ((double) snapshot.minZ() + snapshot.maxZ()) / 2.0D + 0.5D;
        return new Location(world, centerX, snapshot.maxY() + 1.0D, centerZ, 0F, 0F);
    }

    private String resolveParticipantName(FfaMatch match, UUID uuid) {
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

    private boolean hasUsableTotem(Player player) {
        return player != null
                && (player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
                || player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING);
    }

    private String formatResultTitle(String key, String playerName, String opponentName, String fallback) {
        String raw = config().getString("RESULT-TITLES." + key + ".title", fallback);
        return applyResultPlaceholders(raw, playerName, opponentName);
    }

    private String formatResultSubtitle(String key, String playerName, String opponentName, String fallback) {
        String raw = config().getString("RESULT-TITLES." + key + ".subtitle", fallback);
        return applyResultPlaceholders(raw, playerName, opponentName);
    }

    private String applyResultPlaceholders(String text, String playerName, String opponentName) {
        String resolved = text == null ? "" : text;
        resolved = resolved.replace("<player>", playerName == null ? "Player" : playerName);
        resolved = resolved.replace("<opponent>", opponentName == null ? "Opponent" : opponentName);
        return resolved;
    }

    private String formatDuration(long totalSeconds) {
        long safeSeconds = Math.max(0L, totalSeconds);
        long minutes = safeSeconds / 60L;
        long seconds = safeSeconds % 60L;
        return minutes + "m " + seconds + "s";
    }

    private String buildQueueUnavailableMessage() {
        List<String> notReady = new ArrayList<>();
        for (FfaArena arena : getArenas()) {
            if (arena.isEnabled() && !arena.isReady()) {
                notReady.add(arena.getId());
            }
        }

        if (!notReady.isEmpty()) {
            return "&cFFA arenas exist but are not ready yet. &7Check: &f" + String.join("&7, &f", notReady) + "&7.";
        }
        boolean hasResettingArena = false;
        for (FfaArena arena : getArenas()) {
            if (arena != null && hasPendingResetForArena(arena.getId())) {
                hasResettingArena = true;
                break;
            }
        }
        if (hasResettingArena) {
            return "&cFFA arena is resetting right now. Try again in a moment.";
        }
        return "&cNo ready FFA arenas are configured yet.";
    }

    private boolean hasArenaOccupants(String arenaId) {
        if (arenaId == null || arenaId.isBlank()) {
            return false;
        }

        for (WaitingArenaEntry entry : waitingArenaEntries.values()) {
            if (entry != null && entry.arena() != null && arenaId.equalsIgnoreCase(entry.arena().getId())) {
                return true;
            }
        }

        for (FfaMatch match : activeMatches.values()) {
            if (match != null && match.getArena() != null && arenaId.equalsIgnoreCase(match.getArena().getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasActiveMatchForArena(String arenaId) {
        if (arenaId == null || arenaId.isBlank()) {
            return false;
        }

        for (FfaMatch match : activeMatches.values()) {
            if (match != null && match.getArena() != null && arenaId.equalsIgnoreCase(match.getArena().getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPendingResetForArena(String arenaId) {
        if (arenaId == null || arenaId.isBlank()) {
            return false;
        }

        for (FfaMatch match : pendingResetMatches.values()) {
            if (match != null && match.getArena() != null && arenaId.equalsIgnoreCase(match.getArena().getId())) {
                return true;
            }
        }
        return false;
    }

    private void finishWaitingEntry(UUID uuid,
                                    WaitingArenaEntry entry,
                                    String feedbackMessage,
                                    boolean restorePlayerState,
                                    boolean returnToSnapshotLocation) {
        finishWaitingEntry(uuid, entry, feedbackMessage, restorePlayerState, returnToSnapshotLocation, null);
    }

    private void finishWaitingEntry(UUID uuid,
                                    WaitingArenaEntry entry,
                                    String feedbackMessage,
                                    boolean restorePlayerState,
                                    boolean returnToSnapshotLocation,
                                    Runnable afterFinish) {
        if (uuid == null || entry == null) {
            runAfterWaitingFinish(afterFinish);
            return;
        }

        if (restorePlayerState && entry.snapshot() != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                pendingJoinSnapshots.put(uuid, entry.snapshot());
                Location returnLocation = returnToSnapshotLocation
                        ? resolveWaitingExitLocation(entry)
                        : null;
                if (returnToSnapshotLocation && returnLocation != null && returnLocation.getWorld() != null) {
                    pendingJoinLocations.put(uuid, returnLocation);
                } else {
                    pendingJoinLocations.remove(uuid);
                }
                transitionTitles.remove(uuid);
                transitionStates.remove(uuid);
                transitioningPlayers.remove(uuid);
                runAfterWaitingFinish(afterFinish);
                return;
            }

            restoreTransitionState(player);
            restoreSnapshot(player, entry.snapshot());
            Location returnLocation = returnToSnapshotLocation
                    ? resolveWaitingExitLocation(entry)
                    : null;
            Runnable complete = () -> {
                player.setNoDamageTicks(60);
                player.setFallDistance(0F);
                player.setFireTicks(0);
                if (feedbackMessage != null && !feedbackMessage.isBlank()) {
                    send(player, feedbackMessage);
                }
                runAfterWaitingFinish(afterFinish);
            };
            if (returnToSnapshotLocation && returnLocation != null && returnLocation.getWorld() != null) {
                teleportPlayer(player, returnLocation, complete);
            } else {
                complete.run();
            }
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline() && feedbackMessage != null && !feedbackMessage.isBlank()) {
            send(player, feedbackMessage);
        }
        runAfterWaitingFinish(afterFinish);
    }

    private void runAfterWaitingFinish(Runnable afterFinish) {
        if (afterFinish != null) {
            afterFinish.run();
        }
    }

    private void releaseWaitingEntriesForArena(FfaArena arena) {
        if (arena == null) {
            return;
        }

        String arenaId = arena.getId();
        for (Map.Entry<UUID, WaitingArenaEntry> storedEntry : new ArrayList<>(waitingArenaEntries.entrySet())) {
            WaitingArenaEntry entry = storedEntry.getValue();
            if (entry == null || entry.arena() == null || !arenaId.equalsIgnoreCase(entry.arena().getId())) {
                continue;
            }

            UUID uuid = storedEntry.getKey();
            waitingPlayers.remove(uuid);
            waitingArenaEntries.remove(uuid);
            finishWaitingEntry(uuid, entry, null, true, false);
        }
    }

    private void clearParticipantCombatState(UUID uuid) {
        if (uuid == null) {
            return;
        }

        if (plugin.getCombatManager() != null) {
            plugin.getCombatManager().clearTag(uuid);
        }
        if (plugin.getRtpZoneManager() != null) {
            plugin.getRtpZoneManager().clearState(uuid);
        }
        combatLockedPlayers.remove(uuid);
    }

    private void updateStatsAfterWin(UUID winnerUuid, UUID loserUuid) {
        FfaStats winnerStats = getStats(winnerUuid).recordWin();
        FfaStats loserStats = getStats(loserUuid).recordLoss();
        statsCache.put(winnerUuid, winnerStats);
        statsCache.put(loserUuid, loserStats);
        saveStats(winnerUuid, winnerStats);
        saveStats(loserUuid, loserStats);
    }

    private void updateStatsAfterDraw(UUID firstUuid, UUID secondUuid) {
        FfaStats firstStats = getStats(firstUuid).recordDraw();
        FfaStats secondStats = getStats(secondUuid).recordDraw();
        statsCache.put(firstUuid, firstStats);
        statsCache.put(secondUuid, secondStats);
        saveStats(firstUuid, firstStats);
        saveStats(secondUuid, secondStats);
    }

    private ArenaSnapshot captureArenaSnapshot(FfaArena arena) {
        ArenaRegionBounds bounds = resolveArenaRegionBounds(arena);
        if (bounds == null) {
            return null;
        }

        World world = bounds.world();
        int minX = bounds.minX();
        int maxX = bounds.maxX();
        int minY = bounds.minY();
        int maxY = bounds.maxY();
        int minZ = bounds.minZ();
        int maxZ = bounds.maxZ();

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

    private ArenaSnapshot repairArenaSnapshot(ArenaSnapshot snapshot) {
        if (snapshot == null || snapshot.blocks().isEmpty()) {
            return snapshot;
        }

        Map<Long, String> blockDataByPosition = new HashMap<>(snapshot.blocks().size() * 2);
        for (BlockSnapshot blockSnapshot : snapshot.blocks()) {
            blockDataByPosition.put(blockKey(blockSnapshot.x(), blockSnapshot.y(), blockSnapshot.z()), blockSnapshot.blockDataString());
        }

        Map<String, Material> materialCache = new HashMap<>();
        boolean changedAny = false;
        for (int pass = 0; pass < MAX_SNAPSHOT_REPAIR_PASSES; pass++) {
            boolean changedThisPass = false;
            for (int x = snapshot.minX(); x <= snapshot.maxX(); x++) {
                for (int z = snapshot.minZ(); z <= snapshot.maxZ(); z++) {
                    SnapshotSurface currentSurface = findSnapshotSurface(snapshot, blockDataByPosition, materialCache, x, z);
                    int currentSurfaceY = currentSurface == null ? snapshot.minY() - 1 : currentSurface.y();

                    SnapshotRepairTarget repairTarget = resolveSnapshotRepairTarget(
                            snapshot,
                            blockDataByPosition,
                            materialCache,
                            x,
                            z,
                            currentSurfaceY
                    );
                    if (repairTarget == null || repairTarget.surfaceY() <= currentSurfaceY) {
                        continue;
                    }
                    String fillerBlockData = resolveHoleFillBlockData(
                            snapshot,
                            blockDataByPosition,
                            materialCache,
                            x,
                            z,
                            currentSurfaceY,
                            repairTarget.topBlockData()
                    );

                    for (int y = currentSurfaceY + 1; y <= repairTarget.surfaceY(); y++) {
                        long key = blockKey(x, y, z);
                        Material existingMaterial = resolveSnapshotMaterial(blockDataByPosition.get(key), materialCache);
                        if (!shouldRepairHoleBlock(existingMaterial)) {
                            continue;
                        }

                        blockDataByPosition.put(
                                key,
                                y == repairTarget.surfaceY() ? repairTarget.topBlockData() : fillerBlockData
                        );
                        changedThisPass = true;
                    }
                }
            }

            if (!changedThisPass) {
                break;
            }
            changedAny = true;
        }

        if (!changedAny) {
            return snapshot;
        }

        List<BlockSnapshot> repairedBlocks = new ArrayList<>(snapshot.blocks().size());
        for (BlockSnapshot blockSnapshot : snapshot.blocks()) {
            repairedBlocks.add(new BlockSnapshot(
                    blockSnapshot.x(),
                    blockSnapshot.y(),
                    blockSnapshot.z(),
                    blockDataByPosition.getOrDefault(
                            blockKey(blockSnapshot.x(), blockSnapshot.y(), blockSnapshot.z()),
                            blockSnapshot.blockDataString()
                    )
            ));
        }

        return new ArenaSnapshot(
                snapshot.worldName(),
                snapshot.minX(),
                snapshot.maxX(),
                snapshot.minY(),
                snapshot.maxY(),
                snapshot.minZ(),
                snapshot.maxZ(),
                repairedBlocks
        );
    }

    private SnapshotRepairTarget resolveSnapshotRepairTarget(ArenaSnapshot snapshot,
                                                             Map<Long, String> blockDataByPosition,
                                                             Map<String, Material> materialCache,
                                                             int x,
                                                             int z,
                                                             int currentSurfaceY) {
        if (snapshot == null || blockDataByPosition == null || materialCache == null) {
            return null;
        }

        for (int radius = 1; radius <= MAX_SNAPSHOT_REPAIR_RADIUS; radius++) {
            List<SnapshotSurface> neighbors = collectSnapshotNeighborSurfaces(
                    snapshot,
                    blockDataByPosition,
                    materialCache,
                    x,
                    z,
                    radius
            );
            if (neighbors.size() < Math.max(3, radius + 2)) {
                continue;
            }

            List<SnapshotSurface> higherNeighbors = new ArrayList<>();
            for (SnapshotSurface neighbor : neighbors) {
                if (neighbor.y() >= currentSurfaceY + 2) {
                    higherNeighbors.add(neighbor);
                }
            }

            int requiredHigherNeighbors = Math.max(3, (int) Math.ceil(neighbors.size() * 0.45D));
            if (higherNeighbors.size() < requiredHigherNeighbors) {
                continue;
            }

            List<Integer> higherNeighborHeights = new ArrayList<>(higherNeighbors.size());
            for (SnapshotSurface higherNeighbor : higherNeighbors) {
                higherNeighborHeights.add(higherNeighbor.y());
            }
            higherNeighborHeights.sort(Integer::compareTo);
            int targetSurfaceY = higherNeighborHeights.get(higherNeighborHeights.size() / 2);
            if (targetSurfaceY <= currentSurfaceY) {
                continue;
            }

            return new SnapshotRepairTarget(
                    targetSurfaceY,
                    resolveDominantNeighborSurfaceData(higherNeighbors)
            );
        }

        return null;
    }

    private SnapshotSurface findSnapshotSurface(ArenaSnapshot snapshot,
                                                Map<Long, String> blockDataByPosition,
                                                Map<String, Material> materialCache,
                                                int x,
                                                int z) {
        if (snapshot == null || blockDataByPosition == null || materialCache == null) {
            return null;
        }

        for (int y = snapshot.maxY(); y >= snapshot.minY(); y--) {
            String blockData = blockDataByPosition.get(blockKey(x, y, z));
            Material material = resolveSnapshotMaterial(blockData, materialCache);
            if (isSnapshotSolid(material)) {
                return new SnapshotSurface(y, blockData);
            }
        }
        return null;
    }

    private List<SnapshotSurface> collectSnapshotNeighborSurfaces(ArenaSnapshot snapshot,
                                                                  Map<Long, String> blockDataByPosition,
                                                                  Map<String, Material> materialCache,
                                                                  int x,
                                                                  int z,
                                                                  int radius) {
        List<SnapshotSurface> neighbors = new ArrayList<>();
        if (snapshot == null || radius < 1) {
            return neighbors;
        }

        for (int offsetX = -radius; offsetX <= radius; offsetX++) {
            for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                if (offsetX == 0 && offsetZ == 0) {
                    continue;
                }

                int targetX = x + offsetX;
                int targetZ = z + offsetZ;
                if (targetX < snapshot.minX()
                        || targetX > snapshot.maxX()
                        || targetZ < snapshot.minZ()
                        || targetZ > snapshot.maxZ()) {
                    continue;
                }

                SnapshotSurface neighbor = findSnapshotSurface(snapshot, blockDataByPosition, materialCache, targetX, targetZ);
                if (neighbor != null) {
                    neighbors.add(neighbor);
                }
            }
        }
        return neighbors;
    }

    private String resolveDominantNeighborSurfaceData(List<SnapshotSurface> neighbors) {
        Map<String, Integer> frequencies = new HashMap<>();
        String fallback = null;
        for (SnapshotSurface neighbor : neighbors) {
            if (neighbor == null || neighbor.blockDataString() == null || neighbor.blockDataString().isBlank()) {
                continue;
            }
            if (fallback == null) {
                fallback = neighbor.blockDataString();
            }
            frequencies.merge(neighbor.blockDataString(), 1, Integer::sum);
        }

        String dominant = null;
        int bestCount = -1;
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            if (entry.getValue() > bestCount) {
                dominant = entry.getKey();
                bestCount = entry.getValue();
            }
        }

        if (dominant != null) {
            return dominant;
        }
        if (fallback != null) {
            return fallback;
        }
        return Bukkit.createBlockData(Material.DIRT).getAsString();
    }

    private String resolveHoleFillBlockData(ArenaSnapshot snapshot,
                                            Map<Long, String> blockDataByPosition,
                                            Map<String, Material> materialCache,
                                            int x,
                                            int z,
                                            int currentSurfaceY,
                                            String topBlockData) {
        if (snapshot != null && currentSurfaceY >= snapshot.minY()) {
            String existingSurfaceData = blockDataByPosition.get(blockKey(x, currentSurfaceY, z));
            Material existingSurfaceMaterial = resolveSnapshotMaterial(existingSurfaceData, materialCache);
            if (isSnapshotSolid(existingSurfaceMaterial)) {
                return existingSurfaceData;
            }
        }

        return deriveSubsurfaceBlockData(topBlockData, materialCache);
    }

    private String deriveSubsurfaceBlockData(String blockDataString, Map<String, Material> materialCache) {
        Material material = resolveSnapshotMaterial(blockDataString, materialCache);
        if (material == Material.GRASS_BLOCK
                || material == Material.PODZOL
                || material == Material.MYCELIUM
                || material == Material.DIRT_PATH
                || material == Material.ROOTED_DIRT) {
            return Bukkit.createBlockData(Material.DIRT).getAsString();
        }
        return blockDataString != null && !blockDataString.isBlank()
                ? blockDataString
                : Bukkit.createBlockData(Material.DIRT).getAsString();
    }

    private Material resolveSnapshotMaterial(String blockDataString, Map<String, Material> materialCache) {
        if (blockDataString == null || blockDataString.isBlank()) {
            return Material.AIR;
        }
        if (materialCache != null && materialCache.containsKey(blockDataString)) {
            return materialCache.get(blockDataString);
        }

        Material material;
        try {
            material = Bukkit.createBlockData(blockDataString).getMaterial();
        } catch (IllegalArgumentException ignored) {
            material = Material.AIR;
        }

        if (materialCache != null) {
            materialCache.put(blockDataString, material);
        }
        return material;
    }

    private boolean isSnapshotSolid(Material material) {
        return material != null && material.isSolid() && !isHazardous(material);
    }

    private boolean shouldRepairHoleBlock(Material material) {
        return material == null || !material.isSolid() || isHazardous(material);
    }

    private long blockKey(int x, int y, int z) {
        return (((long) x & 0x3FFFFFFL) << 38)
                | (((long) z & 0x3FFFFFFL) << 12)
                | ((long) y & 0xFFFL);
    }

    private boolean rollbackArena(ArenaSnapshot snapshot, FfaMatch match, FfaArena arena) {
        return rollbackArena(snapshot, match, arena, null);
    }

    private boolean rollbackArena(ArenaSnapshot snapshot,
                                  FfaMatch match,
                                  FfaArena arena,
                                  List<BlockSnapshot> changedBlocks) {
        if (!shouldRollbackArena()) {
            return true;
        }

        if (snapshot == null) {
            return false;
        }

        World world = Bukkit.getWorld(snapshot.worldName());
        if (world == null) {
            return false;
        }

        if (changedBlocks == null || changedBlocks.isEmpty()) {
            evacuatePlayersFromResetArea(match, arena, snapshot, world);
        } else {
            evacuatePlayersFromAutoRepairArea(snapshot, world, changedBlocks);
        }

        boolean success = true;
        for (BlockSnapshot blockSnapshot : snapshot.blocks()) {
            Block block = world.getBlockAt(blockSnapshot.x(), blockSnapshot.y(), blockSnapshot.z());
            try {
                BlockData data = Bukkit.createBlockData(blockSnapshot.blockDataString());
                block.setBlockData(data, false);
            } catch (IllegalArgumentException exception) {
                success = false;
                plugin.getLogger().log(Level.WARNING,
                        "Failed to restore FFA arena block at "
                                + blockSnapshot.x() + "," + blockSnapshot.y() + "," + blockSnapshot.z(),
                        exception);
            }
        }

        cleanupTransientEntities(snapshot, world);
        return success;
    }

    private void evacuatePlayersFromAutoRepairArea(ArenaSnapshot snapshot,
                                                   World world,
                                                   List<BlockSnapshot> changedBlocks) {
        if (snapshot == null || world == null || changedBlocks == null || changedBlocks.isEmpty()) {
            return;
        }

        Map<Long, RepairColumn> repairColumns = buildRepairColumns(changedBlocks);
        if (repairColumns.isEmpty()) {
            return;
        }

        for (Player player : world.getPlayers()) {
            if (player == null || !player.isOnline()) {
                continue;
            }

            Location current = player.getLocation();
            RepairColumn repairColumn = repairColumns.get(repairColumnKey(current.getBlockX(), current.getBlockZ()));
            if (repairColumn == null || !shouldEvacuateForAutoRepair(current, repairColumn)) {
                continue;
            }

            Location destination = resolveAutoRepairEvacuationLocation(player, snapshot, world);
            if (destination == null || destination.getWorld() == null) {
                continue;
            }

            boolean wasSneaking = player.isSneaking();
            boolean wasSprinting = player.isSprinting();
            Location orientedDestination = applyPlayerOrientation(destination, player);
            teleportPlayer(player, orientedDestination, () -> {
                restorePlayerResetPose(player, orientedDestination, wasSneaking, wasSprinting);
                player.setFallDistance(0F);
                player.setFireTicks(0);
                player.setNoDamageTicks(100);
            });
        }
    }

    private void evacuatePlayersFromResetArea(FfaMatch match, FfaArena arena, ArenaSnapshot snapshot, World world) {
        if (snapshot == null || world == null) {
            return;
        }

        for (Player player : world.getPlayers()) {
            if (player == null || !player.isOnline() || !snapshot.contains(player.getLocation())) {
                continue;
            }

            if (isWaitingPlayerForArena(player.getUniqueId(), arena)) {
                player.setFallDistance(0F);
                player.setFireTicks(0);
                player.setNoDamageTicks(100);
                continue;
            }

            Location destination = resolveResetEvacuationLocation(player, match, arena, snapshot, world);
            if (destination == null || destination.getWorld() == null) {
                continue;
            }

            boolean wasSneaking = player.isSneaking();
            boolean wasSprinting = player.isSprinting();
            Location orientedDestination = applyPlayerOrientation(destination, player);
            teleportPlayer(player, orientedDestination, () -> {
                restorePlayerResetPose(player, orientedDestination, wasSneaking, wasSprinting);
                player.setFallDistance(0F);
                player.setFireTicks(0);
                player.setNoDamageTicks(100);
            });
        }
    }

    private Location resolveAutoRepairEvacuationLocation(Player player, ArenaSnapshot snapshot, World world) {
        if (player == null || snapshot == null || world == null) {
            return null;
        }

        Location current = player.getLocation();
        Location verticalSpot = findVerticalResetEvacuationLocation(current, snapshot, world);
        if (verticalSpot != null) {
            return verticalSpot;
        }

        Location directionalSpot = findDirectionalResetEvacuationLocation(player, snapshot, world);
        if (directionalSpot != null) {
            return directionalSpot;
        }

        Location outsideSpot = findSafeLocationOutsideResetArea(snapshot, world);
        if (outsideSpot != null) {
            return outsideSpot;
        }

        return new Location(world, current.getX(), snapshot.maxY() + 2.0D, current.getZ());
    }

    private boolean shouldEvacuateForAutoRepair(Location current, RepairColumn repairColumn) {
        if (current == null || repairColumn == null) {
            return false;
        }

        int feetY = current.getBlockY();
        int headY = feetY + 1;
        return headY >= repairColumn.minY() - 1
                && feetY <= repairColumn.maxY() + 1;
    }

    private Map<Long, RepairColumn> buildRepairColumns(List<BlockSnapshot> changedBlocks) {
        Map<Long, RepairColumn> repairColumns = new HashMap<>();
        if (changedBlocks == null || changedBlocks.isEmpty()) {
            return repairColumns;
        }

        for (BlockSnapshot blockSnapshot : changedBlocks) {
            long key = repairColumnKey(blockSnapshot.x(), blockSnapshot.z());
            RepairColumn existing = repairColumns.get(key);
            if (existing == null) {
                repairColumns.put(key, new RepairColumn(blockSnapshot.y(), blockSnapshot.y()));
                continue;
            }

            repairColumns.put(key, new RepairColumn(
                    Math.min(existing.minY(), blockSnapshot.y()),
                    Math.max(existing.maxY(), blockSnapshot.y())
            ));
        }

        return repairColumns;
    }

    private long repairColumnKey(int x, int z) {
        return (((long) x) << 32) ^ (z & 0xFFFFFFFFL);
    }

    private boolean isWaitingPlayerForArena(UUID uuid, FfaArena arena) {
        if (uuid == null || arena == null) {
            return false;
        }

        WaitingArenaEntry entry = waitingArenaEntries.get(uuid);
        return entry != null
                && entry.arena() != null
                && arena.getId().equalsIgnoreCase(entry.arena().getId());
    }

    private Location resolveResetEvacuationLocation(Player player, FfaMatch match, FfaArena arena, ArenaSnapshot snapshot, World world) {
        if (player == null || snapshot == null || world == null) {
            return null;
        }

        Location current = player.getLocation();
        if (isValidResetEvacuationLocation(current, snapshot)) {
            return current.clone();
        }

        Location safeExit = resolveSafeFfaExitLocation(arena, null, current);
        if (isValidResetEvacuationLocation(safeExit, snapshot)) {
            return safeExit;
        }

        Location directionalSpot = findDirectionalResetEvacuationLocation(player, snapshot, world);
        if (directionalSpot != null) {
            return directionalSpot;
        }

        Location outsideSpot = findSafeLocationOutsideResetArea(snapshot, world);
        if (outsideSpot != null) {
            return outsideSpot;
        }

        Location worldSpawn = resolveWorldSpawn(current);
        if (isValidExitLocation(worldSpawn)) {
            return worldSpawn.clone();
        }

        if (safeExit != null && safeExit.getWorld() != null) {
            return safeExit.clone();
        }

        return current.clone();
    }

    private Location findVerticalResetEvacuationLocation(Location current,
                                                         ArenaSnapshot snapshot,
                                                         World world) {
        if (current == null || snapshot == null || world == null) {
            return null;
        }

        int x = current.getBlockX();
        int z = current.getBlockZ();
        int minFeetY = Math.max(snapshot.maxY() + 2, current.getBlockY() + 1);
        int maxFeetY = Math.min(world.getMaxHeight() - 2, snapshot.maxY() + 24);
        for (int feetY = minFeetY; feetY <= maxFeetY; feetY++) {
            if (!isSafeVerticalResetLiftLocation(world, x, feetY, z)) {
                continue;
            }

            Location destination = current.clone();
            destination.setY(feetY);
            return destination;
        }

        return null;
    }

    private boolean isSafeVerticalResetLiftLocation(World world, int x, int feetY, int z) {
        if (world == null || feetY < world.getMinHeight() || feetY + 1 >= world.getMaxHeight()) {
            return false;
        }

        Block feet = world.getBlockAt(x, feetY, z);
        Block head = world.getBlockAt(x, feetY + 1, z);
        return feet.isPassable()
                && head.isPassable()
                && !isHazardous(feet.getType())
                && !isHazardous(head.getType());
    }

    private Location findDirectionalResetEvacuationLocation(Player player, ArenaSnapshot snapshot, World world) {
        if (player == null || snapshot == null || world == null) {
            return null;
        }

        double centerX = ((double) snapshot.minX() + snapshot.maxX()) / 2.0D + 0.5D;
        double centerZ = ((double) snapshot.minZ() + snapshot.maxZ()) / 2.0D + 0.5D;
        Location current = player.getLocation();
        double deltaX = current.getX() - centerX;
        double deltaZ = current.getZ() - centerZ;
        if (Math.abs(deltaX) < 0.001D && Math.abs(deltaZ) < 0.001D) {
            double yawRadians = Math.toRadians(current.getYaw());
            deltaX = -Math.sin(yawRadians);
            deltaZ = Math.cos(yawRadians);
        }

        double length = Math.sqrt((deltaX * deltaX) + (deltaZ * deltaZ));
        if (length < 0.001D) {
            deltaX = 1.0D;
            deltaZ = 0.0D;
            length = 1.0D;
        }

        double normalX = deltaX / length;
        double normalZ = deltaZ / length;
        int minY = Math.max(world.getMinHeight(), snapshot.minY() - 2);
        int maxY = Math.min(world.getMaxHeight() - 3, snapshot.maxY() + 4);
        int baseDistance = Math.max(snapshot.maxX() - snapshot.minX(), snapshot.maxZ() - snapshot.minZ()) / 2 + 2;

        for (int distance = baseDistance; distance <= baseDistance + 8; distance++) {
            int targetX = (int) Math.round(centerX + (normalX * distance));
            int targetZ = (int) Math.round(centerZ + (normalZ * distance));

            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    Location candidate = buildSafeOutsideLocation(world, targetX + offsetX, targetZ + offsetZ, minY, maxY, snapshot);
                    if (candidate != null) {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    private boolean isValidResetEvacuationLocation(Location location, ArenaSnapshot snapshot) {
        return location != null
                && location.getWorld() != null
                && !snapshot.contains(location);
    }

    private Location findSafeLocationOutsideResetArea(ArenaSnapshot snapshot, World world) {
        if (snapshot == null || world == null) {
            return null;
        }

        int minSearchX = snapshot.minX() - 6;
        int maxSearchX = snapshot.maxX() + 6;
        int minSearchZ = snapshot.minZ() - 6;
        int maxSearchZ = snapshot.maxZ() + 6;
        int minY = Math.max(world.getMinHeight(), snapshot.minY() - 2);
        int maxY = Math.min(world.getMaxHeight() - 3, snapshot.maxY() + 2);

        for (int radius = 1; radius <= 6; radius++) {
            int minX = snapshot.minX() - radius;
            int maxX = snapshot.maxX() + radius;
            int minZ = snapshot.minZ() - radius;
            int maxZ = snapshot.maxZ() + radius;

            for (int x = minX; x <= maxX; x++) {
                Location north = buildSafeOutsideLocation(world, x, minZ, minY, maxY, snapshot);
                if (north != null) {
                    return north;
                }
                Location south = buildSafeOutsideLocation(world, x, maxZ, minY, maxY, snapshot);
                if (south != null) {
                    return south;
                }
            }
            for (int z = minZ + 1; z < maxZ; z++) {
                Location west = buildSafeOutsideLocation(world, minX, z, minY, maxY, snapshot);
                if (west != null) {
                    return west;
                }
                Location east = buildSafeOutsideLocation(world, maxX, z, minY, maxY, snapshot);
                if (east != null) {
                    return east;
                }
            }
        }

        for (int x = minSearchX; x <= maxSearchX; x++) {
            for (int z = minSearchZ; z <= maxSearchZ; z++) {
                Location candidate = buildSafeOutsideLocation(world, x, z, minY, maxY, snapshot);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private Location buildSafeOutsideLocation(World world, int x, int z, int minY, int maxY, ArenaSnapshot snapshot) {
        Location candidate = findSafeStandingLocation(world, x, z, minY, maxY);
        if (candidate == null || snapshot.contains(candidate)) {
            return null;
        }
        return candidate;
    }

    private ArenaRegionBounds resolveArenaRegionBounds(FfaArena arena) {
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

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        boolean singlePointRegion = minX == maxX && minY == maxY && minZ == maxZ;
        int horizontalPadding = singlePointRegion
                ? Math.max(getRollbackHorizontalPadding(), MIN_SINGLE_POINT_ROLLBACK_HORIZONTAL_PADDING)
                : getRollbackHorizontalPadding();
        int verticalPadding = singlePointRegion
                ? Math.max(getRollbackVerticalPadding(), MIN_SINGLE_POINT_ROLLBACK_VERTICAL_PADDING)
                : getRollbackVerticalPadding();

        minX -= horizontalPadding;
        maxX += horizontalPadding;
        minY -= verticalPadding;
        maxY += verticalPadding;
        minZ -= horizontalPadding;
        maxZ += horizontalPadding;

        minY = Math.max(world.getMinHeight(), minY);
        maxY = Math.min(world.getMaxHeight() - 3, maxY);
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

    private FfaArena findArenaContainingLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        for (FfaArena arena : getArenas()) {
            ArenaRegionBounds bounds = resolveArenaRegionBounds(arena);
            if (bounds != null && isWithinArenaBounds(location, bounds)) {
                return arena;
            }
        }
        return null;
    }

    private Location applyPlayerOrientation(Location destination, Player player) {
        if (destination == null) {
            return null;
        }

        Location oriented = destination.clone();
        if (player == null) {
            return oriented;
        }

        Location current = player.getLocation();
        oriented.setYaw(current.getYaw());
        oriented.setPitch(current.getPitch());
        return oriented;
    }

    private void teleportPlayer(Player player, Location destination, Runnable afterSuccess) {
        if (player == null || destination == null || destination.getWorld() == null) {
            return;
        }

        plugin.getPaperScheduler().teleport(player, destination).thenAccept(success ->
                plugin.getPaperScheduler().runEntity(player, () -> {
                    if (!Boolean.TRUE.equals(success) || !player.isOnline()) {
                        return;
                    }
                    if (afterSuccess != null) {
                        afterSuccess.run();
                    }
                })
        );
    }

    private void restorePlayerResetPose(Player player,
                                        Location destination,
                                        boolean wasSneaking,
                                        boolean wasSprinting) {
        if (player == null || destination == null) {
            return;
        }

        player.setSneaking(wasSneaking);
        player.setSprinting(wasSprinting);
        player.setRotation(destination.getYaw(), destination.getPitch());
        plugin.getPaperScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }
            player.setSneaking(wasSneaking);
            player.setSprinting(wasSprinting);
            player.setRotation(destination.getYaw(), destination.getPitch());
        });
    }

    private void cleanupTransientEntities(ArenaSnapshot snapshot, World world) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player || !snapshot.contains(entity.getLocation())) {
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
                    || typeName.equals("TNT")
                    || typeName.equals("END_CRYSTAL")
                    || typeName.equals("FIREWORK_ROCKET")
                    || typeName.startsWith("MINECART")
                    || typeName.contains("BOAT")) {
                entity.remove();
            }
        }
    }

    private void ensureTables() {
        Connection connection = connection();
        if (connection == null) {
            return;
        }

        try (Statement st = connection.createStatement()) {
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS ffa_arenas (
                      id TEXT PRIMARY KEY,
                      display_name TEXT NOT NULL,
                      spawn1_data TEXT,
                      spawn2_data TEXT,
                      return_data TEXT,
                      region_pos1_data TEXT,
                      region_pos2_data TEXT,
                      enabled INTEGER DEFAULT 1,
                      queue_enabled INTEGER DEFAULT 1,
                      no_hunger INTEGER DEFAULT 0,
                      no_weather INTEGER DEFAULT 0,
                      always_morning INTEGER DEFAULT 0,
                      no_fall_damage INTEGER DEFAULT 0
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS ffa_stats (
                      player_uuid TEXT PRIMARY KEY,
                      wins INTEGER DEFAULT 0,
                      losses INTEGER DEFAULT 0,
                      draws INTEGER DEFAULT 0,
                      matches_played INTEGER DEFAULT 0,
                      current_streak INTEGER DEFAULT 0,
                      best_streak INTEGER DEFAULT 0,
                      updated_at INTEGER DEFAULT 0
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS ffa_matches (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
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
            ensureArenaSettingColumns(connection);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize FFA tables", e);
        }
    }

    private void ensureArenaSettingColumns(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            if (!plugin.getDatabaseManager().hasColumn("ffa_arenas", "no_hunger")) {
                plugin.getDatabaseManager().executeSchema(statement, "ALTER TABLE ffa_arenas ADD COLUMN no_hunger INTEGER DEFAULT 0");
            }
            if (!plugin.getDatabaseManager().hasColumn("ffa_arenas", "no_weather")) {
                plugin.getDatabaseManager().executeSchema(statement, "ALTER TABLE ffa_arenas ADD COLUMN no_weather INTEGER DEFAULT 0");
            }
            if (!plugin.getDatabaseManager().hasColumn("ffa_arenas", "always_morning")) {
                plugin.getDatabaseManager().executeSchema(statement, "ALTER TABLE ffa_arenas ADD COLUMN always_morning INTEGER DEFAULT 0");
            }
            if (!plugin.getDatabaseManager().hasColumn("ffa_arenas", "no_fall_damage")) {
                plugin.getDatabaseManager().executeSchema(statement, "ALTER TABLE ffa_arenas ADD COLUMN no_fall_damage INTEGER DEFAULT 0");
            }
        }
    }

    private void loadArenas() {
        Map<String, FfaArena> previousArenas = new HashMap<>(arenas);
        arenas.clear();
        nextRegionCorners.clear();
        arenaBaselineSnapshots.clear();
        if (connection() == null) {
            return;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT id, display_name, spawn1_data, spawn2_data, return_data, region_pos1_data, region_pos2_data, enabled, queue_enabled, no_hunger, no_weather, always_morning, no_fall_damage FROM ffa_arenas");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String arenaId = rs.getString("id");
                FfaArena loadedArena = new FfaArena(
                        arenaId,
                        rs.getString("display_name"),
                        LocationUtils.parse(rs.getString("spawn1_data")),
                        LocationUtils.parse(rs.getString("spawn2_data")),
                        LocationUtils.parse(rs.getString("return_data")),
                        LocationUtils.parse(rs.getString("region_pos1_data")),
                        LocationUtils.parse(rs.getString("region_pos2_data")),
                        rs.getInt("enabled") == 1,
                        rs.getInt("queue_enabled") == 1,
                        rs.getInt("no_hunger") == 1,
                        rs.getInt("no_weather") == 1,
                        rs.getInt("always_morning") == 1,
                        rs.getInt("no_fall_damage") == 1
                );
                FfaArena arena = previousArenas.get(arenaId);
                if (arena != null) {
                    updateLoadedArenaState(arena, loadedArena);
                } else {
                    arena = loadedArena;
                }
                arenas.put(arena.getId(), arena);
                nextRegionCorners.put(arena.getId(), determineNextRegionPosIndex(arena));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load FFA arenas", e);
        }

        synchronizeArenaSettingsConfig();
    }

    private void updateLoadedArenaState(FfaArena target, FfaArena loaded) {
        if (target == null || loaded == null) {
            return;
        }

        FfaArena.ArenaState currentState = target.getState();
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

        if (currentState == FfaArena.ArenaState.RESERVED || currentState == FfaArena.ArenaState.RESETTING) {
            target.setState(currentState);
        }
    }

    private void saveArena(FfaArena arena) {
        if (arena == null || connection() == null) {
            return;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "REPLACE INTO ffa_arenas (id, display_name, spawn1_data, spawn2_data, return_data, region_pos1_data, region_pos2_data, enabled, queue_enabled, no_hunger, no_weather, always_morning, no_fall_damage) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, arena.getId());
            ps.setString(2, arena.getDisplayName());
            ps.setString(3, LocationUtils.serialize(arena.getSpawn1()));
            ps.setString(4, LocationUtils.serialize(arena.getSpawn2()));
            ps.setString(5, LocationUtils.serialize(arena.getReturnLocation()));
            ps.setString(6, LocationUtils.serialize(arena.getRegionPos1()));
            ps.setString(7, LocationUtils.serialize(arena.getRegionPos2()));
            ps.setInt(8, arena.isEnabled() ? 1 : 0);
            ps.setInt(9, arena.isQueueEnabled() ? 1 : 0);
            ps.setInt(10, arena.isNoHunger() ? 1 : 0);
            ps.setInt(11, arena.isNoWeather() ? 1 : 0);
            ps.setInt(12, arena.isAlwaysMorning() ? 1 : 0);
            ps.setInt(13, arena.isNoFallDamage() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save FFA arena " + arena.getId(), e);
        }
    }

    private FfaStats loadStats(UUID uuid) {
        if (uuid == null || connection() == null) {
            return FfaStats.empty();
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT wins, losses, draws, matches_played, current_streak, best_streak FROM ffa_stats WHERE player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new FfaStats(
                            rs.getInt("wins"),
                            rs.getInt("losses"),
                            rs.getInt("draws"),
                            rs.getInt("matches_played"),
                            rs.getInt("current_streak"),
                            rs.getInt("best_streak")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load FFA stats for " + uuid, e);
        }

        return FfaStats.empty();
    }

    private void saveStats(UUID uuid, FfaStats stats) {
        if (uuid == null || stats == null || connection() == null) {
            return;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "REPLACE INTO ffa_stats (player_uuid, wins, losses, draws, matches_played, current_streak, best_streak, updated_at) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, stats.getWins());
            ps.setInt(3, stats.getLosses());
            ps.setInt(4, stats.getDraws());
            ps.setInt(5, stats.getMatchesPlayed());
            ps.setInt(6, stats.getCurrentStreak());
            ps.setInt(7, stats.getBestStreak());
            ps.setLong(8, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save FFA stats for " + uuid, e);
        }
    }

    private long insertMatch(FfaArena arena, UUID firstUuid, UUID secondUuid, long startedAt) {
        if (connection() == null || arena == null) {
            return -1L;
        }

        try (PreparedStatement ps = connection().prepareStatement("""
                INSERT INTO ffa_matches (arena_id, player_one_uuid, player_two_uuid, status, started_at)
                VALUES (?,?,?,?,?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, arena.getId());
            ps.setString(2, firstUuid.toString());
            ps.setString(3, secondUuid.toString());
            ps.setString(4, "ACTIVE");
            ps.setLong(5, Math.max(0L, startedAt));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to insert FFA match record", e);
        }
        return -1L;
    }

    private void updateMatchRecord(FfaMatch match, UUID winnerUuid, UUID loserUuid, String status, String endReason) {
        if (match == null || connection() == null) {
            return;
        }

        long endedAt = System.currentTimeMillis();
        long durationSeconds = match.getStartedAt() <= 0L
                ? 0L
                : Math.max(0L, (endedAt - match.getStartedAt()) / 1000L);

        try (PreparedStatement ps = connection().prepareStatement("""
                UPDATE ffa_matches
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
            ps.setString(3, status);
            ps.setString(4, endReason);
            ps.setLong(5, match.getStartedAt());
            ps.setLong(6, endedAt);
            ps.setLong(7, durationSeconds);
            ps.setLong(8, match.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update FFA match record " + match.getId(), e);
        }
    }

    private int determineNextRegionPosIndex(FfaArena arena) {
        if (arena == null) {
            return 0;
        }
        if (arena.getRegionPos1() == null) {
            return 1;
        }
        if (arena.getRegionPos2() == null) {
            return 2;
        }
        return nextRegionCorners.getOrDefault(arena.getId(), 1);
    }

    private boolean isCombatTagged(UUID uuid) {
        return uuid != null
                && plugin.getCombatManager() != null
                && plugin.getCombatManager().isInCombat(uuid);
    }

    private void updateCombatLock(UUID uuid) {
        if (uuid == null) {
            return;
        }
        if (isCombatTagged(uuid)) {
            combatLockedPlayers.add(uuid);
            return;
        }
        combatLockedPlayers.remove(uuid);
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

    private FileConfiguration config() {
        return plugin.getConfigManager().getFfa();
    }

    private void synchronizeArenaSettingsConfig() {
        FileConfiguration ffaConfig = config();
        if (ffaConfig == null) {
            return;
        }

        boolean changed = false;
        for (FfaArena arena : arenas.values()) {
            if (arena == null) {
                continue;
            }
            changed |= ensureArenaSettingsEntry(ffaConfig, arena);
            applyConfiguredArenaSettings(arena, ffaConfig);
        }

        if (changed) {
            plugin.getConfigManager().saveFfa();
        }
    }

    private boolean ensureArenaSettingsEntry(FileConfiguration ffaConfig, FfaArena arena) {
        if (ffaConfig == null || arena == null) {
            return false;
        }

        boolean changed = false;
        for (ArenaSetting setting : ArenaSetting.values()) {
            String path = getArenaSettingPath(arena.getId(), setting);
            if (!ffaConfig.contains(path)) {
                ffaConfig.set(path, false);
                changed = true;
            }
        }
        return changed;
    }

    private void applyConfiguredArenaSettings(FfaArena arena, FileConfiguration ffaConfig) {
        if (arena == null || ffaConfig == null) {
            return;
        }

        arena.setNoHunger(ffaConfig.getBoolean(getArenaSettingPath(arena.getId(), ArenaSetting.NO_HUNGER), false));
        arena.setNoWeather(ffaConfig.getBoolean(getArenaSettingPath(arena.getId(), ArenaSetting.NO_WEATHER), false));
        arena.setAlwaysMorning(ffaConfig.getBoolean(getArenaSettingPath(arena.getId(), ArenaSetting.ALWAYS_MORNING), false));
        arena.setNoFallDamage(ffaConfig.getBoolean(getArenaSettingPath(arena.getId(), ArenaSetting.NO_FALL_DAMAGE), false));
    }

    private String getArenaSettingPath(String arenaId, ArenaSetting setting) {
        return "ARENA_SETTINGS." + normalizeArenaId(arenaId) + "." + switch (setting) {
            case NO_HUNGER -> "NO_HUNGER";
            case NO_WEATHER -> "NO_WEATHER";
            case ALWAYS_MORNING -> "ALWAYS_MORNING";
            case NO_FALL_DAMAGE -> "NO_FALL_DAMAGE";
        };
    }

    private Connection connection() {
        return plugin.getDatabaseManager().getConnection();
    }

    private void send(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isBlank()) {
            sender.sendMessage(ColorUtils.toComponent(message));
        }
    }

    private void play(Player player, String soundPath) {
        if (player != null) {
            SoundUtils.play(player, plugin.getConfigManager().getSound(soundPath));
        }
    }

    private record WaitingArenaEntry(
            FfaArena arena,
            String playerName,
            FfaPlayerSnapshot snapshot,
            ArenaSnapshot arenaSnapshot,
            Location waitingLocation
    ) {
    }

    private record PendingRespawnState(FfaPlayerSnapshot snapshot, Location respawnLocation) {
    }

    private record WaitingMatchPair(
            UUID firstUuid,
            WaitingArenaEntry firstEntry,
            UUID secondUuid,
            WaitingArenaEntry secondEntry
    ) {
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

    private record SnapshotRepairTarget(int surfaceY, String topBlockData) {
    }

    private record SnapshotSurface(int y, String blockDataString) {
    }

    private record RepairColumn(int minY, int maxY) {
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
}
