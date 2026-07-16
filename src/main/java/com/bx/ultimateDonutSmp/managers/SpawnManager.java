package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnManager {

    private static final double LOCATION_COUNT_RADIUS = 16.0D;
    private static final double LOCATION_COUNT_RADIUS_SQUARED = LOCATION_COUNT_RADIUS * LOCATION_COUNT_RADIUS;
    private static final String MENU_LOCATION_KEY = "LOCATION";
    private static final String LEGACY_MENU_LOCATION_KEY = "location";
    private static final String SETUP_SHARD_REGION_PATH = "SHARDS.CUBOIDS.REGIONS.spawn";

    public enum AreaType {
        SPAWN,
        AFK
    }

    public record TeleportArea(
            String id,
            AreaType type,
            int slot,
            Material material,
            String displayName,
            List<String> lore,
            String cuboidName,
            int capacity,
            Location locationOverride,
            String locationOverrideRaw
    ) {
        public TeleportArea {
            lore = List.copyOf(lore == null ? List.of() : lore);
            locationOverride = locationOverride == null ? null : locationOverride.clone();
        }
    }

    public record SetupLocationResult(boolean success, String areaId, int slot, String message) {
        public static SetupLocationResult success(String areaId, int slot) {
            return new SetupLocationResult(true, areaId, slot, "");
        }

        public static SetupLocationResult failure(String message) {
            return new SetupLocationResult(false, "", -1, message);
        }
    }

    public record AreaDeleteResult(boolean success, String message) {
        public static AreaDeleteResult success(String message) {
            return new AreaDeleteResult(true, message);
        }

        public static AreaDeleteResult failure(String message) {
            return new AreaDeleteResult(false, message);
        }
    }

    private record SetupAreaTarget(String path, String areaId, int slot) {
    }

    private final UltimateDonutSmp plugin;
    private String spawnLocationRaw;
    private String afkLocationRaw;
    private Location spawnLocation;
    private Location afkLocation;
    private List<TeleportArea> configuredSpawnAreas = List.of();
    private List<TeleportArea> configuredAfkAreas = List.of();
    private Location cachedSpawnLocation;
    private Location cachedAfkLocation;
    private final java.util.Map<String, Location> cachedAreaDestinations = new java.util.concurrent.ConcurrentHashMap<>();

    public SpawnManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        spawnLocationRaw = config.getString("LOCATIONS.SPAWN-LOCATION", "");
        afkLocationRaw = config.getString("LOCATIONS.AFK-LOCATION", "");
        spawnLocation = LocationUtils.parse(spawnLocationRaw);
        afkLocation = LocationUtils.parse(afkLocationRaw);
        configuredSpawnAreas = loadAreas("SPAWN-MENU", AreaType.SPAWN);
        configuredAfkAreas = loadAreas("AFK-MENU", AreaType.AFK);
        cachedSpawnLocation = null;
        cachedAfkLocation = null;
        cachedAreaDestinations.clear();
    }

    public SetupLocationResult setSpawnLocation(Location loc) {
        return setSetupLocation(AreaType.SPAWN, loc, "LOCATIONS.SPAWN-LOCATION");
    }

    public SetupLocationResult setAfkLocation(Location loc) {
        return setSetupLocation(AreaType.AFK, loc, "LOCATIONS.AFK-LOCATION");
    }

    public List<TeleportArea> getSpawnAreas() {
        return getValidAreas(AreaType.SPAWN);
    }

    public List<TeleportArea> getAfkAreas() {
        return getValidAreas(AreaType.AFK);
    }

    public List<TeleportArea> getValidAreas(AreaType type) {
        return buildAreas(type, true);
    }

    public List<TeleportArea> getMenuAreas(AreaType type) {
        return buildAreas(type, false);
    }

    private List<TeleportArea> buildAreas(AreaType type, boolean requireDestination) {
        List<TeleportArea> configured = type == AreaType.SPAWN ? configuredSpawnAreas : configuredAfkAreas;
        List<String> existingBoundCuboids = getExistingBoundCuboids(type);

        if (configured.isEmpty()) {
            return filterAreasByDestination(
                    buildSyntheticAreas(type, existingBoundCuboids, List.of()),
                    requireDestination
            );
        }

        List<TeleportArea> materialized = new ArrayList<>();
        Set<Integer> usedTemplateIndexes = new HashSet<>();
        Set<String> assignedCuboids = new LinkedHashSet<>();

        for (int index = 0; index < configured.size(); index++) {
            TeleportArea area = materializeConfiguredArea(configured.get(index));
            if (area != null) {
                materialized.add(area);
                usedTemplateIndexes.add(index);

                String cuboidName = trimToNull(area.cuboidName());
                if (cuboidName != null) {
                    assignedCuboids.add(cuboidName.toLowerCase());
                }
                continue;
            }

            if (!requireDestination) {
                continue;
            }
        }

        for (String boundCuboid : existingBoundCuboids) {
            if (assignedCuboids.contains(boundCuboid.toLowerCase())) {
                continue;
            }

            int matchingTemplateIndex = findMatchingTemplateIndex(configured, usedTemplateIndexes, boundCuboid);
            if (matchingTemplateIndex >= 0) {
                TeleportArea template = configured.get(matchingTemplateIndex);
                materialized.add(materializeArea(template, boundCuboid));
                usedTemplateIndexes.add(matchingTemplateIndex);
                assignedCuboids.add(boundCuboid.toLowerCase());
            }
        }

        List<String> remainingCuboids = new ArrayList<>();
        for (String boundCuboid : existingBoundCuboids) {
            if (!assignedCuboids.contains(boundCuboid.toLowerCase())) {
                remainingCuboids.add(boundCuboid);
            }
        }

        for (String boundCuboid : remainingCuboids) {
            int nextTemplateIndex = findNextTemplateIndex(configured, usedTemplateIndexes);
            if (nextTemplateIndex >= 0) {
                TeleportArea template = configured.get(nextTemplateIndex);
                materialized.add(materializeArea(template, boundCuboid));
                usedTemplateIndexes.add(nextTemplateIndex);
                assignedCuboids.add(boundCuboid.toLowerCase());
            }
        }

        List<String> extraCuboids = new ArrayList<>();
        for (String boundCuboid : existingBoundCuboids) {
            if (!assignedCuboids.contains(boundCuboid.toLowerCase())) {
                extraCuboids.add(boundCuboid);
            }
        }

        if (!extraCuboids.isEmpty()) {
            materialized.addAll(buildSyntheticAreas(type, extraCuboids, materialized));
        }

        materialized.sort(Comparator.comparingInt(TeleportArea::slot));
        return filterAreasByDestination(materialized, requireDestination);
    }

    public Set<String> getAreaCuboidNames(AreaType type) {
        LinkedHashSet<String> cuboidNames = new LinkedHashSet<>(getBoundCuboidNames(type));
        List<TeleportArea> configured = type == AreaType.SPAWN ? configuredSpawnAreas : configuredAfkAreas;
        for (TeleportArea area : configured) {
            String cuboidName = trimToNull(area.cuboidName());
            if (cuboidName != null) {
                cuboidNames.add(cuboidName.toLowerCase());
            }
        }
        return Collections.unmodifiableSet(cuboidNames);
    }

    public boolean hasMultipleAreas(AreaType type) {
        return getValidAreas(type).size() > 1;
    }

    public boolean isMenuEnabled(AreaType type) {
        String path = type == AreaType.SPAWN ? "SETTINGS.SPAWN-MENU" : "SETTINGS.AFK-MENU";
        return plugin.getConfigManager().getConfig().getBoolean(path, true);
    }

    public boolean shouldOpenMenu(AreaType type) {
        return isMenuEnabled(type) && hasMenuDefinition(type);
    }

    public boolean hasMenuDefinition(AreaType type) {
        FileConfiguration menus = plugin.getConfigManager().getMenus();
        String menuPath = type == AreaType.SPAWN ? "SPAWN-MENU" : "AFK-MENU";
        return menus.getConfigurationSection(menuPath) != null;
    }

    public TeleportArea getRandomArea(AreaType type) {
        List<TeleportArea> areas = getValidAreas(type);
        if (areas.isEmpty()) {
            return null;
        }

        int startIndex = ThreadLocalRandom.current().nextInt(areas.size());
        for (int offset = 0; offset < areas.size(); offset++) {
            TeleportArea area = areas.get((startIndex + offset) % areas.size());
            if (resolveDestination(area) != null) {
                return area;
            }
        }
        return null;
    }

    public int countPlayersInArea(TeleportArea area) {
        if (area == null) {
            return 0;
        }

        String cuboidName = trimToNull(area.cuboidName());
        if (cuboidName != null && plugin.getCuboidManager().exists(cuboidName)) {
            return plugin.getCuboidManager().countPlayersInCuboid(cuboidName);
        }

        Location destination = resolveDestination(area);
        if (destination == null || destination.getWorld() == null) {
            return 0;
        }

        int count = 0;
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            Location playerLocation = onlinePlayer.getLocation();
            if (playerLocation.getWorld() != null
                    && playerLocation.getWorld().equals(destination.getWorld())
                    && playerLocation.distanceSquared(destination) <= LOCATION_COUNT_RADIUS_SQUARED) {
                count++;
            }
        }
        return count;
    }

    public Location resolveDestination(TeleportArea area) {
        if (area == null) {
            return null;
        }
        String cacheKey = area.type().name() + ":" + area.id() + ":" + (area.cuboidName() == null ? "" : area.cuboidName());
        if (cachedAreaDestinations.containsKey(cacheKey)) {
            return cachedAreaDestinations.get(cacheKey);
        }
        Location resolved = resolveDestinationDirect(area);
        if (resolved != null) {
            cachedAreaDestinations.put(cacheKey, resolved);
        }
        return resolved;
    }

    private Location resolveDestinationDirect(TeleportArea area) {
        Location override = area.locationOverride();
        if (override == null && area.locationOverrideRaw() != null) {
            String serialized = area.locationOverrideRaw().trim();
            if (!serialized.isBlank() && !serialized.matches("\\d+")) {
                override = LocationUtils.parse(serialized);
            }
        }

        if (override != null) {
            Location overrideDestination = makeSafeDestination(override);
            if (overrideDestination != null) {
                return overrideDestination;
            }
        }

        String cuboidName = trimToNull(area.cuboidName());
        if (cuboidName != null) {
            Location destination = plugin.getCuboidManager().getCuboidTeleportLocation(cuboidName);
            if (destination != null) {
                Location safeDestination = makeSafeDestination(destination);
                if (safeDestination != null) {
                    return safeDestination;
                }
            }

            Location center = plugin.getCuboidManager().getCuboidCenter(cuboidName);
            return center == null ? null : makeSafeDestination(center);
        }

        return null;
    }

    public Location getFirstAreaDestination(AreaType type) {
        for (TeleportArea area : getValidAreas(type)) {
            Location destination = resolveDestination(area);
            if (destination != null) {
                return destination;
            }
        }
        return null;
    }

    public Location getRandomAreaDestination(AreaType type) {
        return resolveDestination(getRandomArea(type));
    }

    public Location resolveCommandDestination(AreaType type) {
        List<TeleportArea> areas = getValidAreas(type);
        if (!areas.isEmpty()) {
            if (areas.size() == 1) {
                return resolveDestination(areas.get(0));
            }
            return getRandomAreaDestination(type);
        }

        Location legacyLocation = type == AreaType.SPAWN ? getSpawnLocation() : getAfkLocation();
        if (legacyLocation != null) {
            return legacyLocation;
        }

        return resolveBoundCuboidDestination(type);
    }

    public Location getSpawnLocation() {
        if (cachedSpawnLocation != null) {
            return cachedSpawnLocation;
        }
        Location resolved = resolveSpawnLocationDirect();
        if (resolved != null) {
            cachedSpawnLocation = resolved;
        }
        return resolved;
    }

    private Location resolveSpawnLocationDirect() {
        if (spawnLocation == null && spawnLocationRaw != null && !spawnLocationRaw.isBlank()) {
            spawnLocation = LocationUtils.parse(spawnLocationRaw);
        }
        if (spawnLocation != null) {
            Location safeSpawn = makeSafeDestination(spawnLocation);
            if (safeSpawn != null) {
                return safeSpawn;
            }
        }

        Location areaDestination = getFirstAreaDestination(AreaType.SPAWN);
        if (areaDestination != null) {
            return areaDestination;
        }

        return resolveBoundCuboidDestination(AreaType.SPAWN);
    }

    public Location getAfkLocation() {
        if (cachedAfkLocation != null) {
            return cachedAfkLocation;
        }
        Location resolved = resolveAfkLocationDirect();
        if (resolved != null) {
            cachedAfkLocation = resolved;
        }
        return resolved;
    }

    private Location resolveAfkLocationDirect() {
        if (afkLocation == null && afkLocationRaw != null && !afkLocationRaw.isBlank()) {
            afkLocation = LocationUtils.parse(afkLocationRaw);
        }
        if (afkLocation != null) {
            Location safeAfk = makeSafeDestination(afkLocation);
            if (safeAfk != null) {
                return safeAfk;
            }
        }

        Location areaDestination = getFirstAreaDestination(AreaType.AFK);
        if (areaDestination != null) {
            return areaDestination;
        }

        return resolveBoundCuboidDestination(AreaType.AFK);
    }

    public boolean hasSpawn() {
        return getSpawnLocation() != null;
    }

    public boolean hasAfk() {
        return getAfkLocation() != null;
    }

    private List<TeleportArea> loadAreas(String menuPath, AreaType type) {
        FileConfiguration menus = plugin.getConfigManager().getMenus();
        ConfigurationSection areasSection = menus.getConfigurationSection(menuPath + ".AREAS");
        if (areasSection == null) {
            return List.of();
        }

        int menuSize = normalizeSize(menus.getInt(menuPath + ".SIZE", 54));
        int randomSlot = menus.getInt(menuPath + ".RANDOM-BUTTON.SLOT", -1);
        Set<Integer> usedSlots = new HashSet<>();
        List<TeleportArea> loaded = new ArrayList<>();

        for (String key : areasSection.getKeys(false)) {
            ConfigurationSection section = areasSection.getConfigurationSection(key);
            if (section == null || !section.getBoolean("ENABLED", true)) {
                continue;
            }

            int slot = section.getInt("SLOT", -1);
            if (slot < 0 || slot >= menuSize) {
                warn(menuPath + ".AREAS." + key + " uses invalid slot " + slot + " for size " + menuSize + ".");
                continue;
            }
            if (slot == randomSlot) {
                warn(menuPath + ".AREAS." + key + " collides with the random button slot " + randomSlot + ".");
                continue;
            }
            if (!usedSlots.add(slot)) {
                warn(menuPath + ".AREAS." + key + " collides with another area on slot " + slot + ".");
                continue;
            }

            String cuboidName = section.getString("CUBOID", "").trim();
            if (cuboidName.isBlank()) {
                warn(menuPath + ".AREAS." + key + " is missing cuboid. it can still be used only as a generic visual template.");
            }

            String areaPath = menuPath + ".AREAS." + key;
            String locationOverrideRaw = section.getString(MENU_LOCATION_KEY);
            if (locationOverrideRaw == null) {
                locationOverrideRaw = section.getString(LEGACY_MENU_LOCATION_KEY);
            }
            Location locationOverride = parseConfiguredAreaLocation(type, section, areaPath);

            loaded.add(new TeleportArea(
                    key,
                    type,
                    slot,
                    ItemUtils.parseMaterial(section.getString("MATERIAL", "ITEM_FRAME")),
                    section.getString("DISPLAY-NAME", type == AreaType.SPAWN ? "&bspawn" : "&#A303F9afk"),
                    section.getStringList("LORE"),
                    cuboidName,
                    Math.max(1, section.getInt("CAPACITY", 200)),
                    locationOverride,
                    locationOverrideRaw
            ));
        }

        loaded.sort(Comparator.comparingInt(TeleportArea::slot));
        return List.copyOf(loaded);
    }

    private Location parseAreaLocation(AreaType type, Object rawValue, String path) {
        if (rawValue == null) {
            return null;
        }

        String serialized = String.valueOf(rawValue).trim();
        if (serialized.isBlank()) {
            return null;
        }

        if (serialized.matches("\\d+")) {
            return null;
        }

        Location parsed = LocationUtils.parse(serialized);
        if (parsed == null) {
            warn(path + " has an invalid location override '" + serialized + "'. falling back to cuboid teleport.");
        }
        return parsed;
    }

    private SetupLocationResult setSetupLocation(AreaType type, Location location, String configPath) {
        String serialized = LocationUtils.serialize(location);
        FileConfiguration config = plugin.getConfigManager().getConfig();
        FileConfiguration menus = plugin.getConfigManager().getOriginalMenus();
        SetupAreaTarget target = findNextSetupAreaTarget(type);
        if (target == null) {
            return SetupLocationResult.failure("ɴᴏ ꜰʀᴇᴇ " + getLocationLabel(type) + " ᴍᴇɴᴜ ѕʟᴏᴛ ɪѕ ᴀᴠᴀɪʟᴀʙʟᴇ.");
        }

        config.set(configPath, serialized);
        config.set(getMenuTogglePath(type), true);
        menus.set(target.path() + "." + MENU_LOCATION_KEY, serialized);
        menus.set(target.path() + "." + LEGACY_MENU_LOCATION_KEY, null);
        configureSetupShardRegion(config, menus, target, location, serialized, type);

        try {
            if (!plugin.getConfigManager().saveConfig()) {
                return SetupLocationResult.failure("failed to save config.yml.");
            }
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("[SpawnManager] failed to save config.yml: " + exception.getMessage());
            return SetupLocationResult.failure("failed to save config.yml.");
        }

        boolean savedMenus = plugin.getConfigManager().saveMenus();
        if (!savedMenus) {
            return SetupLocationResult.failure("failed to save menus.yml.");
        }

        load();
        if (plugin.getShardManager() != null) {
            plugin.getShardManager().reloadSettings();
        }
        return SetupLocationResult.success(target.areaId(), target.slot());
    }

    private String getMenuTogglePath(AreaType type) {
        return type == AreaType.SPAWN ? "SETTINGS.SPAWN-MENU" : "SETTINGS.AFK-MENU";
    }

    private void configureSetupShardRegion(
            FileConfiguration config,
            FileConfiguration menus,
            SetupAreaTarget target,
            Location location,
            String serialized,
            AreaType type
    ) {
        if (location == null || location.getWorld() == null || serialized == null || serialized.isBlank()) {
            return;
        }

        String cuboidName = trimToNull(menus.getString(target.path() + ".CUBOID"));
        if (cuboidName == null) {
            cuboidName = defaultCuboidName(type, parsePositiveInt(target.areaId(), 1));
        }

        config.set(SETUP_SHARD_REGION_PATH + ".ENABLED", true);
        config.set(SETUP_SHARD_REGION_PATH + ".WORLD", location.getWorld().getName());

        if (type == AreaType.SPAWN) {
            config.set(SETUP_SHARD_REGION_PATH + ".BOUND", true);
            config.set(SETUP_SHARD_REGION_PATH + ".CUBOID", cuboidName);
            config.set(SETUP_SHARD_REGION_PATH + "." + MENU_LOCATION_KEY, serialized);
            if (config.getDouble(SETUP_SHARD_REGION_PATH + ".RADIUS", 0D) <= 0D) {
                config.set(SETUP_SHARD_REGION_PATH + ".RADIUS", LOCATION_COUNT_RADIUS);
            }
        } else if (type == AreaType.AFK) {
            config.set(SETUP_SHARD_REGION_PATH + ".AFK-CUBOID", cuboidName);
            config.set(SETUP_SHARD_REGION_PATH + ".AFK-LOCATION", serialized);
        }
    }

    private SetupAreaTarget findNextSetupAreaTarget(AreaType type) {
        FileConfiguration menus = plugin.getConfigManager().getOriginalMenus();
        String menuPath = type == AreaType.SPAWN ? "SPAWN-MENU" : "AFK-MENU";
        if (menus.getConfigurationSection(menuPath) == null) {
            return null;
        }

        ConfigurationSection areasSection = menus.getConfigurationSection(menuPath + ".AREAS");
        if (areasSection == null) {
            areasSection = menus.createSection(menuPath + ".AREAS");
        }

        String selectedKey = null;
        int selectedSlot = Integer.MAX_VALUE;
        int menuSize = normalizeSize(menus.getInt(menuPath + ".SIZE", 54));
        int randomSlot = menus.getInt(menuPath + ".RANDOM-BUTTON.SLOT", -1);
        Set<Integer> usedSlots = new HashSet<>();
        if (randomSlot >= 0) {
            usedSlots.add(randomSlot);
        }

        for (String key : areasSection.getKeys(false)) {
            ConfigurationSection areaSection = areasSection.getConfigurationSection(key);
            if (areaSection == null) {
                continue;
            }

            int slot = areaSection.getInt("SLOT", -1);
            if (slot >= 0 && slot < menuSize) {
                usedSlots.add(slot);
            }

            if (!areaSection.getBoolean("ENABLED", true)
                    || slot < 0
                    || slot >= menuSize
                    || slot == randomSlot
                    || hasSetupDestination(type, menuPath + ".AREAS." + key, areaSection)) {
                continue;
            }

            if (selectedKey == null || slot < selectedSlot) {
                selectedKey = key;
                selectedSlot = slot;
            }
        }

        if (selectedKey != null) {
            String missingKey = firstMissingAreaKeyBefore(areasSection, parsePositiveInt(selectedKey, Integer.MAX_VALUE));
            if (missingKey != null) {
                SetupAreaTarget missingTarget = createSetupAreaTarget(
                        type,
                        areasSection,
                        menuPath,
                        missingKey,
                        menuSize,
                        randomSlot,
                        usedSlots
                );
                if (missingTarget != null) {
                    return missingTarget;
                }
            }
            return new SetupAreaTarget(menuPath + ".AREAS." + selectedKey, selectedKey, selectedSlot);
        }

        String nextKey = nextAreaKey(areasSection);
        return createSetupAreaTarget(type, areasSection, menuPath, nextKey, menuSize, randomSlot, usedSlots);
    }

    private SetupAreaTarget createSetupAreaTarget(
            AreaType type,
            ConfigurationSection areasSection,
            String menuPath,
            String nextKey,
            int menuSize,
            int randomSlot,
            Set<Integer> usedSlots
    ) {
        int areaNumber = parsePositiveInt(nextKey, areasSection.getKeys(false).size() + 1);
        int nextSlot = findPreferredSetupSlot(areasSection, areaNumber, menuSize, randomSlot, usedSlots);
        if (nextSlot < 0) {
            return null;
        }

        ConfigurationSection template = findSetupAreaTemplate(areasSection);
        ConfigurationSection newArea = areasSection.createSection(nextKey);
        newArea.set("SLOT", nextSlot);
        newArea.set("MATERIAL", template == null ? "ITEM_FRAME" : template.getString("MATERIAL", "ITEM_FRAME"));
        newArea.set("DISPLAY-NAME", setupAreaDisplayName(type, template, areaNumber));
        newArea.set("LORE", template == null ? defaultLore(type) : template.getStringList("LORE"));
        newArea.set("CUBOID", defaultCuboidName(type, areaNumber));
        newArea.set("CAPACITY", template == null ? 200 : Math.max(1, template.getInt("CAPACITY", 200)));

        return new SetupAreaTarget(menuPath + ".AREAS." + nextKey, nextKey, nextSlot);
    }

    public AreaDeleteResult deleteMenuArea(TeleportArea area) {
        if (area == null) {
            return AreaDeleteResult.failure("area is not available.");
        }

        if (!isStoredMenuArea(area)) {
            return AreaDeleteResult.failure("this area is not stored in menus.yml.");
        }

        FileConfiguration menus = plugin.getConfigManager().getOriginalMenus();
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String menuPath = area.type() == AreaType.SPAWN ? "SPAWN-MENU" : "AFK-MENU";
        String areaPath = menuPath + ".AREAS." + area.id();
        ConfigurationSection areaSection = menus.getConfigurationSection(areaPath);
        Location deletedLocation = areaSection == null ? null : parseConfiguredAreaLocation(area.type(), areaSection, areaPath);
        boolean keepReusableSlot = shouldKeepReusableMenuSlot(areaSection);

        if (keepReusableSlot) {
            menus.set(areaPath + ".CUBOID", null);
            menus.set(areaPath + "." + MENU_LOCATION_KEY, null);
            menus.set(areaPath + "." + LEGACY_MENU_LOCATION_KEY, null);
        } else {
            menus.set(areaPath, null);
        }

        boolean configChanged = clearMatchingSetupLocation(config, area.type(), deletedLocation);
        configChanged = clearMatchingCuboidBind(config, area.type(), area.cuboidName()) || configChanged;
        if (area.type() == AreaType.AFK) {
            configChanged = clearMatchingSetupShardRegion(config, deletedLocation) || configChanged;
        }

        if (configChanged) {
            try {
                if (!plugin.getConfigManager().saveConfig()) {
                    return AreaDeleteResult.failure("failed to save config.yml.");
                }
            } catch (RuntimeException exception) {
                plugin.getLogger().warning("[SpawnManager] failed to save config.yml: " + exception.getMessage());
                return AreaDeleteResult.failure("failed to save config.yml.");
            }
        }

        if (!plugin.getConfigManager().saveMenus()) {
            return AreaDeleteResult.failure("failed to save menus.yml.");
        }

        load();
        if (configChanged && plugin.getShardManager() != null) {
            plugin.getShardManager().reloadSettings();
        }
        return AreaDeleteResult.success("ʀᴇᴍᴏᴠᴇᴅ " + getLocationLabel(area.type()) + " ᴀʀᴇᴀ "
                + area.id() + " from slot " + area.slot() + ".");
    }

    public boolean isStoredMenuArea(TeleportArea area) {
        if (area == null) {
            return false;
        }

        FileConfiguration menus = plugin.getConfigManager().getMenus();
        String menuPath = area.type() == AreaType.SPAWN ? "SPAWN-MENU" : "AFK-MENU";
        return menus.getConfigurationSection(menuPath + ".AREAS." + area.id()) != null;
    }

    private boolean shouldKeepReusableMenuSlot(ConfigurationSection areaSection) {
        return areaSection != null;
    }

    private boolean clearMatchingSetupLocation(FileConfiguration config, AreaType type, Location deletedLocation) {
        if (deletedLocation == null) {
            return false;
        }

        String path = type == AreaType.SPAWN ? "LOCATIONS.SPAWN-LOCATION" : "LOCATIONS.AFK-LOCATION";
        Location configuredLocation = LocationUtils.parse(config.getString(path, ""));
        if (!sameStoredLocation(configuredLocation, deletedLocation)) {
            return false;
        }

        config.set(path, "");
        return true;
    }

    private boolean clearMatchingCuboidBind(FileConfiguration config, AreaType type, String cuboidName) {
        String normalizedCuboid = trimToNull(cuboidName);
        if (normalizedCuboid == null) {
            return false;
        }

        normalizedCuboid = normalizedCuboid.toLowerCase();
        if (type == AreaType.SPAWN) {
            boolean changed = removeFromConfigStringList(config, "CUBOID-BINDS.SPAWN", normalizedCuboid);
            List<String> spawnBinds = config.getStringList("CUBOID-BINDS.SPAWN");
            String legacySpawn = trimToNull(config.getString("AFK-SYSTEM.SPAWN-CUBOID-NAME"));
            if (changed || (legacySpawn != null && legacySpawn.equalsIgnoreCase(normalizedCuboid))) {
                config.set("AFK-SYSTEM.SPAWN-CUBOID-NAME", spawnBinds.isEmpty() ? "" : spawnBinds.get(0));
                changed = true;
            }
            return changed;
        }

        boolean changed = removeFromConfigStringList(config, "CUBOID-BINDS.AFK", normalizedCuboid);
        List<String> afkBinds = config.getStringList("CUBOID-BINDS.AFK");
        String legacyAfk = trimToNull(config.getString("AFK-SYSTEM.AFK-CUBOID-NAME"));
        if (changed || (legacyAfk != null && legacyAfk.equalsIgnoreCase(normalizedCuboid))) {
            config.set("AFK-SYSTEM.AFK-CUBOID-NAME", afkBinds.isEmpty() ? "" : afkBinds.get(0));
            changed = true;
        }

        String shardCuboid = trimToNull(config.getString(SETUP_SHARD_REGION_PATH + ".CUBOID"));
        if (shardCuboid != null && shardCuboid.equalsIgnoreCase(normalizedCuboid)) {
            config.set(SETUP_SHARD_REGION_PATH + ".CUBOID", "");
            config.set(SETUP_SHARD_REGION_PATH + ".WORLD", "");
            if (trimToNull(config.getString(SETUP_SHARD_REGION_PATH + "." + MENU_LOCATION_KEY)) == null) {
                config.set(SETUP_SHARD_REGION_PATH + ".ENABLED", false);
                config.set(SETUP_SHARD_REGION_PATH + ".BOUND", false);
            }
            changed = true;
        }
        return changed;
    }

    private boolean removeFromConfigStringList(FileConfiguration config, String path, String value) {
        List<String> current = new ArrayList<>(config.getStringList(path));
        int originalSize = current.size();
        current.removeIf(entry -> entry.equalsIgnoreCase(value));
        if (current.size() == originalSize) {
            return false;
        }

        config.set(path, current);
        return true;
    }

    private boolean clearMatchingSetupShardRegion(FileConfiguration config, Location deletedLocation) {
        if (deletedLocation == null) {
            return false;
        }

        Location configuredLocation = LocationUtils.parse(config.getString(SETUP_SHARD_REGION_PATH + "." + MENU_LOCATION_KEY, ""));
        if (!sameStoredLocation(configuredLocation, deletedLocation)) {
            return false;
        }

        config.set(SETUP_SHARD_REGION_PATH + "." + MENU_LOCATION_KEY, "");
        config.set(SETUP_SHARD_REGION_PATH + ".AFK-LOCATION", "");

        String cuboidName = trimToNull(config.getString(SETUP_SHARD_REGION_PATH + ".CUBOID"));
        if (cuboidName == null || !plugin.getCuboidManager().exists(cuboidName)) {
            config.set(SETUP_SHARD_REGION_PATH + ".ENABLED", false);
            config.set(SETUP_SHARD_REGION_PATH + ".BOUND", false);
        }
        return true;
    }

    private boolean sameStoredLocation(Location first, Location second) {
        if (first == null || second == null || first.getWorld() == null || second.getWorld() == null) {
            return false;
        }
        return first.getWorld().getName().equalsIgnoreCase(second.getWorld().getName())
                && Math.abs(first.getX() - second.getX()) < 0.000001D
                && Math.abs(first.getY() - second.getY()) < 0.000001D
                && Math.abs(first.getZ() - second.getZ()) < 0.000001D
                && Math.abs(first.getYaw() - second.getYaw()) < 0.0001F
                && Math.abs(first.getPitch() - second.getPitch()) < 0.0001F;
    }

    private boolean hasSetupDestination(AreaType type, String areaPath, ConfigurationSection areaSection) {
        Location location = parseConfiguredAreaLocation(type, areaSection, areaPath);
        if (location != null && makeSafeDestination(location) != null) {
            return true;
        }

        String cuboidName = trimToNull(areaSection.getString("CUBOID", ""));
        return cuboidName != null && plugin.getCuboidManager().exists(cuboidName);
    }

    private Location parseConfiguredAreaLocation(AreaType type, ConfigurationSection areaSection, String areaPath) {
        Location location = parseAreaLocation(type, areaSection.get(MENU_LOCATION_KEY), areaPath + "." + MENU_LOCATION_KEY);
        if (location != null) {
            return location;
        }
        return parseAreaLocation(type, areaSection.get(LEGACY_MENU_LOCATION_KEY), areaPath + "." + LEGACY_MENU_LOCATION_KEY);
    }

    private ConfigurationSection findSetupAreaTemplate(ConfigurationSection areasSection) {
        ConfigurationSection template = null;
        int templateSlot = Integer.MAX_VALUE;
        for (String key : areasSection.getKeys(false)) {
            ConfigurationSection section = areasSection.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            int slot = section.getInt("SLOT", Integer.MAX_VALUE);
            if (template == null || slot < templateSlot) {
                template = section;
                templateSlot = slot;
            }
        }
        return template;
    }

    private String firstMissingAreaKeyBefore(ConfigurationSection areasSection, int maxExclusive) {
        if (maxExclusive <= 1) {
            return null;
        }

        for (int candidate = 1; candidate < maxExclusive; candidate++) {
            String key = String.valueOf(candidate);
            if (!areasSection.contains(key)) {
                return key;
            }
        }
        return null;
    }

    private String nextAreaKey(ConfigurationSection areasSection) {
        int candidate = 1;
        while (areasSection.contains(String.valueOf(candidate))) {
            candidate++;
        }
        return String.valueOf(candidate);
    }

    private int findPreferredSetupSlot(
            ConfigurationSection areasSection,
            int areaNumber,
            int menuSize,
            int randomSlot,
            Set<Integer> usedSlots
    ) {
        int preferredSlot = Integer.MAX_VALUE;
        for (String key : areasSection.getKeys(false)) {
            int existingNumber = parsePositiveInt(key, -1);
            if (existingNumber < 1) {
                continue;
            }

            ConfigurationSection section = areasSection.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            int existingSlot = section.getInt("SLOT", -1);
            int candidateSlot = existingSlot - (existingNumber - areaNumber);
            if (candidateSlot >= 0
                    && candidateSlot < menuSize
                    && candidateSlot != randomSlot
                    && !usedSlots.contains(candidateSlot)
                    && candidateSlot < preferredSlot) {
                preferredSlot = candidateSlot;
            }
        }

        if (preferredSlot != Integer.MAX_VALUE) {
            return preferredSlot;
        }
        return findNextFreeSlot(menuSize, usedSlots);
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String setupAreaDisplayName(AreaType type, ConfigurationSection template, int areaNumber) {
        String templateName = template == null ? null : trimToNull(template.getString("DISPLAY-NAME", ""));
        if (templateName != null) {
            return templateName.replaceFirst("#\\d+\\s*$", "#" + areaNumber);
        }
        return defaultDisplayName(type, areaNumber);
    }

    private String defaultCuboidName(AreaType type, int areaNumber) {
        return (type == AreaType.SPAWN ? "spawn" : "afk") + areaNumber;
    }

    private String getLocationLabel(AreaType type) {
        return type == AreaType.AFK ? "AFK" : "spawn";
    }

    public Location makeSafeDestination(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        Location safe = findSafeStandingLocation(location);
        return safe == null ? null : safe;
    }

    private Location findSafeStandingLocation(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return null;
        }

        int x = location.getBlockX();
        int z = location.getBlockZ();
        int preferredFeetY = clamp((int) Math.floor(location.getY()), world.getMinHeight() + 1, world.getMaxHeight() - 2);

        Location nearby = scanForSafeStandingLocation(
                location,
                Math.min(world.getMaxHeight() - 2, preferredFeetY + 4),
                Math.max(world.getMinHeight() + 1, preferredFeetY - 16)
        );
        if (nearby != null) {
            return nearby;
        }

        int highestGroundY = world.getHighestBlockYAt(x, z);
        Location surface = toSafeStandingLocation(location, highestGroundY + 1);
        if (surface != null) {
            return surface;
        }

        return scanForSafeStandingLocation(location, world.getMaxHeight() - 2, world.getMinHeight() + 1);
    }

    private Location scanForSafeStandingLocation(Location origin, int startFeetY, int minFeetY) {
        for (int feetY = startFeetY; feetY >= minFeetY; feetY--) {
            Location safe = toSafeStandingLocation(origin, feetY);
            if (safe != null) {
                return safe;
            }
        }
        return null;
    }

    private Location toSafeStandingLocation(Location origin, int feetY) {
        World world = origin.getWorld();
        if (world == null || !isSafeStandingLocation(world, origin.getBlockX(), feetY, origin.getBlockZ())) {
            return null;
        }

        return new Location(
                world,
                origin.getX(),
                feetY,
                origin.getZ(),
                origin.getYaw(),
                origin.getPitch()
        );
    }

    private boolean isSafeStandingLocation(World world, int x, int feetY, int z) {
        if (feetY <= world.getMinHeight() || feetY + 1 >= world.getMaxHeight()) {
            return false;
        }

        Block ground = world.getBlockAt(x, feetY - 1, z);
        Block feet = world.getBlockAt(x, feetY, z);
        Block head = world.getBlockAt(x, feetY + 1, z);

        return isSafeGround(ground.getType())
                && isSafeBodySpace(feet)
                && isSafeBodySpace(head);
    }

    private boolean isSafeGround(Material material) {
        return material != null
                && material.isSolid()
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
                || typeName.contains("WATER")
                || typeName.contains("FIRE")
                || typeName.contains("CACTUS")
                || typeName.contains("MAGMA")
                || typeName.contains("CAMPFIRE")
                || typeName.contains("POWDER_SNOW")
                || typeName.contains("SWEET_BERRY_BUSH")
                || typeName.contains("VOID");
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private Location resolveBoundCuboidDestination(AreaType type) {
        for (String cuboidName : getBoundCuboidNames(type)) {
            Location destination = plugin.getCuboidManager().getCuboidTeleportLocation(cuboidName);
            if (destination != null) {
                Location safeDestination = makeSafeDestination(destination);
                if (safeDestination != null) {
                    return safeDestination;
                }
            }

            Location center = plugin.getCuboidManager().getCuboidCenter(cuboidName);
            if (center != null) {
                Location safeCenter = makeSafeDestination(center);
                if (safeCenter != null) {
                    return safeCenter;
                }
            }
        }
        return null;
    }

    private Set<String> getBoundCuboidNames(AreaType type) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (type == AreaType.SPAWN) {
            Set<String> spawnBinds = readBoundList(config, "CUBOID-BINDS.SPAWN");
            if (!spawnBinds.isEmpty()) {
                return spawnBinds;
            }

            String legacySpawn = trimToNull(config.getString("AFK-SYSTEM.SPAWN-CUBOID-NAME"));
            return legacySpawn == null ? Set.of() : Set.of(legacySpawn.toLowerCase());
        }

        Set<String> afkBinds = readBoundList(config, "CUBOID-BINDS.AFK");
        if (!afkBinds.isEmpty()) {
            return afkBinds;
        }

        String legacyAfk = trimToNull(config.getString("AFK-SYSTEM.AFK-CUBOID-NAME"));
        if (legacyAfk != null) {
            return Set.of(legacyAfk.toLowerCase());
        }

        String shardAfk = trimToNull(config.getString("SHARDS.CUBOIDS.REGIONS.spawn.AFK-CUBOID"));
        return shardAfk == null ? Set.of() : Set.of(shardAfk.toLowerCase());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Set<String> readBoundList(FileConfiguration config, String path) {
        List<String> raw = config.getStringList(path);
        if (raw.isEmpty()) {
            return Set.of();
        }

        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (String entry : raw) {
            String trimmed = trimToNull(entry);
            if (trimmed != null) {
                values.add(trimmed.toLowerCase());
            }
        }
        return Collections.unmodifiableSet(values);
    }

    private List<String> getExistingBoundCuboids(AreaType type) {
        List<String> existingBoundCuboids = new ArrayList<>();
        for (String cuboidName : getBoundCuboidNames(type)) {
            if (plugin.getCuboidManager().exists(cuboidName)) {
                existingBoundCuboids.add(cuboidName);
            }
        }
        return existingBoundCuboids;
    }

    private TeleportArea materializeConfiguredArea(TeleportArea template) {
        String cuboidName = trimToNull(template.cuboidName());
        if (cuboidName != null && plugin.getCuboidManager().exists(cuboidName)) {
            return materializeArea(template, cuboidName);
        }

        if (template.locationOverride() != null || template.locationOverrideRaw() != null) {
            return template;
        }

        return null;
    }

    private List<TeleportArea> filterAreasByDestination(List<TeleportArea> areas, boolean requireDestination) {
        if (!requireDestination || areas.isEmpty()) {
            return List.copyOf(areas);
        }

        List<TeleportArea> filtered = new ArrayList<>();
        for (TeleportArea area : areas) {
            if (resolveDestination(area) != null) {
                filtered.add(area);
            }
        }
        return List.copyOf(filtered);
    }

    private int findMatchingTemplateIndex(List<TeleportArea> templates, Set<Integer> usedIndexes, String boundCuboid) {
        for (int index = 0; index < templates.size(); index++) {
            if (usedIndexes.contains(index)) {
                continue;
            }

            TeleportArea template = templates.get(index);
            String templateCuboid = trimToNull(template.cuboidName());
            if (templateCuboid != null && templateCuboid.equalsIgnoreCase(boundCuboid)) {
                return index;
            }
        }
        return -1;
    }

    private int findNextTemplateIndex(List<TeleportArea> templates, Set<Integer> usedIndexes) {
        for (int index = 0; index < templates.size(); index++) {
            if (!usedIndexes.contains(index)) {
                return index;
            }
        }
        return -1;
    }

    private TeleportArea materializeArea(TeleportArea template, String boundCuboid) {
        return new TeleportArea(
                template.id(),
                template.type(),
                template.slot(),
                template.material(),
                template.displayName(),
                template.lore(),
                boundCuboid,
                template.capacity(),
                template.locationOverride(),
                template.locationOverrideRaw()
        );
    }

    private List<TeleportArea> buildSyntheticAreas(AreaType type, List<String> cuboids, List<TeleportArea> existingAreas) {
        if (cuboids.isEmpty()) {
            return List.of();
        }

        FileConfiguration menus = plugin.getConfigManager().getMenus();
        String menuPath = type == AreaType.SPAWN ? "SPAWN-MENU" : "AFK-MENU";
        int menuSize = normalizeSize(menus.getInt(menuPath + ".SIZE", 54));
        int randomSlot = menus.getInt(menuPath + ".RANDOM-BUTTON.SLOT", -1);

        Material material = existingAreas.isEmpty()
                ? ItemUtils.parseMaterial(menus.getString(menuPath + ".AREAS.1.MATERIAL", "ITEM_FRAME"))
                : existingAreas.get(0).material();
        List<String> lore = existingAreas.isEmpty()
                ? defaultLore(type)
                : existingAreas.get(0).lore();

        Set<Integer> usedSlots = new HashSet<>();
        for (TeleportArea area : existingAreas) {
            usedSlots.add(area.slot());
        }
        if (randomSlot >= 0) {
            usedSlots.add(randomSlot);
        }

        List<TeleportArea> synthetic = new ArrayList<>();
        int areaNumber = existingAreas.size() + 1;
        for (String cuboid : cuboids) {
            int slot = findNextFreeSlot(menuSize, usedSlots);
            if (slot < 0) {
                break;
            }

            synthetic.add(new TeleportArea(
                    "bound-" + areaNumber,
                    type,
                    slot,
                    material,
                    defaultDisplayName(type, areaNumber),
                    lore,
                    cuboid,
                    200,
                    null,
                    null
            ));
            usedSlots.add(slot);
            areaNumber++;
        }

        return synthetic;
    }

    private int findNextFreeSlot(int menuSize, Set<Integer> usedSlots) {
        for (int slot = 0; slot < menuSize; slot++) {
            if (!usedSlots.contains(slot)) {
                return slot;
            }
        }
        return -1;
    }

    private String defaultDisplayName(AreaType type, int areaNumber) {
        return type == AreaType.SPAWN
                ? "&#00A4FCspawn #" + areaNumber
                : "&#A303F9afk #" + areaNumber;
    }

    private List<String> defaultLore(AreaType type) {
        return type == AreaType.SPAWN
                ? List.of("&8{players}/200", "&7ᴄʟɪᴄᴋ ᴛᴏ ɢᴏᴛ ᴛᴏ ᴛʜɪѕ", "&7ѕᴘᴀᴡɴ ᴀʀᴇᴀ.")
                : List.of("&8{players}/200", "&7ᴄʟɪᴄᴋ ᴛᴏ ɢᴏᴛ ᴛᴏ ᴛʜɪѕ", "&7ᴀꜰᴋ ᴢᴏɴᴇ ᴀʀᴇᴀ.");
    }

    private int normalizeSize(int size) {
        int normalized = Math.max(9, ((size + 8) / 9) * 9);
        return Math.min(54, normalized);
    }

    private void warn(String message) {
        plugin.getLogger().warning("[SpawnManager] " + message);
    }
}
