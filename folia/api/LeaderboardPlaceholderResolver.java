package com.bx.ultimateDonutSmp.api;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.LeaderboardManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;

/**
 * Resolves leaderboard placeholders such as:
 * %economy_top_money_1_name%
 * %economy_top_money_1_value%
 * %economy_top_money_1_value_short%
 * %economy_top_money_1_rank%
 * %economy_top_money_1_display%
 */
public class LeaderboardPlaceholderResolver {

    private final UltimateDonutSmp plugin;

    public LeaderboardPlaceholderResolver(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public @Nullable String resolve(@Nullable OfflinePlayer offlinePlayer, @NotNull String params) {
        if (params.isBlank() || !params.startsWith("top_")) {
            return null;
        }

        String[] parts = params.split("_");
        if (parts.length < 4) {
            return null;
        }

        int positionIndex = -1;
        for (int i = 1; i < parts.length - 1; i++) {
            if (isPositiveInteger(parts[i])) {
                positionIndex = i;
                break;
            }
        }

        if (positionIndex <= 1 || positionIndex >= parts.length - 1) {
            return null;
        }

        int position = Integer.parseInt(parts[positionIndex]);
        String typeKey = String.join("_", Arrays.copyOfRange(parts, 1, positionIndex));
        String outputKey = String.join("_", Arrays.copyOfRange(parts, positionIndex + 1, parts.length))
                .toLowerCase(Locale.US);

        LeaderboardManager.LeaderboardType type = plugin.getLeaderboardManager().parseType(typeKey).orElse(null);
        if (type == null) {
            return null;
        }

        LeaderboardManager.LeaderboardEntry entry = plugin.getLeaderboardManager().getEntryAt(type, position);
        String entryName = resolveEntryName(entry);
        String fullValue = entry == null ? "0" : plugin.getLeaderboardManager().formatValue(type, entry.playerData(), false, false);
        String shortValue = entry == null ? "0" : plugin.getLeaderboardManager().formatValue(type, entry.playerData(), true, false);

        return switch (outputKey) {
            case "name" -> entryName;
            case "value" -> fullValue;
            case "value_short" -> shortValue;
            case "rank" -> String.valueOf(position);
            case "display" -> "#" + position + " " + entryName + ": " + shortValue;
            default -> null;
        };
    }

    private String resolveEntryName(LeaderboardManager.@Nullable LeaderboardEntry entry) {
        if (entry == null || entry.playerData() == null) {
            return "None";
        }

        String username = entry.playerData().getUsername();
        return username == null || username.isBlank() ? "Unknown" : username;
    }

    private boolean isPositiveInteger(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }

        for (int i = 0; i < input.length(); i++) {
            if (!Character.isDigit(input.charAt(i))) {
                return false;
            }
        }

        return !input.equals("0");
    }
}
