package com.bx.ultimateDonutSmp.commands;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamMessageKeyTest {

    private static final Pattern KEY = Pattern.compile("getMessage\\(\\s*\"(TEAM\\.[A-Z0-9_-]+)\"");

    private static final Path COMMAND =
            Path.of("src/main/java/com/bx/ultimateDonutSmp/commands/TeamCommand.java");

    /**
     * ConfigManager.getMessage(path) reads messages.yml for the legacy value and then asks
     * LanguageManager for "MESSAGES." + path, so a key shipped in either file resolves. One that is in
     * neither reaches the player as "&cmissing language key: MESSAGES.&lt;path&gt;".
     */
    @Test
    void everyTeamMessageKeyTheCommandReadsIsShipped() throws IOException {
        ConfigurationSection legacy = YamlConfiguration
                .loadConfiguration(new File("src/main/resources/messages.yml"));
        ConfigurationSection language = YamlConfiguration
                .loadConfiguration(new File("src/main/resources/languages/en_US.yml"))
                .getConfigurationSection("MESSAGES");

        Set<String> missing = new TreeSet<>();
        Matcher matcher = KEY.matcher(Files.readString(COMMAND));
        while (matcher.find()) {
            String key = matcher.group(1);
            boolean shipped = legacy.isString(key) || (language != null && language.isString(key));
            if (!shipped) {
                missing.add(key);
            }
        }

        assertTrue(missing.isEmpty(),
                "neither messages.yml nor languages/en_US.yml ships keys TeamCommand reads: " + missing);
    }
}
