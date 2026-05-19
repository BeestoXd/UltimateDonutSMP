package com.bx.ultimateDonutSmp.api;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
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
 *   %economy_nicestMoney%          formatted money (1.5K, 2.3M, ...)
 *   %economy_top_money_1_name%     leaderboard name for rank 1
 *   %economy_top_money_1_value%    full leaderboard value for rank 1
 *   %economy_top_money_1_value_short% compact leaderboard value for rank 1
 *   %economy_top_money_1_display%  ready-to-render leaderboard line for rank 1
 *   %economy_shards%               shard count
 *   %economy_kills%                kill count
 *   %economy_deaths%               death count
 *   %economy_playtime%             formatted playtime
 *   %economy_team%                 team name (or "None")
 *   %economy_ping%                 player ping in ms
 *   %economy_username%             player name
 *   %economy_keyall_countdown%     time until next key-all
 *   %economy_booster_countdown%    time until booster expires (or "Inactive")
 *   %economy_shard_cuboid_display% shard cuboid HUD text for scoreboard/action info
 *   %economy_shard_cuboid_status%  current shard cuboid state
 *   %economy_shard_cuboid_name%    active shard cuboid name
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
    public @NotNull String getVersion() { return "1.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        String leaderboardValue = leaderboardPlaceholderResolver.resolve(offlinePlayer, params);
        if (leaderboardValue != null) {
            return leaderboardValue;
        }

        if (offlinePlayer == null) return "";

        // key-all and booster don't need player data
        if (params.equals("keyall_countdown")) {
            return plugin.getKeyAllManager().getFormattedCountdown(offlinePlayer.getUniqueId());
        }

        // booster countdown (needs uuid)
        if (params.equals("booster_countdown")) {
            if (!offlinePlayer.isOnline()) return "ɪɴᴀᴄᴛɪᴠᴇ";
            long secs = plugin.getShardManager().getBoosterRemainingSeconds(offlinePlayer.getUniqueId());
            return secs > 0 ? NumberUtils.formatCountdown(secs) : "ɪɴᴀᴄᴛɪᴠᴇ";
        }

        if (params.equals("shard_cuboid_display")) {
            if (!offlinePlayer.isOnline()) return "-";
            return plugin.getShardManager().getShardCuboidDisplay(offlinePlayer.getUniqueId());
        }

        if (params.equals("shard_cuboid_status")) {
            if (!offlinePlayer.isOnline()) return "OUTSIDE";
            return plugin.getShardManager().getShardCuboidStatus(offlinePlayer.getUniqueId());
        }

        if (params.equals("shard_cuboid_name")) {
            if (!offlinePlayer.isOnline()) return "ɴᴏɴᴇ";
            return plugin.getShardManager().getShardCuboidName(offlinePlayer.getUniqueId());
        }

        // ping (online only)
        if (params.equals("ping")) {
            if (!offlinePlayer.isOnline()) return "0";
            return String.valueOf(offlinePlayer.getPlayer().getPing());
        }

        // username
        if (params.equals("username")) {
            return offlinePlayer.getName() != null ? offlinePlayer.getName() : "ᴜɴᴋɴᴏᴡɴ";
        }

        // team
        if (params.equals("team")) {
            String team = offlinePlayer.isOnline()
                    ? plugin.getTeamManager().getTeamName(offlinePlayer.getPlayer())
                    : null;
            return team != null ? team.toUpperCase() : "ɴᴏɴᴇ";
        }

        // all others require player data
        PlayerData data = plugin.getPlayerDataManager().get(offlinePlayer.getUniqueId());
        if (data == null && offlinePlayer.isOnline()) {
            data = plugin.getPlayerDataManager().get(offlinePlayer.getPlayer());
        }
        if (data == null) return "0";

        return switch (params) {
            case "money" -> NumberUtils.format(data.getMoney());
            case "nicestMoney" -> NumberUtils.formatNice(data.getMoney());
            case "shards" -> String.valueOf(data.getShards());
            case "kills" -> String.valueOf(data.getKills());
            case "deaths" -> String.valueOf(data.getDeaths());
            case "playtime" -> NumberUtils.formatTimeLong(data.getTotalPlaytimeSeconds());
            default -> null;
        };
    }
}
