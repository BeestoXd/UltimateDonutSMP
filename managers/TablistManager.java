package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TablistManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern GRADIENT_TAG_PATTERN = Pattern.compile(
            "<#([A-Fa-f0-9]{6})>(.*?)</#([A-Fa-f0-9]{6})>",
            Pattern.DOTALL
    );
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("[&\\u00A7]([0-9A-FK-ORa-fk-or])");

    private final UltimateDonutSmp plugin;

    public TablistManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return config().getBoolean("TABLIST.ENABLED", true);
    }

    public void update(Player player) {
        if (!isEnabled()) {
            player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
            return;
        }

        String headerText = applyInternalPlaceholders(getMultilineText("TABLIST.HEADER"), player);
        String footerText = applyInternalPlaceholders(getMultilineText("TABLIST.FOOTER"), player);

        Component header = parseTabComponent(headerText, player);
        Component footer = parseTabComponent(footerText, player);

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
    }

    public void updateNamesAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTablistName(player);
        }
    }

    public void updateTablistName(Player player) {
        if (!isEnabled()) {
            player.playerListName(Component.text(player.getName()));
            return;
        }

        player.playerListName(parseTabComponent(resolveNameFormat(player), player));
    }

    private String resolveNameFormat(Player player) {
        String rawTeamName = plugin.getTeamManager().getTeamName(player);
        boolean showTeam = config().getBoolean("TABLIST.SHOW-TEAM-NAME", true);
        String teamName = showTeam ? rawTeamName : null;
        String prefix = resolvePrefix(player);
        String teamSuffix = "";
        String iconHeadSkin = config().getString("TABLIST.ICON-HEAD-SKIN", "<head:%player_name%>");
        String iconMedia = config().getString("TABLIST.ICON-MEDIA", "");
        String mediaBadge = resolveMediaBadge(player, iconMedia == null ? "" : iconMedia);
        String nickname = resolveNickname(player);

        if (showTeam && teamName != null && !teamName.isBlank()) {
            teamSuffix = " &7[&b" + teamName.toUpperCase() + "&7]";
        }

        String configuredFormat = config().getString(
                "TABLIST.NAME-FORMAT",
                "%prefix%%player%%team_suffix%"
        );
        configuredFormat = normalizeTeamFormat(configuredFormat, showTeam);
        if (showTeam && !containsTeamPlaceholder(configuredFormat)) {
            configuredFormat = configuredFormat + "%team_suffix%";
        }

        return applyInternalPlaceholders(configuredFormat, player)
                .replace("%prefix%", prefix)
                .replace("%player%", player.getName())
                .replace("%nick%", nickname)
                .replace("<player>", player.getName())
                .replace("<nick>", nickname)
                .replace("<icon_head_skin>", iconHeadSkin == null ? "" : iconHeadSkin)
                .replace("<icon_media>", iconMedia == null ? "" : iconMedia)
                .replace("%media_badge%", mediaBadge)
                .replace("<media_badge>", mediaBadge)
                .replace("<icon_media_plus>", mediaBadge)
                .replace("%team%", teamName == null ? "" : teamName)
                .replace("%team_name%", teamName == null ? "" : teamName.toUpperCase())
                .replace("%team_suffix%", teamSuffix)
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    private String normalizeTeamFormat(String text, boolean showTeam) {
        if (text == null || text.isBlank() || showTeam) {
            return text;
        }

        return text
                .replace(" &8[&d%team_name%&8]", "")
                .replace(" &8[&b%team_name%&8]", "")
                .replace(" &7[&b%team_name%&7]", "")
                .replace(" [&d%team_name%]", "")
                .replace(" [&b%team_name%]", "")
                .replace("%team_suffix%", "")
                .replace("%team_name%", "")
                .replace("%team%", "");
    }

    private boolean containsTeamPlaceholder(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        return text.contains("%team_suffix%")
                || text.contains("%team_name%")
                || text.contains("%team%");
    }

    private String resolvePrefix(Player player) {
        if (!ColorUtils.hasPAPI()) {
            return "";
        }

        try {
            String prefix = me.clip.placeholderapi.PlaceholderAPI
                    .setPlaceholders(player, "%luckperms_prefix%");
            if (prefix == null || prefix.isBlank() || prefix.startsWith("%")) {
                return "";
            }
            return prefix;
        } catch (Exception ignored) {
            return "";
        }
    }

    private String getMultilineText(String path) {
        if (config().isList(path)) {
            List<String> lines = config().getStringList(path);
            return String.join("\n", lines);
        }

        return config().getString(path, "");
    }

    private String resolveMediaBadge(Player player, String iconMedia) {
        String badgeFormat = config().getString("TABLIST.MEDIA-BADGE-FORMAT", "<icon_media>&#37BFF9+");
        String permission = config().getString("TABLIST.MEDIA-BADGE-PERMISSION", "");

        if (badgeFormat == null || badgeFormat.isBlank() || iconMedia.isBlank()) {
            return "";
        }

        if (permission != null && !permission.isBlank() && !player.hasPermission(permission)) {
            return "";
        }

        return badgeFormat.replace("<icon_media>", iconMedia);
    }

    private String applyInternalPlaceholders(String text, Player player) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("%player_name%", player.getName())
                .replace("%player%", player.getName())
                .replace("<player>", player.getName())
                .replace("%nick%", resolveNickname(player))
                .replace("<nick>", resolveNickname(player));
    }

    private String resolveNickname(Player player) {
        if (!ColorUtils.hasPAPI()) {
            return player.getName();
        }

        try {
            String nickname = me.clip.placeholderapi.PlaceholderAPI
                    .setPlaceholders(player, "%nickplus_nick%");
            if (nickname == null || nickname.isBlank() || nickname.startsWith("%")) {
                return player.getName();
            }
            return nickname;
        } catch (Exception ignored) {
            return player.getName();
        }
    }

    private Component parseTabComponent(String text, Player player) {
        if (text == null || text.isBlank()) {
            return Component.empty();
        }

        String resolved = text;
        if (ColorUtils.hasPAPI()) {
            try {
                resolved = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, resolved);
            } catch (Exception ignored) {
            }
        }

        resolved = applyInternalPlaceholders(resolved, player);
        resolved = convertLegacyAndGradientToMiniMessage(resolved);

        try {
            return MINI_MESSAGE.deserialize(resolved);
        } catch (Exception ignored) {
            return ColorUtils.toComponent(text, player);
        }
    }

    private String convertLegacyAndGradientToMiniMessage(String text) {
        Matcher gradientMatcher = GRADIENT_TAG_PATTERN.matcher(text);
        StringBuffer gradientBuffer = new StringBuffer();
        while (gradientMatcher.find()) {
            String replacement = "<gradient:#" + gradientMatcher.group(1) + ":#" + gradientMatcher.group(3) + ">"
                    + gradientMatcher.group(2)
                    + "</gradient>";
            gradientMatcher.appendReplacement(gradientBuffer, Matcher.quoteReplacement(replacement));
        }
        gradientMatcher.appendTail(gradientBuffer);

        Matcher hexMatcher = LEGACY_HEX_PATTERN.matcher(gradientBuffer.toString());
        StringBuffer hexBuffer = new StringBuffer();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(hexBuffer, Matcher.quoteReplacement("<#" + hexMatcher.group(1) + ">"));
        }
        hexMatcher.appendTail(hexBuffer);

        Matcher legacyMatcher = LEGACY_CODE_PATTERN.matcher(hexBuffer.toString());
        StringBuffer legacyBuffer = new StringBuffer();
        while (legacyMatcher.find()) {
            legacyMatcher.appendReplacement(
                    legacyBuffer,
                    Matcher.quoteReplacement(legacyCodeToMiniMessage(legacyMatcher.group(1).charAt(0)))
            );
        }
        legacyMatcher.appendTail(legacyBuffer);

        return legacyBuffer.toString();
    }

    private String legacyCodeToMiniMessage(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> "<black>";
            case '1' -> "<dark_blue>";
            case '2' -> "<dark_green>";
            case '3' -> "<dark_aqua>";
            case '4' -> "<dark_red>";
            case '5' -> "<dark_purple>";
            case '6' -> "<gold>";
            case '7' -> "<gray>";
            case '8' -> "<dark_gray>";
            case '9' -> "<blue>";
            case 'a' -> "<green>";
            case 'b' -> "<aqua>";
            case 'c' -> "<red>";
            case 'd' -> "<light_purple>";
            case 'e' -> "<yellow>";
            case 'f' -> "<white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> String.valueOf(code);
        };
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getConfig();
    }
}
