package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnStashBlockDefinition;
import com.bx.ultimateDonutSmp.models.SpawnStashInstance;
import com.bx.ultimateDonutSmp.models.SpawnStashItemDefinition;
import com.bx.ultimateDonutSmp.models.SpawnStashOffset;
import com.bx.ultimateDonutSmp.models.SpawnStashTypeDefinition;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class SpawnStashManager {

    public static final String USE_PERMISSION = "ultimatedonutsmp.staff.spawnstash";
    public static final String ALERT_PERMISSION = "ultimatedonutsmp.staff.spawnstash.alert";
    public static final String BYPASS_PERMISSION = "ultimatedonutsmp.staff.spawnstash.bypass";
    public static final String ADMIN_PERMISSION = "ultimatedonutsmp.admin.spawnstash";

    private final UltimateDonutSmp plugin;
    private final AtomicLong nextId = new AtomicLong(1L);
    private final Map<Long, SpawnStashInstance> activeStashes = new LinkedHashMap<>();
    private final Map<String, Long> blockIndex = new HashMap<>();
    private final Map<String, SpawnStashTypeDefinition> typeDefinitions = new LinkedHashMap<>();

    private BukkitTask monitorTask;
    private boolean configEnabled = true;
    private long defaultTtlSeconds = 900L;
    private double defaultAlertRadius = 8.0D;
    private long alertCooldownMillis = 30_000L;
    private long checkIntervalTicks = 20L;
    private boolean overwriteBlocks = true;
    private boolean protectBlocks = false;
    private boolean claimSpawnersOnBreak = false;
    private boolean rollbackOnReload = true;
    private boolean logToConsole = true;
    private int maxBlocksPerStash = 256;

    public SpawnStashManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public synchronized void reload() {
        cancelMonitor();
        boolean restoreOnReload = config().getBoolean("SETTINGS.ROLLBACK_ON_RELOAD", true);
        if (!activeStashes.isEmpty()) {
            if (restoreOnReload) {
                restoreAllInternal("reload");
            } else {
                clearActiveTracking();
            }
        }

        loadSettings();
        loadTypes();
        if (isEnabled()) {
            startMonitor();
        }
    }

    public synchronized void shutdown() {
        cancelMonitor();
        restoreAllInternal("shutdown");
    }

    public boolean isEnabled() {
        boolean featureEnabled = plugin.getFeatureManager() == null
                || plugin.getFeatureManager().isEnabled(FeatureManager.Feature.SPAWN_STASH);
        return configEnabled && featureEnabled;
    }

    public boolean isProtectionEnabled() {
        return protectBlocks;
    }

    public synchronized Collection<SpawnStashTypeDefinition> getTypeDefinitions() {
        return List.copyOf(typeDefinitions.values());
    }

    public synchronized List<String> getTypeKeys() {
        return new ArrayList<>(typeDefinitions.keySet());
    }

    public synchronized SpawnStashTypeDefinition getTypeDefinition(String typeKey) {
        if (typeKey == null || typeKey.isBlank()) {
            return null;
        }
        return typeDefinitions.get(normalizeTypeKey(typeKey));
    }

    public synchronized List<SpawnStashInstance> getActiveStashes() {
        return List.copyOf(activeStashes.values());
    }

    public SpawnResult spawn(Player creator, String typeKey) {
        if (creator == null) {
            return SpawnResult.fail(message("PLAYER-ONLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
        }
        if (!isEnabled()) {
            return SpawnResult.fail(message("DISABLED", "&cѕᴘᴀᴡɴѕᴛᴀѕʜ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."));
        }

        SpawnStashTypeDefinition definition = getTypeDefinition(typeKey);
        if (definition == null) {
            return SpawnResult.fail(message(
                    "INVALID-TYPE",
                    "&cᴜɴᴋɴᴏᴡɴ ѕᴛᴀѕʜ ᴛʏᴘᴇ '&f{type}&c'.",
                    "{type}", typeKey == null ? "" : typeKey
            ));
        }
        if (definition.blocks().isEmpty()) {
            return SpawnResult.fail(message("INVALID-CONFIG", "&cѕᴘᴀᴡɴѕᴛᴀѕʜ ᴄᴏɴꜰɪɢ ɪѕ ɪɴᴠᴀʟɪᴅ: {reason}.",
                    "{reason}", "ᴛʏᴘᴇ ʜᴀѕ ɴᴏ ʙʟᴏᴄᴋѕ"));
        }
        if (definition.blocks().size() > maxBlocksPerStash) {
            return SpawnResult.fail(message("INVALID-CONFIG", "&cѕᴘᴀᴡɴѕᴛᴀѕʜ ᴄᴏɴꜰɪɢ ɪѕ ɪɴᴠᴀʟɪᴅ: {reason}.",
                    "{reason}", "ᴛʏᴘᴇ ᴇxᴄᴇᴇᴅѕ ᴍᴀx ʙʟᴏᴄᴋѕ"));
        }

        Location playerLocation = creator.getLocation();
        World world = playerLocation.getWorld();
        if (world == null) {
            return SpawnResult.fail(message("INVALID-CONFIG", "&cѕᴘᴀᴡɴѕᴛᴀѕʜ ᴄᴏɴꜰɪɢ ɪѕ ɪɴᴠᴀʟɪᴅ: {reason}.",
                    "{reason}", "ᴡᴏʀʟᴅ ɪѕ ᴜɴᴀᴠᴀɪʟᴀʙʟᴇ"));
        }

        BlockFace facing = BlockFace.SOUTH;
        int originX = playerLocation.getBlockX();
        int originY = playerLocation.getBlockY();
        int originZ = playerLocation.getBlockZ();

        List<Block> targetBlocks = new ArrayList<>();
        List<Location> blockLocations = new ArrayList<>();
        Set<String> blockKeys = new LinkedHashSet<>();

        synchronized (this) {
            for (SpawnStashBlockDefinition blockDefinition : definition.blocks()) {
                SpawnStashOffset offset = add(definition.pasteOffset(), blockDefinition.offset());
                Block block = world.getBlockAt(originX + offset.x(), originY + offset.y(), originZ + offset.z());
                String key = blockKey(block);
                if (!blockKeys.add(key)) {
                    return SpawnResult.fail(message("INVALID-CONFIG", "&cѕᴘᴀᴡɴѕᴛᴀѕʜ ᴄᴏɴꜰɪɢ ɪѕ ɪɴᴠᴀʟɪᴅ: {reason}.",
                            "{reason}", "ᴅᴜᴘʟɪᴄᴀᴛᴇ ʙʟᴏᴄᴋ ᴏꜰꜰѕᴇᴛ"));
                }
                if (blockIndex.containsKey(key)) {
                    return SpawnResult.fail(message("INVALID-CONFIG", "&cѕᴘᴀᴡɴѕᴛᴀѕʜ ᴄᴏɴꜰɪɢ ɪѕ ɪɴᴠᴀʟɪᴅ: {reason}.",
                            "{reason}", "ᴛᴀʀɢᴇᴛ ᴏᴠᴇʀʟᴀᴘѕ ᴀɴ ᴀᴄᴛɪᴠᴇ ѕᴛᴀѕʜ"));
                }
                if (!overwriteBlocks && !block.isEmpty()) {
                    return SpawnResult.fail(message("INVALID-CONFIG", "&cѕᴘᴀᴡɴѕᴛᴀѕʜ ᴄᴏɴꜰɪɢ ɪѕ ɪɴᴠᴀʟɪᴅ: {reason}.",
                            "{reason}", "ᴛᴀʀɢᴇᴛ ɪѕ ɴᴏᴛ ᴇᴍᴘᴛʏ"));
                }
                targetBlocks.add(block);
                blockLocations.add(block.getLocation());
            }

            List<BlockState> snapshots = targetBlocks.stream()
                    .map(Block::getState)
                    .toList();
            long id = nextId.getAndIncrement();
            long now = System.currentTimeMillis();
            long expiresAt = now + Math.max(1L, definition.ttlSeconds()) * 1000L;

            try {
                for (int index = 0; index < targetBlocks.size(); index++) {
                    placeBlock(targetBlocks.get(index), definition.blocks().get(index), facing, creator);
                }
            } catch (RuntimeException exception) {
                unregisterTemporarySpawners(targetBlocks);
                restoreSnapshots(snapshots);
                plugin.getLogger().log(Level.WARNING, "Failed to spawn SpawnStash type " + definition.key(), exception);
                return SpawnResult.fail(message("INVALID-CONFIG", "&cѕᴘᴀᴡɴѕᴛᴀѕʜ ᴄᴏɴꜰɪɢ ɪѕ ɪɴᴠᴀʟɪᴅ: {reason}.",
                        "{reason}", exception.getMessage() == null ? "ʙʟᴏᴄᴋ ᴘʟᴀᴄᴇᴍᴇɴᴛ ꜰᴀɪʟᴇᴅ" : exception.getMessage()));
            }

            SpawnStashInstance instance = new SpawnStashInstance(
                    id,
                    definition.key(),
                    definition.displayName(),
                    creator.getUniqueId(),
                    creator.getName(),
                    world.getName(),
                    originX,
                    originY,
                    originZ,
                    facing,
                    now,
                    expiresAt,
                    definition.alertRadius(),
                    blockLocations,
                    blockKeys,
                    snapshots
            );

            activeStashes.put(id, instance);
            for (String key : blockKeys) {
                blockIndex.put(key, id);
            }

            return SpawnResult.success(message(
                    "SPAWNED",
                    "&aѕᴘᴀᴡɴᴇᴅ ѕᴛᴀѕʜ #{type} ѕᴜᴄᴄᴇѕѕꜰᴜʟʟʏ. &7(ɪᴅ: {id})",
                    "{type}", definition.key(),
                    "{display}", ColorUtils.strip(definition.displayName()),
                    "{id}", String.valueOf(id)
            ), instance);
        }
    }

    public synchronized RemovalResult removeById(long id) {
        SpawnStashInstance instance = activeStashes.get(id);
        if (instance == null) {
            return RemovalResult.fail(message("NO-ACTIVE", "&cɴᴏ ᴀᴄᴛɪᴠᴇ ѕᴛᴀѕʜ ꜰᴏᴜɴᴅ."));
        }
        restoreInternal(id, "manual");
        return RemovalResult.success(message("REMOVED", "&aʀᴇᴍᴏᴠᴇᴅ ѕᴛᴀѕʜ &f#{id}&a.",
                "{id}", String.valueOf(id)), 1);
    }

    public RemovalResult removeNearest(Player player) {
        if (player == null) {
            return RemovalResult.fail(message("PLAYER-ONLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
        }

        SpawnStashInstance nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (SpawnStashInstance instance : getActiveStashes()) {
            if (!instance.worldName().equals(player.getWorld().getName())) {
                continue;
            }
            double dx = (instance.originX() + 0.5D) - player.getLocation().getX();
            double dy = (instance.originY() + 0.5D) - player.getLocation().getY();
            double dz = (instance.originZ() + 0.5D) - player.getLocation().getZ();
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = instance;
            }
        }

        if (nearest == null) {
            return RemovalResult.fail(message("NO-ACTIVE", "&cɴᴏ ᴀᴄᴛɪᴠᴇ ѕᴛᴀѕʜ ꜰᴏᴜɴᴅ."));
        }
        return removeById(nearest.id());
    }

    public synchronized RemovalResult removeAll() {
        int count = restoreAllInternal("manual-all");
        if (count <= 0) {
            return RemovalResult.fail(message("NO-ACTIVE", "&cɴᴏ ᴀᴄᴛɪᴠᴇ ѕᴛᴀѕʜ ꜰᴏᴜɴᴅ."));
        }
        return RemovalResult.success(message("REMOVED-ALL", "&aʀᴇᴍᴏᴠᴇᴅ &f{count}&a ᴀᴄᴛɪᴠᴇ ѕᴛᴀѕʜ(ᴇѕ).",
                "{count}", String.valueOf(count)), count);
    }

    public boolean isActiveBlock(Block block) {
        return getInstance(block) != null;
    }

    public boolean isActiveSpawner(Block block) {
        return block != null && block.getType() == Material.SPAWNER && isActiveBlock(block);
    }

    public boolean triggerBlockAlert(Player player, Block block, String reason) {
        SpawnStashInstance instance = getInstance(block);
        if (instance == null) {
            return false;
        }
        boolean forceAlert = reason != null && reason.equalsIgnoreCase("break");
        if (player != null && (forceAlert || !isBypass(player))) {
            broadcastAlert(instance, player, reason, block.getLocation(), forceAlert);
        }
        return true;
    }

    public void releaseDestroyedBlock(Block block) {
        if (block == null || !isActiveBlock(block) || plugin.getSpawnerManager() == null) {
            return;
        }
        plugin.getSpawnerManager().removeTemporarySpawner(block);
    }

    public boolean handleBrokenBlock(Player breaker, Block block) {
        if (block == null || !isActiveBlock(block) || plugin.getSpawnerManager() == null) {
            return false;
        }

        SpawnerInstance spawner = plugin.getSpawnerManager().getSpawner(block);
        if (!plugin.getSpawnerManager().isTemporarySpawner(spawner)) {
            return false;
        }

        if (claimSpawnersOnBreak && breaker != null) {
            giveSpawnerClaim(breaker, spawner);
        }

        plugin.getSpawnerManager().removeTemporarySpawner(block);
        return true;
    }

    public String publicMessage(String path, String fallback, String... placeholders) {
        return message(path, fallback, placeholders);
    }

    public void sendUsage(CommandSender sender, String label) {
        for (String line : messageList("USAGE", List.of(
                "&8&m----------- &dѕᴘᴀᴡɴѕᴛᴀѕʜ &8&m-----------",
                "&f/" + label + " &7- ѕᴘᴀᴡɴ ʀᴀɴᴅᴏᴍ ʙᴀɪᴛ ѕᴛᴀѕʜ",
                "&f/" + label + " <type> &7- ѕᴘᴀᴡɴ ᴀ ʙᴀɪᴛ ѕᴛᴀѕʜ",
                "&f/" + label + " spawn <type> &7- ѕᴘᴀᴡɴ ᴀ ʙᴀɪᴛ ѕᴛᴀѕʜ",
                "&f/" + label + " list &7- ʟɪѕᴛ ᴀᴄᴛɪᴠᴇ/ᴄᴏɴꜰɪɢᴜʀᴇᴅ ѕᴛᴀѕʜᴇѕ",
                "&f/" + label + " remove <id|nearest|all> &7- ʀᴏʟʟʙᴀᴄᴋ ѕᴛᴀѕʜᴇѕ",
                "&f/" + label + " reload &7- ʀᴇʟᴏᴀᴅ spawn-stash.yml"
        ), "{label}", label)) {
            sender.sendMessage(ColorUtils.toComponent(line));
        }
    }

    public long remainingSeconds(SpawnStashInstance instance) {
        if (instance == null) {
            return 0L;
        }
        return Math.max(0L, (instance.expiresAtMillis() - System.currentTimeMillis() + 999L) / 1000L);
    }

    public boolean isBypass(Player player) {
        return player != null && PermissionUtils.has(player, BYPASS_PERMISSION);
    }

    private void loadSettings() {
        FileConfiguration config = config();
        configEnabled = config.getBoolean("SETTINGS.ENABLED", true);
        defaultTtlSeconds = Math.max(1L, config.getLong("SETTINGS.DEFAULT_TTL_SECONDS", 900L));
        defaultAlertRadius = Math.max(1.0D, config.getDouble("SETTINGS.DEFAULT_ALERT_RADIUS", 8.0D));
        alertCooldownMillis = Math.max(1L, config.getLong("SETTINGS.ALERT_COOLDOWN_SECONDS", 30L)) * 1000L;
        checkIntervalTicks = Math.max(1L, config.getLong("SETTINGS.CHECK_INTERVAL_TICKS", 20L));
        overwriteBlocks = config.getBoolean("SETTINGS.OVERWRITE_BLOCKS", true);
        protectBlocks = config.getBoolean("SETTINGS.PROTECT_BLOCKS", false);
        claimSpawnersOnBreak = config.getBoolean("SETTINGS.CLAIM_SPAWNERS_ON_BREAK", false);
        rollbackOnReload = config.getBoolean("SETTINGS.ROLLBACK_ON_RELOAD", true);
        logToConsole = config.getBoolean("SETTINGS.LOG_TO_CONSOLE", true);
        maxBlocksPerStash = Math.max(1, config.getInt("SETTINGS.MAX_BLOCKS_PER_STASH", 256));
    }

    private synchronized void loadTypes() {
        typeDefinitions.clear();
        ConfigurationSection section = config().getConfigurationSection("TYPES");
        if (section == null) {
            plugin.getLogger().warning("spawn-stash.yml has no TYPES section.");
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection typeSection = section.getConfigurationSection(key);
            if (typeSection == null || !typeSection.getBoolean("ENABLED", true)) {
                continue;
            }
            String normalizedKey = normalizeTypeKey(key);
            String displayName = typeSection.getString("DISPLAY_NAME", "&dѕᴛᴀѕʜ " + normalizedKey);
            long ttl = Math.max(1L, typeSection.getLong("TTL_SECONDS", defaultTtlSeconds));
            double radius = Math.max(1.0D, typeSection.getDouble("ALERT_RADIUS", defaultAlertRadius));
            SpawnStashOffset pasteOffset = parseOffsetValue(typeSection.get("PASTE_OFFSET"), SpawnStashOffset.ZERO);
            List<SpawnStashBlockDefinition> blocks = parseBlocks(normalizedKey, typeSection.getList("BLOCKS"));
            if (blocks.isEmpty()) {
                plugin.getLogger().warning("Skipping SpawnStash type " + key + " because it has no valid blocks.");
                continue;
            }
            typeDefinitions.put(normalizedKey, new SpawnStashTypeDefinition(
                    normalizedKey,
                    displayName,
                    ttl,
                    radius,
                    pasteOffset,
                    blocks
            ));
        }
    }

    private List<SpawnStashBlockDefinition> parseBlocks(String typeKey, List<?> rawBlocks) {
        if (rawBlocks == null || rawBlocks.isEmpty()) {
            return List.of();
        }

        List<SpawnStashBlockDefinition> blocks = new ArrayList<>();
        for (Object rawBlock : rawBlocks) {
            if (!(rawBlock instanceof Map<?, ?> map)) {
                plugin.getLogger().warning("Ignoring invalid SpawnStash block in type " + typeKey + ".");
                continue;
            }

            SpawnStashOffset offset = parseOffsetValue(value(map, "OFFSET"), SpawnStashOffset.ZERO);
            String materialName = stringValue(value(map, "MATERIAL"));
            String blockData = stringValue(value(map, "BLOCK_DATA"));
            Material material = materialName == null ? null : parseMaterial(materialName);
            if (material == null && (blockData == null || blockData.isBlank())) {
                plugin.getLogger().warning("Ignoring SpawnStash block in type " + typeKey + " with no material/block data.");
                continue;
            }

            String spawnerTypeKey = normalizeSpawnerTypeKey(stringValue(firstValue(map, "SPAWNER_TYPE", "SPAWNER_KEY", "MOB_TYPE")));
            String legacySpawnerEntity = stringValue(value(map, "SPAWNER_ENTITY"));
            EntityType spawnerEntity = null;
            if (spawnerTypeKey == null && legacySpawnerEntity != null) {
                String legacyKey = normalizeSpawnerTypeKey(legacySpawnerEntity);
                if (legacyKey != null
                        && plugin.getSpawnerManager() != null
                        && plugin.getSpawnerManager().getTypeDefinition(legacyKey) != null) {
                    spawnerTypeKey = legacyKey;
                } else {
                    spawnerEntity = parseEntityType(legacySpawnerEntity);
                }
            }
            long spawnerStackAmount = Math.max(1L, longValue(firstValue(map, "SPAWNER_STACK", "STACK_AMOUNT", "AMOUNT"), 1L));
            SpawnerInstance.AccessMode spawnerAccessMode = SpawnerInstance.AccessMode.fromString(
                    stringValue(firstValue(map, "SPAWNER_ACCESS", "ACCESS_MODE", "ACCESS")),
                    SpawnerInstance.AccessMode.PUBLIC
            );
            List<String> signLines = stringList(value(map, "SIGN_LINES"));
            List<SpawnStashItemDefinition> items = parseItems(firstValue(map, "CONTAINER_ITEMS", "CHEST_ITEMS", "ITEMS"));
            blocks.add(new SpawnStashBlockDefinition(
                    offset,
                    material,
                    blockData,
                    spawnerTypeKey,
                    spawnerStackAmount,
                    spawnerAccessMode,
                    spawnerEntity,
                    signLines,
                    items
            ));
        }
        return List.copyOf(blocks);
    }

    private List<SpawnStashItemDefinition> parseItems(Object rawItems) {
        if (!(rawItems instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }

        List<SpawnStashItemDefinition> items = new ArrayList<>();
        for (Object rawItem : list) {
            if (!(rawItem instanceof Map<?, ?> map)) {
                continue;
            }
            Material material = parseMaterial(stringValue(value(map, "MATERIAL")));
            if (material == null || !material.isItem()) {
                continue;
            }
            int slot = Math.max(0, intValue(value(map, "SLOT"), 0));
            int amount = Math.max(1, Math.min(64, intValue(value(map, "AMOUNT"), 1)));
            String displayName = stringValue(value(map, "DISPLAY_NAME"));
            List<String> lore = stringList(value(map, "LORE"));
            items.add(new SpawnStashItemDefinition(slot, material, amount, displayName, lore));
        }
        return List.copyOf(items);
    }

    private void startMonitor() {
        cancelMonitor();
        monitorTask = plugin.getSpigotScheduler().runGlobalTimer(this::monitorActiveStashes, checkIntervalTicks, checkIntervalTicks);
    }

    private void cancelMonitor() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
    }

    private void monitorActiveStashes() {
        if (!isEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        List<SpawnStashInstance> snapshot = getActiveStashes();
        for (SpawnStashInstance instance : snapshot) {
            if (now >= instance.expiresAtMillis()) {
                synchronized (this) {
                    if (activeStashes.containsKey(instance.id())) {
                        restoreInternal(instance.id(), "expired");
                    }
                }
                if (logToConsole) {
                    plugin.getLogger().info(ColorUtils.strip(message(
                            "EXPIRED",
                            "&7ѕᴘᴀᴡɴѕᴛᴀѕʜ #{id} ᴇxᴘɪʀᴇᴅ ᴀɴᴅ ᴡᴀѕ ʀᴏʟʟᴇᴅ ʙᴀᴄᴋ.",
                            "{id}", String.valueOf(instance.id()),
                            "{type}", instance.typeKey()
                    )));
                }
                continue;
            }

            World world = Bukkit.getWorld(instance.worldName());
            if (world == null) {
                continue;
            }
            double radiusSquared = instance.alertRadius() * instance.alertRadius();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().equals(world) || isBypass(player)) {
                    continue;
                }
                double dx = (instance.originX() + 0.5D) - player.getLocation().getX();
                double dy = (instance.originY() + 0.5D) - player.getLocation().getY();
                double dz = (instance.originZ() + 0.5D) - player.getLocation().getZ();
                if (dx * dx + dy * dy + dz * dz <= radiusSquared) {
                    broadcastAlert(instance, player, "radius", player.getLocation(), false);
                }
            }
        }
    }

    private SpawnStashInstance getInstance(Block block) {
        if (block == null || block.getWorld() == null) {
            return null;
        }
        synchronized (this) {
            Long id = blockIndex.get(blockKey(block));
            return id == null ? null : activeStashes.get(id);
        }
    }

    private void broadcastAlert(SpawnStashInstance instance, Player player, String reason, Location location, boolean forceAlert) {
        if (instance == null || player == null || (!forceAlert && isBypass(player))) {
            return;
        }
        String normalizedReason = reason == null || reason.isBlank()
                ? "unknown"
                : reason.toLowerCase(Locale.US);
        String cooldownKey = player.getUniqueId() + ":" + normalizedReason;
        long now = System.currentTimeMillis();
        Long previous = instance.alertCooldowns().get(cooldownKey);
        if (previous != null && now - previous < alertCooldownMillis) {
            return;
        }
        instance.alertCooldowns().put(cooldownKey, now);

        Location alertLocation = location == null ? player.getLocation() : location;
        List<String> lines = messageList("ALERT", List.of(
                "&8[&dѕᴘᴀᴡɴѕᴛᴀѕʜ&8] &f{player} &7ᴛʀɪɢɢᴇʀᴇᴅ &d{reason}&7 ᴏɴ ѕᴛᴀѕʜ &f#{id}&7 (&f{type}&7)",
                "&7ʟᴏᴄᴀᴛɪᴏɴ: &f{world} {x}, {y}, {z} &8| &7ᴄʀᴇᴀᴛᴇᴅ ʙʏ: &f{creator}"
        ),
                "{player}", player.getName(),
                "{reason}", displayReason(normalizedReason),
                "{id}", String.valueOf(instance.id()),
                "{type}", instance.typeKey(),
                "{display}", ColorUtils.strip(instance.displayName()),
                "{creator}", instance.creatorName(),
                "{world}", alertLocation.getWorld() == null ? instance.worldName() : alertLocation.getWorld().getName(),
                "{x}", String.valueOf(alertLocation.getBlockX()),
                "{y}", String.valueOf(alertLocation.getBlockY()),
                "{z}", String.valueOf(alertLocation.getBlockZ())
        );

        Set<UUID> notified = new HashSet<>();
        for (Player staff : Bukkit.getOnlinePlayers()) {
            boolean isCreator = staff.getUniqueId().equals(instance.creatorUuid());
            boolean hasAlertPermission = PermissionUtils.has(staff, ALERT_PERMISSION)
                    || PermissionUtils.has(staff, "ultimatedonutsmp.staff.alerts.receive");
            if (!isCreator && !hasAlertPermission) {
                continue;
            }
            if (!notified.add(staff.getUniqueId())) {
                continue;
            }
            sendClickableAlert(staff, player, lines);
        }

        if (logToConsole) {
            plugin.getLogger().warning(ColorUtils.strip(String.join(" ", lines)));
        }
    }

    private void sendClickableAlert(Player recipient, Player target, List<String> lines) {
        String command = "/tp " + target.getName();
        String hover = message(
                "ALERT-HOVER",
                "&eᴄʟɪᴄᴋ ᴛᴏ ᴛᴇʟᴇᴘᴏʀᴛ ᴛᴏ &f{player}",
                "{player}", target.getName()
        );
        for (String line : lines) {
            TextComponent component = ColorUtils.toBaseComponent(line);
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    ColorUtils.toBaseComponents(hover)
            ));
            recipient.spigot().sendMessage(component);
        }
    }

    private synchronized int restoreAllInternal(String reason) {
        List<Long> ids = new ArrayList<>(activeStashes.keySet());
        int count = 0;
        for (Long id : ids) {
            if (restoreInternal(id, reason)) {
                count++;
            }
        }
        return count;
    }

    private synchronized boolean restoreInternal(long id, String reason) {
        SpawnStashInstance instance = activeStashes.remove(id);
        if (instance == null) {
            return false;
        }
        for (String key : instance.blockKeys()) {
            blockIndex.remove(key);
        }
        unregisterTemporarySpawners(instance.blockLocations());
        restoreSnapshots(instance.snapshots());
        return true;
    }

    private void unregisterTemporarySpawners(Collection<?> blocksOrLocations) {
        if (blocksOrLocations == null || plugin.getSpawnerManager() == null) {
            return;
        }
        for (Object value : blocksOrLocations) {
            Block block = null;
            if (value instanceof Block matchedBlock) {
                block = matchedBlock;
            } else if (value instanceof Location location && location.getWorld() != null) {
                block = location.getBlock();
            }
            if (block != null) {
                plugin.getSpawnerManager().removeTemporarySpawner(block);
            }
        }
    }

    private void restoreSnapshots(List<BlockState> snapshots) {
        List<BlockState> states = new ArrayList<>(snapshots);
        Collections.reverse(states);
        for (BlockState state : states) {
            try {
                state.update(true, false);
            } catch (RuntimeException exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to restore SpawnStash block snapshot.", exception);
            }
        }
    }

    private void giveSpawnerClaim(Player player, SpawnerInstance instance) {
        long remaining = instance.getStackAmount();
        long totalGiven = 0;
        List<ItemStack> items = new ArrayList<>();
        while (remaining > 0) {
            int amount = (int) Math.min(64, remaining);
            ItemStack item = plugin.getSpawnerManager().createSpawnerItem(instance.getMobTypeKey(), amount);
            if (item != null) {
                items.add(item);
                totalGiven += amount;
            }
            remaining -= amount;
        }

        boolean anyLeftovers = false;
        for (ItemStack item : items) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            if (!leftovers.isEmpty()) {
                anyLeftovers = true;
                leftovers.values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            }
        }

        String typeName = ColorUtils.strip(plugin.getSpawnerManager().getTypeDisplayName(instance.getMobTypeKey()));
        String messagePath = !anyLeftovers ? "SPAWNER-CLAIMED" : "SPAWNER-CLAIMED-DROPPED";
        String fallback = !anyLeftovers
                ? "&aᴄʟᴀɪᴍᴇᴅ &f{amount}x {spawner}&a ꜰʀᴏᴍ ѕᴘᴀᴡɴѕᴛᴀѕʜ."
                : "&aᴄʟᴀɪᴍᴇᴅ &f{amount}x {spawner}&a. &7ɪɴᴠᴇɴᴛᴏʀʏ ꜰᴜʟʟ, ɪᴛᴇᴍ ᴅʀᴏᴘᴘᴇᴅ.";
        player.sendMessage(ColorUtils.toComponent(message(
                messagePath,
                fallback,
                "{amount}", String.valueOf(totalGiven),
                "{spawner}", typeName
        )));
    }

    private synchronized void clearActiveTracking() {
        activeStashes.clear();
        blockIndex.clear();
    }

    private void placeBlock(Block block, SpawnStashBlockDefinition definition, BlockFace facing, Player creator) {
        BlockData data = createBlockData(definition, facing);
        block.setBlockData(data, false);

        BlockState state = block.getState();
        if (definition.spawnerTypeKey() != null && state instanceof CreatureSpawner) {
            SpawnerManager.ActionResult result = plugin.getSpawnerManager().createTemporarySpawner(
                    creator,
                    block,
                    definition.spawnerTypeKey(),
                    definition.spawnerStackAmount(),
                    definition.spawnerAccessMode()
            );
            if (!result.success()) {
                throw new IllegalArgumentException(ColorUtils.strip(result.message()));
            }
        } else if (definition.spawnerEntity() != null && state instanceof CreatureSpawner spawner) {
            spawner.setSpawnedType(definition.spawnerEntity());
            spawner.setDelay(Integer.MAX_VALUE);
            spawner.update(true, false);
        }

        if (!definition.signLines().isEmpty() && state instanceof Sign sign) {
            for (int index = 0; index < 4; index++) {
                String line = index < definition.signLines().size() ? definition.signLines().get(index) : "";
                sign.setLine(index, ColorUtils.colorize(line));
            }
            sign.update(true, false);
        }

        if (!definition.containerItems().isEmpty() && state instanceof Container container) {
            Inventory inventory = container.getSnapshotInventory();
            inventory.clear();
            for (SpawnStashItemDefinition itemDefinition : definition.containerItems()) {
                if (itemDefinition.slot() >= inventory.getSize()) {
                    continue;
                }
                inventory.setItem(itemDefinition.slot(), createItem(itemDefinition));
            }
            container.update(true, false);
        }
    }

    private BlockData createBlockData(SpawnStashBlockDefinition definition, BlockFace facing) {
        BlockData data;
        if (definition.blockData() != null && !definition.blockData().isBlank()) {
            data = Bukkit.createBlockData(definition.blockData());
        } else {
            data = Objects.requireNonNull(definition.material(), "material").createBlockData();
        }

        if (data instanceof Directional directional) {
            BlockFace blockFacing = facing.getOppositeFace();
            if (directional.getFaces().contains(blockFacing)) {
                directional.setFacing(blockFacing);
            }
        }
        if (data instanceof Rotatable rotatable) {
            rotatable.setRotation(facing);
        }
        return data;
    }

    private ItemStack createItem(SpawnStashItemDefinition definition) {
        ItemStack item = new ItemStack(definition.material(), definition.amount());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (definition.displayName() != null && !definition.displayName().isBlank()) {
                meta.setDisplayName(ColorUtils.colorize(definition.displayName()));
            }
            if (!definition.lore().isEmpty()) {
                meta.setLore(definition.lore().stream().map(ColorUtils::colorize).toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private FileConfiguration config() {
        FileConfiguration config = plugin.getConfigManager().getSpawnStash();
        return config == null ? new org.bukkit.configuration.file.YamlConfiguration() : config;
    }

    private String message(String path, String fallback, String... placeholders) {
        String message = config().getString("MESSAGES." + path, fallback);
        return replace(message, placeholders);
    }

    private List<String> messageList(String path, List<String> fallback, String... placeholders) {
        List<String> lines = config().getStringList("MESSAGES." + path);
        if (lines == null || lines.isEmpty()) {
            lines = fallback;
        }
        return lines.stream().map(line -> replace(line, placeholders)).toList();
    }

    private String replace(String text, String... placeholders) {
        String output = text == null ? "" : text;
        for (int index = 0; index + 1 < placeholders.length; index += 2) {
            output = output.replace(placeholders[index], placeholders[index + 1] == null ? "" : placeholders[index + 1]);
        }
        return output;
    }

    private String displayReason(String reason) {
        return switch (reason) {
            case "radius" -> "ʀᴀᴅɪᴜѕ ᴇɴᴛʀʏ";
            case "open" -> "ᴄᴏɴᴛᴀɪɴᴇʀ ᴏᴘᴇɴ";
            case "interact" -> "ʙʟᴏᴄᴋ ɪɴᴛᴇʀᴀᴄᴛ";
            case "break" -> "ʙʀᴇᴀᴋ ᴀᴛᴛᴇᴍᴘᴛ";
            default -> reason;
        };
    }

    private String blockKey(Block block) {
        return blockKey(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    private String blockKey(World world, int x, int y, int z) {
        UUID worldId = world.getUID();
        return worldId + ":" + x + ":" + y + ":" + z;
    }

    private SpawnStashOffset add(SpawnStashOffset first, SpawnStashOffset second) {
        return new SpawnStashOffset(first.x() + second.x(), first.y() + second.y(), first.z() + second.z());
    }

    private SpawnStashOffset parseOffsetValue(Object raw, SpawnStashOffset fallback) {
        if (raw instanceof List<?> list && list.size() >= 3) {
            return new SpawnStashOffset(intValue(list.get(0), fallback.x()), intValue(list.get(1), fallback.y()), intValue(list.get(2), fallback.z()));
        }
        if (raw instanceof Map<?, ?> map) {
            return new SpawnStashOffset(
                    intValue(value(map, "X"), fallback.x()),
                    intValue(value(map, "Y"), fallback.y()),
                    intValue(value(map, "Z"), fallback.z())
            );
        }
        if (raw instanceof String text) {
            String[] parts = text.trim().split("[, ]+");
            if (parts.length >= 3) {
                return new SpawnStashOffset(intValue(parts[0], fallback.x()), intValue(parts[1], fallback.y()), intValue(parts[2], fallback.z()));
            }
        }
        return fallback;
    }

    private Object value(Map<?, ?> map, String key) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (String.valueOf(entry.getKey()).equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Object firstValue(Map<?, ?> map, String... keys) {
        if (keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = value(map, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String stringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        String value = String.valueOf(raw).trim();
        return value.isEmpty() ? null : value;
    }

    private List<String> stringList(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<String> output = new ArrayList<>();
        for (Object value : list) {
            output.add(String.valueOf(value));
        }
        return List.copyOf(output);
    }

    private int intValue(Object raw, int fallback) {
        if (raw instanceof Number number) {
            return number.intValue();
        }
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private long longValue(Object raw, long fallback) {
        if (raw instanceof Number number) {
            return number.longValue();
        }
        if (raw == null) {
            return fallback;
        }
        try {
            return Long.parseLong(String.valueOf(raw).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private Material parseMaterial(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = input.trim()
                .replace("minecraft:", "")
                .replace('-', '_')
                .toUpperCase(Locale.US);
        return Material.matchMaterial(normalized);
    }

    private EntityType parseEntityType(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        try {
            return EntityType.valueOf(input.trim()
                    .replace("minecraft:", "")
                    .replace('-', '_')
                    .toUpperCase(Locale.US));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String normalizeTypeKey(String typeKey) {
        return typeKey == null ? "" : typeKey.trim().toUpperCase(Locale.US);
    }

    private String normalizeSpawnerTypeKey(String typeKey) {
        if (typeKey == null || typeKey.isBlank()) {
            return null;
        }
        return typeKey.trim()
                .replace("minecraft:", "")
                .replace('-', '_')
                .toUpperCase(Locale.US);
    }

    public record SpawnResult(boolean success, String message, SpawnStashInstance instance) {
        public static SpawnResult success(String message, SpawnStashInstance instance) {
            return new SpawnResult(true, message, instance);
        }

        public static SpawnResult fail(String message) {
            return new SpawnResult(false, message, null);
        }
    }

    public record RemovalResult(boolean success, String message, int removedCount) {
        public static RemovalResult success(String message, int removedCount) {
            return new RemovalResult(true, message, removedCount);
        }

        public static RemovalResult fail(String message) {
            return new RemovalResult(false, message, 0);
        }
    }
}
