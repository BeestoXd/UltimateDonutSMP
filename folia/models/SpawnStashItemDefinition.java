package com.bx.ultimateDonutSmp.models;

import org.bukkit.Material;

import java.util.List;

public record SpawnStashItemDefinition(
        int slot,
        Material material,
        int amount,
        String displayName,
        List<String> lore
) {
}
