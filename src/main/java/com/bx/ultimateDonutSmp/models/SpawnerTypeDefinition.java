package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;

public record SpawnerTypeDefinition(
        String key,
        String displayName,
        EntityType entityType,
        Material iconMaterial,
        long baseItemsPerCycle,
        double xpPerCycle,
        String headTexture,
        List<DropDefinition> drops
) {

    public SpawnerTypeDefinition(
            String key,
            String displayName,
            EntityType entityType,
            Material iconMaterial,
            long baseItemsPerCycle,
            List<DropDefinition> drops
    ) {
        this(key, displayName, entityType, iconMaterial, baseItemsPerCycle, 3.7, null, drops);
    }

    public SpawnerTypeDefinition(
            String key,
            String displayName,
            EntityType entityType,
            Material iconMaterial,
            long baseItemsPerCycle,
            double xpPerCycle,
            List<DropDefinition> drops
    ) {
        this(key, displayName, entityType, iconMaterial, baseItemsPerCycle, xpPerCycle, null, drops);
    }

    public SpawnerTypeDefinition {
        displayName = displayName == null || displayName.isBlank() ? key : displayName;
        iconMaterial = iconMaterial == null ? Material.SPAWNER : iconMaterial;
        baseItemsPerCycle = Math.max(1L, baseItemsPerCycle);
        xpPerCycle = Math.max(0.0, xpPerCycle);
        headTexture = headTexture == null || headTexture.isBlank() ? null : headTexture.trim();
        drops = List.copyOf(drops == null ? List.of() : drops);
    }

    public record DropDefinition(
            String key,
            Material material,
            long min,
            long max,
            double chance
    ) {
        public DropDefinition {
            key = key == null ? "" : key.trim();
            material = material == null ? Material.STONE : material;
            min = Math.max(0L, min);
            max = Math.max(min, max);
            chance = Math.max(0D, Math.min(1D, chance));
        }

        public double averageDropAmount() {
            return (min + max) / 2.0D;
        }
    }
}
