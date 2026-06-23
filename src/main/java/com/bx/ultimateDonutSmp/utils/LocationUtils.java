package com.bx.ultimateDonutSmp.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class LocationUtils {

    private LocationUtils() {
    }

    public static Location parse(String serialized) {
        if (serialized == null || serialized.isBlank()) {
            return null;
        }

        String[] parts = serialized.split(",");
        if (parts.length < 4) {
            return null;
        }

        try {
            World world = findWorld(parts[0].trim());
            if (world == null) {
                return null;
            }

            double x = Double.parseDouble(parts[1].trim());
            double y = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[3].trim());
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4].trim()) : 0F;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5].trim()) : 0F;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static String serialize(Location location) {
        if (location == null || location.getWorld() == null) {
            return "";
        }

        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + ","
                + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }

    private static World findWorld(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        World exact = Bukkit.getWorld(name);
        if (exact != null) {
            return exact;
        }

        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equalsIgnoreCase(name.trim())) {
                return world;
            }
        }
        return null;
    }
}
