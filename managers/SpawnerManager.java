package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.SpawnerPanelMenu;
import com.bx.ultimateDonutSmp.menus.SpawnerStorageMenu;
import com.bx.ultimateDonutSmp.menus.SpawnerWorldListMenu;
import com.bx.ultimateDonutSmp.models.EconomyReason;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.SellCategory;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerLootEntry;
import com.bx.ultimateDonutSmp.models.SpawnerTypeDefinition;
import com.bx.ultimateDonutSmp.models.WorthResult;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class SpawnerManager {

    public record ActionResult(boolean success, String message) {}

    public record SellLootResult(
            boolean success,
            String message,
            double payout,
            long soldItems
    ) {}

    public record WorldSummary(String worldName, int count) {}

    private final UltimateDonutSmp plugin;
    private final NamespacedKey spawnerItemMarkerKey;
    private final NamespacedKey spawnerItemTypeKey;
    private final NamespacedKey spawnerItemAmountKey;
    private final Map<Long, SpawnerInstance> spawnersById = new LinkedHashMap<>();
    private final Map<String, Long> locationIndex = new HashMap<>();
    private final Map<String, LinkedHashSet<Long>> worldIndex = new HashMap<>();
    private final Map<String, SpawnerTypeDefinition> typeDefinitions = new LinkedHashMap<>();
    private boolean enabled;
    private SpawnerInstance.AccessMode defaultAccessMode;
    private long generationIntervalSeconds;
    private boolean processOnlyLoadedChunks;
    private boolean requirePlayerNearby;
    private double playerNearbyRadius;
    private long maxStackPerBlock;
    private long storageCapPerLootKey;
    private boolean dropOnBreakIfInventoryFull;
    private String storageTitle;
    private int storageSize;
    private int storageItemsPerPage;
    private String panelTitle;
    private int panelSize;
    private String worldListTitle;
    private int worldListSize;

    public SpawnerManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.spawnerItemMarkerKey = plugin.getKey("managed_spawner_item");
        this.spawnerItemTypeKey = plugin.getKey("managed_spawner_type");
        this.spawnerItemAmountKey = plugin.getKey("managed_spawner_amount");
        reload();
        loadPersistedSpawners();
    }

    public void reload() {
        FileConfiguration config = plugin.getConfigManager().getSpawners();
        enabled = config.getBoolean("SETTINGS.ENABLED", true);
        defaultAccessMode = SpawnerInstance.AccessMode.fromString(
                config.getString("SETTINGS.ACCESS_MODE", "OWNER_ONLY"),
                SpawnerInstance.AccessMode.OWNER_ONLY
        );
        generationIntervalSeconds = Math.max(1L, config.getLong("SETTINGS.GENERATION_INTERVAL_SECONDS", 5L));
        processOnlyLoadedChunks = config.getBoolean("SETTINGS.PROCESS_ONLY_LOADED_CHUNKS", true);
        requirePlayerNearby = config.getBoolean("SETTINGS.REQUIRE_PLAYER_NEARBY", false);
        playerNearbyRadius = Math.max(1D, config.getDouble("SETTINGS.PLAYER_NEARBY_RADIUS", 16D));
        maxStackPerBlock = Math.max(1L, config.getLong("SETTINGS.MAX_STACK_PER_BLOCK", 100_000L));
        storageCapPerLootKey = Math.max(1L, config.getLong("SETTINGS.STORAGE_CAP_PER_LOOT_KEY", 1_000_000L));
        dropOnBreakIfInventoryFull = config.getBoolean("SETTINGS.DROP_ON_BREAK_IF_INVENTORY_FULL", true);
        storageTitle = config.getString("GUI.STORAGE.TITLE", "&8{mob} Spawners - {page}/{max_page}");
        storageSize = normalizeSize(config.getInt("GUI.STORAGE.SIZE", 54));
        storageItemsPerPage = Math.max(9, Math.min(storageSize - 9, config.getInt("GUI.STORAGE.ITEMS_PER_PAGE", 45)));
        panelTitle = config.getString("GUI.PANEL.TITLE", "&8Spawners");
        panelSize = normalizeSize(config.getInt("GUI.PANEL.SIZE", 54));
        worldListTitle = config.getString("GUI.WORLD_LIST.TITLE", "&8Spawners Panel");
        worldListSize = normalizeSize(config.getInt("GUI.WORLD_LIST.SIZE", 27));
        loadTypeDefinitions(config);

        for (SpawnerInstance instance : spawnersById.values()) {
            syncSpawnerBlockState(instance);
        }
    }

    private void loadPersistedSpawners() {
        spawnersById.clear();
        locationIndex.clear();
        worldIndex.clear();

        Map<Long, List<SpawnerLootEntry>> lootBySpawnerId = plugin.getDatabaseManager().loadAllSpawnerLoot();
        for (SpawnerInstance instance : plugin.getDatabaseManager().loadAllSpawners()) {
            instance.setStoredLootEntries(lootBySpawnerId.get(instance.getId()));
            registerSpawner(instance);
            syncSpawnerBlockState(instance);
        }
    }

    private void loadTypeDefinitions(FileConfiguration config) {
        typeDefinitions.clear();

        ConfigurationSection typesSection = config.getConfigurationSection("TYPES");
        if (typesSection == null) {
            plugin.getLogger().warning("[SpawnerManager] No TYPES section found in spawners.yml.");
            return;
        }

        for (String rawKey : typesSection.getKeys(false)) {
            ConfigurationSection section = typesSection.getConfigurationSection(rawKey);
            if (section == null || !section.getBoolean("ENABLED", true)) {
                continue;
            }

            String key = rawKey.trim().toUpperCase(Locale.US);
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(section.getString("ENTITY_TYPE", key).trim().toUpperCase(Locale.US));
            } catch (Exception exception) {
                plugin.getLogger().warning("[SpawnerManager] Invalid ENTITY_TYPE for spawner type " + key + ".");
                continue;
            }

            Material iconMaterial = ItemUtils.parseMaterial(section.getString("ICON_MATERIAL", "SPAWNER"));
            long baseItemsPerCycle = Math.max(1L, section.getLong("BASE_ITEMS_PER_CYCLE", 1L));
            List<SpawnerTypeDefinition.DropDefinition> drops = new ArrayList<>();
            ConfigurationSection dropsSection = section.getConfigurationSection("DROPS");
            if (dropsSection != null) {
                for (String dropKey : dropsSection.getKeys(false)) {
                    ConfigurationSection dropSection = dropsSection.getConfigurationSection(dropKey);
                    if (dropSection == null || !dropSection.getBoolean("ENABLED", true)) {
                        continue;
                    }

                    Material material = ItemUtils.parseMaterial(dropSection.getString("MATERIAL", "STONE"));
                    long min = Math.max(0L, dropSection.getLong("MIN", 0L));
                    long max = Math.max(min, dropSection.getLong("MAX", min));
                    double chance = Math.max(0D, Math.min(1D, dropSection.getDouble("CHANCE", 1D)));
                    drops.add(new SpawnerTypeDefinition.DropDefinition(dropKey.toUpperCase(Locale.US), material, min, max, chance));
                }
            }

            typeDefinitions.put(key, new SpawnerTypeDefinition(
                    key,
                    section.getString("DISPLAY_NAME", "&d" + prettifyKey(key) + " Spawner"),
                    entityType,
                    iconMaterial,
                    baseItemsPerCycle,
                    drops
            ));
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ItemStack createSpawnerItem(String typeKey, long amount) {
        SpawnerTypeDefinition definition = getTypeDefinition(typeKey);
        if (definition == null || amount <= 0L) {
            return null;
        }

        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.displayName(ColorUtils.toComponent(definition.displayName()));
        meta.lore(ColorUtils.toComponentList(List.of(
                "&7Type: &f" + ColorUtils.strip(definition.displayName()),
                "&7Amount: &a" + NumberUtils.format(amount),
                "",
                "&ePlace to create or stack this spawner."
        )));

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(spawnerItemMarkerKey, PersistentDataType.BYTE, (byte) 1);
        container.set(spawnerItemTypeKey, PersistentDataType.STRING, definition.key());
        container.set(spawnerItemAmountKey, PersistentDataType.LONG, amount);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isSpawnerItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        Byte marker = container.get(spawnerItemMarkerKey, PersistentDataType.BYTE);
        return marker != null && marker == (byte) 1
                && container.has(spawnerItemTypeKey, PersistentDataType.STRING)
                && container.has(spawnerItemAmountKey, PersistentDataType.LONG);
    }

    public String getSpawnerItemType(ItemStack item) {
        if (!isSpawnerItem(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        return meta == null ? null : meta.getPersistentDataContainer().get(spawnerItemTypeKey, PersistentDataType.STRING);
    }

    public long getSpawnerItemAmount(ItemStack item) {
        if (!isSpawnerItem(item)) {
            return 0L;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return 0L;
        }

        Long amount = meta.getPersistentDataContainer().get(spawnerItemAmountKey, PersistentDataType.LONG);
        return amount == null ? 0L : Math.max(0L, amount);
    }

    public SpawnerTypeDefinition getTypeDefinition(String typeKey) {
        if (typeKey == null || typeKey.isBlank()) {
            return null;
        }
        return typeDefinitions.get(typeKey.trim().toUpperCase(Locale.US));
    }

    public Collection<SpawnerTypeDefinition> getTypeDefinitions() {
        return List.copyOf(typeDefinitions.values());
    }

    public String getTypeDisplayName(String typeKey) {
        SpawnerTypeDefinition definition = getTypeDefinition(typeKey);
        return definition == null ? "&dSpawner" : definition.displayName();
    }

    public String getPlainTypeDisplayName(String typeKey) {
        return ColorUtils.strip(getTypeDisplayName(typeKey));
    }

    public ActionResult giveSpawner(Player target, String typeKey, long amount) {
        if (!enabled) {
            return fail("&cSpawner system is currently disabled.");
        }
        if (target == null) {
            return fail("&cTarget player must be online.");
        }
        if (amount <= 0L) {
            return fail("&cSpawner amount must be positive.");
        }

        SpawnerTypeDefinition definition = getTypeDefinition(typeKey);
        if (definition == null) {
            return fail("&cUnknown spawner type '&f" + typeKey + "&c'.");
        }

        ItemStack item = createSpawnerItem(definition.key(), amount);
        if (item == null) {
            return fail("&cFailed to create the spawner item.");
        }

        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(item);
        leftovers.values().forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));
        return ok("&aGave &f" + NumberUtils.format(amount) + "x " + ColorUtils.strip(definition.displayName()) + "&a to &f" + target.getName() + "&a.");
    }

    public ActionResult placeSpawner(Player player, Block block, ItemStack item) {
        if (!enabled) {
            return fail("&cSpawner system is currently disabled.");
        }
        if (player == null || block == null || !isSpawnerItem(item)) {
            return fail("&cThat is not a managed spawner item.");
        }

        String typeKey = getSpawnerItemType(item);
        long amount = getSpawnerItemAmount(item);
        if (amount <= 0L) {
            return fail("&cThis spawner item has an invalid amount.");
        }
        if (amount > maxStackPerBlock) {
            return fail("&cThat spawner item exceeds the max stack per block (&f" + NumberUtils.format(maxStackPerBlock) + "&c).");
        }

        if (getSpawner(block) != null) {
            return fail("&cThat block is already a managed spawner.");
        }

        SpawnerTypeDefinition definition = getTypeDefinition(typeKey);
        if (definition == null) {
            return fail("&cUnknown spawner type '&f" + typeKey + "&c'.");
        }

        long now = System.currentTimeMillis();
        SpawnerInstance instance = new SpawnerInstance(
                0L,
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ(),
                player.getUniqueId(),
                player.getName(),
                definition.key(),
                amount,
                defaultAccessMode,
                now,
                now,
                now
        );

        long id = plugin.getDatabaseManager().createSpawner(instance);
        if (id <= 0L) {
            return fail("&cFailed to save that spawner. Please try again.");
        }

        instance.setId(id);
        registerSpawner(instance);
        plugin.getPaperScheduler().runRegion(block.getLocation(), () -> {
            syncSpawnerBlockState(instance);
            if (plugin.getAntiEspManager() != null) {
                plugin.getAntiEspManager().refreshNearby(block.getLocation());
            }
        });

        return ok("&aPlaced &f" + NumberUtils.format(instance.getStackAmount()) + "x "
                + ColorUtils.strip(definition.displayName()) + "&a.");
    }

    public ActionResult stackSpawner(Player player, Block block, ItemStack item) {
        if (!enabled) {
            return fail("&cSpawner system is currently disabled.");
        }
        SpawnerInstance existing = getSpawner(block);
        if (existing == null) {
            return fail("&cThat is not a managed spawner.");
        }
        if (!isSpawnerItem(item)) {
            return fail("&cHold a managed spawner item to stack.");
        }
        if (!canModify(player, existing)) {
            return fail("&cYou do not own that spawner.");
        }

        String typeKey = Objects.requireNonNullElse(getSpawnerItemType(item), "");
        if (!existing.getMobTypeKey().equalsIgnoreCase(typeKey)) {
            return fail("&cYou can only stack the same spawner type onto this block.");
        }

        long addAmount = getSpawnerItemAmount(item);
        if (addAmount <= 0L) {
            return fail("&cThat spawner item has an invalid amount.");
        }

        long targetAmount = existing.getStackAmount() + addAmount;
        if (targetAmount > maxStackPerBlock) {
            return fail("&cThat would exceed the max stack per block (&f" + NumberUtils.format(maxStackPerBlock) + "&c).");
        }

        existing.setStackAmount(targetAmount);
        existing.setUpdatedAt(System.currentTimeMillis());
        plugin.getDatabaseManager().saveSpawner(existing);
        plugin.getPaperScheduler().runRegion(block.getLocation(), () -> {
            syncSpawnerBlockState(existing);
            if (plugin.getAntiEspManager() != null) {
                plugin.getAntiEspManager().refreshNearby(block.getLocation());
            }
        });

        return ok("&aSpawner stack updated to &f" + NumberUtils.format(existing.getStackAmount()) + "&a.");
    }

    public SpawnerInstance getSpawner(Block block) {
        if (block == null) {
            return null;
        }
        return getSpawner(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public SpawnerInstance getSpawner(String world, int x, int y, int z) {
        Long id = locationIndex.get(SpawnerInstance.buildLocationKey(world, x, y, z));
        return id == null ? null : spawnersById.get(id);
    }

    public SpawnerInstance getSpawner(long spawnerId) {
        return spawnersById.get(spawnerId);
    }

    public Collection<SpawnerInstance> getAllSpawners() {
        return List.copyOf(spawnersById.values());
    }

    public List<SpawnerInstance> getSpawnersInWorld(String worldName) {
        LinkedHashSet<Long> ids = worldIndex.get(worldName == null ? "" : worldName.toLowerCase(Locale.US));
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<SpawnerInstance> spawners = new ArrayList<>();
        for (Long id : ids) {
            SpawnerInstance instance = spawnersById.get(id);
            if (instance != null) {
                spawners.add(instance);
            }
        }
        spawners.sort(Comparator.comparingLong(SpawnerInstance::getStackAmount).reversed()
                .thenComparingInt(SpawnerInstance::getX));
        return spawners;
    }

    public List<WorldSummary> getWorldSummaries() {
        List<WorldSummary> summaries = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            summaries.add(new WorldSummary(world.getName(), getSpawnersInWorld(world.getName()).size()));
        }
        summaries.sort(Comparator
                .comparingInt((WorldSummary summary) -> getWorldSortIndex(summary.worldName()))
                .thenComparing(summary -> describeWorld(summary.worldName()), String.CASE_INSENSITIVE_ORDER));
        return summaries;
    }

    public void openStorage(Player player, SpawnerInstance instance, int page) {
        if (player == null || instance == null) {
            return;
        }
        new SpawnerStorageMenu(plugin, instance.getId(), page).open(player);
    }

    public void openPanel(Player player) {
        if (player == null) {
            return;
        }
        new SpawnerWorldListMenu(plugin).open(player);
    }

    public void openWorldPanel(Player player, String worldName, int page) {
        if (player == null) {
            return;
        }
        new SpawnerPanelMenu(plugin, worldName, page).open(player);
    }

    public ActionResult breakSpawner(Player player, Block block) {
        SpawnerInstance instance = getSpawner(block);
        if (instance == null) {
            return fail("&cThat is not a managed spawner.");
        }
        if (!canBreak(player, instance)) {
            return fail("&cYou do not have permission to break that spawner.");
        }

        unregisterSpawner(instance);
        plugin.getDatabaseManager().deleteSpawner(instance.getId());
        ItemStack item = createSpawnerItem(instance.getMobTypeKey(), instance.getStackAmount());
        if (item != null) {
            PlayerInventory inventory = player.getInventory();
            Map<Integer, ItemStack> leftovers = inventory.addItem(item);
            if (dropOnBreakIfInventoryFull) {
                leftovers.values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            } else if (!leftovers.isEmpty()) {
                leftovers.values().forEach(leftover -> inventory.addItem(leftover));
            }
        }

        plugin.getPaperScheduler().runRegion(block.getLocation(), () -> {
            if (plugin.getAntiEspManager() != null) {
                plugin.getAntiEspManager().refreshNearby(block.getLocation());
            }
        });
        return ok("&aPicked up &f" + NumberUtils.format(instance.getStackAmount()) + "x "
                + ColorUtils.strip(getTypeDisplayName(instance.getMobTypeKey())) + "&a.");
    }

    public List<SpawnerLootEntry> getSortedLootEntries(SpawnerInstance instance) {
        if (instance == null) {
            return List.of();
        }

        List<SpawnerLootEntry> entries = new ArrayList<>(instance.getStoredLootEntries());
        entries.sort(Comparator.comparingLong(SpawnerLootEntry::getAmount).reversed()
                .thenComparing(entry -> entry.getMaterial().name()));
        return entries;
    }

    public ActionResult collectLootEntry(Player player, SpawnerInstance instance, String lootKey, boolean collectAll) {
        if (player == null || instance == null) {
            return fail("&cSpawner not found.");
        }
        if (!canOpen(player, instance)) {
            return fail("&cYou do not have access to that spawner.");
        }

        SpawnerLootEntry entry = instance.getStoredLoot(lootKey);
        if (entry == null || entry.getAmount() <= 0L) {
            return fail("&cThat loot entry is empty.");
        }

        long requested = collectAll ? entry.getAmount() : Math.min(entry.getAmount(), entry.getMaterial().getMaxStackSize());
        long moved = moveMaterialToInventory(player.getInventory(), entry.getMaterial(), requested);
        if (moved <= 0L) {
            return fail("&cYour inventory is full.");
        }

        instance.removeStoredLoot(entry.getKey(), moved);
        instance.setUpdatedAt(System.currentTimeMillis());
        saveLoot(instance);
        return ok("&aCollected &f" + NumberUtils.format(moved) + "x "
                + plugin.getWorthManager().prettifyMaterial(entry.getMaterial()) + "&a.");
    }

    public ActionResult collectAllLoot(Player player, SpawnerInstance instance) {
        if (player == null || instance == null) {
            return fail("&cSpawner not found.");
        }
        if (!canOpen(player, instance)) {
            return fail("&cYou do not have access to that spawner.");
        }

        long totalMoved = 0L;
        for (SpawnerLootEntry entry : new ArrayList<>(instance.getStoredLootEntries())) {
            long moved = moveMaterialToInventory(player.getInventory(), entry.getMaterial(), entry.getAmount());
            if (moved <= 0L) {
                continue;
            }

            instance.removeStoredLoot(entry.getKey(), moved);
            totalMoved += moved;
        }

        if (totalMoved <= 0L) {
            return fail("&cThere was no space to collect your spawner loot.");
        }

        instance.setUpdatedAt(System.currentTimeMillis());
        saveLoot(instance);
        return ok("&aCollected &f" + NumberUtils.format(totalMoved) + "&a items from this spawner.");
    }

    public ActionResult dropAllLoot(Player player, SpawnerInstance instance) {
        if (player == null || instance == null) {
            return fail("&cSpawner not found.");
        }
        if (!canOpen(player, instance)) {
            return fail("&cYou do not have access to that spawner.");
        }

        Location dropLocation = getSpawnerCenter(instance).add(0, 0.5D, 0);
        long dropped = 0L;
        for (SpawnerLootEntry entry : new ArrayList<>(instance.getStoredLootEntries())) {
            dropped += dropMaterial(dropLocation, entry.getMaterial(), entry.getAmount());
        }

        if (dropped <= 0L) {
            return fail("&cThere is no loot stored in that spawner.");
        }

        instance.clearStoredLoot();
        instance.setUpdatedAt(System.currentTimeMillis());
        saveLoot(instance);
        return ok("&aDropped &f" + NumberUtils.format(dropped) + "&a stored items on the ground.");
    }

    public SellLootResult sellAllLoot(Player player, SpawnerInstance instance) {
        if (player == null || instance == null) {
            return failSell("&cSpawner not found.");
        }
        if (!canOpen(player, instance)) {
            return failSell("&cYou do not have access to that spawner.");
        }

        Map<SellCategory, Double> progress = new EnumMap<>(SellCategory.class);
        progress.putAll(plugin.getShopManager().getSellProgress(player.getUniqueId()));

        Map<SellCategory, Double> earnedByCategory = new EnumMap<>(SellCategory.class);
        double totalPayout = 0D;
        long soldItems = 0L;

        for (SpawnerLootEntry entry : new ArrayList<>(instance.getStoredLootEntries())) {
            ItemStack single = new ItemStack(entry.getMaterial(), 1);
            WorthResult worthResult = plugin.getWorthManager().resolveWorth(single);
            if (!worthResult.sellable()) {
                continue;
            }

            SellCategory category = plugin.getShopManager().getSellCategory(single);
            if (category == null) {
                continue;
            }

            double unitWorth = worthResult.unitWorth();
            double baseTotal = unitWorth * entry.getAmount();
            double multiplier = plugin.getShopManager().getCurrentSellMultiplier(progress, category);
            double payout = baseTotal * multiplier;
            totalPayout += payout;
            soldItems += entry.getAmount();
            earnedByCategory.merge(category, baseTotal, Double::sum);

            int historyAmount = (int) Math.min(Integer.MAX_VALUE, entry.getAmount());
            plugin.getDatabaseManager().addSellHistory(player.getUniqueId(), entry.getMaterial().name(), historyAmount, payout);
            instance.removeStoredLoot(entry.getKey(), entry.getAmount());
        }

        if (totalPayout <= 0D || soldItems <= 0L) {
            return failSell("&cThere are no sellable items stored in that spawner.");
        }

        for (Map.Entry<SellCategory, Double> progressEntry : earnedByCategory.entrySet()) {
            plugin.getDatabaseManager().addSellProgress(player.getUniqueId(), progressEntry.getKey(), progressEntry.getValue());
        }

        var depositResult = plugin.getEconomyManager().deposit(player, totalPayout, EconomyReason.SELL_PAYOUT);
        if (!depositResult.success()) {
            return failSell("&cFailed to pay out the spawner loot sale.");
        }

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data != null) {
            data.addMoneyMade(totalPayout);
        }

        instance.setUpdatedAt(System.currentTimeMillis());
        saveLoot(instance);
        return new SellLootResult(
                true,
                "&aSold &f" + NumberUtils.format(soldItems) + "&a items for &f$" + NumberUtils.formatNice(totalPayout) + "&a.",
                totalPayout,
                soldItems
        );
    }

    public ActionResult removeSpawner(SpawnerInstance instance, boolean dropItem, Player actor) {
        if (instance == null) {
            return fail("&cSpawner not found.");
        }

        unregisterSpawner(instance);
        plugin.getDatabaseManager().deleteSpawner(instance.getId());
        World world = Bukkit.getWorld(instance.getWorld());
        if (world != null && dropItem) {
            ItemStack item = createSpawnerItem(instance.getMobTypeKey(), instance.getStackAmount());
            if (item != null) {
                world.dropItemNaturally(getSpawnerCenter(instance), item);
            }
        }

        if (world != null && actor != null && actor.getLocation().getWorld() == world) {
            plugin.getPaperScheduler().runRegion(getSpawnerCenter(instance), () -> {
                if (plugin.getAntiEspManager() != null) {
                    plugin.getAntiEspManager().refreshNearby(getSpawnerCenter(instance));
                }
            });
        }
        return ok("&aSpawner removed.");
    }

    public void processGeneration() {
        if (!enabled || spawnersById.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        long intervalMillis = generationIntervalSeconds * 1000L;
        if (intervalMillis <= 0L) {
            return;
        }

        for (SpawnerInstance instance : new ArrayList<>(spawnersById.values())) {
            try {
                processSpawnerGeneration(instance, now, intervalMillis);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to process spawner generation for " + instance.getLocationKey(), exception);
            }
        }
    }

    private void processSpawnerGeneration(SpawnerInstance instance, long now, long intervalMillis) {
        SpawnerTypeDefinition definition = getTypeDefinition(instance.getMobTypeKey());
        if (definition == null) {
            return;
        }

        World world = Bukkit.getWorld(instance.getWorld());
        if (world == null) {
            return;
        }

        int chunkX = instance.getX() >> 4;
        int chunkZ = instance.getZ() >> 4;
        if (processOnlyLoadedChunks && !world.isChunkLoaded(chunkX, chunkZ)) {
            return;
        }
        if (requirePlayerNearby && !hasNearbyPlayer(world, instance, playerNearbyRadius)) {
            return;
        }

        Block block = world.getBlockAt(instance.getX(), instance.getY(), instance.getZ());
        if (block.getType() != Material.SPAWNER) {
            plugin.getLogger().warning("[SpawnerManager] Removing orphaned spawner record at " + instance.getLocationKey());
            removeSpawner(instance, false, null);
            return;
        }

        long elapsed = now - instance.getLastProcessedAt();
        if (elapsed < intervalMillis) {
            return;
        }

        long cycles = elapsed / intervalMillis;
        if (cycles <= 0L) {
            return;
        }

        long totalRolls = cycles * definition.baseItemsPerCycle() * Math.max(1L, instance.getStackAmount());
        if (totalRolls <= 0L) {
            instance.setLastProcessedAt(now);
            plugin.getDatabaseManager().saveSpawner(instance);
            return;
        }

        boolean changed = false;
        for (SpawnerTypeDefinition.DropDefinition drop : definition.drops()) {
            double expected = totalRolls * drop.chance() * drop.averageDropAmount();
            long generated = (long) Math.floor(expected);
            double remainder = expected - generated;
            if (remainder > 0D && ThreadLocalRandom.current().nextDouble() < remainder) {
                generated++;
            }

            if (generated <= 0L) {
                continue;
            }

            instance.addStoredLoot(drop.key(), drop.material(), generated, storageCapPerLootKey);
            changed = true;
        }

        instance.setLastProcessedAt(instance.getLastProcessedAt() + (cycles * intervalMillis));
        instance.setUpdatedAt(now);
        plugin.getDatabaseManager().saveSpawner(instance);
        if (changed) {
            saveLoot(instance);
        }
    }

    public String getStorageTitle(SpawnerInstance instance, int page, int maxPage) {
        String title = storageTitle
                .replace("{mob}", ColorUtils.strip(getTypeDisplayName(instance.getMobTypeKey())))
                .replace("{page}", String.valueOf(page))
                .replace("{max_page}", String.valueOf(maxPage));
        return title.length() > 32 ? title.substring(0, 32) : title;
    }

    public int getStorageSize() {
        return storageSize;
    }

    public int getStorageItemsPerPage() {
        return storageItemsPerPage;
    }

    public String getPanelTitle(String worldName) {
        String title = panelTitle.replace("{world}", describeWorld(worldName));
        return title.length() > 32 ? title.substring(0, 32) : title;
    }

    public int getPanelSize() {
        return panelSize;
    }

    public String getWorldListTitle() {
        return worldListTitle;
    }

    public int getWorldListSize() {
        return worldListSize;
    }

    public String describeWorld(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return "Unknown";
        }

        return switch (worldName.trim().toLowerCase(Locale.US)) {
            case "world", "overworld" -> "Overworld";
            case "world_nether", "nether" -> "Nether";
            case "world_the_end", "the_end", "the-end", "end" -> "The End";
            default -> prettifyLabel(worldName);
        };
    }

    public Location getSpawnerCenter(SpawnerInstance instance) {
        World world = Bukkit.getWorld(instance.getWorld());
        return world == null
                ? new Location(Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst(), 0, 0, 0)
                : new Location(world, instance.getX() + 0.5D, instance.getY() + 0.5D, instance.getZ() + 0.5D);
    }

    public boolean canOpen(Player player, SpawnerInstance instance) {
        if (player == null || instance == null) {
            return false;
        }
        if (player.hasPermission("ultimatedonutsmp.admin.spawner")) {
            return true;
        }
        if (player.getUniqueId().equals(instance.getOwnerUuid())) {
            return true;
        }

        return switch (instance.getAccessMode()) {
            case PUBLIC -> true;
            case OWNER_AND_TEAM -> plugin.getTeamManager().areTeammates(player.getUniqueId(), instance.getOwnerUuid());
            case OWNER_ONLY -> false;
        };
    }

    public boolean canBreak(Player player, SpawnerInstance instance) {
        if (player == null || instance == null) {
            return false;
        }
        return player.hasPermission("ultimatedonutsmp.admin.spawner")
                || player.getUniqueId().equals(instance.getOwnerUuid());
    }

    public boolean canModify(Player player, SpawnerInstance instance) {
        return canOpen(player, instance);
    }

    public void shutdown() {
        for (SpawnerInstance instance : spawnersById.values()) {
            plugin.getDatabaseManager().saveSpawner(instance);
            saveLoot(instance);
        }
    }

    private long moveMaterialToInventory(PlayerInventory inventory, Material material, long amount) {
        if (inventory == null || material == null || amount <= 0L) {
            return 0L;
        }

        long remaining = amount;
        long moved = 0L;
        int maxStack = material.getMaxStackSize();

        while (remaining > 0L) {
            int stackAmount = (int) Math.min(maxStack, remaining);
            ItemStack stack = new ItemStack(material, stackAmount);
            Map<Integer, ItemStack> leftovers = inventory.addItem(stack);
            if (leftovers.isEmpty()) {
                moved += stackAmount;
                remaining -= stackAmount;
                continue;
            }

            int leftoverAmount = leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
            int insertedAmount = stackAmount - leftoverAmount;
            moved += Math.max(0L, insertedAmount);
            break;
        }

        return moved;
    }

    private long dropMaterial(Location location, Material material, long amount) {
        if (location == null || location.getWorld() == null || material == null || amount <= 0L) {
            return 0L;
        }

        long remaining = amount;
        long dropped = 0L;
        int maxStack = material.getMaxStackSize();

        while (remaining > 0L) {
            int stackAmount = (int) Math.min(maxStack, remaining);
            location.getWorld().dropItemNaturally(location, new ItemStack(material, stackAmount));
            dropped += stackAmount;
            remaining -= stackAmount;
        }
        return dropped;
    }

    private boolean hasNearbyPlayer(World world, SpawnerInstance instance, double radius) {
        double radiusSquared = radius * radius;
        Location center = new Location(world, instance.getX() + 0.5D, instance.getY() + 0.5D, instance.getZ() + 0.5D);
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(center) <= radiusSquared) {
                return true;
            }
        }
        return false;
    }

    private void saveLoot(SpawnerInstance instance) {
        plugin.getDatabaseManager().replaceSpawnerLoot(instance.getId(), instance.getStoredLootEntries());
    }

    private void registerSpawner(SpawnerInstance instance) {
        spawnersById.put(instance.getId(), instance);
        locationIndex.put(instance.getLocationKey(), instance.getId());
        worldIndex.computeIfAbsent(instance.getWorld().toLowerCase(Locale.US), ignored -> new LinkedHashSet<>())
                .add(instance.getId());
    }

    private void unregisterSpawner(SpawnerInstance instance) {
        spawnersById.remove(instance.getId());
        locationIndex.remove(instance.getLocationKey());
        LinkedHashSet<Long> ids = worldIndex.get(instance.getWorld().toLowerCase(Locale.US));
        if (ids != null) {
            ids.remove(instance.getId());
            if (ids.isEmpty()) {
                worldIndex.remove(instance.getWorld().toLowerCase(Locale.US));
            }
        }
    }

    public Material getWorldIcon(World world) {
        if (world == null) {
            return Material.GRASS_BLOCK;
        }

        return switch (world.getEnvironment()) {
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE;
            default -> Material.GRASS_BLOCK;
        };
    }

    private int getWorldSortIndex(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return Integer.MAX_VALUE;
        }

        return switch (worldName.trim().toLowerCase(Locale.US)) {
            case "world", "overworld" -> 0;
            case "world_nether", "nether" -> 1;
            case "world_the_end", "the_end", "the-end", "end" -> 2;
            default -> 10;
        };
    }

    private ActionResult ok(String message) {
        return new ActionResult(true, message);
    }

    private ActionResult fail(String message) {
        return new ActionResult(false, message);
    }

    private SellLootResult failSell(String message) {
        return new SellLootResult(false, message, 0D, 0L);
    }

    public void syncSpawnerBlockState(SpawnerInstance instance) {
        if (instance == null) {
            return;
        }

        World world = Bukkit.getWorld(instance.getWorld());
        if (world == null) {
            return;
        }

        Block block = world.getBlockAt(instance.getX(), instance.getY(), instance.getZ());
        if (block.getType() != Material.SPAWNER) {
            return;
        }

        if (!(block.getState() instanceof CreatureSpawner spawnerState)) {
            return;
        }

        SpawnerTypeDefinition definition = getTypeDefinition(instance.getMobTypeKey());
        if (definition == null) {
            return;
        }

        spawnerState.setSpawnedType(definition.entityType());
        spawnerState.update(true, false);
    }

    public void consumeHeldSpawnerItem(Player player) {
        if (player == null || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType().isAir()) {
            return;
        }

        int nextAmount = hand.getAmount() - 1;
        if (nextAmount <= 0) {
            player.getInventory().setItemInMainHand(null);
            return;
        }

        hand.setAmount(nextAmount);
        player.getInventory().setItemInMainHand(hand);
    }

    private String prettifyKey(String key) {
        if (key == null || key.isBlank()) {
            return "Spawner";
        }

        return prettifyLabel(key);
    }

    private String prettifyLabel(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        String[] tokens = value.toLowerCase(Locale.US).replace('-', '_').split("_");
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(token.charAt(0))).append(token.substring(1));
        }
        return builder.toString();
    }

    private int normalizeSize(int size) {
        int normalized = Math.max(9, ((size + 8) / 9) * 9);
        return Math.min(54, normalized);
    }
}
