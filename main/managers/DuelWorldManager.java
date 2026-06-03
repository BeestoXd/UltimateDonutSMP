package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.DuelArena;
import com.bx.ultimateDonutSmp.models.DuelMapSelection;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class DuelWorldManager {

    public enum TerrainMode {
        FLAT,
        VANILLA
    }

    private static final String STATIC_WORLDS_PATH = "MAP_SOURCES.STATIC_WORLDS";
    private static final String RANDOM_BIOMES_PATH = "MAP_SOURCES.RANDOM_BIOMES";
    private static final String FLAT_POOL_PATH = RANDOM_BIOMES_PATH + ".FLAT_POOL";
    private static final String VANILLA_POOL_PATH = RANDOM_BIOMES_PATH + ".VANILLA_POOL";
    private static final String WORLDBORDER_PATH = "WORLDBORDER";

    private final UltimateDonutSmp plugin;
    private final Set<String> generatedWorldNames = new HashSet<>();
    private final Set<String> reusableFlatWorldNames = new HashSet<>();
    private final Set<String> leasedFlatWorldNames = new HashSet<>();
    private final Map<String, String> flatWorldBiomeKeys = new HashMap<>();
    private final Deque<GeneratedArena> readyFlatArenas = new ArrayDeque<>();
    private final Deque<Biome> requestedFlatBiomes = new ArrayDeque<>();
    private final Deque<GeneratedArena> readyVanillaArenas = new ArrayDeque<>();
    private final Deque<Biome> requestedVanillaBiomes = new ArrayDeque<>();
    private BukkitTask flatPoolTask;
    private long flatPoolTaskPeriodTicks = -1L;
    private VanillaPreparation activeVanillaPreparation;
    private BukkitTask vanillaPoolTask;
    private long vanillaPoolTaskPeriodTicks = -1L;
    private String lastInvalidTerrainModeWarning = "";
    private boolean asyncChunkLoadMethodChecked;
    private Method asyncChunkLoadMethod;

    public DuelWorldManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    private World createWorldWithoutSpawnMemory(WorldCreator creator) {
        World world = creator.createWorld();
        if (world != null) {
            world.setKeepSpawnInMemory(false);
        }
        return world;
    }

    public void loadConfiguredStaticWorlds() {
        FileConfiguration config = config();
        if (!config.getBoolean(STATIC_WORLDS_PATH + ".ENABLED", true)
                || !config.getBoolean(STATIC_WORLDS_PATH + ".AUTO_LOAD", true)) {
            return;
        }

        for (String worldName : config.getStringList(STATIC_WORLDS_PATH + ".WORLDS")) {
            if (worldName == null || worldName.isBlank() || Bukkit.getWorld(worldName.trim()) != null) {
                continue;
            }

            try {
                World world = createWorldWithoutSpawnMemory(WorldCreator.name(worldName.trim()));
                if (world != null) {
                    plugin.getLogger().info("Loaded duel static world: " + world.getName());
                }
            } catch (RuntimeException exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to load duel static world " + worldName, exception);
            }
        }
    }

    public boolean isRandomBiomesEnabled() {
        return config().getBoolean(RANDOM_BIOMES_PATH + ".ENABLED", true);
    }

    public TerrainMode getTerrainMode() {
        String raw = config().getString(RANDOM_BIOMES_PATH + ".TERRAIN_MODE", "FLAT");
        String normalized = raw == null ? "FLAT" : raw.trim().toUpperCase(Locale.ROOT);
        if ("VANILLA".equals(normalized)) {
            return TerrainMode.VANILLA;
        }
        if ("FLAT".equals(normalized) || normalized.isBlank()) {
            return TerrainMode.FLAT;
        }

        if (!normalized.equals(lastInvalidTerrainModeWarning)) {
            lastInvalidTerrainModeWarning = normalized;
            plugin.getLogger().warning("Invalid duel random biome TERRAIN_MODE '" + raw + "'. Falling back to FLAT.");
        }
        return TerrainMode.FLAT;
    }

    public boolean isFlatTerrainMode() {
        return getTerrainMode() == TerrainMode.FLAT;
    }

    public boolean isVanillaTerrainMode() {
        return getTerrainMode() == TerrainMode.VANILLA;
    }

    public boolean canPrepareGeneratedArenas() {
        return canPrepareFlatArenas() || canPrepareVanillaArenas();
    }

    public boolean canPrepareFlatArenas() {
        return isRandomBiomesEnabled()
                && isFlatPoolEnabled()
                && isFlatTerrainMode();
    }

    public boolean canPrepareVanillaArenas() {
        return isRandomBiomesEnabled()
                && isVanillaTerrainMode()
                && isVanillaPoolEnabled()
                && isVanillaRuntimeGenerationEnabled();
    }

    public List<Biome> getSelectableBiomes() {
        List<Biome> allBiomes = Registry.BIOME.stream()
                .filter(Objects::nonNull)
                .filter(biome -> biome != Biome.CUSTOM)
                .sorted(Comparator.comparing(this::biomeKey))
                .toList();

        Set<String> allowed = normalizeBiomeKeys(config().getStringList(RANDOM_BIOMES_PATH + ".ALLOWLIST"));
        Set<String> excluded = normalizeBiomeKeys(config().getStringList(RANDOM_BIOMES_PATH + ".EXCLUDE"));

        List<Biome> result = new ArrayList<>();
        for (Biome biome : allBiomes) {
            String key = biomeKey(biome);
            if (!allowed.isEmpty() && !allowed.contains(key)) {
                continue;
            }
            if (excluded.contains(key)) {
                continue;
            }
            result.add(biome);
        }
        return result;
    }

    public Optional<Biome> resolveBiome(String raw) {
        String normalized = normalizeBiomeKey(raw);
        if (normalized.isBlank()) {
            return Optional.empty();
        }

        NamespacedKey key = NamespacedKey.fromString(normalized);
        Biome biome = key == null ? null : Registry.BIOME.get(key);
        if (biome != null && biome != Biome.CUSTOM) {
            return Optional.of(biome);
        }

        try {
            biome = Biome.valueOf(normalized.substring(normalized.indexOf(':') + 1).toUpperCase(Locale.ROOT));
            return biome == Biome.CUSTOM ? Optional.empty() : Optional.of(biome);
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public GeneratedArena createGeneratedArena(DuelMapSelection selection) {
        if (!isRandomBiomesEnabled()) {
            return null;
        }

        Biome biome = resolveSelectedBiome(selection);
        if (biome == null) {
            return null;
        }

        if (isVanillaTerrainMode()) {
            GeneratedArena arena = takeReadyVanillaArena(selection, biome);
            if (arena != null) {
                ensureVanillaPool();
                return arena;
            }
            if (isVanillaRuntimeGenerationEnabled()) {
                requestVanillaArena(selection, biome);
                ensureVanillaPool();
                return null;
            }
            return null;
        }

        GeneratedArena flatArena = takeReadyFlatArena(selection, biome);
        if (flatArena != null) {
            ensureFlatPool();
            return flatArena;
        }
        requestFlatArena(selection, biome);
        ensureFlatPool();
        return null;
    }

    private GeneratedArena takeReadyFlatArena(DuelMapSelection selection, Biome biome) {
        if (readyFlatArenas.isEmpty()) {
            return null;
        }

        GeneratedArena selected = null;
        if (selection != null && selection.type() == DuelMapSelection.Type.BIOME && biome != null) {
            String requiredKey = biomeKey(biome);
            for (GeneratedArena arena : new ArrayList<>(readyFlatArenas)) {
                if (arena != null && arena.biomeKey().equalsIgnoreCase(requiredKey)) {
                    readyFlatArenas.remove(arena);
                    selected = arena;
                    break;
                }
            }
        }

        if (selected == null) {
            selected = readyFlatArenas.pollFirst();
        }
        if (selected != null) {
            leasedFlatWorldNames.add(selected.worldName());
        }
        return selected;
    }

    private void requestFlatArena(DuelMapSelection selection, Biome biome) {
        if (biome == null || selection == null || selection.type() != DuelMapSelection.Type.BIOME) {
            return;
        }
        if (hasReadyFlatArenaFor(biome) || isFlatBiomeRequested(biome)) {
            return;
        }
        requestedFlatBiomes.addLast(biome);
    }

    public void ensureFlatPool() {
        if (!canPrepareFlatArenas()) {
            shutdownFlatPool();
            return;
        }

        long periodTicks = getFlatPoolPrepareIntervalTicks();
        if (flatPoolTask != null && flatPoolTaskPeriodTicks == periodTicks) {
            return;
        }

        stopFlatPoolTask();
        flatPoolTaskPeriodTicks = periodTicks;
        flatPoolTask = plugin.getSpigotScheduler().runGlobalTimer(this::tickFlatPool, 1L, periodTicks);
    }

    public void shutdownFlatPool() {
        stopFlatPoolTask();
        for (GeneratedArena arena : new ArrayList<>(readyFlatArenas)) {
            deleteGeneratedWorld(arena.worldName());
        }
        readyFlatArenas.clear();
        requestedFlatBiomes.clear();
        reusableFlatWorldNames.clear();
        leasedFlatWorldNames.clear();
        flatWorldBiomeKeys.clear();
    }

    private void stopFlatPoolTask() {
        if (flatPoolTask != null) {
            flatPoolTask.cancel();
            flatPoolTask = null;
        }
        flatPoolTaskPeriodTicks = -1L;
    }

    private void tickFlatPool() {
        if (!canPrepareFlatArenas()) {
            stopFlatPoolTask();
            return;
        }

        Biome requestedBiome = requestedFlatBiomes.pollFirst();
        if (requestedBiome != null) {
            startFlatPreparation(requestedBiome);
            return;
        }

        int totalPrepared = readyFlatArenas.size() + leasedFlatWorldNames.size();
        if (totalPrepared < getFlatPoolSize()) {
            startFlatPreparation(resolveRandomPoolBiome());
        }
    }

    private void startFlatPreparation(Biome biome) {
        if (biome == null) {
            return;
        }

        String worldName = createGeneratedWorldName(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        World world;
        try {
            WorldCreator creator = WorldCreator.name(worldName)
                    .environment(World.Environment.NORMAL)
                    .generateStructures(config().getBoolean(RANDOM_BIOMES_PATH + ".GENERATE_STRUCTURES", false))
                    .biomeProvider(new SingleBiomeProvider(biome));
            creator.type(WorldType.FLAT)
                    .generator(new FlatBiomeChunkGenerator(biome));
            world = createWorldWithoutSpawnMemory(creator);
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to create generated duel biome world " + worldName, exception);
            return;
        }

        if (world == null) {
            return;
        }

        generatedWorldNames.add(world.getName());
        if (isFlatPoolReuseWorlds()) {
            reusableFlatWorldNames.add(world.getName());
        }
        configureGeneratedWorld(world, false);

        int spawnDistance = getSpawnDistance(getArenaRadius());
        Location firstSpawn = flatSpawn(world, -spawnDistance, -90F);
        Location secondSpawn = flatSpawn(world, spawnDistance, 90F);
        if (firstSpawn == null || secondSpawn == null) {
            cleanupGeneratedWorld(world.getName());
            return;
        }

        world.loadChunk(firstSpawn.getBlockX() >> 4, firstSpawn.getBlockZ() >> 4, true);
        world.loadChunk(secondSpawn.getBlockX() >> 4, secondSpawn.getBlockZ() >> 4, true);

        String biomeKey = biomeKey(biome);
        flatWorldBiomeKeys.put(world.getName(), biomeKey);
        DuelArena arena = new DuelArena(
                world.getName(),
                "Flat Arena",
                firstSpawn,
                secondSpawn,
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
        DuelMapSelection resolvedSelection = DuelMapSelection.biome(biomeKey);
        readyFlatArenas.addLast(new GeneratedArena(arena, resolvedSelection, biomeKey, world.getName(), TerrainMode.FLAT));
    }

    private Location flatSpawn(World world, int x, float yaw) {
        if (world == null) {
            return null;
        }
        Location location = new Location(world, x + 0.5D, FlatBiomeChunkGenerator.SURFACE_Y + 1D, 0.5D);
        location.setYaw(yaw);
        return location;
    }

    private GeneratedArena takeReadyVanillaArena(DuelMapSelection selection, Biome biome) {
        if (readyVanillaArenas.isEmpty()) {
            return null;
        }

        if (selection == null || selection.type() == DuelMapSelection.Type.RANDOM_BIOME) {
            return readyVanillaArenas.pollFirst();
        }

        String requiredKey = biomeKey(biome);
        for (GeneratedArena arena : new ArrayList<>(readyVanillaArenas)) {
            if (arena != null && arena.biomeKey().equalsIgnoreCase(requiredKey)) {
                readyVanillaArenas.remove(arena);
                return arena;
            }
        }
        return null;
    }

    private void requestVanillaArena(DuelMapSelection selection, Biome biome) {
        if (biome == null || selection == null || selection.type() != DuelMapSelection.Type.BIOME) {
            return;
        }
        if (hasReadyVanillaArenaFor(biome) || isVanillaPreparationFor(biome) || isVanillaBiomeRequested(biome)) {
            return;
        }
        requestedVanillaBiomes.addLast(biome);
    }

    public void ensureVanillaPool() {
        if (!canPrepareVanillaArenas()) {
            shutdownVanillaPool();
            return;
        }

        long periodTicks = getVanillaPoolPrepareIntervalTicks();
        if (vanillaPoolTask != null && vanillaPoolTaskPeriodTicks == periodTicks) {
            return;
        }

        stopVanillaPoolTask();
        vanillaPoolTaskPeriodTicks = periodTicks;
        vanillaPoolTask = plugin.getSpigotScheduler().runGlobalTimer(this::tickVanillaPool, 1L, periodTicks);
    }

    public void shutdownVanillaPool() {
        stopVanillaPoolTask();
        if (activeVanillaPreparation != null) {
            cleanupGeneratedWorld(activeVanillaPreparation.worldName());
            activeVanillaPreparation = null;
        }
        for (GeneratedArena arena : new ArrayList<>(readyVanillaArenas)) {
            cleanupGeneratedWorld(arena.worldName());
        }
        readyVanillaArenas.clear();
        requestedVanillaBiomes.clear();
    }

    private void stopVanillaPoolTask() {
        if (vanillaPoolTask != null) {
            vanillaPoolTask.cancel();
            vanillaPoolTask = null;
        }
        vanillaPoolTaskPeriodTicks = -1L;
    }

    private void tickVanillaPool() {
        if (!canPrepareVanillaArenas()) {
            stopVanillaPoolTask();
            return;
        }

        if (activeVanillaPreparation != null) {
            continueVanillaPreparation();
            return;
        }

        Biome requestedBiome = requestedVanillaBiomes.pollFirst();
        if (requestedBiome != null) {
            startVanillaPreparation(requestedBiome);
            return;
        }

        if (readyVanillaArenas.size() < getVanillaPoolSize()) {
            startVanillaPreparation(resolveRandomPoolBiome());
        }
    }

    private void startVanillaPreparation(Biome biome) {
        if (biome == null) {
            return;
        }

        String worldName = createGeneratedWorldName("vanilla_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        World world;
        try {
            WorldCreator creator = WorldCreator.name(worldName)
                    .environment(World.Environment.NORMAL)
                    .generateStructures(config().getBoolean(RANDOM_BIOMES_PATH + ".GENERATE_STRUCTURES", false))
                    .biomeProvider(new SingleBiomeProvider(biome));
            world = createWorldWithoutSpawnMemory(creator);
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to create prepared vanilla duel biome world " + worldName, exception);
            return;
        }

        if (world == null) {
            return;
        }

        generatedWorldNames.add(world.getName());
        configureGeneratedWorld(world, false);
        activeVanillaPreparation = new VanillaPreparation(world.getName(), biome, buildPreparationChunks());
    }

    private void continueVanillaPreparation() {
        VanillaPreparation preparation = activeVanillaPreparation;
        if (preparation == null) {
            return;
        }

        if (preparation.asyncChunkPending()) {
            return;
        }

        if (preparation.pauseTicks() > 0) {
            preparation.setPauseTicks(preparation.pauseTicks() - 1);
            return;
        }

        World world = Bukkit.getWorld(preparation.worldName());
        if (world == null) {
            activeVanillaPreparation = null;
            return;
        }

        int chunksPerTick = getVanillaPoolChunksPerTick();
        long maxStepNanos = Math.max(1L, getVanillaPoolMaxSyncStepMillis()) * 1_000_000L;
        long startedAt = System.nanoTime();

        for (int loaded = 0; loaded < chunksPerTick && !preparation.chunks().isEmpty(); loaded++) {
            ChunkCoord chunk = preparation.chunks().pollFirst();
            if (tryLoadPreparationChunkAsync(preparation, world, chunk)) {
                return;
            }
            world.loadChunk(chunk.x(), chunk.z(), true);
            world.setChunkForceLoaded(chunk.x(), chunk.z(), true);
            if (System.nanoTime() - startedAt >= maxStepNanos) {
                break;
            }
        }

        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L;
        if (elapsedMillis > getVanillaPoolMaxSyncStepMillis()) {
            if (!preparation.slowWarningSent()) {
                preparation.setSlowWarningSent(true);
                plugin.getLogger().warning("Duel vanilla arena preparation step took " + elapsedMillis
                        + "ms for " + preparation.worldName() + ". Preparation will continue throttled.");
            }
            if (isVanillaPoolPauseOnSlowStep()) {
                preparation.setPauseTicks(Math.max(1, getVanillaPoolPrepareIntervalTicks() * 5));
            }
        }

        if (preparation.chunks().isEmpty()) {
            finishVanillaPreparation(preparation, world);
        }
    }

    private boolean tryLoadPreparationChunkAsync(VanillaPreparation preparation, World world, ChunkCoord chunk) {
        Method method = getAsyncChunkLoadMethod(world);
        if (method == null || preparation == null || world == null || chunk == null) {
            return false;
        }

        Object result;
        try {
            result = method.invoke(world, chunk.x(), chunk.z(), true);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }

        if (!(result instanceof CompletableFuture<?> future)) {
            return false;
        }

        preparation.setAsyncChunkPending(true);
        future.whenComplete((loadedChunk, throwable) -> plugin.getSpigotScheduler().runGlobal(() -> {
            if (activeVanillaPreparation != preparation) {
                return;
            }

            preparation.setAsyncChunkPending(false);
            if (throwable != null) {
                plugin.getLogger().log(Level.WARNING,
                        "Async duel vanilla chunk preparation failed for " + preparation.worldName()
                                + " at " + chunk.x() + "," + chunk.z(),
                        throwable);
                cleanupGeneratedWorld(preparation.worldName());
                activeVanillaPreparation = null;
                return;
            }

            World preparedWorld = Bukkit.getWorld(preparation.worldName());
            if (preparedWorld != null) {
                preparedWorld.setChunkForceLoaded(chunk.x(), chunk.z(), true);
            }
            continueVanillaPreparation();
        }));
        return true;
    }

    private Method getAsyncChunkLoadMethod(World world) {
        if (asyncChunkLoadMethodChecked) {
            return asyncChunkLoadMethod;
        }

        asyncChunkLoadMethodChecked = true;
        if (world == null) {
            return null;
        }

        try {
            Method method = world.getClass().getMethod("getChunkAtAsync", int.class, int.class, boolean.class);
            if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
                asyncChunkLoadMethod = method;
            }
        } catch (NoSuchMethodException ignored) {
            asyncChunkLoadMethod = null;
        }
        return asyncChunkLoadMethod;
    }

    private void finishVanillaPreparation(VanillaPreparation preparation, World world) {
        int radius = getArenaRadius();
        int spawnDistance = getSpawnDistance(radius);
        Location firstSpawn = findSafeSpawn(world, -spawnDistance, 0, null);
        Location secondSpawn = findSafeSpawn(world, spawnDistance, 0, firstSpawn);
        if (firstSpawn == null) {
            firstSpawn = createVanillaFallbackSpawn(world, -spawnDistance, 0, -90F);
        }
        if (secondSpawn == null || firstSpawn.distanceSquared(secondSpawn) < 64D) {
            secondSpawn = createVanillaFallbackSpawn(world, spawnDistance, 0, 90F);
        }
        if (firstSpawn == null || secondSpawn == null) {
            plugin.getLogger().warning("Failed to find safe spawns for prepared vanilla duel world " + world.getName());
            activeVanillaPreparation = null;
            cleanupGeneratedWorld(world.getName());
            return;
        }

        firstSpawn.setYaw(-90F);
        secondSpawn.setYaw(90F);

        String biomeKey = biomeKey(preparation.biome());
        DuelArena arena = new DuelArena(
                world.getName(),
                "Biome: " + prettifyBiomeKey(biomeKey),
                firstSpawn,
                secondSpawn,
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
        readyVanillaArenas.addLast(new GeneratedArena(
                arena,
                DuelMapSelection.biome(biomeKey),
                biomeKey,
                world.getName(),
                TerrainMode.VANILLA
        ));
        activeVanillaPreparation = null;
    }

    public boolean isManagedGeneratedWorld(String worldName) {
        return worldName != null
                && (generatedWorldNames.contains(worldName)
                || generatedWorldBaseName(worldName).startsWith(getGeneratedWorldPrefix()));
    }

    public boolean shouldCleanupGeneratedWorlds() {
        return config().getBoolean(RANDOM_BIOMES_PATH + ".CLEANUP_AFTER_MATCH", true);
    }

    public void cleanupGeneratedWorld(String worldName) {
        if (worldName == null || worldName.isBlank() || !isManagedGeneratedWorld(worldName)) {
            return;
        }

        readyFlatArenas.removeIf(arena -> arena != null && worldName.equals(arena.worldName()));
        readyVanillaArenas.removeIf(arena -> arena != null && worldName.equals(arena.worldName()));
        leasedFlatWorldNames.remove(worldName);
        if (activeVanillaPreparation != null && worldName.equals(activeVanillaPreparation.worldName())) {
            activeVanillaPreparation = null;
        }

        World world = Bukkit.getWorld(worldName);
        if (world != null && shouldRecycleFlatWorld(worldName)) {
            recycleFlatWorld(world);
            return;
        }

        deleteGeneratedWorld(worldName);
    }

    private boolean shouldRecycleFlatWorld(String worldName) {
        return !shouldCleanupGeneratedWorlds() && isFlatPoolReuseWorlds() && reusableFlatWorldNames.contains(worldName);
    }

    private void recycleFlatWorld(World world) {
        if (world == null) {
            return;
        }

        clearForceLoadedChunks(world);
        configureGeneratedWorld(world, false);
        GeneratedArena arena = createFlatArenaForExistingWorld(world);
        if (arena != null) {
            readyFlatArenas.addLast(arena);
        } else {
            deleteGeneratedWorld(world.getName());
        }
    }

    private GeneratedArena createFlatArenaForExistingWorld(World world) {
        if (world == null) {
            return null;
        }

        String biomeKey = flatWorldBiomeKeys.getOrDefault(world.getName(), "");
        if (biomeKey.isBlank()) {
            biomeKey = "minecraft:plains";
        }

        int spawnDistance = getSpawnDistance(getArenaRadius());
        DuelArena arena = new DuelArena(
                world.getName(),
                "Flat Arena",
                flatSpawn(world, -spawnDistance, -90F),
                flatSpawn(world, spawnDistance, 90F),
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
        return new GeneratedArena(arena, DuelMapSelection.biome(biomeKey), biomeKey, world.getName(), TerrainMode.FLAT);
    }

    private void deleteGeneratedWorld(String worldName) {
        if (worldName == null || worldName.isBlank() || !isManagedGeneratedWorld(worldName)) {
            return;
        }

        readyFlatArenas.removeIf(arena -> arena != null && worldName.equals(arena.worldName()));
        readyVanillaArenas.removeIf(arena -> arena != null && worldName.equals(arena.worldName()));
        reusableFlatWorldNames.remove(worldName);
        leasedFlatWorldNames.remove(worldName);
        flatWorldBiomeKeys.remove(worldName);

        World world = Bukkit.getWorld(worldName);
        Path worldFolder = null;
        if (world != null) {
            clearForceLoadedChunks(world);
            worldFolder = world.getWorldFolder().toPath();
            Bukkit.unloadWorld(world, false);
        } else {
            worldFolder = Bukkit.getWorldContainer().toPath().resolve(worldName).normalize();
        }

        generatedWorldNames.remove(worldName);
        if (!shouldCleanupGeneratedWorlds() || worldFolder == null || !isSafeGeneratedWorldFolder(worldFolder, worldName)) {
            return;
        }

        Path folderToDelete = worldFolder;
        plugin.getSpigotScheduler().runAsync(() -> deleteDirectory(folderToDelete));
    }

    private void clearForceLoadedChunks(World world) {
        if (world == null) {
            return;
        }

        for (Chunk chunk : new ArrayList<>(world.getForceLoadedChunks())) {
            world.setChunkForceLoaded(chunk.getX(), chunk.getZ(), false);
        }
    }

    public void applyBorder(World world) {
        if (world == null || !config().getBoolean(WORLDBORDER_PATH + ".ENABLED", true)) {
            return;
        }

        WorldBorder border = world.getWorldBorder();
        int radius = getArenaRadius();
        border.setCenter(0.5D, 0.5D);
        border.setSize(Math.max(2D, config().getDouble(WORLDBORDER_PATH + ".SIZE", radius * 2D)));
        border.setDamageAmount(0D);
        border.setDamageBuffer(Math.max(0D, config().getDouble(WORLDBORDER_PATH + ".DAMAGE_BUFFER", 0D)));
        border.setWarningDistance(Math.max(0, config().getInt(WORLDBORDER_PATH + ".WARNING_DISTANCE", 4)));
        border.setWarningTime(Math.max(0, config().getInt(WORLDBORDER_PATH + ".WARNING_TIME", 5)));
    }

    public boolean isBorderEnabled() {
        return config().getBoolean(WORLDBORDER_PATH + ".ENABLED", true);
    }

    public int getBorderGraceTicks() {
        return Math.max(1, config().getInt(WORLDBORDER_PATH + ".ESCAPE_GRACE_TICKS", 40));
    }

    public String getBorderAction() {
        return config().getString(WORLDBORDER_PATH + ".ACTION", "PUSH_BACK").trim().toUpperCase(Locale.ROOT);
    }

    public String getBorderFallbackAction() {
        return config().getString(WORLDBORDER_PATH + ".FALLBACK_ACTION", "FORFEIT").trim().toUpperCase(Locale.ROOT);
    }

    public int getArenaRadius() {
        return Math.max(16, config().getInt(RANDOM_BIOMES_PATH + ".ARENA_RADIUS", 48));
    }

    public int getSpawnDistance() {
        return getSpawnDistance(getArenaRadius());
    }

    private int getSpawnDistance(int arenaRadius) {
        int configured = config().getInt(RANDOM_BIOMES_PATH + ".SPAWN_DISTANCE", 16);
        return Math.max(4, Math.min(Math.max(4, arenaRadius - 8), configured));
    }

    public String biomeKey(Biome biome) {
        if (biome == null) {
            return "";
        }
        NamespacedKey key = biome.getKey();
        return key == null ? "" : key.toString().toLowerCase(Locale.ROOT);
    }

    public String prettifyBiomeKey(String key) {
        String raw = key == null ? "" : key;
        int colon = raw.indexOf(':');
        if (colon >= 0) {
            raw = raw.substring(colon + 1);
        }

        String[] parts = raw.replace('-', '_').split("_");
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
        return builder.isEmpty() ? "Random Biome" : builder.toString();
    }

    private Biome resolveSelectedBiome(DuelMapSelection selection) {
        if (selection != null && selection.type() == DuelMapSelection.Type.BIOME) {
            Optional<Biome> configured = resolveBiome(selection.value());
            if (configured.isPresent() && getSelectableBiomes().contains(configured.get())) {
                return configured.get();
            }
            return null;
        }

        List<Biome> biomes = getSelectableBiomes();
        if (biomes.isEmpty()) {
            return null;
        }
        return biomes.get(ThreadLocalRandom.current().nextInt(biomes.size()));
    }

    private Biome resolveRandomPoolBiome() {
        List<Biome> biomes = getSelectableBiomes();
        if (biomes.isEmpty()) {
            return null;
        }
        return biomes.get(ThreadLocalRandom.current().nextInt(biomes.size()));
    }

    private Deque<ChunkCoord> buildPreparationChunks() {
        Deque<ChunkCoord> chunks = new ArrayDeque<>();
        int radius = getArenaRadius();
        int minChunk = Math.floorDiv(-radius, 16);
        int maxChunk = Math.floorDiv(radius, 16);
        for (int x = minChunk; x <= maxChunk; x++) {
            for (int z = minChunk; z <= maxChunk; z++) {
                chunks.addLast(new ChunkCoord(x, z));
            }
        }
        return chunks;
    }

    private boolean hasReadyVanillaArenaFor(Biome biome) {
        String key = biomeKey(biome);
        for (GeneratedArena arena : readyVanillaArenas) {
            if (arena != null && arena.biomeKey().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private boolean isVanillaPreparationFor(Biome biome) {
        return activeVanillaPreparation != null
                && activeVanillaPreparation.biomeKey().equalsIgnoreCase(biomeKey(biome));
    }

    private boolean isVanillaBiomeRequested(Biome biome) {
        String key = biomeKey(biome);
        for (Biome requested : requestedVanillaBiomes) {
            if (biomeKey(requested).equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasReadyFlatArenaFor(Biome biome) {
        String key = biomeKey(biome);
        for (GeneratedArena arena : readyFlatArenas) {
            if (arena != null && arena.biomeKey().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFlatBiomeRequested(Biome biome) {
        String key = biomeKey(biome);
        for (Biome requested : requestedFlatBiomes) {
            if (biomeKey(requested).equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFlatPoolEnabled() {
        return config().getBoolean(FLAT_POOL_PATH + ".ENABLED", true);
    }

    private boolean isFlatPoolReuseWorlds() {
        return config().getBoolean(FLAT_POOL_PATH + ".REUSE_WORLDS", true);
    }

    private int getFlatPoolSize() {
        return Math.max(1, config().getInt(FLAT_POOL_PATH + ".SIZE", 2));
    }

    private int getFlatPoolPrepareIntervalTicks() {
        return Math.max(20, config().getInt(FLAT_POOL_PATH + ".PREPARE_INTERVAL_TICKS", 20));
    }

    private boolean isVanillaPoolEnabled() {
        return config().getBoolean(VANILLA_POOL_PATH + ".ENABLED", true);
    }

    public boolean isVanillaRuntimeGenerationEnabled() {
        return config().getBoolean(VANILLA_POOL_PATH + ".RUNTIME_GENERATION", true);
    }

    private int getVanillaPoolSize() {
        return Math.max(1, config().getInt(VANILLA_POOL_PATH + ".SIZE", 2));
    }

    private int getVanillaPoolChunksPerTick() {
        return Math.max(1, config().getInt(VANILLA_POOL_PATH + ".CHUNKS_PER_TICK", 1));
    }

    private int getVanillaPoolPrepareIntervalTicks() {
        return Math.max(1, config().getInt(VANILLA_POOL_PATH + ".PREPARE_INTERVAL_TICKS", 1));
    }

    private int getVanillaPoolMaxSyncStepMillis() {
        return Math.max(2000, config().getInt(VANILLA_POOL_PATH + ".MAX_SYNC_STEP_MS", 2000));
    }

    private boolean isVanillaPoolPauseOnSlowStep() {
        return config().getBoolean(VANILLA_POOL_PATH + ".PAUSE_ON_SLOW_STEP", true);
    }

    private void configureGeneratedWorld(World world, boolean loadCenterChunk) {
        world.setPVP(true);
        world.setAutoSave(false);
        world.setKeepSpawnInMemory(false);
        applyBorder(world);
        if (loadCenterChunk) {
            world.loadChunk(0, 0, true);
        }
    }

    private Location findSafeSpawn(World world, int preferredX, int preferredZ, Location avoid) {
        int searchRadius = Math.max(4, config().getInt(RANDOM_BIOMES_PATH + ".SPAWN_SEARCH_RADIUS", 16));
        Location fallback = null;
        for (int radius = 0; radius <= searchRadius; radius += 4) {
            for (int x = preferredX - radius; x <= preferredX + radius; x += 4) {
                for (int z = preferredZ - radius; z <= preferredZ + radius; z += 4) {
                    if (Math.abs(x) > getArenaRadius() - 4 || Math.abs(z) > getArenaRadius() - 4) {
                        continue;
                    }
                    Location candidate = safeSpawnAt(world, x, z);
                    if (candidate == null) {
                        continue;
                    }
                    if (avoid != null && candidate.distanceSquared(avoid) < 64D) {
                        fallback = fallback == null ? candidate : fallback;
                        continue;
                    }
                    return candidate;
                }
            }
        }
        return fallback;
    }

    private Location safeSpawnAt(World world, int x, int z) {
        Block ground = world.getHighestBlockAt(x, z);
        if (ground == null || !isSafeGround(ground.getType())) {
            return null;
        }

        int y = ground.getY() + 1;
        if (y >= world.getMaxHeight() - 1) {
            return null;
        }

        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        if (!isSafeBody(feet.getType()) || !isSafeBody(head.getType())) {
            return null;
        }
        return new Location(world, x + 0.5D, y, z + 0.5D);
    }

    private Location createVanillaFallbackSpawn(World world, int x, int z, float yaw) {
        if (world == null) {
            return null;
        }

        int clampedX = Math.max(-getArenaRadius() + 6, Math.min(getArenaRadius() - 6, x));
        int clampedZ = Math.max(-getArenaRadius() + 6, Math.min(getArenaRadius() - 6, z));
        Block highest = world.getHighestBlockAt(clampedX, clampedZ);
        int platformY = highest == null ? 80 : highest.getY() + 2;
        platformY = Math.max(world.getMinHeight() + 4, Math.min(world.getMaxHeight() - 4, platformY));

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int blockX = clampedX + dx;
                int blockZ = clampedZ + dz;
                if (Math.abs(blockX) > getArenaRadius() - 2 || Math.abs(blockZ) > getArenaRadius() - 2) {
                    continue;
                }
                world.getBlockAt(blockX, platformY - 1, blockZ).setType(Material.STONE_BRICKS, false);
                world.getBlockAt(blockX, platformY, blockZ).setType(Material.AIR, false);
                world.getBlockAt(blockX, platformY + 1, blockZ).setType(Material.AIR, false);
                world.getBlockAt(blockX, platformY + 2, blockZ).setType(Material.AIR, false);
            }
        }

        Location spawn = new Location(world, clampedX + 0.5D, platformY, clampedZ + 0.5D);
        spawn.setYaw(yaw);
        return spawn;
    }

    private boolean isSafeGround(Material material) {
        if (material == null || material.isAir() || !material.isSolid()) {
            return false;
        }
        String name = material.name();
        return !name.contains("LAVA")
                && !name.contains("FIRE")
                && !name.contains("CACTUS")
                && !name.contains("MAGMA")
                && !name.contains("WATER");
    }

    private boolean isSafeBody(Material material) {
        return material != null && (material.isAir() || !material.isSolid());
    }

    private Set<String> normalizeBiomeKeys(List<String> rawKeys) {
        Set<String> keys = new HashSet<>();
        if (rawKeys == null) {
            return keys;
        }
        for (String raw : rawKeys) {
            String key = normalizeBiomeKey(raw);
            if (key.isBlank()) {
                continue;
            }
            if (resolveBiome(key).isPresent()) {
                keys.add(key);
            } else {
                plugin.getLogger().warning("Ignoring invalid duel biome key in config: " + raw);
            }
        }
        return keys;
    }

    private String normalizeBiomeKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        if (!normalized.contains(":")) {
            normalized = "minecraft:" + normalized;
        }
        return normalized;
    }

    private String getGeneratedWorldPrefix() {
        String prefix = config().getString(RANDOM_BIOMES_PATH + ".WORLD_PREFIX", "duel_biome_");
        return prefix == null || prefix.isBlank() ? "duel_biome_" : prefix.trim();
    }

    private String createGeneratedWorldName(String suffix) {
        ensureGeneratedWorldRoot();
        String baseName = getGeneratedWorldPrefix() + (suffix == null ? "" : suffix.trim());
        String folderName = getGeneratedWorldFolderName();
        return folderName.isBlank() ? baseName : folderName + "/" + baseName;
    }

    private void ensureGeneratedWorldRoot() {
        try {
            Files.createDirectories(getGeneratedWorldRoot());
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to create duel generated world folder " + getGeneratedWorldRoot(), exception);
        }
    }

    private Path getGeneratedWorldRoot() {
        return Bukkit.getWorldContainer().toPath().toAbsolutePath().normalize().resolve(getGeneratedWorldFolderName()).normalize();
    }

    private String getGeneratedWorldFolderName() {
        String configured = config().getString(RANDOM_BIOMES_PATH + ".WORLD_FOLDER", "duel");
        String folder = configured == null || configured.isBlank() ? "duel" : configured.trim().replace('\\', '/');
        while (folder.startsWith("/")) {
            folder = folder.substring(1);
        }
        while (folder.endsWith("/")) {
            folder = folder.substring(0, folder.length() - 1);
        }
        if (folder.isBlank() || folder.contains("..") || folder.contains(":")) {
            return "duel";
        }
        return folder;
    }

    private String generatedWorldBaseName(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return "";
        }
        String normalized = worldName.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        return slash >= 0 ? normalized.substring(slash + 1) : normalized;
    }

    private boolean isSafeGeneratedWorldFolder(Path folder, String worldName) {
        Path normalizedFolder = folder.toAbsolutePath().normalize();
        return generatedWorldBaseName(worldName).startsWith(getGeneratedWorldPrefix())
                && normalizedFolder.startsWith(getGeneratedWorldRoot());
    }

    private void deleteDirectory(Path folder) {
        try {
            if (!Files.exists(folder)) {
                return;
            }
            Files.walkFileTree(folder, new SimpleFileVisitor<>() {
                @Override
                public java.nio.file.FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return java.nio.file.FileVisitResult.CONTINUE;
                }

                @Override
                public java.nio.file.FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return java.nio.file.FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete generated duel world folder " + folder, exception);
        }
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getDuels();
    }

    public record GeneratedArena(
            DuelArena arena,
            DuelMapSelection selection,
            String biomeKey,
            String worldName,
            TerrainMode terrainMode
    ) {
    }

    private record ChunkCoord(int x, int z) {
    }

    private static final class VanillaPreparation {

        private final String worldName;
        private final Biome biome;
        private final String biomeKey;
        private final Deque<ChunkCoord> chunks;
        private int pauseTicks;
        private boolean slowWarningSent;
        private boolean asyncChunkPending;

        private VanillaPreparation(String worldName, Biome biome, Deque<ChunkCoord> chunks) {
            this.worldName = worldName;
            this.biome = biome;
            NamespacedKey key = biome == null ? null : biome.getKey();
            this.biomeKey = key == null ? "" : key.toString().toLowerCase(Locale.ROOT);
            this.chunks = chunks == null ? new ArrayDeque<>() : chunks;
        }

        private String worldName() {
            return worldName;
        }

        private Biome biome() {
            return biome;
        }

        private String biomeKey() {
            return biomeKey;
        }

        private Deque<ChunkCoord> chunks() {
            return chunks;
        }

        private int pauseTicks() {
            return pauseTicks;
        }

        private void setPauseTicks(int pauseTicks) {
            this.pauseTicks = Math.max(0, pauseTicks);
        }

        private boolean slowWarningSent() {
            return slowWarningSent;
        }

        private void setSlowWarningSent(boolean slowWarningSent) {
            this.slowWarningSent = slowWarningSent;
        }

        private boolean asyncChunkPending() {
            return asyncChunkPending;
        }

        private void setAsyncChunkPending(boolean asyncChunkPending) {
            this.asyncChunkPending = asyncChunkPending;
        }
    }

    private static final class SingleBiomeProvider extends BiomeProvider {

        private final Biome biome;

        private SingleBiomeProvider(Biome biome) {
            this.biome = biome;
        }

        @Override
        public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            return biome;
        }

        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            return List.of(biome);
        }
    }

    private static final class FlatBiomeChunkGenerator extends ChunkGenerator {

        private static final int SURFACE_Y = 64;
        private final SingleBiomeProvider biomeProvider;

        private FlatBiomeChunkGenerator(Biome biome) {
            this.biomeProvider = new SingleBiomeProvider(biome);
        }

        @Override
        public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
            return biomeProvider;
        }

        @Override
        public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            int minY = chunkData.getMinHeight();
            int maxY = chunkData.getMaxHeight();
            int bedrockY = Math.max(minY, Math.min(SURFACE_Y - 5, maxY - 1));
            int stoneEnd = Math.max(bedrockY + 1, Math.min(SURFACE_Y - 3, maxY));
            int dirtStart = Math.max(stoneEnd, Math.min(SURFACE_Y - 3, maxY));
            int surfaceY = Math.max(dirtStart, Math.min(SURFACE_Y, maxY - 1));

            chunkData.setRegion(0, bedrockY, 0, 16, bedrockY + 1, 16, Material.BEDROCK);
            if (stoneEnd > bedrockY + 1) {
                chunkData.setRegion(0, bedrockY + 1, 0, 16, stoneEnd, 16, Material.STONE);
            }
            if (surfaceY > dirtStart) {
                chunkData.setRegion(0, dirtStart, 0, 16, surfaceY, 16, Material.DIRT);
            }
            chunkData.setRegion(0, surfaceY, 0, 16, surfaceY + 1, 16, Material.GRASS_BLOCK);
        }

        @Override
        public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, HeightMap heightMap) {
            return SURFACE_Y + 1;
        }

        @Override
        public Location getFixedSpawnLocation(World world, Random random) {
            return new Location(world, 0.5D, SURFACE_Y + 1, 0.5D);
        }

        @Override
        public boolean canSpawn(World world, int x, int z) {
            return true;
        }

        @Override
        public boolean shouldGenerateNoise() {
            return true;
        }

        @Override
        public boolean shouldGenerateSurface() {
            return false;
        }

        @Override
        public boolean shouldGenerateBedrock() {
            return false;
        }

        @Override
        public boolean shouldGenerateCaves() {
            return false;
        }

        @Override
        public boolean shouldGenerateDecorations() {
            return false;
        }

        @Override
        public boolean shouldGenerateMobs() {
            return false;
        }

        @Override
        public boolean shouldGenerateStructures() {
            return false;
        }
    }
}
