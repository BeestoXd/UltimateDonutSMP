package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lidded;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CrateVisualManager {

    private static final String HOLOGRAM_TAG = "uds_crate_hologram";
    private static final String PERSONAL_TAG = "uds_crate_personal_hologram";

    private final UltimateDonutSmp plugin;
    private final Map<CrateManager.CrateBlockKey, List<UUID>> holograms = new ConcurrentHashMap<>();
    private final Map<UUID, Map<CrateManager.CrateBlockKey, UUID>> personalKeyDisplays = new ConcurrentHashMap<>();
    private final Map<UUID, Map<CrateManager.CrateBlockKey, String>> personalKeyDisplayTexts = new ConcurrentHashMap<>();
    private final AtomicInteger lifecycleGeneration = new AtomicInteger();

    private ScheduledTask visualTask;
    private int visualPulse;

    public CrateVisualManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        stopVisualTask();
        int generation = lifecycleGeneration.incrementAndGet();
        Map<CrateManager.CrateBlockKey, CrateManager.CrateDefinition> boundCrates = snapshotBoundCrates();
        Map<CrateManager.CrateBlockKey, List<UUID>> globalSnapshot = snapshotGlobalHolograms();
        Map<CrateManager.CrateBlockKey, List<UUID>> personalSnapshot = snapshotPersonalKeyDisplaysByCrate();
        Set<CrateManager.CrateBlockKey> keys = collectKnownKeys(boundCrates.keySet(), globalSnapshot.keySet(), personalSnapshot.keySet());

        holograms.clear();
        personalKeyDisplays.clear();
        personalKeyDisplayTexts.clear();
        visualPulse = 0;

        for (CrateManager.CrateBlockKey key : keys) {
            CrateManager.CrateDefinition crate = boundCrates.get(key);
            List<UUID> entityIds = combineTrackedIds(key, globalSnapshot, personalSnapshot);
            runAtCrateRegion(key, () -> {
                if (isStale(generation)) {
                    return;
                }

                removeEntityIdsNow(entityIds);
                purgeNearbyDisplaysForCrateNow(key);
                if (crate != null) {
                    spawnHologramNow(key, crate);
                }
            });
        }

        startVisualTask();
    }

    public void shutdown() {
        stopVisualTask();
        int generation = lifecycleGeneration.incrementAndGet();
        Map<CrateManager.CrateBlockKey, CrateManager.CrateDefinition> boundCrates = snapshotBoundCrates();
        Map<CrateManager.CrateBlockKey, List<UUID>> globalSnapshot = snapshotGlobalHolograms();
        Map<CrateManager.CrateBlockKey, List<UUID>> personalSnapshot = snapshotPersonalKeyDisplaysByCrate();
        Set<CrateManager.CrateBlockKey> keys = collectKnownKeys(boundCrates.keySet(), globalSnapshot.keySet(), personalSnapshot.keySet());

        holograms.clear();
        personalKeyDisplays.clear();
        personalKeyDisplayTexts.clear();

        for (CrateManager.CrateBlockKey key : keys) {
            List<UUID> entityIds = combineTrackedIds(key, globalSnapshot, personalSnapshot);
            runAtCrateRegion(key, () -> {
                if (isStale(generation)) {
                    return;
                }

                removeEntityIdsNow(entityIds);
                purgeNearbyDisplaysForCrateNow(key);
            });
        }
    }

    public void handleJoin(Player player) {
        // Personal crate displays are hidden by default, so joins do not need per-entity hide calls.
    }

    public void handleQuit(UUID playerId) {
        if (playerId != null) {
            scheduleClearPersonalKeyDisplays(playerId);
        }
    }

    public void refreshHologram(Block block) {
        if (block == null || block.getWorld() == null) {
            return;
        }

        scheduleRefreshHologram(toKey(block));
    }

    public void removeHologram(Block block) {
        if (block == null || block.getWorld() == null) {
            return;
        }

        scheduleRemoveHologram(toKey(block));
    }

    public void playOpenEffects(Player player, Block block, CrateManager.CrateDefinition crate) {
        if (player == null || block == null || crate == null || block.getWorld() == null) {
            return;
        }

        String sound = plugin.getConfigManager().getSounds().getString(
                "CRATES.OPEN",
                "minecraft:block.ender_chest.open|1.0|1.05"
        );
        plugin.getFoliaScheduler().runEntity(player, () -> {
            if (player.isOnline()) {
                SoundUtils.play(player, sound);
            }
        });

        CrateManager.CrateBlockKey key = toKey(block);
        runAtCrateRegion(key, () -> playOpenEffectsNow(key));
    }

    public void playNoKeyEffects(Player player) {
        if (player == null) {
            return;
        }

        String sound = plugin.getConfigManager().getSounds().getString(
                "CRATES.NO-KEY",
                "minecraft:entity.villager.no|1.0|1.0"
        );
        plugin.getFoliaScheduler().runEntity(player, () -> {
            if (player.isOnline()) {
                SoundUtils.play(player, sound);
            }
        });
    }

    public void playClaimEffects(Player player, CrateManager.CrateDefinition crate) {
        if (player == null || crate == null) {
            return;
        }

        String sound = plugin.getConfigManager().getSounds().getString(
                "CRATES.CLAIM",
                "minecraft:entity.player.levelup|1.0|1.25"
        );
        plugin.getFoliaScheduler().runEntity(player, () -> {
            if (!player.isOnline() || player.getWorld() == null) {
                return;
            }

            SoundUtils.play(player, sound);
            Location origin = player.getLocation().add(0, 1.0, 0);
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, origin, 20, 0.4, 0.6, 0.4, 0.05);
            player.getWorld().spawnParticle(Particle.GLOW, origin, 15, 0.35, 0.5, 0.35, 0.0);
        });
    }

    private void startVisualTask() {
        long updateTicks = getHologramUpdateTicks();
        visualTask = plugin.getFoliaScheduler().runGlobalTimer(this::dispatchVisualTick, updateTicks, updateTicks);
    }

    private void stopVisualTask() {
        if (visualTask != null) {
            visualTask.cancel();
            visualTask = null;
        }
    }

    private void dispatchVisualTick() {
        int generation = lifecycleGeneration.get();
        visualPulse++;

        Map<CrateManager.CrateBlockKey, CrateManager.CrateDefinition> boundCrates = snapshotBoundCrates();
        boolean particlesEnabled = plugin.getConfigManager().getCrates().getBoolean("SETTINGS.PARTICLES.ENABLED", true);
        Particle particle = parseParticle(
                plugin.getConfigManager().getCrates().getString("SETTINGS.PARTICLES.TYPE", "ENCHANT")
        );
        int particleCount = Math.max(1, plugin.getConfigManager().getCrates().getInt("SETTINGS.PARTICLES.COUNT", 4));

        for (Map.Entry<CrateManager.CrateBlockKey, CrateManager.CrateDefinition> entry : boundCrates.entrySet()) {
            CrateManager.CrateBlockKey key = entry.getKey();
            CrateManager.CrateDefinition crate = entry.getValue();
            runAtCrateRegion(key, () -> {
                if (isStale(generation)) {
                    return;
                }

                if (particlesEnabled) {
                    spawnIdleParticlesNow(key, particle, particleCount);
                }
                ensureGlobalHologramNow(key, crate);
            });
        }

        updatePersonalKeyDisplays(generation, boundCrates);
    }

    private void scheduleRefreshHologram(CrateManager.CrateBlockKey key) {
        int generation = lifecycleGeneration.get();
        CrateManager.CrateDefinition crate = snapshotBoundCrates().get(key);
        List<UUID> entityIds = removeTrackedDisplaysForCrate(key);

        runAtCrateRegion(key, () -> {
            if (isStale(generation)) {
                return;
            }

            removeEntityIdsNow(entityIds);
            purgeNearbyDisplaysForCrateNow(key);
            if (crate != null) {
                spawnHologramNow(key, crate);
            }
        });
    }

    private void scheduleRemoveHologram(CrateManager.CrateBlockKey key) {
        int generation = lifecycleGeneration.get();
        List<UUID> entityIds = removeTrackedDisplaysForCrate(key);

        runAtCrateRegion(key, () -> {
            if (isStale(generation)) {
                return;
            }

            removeEntityIdsNow(entityIds);
            purgeNearbyDisplaysForCrateNow(key);
        });
    }

    private void spawnHologramNow(CrateManager.CrateBlockKey key, CrateManager.CrateDefinition crate) {
        removeTrackedGlobalHologramNow(key);

        List<String> lines = getHologramLines(crate);
        if (lines.isEmpty()) {
            return;
        }

        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return;
        }

        List<UUID> entityIds = new ArrayList<>();
        double baseY = key.y() + getHologramOffsetY();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Location location = new Location(world, key.x() + 0.5, baseY - (i * 0.27), key.z() + 0.5);
            TextDisplay display = world.spawn(location, TextDisplay.class, textDisplay -> {
                textDisplay.text(ColorUtils.toComponent(line));
                configureHologramDisplay(textDisplay);
                textDisplay.addScoreboardTag(HOLOGRAM_TAG);
                textDisplay.getPersistentDataContainer().set(
                        plugin.getKey("crate_hologram"),
                        PersistentDataType.STRING,
                        formatBlockKey(key)
                );
            });
            entityIds.add(display.getUniqueId());
        }

        holograms.put(key, List.copyOf(entityIds));
    }

    private void ensureGlobalHologramNow(CrateManager.CrateBlockKey key, CrateManager.CrateDefinition crate) {
        int requiredLines = getHologramLines(crate).size();
        if (requiredLines <= 0) {
            removeTrackedGlobalHologramNow(key);
            return;
        }

        if (!hasValidGlobalHologramNow(key, requiredLines)) {
            spawnHologramNow(key, crate);
        }
    }

    private boolean hasValidGlobalHologramNow(CrateManager.CrateBlockKey key, int requiredLines) {
        List<UUID> trackedIds = holograms.get(key);
        if (trackedIds == null || trackedIds.size() != requiredLines) {
            removeTrackedGlobalHologramNow(key);
            return false;
        }

        int validCount = 0;
        for (UUID entityId : trackedIds) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity instanceof TextDisplay textDisplay
                    && entity.isValid()
                    && textDisplay.getScoreboardTags().contains(HOLOGRAM_TAG)) {
                validCount++;
            }
        }

        if (validCount == requiredLines) {
            return true;
        }

        removeTrackedGlobalHologramNow(key);
        return false;
    }

    private void updatePersonalKeyDisplays(
            int generation,
            Map<CrateManager.CrateBlockKey, CrateManager.CrateDefinition> boundCrates
    ) {
        double viewDistance = getHologramViewDistance();
        boolean unlimitedDistance = viewDistance <= 0D;
        double maxDistanceSquared = unlimitedDistance ? 0D : Math.pow(viewDistance, 2);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.getFoliaScheduler().runEntity(player, () -> updatePersonalKeyDisplaysForPlayer(
                    generation,
                    player,
                    boundCrates,
                    unlimitedDistance,
                    maxDistanceSquared
            ));
        }
    }

    private void updatePersonalKeyDisplaysForPlayer(
            int generation,
            Player player,
            Map<CrateManager.CrateBlockKey, CrateManager.CrateDefinition> boundCrates,
            boolean unlimitedDistance,
            double maxDistanceSquared
    ) {
        if (isStale(generation)) {
            return;
        }

        UUID viewerId = player.getUniqueId();
        if (!player.isOnline() || player.getWorld() == null) {
            scheduleClearPersonalKeyDisplays(viewerId);
            return;
        }

        Set<CrateManager.CrateBlockKey> active = new HashSet<>();
        World playerWorld = player.getWorld();
        Location playerLocation = player.getLocation();

        for (Map.Entry<CrateManager.CrateBlockKey, CrateManager.CrateDefinition> entry : boundCrates.entrySet()) {
            CrateManager.CrateBlockKey key = entry.getKey();
            if (!playerWorld.getName().equals(key.world())) {
                continue;
            }

            Location crateLocation = new Location(playerWorld, key.x() + 0.5, key.y() + 0.5, key.z() + 0.5);
            double distanceSquared = playerLocation.distanceSquared(crateLocation);
            if (!unlimitedDistance && distanceSquared > maxDistanceSquared) {
                continue;
            }

            CrateManager.CrateDefinition crate = entry.getValue();
            int keys = plugin.getCrateManager().getKeyBalance(player, crate.id());
            active.add(key);
            scheduleUpsertPersonalKeyDisplay(generation, viewerId, key, crate, getKeyLine(crate, keys));
        }

        Map<CrateManager.CrateBlockKey, UUID> playerDisplays = personalKeyDisplays.get(viewerId);
        if (playerDisplays == null) {
            return;
        }

        for (CrateManager.CrateBlockKey key : new HashSet<>(playerDisplays.keySet())) {
            if (!active.contains(key)) {
                scheduleRemovePersonalKeyDisplay(generation, viewerId, key);
            }
        }
    }

    private void scheduleUpsertPersonalKeyDisplay(
            int generation,
            UUID viewerId,
            CrateManager.CrateBlockKey key,
            CrateManager.CrateDefinition crate,
            String line
    ) {
        runAtCrateRegion(key, () -> {
            if (isStale(generation)) {
                return;
            }

            upsertPersonalKeyDisplayNow(viewerId, key, crate, line);
        });
    }

    private void upsertPersonalKeyDisplayNow(
            UUID viewerId,
            CrateManager.CrateBlockKey key,
            CrateManager.CrateDefinition crate,
            String line
    ) {
        if (Bukkit.getPlayer(viewerId) == null) {
            return;
        }

        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return;
        }

        Map<CrateManager.CrateBlockKey, UUID> playerDisplays = personalKeyDisplays.computeIfAbsent(
                viewerId,
                ignored -> new ConcurrentHashMap<>()
        );
        Map<CrateManager.CrateBlockKey, String> playerDisplayTexts = personalKeyDisplayTexts.computeIfAbsent(
                viewerId,
                ignored -> new ConcurrentHashMap<>()
        );

        TextDisplay display = null;
        boolean shouldShowToOwner = false;
        UUID existingId = playerDisplays.get(key);
        if (existingId != null) {
            Entity entity = Bukkit.getEntity(existingId);
            if (entity instanceof TextDisplay textDisplay
                    && entity.isValid()
                    && textDisplay.getScoreboardTags().contains(PERSONAL_TAG)) {
                display = textDisplay;
                shouldShowToOwner = !playerDisplayTexts.containsKey(key);
            } else {
                playerDisplays.remove(key);
                playerDisplayTexts.remove(key);
            }
        }

        if (display == null) {
            double baseY = key.y() + getHologramOffsetY() - (getHologramLines(crate).size() * 0.27D);
            Location location = new Location(world, key.x() + 0.5, baseY, key.z() + 0.5);
            display = world.spawn(location, TextDisplay.class, textDisplay -> {
                configureHologramDisplay(textDisplay);
                textDisplay.setVisibleByDefault(false);
                textDisplay.addScoreboardTag(PERSONAL_TAG);
                textDisplay.getPersistentDataContainer().set(
                        plugin.getKey("crate_hologram"),
                        PersistentDataType.STRING,
                        formatBlockKey(key)
                );
                textDisplay.getPersistentDataContainer().set(
                        plugin.getKey("crate_hologram_owner"),
                        PersistentDataType.STRING,
                        viewerId.toString()
                );
            });
            playerDisplays.put(key, display.getUniqueId());
            shouldShowToOwner = true;
        }

        if (display.isVisibleByDefault()) {
            display.setVisibleByDefault(false);
        }
        if (!line.equals(playerDisplayTexts.get(key))) {
            display.text(ColorUtils.toComponent(line));
            playerDisplayTexts.put(key, line);
        }
        if (shouldShowToOwner) {
            TextDisplay displayToShow = display;
            Player owner = Bukkit.getPlayer(viewerId);
            if (owner != null) {
                plugin.getFoliaScheduler().runEntity(owner, () -> {
                    if (owner.isOnline()) {
                        owner.showEntity(plugin, displayToShow);
                    }
                });
            }
        }
    }

    private void scheduleClearPersonalKeyDisplays(UUID viewerId) {
        Map<CrateManager.CrateBlockKey, UUID> displays = personalKeyDisplays.remove(viewerId);
        personalKeyDisplayTexts.remove(viewerId);
        if (displays == null) {
            return;
        }

        for (Map.Entry<CrateManager.CrateBlockKey, UUID> entry : displays.entrySet()) {
            CrateManager.CrateBlockKey key = entry.getKey();
            UUID entityId = entry.getValue();
            runAtCrateRegion(key, () -> removeEntityIdNow(entityId));
        }
    }

    private void scheduleRemovePersonalKeyDisplay(int generation, UUID viewerId, CrateManager.CrateBlockKey key) {
        Map<CrateManager.CrateBlockKey, UUID> displays = personalKeyDisplays.get(viewerId);
        if (displays == null) {
            return;
        }

        UUID entityId = displays.remove(key);
        Map<CrateManager.CrateBlockKey, String> textCache = personalKeyDisplayTexts.get(viewerId);
        if (textCache != null) {
            textCache.remove(key);
            if (textCache.isEmpty()) {
                personalKeyDisplayTexts.remove(viewerId, textCache);
            }
        }
        if (displays.isEmpty()) {
            personalKeyDisplays.remove(viewerId, displays);
        }
        if (entityId == null) {
            return;
        }

        runAtCrateRegion(key, () -> {
            if (!isStale(generation)) {
                removeEntityIdNow(entityId);
            }
        });
    }

    private List<UUID> removeTrackedDisplaysForCrate(CrateManager.CrateBlockKey key) {
        List<UUID> entityIds = new ArrayList<>();

        List<UUID> globalIds = holograms.remove(key);
        if (globalIds != null) {
            entityIds.addAll(globalIds);
        }

        for (Map.Entry<UUID, Map<CrateManager.CrateBlockKey, UUID>> entry : personalKeyDisplays.entrySet()) {
            UUID personalId = entry.getValue().remove(key);
            Map<CrateManager.CrateBlockKey, String> textCache = personalKeyDisplayTexts.get(entry.getKey());
            if (textCache != null) {
                textCache.remove(key);
                if (textCache.isEmpty()) {
                    personalKeyDisplayTexts.remove(entry.getKey(), textCache);
                }
            }
            if (entry.getValue().isEmpty()) {
                personalKeyDisplays.remove(entry.getKey(), entry.getValue());
            }
            if (personalId != null) {
                entityIds.add(personalId);
            }
        }

        return entityIds;
    }

    private void removeTrackedGlobalHologramNow(CrateManager.CrateBlockKey key) {
        List<UUID> entityIds = holograms.remove(key);
        if (entityIds != null) {
            removeEntityIdsNow(entityIds);
        }
    }

    private void removeEntityIdsNow(List<UUID> entityIds) {
        if (entityIds == null || entityIds.isEmpty()) {
            return;
        }

        for (UUID entityId : new HashSet<>(entityIds)) {
            removeEntityIdNow(entityId);
        }
    }

    private void removeEntityIdNow(UUID entityId) {
        if (entityId == null) {
            return;
        }

        Entity entity = Bukkit.getEntity(entityId);
        if (entity != null && entity.isValid()) {
            entity.remove();
        }
    }

    private void purgeNearbyDisplaysForCrateNow(CrateManager.CrateBlockKey key) {
        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return;
        }

        Location center = new Location(world, key.x() + 0.5, key.y() + getHologramOffsetY() - 0.35, key.z() + 0.5);
        for (Entity entity : world.getNearbyEntities(center, 0.4, 1.5, 0.4, candidate -> candidate instanceof TextDisplay)) {
            if (isAtCrateHologramColumn(entity, key)) {
                entity.remove();
            }
        }
    }

    private void spawnIdleParticlesNow(CrateManager.CrateBlockKey key, Particle particle, int count) {
        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return;
        }

        Location location = new Location(world, key.x() + 0.5, key.y() + 1.05, key.z() + 0.5);
        world.spawnParticle(particle, location, count, 0.18, 0.25, 0.18, 0.02);
        world.spawnParticle(Particle.GLOW, location.clone().add(0, 0.15, 0), 1, 0.06, 0.06, 0.06, 0.0);
    }

    private void playOpenEffectsNow(CrateManager.CrateBlockKey key) {
        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return;
        }

        Location origin = new Location(world, key.x() + 0.5, key.y() + 1.0, key.z() + 0.5);
        world.spawnParticle(Particle.END_ROD, origin, 10, 0.25, 0.35, 0.25, 0.01);
        world.spawnParticle(Particle.GLOW, origin.clone().add(0, 0.25, 0), 8, 0.3, 0.3, 0.3, 0.0);
        animateLidNow(world.getBlockAt(key.x(), key.y(), key.z()));
    }

    private void animateLidNow(Block block) {
        BlockState state = block.getState();
        if (!(state instanceof Lidded lidded)) {
            return;
        }

        try {
            lidded.open();
            plugin.getFoliaScheduler().runRegionLater(block.getLocation(), () -> {
                BlockState latest = block.getState();
                if (latest instanceof Lidded latestLidded) {
                    latestLidded.close();
                }
            }, 20L);
        } catch (Exception ignored) {
        }
    }

    private ScheduledTask runAtCrateRegion(CrateManager.CrateBlockKey key, Runnable runnable) {
        if (key == null) {
            return null;
        }

        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return null;
        }

        return plugin.getFoliaScheduler().runRegion(world, key.x() >> 4, key.z() >> 4, runnable);
    }

    private Map<CrateManager.CrateBlockKey, CrateManager.CrateDefinition> snapshotBoundCrates() {
        if (plugin.getCrateManager() == null) {
            return Map.of();
        }

        Map<CrateManager.CrateBlockKey, String> boundBlockIds = new HashMap<>(plugin.getCrateManager().getBoundBlockIds());
        Map<CrateManager.CrateBlockKey, CrateManager.CrateDefinition> snapshot = new HashMap<>();
        for (Map.Entry<CrateManager.CrateBlockKey, String> entry : boundBlockIds.entrySet()) {
            CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(entry.getValue());
            if (crate != null) {
                snapshot.put(entry.getKey(), crate);
            }
        }
        return snapshot;
    }

    private Map<CrateManager.CrateBlockKey, List<UUID>> snapshotGlobalHolograms() {
        Map<CrateManager.CrateBlockKey, List<UUID>> snapshot = new HashMap<>();
        for (Map.Entry<CrateManager.CrateBlockKey, List<UUID>> entry : holograms.entrySet()) {
            snapshot.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return snapshot;
    }

    private Map<CrateManager.CrateBlockKey, List<UUID>> snapshotPersonalKeyDisplaysByCrate() {
        Map<CrateManager.CrateBlockKey, List<UUID>> snapshot = new HashMap<>();
        for (Map<CrateManager.CrateBlockKey, UUID> displays : personalKeyDisplays.values()) {
            for (Map.Entry<CrateManager.CrateBlockKey, UUID> entry : displays.entrySet()) {
                snapshot.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(entry.getValue());
            }
        }
        return snapshot;
    }

    @SafeVarargs
    private final Set<CrateManager.CrateBlockKey> collectKnownKeys(Set<CrateManager.CrateBlockKey>... keySets) {
        Set<CrateManager.CrateBlockKey> keys = new HashSet<>();
        for (Set<CrateManager.CrateBlockKey> keySet : keySets) {
            keys.addAll(keySet);
        }
        return keys;
    }

    private List<UUID> combineTrackedIds(
            CrateManager.CrateBlockKey key,
            Map<CrateManager.CrateBlockKey, List<UUID>> globalSnapshot,
            Map<CrateManager.CrateBlockKey, List<UUID>> personalSnapshot
    ) {
        List<UUID> entityIds = new ArrayList<>();
        List<UUID> globalIds = globalSnapshot.get(key);
        if (globalIds != null) {
            entityIds.addAll(globalIds);
        }

        List<UUID> personalIds = personalSnapshot.get(key);
        if (personalIds != null) {
            entityIds.addAll(personalIds);
        }
        return entityIds;
    }

    private boolean isStale(int generation) {
        return generation != lifecycleGeneration.get();
    }

    private void configureHologramDisplay(TextDisplay textDisplay) {
        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.setDefaultBackground(false);
        textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        textDisplay.setSeeThrough(true);
        textDisplay.setShadowed(false);
        textDisplay.setViewRange(getDisplayViewRange());
        textDisplay.setPersistent(false);
    }

    private List<String> getHologramLines(CrateManager.CrateDefinition crate) {
        FileConfiguration cratesConfig = plugin.getConfigManager().getCrates();
        List<String> lines = cratesConfig.getStringList("SETTINGS.HOLOGRAM.LINES");
        if (lines.isEmpty()) {
            lines = List.of(
                    "{crate}",
                    "&7Ê€ÉªÉ¢Êœá´›-á´„ÊŸÉªá´„á´‹ á´›á´ á´á´˜á´‡É´"
            );
        }

        List<String> resolved = new ArrayList<>();
        for (String line : lines) {
            resolved.add(line
                    .replace("{crate}", crate.display().displayName())
                    .replace("{crate_id}", crate.id()));
        }
        return resolved;
    }

    private String getKeyLine(CrateManager.CrateDefinition crate, int keys) {
        String template = plugin.getConfigManager().getCrates().getString(
                "SETTINGS.HOLOGRAM.KEY-LINE",
                "&7á´‹á´‡ÊÑ•: &f{keys}"
        );
        return template
                .replace("{crate}", crate.display().displayName())
                .replace("{crate_id}", crate.id())
                .replace("{keys}", String.valueOf(keys));
    }

    private float getDisplayViewRange() {
        return 1000.0F;
    }

    private double getHologramOffsetY() {
        return plugin.getConfigManager().getCrates().getDouble("SETTINGS.HOLOGRAM.OFFSET-Y", 1.6D);
    }

    private long getHologramUpdateTicks() {
        return Math.max(1L, plugin.getConfigManager().getCrates().getLong("SETTINGS.HOLOGRAM.UPDATE-TICKS", 20L));
    }

    private double getHologramViewDistance() {
        return plugin.getConfigManager().getCrates().getDouble("SETTINGS.HOLOGRAM.VIEW-DISTANCE", 24D);
    }

    private Particle parseParticle(String particleName) {
        if (particleName == null || particleName.isBlank()) {
            return Particle.ENCHANT;
        }

        try {
            return Particle.valueOf(particleName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Particle.ENCHANT;
        }
    }

    private CrateManager.CrateBlockKey toKey(Block block) {
        return new CrateManager.CrateBlockKey(
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()
        );
    }

    private String formatBlockKey(CrateManager.CrateBlockKey key) {
        return key.world().toLowerCase(Locale.ROOT) + ":" + key.x() + ":" + key.y() + ":" + key.z();
    }

    private boolean isAtCrateHologramColumn(Entity entity, CrateManager.CrateBlockKey key) {
        if (entity == null || entity.getWorld() == null || !entity.getWorld().getName().equals(key.world())) {
            return false;
        }

        Location location = entity.getLocation();
        double deltaX = Math.abs(location.getX() - (key.x() + 0.5D));
        double deltaZ = Math.abs(location.getZ() - (key.z() + 0.5D));
        double minY = key.y() + getHologramOffsetY() - 1.1D;
        double maxY = key.y() + getHologramOffsetY() + 0.25D;

        return deltaX <= 0.2D
                && deltaZ <= 0.2D
                && location.getY() >= minY
                && location.getY() <= maxY;
    }
}
