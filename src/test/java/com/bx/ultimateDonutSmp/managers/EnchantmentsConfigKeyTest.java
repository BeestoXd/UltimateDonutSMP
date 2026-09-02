package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EnchantmentsManager used to look these values up as GUI.TITLE and MESSAGES.SELECT while
 * enchantments.yml shipped them as gui.title and messages.select. Bukkit config paths are case
 * sensitive, so every lookup missed and the hardcoded fallback was used instead, which made the
 * whole gui and messages part of the file look configurable while doing nothing. Most of it went
 * unnoticed because the shipped values matched the fallbacks anyway; only the menu title differed.
 *
 * <p>Rather than pin the ten paths by hand, this reads the lookups back out of the manager, so it
 * fails whichever side drifts: renaming a key in the file, or re-casing a path in the code.
 */
class EnchantmentsConfigKeyTest {

    private static final Pattern ROOT_LOOKUP = Pattern.compile(
            "config\\.get(?:String|Int|Boolean|Double|StringList)\\(\"([^\"]+)\""
    );

    private static YamlConfiguration enchantments() {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/enchantments.yml"));
    }

    private static List<String> lookupPaths() throws Exception {
        String source = Files.readString(
                new File("src/main/java/com/bx/ultimateDonutSmp/managers/EnchantmentsManager.java").toPath(),
                StandardCharsets.UTF_8
        );
        List<String> paths = new ArrayList<>();
        Matcher matcher = ROOT_LOOKUP.matcher(source);
        while (matcher.find()) {
            paths.add(matcher.group(1));
        }
        return paths;
    }

    @Test
    void everyPathTheManagerLooksUpIsShippedInTheFile() throws Exception {
        List<String> paths = lookupPaths();
        assertTrue(paths.size() >= 10, "expected to find the gui and message lookups, found " + paths);

        YamlConfiguration config = enchantments();
        for (String path : paths) {
            assertTrue(
                    config.contains(path),
                    "EnchantmentsManager reads " + path + " but enchantments.yml does not ship it, so the"
                            + " lookup silently falls back to a hardcoded default"
            );
        }
    }

    @Test
    void theUnreadButtonAndSoundSubtreesAreNoLongerShipped() {
        YamlConfiguration config = enchantments();
        for (String path : List.of("gui.buttons", "gui.sounds")) {
            assertFalse(
                    config.contains(path),
                    "nothing reads " + path + ", so shipping it advertises settings that do nothing"
            );
        }
    }

    @Test
    void theItemCategoriesTheMenuLoadsAreStillPresent() {
        YamlConfiguration config = enchantments();
        for (String category : List.of("helmet", "chestplate", "leggings", "boots", "elytra", "bow",
                "crossbow", "fishing_rod", "shovel", "pickaxe", "axe", "hoe", "shield", "sword")) {
            assertTrue(
                    config.isConfigurationSection(category),
                    category + " has no options, so that item would open an empty enchant menu"
            );
        }
    }
}
