package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AntiEspManager {

    private final UltimateDonutSmp plugin;
    private boolean enabled;
    private int revealRadius;
    private int ownerRevealRadius;
    private boolean requireLineOfSight;
    private int trackingRadius;
    private String bypassPermission;
    private Material overworldCamouflage;
    private Material netherCamouflage;
    private Material endCamouflage;
    private final Map<java.util.UUID, Set<Long>> revealedSpawners = new HashMap<>();

    public AntiEspManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        var config = plugin.getConfigManager().getSpawners();
        enabled = config.getBoolean("ANTI_ESP.ENABLED", true);
        revealRadius = Math.max(1, config.getInt("ANTI_ESP.REVEAL_RADIUS", 7));
        ownerRevealRadius = Math.max(revealRadius, config.getInt("ANTI_ESP.OWNER_SEE_RADIUS", revealRadius));
        requireLineOfSight = config.getBoolean("ANTI_ESP.REQUIRE_LINE_OF_SIGHT", true);
        trackingRadius = Math.max(revealRadius + 8, config.getInt("ANTI_ESP.TRACKING_RADIUS", 128));
        bypassPermission = config.getString("ANTI_ESP.STAFF_BYPASS_PERMISSION", "ULTIMATEDONUTSMP.ADMIN.SPAWNER.SEEALL");
        overworldCamouflage = Material.matchMaterial(config.getString("ANTI_ESP.CAMOUFLAGE.OVERWORLD", "DEEPSLATE"));
        netherCamouflage = Material.matchMaterial(config.getString("ANTI_ESP.CAMOUFLAGE.NETHER", "NETHERRACK"));
        endCamouflage = Material.matchMaterial(config.getString("ANTI_ESP.CAMOUFLAGE.THE_END", "END_STONE"));
    }

    public void updatePlayer(Player player) {
        if (player == null || !player.isOnline() || !enabled || plugin.getSpawnerManager() == null) {
            return;
        }

        var allSpawners = plugin.getSpawnerManager().getSpawnersInWorld(player.getWorld().getName());
        if (allSpawners.isEmpty()) {
            clearPlayer(player.getUniqueId());
            return;
        }

        Set<Long> currentlyRevealed = revealedSpawners.computeIfAbsent(player.getUniqueId(), ignored -> new HashSet<>());
        Set<Long> newRevealed = new HashSet<>();
        double trackingRadiusSquared = trackingRadius * (double) trackingRadius;

        for (SpawnerInstance instance : allSpawners) {
            Location center = plugin.getSpawnerManager().getSpawnerCenter(instance);
            if (center.getWorld() == null) {
                continue;
            }
            if (player.getLocation().distanceSquared(center) > trackingRadiusSquared) {
                continue;
            }

            if (shouldReveal(player, instance, center)) {
                revealActual(player, instance);
                newRevealed.add(instance.getId());
                continue;
            }

            conceal(player, instance);
        }

        for (Long previous : new ArrayList<>(currentlyRevealed)) {
            if (!newRevealed.contains(previous)) {
                SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(previous);
                if (instance != null) {
                    conceal(player, instance);
                }
            }
        }

        currentlyRevealed.clear();
        currentlyRevealed.addAll(newRevealed);
    }

    public void refreshAllPlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    public void refreshNearby(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        double radiusSquared = trackingRadius * (double) trackingRadius;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= radiusSquared) {
                updatePlayer(player);
            }
        }
    }

    public void clearPlayer(java.util.UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        revealedSpawners.remove(playerUuid);
    }

    public void shutdown() {
        if (plugin.getSpawnerManager() == null) {
            revealedSpawners.clear();
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            for (SpawnerInstance instance : plugin.getSpawnerManager().getSpawnersInWorld(player.getWorld().getName())) {
                revealActual(player, instance);
            }
        }
        revealedSpawners.clear();
    }

    private boolean shouldReveal(Player player, SpawnerInstance instance, Location center) {
        if (PermissionUtils.has(player, bypassPermission) || PermissionUtils.has(player, "ultimatedonutsmp.admin.spawner")) {
            return true;
        }

        boolean owner = player.getUniqueId().equals(instance.getOwnerUuid());
        int radius = owner ? ownerRevealRadius : revealRadius;
        if (player.getLocation().distanceSquared(center) > radius * (double) radius) {
            return false;
        }

        if (!requireLineOfSight) {
            return true;
        }

        return hasLineOfSight(player, center);
    }

    private boolean hasLineOfSight(Player player, Location target) {
        World world = player.getWorld();
        Location eye = player.getEyeLocation();

        double bx = target.getBlockX();
        double by = target.getBlockY();
        double bz = target.getBlockZ();

        double ex = eye.getX();
        double ey = eye.getY();
        double ez = eye.getZ();

        // Find a target point slightly inside the block face facing the player to prevent corner-clipping of adjacent blocks
        double tx = ex < bx ? bx + 0.1 : (ex > bx + 1.0 ? bx + 0.9 : bx + 0.5);
        double ty = ey < by ? by + 0.1 : (ey > by + 1.0 ? by + 0.9 : by + 0.5);
        double tz = ez < bz ? bz + 0.1 : (ez > bz + 1.0 ? bz + 0.9 : bz + 0.5);

        Location traceTarget = new Location(world, tx, ty, tz);
        Vector direction = traceTarget.toVector().subtract(eye.toVector());
        double distance = direction.length();
        if (distance <= 0D) {
            return true;
        }

        RayTraceResult rayTrace = world.rayTraceBlocks(
                eye,
                direction.normalize(),
                distance,
                FluidCollisionMode.NEVER,
                true
        );
        if (rayTrace == null || rayTrace.getHitBlock() == null) {
            return true;
        }

        Block hitBlock = rayTrace.getHitBlock();
        return hitBlock.getX() == target.getBlockX()
                && hitBlock.getY() == target.getBlockY()
                && hitBlock.getZ() == target.getBlockZ();
    }

    private void revealActual(Player player, SpawnerInstance instance) {
        if (plugin.getSpawnerManager() == null) {
            return;
        }

        plugin.getSpawnerManager().sendSpawnerVisual(player, instance);
    }

    private void conceal(Player player, SpawnerInstance instance) {
        World world = player.getWorld();
        if (!world.getName().equalsIgnoreCase(instance.getWorld())) {
            return;
        }

        Material camouflage = resolveCamouflageMaterial(world);
        player.sendBlockChange(
                new Location(world, instance.getX(), instance.getY(), instance.getZ()),
                camouflage.createBlockData()
        );
    }

    private Material resolveCamouflageMaterial(World world) {
        if (world == null) {
            return Material.DEEPSLATE;
        }

        return switch (world.getEnvironment()) {
            case NETHER -> fallback(netherCamouflage, Material.NETHERRACK);
            case THE_END -> fallback(endCamouflage, Material.END_STONE);
            default -> fallback(overworldCamouflage, Material.DEEPSLATE);
        };
    }

    private Material fallback(Material material, Material fallback) {
        return material == null ? fallback : material;
    }
}
