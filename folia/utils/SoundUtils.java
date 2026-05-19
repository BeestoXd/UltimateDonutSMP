package com.bx.ultimateDonutSmp.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

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
            player.playSound(player.getLocation(), key, volume, pitch);
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
