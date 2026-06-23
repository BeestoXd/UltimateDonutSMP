package com.bx.ultimateDonutSmp.api;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.HideState;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class HideExpansion extends PlaceholderExpansion {

    private final UltimateDonutSmp plugin;

    public HideExpansion(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "hide";
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
        if (player == null || plugin.getHideManager() == null) {
            return "";
        }
        HideState state = plugin.getHideManager().getState(player.getUniqueId());
        return switch (params.toLowerCase(Locale.ROOT)) {
            case "active" -> String.valueOf(state != null);
            case "public_name", "name" ->
                    plugin.getHideManager().publicName(player.getUniqueId(), player.getName());
            case "plain_name", "plain_public_name" ->
                    plugin.getHideManager().plainPublicName(player.getUniqueId(), player.getName());
            case "mode" -> state == null ? "NONE" : state.mode().name();
            case "alias" -> state == null ? "" : state.alias();
            case "skin" -> state == null ? "" : state.skinUsername();
            default -> null;
        };
    }
}
