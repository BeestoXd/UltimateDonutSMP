package com.bx.ultimateDonutSmp.managers;

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
        bypassPermission = config.getString("ANTI_ESP.STAFF_BYPASS_PERMISSION", "ultimatedonutsmp.admin.spawner.seeall");
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
        if (player.hasPermission(bypassPermission) || player.hasPermission("ultimatedonutsmp.admin.spawner")) {
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
        Vector direction = target.toVector().subtract(player.getEyeLocation().toVector());
        double distance = direction.length();
        if (distance <= 0D) {
            return true;
        }

        RayTraceResult rayTrace = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
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
        World world = player.getWorld();
        if (!world.getName().equalsIgnoreCase(instance.getWorld())) {
            return;
        }

        Block block = world.getBlockAt(instance.getX(), instance.getY(), instance.getZ());
        player.sendBlockChange(block.getLocation(), block.getBlockData());
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
