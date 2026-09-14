package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnerMessageKeyTest {

    private static final Pattern GET_MESSAGE = Pattern.compile("getMessage(?:List)?\\(\"([A-Z0-9-]+)\"");

    @Test
    void everyMessageKeyTheCodeReadsIsShipped() throws IOException {
        ConfigurationSection messages = YamlConfiguration
                .loadConfiguration(new File("src/main/resources/spawners.yml"))
                .getConfigurationSection("MESSAGES");
        assertNotNull(messages, "MESSAGES section in spawners.yml must exist");

        Set<String> missing = new TreeSet<>();
        for (String key : readMessageKeys()) {
            if (!messages.contains(key)) {
                missing.add(key);
            }
        }

        assertTrue(missing.isEmpty(), "spawners.yml MESSAGES is missing keys the code reads: " + missing);
    }

    @Test
    void essentialKeysAreConfigured() {
        ConfigurationSection messages = YamlConfiguration
                .loadConfiguration(new File("src/main/resources/spawners.yml"))
                .getConfigurationSection("MESSAGES");
        assertNotNull(messages, "MESSAGES section in spawners.yml must exist");

        List<String> essential = List.of(
                "PLACED", "STACKED", "PICKED-UP", "REMOVED", "REMOVED-ADMIN",
                "GAVE-SPAWNER", "RECEIVED-SPAWNER", "COLLECTED", "COLLECTED-ALL",
                "DROPPED-PAGE", "SOLD", "COLLECTED-XP", "RELOADED", "SPLIT-SUCCESS",
                "SILK-TOUCH-REQUIRED", "NO-ACCESS", "DISABLED", "USAGE", "INFO-HEADER"
        );

        for (String key : essential) {
            assertTrue(messages.contains(key), "Essential spawner message key missing: " + key);
        }
    }

    private Set<String> readMessageKeys() throws IOException {
        Set<String> keys = new TreeSet<>();
        List<Path> files = List.of(
                Path.of("src/main/java/com/bx/ultimateDonutSmp/managers/SpawnerManager.java"),
                Path.of("src/main/java/com/bx/ultimateDonutSmp/commands/SpawnerCommand.java"),
                Path.of("src/main/java/com/bx/ultimateDonutSmp/listeners/SpawnerBlockListener.java"),
                Path.of("src/main/java/com/bx/ultimateDonutSmp/listeners/SpawnerInteractListener.java"),
                Path.of("src/main/java/com/bx/ultimateDonutSmp/menus/SpawnerStorageMenu.java"),
                Path.of("src/main/java/com/bx/ultimateDonutSmp/menus/SpawnerFilterMenu.java"),
                Path.of("src/main/java/com/bx/ultimateDonutSmp/menus/SpawnerPanelMenu.java")
        );

        for (Path file : files) {
            if (Files.exists(file)) {
                collect(file, keys);
            }
        }
        return keys;
    }

    private void collect(Path path, Set<String> keys) {
        Matcher matcher = GET_MESSAGE.matcher(read(path));
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
