package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CrateVisualManager {

    private static final String HOLOGRAM_TAG = "uds_crate_hologram";
    private static final String PERSONAL_TAG = "uds_crate_personal_hologram";

    private final UltimateDonutSmp plugin;
    private final Map<CrateManager.CrateBlockKey, List<UUID>> holograms = new ConcurrentHashMap<>();
    private final Map<UUID, Map<CrateManager.CrateBlockKey, List<UUID>>> personalLineDisplays = new ConcurrentHashMap<>();
    private final Map<UUID, Map<CrateManager.CrateBlockKey, UUID>> personalKeyDisplays = new ConcurrentHashMap<>();

    private ScheduledTask visualTask;
    private int visualPulse;

    public CrateVisualManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        shutdown();
        purgeNearbyDisplaysForAllBoundCrates();
        purgeAllCrateHologramsInWorlds();
        spawnAllHolograms();
        visualPulse = 0;
        startVisualTask();
    }

    public void shutdown() {
        if (visualTask != null) {
            visualTask.cancel();
            visualTask = null;
        }
        clearAllPersonalLineDisplays();
        clearAllPersonalDisplays();
        clearAllHolograms();
        purgeNearbyDisplaysForAllBoundCrates();
        purgeAllCrateHologramsInWorlds();
    }

    public void handleJoin(Player player) {
        if (player == null) {
            return;
        }

        for (Map.Entry<UUID, Map<CrateManager.CrateBlockKey, UUID>> entry : personalKeyDisplays.entrySet()) {
            if (entry.getKey().equals(player.getUniqueId())) {
                continue;
            }

            for (UUID entityId : entry.getValue().values()) {
                Entity entity = Bukkit.getEntity(entityId);
                if (entity != null && entity.isValid()) {
                    player.hideEntity(plugin, entity);
                }
            }
        }

        for (Map.Entry<UUID, Map<CrateManager.CrateBlockKey, List<UUID>>> entry : personalLineDisplays.entrySet()) {
            if (entry.getKey().equals(player.getUniqueId())) {
                continue;
            }

            for (List<UUID> entityIds : entry.getValue().values()) {
                for (UUID entityId : entityIds) {
                    Entity entity = Bukkit.getEntity(entityId);
                    if (entity != null && entity.isValid()) {
                        player.hideEntity(plugin, entity);
                    }
                }
            }
        }
    }

    public void handleQuit(UUID playerId) {
        if (playerId != null) {
            clearPersonalLineDisplays(playerId);
            clearPersonalDisplays(playerId);
        }
    }

    public void refreshHologram(Block block) {
        if (block == null || block.getWorld() == null) {
            return;
        }

        CrateManager.CrateBlockKey key = toKey(block);
        removeHologram(key);
        purgeNearbyDisplaysForCrate(key);
    }

    public void removeHologram(Block block) {
        if (block == null || block.getWorld() == null) {
            return;
        }
        removeHologram(toKey(block));
    }

    public void playOpenEffects(Player player, Block block, CrateManager.CrateDefinition crate) {
        if (player == null || block == null || crate == null || block.getWorld() == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSounds().getString(
                "CRATES.OPEN",
                "minecraft:block.ender_chest.open|1.0|1.05"
        ));

        Location origin = block.getLocation().add(0.5, 1.0, 0.5);
        block.getWorld().spawnParticle(Particle.END_ROD, origin, 10, 0.25, 0.35, 0.25, 0.01);
        block.getWorld().spawnParticle(Particle.GLOW, origin.clone().add(0, 0.25, 0), 8, 0.3, 0.3, 0.3, 0.0);
        animateLid(block);
    }

    public void playNoKeyEffects(Player player) {
        if (player == null) {
            return;
        }
        SoundUtils.play(player, plugin.getConfigManager().getSounds().getString(
                "CRATES.NO-KEY",
                "minecraft:entity.villager.no|1.0|1.0"
        ));
    }

    public void playClaimEffects(Player player, CrateManager.CrateDefinition crate) {
        if (player == null || crate == null || player.getWorld() == null) {
            return;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSounds().getString(
                "CRATES.CLAIM",
                "minecraft:entity.player.levelup|1.0|1.25"
        ));

        Location origin = player.getLocation().add(0, 1.0, 0);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, origin, 20, 0.4, 0.6, 0.4, 0.05);
        player.getWorld().spawnParticle(Particle.GLOW, origin, 15, 0.35, 0.5, 0.35, 0.0);
    }

    private void startVisualTask() {
        visualTask = plugin.getFoliaScheduler().runGlobalTimer(() -> {
            visualPulse++;
            spawnIdleParticles();
            ensureGlobalHolograms();
            updatePersonalKeyDisplays();
        }, 10L, 10L);
    }

    private void spawnAllHolograms() {
        for (Map.Entry<CrateManager.CrateBlockKey, String> entry : plugin.getCrateManager().getBoundBlockIds().entrySet()) {
            CrateManager.CrateBlockKey key = entry.getKey();
            World world = Bukkit.getWorld(key.world());
            if (world == null) {
                continue;
            }

            CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(entry.getValue());
            if (crate == null) {
                continue;
            }

            plugin.getFoliaScheduler().runRegion(world, key.x() >> 4, key.z() >> 4,
                    () -> spawnHologram(world.getBlockAt(key.x(), key.y(), key.z()), crate));
        }
    }

    private void spawnHologram(Block block, CrateManager.CrateDefinition crate) {
        removeTrackedGlobalHologram(toKey(block));

        List<String> lines = getHologramLines(crate);
        if (lines.isEmpty()) {
            return;
        }

        List<UUID> entityIds = new ArrayList<>();
        double baseY = block.getY() + getHologramOffsetY();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Location location = new Location(block.getWorld(), block.getX() + 0.5, baseY - (i * 0.27), block.getZ() + 0.5);
            TextDisplay display = block.getWorld().spawn(location, TextDisplay.class, textDisplay -> {
                textDisplay.text(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(line));
                configureHologramDisplay(textDisplay);
                textDisplay.addScoreboardTag(HOLOGRAM_TAG);
                textDisplay.getPersistentDataContainer().set(
                        plugin.getKey("crate_hologram"),
                        PersistentDataType.STRING,
                        formatBlockKey(toKey(block))
                );
            });
            entityIds.add(display.getUniqueId());
        }

        holograms.put(toKey(block), entityIds);
    }

    private void ensureGlobalHolograms() {
        for (Map.Entry<CrateManager.CrateBlockKey, String> entry : plugin.getCrateManager().getBoundBlockIds().entrySet()) {
            CrateManager.CrateBlockKey key = entry.getKey();
            CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(entry.getValue());
            if (crate == null) {
                continue;
            }

            World world = Bukkit.getWorld(key.world());
            if (world == null) {
                continue;
            }

            plugin.getFoliaScheduler().runRegion(world, key.x() >> 4, key.z() >> 4, () -> {
                if (!hasValidGlobalHologram(key, getHologramLines(crate).size())) {
                    spawnHologram(world.getBlockAt(key.x(), key.y(), key.z()), crate);
                }
            });
        }
    }

    private boolean hasValidGlobalHologram(CrateManager.CrateBlockKey key, int requiredLines) {
        List<UUID> trackedIds = holograms.get(key);
        if (trackedIds == null || trackedIds.size() != requiredLines) {
            removeTrackedGlobalHologram(key);
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

        removeTrackedGlobalHologram(key);
        return false;
    }

    private void updatePersonalKeyDisplays() {
        double viewDistance = getHologramViewDistance();
        boolean unlimitedDistance = viewDistance <= 0D;
        double maxDistanceSquared = unlimitedDistance ? 0D : Math.pow(viewDistance, 2);

        plugin.getFoliaScheduler().forEachOnlinePlayer(player -> {
            Set<CrateManager.CrateBlockKey> playerActive = new HashSet<>();

            for (Map.Entry<CrateManager.CrateBlockKey, String> entry : plugin.getCrateManager().getBoundBlockIds().entrySet()) {
                CrateManager.CrateBlockKey key = entry.getKey();
                if (!player.getWorld().getName().equals(key.world())) {
                    continue;
                }

                double distanceSquared = player.getLocation().distanceSquared(new Location(player.getWorld(), key.x() + 0.5, key.y() + 0.5, key.z() + 0.5));
                if (!unlimitedDistance && distanceSquared > maxDistanceSquared) {
                    continue;
                }

                CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(entry.getValue());
                if (crate == null) {
                    continue;
                }

                playerActive.add(key);
                int keyBalance = plugin.getCrateManager().getKeyBalance(player.getUniqueId(), crate.id());
                World world = Bukkit.getWorld(key.world());
                if (world != null) {
                    Location displayLocation = new Location(world, key.x() + 0.5, key.y() + getHologramOffsetY(), key.z() + 0.5);
                    plugin.getFoliaScheduler().runRegion(displayLocation,
                            () -> upsertPersonalKeyDisplay(player.getUniqueId(), key, crate, keyBalance));
                }
            }

            Map<CrateManager.CrateBlockKey, UUID> playerDisplays = personalKeyDisplays.get(player.getUniqueId());
            if (playerDisplays != null) {
                for (CrateManager.CrateBlockKey key : new HashSet<>(playerDisplays.keySet())) {
                    if (!playerActive.contains(key)) {
                        removePersonalKeyDisplay(player.getUniqueId(), key);
                    }
                }
            }
        });
    }

    private void upsertPersonalLineDisplays(Player player, CrateManager.CrateBlockKey key, CrateManager.CrateDefinition crate) {
        removePersonalLineDisplays(player.getUniqueId(), key);
    }

    private void upsertPersonalKeyDisplay(UUID ownerId, CrateManager.CrateBlockKey key, CrateManager.CrateDefinition crate, int keyBalance) {
        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return;
        }

        Map<CrateManager.CrateBlockKey, UUID> playerDisplays = personalKeyDisplays.computeIfAbsent(
                ownerId,
                ignored -> new ConcurrentHashMap<>()
        );

        TextDisplay display = null;
        UUID existingId = playerDisplays.get(key);
        if (existingId != null) {
            Entity entity = Bukkit.getEntity(existingId);
            if (entity instanceof TextDisplay textDisplay && entity.isValid()) {
                display = textDisplay;
            } else {
                playerDisplays.remove(key);
            }
        }

        if (display == null) {
            double baseY = key.y() + getHologramOffsetY() - (getHologramLines(crate).size() * 0.27D);
            Location location = new Location(world, key.x() + 0.5, baseY, key.z() + 0.5);
            display = world.spawn(location, TextDisplay.class, textDisplay -> {
                configureHologramDisplay(textDisplay);
                textDisplay.addScoreboardTag(PERSONAL_TAG);
                textDisplay.getPersistentDataContainer().set(
                        plugin.getKey("crate_hologram"),
                        PersistentDataType.STRING,
                        formatBlockKey(key)
                );
                textDisplay.getPersistentDataContainer().set(
                        plugin.getKey("crate_hologram_owner"),
                        PersistentDataType.STRING,
                        ownerId.toString()
                );
            });
            playerDisplays.put(key, display.getUniqueId());
        }

        display.text(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(
                getKeyLine(crate, keyBalance)
        ));
        hideDisplayFromOtherPlayers(ownerId, display);
    }

    private void hideDisplayFromOtherPlayers(UUID ownerId, TextDisplay display) {
        plugin.getFoliaScheduler().forEachOnlinePlayer(other -> {
            if (other.getUniqueId().equals(ownerId)) {
                other.showEntity(plugin, display);
            } else {
                other.hideEntity(plugin, display);
            }
        });
    }

    private void removeHologram(CrateManager.CrateBlockKey key) {
        removeTrackedGlobalHologram(key);

        for (UUID viewerId : new HashSet<>(personalLineDisplays.keySet())) {
            removePersonalLineDisplays(viewerId, key);
        }

        for (UUID viewerId : new HashSet<>(personalKeyDisplays.keySet())) {
            removePersonalKeyDisplay(viewerId, key);
        }

        purgeNearbyDisplaysForCrate(key);
    }

    private void removeTrackedGlobalHologram(CrateManager.CrateBlockKey key) {
        List<UUID> entityIds = holograms.remove(key);
        if (entityIds == null) {
            return;
        }

        for (UUID entityId : entityIds) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                removeEntity(entity);
            }
        }
    }

    private void removeEntity(Entity entity) {
        if (entity != null && entity.isValid()) {
            plugin.getFoliaScheduler().runEntity(entity, entity::remove);
        }
    }

    private void clearAllHolograms() {
        for (CrateManager.CrateBlockKey key : new HashSet<>(holograms.keySet())) {
            removeTrackedGlobalHologram(key);
        }
        holograms.clear();
    }

    private void purgeAllCrateHologramsInWorlds() {
        purgeNearbyDisplaysForAllBoundCrates();
    }

    private void purgeNearbyDisplaysForAllBoundCrates() {
        for (CrateManager.CrateBlockKey key : plugin.getCrateManager().getBoundBlockIds().keySet()) {
            purgeNearbyDisplaysForCrate(key);
        }
    }

    private void purgeUntrackedDisplaysForBoundCrates() {
        for (CrateManager.CrateBlockKey key : plugin.getCrateManager().getBoundBlockIds().keySet()) {
            purgeUntrackedDisplaysForCrate(key);
        }
    }

    private void purgeNearbyDisplaysForCrate(CrateManager.CrateBlockKey key) {
        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return;
        }

        Location center = new Location(world, key.x() + 0.5, key.y() + getHologramOffsetY() - 0.35, key.z() + 0.5);
        plugin.getFoliaScheduler().runRegion(center, () -> {
            for (Entity entity : world.getNearbyEntities(center, 0.4, 1.5, 0.4, candidate -> candidate instanceof TextDisplay)) {
                if (isAtCrateHologramColumn(entity, key)) {
                    entity.remove();
                }
            }
        });
    }

    private void purgeUntrackedDisplaysForCrate(CrateManager.CrateBlockKey key) {
        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return;
        }

        Set<UUID> trackedIds = collectTrackedDisplayIds(key);
        Location center = new Location(world, key.x() + 0.5, key.y() + getHologramOffsetY() - 0.35, key.z() + 0.5);
        plugin.getFoliaScheduler().runRegion(center, () -> {
            for (Entity entity : world.getNearbyEntities(center, 0.4, 1.5, 0.4, candidate -> candidate instanceof TextDisplay)) {
                if (!isAtCrateHologramColumn(entity, key)) {
                    continue;
                }

                if (!trackedIds.contains(entity.getUniqueId())) {
                    entity.remove();
                }
            }
        });
    }

    private void clearAllPersonalLineDisplays() {
        for (Map<CrateManager.CrateBlockKey, List<UUID>> displays : personalLineDisplays.values()) {
            for (List<UUID> entityIds : displays.values()) {
                for (UUID entityId : entityIds) {
                    Entity entity = Bukkit.getEntity(entityId);
                    if (entity != null && entity.isValid()) {
                        removeEntity(entity);
                    }
                }
            }
        }
        personalLineDisplays.clear();
    }

    private void clearAllPersonalDisplays() {
        for (Map<CrateManager.CrateBlockKey, UUID> displays : personalKeyDisplays.values()) {
            for (UUID entityId : displays.values()) {
                Entity entity = Bukkit.getEntity(entityId);
                if (entity != null && entity.isValid()) {
                    removeEntity(entity);
                }
            }
        }
        personalKeyDisplays.clear();
    }

    private void clearPersonalLineDisplays(UUID viewerId) {
        Map<CrateManager.CrateBlockKey, List<UUID>> displays = personalLineDisplays.remove(viewerId);
        if (displays == null) {
            return;
        }

        for (List<UUID> entityIds : displays.values()) {
            for (UUID entityId : entityIds) {
                Entity entity = Bukkit.getEntity(entityId);
                if (entity != null && entity.isValid()) {
                    removeEntity(entity);
                }
            }
        }
    }

    private void clearPersonalDisplays(UUID viewerId) {
        Map<CrateManager.CrateBlockKey, UUID> displays = personalKeyDisplays.remove(viewerId);
        if (displays == null) {
            return;
        }

        for (UUID entityId : displays.values()) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                removeEntity(entity);
            }
        }
    }

    private void removePersonalLineDisplays(UUID viewerId, CrateManager.CrateBlockKey key) {
        Map<CrateManager.CrateBlockKey, List<UUID>> displays = personalLineDisplays.get(viewerId);
        if (displays == null) {
            return;
        }

        List<UUID> entityIds = displays.remove(key);
        if (entityIds != null) {
            for (UUID entityId : entityIds) {
                Entity entity = Bukkit.getEntity(entityId);
                if (entity != null && entity.isValid()) {
                    removeEntity(entity);
                }
            }
        }

        if (displays.isEmpty()) {
            personalLineDisplays.remove(viewerId);
        }
    }

    private void removePersonalKeyDisplay(UUID viewerId, CrateManager.CrateBlockKey key) {
        Map<CrateManager.CrateBlockKey, UUID> displays = personalKeyDisplays.get(viewerId);
        if (displays == null) {
            return;
        }

        UUID entityId = displays.remove(key);
        if (entityId != null) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                removeEntity(entity);
            }
        }

        if (displays.isEmpty()) {
            personalKeyDisplays.remove(viewerId);
        }
    }

    private void spawnIdleParticles() {
        if (!plugin.getConfigManager().getCrates().getBoolean("SETTINGS.PARTICLES.ENABLED", true)) {
            return;
        }

        Particle particle = parseParticle(
                plugin.getConfigManager().getCrates().getString("SETTINGS.PARTICLES.TYPE", "ENCHANT")
        );
        int count = Math.max(1, plugin.getConfigManager().getCrates().getInt("SETTINGS.PARTICLES.COUNT", 4));

        for (CrateManager.CrateBlockKey key : plugin.getCrateManager().getBoundBlockIds().keySet()) {
            World world = Bukkit.getWorld(key.world());
            if (world == null) {
                continue;
            }

            Location location = new Location(world, key.x() + 0.5, key.y() + 1.05, key.z() + 0.5);
            plugin.getFoliaScheduler().runRegion(location, () -> {
                world.spawnParticle(particle, location, count, 0.18, 0.25, 0.18, 0.02);
                world.spawnParticle(Particle.GLOW, location.clone().add(0, 0.15, 0), 1, 0.06, 0.06, 0.06, 0.0);
            });
        }
    }

    private void animateLid(Block block) {
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
                    "&7Right-click to open"
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
                "&7Keys: &f{keys}"
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

    private double getHologramViewDistance() {
        return Math.max(16D, plugin.getConfigManager().getCrates()
                .getDouble("SETTINGS.HOLOGRAM.VIEW-DISTANCE", 64D));
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

    private boolean isManagedCrateHologram(Entity entity) {
        if (!(entity instanceof TextDisplay)) {
            return false;
        }

        if (entity.getScoreboardTags().contains(HOLOGRAM_TAG) || entity.getScoreboardTags().contains(PERSONAL_TAG)) {
            return true;
        }

        return entity.getPersistentDataContainer().has(plugin.getKey("crate_hologram"), PersistentDataType.STRING)
                || entity.getPersistentDataContainer().has(plugin.getKey("crate_hologram_owner"), PersistentDataType.STRING);
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

    private Set<UUID> collectTrackedDisplayIds(CrateManager.CrateBlockKey key) {
        Set<UUID> trackedIds = new HashSet<>();

        List<UUID> globalIds = holograms.get(key);
        if (globalIds != null) {
            trackedIds.addAll(globalIds);
        }

        for (Map<CrateManager.CrateBlockKey, List<UUID>> displays : personalLineDisplays.values()) {
            List<UUID> ids = displays.get(key);
            if (ids != null) {
                trackedIds.addAll(ids);
            }
        }

        for (Map<CrateManager.CrateBlockKey, UUID> displays : personalKeyDisplays.values()) {
            UUID id = displays.get(key);
            if (id != null) {
                trackedIds.add(id);
            }
        }

        return trackedIds;
    }
}
