package com.bx.ultimateDonutSmp.models;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public record SpawnStashInstance(
        long id,
        String typeKey,
        String displayName,
        UUID creatorUuid,
        String creatorName,
        String worldName,
        int originX,
        int originY,
        int originZ,
        BlockFace facing,
        long createdAtMillis,
        long expiresAtMillis,
        double alertRadius,
        List<Location> blockLocations,
        Set<String> blockKeys,
        List<BlockState> snapshots,
        Map<String, Long> alertCooldowns
) {
    public SpawnStashInstance(
            long id,
            String typeKey,
            String displayName,
            UUID creatorUuid,
            String creatorName,
            String worldName,
            int originX,
            int originY,
            int originZ,
            BlockFace facing,
            long createdAtMillis,
            long expiresAtMillis,
            double alertRadius,
            List<Location> blockLocations,
            Set<String> blockKeys,
            List<BlockState> snapshots
    ) {
        this(
                id,
                typeKey,
                displayName,
                creatorUuid,
                creatorName,
                worldName,
                originX,
                originY,
                originZ,
                facing,
                createdAtMillis,
                expiresAtMillis,
                alertRadius,
                List.copyOf(blockLocations),
                Set.copyOf(blockKeys),
                List.copyOf(snapshots),
                new ConcurrentHashMap<>()
        );
    }

    public Location originLocation() {
        return new Location(blockLocations.isEmpty() ? null : blockLocations.get(0).getWorld(),
                originX + 0.5D, originY + 0.5D, originZ + 0.5D);
    }
}
