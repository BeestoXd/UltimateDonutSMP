package com.bx.ultimateDonutSmp.models;

import java.util.Locale;
import java.util.Objects;

public record DuelMapSelection(Type type, String value) {

    public enum Type {
        STATIC_ARENA,
        RANDOM_STATIC,
        BIOME,
        RANDOM_BIOME
    }

    public DuelMapSelection {
        type = type == null ? Type.RANDOM_STATIC : type;
        value = value == null || value.isBlank() ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static DuelMapSelection staticArena(String arenaId) {
        return new DuelMapSelection(Type.STATIC_ARENA, arenaId);
    }

    public static DuelMapSelection randomStatic() {
        return new DuelMapSelection(Type.RANDOM_STATIC, "");
    }

    public static DuelMapSelection biome(String biomeKey) {
        return new DuelMapSelection(Type.BIOME, biomeKey);
    }

    public static DuelMapSelection randomBiome() {
        return new DuelMapSelection(Type.RANDOM_BIOME, "");
    }

    public static DuelMapSelection parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return randomStatic();
        }

        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("random") || normalized.equals("random_static") || normalized.equals("random-static")
                || normalized.equals("arena:random") || normalized.equals("static:random")) {
            return randomStatic();
        }
        if (normalized.equals("random_biome") || normalized.equals("random-biome") || normalized.equals("biome:random")) {
            return randomBiome();
        }
        if (normalized.startsWith("biome:")) {
            return biome(normalized.substring("biome:".length()));
        }
        if (normalized.startsWith("arena:")) {
            return staticArena(normalized.substring("arena:".length()));
        }
        if (normalized.startsWith("static:")) {
            return staticArena(normalized.substring("static:".length()));
        }

        return staticArena(normalized);
    }

    public boolean usesGeneratedWorld() {
        return type == Type.BIOME || type == Type.RANDOM_BIOME;
    }

    public boolean usesStaticArena() {
        return !usesGeneratedWorld();
    }

    public String serialize() {
        return switch (type) {
            case STATIC_ARENA -> "arena:" + value;
            case RANDOM_STATIC -> "random_static";
            case BIOME -> "biome:" + value;
            case RANDOM_BIOME -> "random_biome";
        };
    }

    public String matchSourceName() {
        return type.name();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DuelMapSelection selection)) {
            return false;
        }
        return type == selection.type && Objects.equals(value, selection.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
