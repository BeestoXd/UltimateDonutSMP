package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.listeners.PlayerRespawnListener;
import com.bx.ultimateDonutSmp.models.DuelArena;
import com.bx.ultimateDonutSmp.models.DuelClaim;
import com.bx.ultimateDonutSmp.models.DuelMapSelection;
import com.bx.ultimateDonutSmp.models.DuelMatch;
import com.bx.ultimateDonutSmp.models.DuelPrivacyMode;
import com.bx.ultimateDonutSmp.models.DuelRequest;
import com.bx.ultimateDonutSmp.models.DuelStats;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.AttributeUtils;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemSerializationUtils;
import com.bx.ultimateDonutSmp.utils.LocationUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import com.bx.ultimateDonutSmp.utils.TitleUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class DuelManager {

    public enum ArenaSetting {
        ALLOW_ITEM_DROP("allow item drop"),
        ALLOW_BLOCK_BREAK("allow block break"),
        ALLOW_BLOCK_PLACE("allow block place"),
        ALLOW_BUCKET_USE("allow bucket use"),
        NO_HUNGER("no hunger"),
        NO_WEATHER("no weather"),
        ALWAYS_MORNING("always morning"),
        NO_FALL_DAMAGE("no fall damage");

        private final String displayName;

        ArenaSetting(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public record DuelMapOption(DuelMapSelection selection, String displayName, String description) {
    }

    private record ResolvedArena(
            DuelArena arena,
            DuelMapSelection selection,
            String biomeKey,
            String generatedWorldName,
            DuelWorldManager.TerrainMode generatedTerrainMode
    ) {
    }

    private record CrossQueueEntry(UUID uuid, String name, String serverId, DuelMapSelection selection, long queuedAt) {
    }

    private record PendingCrossServerMatch(String matchId, UUID firstUuid, String firstName,
                                           UUID secondUuid, String secondName,
                                           DuelMapSelection selection, long expiresAt) {
    }

    private static final String TERRAIN_MODE_SETTINGS_PATH = "MAP_SOURCES.RANDOM_BIOMES.TERRAIN_MODE_SETTINGS";

    private final UltimateDonutSmp plugin;
    private final DuelWorldManager worldManager;
    private final Map<String, DuelArena> arenas = new HashMap<>();
    private final Map<UUID, DuelRequest> requestsByTarget = new HashMap<>();
    private final LinkedHashSet<UUID> queue = new LinkedHashSet<>();
    private final Map<UUID, DuelMapSelection> queueSelections = new HashMap<>();
    private final Map<Long, DuelMatch> activeMatches = new HashMap<>();
    private final Map<UUID, Long> activeMatchIds = new HashMap<>();
    private final Set<String> reservedArenaIds = new HashSet<>();
    private final Map<UUID, PendingRespawnState> pendingRespawns = new HashMap<>();
    private final Map<UUID, DuelStats> statsCache = new HashMap<>();
    private final Map<Long, ArenaSnapshot> arenaSnapshots = new HashMap<>();
    private final Set<UUID> preparingDuelPlayers = new HashSet<>();
    private final Set<UUID> transitioningPlayers = new HashSet<>();
    private final Map<UUID, TransitionPlayerState> transitionStates = new HashMap<>();
    private final Map<UUID, TransitionTitleState> transitionTitles = new HashMap<>();
    private final Map<UUID, Integer> borderEscapeTicks = new HashMap<>();
    private final Map<Long, Map<UUID, GeneratedInventorySnapshot>> generatedMatchInventorySnapshots = new HashMap<>();
    private final Map<Long, Map<BlockKey, String>> generatedBlockSnapshots = new HashMap<>();
    private final Map<String, PendingCrossServerMatch> pendingCrossServerMatches = new HashMap<>();
    private final Set<String> seenCrossServerMessages = new HashSet<>();
    private boolean crossServerSubscribed = false;
    private String crossServerSubscribedChannel = "";
    private long tickCounter = 0L;

    public DuelManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.worldManager = new DuelWorldManager(plugin);
        ensureTables();
        reload();
    }

    public void reload() {
        loadArenas();
        worldManager.ensureFlatPool();
        worldManager.ensureVanillaPool();
        initializeCrossServer();
        syncArenaRulesForAllOccupants();
    }

    public void refreshArenaAvailability() {
        reload();
    }

    public void shutdown() {
        for (DuelMatch match : activeMatches.values()) {
            if (match.usesGeneratedWorld()) {
                worldManager.cleanupGeneratedWorld(match.getGeneratedWorldName());
            }
        }
        requestsByTarget.clear();
        queue.clear();
        queueSelections.clear();
        activeMatches.clear();
        activeMatchIds.clear();
        reservedArenaIds.clear();
        pendingRespawns.clear();
        arenaSnapshots.clear();
        preparingDuelPlayers.clear();
        transitioningPlayers.clear();
        transitionStates.clear();
        transitionTitles.clear();
        borderEscapeTicks.clear();
        generatedMatchInventorySnapshots.clear();
        generatedBlockSnapshots.clear();
        pendingCrossServerMatches.clear();
        seenCrossServerMessages.clear();
        unsubscribeCrossServer();
        worldManager.shutdownFlatPool();
        worldManager.shutdownVanillaPool();
        showAllVanishedPlayers();
    }

    public void initializeCrossServer() {
        if (plugin.getRedisManager() == null) {
            return;
        }

        if (!isCrossServerEnabled()) {
            unsubscribeCrossServer();
            return;
        }

        String channel = getCrossServerChannel();
        if (crossServerSubscribed && channel.equals(crossServerSubscribedChannel)) {
            return;
        }

        unsubscribeCrossServer();
        plugin.getRedisManager().subscribe(channel, this::handleCrossServerPayload);
        crossServerSubscribed = true;
        crossServerSubscribedChannel = channel;
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.DUELS)
                && config().getBoolean("SETTINGS.ENABLED", true);
    }

    public boolean isVanillaBiomeTerrainMode() {
        return worldManager.isVanillaTerrainMode();
    }

    public boolean isFlatBiomeTerrainMode() {
        return worldManager.isFlatTerrainMode();
    }

    public boolean isVanillaRuntimeGenerationEnabled() {
        return worldManager.isVanillaRuntimeGenerationEnabled();
    }

    public String getQueueTitle() {
        return config().getString("GUI.QUEUE.TITLE", "&8ᴄᴀѕᴜᴀʟ ǫᴜᴇᴜᴇ");
    }

    public int getQueueSize() {
        return normalizeSize(config().getInt("GUI.QUEUE.SIZE", 27));
    }

    public String getCreateTitle(Player target) {
        String name = target == null ? "Player" : publicName(target);
        return config().getString("GUI.CREATE.TITLE", "&8ᴄʀᴇᴀᴛᴇ ᴅᴜᴇʟ -> {player}")
                .replace("{player}", name);
    }

    public int getCreateSize() {
        return normalizeSize(config().getInt("GUI.CREATE.SIZE", 27));
    }

    public String getClaimsTitle() {
        return config().getString("GUI.CLAIMS.TITLE", "&8ᴅᴜᴇʟ ᴄʟᴀɪᴍѕ");
    }

    public int getClaimsSize() {
        return normalizeSize(config().getInt("GUI.CLAIMS.SIZE", 54));
    }

    public int getClaimsItemsPerPage() {
        return Math.max(1, Math.min(45, config().getInt("GUI.CLAIMS.ITEMS_PER_PAGE", 45)));
    }

    public int getCountdownSeconds() {
        int configured = Math.max(0, config().getInt("SETTINGS.COUNTDOWN_SECONDS", 5));
        ConfigurationSection section = config().getConfigurationSection("START-COUNTDOWN");
        if (section == null || !section.getBoolean("ENABLED", true)) {
            return configured;
        }

        int maxKey = -1;
        maxKey = Math.max(maxKey, getMaxNumericKey(section.getConfigurationSection("TITLES")));
        maxKey = Math.max(maxKey, getMaxNumericKey(section.getConfigurationSection("MESSAGES")));
        return maxKey >= 0 ? maxKey : configured;
    }

    public int getMatchDurationSeconds() {
        return Math.max(30, config().getInt("SETTINGS.MATCH_DURATION_SECONDS", 900));
    }

    public int getRequestTimeoutSeconds() {
        return Math.max(5, config().getInt("SETTINGS.REQUEST_TIMEOUT_SECONDS", 30));
    }

    public int getDrawTimeoutSeconds() {
        return Math.max(5, config().getInt("SETTINGS.DRAW_REQUEST_TIMEOUT_SECONDS", 15));
    }

    public int getReturnDelayTicks() {
        int seconds = config().getInt("SETTINGS.RETURN_DELAY_SECONDS",
                config().getInt("SETTINGS.WINNER_RETURN_DELAY_SECONDS", 3));
        return Math.max(0, seconds) * 20;
    }

    public int getRollbackHorizontalPadding() {
        return Math.max(0, config().getInt("SETTINGS.ROLLBACK_PADDING_HORIZONTAL", 8));
    }

    public int getRollbackVerticalPadding() {
        return Math.max(0, config().getInt("SETTINGS.ROLLBACK_PADDING_VERTICAL", 6));
    }

    public int getQueueSizeCount() {
        return queue.size();
    }

    public boolean isInQueue(UUID uuid) {
        return uuid != null && queue.contains(uuid);
    }

    public boolean isInDuel(UUID uuid) {
        return uuid != null && activeMatchIds.containsKey(uuid);
    }

    public boolean isInCountdown(UUID uuid) {
        DuelMatch match = getActiveMatch(uuid);
        return match != null && match.getStatus() == DuelMatch.MatchStatus.COUNTDOWN;
    }

    public boolean isTransitioning(UUID uuid) {
        return uuid != null && transitioningPlayers.contains(uuid);
    }

    public boolean isMatchActive(UUID uuid) {
        DuelMatch match = getActiveMatch(uuid);
        return match != null && match.getStatus() == DuelMatch.MatchStatus.ACTIVE;
    }

    public boolean areOpponents(UUID first, UUID second) {
        if (first == null || second == null || first.equals(second)) {
            return false;
        }
        DuelMatch match = getActiveMatch(first);
        return match != null && match.getStatus() != DuelMatch.MatchStatus.FINISHED
                && second.equals(match.getOpponent(first));
    }

    public boolean canModifyArena(Player player) {
        if (player == null) {
            return false;
        }

        DuelMatch match = getActiveMatch(player.getUniqueId());
        return match != null
                && match.getStatus() == DuelMatch.MatchStatus.ACTIVE
                && (match.usesGeneratedWorld()
                || (match.getArena().hasRollbackRegion() && arenaSnapshots.containsKey(match.getId())));
    }

    public void recordGeneratedBlockChange(Player player, Block block) {
        if (player == null || block == null) {
            return;
        }
        DuelMatch match = getActiveMatch(player.getUniqueId());
        recordGeneratedBlockChange(match, block);
    }

    public void recordGeneratedBlockChange(Block block) {
        if (block == null || block.getWorld() == null) {
            return;
        }
        String worldName = block.getWorld().getName();
        for (DuelMatch match : activeMatches.values()) {
            if (match != null
                    && match.usesGeneratedWorld()
                    && worldName.equalsIgnoreCase(match.getGeneratedWorldName())) {
                recordGeneratedBlockChange(match, block);
                return;
            }
        }
    }

    private void recordGeneratedBlockChange(DuelMatch match, Block block) {
        if (match == null || !match.usesGeneratedWorld() || block == null || block.getWorld() == null) {
            return;
        }
        if (!block.getWorld().getName().equalsIgnoreCase(match.getGeneratedWorldName())) {
            return;
        }

        BlockKey key = new BlockKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        generatedBlockSnapshots
                .computeIfAbsent(match.getId(), ignored -> new LinkedHashMap<>())
                .putIfAbsent(key, block.getBlockData().getAsString());
    }

    public boolean shouldBypassGlobalCombat(Player attacker, Player victim) {
        if (victim != null && isInDuel(victim.getUniqueId())) {
            return true;
        }
        return attacker != null && isInDuel(attacker.getUniqueId());
    }

    public DuelStats getStats(UUID uuid) {
        if (uuid == null) {
            return DuelStats.empty();
        }
        return statsCache.computeIfAbsent(uuid, this::loadStats);
    }

    public List<DuelClaim> getClaims(UUID uuid) {
        List<DuelClaim> claims = new ArrayList<>();
        if (uuid == null || connection() == null) {
            return claims;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "select match_id, defeated_name, item_data, created_at from duel_claims where player_uuid = ? order by created_at desc, id asc")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                Map<Long, ClaimAccumulator> groupedClaims = new LinkedHashMap<>();
                while (rs.next()) {
                    long matchId = rs.getLong("match_id");
                    String defeatedName = rs.getString("defeated_name");
                    long createdAt = rs.getLong("created_at");
                    ItemStack item = deserializeItem(rs.getString("item_data"));
                    if (item == null) {
                        continue;
                    }
                    ClaimAccumulator accumulator = groupedClaims.computeIfAbsent(
                            matchId,
                            ignored -> new ClaimAccumulator(defeatedName, createdAt)
                    );
                    accumulator.items().add(item);
                    accumulator.updateMetadata(defeatedName, createdAt);
                }

                for (Map.Entry<Long, ClaimAccumulator> entry : groupedClaims.entrySet()) {
                    ClaimAccumulator accumulator = entry.getValue();
                    claims.add(new DuelClaim(
                            entry.getKey(),
                            uuid,
                            accumulator.defeatedName(),
                            new ArrayList<>(accumulator.items()),
                            accumulator.createdAt()
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load duel claims for " + uuid, e);
        }

        return claims;
    }

    public DuelClaim getClaim(UUID playerUuid, long matchId) {
        if (playerUuid == null || matchId <= 0L) {
            return null;
        }

        for (DuelClaim claim : getClaims(playerUuid)) {
            if (claim.matchId() == matchId) {
                return claim;
            }
        }
        return null;
    }

    public boolean claim(Player player, long matchId) {
        if (player == null || matchId <= 0L || connection() == null) {
            return false;
        }

        DuelClaim claim = getClaim(player.getUniqueId(), matchId);
        if (claim == null || claim.items() == null || claim.items().isEmpty()) {
            send(player, "&cᴛʜᴀᴛ ᴅᴜᴇʟ ᴄʟᴀɪᴍ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.");
            return false;
        }

        List<ClaimItemRow> claimRows = loadClaimItemRows(player.getUniqueId(), matchId);
        if (claimRows.isEmpty()) {
            send(player, "&cᴛʜᴀᴛ ᴅᴜᴇʟ ᴄʟᴀɪᴍ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.");
            return false;
        }

        List<Long> claimedRowIds = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        int remainingCount = 0;
        for (ClaimItemRow row : claimRows) {
            ItemStack item = row.item();
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }

            if (!canFullyFit(inventory, item)) {
                remainingCount++;
                continue;
            }

            inventory.addItem(item.clone());
            claimedRowIds.add(row.id());
        }

        if (claimedRowIds.isEmpty()) {
            send(player, "&cᴍᴀᴋᴇ ʀᴏᴏᴍ ɪɴ ʏᴏᴜʀ ɪɴᴠᴇɴᴛᴏʀʏ ʙᴇꜰᴏʀᴇ ᴄʟᴀɪᴍɪɴɢ ᴛʜᴀᴛ ʟᴏᴏᴛ.");
            return false;
        }

        try (PreparedStatement deleteOne = connection().prepareStatement(
                "delete from duel_claims where id = ? and player_uuid = ?")) {
            for (Long rowId : claimedRowIds) {
                deleteOne.setLong(1, rowId);
                deleteOne.setString(2, player.getUniqueId().toString());
                deleteOne.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete duel claim package " + matchId, e);
            send(player, "&cᴄᴏᴜʟᴅ ɴᴏᴛ ᴄʟᴀɪᴍ ᴛʜᴀᴛ ʟᴏᴏᴛ ʀɪɢʜᴛ ɴᴏᴡ.");
            return false;
        }

        play(player, "DUELS.CLAIM");
        String defeatedName = claim.defeatedName() == null || claim.defeatedName().isBlank()
                ? "unknown"
                : claim.defeatedName();
        if (remainingCount > 0) {
            send(player, "&eᴄʟᴀɪᴍᴇᴅ ѕᴏᴍᴇ ᴅᴜᴇʟ ʟᴏᴏᴛ ꜰʀᴏᴍ &f" + defeatedName + "&e. "
                    + "&7ѕᴏᴍᴇ ɪᴛᴇᴍѕ ᴀʀᴇ ѕᴛɪʟʟ ᴡᴀɪᴛɪɴɢ ɪɴ ᴄʟᴀɪᴍѕ.");
        } else {
            send(player, "&aᴄʟᴀɪᴍᴇᴅ ᴅᴜᴇʟ ʟᴏᴏᴛ ꜰʀᴏᴍ &f" + defeatedName + "&a.");
        }
        return true;
    }

    public boolean deleteClaim(Player player, long matchId) {
        if (player == null || matchId <= 0L || connection() == null) {
            return false;
        }

        DuelClaim claim = getClaim(player.getUniqueId(), matchId);
        if (claim == null) {
            send(player, "&cᴛʜᴀᴛ ᴅᴜᴇʟ ᴄʟᴀɪᴍ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.");
            return false;
        }

        int deletedRows;
        try (PreparedStatement deletePackage = connection().prepareStatement(
                "delete from duel_claims where match_id = ? and player_uuid = ?")) {
            deletePackage.setLong(1, matchId);
            deletePackage.setString(2, player.getUniqueId().toString());
            deletedRows = deletePackage.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete duel claim package " + matchId, e);
            send(player, "&cᴄᴏᴜʟᴅ ɴᴏᴛ ᴅᴇʟᴇᴛᴇ ᴛʜᴀᴛ ᴅᴜᴇʟ ᴄʟᴀɪᴍ ʀɪɢʜᴛ ɴᴏᴡ.");
            return false;
        }

        if (deletedRows <= 0) {
            send(player, "&cᴛʜᴀᴛ ᴅᴜᴇʟ ᴄʟᴀɪᴍ ɴᴏ ʟᴏɴɢᴇʀ ᴇxɪѕᴛѕ.");
            return false;
        }

        String defeatedName = claim.defeatedName() == null || claim.defeatedName().isBlank()
                ? "unknown"
                : claim.defeatedName();
        send(player, "&cᴅᴇʟᴇᴛᴇᴅ ᴅᴜᴇʟ ʟᴏᴏᴛ ᴄʟᴀɪᴍ ꜰʀᴏᴍ &f" + defeatedName + "&c.");
        return true;
    }

    public List<DuelArena> getArenas() {
        List<DuelArena> values = new ArrayList<>(arenas.values());
        values.sort(Comparator.comparing(DuelArena::getId, String.CASE_INSENSITIVE_ORDER));
        return values;
    }

    public List<DuelMapOption> getSelectableMapOptions(boolean queueOnly) {
        List<DuelMapOption> options = new ArrayList<>();
        List<DuelArena> staticArenas = queueOnly ? getReadyQueueArenas() : getReadyEnabledArenas();
        if (!staticArenas.isEmpty()) {
            options.add(new DuelMapOption(
                    DuelMapSelection.randomStatic(),
                    "random arena",
                    "use any available configured duel arena."
            ));
            for (DuelArena arena : staticArenas) {
                options.add(new DuelMapOption(
                        DuelMapSelection.staticArena(arena.getId()),
                        arena.getDisplayName(),
                        "arena id: " + arena.getId()
                ));
            }
        }

        if (worldManager.isRandomBiomesEnabled()) {
            List<Biome> biomes = worldManager.getSelectableBiomes();
            if (!biomes.isEmpty()) {
                options.add(new DuelMapOption(
                        DuelMapSelection.randomBiome(),
                        "random biome",
                        "generate a fresh duel world using a random configured biome."
                ));
                for (Biome biome : biomes) {
                    String key = worldManager.biomeKey(biome);
                    options.add(new DuelMapOption(
                            DuelMapSelection.biome(key),
                            worldManager.prettifyBiomeKey(key),
                            "biome: " + key
                    ));
                }
            }
        }
        return options;
    }

    public DuelMapSelection parseMapSelection(String raw) {
        DuelMapSelection selection = DuelMapSelection.parse(raw);
        if (selection.type() != DuelMapSelection.Type.BIOME) {
            return selection;
        }
        Optional<Biome> biome = worldManager.resolveBiome(selection.value());
        return biome.map(value -> DuelMapSelection.biome(worldManager.biomeKey(value))).orElse(selection);
    }

    public List<String> getMapSelectionSuggestions(boolean queueOnly) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("random");
        suggestions.add("random_biome");
        for (DuelMapOption option : getSelectableMapOptions(queueOnly)) {
            DuelMapSelection selection = option.selection();
            switch (selection.type()) {
                case STATIC_ARENA -> suggestions.add("arena:" + selection.value());
                case BIOME -> suggestions.add("biome:" + selection.value());
                case RANDOM_STATIC -> {
                    if (!suggestions.contains("random")) {
                        suggestions.add("random");
                    }
                }
                case RANDOM_BIOME -> {
                    if (!suggestions.contains("random_biome")) {
                        suggestions.add("random_biome");
                    }
                }
            }
        }
        return suggestions.stream().distinct().toList();
    }

    public DuelArena getArena(String id) {
        if (id == null) {
            return null;
        }
        return arenas.get(normalizeArenaId(id));
    }

    public DuelArena getSessionArena(UUID uuid) {
        DuelMatch match = getActiveMatch(uuid);
        return match == null ? null : match.getArena();
    }

    public boolean hasArenaSetting(UUID uuid, ArenaSetting setting) {
        if (setting == null) {
            return false;
        }

        DuelArena arena = getSessionArena(uuid);
        if (arena == null) {
            return false;
        }

        return switch (setting) {
            case ALLOW_ITEM_DROP -> arena.isAllowItemDrop();
            case ALLOW_BLOCK_BREAK -> arena.isAllowBlockBreak();
            case ALLOW_BLOCK_PLACE -> arena.isAllowBlockPlace();
            case ALLOW_BUCKET_USE -> arena.isAllowBucketUse();
            case NO_HUNGER -> arena.isNoHunger();
            case NO_WEATHER -> arena.isNoWeather();
            case ALWAYS_MORNING -> arena.isAlwaysMorning();
            case NO_FALL_DAMAGE -> arena.isNoFallDamage();
        };
    }

    public List<DuelArena> getReadyEnabledArenas() {
        List<DuelArena> result = new ArrayList<>();
        for (DuelArena arena : getArenas()) {
            if (arena.isEnabled() && arena.isReady()) {
                result.add(arena);
            }
        }
        return result;
    }

    public List<DuelArena> getReadyQueueArenas() {
        List<DuelArena> result = new ArrayList<>();
        for (DuelArena arena : getArenas()) {
            if (arena.isEnabled() && arena.isQueueEnabled() && arena.isReady()) {
                result.add(arena);
            }
        }
        return result;
    }

    public boolean createArena(String id) {
        String normalized = normalizeArenaId(id);
        if (normalized == null || arenas.containsKey(normalized)) {
            return false;
        }

        DuelArena arena = new DuelArena(
                normalized,
                prettifyId(normalized),
                null,
                null,
                null,
                null,
                null,
                true,
                true,
                false,
                true,
                true,
                true,
                false,
                false,
                false,
                false
        );
        arenas.put(normalized, arena);
        saveArena(arena);
        synchronizeArenaSettingsConfig();
        return true;
    }

    public boolean deleteArena(String id) {
        DuelArena arena = getArena(id);
        if (arena == null || reservedArenaIds.contains(arena.getId())) {
            return false;
        }

        arenas.remove(arena.getId());
        if (connection() == null) {
            return true;
        }

        try (PreparedStatement ps = connection().prepareStatement("delete from duel_arenas where id = ?")) {
            ps.setString(1, arena.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete duel arena " + arena.getId(), e);
            return false;
        }
    }

    public boolean setArenaSpawn(String id, int spawnIndex, Location location) {
        DuelArena arena = getArena(id);
        if (arena == null || location == null || location.getWorld() == null) {
            return false;
        }

        if (spawnIndex == 1) {
            arena.setSpawn1(location);
        } else if (spawnIndex == 2) {
            arena.setSpawn2(location);
        } else {
            return false;
        }

        saveArena(arena);
        return true;
    }

    public boolean setArenaReturn(String id, Location location) {
        DuelArena arena = getArena(id);
        if (arena == null || location == null || location.getWorld() == null) {
            return false;
        }

        arena.setReturnLocation(location);
        saveArena(arena);
        return true;
    }

    public boolean setArenaRegionPos(String id, int posIndex, Location location) {
        DuelArena arena = getArena(id);
        if (arena == null || location == null || location.getWorld() == null) {
            return false;
        }

        Location blockLocation = location.getBlock().getLocation();
        if (posIndex == 1) {
            arena.setSpawn1(location);
            arena.setRegionPos1(blockLocation);
        } else if (posIndex == 2) {
            arena.setSpawn2(location);
            arena.setRegionPos2(blockLocation);
        } else {
            return false;
        }

        saveArena(arena);
        return true;
    }

    public boolean setArenaDisplayName(String id, String displayName) {
        DuelArena arena = getArena(id);
        if (arena == null || displayName == null || displayName.isBlank()) {
            return false;
        }

        arena.setDisplayName(displayName.trim());
        saveArena(arena);
        return true;
    }

    public boolean setArenaEnabled(String id, boolean enabled) {
        DuelArena arena = getArena(id);
        if (arena == null) {
            return false;
        }

        arena.setEnabled(enabled);
        saveArena(arena);
        return true;
    }

    public boolean setArenaQueueEnabled(String id, boolean enabled) {
        DuelArena arena = getArena(id);
        if (arena == null) {
            return false;
        }

        arena.setQueueEnabled(enabled);
        saveArena(arena);
        return true;
    }

    public boolean sendChallenge(Player challenger, Player target, String arenaId) {
        return sendChallenge(
                challenger,
                target,
                arenaId == null || arenaId.isBlank() ? DuelMapSelection.randomStatic() : DuelMapSelection.staticArena(arenaId),
                DuelPrivacyMode.INVITE_ONLY
        );
    }

    public boolean sendChallenge(Player challenger, Player target, DuelMapSelection mapSelection, DuelPrivacyMode privacyMode) {
        if (!isEnabled()) {
            send(challenger, "&cᴅᴜᴇʟѕ ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ.");
            return false;
        }
        if (challenger == null || target == null) {
            return false;
        }
        if (challenger.getUniqueId().equals(target.getUniqueId())) {
            send(challenger, "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴅᴜᴇʟ ʏᴏᴜʀѕᴇʟꜰ.");
            return false;
        }
        if (!canEnterDuel(challenger, true) || !canEnterDuel(target, false)) {
            return false;
        }
        if (!isAcceptingDuelRequests(target)) {
            send(challenger, "&cᴛʜᴀᴛ ᴘʟᴀʏᴇʀ ɪѕ ɴᴏᴛ ᴀᴄᴄᴇᴘᴛɪɴɢ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛѕ.");
            return false;
        }

        DuelPrivacyMode resolvedPrivacyMode = privacyMode == null ? DuelPrivacyMode.INVITE_ONLY : privacyMode;
        if (!canUsePrivacyMode(challenger, target, resolvedPrivacyMode, true)) {
            return false;
        }

        DuelMapSelection resolvedSelection = mapSelection == null ? DuelMapSelection.randomStatic() : mapSelection;
        if (!isSelectionAvailable(resolvedSelection, false)) {
            send(challenger, "&cᴛʜᴀᴛ ᴅᴜᴇʟ ᴍᴀᴘ ɪѕ ɴᴏᴛ ᴀᴠᴀɪʟᴀʙʟᴇ.");
            return false;
        }

        String arenaId = resolvedSelection.type() == DuelMapSelection.Type.STATIC_ARENA ? resolvedSelection.value() : null;
        String preferredArenaId = normalizeArenaId(arenaId);
        if (preferredArenaId != null) {
            DuelArena arena = getArena(preferredArenaId);
            if (arena == null || !arena.isEnabled() || !arena.isReady()) {
                send(challenger, "&cᴛʜᴀᴛ ᴀʀᴇɴᴀ ɪѕ ɴᴏᴛ ᴀᴠᴀɪʟᴀʙʟᴇ.");
                return false;
            }
        } else if (resolvedSelection.type() == DuelMapSelection.Type.RANDOM_STATIC && getReadyEnabledArenas().isEmpty()) {
            send(challenger, "&cᴛʜᴇʀᴇ ᴀʀᴇ ɴᴏ ᴅᴜᴇʟ ᴀʀᴇɴᴀѕ ʀᴇᴀᴅʏ ʏᴇᴛ.");
            return false;
        }

        removeRequestsFor(challenger.getUniqueId(), false);
        removeRequestsFor(target.getUniqueId(), false);

        long expiresAt = System.currentTimeMillis() + (getRequestTimeoutSeconds() * 1000L);
        DuelRequest request = new DuelRequest(
                challenger.getUniqueId(),
                plainPublicName(challenger),
                target.getUniqueId(),
                plainPublicName(target),
                resolvedSelection,
                resolvedPrivacyMode,
                expiresAt
        );
        requestsByTarget.put(target.getUniqueId(), request);

        send(challenger, "&aѕᴇɴᴛ ᴀ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ᴛᴏ &f" + publicName(target) + "&a.");
        send(target, "&e" + publicName(challenger) + " &fʜᴀѕ ᴄʜᴀʟʟᴇɴɢᴇᴅ ʏᴏᴜ ᴛᴏ ᴀ ᴅᴜᴇʟ.");
        send(target, "&7ᴜѕᴇ &f/duel ᴀᴄᴄᴇᴘᴛ " + publicName(challenger) + " &7ᴏʀ &f/duel ᴅᴇɴʏ " + publicName(challenger) + "&7.");
        play(challenger, "DUELS.REQUEST-SENT");
        play(target, "DUELS.REQUEST-RECEIVED");
        return true;
    }

    public boolean acceptChallenge(Player target, String challengerName) {
        DuelRequest request = requestsByTarget.get(target.getUniqueId());
        if (request == null) {
            send(target, "&cʏᴏᴜ ʜᴀᴠᴇ ɴᴏ ᴘᴇɴᴅɪɴɢ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ.");
            return false;
        }
        if (request.isExpired(System.currentTimeMillis())) {
            requestsByTarget.remove(target.getUniqueId());
            send(target, "&cᴛʜᴀᴛ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ʜᴀѕ ᴇxᴘɪʀᴇᴅ.");
            return false;
        }
        if (!matchesIdentity(target, request.challengerUuid(), request.challengerName(), challengerName)) {
            send(target, "&cʏᴏᴜʀ ᴘᴇɴᴅɪɴɢ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ɪѕ ꜰʀᴏᴍ &f" + request.challengerName() + "&c.");
            return false;
        }

        Player challenger = Bukkit.getPlayer(request.challengerUuid());
        if (challenger == null || !challenger.isOnline()) {
            requestsByTarget.remove(target.getUniqueId());
            send(target, "&cᴛʜᴀᴛ ᴄʜᴀʟʟᴇɴɢᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴏɴʟɪɴᴇ.");
            return false;
        }

        requestsByTarget.remove(target.getUniqueId());
        if (!canEnterDuel(challenger, true) || !canEnterDuel(target, false)) {
            return false;
        }

        if (!canUsePrivacyMode(challenger, target, request.privacyMode(), true)) {
            return false;
        }

        ResolvedArena resolvedArena = resolveArena(request.mapSelection(), false);
        if (resolvedArena == null) {
            send(target, "&cɴᴏ ᴅᴜᴇʟ ᴀʀᴇɴᴀ ɪѕ ᴀᴠᴀɪʟᴀʙʟᴇ ʀɪɢʜᴛ ɴᴏᴡ.");
            send(challenger, "&cʏᴏᴜʀ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴛᴀʀᴛ ʙᴇᴄᴀᴜѕᴇ ɴᴏ ᴀʀᴇɴᴀ ɪѕ ᴀᴠᴀɪʟᴀʙʟᴇ.");
            return false;
        }

        startMatch(challenger, target, resolvedArena, DuelMatch.MatchType.DIRECT, request.privacyMode(), getLocalServerId());
        return true;
    }

    public boolean denyChallenge(Player target, String challengerName) {
        DuelRequest request = requestsByTarget.get(target.getUniqueId());
        if (request == null) {
            send(target, "&cʏᴏᴜ ʜᴀᴠᴇ ɴᴏ ᴘᴇɴᴅɪɴɢ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ.");
            return false;
        }
        if (!matchesIdentity(target, request.challengerUuid(), request.challengerName(), challengerName)) {
            send(target, "&cʏᴏᴜʀ ᴘᴇɴᴅɪɴɢ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ɪѕ ꜰʀᴏᴍ &f" + request.challengerName() + "&c.");
            return false;
        }

        requestsByTarget.remove(target.getUniqueId());
        Player challenger = Bukkit.getPlayer(request.challengerUuid());
        if (challenger != null) {
            send(challenger, "&c" + publicName(target) + " ᴅᴇɴɪᴇᴅ ʏᴏᴜʀ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ.");
        }
        send(target, "&eᴅᴇɴɪᴇᴅ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ꜰʀᴏᴍ &f" + request.challengerName() + "&e.");
        return true;
    }

    public boolean joinQueue(Player player) {
        return joinQueue(player, DuelMapSelection.randomStatic());
    }

    public boolean joinQueue(Player player, DuelMapSelection mapSelection) {
        if (!isEnabled()) {
            send(player, "&cᴅᴜᴇʟѕ ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ.");
            return false;
        }
        DuelMapSelection resolvedSelection = mapSelection == null ? DuelMapSelection.randomStatic() : mapSelection;
        if (!isSelectionAvailable(resolvedSelection, true)) {
            send(player, buildQueueUnavailableMessage());
            return false;
        }
        if (!canEnterDuel(player, true)) {
            return false;
        }
        if (queue.contains(player.getUniqueId())) {
            send(player, "&eʏᴏᴜ ᴀʀᴇ ᴀʟʀᴇᴀᴅʏ ɪɴ ᴛʜᴇ ᴄᴀѕᴜᴀʟ ᴅᴜᴇʟ ǫᴜᴇᴜᴇ.");
            return false;
        }

        removeRequestsFor(player.getUniqueId(), false);
        queue.add(player.getUniqueId());
        queueSelections.put(player.getUniqueId(), resolvedSelection);
        publishCrossServerQueueJoin(player, resolvedSelection);
        send(player, "&aᴊᴏɪɴᴇᴅ ᴛʜᴇ ᴄᴀѕᴜᴀʟ ᴅᴜᴇʟ ǫᴜᴇᴜᴇ.");
        play(player, "DUELS.QUEUE-JOIN");
        attemptQueueMatchmaking();
        return true;
    }

    public boolean leaveState(Player player) {
        if (player == null) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (preparingDuelPlayers.contains(uuid)) {
            preparingDuelPlayers.remove(uuid);
            send(player, "&eʏᴏᴜʀ ᴘʀᴇᴘᴀʀɪɴɢ ᴅᴜᴇʟ ʜᴀѕ ʙᴇᴇɴ ᴄᴀɴᴄᴇʟʟᴇᴅ.");
            return true;
        }
        if (queue.remove(uuid)) {
            queueSelections.remove(uuid);
            removeCrossServerQueueEntry(uuid);
            send(player, "&eʏᴏᴜ ʟᴇꜰᴛ ᴛʜᴇ ᴄᴀѕᴜᴀʟ ᴅᴜᴇʟ ǫᴜᴇᴜᴇ.");
            return true;
        }

        DuelRequest incoming = requestsByTarget.remove(uuid);
        if (incoming != null) {
            send(player, "&eʏᴏᴜʀ ᴘᴇɴᴅɪɴɢ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ᴡᴀѕ ᴄʟᴇᴀʀᴇᴅ.");
            return true;
        }

        if (removeOutgoingRequest(uuid)) {
            send(player, "&eʏᴏᴜʀ ᴏᴜᴛɢᴏɪɴɢ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ᴡᴀѕ ᴄᴀɴᴄᴇʟʟᴇᴅ.");
            return true;
        }

        DuelMatch match = getActiveMatch(uuid);
        if (match != null) {
            boolean active = match.getStatus() == DuelMatch.MatchStatus.ACTIVE;
            handleForfeit(player, active ? "FORFEIT" : "COUNTDOWN_LEAVE", active);
            return true;
        }

        send(player, "&cʏᴏᴜ ᴀʀᴇ ɴᴏᴛ ɪɴ ᴀ ᴅᴜᴇʟ ᴏʀ ǫᴜᴇᴜᴇ.");
        return false;
    }

    public boolean requestDraw(Player player) {
        DuelMatch match = getActiveMatch(player.getUniqueId());
        if (match == null || match.getStatus() != DuelMatch.MatchStatus.ACTIVE) {
            send(player, "&cʏᴏᴜ ᴄᴀɴ ᴏɴʟʏ ʀᴇǫᴜᴇѕᴛ ᴀ ᴅʀᴀᴡ ᴅᴜʀɪɴɢ ᴀɴ ᴀᴄᴛɪᴠᴇ ᴅᴜᴇʟ.");
            return false;
        }

        UUID requester = player.getUniqueId();
        UUID opponentUuid = match.getOpponent(requester);
        Player opponent = Bukkit.getPlayer(opponentUuid);
        if (opponent == null || !opponent.isOnline()) {
            send(player, "&cʏᴏᴜʀ ᴏᴘᴘᴏɴᴇɴᴛ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴏɴʟɪɴᴇ.");
            return false;
        }

        if (match.getDrawRequester() == null) {
            match.setDrawRequester(requester);
            match.setDrawRequestExpiresAt(System.currentTimeMillis() + (getDrawTimeoutSeconds() * 1000L));
            send(player, "&eᴅʀᴀᴡ ʀᴇǫᴜᴇѕᴛ ѕᴇɴᴛ ᴛᴏ &f" + publicName(opponent) + "&e.");
            send(opponent, "&e" + publicName(player) + " &fʜᴀѕ ʀᴇǫᴜᴇѕᴛᴇᴅ ᴀ ᴅʀᴀᴡ. ᴜѕᴇ &f/draw &fᴛᴏ ᴀᴄᴄᴇᴘᴛ.");
            return true;
        }

        if (requester.equals(match.getDrawRequester())) {
            send(player, "&eʏᴏᴜ ᴀʟʀᴇᴀᴅʏ ʀᴇǫᴜᴇѕᴛᴇᴅ ᴀ ᴅʀᴀᴡ.");
            return false;
        }

        finishMatch(match, null, null, "DRAW", false, List.of(), true);
        return true;
    }

    public void tick() {
        tickCounter++;
        boolean secondPulse = tickCounter % 20L == 0L;
        if (secondPulse) {
            expireRequests();
            cleanupQueue();
            initializeCrossServer();
            worldManager.ensureFlatPool();
            worldManager.ensureVanillaPool();
            attemptCrossServerMatchmaking();
            attemptPendingCrossServerMatches();
            attemptQueueMatchmaking();
        }

        long now = System.currentTimeMillis();
        List<DuelMatch> matches = new ArrayList<>(activeMatches.values());
        for (DuelMatch match : matches) {
            if (match.getStatus() == DuelMatch.MatchStatus.COUNTDOWN) {
                if (secondPulse) {
                    tickCountdown(match);
                }
                continue;
            }

            if (match.getStatus() != DuelMatch.MatchStatus.ACTIVE) {
                continue;
            }

            enforceArenaBorder(match);

            if (match.getDrawRequester() != null && now >= match.getDrawRequestExpiresAt()) {
                UUID requester = match.getDrawRequester();
                match.setDrawRequester(null);
                match.setDrawRequestExpiresAt(0L);
                Player requesterPlayer = Bukkit.getPlayer(requester);
                if (requesterPlayer != null) {
                    send(requesterPlayer, "&cʏᴏᴜʀ ᴅʀᴀᴡ ʀᴇǫᴜᴇѕᴛ ᴇxᴘɪʀᴇᴅ.");
                }
            }

            long remaining = Math.max(0L, (match.getEndsAt() - now + 999L) / 1000L);
            sendMatchActionBar(match, remaining);
            if (now >= match.getEndsAt()) {
                finishMatch(match, null, null, "TIMEOUT", false, List.of(), true);
            }
        }
    }

    public void handleArenaBorderMove(PlayerMoveEvent event) {
        if (event == null || event.getTo() == null) {
            return;
        }

        Player player = event.getPlayer();
        DuelMatch match = getActiveMatch(player.getUniqueId());
        if (match == null || !match.usesGeneratedWorld() || !worldManager.isBorderEnabled()) {
            return;
        }

        if (!"PUSH_BACK".equals(worldManager.getBorderAction())) {
            return;
        }

        if (isInsideGeneratedArenaBorder(match, event.getTo())) {
            borderEscapeTicks.remove(player.getUniqueId());
            return;
        }

        if (isInsideGeneratedArenaBorder(match, event.getFrom())) {
            event.setTo(event.getFrom());
        } else {
            pushPlayerBackToArena(player, match);
        }
    }

    public boolean isCommandAllowedDuringMatch(String rawCommand) {
        if (!config().getBoolean("COMMAND_BLOCK.ENABLED", true)) {
            return true;
        }

        String raw = normalizeCommandPattern(rawCommand);
        if (raw.isBlank()) {
            return true;
        }

        String mode = config().getString("COMMAND_BLOCK.MODE", "ALLOWLIST").trim().toUpperCase(Locale.ROOT);
        if ("BLOCKLIST".equals(mode)) {
            for (String blocked : commandPatterns("COMMAND_BLOCK.BLOCKLIST", List.of("/tpa", "/home", "/spawn", "/rtp"))) {
                if (matchesCommandPattern(raw, blocked)) {
                    return false;
                }
            }
            return true;
        }

        for (String allowed : commandPatterns("COMMAND_BLOCK.ALLOWLIST", List.of("/duel", "/draw", "/leave", "/queue", "/create"))) {
            if (matchesCommandPattern(raw, allowed)) {
                return true;
            }
        }
        return false;
    }

    public String getCommandBlockedMessage() {
        return config().getString(
                "COMMAND_BLOCK.MESSAGE",
                "&cyou cannot use that command during a duel."
        );
    }

    public boolean handleDuelDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        DuelMatch match = getActiveMatch(victim.getUniqueId());
        if (match == null) {
            return false;
        }

        UUID victimUuid = victim.getUniqueId();
        UUID winnerUuid = match.getOpponent(victimUuid);
        Player winner = winnerUuid == null ? null : Bukkit.getPlayer(winnerUuid);
        List<ItemStack> loot = copyLoot(event.getDrops());

        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);
        if (match.usesGeneratedWorld()) {
            event.setKeepInventory(true);
        }

        finishMatch(match, winnerUuid, victimUuid, "DEATH", false, loot, true);

        if (winner != null) {
            send(winner, "&aʏᴏᴜ ᴅᴇꜰᴇᴀᴛᴇᴅ &f" + publicName(victim) + "&a.");
        }
        send(victim, "&cʏᴏᴜ ʟᴏѕᴛ ᴛʜᴇ ᴅᴜᴇʟ ᴀɢᴀɪɴѕᴛ &f" + match.getOpponentName(victimUuid) + "&c.");
        return true;
    }

    public boolean handleLethalPvPHit(Player attacker, Player victim) {
        if (attacker == null || victim == null) {
            return false;
        }

        DuelMatch match = getActiveMatch(victim.getUniqueId());
        if (match == null || match.getStatus() != DuelMatch.MatchStatus.ACTIVE) {
            return false;
        }
        if (!match.isParticipant(attacker.getUniqueId())
                || !attacker.getUniqueId().equals(match.getOpponent(victim.getUniqueId()))) {
            return false;
        }

        List<ItemStack> loot = extractInventory(victim);
        finishMatch(match, attacker.getUniqueId(), victim.getUniqueId(), "PVP_KILL", false, loot, true);
        PlayerRespawnListener.scheduleChainmailKit(plugin, victim, getReturnDelayTicks() + 2L);

        send(attacker, "&aʏᴏᴜ ᴅᴇꜰᴇᴀᴛᴇᴅ &f" + publicName(victim) + "&a.");
        send(victim, "&cʏᴏᴜ ʟᴏѕᴛ ᴛʜᴇ ᴅᴜᴇʟ ᴀɢᴀɪɴѕᴛ &f" + publicName(attacker) + "&c.");
        return true;
    }

    public boolean consumeRespawn(Player player, org.bukkit.event.player.PlayerRespawnEvent event) {
        PendingRespawnState state = pendingRespawns.remove(player.getUniqueId());
        if (state == null || state.respawnLocation() == null || state.respawnLocation().getWorld() == null) {
            return false;
        }

        event.setRespawnLocation(state.respawnLocation());
        plugin.getSpigotScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }
            applyTransitionState(player);
            showStoredTransitionTitle(player);
            if (state.restoreSnapshot() != null) {
                restoreSnapshotDirectly(player, state.restoreSnapshot());
            }
            teleportAfterDelay(player.getUniqueId(), state.returnLocation(), state.delayTicks(), true);
        });
        return true;
    }

    public void handleJoin(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (isTransitioning(uuid) || transitionStates.containsKey(uuid)) {
            restoreTransitionState(player);
        }

        DuelArena arena = findArenaContainingLocation(player.getLocation());
        if (arena == null) {
            return;
        }

        Location fallbackLocation = resolveArenaJoinFallbackLocation(arena, player.getLocation());
        if (fallbackLocation == null || fallbackLocation.getWorld() == null) {
            return;
        }

        plugin.getSpigotScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }

            DuelArena currentArena = findArenaContainingLocation(player.getLocation());
            if (currentArena == null) {
                return;
            }

            Location destination = resolveArenaJoinFallbackLocation(currentArena, player.getLocation());
            if (destination == null || destination.getWorld() == null) {
                return;
            }

            String arenaName = currentArena.getDisplayName();
            plugin.getSpigotScheduler().teleport(player, destination).thenAccept(success ->
                    plugin.getSpigotScheduler().runEntity(player, () -> {
                        if (!Boolean.TRUE.equals(success) || !player.isOnline()) {
                            return;
                        }
                        player.resetPlayerTime();
                        player.resetPlayerWeather();
                        player.setNoDamageTicks(60);
                        player.setFallDistance(0F);
                        player.setFireTicks(0);
                        send(player, "&eʏᴏᴜ ᴡᴇʀᴇ ᴍᴏᴠᴇᴅ ᴏᴜᴛ ᴏꜰ ᴅᴜᴇʟ ᴀʀᴇɴᴀ &f" + arenaName + "&e ᴀꜰᴛᴇʀ ʀᴇᴄᴏɴɴᴇᴄᴛɪɴɢ.");
                    }));
        });
    }

    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }

        if (isTransitioning(player.getUniqueId()) || transitionStates.containsKey(player.getUniqueId())) {
            restoreTransitionState(player);
        }

        pendingRespawns.remove(player.getUniqueId());
        queue.remove(player.getUniqueId());
        queueSelections.remove(player.getUniqueId());
        removeCrossServerQueueEntry(player.getUniqueId());
        requestsByTarget.remove(player.getUniqueId());
        removeOutgoingRequest(player.getUniqueId());

        DuelMatch match = getActiveMatch(player.getUniqueId());
        if (match == null) {
            return;
        }

        boolean active = match.getStatus() == DuelMatch.MatchStatus.ACTIVE;
        handleForfeit(player, active ? "QUIT" : "COUNTDOWN_QUIT", active);
    }

    private void tickCountdown(DuelMatch match) {
        Player first = Bukkit.getPlayer(match.getPlayerOneUuid());
        Player second = Bukkit.getPlayer(match.getPlayerTwoUuid());
        if (first == null || second == null) {
            finishMatch(match, null, null, "COUNTDOWN_FAIL", true, List.of(), false);
            return;
        }

        int remaining = match.getCountdownSecondsRemaining();
        if (remaining > 0) {
            sendCountdownTick(first, remaining);
            sendCountdownTick(second, remaining);
            match.decrementCountdown();
            return;
        }

        if (match.usesGeneratedWorld()) {
            rememberGeneratedInventorySnapshot(match, first, second);
        }

        match.setStatus(DuelMatch.MatchStatus.ACTIVE);
        long now = System.currentTimeMillis();
        match.setStartedAt(now);
        match.setEndsAt(now + (getMatchDurationSeconds() * 1000L));
        sendCountdownStart(first);
        sendCountdownStart(second);
        send(first, "&aᴅᴜᴇʟ ѕᴛᴀʀᴛᴇᴅ ᴀɢᴀɪɴѕᴛ &f" + publicName(second) + "&a.");
        send(second, "&aᴅᴜᴇʟ ѕᴛᴀʀᴛᴇᴅ ᴀɢᴀɪɴѕᴛ &f" + publicName(first) + "&a.");
        playCountdownStartSound(first);
        playCountdownStartSound(second);
    }

    private void sendCountdownTick(Player player, int remaining) {
        if (player == null || !player.isOnline()) {
            return;
        }

        ConfigurationSection countdown = config().getConfigurationSection("START-COUNTDOWN");
        if (countdown == null || !countdown.getBoolean("ENABLED", true)) {
            TitleUtils.sendTitle(player, "&e" + remaining, "&7ᴅᴜᴇʟ ѕᴛᴀʀᴛѕ ѕᴏᴏɴ", 0, 15, 5);
            return;
        }

        ConfigurationSection titles = countdown.getConfigurationSection("TITLES");
        String title = titles == null ? null : titles.getString(String.valueOf(remaining));
        if (title == null) {
            title = "&e" + remaining;
        }

        TitleUtils.sendTitle(player, title, "", 0, 20, 0);

        ConfigurationSection messages = countdown.getConfigurationSection("MESSAGES");
        String message = messages == null ? null : messages.getString(String.valueOf(remaining));
        if (message != null && !message.isBlank()) {
            send(player, message);
        }

        playCountdownTickSound(player, remaining);
    }

    private void sendCountdownStart(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        ConfigurationSection countdown = config().getConfigurationSection("START-COUNTDOWN");
        if (countdown == null || !countdown.getBoolean("ENABLED", true)) {
            TitleUtils.sendTitle(player, "&aꜰɪɢʜᴛ!", "&7ᴅᴇꜰᴇᴀᴛ ʏᴏᴜʀ ᴏᴘᴘᴏɴᴇɴᴛ", 0, 25, 10);
            return;
        }

        ConfigurationSection titles = countdown.getConfigurationSection("TITLES");
        String title = titles == null ? null : titles.getString("0");
        if (title == null) {
            title = "&a&lfight!";
        }

        TitleUtils.sendTitle(player, title, "", 0, 24, 3);

        String startMessage = countdown.getString("START-MESSAGE", "");
        if (!startMessage.isBlank()) {
            send(player, startMessage);
        }
    }

    private void playCountdownTickSound(Player player, int remaining) {
        ConfigurationSection countdown = config().getConfigurationSection("START-COUNTDOWN");
        if (player == null || countdown == null || !countdown.getBoolean("SOUNDS.ENABLED", true)) {
            return;
        }

        String sound = plugin.getConfigManager().getSound("DUELS.START-COUNTDOWN.PER-SECOND." + remaining);
        if (sound != null && !sound.isBlank()) {
            SoundUtils.play(plugin, player, sound, PlayerSettingUtils.SoundChannel.DUEL);
        }
    }

    private void playCountdownStartSound(Player player) {
        ConfigurationSection countdown = config().getConfigurationSection("START-COUNTDOWN");
        if (player == null) {
            return;
        }

        if (countdown != null && countdown.getBoolean("SOUNDS.ENABLED", true)) {
            String sound = plugin.getConfigManager().getSound("DUELS.START-COUNTDOWN.START-SOUND");
            if (sound != null && !sound.isBlank()) {
                SoundUtils.play(plugin, player, sound, PlayerSettingUtils.SoundChannel.DUEL);
                return;
            }
        }

        play(player, "DUELS.MATCH-START");
    }

    private void sendMatchActionBar(DuelMatch match, long remainingSeconds) {
        Player first = Bukkit.getPlayer(match.getPlayerOneUuid());
        Player second = Bukkit.getPlayer(match.getPlayerTwoUuid());
        if (first != null && first.isOnline()) {
            String text = "&eopponent: &f" + match.getPlayerTwoName() + " &8| &etime left: &f" + formatDuration(remainingSeconds);
            if (match.getDrawRequester() != null && !match.getDrawRequester().equals(first.getUniqueId())) {
                text += " &8| &a/draw to accept";
            }
            com.bx.ultimateDonutSmp.utils.PlayerSettingUtils.sendActionBar(plugin, first, text);
        }
        if (second != null && second.isOnline()) {
            String text = "&eopponent: &f" + match.getPlayerOneName() + " &8| &etime left: &f" + formatDuration(remainingSeconds);
            if (match.getDrawRequester() != null && !match.getDrawRequester().equals(second.getUniqueId())) {
                text += " &8| &a/draw to accept";
            }
            com.bx.ultimateDonutSmp.utils.PlayerSettingUtils.sendActionBar(plugin, second, text);
        }
    }

    private void handleForfeit(Player loser, String reason, boolean transferLoot) {
        if (loser == null) {
            return;
        }

        DuelMatch match = getActiveMatch(loser.getUniqueId());
        if (match == null) {
            return;
        }

        UUID loserUuid = loser.getUniqueId();
        UUID winnerUuid = match.getOpponent(loserUuid);
        List<ItemStack> loot = transferLoot ? extractInventory(loser) : List.of();
        finishMatch(match, winnerUuid, loserUuid, reason, false, loot, true);
    }

    private void finishMatch(DuelMatch match,
                             UUID winnerUuid,
                             UUID loserUuid,
                             String endReason,
                             boolean cancelled,
                             List<ItemStack> loot,
                             boolean recordStats) {
        if (match == null || match.getStatus() == DuelMatch.MatchStatus.FINISHED) {
            return;
        }

        match.setStatus(DuelMatch.MatchStatus.FINISHED);
        activeMatches.remove(match.getId());
        activeMatchIds.remove(match.getPlayerOneUuid());
        activeMatchIds.remove(match.getPlayerTwoUuid());
        reservedArenaIds.remove(match.getArena().getId());
        borderEscapeTicks.remove(match.getPlayerOneUuid());
        borderEscapeTicks.remove(match.getPlayerTwoUuid());

        Player winner = winnerUuid == null ? null : Bukkit.getPlayer(winnerUuid);
        Player loser = loserUuid == null ? null : Bukkit.getPlayer(loserUuid);

        if (recordStats) {
            if (winnerUuid != null && loserUuid != null) {
                updateStatsAfterWin(winnerUuid, loserUuid);
            } else if (!cancelled) {
                updateStatsAfterDraw(match.getPlayerOneUuid(), match.getPlayerTwoUuid());
            }
        }

        updateMatchRecord(match, winnerUuid, loserUuid, endReason);

        List<ItemStack> claimLoot = loot == null ? List.of() : loot;
        GeneratedInventorySnapshot restoreSnapshot = null;
        if (match.usesGeneratedWorld()) {
            if (loserUuid == null || !"DEATH".equalsIgnoreCase(endReason)) {
                restoreGeneratedInventory(match, match.getPlayerOneUuid(),
                        match.getPlayerOneUuid().equals(loserUuid) ? claimLoot : List.of());
                restoreGeneratedInventory(match, match.getPlayerTwoUuid(),
                        match.getPlayerTwoUuid().equals(loserUuid) ? claimLoot : List.of());
            } else {
                UUID winnerUuidToRestore = match.getPlayerOneUuid().equals(loserUuid) ? match.getPlayerTwoUuid() : match.getPlayerOneUuid();
                restoreGeneratedInventory(match, winnerUuidToRestore, List.of());
                restoreSnapshot = computeRestoredInventory(match, loserUuid, claimLoot);
            }
            cleanupGeneratedTransientEntities(match);
            generatedMatchInventorySnapshots.remove(match.getId());
        }

        if (winnerUuid != null && !claimLoot.isEmpty()) {
            storeLootClaimPackage(winnerUuid, winner, match.getId(), resolveParticipantName(match, loserUuid), claimLoot);
        }

        if (winner != null && loser != null) {
            storeTransitionTitle(
                    winner.getUniqueId(),
                    formatResultTitle("victory", publicName(winner), publicName(loser), "&e&lvictory!"),
                    formatResultSubtitle("victory", publicName(winner), publicName(loser), "&e" + publicName(winner) + " &fwon the match!")
            );
            storeTransitionTitle(
                    loser.getUniqueId(),
                    formatResultTitle("defeat", publicName(loser), publicName(winner), "&c&ldefeat!"),
                    formatResultSubtitle("defeat", publicName(loser), publicName(winner), "&c" + publicName(winner) + " &fwon this match!")
            );
            play(winner, "DUELS.VICTORY");
            play(loser, "DUELS.DEFEAT");
        } else if (!cancelled) {
            Player first = Bukkit.getPlayer(match.getPlayerOneUuid());
            Player second = Bukkit.getPlayer(match.getPlayerTwoUuid());
            String drawTitle = formatResultTitle("draw", null, null, "&e&ldraw!");
            String drawSubtitle = formatResultSubtitle(
                    "draw",
                    null,
                    null,
                    "TIMEOUT".equalsIgnoreCase(endReason) ? "&ftime's up - no winner." : "&7no one won this duel"
            );
            if (first != null) {
                storeTransitionTitle(first.getUniqueId(), drawTitle, drawSubtitle);
            }
            if (second != null) {
                storeTransitionTitle(second.getUniqueId(), drawTitle, drawSubtitle);
            }

            if ("TIMEOUT".equalsIgnoreCase(endReason)) {
                String timeoutMessage = formatResultMessage(
                        "draw",
                        null,
                        null,
                        "&e[timer] &ftime limit reached! match ended as a &edraw &f- streaks unchanged."
                );
                if (first != null && !timeoutMessage.isBlank()) {
                    send(first, timeoutMessage);
                }
                if (second != null && !timeoutMessage.isBlank()) {
                    send(second, timeoutMessage);
                }
            }
        }

        scheduleTransitionAndReturn(match, winnerUuid, loserUuid, endReason, restoreSnapshot);
        plugin.getCombatManager().clearTag(match.getPlayerOneUuid());
        plugin.getCombatManager().clearTag(match.getPlayerTwoUuid());
    }

    private void scheduleTransitionAndReturn(DuelMatch match, UUID winnerUuid, UUID loserUuid, String endReason, GeneratedInventorySnapshot restoreSnapshot) {
        int delayTicks = getReturnDelayTicks();

        prepareTransition(match.getPlayerOneUuid());
        prepareTransition(match.getPlayerTwoUuid());

        if ("DEATH".equalsIgnoreCase(endReason) && loserUuid != null) {
            pendingRespawns.put(loserUuid, new PendingRespawnState(
                    resolveArenaStayLocation(match, loserUuid),
                    resolveReturnLocation(match, loserUuid),
                    delayTicks,
                    restoreSnapshot
            ));
        }

        if (loserUuid != null && shouldGrantPostReturnRespawnKit(endReason)) {
            Player loser = Bukkit.getPlayer(loserUuid);
            if (loser != null && loser.isOnline()) {
                PlayerRespawnListener.scheduleChainmailKit(plugin, loser, delayTicks + 2L);
            }
        }

        if (winnerUuid != null) {
            teleportAfterDelay(winnerUuid, resolveReturnLocation(match, winnerUuid), delayTicks, true);
        }

        if (loserUuid != null && !"DEATH".equalsIgnoreCase(endReason)) {
            teleportAfterDelay(loserUuid, resolveReturnLocation(match, loserUuid), delayTicks, true);
        }

        if (winnerUuid == null && loserUuid == null) {
            teleportAfterDelay(match.getPlayerOneUuid(), resolveReturnLocation(match, match.getPlayerOneUuid()), delayTicks, true);
            teleportAfterDelay(match.getPlayerTwoUuid(), resolveReturnLocation(match, match.getPlayerTwoUuid()), delayTicks, true);
        }

        if (match.usesGeneratedWorld()) {
            plugin.getSpigotScheduler().runGlobalLater(
                    () -> {
                        rollbackGeneratedArena(match);
                        cleanupGeneratedTransientEntities(match);
                        worldManager.cleanupGeneratedWorld(match.getGeneratedWorldName());
                    },
                    delayTicks + 40L
            );
        } else {
            plugin.getSpigotScheduler().runGlobalLater(() -> rollbackArena(match.getId()), delayTicks + 1L);
        }
    }

    private void teleportAfterDelay(UUID uuid, Location location, long delayTicks, boolean clearTransition) {
        if (uuid == null || location == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }

        plugin.getSpigotScheduler().runEntityLater(player, () -> {
            if (player.isOnline()) {
                plugin.getSpigotScheduler().teleport(player, location).thenAccept(success ->
                        plugin.getSpigotScheduler().runEntity(player, () -> {
                            if (!Boolean.TRUE.equals(success) || !player.isOnline()) {
                                return;
                            }
                            healPlayer(player);
                            if (clearTransition) {
                                restoreTransitionState(player);
                            }
                        }));
            }
        }, delayTicks);
    }

    private boolean shouldGrantPostReturnRespawnKit(String endReason) {
        if (endReason == null || endReason.isBlank()) {
            return false;
        }

        return endReason.equalsIgnoreCase("FORFEIT")
                || endReason.equalsIgnoreCase("QUIT");
    }

    private void healPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        player.setHealth(AttributeUtils.getMaxHealth(player));
        player.setFoodLevel(20);
        player.setSaturation(20F);
        player.setFireTicks(0);
        player.setFallDistance(0F);
    }

    private void storeTransitionTitle(UUID uuid, String title, String subtitle) {
        if (uuid == null) {
            return;
        }

        transitionTitles.put(uuid, new TransitionTitleState(title, subtitle));
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            showStoredTransitionTitle(player);
        }
    }

    private void showStoredTransitionTitle(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        TransitionTitleState state = transitionTitles.get(player.getUniqueId());
        if (state == null) {
            return;
        }

        long delayMillis = Math.max(1000L, getReturnDelayTicks() * 50L);
        TitleUtils.sendTitle(player, state.title(), state.subtitle(), 0, (int) Math.max(1L, delayMillis / 50L), 0);
    }

    private String formatResultTitle(String key, String playerName, String opponentName, String fallback) {
        String raw = config().getString("RESULT-TITLES." + key + ".TITLE", fallback);
        return applyResultPlaceholders(raw, playerName, opponentName);
    }

    private String formatResultSubtitle(String key, String playerName, String opponentName, String fallback) {
        String raw = config().getString("RESULT-TITLES." + key + ".SUBTITLE", fallback);
        return applyResultPlaceholders(raw, playerName, opponentName);
    }

    private String formatResultMessage(String key, String playerName, String opponentName, String fallback) {
        String raw = config().getString("RESULT-TITLES." + key + ".MESSAGE", fallback);
        return applyResultPlaceholders(raw, playerName, opponentName);
    }

    private String applyResultPlaceholders(String text, String playerName, String opponentName) {
        String resolved = text == null ? "" : text;
        resolved = resolved.replace("<player>", playerName == null ? "Player" : playerName);
        resolved = resolved.replace("<opponent>", opponentName == null ? "Opponent" : opponentName);
        return resolved;
    }

    private void updateStatsAfterWin(UUID winnerUuid, UUID loserUuid) {
        DuelStats winnerStats = getStats(winnerUuid).recordWin();
        DuelStats loserStats = getStats(loserUuid).recordLoss();
        statsCache.put(winnerUuid, winnerStats);
        statsCache.put(loserUuid, loserStats);
        saveStats(winnerUuid, winnerStats);
        saveStats(loserUuid, loserStats);
    }

    private void updateStatsAfterDraw(UUID firstUuid, UUID secondUuid) {
        DuelStats firstStats = getStats(firstUuid).recordDraw();
        DuelStats secondStats = getStats(secondUuid).recordDraw();
        statsCache.put(firstUuid, firstStats);
        statsCache.put(secondUuid, secondStats);
        saveStats(firstUuid, firstStats);
        saveStats(secondUuid, secondStats);
    }

    private void storeLootClaimPackage(UUID winnerUuid, Player winner, long matchId, String defeatedName, List<ItemStack> loot) {
        if (winnerUuid == null || loot == null || loot.isEmpty()) {
            return;
        }

        int blockedItems = 0;
        for (ItemStack item : loot) {
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }
            CrashProtectionManager.ValidationResult safetyResult = plugin.getCrashProtectionManager()
                    .validateForStorage(item, CrashProtectionManager.Context.DUELS);
            if (!safetyResult.allowed()) {
                blockedItems++;
                plugin.getCrashProtectionManager().logBlockedItem(
                        "duel loot match #" + matchId,
                        item,
                        CrashProtectionManager.Context.DUELS,
                        safetyResult
                );
                continue;
            }
            createClaim(winnerUuid, matchId, defeatedName, item.clone());
        }

        if (winner != null) {
            String name = defeatedName == null || defeatedName.isBlank() ? "your opponent" : defeatedName;
            send(winner, "&eʟᴏᴏᴛ ꜰʀᴏᴍ &f" + name + " &eʜᴀѕ ʙᴇᴇɴ ѕᴇɴᴛ ᴛᴏ ʏᴏᴜʀ ᴅᴜᴇʟ ᴄʟᴀɪᴍѕ.");
        }
    }

    private void createClaim(UUID playerUuid, long matchId, String defeatedName, ItemStack item) {
        if (playerUuid == null || item == null || item.getType().isAir() || connection() == null) {
            return;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "insert into duel_claims (player_uuid, match_id, defeated_name, item_data, created_at) values (?,?,?,?,?)")) {
            ps.setString(1, playerUuid.toString());
            ps.setLong(2, matchId);
            ps.setString(3, defeatedName == null ? "" : defeatedName);
            ps.setString(4, serializeItem(item));
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create duel claim for " + playerUuid, e);
        }
    }

    private void attemptQueueMatchmaking() {
        if (queue.size() < 2) {
            return;
        }

        List<UUID> queuedPlayers = new ArrayList<>(queue);
        for (UUID uuid : queuedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || isInDuel(uuid)) {
                queue.remove(uuid);
                queueSelections.remove(uuid);
                removeCrossServerQueueEntry(uuid);
            }
        }

        while (queue.size() >= 2) {
            List<UUID> available = new ArrayList<>(queue);
            if (available.size() < 2) {
                return;
            }

            UUID firstUuid = available.get(0);
            UUID secondUuid = available.get(1);
            Player first = Bukkit.getPlayer(firstUuid);
            Player second = Bukkit.getPlayer(secondUuid);
            if (first == null || second == null || !first.isOnline() || !second.isOnline()) {
                queue.remove(firstUuid);
                queue.remove(secondUuid);
                queueSelections.remove(firstUuid);
                queueSelections.remove(secondUuid);
                removeCrossServerQueueEntry(firstUuid);
                removeCrossServerQueueEntry(secondUuid);
                continue;
            }

            DuelMapSelection selection = queueSelections.getOrDefault(firstUuid, DuelMapSelection.randomStatic());
            if (selection.usesGeneratedWorld()) {
                queue.remove(firstUuid);
                queue.remove(secondUuid);
                queueSelections.remove(firstUuid);
                queueSelections.remove(secondUuid);
                removeCrossServerQueueEntry(firstUuid);
                removeCrossServerQueueEntry(secondUuid);
                prepareGeneratedQueueMatch(firstUuid, secondUuid, selection);
                continue;
            }

            ResolvedArena resolvedArena = resolveArena(selection, true);
            if (resolvedArena == null) {
                return;
            }

            queue.remove(firstUuid);
            queue.remove(secondUuid);
            queueSelections.remove(firstUuid);
            queueSelections.remove(secondUuid);
            removeCrossServerQueueEntry(firstUuid);
            removeCrossServerQueueEntry(secondUuid);
            startMatch(first, second, resolvedArena, DuelMatch.MatchType.QUEUE, DuelPrivacyMode.INVITE_ONLY, getLocalServerId());
        }
    }

    private void prepareGeneratedQueueMatch(UUID firstUuid, UUID secondUuid, DuelMapSelection selection) {
        if (firstUuid == null || secondUuid == null) {
            return;
        }

        preparingDuelPlayers.add(firstUuid);
        preparingDuelPlayers.add(secondUuid);
        send(Bukkit.getPlayer(firstUuid), "&eᴘʀᴇᴘᴀʀɪɴɢ ᴅᴜᴇʟ ʙɪᴏᴍᴇ ᴀʀᴇɴᴀ...");
        send(Bukkit.getPlayer(secondUuid), "&eᴘʀᴇᴘᴀʀɪɴɢ ᴅᴜᴇʟ ʙɪᴏᴍᴇ ᴀʀᴇɴᴀ...");

        scheduleGeneratedQueuePreparation(firstUuid, secondUuid, selection, 1L);
    }

    private void scheduleGeneratedQueuePreparation(UUID firstUuid, UUID secondUuid, DuelMapSelection selection, long delayTicks) {
        plugin.getSpigotScheduler().runGlobalLater(() -> {
            Player first = Bukkit.getPlayer(firstUuid);
            Player second = Bukkit.getPlayer(secondUuid);
            if (!preparingDuelPlayers.contains(firstUuid) || !preparingDuelPlayers.contains(secondUuid)) {
                preparingDuelPlayers.remove(firstUuid);
                preparingDuelPlayers.remove(secondUuid);
                send(first, "&cᴅᴜᴇʟ ᴄᴀɴᴄᴇʟʟᴇᴅ ʙᴇᴄᴀᴜѕᴇ ᴏɴᴇ ᴘʟᴀʏᴇʀ ʟᴇꜰᴛ ᴘʀᴇᴘᴀʀᴀᴛɪᴏɴ.");
                send(second, "&cᴅᴜᴇʟ ᴄᴀɴᴄᴇʟʟᴇᴅ ʙᴇᴄᴀᴜѕᴇ ᴏɴᴇ ᴘʟᴀʏᴇʀ ʟᴇꜰᴛ ᴘʀᴇᴘᴀʀᴀᴛɪᴏɴ.");
                return;
            }

            if (!canStartPreparedDuel(first) || !canStartPreparedDuel(second)) {
                preparingDuelPlayers.remove(firstUuid);
                preparingDuelPlayers.remove(secondUuid);
                send(first, "&cᴅᴜᴇʟ ᴄᴀɴᴄᴇʟʟᴇᴅ ʙᴇᴄᴀᴜѕᴇ ᴏɴᴇ ᴘʟᴀʏᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴠᴀɪʟᴀʙʟᴇ.");
                send(second, "&cᴅᴜᴇʟ ᴄᴀɴᴄᴇʟʟᴇᴅ ʙᴇᴄᴀᴜѕᴇ ᴏɴᴇ ᴘʟᴀʏᴇʀ ɪѕ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴠᴀɪʟᴀʙʟᴇ.");
                return;
            }

            ResolvedArena resolvedArena = resolveArena(selection, true);
            if (resolvedArena == null) {
                if (worldManager.canPrepareGeneratedArenas()) {
                    scheduleGeneratedQueuePreparation(firstUuid, secondUuid, selection, 20L);
                    return;
                }

                preparingDuelPlayers.remove(firstUuid);
                preparingDuelPlayers.remove(secondUuid);
                send(first, "&cɴᴏ ᴅᴜᴇʟ ʙɪᴏᴍᴇ ᴀʀᴇɴᴀ ɪѕ ᴀᴠᴀɪʟᴀʙʟᴇ ʀɪɢʜᴛ ɴᴏᴡ.");
                send(second, "&cɴᴏ ᴅᴜᴇʟ ʙɪᴏᴍᴇ ᴀʀᴇɴᴀ ɪѕ ᴀᴠᴀɪʟᴀʙʟᴇ ʀɪɢʜᴛ ɴᴏᴡ.");
                return;
            }

            preparingDuelPlayers.remove(firstUuid);
            preparingDuelPlayers.remove(secondUuid);
            startMatch(first, second, resolvedArena, DuelMatch.MatchType.QUEUE, DuelPrivacyMode.INVITE_ONLY, getLocalServerId());
        }, delayTicks);
    }

    private boolean canStartPreparedDuel(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (isInDuel(uuid) || isInQueue(uuid)) {
            return false;
        }
        return plugin.getFfaManager() == null || !plugin.getFfaManager().isBusy(uuid);
    }

    private void startMatch(Player first, Player second, ResolvedArena resolvedArena, DuelMatch.MatchType type,
                            DuelPrivacyMode privacyMode, String hostServerId) {
        if (first == null || second == null || resolvedArena == null || resolvedArena.arena() == null) {
            return;
        }
        boolean firstInventorySafe = validatePlayerInventoryForDuel(first);
        boolean secondInventorySafe = validatePlayerInventoryForDuel(second);
        if (!firstInventorySafe || !secondInventorySafe) {
            if (firstInventorySafe) {
                send(first, "&cᴛʜᴇ ᴅᴜᴇʟ ᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴛᴀʀᴛ ʙᴇᴄᴀᴜѕᴇ ʏᴏᴜʀ ᴏᴘᴘᴏɴᴇɴᴛ ʜᴀѕ ᴜɴѕᴀꜰᴇ ɪᴛᴇᴍ ᴅᴀᴛᴀ.");
            }
            if (secondInventorySafe) {
                send(second, "&cᴛʜᴇ ᴅᴜᴇʟ ᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴛᴀʀᴛ ʙᴇᴄᴀᴜѕᴇ ʏᴏᴜʀ ᴏᴘᴘᴏɴᴇɴᴛ ʜᴀѕ ᴜɴѕᴀꜰᴇ ɪᴛᴇᴍ ᴅᴀᴛᴀ.");
            }
            if (resolvedArena.generatedWorldName() != null && !resolvedArena.generatedWorldName().isBlank()) {
                worldManager.cleanupGeneratedWorld(resolvedArena.generatedWorldName());
            }
            return;
        }

        DuelArena arena = resolvedArena.arena();
        long matchId = insertMatch(type, arena, first.getUniqueId(), second.getUniqueId(), resolvedArena.selection(),
                resolvedArena.biomeKey(), resolvedArena.generatedWorldName(), privacyMode, hostServerId);
        if (matchId <= 0L) {
            if (resolvedArena.generatedWorldName() != null && !resolvedArena.generatedWorldName().isBlank()) {
                worldManager.cleanupGeneratedWorld(resolvedArena.generatedWorldName());
            }
            send(first, "&cᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴛᴀʀᴛ ᴛʜᴇ ᴅᴜᴇʟ ʀɪɢʜᴛ ɴᴏᴡ.");
            send(second, "&cᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴛᴀʀᴛ ᴛʜᴇ ᴅᴜᴇʟ ʀɪɢʜᴛ ɴᴏᴡ.");
            return;
        }

        DuelMatch match = new DuelMatch(
                matchId,
                type,
                arena,
                resolvedArena.selection(),
                first.getUniqueId(),
                publicName(first),
                second.getUniqueId(),
                publicName(second),
                getCountdownSeconds(),
                resolvedArena.biomeKey(),
                resolvedArena.generatedWorldName(),
                privacyMode,
                hostServerId
        );
        match.setReturnLocation(first.getUniqueId(), first.getLocation());
        match.setReturnLocation(second.getUniqueId(), second.getLocation());

        activeMatches.put(matchId, match);
        activeMatchIds.put(first.getUniqueId(), matchId);
        activeMatchIds.put(second.getUniqueId(), matchId);
        reservedArenaIds.add(arena.getId());
        transitioningPlayers.remove(first.getUniqueId());
        transitioningPlayers.remove(second.getUniqueId());

        if (!match.usesGeneratedWorld() && arena.hasRollbackRegion()) {
            ArenaSnapshot snapshot = captureArenaSnapshot(arena);
            if (snapshot != null) {
                arenaSnapshots.put(matchId, snapshot);
            } else {
                plugin.getLogger().warning("Failed to capture rollback snapshot for duel arena " + arena.getId());
            }
        }

        preparePlayerForMatch(first, arena.getSpawn1(), arena);
        preparePlayerForMatch(second, arena.getSpawn2(), arena);

        send(first, "&aᴅᴜᴇʟ ꜰᴏᴜɴᴅ ᴀɢᴀɪɴѕᴛ &f" + publicName(second) + "&a ᴏɴ ᴀʀᴇɴᴀ &f" + arena.getDisplayName() + "&a.");
        send(second, "&aᴅᴜᴇʟ ꜰᴏᴜɴᴅ ᴀɢᴀɪɴѕᴛ &f" + publicName(first) + "&a ᴏɴ ᴀʀᴇɴᴀ &f" + arena.getDisplayName() + "&a.");
        play(first, "DUELS.MATCH-FOUND");
        play(second, "DUELS.MATCH-FOUND");
    }

    private void preparePlayerForMatch(Player player, Location teleportLocation, DuelArena arena) {
        player.closeInventory();
        restoreTransitionState(player);
        player.setGameMode(GameMode.SURVIVAL);
        healPlayer(player);
        applyArenaRules(player, arena);
        if (teleportLocation != null) {
            plugin.getSpigotScheduler().teleport(player, teleportLocation);
        }
    }

    private void applyArenaRules(Player player, DuelArena arena) {
        if (player == null) {
            return;
        }

        if (arena == null) {
            player.resetPlayerTime();
            player.resetPlayerWeather();
            return;
        }

        if (arena.isNoHunger()) {
            player.setFoodLevel(20);
            player.setSaturation(20F);
            player.setExhaustion(0F);
        }

        if (arena.isAlwaysMorning()) {
            player.setPlayerTime(1000L, false);
        } else {
            player.resetPlayerTime();
        }

        if (arena.isNoWeather()) {
            player.setPlayerWeather(WeatherType.CLEAR);
        } else {
            player.resetPlayerWeather();
        }
    }

    private void syncArenaRulesForOccupants(DuelArena arena) {
        if (arena == null) {
            return;
        }

        Set<UUID> participantUuids = new HashSet<>();
        for (DuelMatch match : activeMatches.values()) {
            if (match == null || match.getArena() == null || !arena.getId().equalsIgnoreCase(match.getArena().getId())) {
                continue;
            }
            participantUuids.add(match.getPlayerOneUuid());
            participantUuids.add(match.getPlayerTwoUuid());
        }

        for (UUID uuid : participantUuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                applyArenaRules(player, arena);
            }
        }
    }

    private void syncArenaRulesForAllOccupants() {
        Set<String> syncedArenaIds = new HashSet<>();
        for (DuelArena arena : arenas.values()) {
            if (arena == null || !syncedArenaIds.add(arena.getId())) {
                continue;
            }
            syncArenaRulesForOccupants(arena);
        }
    }

    private boolean canEnterDuel(Player player, boolean selfFeedback) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (isInDuel(uuid)) {
            if (selfFeedback) {
                send(player, "&cʏᴏᴜ ᴀʀᴇ ᴀʟʀᴇᴀᴅʏ ɪɴ ᴀ ᴅᴜᴇʟ.");
            }
            return false;
        }
        if (preparingDuelPlayers.contains(uuid)) {
            if (selfFeedback) {
                send(player, "&cʏᴏᴜʀ ᴅᴜᴇʟ ᴀʀᴇɴᴀ ɪѕ ᴘʀᴇᴘᴀʀɪɴɢ.");
            }
            return false;
        }
        if (isInQueue(uuid)) {
            if (selfFeedback) {
                send(player, "&cʏᴏᴜ ᴀʀᴇ ᴀʟʀᴇᴀᴅʏ ɪɴ ᴛʜᴇ ǫᴜᴇᴜᴇ.");
            }
            return false;
        }
        if (requestsByTarget.containsKey(uuid)) {
            if (selfFeedback) {
                send(player, "&cʏᴏᴜ ᴀʟʀᴇᴀᴅʏ ʜᴀᴠᴇ ᴀ ᴘᴇɴᴅɪɴɢ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ.");
            }
            return false;
        }
        if (plugin.getFfaManager() != null && plugin.getFfaManager().isBusy(uuid)) {
            if (selfFeedback) {
                send(player, "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴜѕᴇ ᴅᴜᴇʟѕ ᴡʜɪʟᴇ ɪɴѕɪᴅᴇ ᴛʜᴇ ꜰꜰᴀ ѕʏѕᴛᴇᴍ.");
            }
            return false;
        }
        return true;
    }

    private boolean canUsePrivacyMode(Player challenger, Player target, DuelPrivacyMode privacyMode, boolean selfFeedback) {
        if (privacyMode != DuelPrivacyMode.FRIENDS_ONLY) {
            return true;
        }
        if (challenger == null || target == null || plugin.getTeamManager() == null) {
            if (selfFeedback && challenger != null) {
                send(challenger, "&cꜰʀɪᴇɴᴅѕ-ᴏɴʟʏ ᴅᴜᴇʟѕ ʀᴇǫᴜɪʀᴇ ʙᴏᴛʜ ᴘʟᴀʏᴇʀѕ ᴛᴏ ʙᴇ ɪɴ ᴛʜᴇ ѕᴀᴍᴇ ᴛᴇᴀᴍ.");
            }
            return false;
        }
        boolean teammates = plugin.getTeamManager().areTeammates(challenger.getUniqueId(), target.getUniqueId());
        if (!teammates && selfFeedback) {
            send(challenger, "&cꜰʀɪᴇɴᴅѕ-ᴏɴʟʏ ᴅᴜᴇʟѕ ᴄᴀɴ ᴏɴʟʏ ᴛᴀʀɢᴇᴛ ᴍᴇᴍʙᴇʀѕ ᴏꜰ ʏᴏᴜʀ ᴛᴇᴀᴍ.");
        }
        return teammates;
    }

    private boolean isAcceptingDuelRequests(Player target) {
        if (target == null || plugin.getPlayerDataManager() == null) {
            return true;
        }
        PlayerData data = plugin.getPlayerDataManager().get(target);
        return data == null || data.isDuelRequestsEnabled();
    }

    private void enforceArenaBorder(DuelMatch match) {
        if (match == null || !match.usesGeneratedWorld() || !worldManager.isBorderEnabled()) {
            return;
        }
        enforceArenaBorderFor(match, match.getPlayerOneUuid());
        enforceArenaBorderFor(match, match.getPlayerTwoUuid());
    }

    private void enforceArenaBorderFor(DuelMatch match, UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        if (isInsideGeneratedArenaBorder(match, player.getLocation())) {
            borderEscapeTicks.remove(uuid);
            return;
        }

        int ticks = borderEscapeTicks.merge(uuid, 1, Integer::sum);
        if (ticks >= worldManager.getBorderGraceTicks()) {
            String fallback = worldManager.getBorderFallbackAction();
            if ("DRAW".equals(fallback)) {
                finishMatch(match, null, null, "BOUNDARY_ESCAPE", false, List.of(), true);
                return;
            }
            if ("FORFEIT".equals(fallback)) {
                handleForfeit(player, "BOUNDARY_ESCAPE", true);
                return;
            }
        }

        if ("PUSH_BACK".equals(worldManager.getBorderAction()) && (ticks == 1 || ticks % 10 == 0)) {
            pushPlayerBackToArena(player, match);
        }
    }

    private boolean isInsideGeneratedArenaBorder(DuelMatch match, Location location) {
        if (match == null || location == null || location.getWorld() == null) {
            return false;
        }
        if (!location.getWorld().getName().equals(match.getGeneratedWorldName())) {
            return false;
        }
        WorldBorder border = location.getWorld().getWorldBorder();
        return border == null || border.isInside(location);
    }

    private void pushPlayerBackToArena(Player player, DuelMatch match) {
        Location destination = resolveArenaStayLocation(match, player.getUniqueId());
        if (destination == null || destination.getWorld() == null) {
            return;
        }
        plugin.getSpigotScheduler().teleport(player, destination);
    }

    private boolean isSelectionAvailable(DuelMapSelection selection, boolean queueOnly) {
        DuelMapSelection resolved = selection == null ? DuelMapSelection.randomStatic() : selection;
        return switch (resolved.type()) {
            case RANDOM_STATIC -> !getReadyArenas(queueOnly).isEmpty();
            case STATIC_ARENA -> {
                DuelArena arena = getArena(resolved.value());
                yield arena != null && arena.isEnabled() && arena.isReady() && (!queueOnly || arena.isQueueEnabled());
            }
            case RANDOM_BIOME -> worldManager.isRandomBiomesEnabled() && !worldManager.getSelectableBiomes().isEmpty();
            case BIOME -> worldManager.isRandomBiomesEnabled()
                    && worldManager.resolveBiome(resolved.value())
                    .filter(biome -> worldManager.getSelectableBiomes().contains(biome))
                    .isPresent();
        };
    }

    private List<DuelArena> getReadyArenas(boolean queueOnly) {
        return queueOnly ? getReadyQueueArenas() : getReadyEnabledArenas();
    }

    private ResolvedArena resolveArena(DuelMapSelection selection, boolean queueArena) {
        DuelMapSelection resolved = selection == null ? DuelMapSelection.randomStatic() : selection;
        if (resolved.usesGeneratedWorld()) {
            DuelWorldManager.GeneratedArena generatedArena = worldManager.createGeneratedArena(resolved);
            if (generatedArena == null) {
                return null;
            }
            applyGeneratedTerrainSettings(generatedArena.arena(), generatedArena.terrainMode());
            return new ResolvedArena(
                    generatedArena.arena(),
                    generatedArena.selection(),
                    generatedArena.biomeKey(),
                    generatedArena.worldName(),
                    generatedArena.terrainMode()
            );
        }

        String preferredArenaId = resolved.type() == DuelMapSelection.Type.STATIC_ARENA ? normalizeArenaId(resolved.value()) : null;
        DuelArena arena = findAvailableArena(preferredArenaId, queueArena);
        if (arena == null) {
            return null;
        }
        DuelMapSelection actualSelection = preferredArenaId == null
                ? DuelMapSelection.staticArena(arena.getId())
                : DuelMapSelection.staticArena(preferredArenaId);
        return new ResolvedArena(arena, actualSelection, "", "", null);
    }

    private String getLocalServerId() {
        String configured = config().getString("CROSS_SERVER.LOCAL_SERVER_ID", "");
        if (configured == null || configured.isBlank()) {
            configured = plugin.getConfigManager().getNetwork().getString("NETWORK.LOCAL_SERVER_ID", "local");
        }
        return configured == null || configured.isBlank() ? "local" : configured.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isCrossServerEnabled() {
        return config().getBoolean("CROSS_SERVER.ENABLED", false)
                && plugin.getRedisManager() != null
                && plugin.getRedisManager().isEnabled();
    }

    private void unsubscribeCrossServer() {
        if (plugin.getRedisManager() != null && crossServerSubscribed) {
            plugin.getRedisManager().unsubscribe(crossServerSubscribedChannel.isBlank()
                    ? getCrossServerChannel()
                    : crossServerSubscribedChannel);
        }
        crossServerSubscribed = false;
        crossServerSubscribedChannel = "";
    }

    private void publishCrossServerQueueJoin(Player player, DuelMapSelection selection) {
        if (!isCrossServerEnabled() || player == null) {
            return;
        }

        String uuid = player.getUniqueId().toString();
        long now = System.currentTimeMillis();
        Map<String, String> values = new HashMap<>();
        values.put("uuid", uuid);
        values.put("name", plainPublicName(player));
        values.put("serverId", getLocalServerId());
        values.put("map", (selection == null ? DuelMapSelection.randomStatic() : selection).serialize());
        values.put("queuedAt", Long.toString(now));

        String dataKey = getCrossQueueDataKey(player.getUniqueId());
        plugin.getRedisManager().hset(dataKey, values);
        plugin.getRedisManager().expire(dataKey, getCrossServerStaleQueueSeconds());
        plugin.getRedisManager().zadd(getCrossQueueKey(), now, uuid);
    }

    private void removeCrossServerQueueEntry(UUID uuid) {
        if (!isCrossServerEnabled() || uuid == null) {
            return;
        }
        String key = uuid.toString();
        plugin.getRedisManager().zrem(getCrossQueueKey(), key);
        plugin.getRedisManager().del(getCrossQueueDataKey(uuid));
    }

    private void attemptCrossServerMatchmaking() {
        if (!isCrossServerEnabled()) {
            return;
        }
        if (!plugin.getRedisManager().setIfAbsent(getCrossLockKey(), getLocalServerId(), 3L)) {
            return;
        }

        List<CrossQueueEntry> entries = loadCrossQueueEntries();
        if (entries.size() < 2) {
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            CrossQueueEntry first = entries.get(i);
            for (int j = i + 1; j < entries.size(); j++) {
                CrossQueueEntry second = entries.get(j);
                if (first.uuid().equals(second.uuid())) {
                    continue;
                }
                if (first.serverId().equals(getLocalServerId()) && second.serverId().equals(getLocalServerId())) {
                    continue;
                }

                DuelMapSelection selection = first.selection() == null ? DuelMapSelection.randomStatic() : first.selection();
                if (!isSelectionAvailable(selection, true)) {
                    continue;
                }

                String matchId = UUID.randomUUID().toString();
                PendingCrossServerMatch pendingMatch = new PendingCrossServerMatch(
                        matchId,
                        first.uuid(),
                        first.name(),
                        second.uuid(),
                        second.name(),
                        selection,
                        System.currentTimeMillis() + getCrossTransferTimeoutMillis()
                );
                pendingCrossServerMatches.put(matchId, pendingMatch);

                removeCrossServerQueueEntry(first.uuid());
                removeCrossServerQueueEntry(second.uuid());
                removeLocalQueueEntry(first.uuid());
                removeLocalQueueEntry(second.uuid());
                publishTransferRequest(pendingMatch, first);
                publishTransferRequest(pendingMatch, second);
                return;
            }
        }
    }

    private List<CrossQueueEntry> loadCrossQueueEntries() {
        List<String> members = plugin.getRedisManager().zrange(getCrossQueueKey(), 0, 24);
        List<CrossQueueEntry> entries = new ArrayList<>();
        long staleBefore = System.currentTimeMillis() - (getCrossServerStaleQueueSeconds() * 1000L);
        for (String member : members) {
            UUID uuid;
            try {
                uuid = UUID.fromString(member);
            } catch (IllegalArgumentException exception) {
                plugin.getRedisManager().zrem(getCrossQueueKey(), member);
                continue;
            }

            Map<String, String> values = plugin.getRedisManager().hgetAll(getCrossQueueDataKey(uuid));
            CrossQueueEntry entry = parseCrossQueueEntry(values);
            if (entry == null || entry.queuedAt() < staleBefore) {
                removeCrossServerQueueEntry(uuid);
                continue;
            }
            entries.add(entry);
        }
        return entries;
    }

    private CrossQueueEntry parseCrossQueueEntry(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(values.getOrDefault("uuid", ""));
            String name = values.getOrDefault("name", "Player");
            String serverId = values.getOrDefault("serverId", "").trim().toLowerCase(Locale.ROOT);
            long queuedAt = Long.parseLong(values.getOrDefault("queuedAt", "0"));
            if (serverId.isBlank()) {
                return null;
            }
            return new CrossQueueEntry(uuid, name, serverId, parseMapSelection(values.get("map")), queuedAt);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void publishTransferRequest(PendingCrossServerMatch match, CrossQueueEntry entry) {
        if (match == null || entry == null) {
            return;
        }

        if (entry.serverId().equals(getLocalServerId())) {
            Player player = Bukkit.getPlayer(entry.uuid());
            if (player != null) {
                send(player, "&aᴄʀᴏѕѕ-ѕᴇʀᴠᴇʀ ᴅᴜᴇʟ ꜰᴏᴜɴᴅ. ᴘʀᴇᴘᴀʀɪɴɢ ᴍᴀᴛᴄʜ...");
            }
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("type", "TRANSFER_REQUEST");
        payload.put("messageId", UUID.randomUUID().toString());
        payload.put("sourceServerId", getLocalServerId());
        payload.put("targetServerId", entry.serverId());
        payload.put("hostServerId", getLocalServerId());
        payload.put("hostProxyServerName", getCrossProxyServerName());
        payload.put("matchId", match.matchId());
        payload.put("playerUuid", entry.uuid().toString());
        payload.put("playerName", entry.name());
        payload.put("playerOneUuid", match.firstUuid().toString());
        payload.put("playerOneName", match.firstName());
        payload.put("playerTwoUuid", match.secondUuid().toString());
        payload.put("playerTwoName", match.secondName());
        payload.put("map", match.selection().serialize());
        payload.put("createdAt", Long.toString(System.currentTimeMillis()));
        plugin.getRedisManager().publish(getCrossServerChannel(), serializeProperties(payload));
    }

    private void handleCrossServerPayload(String rawPayload) {
        Map<String, String> payload = deserializeProperties(rawPayload);
        String messageId = payload.getOrDefault("messageId", "");
        if (messageId.isBlank() || !seenCrossServerMessages.add(messageId)) {
            return;
        }
        if (seenCrossServerMessages.size() > 1000) {
            seenCrossServerMessages.clear();
        }

        String type = payload.getOrDefault("type", "").trim().toUpperCase(Locale.ROOT);
        if (!"TRANSFER_REQUEST".equals(type)) {
            return;
        }
        if (!getLocalServerId().equals(payload.getOrDefault("targetServerId", "").trim().toLowerCase(Locale.ROOT))) {
            return;
        }

        UUID playerUuid;
        try {
            playerUuid = UUID.fromString(payload.getOrDefault("playerUuid", ""));
        } catch (IllegalArgumentException exception) {
            return;
        }

        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        removeLocalQueueEntry(playerUuid);
        removeCrossServerQueueEntry(playerUuid);
        send(player, "&aᴄʀᴏѕѕ-ѕᴇʀᴠᴇʀ ᴅᴜᴇʟ ꜰᴏᴜɴᴅ. ᴛʀᴀɴѕꜰᴇʀʀɪɴɢ ᴛᴏ ᴍᴀᴛᴄʜ ѕᴇʀᴠᴇʀ...");
        transferPlayerToProxyServer(player, payload.getOrDefault("hostProxyServerName", getCrossProxyServerName()));
    }

    private void attemptPendingCrossServerMatches() {
        if (pendingCrossServerMatches.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        List<String> completed = new ArrayList<>();
        for (PendingCrossServerMatch match : new ArrayList<>(pendingCrossServerMatches.values())) {
            if (now >= match.expiresAt()) {
                completed.add(match.matchId());
                continue;
            }

            Player first = Bukkit.getPlayer(match.firstUuid());
            Player second = Bukkit.getPlayer(match.secondUuid());
            if (first == null || second == null || !first.isOnline() || !second.isOnline()) {
                continue;
            }

            if (!canEnterDuel(first, true) || !canEnterDuel(second, false)) {
                completed.add(match.matchId());
                continue;
            }

            ResolvedArena resolvedArena = resolveArena(match.selection(), true);
            if (resolvedArena == null) {
                if (worldManager.canPrepareGeneratedArenas()) {
                    continue;
                }
                completed.add(match.matchId());
                send(first, "&cᴄʀᴏѕѕ-ѕᴇʀᴠᴇʀ ᴅᴜᴇʟ ᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴛᴀʀᴛ ʙᴇᴄᴀᴜѕᴇ ɴᴏ ᴀʀᴇɴᴀ ɪѕ ᴀᴠᴀɪʟᴀʙʟᴇ.");
                send(second, "&cᴄʀᴏѕѕ-ѕᴇʀᴠᴇʀ ᴅᴜᴇʟ ᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴛᴀʀᴛ ʙᴇᴄᴀᴜѕᴇ ɴᴏ ᴀʀᴇɴᴀ ɪѕ ᴀᴠᴀɪʟᴀʙʟᴇ.");
                continue;
            }

            removeLocalQueueEntry(first.getUniqueId());
            removeLocalQueueEntry(second.getUniqueId());
            startMatch(first, second, resolvedArena, DuelMatch.MatchType.QUEUE, DuelPrivacyMode.INVITE_ONLY, getLocalServerId());
            completed.add(match.matchId());
        }
        completed.forEach(pendingCrossServerMatches::remove);
    }

    private void removeLocalQueueEntry(UUID uuid) {
        if (uuid == null) {
            return;
        }
        queue.remove(uuid);
        queueSelections.remove(uuid);
    }

    private void transferPlayerToProxyServer(Player player, String serverName) {
        if (player == null || serverName == null || serverName.isBlank()) {
            return;
        }

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteStream);
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(plugin, "BungeeCord", byteStream.toByteArray());
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to send duel proxy transfer request for " + player.getName(), exception);
        }
    }

    private String serializeProperties(Map<String, String> values) {
        Properties properties = new Properties();
        if (values != null) {
            values.forEach((key, value) -> properties.setProperty(key, value == null ? "" : value));
        }
        StringWriter writer = new StringWriter();
        try {
            properties.store(writer, "ultimatedonutsmp duel cross-server payload");
        } catch (IOException ignored) {
        }
        return writer.toString();
    }

    private Map<String, String> deserializeProperties(String raw) {
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(raw));
        } catch (IOException exception) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        for (String name : properties.stringPropertyNames()) {
            result.put(name, properties.getProperty(name, ""));
        }
        return result;
    }

    private String getCrossServerChannel() {
        return config().getString("CROSS_SERVER.REDIS_CHANNEL", "ultimatedonutsmp:duels");
    }

    private String getCrossKeyPrefix() {
        String prefix = config().getString("CROSS_SERVER.KEY_PREFIX", "uds:duels:");
        return prefix == null || prefix.isBlank() ? "uds:duels:" : prefix.trim();
    }

    private String getCrossQueueKey() {
        return getCrossKeyPrefix() + "queue";
    }

    private String getCrossLockKey() {
        return getCrossKeyPrefix() + "match-lock";
    }

    private String getCrossQueueDataKey(UUID uuid) {
        return getCrossKeyPrefix() + "queue:" + uuid;
    }

    private long getCrossServerStaleQueueSeconds() {
        return Math.max(5L, config().getLong("CROSS_SERVER.STALE_QUEUE_TIMEOUT_SECONDS", 45L));
    }

    private long getCrossTransferTimeoutMillis() {
        return Math.max(5L, config().getLong("CROSS_SERVER.TRANSFER_TIMEOUT_SECONDS", 20L)) * 1000L;
    }

    private String getCrossProxyServerName() {
        String configured = config().getString("CROSS_SERVER.PROXY_SERVER_NAME", "");
        return configured == null || configured.isBlank() ? getLocalServerId() : configured.trim();
    }

    private List<String> commandPatterns(String path, List<String> defaults) {
        List<String> configured = config().getStringList(path);
        List<String> source = configured.isEmpty() ? defaults : configured;
        List<String> patterns = new ArrayList<>();
        for (String value : source) {
            String normalized = normalizeCommandPattern(value);
            if (!normalized.isBlank()) {
                patterns.add(normalized);
            }
        }
        return patterns;
    }

    private String normalizeCommandPattern(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private boolean matchesCommandPattern(String raw, String pattern) {
        if (raw == null || pattern == null || pattern.isBlank()) {
            return false;
        }
        return raw.equals(pattern) || raw.startsWith(pattern + " ");
    }

    private DuelArena findAvailableArena(String preferredArenaId, boolean queueArena) {
        if (preferredArenaId != null) {
            DuelArena arena = getArena(preferredArenaId);
            if (arena == null || !arena.isEnabled() || !arena.isReady()) {
                return null;
            }
            if (queueArena && !arena.isQueueEnabled()) {
                return null;
            }
            return reservedArenaIds.contains(arena.getId()) ? null : arena;
        }

        List<DuelArena> arenasToSearch = queueArena ? getReadyQueueArenas() : getReadyEnabledArenas();
        for (DuelArena arena : arenasToSearch) {
            if (!reservedArenaIds.contains(arena.getId())) {
                return arena;
            }
        }
        return null;
    }

    private void expireRequests() {
        long now = System.currentTimeMillis();
        List<UUID> expiredTargets = new ArrayList<>();
        for (Map.Entry<UUID, DuelRequest> entry : requestsByTarget.entrySet()) {
            if (entry.getValue().isExpired(now)) {
                expiredTargets.add(entry.getKey());
            }
        }

        for (UUID targetUuid : expiredTargets) {
            DuelRequest request = requestsByTarget.remove(targetUuid);
            if (request == null) {
                continue;
            }

            Player challenger = Bukkit.getPlayer(request.challengerUuid());
            Player target = Bukkit.getPlayer(targetUuid);
            if (challenger != null) {
                send(challenger, "&cʏᴏᴜʀ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ᴛᴏ &f" + request.targetName() + " &cᴇxᴘɪʀᴇᴅ.");
            }
            if (target != null) {
                send(target, "&cʏᴏᴜʀ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ꜰʀᴏᴍ &f" + request.challengerName() + " &cᴇxᴘɪʀᴇᴅ.");
            }
        }
    }

    private void cleanupQueue() {
        List<UUID> toRemove = new ArrayList<>();
        for (UUID uuid : queue) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || isInDuel(uuid)) {
                toRemove.add(uuid);
            }
        }
        queue.removeAll(toRemove);
        toRemove.forEach(queueSelections::remove);
        toRemove.forEach(this::removeCrossServerQueueEntry);
    }

    private boolean removeOutgoingRequest(UUID challengerUuid) {
        if (challengerUuid == null) {
            return false;
        }

        UUID matchKey = null;
        for (Map.Entry<UUID, DuelRequest> entry : requestsByTarget.entrySet()) {
            if (entry.getValue().challengerUuid().equals(challengerUuid)) {
                matchKey = entry.getKey();
                break;
            }
        }

        if (matchKey != null) {
            requestsByTarget.remove(matchKey);
            return true;
        }
        return false;
    }

    private void removeRequestsFor(UUID playerUuid, boolean notifyPlayers) {
        if (playerUuid == null) {
            return;
        }

        DuelRequest removedIncoming = requestsByTarget.remove(playerUuid);
        if (notifyPlayers && removedIncoming != null) {
            Player challenger = Bukkit.getPlayer(removedIncoming.challengerUuid());
            if (challenger != null) {
                send(challenger, "&cʏᴏᴜʀ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ᴡᴀѕ ᴄʟᴇᴀʀᴇᴅ.");
            }
        }

        UUID outgoingTarget = null;
        DuelRequest outgoing = null;
        for (Map.Entry<UUID, DuelRequest> entry : requestsByTarget.entrySet()) {
            if (entry.getValue().challengerUuid().equals(playerUuid)) {
                outgoingTarget = entry.getKey();
                outgoing = entry.getValue();
                break;
            }
        }

        if (outgoingTarget != null) {
            requestsByTarget.remove(outgoingTarget);
            if (notifyPlayers) {
                Player target = Bukkit.getPlayer(outgoingTarget);
                if (target != null && outgoing != null) {
                    send(target, "&cᴛʜᴀᴛ ᴅᴜᴇʟ ʀᴇǫᴜᴇѕᴛ ᴡᴀѕ ᴄᴀɴᴄᴇʟʟᴇᴅ.");
                }
            }
        }
    }

    private DuelMatch getActiveMatch(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        Long matchId = activeMatchIds.get(uuid);
        return matchId == null ? null : activeMatches.get(matchId);
    }

    private Location resolveReturnLocation(DuelMatch match, UUID uuid) {
        Location location = match.getReturnLocation(uuid);
        if (location != null && location.getWorld() != null) {
            return location;
        }

        Location arenaReturn = match.getArena().getReturnLocation();
        if (arenaReturn != null && arenaReturn.getWorld() != null) {
            return arenaReturn;
        }

        return plugin.getSpawnManager().hasSpawn() ? plugin.getSpawnManager().getSpawnLocation() : null;
    }

    private Location resolveArenaJoinFallbackLocation(DuelArena arena, Location currentLocation) {
        if (arena != null) {
            Location arenaReturn = arena.getReturnLocation();
            if (arenaReturn != null && arenaReturn.getWorld() != null) {
                return arenaReturn;
            }
        }
        if (plugin.getSpawnManager().hasSpawn()) {
            Location spawn = plugin.getSpawnManager().getSpawnLocation();
            if (spawn != null && spawn.getWorld() != null) {
                return spawn;
            }
        }
        if (currentLocation != null && currentLocation.getWorld() != null) {
            Location worldSpawn = currentLocation.getWorld().getSpawnLocation();
            if (worldSpawn != null && worldSpawn.getWorld() != null) {
                return worldSpawn;
            }
        }
        if (!Bukkit.getWorlds().isEmpty()) {
            Location worldSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
            if (worldSpawn != null && worldSpawn.getWorld() != null) {
                return worldSpawn;
            }
        }
        return null;
    }

    private Location resolveArenaStayLocation(DuelMatch match, UUID uuid) {
        if (match == null || uuid == null) {
            return null;
        }

        if (uuid.equals(match.getPlayerOneUuid())) {
            return match.getArena().getSpawn1();
        }
        if (uuid.equals(match.getPlayerTwoUuid())) {
            return match.getArena().getSpawn2();
        }
        return match.getArena().getSpawn1();
    }

    private String resolveParticipantName(DuelMatch match, UUID uuid) {
        if (match == null || uuid == null) {
            return "unknown";
        }
        if (uuid.equals(match.getPlayerOneUuid())) {
            return match.getPlayerOneName();
        }
        if (uuid.equals(match.getPlayerTwoUuid())) {
            return match.getPlayerTwoName();
        }
        return "unknown";
    }

    private boolean canFullyFit(PlayerInventory inventory, ItemStack item) {
        if (inventory == null || item == null || item.getType().isAir() || item.getAmount() <= 0) {
            return false;
        }

        int remaining = item.getAmount();
        for (ItemStack existing : inventory.getStorageContents()) {
            if (existing == null || existing.getType().isAir()) {
                remaining -= item.getMaxStackSize();
            } else if (existing.isSimilar(item)) {
                remaining -= Math.max(0, existing.getMaxStackSize() - existing.getAmount());
            }

            if (remaining <= 0) {
                return true;
            }
        }

        return remaining <= 0;
    }

    private List<ClaimItemRow> loadClaimItemRows(UUID playerUuid, long matchId) {
        List<ClaimItemRow> rows = new ArrayList<>();
        if (playerUuid == null || matchId <= 0L || connection() == null) {
            return rows;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "select id, defeated_name, item_data, created_at from duel_claims where player_uuid = ? and match_id = ? order by id asc")) {
            ps.setString(1, playerUuid.toString());
            ps.setLong(2, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemStack item = deserializeItem(rs.getString("item_data"));
                    if (item == null) {
                        continue;
                    }
                    rows.add(new ClaimItemRow(
                            rs.getLong("id"),
                            rs.getString("defeated_name"),
                            item,
                            rs.getLong("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load duel claim rows for match " + matchId, e);
        }

        return rows;
    }

    private List<ItemStack> extractInventory(Player player) {
        List<ItemStack> loot = new ArrayList<>();
        if (player == null) {
            return loot;
        }

        PlayerInventory inventory = player.getInventory();
        collectItems(loot, inventory.getStorageContents());
        collectItems(loot, inventory.getArmorContents());
        collectItems(loot, new ItemStack[]{inventory.getItemInOffHand()});

        inventory.clear();
        inventory.setArmorContents(null);
        inventory.setItemInOffHand(null);
        player.updateInventory();
        return loot;
    }

    private void rememberGeneratedInventorySnapshot(DuelMatch match, Player first, Player second) {
        if (match == null || !match.usesGeneratedWorld()) {
            return;
        }

        Map<UUID, GeneratedInventorySnapshot> snapshots = new HashMap<>();
        if (first != null) {
            snapshots.put(first.getUniqueId(), snapshotInventory(first));
        }
        if (second != null) {
            snapshots.put(second.getUniqueId(), snapshotInventory(second));
        }
        generatedMatchInventorySnapshots.put(match.getId(), snapshots);
    }

    private GeneratedInventorySnapshot snapshotInventory(Player player) {
        if (player == null) {
            return GeneratedInventorySnapshot.empty();
        }

        PlayerInventory inventory = player.getInventory();
        return new GeneratedInventorySnapshot(
                cloneContents(inventory.getStorageContents()),
                cloneContents(inventory.getArmorContents()),
                cloneItem(inventory.getItemInOffHand()),
                cloneItem(player.getItemOnCursor())
        );
    }

    private void sanitizeGeneratedInventory(DuelMatch match, UUID uuid) {
        if (match == null || !match.usesGeneratedWorld() || uuid == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        GeneratedInventorySnapshot snapshot = getGeneratedInventorySnapshot(match, uuid);
        if (snapshot == null) {
            return;
        }

        List<ItemStack> remaining = snapshot.copyItems();
        PlayerInventory inventory = player.getInventory();

        ItemStack[] storage = inventory.getStorageContents();
        boolean[] preservedStorage = preserveSnapshotSlots(storage, snapshot.storage(), remaining);
        trimContentsToSnapshot(storage, remaining, preservedStorage);
        inventory.setStorageContents(storage);

        ItemStack[] armor = inventory.getArmorContents();
        boolean[] preservedArmor = preserveSnapshotSlots(armor, snapshot.armor(), remaining);
        trimContentsToSnapshot(armor, remaining, preservedArmor);
        inventory.setArmorContents(armor);

        ItemStack[] offHand = new ItemStack[]{inventory.getItemInOffHand()};
        boolean[] preservedOffHand = preserveSnapshotSlots(offHand, new ItemStack[]{snapshot.offHand()}, remaining);
        trimContentsToSnapshot(offHand, remaining, preservedOffHand);
        inventory.setItemInOffHand(offHand[0]);

        ItemStack[] cursor = new ItemStack[]{player.getItemOnCursor()};
        boolean[] preservedCursor = preserveSnapshotSlots(cursor, new ItemStack[]{snapshot.cursor()}, remaining);
        trimContentsToSnapshot(cursor, remaining, preservedCursor);
        player.setItemOnCursor(cursor[0]);

        player.updateInventory();
    }

    private void restoreGeneratedInventory(DuelMatch match, UUID uuid) {
        restoreGeneratedInventory(match, uuid, List.of());
    }

    private void restoreGeneratedInventory(DuelMatch match, UUID uuid, List<ItemStack> removedItems) {
        if (match == null || !match.usesGeneratedWorld() || uuid == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        GeneratedInventorySnapshot snapshot = getGeneratedInventorySnapshot(match, uuid);
        if (snapshot == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        List<ItemStack> remainingRemovedItems = cloneItemList(removedItems);
        inventory.setStorageContents(fitContentsWithoutRemovedItems(
                snapshot.storage(),
                inventory.getStorageContents().length,
                remainingRemovedItems
        ));
        inventory.setArmorContents(fitContentsWithoutRemovedItems(
                snapshot.armor(),
                inventory.getArmorContents().length,
                remainingRemovedItems
        ));
        inventory.setItemInOffHand(cloneItemWithoutRemovedItems(snapshot.offHand(), remainingRemovedItems));
        player.setItemOnCursor(cloneItemWithoutRemovedItems(snapshot.cursor(), remainingRemovedItems));
    }

    private GeneratedInventorySnapshot computeRestoredInventory(DuelMatch match, UUID uuid, List<ItemStack> removedItems) {
        if (match == null || !match.usesGeneratedWorld() || uuid == null) {
            return null;
        }

        GeneratedInventorySnapshot snapshot = getGeneratedInventorySnapshot(match, uuid);
        if (snapshot == null) {
            return null;
        }

        List<ItemStack> remainingRemovedItems = cloneItemList(removedItems);
        ItemStack[] storage = fitContentsWithoutRemovedItems(
                snapshot.storage(),
                snapshot.storage() != null ? snapshot.storage().length : 36,
                remainingRemovedItems
        );
        ItemStack[] armor = fitContentsWithoutRemovedItems(
                snapshot.armor(),
                snapshot.armor() != null ? snapshot.armor().length : 4,
                remainingRemovedItems
        );
        ItemStack offHand = cloneItemWithoutRemovedItems(snapshot.offHand(), remainingRemovedItems);
        ItemStack cursor = cloneItemWithoutRemovedItems(snapshot.cursor(), remainingRemovedItems);

        return new GeneratedInventorySnapshot(storage, armor, offHand, cursor);
    }

    private void restoreSnapshotDirectly(Player player, GeneratedInventorySnapshot snapshot) {
        if (player == null || snapshot == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        inventory.setStorageContents(snapshot.storage());
        inventory.setArmorContents(snapshot.armor());
        inventory.setItemInOffHand(snapshot.offHand());
        player.setItemOnCursor(snapshot.cursor());
        player.updateInventory();
    }

    private ItemStack[] fitContentsWithoutRemovedItems(ItemStack[] source, int targetLength, List<ItemStack> remainingRemovedItems) {
        int length = Math.max(0, targetLength);
        ItemStack[] fitted = new ItemStack[length];
        if (source == null) {
            return fitted;
        }
        for (int index = 0; index < Math.min(source.length, fitted.length); index++) {
            fitted[index] = cloneItemWithoutRemovedItems(source[index], remainingRemovedItems);
        }
        return fitted;
    }

    private ItemStack cloneItemWithoutRemovedItems(ItemStack item, List<ItemStack> remainingRemovedItems) {
        ItemStack clone = cloneItem(item);
        if (clone == null) {
            return null;
        }

        int removedAmount = consumeAllowedAmount(remainingRemovedItems, clone, clone.getAmount());
        int restoredAmount = clone.getAmount() - removedAmount;
        if (restoredAmount <= 0) {
            return null;
        }

        clone.setAmount(restoredAmount);
        return clone;
    }

    private List<ItemStack> cloneItemList(List<ItemStack> source) {
        List<ItemStack> clones = new ArrayList<>();
        if (source == null) {
            return clones;
        }
        for (ItemStack item : source) {
            ItemStack clone = cloneItem(item);
            if (clone != null) {
                clones.add(clone);
            }
        }
        return clones;
    }

    private GeneratedInventorySnapshot getGeneratedInventorySnapshot(DuelMatch match, UUID uuid) {
        Map<UUID, GeneratedInventorySnapshot> snapshots = generatedMatchInventorySnapshots.get(match.getId());
        if (snapshots == null) {
            return null;
        }
        return snapshots.get(uuid);
    }

    private boolean[] preserveSnapshotSlots(ItemStack[] contents, ItemStack[] snapshotContents, List<ItemStack> remaining) {
        if (contents == null) {
            return new boolean[0];
        }

        boolean[] preserved = new boolean[contents.length];
        for (int index = 0; index < contents.length; index++) {
            ItemStack current = contents[index];
            ItemStack original = snapshotContents != null && index < snapshotContents.length ? snapshotContents[index] : null;
            if (current == null || current.getType().isAir() || current.getAmount() <= 0
                    || original == null || original.getType().isAir() || original.getAmount() <= 0
                    || !isGeneratedSnapshotCompatible(original, current)) {
                continue;
            }

            int allowedAmount = Math.min(current.getAmount(), original.getAmount());
            int consumed = consumeAllowedAmount(remaining, current, allowedAmount);
            if (consumed <= 0) {
                contents[index] = null;
                continue;
            }

            ItemStack clone = current.clone();
            clone.setAmount(consumed);
            contents[index] = clone;
            preserved[index] = true;
        }
        return preserved;
    }

    private void trimContentsToSnapshot(ItemStack[] contents, List<ItemStack> remaining, boolean[] preserved) {
        if (contents == null) {
            return;
        }

        for (int index = 0; index < contents.length; index++) {
            if (preserved != null && index < preserved.length && preserved[index]) {
                continue;
            }

            ItemStack item = contents[index];
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }

            int allowedAmount = consumeAllowedAmount(remaining, item, item.getAmount());
            if (allowedAmount <= 0) {
                contents[index] = null;
                continue;
            }
            if (allowedAmount < item.getAmount()) {
                ItemStack clone = item.clone();
                clone.setAmount(allowedAmount);
                contents[index] = clone;
            }
        }
    }

    private int consumeAllowedAmount(List<ItemStack> remaining, ItemStack item, int requestedAmount) {
        if (remaining == null || remaining.isEmpty() || item == null || item.getType().isAir() || requestedAmount <= 0) {
            return 0;
        }

        int allowedAmount = 0;
        for (int index = 0; index < remaining.size() && allowedAmount < requestedAmount; ) {
            ItemStack allowed = remaining.get(index);
            if (allowed == null || allowed.getType().isAir() || allowed.getAmount() <= 0) {
                remaining.remove(index);
                continue;
            }
            if (!isGeneratedSnapshotCompatible(allowed, item)) {
                index++;
                continue;
            }

            int taken = Math.min(requestedAmount - allowedAmount, allowed.getAmount());
            allowedAmount += taken;
            int leftover = allowed.getAmount() - taken;
            if (leftover <= 0) {
                remaining.remove(index);
            } else {
                allowed.setAmount(leftover);
                index++;
            }
        }
        return allowedAmount;
    }

    private boolean isGeneratedSnapshotCompatible(ItemStack allowed, ItemStack item) {
        if (allowed == null || item == null || allowed.getType().isAir() || item.getType().isAir()) {
            return false;
        }
        if (allowed.getType() != item.getType()) {
            return false;
        }
        if (allowed.isSimilar(item)) {
            return true;
        }
        return isSimilarIgnoringDamage(allowed, item);
    }

    private boolean isSimilarIgnoringDamage(ItemStack first, ItemStack second) {
        ItemStack firstClone = first.clone();
        ItemStack secondClone = second.clone();
        clearDamage(firstClone);
        clearDamage(secondClone);
        return firstClone.isSimilar(secondClone);
    }

    private void clearDamage(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(0);
            item.setItemMeta(meta);
        }
    }

    private ItemStack[] cloneContents(ItemStack[] contents) {
        if (contents == null) {
            return new ItemStack[0];
        }
        ItemStack[] clones = new ItemStack[contents.length];
        for (int index = 0; index < contents.length; index++) {
            clones[index] = cloneItem(contents[index]);
        }
        return clones;
    }

    private ItemStack cloneItem(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
            return null;
        }
        return item.clone();
    }

    private List<ItemStack> copyLoot(List<ItemStack> drops) {
        List<ItemStack> loot = new ArrayList<>();
        if (drops == null) {
            return loot;
        }
        for (ItemStack item : drops) {
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }
            loot.add(item.clone());
        }
        return loot;
    }

    private void collectItems(List<ItemStack> destination, ItemStack[] items) {
        if (items == null) {
            return;
        }
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }
            destination.add(item.clone());
        }
    }

    private boolean validatePlayerInventoryForDuel(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        PlayerInventory inventory = player.getInventory();
        if (!validateDuelItems(player, inventory.getStorageContents())) {
            return false;
        }
        if (!validateDuelItems(player, inventory.getArmorContents())) {
            return false;
        }
        if (!validateDuelItems(player, new ItemStack[]{inventory.getItemInOffHand(), player.getItemOnCursor()})) {
            return false;
        }
        return true;
    }

    private boolean validateDuelItems(Player player, ItemStack[] items) {
        if (items == null) {
            return true;
        }
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }
            if (!plugin.getCrashProtectionManager()
                    .validateOrNotify(player, item, CrashProtectionManager.Context.DUELS)
                    .allowed()) {
                return false;
            }
        }
        return true;
    }

    public boolean shouldHandleAsCustomLethalPvP(Player attacker, Player victim, double finalDamage) {
        if (attacker == null || victim == null || finalDamage <= 0D) {
            return false;
        }
        if (!areOpponents(attacker.getUniqueId(), victim.getUniqueId())
                || !isMatchActive(attacker.getUniqueId())) {
            return false;
        }
        if (hasUsableTotem(victim)) {
            return false;
        }

        double effectiveHealth = victim.getHealth() + Math.max(0D, victim.getAbsorptionAmount());
        return finalDamage >= effectiveHealth;
    }

    private boolean hasUsableTotem(Player player) {
        if (player == null) {
            return false;
        }

        return player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
                || player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING;
    }

    private ArenaSnapshot captureArenaSnapshot(DuelArena arena) {
        if (arena == null || !arena.hasRollbackRegion()) {
            return null;
        }

        Location pos1 = arena.getRegionPos1();
        Location pos2 = arena.getRegionPos2();
        if (pos1 == null || pos2 == null || pos1.getWorld() == null || pos2.getWorld() == null) {
            return null;
        }

        World world = pos1.getWorld();
        if (!world.getName().equalsIgnoreCase(pos2.getWorld().getName())) {
            return null;
        }

        int horizontalPadding = getRollbackHorizontalPadding();
        int verticalPadding = getRollbackVerticalPadding();
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX()) - horizontalPadding;
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX()) + horizontalPadding;
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY()) - verticalPadding;
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY()) + verticalPadding;
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ()) - horizontalPadding;
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ()) + horizontalPadding;

        List<BlockSnapshot> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blocks.add(new BlockSnapshot(x, y, z, block.getBlockData().getAsString()));
                }
            }
        }

        return new ArenaSnapshot(world.getName(), minX, maxX, minY, maxY, minZ, maxZ, blocks);
    }

    private void rollbackArena(long matchId) {
        ArenaSnapshot snapshot = arenaSnapshots.remove(matchId);
        if (snapshot == null) {
            return;
        }

        World world = Bukkit.getWorld(snapshot.worldName());
        if (world == null) {
            return;
        }

        for (BlockSnapshot blockSnapshot : snapshot.blocks()) {
            Block block = world.getBlockAt(blockSnapshot.x(), blockSnapshot.y(), blockSnapshot.z());
            try {
                BlockData data = Bukkit.createBlockData(blockSnapshot.blockDataString());
                block.setBlockData(data, false);
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().log(Level.WARNING,
                        "failed to restore duel arena block at "
                                + blockSnapshot.x() + "," + blockSnapshot.y() + "," + blockSnapshot.z(),
                        exception);
            }
        }

        cleanupTransientEntities(snapshot, world);
    }

    private void rollbackGeneratedArena(DuelMatch match) {
        if (match == null || !match.usesGeneratedWorld()) {
            return;
        }

        Map<BlockKey, String> snapshots = generatedBlockSnapshots.remove(match.getId());
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }

        World world = Bukkit.getWorld(match.getGeneratedWorldName());
        if (world == null) {
            return;
        }

        for (Map.Entry<BlockKey, String> entry : snapshots.entrySet()) {
            BlockKey key = entry.getKey();
            if (key == null || !world.getName().equalsIgnoreCase(key.worldName())) {
                continue;
            }
            Block block = world.getBlockAt(key.x(), key.y(), key.z());
            try {
                BlockData data = Bukkit.createBlockData(entry.getValue());
                block.setBlockData(data, false);
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().log(Level.WARNING,
                        "failed to restore generated duel block at "
                                + key.x() + "," + key.y() + "," + key.z(),
                        exception);
            }
        }
    }

    private void cleanupTransientEntities(ArenaSnapshot snapshot, World world) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player) {
                continue;
            }
            Location location = entity.getLocation();
            if (!snapshot.contains(location)) {
                continue;
            }

            if (isTransientDuelEntity(entity)) {
                entity.remove();
            }
        }
    }

    private void cleanupGeneratedTransientEntities(DuelMatch match) {
        if (match == null || !match.usesGeneratedWorld()) {
            return;
        }

        World world = Bukkit.getWorld(match.getGeneratedWorldName());
        if (world == null) {
            return;
        }

        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof Player) && isTransientDuelEntity(entity)) {
                entity.remove();
            }
        }
    }

    private boolean isTransientDuelEntity(Entity entity) {
        if (entity == null) {
            return false;
        }

        String typeName = entity.getType().name();
        return typeName.equals("ITEM")
                || typeName.equals("EXPERIENCE_ORB")
                || typeName.equals("ARROW")
                || typeName.equals("SPECTRAL_ARROW")
                || typeName.equals("TRIDENT")
                || typeName.equals("EGG")
                || typeName.equals("ENDER_PEARL")
                || typeName.equals("SNOWBALL")
                || typeName.equals("POTION")
                || typeName.equals("SPLASH_POTION")
                || typeName.equals("THROWN_POTION")
                || typeName.equals("LINGERING_POTION")
                || typeName.equals("AREA_EFFECT_CLOUD")
                || typeName.equals("FALLING_BLOCK")
                || typeName.equals("PRIMED_TNT")
                || typeName.equals("TNT");
    }

    private void prepareTransition(UUID uuid) {
        if (uuid == null) {
            return;
        }

        transitioningPlayers.add(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline() && !player.isDead()) {
            applyTransitionState(player);
        }
    }

    private void applyTransitionState(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        transitioningPlayers.add(player.getUniqueId());
        transitionStates.putIfAbsent(player.getUniqueId(), new TransitionPlayerState(
                player.getGameMode(),
                player.getAllowFlight(),
                player.isFlying(),
                player.isInvulnerable(),
                player.isCollidable()
        ));
        applyTemporaryVanish(player);
        if (player.getGameMode() != GameMode.ADVENTURE) {
            player.setGameMode(GameMode.ADVENTURE);
        }
        healPlayer(player);
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        showStoredTransitionTitle(player);
    }

    private void restoreTransitionState(Player player) {
        if (player == null) {
            return;
        }

        TransitionPlayerState state = transitionStates.remove(player.getUniqueId());
        if (state == null) {
            state = new TransitionPlayerState(GameMode.SURVIVAL, false, false, false, true);
        }
        if (player.getGameMode() != state.gameMode()) {
            player.setGameMode(state.gameMode());
        }
        player.setAllowFlight(state.allowFlight());
        player.setFlying(state.allowFlight() && state.flying());
        player.setInvulnerable(state.invulnerable());
        player.setCollidable(state.collidable());
        player.resetPlayerTime();
        player.resetPlayerWeather();
        transitionTitles.remove(player.getUniqueId());
        TitleUtils.clearTitle(player);
        clearTemporaryVanish(player);
        transitioningPlayers.remove(player.getUniqueId());
    }

    private void applyTemporaryVanish(Player hidden) {
        if (hidden == null) {
            return;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(hidden.getUniqueId())) {
                continue;
            }
            plugin.getPlayerVisibilityManager().hide(
                    viewer,
                    hidden,
                    PlayerVisibilityManager.Reason.DUEL
            );
        }
    }

    private void clearTemporaryVanish(Player shown) {
        if (shown == null) {
            return;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(shown.getUniqueId())) {
                continue;
            }
            plugin.getPlayerVisibilityManager().show(
                    viewer,
                    shown,
                    PlayerVisibilityManager.Reason.DUEL
            );
        }
    }

    private void showAllVanishedPlayers() {
        for (Player shown : Bukkit.getOnlinePlayers()) {
            clearTemporaryVanish(shown);
        }
    }

    private String formatDuration(long totalSeconds) {
        long safeSeconds = Math.max(0L, totalSeconds);
        long minutes = safeSeconds / 60L;
        long seconds = safeSeconds % 60L;
        return minutes + "m " + seconds + "s";
    }

    private void ensureTables() {
        Connection connection = connection();
        if (connection == null) {
            return;
        }

        try (Statement st = connection.createStatement()) {
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS duel_arenas (
                      id TEXT PRIMARY KEY,
                      display_name TEXT NOT NULL,
                      spawn1_data TEXT,
                      spawn2_data TEXT,
                      return_data TEXT,
                      region_pos1_data TEXT,
                      region_pos2_data TEXT,
                      enabled INTEGER DEFAULT 1,
                      queue_enabled INTEGER DEFAULT 1
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS duel_stats (
                      player_uuid TEXT PRIMARY KEY,
                      wins INTEGER DEFAULT 0,
                      losses INTEGER DEFAULT 0,
                      draws INTEGER DEFAULT 0,
                      current_streak INTEGER DEFAULT 0,
                      best_streak INTEGER DEFAULT 0
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS duel_matches (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      match_type TEXT NOT NULL,
                      arena_id TEXT NOT NULL,
                      map_source VARCHAR(191) DEFAULT '',
                      biome_key VARCHAR(191) DEFAULT '',
                      world_name VARCHAR(191) DEFAULT '',
                      host_server_id VARCHAR(191) DEFAULT '',
                      privacy_mode VARCHAR(191) DEFAULT 'INVITE_ONLY',
                      player_one_uuid TEXT NOT NULL,
                      player_two_uuid TEXT NOT NULL,
                      winner_uuid TEXT,
                      loser_uuid TEXT,
                      status TEXT NOT NULL,
                      end_reason TEXT,
                      started_at INTEGER DEFAULT 0,
                      ended_at INTEGER DEFAULT 0,
                      duration_seconds INTEGER DEFAULT 0
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS duel_claims (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      player_uuid TEXT NOT NULL,
                      match_id INTEGER NOT NULL,
                      defeated_name TEXT DEFAULT '',
                      item_data TEXT NOT NULL,
                      created_at INTEGER NOT NULL
                    )
                    """);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize duel tables", e);
        }

        ensureArenaColumn("region_pos1_data", "TEXT");
        ensureArenaColumn("region_pos2_data", "TEXT");
        ensureMatchColumn("map_source", "varchar(191) default ''");
        ensureMatchColumn("biome_key", "varchar(191) default ''");
        ensureMatchColumn("world_name", "varchar(191) default ''");
        ensureMatchColumn("host_server_id", "varchar(191) default ''");
        ensureMatchColumn("privacy_mode", "varchar(191) default 'invite_only'");
        ensureClaimColumn("defeated_name", "text default ''");
    }

    private void ensureArenaColumn(String columnName, String definition) {
        if (connection() == null) {
            return;
        }

        try {
            if (plugin.getDatabaseManager().hasColumn("duel_arenas", columnName)) {
                return;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to inspect duel_arenas schema", e);
            return;
        }

        try (Statement st = connection().createStatement()) {
            plugin.getDatabaseManager().executeSchema(st, "alter table duel_arenas add column " + columnName + " " + definition);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add duel_arenas column " + columnName, e);
        }
    }

    private void ensureClaimColumn(String columnName, String definition) {
        if (connection() == null) {
            return;
        }

        try {
            if (plugin.getDatabaseManager().hasColumn("duel_claims", columnName)) {
                return;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to inspect duel_claims schema", e);
            return;
        }

        try (Statement st = connection().createStatement()) {
            plugin.getDatabaseManager().executeSchema(st, "alter table duel_claims add column " + columnName + " " + definition);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add duel_claims column " + columnName, e);
        }
    }

    private void ensureMatchColumn(String columnName, String definition) {
        if (connection() == null) {
            return;
        }

        try {
            if (plugin.getDatabaseManager().hasColumn("duel_matches", columnName)) {
                return;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to inspect duel_matches schema", e);
            return;
        }

        try (Statement st = connection().createStatement()) {
            plugin.getDatabaseManager().executeSchema(st, "alter table duel_matches add column " + columnName + " " + definition);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add duel_matches column " + columnName, e);
        }
    }

    private int getMaxNumericKey(ConfigurationSection section) {
        if (section == null) {
            return -1;
        }

        int max = -1;
        for (String key : section.getKeys(false)) {
            try {
                max = Math.max(max, Integer.parseInt(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return max;
    }

    private void loadArenas() {
        worldManager.loadConfiguredStaticWorlds();
        Map<String, DuelArena> previousArenas = new HashMap<>(arenas);
        arenas.clear();
        if (connection() == null) {
            return;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "select id, display_name, spawn1_data, spawn2_data, return_data, region_pos1_data, region_pos2_data, enabled, queue_enabled from duel_arenas");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String arenaId = rs.getString("id");
                DuelArena loadedArena = new DuelArena(
                        arenaId,
                        rs.getString("display_name"),
                        LocationUtils.parse(rs.getString("spawn1_data")),
                        LocationUtils.parse(rs.getString("spawn2_data")),
                        LocationUtils.parse(rs.getString("return_data")),
                        LocationUtils.parse(rs.getString("region_pos1_data")),
                        LocationUtils.parse(rs.getString("region_pos2_data")),
                        rs.getInt("enabled") == 1,
                        rs.getInt("queue_enabled") == 1,
                        false,
                        true,
                        true,
                        true,
                        false,
                        false,
                        false,
                        false
                );
                DuelArena arena = previousArenas.get(arenaId);
                if (arena != null) {
                    updateLoadedArenaState(arena, loadedArena);
                } else {
                    arena = loadedArena;
                }
                arenas.put(arena.getId(), arena);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "failed to load duel arenas", e);
        }

        synchronizeArenaSettingsConfig();
    }

    private void updateLoadedArenaState(DuelArena target, DuelArena loaded) {
        if (target == null || loaded == null) {
            return;
        }

        target.setDisplayName(loaded.getDisplayName());
        target.setSpawn1(loaded.getSpawn1());
        target.setSpawn2(loaded.getSpawn2());
        target.setReturnLocation(loaded.getReturnLocation());
        target.setRegionPos1(loaded.getRegionPos1());
        target.setRegionPos2(loaded.getRegionPos2());
        target.setEnabled(loaded.isEnabled());
        target.setQueueEnabled(loaded.isQueueEnabled());
        target.setAllowItemDrop(loaded.isAllowItemDrop());
        target.setAllowBlockBreak(loaded.isAllowBlockBreak());
        target.setAllowBlockPlace(loaded.isAllowBlockPlace());
        target.setAllowBucketUse(loaded.isAllowBucketUse());
        target.setNoHunger(loaded.isNoHunger());
        target.setNoWeather(loaded.isNoWeather());
        target.setAlwaysMorning(loaded.isAlwaysMorning());
        target.setNoFallDamage(loaded.isNoFallDamage());
    }

    private void saveArena(DuelArena arena) {
        if (arena == null || connection() == null) {
            return;
        }

        StoredArenaLocationData existingData = loadStoredArenaLocationData(arena.getId());
        String spawn1Data = preserveExistingLocationData(LocationUtils.serialize(arena.getSpawn1()),
                existingData == null ? null : existingData.spawn1Data());
        String spawn2Data = preserveExistingLocationData(LocationUtils.serialize(arena.getSpawn2()),
                existingData == null ? null : existingData.spawn2Data());
        String returnData = preserveExistingLocationData(LocationUtils.serialize(arena.getReturnLocation()),
                existingData == null ? null : existingData.returnData());
        String regionPos1Data = preserveExistingLocationData(LocationUtils.serialize(arena.getRegionPos1()),
                existingData == null ? null : existingData.regionPos1Data());
        String regionPos2Data = preserveExistingLocationData(LocationUtils.serialize(arena.getRegionPos2()),
                existingData == null ? null : existingData.regionPos2Data());

        try (PreparedStatement ps = connection().prepareStatement(
                "replace into duel_arenas (id, display_name, spawn1_data, spawn2_data, return_data, region_pos1_data, region_pos2_data, enabled, queue_enabled) " +
                        "values (?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, arena.getId());
            ps.setString(2, arena.getDisplayName());
            ps.setString(3, spawn1Data);
            ps.setString(4, spawn2Data);
            ps.setString(5, returnData);
            ps.setString(6, regionPos1Data);
            ps.setString(7, regionPos2Data);
            ps.setInt(8, arena.isEnabled() ? 1 : 0);
            ps.setInt(9, arena.isQueueEnabled() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save duel arena " + arena.getId(), e);
        }
    }

    private StoredArenaLocationData loadStoredArenaLocationData(String arenaId) {
        if (arenaId == null || arenaId.isBlank() || connection() == null) {
            return null;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "select spawn1_data, spawn2_data, return_data, region_pos1_data, region_pos2_data from duel_arenas where id = ?")) {
            ps.setString(1, arenaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new StoredArenaLocationData(
                        rs.getString("spawn1_data"),
                        rs.getString("spawn2_data"),
                        rs.getString("return_data"),
                        rs.getString("region_pos1_data"),
                        rs.getString("region_pos2_data")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to inspect existing duel arena data for " + arenaId, e);
            return null;
        }
    }

    private String preserveExistingLocationData(String currentSerialized, String existingSerialized) {
        if (currentSerialized != null && !currentSerialized.isBlank()) {
            return currentSerialized;
        }
        return existingSerialized == null ? "" : existingSerialized;
    }

    private DuelStats loadStats(UUID uuid) {
        if (uuid == null || connection() == null) {
            return DuelStats.empty();
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "select wins, losses, draws, current_streak, best_streak from duel_stats where player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DuelStats(
                            rs.getInt("wins"),
                            rs.getInt("losses"),
                            rs.getInt("draws"),
                            rs.getInt("current_streak"),
                            rs.getInt("best_streak")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load duel stats for " + uuid, e);
        }

        return DuelStats.empty();
    }

    private void saveStats(UUID uuid, DuelStats stats) {
        if (uuid == null || stats == null || connection() == null) {
            return;
        }

        try (PreparedStatement ps = connection().prepareStatement(
                "replace into duel_stats (player_uuid, wins, losses, draws, current_streak, best_streak) values (?,?,?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, stats.getWins());
            ps.setInt(3, stats.getLosses());
            ps.setInt(4, stats.getDraws());
            ps.setInt(5, stats.getCurrentStreak());
            ps.setInt(6, stats.getBestStreak());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save duel stats for " + uuid, e);
        }
    }

    private long insertMatch(DuelMatch.MatchType type, DuelArena arena, UUID firstUuid, UUID secondUuid,
                             DuelMapSelection mapSelection, String biomeKey, String worldName,
                             DuelPrivacyMode privacyMode, String hostServerId) {
        if (connection() == null) {
            return -1L;
        }

        try (PreparedStatement ps = connection().prepareStatement("""
                INSERT INTO duel_matches (match_type, arena_id, map_source, biome_key, world_name, host_server_id, privacy_mode,
                                          player_one_uuid, player_two_uuid, status, started_at)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type.name());
            ps.setString(2, arena.getId());
            DuelMapSelection resolvedSelection = mapSelection == null ? DuelMapSelection.randomStatic() : mapSelection;
            ps.setString(3, resolvedSelection.matchSourceName());
            ps.setString(4, biomeKey == null ? "" : biomeKey);
            ps.setString(5, worldName == null ? "" : worldName);
            ps.setString(6, hostServerId == null ? "" : hostServerId);
            ps.setString(7, (privacyMode == null ? DuelPrivacyMode.INVITE_ONLY : privacyMode).name());
            ps.setString(8, firstUuid.toString());
            ps.setString(9, secondUuid.toString());
            ps.setString(10, DuelMatch.MatchStatus.COUNTDOWN.name());
            ps.setLong(11, 0L);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to insert duel match record", e);
        }
        return -1L;
    }

    private void updateMatchRecord(DuelMatch match, UUID winnerUuid, UUID loserUuid, String endReason) {
        if (match == null || connection() == null) {
            return;
        }

        long endedAt = System.currentTimeMillis();
        long durationSeconds = match.getStartedAt() <= 0L ? 0L : Math.max(0L, (endedAt - match.getStartedAt()) / 1000L);

        try (PreparedStatement ps = connection().prepareStatement("""
                UPDATE duel_matches
                SET winner_uuid = ?, loser_uuid = ?, status = ?, end_reason = ?, started_at = ?, ended_at = ?, duration_seconds = ?
                WHERE id = ?
                """)) {
            if (winnerUuid == null) {
                ps.setNull(1, java.sql.Types.VARCHAR);
            } else {
                ps.setString(1, winnerUuid.toString());
            }
            if (loserUuid == null) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, loserUuid.toString());
            }
            ps.setString(3, DuelMatch.MatchStatus.FINISHED.name());
            ps.setString(4, endReason);
            ps.setLong(5, match.getStartedAt());
            ps.setLong(6, endedAt);
            ps.setLong(7, durationSeconds);
            ps.setLong(8, match.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update duel match record " + match.getId(), e);
        }
    }

    private String serializeItem(ItemStack item) {
        try {
            return ItemSerializationUtils.serialize(item);
        } catch (java.io.IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to serialize duel item", e);
            return "";
        }
    }

    private ItemStack deserializeItem(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }

        try {
            ItemStack item = ItemSerializationUtils.deserialize(encoded);
            CrashProtectionManager.ValidationResult safetyResult = plugin.getCrashProtectionManager()
                    .validateForStorage(item, CrashProtectionManager.Context.DATABASE_LOAD);
            if (!safetyResult.allowed()) {
                plugin.getCrashProtectionManager().logBlockedItem(
                        "duel claim item data",
                        item,
                        CrashProtectionManager.Context.DATABASE_LOAD,
                        safetyResult
                );
                return null;
            }
            return item;
        } catch (IllegalArgumentException | java.io.IOException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to deserialize duel item", e);
            return null;
        }
    }

    private String normalizeArenaId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return id.trim().toLowerCase(Locale.ROOT);
    }

    private String prettifyId(String id) {
        if (id == null || id.isBlank()) {
            return "Arena";
        }
        String[] parts = id.replace('-', ' ').replace('_', ' ').split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return builder.isEmpty() ? "Arena" : builder.toString();
    }

    private String buildQueueUnavailableMessage() {
        List<String> queueEnabledNotReady = new ArrayList<>();
        for (DuelArena arena : getArenas()) {
            if (arena.isEnabled() && arena.isQueueEnabled() && !arena.isReady()) {
                queueEnabledNotReady.add(arena.getId());
            }
        }

        if (!queueEnabledNotReady.isEmpty()) {
            return "&cqueue arenas exist but are not ready yet. "
                    + "&7use &f/arena setpos1 <id> &7and &f/arena setpos2 <id> "
                    + "&7for: &f" + String.join("&7, &f", queueEnabledNotReady) + "&7.";
        }

        return "&cno ready queue arenas are configured yet. "
                + "&7enable queue with &f/arena queue <id> true&7, then set &fpos1 &7and &fpos2&7.";
    }

    private void synchronizeArenaSettingsConfig() {
        FileConfiguration duelConfig = plugin.getConfigManager().getOriginalDuels();
        if (duelConfig == null) {
            return;
        }

        boolean changed = ensureGeneratedTerrainSettingsEntry(duelConfig);
        for (DuelArena arena : arenas.values()) {
            if (arena == null) {
                continue;
            }
            changed |= ensureArenaSettingsEntry(duelConfig, arena);
            applyConfiguredArenaSettings(arena, duelConfig);
        }

        if (changed) {
            plugin.getConfigManager().saveDuels();
        }
    }

    private boolean ensureGeneratedTerrainSettingsEntry(FileConfiguration duelConfig) {
        if (duelConfig == null) {
            return false;
        }

        boolean changed = false;
        for (DuelWorldManager.TerrainMode terrainMode : DuelWorldManager.TerrainMode.values()) {
            for (ArenaSetting setting : ArenaSetting.values()) {
                String path = getTerrainModeSettingPath(terrainMode, setting);
                if (!duelConfig.contains(path)) {
                    duelConfig.set(path, defaultArenaSettingValue(setting));
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean ensureArenaSettingsEntry(FileConfiguration duelConfig, DuelArena arena) {
        if (duelConfig == null || arena == null) {
            return false;
        }

        boolean changed = false;
        for (ArenaSetting setting : ArenaSetting.values()) {
            String path = getArenaSettingPath(arena.getId(), setting);
            if (!duelConfig.contains(path)) {
                duelConfig.set(path, defaultArenaSettingValue(setting));
                changed = true;
            }
        }
        return changed;
    }

    private void applyConfiguredArenaSettings(DuelArena arena, FileConfiguration duelConfig) {
        if (arena == null || duelConfig == null) {
            return;
        }

        for (ArenaSetting setting : ArenaSetting.values()) {
            setArenaSetting(arena, setting, duelConfig.getBoolean(
                    getArenaSettingPath(arena.getId(), setting),
                    defaultArenaSettingValue(setting)
            ));
        }
    }

    private void applyGeneratedTerrainSettings(DuelArena arena, DuelWorldManager.TerrainMode terrainMode) {
        if (arena == null || terrainMode == null) {
            return;
        }

        FileConfiguration duelConfig = config();
        for (ArenaSetting setting : ArenaSetting.values()) {
            setArenaSetting(arena, setting, duelConfig.getBoolean(
                    getTerrainModeSettingPath(terrainMode, setting),
                    defaultArenaSettingValue(setting)
            ));
        }
    }

    private String getArenaSettingPath(String arenaId, ArenaSetting setting) {
        return "ARENA_SETTINGS." + normalizeArenaId(arenaId) + "." + getArenaSettingKey(setting);
    }

    private String getTerrainModeSettingPath(DuelWorldManager.TerrainMode terrainMode, ArenaSetting setting) {
        return TERRAIN_MODE_SETTINGS_PATH + "." + terrainMode.name() + "." + getArenaSettingKey(setting);
    }

    private String getArenaSettingKey(ArenaSetting setting) {
        return switch (setting) {
            case ALLOW_ITEM_DROP -> "ALLOW_ITEM_DROP";
            case ALLOW_BLOCK_BREAK -> "ALLOW_BLOCK_BREAK";
            case ALLOW_BLOCK_PLACE -> "ALLOW_BLOCK_PLACE";
            case ALLOW_BUCKET_USE -> "ALLOW_BUCKET_USE";
            case NO_HUNGER -> "NO_HUNGER";
            case NO_WEATHER -> "NO_WEATHER";
            case ALWAYS_MORNING -> "ALWAYS_MORNING";
            case NO_FALL_DAMAGE -> "NO_FALL_DAMAGE";
        };
    }

    private boolean defaultArenaSettingValue(ArenaSetting setting) {
        return switch (setting) {
            case ALLOW_BLOCK_BREAK, ALLOW_BLOCK_PLACE, ALLOW_BUCKET_USE -> true;
            case ALLOW_ITEM_DROP, NO_HUNGER, NO_WEATHER, ALWAYS_MORNING, NO_FALL_DAMAGE -> false;
        };
    }

    private void setArenaSetting(DuelArena arena, ArenaSetting setting, boolean enabled) {
        switch (setting) {
            case ALLOW_ITEM_DROP -> arena.setAllowItemDrop(enabled);
            case ALLOW_BLOCK_BREAK -> arena.setAllowBlockBreak(enabled);
            case ALLOW_BLOCK_PLACE -> arena.setAllowBlockPlace(enabled);
            case ALLOW_BUCKET_USE -> arena.setAllowBucketUse(enabled);
            case NO_HUNGER -> arena.setNoHunger(enabled);
            case NO_WEATHER -> arena.setNoWeather(enabled);
            case ALWAYS_MORNING -> arena.setAlwaysMorning(enabled);
            case NO_FALL_DAMAGE -> arena.setNoFallDamage(enabled);
        }
    }

    private ArenaRegionBounds resolveArenaRegionBounds(DuelArena arena) {
        if (arena == null) {
            return null;
        }

        Location pos1 = arena.getRegionPos1();
        Location pos2 = arena.getRegionPos2();
        if ((pos1 == null || pos1.getWorld() == null || pos2 == null || pos2.getWorld() == null) && arena.isReady()) {
            pos1 = arena.getSpawn1();
            pos2 = arena.getSpawn2();
        }
        if (pos1 == null || pos1.getWorld() == null || pos2 == null || pos2.getWorld() == null) {
            return null;
        }

        World world = pos1.getWorld();
        if (!world.getName().equalsIgnoreCase(pos2.getWorld().getName())) {
            return null;
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX()) - getRollbackHorizontalPadding();
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX()) + getRollbackHorizontalPadding();
        int minY = Math.max(world.getMinHeight(), Math.min(pos1.getBlockY(), pos2.getBlockY()) - getRollbackVerticalPadding());
        int maxY = Math.min(world.getMaxHeight() - 1, Math.max(pos1.getBlockY(), pos2.getBlockY()) + getRollbackVerticalPadding());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ()) - getRollbackHorizontalPadding();
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ()) + getRollbackHorizontalPadding();
        if (maxY < minY) {
            return null;
        }

        return new ArenaRegionBounds(world, minX, maxX, minY, maxY, minZ, maxZ);
    }

    private boolean isWithinArenaBounds(Location location, ArenaRegionBounds bounds) {
        if (location == null || bounds == null || location.getWorld() == null) {
            return false;
        }

        return bounds.world().getName().equalsIgnoreCase(location.getWorld().getName())
                && location.getBlockX() >= bounds.minX()
                && location.getBlockX() <= bounds.maxX()
                && location.getBlockY() >= bounds.minY()
                && location.getBlockY() <= bounds.maxY()
                && location.getBlockZ() >= bounds.minZ()
                && location.getBlockZ() <= bounds.maxZ();
    }

    private DuelArena findArenaContainingLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        for (DuelArena arena : getArenas()) {
            ArenaRegionBounds bounds = resolveArenaRegionBounds(arena);
            if (bounds != null && isWithinArenaBounds(location, bounds)) {
                return arena;
            }
        }
        return null;
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getDuels();
    }

    private Connection connection() {
        return plugin.getDatabaseManager().getConnection();
    }

    private int normalizeSize(int size) {
        int normalized = Math.max(9, size);
        normalized = ((normalized + 8) / 9) * 9;
        return Math.min(54, normalized);
    }

    private void send(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isBlank()) {
            sender.sendMessage(ColorUtils.toComponent(message));
        }
    }

    private void play(Player player, String path) {
        if (player == null) {
            return;
        }
        PlayerSettingUtils.SoundChannel channel;
        if (path != null && (path.contains("REQUEST") || path.contains("QUEUE-JOIN"))) {
            channel = PlayerSettingUtils.SoundChannel.NOTIFICATION;
        } else if (path != null && (path.contains("MATCH-FOUND")
                || path.contains("MATCH-START")
                || path.contains("VICTORY")
                || path.contains("DEFEAT"))) {
            channel = PlayerSettingUtils.SoundChannel.DUEL;
        } else {
            channel = PlayerSettingUtils.SoundChannel.GAMEPLAY;
        }
        SoundUtils.play(plugin, player, plugin.getConfigManager().getSound(path), channel);
    }

    private record PendingRespawnState(Location respawnLocation, Location returnLocation, long delayTicks, GeneratedInventorySnapshot restoreSnapshot) {
    }

    private static final class ClaimAccumulator {
        private String defeatedName;
        private long createdAt;
        private final List<ItemStack> items = new ArrayList<>();

        private ClaimAccumulator(String defeatedName, long createdAt) {
            this.defeatedName = defeatedName;
            this.createdAt = createdAt;
        }

        private void updateMetadata(String defeatedName, long createdAt) {
            if ((this.defeatedName == null || this.defeatedName.isBlank())
                    && defeatedName != null && !defeatedName.isBlank()) {
                this.defeatedName = defeatedName;
            }
            this.createdAt = Math.min(this.createdAt, createdAt);
        }

        private String defeatedName() {
            return defeatedName == null || defeatedName.isBlank() ? "unknown" : defeatedName;
        }

        private long createdAt() {
            return createdAt;
        }

        private List<ItemStack> items() {
            return items;
        }
    }

    private record ClaimItemRow(long id, String defeatedName, ItemStack item, long createdAt) {
    }

    private record TransitionPlayerState(
            GameMode gameMode,
            boolean allowFlight,
            boolean flying,
            boolean invulnerable,
            boolean collidable
    ) {
    }

    private record TransitionTitleState(String title, String subtitle) {
    }

    private String publicName(Player player) {
        return plugin.getHideManager() == null ? player.getName() : plugin.getHideManager().publicName(player);
    }

    private String plainPublicName(Player player) {
        return plugin.getHideManager() == null ? player.getName() : plugin.getHideManager().plainPublicName(player);
    }

    private boolean matchesIdentity(Player viewer, UUID subjectUuid, String publicSnapshot, String input) {
        if (input == null || input.isBlank()) {
            return true;
        }
        String candidate = input.trim();
        if (publicSnapshot != null && publicSnapshot.equalsIgnoreCase(candidate)) {
            return true;
        }
        Player subject = Bukkit.getPlayer(subjectUuid);
        return subject != null
                && plugin.getHideManager() != null
                && plugin.getHideManager().canSeeRealIdentity(viewer)
                && subject.getName().equalsIgnoreCase(candidate);
    }

    private record GeneratedInventorySnapshot(
            ItemStack[] storage,
            ItemStack[] armor,
            ItemStack offHand,
            ItemStack cursor
    ) {
        private static GeneratedInventorySnapshot empty() {
            return new GeneratedInventorySnapshot(new ItemStack[0], new ItemStack[0], null, null);
        }

        private List<ItemStack> copyItems() {
            List<ItemStack> items = new ArrayList<>();
            addItems(items, storage);
            addItems(items, armor);
            addItems(items, new ItemStack[]{offHand});
            addItems(items, new ItemStack[]{cursor});
            return items;
        }

        private static void addItems(List<ItemStack> destination, ItemStack[] source) {
            if (destination == null || source == null) {
                return;
            }
            for (ItemStack item : source) {
                if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                    continue;
                }
                destination.add(item.clone());
            }
        }
    }

    private record BlockKey(String worldName, int x, int y, int z) {
    }

    private record StoredArenaLocationData(
            String spawn1Data,
            String spawn2Data,
            String returnData,
            String regionPos1Data,
            String regionPos2Data
    ) {
    }

    private record BlockSnapshot(int x, int y, int z, String blockDataString) {
    }

    private record ArenaSnapshot(
            String worldName,
            int minX,
            int maxX,
            int minY,
            int maxY,
            int minZ,
            int maxZ,
            List<BlockSnapshot> blocks
    ) {
        private boolean contains(Location location) {
            if (location == null || location.getWorld() == null) {
                return false;
            }
            if (!worldName.equalsIgnoreCase(location.getWorld().getName())) {
                return false;
            }
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            return x >= minX && x <= maxX
                    && y >= minY && y <= maxY
                    && z >= minZ && z <= maxZ;
        }
    }

    private record ArenaRegionBounds(
            World world,
            int minX,
            int maxX,
            int minY,
            int maxY,
            int minZ,
            int maxZ
    ) {
    }
}
