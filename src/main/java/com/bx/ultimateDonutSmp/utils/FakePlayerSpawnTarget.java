package com.bx.ultimateDonutSmp.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Picks where {@code /fakeplayer} should appear: either the staff member's feet, or the point their
 * crosshair is aimed at.
 */
public final class FakePlayerSpawnTarget {

    public static final double FACE_PUSH = 0.51D;
    public static final double TOP_SURFACE_LIFT = 0.01D;
    public static final double DEFAULT_MISS_DISTANCE = 8.0D;

    private FakePlayerSpawnTarget() {
    }

    public static Location resolve(
            Location standing,
            Location eye,
            Vector lookDirection,
            Vector hitPosition,
            Vector hitNormal,
            boolean spawnAtLookTarget,
            double missDistance
    ) {
        if (standing == null) {
            return null;
        }
        if (!spawnAtLookTarget) {
            return standing.clone();
        }

        if (hitPosition != null) {
            Location spawn = new Location(
                    standing.getWorld(),
                    hitPosition.getX(),
                    hitPosition.getY(),
                    hitPosition.getZ(),
                    standing.getYaw(),
                    0F
            );
            pushOutOfFace(spawn, hitPosition, hitNormal);
            return spawn;
        }

        Vector direction = lookDirection == null ? standing.getDirection() : lookDirection.clone();
        if (direction.lengthSquared() < 1.0E-8D) {
            return standing.clone();
        }
        direction.normalize();

        Location origin = eye == null ? standing.clone().add(0D, 1.62D, 0D) : eye.clone();
        Location spawn = origin.add(direction.multiply(Math.max(1.0D, missDistance)));
        spawn.setYaw(standing.getYaw());
        spawn.setPitch(0F);
        return spawn;
    }

    private static void pushOutOfFace(Location spawn, Vector hitPosition, Vector hitNormal) {
        if (hitNormal == null || hitNormal.lengthSquared() < 1.0E-8D) {
            spawn.setY(hitPosition.getY() + TOP_SURFACE_LIFT);
            return;
        }

        Vector offset = hitNormal.clone();
        offset.normalize();
        if (offset.getY() > 0.5D) {
            spawn.setY(hitPosition.getY() + TOP_SURFACE_LIFT);
            return;
        }
        spawn.add(offset.multiply(FACE_PUSH));
    }
}
