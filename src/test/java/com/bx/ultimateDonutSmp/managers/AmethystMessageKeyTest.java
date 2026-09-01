package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmethystMessageKeyTest {

    private static final Pattern GET_MESSAGE = Pattern.compile("getMessage\\(\"([A-Z0-9-]+)\"");

    @Test
    void everyMessageKeyTheCodeReadsIsShipped() throws IOException {
        ConfigurationSection messages = YamlConfiguration
                .loadConfiguration(new java.io.File("src/main/resources/amethyst-tools.yml"))
                .getConfigurationSection("AMETHYST-MESSAGES");
        assertNotNull(messages, "AMETHYST-MESSAGES");

        Set<String> missing = new TreeSet<>();
        for (String key : readMessageKeys()) {
            if (!messages.isString(key)) {
                missing.add(key);
            }
        }

        // getMessage falls back to the key itself, so a key the config never ships is
        // sent to the player as raw text instead of a message.
        assertTrue(missing.isEmpty(), "AMETHYST-MESSAGES is missing keys the code reads: " + missing);
    }

    private Set<String> readMessageKeys() throws IOException {
        Set<String> keys = new TreeSet<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/com/bx/ultimateDonutSmp/amethyst"))) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> collect(path, keys));
        }
        // The command reads the same AMETHYST-MESSAGES section from outside the package.
        collect(Path.of("src/main/java/com/bx/ultimateDonutSmp/commands/AmethystToolCommand.java"), keys);
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
