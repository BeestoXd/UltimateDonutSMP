package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CuboidManager {

    public record Cuboid(String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        public boolean contains(Location loc) {
            if (loc.getWorld() == null) {
                return false;
            }
            return isInside(this, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    private static final String SPAWN_SECTION = "CUBOID-SPAWNS";

    private final UltimateDonutSmp plugin;
    private final Map<String, Cuboid> cuboids = new HashMap<>();
    private final Map<UUID, Location[]> selections = new HashMap<>();

    public CuboidManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        Map<String, DatabaseManager.CuboidData> raw = plugin.getDatabaseManager().loadCuboids();
        cuboids.clear();
        raw.forEach((name, data) -> cuboids.put(name.toLowerCase(), new Cuboid(
                data.world(),
                data.x1(),
                data.y1(),
                data.z1(),
                data.x2(),
                data.y2(),
                data.z2()
        )));
    }

    public void addCuboid(String name, Location pos1, Location pos2) {
        if (pos1.getWorld() == null || pos2.getWorld() == null) {
            return;
        }
        if (!pos1.getWorld().getName().equalsIgnoreCase(pos2.getWorld().getName())) {
            return;
        }

        Cuboid cuboid = new Cuboid(
                pos1.getWorld().getName(),
                pos1.getBlockX(),
                pos1.getBlockY(),
                pos1.getBlockZ(),
                pos2.getBlockX(),
                pos2.getBlockY(),
                pos2.getBlockZ()
        );
        cuboids.put(name.toLowerCase(), cuboid);
        plugin.getDatabaseManager().saveCuboid(
                name.toLowerCase(),
                pos1.getWorld().getName(),
                pos1.getBlockX(),
                pos1.getBlockY(),
                pos1.getBlockZ(),
                pos2.getBlockX(),
                pos2.getBlockY(),
                pos2.getBlockZ()
        );
    }

    public void removeCuboid(String name) {
        cuboids.remove(name.toLowerCase());
        plugin.getDatabaseManager().deleteCuboid(name.toLowerCase());
    }

    public Cuboid getCuboid(String name) {
        if (name == null) {
            return null;
        }
        return cuboids.get(name.toLowerCase());
    }

    public boolean exists(String name) {
        return getCuboid(name) != null;
    }

    public boolean isInCuboid(Player player, String name) {
        Cuboid cuboid = getCuboid(name);
        return cuboid != null && cuboid.contains(player.getLocation());
    }

    public boolean isInAnyCuboid(Player player, String... names) {
        for (String name : names) {
            if (isInCuboid(player, name)) {
                return true;
            }
        }
        return false;
    }

    public Location getCuboidCenter(String name) {
        Cuboid cuboid = getCuboid(name);
        if (cuboid == null) {
            return null;
        }

        World world = Bukkit.getWorld(cuboid.world());
        if (world == null) {
            return null;
        }

        double centerX = (Math.min(cuboid.x1(), cuboid.x2()) + Math.max(cuboid.x1(), cuboid.x2()) + 1) / 2.0;
        double centerY = (Math.min(cuboid.y1(), cuboid.y2()) + Math.max(cuboid.y1(), cuboid.y2())) / 2.0;
        double centerZ = (Math.min(cuboid.z1(), cuboid.z2()) + Math.max(cuboid.z1(), cuboid.z2()) + 1) / 2.0;
        return new Location(world, centerX, centerY, centerZ);
    }

    /**
     * The spawn point an admin saved with /cuboid setspawn, or null when there is none. A point that
     * no longer sits inside the region counts as none, so redefining a cuboid can never leave players
     * landing outside it.
     */
    public Location getCuboidSpawn(String name) {
        Cuboid cuboid = getCuboid(name);
        if (cuboid == null) {
            return null;
        }

        String serialized = plugin.getConfigManager().getConfig().getString(spawnPath(name));
        if (!spawnFitsCuboid(cuboid, serialized)) {
            return null;
        }
        return LocationUtils.parse(serialized);
    }

    public void setCuboidSpawn(String name, Location location) {
        plugin.getConfigManager().getConfig().set(spawnPath(name), LocationUtils.serialize(location));
    }

    public boolean clearCuboidSpawn(String name) {
        String path = spawnPath(name);
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (config.getString(path) == null) {
            return false;
        }
        config.set(path, null);
        return true;
    }

    /**
     * Drops a saved spawn point that the region no longer covers. Recreating a cuboid under a name
     * that already had one is the way that happens.
     */
    public boolean dropSpawnOutsideBounds(String name) {
        String serialized = plugin.getConfigManager().getConfig().getString(spawnPath(name));
        if (serialized == null || serialized.isBlank() || spawnFitsCuboid(getCuboid(name), serialized)) {
            return false;
        }
        return clearCuboidSpawn(name);
    }

    static String spawnPath(String name) {
        return SPAWN_SECTION + "." + (name == null ? "" : name.toLowerCase());
    }

    /**
     * Bounds check on a serialized location, so a stored spawn can be judged without the world being
     * loaded. Only the feet block has to be inside, matching how the automatic teleport spot is picked.
     */
    static boolean spawnFitsCuboid(Cuboid cuboid, String serialized) {
        if (cuboid == null || serialized == null || serialized.isBlank()) {
            return false;
        }

        String[] parts = serialized.split(",");
        if (parts.length < 4) {
            return false;
        }

        try {
            return isInside(
                    cuboid,
                    parts[0].trim(),
                    (int) Math.floor(Double.parseDouble(parts[1].trim())),
                    (int) Math.floor(Double.parseDouble(parts[2].trim())),
                    (int) Math.floor(Double.parseDouble(parts[3].trim()))
            );
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    static boolean isInside(Cuboid cuboid, String worldName, int x, int y, int z) {
        if (cuboid == null || worldName == null || !worldName.equalsIgnoreCase(cuboid.world())) {
            return false;
        }

        return x >= Math.min(cuboid.x1(), cuboid.x2()) && x <= Math.max(cuboid.x1(), cuboid.x2())
                && y >= Math.min(cuboid.y1(), cuboid.y2()) && y <= Math.max(cuboid.y1(), cuboid.y2())
                && z >= Math.min(cuboid.z1(), cuboid.z2()) && z <= Math.max(cuboid.z1(), cuboid.z2());
    }

    public Location getCuboidTeleportLocation(String name) {
        Cuboid cuboid = getCuboid(name);
        if (cuboid == null) {
            return null;
        }

        Location savedSpawn = getCuboidSpawn(name);
        if (savedSpawn != null) {
            return savedSpawn;
        }

        World world = Bukkit.getWorld(cuboid.world());
        if (world == null) {
            return null;
        }

        int minX = Math.min(cuboid.x1(), cuboid.x2());
        int maxX = Math.max(cuboid.x1(), cuboid.x2());
        int minY = Math.min(cuboid.y1(), cuboid.y2());
        int maxY = Math.max(cuboid.y1(), cuboid.y2());
        int minZ = Math.min(cuboid.z1(), cuboid.z2());
        int maxZ = Math.max(cuboid.z1(), cuboid.z2());

        int centerX = (minX + maxX) / 2;
        int centerZ = (minZ + maxZ) / 2;

        // Ensure feet (groundY + 1) remain inside the cuboid bounds (<= maxY)
        int maxSafeY = (maxY > minY) ? Math.min(maxY - 1, world.getMaxHeight() - 3) : Math.min(maxY, world.getMaxHeight() - 3);

        // 1. Try finding safe standing spot at center X/Z inside the cuboid
        for (int groundY = maxSafeY; groundY >= minY; groundY--) {
            if (isSafeStandingSpot(world, centerX, groundY, centerZ)) {
                return new Location(world, centerX + 0.5, groundY + 1.0, centerZ + 0.5);
            }
        }

        // 2. Search other X/Z coordinates inside the cuboid if center has no safe spot
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x == centerX && z == centerZ) {
                    continue;
                }
                for (int groundY = maxSafeY; groundY >= minY; groundY--) {
                    if (isSafeStandingSpot(world, x, groundY, z)) {
                        return new Location(world, x + 0.5, groundY + 1.0, z + 0.5);
                    }
                }
            }
        }

        // 3. Fallback to center of cuboid (inside) rather than outside on top of the roof
        return getCuboidCenter(name);
    }

    public int countPlayersInCuboid(String name) {
        Cuboid cuboid = getCuboid(name);
        if (cuboid == null) {
            return 0;
        }

        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (cuboid.contains(player.getLocation())) {
                count++;
            }
        }
        return count;
    }

    public Set<String> getCuboidNames() {
        return cuboids.keySet();
    }

    public void setPos1(UUID uuid, Location loc) {
        Location[] selection = selections.computeIfAbsent(uuid, ignored -> new Location[2]);
        selection[0] = loc;
    }

    public void setPos2(UUID uuid, Location loc) {
        Location[] selection = selections.computeIfAbsent(uuid, ignored -> new Location[2]);
        selection[1] = loc;
    }

    public Location[] getSelection(UUID uuid) {
        return selections.get(uuid);
    }

    public boolean hasFullSelection(UUID uuid) {
        Location[] selection = selections.get(uuid);
        return selection != null && selection[0] != null && selection[1] != null;
    }

    public void clearSelection(UUID uuid) {
        selections.remove(uuid);
    }

    private boolean isSafeStandingSpot(World world, int x, int groundY, int z) {
        if (groundY < world.getMinHeight() || groundY + 2 >= world.getMaxHeight()) {
            return false;
        }

        Block ground = world.getBlockAt(x, groundY, z);
        Block feet = world.getBlockAt(x, groundY + 1, z);
        Block head = world.getBlockAt(x, groundY + 2, z);

        return ground.getType().isSolid()
                && feet.isPassable()
                && head.isPassable()
                && !isHazardous(ground.getType())
                && !isHazardous(feet.getType())
                && !isHazardous(head.getType());
    }

    private boolean isHazardous(Material material) {
        String name = material.name();
        return name.contains("LAVA")
                || name.contains("WATER")
                || name.contains("FIRE")
                || name.contains("CACTUS")
                || name.contains("MAGMA")
                || name.contains("CAMPFIRE")
                || name.contains("POWDER_SNOW")
                || name.contains("SWEET_BERRY_BUSH")
                || name.contains("VOID");
    }
}
