package com.bx.ultimateDonutSmp.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Locale;

public class SoundUtils {

    /**
     * Play a sound from config format: "namespace:sound.key|volume|pitch"
     * e.g. "minecraft:ui.button.click|1.0|1.0"
     */
    public static void play(Player player, String soundConfig) {
        if (player == null || soundConfig == null || soundConfig.isBlank()) return;
        String[] parts = soundConfig.split("\\|");
        String key = parts[0].trim();
        float volume = parseFloat(parts.length > 1 ? parts[1] : "1.0", 1.0f);
        float pitch  = parseFloat(parts.length > 2 ? parts[2] : "1.0", 1.0f);
        try {
            if (key.indexOf(':') >= 0) {
                player.playSound(player.getLocation(), key, volume, pitch);
                return;
            }

            player.playSound(player.getLocation(), Sound.valueOf(key.toUpperCase(Locale.US)), volume, pitch);
        } catch (Exception ignored) {}
    }

    public static void play(Location location, String soundConfig) {
        if (location == null || soundConfig == null || soundConfig.isBlank()) return;
        World world = location.getWorld();
        if (world == null) return;

        String[] parts = soundConfig.split("\\|");
        String key = parts[0].trim();
        float volume = parseFloat(parts.length > 1 ? parts[1] : "1.0", 1.0f);
        float pitch  = parseFloat(parts.length > 2 ? parts[2] : "1.0", 1.0f);
        try {
            if (key.indexOf(':') >= 0) {
                world.playSound(location, key, SoundCategory.BLOCKS, volume, pitch);
                return;
            }

            world.playSound(location, Sound.valueOf(key.toUpperCase(Locale.US)), SoundCategory.BLOCKS, volume, pitch);
        } catch (Exception ignored) {}
    }

    /** Play a sound from a ConfigurationSection by key path */
    public static void play(Player player, ConfigurationSection section, String path) {
        if (section == null) return;
        String val = section.getString(path);
        if (val != null) play(player, val);
    }

    private static float parseFloat(String s, float def) {
        try { return Float.parseFloat(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }
}
