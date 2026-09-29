package com.bx.ultimateDonutSmp.utils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Locale;

public class SoundUtils {

    /**
     * Parses a sound key (e.g. "minecraft:ui.button.click" or "UI_BUTTON_CLICK") to a Bukkit Sound enum
     * if one matches, avoiding unregistered direct holder wrapping on modern server platforms.
     */
    public static Sound parseSound(String key) {
        if (key == null || key.isBlank()) return null;
        String trimmed = key.trim();
        if (trimmed.indexOf(':') >= 0) {
            try {
                NamespacedKey nsk = NamespacedKey.fromString(trimmed);
                if (nsk != null) {
                    Sound s = Registry.SOUNDS.get(nsk);
                    if (s != null) return s;
                }
            } catch (Throwable ignored) {}

            String path = trimmed.substring(trimmed.indexOf(':') + 1).replace('.', '_').toUpperCase(Locale.US);
            try {
                return Sound.valueOf(path);
            } catch (Throwable ignored) {}
        } else {
            try {
                NamespacedKey nsk = NamespacedKey.minecraft(trimmed.replace('_', '.').toLowerCase(Locale.US));
                Sound s = Registry.SOUNDS.get(nsk);
                if (s != null) return s;
            } catch (Throwable ignored) {}

            String formatted = trimmed.replace('.', '_').toUpperCase(Locale.US);
            try {
                return Sound.valueOf(formatted);
            } catch (Throwable ignored) {}
        }
        return null;
    }

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
            Sound sound = parseSound(key);
            if (sound != null) {
                player.playSound(player.getLocation(), sound, volume, pitch);
                return;
            }

            if (key.indexOf(':') >= 0) {
                player.playSound(player.getLocation(), key, volume, pitch);
                return;
            }

            player.playSound(player.getLocation(), Sound.valueOf(key.toUpperCase(Locale.US)), volume, pitch);
        } catch (Exception ignored) {}
    }

    public static void play(
            UltimateDonutSmp plugin,
            Player player,
            String soundConfig,
            PlayerSettingUtils.SoundChannel channel
    ) {
        if (plugin == null || !PlayerSettingUtils.soundEnabled(plugin, player, channel)) {
            return;
        }
        play(player, soundConfig);
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
            Sound sound = parseSound(key);
            if (sound != null) {
                world.playSound(location, sound, SoundCategory.BLOCKS, volume, pitch);
                return;
            }

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
