package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaderboardConfigurationTest {

    @Test
    void bountiesIsAResolvableLeaderboardType() {
        // Regression guard for #124: %economylb_bounties_<n>_display% rendered raw because
        // the documented "bounties" type was never backed by an enum constant.
        LeaderboardManager.LeaderboardType bounties = LeaderboardManager.LeaderboardType.BOUNTIES;
        assertEquals("bounties", bounties.getConfigKey());
    }

    @Test
    void everyLeaderboardTypeHasADisplayName() throws Exception {
        ConfigurationSection typeNames = load("menus.yml")
                .getConfigurationSection("LEADERBOARDS-MENU.TYPE-NAMES");
        assertNotNull(typeNames);

        for (LeaderboardManager.LeaderboardType type : LeaderboardManager.LeaderboardType.values()) {
            String name = typeNames.getString(type.getConfigKey());
            assertNotNull(name, "missing TYPE-NAMES entry for " + type.getConfigKey());
            assertTrue(!name.isBlank(), "blank TYPE-NAMES entry for " + type.getConfigKey());
        }
    }

    @Test
    void everyLeaderboardTypeHasAButtonInAUniqueValidSlot() throws Exception {
        YamlConfiguration menus = load("menus.yml");
        int size = menus.getInt("LEADERBOARDS-MENU.SIZE");
        ConfigurationSection buttons = menus.getConfigurationSection("LEADERBOARDS-MENU.BUTTONS");
        assertNotNull(buttons);

        Map<String, String> typesByKey = new HashMap<>();
        Set<Integer> usedSlots = new HashSet<>();
        for (String key : buttons.getKeys(false)) {
            int slot = buttons.getInt(key + ".SLOT", -1);
            assertTrue(slot >= 0 && slot < size, key + " has invalid slot " + slot);
            assertTrue(usedSlots.add(slot), key + " duplicates slot " + slot);

            String type = buttons.getString(key + ".TYPE");
            assertNotNull(type, key + " has no TYPE");
            typesByKey.put(normalize(type), key);
        }

        for (LeaderboardManager.LeaderboardType type : LeaderboardManager.LeaderboardType.values()) {
            assertTrue(typesByKey.containsKey(normalize(type.getConfigKey())),
                    "no LEADERBOARDS-MENU button for " + type.getConfigKey());
        }
    }

    @Test
    void placeholderDocsListEveryLeaderboardType() throws Exception {
        String docs = Files.readString(
                Path.of("docs/wiki/Placeholders-and-Integrations.md"), StandardCharsets.UTF_8);

        String supportedTypes = docs.lines()
                .filter(line -> line.startsWith("*Supported Types*"))
                .findFirst()
                .orElse(null);
        assertNotNull(supportedTypes, "docs no longer declare the supported leaderboard types");

        for (LeaderboardManager.LeaderboardType type : LeaderboardManager.LeaderboardType.values()) {
            assertTrue(supportedTypes.contains("`" + type.getConfigKey() + "`"),
                    "placeholder docs omit leaderboard type " + type.getConfigKey());
        }
    }

    private static String normalize(String input) {
        return input.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.US);
    }

    private static YamlConfiguration load(String fileName) throws Exception {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(Path.of("src/main/resources", fileName).toFile());
        return configuration;
    }
}
