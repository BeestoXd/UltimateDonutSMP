package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Bounty;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.NumberUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LeaderboardManager {

    private static final long CACHE_TTL_MS = 2_000L;

    public enum LeaderboardType {
        MONEY("money"),
        SHARDS("shards"),
        KILLS("kills"),
        DEATHS("deaths"),
        PLAYTIME("playtime"),
        BLOCKS_PLACED("blocksPlaced"),
        BLOCKS_BROKEN("blocksBroken"),
        MOBS_KILLED("mobsKilled"),
        KILL_STREAK("killStreak"),
        HIGHEST_KILL_STREAK("highestKillStreak"),
        MONEY_SPENT("moneySpent"),
        MONEY_MADE("moneyMade"),
        BOUNTIES("bounties");

        private final String configKey;

        LeaderboardType(String configKey) {
            this.configKey = configKey;
        }

        public String getConfigKey() {
            return configKey;
        }
    }

    public record LeaderboardEntry(int position, PlayerData playerData) {
    }

    private record CachedLeaderboard(long cachedAtMillis, List<PlayerData> players, boolean stale) {
        private CachedLeaderboard(long cachedAtMillis, List<PlayerData> players) {
            this(cachedAtMillis, players, false);
        }

        private boolean needsRefresh(long now) {
            return stale || now - cachedAtMillis >= CACHE_TTL_MS;
        }

        private CachedLeaderboard markStale() {
            return stale ? this : new CachedLeaderboard(cachedAtMillis, players, true);
        }
    }

    private final UltimateDonutSmp plugin;
    private final Map<LeaderboardType, CachedLeaderboard> leaderboardCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Set<LeaderboardType> refreshingTypes = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public LeaderboardManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        for (LeaderboardType type : LeaderboardType.values()) {
            triggerAsyncRefresh(type);
        }
    }

    public Optional<LeaderboardType> parseType(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        String normalized = normalize(input);
        for (LeaderboardType type : LeaderboardType.values()) {
            if (normalize(type.getConfigKey()).equals(normalized) || normalize(type.name()).equals(normalized)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public List<LeaderboardType> getTypes() {
        return List.of(LeaderboardType.values());
    }

    public String getDisplayName(LeaderboardType type) {
        String configured = plugin.getConfigManager().getMenus()
                .getString("LEADERBOARDS-MENU.TYPE-NAMES." + type.getConfigKey(), prettify(type));
        return plugin.getCurrencyManager().applyStaticPlaceholders(configured);
    }

    public String formatValue(LeaderboardType type, PlayerData data) {
        return formatValue(type, data, true, true);
    }

    public String formatValue(LeaderboardType type, PlayerData data, boolean compact, boolean includeCurrencySymbol) {
        return switch (type) {
            case MONEY -> formatCurrencyValue(CurrencyManager.CurrencyType.MONEY, data.getMoney(), compact, includeCurrencySymbol);
            case SHARDS -> formatCurrencyValue(CurrencyManager.CurrencyType.SHARDS, data.getShards(), compact, includeCurrencySymbol);
            case KILLS -> NumberUtils.format(data.getKills());
            case DEATHS -> NumberUtils.format(data.getDeaths());
            case PLAYTIME -> NumberUtils.formatTimeLong(data.getTotalPlaytimeSeconds());
            case BLOCKS_PLACED -> NumberUtils.format(data.getBlocksPlaced());
            case BLOCKS_BROKEN -> NumberUtils.format(data.getBlocksBroken());
            case MOBS_KILLED -> NumberUtils.format(data.getMobsKilled());
            case KILL_STREAK -> NumberUtils.format(data.getKillStreak());
            case HIGHEST_KILL_STREAK -> NumberUtils.format(data.getHighestKillStreak());
            case MONEY_SPENT -> formatCurrencyValue(CurrencyManager.CurrencyType.MONEY, data.getMoneySpent(), compact, includeCurrencySymbol);
            case MONEY_MADE -> formatCurrencyValue(CurrencyManager.CurrencyType.MONEY, data.getMoneyMade(), compact, includeCurrencySymbol);
            case BOUNTIES -> formatCurrencyValue(CurrencyManager.CurrencyType.MONEY, bountyAmount(data), compact, includeCurrencySymbol);
        };
    }

    public List<LeaderboardEntry> getEntries(LeaderboardType type, int offset, int limit) {
        List<PlayerData> sorted = getSortedPlayers(type);
        if (offset >= sorted.size()) {
            return List.of();
        }

        int endIndex = Math.min(sorted.size(), offset + Math.max(0, limit));
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (int i = offset; i < endIndex; i++) {
            entries.add(new LeaderboardEntry(i + 1, sorted.get(i)));
        }
        return entries;
    }

    public int getTotalEntries(LeaderboardType type) {
        return getSortedPlayers(type).size();
    }

    public LeaderboardEntry getPlayerEntry(UUID uuid, LeaderboardType type) {
        List<PlayerData> sorted = getSortedPlayers(type);
        for (int i = 0; i < sorted.size(); i++) {
            PlayerData data = sorted.get(i);
            if (data.getUuid().equals(uuid)) {
                return new LeaderboardEntry(i + 1, data);
            }
        }
        return null;
    }

    public LeaderboardEntry getEntryAt(LeaderboardType type, int position) {
        if (type == null || position <= 0) {
            return null;
        }

        List<PlayerData> sorted = getSortedPlayers(type);
        if (position > sorted.size()) {
            return null;
        }

        return new LeaderboardEntry(position, sorted.get(position - 1));
    }

    public double getNumericValue(LeaderboardType type, PlayerData data) {
        return numericValue(type, data);
    }

    // Keeps the last snapshot readable while a refresh runs; dropping it here made the
    // next menu open render "no leaderboard data" until the async reload finished.
    public void invalidate(LeaderboardType type) {
        if (type == null) {
            return;
        }
        leaderboardCache.computeIfPresent(type, (key, cached) -> cached.markStale());
    }

    public void invalidateAll() {
        leaderboardCache.replaceAll((key, cached) -> cached.markStale());
        for (LeaderboardType type : LeaderboardType.values()) {
            triggerAsyncRefresh(type);
        }
    }

    private List<PlayerData> getSortedPlayers(LeaderboardType type) {
        if (type == null) {
            return List.of();
        }
        long now = System.currentTimeMillis();
        CachedLeaderboard cachedLeaderboard = leaderboardCache.get(type);
        if (cachedLeaderboard == null) {
            triggerAsyncRefresh(type);
            return List.of();
        }

        if (cachedLeaderboard.needsRefresh(now)) {
            triggerAsyncRefresh(type);
        }
        return cachedLeaderboard.players();
    }

    public void triggerAsyncRefresh(LeaderboardType type) {
        if (type == null || !refreshingTypes.add(type)) {
            return;
        }
        plugin.getDatabaseManager().executeAsync(() -> {
            try {
                Map<UUID, PlayerData> merged = new LinkedHashMap<>();
                for (PlayerData stored : plugin.getDatabaseManager().loadAllPlayers()) {
                    merged.put(stored.getUuid(), stored);
                }
                for (PlayerData live : plugin.getPlayerDataManager().getAll()) {
                    merged.put(live.getUuid(), live);
                }

                List<PlayerData> players = new ArrayList<>(merged.values());
                players.removeIf(data -> data == null || data.getUsername() == null || data.getUsername().isBlank());
                if (type == LeaderboardType.BOUNTIES) {
                    // Every player carries a zero bounty, so an unfiltered board would pad the
                    // ranking with players nobody placed a bounty on.
                    players.removeIf(data -> bountyAmount(data) <= 0D);
                }
                players.sort(comparator(type));

                List<PlayerData> snapshot = List.copyOf(players);
                leaderboardCache.put(type, new CachedLeaderboard(System.currentTimeMillis(), snapshot));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to update leaderboard cache for " + type.name() + ": " + e.getMessage());
            } finally {
                refreshingTypes.remove(type);
            }
        });
    }

    private Comparator<PlayerData> comparator(LeaderboardType type) {
        Comparator<PlayerData> comparator = Comparator
                .comparingDouble((PlayerData data) -> numericValue(type, data))
                .reversed();
        return comparator.thenComparing(
                data -> data.getUsername() == null ? "" : data.getUsername().toLowerCase(Locale.US)
        );
    }

    private double numericValue(LeaderboardType type, PlayerData data) {
        return switch (type) {
            case MONEY -> data.getMoney();
            case SHARDS -> data.getShards();
            case KILLS -> data.getKills();
            case DEATHS -> data.getDeaths();
            case PLAYTIME -> data.getTotalPlaytimeSeconds();
            case BLOCKS_PLACED -> data.getBlocksPlaced();
            case BLOCKS_BROKEN -> data.getBlocksBroken();
            case MOBS_KILLED -> data.getMobsKilled();
            case KILL_STREAK -> data.getKillStreak();
            case HIGHEST_KILL_STREAK -> data.getHighestKillStreak();
            case MONEY_SPENT -> data.getMoneySpent();
            case MONEY_MADE -> data.getMoneyMade();
            case BOUNTIES -> bountyAmount(data);
        };
    }

    private double bountyAmount(PlayerData data) {
        if (data == null || data.getUuid() == null) {
            return 0D;
        }

        BountyManager bountyManager = plugin.getBountyManager();
        if (bountyManager == null) {
            return 0D;
        }

        Bounty bounty = bountyManager.getBounty(data.getUuid());
        return bounty == null ? 0D : bounty.getAmount();
    }

    private String formatCurrencyValue(
            CurrencyManager.CurrencyType type,
            double amount,
            boolean compact,
            boolean includeCurrencySymbol
    ) {
        CurrencyManager currencyManager = plugin.getCurrencyManager();
        if (includeCurrencySymbol) {
            return currencyManager.format(type, amount, compact);
        }
        return compact
                ? currencyManager.formatCompactAmount(type, amount)
                : currencyManager.formatAmount(type, amount);
    }

    private String prettify(LeaderboardType type) {
        String[] parts = type.getConfigKey()
                .replace('-', ' ')
                .replace('_', ' ')
                .split("(?=[A-Z])|\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private String normalize(String input) {
        return input.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.US);
    }
}
