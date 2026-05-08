package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class StatsWipeManager {

    public enum WipeTarget {
        PLAYER_STATS("PLAYER_STATS", "Player Stats", "stats", "playerstats", "player_stats"),
        TEAM_DOCUMENTS("TEAM_DOCUMENTS", "Team Documents", "teams", "team", "teamdocs", "team_documents"),
        HOME_DOCUMENTS("HOME_DOCUMENTS", "Home Documents", "homes", "home", "homedocs", "home_documents"),
        BOUNTIES("BOUNTIES", "Bounties", "bounty", "bounties"),
        SELL_DOCUMENTS("SELL_DOCUMENTS", "Sell Documents", "sell", "sellhistory", "sell_history", "selldocuments", "sell_documents");

        private final String configKey;
        private final String displayName;
        private final Set<String> aliases;

        WipeTarget(String configKey, String displayName, String... aliases) {
            this.configKey = configKey;
            this.displayName = displayName;
            this.aliases = Set.of(aliases);
        }

        public String getConfigKey() {
            return configKey;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Optional<WipeTarget> fromInput(String input) {
            if (input == null || input.isBlank()) {
                return Optional.empty();
            }

            String normalized = normalize(input);
            for (WipeTarget target : values()) {
                if (normalize(target.name()).equals(normalized)
                        || normalize(target.configKey).equals(normalized)
                        || target.aliases.stream().map(WipeTarget::normalize).anyMatch(normalized::equals)) {
                    return Optional.of(target);
                }
            }
            return Optional.empty();
        }

        private static String normalize(String input) {
            return input.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
        }
    }

    public record WipeResult(boolean success, boolean busy, Map<WipeTarget, Integer> affectedCounts, String errorMessage) {
        public int affectedCount(WipeTarget target) {
            return affectedCounts.getOrDefault(target, 0);
        }
    }

    private final UltimateDonutSmp plugin;
    private final AtomicBoolean wipeInProgress = new AtomicBoolean(false);

    public StatsWipeManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isWipeInProgress() {
        return wipeInProgress.get();
    }

    public Map<WipeTarget, Integer> buildPreviewCounts() {
        Map<WipeTarget, Integer> counts = new LinkedHashMap<>();
        for (WipeTarget target : WipeTarget.values()) {
            counts.put(target, getPreviewCount(target));
        }
        return counts;
    }

    public int getPreviewCount(WipeTarget target) {
        return switch (target) {
            case PLAYER_STATS -> countPlayerStatsTargets();
            case TEAM_DOCUMENTS -> plugin.getDatabaseManager().countTeams();
            case HOME_DOCUMENTS -> plugin.getDatabaseManager().countHomes();
            case BOUNTIES -> plugin.getDatabaseManager().countBounties();
            case SELL_DOCUMENTS -> plugin.getDatabaseManager().countSellDocuments();
        };
    }

    public WipeResult wipeTarget(WipeTarget target, String actorName) {
        return wipeTargets(EnumSet.of(target), actorName);
    }

    public WipeResult wipeTargets(Set<WipeTarget> targets, String actorName) {
        if (targets == null || targets.isEmpty()) {
            return new WipeResult(false, false, Map.of(), "No wipe targets selected.");
        }
        if (!wipeInProgress.compareAndSet(false, true)) {
            return new WipeResult(false, true, Map.of(), "A wipe is already in progress.");
        }

        EnumSet<WipeTarget> normalizedTargets = EnumSet.copyOf(targets);
        Map<WipeTarget, Integer> affectedCounts = new LinkedHashMap<>();

        try {
            for (WipeTarget target : normalizedTargets) {
                affectedCounts.put(target, wipeSingleTarget(target));
            }
            refreshRuntimeStateAfterWipe(normalizedTargets);
            plugin.getLogger().info("Stats wipe completed by " + actorName + " for targets " + normalizedTargets + ".");
            return new WipeResult(true, false, Map.copyOf(affectedCounts), null);
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Stats wipe failed for " + normalizedTargets, exception);
            return new WipeResult(false, false, Map.copyOf(affectedCounts), exception.getMessage());
        } finally {
            wipeInProgress.set(false);
        }
    }

    private int wipeSingleTarget(WipeTarget target) {
        return switch (target) {
            case PLAYER_STATS -> wipePlayerStats();
            case TEAM_DOCUMENTS -> wipeTeams();
            case HOME_DOCUMENTS -> wipeHomes();
            case BOUNTIES -> wipeBounties();
            case SELL_DOCUMENTS -> wipeSellDocuments();
        };
    }

    private int wipePlayerStats() {
        int affectedPlayers = countPlayerStatsTargets();
        long now = System.currentTimeMillis();
        resetLivePlayerStats(now);
        plugin.getDatabaseManager().resetPlayerStats();
        persistLivePlayerData();
        return affectedPlayers;
    }

    private int wipeTeams() {
        int affectedTeams = plugin.getDatabaseManager().countTeams();
        plugin.getDatabaseManager().clearTeams();
        plugin.getTeamManager().loadAll();
        return affectedTeams;
    }

    private int wipeHomes() {
        int affectedHomes = plugin.getDatabaseManager().countHomes();
        plugin.getDatabaseManager().clearHomes();
        plugin.getHomeManager().clearAllCaches();
        return affectedHomes;
    }

    private int wipeBounties() {
        int affectedBounties = plugin.getDatabaseManager().countBounties();
        plugin.getDatabaseManager().clearBounties();
        plugin.getBountyManager().clearAll();
        return affectedBounties;
    }

    private int wipeSellDocuments() {
        int affectedDocuments = plugin.getDatabaseManager().countSellDocuments();
        plugin.getDatabaseManager().clearSellHistoryAndProgress();
        return affectedDocuments;
    }

    private void resetLivePlayerStats(long now) {
        Collection<PlayerData> loadedPlayers = plugin.getPlayerDataManager().getAll();
        for (PlayerData data : loadedPlayers) {
            data.resetTrackedStats(now);
        }
    }

    private void persistLivePlayerData() {
        for (PlayerData data : plugin.getPlayerDataManager().getAll()) {
            plugin.getDatabaseManager().savePlayer(data);
        }
    }

    private int countPlayerStatsTargets() {
        Map<UUID, PlayerData> merged = new HashMap<>();
        for (PlayerData stored : plugin.getDatabaseManager().loadAllPlayers()) {
            merged.put(stored.getUuid(), stored);
        }
        for (PlayerData live : plugin.getPlayerDataManager().getAll()) {
            merged.put(live.getUuid(), live);
        }

        int count = 0;
        for (PlayerData data : merged.values()) {
            if (hasTrackedStats(data)) {
                count++;
            }
        }
        return count;
    }

    private boolean hasTrackedStats(PlayerData data) {
        return data != null
                && (data.getKills() != 0
                || data.getDeaths() != 0
                || data.getTotalPlaytimeSeconds() != 0
                || data.getBlocksPlaced() != 0
                || data.getBlocksBroken() != 0
                || data.getMobsKilled() != 0
                || data.getKillStreak() != 0
                || data.getHighestKillStreak() != 0
                || Double.compare(data.getMoneySpent(), 0D) != 0
                || Double.compare(data.getMoneyMade(), 0D) != 0);
    }

    private void refreshRuntimeStateAfterWipe(Set<WipeTarget> targets) {
        if (targets.contains(WipeTarget.HOME_DOCUMENTS)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getHomeManager().loadHomes(player);
            }
        }

        if (targets.contains(WipeTarget.TEAM_DOCUMENTS)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getTeamManager().setTeamChat(player.getUniqueId(), false);
                plugin.getTeamManager().clearSearchState(player.getUniqueId());
                plugin.getTablistManager().updateTablistName(player);
            }
        }

        if (targets.contains(WipeTarget.PLAYER_STATS) || targets.contains(WipeTarget.TEAM_DOCUMENTS)) {
            plugin.getScoreboardManager().updateAll();
            plugin.getTablistManager().updateAll();
        }
    }
}
