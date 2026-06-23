package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AnvilModerationManager {

    private final UltimateDonutSmp plugin;
    private List<String> bannedWords = new ArrayList<>();
    private List<String> punishments = new ArrayList<>();

    public AnvilModerationManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getConfigManager().reloadAnvilModeration();
        FileConfiguration config = plugin.getConfigManager().getAnvilModeration();
        if (config != null) {
            this.bannedWords = config.getStringList("banned-words");
            this.punishments = config.getStringList("punishments");
        }
    }

    public boolean addBannedWord(String word) {
        if (word == null || word.isBlank()) {
            return false;
        }
        String normalizedWord = word.trim();
        FileConfiguration config = plugin.getConfigManager().getAnvilModeration();
        if (config == null) {
            return false;
        }

        List<String> list = config.getStringList("banned-words");
        boolean exists = list.stream().anyMatch(w -> w.equalsIgnoreCase(normalizedWord));
        if (exists) {
            return false;
        }

        list.add(normalizedWord);
        config.set("banned-words", list);
        plugin.getConfigManager().saveAnvilModeration();

        this.bannedWords = list;
        return true;
    }

    public String findInappropriateWord(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String stripped = ColorUtils.strip(text);
        String lowerText = stripped.toLowerCase(Locale.ROOT);
        for (String word : bannedWords) {
            if (lowerText.contains(word.toLowerCase(Locale.ROOT))) {
                return word;
            }
        }
        return null;
    }

    public void punish(Player player, String matchedWord, String fullStrippedInput) {
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid;
        FileConfiguration config = plugin.getConfigManager().getAnvilModeration();
        if (config == null) {
            return;
        }

        int offenseCount = config.getInt(path + ".PUNISHMENTS", 0) + 1;
        long timestamp = System.currentTimeMillis();

        config.set(path + ".PUNISHMENTS", offenseCount);
        config.set(path + ".LAST-OFFENSE", timestamp);
        config.set(path + ".LAST-INPUT", fullStrippedInput);
        plugin.getConfigManager().saveAnvilModeration();

        executePunishment(player, offenseCount);
    }

    private void executePunishment(Player player, int offenseCount) {
        if (punishments.isEmpty()) {
            plugin.getLogger().warning("No punishments configured in anvil-moderation.yml!");
            return;
        }

        int index = Math.min(offenseCount, punishments.size()) - 1;
        if (index < 0) {
            index = 0;
        }

        String template = punishments.get(index);
        String resolvedCommand = template.replace("%player%", player.getName());

        plugin.getSpigotScheduler().runEntity(player, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolvedCommand);
        });
    }

    public List<String> getBannedWords() {
        return new ArrayList<>(bannedWords);
    }

    public List<String> getPunishments() {
        return new ArrayList<>(punishments);
    }
}
