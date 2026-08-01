package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OffenseManager {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*([s|m|h|d|w|mo|y])", Pattern.CASE_INSENSITIVE);

    public record OffenseRule(
            String key,
            String name,
            PunishmentType type,
            List<String> durations
    ) {
        public String getDurationForTier(int tierIndex) {
            if (durations == null || durations.isEmpty()) {
                return "perm";
            }
            int index = Math.min(tierIndex, durations.size() - 1);
            return durations.get(index);
        }
    }

    private final UltimateDonutSmp plugin;
    private final Map<String, OffenseRule> offenses = new HashMap<>();

    public OffenseManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public synchronized void reload() {
        offenses.clear();
        loadConfig();
    }

    private void loadConfig() {
        File file = new File(plugin.getDataFolder(), "offenses.yml");
        if (!file.exists()) {
            plugin.saveResource("offenses.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("offenses");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection offenseSec = section.getConfigurationSection(key);
            if (offenseSec == null) continue;

            String name = offenseSec.getString("name", key);
            String typeStr = offenseSec.getString("type", "BAN").toUpperCase(Locale.ROOT);
            PunishmentType type;
            try {
                type = PunishmentType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                type = PunishmentType.BAN;
            }

            List<String> durations = offenseSec.getStringList("durations");
            if (durations.isEmpty() && offenseSec.isString("durations")) {
                durations = Collections.singletonList(offenseSec.getString("durations"));
            }

            OffenseRule rule = new OffenseRule(key.toLowerCase(Locale.ROOT), name, type, durations);
            offenses.put(key.toLowerCase(Locale.ROOT), rule);
        }
    }

    public Optional<OffenseRule> getOffenseRule(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(offenses.get(key.toLowerCase(Locale.ROOT)));
    }

    public Set<String> getOffenseKeys() {
        return Collections.unmodifiableSet(offenses.keySet());
    }

    public Map<String, OffenseRule> getOffenses() {
        return Collections.unmodifiableMap(offenses);
    }

    /**
     * Parses a duration string (e.g. "30s", "15m", "2h", "5d", "1w", "1mo", "1y", "perm") into milliseconds.
     * Returns null if duration is permanent or 0 for warn/instant.
     */
    public static Long parseDurationToMillis(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String cleaned = input.trim().toLowerCase(Locale.ROOT);
        if (cleaned.equals("perm") || cleaned.equals("permanent") || cleaned.equals("ban appeal")) {
            return null;
        }

        if (cleaned.equals("0") || cleaned.equals("0s")) {
            return 0L;
        }

        Matcher matcher = DURATION_PATTERN.matcher(cleaned);
        long totalMillis = 0L;
        boolean matchedAny = false;

        while (matcher.find()) {
            matchedAny = true;
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase(Locale.ROOT);

            switch (unit) {
                case "s" -> totalMillis += amount * 1000L;
                case "m" -> totalMillis += amount * 60 * 1000L;
                case "h" -> totalMillis += amount * 60 * 60 * 1000L;
                case "d" -> totalMillis += amount * 24 * 60 * 60 * 1000L;
                case "w" -> totalMillis += amount * 7 * 24 * 60 * 60 * 1000L;
                case "mo" -> totalMillis += amount * 30 * 24 * 60 * 60 * 1000L;
                case "y" -> totalMillis += amount * 365 * 24 * 60 * 60 * 1000L;
            }
        }

        if (!matchedAny) {
            // Try raw numeric days if no unit match
            try {
                long days = Long.parseLong(cleaned);
                return days * 24 * 60 * 60 * 1000L;
            } catch (NumberFormatException ignored) {
                return null; // default to permanent if unrecognized
            }
        }

        return totalMillis;
    }
}
