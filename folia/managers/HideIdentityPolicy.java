package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.HideMode;
import com.bx.ultimateDonutSmp.models.HideState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.random.RandomGenerator;

final class HideIdentityPolicy {

    static final String OBFUSCATED_DISGUISE_SKIN_KEY_PREFIX = "obfuscated_";

    private static final String DEFAULT_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";

    private HideIdentityPolicy() {
    }

    static String validCharacters(String configured) {
        String source = configured == null ? "" : configured;
        StringBuilder valid = new StringBuilder();
        for (int index = 0; index < source.length(); index++) {
            char character = source.charAt(index);
            if ((Character.isLetterOrDigit(character) || character == '_')
                    && valid.indexOf(String.valueOf(character)) < 0) {
                valid.append(character);
            }
        }
        return valid.isEmpty() ? DEFAULT_CHARACTERS : valid.toString();
    }

    static String generateAlias(RandomGenerator random, String characters, int length, int maxLength) {
        String alphabet = validCharacters(characters);
        int safeMax = Math.max(1, Math.min(16, maxLength));
        int safeLength = Math.max(3, Math.min(safeMax, length));
        StringBuilder alias = new StringBuilder(safeLength);
        for (int index = 0; index < safeLength; index++) {
            alias.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return alias.toString();
    }

    static boolean isValidProfileName(String name, int maxLength) {
        int safeMax = Math.max(1, Math.min(16, maxLength));
        return name != null
                && !name.isBlank()
                && name.length() <= safeMax
                && name.matches("[A-Za-z0-9_]+");
    }

    static long cooldownRemaining(long changedAt, long now, long cooldownSeconds) {
        if (changedAt <= 0L || cooldownSeconds <= 0L) {
            return 0L;
        }
        long remaining = changedAt + (cooldownSeconds * 1000L) - now;
        return remaining <= 0L ? 0L : Math.max(1L, (remaining + 999L) / 1000L);
    }

    static boolean matchesState(HideState state, String input, boolean bypass) {
        if (state == null || input == null || input.isBlank()) {
            return false;
        }
        return state.alias().equalsIgnoreCase(input)
                || (bypass && state.realNameSnapshot().equalsIgnoreCase(input));
    }

    static String formatPublicName(HideState state, String fallback, boolean obfuscated) {
        if (state == null) {
            return fallback == null ? "" : fallback;
        }
        if (obfuscated && usesObfuscatedText(state)) {
            return "&k" + state.alias() + "&r";
        }
        return state.alias();
    }

    static boolean usesObfuscatedText(HideState state) {
        return state != null && (state.mode() == HideMode.SCRAMBLE
                || state.mode() == HideMode.DISGUISE
                && state.skinKey().startsWith(OBFUSCATED_DISGUISE_SKIN_KEY_PREFIX));
    }

    static List<String> validate(FileConfiguration config) {
        List<String> problems = new ArrayList<>();
        int maxLength = config.getInt("MAX-NAME-LENGTH", 16);
        if (maxLength < 1 || maxLength > 16) {
            problems.add("MAX-NAME-LENGTH must be between 1 and 16.");
        }
        int safeMax = Math.max(1, Math.min(16, maxLength));
        int scrambleLength = config.getInt("SCRAMBLE.LENGTH", 10);
        if (scrambleLength < 3 || scrambleLength > safeMax) {
            problems.add("SCRAMBLE.LENGTH must be between 3 and MAX-NAME-LENGTH.");
        }
        String characters = config.getString("SCRAMBLE.CHARACTERS", "");
        if (!validCharacters(characters).equals(characters)) {
            problems.add("SCRAMBLE.CHARACTERS contains invalid or duplicate characters.");
        }

        ConfigurationSection aliases = config.getConfigurationSection("ALIASES");
        Set<String> normalizedAliases = new HashSet<>();
        if (aliases == null || aliases.getKeys(false).isEmpty()) {
            problems.add("At least one ALIASES entry is required.");
        } else {
            for (String key : aliases.getKeys(false)) {
                String name = aliases.getString(key + ".NAME", key);
                if (!isValidProfileName(name, safeMax)) {
                    problems.add("ALIASES." + key + ".NAME is not a valid profile name.");
                } else if (!normalizedAliases.add(name.toLowerCase(Locale.ROOT))) {
                    problems.add("ALIASES contains duplicate name " + name + ".");
                }
            }
        }

        ConfigurationSection skins = config.getConfigurationSection("SKINS");
        if (skins == null || skins.getKeys(false).isEmpty()) {
            problems.add("At least one SKINS entry is required.");
        } else {
            for (String key : skins.getKeys(false)) {
                String username = skins.getString(key + ".USERNAME", key);
                if (!isValidProfileName(username, 16)) {
                    problems.add("SKINS." + key + ".USERNAME is not a valid account name.");
                }
            }
        }
        if (aliases != null && skins != null) {
            for (String key : aliases.getKeys(false)) {
                String skinKey = aliases.getString(key + ".SKIN", key);
                if (skinKey == null || !skins.contains(skinKey + ".USERNAME")) {
                    problems.add("ALIASES." + key + ".SKIN must reference a configured SKINS key.");
                }
            }
        }
        return problems;
    }
}
