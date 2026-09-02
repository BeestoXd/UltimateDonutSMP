package com.bx.ultimateDonutSmp.utils;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Decides who is close enough to hear the two boss sounds Minecraft plays to the whole server.
 *
 * <p>A wither finishing its charge-up and an ender dragon starting to die are sent as global level
 * events, so every player online hears them wherever they happen to be standing. The only vanilla
 * lever is the {@code globalSoundEvents} game rule, which drops both all the way down to the
 * ordinary 64 block level event radius. This sits in between: the sound still carries a long way,
 * but it stops at the configured radius.
 */
public final class BossSoundRangePolicy {

    /** Level event Minecraft fires once a wither finishes charging up. */
    public static final int WITHER_SPAWN_EFFECT_ID = 1023;

    /** Level event Minecraft fires as an ender dragon starts its death animation. */
    public static final int ENDER_DRAGON_DEATH_EFFECT_ID = 1028;

    private static final int DEFAULT_RADIUS = 1600;

    /**
     * A point the sound is measured between. {@code world} is null when the world could not be
     * worked out, which leaves the distance as the only thing checked.
     */
    public record Position(String world, double x, double y, double z) {
    }

    private final boolean enabled;
    private final int radius;
    private final boolean witherSpawn;
    private final boolean enderDragonDeath;

    public BossSoundRangePolicy(boolean enabled, int radius, boolean witherSpawn, boolean enderDragonDeath) {
        this.enabled = enabled;
        this.radius = radius;
        this.witherSpawn = witherSpawn;
        this.enderDragonDeath = enderDragonDeath;
    }

    public static BossSoundRangePolicy fromConfig(ConfigurationSection config) {
        if (config == null) {
            return new BossSoundRangePolicy(true, DEFAULT_RADIUS, true, true);
        }
        return new BossSoundRangePolicy(
                config.getBoolean("BOSS-SOUNDS.ENABLED", true),
                config.getInt("BOSS-SOUNDS.RADIUS", DEFAULT_RADIUS),
                config.getBoolean("BOSS-SOUNDS.WITHER-SPAWN", true),
                config.getBoolean("BOSS-SOUNDS.ENDER-DRAGON-DEATH", true)
        );
    }

    public int getRadius() {
        return radius;
    }

    /** Whether anything is being limited at all, so callers can skip the packet listener entirely. */
    public boolean isActive() {
        return enabled && radius > 0 && (witherSpawn || enderDragonDeath);
    }

    /** Whether this level event is one of the two the radius applies to. */
    public boolean filters(int effectId) {
        if (!enabled || radius <= 0) {
            return false;
        }
        return switch (effectId) {
            case WITHER_SPAWN_EFFECT_ID -> witherSpawn;
            case ENDER_DRAGON_DEATH_EFFECT_ID -> enderDragonDeath;
            default -> false;
        };
    }

    /**
     * Whether {@code listener} is near enough to the boss to be left hearing it. A world missing on
     * either side means the worlds cannot be compared, so only the distance decides.
     */
    public boolean canHear(Position origin, Position listener) {
        if (origin == null || listener == null) {
            return true;
        }
        if (origin.world() != null && listener.world() != null && !origin.world().equals(listener.world())) {
            return false;
        }
        double dx = origin.x() - listener.x();
        double dy = origin.y() - listener.y();
        double dz = origin.z() - listener.z();
        double radiusSquared = (double) radius * radius;
        return dx * dx + dy * dy + dz * dz <= radiusSquared;
    }
}
