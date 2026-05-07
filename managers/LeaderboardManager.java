package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.NumberUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
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
        MONEY_MADE("moneyMade");

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

    private record CachedLeaderboard(long cachedAtMillis, List<PlayerData> players) {
        private boolean isExpired(long now) {
            return now - cachedAtMillis >= CACHE_TTL_MS;
        }
    }

    private final UltimateDonutSmp plugin;
    private final Map<LeaderboardType, CachedLeaderboard> leaderboardCache = new EnumMap<>(LeaderboardType.class);

    public LeaderboardManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
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
        return plugin.getConfigManager().getMenus()
                .getString("LEADERBOARDS-MENU.TYPE-NAMES." + type.getConfigKey(), prettify(type));
    }

    public String formatValue(LeaderboardType type, PlayerData data) {
        return formatValue(type, data, true, true);
    }

    public String formatValue(LeaderboardType type, PlayerData data, boolean compact, boolean includeCurrencySymbol) {
        return switch (type) {
            case MONEY -> formatCurrencyValue(data.getMoney(), compact, includeCurrencySymbol);
            case SHARDS -> NumberUtils.format(data.getShards());
            case KILLS -> NumberUtils.format(data.getKills());
            case DEATHS -> NumberUtils.format(data.getDeaths());
            case PLAYTIME -> NumberUtils.formatTimeLong(data.getTotalPlaytimeSeconds());
            case BLOCKS_PLACED -> NumberUtils.format(data.getBlocksPlaced());
            case BLOCKS_BROKEN -> NumberUtils.format(data.getBlocksBroken());
            case MOBS_KILLED -> NumberUtils.format(data.getMobsKilled());
            case KILL_STREAK -> NumberUtils.format(data.getKillStreak());
            case HIGHEST_KILL_STREAK -> NumberUtils.format(data.getHighestKillStreak());
            case MONEY_SPENT -> formatCurrencyValue(data.getMoneySpent(), compact, includeCurrencySymbol);
            case MONEY_MADE -> formatCurrencyValue(data.getMoneyMade(), compact, includeCurrencySymbol);
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

    public void invalidate(LeaderboardType type) {
        if (type != null) {
            leaderboardCache.remove(type);
        }
    }

    public void invalidateAll() {
        leaderboardCache.clear();
    }

    private List<PlayerData> getSortedPlayers(LeaderboardType type) {
        long now = System.currentTimeMillis();
        CachedLeaderboard cachedLeaderboard = leaderboardCache.get(type);
        if (cachedLeaderboard != null && !cachedLeaderboard.isExpired(now)) {
            return cachedLeaderboard.players();
        }

        Map<UUID, PlayerData> merged = new LinkedHashMap<>();
        for (PlayerData stored : plugin.getDatabaseManager().loadAllPlayers()) {
            merged.put(stored.getUuid(), stored);
        }
        for (PlayerData live : plugin.getPlayerDataManager().getAll()) {
            merged.put(live.getUuid(), live);
        }

        List<PlayerData> players = new ArrayList<>(merged.values());
        players.removeIf(data -> data == null || data.getUsername() == null || data.getUsername().isBlank());
        players.sort(comparator(type));

        List<PlayerData> snapshot = List.copyOf(players);
        leaderboardCache.put(type, new CachedLeaderboard(now, snapshot));
        return snapshot;
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
        };
    }

    private String formatCurrencyValue(double amount, boolean compact, boolean includeCurrencySymbol) {
        String formatted = compact ? NumberUtils.formatNice(amount) : NumberUtils.format(amount);
        return includeCurrencySymbol ? "$" + formatted : formatted;
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
