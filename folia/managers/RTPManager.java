package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class RTPManager {

    public record SearchSettings(
            String worldName,
            int minRadius,
            int maxRadius,
            int centerX,
            int centerZ,
            int maxAttempts,
            int maxChunkSamples,
            int attemptIntervalTicks
    ) {}

    public record RTPDestination(
            String id,
            int slot,
            org.bukkit.Material material,
            String displayName,
            List<String> lore,
            String worldName,
            boolean enabled
    ) {
        public RTPDestination {
            lore = List.copyOf(lore == null ? List.of() : lore);
        }
    }

    private static final long SEARCH_ACTIONBAR_REFRESH_TICKS = 1L;
    private static final long MIN_SEARCH_DISPLAY_TICKS = 60L;
    private static final int SEARCH_ATTEMPTS_PER_TICK = 1;
    private static final long FOUND_ACTIONBAR_DELAY_TICKS = 20L;
    private static final int DEFAULT_MAX_CONCURRENT_RTP = 1;
    private static final int MIN_MAX_ATTEMPTS = 32;
    private static final int MIN_MAX_CHUNK_SAMPLES = 64;
    private static final int DEFAULT_MAX_ATTEMPTS = 64;
    private static final int DEFAULT_MAX_CHUNK_SAMPLES = 128;
    private static final int DEFAULT_ATTEMPT_INTERVAL_TICKS = 8;
    private static final int CHUNK_COLUMN_CHECKS = 8;
    private static final int NETHER_ROOF_PADDING_BLOCKS = 8;
    private static final int PLAYER_CLEARANCE_BLOCKS = 2;
    private static final String GENERATE_CHUNKS_SETTING = "SETTINGS.GENERATE-CHUNKS";
    private static final String GENERATE_FALLBACK_CHUNKS_SETTING = "SETTINGS.GENERATE-FALLBACK-CHUNKS";
    private static final String GENERATE_FALLBACK_AFTER_SETTING = "SETTINGS.GENERATE-FALLBACK-AFTER-SAMPLES";
    private static final String MAX_GENERATE_FALLBACK_SAMPLES_SETTING = "SETTINGS.MAX-GENERATE-FALLBACK-SAMPLES";
    private static final String LOAD_GENERATED_CHUNKS_SETTING = "SETTINGS.LOAD-GENERATED-CHUNKS";
    private static final String LOADED_CHUNK_FALLBACK_SETTING = "SETTINGS.FALLBACK-TO-LOADED-CHUNKS";
    private static final String LOADED_CHUNK_FALLBACK_AFTER_SETTING = "SETTINGS.LOADED-CHUNK-FALLBACK-AFTER-SAMPLES";
    private static final String PRELOAD_TELEPORT_CHUNKS_SETTING = "SETTINGS.PRELOAD-TELEPORT-CHUNKS";
    private static final String PRELOAD_RADIUS_SETTING = "SETTINGS.PRELOAD-RADIUS";
    private static final String PRELOAD_CHUNKS_PER_TICK_SETTING = "SETTINGS.PRELOAD-CHUNKS-PER-TICK";
    private static final String PRELOAD_MAX_TICKS_SETTING = "SETTINGS.PRELOAD-MAX-TICKS";
    private static final String POST_TELEPORT_CHUNK_THROTTLE_SETTING = "SETTINGS.POST-TELEPORT-CHUNK-THROTTLE";
    private static final String POST_TELEPORT_VIEW_DISTANCE_SETTING = "SETTINGS.POST-TELEPORT-VIEW-DISTANCE";
    private static final int DEFAULT_GENERATE_FALLBACK_AFTER_SAMPLES = 48;
    private static final int DEFAULT_MAX_GENERATE_FALLBACK_SAMPLES = 32;

    private static final class SearchProgress {
        private final String worldName;
        private final SearchSettings settings;
        private long elapsedTicks;
        private int attemptsUsed;
        private int chunkSamplesUsed;
        private int generateFallbackSamplesUsed;
        private long lastElapsedSecond;
        private boolean attemptInFlight;
        private Location pendingFoundLocation;

        private SearchProgress(String worldName, SearchSettings settings) {
            this.worldName = worldName;
            this.settings = settings;
        }
    }

    private record LocationAttempt(Location location, boolean countedAttempt) {
    }

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Map<String, Long>> cooldownsByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> activeSearchTasks = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> activeResultTasks = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Location>> activeDirectSearches = new ConcurrentHashMap<>();
    private final Map<UUID, SearchProgress> activeSearches = new ConcurrentHashMap<>();
    private List<RTPDestination> configuredDestinations = List.of();
    private List<RTPDestination> menuDestinations = List.of();

    public RTPManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        clearAllSearches();
        cooldownsByPlayer.clear();
        configuredDestinations = loadConfiguredDestinations();
        menuDestinations = buildMenuDestinations(configuredDestinations);
    }

    public boolean isEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.RTP)
                && plugin.getConfigManager().getRtp().getBoolean("ENABLED", true);
    }

    public void clearSearch(UUID playerId) {
        stopSearch(playerId, true);
    }

    private void stopSearch(UUID playerId, boolean clearActionBar) {
        ScheduledTask task = activeSearchTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
        ScheduledTask resultTask = activeResultTasks.remove(playerId);
        if (resultTask != null) {
            resultTask.cancel();
        }
        activeSearches.remove(playerId);
        CompletableFuture<Location> directSearch = activeDirectSearches.remove(playerId);
        if (directSearch != null) {
            directSearch.complete(null);
        }

        if (!clearActionBar) {
            return;
        }

        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            PlayerSettingUtils.clearActionBar(player);
        }
    }

    public List<RTPDestination> getMenuDestinations() {
        return menuDestinations;
    }

    public boolean hasMenuDestinations() {
        return !menuDestinations.isEmpty();
    }

    public RTPDestination getDestinationBySlot(int slot) {
        for (RTPDestination destination : menuDestinations) {
            if (destination.slot() == slot) {
                return destination;
            }
        }
        return null;
    }

    public int getPlayersInWorld(String worldName) {
        World world = getLoadedWorld(worldName);
        return world == null ? 0 : world.getPlayers().size();
    }

    public int getWorldCooldownSeconds(String worldName) {
        ConfigurationSection settings = getWorldSettingsSection(worldName);
        return settings == null ? 0 : Math.max(0, settings.getInt("COOLDOWN", 0));
    }

    public SearchSettings getWorldSearchSettings(String worldName) {
        ConfigurationSection worldSettings = getWorldSettingsSection(worldName);
        if (worldSettings == null) {
            return null;
        }

        int minRadius = worldSettings.getInt("MIN-RADIUS", 500);
        int maxRadius = worldSettings.getInt("MAX-RADIUS", 5000);
        int centerX = worldSettings.getInt("CENTER-X", 0);
        int centerZ = worldSettings.getInt("CENTER-Z", 0);
        int maxAttempts = plugin.getConfigManager().getRtp().getInt("SETTINGS.MAX-ATTEMPTS", DEFAULT_MAX_ATTEMPTS);
        int maxChunkSamples = plugin.getConfigManager().getRtp().getInt("SETTINGS.MAX-CHUNK-SAMPLES", DEFAULT_MAX_CHUNK_SAMPLES);
        int attemptIntervalTicks = plugin.getConfigManager().getRtp().getInt("SETTINGS.ATTEMPT-INTERVAL-TICKS", DEFAULT_ATTEMPT_INTERVAL_TICKS);

        return new SearchSettings(
                worldName,
                minRadius,
                Math.max(minRadius, maxRadius),
                centerX,
                centerZ,
                normalizeSearchLimit(maxAttempts),
                normalizeChunkSampleLimit(maxChunkSamples),
                normalizeAttemptInterval(attemptIntervalTicks)
        );
    }

    public SearchSettings getZoneSearchSettings() {
        int minRadius = plugin.getConfigManager().getConfig().getInt("RTP-ZONE.WORLD.MIN-RADIUS", 500);
        int maxRadius = plugin.getConfigManager().getConfig().getInt("RTP-ZONE.WORLD.MAX-RADIUS", 2000);
        int centerX = plugin.getConfigManager().getConfig().getInt("RTP-ZONE.WORLD.CENTER-X", 0);
        int centerZ = plugin.getConfigManager().getConfig().getInt("RTP-ZONE.WORLD.CENTER-Z", 0);
        int maxAttempts = plugin.getConfigManager().getRtp().getInt("SETTINGS.MAX-ATTEMPTS", DEFAULT_MAX_ATTEMPTS);
        int maxChunkSamples = plugin.getConfigManager().getRtp().getInt("SETTINGS.MAX-CHUNK-SAMPLES", DEFAULT_MAX_CHUNK_SAMPLES);
        int attemptIntervalTicks = plugin.getConfigManager().getRtp().getInt("SETTINGS.ATTEMPT-INTERVAL-TICKS", DEFAULT_ATTEMPT_INTERVAL_TICKS);
        String worldName = normalizeConfiguredWorldName(
                plugin.getConfigManager().getConfig().getString("RTP-ZONE.WORLD.NAME", "world")
        );

        return new SearchSettings(
                worldName,
                minRadius,
                Math.max(minRadius, maxRadius),
                centerX,
                centerZ,
                normalizeSearchLimit(maxAttempts),
                normalizeChunkSampleLimit(maxChunkSamples),
                normalizeAttemptInterval(attemptIntervalTicks)
        );
    }

    public String describeWorld(String worldName) {
        for (RTPDestination destination : configuredDestinations) {
            if (destination.worldName().equalsIgnoreCase(worldName)) {
                String displayName = ColorUtils.strip(destination.displayName()).trim();
                if (!displayName.isBlank()) {
                    return displayName;
                }
            }
        }

        String lower = worldName.toLowerCase(Locale.ROOT);
        if (lower.equalsIgnoreCase(getLoadedNormalWorldName())) {
            return "Overworld";
        }
        if (lower.equalsIgnoreCase(getLoadedNetherWorldName())) {
            return "Nether";
        }
        if (lower.equalsIgnoreCase(getLoadedEndWorldName())) {
            return "the end";
        }
        return worldName;
    }

    public boolean queueMenuTeleport(Player player, RTPDestination destination) {
        if (!isEnabled()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp().getString("MESSAGES.DISABLED", "&cʀᴛᴘ ɪѕ ᴅɪѕᴀʙʟᴇᴅ.")
            ));
            return false;
        }
        if (destination == null) {
            return false;
        }
        return queueTeleport(player, destination.worldName());
    }

    public boolean queueCommandTeleport(Player player, String selector) {
        if (!isEnabled()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp().getString("MESSAGES.DISABLED", "&cʀᴛᴘ ɪѕ ᴅɪѕᴀʙʟᴇᴅ.")
            ));
            return false;
        }
        String worldName = resolveWorldSelector(selector);
        if (worldName == null || worldName.isBlank()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp().getString("MESSAGES.WORLD-NOT-EXIST", "&cᴡᴏʀʟᴅ ɴᴏᴛ ꜰᴏᴜɴᴅ.")
            ));
            return false;
        }
        return queueTeleport(player, worldName);
    }

    public boolean isPortalDestinationAvailable(String selector) {
        if (!isEnabled()) {
            return false;
        }
        String worldName = resolveWorldSelector(selector);
        if (worldName == null || worldName.isBlank()) {
            return false;
        }
        if (isDeniedWorld(worldName) || isConfiguredDestinationDisabled(worldName)) {
            return false;
        }
        if (!hasWorldSearchSettings(worldName)) {
            return false;
        }
        return isWorldAvailable(worldName);
    }

    public List<String> getPortalSelectorSuggestions() {
        if (!isEnabled()) {
            return List.of();
        }
        Set<String> selectors = new LinkedHashSet<>();

        for (RTPDestination destination : configuredDestinations) {
            if (!destination.enabled()) {
                continue;
            }
            if (!hasWorldSearchSettings(destination.worldName())) {
                continue;
            }
            if (!isWorldAvailable(destination.worldName())) {
                continue;
            }

            selectors.add(destination.id());
            selectors.add(destination.worldName());
        }

        String normalWorld = getLoadedNormalWorldName();
        if (isWorldAvailable(normalWorld)
                && hasWorldSearchSettings(normalWorld)
                && !isDeniedWorld(normalWorld)
                && !isConfiguredDestinationDisabled(normalWorld)) {
            selectors.add("overworld");
        }
        String netherWorld = getLoadedNetherWorldName();
        if (isWorldAvailable(netherWorld)
                && hasWorldSearchSettings(netherWorld)
                && !isDeniedWorld(netherWorld)
                && !isConfiguredDestinationDisabled(netherWorld)) {
            selectors.add("nether");
        }
        String endWorld = getLoadedEndWorldName();
        if (isWorldAvailable(endWorld)
                && hasWorldSearchSettings(endWorld)
                && !isDeniedWorld(endWorld)
                && !isConfiguredDestinationDisabled(endWorld)) {
            selectors.add("end");
        }

        List<String> list = new ArrayList<>(selectors);
        list.sort(String.CASE_INSENSITIVE_ORDER);
        return List.copyOf(list);
    }

    public CompletableFuture<Location> findSafeLocationAsync(Player player, SearchSettings settings) {
        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }
        UUID playerId = player.getUniqueId();
        if (hasActiveRtpFlow(playerId) || plugin.getTeleportManager().hasPendingType(playerId, "RTP")) {
            return CompletableFuture.completedFuture(null);
        }
        if (isQueueFull(playerId)) {
            return CompletableFuture.completedFuture(null);
        }
        if (!isSearchRequestValid(settings)) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Location> future = new CompletableFuture<>();
        CompletableFuture<Location> existing = activeDirectSearches.putIfAbsent(playerId, future);
        if (existing != null) {
            return CompletableFuture.completedFuture(null);
        }
        future.whenComplete((location, throwable) -> activeDirectSearches.remove(playerId, future));
        scheduleFindSafeLocationAsyncHelper(settings, 0, 0, 0, future, settings.attemptIntervalTicks());
        return future;
    }

    public CompletableFuture<Location> findSafeLocationAsync(SearchSettings settings) {
        if (!isSearchRequestValid(settings)) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Location> future = new CompletableFuture<>();
        scheduleFindSafeLocationAsyncHelper(settings, 0, 0, 0, future, settings.attemptIntervalTicks());
        return future;
    }

    private boolean isSearchRequestValid(SearchSettings settings) {
        if (!plugin.getFeatureManager().isEnabled(FeatureManager.Feature.RTP)) {
            return false;
        }
        return settings != null && settings.worldName() != null && !settings.worldName().isBlank();
    }

    private void scheduleFindSafeLocationAsyncHelper(
            SearchSettings settings,
            int attemptsUsed,
            int chunkSamplesUsed,
            int generateFallbackSamplesUsed,
            CompletableFuture<Location> future,
            long delayTicks
    ) {
        if (future.isDone()) {
            return;
        }
        plugin.getFoliaScheduler().runGlobalLater(
                () -> findSafeLocationAsyncHelper(
                        settings,
                        attemptsUsed,
                        chunkSamplesUsed,
                        generateFallbackSamplesUsed,
                        future
                ),
                Math.max(0L, delayTicks)
        );
    }

    private void retryFindSafeLocationAsyncHelper(
            SearchSettings settings,
            int attemptsUsed,
            int chunkSamplesUsed,
            int generateFallbackSamplesUsed,
            CompletableFuture<Location> future
    ) {
        scheduleFindSafeLocationAsyncHelper(
                settings,
                attemptsUsed,
                chunkSamplesUsed,
                generateFallbackSamplesUsed,
                future,
                settings.attemptIntervalTicks()
        );
    }

    private void findSafeLocationAsyncHelper(
            SearchSettings settings,
            int attemptsUsed,
            int chunkSamplesUsed,
            int generateFallbackSamplesUsed,
            CompletableFuture<Location> future
    ) {
        if (future.isDone()) {
            return;
        }
        if (!hasAttemptBudget(attemptsUsed, settings)
                || !hasChunkSampleBudget(chunkSamplesUsed, settings)) {
            completeDirectSearchFailure(settings, attemptsUsed, chunkSamplesUsed, generateFallbackSamplesUsed, future);
            return;
        }
        int nextChunkSamplesUsed = chunkSamplesUsed + 1;
        boolean generateFallback = shouldUseGenerateFallback(chunkSamplesUsed, generateFallbackSamplesUsed);
        boolean useLoadedFallback = !generateFallback && shouldUseLoadedChunkFallback(chunkSamplesUsed);
        if (useLoadedFallback) {
            World world = resolveWorld(settings.worldName());
            if (world == null) {
                completeDirectSearchFailure(settings, attemptsUsed, nextChunkSamplesUsed, generateFallbackSamplesUsed, future);
                return;
            }
            plugin.getFoliaScheduler().runRegion(world, settings.centerX() >> 4, settings.centerZ() >> 4, () -> {
                try {
                    LocationAttempt attempt = tryLoadedChunkLocationAttempt(settings);
                    int nextAttemptsUsed = attemptsUsed + (attempt.countedAttempt() ? 1 : 0);
                    if (attempt.location() != null) {
                        future.complete(attempt.location());
                    } else {
                        retryFindSafeLocationAsyncHelper(
                                settings,
                                nextAttemptsUsed,
                                nextChunkSamplesUsed,
                                generateFallbackSamplesUsed,
                                future
                        );
                    }
                } catch (RuntimeException exception) {
                    retryFindSafeLocationAsyncHelper(
                            settings,
                            attemptsUsed + 1,
                            nextChunkSamplesUsed,
                            generateFallbackSamplesUsed,
                            future
                    );
                }
            });
        } else {
            World world = resolveWorld(settings.worldName());
            if (world == null) {
                completeDirectSearchFailure(settings, attemptsUsed, nextChunkSamplesUsed, generateFallbackSamplesUsed, future);
                return;
            }
            int minRadius = Math.max(0, settings.minRadius());
            int maxRadius = Math.max(minRadius, settings.maxRadius());
            double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
            double distance = minRadius;
            if (maxRadius > minRadius) {
                distance += ThreadLocalRandom.current().nextDouble(0, maxRadius - minRadius);
            }
            int x = settings.centerX() + (int) Math.round(Math.cos(angle) * distance);
            int z = settings.centerZ() + (int) Math.round(Math.sin(angle) * distance);
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            boolean generateChunks = plugin.getConfigManager().getRtp().getBoolean(GENERATE_CHUNKS_SETTING, false);
            boolean generateForSample = generateChunks || generateFallback;
            int nextGenerateFallbackSamplesUsed = generateFallback
                    ? generateFallbackSamplesUsed + 1
                    : generateFallbackSamplesUsed;
            if (!generateForSample && !plugin.getConfigManager().getRtp().getBoolean(LOAD_GENERATED_CHUNKS_SETTING, true)) {
                retryFindSafeLocationAsyncHelper(
                        settings,
                        attemptsUsed,
                        nextChunkSamplesUsed,
                        nextGenerateFallbackSamplesUsed,
                        future
                );
                return;
            }
            getChunkAtAsync(world, chunkX, chunkZ, generateForSample).thenAccept(chunk -> {
                plugin.getFoliaScheduler().runRegion(world, chunkX, chunkZ, () -> {
                    try {
                        if (chunk == null) {
                            retryFindSafeLocationAsyncHelper(
                                    settings,
                                    attemptsUsed,
                                    nextChunkSamplesUsed,
                                    nextGenerateFallbackSamplesUsed,
                                    future
                            );
                            return;
                        }
                        Location found = resolveSafeLocationInChunk(world, settings, x, z, chunkX, chunkZ);
                        int nextAttemptsUsed = attemptsUsed + 1;
                        if (found != null) {
                            future.complete(found);
                        } else {
                            retryFindSafeLocationAsyncHelper(
                                    settings,
                                    nextAttemptsUsed,
                                    nextChunkSamplesUsed,
                                    nextGenerateFallbackSamplesUsed,
                                    future
                            );
                        }
                    } catch (RuntimeException exception) {
                        retryFindSafeLocationAsyncHelper(
                                settings,
                                attemptsUsed + 1,
                                nextChunkSamplesUsed,
                                nextGenerateFallbackSamplesUsed,
                                future
                        );
                    }
                });
            }).exceptionally(throwable -> {
                plugin.getFoliaScheduler().runRegion(world, chunkX, chunkZ, () -> {
                    int nextAttemptsUsed = generateForSample ? attemptsUsed + 1 : attemptsUsed;
                    retryFindSafeLocationAsyncHelper(
                            settings,
                            nextAttemptsUsed,
                            nextChunkSamplesUsed,
                            nextGenerateFallbackSamplesUsed,
                            future
                    );
                });
                return null;
            });
        }
    }

    private void completeDirectSearchFailure(
            SearchSettings settings,
            int attemptsUsed,
            int chunkSamplesUsed,
            int generateFallbackSamplesUsed,
            CompletableFuture<Location> future
    ) {
        if (future.complete(null)) {
            logSearchFailure(settings, attemptsUsed, chunkSamplesUsed, generateFallbackSamplesUsed);
        }
    }

    private boolean queueTeleport(Player player, String worldName) {
        if (isDeniedWorld(worldName)) {
            player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ʀᴛᴘ ɪɴ ᴛʜɪѕ ᴡᴏʀʟᴅ."));
            return false;
        }

        if (isConfiguredDestinationDisabled(worldName)) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp()
                            .getString("MESSAGES.DESTINATION-DISABLED", "&cᴛʜɪѕ ᴅᴇѕᴛɪɴᴀᴛɪᴏɴ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ.")
            ));
            return false;
        }

        World world = resolveWorld(worldName);
        if (world == null) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp().getString("MESSAGES.WORLD-NOT-EXIST", "&cᴡᴏʀʟᴅ ɴᴏᴛ ꜰᴏᴜɴᴅ.")
            ));
            return false;
        }

        SearchSettings settings = getWorldSearchSettings(worldName);
        if (settings == null) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp()
                            .getString("MESSAGES.DESTINATION-DISABLED", "&cᴛʜɪѕ ᴅᴇѕᴛɪɴᴀᴛɪᴏɴ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ.")
            ));
            return false;
        }

        if (hasActiveRtpFlow(player.getUniqueId()) || plugin.getTeleportManager().hasPendingType(player.getUniqueId(), "RTP")) {
            player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜʀ ʀᴛᴘ ɪѕ ᴀʟʀᴇᴀᴅʏ ɪɴ ᴘʀᴏɢʀᴇѕѕ."));
            return false;
        }

        long cooldownRemaining = getCooldownRemainingMillis(player.getUniqueId(), worldName);
        if (cooldownRemaining > 0L) {
            long remainingSeconds = Math.max(1L, (long) Math.ceil(cooldownRemaining / 1000.0D));
            String message = plugin.getConfigManager().getRtp()
                    .getString("MESSAGES.COOLDOWN", "&cʏᴏᴜ ᴄᴀɴ'ᴛ ʀᴛᴘ ꜰᴏʀ ᴀɴᴏᴛʜᴇʀ {remaining}ѕ.");
            message = message.replace("{remaining}", String.valueOf(remainingSeconds))
                    .replace("%remaining%", String.valueOf(remainingSeconds));
            player.sendMessage(ColorUtils.toComponent(message));
            return false;
        }

        if (isQueueFull(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp()
                            .getString("MESSAGES.MAX-PLAYERS", "&cᴛᴏᴏ ᴍᴀɴʏ ᴘʟᴀʏᴇʀѕ ᴀʀᴇ ᴜѕɪɴɢ ʀᴛᴘ ʀɪɢʜᴛ ɴᴏᴡ. ᴘʟᴇᴀѕᴇ ᴛʀʏ ᴀɢᴀɪɴ ʟᴀᴛᴇʀ.")
            ));
            return false;
        }

        startSearch(player, worldName, settings);
        return true;
    }

    private void startSearch(Player player, String worldName, SearchSettings settings) {
        clearSearch(player.getUniqueId());

        String worldLabel = describeWorld(worldName);
        String searching = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.SEARCHING", "&aѕᴇᴀʀᴄʜɪɴɢ ꜰᴏʀ ѕᴀꜰᴇ ʟᴏᴄᴀᴛɪᴏɴ ɪɴ {world}...")
                .replace("{world}", worldLabel);
        player.sendMessage(ColorUtils.toComponent(searching));

        SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-START"));

        SearchProgress progress = new SearchProgress(worldName, settings);
        activeSearches.put(player.getUniqueId(), progress);

        ScheduledTask task = plugin.getFoliaScheduler().runEntityTimer(
                player,
                () -> tickSearch(player.getUniqueId()),
                0L,
                SEARCH_ACTIONBAR_REFRESH_TICKS
        );
        if (task != null) {
            activeSearchTasks.put(player.getUniqueId(), task);
        }
    }

    private void tickSearch(UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        SearchProgress progress = activeSearches.get(playerId);
        if (player == null || !player.isOnline() || progress == null) {
            clearSearch(playerId);
            return;
        }

        progress.elapsedTicks++;
        sendSearchActionBar(player, progress);

        if (progress.pendingFoundLocation != null) {
            if (progress.elapsedTicks >= MIN_SEARCH_DISPLAY_TICKS) {
                stopSearch(playerId, false);
                finishSearch(player, progress.worldName, progress.pendingFoundLocation);
            }
            return;
        }

        if (progress.attemptInFlight || progress.elapsedTicks % progress.settings.attemptIntervalTicks() != 0L) {
            return;
        }

        for (int i = 0; i < SEARCH_ATTEMPTS_PER_TICK
                && hasSearchBudget(progress); i++) {
            beginAsyncLocationAttempt(playerId, progress);
        }
    }

    private void beginAsyncLocationAttempt(UUID playerId, SearchProgress progress) {
        World world = resolveWorld(progress.worldName);
        if (world == null) {
            failSearch(playerId, progress);
            return;
        }
        boolean generateFallback = shouldUseGenerateFallback(progress);
        if (!generateFallback && shouldUseLoadedChunkFallback(progress)) {
            progress.chunkSamplesUsed++;
            progress.attemptInFlight = true;
            plugin.getFoliaScheduler().runRegion(world, progress.settings.centerX() >> 4, progress.settings.centerZ() >> 4, () -> {
                try {
                    LocationAttempt attempt = tryLoadedChunkLocationAttempt(progress.settings);
                    completeAsyncLocationAttempt(playerId, progress, attempt, null);
                } catch (RuntimeException exception) {
                    completeAsyncLocationAttempt(playerId, progress, null, exception);
                }
            });
            return;
        }
        int minRadius = Math.max(0, progress.settings.minRadius());
        int maxRadius = Math.max(minRadius, progress.settings.maxRadius());
        double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
        double distance = minRadius;
        if (maxRadius > minRadius) {
            distance += ThreadLocalRandom.current().nextDouble(0, maxRadius - minRadius);
        }
        int x = progress.settings.centerX() + (int) Math.round(Math.cos(angle) * distance);
        int z = progress.settings.centerZ() + (int) Math.round(Math.sin(angle) * distance);
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        progress.chunkSamplesUsed++;
        if (generateFallback) {
            progress.generateFallbackSamplesUsed++;
        }
        progress.attemptInFlight = true;
        boolean generateChunks = plugin.getConfigManager().getRtp().getBoolean(GENERATE_CHUNKS_SETTING, false);
        boolean generateForSample = generateChunks || generateFallback;
        if (!generateForSample && !plugin.getConfigManager().getRtp().getBoolean(LOAD_GENERATED_CHUNKS_SETTING, true)) {
            completeAsyncLocationAttempt(playerId, progress, new LocationAttempt(null, false), null);
            return;
        }
        getChunkAtAsync(world, chunkX, chunkZ, generateForSample).thenAccept(chunk -> {
            plugin.getFoliaScheduler().runRegion(world, chunkX, chunkZ, () -> {
                try {
                    if (chunk == null) {
                        completeAsyncLocationAttempt(playerId, progress, new LocationAttempt(null, false), null);
                        return;
                    }
                    Location found = resolveSafeLocationInChunk(world, progress.settings, x, z, chunkX, chunkZ);
                    completeAsyncLocationAttempt(playerId, progress, new LocationAttempt(found, true), null);
                } catch (RuntimeException exception) {
                    completeAsyncLocationAttempt(playerId, progress, null, exception);
                }
            });
        }).exceptionally(throwable -> {
            plugin.getFoliaScheduler().runRegion(world, chunkX, chunkZ, () -> {
                if (generateForSample) {
                    completeAsyncLocationAttempt(playerId, progress, null, throwable);
                } else {
                    completeAsyncLocationAttempt(playerId, progress, new LocationAttempt(null, false), null);
                }
            });
            return null;
        });
    }

    private void completeAsyncLocationAttempt(UUID playerId, SearchProgress progress, LocationAttempt attempt, Throwable throwable) {
        SearchProgress activeProgress = activeSearches.get(playerId);
        if (activeProgress != progress) {
            return;
        }

        progress.attemptInFlight = false;
        if (throwable != null) {
            progress.attemptsUsed++;
            plugin.getLogger().warning("[RTPManager] async rtp chunk load failed: " + throwable.getMessage());
            if (isSearchLimitReached(progress)) {
                failSearch(playerId, progress);
            }
            return;
        }

        if (attempt != null && attempt.countedAttempt()) {
            progress.attemptsUsed++;
        }

        Location found = attempt == null ? null : attempt.location();
        if (found != null) {
            progress.pendingFoundLocation = found;
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline() && progress.elapsedTicks >= MIN_SEARCH_DISPLAY_TICKS) {
                stopSearch(playerId, false);
                finishSearch(player, progress.worldName, found);
            }
            return;
        }

        if (isSearchLimitReached(progress)) {
            failSearch(playerId, progress);
        }
    }

    private boolean isSearchLimitReached(SearchProgress progress) {
        return isFiniteLimitReached(progress.attemptsUsed, progress.settings.maxAttempts())
                || isFiniteLimitReached(progress.chunkSamplesUsed, progress.settings.maxChunkSamples());
    }

    private void failSearch(UUID playerId, SearchProgress progress) {
        boolean generateChunks = plugin.getConfigManager().getRtp().getBoolean(GENERATE_CHUNKS_SETTING, false);
        warn("rtp search failed in world '" + progress.worldName
                + "' radius " + progress.settings.minRadius() + "-" + progress.settings.maxRadius()
                + ", attempts " + progress.attemptsUsed + "/" + progress.settings.maxAttempts()
                + ", samples " + progress.chunkSamplesUsed + "/" + progress.settings.maxChunkSamples()
                + ", generatechunks=" + generateChunks
                + ", generatefallback=" + isGenerateFallbackEnabled()
                + ", fallbackgeneratedsamples=" + progress.generateFallbackSamplesUsed
                + "/" + getMaxGenerateFallbackSamples()
                + ".");

        Player player = plugin.getServer().getPlayer(playerId);
        clearSearch(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }

        String attempts = String.valueOf(progress.attemptsUsed);
        String maxAttempts = formatSearchLimit(progress.settings.maxAttempts());
        String samples = String.valueOf(progress.chunkSamplesUsed);
        String maxSamples = formatSearchLimit(progress.settings.maxChunkSamples());
        String maxAttemptsMessage = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.MAX-ATTEMPTS", "&cᴄᴏᴜʟᴅ ɴᴏᴛ ꜰɪɴᴅ ᴀ ѕᴀꜰᴇ ʟᴏᴄᴀᴛɪᴏɴ ᴀꜰᴛᴇʀ %attempts% ᴀᴛᴛᴇᴍᴘᴛѕ.")
                .replace("%attempts%", attempts)
                .replace("{attempts}", attempts)
                .replace("%max_attempts%", maxAttempts)
                .replace("{max_attempts}", maxAttempts)
                .replace("%samples%", samples)
                .replace("{samples}", samples)
                .replace("%max_samples%", maxSamples)
                .replace("{max_samples}", maxSamples);
        String sampleRatio = samples + "/" + maxSamples;
        if (!maxAttemptsMessage.contains(sampleRatio)) {
            maxAttemptsMessage += " &7(attempts: &f" + attempts + "/" + maxAttempts
                    + "&7, samples: &f" + sampleRatio + "&7)";
        }
        player.sendMessage(ColorUtils.toComponent(maxAttemptsMessage));
        SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-FAIL"));
    }

    private void logSearchFailure(
            SearchSettings settings,
            int attemptsUsed,
            int chunkSamplesUsed,
            int generateFallbackSamplesUsed
    ) {
        boolean generateChunks = plugin.getConfigManager().getRtp().getBoolean(GENERATE_CHUNKS_SETTING, false);
        warn("rtp search failed in world '" + settings.worldName()
                + "' radius " + settings.minRadius() + "-" + settings.maxRadius()
                + ", attempts " + attemptsUsed + "/" + settings.maxAttempts()
                + ", samples " + chunkSamplesUsed + "/" + settings.maxChunkSamples()
                + ", generatechunks=" + generateChunks
                + ", generatefallback=" + isGenerateFallbackEnabled()
                + ", fallbackgeneratedsamples=" + generateFallbackSamplesUsed
                + "/" + getMaxGenerateFallbackSamples()
                + ".");
    }

    private void finishSearch(Player player, String worldName, Location found) {
        applyCooldown(player.getUniqueId(), worldName);

        String foundMessage = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.SAFE-LOCATION-FOUND", "&aѕᴀꜰᴇ ʟᴏᴄᴀᴛɪᴏɴ ꜰᴏᴜɴᴅ ᴀᴛ: x:{x} ʏ:{y} ᴢ:{z}")
                .replace("{x}", String.valueOf(found.getBlockX()))
                .replace("{y}", String.valueOf(found.getBlockY()))
                .replace("{z}", String.valueOf(found.getBlockZ()));
        if (!PlayerSettingUtils.rtpCoordinatesEnabled(plugin, player)) {
            foundMessage = plugin.getConfigManager().getRtp().getString(
                    "MESSAGES.SAFE-LOCATION-FOUND-HIDDEN",
                    "&aѕᴀꜰᴇ ʟᴏᴄᴀᴛɪᴏɴ ꜰᴏᴜɴᴅ."
            );
        }
        player.sendMessage(ColorUtils.toComponent(foundMessage));
        sendTeleportWarning(player, worldName);
        SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-FOUND"));
        UUID playerId = player.getUniqueId();
        CompletableFuture<Void> preloadFuture = preloadTeleportChunks(found);

        final long[] shownTicks = {0L};
        final boolean[] queuedTeleport = {false};
        final ScheduledTask[] resultTaskRef = new ScheduledTask[1];
        ScheduledTask resultTask = plugin.getFoliaScheduler().runEntityTimer(player, () -> {
            if (!player.isOnline()) {
                activeResultTasks.remove(playerId);
                preloadFuture.cancel(false);
                if (resultTaskRef[0] != null) {
                    resultTaskRef[0].cancel();
                }
                return;
            }

            sendFoundActionBar(player, worldName, found, shownTicks[0]);
            shownTicks[0]++;

            if (shownTicks[0] >= FOUND_ACTIONBAR_DELAY_TICKS && preloadFuture.isDone()) {
                queuePreparedRtpTeleport(player, found, playerId, resultTaskRef, queuedTeleport);
            }
        }, 1L, SEARCH_ACTIONBAR_REFRESH_TICKS);
        resultTaskRef[0] = resultTask;
        if (resultTask != null) {
            activeResultTasks.put(playerId, resultTask);
        }
    }

    private void queuePreparedRtpTeleport(
            Player player,
            Location found,
            UUID playerId,
            ScheduledTask[] resultTaskRef,
            boolean[] queuedTeleport
    ) {
        if (queuedTeleport[0]) {
            return;
        }
        queuedTeleport[0] = true;
        activeResultTasks.remove(playerId);
        if (resultTaskRef[0] != null) {
            resultTaskRef[0].cancel();
        }
        if (player.isOnline()) {
            plugin.getTeleportManager().queue(player, found, "RTP", null);
        }
    }

    private void sendSearchActionBar(Player player, SearchProgress progress) {
        long displayedSeconds = getDisplayedSearchSeconds(progress.elapsedTicks);

        String actionBar = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.SEARCH-ACTIONBAR", "&7ѕᴇᴀʀᴄʜɪɴɢ {world}... &b{elapsed}ѕ");
        actionBar = stripSearchCounter(actionBar)
                .replace("{world}", describeWorld(progress.worldName))
                .replace("{elapsed}", formatElapsedSeconds(progress.elapsedTicks))
                .replace("{attempts}", String.valueOf(progress.attemptsUsed))
                .replace("{max_attempts}", formatSearchLimit(progress.settings.maxAttempts()))
                .replace("{samples}", String.valueOf(progress.chunkSamplesUsed))
                .replace("{max_samples}", formatSearchLimit(progress.settings.maxChunkSamples()));

        sendPersistentActionBar(player, actionBar, progress.elapsedTicks);
        if (displayedSeconds > progress.lastElapsedSecond) {
            progress.lastElapsedSecond = displayedSeconds;
            SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-TICK"));
        }
    }

    private void sendFoundActionBar(Player player, String worldName, Location found) {
        sendFoundActionBar(player, worldName, found, 0L);
    }

    private void sendFoundActionBar(Player player, String worldName, Location found, long refreshTick) {
        String actionBar = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.SEARCH-FOUND-ACTIONBAR", "&aѕᴀꜰᴇ ʟᴏᴄᴀᴛɪᴏɴ ꜰᴏᴜɴᴅ ɪɴ {world}! &7ᴘʀᴇᴘᴀʀɪɴɢ ᴛᴇʟᴇᴘᴏʀᴛ...")
                .replace("{world}", describeWorld(worldName))
                .replace("{x}", String.valueOf(found.getBlockX()))
                .replace("{y}", String.valueOf(found.getBlockY()))
                .replace("{z}", String.valueOf(found.getBlockZ()));
        if (!PlayerSettingUtils.rtpCoordinatesEnabled(plugin, player)) {
            actionBar = plugin.getConfigManager().getRtp().getString(
                    "MESSAGES.SEARCH-FOUND-ACTIONBAR-HIDDEN",
                    "&aѕᴀꜰᴇ ʟᴏᴄᴀᴛɪᴏɴ ꜰᴏᴜɴᴅ ɪɴ {world}! &7ᴘʀᴇᴘᴀʀɪɴɢ ᴛᴇʟᴇᴘᴏʀᴛ..."
            ).replace("{world}", describeWorld(worldName));
        }
        sendPersistentActionBar(player, actionBar, refreshTick);
    }

    private LocationAttempt tryFindSafeLocationAttempt(SearchSettings settings) {
        if (settings == null || settings.worldName() == null || settings.worldName().isBlank()) {
            return new LocationAttempt(null, false);
        }

        World world = resolveWorld(settings.worldName());
        if (world == null) {
            return new LocationAttempt(null, false);
        }

        int minRadius = Math.max(0, settings.minRadius());
        int maxRadius = Math.max(minRadius, settings.maxRadius());

        double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
        double distance = minRadius;
        if (maxRadius > minRadius) {
            distance += ThreadLocalRandom.current().nextDouble(0, maxRadius - minRadius);
        }

        int x = settings.centerX() + (int) Math.round(Math.cos(angle) * distance);
        int z = settings.centerZ() + (int) Math.round(Math.sin(angle) * distance);
        return tryResolveSafeLocation(world, x, z, x >> 4, z >> 4);
    }

    private LocationAttempt tryResolveSafeLocation(
            World world,
            int x,
            int z,
            int chunkX,
            int chunkZ
    ) {
        if (!prepareChunkForRtp(world, chunkX, chunkZ)) {
            return new LocationAttempt(null, false);
        }
        return new LocationAttempt(resolveSafeLocation(world, x, z), true);
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Chunk> getChunkAtAsync(World world, int chunkX, int chunkZ, boolean gen) {
        try {
            Method method = world.getClass().getMethod("getChunkAtAsync", int.class, int.class, boolean.class);
            if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
                return (CompletableFuture<Chunk>) method.invoke(world, chunkX, chunkZ, gen);
            }
        } catch (Exception ignored) {
        }
        try {
            Method method = world.getClass().getMethod("getChunkAtAsync", int.class, int.class, boolean.class, java.util.function.Consumer.class);
            CompletableFuture<Chunk> future = new CompletableFuture<>();
            method.invoke(world, chunkX, chunkZ, gen, (java.util.function.Consumer<Chunk>) chunk -> future.complete(chunk));
            return future;
        } catch (Exception ignored) {
        }
        try {
            Method method = world.getClass().getMethod("getChunkAtAsync", int.class, int.class, boolean.class, boolean.class, java.util.function.Consumer.class);
            CompletableFuture<Chunk> future = new CompletableFuture<>();
            method.invoke(world, chunkX, chunkZ, gen, false, (java.util.function.Consumer<Chunk>) chunk -> future.complete(chunk));
            return future;
        } catch (Exception ignored) {
        }
        if (!gen) {
            return CompletableFuture.completedFuture(null);
        }
        try {
            Method method = world.getClass().getMethod("getChunkAtAsync", int.class, int.class);
            if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
                return (CompletableFuture<Chunk>) method.invoke(world, chunkX, chunkZ);
            }
        } catch (Exception ignored) {
        }
        try {
            Method method = world.getClass().getMethod("getChunkAtAsync", int.class, int.class, java.util.function.Consumer.class);
            CompletableFuture<Chunk> future = new CompletableFuture<>();
            method.invoke(world, chunkX, chunkZ, (java.util.function.Consumer<Chunk>) chunk -> future.complete(chunk));
            return future;
        } catch (Exception ignored) {
        }
        CompletableFuture<Chunk> future = new CompletableFuture<>();
        plugin.getFoliaScheduler().runRegion(world, chunkX, chunkZ, () -> {
            try {
                Chunk chunk = world.loadChunk(chunkX, chunkZ, gen) ? world.getChunkAt(chunkX, chunkZ) : null;
                future.complete(chunk);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private boolean prepareChunkForRtp(World world, int chunkX, int chunkZ) {
        if (world.isChunkLoaded(chunkX, chunkZ)) {
            return true;
        }

        boolean generateChunks = plugin.getConfigManager().getRtp()
                .getBoolean(GENERATE_CHUNKS_SETTING, false);
        if (!generateChunks && !plugin.getConfigManager().getRtp()
                .getBoolean(LOAD_GENERATED_CHUNKS_SETTING, true)) {
            return false;
        }
        if (!generateChunks) {
            return false;
        }

        return world.loadChunk(chunkX, chunkZ, true);
    }

    private CompletableFuture<Void> preloadTeleportChunks(Location destination) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (!plugin.getConfigManager().getRtp().getBoolean(PRELOAD_TELEPORT_CHUNKS_SETTING, true)
                || destination == null
                || destination.getWorld() == null) {
            future.complete(null);
            return future;
        }

        World world = destination.getWorld();
        int centerChunkX = destination.getBlockX() >> 4;
        int centerChunkZ = destination.getBlockZ() >> 4;
        List<int[]> chunks = buildPreloadChunkOrder(centerChunkX, centerChunkZ, getPreloadRadius());
        if (chunks.isEmpty()) {
            future.complete(null);
            return future;
        }

        int chunksPerTick = getPreloadChunksPerTick();
        int minimumTicksForFullWarmup = (int) Math.ceil((double) chunks.size() / chunksPerTick) + 10;
        int maxTicks = Math.max(getPreloadMaxTicks(), minimumTicksForFullWarmup);
        AtomicInteger nextIndex = new AtomicInteger();
        AtomicInteger pendingLoads = new AtomicInteger();
        AtomicInteger elapsedTicks = new AtomicInteger();
        final ScheduledTask[] taskRef = new ScheduledTask[1];

        Runnable complete = () -> {
            if (future.complete(null) && taskRef[0] != null) {
                taskRef[0].cancel();
            }
        };

        taskRef[0] = plugin.getFoliaScheduler().runGlobalTimer(() -> {
            if (future.isDone()) {
                if (taskRef[0] != null) {
                    taskRef[0].cancel();
                }
                return;
            }

            if (elapsedTicks.incrementAndGet() > maxTicks) {
                complete.run();
                return;
            }

            int scheduled = 0;
            while (scheduled < chunksPerTick) {
                int index = nextIndex.getAndIncrement();
                if (index >= chunks.size()) {
                    break;
                }
                int[] chunk = chunks.get(index);
                scheduled++;
                pendingLoads.incrementAndGet();
                getChunkAtAsync(world, chunk[0], chunk[1], false).whenComplete((chunkResult, throwable) -> {
                    if (pendingLoads.decrementAndGet() <= 0 && nextIndex.get() >= chunks.size()) {
                        complete.run();
                    }
                });
            }

            if (nextIndex.get() >= chunks.size() && pendingLoads.get() <= 0) {
                complete.run();
            }
        }, 1L, 1L);

        if (taskRef[0] == null) {
            complete.run();
        }
        future.whenComplete((ignored, throwable) -> {
            if (taskRef[0] != null) {
                taskRef[0].cancel();
            }
        });
        return future;
    }

    private List<int[]> buildPreloadChunkOrder(int centerChunkX, int centerChunkZ, int radius) {
        List<int[]> chunks = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                chunks.add(new int[]{centerChunkX + dx, centerChunkZ + dz});
            }
        }
        chunks.sort(Comparator.comparingInt(chunk ->
                Math.abs(chunk[0] - centerChunkX) + Math.abs(chunk[1] - centerChunkZ)));
        return chunks;
    }

    private int getPreloadRadius() {
        int configured = plugin.getConfigManager().getRtp().getInt(PRELOAD_RADIUS_SETTING, 1);
        int radius = Math.max(0, Math.min(4, configured));
        if (plugin.getConfigManager().getRtp().getBoolean(POST_TELEPORT_CHUNK_THROTTLE_SETTING, true)) {
            int throttledViewDistance = Math.max(2, plugin.getConfigManager().getRtp()
                    .getInt(POST_TELEPORT_VIEW_DISTANCE_SETTING, 4));
            radius = Math.max(radius, Math.min(4, throttledViewDistance));
        }
        return Math.max(2, radius);
    }

    private int getPreloadChunksPerTick() {
        return Math.max(2, plugin.getConfigManager().getRtp().getInt(PRELOAD_CHUNKS_PER_TICK_SETTING, 1));
    }

    private int getPreloadMaxTicks() {
        return Math.max(1, plugin.getConfigManager().getRtp().getInt(PRELOAD_MAX_TICKS_SETTING, 40));
    }

    private LocationAttempt tryLoadedChunkLocationAttempt(SearchSettings settings) {
        if (settings == null || settings.worldName() == null || settings.worldName().isBlank()) {
            return new LocationAttempt(null, false);
        }

        World world = resolveWorld(settings.worldName());
        if (world == null) {
            return new LocationAttempt(null, false);
        }

        Chunk[] loadedChunks = world.getLoadedChunks();
        if (loadedChunks.length == 0) {
            return new LocationAttempt(null, false);
        }

        int start = ThreadLocalRandom.current().nextInt(loadedChunks.length);
        int checked = 0;
        int maxChecks = Math.min(loadedChunks.length, 32);
        for (int offset = 0; offset < loadedChunks.length && checked < maxChecks; offset++) {
            Chunk chunk = loadedChunks[(start + offset) % loadedChunks.length];
            int baseX = chunk.getX() << 4;
            int baseZ = chunk.getZ() << 4;
            int x = baseX + ThreadLocalRandom.current().nextInt(16);
            int z = baseZ + ThreadLocalRandom.current().nextInt(16);
            checked++;

            Location found = resolveSafeLocation(world, x, z);
            if (found == null) {
                continue;
            }
            if (isWithinRadius(settings, found, true)) {
                return new LocationAttempt(found, true);
            }
        }

        return new LocationAttempt(null, true);
    }

    private boolean isWithinRadius(SearchSettings settings, Location location, boolean requireMinimum) {
        double dx = location.getX() - settings.centerX();
        double dz = location.getZ() - settings.centerZ();
        double distanceSquared = dx * dx + dz * dz;
        double maxRadius = Math.max(settings.minRadius(), settings.maxRadius());
        if (distanceSquared > maxRadius * maxRadius) {
            return false;
        }
        if (!requireMinimum) {
            return true;
        }
        double minRadius = Math.max(0, settings.minRadius());
        return distanceSquared >= minRadius * minRadius;
    }

    private boolean shouldUseLoadedChunkFallback(SearchProgress progress) {
        return shouldUseLoadedChunkFallback(progress.chunkSamplesUsed);
    }

    private boolean shouldUseLoadedChunkFallback(int chunkSamplesUsed) {
        FileConfiguration rtp = plugin.getConfigManager().getRtp();
        if (!rtp.getBoolean(LOADED_CHUNK_FALLBACK_SETTING, true)) {
            return false;
        }

        int afterSamples = Math.max(1, rtp.getInt(LOADED_CHUNK_FALLBACK_AFTER_SETTING, 32));
        return chunkSamplesUsed >= afterSamples;
    }

    private boolean shouldUseGenerateFallback(SearchProgress progress) {
        return shouldUseGenerateFallback(progress.chunkSamplesUsed, progress.generateFallbackSamplesUsed);
    }

    private boolean shouldUseGenerateFallback(int chunkSamplesUsed, int generateFallbackSamplesUsed) {
        if (!isGenerateFallbackEnabled()) {
            return false;
        }
        if (plugin.getConfigManager().getRtp().getBoolean(GENERATE_CHUNKS_SETTING, false)) {
            return false;
        }

        int maxFallbackSamples = getMaxGenerateFallbackSamples();
        if (maxFallbackSamples <= 0 || generateFallbackSamplesUsed >= maxFallbackSamples) {
            return false;
        }

        return chunkSamplesUsed >= getGenerateFallbackAfterSamples();
    }

    private boolean isGenerateFallbackEnabled() {
        return plugin.getConfigManager().getRtp().getBoolean(GENERATE_FALLBACK_CHUNKS_SETTING, true);
    }

    private int getGenerateFallbackAfterSamples() {
        return Math.max(1, plugin.getConfigManager().getRtp()
                .getInt(GENERATE_FALLBACK_AFTER_SETTING, DEFAULT_GENERATE_FALLBACK_AFTER_SAMPLES));
    }

    private int getMaxGenerateFallbackSamples() {
        return Math.max(0, plugin.getConfigManager().getRtp()
                .getInt(MAX_GENERATE_FALLBACK_SAMPLES_SETTING, DEFAULT_MAX_GENERATE_FALLBACK_SAMPLES));
    }

    private Location resolveSafeLocation(World world, int x, int z) {
        if (world.getEnvironment() == World.Environment.NETHER) {
            return resolveNetherSafeLocation(world, x, z);
        }

        int y = world.getHighestBlockYAt(x, z);
        if (!isSafeStandLocation(world, x, y, z)) {
            return null;
        }
        return new Location(world, x + 0.5, y + 1.0, z + 0.5);
    }

    private Location resolveSafeLocationInChunk(
            World world,
            SearchSettings settings,
            int preferredX,
            int preferredZ,
            int chunkX,
            int chunkZ
    ) {
        Location preferred = resolveSafeLocationWithinRadius(world, settings, preferredX, preferredZ);
        if (preferred != null) {
            return preferred;
        }

        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        for (int check = 1; check < CHUNK_COLUMN_CHECKS; check++) {
            int x = baseX + ThreadLocalRandom.current().nextInt(16);
            int z = baseZ + ThreadLocalRandom.current().nextInt(16);
            Location found = resolveSafeLocationWithinRadius(world, settings, x, z);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private Location resolveSafeLocationWithinRadius(World world, SearchSettings settings, int x, int z) {
        Location found = resolveSafeLocation(world, x, z);
        if (found == null || !isWithinRadius(settings, found, true)) {
            return null;
        }
        return found;
    }

    private Location resolveNetherSafeLocation(World world, int x, int z) {
        int minGroundY = world.getMinHeight();
        int logicalTopY = Math.min(world.getLogicalHeight(), world.getMaxHeight()) - 1;
        int maxGroundY = Math.min(
                world.getMaxHeight() - 1 - PLAYER_CLEARANCE_BLOCKS,
                logicalTopY - NETHER_ROOF_PADDING_BLOCKS
        );

        if (maxGroundY < minGroundY) {
            maxGroundY = world.getMaxHeight() - 1 - PLAYER_CLEARANCE_BLOCKS;
        }

        for (int groundY = maxGroundY; groundY >= minGroundY; groundY--) {
            if (isSafeStandLocation(world, x, groundY, z)) {
                return new Location(world, x + 0.5, groundY + 1.0, z + 0.5);
            }
        }

        return null;
    }

    private List<RTPDestination> loadConfiguredDestinations() {
        FileConfiguration rtp = plugin.getConfigManager().getRtp();
        ConfigurationSection buttons = rtp.getConfigurationSection("RTP-MENU.BUTTONS");
        if (buttons == null) {
            return List.of();
        }

        List<RTPDestination> loaded = new ArrayList<>();
        for (String key : buttons.getKeys(false)) {
            ConfigurationSection button = buttons.getConfigurationSection(key);
            if (button == null) {
                continue;
            }

            String worldName = button.getString("WORLD", "").trim();
            if (worldName.isBlank()) {
                warn("RTP-MENU.BUTTONS." + key + " is missing world.");
                continue;
            }

            loaded.add(new RTPDestination(
                    key,
                    button.getInt("SLOT", -1),
                    ItemUtils.parseMaterial(button.getString("MATERIAL", "GRASS_BLOCK")),
                    button.getString("DISPLAY-NAME", key),
                    button.getStringList("LORE"),
                    worldName,
                    button.getBoolean("ENABLED", true)
            ));
        }

        return List.copyOf(loaded);
    }

    private List<RTPDestination> buildMenuDestinations(List<RTPDestination> configured) {
        FileConfiguration rtp = plugin.getConfigManager().getRtp();
        int menuSize = normalizeSize(rtp.getInt("RTP-MENU.SIZE", 27));
        Set<Integer> usedSlots = new HashSet<>();
        List<RTPDestination> visible = new ArrayList<>();

        for (RTPDestination destination : configured) {
            if (!destination.enabled()) {
                continue;
            }

            if (destination.slot() < 0 || destination.slot() >= menuSize) {
                warn("rtp destination '" + destination.id() + "' uses invalid slot " + destination.slot()
                        + " for menu size " + menuSize + ".");
                continue;
            }

            if (!usedSlots.add(destination.slot())) {
                warn("rtp destination '" + destination.id() + "' collides with another rtp button on slot "
                        + destination.slot() + ".");
                continue;
            }

            if (!hasWorldSearchSettings(destination.worldName())) {
                warn("rtp destination '" + destination.id() + "' points to world '" + destination.worldName()
                        + "' without world-settings.");
                continue;
            }

            if (!isWorldAvailable(destination.worldName())) {
                warn("rtp destination '" + destination.id() + "' points to missing world '" + destination.worldName() + "'.");
                continue;
            }

            visible.add(destination);
        }

        visible.sort(Comparator.comparingInt(RTPDestination::slot));
        return List.copyOf(visible);
    }

    public String resolveWorldSelector(String selector) {
        if (selector == null) {
            return null;
        }

        String trimmed = selector.trim();
        if (trimmed.isBlank()) {
            return null;
        }

        for (RTPDestination destination : configuredDestinations) {
            if (destination.id().equalsIgnoreCase(trimmed)) {
                return destination.worldName();
            }
        }

        return switch (trimmed.toLowerCase(Locale.ROOT)) {
            case "overworld" -> getLoadedNormalWorldName();
            case "nether" -> getLoadedNetherWorldName();
            case "end", "the_end", "the-end" -> getLoadedEndWorldName();
            default -> trimmed;
        };
    }

    private boolean isConfiguredDestinationDisabled(String worldName) {
        boolean hasConfiguredDestination = false;
        for (RTPDestination destination : configuredDestinations) {
            if (!destination.worldName().equalsIgnoreCase(worldName)) {
                continue;
            }
            hasConfiguredDestination = true;
            if (destination.enabled()) {
                return false;
            }
        }
        return hasConfiguredDestination;
    }

    private boolean isDeniedWorld(String worldName) {
        for (String deniedWorld : plugin.getConfigManager().getRtp().getStringList("DENIED-WORLDS")) {
            if (deniedWorld.equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasWorldSearchSettings(String worldName) {
        return getWorldSettingsSection(worldName) != null;
    }

    private ConfigurationSection getWorldSettingsSection(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return null;
        }

        ConfigurationSection worlds = plugin.getConfigManager().getRtp().getConfigurationSection("WORLD-SETTINGS");
        if (worlds == null) {
            return null;
        }

        ConfigurationSection exact = worlds.getConfigurationSection(worldName);
        if (exact != null) {
            return exact;
        }

        for (String key : worlds.getKeys(false)) {
            if (key.equalsIgnoreCase(worldName)) {
                return worlds.getConfigurationSection(key);
            }
        }

        World worldObj = Bukkit.getWorld(worldName);
        if (worldObj != null) {
            if (worldObj.getEnvironment() == World.Environment.NETHER) {
                ConfigurationSection fallback = worlds.getConfigurationSection("world_nether");
                if (fallback != null) return fallback;
            } else if (worldObj.getEnvironment() == World.Environment.THE_END) {
                ConfigurationSection fallback = worlds.getConfigurationSection("world_the_end");
                if (fallback != null) return fallback;
            } else {
                ConfigurationSection fallback = worlds.getConfigurationSection("world");
                if (fallback != null) return fallback;
            }
        }

        return null;
    }

    private World getLoadedWorld(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return null;
        }

        World exact = Bukkit.getWorld(worldName);
        if (exact != null) {
            return exact;
        }

        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equalsIgnoreCase(worldName.trim())) {
                return world;
            }
        }
        return null;
    }

    private World resolveWorld(String worldName) {
        World loaded = getLoadedWorld(worldName);
        if (loaded != null) {
            return loaded;
        }

        String folderWorldName = findWorldFolderName(worldName);
        if (folderWorldName == null) {
            return null;
        }

        try {
            return WorldCreator.name(folderWorldName)
                    .environment(inferWorldEnvironment(folderWorldName))
                    .createWorld();
        } catch (RuntimeException exception) {
            warn("failed to load rtp world '" + folderWorldName + "': " + exception.getMessage());
            return null;
        }
    }

    private String getLoadedNetherWorldName() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NETHER) {
                return world.getName();
            }
        }
        return "world_nether";
    }

    private String getLoadedEndWorldName() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.THE_END) {
                return world.getName();
            }
        }
        return "world_the_end";
    }

    private String getLoadedNormalWorldName() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                String name = world.getName();
                if (!name.equalsIgnoreCase("afk") && !name.equalsIgnoreCase("duels") && !name.equalsIgnoreCase("lobby")) {
                    return name;
                }
            }
        }
        return "world";
    }

    private boolean isWorldAvailable(String worldName) {
        return getLoadedWorld(worldName) != null || findWorldFolderName(worldName) != null;
    }

    private String findWorldFolderName(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return null;
        }

        String trimmed = worldName.trim();
        File worldContainer = Bukkit.getWorldContainer();
        File exactFolder = new File(worldContainer, trimmed);
        if (exactFolder.isDirectory()) {
            return exactFolder.getName();
        }

        File[] folders = worldContainer.listFiles(File::isDirectory);
        if (folders == null) {
            return null;
        }

        for (File folder : folders) {
            if (folder.getName().equalsIgnoreCase(trimmed)) {
                return folder.getName();
            }
        }
        return null;
    }

    private World.Environment inferWorldEnvironment(String worldName) {
        String lower = worldName.toLowerCase(Locale.ROOT);
        if (lower.endsWith("_nether") || lower.equals("nether")) {
            return World.Environment.NETHER;
        }
        if (lower.endsWith("_the_end") || lower.equals("the_end") || lower.equals("end")) {
            return World.Environment.THE_END;
        }
        return World.Environment.NORMAL;
    }

    private String normalizeConfiguredWorldName(String worldName) {
        String trimmed = worldName == null ? "" : worldName.trim();
        if (trimmed.isBlank()) {
            return "world";
        }

        String ascii = trimmed
                .replace('w', 'w')
                .replace('o', 'o')
                .replace('r', 'r')
                .replace('l', 'l')
                .replace('d', 'd')
                .replace('n', 'n')
                .replace('e', 'e')
                .replace('t', 't')
                .replace('h', 'h')
                .replace('a', 'a')
                .replace('s', 's');
        return switch (ascii.toLowerCase(Locale.ROOT)) {
            case "overworld", "world" -> getLoadedNormalWorldName();
            case "nether", "world_nether" -> getLoadedNetherWorldName();
            case "end", "the_end", "the-end", "world_the_end" -> getLoadedEndWorldName();
            default -> trimmed;
        };
    }

    private int normalizeSearchLimit(int limit) {
        return limit <= 0 ? DEFAULT_MAX_ATTEMPTS : Math.max(MIN_MAX_ATTEMPTS, limit);
    }

    private int normalizeChunkSampleLimit(int maxChunkSamples) {
        return maxChunkSamples <= 0 ? DEFAULT_MAX_CHUNK_SAMPLES : Math.max(MIN_MAX_CHUNK_SAMPLES, maxChunkSamples);
    }

    private int normalizeAttemptInterval(int attemptIntervalTicks) {
        return Math.max(DEFAULT_ATTEMPT_INTERVAL_TICKS, attemptIntervalTicks);
    }

    private boolean hasSearchBudget(SearchProgress progress) {
        return hasAttemptBudget(progress.attemptsUsed, progress.settings)
                && hasChunkSampleBudget(progress.chunkSamplesUsed, progress.settings);
    }

    private boolean hasAttemptBudget(int attemptsUsed, SearchSettings settings) {
        return attemptsUsed < settings.maxAttempts();
    }

    private boolean hasChunkSampleBudget(int chunkSamplesUsed, SearchSettings settings) {
        return chunkSamplesUsed < settings.maxChunkSamples();
    }

    private boolean isFiniteLimitReached(int used, int limit) {
        return limit > 0 && used >= limit;
    }

    private String formatSearchLimit(int limit) {
        return String.valueOf(Math.max(0, limit));
    }

    private String stripSearchCounter(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace(" &8(&f{attempts}/{max_attempts}&8)", "")
                .replace("&8(&f{attempts}/{max_attempts}&8)", "")
                .replace(" &8(&f{attempts} checks&8)", "")
                .replace("&8(&f{attempts} checks&8)", "")
                .replace(" ({attempts}/{max_attempts})", "")
                .replace("{attempts}/{max_attempts}", "")
                .trim();
    }

    private long getCooldownRemainingMillis(UUID playerId, String worldName) {
        Map<String, Long> cooldowns = cooldownsByPlayer.get(playerId);
        if (cooldowns == null) {
            return 0L;
        }

        String key = normalizeWorldKey(worldName);
        Long expiresAt = cooldowns.get(key);
        if (expiresAt == null) {
            return 0L;
        }

        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining <= 0L) {
            cooldowns.remove(key);
            if (cooldowns.isEmpty()) {
                cooldownsByPlayer.remove(playerId);
            }
            return 0L;
        }
        return remaining;
    }

    private void applyCooldown(UUID playerId, String worldName) {
        int cooldownSeconds = getWorldCooldownSeconds(worldName);
        if (cooldownSeconds <= 0) {
            return;
        }

        cooldownsByPlayer.computeIfAbsent(playerId, ignored -> new HashMap<>())
                .put(normalizeWorldKey(worldName), System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }

    private boolean isQueueFull(UUID playerId) {
        int maxPlayers = getMaxConcurrentRtp();
        int inProgress = countActiveSearches() + plugin.getTeleportManager().countPendingByType("RTP");
        if (isSearching(playerId)) {
            inProgress = Math.max(0, inProgress - 1);
        }
        if (plugin.getTeleportManager().hasPendingType(playerId, "RTP")) {
            inProgress = Math.max(0, inProgress - 1);
        }
        return inProgress >= maxPlayers;
    }

    private int getMaxConcurrentRtp() {
        int configured = plugin.getConfigManager().getRtp()
                .getInt("SETTINGS.PLAYERS-IN-RTP", DEFAULT_MAX_CONCURRENT_RTP);
        return configured <= 0 ? DEFAULT_MAX_CONCURRENT_RTP : configured;
    }

    private int countActiveSearches() {
        return activeSearches.size() + activeResultTasks.size() + activeDirectSearches.size();
    }

    private boolean isSearching(UUID playerId) {
        return activeSearches.containsKey(playerId) || activeDirectSearches.containsKey(playerId);
    }

    private boolean hasActiveRtpFlow(UUID playerId) {
        return activeSearches.containsKey(playerId)
                || activeResultTasks.containsKey(playerId)
                || activeDirectSearches.containsKey(playerId);
    }

    private boolean isSafe(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z);
        if (y <= world.getMinHeight()) {
            return false;
        }

        Block top = world.getBlockAt(x, y, z);
        String typeName = top.getType().name();
        return !typeName.contains("WATER")
                && !typeName.contains("LAVA")
                && !typeName.contains("VOID");
    }

    private boolean isSafeStandLocation(World world, int x, int groundY, int z) {
        if (groundY < world.getMinHeight() || groundY + PLAYER_CLEARANCE_BLOCKS >= world.getMaxHeight()) {
            return false;
        }

        Block ground = world.getBlockAt(x, groundY, z);
        Block feet = world.getBlockAt(x, groundY + 1, z);
        Block head = world.getBlockAt(x, groundY + 2, z);

        return isSafeGround(ground.getType())
                && isSafeBodySpace(feet)
                && isSafeBodySpace(head);
    }

    private boolean isSafeGround(Material material) {
        return material != null
                && material.isSolid()
                && material != Material.BEDROCK
                && !isHazardous(material);
    }

    private boolean isSafeBodySpace(Block block) {
        return block.isPassable() && !isHazardous(block.getType());
    }

    private boolean isHazardous(Material material) {
        if (material == null) {
            return true;
        }

        String typeName = material.name();
        return typeName.contains("LAVA")
                || typeName.contains("FIRE")
                || typeName.contains("MAGMA")
                || typeName.contains("CACTUS")
                || typeName.contains("CAMPFIRE")
                || typeName.contains("SWEET_BERRY_BUSH")
                || typeName.contains("POWDER_SNOW")
                || typeName.contains("VOID");
    }

    private int normalizeSize(int size) {
        int normalized = Math.max(9, ((size + 8) / 9) * 9);
        return Math.min(54, normalized);
    }

    private String normalizeWorldKey(String worldName) {
        return worldName.toLowerCase(Locale.ROOT);
    }

    private String formatElapsedSeconds(long elapsedTicks) {
        return String.valueOf(getDisplayedSearchSeconds(elapsedTicks));
    }

    private long getDisplayedSearchSeconds(long elapsedTicks) {
        return Math.max(1L, (long) Math.ceil(Math.max(0D, elapsedTicks / 20.0D)));
    }

    private void sendTeleportWarning(Player player, String worldName) {
        int teleportCountdown = plugin.getConfigManager().getConfig().getInt("TELEPORT-COOLDOWN.RTP", 5);
        String warning = plugin.getConfigManager().getRtp()
                .getString(
                        "MESSAGES.TP-WARNING",
                        "&eᴅᴏ ɴᴏᴛ ᴍᴏᴠᴇ ꜰᴏʀ &b{countdown}&e ѕᴇᴄᴏɴᴅѕ ᴏʀ ᴛʜᴇ ᴛᴇʟᴇᴘᴏʀᴛ ᴡɪʟʟ ʙᴇ ᴄᴀɴᴄᴇʟᴇᴅ."
                )
                .replace("{world}", describeWorld(worldName))
                .replace("{countdown}", String.valueOf(teleportCountdown));
        if (!warning.isBlank()) {
            player.sendMessage(ColorUtils.toComponent(warning));
        }
    }

    private void sendPersistentActionBar(Player player, String text, long refreshTick) {
        PlayerSettingUtils.sendActionBar(plugin, player, ColorUtils.toComponent(text));
    }

    private void clearAllSearches() {
        Set<UUID> playerIds = new HashSet<>();
        playerIds.addAll(activeSearches.keySet());
        playerIds.addAll(activeSearchTasks.keySet());
        playerIds.addAll(activeResultTasks.keySet());
        playerIds.addAll(activeDirectSearches.keySet());
        for (UUID playerId : playerIds) {
            clearSearch(playerId);
        }
    }

    private void warn(String message) {
        plugin.getLogger().warning("[RTPManager] " + message);
    }
}
