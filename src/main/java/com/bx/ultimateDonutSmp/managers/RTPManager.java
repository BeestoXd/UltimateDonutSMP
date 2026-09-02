package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import com.bx.ultimateDonutSmp.utils.RtpChunkPreloadPolicy;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.scheduler.BukkitTask;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    private static final long DEFAULT_MIN_SEARCH_DISPLAY_TICKS = 30L;
    private static final int DEFAULT_SEARCH_ATTEMPTS_PER_TICK = 1;
    private static final long DEFAULT_FOUND_DISPLAY_TICKS = 20L;
    private static final int DEFAULT_MAX_CONCURRENT_RTP = 1;
    private static final int MIN_MAX_ATTEMPTS = 32;
    private static final int MIN_MAX_CHUNK_SAMPLES = 64;
    private static final int DEFAULT_MAX_ATTEMPTS = 64;
    private static final int DEFAULT_MAX_CHUNK_SAMPLES = 128;
    private static final int DEFAULT_ATTEMPT_INTERVAL_TICKS = 2;
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
    private static final String MIN_SEARCH_DISPLAY_TICKS_SETTING = "SETTINGS.SEARCH-DISPLAY-MIN-TICKS";
    private static final String FOUND_DISPLAY_TICKS_SETTING = "SETTINGS.FOUND-DISPLAY-TICKS";
    private static final String PRELOAD_TELEPORT_CHUNKS_SETTING = "SETTINGS.PRELOAD-TELEPORT-CHUNKS";
    private static final String PRELOAD_MAX_TICKS_SETTING = "SETTINGS.PRELOAD-MAX-TICKS";
    private static final String COOLDOWN_PERMISSION_PREFIX = "ultimatedonutsmp.rtp.cooldown.";
    private static final int DEFAULT_GENERATE_FALLBACK_AFTER_SAMPLES = 32;
    private static final int DEFAULT_MAX_GENERATE_FALLBACK_SAMPLES = 32;
    private static final String LOCATION_CACHE_ENABLED_SETTING = "SETTINGS.LOCATION-CACHE.ENABLED";
    private static final String LOCATION_CACHE_SIZE_SETTING = "SETTINGS.LOCATION-CACHE.SIZE";
    private static final String LOCATION_CACHE_MAX_AGE_SETTING = "SETTINGS.LOCATION-CACHE.MAX-AGE-SECONDS";
    private static final String LOCATION_CACHE_GENERATE_CHUNKS_SETTING = "SETTINGS.LOCATION-CACHE.GENERATE-CHUNKS";
    private static final int DEFAULT_LOCATION_CACHE_SIZE = 3;
    private static final int MAX_LOCATION_CACHE_SIZE = 16;
    private static final int DEFAULT_LOCATION_CACHE_MAX_AGE_SECONDS = 600;
    private static final int PRE_CACHE_PARALLEL_ATTEMPTS = 4;
    private static final long PRE_CACHE_SEARCH_TIMEOUT_MILLIS = 30_000L;
    private static final long PRE_CACHE_SEARCH_COOLDOWN_MILLIS = 5_000L;
    private static final long PRE_CACHE_BACKOFF_START_MILLIS = 30_000L;
    private static final long PRE_CACHE_BACKOFF_MAX_MILLIS = 600_000L;

    private static final class SearchProgress {
        private final String worldName;
        private final SearchSettings settings;
        private long elapsedTicks;
        private int attemptsUsed;
        private int chunkSamplesUsed;
        private int generateFallbackSamplesUsed;
        private long lastElapsedSecond;
        private int activeAttemptsInFlight;
        private Location pendingFoundLocation;

        private SearchProgress(String worldName, SearchSettings settings) {
            this.worldName = worldName;
            this.settings = settings;
        }
    }

    private record LocationAttempt(Location location, boolean countedAttempt) {
    }

    private record CachedLocation(Location location, long cachedAtMillis) {
    }

    private static final class DirectSearchState {
        private final SearchSettings settings;
        /** Whether this particular search may generate terrain, regardless of what the config allows. */
        private final boolean allowChunkGeneration;
        /** Whether this search is the background warm-up rather than one somebody is waiting on. */
        private final boolean backgroundWarmUp;
        private final AtomicInteger attemptsUsed = new AtomicInteger();
        private final AtomicInteger chunkSamplesUsed = new AtomicInteger();
        private final AtomicInteger generateFallbackSamplesUsed = new AtomicInteger();
        private final AtomicInteger activeChains = new AtomicInteger();

        private DirectSearchState(SearchSettings settings, boolean allowChunkGeneration, boolean backgroundWarmUp) {
            this.settings = settings;
            this.allowChunkGeneration = allowChunkGeneration;
            this.backgroundWarmUp = backgroundWarmUp;
        }
    }

    public record RTPQueueEntry(
            UUID playerId,
            String worldName,
            int priority,
            long queueTimeMillis
    ) {}

    private static final Comparator<RTPQueueEntry> QUEUE_COMPARATOR = Comparator
            .comparingInt(RTPQueueEntry::priority).reversed()
            .thenComparingLong(RTPQueueEntry::queueTimeMillis);

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Map<String, Long>> lastRtpUseByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> activeSearchTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> activeResultTasks = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Location>> activeDirectSearches = new ConcurrentHashMap<>();
    private final Map<UUID, SearchProgress> activeSearches = new ConcurrentHashMap<>();
    private final Map<String, java.util.Queue<CachedLocation>> locationPreCache = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicReference<CompletableFuture<Location>> preCacheSearch =
            new java.util.concurrent.atomic.AtomicReference<>();
    private final AtomicInteger preCacheRotation = new AtomicInteger();
    private final Map<String, Integer> preCacheFailureStreak = new ConcurrentHashMap<>();
    private final Map<String, Long> preCacheRetryAtMillis = new ConcurrentHashMap<>();
    private volatile long preCacheSearchDeadlineMillis;
    private volatile long nextPreCacheSearchAtMillis;
    private volatile boolean warnedPreCacheCannotPrepareChunks;
    private final List<RTPQueueEntry> waitingQueue = java.util.Collections.synchronizedList(new ArrayList<>());
    private List<RTPDestination> configuredDestinations = List.of();
    private List<RTPDestination> menuDestinations = List.of();

    public RTPManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        clearAllSearches();
        clearQueue();
        lastRtpUseByPlayer.clear();
        locationPreCache.clear();
        cancelPreCacheSearch();
        nextPreCacheSearchAtMillis = 0L;
        preCacheFailureStreak.clear();
        preCacheRetryAtMillis.clear();
        warnedPreCacheCannotPrepareChunks = false;
        configuredDestinations = loadConfiguredDestinations();
        menuDestinations = buildMenuDestinations(configuredDestinations);
        refillPreCacheAllWorlds();
    }

    public boolean isPriorityQueueEnabled() {
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getRtp() == null) {
            return true;
        }
        return plugin.getConfigManager().getRtp().getBoolean("SETTINGS.PRIORITY-QUEUE.ENABLED", true);
    }

    public int getPlayerPriority(Player player) {
        if (player == null || !isPriorityQueueEnabled()) {
            return 0;
        }

        int maxPriority = plugin.getConfigManager().getRtp()
                .getInt("SETTINGS.PRIORITY-QUEUE.DEFAULT-PRIORITY", 0);

        ConfigurationSection section = plugin.getConfigManager().getRtp()
                .getConfigurationSection("SETTINGS.PRIORITY-QUEUE.PERMISSIONS");
        if (section != null) {
            Map<String, Object> values = section.getValues(true);
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                if (entry.getValue() instanceof Number number) {
                    String permNode = entry.getKey();
                    if (player.hasPermission(permNode)) {
                        int val = number.intValue();
                        if (val > maxPriority) {
                            maxPriority = val;
                        }
                    }
                }
            }
        }

        for (org.bukkit.permissions.PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String perm = pai.getPermission();
            if (perm != null && pai.getValue() && perm.toLowerCase(Locale.ROOT).startsWith("ultimatedonutsmp.rtp.priority.")) {
                String sub = perm.substring("ultimatedonutsmp.rtp.priority.".length());
                try {
                    int val = Integer.parseInt(sub);
                    if (val > maxPriority) {
                        maxPriority = val;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return maxPriority;
    }

    public boolean isInQueue(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        synchronized (waitingQueue) {
            for (RTPQueueEntry entry : waitingQueue) {
                if (entry.playerId().equals(playerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getQueuePosition(UUID playerId) {
        if (playerId == null) {
            return -1;
        }
        synchronized (waitingQueue) {
            List<RTPQueueEntry> sorted = new ArrayList<>(waitingQueue);
            sorted.sort(QUEUE_COMPARATOR);
            for (int i = 0; i < sorted.size(); i++) {
                if (sorted.get(i).playerId().equals(playerId)) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    public void removeFromQueue(UUID playerId) {
        if (playerId == null) {
            return;
        }
        synchronized (waitingQueue) {
            waitingQueue.removeIf(entry -> entry.playerId().equals(playerId));
        }
    }

    public void clearQueue() {
        synchronized (waitingQueue) {
            waitingQueue.clear();
        }
    }

    public int getQueueSize() {
        return waitingQueue.size();
    }

    public synchronized void processNextInQueue() {
        if (!isPriorityQueueEnabled()) {
            return;
        }
        if (isQueueFull(null)) {
            return;
        }

        List<RTPQueueEntry> sorted;
        synchronized (waitingQueue) {
            if (waitingQueue.isEmpty()) {
                return;
            }
            sorted = new ArrayList<>(waitingQueue);
            sorted.sort(QUEUE_COMPARATOR);
        }

        for (RTPQueueEntry entry : sorted) {
            removeFromQueue(entry.playerId());

            Player player = Bukkit.getPlayer(entry.playerId());
            if (player == null || !player.isOnline()) {
                continue;
            }

            if (hasActiveRtpFlow(entry.playerId()) || plugin.getTeleportManager().hasPendingType(entry.playerId(), "RTP")) {
                continue;
            }

            SearchSettings settings = getWorldSearchSettings(entry.worldName());
            if (settings != null) {
                startSearch(player, entry.worldName(), settings);
                break;
            }
        }
    }

    public boolean isPreCacheEnabled() {
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getRtp() == null) {
            return false;
        }
        return plugin.getConfigManager().getRtp().getBoolean(LOCATION_CACHE_ENABLED_SETTING, true);
    }

    public int getPreCacheSize() {
        if (!isPreCacheEnabled()) {
            return 0;
        }
        int size = plugin.getConfigManager().getRtp().getInt(LOCATION_CACHE_SIZE_SETTING, DEFAULT_LOCATION_CACHE_SIZE);
        return Math.min(MAX_LOCATION_CACHE_SIZE, Math.max(0, size));
    }

    public int getSearchAttemptsPerTick() {
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getRtp() == null) {
            return DEFAULT_SEARCH_ATTEMPTS_PER_TICK;
        }
        return Math.max(1, plugin.getConfigManager().getRtp().getInt("SETTINGS.SEARCH-ATTEMPTS-PER-TICK", DEFAULT_SEARCH_ATTEMPTS_PER_TICK));
    }

    /**
     * How long the searching display is held before the teleport is allowed to start, in ticks.
     *
     * <p>A search that succeeds on its first sample would otherwise flash the action bar and
     * teleport in the same breath, so this floor exists to keep the message readable rather than
     * to pace the search itself. 0 teleports as soon as a spot is known.</p>
     */
    public long getMinSearchDisplayTicks() {
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getRtp() == null) {
            return DEFAULT_MIN_SEARCH_DISPLAY_TICKS;
        }
        return Math.max(0L, plugin.getConfigManager().getRtp()
                .getLong(MIN_SEARCH_DISPLAY_TICKS_SETTING, DEFAULT_MIN_SEARCH_DISPLAY_TICKS));
    }

    /**
     * How long the found location display is held before the teleport is queued, in ticks.
     *
     * <p>This one is paid on every teleport, including one served straight from the location
     * cache, so it is the whole wait a player sees when nothing had to be searched for at all.
     * 0 queues the teleport as soon as the surrounding chunks are ready.</p>
     */
    public long getFoundDisplayTicks() {
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getRtp() == null) {
            return DEFAULT_FOUND_DISPLAY_TICKS;
        }
        return Math.max(0L, plugin.getConfigManager().getRtp()
                .getLong(FOUND_DISPLAY_TICKS_SETTING, DEFAULT_FOUND_DISPLAY_TICKS));
    }

    public void refillPreCacheAllWorlds() {
        if (!isPreCacheReady() || getPreCacheSize() <= 0) {
            return;
        }

        expireOverduePreCacheSearch();

        Set<String> worldNames = new LinkedHashSet<>();
        for (RTPDestination destination : configuredDestinations) {
            if (destination.enabled()) {
                worldNames.add(destination.worldName());
            }
        }
        if (worldNames.isEmpty()) {
            return;
        }

        // Rotate where the sweep starts so a world that always finds a spot cannot starve the rest.
        List<String> ordered = new ArrayList<>(worldNames);
        int start = Math.floorMod(preCacheRotation.getAndIncrement(), ordered.size());
        for (int offset = 0; offset < ordered.size(); offset++) {
            if (refillPreCache(ordered.get((start + offset) % ordered.size()))) {
                return;
            }
        }
    }

    /** @return whether a background search was actually started for this world. */
    public boolean refillPreCache(String worldName) {
        if (!isPreCacheReady() || worldName == null || worldName.isBlank()) {
            return false;
        }
        int cacheSize = getPreCacheSize();
        if (cacheSize <= 0) {
            return false;
        }

        String worldKey = normalizeWorldKey(worldName);
        java.util.Queue<CachedLocation> cached = locationPreCache
                .computeIfAbsent(worldKey, ignored -> new ConcurrentLinkedQueue<>());
        cached.removeIf(entry -> !isCachedLocationUsable(entry));
        if (cached.size() >= cacheSize) {
            return false;
        }

        if (isDeniedWorld(worldName) || isConfiguredDestinationDisabled(worldName)) {
            return false;
        }
        if (!canPrepareChunks(isPreCacheChunkGenerationEnabled())) {
            warnPreCacheCannotPrepareChunksOnce();
            return false;
        }
        if (System.currentTimeMillis() < preCacheRetryAtMillis.getOrDefault(worldKey, 0L)) {
            return false;
        }
        SearchSettings settings = getWorldSearchSettings(worldName);
        if (settings == null || getLoadedWorld(worldName) == null || !isSearchRequestValid(settings)) {
            return false;
        }

        return startPreCacheSearch(worldKey, settings);
    }

    /**
     * Whether a search under the current config can get hold of a chunk at all.
     *
     * <p>A sample is only ever prepared by generating the chunk or by reading one that is already
     * generated. Forbid both and every random sample returns nothing without so much as touching the
     * world, so the search burns its whole budget and fails on a world that is perfectly fine. The
     * background warm-up is the one that suffers for it, because it is not allowed to generate and
     * so depends entirely on {@code LOAD-GENERATED-CHUNKS} being on.</p>
     */
    private boolean canPrepareChunks(boolean allowChunkGeneration) {
        FileConfiguration rtp = plugin.getConfigManager().getRtp();
        if (rtp.getBoolean(LOAD_GENERATED_CHUNKS_SETTING, true)) {
            return true;
        }
        return allowChunkGeneration
                && (rtp.getBoolean(GENERATE_CHUNKS_SETTING, false)
                || rtp.getBoolean(GENERATE_FALLBACK_CHUNKS_SETTING, true));
    }

    private void warnPreCacheCannotPrepareChunksOnce() {
        if (warnedPreCacheCannotPrepareChunks) {
            return;
        }
        warnedPreCacheCannotPrepareChunks = true;
        warn("the rtp location cache is off because nothing lets it reach a chunk. Turn on"
                + " SETTINGS.LOAD-GENERATED-CHUNKS to read pregenerated terrain, or"
                + " SETTINGS.LOCATION-CACHE.GENERATE-CHUNKS to let the warm-up generate its own.");
    }

    /**
     * Slows a world's warm-up down after it comes back empty-handed.
     *
     * <p>A world nobody has explored has no terrain to offer, and no amount of retrying changes
     * that. Without a backoff the sweep starts another doomed search every few seconds and buries
     * the console in warnings for a server that is behaving exactly as configured. Each failure
     * doubles the wait for that world alone, so a world that is fine keeps its normal cadence, and
     * one success puts the failing world straight back to it.</p>
     */
    private void backOffPreCacheWorld(String worldKey) {
        int streak = preCacheFailureStreak.merge(worldKey, 1, Integer::sum);
        long wait = Math.min(
                PRE_CACHE_BACKOFF_MAX_MILLIS,
                PRE_CACHE_BACKOFF_START_MILLIS << Math.min(streak - 1, 20)
        );
        preCacheRetryAtMillis.put(worldKey, System.currentTimeMillis() + wait);
        if (streak == 1) {
            warn("no safe rtp location was found in '" + worldKey + "' while warming the cache up."
                    + " That world most likely has no generated terrain inside its rtp radius yet."
                    + " Pregenerate it, or turn on SETTINGS.LOCATION-CACHE.GENERATE-CHUNKS to have the"
                    + " warm-up build its own. Retrying quietly from here on.");
        }
    }

    private void clearPreCacheBackoff(String worldKey) {
        preCacheFailureStreak.remove(worldKey);
        preCacheRetryAtMillis.remove(worldKey);
    }

    /**
     * Starts the one background search this server is allowed to have running.
     *
     * <p>The ceiling is deliberately low. A search may generate terrain, and on a world that has
     * never been walked that is the expensive part, so the warm-up takes one world at a time with a
     * pause between searches rather than filling every world at once.</p>
     *
     * @return whether the search was started
     */
    private boolean startPreCacheSearch(String worldKey, SearchSettings settings) {
        expireOverduePreCacheSearch();
        if (System.currentTimeMillis() < nextPreCacheSearchAtMillis) {
            return false;
        }

        CompletableFuture<Location> future = new CompletableFuture<>();
        if (!preCacheSearch.compareAndSet(null, future)) {
            return false;
        }
        preCacheSearchDeadlineMillis = System.currentTimeMillis() + PRE_CACHE_SEARCH_TIMEOUT_MILLIS;

        future.whenComplete((location, throwable) -> {
            preCacheSearch.compareAndSet(future, null);
            if (throwable instanceof java.util.concurrent.CancellationException) {
                return;
            }
            nextPreCacheSearchAtMillis = System.currentTimeMillis() + PRE_CACHE_SEARCH_COOLDOWN_MILLIS;
            if (throwable != null || location == null) {
                backOffPreCacheWorld(worldKey);
                return;
            }
            clearPreCacheBackoff(worldKey);
            java.util.Queue<CachedLocation> target = locationPreCache
                    .computeIfAbsent(worldKey, ignored -> new ConcurrentLinkedQueue<>());
            if (target.size() < getPreCacheSize()) {
                target.add(new CachedLocation(location, System.currentTimeMillis()));
            }
        });

        try {
            startDirectSearch(
                    settings,
                    future,
                    getPreCacheParallelAttempts(),
                    isPreCacheChunkGenerationEnabled(),
                    true
            );
        } catch (RuntimeException exception) {
            future.complete(null);
            throw exception;
        }
        return true;
    }

    /**
     * Ends a background search that has outstayed its deadline.
     *
     * <p>Completing the future is what stops it. Every chain checks the future before its next step,
     * so this winds the search down for real. Merely dropping the record of it would leave it
     * generating chunks alongside the replacement that took its place, and each round of that makes
     * the next search slower and more likely to overrun in turn.</p>
     */
    private void expireOverduePreCacheSearch() {
        CompletableFuture<Location> running = preCacheSearch.get();
        if (running == null || System.currentTimeMillis() < preCacheSearchDeadlineMillis) {
            return;
        }
        running.complete(null);
    }

    /**
     * Drops the running warm-up on reload. Cancelling rather than completing it keeps the search
     * from being counted as a world that had nothing to offer, which it never got the chance to
     * decide either way.
     */
    private void cancelPreCacheSearch() {
        CompletableFuture<Location> running = preCacheSearch.getAndSet(null);
        if (running != null) {
            running.cancel(false);
        }
    }

    /**
     * Whether one sample of a search may generate the chunk it lands on.
     *
     * <p>A search that is not allowed to generate never does, whatever the config says. That is what
     * keeps the background warm-up off chunk generation on servers where {@code GENERATE-CHUNKS} is
     * on for players.</p>
     */
    static boolean shouldGenerateForSample(
            boolean allowChunkGeneration,
            boolean generateChunksConfigured,
            boolean generateFallback
    ) {
        return allowChunkGeneration && (generateChunksConfigured || generateFallback);
    }

    /**
     * Whether the background warm-up may generate terrain.
     *
     * <p>Off by default, and deliberately separate from {@code SETTINGS.GENERATE-CHUNKS}. Generating
     * on demand for a player who asked costs a burst; generating in the background costs it over and
     * over on a world nobody has walked yet, which is enough to bury a small box. An admin with the
     * headroom can turn it on and get a cache that fills on brand new terrain.</p>
     */
    public boolean isPreCacheChunkGenerationEnabled() {
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getRtp() == null) {
            return false;
        }
        return plugin.getConfigManager().getRtp().getBoolean(LOCATION_CACHE_GENERATE_CHUNKS_SETTING, false);
    }

    /**
     * Background searches run on fewer parallel chains than a player-facing one. Nobody is waiting
     * on the result, so the warm-up has no business claiming the chunk throughput a waiting player
     * gets.
     */
    private int getPreCacheParallelAttempts() {
        return Math.max(1, Math.min(PRE_CACHE_PARALLEL_ATTEMPTS, getSearchAttemptsPerTick()));
    }

    private boolean isPreCacheReady() {
        return plugin != null
                && plugin.getConfigManager() != null
                && plugin.getConfigManager().getRtp() != null
                && plugin.getFeatureManager() != null
                && plugin.getSpigotScheduler() != null;
    }

    private Location pollPreCachedLocation(String worldName) {
        if (worldName == null || worldName.isBlank() || getPreCacheSize() <= 0) {
            return null;
        }
        java.util.Queue<CachedLocation> cached = locationPreCache.get(normalizeWorldKey(worldName));
        if (cached == null) {
            return null;
        }

        CachedLocation entry;
        while ((entry = cached.poll()) != null) {
            if (isCachedLocationUsable(entry)) {
                return entry.location().clone();
            }
        }
        return null;
    }

    private boolean isCachedLocationUsable(CachedLocation entry) {
        if (entry == null) {
            return false;
        }
        long maxAgeMillis = getPreCacheMaxAgeMillis();
        if (maxAgeMillis > 0L && System.currentTimeMillis() - entry.cachedAtMillis() > maxAgeMillis) {
            return false;
        }
        return isPreCachedLocationValid(entry.location());
    }

    private long getPreCacheMaxAgeMillis() {
        int seconds = plugin.getConfigManager().getRtp()
                .getInt(LOCATION_CACHE_MAX_AGE_SETTING, DEFAULT_LOCATION_CACHE_MAX_AGE_SECONDS);
        return seconds <= 0 ? 0L : seconds * 1000L;
    }

    private boolean isPreCachedLocationValid(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return false;
        }
        World world = getLoadedWorld(loc.getWorld().getName());
        if (world != loc.getWorld()) {
            return false;
        }
        SearchSettings settings = getWorldSearchSettings(world.getName());
        return settings == null || isWithinRadius(settings, loc, true);
    }

    public boolean isEnabled() {
        return plugin != null
                && plugin.getFeatureManager() != null
                && plugin.getFeatureManager().isEnabled(FeatureManager.Feature.RTP)
                && plugin.getConfigManager() != null
                && plugin.getConfigManager().getRtp() != null
                && plugin.getConfigManager().getRtp().getBoolean("ENABLED", true);
    }

    public void clearSearch(UUID playerId) {
        stopSearch(playerId, true);
    }

    private void stopSearch(UUID playerId, boolean clearActionBar) {
        BukkitTask task = activeSearchTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
        BukkitTask resultTask = activeResultTasks.remove(playerId);
        if (resultTask != null) {
            resultTask.cancel();
        }
        activeSearches.remove(playerId);
        CompletableFuture<Location> directSearch = activeDirectSearches.remove(playerId);
        if (directSearch != null) {
            directSearch.complete(null);
        }

        removeFromQueue(playerId);

        if (clearActionBar) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                PlayerSettingUtils.clearActionBar(player);
            }
        }

        processNextInQueue();
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

    public boolean isRankCooldownsEnabled() {
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getRtp() == null) {
            return true;
        }
        return plugin.getConfigManager().getRtp().getBoolean("SETTINGS.RANK-COOLDOWNS.ENABLED", true);
    }

    public int getPlayerCooldownSeconds(Player player, String worldName) {
        int worldCooldown = getWorldCooldownSeconds(worldName);
        if (player == null || !isRankCooldownsEnabled()) {
            return worldCooldown;
        }

        int lowest = Integer.MAX_VALUE;

        ConfigurationSection rtpConfig = plugin == null || plugin.getConfigManager() == null
                ? null
                : plugin.getConfigManager().getRtp();
        ConfigurationSection section = rtpConfig == null
                ? null
                : rtpConfig.getConfigurationSection("SETTINGS.RANK-COOLDOWNS.PERMISSIONS");
        if (section != null) {
            Map<String, Object> values = section.getValues(true);
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                if (entry.getValue() instanceof Number number) {
                    String permNode = entry.getKey();
                    if (player.hasPermission(permNode)) {
                        int val = Math.max(0, number.intValue());
                        if (val < lowest) {
                            lowest = val;
                        }
                    }
                }
            }
        }

        for (org.bukkit.permissions.PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String perm = pai.getPermission();
            if (perm == null || !pai.getValue()) {
                continue;
            }
            String normalized = perm.toLowerCase(Locale.ROOT);
            if (!normalized.startsWith(COOLDOWN_PERMISSION_PREFIX)) {
                continue;
            }
            try {
                int val = Integer.parseInt(normalized.substring(COOLDOWN_PERMISSION_PREFIX.length()).trim());
                if (val >= 0 && val < lowest) {
                    lowest = val;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return lowest == Integer.MAX_VALUE ? worldCooldown : lowest;
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

    public SearchSettings getFirstJoinSearchSettings(String fallbackWorldName) {
        int minRadius = plugin.getConfigManager().getConfig().getInt("FIRST-JOIN-RTP.WORLD.MIN-RADIUS", 500);
        int maxRadius = plugin.getConfigManager().getConfig().getInt("FIRST-JOIN-RTP.WORLD.MAX-RADIUS", 5000);
        int centerX = plugin.getConfigManager().getConfig().getInt("FIRST-JOIN-RTP.WORLD.CENTER-X", 0);
        int centerZ = plugin.getConfigManager().getConfig().getInt("FIRST-JOIN-RTP.WORLD.CENTER-Z", 0);
        int maxAttempts = plugin.getConfigManager().getRtp().getInt("SETTINGS.MAX-ATTEMPTS", DEFAULT_MAX_ATTEMPTS);
        int maxChunkSamples = plugin.getConfigManager().getRtp().getInt("SETTINGS.MAX-CHUNK-SAMPLES", DEFAULT_MAX_CHUNK_SAMPLES);
        int attemptIntervalTicks = plugin.getConfigManager().getRtp().getInt("SETTINGS.ATTEMPT-INTERVAL-TICKS", DEFAULT_ATTEMPT_INTERVAL_TICKS);
        String configuredWorld = plugin.getConfigManager().getConfig().getString("FIRST-JOIN-RTP.WORLD.NAME", "");
        String worldName = normalizeConfiguredWorldName(
                configuredWorld == null || configuredWorld.isBlank() ? fallbackWorldName : configuredWorld
        );
        if (isDeniedWorld(worldName) || !isWorldAvailable(worldName)) {
            return null;
        }

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

    public SearchSettings getRespawnSearchSettings(String deathWorldName) {
        String configuredWorld = plugin.getConfigManager().getConfig().getString("RESPAWN-RTP.WORLD.NAME", "");
        String worldName = normalizeConfiguredWorldName(
                configuredWorld == null || configuredWorld.isBlank() ? deathWorldName : configuredWorld
        );
        if (worldName == null || worldName.isBlank() || isDeniedWorld(worldName) || !isWorldAvailable(worldName)) {
            return null;
        }

        boolean useRtpBounds = plugin.getConfigManager().getConfig()
                .getBoolean("RESPAWN-RTP.WORLD.USE-RTP-BOUNDS", true);
        if (useRtpBounds) {
            SearchSettings rtpBounds = getWorldSearchSettings(worldName);
            if (rtpBounds != null) {
                return rtpBounds;
            }
        }

        int minRadius = plugin.getConfigManager().getConfig().getInt("RESPAWN-RTP.WORLD.MIN-RADIUS", 500);
        int maxRadius = plugin.getConfigManager().getConfig().getInt("RESPAWN-RTP.WORLD.MAX-RADIUS", 5000);
        int centerX = plugin.getConfigManager().getConfig().getInt("RESPAWN-RTP.WORLD.CENTER-X", 0);
        int centerZ = plugin.getConfigManager().getConfig().getInt("RESPAWN-RTP.WORLD.CENTER-Z", 0);
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
                    plugin.getConfigManager().getRtp().getString("MESSAGES.DISABLED", "&cRTP is disabled.")
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
                    plugin.getConfigManager().getRtp().getString("MESSAGES.DISABLED", "&cRTP is disabled.")
            ));
            return false;
        }
        String worldName = resolveWorldSelector(selector);
        if (worldName == null || worldName.isBlank()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp().getString("MESSAGES.WORLD-NOT-EXIST", "&cWorld not found.")
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
        startDirectSearch(settings, future);
        return future;
    }

    public CompletableFuture<Location> findSafeLocationAsync(SearchSettings settings) {
        if (!isSearchRequestValid(settings)) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Location> future = new CompletableFuture<>();
        startDirectSearch(settings, future);
        return future;
    }

    private boolean isSearchRequestValid(SearchSettings settings) {
        if (!plugin.getFeatureManager().isEnabled(FeatureManager.Feature.RTP)) {
            return false;
        }
        return settings != null && settings.worldName() != null && !settings.worldName().isBlank();
    }

    private void startDirectSearch(SearchSettings settings, CompletableFuture<Location> future) {
        startDirectSearch(settings, future, getSearchAttemptsPerTick(), true, false);
    }

    private void startDirectSearch(
            SearchSettings settings,
            CompletableFuture<Location> future,
            int parallelAttempts,
            boolean allowChunkGeneration,
            boolean backgroundWarmUp
    ) {
        DirectSearchState state = new DirectSearchState(settings, allowChunkGeneration, backgroundWarmUp);
        int chains = Math.max(1, Math.min(parallelAttempts, settings.maxChunkSamples()));
        state.activeChains.set(chains);
        for (int chain = 0; chain < chains; chain++) {
            scheduleDirectSearchStep(state, future, settings.attemptIntervalTicks());
        }
    }

    private void scheduleDirectSearchStep(
            DirectSearchState state,
            CompletableFuture<Location> future,
            long delayTicks
    ) {
        if (future.isDone()) {
            finishDirectSearchChain(state, future);
            return;
        }
        plugin.getSpigotScheduler().runGlobalLater(
                () -> runDirectSearchStep(state, future),
                Math.max(0L, delayTicks)
        );
    }

    private void finishDirectSearchChain(DirectSearchState state, CompletableFuture<Location> future) {
        if (state.activeChains.decrementAndGet() > 0) {
            return;
        }
        if (!future.complete(null) || state.backgroundWarmUp) {
            // The warm-up reports through its own backoff instead, so a world with nothing to offer
            // costs one warning rather than one every few seconds for as long as the server runs.
            return;
        }
        logSearchFailure(
                state.settings,
                state.attemptsUsed.get(),
                state.chunkSamplesUsed.get(),
                state.generateFallbackSamplesUsed.get()
        );
    }

    private void runDirectSearchStep(DirectSearchState state, CompletableFuture<Location> future) {
        if (future.isDone()) {
            finishDirectSearchChain(state, future);
            return;
        }

        SearchSettings settings = state.settings;
        if (!hasAttemptBudget(state.attemptsUsed.get(), settings)
                || !hasChunkSampleBudget(state.chunkSamplesUsed.get(), settings)) {
            finishDirectSearchChain(state, future);
            return;
        }

        World world = resolveWorld(settings.worldName());
        if (world == null) {
            finishDirectSearchChain(state, future);
            return;
        }

        int chunkSamplesUsed = state.chunkSamplesUsed.getAndIncrement();
        boolean generateFallback = state.allowChunkGeneration
                && shouldUseGenerateFallback(chunkSamplesUsed, state.generateFallbackSamplesUsed.get());
        if (!generateFallback && shouldUseLoadedChunkFallback(chunkSamplesUsed)) {
            plugin.getSpigotScheduler().runRegion(world, settings.centerX() >> 4, settings.centerZ() >> 4, () -> {
                int[] loadedChunk = nextLoadedChunkSample(world);
                if (loadedChunk == null) {
                    continueDirectSearch(state, future, null);
                    return;
                }
                plugin.getSpigotScheduler().runRegion(world, loadedChunk[0], loadedChunk[1], () -> {
                    Location found = null;
                    try {
                        LocationAttempt attempt =
                                tryLoadedChunkLocationAttempt(settings, loadedChunk[0], loadedChunk[1]);
                        if (attempt.countedAttempt()) {
                            state.attemptsUsed.incrementAndGet();
                        }
                        found = attempt.location();
                    } catch (RuntimeException exception) {
                        state.attemptsUsed.incrementAndGet();
                    }
                    continueDirectSearch(state, future, found);
                });
            });
            return;
        }

        if (generateFallback) {
            state.generateFallbackSamplesUsed.incrementAndGet();
        }

        FileConfiguration rtp = plugin.getConfigManager().getRtp();
        boolean generateForSample = shouldGenerateForSample(
                state.allowChunkGeneration,
                rtp.getBoolean(GENERATE_CHUNKS_SETTING, false),
                generateFallback
        );
        if (!generateForSample && !rtp.getBoolean(LOAD_GENERATED_CHUNKS_SETTING, true)) {
            continueDirectSearch(state, future, null);
            return;
        }

        int[] sample = nextRandomSample(settings);
        int x = sample[0];
        int z = sample[1];
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        getChunkAtAsync(world, chunkX, chunkZ, generateForSample).thenAccept(chunk ->
                plugin.getSpigotScheduler().runRegion(world, chunkX, chunkZ, () -> {
                    Location found = null;
                    try {
                        if (chunk != null) {
                            found = resolveSafeLocationInChunk(world, settings, x, z, chunkX, chunkZ);
                            state.attemptsUsed.incrementAndGet();
                        }
                    } catch (RuntimeException exception) {
                        state.attemptsUsed.incrementAndGet();
                    }
                    continueDirectSearch(state, future, found);
                })
        ).exceptionally(throwable -> {
            plugin.getSpigotScheduler().runRegion(world, chunkX, chunkZ, () -> {
                if (generateForSample) {
                    state.attemptsUsed.incrementAndGet();
                }
                continueDirectSearch(state, future, null);
            });
            return null;
        });
    }

    private void continueDirectSearch(DirectSearchState state, CompletableFuture<Location> future, Location found) {
        if (found != null) {
            future.complete(found);
            finishDirectSearchChain(state, future);
            return;
        }
        scheduleDirectSearchStep(state, future, state.settings.attemptIntervalTicks());
    }

    private boolean queueTeleport(Player player, String worldName) {
        if (isDeniedWorld(worldName)) {
            player.sendMessage(ColorUtils.toComponent("&cYou cannot RTP in this world."));
            return false;
        }

        double reqHours = getWorldRequiredPlaytimeHours(worldName);
        if (reqHours > 0.0) {
            com.bx.ultimateDonutSmp.models.PlayerData data = plugin.getPlayerDataManager().get(player);
            double playtimeHours = data != null ? data.getTotalPlaytimeSeconds() / 3600.0 : 0.0;
            if (playtimeHours < reqHours) {
                double currentDisplayHours = data != null ? (data.getTotalPlaytimeSeconds() / 360) / 10.0 : 0.0;
                String message = plugin.getConfigManager().getRtp().getString("MESSAGES.PLAYTIME-REQUIRED", "&cYou need at least {required} hours of playtime to RTP to {world}. &7(Current: {current}h)");
                message = message.replace("{required}", String.format(Locale.ROOT, "%.1f", reqHours))
                        .replace("{world}", describeWorld(worldName))
                        .replace("{current}", String.format(Locale.ROOT, "%.1f", currentDisplayHours));
                player.sendMessage(ColorUtils.toComponent(message));
                return false;
            }
        }

        if (isConfiguredDestinationDisabled(worldName)) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp()
                            .getString("MESSAGES.DESTINATION-DISABLED", "&cThis destination is currently disabled.")
            ));
            return false;
        }

        World world = resolveWorld(worldName);
        if (world == null) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp().getString("MESSAGES.WORLD-NOT-EXIST", "&cWorld not found.")
            ));
            return false;
        }

        SearchSettings settings = getWorldSearchSettings(worldName);
        if (settings == null) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp()
                            .getString("MESSAGES.DESTINATION-DISABLED", "&cThis destination is currently disabled.")
            ));
            return false;
        }

        if (hasActiveRtpFlow(player.getUniqueId()) || plugin.getTeleportManager().hasPendingType(player.getUniqueId(), "RTP")) {
            player.sendMessage(ColorUtils.toComponent("&cYour RTP is already in progress."));
            return false;
        }

        if (isInQueue(player.getUniqueId())) {
            int pos = getQueuePosition(player.getUniqueId());
            String message = plugin.getConfigManager().getRtp()
                    .getString("MESSAGES.ALREADY-IN-QUEUE", "&cYou are already in the RTP waiting queue at position #{position}.")
                    .replace("{position}", String.valueOf(pos));
            player.sendMessage(ColorUtils.toComponent(message));
            return false;
        }

        long cooldownRemaining = getCooldownRemainingMillis(player, worldName);
        if (cooldownRemaining > 0L) {
            long remainingSeconds = Math.max(1L, (long) Math.ceil(cooldownRemaining / 1000.0D));
            String message = plugin.getConfigManager().getRtp()
                    .getString("MESSAGES.COOLDOWN", "&cYou can't RTP for another {remaining}s.");
            message = message.replace("{remaining}", String.valueOf(remainingSeconds))
                    .replace("%remaining%", String.valueOf(remainingSeconds));
            player.sendMessage(ColorUtils.toComponent(message));
            return false;
        }

        if (isQueueFull(player.getUniqueId())) {
            if (isPriorityQueueEnabled()) {
                int priority = getPlayerPriority(player);
                RTPQueueEntry entry = new RTPQueueEntry(player.getUniqueId(), worldName, priority, System.currentTimeMillis());
                synchronized (waitingQueue) {
                    waitingQueue.add(entry);
                }
                int pos = getQueuePosition(player.getUniqueId());
                String message = plugin.getConfigManager().getRtp()
                        .getString("MESSAGES.QUEUE-JOINED", "&eRTP slots are full. You are in queue at position &#4B72FF#{position}&e (Priority: &f{priority}&e).")
                        .replace("{position}", String.valueOf(pos))
                        .replace("{priority}", String.valueOf(priority));
                player.sendMessage(ColorUtils.toComponent(message));
                return true;
            } else {
                player.sendMessage(ColorUtils.toComponent(
                        plugin.getConfigManager().getRtp()
                                .getString("MESSAGES.MAX-PLAYERS", "&cToo many players are using RTP right now. Please try again later.")
                ));
                return false;
            }
        }

        startSearch(player, worldName, settings);
        return true;
    }

    private void startSearch(Player player, String worldName, SearchSettings settings) {
        clearSearch(player.getUniqueId());

        // A normal RTP replaces whatever the matchmaking queue had lined up, so the player
        // does not get pulled to a meeting point they have already teleported away from.
        if (plugin.getRtpQueueManager() != null
                && plugin.getRtpQueueManager().isInQueue(player.getUniqueId())) {
            plugin.getRtpQueueManager().leave(player);
        }

        Location preCached = pollPreCachedLocation(worldName);
        if (preCached != null) {
            SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-START"));
            finishSearch(player, worldName, preCached);
            refillPreCache(worldName);
            return;
        }

        String worldLabel = describeWorld(worldName);
        String searching = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.SEARCHING", "&aSearching for safe location in {world}...")
                .replace("{world}", worldLabel);
        player.sendMessage(ColorUtils.toComponent(searching));

        SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-START"));

        SearchProgress progress = new SearchProgress(worldName, settings);
        activeSearches.put(player.getUniqueId(), progress);

        if (plugin.getSpigotScheduler() != null) {
            BukkitTask task = plugin.getSpigotScheduler().runEntityTimer(
                    player,
                    () -> tickSearch(player.getUniqueId()),
                    0L,
                    SEARCH_ACTIONBAR_REFRESH_TICKS
            );
            if (task != null) {
                activeSearchTasks.put(player.getUniqueId(), task);
            }
        }

        refillPreCache(worldName);
    }

    private void tickSearch(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        SearchProgress progress = activeSearches.get(playerId);
        if (player == null || !player.isOnline() || progress == null) {
            clearSearch(playerId);
            return;
        }

        progress.elapsedTicks++;
        sendSearchActionBar(player, progress);

        if (progress.pendingFoundLocation != null) {
            if (progress.elapsedTicks >= getMinSearchDisplayTicks()) {
                stopSearch(playerId, false);
                finishSearch(player, progress.worldName, progress.pendingFoundLocation);
            }
            return;
        }

        int maxConcurrent = Math.max(1, getSearchAttemptsPerTick());
        while (progress.activeAttemptsInFlight < maxConcurrent && hasSearchBudget(progress)) {
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
            progress.activeAttemptsInFlight++;
            plugin.getSpigotScheduler().runRegion(world, progress.settings.centerX() >> 4, progress.settings.centerZ() >> 4, () -> {
                int[] loadedChunk = nextLoadedChunkSample(world);
                if (loadedChunk == null) {
                    completeAsyncLocationAttempt(playerId, progress, new LocationAttempt(null, false), null);
                    return;
                }
                plugin.getSpigotScheduler().runRegion(world, loadedChunk[0], loadedChunk[1], () -> {
                    try {
                        LocationAttempt attempt =
                                tryLoadedChunkLocationAttempt(progress.settings, loadedChunk[0], loadedChunk[1]);
                        completeAsyncLocationAttempt(playerId, progress, attempt, null);
                    } catch (RuntimeException exception) {
                        completeAsyncLocationAttempt(playerId, progress, null, exception);
                    }
                });
            });
            return;
        }
        int[] sample = nextRandomSample(progress.settings);
        int x = sample[0];
        int z = sample[1];
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        progress.chunkSamplesUsed++;
        if (generateFallback) {
            progress.generateFallbackSamplesUsed++;
        }
        progress.activeAttemptsInFlight++;
        boolean generateChunks = plugin.getConfigManager().getRtp().getBoolean(GENERATE_CHUNKS_SETTING, false);
        boolean generateForSample = generateChunks || generateFallback;
        if (!generateForSample && !plugin.getConfigManager().getRtp().getBoolean(LOAD_GENERATED_CHUNKS_SETTING, true)) {
            completeAsyncLocationAttempt(playerId, progress, new LocationAttempt(null, false), null);
            return;
        }
        getChunkAtAsync(world, chunkX, chunkZ, generateForSample).thenAccept(chunk -> {
            plugin.getSpigotScheduler().runRegion(world, chunkX, chunkZ, () -> {
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
            plugin.getSpigotScheduler().runRegion(world, chunkX, chunkZ, () -> {
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

        progress.activeAttemptsInFlight = Math.max(0, progress.activeAttemptsInFlight - 1);
        if (throwable != null) {
            progress.attemptsUsed++;
            String msg = throwable.getMessage();
            if (msg != null && (msg.contains("newer version") || msg.contains("4903"))) {
                // Silently skip incompatible chunk versions without spamming logs
            } else {
                plugin.getLogger().warning("[RTPManager] async rtp chunk load skipped: " + msg);
            }
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
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline() && progress.elapsedTicks >= getMinSearchDisplayTicks()) {
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
                + ", attempts " + displaySearchCount(progress.attemptsUsed, progress.settings.maxAttempts())
                + "/" + progress.settings.maxAttempts()
                + ", samples " + displaySearchCount(progress.chunkSamplesUsed, progress.settings.maxChunkSamples())
                + "/" + progress.settings.maxChunkSamples()
                + ", generatechunks=" + generateChunks
                + ", generatefallback=" + isGenerateFallbackEnabled()
                + ", fallbackgeneratedsamples=" + progress.generateFallbackSamplesUsed
                + "/" + getMaxGenerateFallbackSamples()
                + ".");

        Player player = Bukkit.getPlayer(playerId);
        clearSearch(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }

        String attempts = String.valueOf(
                displaySearchCount(progress.attemptsUsed, progress.settings.maxAttempts()));
        String maxAttempts = formatSearchLimit(progress.settings.maxAttempts());
        String samples = String.valueOf(
                displaySearchCount(progress.chunkSamplesUsed, progress.settings.maxChunkSamples()));
        String maxSamples = formatSearchLimit(progress.settings.maxChunkSamples());
        String maxAttemptsMessage = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.MAX-ATTEMPTS", "&cCould not find a safe location after %attempts% attempts.")
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
                + ", attempts " + displaySearchCount(attemptsUsed, settings.maxAttempts())
                + "/" + settings.maxAttempts()
                + ", samples " + displaySearchCount(chunkSamplesUsed, settings.maxChunkSamples())
                + "/" + settings.maxChunkSamples()
                + ", generatechunks=" + generateChunks
                + ", generatefallback=" + isGenerateFallbackEnabled()
                + ", fallbackgeneratedsamples=" + generateFallbackSamplesUsed
                + "/" + getMaxGenerateFallbackSamples()
                + ".");
    }

    private void finishSearch(Player player, String worldName, Location found) {
        markRtpUsed(player.getUniqueId(), worldName);

        String foundMessage = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.SAFE-LOCATION-FOUND", "&aSafe location found at: x:{x} y:{y} z:{z}")
                .replace("{x}", String.valueOf(found.getBlockX()))
                .replace("{y}", String.valueOf(found.getBlockY()))
                .replace("{z}", String.valueOf(found.getBlockZ()));
        if (!PlayerSettingUtils.rtpCoordinatesEnabled(plugin, player)) {
            foundMessage = plugin.getConfigManager().getRtp().getString(
                    "MESSAGES.SAFE-LOCATION-FOUND-HIDDEN",
                    "&aSafe location found."
            );
        }
        if (!foundMessage.isBlank()) {
            player.sendMessage(ColorUtils.toComponent(foundMessage));
        }
        sendTeleportWarning(player, worldName);
        SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-FOUND"));
        UUID playerId = player.getUniqueId();
        CompletableFuture<Void> preloadFuture = preloadTeleportChunks(found);

        final long foundDisplayTicks = getFoundDisplayTicks();
        final long[] shownTicks = {0L};
        final boolean[] queuedTeleport = {false};
        final BukkitTask[] resultTaskRef = new BukkitTask[1];
        BukkitTask resultTask = plugin.getSpigotScheduler().runEntityTimer(player, () -> {
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

            if (shownTicks[0] >= foundDisplayTicks && preloadFuture.isDone()) {
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
            BukkitTask[] resultTaskRef,
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
        processNextInQueue();
    }

    private void sendSearchActionBar(Player player, SearchProgress progress) {
        long displayedSeconds = getDisplayedSearchSeconds(progress.elapsedTicks);

        String actionBar = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.SEARCH-ACTIONBAR", "&7Searching {world}... &b{elapsed}s");
        actionBar = stripSearchCounter(actionBar)
                .replace("{world}", describeWorld(progress.worldName))
                .replace("{elapsed}", formatElapsedSeconds(progress.elapsedTicks))
                .replace("{attempts}", String.valueOf(
                        displaySearchCount(progress.attemptsUsed, progress.settings.maxAttempts())))
                .replace("{max_attempts}", formatSearchLimit(progress.settings.maxAttempts()))
                .replace("{samples}", String.valueOf(
                        displaySearchCount(progress.chunkSamplesUsed, progress.settings.maxChunkSamples())))
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
                .getString("MESSAGES.SEARCH-FOUND-ACTIONBAR", "&aSafe location found in {world}! &7Preparing teleport...")
                .replace("{world}", describeWorld(worldName))
                .replace("{x}", String.valueOf(found.getBlockX()))
                .replace("{y}", String.valueOf(found.getBlockY()))
                .replace("{z}", String.valueOf(found.getBlockZ()));
        if (!PlayerSettingUtils.rtpCoordinatesEnabled(plugin, player)) {
            actionBar = plugin.getConfigManager().getRtp().getString(
                    "MESSAGES.SEARCH-FOUND-ACTIONBAR-HIDDEN",
                    "&aSafe location found in {world}! &7Preparing teleport..."
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

        int[] sample = nextRandomSample(settings);
        int x = sample[0];
        int z = sample[1];
        return tryResolveSafeLocation(world, x, z, x >> 4, z >> 4);
    }

    private int[] nextRandomSample(SearchSettings settings) {
        int minRadius = Math.max(0, settings.minRadius());
        int maxRadius = Math.max(minRadius, settings.maxRadius());

        double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
        double distance = minRadius;
        if (maxRadius > minRadius) {
            distance += ThreadLocalRandom.current().nextDouble(0, maxRadius - minRadius);
        }

        return new int[] {
                settings.centerX() + (int) Math.round(Math.cos(angle) * distance),
                settings.centerZ() + (int) Math.round(Math.sin(angle) * distance)
        };
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
        plugin.getSpigotScheduler().runRegion(world, chunkX, chunkZ, () -> {
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
        final BukkitTask[] taskRef = new BukkitTask[1];

        Runnable complete = () -> {
            if (future.complete(null) && taskRef[0] != null) {
                taskRef[0].cancel();
            }
        };

        taskRef[0] = plugin.getSpigotScheduler().runGlobalTimer(() -> {
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
        return RtpChunkPreloadPolicy.chunkOrder(centerChunkX, centerChunkZ, radius);
    }

    private int getPreloadRadius() {
        return RtpChunkPreloadPolicy.radius(plugin.getConfigManager().getRtp());
    }

    private int getPreloadChunksPerTick() {
        return RtpChunkPreloadPolicy.chunksPerTick(plugin.getConfigManager().getRtp());
    }

    private int getPreloadMaxTicks() {
        return Math.max(1, plugin.getConfigManager().getRtp().getInt(PRELOAD_MAX_TICKS_SETTING, 40));
    }

    /**
     * Picks one of the world's loaded chunks at random, or {@code null} when it has none. Only the
     * chunk coordinates are read here, never a block, so the caller can hop to the region that owns
     * that chunk before touching it.
     */
    int[] nextLoadedChunkSample(World world) {
        if (world == null) {
            return null;
        }

        Chunk[] loadedChunks = world.getLoadedChunks();
        if (loadedChunks.length == 0) {
            return null;
        }

        Chunk chunk = loadedChunks[ThreadLocalRandom.current().nextInt(loadedChunks.length)];
        return new int[] {chunk.getX(), chunk.getZ()};
    }

    /**
     * Probes a single column inside one already-loaded chunk.
     *
     * <p>Folia splits a world into regions and only lets a thread read the chunks its own region
     * owns, so this has to run on the region that owns {@code chunkX, chunkZ} and must not wander
     * outside it. Reading a spread of loaded chunks from one region thread trips Folia's tick
     * thread check and finds nothing.</p>
     */
    private LocationAttempt tryLoadedChunkLocationAttempt(SearchSettings settings, int chunkX, int chunkZ) {
        if (settings == null || settings.worldName() == null || settings.worldName().isBlank()) {
            return new LocationAttempt(null, false);
        }

        World world = resolveWorld(settings.worldName());
        if (world == null) {
            return new LocationAttempt(null, false);
        }

        int x = (chunkX << 4) + ThreadLocalRandom.current().nextInt(16);
        int z = (chunkZ << 4) + ThreadLocalRandom.current().nextInt(16);

        Location found = resolveSafeLocation(world, x, z);
        if (found != null && isWithinRadius(settings, found, true)) {
            return new LocationAttempt(found, true);
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

        int afterSamples = Math.max(1, rtp.getInt(LOADED_CHUNK_FALLBACK_AFTER_SETTING, 64));
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
                    normalizeConfiguredWorldName(worldName),
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

        return normalizeConfiguredWorldName(trimmed);
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

    public double getWorldRequiredPlaytimeHours(String worldName) {
        ConfigurationSection worldSettings = getWorldSettingsSection(worldName);
        if (worldSettings == null) {
            return 0.0;
        }
        return worldSettings.getDouble("REQUIRED-PLAYTIME-HOURS", 0.0);
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
        World.Environment env = null;
        if (worldObj != null) {
            env = worldObj.getEnvironment();
        } else if (isWorldAvailable(worldName)) {
            env = inferWorldEnvironment(worldName);
        }

        if (env != null) {
            if (env == World.Environment.NETHER) {
                ConfigurationSection fallback = worlds.getConfigurationSection("world_nether");
                if (fallback != null) return fallback;
            } else if (env == World.Environment.THE_END) {
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
                String name = world.getName();
                if (!isDeniedWorld(name)) {
                    return name;
                }
            }
        }
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
                String name = world.getName();
                if (!isDeniedWorld(name)) {
                    return name;
                }
            }
        }
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
                if (!name.equalsIgnoreCase("afk")
                        && !name.equalsIgnoreCase("duels")
                        && !name.equalsIgnoreCase("lobby")
                        && !name.equalsIgnoreCase("hub")
                        && !name.equalsIgnoreCase("spawn")
                        && !isDeniedWorld(name)) {
                    return name;
                }
            }
        }
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
        return Math.max(1, attemptIntervalTicks);
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

    /**
     * Caps a search counter at its own limit for display.
     *
     * <p>Checks run several at a time and the budget is tested before they report back, so the last
     * few can land after the limit has already been reached. The raw tally is honest about how much
     * work happened, but "attempts 72/64" in a log just reads as a broken counter to whoever runs
     * the server, so what gets shown stops at the ceiling they configured.</p>
     */
    static int displaySearchCount(int used, int limit) {
        if (limit <= 0) {
            return Math.max(0, used);
        }
        return Math.max(0, Math.min(used, limit));
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

    private long getCooldownRemainingMillis(Player player, String worldName) {
        if (player == null) {
            return 0L;
        }

        UUID playerId = player.getUniqueId();
        Map<String, Long> lastUses = lastRtpUseByPlayer.get(playerId);
        if (lastUses == null) {
            return 0L;
        }

        String key = normalizeWorldKey(worldName);
        Long lastUsedAt = lastUses.get(key);
        if (lastUsedAt == null) {
            return 0L;
        }

        int cooldownSeconds = getPlayerCooldownSeconds(player, worldName);
        long remaining = cooldownSeconds <= 0
                ? 0L
                : (lastUsedAt + (cooldownSeconds * 1000L)) - System.currentTimeMillis();
        if (remaining <= 0L) {
            lastUses.remove(key);
            if (lastUses.isEmpty()) {
                lastRtpUseByPlayer.remove(playerId);
            }
            return 0L;
        }
        return remaining;
    }

    private void markRtpUsed(UUID playerId, String worldName) {
        if (playerId == null) {
            return;
        }

        lastRtpUseByPlayer.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                .put(normalizeWorldKey(worldName), System.currentTimeMillis());
    }

    private boolean isQueueFull(UUID playerId) {
        int maxPlayers = getMaxConcurrentRtp();
        int inProgress = countActiveSearches() + plugin.getTeleportManager().countPendingByType("RTP");
        if (playerId != null) {
            if (isSearching(playerId)) {
                inProgress = Math.max(0, inProgress - 1);
            }
            if (plugin.getTeleportManager().hasPendingType(playerId, "RTP")) {
                inProgress = Math.max(0, inProgress - 1);
            }
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

    public boolean hasActiveRtpFlow(UUID playerId) {
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
                        "&eDo not move for &b{countdown}&e seconds or the teleport will be canceled."
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
