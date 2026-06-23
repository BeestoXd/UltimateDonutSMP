package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.AdventureHeadComponentBridge;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.TablistComponentUpdater;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
    private static final List<Long> DEFAULT_SKIN_HEAD_REFRESH_DELAYS = List.of(5L, 40L, 100L);
    private static final long SKIN_HEAD_TEXTURE_REFRESH_INTERVAL_MS = 60_000L;
    private static final long SKIN_HEAD_TEXTURE_RETRY_INTERVAL_MS = 15_000L;

    private final UltimateDonutSmp plugin;
    private final TablistComponentUpdater componentUpdater;
    private final AdventureHeadComponentBridge headComponentBridge;
    private final Set<UUID> refreshedSkinHeads = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingSkinHeadTextureRefreshes = ConcurrentHashMap.newKeySet();
    private final Set<UUID> skinsRestorerSkinHeadTextures = ConcurrentHashMap.newKeySet();
    private final Map<UUID, SkinTexture> skinHeadTextures = new ConcurrentHashMap<>();
    private final Map<UUID, Long> skinHeadTextureRefreshTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, PermissionOverride>> luckPermsCommandOverrides = new ConcurrentHashMap<>();

    public TablistManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.componentUpdater = new TablistComponentUpdater(plugin);
        this.headComponentBridge = new AdventureHeadComponentBridge();
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
        player.setPlayerListHeaderFooter(parseTabText(headerText, player), parseTabText(footerText, player));
    }

    public void updateAll() {
        if (!isEnabled()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
    }

    public void updateNamesAll() {
        if (!isEnabled()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTablistName(player);
        }
    }

    public void updateTablistName(Player player) {
        if (!isEnabled()) {
            return;
        }

        String nameFormat = resolveNameFormat(player);
        refreshSkinHeadTextureIfNeeded(player, nameFormat, false);
        Component adventureComponent = parseTabComponent(nameFormat, player);
        if (adventureComponent != null) {
            if (componentUpdater.updateName(player, adventureComponent)) return;
        }

        player.setPlayerListName(parseTabText(nameFormat, player));
    }

    public void refreshSkinHeads(Player player) {
        if (player == null || !player.isOnline() || !isEnabled()
                || !config().getBoolean("TABLIST.REFRESH-SKIN-HEADS", true)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        if (!refreshedSkinHeads.add(playerId)) {
            return;
        }

        scheduleSkinHeadRefresh(player, playerId);
    }

    /**
     * Forces a skin head refresh for the given player, bypassing the once-per-join
     * deduplication guard. This should be used when a permission change or skin
     * change is detected at runtime (e.g. via LuckPerms or SkinsRestorer events)
     * so the tablist updates without requiring a rejoin.
     */
    public void forceRefreshSkinHeads(Player player) {
        if (player == null || !player.isOnline() || !isEnabled()
                || !config().getBoolean("TABLIST.REFRESH-SKIN-HEADS", true)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        // Clear cached texture data so the refresh fetches fresh textures
        removeCachedSkinTexture(playerId);
        skinHeadTextureRefreshTimes.remove(playerId);
        pendingSkinHeadTextureRefreshes.remove(playerId);
        // Allow re-entry into the refresh logic
        refreshedSkinHeads.add(playerId);

        scheduleSkinHeadRefresh(player, playerId);
    }

    /**
     * Invalidates the cached skin texture for the given player so the next
     * refresh cycle re-fetches the texture from the game profile or SkinsRestorer.
     */
    public void invalidateSkinCache(UUID playerId) {
        removeCachedSkinTexture(playerId);
        skinHeadTextureRefreshTimes.remove(playerId);
        pendingSkinHeadTextureRefreshes.remove(playerId);
        refreshedSkinHeads.remove(playerId);
    }

    /**
     * Updates the cached skin texture for the given player and refreshes their tablist entry.
     * This is typically called when SkinsRestorer applies a new skin.
     */
    public void updateSkinTexture(UUID playerId, String value, String signature) {
        if (playerId == null) {
            return;
        }

        if (value == null || value.isBlank()) {
            invalidateSkinCache(playerId);
            return;
        }

        SkinTexture texture = new SkinTexture(value, signature);
        cacheSkinTexture(playerId, texture, true);
        skinHeadTextureRefreshTimes.put(playerId, System.currentTimeMillis());
        refreshedSkinHeads.add(playerId);

        if (!isEnabled()) {
            return;
        }

        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            applySkinTexture(player, texture);
            refreshTablistAvatar(player);
            updateTablistName(player);
            update(player);
        }
    }

    SkinTexture resolveCurrentSkinTexture(Player player) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        SkinTexture cachedSkinsRestorerTexture = cachedSkinsRestorerTexture(player.getUniqueId());
        if (cachedSkinsRestorerTexture != null && cachedSkinsRestorerTexture.isValid()) {
            return cachedSkinsRestorerTexture;
        }

        SkinTexture skinsRestorerTexture = resolveSkinsRestorerCurrentTextureSync(player.getUniqueId(), player.getName());
        if (skinsRestorerTexture != null && skinsRestorerTexture.isValid()) {
            cacheSkinTexture(player.getUniqueId(), skinsRestorerTexture, true);
            skinHeadTextureRefreshTimes.put(player.getUniqueId(), System.currentTimeMillis());
            applySkinTexture(player, skinsRestorerTexture);
            return skinsRestorerTexture;
        }

        skinsRestorerTexture = resolveSkinsRestorerStoredTextureSync(player.getUniqueId());
        if (skinsRestorerTexture != null && skinsRestorerTexture.isValid()) {
            cacheSkinTexture(player.getUniqueId(), skinsRestorerTexture, true);
            skinHeadTextureRefreshTimes.put(player.getUniqueId(), System.currentTimeMillis());
            applySkinTexture(player, skinsRestorerTexture);
            return skinsRestorerTexture;
        }

        SkinTexture paperAppliedTexture = resolvePaperAppliedSkinTexture(player);
        if (paperAppliedTexture != null && paperAppliedTexture.isValid()) {
            cacheSkinTexture(player.getUniqueId(), paperAppliedTexture, false);
            skinHeadTextureRefreshTimes.put(player.getUniqueId(), System.currentTimeMillis());
            return paperAppliedTexture;
        }

        SkinTexture knownTexture = resolveKnownSkinTexture(player);
        return knownTexture != null && knownTexture.isValid() ? knownTexture : null;
    }

    SkinTexture resolveKnownSkinTexture(Player player) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        SkinTexture cached = cachedSkinTexture(player.getUniqueId());
        if (cached != null && cached.isValid()) {
            return cached;
        }

        SkinTexture paperAppliedTexture = resolvePaperAppliedSkinTexture(player);
        if (paperAppliedTexture != null && paperAppliedTexture.isValid()) {
            cacheSkinTexture(player.getUniqueId(), paperAppliedTexture, false);
            skinHeadTextureRefreshTimes.put(player.getUniqueId(), System.currentTimeMillis());
            return paperAppliedTexture;
        }

        SkinTexture profileTexture = resolveGameProfileTexture(player);
        if (profileTexture != null && profileTexture.isValid()) {
            cacheSkinTexture(player.getUniqueId(), profileTexture, false);
            skinHeadTextureRefreshTimes.put(player.getUniqueId(), System.currentTimeMillis());
            return profileTexture;
        }

        SkinTexture storedTexture = resolveSkinsRestorerStoredTextureSync(player.getUniqueId());
        if (storedTexture != null && storedTexture.isValid()) {
            cacheSkinTexture(player.getUniqueId(), storedTexture, true);
            skinHeadTextureRefreshTimes.put(player.getUniqueId(), System.currentTimeMillis());
            applySkinTexture(player, storedTexture);
            return storedTexture;
        }

        return null;
    }

    SkinTexture resolveGameProfileSkinTexture(Player player) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        SkinTexture paperAppliedTexture = resolvePaperAppliedSkinTexture(player);
        if (paperAppliedTexture != null && paperAppliedTexture.isValid()) {
            cacheSkinTexture(player.getUniqueId(), paperAppliedTexture, false);
            skinHeadTextureRefreshTimes.put(player.getUniqueId(), System.currentTimeMillis());
            return paperAppliedTexture;
        }

        SkinTexture profileTexture = resolveGameProfileTexture(player);
        if (profileTexture != null && profileTexture.isValid()) {
            cacheSkinTexture(player.getUniqueId(), profileTexture, false);
            skinHeadTextureRefreshTimes.put(player.getUniqueId(), System.currentTimeMillis());
            return profileTexture;
        }

        return null;
    }

    SkinTexture resolveOriginalGameProfileSkinTexture(UUID playerId, String playerName) {
        SkinTexture sessionTexture = resolveMojangSessionSkinTexture(playerId);
        if (sessionTexture != null && sessionTexture.isValid()) {
            return sessionTexture;
        }

        SkinTexture namedSessionTexture = resolveMojangNamedSkinTexture(playerName);
        if (namedSessionTexture != null && namedSessionTexture.isValid()) {
            return namedSessionTexture;
        }

        SkinTexture updatedProfileTexture = resolveUpdatedBukkitProfileTexture(playerId, playerName);
        return updatedProfileTexture != null && updatedProfileTexture.isValid() ? updatedProfileTexture : null;
    }

    SkinTexture resolveLiveGameProfileSkinTexture(Player player) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        SkinTexture profileTexture = resolveGameProfileTexture(player);
        return profileTexture != null && profileTexture.isValid() ? profileTexture : null;
    }

    SkinTexture resolveSkinTextureForFakePlayer(UUID playerId, String playerName) {
        SkinTexture texture = !Bukkit.isPrimaryThread()
                ? resolveSkinsRestorerTexture(playerId, playerName)
                : resolveSkinsRestorerCurrentTextureSync(playerId, playerName);
        if (texture != null && texture.isValid()) {
            return texture;
        }

        texture = resolveSkinsRestorerStoredTextureSync(playerId);
        if (texture != null && texture.isValid()) {
            return texture;
        }

        return null;
    }

    SkinTexture cachedSkinTexture(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        SkinTexture cached = skinHeadTextures.get(playerId);
        return cached != null && cached.isValid() ? cached : null;
    }

    SkinTexture cachedSkinsRestorerTexture(UUID playerId) {
        if (playerId == null || !skinsRestorerSkinHeadTextures.contains(playerId)) {
            return null;
        }
        return cachedSkinTexture(playerId);
    }

    public void refreshTablistEntry(Player player, boolean forceSkinTextureRefresh) {
        if (player == null || !player.isOnline()) {
            return;
        }

        if (!isEnabled()) {
            return;
        }

        if (forceSkinTextureRefresh) {
            refreshSkinHeadTextureIfNeeded(player, resolveNameFormat(player), true);
        }

        updateTablistName(player);
        update(player);
    }

    public void forceRefreshPermissionTablistEntry(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        if (!isEnabled()) {
            return;
        }

        String nameFormat = resolveNameFormat(player);
        refreshSkinHeadTextureIfNeeded(player, nameFormat, false);
        Component adventureComponent = parseTabComponent(nameFormat, player);
        if (adventureComponent != null && componentUpdater.refreshEntry(player, adventureComponent)) {
            update(player);
            return;
        }

        updateTablistName(player);
        update(player);
        refreshTablistAvatar(player);
        UUID playerId = player.getUniqueId();
        for (long delayTicks : List.of(8L, 20L)) {
            plugin.getFoliaScheduler().runEntityLater(player, () -> {
                Player online = Bukkit.getPlayer(playerId);
                if (online != null && online.isOnline()) {
                    updateTablistName(online);
                    update(online);
                }
            }, delayTicks);
        }
    }

    public void refreshStoredSkinTexture(Player player) {
        if (player == null || !player.isOnline() || !isEnabled()
                || !config().getBoolean("TABLIST.REFRESH-SKIN-HEADS", true)) {
            return;
        }

        String nameFormat = resolveNameFormat(player);
        if (!usesConfiguredSkinHead(nameFormat)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        SkinTexture storedTexture = resolveSkinsRestorerStoredTextureSync(playerId);
        if (storedTexture != null && storedTexture.isValid()) {
            boolean changed = cacheAndApplySkinTexture(player, storedTexture, false, true);
            if (changed) {
                updateTablistName(player);
                update(player);
            }
            return;
        }

        SkinTexture paperAppliedTexture = resolvePaperAppliedSkinTexture(player);
        if (paperAppliedTexture != null && paperAppliedTexture.isValid()) {
            boolean changed = cacheAndApplySkinTexture(player, paperAppliedTexture, false, false);
            if (changed) {
                updateTablistName(player);
                update(player);
            }
            return;
        }

        SkinTexture profileTexture = resolveGameProfileTexture(player);
        if (profileTexture == null || !profileTexture.isValid()) {
            if (clearCachedSkinTexture(player, false)) {
                updateTablistName(player);
            }
            return;
        }

        boolean changed = cacheAndApplySkinTexture(player, profileTexture, false, false);
        if (changed) {
            updateTablistName(player);
            update(player);
        }
    }

    private void scheduleSkinHeadRefresh(Player player, UUID playerId) {
        for (long delayTicks : getSkinHeadRefreshDelays()) {
            plugin.getFoliaScheduler().runEntityLater(player, () -> {
                Player online = Bukkit.getPlayer(playerId);
                if (online != null && online.isOnline() && isEnabled()) {
                    refreshSkinHeadTextureIfNeeded(online, resolveNameFormat(online), true);
                    updateTablistName(online);
                } else {
                    refreshedSkinHeads.remove(playerId);
                }
            }, delayTicks);
        }
    }

    public void removePlayer(UUID playerId) {
        refreshedSkinHeads.remove(playerId);
        pendingSkinHeadTextureRefreshes.remove(playerId);
        removeCachedSkinTexture(playerId);
        skinHeadTextureRefreshTimes.remove(playerId);
        luckPermsCommandOverrides.remove(playerId);
    }

    public String createPermissionRefreshSnapshot(Player player) {
        if (player == null || !player.isOnline() || !isEnabled()) {
            return "";
        }

        return resolveNameFormat(player);
    }

    public void invalidateLuckPermsCachedData(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        try {
            Object adapter = resolveLuckPermsPlayerAdapter();
            Object user = invokeCompatible(adapter, "getUser", player);
            invalidateLuckPermsCachedData(user);

            Object permissionData = invokeCompatible(adapter, "getPermissionData", player);
            invokeNoArgIfPresent(permissionData, "invalidateCache");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
        }
    }

    public void rememberLuckPermsPermissionOverride(UUID playerId, String permission, boolean value) {
        if (playerId == null || permission == null || permission.isBlank()) {
            return;
        }

        String normalized = PermissionUtils.normalizePermissionNode(permission);
        if (normalized.isBlank()) {
            return;
        }

        Map<String, PermissionOverride> overrides = luckPermsCommandOverrides
                .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>());
        putPermissionOverride(overrides, normalized, value);

        if (normalized.equals("rank.media")) {
            putPermissionOverride(overrides, config().getString("TABLIST.MEDIA-BADGE-PERMISSION", "RANK.MEDIA"), value);
        } else if (normalized.equals("rank.media.plus")) {
            putPermissionOverride(overrides, config().getString("TABLIST.MEDIA-PLUS-PERMISSION", "RANK.MEDIA.PLUS"), value);
        } else if (normalized.equals("rank.media.include")) {
            putPermissionOverride(
                    overrides,
                    config().getString("TABLIST.MEDIA-BADGE-INCLUDE-PERMISSION", "RANK.MEDIA.INCLUDE"),
                    value
            );
        }
    }

    private void putPermissionOverride(Map<String, PermissionOverride> overrides, String permission, boolean value) {
        String normalized = PermissionUtils.normalizePermissionNode(permission);
        if (!normalized.isBlank()) {
            overrides.put(normalized, new PermissionOverride(value, System.currentTimeMillis() + 3_600_000L));
        }
    }

    private String resolveNameFormat(Player player) {
        String rawTeamName = plugin.getTeamManager().getTeamName(player);
        boolean showTeam = config().getBoolean("TABLIST.SHOW-TEAM-NAME", true);
        String teamName = showTeam ? rawTeamName : null;
        String prefix = resolvePrefix(player);
        String teamSuffix = "";
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
        String luckPermsPrefix = resolveLuckPermsPrefix(player);
        if (luckPermsPrefix != null && !luckPermsPrefix.isBlank()) {
            return luckPermsPrefix;
        }

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
                "RANK.MEDIA.INCLUDE"
        );
        return hasLivePermission(player, permission);
    }

    private String resolveMediaIconBadge(Player player, String iconMedia, boolean includeMediaBadge) {
        String iconFormat = config().getString("TABLIST.MEDIA-ICON-FORMAT", "&d<icon_media>");
        String permission = config().getString("TABLIST.MEDIA-BADGE-PERMISSION", "RANK.MEDIA");

        if (iconFormat == null || iconFormat.isBlank() || iconMedia.isBlank()) {
            return "";
        }

        if (!includeMediaBadge && permission != null && !permission.isBlank() && !hasLivePermission(player, permission)) {
            return "";
        }

        return iconFormat.replace("<icon_media>", iconMedia);
    }

    private String resolveMediaPlusBadge(Player player, boolean includeMediaBadge) {
        String plusFormat = config().getString("TABLIST.MEDIA-PLUS-FORMAT", "&#37BFF9+");
        String permission = config().getString("TABLIST.MEDIA-PLUS-PERMISSION", "RANK.MEDIA.PLUS");

        if (plusFormat == null || plusFormat.isBlank()) {
            return "";
        }

        if (!includeMediaBadge && permission != null && !permission.isBlank() && !hasLivePermission(player, permission)) {
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

    private boolean hasLivePermission(Player player, String permission) {
        if (player == null || permission == null || permission.isBlank()) {
            return false;
        }

        Optional<Boolean> commandOverride = resolveLuckPermsCommandOverride(player, permission);
        if (commandOverride.isPresent()) {
            return commandOverride.get();
        }

        Optional<Boolean> luckPermsValue = resolveLuckPermsPermission(player, permission);
        if (luckPermsValue.isPresent()) {
            return luckPermsValue.get();
        }

        return PermissionUtils.has(player, permission);
    }

    private Optional<Boolean> resolveLuckPermsPermission(Player player, String permission) {
        try {
            Object adapter = resolveLuckPermsPlayerAdapter();
            Object user = invokeCompatible(adapter, "getUser", player);
            Optional<Boolean> directUserValue = resolveLuckPermsUserNodePermission(user, permission);
            if (directUserValue.isPresent()) {
                return directUserValue;
            }

            Object permissionData = invokeCompatible(adapter, "getPermissionData", player);
            Optional<Boolean> adapterValue = resolveLuckPermsPermissionData(permissionData, permission);
            if (adapterValue.isPresent()) {
                return adapterValue;
            }

            Object cachedData = invokeNoArg(user, "getCachedData", "cachedData");
            Object cachedPermissionData = invokeNoArg(cachedData, "getPermissionData", "permissionData");
            return resolveLuckPermsPermissionData(cachedPermissionData, permission);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return Optional.empty();
        }
    }

    private Optional<Boolean> resolveLuckPermsCommandOverride(Player player, String permission) {
        Map<String, PermissionOverride> overrides = luckPermsCommandOverrides.get(player.getUniqueId());
        if (overrides == null || overrides.isEmpty()) {
            return Optional.empty();
        }

        long now = System.currentTimeMillis();
        overrides.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().expiresAt() <= now);
        if (overrides.isEmpty()) {
            luckPermsCommandOverrides.remove(player.getUniqueId(), overrides);
            return Optional.empty();
        }

        PermissionOverride override = overrides.get(PermissionUtils.normalizePermissionNode(permission));
        return override == null ? Optional.empty() : Optional.of(override.value());
    }

    private Optional<Boolean> resolveLuckPermsUserNodePermission(Object user, String permission)
            throws ReflectiveOperationException {
        if (user == null) {
            return Optional.empty();
        }

        Object nodesObject = invokeNoArg(user, "getNodes", "nodes");
        if (!(nodesObject instanceof Iterable<?> nodes)) {
            return Optional.empty();
        }

        String normalized = PermissionUtils.normalizePermissionNode(permission);
        boolean matchedTrue = false;
        for (Object node : nodes) {
            if (node == null || isLuckPermsNodeExpired(node)) {
                continue;
            }

            String key = readStringNoArg(node, "getKey", "key", "getPermission", "permission");
            String normalizedKey = PermissionUtils.normalizePermissionNode(key);
            if (!permissionMatches(normalizedKey, normalized)) {
                continue;
            }

            Object value = invokeNoArg(node, "getValue", "value");
            boolean granted = !(value instanceof Boolean booleanValue) || booleanValue;
            if (!granted) {
                return Optional.of(false);
            }
            matchedTrue = true;
        }
        return matchedTrue ? Optional.of(true) : Optional.empty();
    }

    private boolean isLuckPermsNodeExpired(Object node) throws ReflectiveOperationException {
        Object expired = invokeNoArg(node, "hasExpired", "isExpired");
        return expired instanceof Boolean value && value;
    }

    private Optional<Boolean> resolveLuckPermsPermissionData(Object permissionData, String permission)
            throws ReflectiveOperationException {
        if (permissionData == null) {
            return Optional.empty();
        }

        String normalized = PermissionUtils.normalizePermissionNode(permission);
        boolean checked = false;
        for (String candidate : List.of(permission, normalized)) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }

            Object queryResult = invokeCompatible(permissionData, "queryPermission", candidate);
            Optional<Boolean> queriedValue = resolveLuckPermsQueryResult(queryResult);
            if (queriedValue.orElse(false)) {
                return Optional.of(true);
            }
            checked |= queriedValue.isPresent();

            Object result = invokeCompatible(permissionData, "checkPermission", candidate);
            Optional<Boolean> value = resolveLuckPermsTristate(result);
            if (value.orElse(false)) {
                return Optional.of(true);
            }
            checked |= value.isPresent();
        }

        Optional<Boolean> mappedValue = resolveLuckPermsPermissionMap(permissionData, normalized);
        if (mappedValue.isPresent()) {
            return mappedValue;
        }
        return checked ? Optional.of(false) : Optional.empty();
    }

    private Optional<Boolean> resolveLuckPermsQueryResult(Object value) throws ReflectiveOperationException {
        if (value == null) {
            return Optional.empty();
        }

        Optional<Boolean> direct = resolveLuckPermsTristate(value);
        if (direct.isPresent()) {
            return direct;
        }

        Object result = invokeNoArg(value, "result", "getResult");
        Optional<Boolean> nested = resolveLuckPermsTristate(result);
        if (nested.isPresent()) {
            return nested;
        }

        result = invokeNoArg(value, "value", "getValue");
        return resolveLuckPermsTristate(result);
    }

    private Optional<Boolean> resolveLuckPermsPermissionMap(Object permissionData, String normalizedPermission)
            throws ReflectiveOperationException {
        Object mapObject = invokeNoArg(permissionData, "getPermissionMap", "permissionMap");
        if (!(mapObject instanceof Map<?, ?> permissions) || permissions.isEmpty()) {
            return Optional.empty();
        }

        boolean matchedTrue = false;
        for (Map.Entry<?, ?> entry : permissions.entrySet()) {
            String granted = PermissionUtils.normalizePermissionNode(String.valueOf(entry.getKey()));
            if (!permissionMatches(granted, normalizedPermission)) {
                continue;
            }

            Object rawValue = entry.getValue();
            boolean value = rawValue instanceof Boolean booleanValue
                    ? booleanValue
                    : Boolean.parseBoolean(String.valueOf(rawValue));
            if (!value) {
                return Optional.of(false);
            }
            matchedTrue = true;
        }
        return matchedTrue ? Optional.of(true) : Optional.empty();
    }

    private boolean permissionMatches(String granted, String requested) {
        if (granted == null || requested == null || granted.isBlank() || requested.isBlank()) {
            return false;
        }
        if (granted.equals(requested) || granted.equals("*")) {
            return true;
        }
        if (!granted.endsWith(".*")) {
            return false;
        }
        String prefix = granted.substring(0, granted.length() - 1);
        return requested.startsWith(prefix);
    }

    private Optional<Boolean> resolveLuckPermsTristate(Object value) throws ReflectiveOperationException {
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Boolean booleanValue) {
            return Optional.of(booleanValue);
        }

        String name = value instanceof Enum<?> enumValue
                ? enumValue.name()
                : readStringNoArg(value, "name", "toString");
        if (name != null) {
            if (name.equalsIgnoreCase("TRUE")) {
                return Optional.of(true);
            }
            if (name.equalsIgnoreCase("FALSE") || name.equalsIgnoreCase("UNDEFINED")) {
                return Optional.of(false);
            }
        }

        Object booleanValue = invokeNoArg(value, "asBoolean", "asBooleanValue");
        return booleanValue instanceof Boolean result ? Optional.of(result) : Optional.empty();
    }

    private String resolveLuckPermsPrefix(Player player) {
        try {
            Object adapter = resolveLuckPermsPlayerAdapter();
            Object metaData = invokeCompatible(adapter, "getMetaData", player);
            String prefix = readStringNoArg(metaData, "getPrefix", "prefix");
            if (prefix != null && !prefix.isBlank()) {
                return prefix;
            }

            Object user = invokeCompatible(adapter, "getUser", player);
            Object cachedData = invokeNoArg(user, "getCachedData", "cachedData");
            Object cachedMetaData = invokeNoArg(cachedData, "getMetaData", "metaData");
            prefix = readStringNoArg(cachedMetaData, "getPrefix", "prefix");
            return prefix == null || prefix.isBlank() ? null : prefix;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private Object resolveLuckPermsPlayerAdapter() throws ReflectiveOperationException {
        Class<?> providerClass = findOptionalPluginClass("LuckPerms", "net.luckperms.api.LuckPermsProvider");
        Object luckPerms = invokeStaticNoArg(providerClass, "get");
        return invokeCompatible(luckPerms, "getPlayerAdapter", Player.class);
    }

    private void invalidateLuckPermsCachedData(Object user) throws ReflectiveOperationException {
        Object cachedData = invokeNoArg(user, "getCachedData", "cachedData");
        invokeNoArgIfPresent(cachedData, "invalidate");
        invokeNoArgIfPresent(cachedData, "invalidatePermissionCalculators");
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

    private String parseTabText(String text, Player player) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String resolved = text;
        if (ColorUtils.hasPAPI()) {
            try {
                resolved = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, resolved);
            } catch (Exception ignored) {
            }
        }

        resolved = applyInternalPlaceholders(resolved, player);
        return ColorUtils.colorize(stripUnsupportedHeadTags(resolved));
    }

    private Component parseTabComponent(String text, Player player) {
        if (text == null || text.isBlank()) {
            return null;
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
            return MINI_MESSAGE.deserialize(resolved, headTagResolver(player));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private TagResolver headTagResolver(Player player) {
        return TagResolver.resolver("head", (arguments, context) -> {
            String source = arguments.hasNext() ? arguments.pop().value() : player.getName();
            if (source == null || source.isBlank()) {
                source = player.getName();
            }

            AdventureHeadComponentBridge.HeadBuilder builder = headComponentBridge.newPlayerHeadBuilder();
            if (builder == null) {
                return Tag.inserting(Component.empty());
            }

            boolean selfHead = isSelfHeadSource(source, player);

            // Resolve target UUID and player object
            UUID targetId = null;
            Player targetPlayer = null;
            if (selfHead) {
                targetId = player.getUniqueId();
                targetPlayer = player;
            } else {
                targetId = parseUuid(source);
                if (targetId == null) {
                    Player p = Bukkit.getPlayer(source);
                    if (p != null) {
                        targetId = p.getUniqueId();
                        targetPlayer = p;
                    }
                } else {
                    targetPlayer = Bukkit.getPlayer(targetId);
                }
            }

            // Check if we have a cached custom texture (SkinsRestorer) for the target player
            SkinTexture skinTexture = targetId != null ? skinHeadTextures.get(targetId) : null;
            if (skinTexture != null && skinTexture.isValid()) {
                builder.name(targetPlayer != null ? targetPlayer.getName() : source);
                builder.id(targetId);
                builder.profileProperty(skinTexture.value(), skinTexture.signature());
            } else {
                SkinTexture profileTexture = targetPlayer != null ? resolveGameProfileTexture(targetPlayer) : null;
                if (profileTexture != null && profileTexture.isValid()) {
                    builder.name(targetPlayer.getName());
                    builder.id(targetPlayer.getUniqueId());
                    builder.profileProperty(profileTexture.value(), profileTexture.signature());
                    return Tag.inserting(builder.buildComponent());
                }

                // Fallback to Paper's default skin application if it's the player themselves
                boolean paperSkinApplied = selfHead && builder.applyPaperSkin(player);
                if (!paperSkinApplied) {
                    builder.name(source);
                    if (targetId != null) {
                        builder.id(targetId);
                    } else if (selfHead) {
                        builder.id(player.getUniqueId());
                    }
                }
            }

            return Tag.inserting(builder.buildComponent());
        });
    }

    private boolean isSelfHeadSource(String source, Player player) {
        if (source == null || source.isBlank()) {
            return true;
        }

        if (source.equalsIgnoreCase(player.getName())) {
            return true;
        }

        HideManager hideManager = plugin.getHideManager();
        if (hideManager != null && source.equalsIgnoreCase(hideManager.plainPublicName(player))) {
            return true;
        }

        UUID sourceId = parseUuid(source);
        return sourceId != null && sourceId.equals(player.getUniqueId());
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void refreshSkinHeadTextureIfNeeded(Player player, String text, boolean force) {
        if (player == null || !player.isOnline() || !isEnabled() || !usesConfiguredSkinHead(text)) {
            return;
        }

        if (!config().getBoolean("TABLIST.REFRESH-SKIN-HEADS", true)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastRefresh = skinHeadTextureRefreshTimes.get(playerId);
        boolean hasTexture = skinHeadTextures.containsKey(playerId);
        long interval = hasTexture ? SKIN_HEAD_TEXTURE_REFRESH_INTERVAL_MS : SKIN_HEAD_TEXTURE_RETRY_INTERVAL_MS;
        if (!force && lastRefresh != null && now - lastRefresh < interval) {
            return;
        }

        if (!pendingSkinHeadTextureRefreshes.add(playerId)) {
            return;
        }

        SkinTexture paperAppliedTexture = resolvePaperAppliedSkinTexture(player);
        if (paperAppliedTexture != null && paperAppliedTexture.isValid()) {
            cacheAndApplySkinTexture(player, paperAppliedTexture, force, false);
            pendingSkinHeadTextureRefreshes.remove(playerId);
            return;
        }

        SkinTexture skinsRestorerTexture = resolveSkinsRestorerStoredTextureSync(playerId);
        if (skinsRestorerTexture != null && skinsRestorerTexture.isValid()) {
            cacheAndApplySkinTexture(player, skinsRestorerTexture, force, true);
            pendingSkinHeadTextureRefreshes.remove(playerId);
            return;
        }

        SkinTexture profileTexture = resolveGameProfileTexture(player);
        if (profileTexture != null && profileTexture.isValid()) {
            cacheAndApplySkinTexture(player, profileTexture, force, false);
            pendingSkinHeadTextureRefreshes.remove(playerId);
            return;
        }

        String playerName = player.getName();
        plugin.getFoliaScheduler().runAsync(() -> {
            SkinTexture skinTexture = null;
            try {
                skinTexture = resolveSkinsRestorerTexture(playerId, playerName);
            } catch (RuntimeException | LinkageError ignored) {
            }

            SkinTexture resolvedTexture = skinTexture;
            plugin.getFoliaScheduler().runGlobal(() -> finishSkinHeadTextureRefresh(playerId, resolvedTexture, force));
        });
    }

    private void finishSkinHeadTextureRefresh(UUID playerId, SkinTexture skinTexture, boolean force) {
        try {
            skinHeadTextureRefreshTimes.put(playerId, System.currentTimeMillis());

            Player online = Bukkit.getPlayer(playerId);
            if (online == null || !online.isOnline() || !isEnabled()) {
                return;
            }

            if (skinTexture != null && skinTexture.isValid()) {
                boolean changed = cacheAndApplySkinTexture(online, skinTexture, force, true);
                if (changed) {
                    updateTablistName(online);
                }
            } else if (clearCachedSkinTexture(online, force)) {
                updateTablistName(online);
            }
        } finally {
            pendingSkinHeadTextureRefreshes.remove(playerId);
        }
    }

    private SkinTexture resolveSkinsRestorerTexture(UUID playerId, String playerName) {
        if (Bukkit.isPrimaryThread()) {
            return null;
        }

        SkinTexture apiTexture = resolveSkinsRestorerApiTexture(playerId, playerName);
        if (apiTexture != null && apiTexture.isValid()) {
            return apiTexture;
        }

        try {
            Class<?> providerClass = findSkinsRestorerClass("net.skinsrestorer.api.SkinsRestorerProvider");
            Object skinsRestorer = invokeStaticNoArg(providerClass, "get");
            if (skinsRestorer == null) {
                return null;
            }

            Object playerStorage = invokeNoArg(skinsRestorer, "getPlayerStorage", "playerStorage");
            Object skinStorage = invokeNoArg(skinsRestorer, "getSkinStorage", "skinStorage");
            SkinTexture texture = resolveSkinsRestorerStoredTexture(skinsRestorer, playerStorage, playerId);
            if (texture != null && texture.isValid()) {
                return texture;
            }

            texture = resolveSkinsRestorerPlayerStorageTexture(
                    playerStorage,
                    skinStorage,
                    playerId,
                    playerName
            );
            if (texture != null && texture.isValid()) {
                return texture;
            }

            return null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private SkinTexture resolveSkinsRestorerStoredTexture(Object skinsRestorer, Object playerStorage, UUID playerId)
            throws ReflectiveOperationException {
        if (skinsRestorer == null || playerStorage == null) {
            return null;
        }

        SkinTexture texture = null;
        try {
            texture = extractSkinTexture(unwrapOptional(invokeCompatible(playerStorage, "getSkinOfPlayer", playerId)));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
        if (texture != null && texture.isValid()) {
            return texture;
        }

        Object skinId = null;
        try {
            skinId = unwrapOptional(invokeCompatible(playerStorage, "getSkinIdOfPlayer", playerId));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
        if (skinId == null) {
            return null;
        }

        Object skinStorage = invokeNoArg(skinsRestorer, "getSkinStorage", "skinStorage");
        if (skinStorage == null) {
            return null;
        }

        try {
            texture = extractSkinTexture(unwrapOptional(invokeCompatible(skinStorage, "getSkinDataByIdentifier", skinId)));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            texture = null;
        }
        if (texture != null && texture.isValid()) {
            return texture;
        }

        try {
            texture = extractSkinTexture(unwrapOptional(invokeCompatible(skinStorage, "findSkinData", String.valueOf(skinId))));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            texture = null;
        }
        return texture != null && texture.isValid() ? texture : null;
    }

    private SkinTexture resolveSkinsRestorerPlayerStorageTexture(
            Object playerStorage,
            Object skinStorage,
            UUID playerId,
            String playerName
    )
            throws ReflectiveOperationException {
        if (playerStorage == null) {
            return null;
        }

        // This can contact Mojang through SkinsRestorer, so callers must keep it off the server thread.
        List<Object[]> attempts = List.of(
                new Object[]{playerId, playerName, false},
                new Object[]{playerId, playerName},
                new Object[]{playerId, playerName, true},
                new Object[]{playerId, true},
                new Object[]{playerId},
                new Object[]{playerName, true},
                new Object[]{playerName}
        );
        for (Object[] args : attempts) {
            try {
                Object result = unwrapOptional(invokeCompatible(playerStorage, "getSkinForPlayer", args));
                SkinTexture texture = extractSkinTexture(result);
                if (texture != null && texture.isValid()) {
                    return texture;
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        }

        if (skinStorage != null) {
            for (Object[] args : attempts) {
                try {
                    Object skinId = unwrapOptional(invokeCompatible(playerStorage, "getSkinIdForPlayer", args));
                    SkinTexture texture = resolveSkinStorageIdentifierTexture(skinStorage, skinId);
                    if (texture != null && texture.isValid()) {
                        return texture;
                    }
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                }
            }
        }

        return null;
    }

    private SkinTexture resolveSkinStorageIdentifierTexture(Object skinStorage, Object skinId)
            throws ReflectiveOperationException {
        skinId = unwrapOptional(skinId);
        if (skinStorage == null || skinId == null) {
            return null;
        }

        SkinTexture texture = extractSkinTexture(unwrapOptional(invokeCompatible(
                skinStorage,
                "getSkinDataByIdentifier",
                skinId
        )));
        if (texture != null && texture.isValid()) {
            return texture;
        }

        texture = extractSkinTexture(unwrapOptional(invokeCompatible(skinStorage, "findSkinData", String.valueOf(skinId))));
        return texture != null && texture.isValid() ? texture : null;
    }

    private SkinTexture resolveSkinsRestorerStoredTextureSync(UUID playerId) {
        try {
            Class<?> providerClass = findSkinsRestorerClass("net.skinsrestorer.api.SkinsRestorerProvider");
            Object skinsRestorer = invokeStaticNoArg(providerClass, "get");
            if (skinsRestorer == null) {
                return null;
            }

            Object playerStorage = invokeNoArg(skinsRestorer, "getPlayerStorage", "playerStorage");
            return resolveSkinsRestorerStoredTexture(skinsRestorer, playerStorage, playerId);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private SkinTexture resolveSkinsRestorerCurrentTextureSync(UUID playerId, String playerName) {
        if (playerId == null || playerName == null || playerName.isBlank()) {
            return null;
        }

        SkinTexture apiTexture = resolveSkinsRestorerApiTexture(playerId, playerName);
        if (apiTexture != null && apiTexture.isValid()) {
            return apiTexture;
        }

        try {
            Class<?> providerClass = findSkinsRestorerClass("net.skinsrestorer.api.SkinsRestorerProvider");
            Object skinsRestorer = invokeStaticNoArg(providerClass, "get");
            if (skinsRestorer == null) {
                return null;
            }

            Object playerStorage = invokeNoArg(skinsRestorer, "getPlayerStorage", "playerStorage");
            Object skinStorage = invokeNoArg(skinsRestorer, "getSkinStorage", "skinStorage");
            SkinTexture texture = resolveSkinsRestorerPlayerStorageTexture(
                    playerStorage,
                    skinStorage,
                    playerId,
                    playerName
            );
            return texture != null && texture.isValid() ? texture : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private SkinTexture resolveSkinsRestorerApiTexture(UUID playerId, String playerName) {
        try {
            SkinTexture texture = SkinsRestorerSkinLookup.resolve(playerId, playerName);
            return texture != null && texture.isValid() ? texture : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private boolean cacheAndApplySkinTexture(
            Player player,
            SkinTexture skinTexture,
            boolean force,
            boolean fromSkinsRestorer
    ) {
        if (player == null || !player.isOnline() || skinTexture == null || !skinTexture.isValid()) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        skinHeadTextureRefreshTimes.put(playerId, System.currentTimeMillis());
        SkinTexture previous = cacheSkinTexture(playerId, skinTexture, fromSkinsRestorer);
        boolean applied = applySkinTexture(player, skinTexture);
        boolean changed = force || applied || !skinTexture.equals(previous);
        if (changed) {
            refreshTablistAvatar(player);
        }
        return changed;
    }

    private boolean clearCachedSkinTexture(Player player, boolean force) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        skinHeadTextureRefreshTimes.put(playerId, System.currentTimeMillis());
        SkinTexture previous = removeCachedSkinTexture(playerId);
        boolean changed = force || previous != null;
        if (changed) {
            refreshTablistAvatar(player);
            update(player);
        }
        return changed;
    }

    private SkinTexture cacheSkinTexture(UUID playerId, SkinTexture skinTexture, boolean fromSkinsRestorer) {
        if (playerId == null) {
            return null;
        }
        SkinTexture previous = skinHeadTextures.put(playerId, skinTexture);
        if (fromSkinsRestorer) {
            skinsRestorerSkinHeadTextures.add(playerId);
        } else {
            skinsRestorerSkinHeadTextures.remove(playerId);
        }
        return previous;
    }

    private SkinTexture removeCachedSkinTexture(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        skinsRestorerSkinHeadTextures.remove(playerId);
        return skinHeadTextures.remove(playerId);
    }

    private SkinTexture resolvePaperAppliedSkinTexture(Player player) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        AdventureHeadComponentBridge.HeadBuilder builder = headComponentBridge.newPlayerHeadBuilder();
        if (builder == null || !builder.applyPaperSkin(player)) {
            return null;
        }

        try {
            SkinTexture texture = extractSkinTexture(builder.buildContents());
            return texture != null && texture.isValid() ? texture : null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private SkinTexture resolveGameProfileTexture(Player player) {
        try {
            for (Object profile : resolveGameProfiles(player)) {
                SkinTexture texture = resolveProfileTexture(profile);
                if (texture != null && texture.isValid()) {
                    return texture;
                }
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
        return null;
    }

    private SkinTexture resolveMojangSessionSkinTexture(UUID playerId) {
        if (playerId == null) {
            return null;
        }

        URI uri = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/"
                + playerId.toString().replace("-", "")
                + "?unsigned=false");
        try {
            JsonObject root = fetchJsonObject(uri);
            return root == null ? null : extractMojangSessionTexture(root);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private SkinTexture resolveMojangNamedSkinTexture(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return null;
        }

        UUID mojangUuid = resolveMojangUuidByName(playerName);
        return mojangUuid == null ? null : resolveMojangSessionSkinTexture(mojangUuid);
    }

    private UUID resolveMojangUuidByName(String playerName) {
        try {
            JsonObject root = fetchJsonObject(URI.create(
                    "https://api.mojang.com/users/profiles/minecraft/" + playerName
            ));
            if (root == null || !root.has("id")) {
                return null;
            }
            return uuidFromCompactString(root.get("id").getAsString());
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private JsonObject fetchJsonObject(URI uri) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(4000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                return null;
            }

            try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (!parsed.isJsonObject()) {
                    return null;
                }
                return parsed.getAsJsonObject();
            }
        } catch (RuntimeException | java.io.IOException | LinkageError ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private UUID uuidFromCompactString(String rawUuid) {
        if (rawUuid == null || rawUuid.isBlank()) {
            return null;
        }

        String compact = rawUuid.replace("-", "");
        if (compact.length() != 32) {
            return null;
        }

        return UUID.fromString(compact.substring(0, 8)
                + "-"
                + compact.substring(8, 12)
                + "-"
                + compact.substring(12, 16)
                + "-"
                + compact.substring(16, 20)
                + "-"
                + compact.substring(20));
    }

    private SkinTexture extractMojangSessionTexture(JsonObject root) throws ReflectiveOperationException {
        JsonArray properties = root.getAsJsonArray("properties");
        if (properties == null || properties.isEmpty()) {
            return null;
        }

        for (JsonElement element : properties) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject property = element.getAsJsonObject();
            JsonElement name = property.get("name");
            JsonElement value = property.get("value");
            if (name == null || value == null || !"textures".equalsIgnoreCase(name.getAsString())) {
                continue;
            }
            JsonElement signature = property.get("signature");
            SkinTexture texture = new SkinTexture(
                    value.getAsString(),
                    signature == null || signature.isJsonNull() ? null : signature.getAsString()
            );
            return texture.isValid() ? texture : null;
        }
        return null;
    }

    private SkinTexture resolveUpdatedBukkitProfileTexture(UUID playerId, String playerName) {
        if (playerId == null && (playerName == null || playerName.isBlank())) {
            return null;
        }

        try {
            Object profile = playerName != null && !playerName.isBlank()
                    ? Bukkit.createPlayerProfile(playerName)
                    : Bukkit.createPlayerProfile(playerId);
            Object updateResult = invokeNoArg(profile, "update");
            if (updateResult instanceof CompletableFuture<?> future) {
                Object updatedProfile = future.get(4L, TimeUnit.SECONDS);
                SkinTexture texture = resolveProfileTexture(updatedProfile);
                if (texture != null && texture.isValid()) {
                    return texture;
                }
            }
            return resolveProfileTexture(profile);
        } catch (ReflectiveOperationException
                 | java.util.concurrent.ExecutionException
                 | java.util.concurrent.TimeoutException
                 | InterruptedException
                 | RuntimeException
                 | LinkageError ignored) {
            if (ignored instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    private List<Object> resolveGameProfiles(Player player) throws ReflectiveOperationException {
        List<Object> profiles = new ArrayList<>();
        addProfileCandidate(profiles, invokeNoArg(player, "getPlayerProfile"));
        addProfileCandidate(profiles, invokeNoArg(player, "getProfile", "getGameProfile"));

        Object handle = invokeNoArg(player, "getHandle");
        if (handle != null) {
            addProfileCandidate(profiles, invokeNoArg(handle, "getGameProfile", "getProfile"));
            addProfileCandidate(profiles, findNamedField(handle, "gameProfile", "profile"));
        }

        addProfileCandidate(profiles, findNamedField(player, "gameProfile", "profile"));
        return profiles;
    }

    private void addProfileCandidate(List<Object> profiles, Object profile) {
        if (profile != null && !profiles.contains(profile)) {
            profiles.add(profile);
        }
    }

    private SkinTexture resolveProfileTexture(Object profile) throws ReflectiveOperationException {
        Object propertyMap = invokeNoArg(profile, "getProperties", "properties");
        if (propertyMap == null) {
            return extractSkinTexture(profile);
        }

        SkinTexture iterableTexture = extractFirstTexture(propertyMap);
        if (iterableTexture != null && iterableTexture.isValid()) {
            return iterableTexture;
        }

        Object textures = unwrapOptional(invokeCompatible(propertyMap, "get", "textures"));
        SkinTexture texture = extractFirstTexture(textures);
        if (texture != null && texture.isValid()) {
            return texture;
        }

        Object values = unwrapOptional(invokeNoArg(propertyMap, "values"));
        return extractFirstTexture(values);
    }

    private boolean applySkinTexture(Player player, SkinTexture skinTexture) {
        if (player == null || skinTexture == null || !skinTexture.isValid()) {
            return false;
        }

        boolean changed = applyPaperPlayerProfileTexture(player, skinTexture);
        try {
            for (Object profile : resolveGameProfiles(player)) {
                changed |= applySkinTexture(profile, skinTexture);
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return changed;
        }
        return changed;
    }

    private boolean applyPaperPlayerProfileTexture(Player player, SkinTexture skinTexture) {
        try {
            Object profile = invokeNoArg(player, "getPlayerProfile");
            if (profile == null) {
                return false;
            }

            SkinTexture current = resolveProfileTexture(profile);
            if (skinTexture.equals(current)) {
                return false;
            }

            Object property = createPaperProfileProperty(profile.getClass().getClassLoader(), skinTexture);
            boolean removed = invokeCompatibleIfPresent(profile, "removeProperty", "textures");
            boolean set = invokeCompatibleIfPresent(profile, "setProperty", property);
            boolean applied = invokeCompatibleIfPresent(player, "setPlayerProfile", profile);
            return removed || set || applied;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private boolean applySkinTexture(Object profile, SkinTexture skinTexture) throws ReflectiveOperationException {
        Object propertyMap = invokeNoArg(profile, "getProperties", "properties");
        if (propertyMap == null) {
            return false;
        }

        SkinTexture current = resolveProfileTexture(profile);
        if (skinTexture.equals(current)) {
            return false;
        }

        Object property = createProfileProperty(propertyMap.getClass().getClassLoader(), skinTexture);
        boolean removed = invokeCompatibleIfPresent(propertyMap, "removeAll", "textures");
        boolean added = invokeCompatibleIfPresent(propertyMap, "put", "textures", property);
        return added || removed;
    }

    private Object createPaperProfileProperty(ClassLoader preferredLoader, SkinTexture skinTexture)
            throws ReflectiveOperationException {
        List<ClassLoader> loaders = new ArrayList<>();
        if (preferredLoader != null) {
            loaders.add(preferredLoader);
        }
        loaders.add(Bukkit.class.getClassLoader());
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null && !loaders.contains(contextLoader)) {
            loaders.add(contextLoader);
        }

        ClassNotFoundException missing = null;
        for (ClassLoader loader : loaders) {
            try {
                Class<?> propertyClass = Class.forName("com.destroystokyo.paper.profile.ProfileProperty", false, loader);
                for (Constructor<?> constructor : propertyClass.getDeclaredConstructors()) {
                    Class<?>[] parameters = constructor.getParameterTypes();
                    if (parameters.length == 3
                            && parameters[0] == String.class
                            && parameters[1] == String.class
                            && parameters[2] == String.class) {
                        constructor.setAccessible(true);
                        return constructor.newInstance("textures", skinTexture.value(), skinTexture.signature());
                    }
                    if (parameters.length == 2
                            && parameters[0] == String.class
                            && parameters[1] == String.class) {
                        constructor.setAccessible(true);
                        return constructor.newInstance("textures", skinTexture.value());
                    }
                }
            } catch (ClassNotFoundException exception) {
                missing = exception;
            }
        }

        throw missing == null
                ? new ClassNotFoundException("com.destroystokyo.paper.profile.ProfileProperty")
                : missing;
    }

    private Object createProfileProperty(ClassLoader preferredLoader, SkinTexture skinTexture)
            throws ReflectiveOperationException {
        List<ClassLoader> loaders = new ArrayList<>();
        if (preferredLoader != null) {
            loaders.add(preferredLoader);
        }
        loaders.add(Bukkit.class.getClassLoader());
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null && !loaders.contains(contextLoader)) {
            loaders.add(contextLoader);
        }

        ClassNotFoundException missing = null;
        for (ClassLoader loader : loaders) {
            try {
                Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property", false, loader);
                for (Constructor<?> constructor : propertyClass.getDeclaredConstructors()) {
                    Class<?>[] parameters = constructor.getParameterTypes();
                    if (parameters.length == 3
                            && parameters[0] == String.class
                            && parameters[1] == String.class
                            && parameters[2] == String.class) {
                        constructor.setAccessible(true);
                        return constructor.newInstance("textures", skinTexture.value(), skinTexture.signature());
                    }
                    if (parameters.length == 2
                            && parameters[0] == String.class
                            && parameters[1] == String.class) {
                        constructor.setAccessible(true);
                        return constructor.newInstance("textures", skinTexture.value());
                    }
                }
            } catch (ClassNotFoundException exception) {
                missing = exception;
            }
        }

        throw missing == null
                ? new ClassNotFoundException("com.mojang.authlib.properties.Property")
                : missing;
    }

    private void refreshTablistAvatar(Player player) {
        if (player == null || !player.isOnline() || !isEnabled() || !componentUpdater.refreshAvatar(player)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        plugin.getFoliaScheduler().runEntityLater(player, () -> {
            Player online = Bukkit.getPlayer(playerId);
            if (online != null && online.isOnline() && isEnabled()) {
                updateTablistName(online);
            }
        }, 1L);
    }

    private SkinTexture extractFirstTexture(Object textures) throws ReflectiveOperationException {
        textures = unwrapOptional(textures);
        if (textures == null) {
            return null;
        }

        if (textures instanceof Iterable<?> iterable) {
            for (Object property : iterable) {
                SkinTexture texture = extractSkinTexture(property);
                if (texture != null && texture.isValid()) {
                    return texture;
                }
            }
            return null;
        }

        if (textures.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(textures);
            for (int index = 0; index < length; index++) {
                SkinTexture texture = extractSkinTexture(java.lang.reflect.Array.get(textures, index));
                if (texture != null && texture.isValid()) {
                    return texture;
                }
            }
            return null;
        }

        return extractSkinTexture(textures);
    }

    private SkinTexture extractSkinTexture(Object source) throws ReflectiveOperationException {
        source = unwrapOptional(source);
        if (source == null) {
            return null;
        }

        if (source instanceof Map<?, ?> map) {
            SkinTexture texture = extractSkinTextureFromMap(map);
            if (texture != null && texture.isValid()) {
                return texture;
            }
        }

        if (source instanceof Iterable<?> || source.getClass().isArray()) {
            return extractFirstTexture(source);
        }

        String propertyName = readStringNoArg(source, "getName", "name");
        String value = readStringNoArg(source, "getValue", "value", "getTexture", "texture");
        String signature = readStringNoArg(source, "getSignature", "signature");
        if (value != null && !value.isBlank()
                && (propertyName == null || propertyName.equalsIgnoreCase("textures"))) {
            return new SkinTexture(value, signature);
        }

        for (String methodName : List.of(
                "getProperty",
                "property",
                "getSkinProperty",
                "skinProperty",
                "getProfileProperty",
                "profileProperty",
                "getProfileProperties",
                "profileProperties",
                "getTextures",
                "textures",
                "getSkinData",
                "skinData",
                "getSkinProfile",
                "skinProfile"
        )) {
            SkinTexture nested = extractSkinTexture(unwrapOptional(invokeNoArg(source, methodName)));
            if (nested != null && nested.isValid()) {
                return nested;
            }
        }

        return null;
    }

    private SkinTexture extractSkinTextureFromMap(Map<?, ?> map) throws ReflectiveOperationException {
        Object propertyNameValue = findMapValue(map, "name", "propertyName", "property");
        Object rawValue = findMapValue(map, "value", "texture", "textures");
        Object rawSignature = findMapValue(map, "signature");

        SkinTexture nestedTexture = extractSkinTexture(rawValue);
        if (nestedTexture != null && nestedTexture.isValid()) {
            return nestedTexture;
        }

        String propertyName = stringValue(propertyNameValue);
        String value = stringValue(rawValue);
        String signature = stringValue(rawSignature);
        if (value != null && !value.isBlank()
                && (propertyName == null || propertyName.equalsIgnoreCase("textures"))) {
            return new SkinTexture(value, signature);
        }

        return extractFirstTexture(map.values());
    }

    private Object findMapValue(Map<?, ?> map, String... wantedKeys) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Set<String> wanted = new LinkedHashSet<>();
        for (String key : wantedKeys) {
            wanted.add(key.toLowerCase(Locale.ROOT));
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = unwrapOptional(entry.getKey());
            if (key != null && wanted.contains(String.valueOf(key).toLowerCase(Locale.ROOT))) {
                return unwrapOptional(entry.getValue());
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        value = unwrapOptional(value);
        return value instanceof CharSequence sequence ? sequence.toString() : null;
    }

    private Object invokeStaticNoArg(Class<?> type, String methodName) throws ReflectiveOperationException {
        Method method = type.getMethod(methodName);
        method.setAccessible(true);
        return method.invoke(null);
    }

    private Class<?> findSkinsRestorerClass(String className) throws ClassNotFoundException {
        return findOptionalPluginClass("SkinsRestorer", className);
    }

    private Class<?> findOptionalPluginClass(String pluginName, String className) throws ClassNotFoundException {
        ClassNotFoundException missing = null;
        for (ClassLoader loader : optionalPluginClassLoaders(pluginName)) {
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException exception) {
                missing = exception;
            }
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw missing == null ? exception : missing;
        }
    }

    private List<ClassLoader> skinsRestorerClassLoaders() {
        return optionalPluginClassLoaders("SkinsRestorer");
    }

    private List<ClassLoader> optionalPluginClassLoaders(String pluginName) {
        List<ClassLoader> loaders = new ArrayList<>();
        addClassLoader(loaders, Thread.currentThread().getContextClassLoader());
        addClassLoader(loaders, getClass().getClassLoader());
        addClassLoader(loaders, Bukkit.class.getClassLoader());

        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            for (Plugin pluginCandidate : Bukkit.getPluginManager().getPlugins()) {
                if (pluginCandidate != null && pluginName.equalsIgnoreCase(pluginCandidate.getName())) {
                    plugin = pluginCandidate;
                    break;
                }
            }
        }
        if (plugin != null) {
            addClassLoader(loaders, plugin.getClass().getClassLoader());
        }
        return loaders;
    }

    private void addClassLoader(List<ClassLoader> loaders, ClassLoader loader) {
        if (loader != null && !loaders.contains(loader)) {
            loaders.add(loader);
        }
    }

    private Object invokeNoArg(Object target, String... methodNames) throws ReflectiveOperationException {
        if (target == null) {
            return null;
        }

        for (String methodName : methodNames) {
            Method method = findNoArgMethod(target.getClass(), methodName);
            if (method != null) {
                method.setAccessible(true);
                return method.invoke(target);
            }
        }
        return null;
    }

    private boolean invokeNoArgIfPresent(Object target, String methodName) throws ReflectiveOperationException {
        if (target == null) {
            return false;
        }

        Method method = findNoArgMethod(target.getClass(), methodName);
        if (method == null) {
            return false;
        }

        method.setAccessible(true);
        method.invoke(target);
        return true;
    }

    private Method findNoArgMethod(Class<?> type, String methodName) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                    return method;
                }
            }
        }

        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                return method;
            }
        }
        return null;
    }

    private Object invokeCompatible(Object target, String methodName, Object... args) throws ReflectiveOperationException {
        Method method = findCompatibleMethod(target, methodName, args);
        if (method == null) {
            return null;
        }

        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private boolean invokeCompatibleIfPresent(Object target, String methodName, Object... args)
            throws ReflectiveOperationException {
        Method method = findCompatibleMethod(target, methodName, args);
        if (method == null) {
            return false;
        }

        method.setAccessible(true);
        method.invoke(target, args);
        return true;
    }

    private Method findCompatibleMethod(Object target, String methodName, Object... args) {
        if (target == null) {
            return null;
        }

        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != args.length) {
                continue;
            }

            if (canAccept(method.getParameterTypes(), args)) {
                return method;
            }
        }

        for (Class<?> current = target.getClass(); current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != args.length) {
                    continue;
                }

                if (canAccept(method.getParameterTypes(), args)) {
                    return method;
                }
            }
        }

        return null;
    }

    private boolean canAccept(Class<?>[] parameterTypes, Object[] args) {
        for (int index = 0; index < parameterTypes.length; index++) {
            Object arg = args[index];
            if (arg == null) {
                continue;
            }

            Class<?> parameterType = wrapPrimitive(parameterTypes[index]);
            if (!parameterType.isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }

    private Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }

        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }

    private Object findNamedField(Object target, String... names) throws ReflectiveOperationException {
        if (target == null) {
            return null;
        }

        Set<String> wanted = new LinkedHashSet<>();
        for (String name : names) {
            wanted.add(name.toLowerCase(Locale.ROOT));
        }

        for (Class<?> current = target.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!wanted.contains(field.getName().toLowerCase(Locale.ROOT))) {
                    continue;
                }

                field.setAccessible(true);
                Object value = field.get(target);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private String readStringNoArg(Object target, String... methodNames) throws ReflectiveOperationException {
        Object value = invokeNoArg(target, methodNames);
        value = unwrapOptional(value);
        return value instanceof String string ? string : null;
    }

    private Object unwrapOptional(Object value) {
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return value;
    }

    private boolean usesConfiguredSkinHead(String text) {
        if (text != null && HEAD_TAG_PATTERN.matcher(text).find()) {
            return true;
        }

        String nameFormat = config().getString("TABLIST.NAME-FORMAT", "");
        return nameFormat != null && HEAD_TAG_PATTERN.matcher(nameFormat).find();
    }

    private String stripUnsupportedHeadTags(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return HEAD_TAG_PATTERN.matcher(text).replaceAll("");
    }

    private List<Long> getSkinHeadRefreshDelays() {
        List<Long> delays = plugin.getConfigManager().getConfig()
                .getLongList("TABLIST.SKIN-HEAD-REFRESH-DELAYS");
        if (delays.isEmpty()) {
            return DEFAULT_SKIN_HEAD_REFRESH_DELAYS;
        }

        ArrayList<Long> sanitized = new ArrayList<>();
        sanitized.addAll(DEFAULT_SKIN_HEAD_REFRESH_DELAYS);
        for (long delay : delays) {
            if (delay >= 1L) {
                sanitized.add(delay);
            }
        }
        return sanitized.stream().distinct().sorted().toList();
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

    private record PermissionOverride(boolean value, long expiresAt) {
    }
}
