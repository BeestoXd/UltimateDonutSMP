package com.bx.ultimateDonutSmp.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * plugin.yml declares folia-supported: true. On Folia the legacy BukkitScheduler is gone and
 * Bukkit.getScheduler().runTask(...) throws UnsupportedOperationException, so every scheduling
 * call has to go through SpigotScheduler, which picks the global / entity / region scheduler.
 */
class FoliaSchedulerUsageTest {

    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");
    private static final Path SCHEDULER_WRAPPER =
            SOURCE_ROOT.resolve(Path.of("com", "bx", "ultimateDonutSmp", "utils", "SpigotScheduler.java"));

    @Test
    void pluginDeclaresFoliaSupport() throws Exception {
        YamlConfiguration plugin = new YamlConfiguration();
        plugin.load(new File("src/main/resources/plugin.yml"));
        assertTrue(plugin.getBoolean("folia-supported"),
                "This test only matters while the plugin claims Folia support");
    }

    @Test
    void nothingOutsideSpigotSchedulerTouchesTheLegacyBukkitScheduler() throws Exception {
        List<String> offenders = new ArrayList<>();

        try (Stream<Path> sources = Files.walk(SOURCE_ROOT)) {
            for (Path source : sources.filter(Files::isRegularFile).toList()) {
                if (!source.toString().endsWith(".java") || source.equals(SCHEDULER_WRAPPER)) {
                    continue;
                }
                List<String> lines = Files.readAllLines(source, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).contains("Bukkit.getScheduler()")) {
                        offenders.add(SOURCE_ROOT.relativize(source) + ":" + (i + 1)
                                + " -> " + lines.get(i).trim());
                    }
                }
            }
        }

        assertEquals(List.of(), offenders,
                "These call sites throw UnsupportedOperationException on Folia; use SpigotScheduler instead");
    }
}
