package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.scheduler.BukkitTask;
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
import org.bukkit.entity.ItemDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.bx.ultimateDonutSmp.managers.CrateManager.CrateReward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CrateVisualManager {

    private static final String HOLOGRAM_TAG = "uds_crate_hologram";
    private static final String PERSONAL_TAG = "uds_crate_personal_hologram";
    private static final String PREVIEW_TAG = "uds_crate_preview";

    private final UltimateDonutSmp plugin;
    private final Map<CrateManager.CrateBlockKey, List<UUID>> holograms = new HashMap<>();
    private final Map<UUID, Map<CrateManager.CrateBlockKey, List<UUID>>> personalLineDisplays = new HashMap<>();
    private final Map<UUID, Map<CrateManager.CrateBlockKey, UUID>> personalKeyDisplays = new HashMap<>();
    private final Map<UUID, Map<CrateManager.CrateBlockKey, String>> personalKeyDisplayTexts = new HashMap<>();

    private final Map<CrateManager.CrateBlockKey, UUID> previews = new HashMap<>();
    private final Map<CrateManager.CrateBlockKey, Integer> previewIndices = new HashMap<>();
    private final Map<CrateManager.CrateBlockKey, Integer> previewCycleTicks = new HashMap<>();

    private BukkitTask visualTask;
    private int visualPulse;

    private BukkitTask animationTask;
    private int animationPulse;

    public CrateVisualManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        shutdown();
        if (!isCratesEnabled()) {
            return;
        }
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
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        clearAllPersonalLineDisplays();
        clearAllPersonalDisplays();
        clearAllHolograms();
        purgeNearbyDisplaysForAllBoundCrates();
        purgeAllCrateHologramsInWorlds();
    }

    public void handleJoin(Player player) {
        // Personal crate displays are hidden by default, so joins do not need per-entity hide calls.
    }

    public void handleQuit(UUID playerId) {
        if (playerId != null) {
            clearPersonalLineDisplays(playerId);
            clearPersonalDisplays(playerId);
        }
    }

    public void refreshHologram(Block block) {
        if (block == null || block.getWorld() == null || !isCratesEnabled()) {
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
        if (player == null || block == null || crate == null || block.getWorld() == null || !isCratesEnabled()) {
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
        if (player == null || !isCratesEnabled()) {
            return;
        }
        SoundUtils.play(player, plugin.getConfigManager().getSounds().getString(
                "CRATES.NO-KEY",
                "minecraft:entity.villager.no|1.0|1.0"
        ));
    }

    public void playClaimEffects(Player player, CrateManager.CrateDefinition crate) {
        if (player == null || crate == null || player.getWorld() == null || !isCratesEnabled()) {
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
        long updateTicks = getHologramUpdateTicks();
        visualTask = plugin.getSpigotScheduler().runGlobalTimer(() -> {
            if (!isCratesEnabled()) {
                shutdown();
                return;
            }
            visualPulse++;
            spawnIdleParticles();
            ensureGlobalHolograms();
            updatePersonalKeyDisplays();
        }, updateTicks, updateTicks);

        if (isPreviewEnabled()) {
            startAnimationTask();
        }
    }

    private boolean isCratesEnabled() {
        return plugin.getFeatureManager().isEnabled(FeatureManager.Feature.CRATES);
    }

    private void spawnAllHolograms() {
        for (Map.Entry<CrateManager.CrateBlockKey, String> entry : plugin.getCrateManager().getBoundBlockIds().entrySet()) {
            CrateManager.CrateBlockKey key = entry.getKey();
            World world = Bukkit.getWorld(key.world());
            if (world == null) {
                continue;
            }

            Block block = world.getBlockAt(key.x(), key.y(), key.z());
            CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(entry.getValue());
            if (crate == null) {
                continue;
            }

            spawnHologram(block, crate);
            if (isPreviewEnabled()) {
                spawnPreview(block, crate);
            }
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
                textDisplay.setText(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(line));
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

            Block block = world.getBlockAt(key.x(), key.y(), key.z());

            if (!hasValidGlobalHologram(key, getHologramLines(crate).size())) {
                spawnHologram(block, crate);
            }

            if (isPreviewEnabled() && !hasValidPreview(key)) {
                spawnPreview(block, crate);
            }
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
        Map<UUID, Set<CrateManager.CrateBlockKey>> activeDisplays = new HashMap<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Set<CrateManager.CrateBlockKey> playerActive = new HashSet<>();
            activeDisplays.put(player.getUniqueId(), playerActive);

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
                upsertPersonalKeyDisplay(player, key, crate);
            }
        }

        for (Map.Entry<UUID, Map<CrateManager.CrateBlockKey, UUID>> entry : new HashMap<>(personalKeyDisplays).entrySet()) {
            UUID viewerId = entry.getKey();
            Set<CrateManager.CrateBlockKey> active = activeDisplays.getOrDefault(viewerId, Set.of());
            for (CrateManager.CrateBlockKey key : new HashSet<>(entry.getValue().keySet())) {
                if (!active.contains(key)) {
                    removePersonalKeyDisplay(viewerId, key);
                }
            }
        }
    }

    private void upsertPersonalLineDisplays(Player player, CrateManager.CrateBlockKey key, CrateManager.CrateDefinition crate) {
        removePersonalLineDisplays(player.getUniqueId(), key);
    }

    private void upsertPersonalKeyDisplay(Player player, CrateManager.CrateBlockKey key, CrateManager.CrateDefinition crate) {
        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return;
        }

        Map<CrateManager.CrateBlockKey, UUID> playerDisplays = personalKeyDisplays.computeIfAbsent(
                player.getUniqueId(),
                ignored -> new HashMap<>()
        );
        Map<CrateManager.CrateBlockKey, String> playerDisplayTexts = personalKeyDisplayTexts.computeIfAbsent(
                player.getUniqueId(),
                ignored -> new HashMap<>()
        );

        TextDisplay display = null;
        boolean shouldShowToOwner = false;
        UUID existingId = playerDisplays.get(key);
        if (existingId != null) {
            Entity entity = Bukkit.getEntity(existingId);
            if (entity instanceof TextDisplay textDisplay && entity.isValid()) {
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
                        player.getUniqueId().toString()
                );
            });
            playerDisplays.put(key, display.getUniqueId());
            shouldShowToOwner = true;
        }

        if (display.isVisibleByDefault()) {
            display.setVisibleByDefault(false);
        }

        String text = getKeyLine(crate, plugin.getCrateManager().getKeyBalance(player, crate.id()));
        if (!text.equals(playerDisplayTexts.get(key))) {
            display.setText(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(text));
            playerDisplayTexts.put(key, text);
        }
        if (shouldShowToOwner) {
            player.showEntity(plugin, display);
        }
    }

    private void removeHologram(CrateManager.CrateBlockKey key) {
        removeTrackedGlobalHologram(key);
        removeTrackedPreview(key);

        for (UUID viewerId : new HashSet<>(personalLineDisplays.keySet())) {
            removePersonalLineDisplays(viewerId, key);
        }

        for (UUID viewerId : new HashSet<>(personalKeyDisplays.keySet())) {
            removePersonalKeyDisplay(viewerId, key);
        }

        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return;
        }

        String expected = formatBlockKey(key);
        for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
            if (!entity.getScoreboardTags().contains(HOLOGRAM_TAG) && !entity.getScoreboardTags().contains(PERSONAL_TAG)) {
                continue;
            }

            String attachedKey = entity.getPersistentDataContainer().get(plugin.getKey("crate_hologram"), PersistentDataType.STRING);
            if (expected.equals(attachedKey)) {
                entity.remove();
            }
        }

        for (Entity entity : world.getEntitiesByClass(ItemDisplay.class)) {
            if (!entity.getScoreboardTags().contains(PREVIEW_TAG)) {
                continue;
            }

            String attachedKey = entity.getPersistentDataContainer().get(plugin.getKey("crate_preview"), PersistentDataType.STRING);
            if (expected.equals(attachedKey)) {
                entity.remove();
            }
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
                entity.remove();
            }
        }
    }

    private void clearAllHolograms() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
                if (entity.getScoreboardTags().contains(HOLOGRAM_TAG)) {
                    entity.remove();
                }
            }
            for (Entity entity : world.getEntitiesByClass(ItemDisplay.class)) {
                if (entity.getScoreboardTags().contains(PREVIEW_TAG)) {
                    entity.remove();
                }
            }
        }
        holograms.clear();
        previews.clear();
        previewIndices.clear();
        previewCycleTicks.clear();
    }

    private void purgeAllCrateHologramsInWorlds() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
                if (!isManagedCrateHologram(entity)) {
                    continue;
                }
                entity.remove();
            }
            for (Entity entity : world.getEntitiesByClass(ItemDisplay.class)) {
                if (!isManagedCratePreview(entity)) {
                    continue;
                }
                entity.remove();
            }
        }
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
        for (Entity entity : world.getNearbyEntities(center, 0.4, 2.5, 0.4, candidate -> candidate instanceof TextDisplay || candidate instanceof ItemDisplay)) {
            if (isAtCrateHologramColumn(entity, key) || isAtCratePreviewColumn(entity, key)) {
                entity.remove();
            }
        }
    }

    private void purgeUntrackedDisplaysForCrate(CrateManager.CrateBlockKey key) {
        World world = Bukkit.getWorld(key.world());
        if (world == null) {
            return;
        }

        Set<UUID> trackedIds = collectTrackedDisplayIds(key);
        Location center = new Location(world, key.x() + 0.5, key.y() + getHologramOffsetY() - 0.35, key.z() + 0.5);
        for (Entity entity : world.getNearbyEntities(center, 0.4, 2.5, 0.4, candidate -> candidate instanceof TextDisplay || candidate instanceof ItemDisplay)) {
            if (isAtCrateHologramColumn(entity, key)) {
                if (!trackedIds.contains(entity.getUniqueId())) {
                    entity.remove();
                }
            } else if (isAtCratePreviewColumn(entity, key)) {
                if (!trackedIds.contains(entity.getUniqueId())) {
                    entity.remove();
                }
            }
        }
    }

    private void clearAllPersonalLineDisplays() {
        for (Map<CrateManager.CrateBlockKey, List<UUID>> displays : personalLineDisplays.values()) {
            for (List<UUID> entityIds : displays.values()) {
                for (UUID entityId : entityIds) {
                    Entity entity = Bukkit.getEntity(entityId);
                    if (entity != null && entity.isValid()) {
                        entity.remove();
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
                    entity.remove();
                }
            }
        }
        personalKeyDisplays.clear();
        personalKeyDisplayTexts.clear();
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
                    entity.remove();
                }
            }
        }
    }

    private void clearPersonalDisplays(UUID viewerId) {
        Map<CrateManager.CrateBlockKey, UUID> displays = personalKeyDisplays.remove(viewerId);
        if (displays == null) {
            personalKeyDisplayTexts.remove(viewerId);
            return;
        }

        for (UUID entityId : displays.values()) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        personalKeyDisplayTexts.remove(viewerId);
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
                    entity.remove();
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
                entity.remove();
            }
        }
        Map<CrateManager.CrateBlockKey, String> textCache = personalKeyDisplayTexts.get(viewerId);
        if (textCache != null) {
            textCache.remove(key);
            if (textCache.isEmpty()) {
                personalKeyDisplayTexts.remove(viewerId);
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
            world.spawnParticle(particle, location, count, 0.18, 0.25, 0.18, 0.02);
            world.spawnParticle(Particle.GLOW, location.clone().add(0, 0.15, 0), 1, 0.06, 0.06, 0.06, 0.0);
        }
    }

    private void animateLid(Block block) {
        BlockState state = block.getState();
        if (!(state instanceof Lidded lidded)) {
            return;
        }

        try {
            lidded.open();
            plugin.getSpigotScheduler().runRegionLater(block.getLocation(), () -> {
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
                    "&7ʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴏᴘᴇɴ"
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
                "&7ᴋᴇʏѕ: &f{keys}"
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

        UUID previewId = previews.get(key);
        if (previewId != null) {
            trackedIds.add(previewId);
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

    private boolean isManagedCratePreview(Entity entity) {
        if (!(entity instanceof ItemDisplay)) {
            return false;
        }

        if (entity.getScoreboardTags().contains(PREVIEW_TAG)) {
            return true;
        }

        return entity.getPersistentDataContainer().has(plugin.getKey("crate_preview"), PersistentDataType.STRING);
    }

    private boolean isAtCratePreviewColumn(Entity entity, CrateManager.CrateBlockKey key) {
        if (entity == null || entity.getWorld() == null || !entity.getWorld().getName().equals(key.world())) {
            return false;
        }

        Location location = entity.getLocation();
        double deltaX = Math.abs(location.getX() - (key.x() + 0.5D));
        double deltaZ = Math.abs(location.getZ() - (key.z() + 0.5D));
        double minY = key.y() + getPreviewOffsetY() - 0.5D;
        double maxY = key.y() + getPreviewOffsetY() + 0.5D;

        return deltaX <= 0.2D
                && deltaZ <= 0.2D
                && location.getY() >= minY
                && location.getY() <= maxY;
    }

    private boolean isPreviewEnabled() {
        return plugin.getConfigManager().getCrates().getBoolean("SETTINGS.PREVIEW.ENABLED", true);
    }

    private double getPreviewOffsetY() {
        return plugin.getConfigManager().getCrates().getDouble("SETTINGS.PREVIEW.OFFSET-Y", 1.25D);
    }

    private int getPreviewCycleIntervalTicks() {
        return plugin.getConfigManager().getCrates().getInt("SETTINGS.PREVIEW.CYCLE-INTERVAL-TICKS", 40);
    }

    private void spawnPreview(Block block, CrateManager.CrateDefinition crate) {
        removeTrackedPreview(toKey(block));

        if (!isPreviewEnabled() || crate.rewards().isEmpty()) {
            return;
        }

        Location location = new Location(block.getWorld(), block.getX() + 0.5, block.getY() + getPreviewOffsetY(), block.getZ() + 0.5);
        ItemDisplay display = block.getWorld().spawn(location, ItemDisplay.class, itemDisplay -> {
            itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            itemDisplay.setBillboard(Display.Billboard.FIXED);

            CrateReward reward = crate.rewards().get(0);
            ItemStack item = plugin.getCrateManager().createRewardDisplayItem(null, crate, reward);
            itemDisplay.setItemStack(item);

            itemDisplay.setVisibleByDefault(true);
            itemDisplay.addScoreboardTag(PREVIEW_TAG);
            itemDisplay.getPersistentDataContainer().set(
                    plugin.getKey("crate_preview"),
                    PersistentDataType.STRING,
                    formatBlockKey(toKey(block))
            );
        });

        previews.put(toKey(block), display.getUniqueId());
        previewIndices.put(toKey(block), 0);
        previewCycleTicks.put(toKey(block), 0);
    }

    private void removeTrackedPreview(CrateManager.CrateBlockKey key) {
        UUID entityId = previews.remove(key);
        previewIndices.remove(key);
        previewCycleTicks.remove(key);
        if (entityId == null) {
            return;
        }

        Entity entity = Bukkit.getEntity(entityId);
        if (entity != null && entity.isValid()) {
            entity.remove();
        }
    }

    private boolean hasValidPreview(CrateManager.CrateBlockKey key) {
        UUID entityId = previews.get(key);
        if (entityId == null) {
            removeTrackedPreview(key);
            return false;
        }

        Entity entity = Bukkit.getEntity(entityId);
        if (entity instanceof ItemDisplay display
                && entity.isValid()
                && display.getScoreboardTags().contains(PREVIEW_TAG)) {
            return true;
        }

        removeTrackedPreview(key);
        return false;
    }

    private void startAnimationTask() {
        if (animationTask != null) {
            animationTask.cancel();
        }
        animationPulse = 0;
        animationTask = plugin.getSpigotScheduler().runGlobalTimer(() -> {
            if (!isCratesEnabled() || !isPreviewEnabled()) {
                if (animationTask != null) {
                    animationTask.cancel();
                    animationTask = null;
                }
                return;
            }
            animationPulse++;
            animatePreviews();
        }, 1L, 1L);
    }

    private void animatePreviews() {
        double rotationSpeed = plugin.getConfigManager().getCrates().getDouble("SETTINGS.PREVIEW.ROTATION-SPEED", 4.0D);
        double hoverAmplitude = plugin.getConfigManager().getCrates().getDouble("SETTINGS.PREVIEW.HOVER-AMPLITUDE", 0.08D);
        double hoverSpeed = plugin.getConfigManager().getCrates().getDouble("SETTINGS.PREVIEW.HOVER-SPEED", 0.08D);
        int cycleInterval = getPreviewCycleIntervalTicks();

        for (Map.Entry<CrateManager.CrateBlockKey, UUID> entry : new HashMap<>(previews).entrySet()) {
            CrateManager.CrateBlockKey key = entry.getKey();
            UUID entityId = entry.getValue();

            Entity entity = Bukkit.getEntity(entityId);
            if (!(entity instanceof ItemDisplay display) || !entity.isValid()) {
                continue;
            }

            float yaw = (float) ((animationPulse * rotationSpeed) % 360.0D);
            double hover = Math.sin(animationPulse * hoverSpeed) * hoverAmplitude;

            try {
                org.joml.Vector3f translation = new org.joml.Vector3f(0.0f, (float) hover, 0.0f);
                float theta = (float) Math.toRadians(yaw);
                org.joml.Quaternionf leftRotation = new org.joml.Quaternionf(0.0f, (float) Math.sin(theta / 2.0f), 0.0f, (float) Math.cos(theta / 2.0f));
                org.joml.Vector3f scale = new org.joml.Vector3f(0.6f, 0.6f, 0.6f);
                org.joml.Quaternionf rightRotation = new org.joml.Quaternionf(0.0f, 0.0f, 0.0f, 1.0f);

                org.bukkit.util.Transformation transformation = new org.bukkit.util.Transformation(translation, leftRotation, scale, rightRotation);
                display.setTransformation(transformation);
            } catch (Throwable t) {
                display.setRotation(yaw, 0.0f);
            }

            CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(plugin.getCrateManager().getBoundBlockIds().get(key));
            if (crate != null && !crate.rewards().isEmpty()) {
                int cycleTicks = previewCycleTicks.getOrDefault(key, 0) + 1;
                if (cycleTicks >= cycleInterval) {
                    cycleTicks = 0;
                    int nextIndex = (previewIndices.getOrDefault(key, 0) + 1) % crate.rewards().size();
                    previewIndices.put(key, nextIndex);

                    CrateReward nextReward = crate.rewards().get(nextIndex);
                    ItemStack nextItem = plugin.getCrateManager().createRewardDisplayItem(null, crate, nextReward);
                    display.setItemStack(nextItem);
                }
                previewCycleTicks.put(key, cycleTicks);
            }
        }
    }
}
