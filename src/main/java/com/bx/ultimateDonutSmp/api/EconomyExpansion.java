package com.bx.ultimateDonutSmp.api;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CurrencyManager;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides %economy_*% placeholders for scoreboards, chat, holograms, etc.
 *
 * Supported:
 *   %economy_money%                raw money
 *   %economy_nicestMoney%          compact money amount (1,5K, 2,3M, ...)
 *   %economy_money_short%          compact money amount
 *   %economy_money_formatted%      configured money display
 *   %economy_money_symbol%         configured money symbol
 *   %economy_money_symbol_color%   configured money symbol color
 *   %economy_money_color%          configured money amount/display color
 *   %economy_money_name%           configured singular money name
 *   %economy_top_money_1_name%     leaderboard name for rank 1
 *   %economy_top_money_1_value%    full leaderboard value for rank 1
 *   %economy_top_money_1_value_short% compact leaderboard value for rank 1
 *   %economy_top_money_1_display%  ready-to-render leaderboard line for rank 1
 *   %economy_shards%               shard count
 *   %economy_nicestShards%         compact shard count
 *   %economy_shards_short%         compact shard count
 *   %economy_shards_formatted%     configured shard display
 *   %economy_shards_symbol%        configured shard symbol
 *   %economy_shards_symbol_color%  configured shard symbol color
 *   %economy_shards_color%         configured shard amount/display color
 *   %economy_shards_name%          configured singular shard name
 *   %economy_kills%                kill count
 *   %economy_deaths%               death count
 *   %economy_playtime%             formatted playtime
 *   %economy_team%                 team name (or "none")
 *   %economy_ping%                 player ping in ms
 *   %economy_username%             player name
 *   %economy_keyall_countdown%     time until next key-all
 *   %economy_booster_countdown%    time until booster expires (or "inactive")
 *   %economy_shard_cuboid_display% shard cuboid HUD text for scoreboard/action info
 *   %economy_shard_cuboid_status%  current shard cuboid state
 *   %economy_shard_cuboid_name%    active shard cuboid name
 *   %economy_x%                    player display X coordinate (randomized if setting enabled)
 *   %economy_y%                    player display Y coordinate
 *   %economy_z%                    player display Z coordinate (randomized if setting enabled)
 *   %economy_coords%               player display formatted coordinates (X, Y, Z)
 *   %economy_randomized_coords%    boolean setting state
 */
public class EconomyExpansion extends PlaceholderExpansion {

    private final UltimateDonutSmp plugin;
    private final LeaderboardPlaceholderResolver leaderboardPlaceholderResolver;

    public EconomyExpansion(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.leaderboardPlaceholderResolver = new LeaderboardPlaceholderResolver(plugin);
    }

    @Override
    public @NotNull String getIdentifier() { return "economy"; }

    @Override
    public @NotNull String getAuthor() { return "UltimateDonutSmp"; }

    @Override
    public @NotNull String getVersion() { return "1.1"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        String leaderboardValue = leaderboardPlaceholderResolver.resolve(offlinePlayer, params);
        if (leaderboardValue != null) {
            return leaderboardValue;
        }

        // Key-all and booster don't need player data
        if (params.equalsIgnoreCase("keyall_countdown")) {
            java.util.UUID uuid = offlinePlayer != null ? offlinePlayer.getUniqueId() : null;
            return plugin.getKeyAllManager().getFormattedCountdown(uuid);
        }

        // Booster countdown (needs uuid)
        if (params.equalsIgnoreCase("booster_countdown")) {
            if (offlinePlayer == null || !offlinePlayer.isOnline()) return "inactive";
            long secs = plugin.getShardManager().getBoosterRemainingSeconds(offlinePlayer.getUniqueId());
            return secs > 0 ? NumberUtils.formatCountdown(secs) : "inactive";
        }

        if (params.equalsIgnoreCase("rtp_countdown")) {
            if (!plugin.getFeatureManager().isEnabled(com.bx.ultimateDonutSmp.managers.FeatureManager.Feature.RTP_ZONE)) {
                return "disabled";
            }
            java.util.UUID uuid = offlinePlayer != null ? offlinePlayer.getUniqueId() : null;
            return plugin.getRtpZoneManager().getFormattedCountdown(uuid);
        }

        if (params.equalsIgnoreCase("billford_countdown")) {
            if (!plugin.getFeatureManager().isEnabled(com.bx.ultimateDonutSmp.managers.FeatureManager.Feature.BILLFORD)) {
                return "disabled";
            }
            return plugin.getBillfordManager().getFormattedCountdown();
        }

        if (params.equalsIgnoreCase("shard_cuboid_display")) {
            if (offlinePlayer == null || !offlinePlayer.isOnline()) return "-";
            return plugin.getShardManager().getShardCuboidDisplay(offlinePlayer.getUniqueId());
        }

        if (params.equalsIgnoreCase("shard_cuboid_status")) {
            if (offlinePlayer == null || !offlinePlayer.isOnline()) return "outside";
            return plugin.getShardManager().getShardCuboidStatus(offlinePlayer.getUniqueId());
        }

        if (params.equalsIgnoreCase("shard_cuboid_name")) {
            if (offlinePlayer == null || !offlinePlayer.isOnline()) return "none";
            return plugin.getShardManager().getShardCuboidName(offlinePlayer.getUniqueId());
        }

        // Ping (online only)
        if (params.equalsIgnoreCase("ping")) {
            if (offlinePlayer == null || !offlinePlayer.isOnline()) return "0";
            return String.valueOf(offlinePlayer.getPlayer().getPing());
        }

        // Username
        if (params.equalsIgnoreCase("username")) {
            if (offlinePlayer == null) return "unknown";
            return plugin.getHideManager() == null
                    ? (offlinePlayer.getName() != null ? offlinePlayer.getName() : "unknown")
                    : plugin.getHideManager().publicName(offlinePlayer.getUniqueId(), offlinePlayer.getName());
        }

        CurrencyManager currencyManager = plugin.getCurrencyManager();
        if (params.equalsIgnoreCase("money_symbol")) {
            return currencyManager.symbol(CurrencyManager.CurrencyType.MONEY);
        }
        if (params.equalsIgnoreCase("money_symbol_color")) {
            return currencyManager.symbolColor(CurrencyManager.CurrencyType.MONEY);
        }
        if (params.equalsIgnoreCase("money_symbol_colored")) {
            return currencyManager.coloredSymbol(CurrencyManager.CurrencyType.MONEY);
        }
        if (params.equalsIgnoreCase("money_color")) {
            return currencyManager.color(CurrencyManager.CurrencyType.MONEY);
        }
        if (params.equalsIgnoreCase("money_name")) {
            return currencyManager.singular(CurrencyManager.CurrencyType.MONEY);
        }
        if (params.equalsIgnoreCase("money_name_plural")) {
            return currencyManager.plural(CurrencyManager.CurrencyType.MONEY);
        }
        if (params.equalsIgnoreCase("shards_symbol")) {
            return currencyManager.symbol(CurrencyManager.CurrencyType.SHARDS);
        }
        if (params.equalsIgnoreCase("shards_symbol_color")) {
            return currencyManager.symbolColor(CurrencyManager.CurrencyType.SHARDS);
        }
        if (params.equalsIgnoreCase("shards_symbol_colored")) {
            return currencyManager.coloredSymbol(CurrencyManager.CurrencyType.SHARDS);
        }
        if (params.equalsIgnoreCase("shards_color")) {
            return currencyManager.color(CurrencyManager.CurrencyType.SHARDS);
        }
        if (params.equalsIgnoreCase("shards_name")) {
            return currencyManager.singular(CurrencyManager.CurrencyType.SHARDS);
        }
        if (params.equalsIgnoreCase("shards_name_plural")) {
            return currencyManager.plural(CurrencyManager.CurrencyType.SHARDS);
        }

        // Team
        if (params.equalsIgnoreCase("team")) {
            if (offlinePlayer == null) return "none";
            String team = offlinePlayer.isOnline()
                    ? plugin.getTeamManager().getTeamName(offlinePlayer.getPlayer())
                    : null;
            return team != null ? team.toUpperCase() : "none";
        }

        if (offlinePlayer == null) return "";

        // All others require player data
        PlayerData data = plugin.getPlayerDataManager().get(offlinePlayer.getUniqueId());
        if (data == null && offlinePlayer.isOnline()) {
            data = plugin.getPlayerDataManager().get(offlinePlayer.getPlayer());
        }
        if (data == null) {
            return switch (params) {
                case "nicestMoney", "money_short", "money_amount_short", "nicestmoney" ->
                        currencyManager.formatCompactAmount(CurrencyManager.CurrencyType.MONEY, 0D);
                case "money_formatted", "money-formatted" -> currencyManager.formatMoney(0D);
                case "money_short_formatted", "nicestMoney_formatted", "nicestmoney_formatted" -> currencyManager.formatMoneyCompact(0D);
                case "nicestShards", "shards_short", "shards_amount_short", "nicestshards" ->
                        currencyManager.formatCompactAmount(CurrencyManager.CurrencyType.SHARDS, 0D);
                case "shards_formatted", "shards-formatted" -> currencyManager.formatShards(0L);
                case "shards_short_formatted" -> currencyManager.formatShardsCompact(0L);
                default -> "0";
            };
        }

        return switch (params) {
            case "donutplus", "display_donutplus" -> {
                if (offlinePlayer == null || !offlinePlayer.isOnline()) yield "";
                org.bukkit.entity.Player p = offlinePlayer.getPlayer();
                if (p != null && data.isDisplayDonutPlusEnabled() && (p.hasPermission("ultimatedonutsmp.donutplus") || p.hasPermission("donutplus"))) {
                    yield "&d&lDonut+ &r";
                }
                yield "";
            }
            case "money" -> NumberUtils.format(data.getMoney());
            case "nicestMoney", "money_short", "money_amount_short", "nicestmoney" ->
                    currencyManager.formatCompactAmount(CurrencyManager.CurrencyType.MONEY, data.getMoney());
            case "money_formatted", "money-formatted" -> currencyManager.formatMoney(data.getMoney());
            case "money_short_formatted", "nicestMoney_formatted", "nicestmoney_formatted" -> currencyManager.formatMoneyCompact(data.getMoney());
            case "shards" -> String.valueOf(data.getShards());
            case "nicestShards", "shards_short", "shards_amount_short", "nicestshards" ->
                    currencyManager.formatCompactAmount(CurrencyManager.CurrencyType.SHARDS, data.getShards());
            case "shards_formatted", "shards-formatted" -> currencyManager.formatShards(data.getShards());
            case "shards_short_formatted" -> currencyManager.formatShardsCompact(data.getShards());
            case "kills" -> String.valueOf(data.getKills());
            case "deaths" -> String.valueOf(data.getDeaths());
            case "playtime" -> NumberUtils.formatTimeLong(data.getTotalPlaytimeSeconds());
            case "killStreak", "killstreak" -> String.valueOf(data.getKillStreak());
            case "highestKillStreak", "highestkillstreak" -> String.valueOf(data.getHighestKillStreak());
            case "blocksPlaced", "blocksplaced" -> String.valueOf(data.getBlocksPlaced());
            case "blocksBroken", "blocksbroken" -> String.valueOf(data.getBlocksBroken());
            case "mobsKilled", "mobskilled" -> String.valueOf(data.getMobsKilled());
            case "moneySpent", "moneyspent" -> NumberUtils.format(data.getMoneySpent());
            case "moneyMade", "moneymade" -> NumberUtils.format(data.getMoneyMade());
            case "x", "coord_x", "coords_x" -> {
                if (offlinePlayer == null || !offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null) yield "0";
                yield String.valueOf(data.getDisplayX(offlinePlayer.getPlayer().getLocation().getBlockX()));
            }
            case "y", "coord_y", "coords_y" -> {
                if (offlinePlayer == null || !offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null) yield "0";
                yield String.valueOf(data.getDisplayY(offlinePlayer.getPlayer().getLocation().getBlockY()));
            }
            case "z", "coord_z", "coords_z" -> {
                if (offlinePlayer == null || !offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null) yield "0";
                yield String.valueOf(data.getDisplayZ(offlinePlayer.getPlayer().getLocation().getBlockZ()));
            }
            case "coords", "location", "formatted_coords" -> {
                if (offlinePlayer == null || !offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null) yield "0, 0, 0";
                yield data.getDisplayCoords(offlinePlayer.getPlayer().getLocation());
            }
            case "randomized_coords", "randomized_coords_enabled" -> String.valueOf(data.isRandomizedCoords());
            default -> null;
        };
    }
}
