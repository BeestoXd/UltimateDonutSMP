package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;

public record SpawnStashBlockDefinition(
        SpawnStashOffset offset,
        Material material,
        String blockData,
        String spawnerTypeKey,
        long spawnerStackAmount,
        SpawnerInstance.AccessMode spawnerAccessMode,
        EntityType spawnerEntity,
        List<String> signLines,
        List<SpawnStashItemDefinition> containerItems
) {
}
