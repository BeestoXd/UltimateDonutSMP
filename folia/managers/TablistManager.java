package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TablistManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern HEAD_TAG_PATTERN = Pattern.compile("(?i)<head:[^>\\r\\n]*>");
    private static final Pattern GRADIENT_TAG_PATTERN = Pattern.compile(
            "<#([A-Fa-f0-9]{6})>(.*?)</#([A-Fa-f0-9]{6})>",
            Pattern.DOTALL
    );
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("[&\\u00A7]([0-9A-FK-ORa-fk-or])");

    private final UltimateDonutSmp plugin;
    private final Map<UUID, SkinTexture> skinTextures = new ConcurrentHashMap<>();

    public TablistManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void updateSkinTexture(UUID playerId, String value, String signature) {
        if (playerId == null) {
            return;
        }
        if (value == null || value.isBlank()) {
            skinTextures.remove(playerId);
            return;
        }
        skinTextures.put(playerId, new SkinTexture(value, signature));
    }

    SkinTexture resolveCurrentSkinTexture(Player player) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        SkinTexture cached = skinTextures.get(player.getUniqueId());
        if (cached != null && cached.isValid()) {
            return cached;
        }

        SkinTexture restored = resolveSkinTextureForFakePlayer(player.getUniqueId(), player.getName());
        if (restored != null && restored.isValid()) {
            skinTextures.put(player.getUniqueId(), restored);
            return restored;
        }

        return resolveLiveGameProfileSkinTexture(player);
    }

    SkinTexture resolveSkinTextureForFakePlayer(UUID playerId, String playerName) {
        SkinTexture texture = SkinsRestorerSkinLookup.resolve(playerId, playerName);
        return texture != null && texture.isValid() ? texture : null;
    }

    SkinTexture resolveOriginalGameProfileSkinTexture(UUID playerId, String playerName) {
        SkinTexture texture = resolveMojangSessionSkinTexture(playerId);
        if (texture != null && texture.isValid()) {
            return texture;
        }

        UUID namedUuid = resolveMojangUuidByName(playerName);
        return namedUuid == null ? null : resolveMojangSessionSkinTexture(namedUuid);
    }

    SkinTexture resolveLiveGameProfileSkinTexture(Player player) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        try {
            for (ProfileProperty property : player.getPlayerProfile().getProperties()) {
                if (property != null
                        && "textures".equalsIgnoreCase(property.getName())
                        && property.getValue() != null
                        && !property.getValue().isBlank()) {
                    return new SkinTexture(property.getValue(), property.getSignature());
                }
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        return null;
    }

    private SkinTexture resolveMojangSessionSkinTexture(UUID playerId) {
        if (playerId == null) {
            return null;
        }

        JsonObject root = fetchJsonObject(URI.create(
                "https://sessionserver.mojang.com/session/minecraft/profile/"
                        + playerId.toString().replace("-", "")
                        + "?unsigned=false"
        ));
        if (root == null) {
            return null;
        }

        JsonArray properties = root.getAsJsonArray("properties");
        if (properties == null) {
            return null;
        }
        for (JsonElement element : properties) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject property = element.getAsJsonObject();
            if (!property.has("name")
                    || !property.has("value")
                    || !"textures".equalsIgnoreCase(property.get("name").getAsString())) {
                continue;
            }
            JsonElement signature = property.get("signature");
            SkinTexture texture = new SkinTexture(
                    property.get("value").getAsString(),
                    signature == null || signature.isJsonNull() ? null : signature.getAsString()
            );
            return texture.isValid() ? texture : null;
        }
        return null;
    }

    private UUID resolveMojangUuidByName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return null;
        }

        JsonObject root = fetchJsonObject(URI.create(
                "https://api.mojang.com/users/profiles/minecraft/" + playerName
        ));
        if (root == null || !root.has("id")) {
            return null;
        }

        String compact = root.get("id").getAsString().replace("-", "");
        if (compact.length() != 32) {
            return null;
        }
        return UUID.fromString(compact.substring(0, 8)
                + "-" + compact.substring(8, 12)
                + "-" + compact.substring(12, 16)
                + "-" + compact.substring(16, 20)
                + "-" + compact.substring(20));
    }

    private JsonObject fetchJsonObject(URI uri) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(4000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) {
                return null;
            }

            try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                return parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
            }
        } catch (Exception | LinkageError ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.TABLIST)
                && config().getBoolean("TABLIST.ENABLED", true);
    }

    public void update(Player player) {
        if (!isEnabled()) {
            return;
        }

        String headerText = applyInternalPlaceholders(getMultilineText("TABLIST.HEADER"), player);
        String footerText = applyInternalPlaceholders(getMultilineText("TABLIST.FOOTER"), player);

        Component header = parseTabComponent(headerText, player);
        Component footer = parseTabComponent(footerText, player);

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    public void updateAll() {
        if (!isEnabled()) {
            return;
        }

        plugin.getFoliaScheduler().forEachOnlinePlayer(this::update);
    }

    public void updateNamesAll() {
        if (!isEnabled()) {
            return;
        }

        plugin.getFoliaScheduler().forEachOnlinePlayer(this::updateTablistName);
    }

    public void updateTablistName(Player player) {
        if (!isEnabled()) {
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
        String normalizedIconMedia = iconMedia == null ? "" : iconMedia;
        boolean includeMediaBadge = hasMediaBadgeIncludePermission(player);
        String mediaIconBadge = resolveMediaIconBadge(player, normalizedIconMedia, includeMediaBadge);
        String mediaPlusBadge = resolveMediaPlusBadge(player, includeMediaBadge);
        String mediaBadge = resolveMediaBadge(mediaIconBadge, mediaPlusBadge, normalizedIconMedia);
        String nickname = resolveNickname(player);
        String publicName = plugin.getHideManager() == null
                ? player.getName()
                : plugin.getHideManager().publicName(player);

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
                .replace("%player%", publicName)
                .replace("%nick%", nickname)
                .replace("<player>", publicName)
                .replace("<nick>", nickname)
                .replace("<icon_head_skin>", iconHeadSkin == null ? "" : iconHeadSkin)
                .replace("<icon_media>", normalizedIconMedia)
                .replace("%media_icon_badge%", mediaIconBadge)
                .replace("<media_icon_badge>", mediaIconBadge)
                .replace("%media_plus_badge%", mediaPlusBadge)
                .replace("<media_plus_badge>", mediaPlusBadge)
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

    private boolean hasMediaBadgeIncludePermission(Player player) {
        String permission = config().getString(
                "TABLIST.MEDIA-BADGE-INCLUDE-PERMISSION",
                "rank.media.include"
        );
        return permission != null && !permission.isBlank() && PermissionUtils.has(player, permission);
    }

    private String resolveMediaIconBadge(Player player, String iconMedia, boolean includeMediaBadge) {
        String iconFormat = config().getString("TABLIST.MEDIA-ICON-FORMAT", "&d<icon_media>");
        String permission = config().getString("TABLIST.MEDIA-BADGE-PERMISSION", "rank.media");

        if (iconFormat == null || iconFormat.isBlank() || iconMedia.isBlank()) {
            return "";
        }

        if (!includeMediaBadge && permission != null && !permission.isBlank() && !PermissionUtils.has(player, permission)) {
            return "";
        }

        return iconFormat.replace("<icon_media>", iconMedia);
    }

    private String resolveMediaPlusBadge(Player player, boolean includeMediaBadge) {
        String plusFormat = config().getString("TABLIST.MEDIA-PLUS-FORMAT", "&#37BFF9+");
        String permission = config().getString("TABLIST.MEDIA-PLUS-PERMISSION", "rank.media.plus");

        if (plusFormat == null || plusFormat.isBlank()) {
            return "";
        }

        if (!includeMediaBadge && permission != null && !permission.isBlank() && !PermissionUtils.has(player, permission)) {
            return "";
        }

        return plusFormat;
    }

    private String resolveMediaBadge(String mediaIconBadge, String mediaPlusBadge, String iconMedia) {
        String badgeFormat = config().getString(
                "TABLIST.MEDIA-BADGE-FORMAT",
                "<media_icon_badge><media_plus_badge>"
        );

        if (badgeFormat == null || badgeFormat.isBlank()) {
            return "";
        }

        if (!usesSplitMediaBadgePlaceholders(badgeFormat)) {
            if (mediaIconBadge.isBlank() || iconMedia.isBlank()) {
                return "";
            }
            return badgeFormat.replace("<icon_media>", iconMedia);
        }

        return badgeFormat
                .replace("%media_icon_badge%", mediaIconBadge)
                .replace("<media_icon_badge>", mediaIconBadge)
                .replace("%media_plus_badge%", mediaPlusBadge)
                .replace("<media_plus_badge>", mediaPlusBadge)
                .replace("<icon_media>", mediaIconBadge.isBlank() ? "" : iconMedia);
    }

    private boolean usesSplitMediaBadgePlaceholders(String text) {
        return text.contains("%media_icon_badge%")
                || text.contains("<media_icon_badge>")
                || text.contains("%media_plus_badge%")
                || text.contains("<media_plus_badge>");
    }

    private String applyInternalPlaceholders(String text, Player player) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String publicName = plugin.getHideManager() == null
                ? player.getName()
                : plugin.getHideManager().publicName(player);
        return text
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("%player_name%", publicName)
                .replace("%player%", publicName)
                .replace("<player>", publicName)
                .replace("%nick%", resolveNickname(player))
                .replace("<nick>", resolveNickname(player));
    }

    private String resolveNickname(Player player) {
        if (plugin.getHideManager() != null && plugin.getHideManager().isHidden(player.getUniqueId())) {
            return plugin.getHideManager().publicName(player);
        }
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
        resolved = stripUnsupportedHeadTags(resolved);
        resolved = convertLegacyAndGradientToMiniMessage(resolved);

        try {
            return MINI_MESSAGE.deserialize(resolved);
        } catch (Exception ignored) {
            return ColorUtils.toComponent(stripUnsupportedHeadTags(text), player);
        }
    }

    private String stripUnsupportedHeadTags(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return HEAD_TAG_PATTERN.matcher(text).replaceAll("");
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

    record SkinTexture(String value, String signature) {
        boolean isValid() {
            return value != null && !value.isBlank();
        }
    }
}
