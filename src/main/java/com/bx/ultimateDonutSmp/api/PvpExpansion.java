package com.bx.ultimateDonutSmp.api;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.PvpManager;
import com.bx.ultimateDonutSmp.models.PvpRank;
import com.bx.ultimateDonutSmp.models.PvpStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * The {@code %pvp_...%} placeholders.
 *
 * <p>Everything the arena tracks is exposed here rather than being drawn by the plugin itself, so
 * TAB stays in charge of nametags and any scoreboard or chat plugin can read the same numbers.</p>
 */
public class PvpExpansion extends PlaceholderExpansion {

    private static final DecimalFormat RATIO_FORMAT = new DecimalFormat("0.00");

    private final UltimateDonutSmp plugin;

    public PvpExpansion(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "pvp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "UltimateDonutSmp";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        PvpManager pvp = plugin.getPvpManager();
        if (pvp == null) {
            return null;
        }

        String key = params.toLowerCase(Locale.ROOT);
        if (key.equals("arena")) {
            return pvp.getArenaName();
        }
        if (key.equals("arena_reset")) {
            return pvp.getFormattedReset();
        }
        if (key.equals("arena_players")) {
            return String.valueOf(pvp.getArenaPlayerCount());
        }

        if (player == null) {
            return null;
        }

        PvpStats stats = pvp.getStats(player.getUniqueId());
        PvpRank rank = pvp.getRankFor(stats.getElo());

        return switch (key) {
            case "rank" -> rank == null ? "" : rank.getDisplay();
            case "rank_id" -> rank == null ? "" : rank.getId();
            case "elo" -> String.valueOf(stats.getElo());
            case "level" -> String.valueOf(stats.getLevel());
            case "xp" -> String.valueOf(stats.getXp());
            case "next_xp" -> String.valueOf(pvp.getXpForNextLevel(stats.getLevel()));
            case "kills" -> String.valueOf(stats.getKills());
            case "deaths" -> String.valueOf(stats.getDeaths());
            case "kd" -> RATIO_FORMAT.format(stats.getKillDeathRatio());
            case "streak" -> String.valueOf(stats.getStreak());
            case "best_streak" -> String.valueOf(stats.getBestStreak());
            case "joins" -> String.valueOf(stats.getArenaJoins());
            case "in_arena" -> String.valueOf(pvp.isInArena(player.getUniqueId()));
            default -> null;
        };
    }
}
