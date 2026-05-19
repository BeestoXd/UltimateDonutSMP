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
            Location locationOverride
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
    private Location spawnLocation;
    private Location afkLocation;
    private List<TeleportArea> configuredSpawnAreas = List.of();
    private List<TeleportArea> configuredAfkAreas = List.of();

    public SpawnManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String spawnStr = config.getString("LOCATIONS.SPAWN-LOCATION", "");
        String afkStr = config.getString("LOCATIONS.AFK-LOCATION", "");
        spawnLocation = LocationUtils.parse(spawnStr);
        afkLocation = LocationUtils.parse(afkStr);
        configuredSpawnAreas = loadAreas("SPAWN-MENU", AreaType.SPAWN);
        configuredAfkAreas = loadAreas("AFK-MENU", AreaType.AFK);
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
                materialized.add(configured.get(index));
                usedTemplateIndexes.add(index);
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
        if (area.locationOverride() != null) {
            Location overrideDestination = makeSafeDestination(area.locationOverride());
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

        Location legacyLocation = type == AreaType.SPAWN ? makeSafeDestination(spawnLocation) : makeSafeDestination(afkLocation);
        if (legacyLocation != null) {
            return legacyLocation;
        }

        return resolveBoundCuboidDestination(type);
    }

    public Location getSpawnLocation() {
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
                warn(menuPath + ".AREAS." + key + " is missing CUBOID. It can still be used only as a generic visual template.");
            }

            Location locationOverride = parseAreaLocation(
                    type,
                    section.get("LOCATION"),
                    menuPath + ".AREAS." + key + ".LOCATION"
            );

            loaded.add(new TeleportArea(
                    key,
                    type,
                    slot,
                    ItemUtils.parseMaterial(section.getString("MATERIAL", "ITEM_FRAME")),
                    section.getString("DISPLAY-NAME", type == AreaType.SPAWN ? "&bѕᴘᴀᴡɴ" : "&#A303F9ᴀꜰᴋ"),
                    section.getStringList("LORE"),
                    cuboidName,
                    Math.max(1, section.getInt("CAPACITY", 200)),
                    locationOverride
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
            warn(path + " has an invalid location override '" + serialized + "'. Falling back to cuboid teleport.");
        }
        return parsed;
    }

    private SetupLocationResult setSetupLocation(AreaType type, Location location, String configPath) {
        String serialized = LocationUtils.serialize(location);
        FileConfiguration config = plugin.getConfigManager().getConfig();
        FileConfiguration menus = plugin.getConfigManager().getMenus();
        SetupAreaTarget target = findNextSetupAreaTarget(type);
        if (target == null) {
            return SetupLocationResult.failure("No free " + getLocationLabel(type) + " menu slot is available.");
        }

        config.set(configPath, serialized);
        menus.set(target.path() + ".ʟᴏᴄᴀᴛɪᴏɴ", serialized);

        try {
            plugin.saveConfig();
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("[SpawnManager] ꜰᴀɪʟᴇᴅ ᴛᴏ ѕᴀᴠᴇ ᴄᴏɴꜰɪɢ.ʏᴍʟ: " + exception.getMessage());
            return SetupLocationResult.failure("ꜰᴀɪʟᴇᴅ ᴛᴏ ѕᴀᴠᴇ ᴄᴏɴꜰɪɢ.ʏᴍʟ.");
        }

        boolean savedMenus = plugin.getConfigManager().saveMenus();
        if (!savedMenus) {
            return SetupLocationResult.failure("ꜰᴀɪʟᴇᴅ ᴛᴏ ѕᴀᴠᴇ ᴍᴇɴᴜѕ.ʏᴍʟ.");
        }

        load();
        return SetupLocationResult.success(target.areaId(), target.slot());
    }

    private SetupAreaTarget findNextSetupAreaTarget(AreaType type) {
        FileConfiguration menus = plugin.getConfigManager().getMenus();
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
            return new SetupAreaTarget(menuPath + ".AREAS." + selectedKey, selectedKey, selectedSlot);
        }

        int nextSlot = findNextFreeSlot(menuSize, usedSlots);
        if (nextSlot < 0) {
            return null;
        }

        String nextKey = nextAreaKey(areasSection);
        int areaNumber = parsePositiveInt(nextKey, areasSection.getKeys(false).size() + 1);
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
            return AreaDeleteResult.failure("Area is not available.");
        }

        if (!isStoredMenuArea(area)) {
            return AreaDeleteResult.failure("This area is not stored in menus.yml.");
        }

        FileConfiguration menus = plugin.getConfigManager().getMenus();
        String menuPath = area.type() == AreaType.SPAWN ? "SPAWN-MENU" : "AFK-MENU";
        String areaPath = menuPath + ".AREAS." + area.id();
        menus.set(areaPath, null);
        if (!plugin.getConfigManager().saveMenus()) {
            return AreaDeleteResult.failure("Failed to save menus.yml.");
        }

        load();
        return AreaDeleteResult.success("Removed " + getLocationLabel(area.type()) + " area "
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

    private boolean hasSetupDestination(AreaType type, String areaPath, ConfigurationSection areaSection) {
        Location location = parseAreaLocation(type, areaSection.get("LOCATION"), areaPath + ".LOCATION");
        if (location != null && makeSafeDestination(location) != null) {
            return true;
        }

        String cuboidName = trimToNull(areaSection.getString("CUBOID", ""));
        return cuboidName != null && plugin.getCuboidManager().exists(cuboidName);
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

    private String nextAreaKey(ConfigurationSection areasSection) {
        int candidate = 1;
        while (areasSection.contains(String.valueOf(candidate))) {
            candidate++;
        }
        return String.valueOf(candidate);
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

        if (template.locationOverride() != null) {
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
                template.locationOverride()
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
                ? "&#00A4FCѕᴘᴀᴡɴ #" + areaNumber
                : "&#A303F9ᴀꜰᴋ #" + areaNumber;
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
