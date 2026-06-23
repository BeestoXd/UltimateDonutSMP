package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.HideMode;
import com.bx.ultimateDonutSmp.models.HideState;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class HideManager {

    public static final String SCRAMBLE_PERMISSION = "ultimatedonutsmp.hide.scramble";
    public static final String DISGUISE_PERMISSION = "ultimatedonutsmp.hide.disguise";
    public static final String ADMIN_PERMISSION = "ultimatedonutsmp.hide.admin";
    public static final String BYPASS_PERMISSION = "ultimatedonutsmp.hide.bypass";

    private static final int MAX_GENERATION_ATTEMPTS = 64;

    public enum ResultType {
        SUCCESS,
        DISABLED,
        DEPENDENCY_MISSING,
        NO_PERMISSION,
        IN_COMBAT,
        COOLDOWN,
        INVALID_ALIAS,
        INVALID_SKIN,
        ALIAS_IN_USE,
        NOT_HIDDEN,
        DATABASE_ERROR
    }

    public record Result(ResultType type, HideState state, long remainingSeconds) {
        public boolean success() {
            return type == ResultType.SUCCESS;
        }
    }

    public record AliasOption(String key, String name, String skinKey, String skinUsername) {
    }

    public record SkinOption(String key, String displayName, String username) {
    }

    public record HeadTexture(String value, String signature) {
        public boolean isValid() {
            return value != null && !value.isBlank();
        }
    }

    private final UltimateDonutSmp plugin;
    private final SecureRandom random = new SecureRandom();
    private final ConcurrentMap<UUID, HideState> states = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, UUID> aliasOwners = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Long> lastChanges = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, HeadTexture> originalSkinTextures = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, HeadTexture> headTextures = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CompletableFuture<HeadTexture>> headTextureLoads = new ConcurrentHashMap<>();
    private volatile HidePacketBridge packetBridge;

    public HideManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        logConfigurationProblems();
        this.packetBridge = createPacketBridge();
    }

    public void loadAll() {
        states.clear();
        aliasOwners.clear();
        for (HideState state : plugin.getDatabaseManager().loadAllHideStates()) {
            cache(state);
        }
        for (SkinOption skin : skins().values()) {
            resolveHeadTextureAsync(skin.username());
        }
    }

    public void reload() {
        plugin.getConfigManager().reloadHide();
        logConfigurationProblems();
        loadAll();
        if (packetBridge == null) {
            packetBridge = createPacketBridge();
        }
        refreshAll();
    }

    public void shutdown() {
        for (UUID playerUuid : Set.copyOf(originalSkinTextures.keySet())) {
            Player online = Bukkit.getPlayer(playerUuid);
            if (online != null) {
                restoreOriginalSkin(online);
            }
        }
        if (packetBridge != null) {
            packetBridge.shutdown();
        }
        states.clear();
        aliasOwners.clear();
        lastChanges.clear();
        originalSkinTextures.clear();
        headTextures.clear();
        headTextureLoads.clear();
    }

    public boolean isAvailable() {
        return packetBridge != null;
    }

    public boolean isEnabled() {
        return config().getBoolean("ENABLED", true)
                && (plugin.getFeatureManager() == null
                || plugin.getFeatureManager().isEnabled(FeatureManager.Feature.HIDE));
    }

    public HideState getState(UUID playerUuid) {
        return playerUuid == null ? null : states.get(playerUuid);
    }

    public boolean isHidden(UUID playerUuid) {
        return getState(playerUuid) != null;
    }

    public Collection<HideState> getStates() {
        return states.values().stream()
                .sorted(Comparator.comparing(HideState::alias, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public HideState findState(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = normalize(input);
        UUID aliasOwner = aliasOwners.get(normalized);
        if (aliasOwner != null) {
            return states.get(aliasOwner);
        }
        return states.values().stream()
                .filter(state -> state.realNameSnapshot().equalsIgnoreCase(input))
                .findFirst()
                .orElse(null);
    }

    public String publicName(Player player) {
        return player == null ? "" : publicName(player.getUniqueId(), player.getName());
    }

    public String publicName(UUID playerUuid) {
        Player online = playerUuid == null ? null : Bukkit.getPlayer(playerUuid);
        String fallback = online == null
                ? plugin.getDatabaseManager().getLastKnownUsername(playerUuid)
                : online.getName();
        return publicName(playerUuid, fallback);
    }

    public String publicName(UUID playerUuid, String fallback) {
        HideState state = getState(playerUuid);
        return publicName(state, fallback);
    }

    public String publicName(HideState state) {
        return publicName(state, state == null ? "" : state.alias());
    }

    private String publicName(HideState state, String fallback) {
        return HideIdentityPolicy.formatPublicName(
                state,
                safeName(fallback),
                usesObfuscatedText(state)
        );
    }

    public boolean usesObfuscatedText(HideState state) {
        return config().getBoolean("SCRAMBLE.OBFUSCATED", true)
                && HideIdentityPolicy.usesObfuscatedText(state);
    }

    public String plainPublicName(Player player) {
        return player == null ? "" : plainPublicName(player.getUniqueId(), player.getName());
    }

    public String plainPublicName(UUID playerUuid) {
        Player online = playerUuid == null ? null : Bukkit.getPlayer(playerUuid);
        String fallback = online == null
                ? plugin.getDatabaseManager().getLastKnownUsername(playerUuid)
                : online.getName();
        return plainPublicName(playerUuid, fallback);
    }

    public String plainPublicName(UUID playerUuid, String fallback) {
        HideState state = getState(playerUuid);
        return state == null ? safeName(fallback) : state.alias();
    }

    public String visibleName(CommandSender viewer, Player target) {
        if (target == null) {
            return "";
        }
        HideState state = getState(target.getUniqueId());
        if (state == null) {
            return target.getName();
        }
        return canSeeRealIdentity(viewer)
                ? staffMarker() + target.getName()
                : publicName(target);
    }

    public boolean canSeeRealIdentity(CommandSender viewer) {
        return viewer != null && PermissionUtils.has(viewer, BYPASS_PERMISSION);
    }

    public String staffMarker() {
        return config().getString("STAFF-MARKER", "&8[&cH&8] ");
    }

    public Player findOnlinePlayer(CommandSender viewer, String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = normalize(input);
        UUID aliasOwner = aliasOwners.get(normalized);
        if (aliasOwner != null) {
            Player aliasTarget = Bukkit.getPlayer(aliasOwner);
            if (aliasTarget != null && aliasTarget.isOnline()) {
                return aliasTarget;
            }
        }

        boolean bypass = canSeeRealIdentity(viewer);
        for (Player player : Bukkit.getOnlinePlayers()) {
            HideState state = getState(player.getUniqueId());
            if (state != null && !bypass) {
                continue;
            }
            if (player.getName().equalsIgnoreCase(input)) {
                return player;
            }
        }
        return null;
    }

    public UUID findKnownPlayerUuid(CommandSender viewer, String input) {
        Player online = findOnlinePlayer(viewer, input);
        if (online != null) {
            return online.getUniqueId();
        }
        if (input == null || input.isBlank()) {
            return null;
        }

        HideState hidden = findState(input);
        if (hidden != null) {
            if (hidden.alias().equalsIgnoreCase(input) || canSeeRealIdentity(viewer)) {
                return hidden.playerUuid();
            }
            return null;
        }

        UUID stored = plugin.getDatabaseManager().findPlayerUuidByUsername(input);
        if (stored != null && isHidden(stored) && !canSeeRealIdentity(viewer)) {
            return null;
        }
        return stored;
    }

    public List<String> onlineNames(CommandSender viewer) {
        boolean bypass = canSeeRealIdentity(viewer);
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            HideState state = getState(player.getUniqueId());
            if (state == null) {
                names.add(player.getName());
            } else {
                names.add(state.alias());
                if (bypass) {
                    names.add(player.getName());
                }
            }
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public Result scramble(Player player) {
        Result blocked = validateChange(player, SCRAMBLE_PERMISSION, false);
        if (blocked != null) {
            return blocked;
        }

        HeadTexture originalTexture = rememberOriginalSkin(player);
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String alias = generateScrambleAlias();
            if (!isAliasAvailable(alias, player.getUniqueId())) {
                continue;
            }
            long now = System.currentTimeMillis();
            HideState previous = getState(player.getUniqueId());
            HideState state = new HideState(
                    player.getUniqueId(),
                    player.getName(),
                    HideMode.SCRAMBLE,
                    alias,
                    normalize(alias),
                    "",
                    "",
                    originalTexture == null ? "" : originalTexture.value(),
                    originalTexture == null ? "" : originalTexture.signature(),
                    previous == null ? now : previous.createdAt(),
                    now
            );
            if (persistAndApply(state)) {
                restoreOriginalSkin(player);
                if (!state.hasTexture()) {
                    resolveScrambleSkinAsync(state);
                }
                return new Result(ResultType.SUCCESS, state, 0L);
            }
        }
        return new Result(ResultType.ALIAS_IN_USE, null, 0L);
    }

    public Result disguise(Player player, String aliasKey, String skinKey) {
        Result blocked = validateChange(player, DISGUISE_PERMISSION, false);
        if (blocked != null) {
            return blocked;
        }

        AliasOption aliasOption = aliases().get(normalizeKey(aliasKey));
        if (aliasOption == null || !validProfileName(aliasOption.name())) {
            return new Result(ResultType.INVALID_ALIAS, null, 0L);
        }
        SkinOption skinOption = skins().get(normalizeKey(skinKey));
        if (skinOption == null || skinOption.username().isBlank()) {
            return new Result(ResultType.INVALID_SKIN, null, 0L);
        }
        if (!isAliasAvailable(aliasOption.name(), player.getUniqueId())) {
            return new Result(ResultType.ALIAS_IN_USE, null, 0L);
        }

        long now = System.currentTimeMillis();
        HideState previous = getState(player.getUniqueId());
        HeadTexture cachedTexture = cachedHeadTexture(skinOption.username());
        HideState state = new HideState(
                player.getUniqueId(),
                player.getName(),
                HideMode.DISGUISE,
                aliasOption.name(),
                normalize(aliasOption.name()),
                skinOption.key(),
                skinOption.username(),
                cachedTexture == null ? "" : cachedTexture.value(),
                cachedTexture == null ? "" : cachedTexture.signature(),
                previous == null ? now : previous.createdAt(),
                now
        );
        rememberOriginalSkin(player);
        if (!persistAndApply(state)) {
            return new Result(ResultType.ALIAS_IN_USE, null, 0L);
        }
        if (cachedTexture != null) {
            applyDisguiseSkin(player, state);
        } else {
            resolveSkinAsync(state);
        }
        return new Result(ResultType.SUCCESS, state, 0L);
    }

    public void disguise(
            Player player,
            String aliasInput,
            String skinInput,
            Consumer<Result> completion
    ) {
        disguise(player, aliasInput, skinInput, false, completion);
    }

    public void disguiseWithScrambledAlias(
            Player player,
            String skinInput,
            Consumer<Result> completion
    ) {
        disguise(player, null, skinInput, true, completion);
    }

    private void disguise(
            Player player,
            String aliasInput,
            String skinInput,
            boolean scrambledAlias,
            Consumer<Result> completion
    ) {
        Result blocked = validateChange(player, DISGUISE_PERMISSION, false);
        if (blocked != null) {
            completion.accept(blocked);
            return;
        }

        String requestedAlias = scrambledAlias ? null : resolveAliasName(aliasInput);
        if (!scrambledAlias) {
            if (!validProfileName(requestedAlias)) {
                completion.accept(new Result(ResultType.INVALID_ALIAS, null, 0L));
                return;
            }
            if (!isAliasAvailable(requestedAlias, player.getUniqueId())) {
                completion.accept(new Result(ResultType.ALIAS_IN_USE, null, 0L));
                return;
            }
        }

        String source = resolveSkinSource(skinInput);
        if (source == null || source.isBlank()) {
            completion.accept(new Result(ResultType.INVALID_SKIN, null, 0L));
            return;
        }

        plugin.getSpigotScheduler().runAsync(() -> {
            HeadTexture texture = resolveCustomSkin(source);
            plugin.getSpigotScheduler().runEntity(player, () -> {
                if (!player.isOnline() || texture == null || !texture.isValid()) {
                    completion.accept(new Result(ResultType.INVALID_SKIN, null, 0L));
                    return;
                }
                Result rechecked = validateChange(player, DISGUISE_PERMISSION, false);
                if (rechecked != null) {
                    completion.accept(rechecked);
                    return;
                }

                rememberOriginalSkin(player);
                long now = System.currentTimeMillis();
                HideState previous = getState(player.getUniqueId());
                int attempts = scrambledAlias ? MAX_GENERATION_ATTEMPTS : 1;
                for (int attempt = 0; attempt < attempts; attempt++) {
                    String alias = scrambledAlias ? generateScrambleAlias() : requestedAlias;
                    if (!isAliasAvailable(alias, player.getUniqueId())) {
                        continue;
                    }
                    String skinKey = customSkinKey(source);
                    if (scrambledAlias) {
                        skinKey = HideIdentityPolicy.OBFUSCATED_DISGUISE_SKIN_KEY_PREFIX + skinKey;
                    }
                    HideState state = new HideState(
                            player.getUniqueId(),
                            player.getName(),
                            HideMode.DISGUISE,
                            alias,
                            normalize(alias),
                            skinKey,
                            source,
                            texture.value(),
                            texture.signature(),
                            previous == null ? now : previous.createdAt(),
                            now
                    );
                    if (!persistAndApply(state)) {
                        continue;
                    }
                    headTextures.put(normalizeKey(source), texture);
                    applyDisguiseSkin(player, state);
                    completion.accept(new Result(ResultType.SUCCESS, state, 0L));
                    return;
                }
                completion.accept(new Result(ResultType.ALIAS_IN_USE, null, 0L));
            });
        });
    }

    public Result remove(Player player, boolean administrative) {
        if (player == null) {
            return new Result(ResultType.NOT_HIDDEN, null, 0L);
        }
        if (!administrative) {
            Result blocked = validateChange(player, null, true);
            if (blocked != null) {
                return blocked;
            }
        }
        HideState previous = states.remove(player.getUniqueId());
        if (previous == null) {
            return new Result(ResultType.NOT_HIDDEN, null, 0L);
        }
        aliasOwners.remove(previous.aliasNormalized(), player.getUniqueId());
        plugin.getDatabaseManager().deleteHideState(player.getUniqueId());
        lastChanges.put(player.getUniqueId(), System.currentTimeMillis());
        restoreOriginalSkin(player);
        return new Result(ResultType.SUCCESS, previous, 0L);
    }

    public Result remove(UUID playerUuid) {
        HideState previous = states.remove(playerUuid);
        if (previous == null) {
            return new Result(ResultType.NOT_HIDDEN, null, 0L);
        }
        aliasOwners.remove(previous.aliasNormalized(), playerUuid);
        plugin.getDatabaseManager().deleteHideState(playerUuid);
        Player online = Bukkit.getPlayer(playerUuid);
        if (online != null) {
            restoreOriginalSkin(online);
        }
        return new Result(ResultType.SUCCESS, previous, 0L);
    }

    public void handleJoin(Player player, Consumer<String> notification) {
        if (player == null) {
            return;
        }
        HideState state = states.get(player.getUniqueId());
        if (state == null) {
            state = plugin.getDatabaseManager().loadHideState(player.getUniqueId());
            if (state != null) {
                cache(state);
            }
        }
        if (state == null) {
            return;
        }

        state = ensureScrambleTexture(player, state);
        rememberOriginalSkin(player);
        if (!enforcePermission(player, notification)) {
            return;
        }
        HideState joinedState = state;
        plugin.getSpigotScheduler().runEntityLater(player, () -> {
            if (joinedState.mode() == HideMode.DISGUISE && joinedState.hasTexture()) {
                applyDisguiseSkin(player, joinedState);
            } else {
                refresh(player);
                if (joinedState.mode() == HideMode.DISGUISE) {
                    resolveSkinAsync(joinedState);
                }
            }
        }, 10L);
    }

    public boolean enforcePermission(Player player, Consumer<String> notification) {
        if (player == null) {
            return true;
        }
        HideState state = getState(player.getUniqueId());
        if (state == null) {
            return true;
        }
        String requiredPermission = state.mode() == HideMode.SCRAMBLE
                ? SCRAMBLE_PERMISSION
                : DISGUISE_PERMISSION;
        if (PermissionUtils.has(player, requiredPermission)) {
            return true;
        }
        remove(player, true);
        if (notification != null) {
            notification.accept(message("PERMISSION-REMOVED",
                    "&cʏᴏᴜʀ ʜɪᴅᴇ ѕᴛᴀᴛᴇ ᴡᴀѕ ʀᴇᴍᴏᴠᴇᴅ ʙᴇᴄᴀᴜѕᴇ ɪᴛѕ ᴘᴇʀᴍɪѕѕɪᴏɴ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴠᴀɪʟᴀʙʟᴇ."));
        }
        return false;
    }

    public void handleQuit(UUID playerUuid) {
        if (packetBridge != null) {
            packetBridge.clear(playerUuid);
        }
        lastChanges.remove(playerUuid);
        originalSkinTextures.remove(playerUuid);
    }

    HeadTexture originalSkinTexture(UUID playerUuid) {
        return playerUuid == null ? null : originalSkinTextures.get(playerUuid);
    }

    public Map<String, AliasOption> aliases() {
        ConfigurationSection section = config().getConfigurationSection("ALIASES");
        Map<String, AliasOption> options = new LinkedHashMap<>();
        if (section == null) {
            return options;
        }
        for (String key : section.getKeys(false)) {
            String path = key + ".";
            String name = section.getString(path + "NAME", key);
            String skinKey = normalizeKey(section.getString(path + "SKIN", key));
            String skinUsername = config().getString("SKINS." + skinKey + ".USERNAME", name);
            if (validProfileName(name)) {
                options.put(normalizeKey(key), new AliasOption(
                        normalizeKey(key),
                        name,
                        skinKey,
                        skinUsername
                ));
            }
        }
        return options;
    }

    public Map<String, SkinOption> skins() {
        ConfigurationSection section = config().getConfigurationSection("SKINS");
        Map<String, SkinOption> options = new LinkedHashMap<>();
        if (section == null) {
            return options;
        }
        for (String key : section.getKeys(false)) {
            String path = key + ".";
            String username = section.getString(path + "USERNAME", key);
            String displayName = section.getString(path + "DISPLAY-NAME", username);
            if (username != null && !username.isBlank()) {
                options.put(normalizeKey(key), new SkinOption(normalizeKey(key), displayName, username));
            }
        }
        return options;
    }

    public HeadTexture cachedHeadTexture(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        HeadTexture texture = headTextures.get(normalizeKey(username));
        return texture != null && texture.isValid() ? texture : null;
    }

    public CompletableFuture<HeadTexture> resolveHeadTextureAsync(String username) {
        HeadTexture cached = cachedHeadTexture(username);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        if (username == null || username.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        String cacheKey = normalizeKey(username);
        CompletableFuture<HeadTexture> existing = headTextureLoads.get(cacheKey);
        if (existing != null) {
            return existing;
        }

        CompletableFuture<HeadTexture> created = new CompletableFuture<>();
        CompletableFuture<HeadTexture> raced = headTextureLoads.putIfAbsent(cacheKey, created);
        if (raced != null) {
            return raced;
        }

        plugin.getSpigotScheduler().runAsync(() -> {
            try {
                HeadTexture resolved = resolveHeadTexture(username);
                if (resolved != null && resolved.isValid()) {
                    headTextures.put(cacheKey, resolved);
                }
                created.complete(resolved);
            } catch (RuntimeException | LinkageError error) {
                created.complete(null);
            } finally {
                headTextureLoads.remove(cacheKey, created);
            }
        });
        return created;
    }

    public String message(String key, String fallback, String... placeholders) {
        String value = config().getString("MESSAGES." + key, fallback);
        for (int index = 0; index + 1 < placeholders.length; index += 2) {
            value = value.replace(placeholders[index], placeholders[index + 1]);
        }
        return value;
    }

    private Result validateChange(Player player, String permission, boolean removing) {
        long remaining = cooldownRemaining(player.getUniqueId());
        ResultType blocked = evaluateChange(
                isEnabled(),
                isAvailable(),
                permission == null || PermissionUtils.has(player, permission),
                plugin.getCombatManager() != null
                        && plugin.getCombatManager().isInCombat(player.getUniqueId()),
                remaining > 0L,
                removing,
                isHidden(player.getUniqueId())
        );
        return blocked == null ? null : new Result(blocked, null, remaining);
    }

    private long cooldownRemaining(UUID playerUuid) {
        if (PermissionUtils.has(Bukkit.getPlayer(playerUuid), BYPASS_PERMISSION)) {
            return 0L;
        }
        Long changedAt = lastChanges.get(playerUuid);
        return HideIdentityPolicy.cooldownRemaining(
                changedAt == null ? 0L : changedAt,
                System.currentTimeMillis(),
                Math.max(0L, config().getLong("COOLDOWN-SECONDS", 30L))
        );
    }

    private boolean persistAndApply(HideState state) {
        HideState previous = states.get(state.playerUuid());
        if (!plugin.getDatabaseManager().saveHideState(state)) {
            return false;
        }
        if (previous != null) {
            aliasOwners.remove(previous.aliasNormalized(), state.playerUuid());
        }
        cache(state);
        lastChanges.put(state.playerUuid(), System.currentTimeMillis());
        Player online = Bukkit.getPlayer(state.playerUuid());
        if (online != null) {
            refresh(online);
        }
        return true;
    }

    private void resolveSkinAsync(HideState requestedState) {
        plugin.getSpigotScheduler().runAsync(() -> {
            HeadTexture texture = resolveHeadTexture(requestedState.skinUsername());
            if (texture == null || !texture.isValid()) {
                return;
            }
            headTextures.put(normalizeKey(requestedState.skinUsername()), texture);
            HideState current = states.get(requestedState.playerUuid());
            if (current == null
                    || current.mode() != HideMode.DISGUISE
                    || !current.aliasNormalized().equals(requestedState.aliasNormalized())
                    || !current.skinKey().equals(requestedState.skinKey())) {
                return;
            }
            HideState updated = current.withTexture(
                    texture.value(),
                    texture.signature(),
                    System.currentTimeMillis()
            );
            if (!plugin.getDatabaseManager().saveHideState(updated)) {
                return;
            }
            cache(updated);
            Player online = Bukkit.getPlayer(updated.playerUuid());
            if (online != null) {
                plugin.getSpigotScheduler().runEntity(online, () -> applyDisguiseSkin(online, updated));
            }
        });
    }

    private void resolveScrambleSkinAsync(HideState requestedState) {
        TablistManager tablist = plugin.getTablistManager();
        if (requestedState == null || tablist == null) {
            return;
        }
        plugin.getSpigotScheduler().runAsync(() -> {
            TablistManager.SkinTexture resolved = tablist.resolveOriginalGameProfileSkinTexture(
                    requestedState.playerUuid(),
                    requestedState.realNameSnapshot()
            );
            if (resolved == null || !resolved.isValid()) {
                return;
            }
            Player online = Bukkit.getPlayer(requestedState.playerUuid());
            if (online != null) {
                plugin.getSpigotScheduler().runEntity(
                        online,
                        () -> applyResolvedScrambleTexture(requestedState, resolved)
                );
            }
        });
    }

    private void applyResolvedScrambleTexture(
            HideState requestedState,
            TablistManager.SkinTexture resolved
    ) {
        HideState current = getState(requestedState.playerUuid());
        if (current == null
                || current.mode() != HideMode.SCRAMBLE
                || !current.aliasNormalized().equals(requestedState.aliasNormalized())) {
            return;
        }
        HideState updated = current.withTexture(
                resolved.value(),
                resolved.signature(),
                System.currentTimeMillis()
        );
        if (!plugin.getDatabaseManager().saveHideState(updated)) {
            return;
        }
        cache(updated);
        HeadTexture texture = new HeadTexture(resolved.value(), resolved.signature());
        originalSkinTextures.put(updated.playerUuid(), texture);
        Player online = Bukkit.getPlayer(updated.playerUuid());
        if (online != null) {
            applySkinTexture(online, texture);
        }
    }

    private HeadTexture resolveCustomSkin(String source) {
        SkinsRestorerHideBridge.ResolvedSkin skinsRestorer = null;
        try {
            skinsRestorer = SkinsRestorerHideBridge.resolve(source);
        } catch (Throwable ignored) {
        }
        if (skinsRestorer != null) {
            return new HeadTexture(skinsRestorer.value(), skinsRestorer.signature());
        }
        if (isSkinUrl(source)) {
            return null;
        }
        return resolveHeadTexture(source);
    }

    private HideState ensureScrambleTexture(Player player, HideState state) {
        if (state == null || state.mode() != HideMode.SCRAMBLE) {
            return state;
        }
        if (state.hasTexture()) {
            originalSkinTextures.putIfAbsent(
                    state.playerUuid(),
                    new HeadTexture(state.textureValue(), state.textureSignature())
            );
            return state;
        }
        HeadTexture original = rememberOriginalSkin(player);
        if (original == null || !original.isValid()) {
            resolveScrambleSkinAsync(state);
            return state;
        }
        HideState updated = state.withTexture(
                original.value(),
                original.signature(),
                System.currentTimeMillis()
        );
        if (plugin.getDatabaseManager().saveHideState(updated)) {
            cache(updated);
            return updated;
        }
        return state;
    }

    private HeadTexture rememberOriginalSkin(Player player) {
        if (player == null) {
            return null;
        }
        HeadTexture cached = originalSkinTextures.get(player.getUniqueId());
        if (cached != null && cached.isValid()) {
            return cached;
        }
        TablistManager tablist = plugin.getTablistManager();
        TablistManager.SkinTexture texture = tablist == null
                ? null
                : tablist.resolveLiveGameProfileSkinTexture(player);
        if ((texture == null || !texture.isValid()) && tablist != null) {
            texture = tablist.resolveCurrentSkinTexture(player);
        }
        if (texture != null && texture.isValid()) {
            HeadTexture captured = new HeadTexture(texture.value(), texture.signature());
            originalSkinTextures.putIfAbsent(player.getUniqueId(), captured);
            return originalSkinTextures.get(player.getUniqueId());
        }
        return null;
    }

    private void applyDisguiseSkin(Player player, HideState state) {
        if (player == null || state == null || !state.hasTexture()) {
            return;
        }
        boolean applied = applyWithSkinsRestorer(
                player, state.textureValue(), state.textureSignature());
        if (!applied) {
            try {
                applied = NativeGameProfileFactory.applyTexture(
                        WrappedGameProfile.fromPlayer(player),
                        state.textureValue(),
                        state.textureSignature()
                );
            } catch (RuntimeException | LinkageError ignored) {
                applied = false;
            }
        }
        syncTablistSkinTexture(player, state.textureValue(), state.textureSignature());
        refresh(player);
        if (!applied) {
            plugin.getLogger().fine("Disguise texture could not be applied directly to " + player.getName() + ".");
        }
    }

    private void restoreOriginalSkin(Player player) {
        HeadTexture original = player == null ? null : originalSkinTextures.remove(player.getUniqueId());
        if (player == null || original == null || !original.isValid()) {
            if (player != null) {
                refresh(player);
            }
            return;
        }
        applySkinTexture(player, original);
    }

    private void applySkinTexture(Player player, HeadTexture texture) {
        if (player == null || texture == null || !texture.isValid()) {
            return;
        }
        boolean applied = applyWithSkinsRestorer(player, texture.value(), texture.signature());
        if (!applied) {
            try {
                NativeGameProfileFactory.applyTexture(
                        WrappedGameProfile.fromPlayer(player),
                        texture.value(),
                        texture.signature()
                );
            } catch (RuntimeException | LinkageError ignored) {
            }
        }
        syncTablistSkinTexture(player, texture.value(), texture.signature());
        refresh(player);
    }

    private void syncTablistSkinTexture(Player player, String value, String signature) {
        TablistManager tablist = plugin.getTablistManager();
        if (player != null && tablist != null) {
            tablist.updateSkinTexture(player.getUniqueId(), value, signature);
        }
    }

    private boolean applyWithSkinsRestorer(Player player, String value, String signature) {
        try {
            return SkinsRestorerHideBridge.apply(player, value, signature);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private String resolveAliasName(String input) {
        AliasOption configured = aliases().get(normalizeKey(input));
        return configured == null ? input : configured.name();
    }

    private String resolveSkinSource(String input) {
        SkinOption configured = skins().get(normalizeKey(input));
        return configured == null ? input : configured.username();
    }

    private String customSkinKey(String source) {
        return "custom_" + Integer.toUnsignedString(source.toLowerCase(Locale.ROOT).hashCode(), 36);
    }

    public static boolean isSkinUrl(String input) {
        if (input == null) {
            return false;
        }
        String lower = input.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private HeadTexture resolveHeadTexture(String username) {
        TablistManager tablist = plugin.getTablistManager();
        if (tablist == null) {
            return null;
        }

        TablistManager.SkinTexture resolved = tablist.resolveSkinTextureForFakePlayer(null, username);
        if (resolved == null || !resolved.isValid()) {
            resolved = tablist.resolveOriginalGameProfileSkinTexture(null, username);
        }
        return resolved == null || !resolved.isValid()
                ? null
                : new HeadTexture(resolved.value(), resolved.signature());
    }

    private void cache(HideState state) {
        states.put(state.playerUuid(), state);
        aliasOwners.put(state.aliasNormalized(), state.playerUuid());
        if (!state.skinUsername().isBlank() && state.hasTexture()) {
            headTextures.put(
                    normalizeKey(state.skinUsername()),
                    new HeadTexture(state.textureValue(), state.textureSignature())
            );
        }
    }

    private boolean isAliasAvailable(String alias, UUID owner) {
        if (!validProfileName(alias)) {
            return false;
        }
        String normalized = normalize(alias);
        UUID existing = aliasOwners.get(normalized);
        if (existing != null && !existing.equals(owner)) {
            return false;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.getUniqueId().equals(owner)
                    && online.getName().equalsIgnoreCase(alias)) {
                return false;
            }
        }
        return plugin.getDatabaseManager().findPlayerUuidByUsername(alias) == null;
    }

    private String generateScrambleAlias() {
        int maxLength = Math.max(1, Math.min(16, config().getInt("MAX-NAME-LENGTH", 16)));
        int length = Math.max(3, Math.min(maxLength, config().getInt("SCRAMBLE.LENGTH", 10)));
        return HideIdentityPolicy.generateAlias(
                random,
                config().getString("SCRAMBLE.CHARACTERS", ""),
                length,
                maxLength
        );
    }

    private boolean validProfileName(String name) {
        return HideIdentityPolicy.isValidProfileName(
                name,
                config().getInt("MAX-NAME-LENGTH", 16)
        );
    }

    public void refresh(Player player) {
        if (packetBridge != null) {
            packetBridge.refresh(player);
        }
        if (plugin.getTablistManager() != null) {
            plugin.getTablistManager().updateTablistName(player);
        }
    }

    public void refreshNametag(Player player) {
        if (packetBridge != null) {
            packetBridge.refreshNametag(player);
        }
    }

    public void clearNametag(UUID playerUuid) {
        if (packetBridge != null) {
            packetBridge.clear(playerUuid);
        }
    }

    private void refreshAll() {
        Set<UUID> hidden = Set.copyOf(states.keySet());
        for (UUID playerUuid : hidden) {
            Player online = Bukkit.getPlayer(playerUuid);
            if (online != null) {
                refresh(online);
            }
        }
    }

    private HidePacketBridge createPacketBridge() {
        try {
            if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")
                    && Class.forName("com.comphenix.protocol.ProtocolLibrary") != null) {
                return new HideProtocolLibBridge(plugin, this);
            }
        } catch (Throwable error) {
            plugin.getLogger().warning("Hide could not initialize ProtocolLib: " + error.getMessage());
        }
        return null;
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getHide();
    }

    private void logConfigurationProblems() {
        for (String problem : HideIdentityPolicy.validate(config())) {
            plugin.getLogger().warning("hide.yml: " + problem);
        }
    }

    static ResultType evaluateChange(
            boolean enabled,
            boolean available,
            boolean permitted,
            boolean inCombat,
            boolean coolingDown,
            boolean removing,
            boolean hidden
    ) {
        if (!enabled) return ResultType.DISABLED;
        if (!available) return ResultType.DEPENDENCY_MISSING;
        if (!permitted) return ResultType.NO_PERMISSION;
        if (inCombat) return ResultType.IN_COMBAT;
        if (coolingDown) return ResultType.COOLDOWN;
        if (removing && !hidden) return ResultType.NOT_HIDDEN;
        return null;
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeKey(String value) {
        return normalize(value).replace(' ', '_').replace('-', '_');
    }

    private static String safeName(String value) {
        return value == null || value.isBlank() ? "Unknown" : value;
    }
}
