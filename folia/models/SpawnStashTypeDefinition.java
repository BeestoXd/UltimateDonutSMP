package com.bx.ultimateDonutSmp.models;

import java.util.List;

public record SpawnStashTypeDefinition(
        String key,
        String displayName,
        long ttlSeconds,
        double alertRadius,
        SpawnStashOffset pasteOffset,
        List<SpawnStashBlockDefinition> blocks
) {
}
