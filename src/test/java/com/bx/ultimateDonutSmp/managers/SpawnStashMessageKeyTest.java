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

class SpawnStashMessageKeyTest {

    private static final Pattern MESSAGE_KEY =
            Pattern.compile("(?:publicMessage|message)\\(\\s*\"([A-Za-z0-9-]+)\"");

    private static final List<Path> SOURCES = List.of(
            Path.of("src/main/java/com/bx/ultimateDonutSmp/listeners/SpawnStashListener.java"),
            Path.of("src/main/java/com/bx/ultimateDonutSmp/commands/SpawnStashCommand.java"),
            Path.of("src/main/java/com/bx/ultimateDonutSmp/managers/SpawnStashManager.java")
    );

    @Test
    void everySpawnStashMessageKeyTheCodeReadsIsShipped() throws IOException {
        ConfigurationSection messages = YamlConfiguration
                .loadConfiguration(new File("src/main/resources/spawn-stash.yml"))
                .getConfigurationSection("MESSAGES");
        assertNotNull(messages, "MESSAGES");

        Set<String> missing = new TreeSet<>();
        for (Path source : SOURCES) {
            Matcher matcher = MESSAGE_KEY.matcher(read(source));
            while (matcher.find()) {
                String key = matcher.group(1);
                if (!messages.isString(key)) {
                    missing.add(key);
                }
            }
        }

        // SpawnStashManager.message does config().getString("MESSAGES." + path, fallback), and Bukkit
        // paths are case sensitive, so a key spelled differently from the shipped one silently falls
        // back to the hardcoded English string and the configured (and translated) value is dead.
        assertTrue(missing.isEmpty(), "spawn-stash.yml MESSAGES is missing keys the code reads: " + missing);
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
