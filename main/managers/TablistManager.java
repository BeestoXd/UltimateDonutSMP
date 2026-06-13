package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.AdventureHeadComponentBridge;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.TablistComponentUpdater;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TablistManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final GsonComponentSerializer GSON_COMPONENT_SERIALIZER = GsonComponentSerializer.gson();
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
    private final boolean inlinePlayerHeadSupported;
    private final Set<UUID> refreshedSkinHeads = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingSkinHeadTextureRefreshes = ConcurrentHashMap.newKeySet();
    private final Map<UUID, SkinTexture> skinHeadTextures = new ConcurrentHashMap<>();
    private final Map<UUID, Long> skinHeadTextureRefreshTimes = new ConcurrentHashMap<>();

    public TablistManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.componentUpdater = new TablistComponentUpdater(plugin);
        this.headComponentBridge = new AdventureHeadComponentBridge();
        this.inlinePlayerHeadSupported = detectInlinePlayerHeadSupport();
    }

    public void updateSkinTexture(UUID playerId, String value, String signature) {
        if (playerId == null) {
            return;
        }
        if (value == null || value.isBlank()) {
            skinHeadTextures.remove(playerId);
            skinHeadTextureRefreshTimes.remove(playerId);
            return;
        }

        SkinTexture texture = new SkinTexture(value, signature);
        skinHeadTextures.put(playerId, texture);
        skinHeadTextureRefreshTimes.put(playerId, System.currentTimeMillis());
        refreshedSkinHeads.add(playerId);

        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            applySkinTexture(player, texture);
            refreshTablistAvatar(player);
            updateTablistName(player);
            update(player);
        }
    }

    SkinTexture resolveSkinTextureForFakePlayer(UUID playerId, String playerName) {
        if (Bukkit.isPrimaryThread()) {
            return null;
        }
        SkinTexture texture = resolveSkinsRestorerTexture(playerId, playerName);
        return texture != null && texture.isValid() ? texture : null;
    }

    SkinTexture resolveOriginalGameProfileSkinTexture(UUID playerId, String playerName) {
        Player online = playerId == null ? null : Bukkit.getPlayer(playerId);
        if (online != null) {
            SkinTexture live = resolveGameProfileTexture(online);
            if (live != null && live.isValid()) {
                return live;
            }
        }
        if (playerName == null || playerName.isBlank()) {
            return null;
        }
        try {
            Object updatedProfile = Bukkit.createPlayerProfile(playerName).update().join();
            SkinTexture texture = resolveProfileTexture(updatedProfile);
            return texture != null && texture.isValid() ? texture : null;
        } catch (RuntimeException | ReflectiveOperationException ignored) {
            return null;
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
        String componentJson = parseTabComponentJson(nameFormat, player);
        if (componentJson != null && componentUpdater.updateName(player, componentJson)) {
            return;
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

        for (long delayTicks : getSkinHeadRefreshDelays()) {
            plugin.getSpigotScheduler().runEntityLater(player, () -> {
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
        skinHeadTextures.remove(playerId);
        skinHeadTextureRefreshTimes.remove(playerId);
    }

    private String resolveNameFormat(Player player) {
        String rawTeamName = plugin.getTeamManager().getTeamName(player);
        boolean showTeam = config().getBoolean("TABLIST.SHOW-TEAM-NAME", true);
        String teamName = showTeam ? rawTeamName : null;
        String prefix = resolvePrefix(player);
        String teamSuffix = "";
        String iconHeadSkin = inlinePlayerHeadSupported
                ? config().getString("TABLIST.ICON-HEAD-SKIN", "<head:%player_name%>")
                : "";
        if (iconHeadSkin != null) {
            String profileId = player.getUniqueId().toString();
            iconHeadSkin = iconHeadSkin
                    .replace("%player_name%", profileId)
                    .replace("%player%", profileId)
                    .replace("<player>", profileId);
        }
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

    private String parseTabComponentJson(String text, Player player) {
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
        if (!inlinePlayerHeadSupported) {
            resolved = stripUnsupportedHeadTags(resolved);
        }

        try {
            Component component = MINI_MESSAGE.deserialize(resolved, headTagResolver(player));
            return GSON_COMPONENT_SERIALIZER.serialize(component);
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
            SkinTexture skinTexture = selfHead ? skinHeadTextures.get(player.getUniqueId()) : null;
            if (skinTexture != null && skinTexture.isValid()) {
                builder.name(player.getName());
                builder.id(player.getUniqueId());
                builder.profileProperty(skinTexture.value(), skinTexture.signature());
            } else {
                boolean paperSkinApplied = selfHead && builder.applyPaperSkin(player);
                if (paperSkinApplied) {
                    return Tag.inserting(builder.buildComponent());
                }

                builder.name(source);
                UUID id = parseUuid(source);
                if (id != null) {
                    builder.id(id);
                } else if (selfHead) {
                    builder.id(player.getUniqueId());
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

        SkinTexture profileTexture = resolveGameProfileTexture(player);
        if (profileTexture != null && profileTexture.isValid()) {
            skinHeadTextureRefreshTimes.put(playerId, now);
            SkinTexture previous = skinHeadTextures.put(playerId, profileTexture);
            boolean applied = applySkinTexture(player, profileTexture);
            if (force || applied || !profileTexture.equals(previous)) {
                refreshTablistAvatar(player);
            }
            pendingSkinHeadTextureRefreshes.remove(playerId);
            return;
        }

        String playerName = player.getName();
        plugin.getSpigotScheduler().runAsync(() -> {
            SkinTexture skinTexture = null;
            try {
                skinTexture = resolveSkinsRestorerTexture(playerId, playerName);
            } catch (RuntimeException | LinkageError ignored) {
            }

            SkinTexture resolvedTexture = skinTexture;
            plugin.getSpigotScheduler().runGlobal(() -> finishSkinHeadTextureRefresh(playerId, resolvedTexture, force));
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
                SkinTexture previous = skinHeadTextures.put(playerId, skinTexture);
                boolean applied = applySkinTexture(online, skinTexture);
                if (force || applied || !skinTexture.equals(previous)) {
                    refreshTablistAvatar(online);
                    updateTablistName(online);
                }
            }
        } finally {
            pendingSkinHeadTextureRefreshes.remove(playerId);
        }
    }

    private SkinTexture resolveSkinsRestorerTexture(UUID playerId, String playerName) {
        if (Bukkit.isPrimaryThread()) {
            return null;
        }

        try {
            Class<?> providerClass = Class.forName("net.skinsrestorer.api.SkinsRestorerProvider");
            Object skinsRestorer = invokeStaticNoArg(providerClass, "get");
            if (skinsRestorer == null) {
                return null;
            }

            Object playerStorage = invokeNoArg(skinsRestorer, "getPlayerStorage", "playerStorage");
            SkinTexture texture = resolveSkinsRestorerStoredTexture(skinsRestorer, playerStorage, playerId);
            if (texture != null && texture.isValid()) {
                return texture;
            }

            return resolveSkinsRestorerPlayerStorageTexture(playerStorage, playerId, playerName);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private SkinTexture resolveSkinsRestorerStoredTexture(Object skinsRestorer, Object playerStorage, UUID playerId)
            throws ReflectiveOperationException {
        if (skinsRestorer == null || playerStorage == null) {
            return null;
        }

        Object skinId = unwrapOptional(invokeCompatible(playerStorage, "getSkinIdOfPlayer", playerId));
        if (skinId == null) {
            skinId = unwrapOptional(invokeCompatible(playerStorage, "getSkinIdForPlayer", playerId));
        }
        if (skinId == null) {
            return null;
        }

        Object skinStorage = invokeNoArg(skinsRestorer, "getSkinStorage", "skinStorage");
        if (skinStorage == null) {
            return null;
        }

        SkinTexture texture = extractSkinTexture(unwrapOptional(invokeCompatible(skinStorage, "findSkinData", skinId)));
        if (texture != null && texture.isValid()) {
            return texture;
        }

        texture = extractSkinTexture(unwrapOptional(invokeCompatible(skinStorage, "getSkinDataByIdentifier", skinId)));
        return texture != null && texture.isValid() ? texture : null;
    }

    private SkinTexture resolveSkinsRestorerPlayerStorageTexture(Object playerStorage, UUID playerId, String playerName)
            throws ReflectiveOperationException {
        if (playerStorage == null) {
            return null;
        }

        // This can contact Mojang through SkinsRestorer, so callers must keep it off the server thread.
        List<Object[]> attempts = List.of(
                new Object[]{playerId, playerName, true},
                new Object[]{playerId, playerName},
                new Object[]{playerId, true},
                new Object[]{playerId},
                new Object[]{playerName, true},
                new Object[]{playerName}
        );
        for (Object[] args : attempts) {
            Object result = unwrapOptional(invokeCompatible(playerStorage, "getSkinForPlayer", args));
            SkinTexture texture = extractSkinTexture(result);
            if (texture != null && texture.isValid()) {
                return texture;
            }
        }

        return null;
    }

    SkinTexture resolveLiveGameProfileSkinTexture(Player player) {
        return resolveGameProfileTexture(player);
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

    private List<Object> resolveGameProfiles(Player player) throws ReflectiveOperationException {
        List<Object> profiles = new ArrayList<>();
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

        boolean changed = false;
        try {
            for (Object profile : resolveGameProfiles(player)) {
                changed |= applySkinTexture(profile, skinTexture);
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return changed;
        }
        return changed;
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
        plugin.getSpigotScheduler().runEntityLater(player, () -> {
            Player online = Bukkit.getPlayer(playerId);
            if (online != null && online.isOnline() && isEnabled()) {
                updateTablistName(online);
            }
        }, 1L);
    }

    private SkinTexture extractFirstTexture(Object textures) throws ReflectiveOperationException {
        if (textures instanceof Iterable<?> iterable) {
            for (Object property : iterable) {
                SkinTexture texture = extractSkinTexture(property);
                if (texture != null && texture.isValid()) {
                    return texture;
                }
            }
        }

        return extractSkinTexture(textures);
    }

    private SkinTexture extractSkinTexture(Object source) throws ReflectiveOperationException {
        source = unwrapOptional(source);
        if (source == null) {
            return null;
        }

        String propertyName = readStringNoArg(source, "getName", "name");
        String value = readStringNoArg(source, "getValue", "value", "getTexture", "texture");
        String signature = readStringNoArg(source, "getSignature", "signature");
        if (value != null && !value.isBlank()
                && (propertyName == null || propertyName.equalsIgnoreCase("textures"))) {
            return new SkinTexture(value, signature);
        }

        for (String methodName : List.of("getProperty", "property", "getSkinProperty", "skinProperty")) {
            SkinTexture nested = extractSkinTexture(unwrapOptional(invokeNoArg(source, methodName)));
            if (nested != null && nested.isValid()) {
                return nested;
            }
        }

        return null;
    }

    private Object invokeStaticNoArg(Class<?> type, String methodName) throws ReflectiveOperationException {
        Method method = type.getMethod(methodName);
        method.setAccessible(true);
        return method.invoke(null);
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

        String iconHeadSkin = config().getString("TABLIST.ICON-HEAD-SKIN", "<head:%player_name%>");
        if (iconHeadSkin != null && HEAD_TAG_PATTERN.matcher(iconHeadSkin).find()) {
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

    private boolean detectInlinePlayerHeadSupport() {
        String version = resolveMinecraftVersion();
        int[] parsed = parseVersion(version);
        if (parsed[0] > 1) {
            return true;
        }
        if (parsed[0] == 1 && parsed[1] > 21) {
            return true;
        }
        return parsed[0] == 1 && parsed[1] == 21 && parsed[2] >= 9;
    }

    private String resolveMinecraftVersion() {
        try {
            Method method = Bukkit.class.getMethod("getMinecraftVersion");
            Object value = method.invoke(null);
            if (value instanceof String version && !version.isBlank()) {
                return version;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
        return Bukkit.getBukkitVersion();
    }

    private int[] parseVersion(String version) {
        int[] parsed = new int[]{0, 0, 0};
        if (version == null || version.isBlank()) {
            return parsed;
        }

        String[] parts = version.split("[^0-9]+");
        int index = 0;
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (index >= parsed.length) {
                break;
            }
            try {
                parsed[index++] = Integer.parseInt(part);
            } catch (NumberFormatException ignored) {
                return parsed;
            }
        }
        return parsed;
    }

    record SkinTexture(String value, String signature) {
        boolean isValid() {
            return value != null && !value.isBlank();
        }
    }
}
