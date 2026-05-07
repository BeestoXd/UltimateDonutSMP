package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CuboidManager {

    public record Cuboid(String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        public boolean contains(Location loc) {
            if (loc.getWorld() == null || !loc.getWorld().getName().equalsIgnoreCase(world)) {
                return false;
            }

            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            return x >= Math.min(x1, x2) && x <= Math.max(x1, x2)
                    && y >= Math.min(y1, y2) && y <= Math.max(y1, y2)
                    && z >= Math.min(z1, z2) && z <= Math.max(z1, z2);
        }
    }

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

    public Location getCuboidTeleportLocation(String name) {
        Cuboid cuboid = getCuboid(name);
        if (cuboid == null) {
            return null;
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
        int maxSafeY = Math.min(maxY, world.getMaxHeight() - 3);

        for (int groundY = maxSafeY; groundY >= minY; groundY--) {
            if (isSafeStandingSpot(world, centerX, groundY, centerZ)) {
                return new Location(world, centerX + 0.5, groundY + 1.0, centerZ + 0.5);
            }
        }

        int highestY = world.getHighestBlockYAt(centerX, centerZ);
        if (highestY > world.getMinHeight()) {
            return new Location(world, centerX + 0.5, highestY + 1.0, centerZ + 0.5);
        }

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
