package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigManagerTest {

    @Test
    void mergeBundledDefaultsOnlyAddsMissingScalar() throws Exception {
        List<String> currentLines = lines(
                "# admin header",
                "SETTINGS:",
                "  ENABLED: false # admin comment",
                "  AFTER: 2",
                "CUSTOM:",
                "  VALUE: 9"
        );
        List<String> bundledLines = lines(
                "SETTINGS:",
                "  ENABLED: true # bundled comment",
                "  # Missing setting comment.",
                "  MISSING: 5",
                "  AFTER: 2"
        );

        int changes = mergeBundledDefaults("unit.yml", currentLines, bundledLines);

        assertEquals(1, changes);
        assertEquals("# admin header", currentLines.get(0));
        assertTrue(currentLines.contains("  ENABLED: false # admin comment"));
        assertTrue(currentLines.contains("CUSTOM:"));
        assertTrue(currentLines.contains("  VALUE: 9"));
        assertTrue(indexOfLine(currentLines, "  ENABLED: false # admin comment")
                < indexOfLine(currentLines, "  MISSING: 5"));
        assertTrue(indexOfLine(currentLines, "  MISSING: 5")
                < indexOfLine(currentLines, "  AFTER: 2"));
        assertEquals(
                "  # Missing setting comment.",
                currentLines.get(indexOfLine(currentLines, "  MISSING: 5") - 1)
        );
    }

    @Test
    void mergeBundledDefaultsAddsMissingTopLevelSectionWithoutReorderingExistingSections() throws Exception {
        List<String> currentLines = lines(
                "THIRD:",
                "  ENABLED: true",
                "",
                "FIRST:",
                "  ENABLED: false"
        );
        List<String> bundledLines = lines(
                "FIRST:",
                "  ENABLED: true",
                "",
                "# UDS setup: Second section.",
                "SECOND:",
                "  ENABLED: false",
                "",
                "THIRD:",
                "  ENABLED: true"
        );

        int changes = mergeBundledDefaults("unit.yml", currentLines, bundledLines);

        assertEquals(2, changes);
        assertTrue(indexOfLine(currentLines, "THIRD:") < indexOfLine(currentLines, "FIRST:"));
        assertTrue(currentLines.contains("SECOND:"));
        assertEquals(
                "# UDS setup: Second section.",
                currentLines.get(indexOfLine(currentLines, "SECOND:") - 1)
        );
    }

    @Test
    void mergeBundledDefaultsNeverUpdatesExistingValue() throws Exception {
        List<String> currentLines = lines(
                "SETTING: 1 # admin kept the old default",
                "CUSTOM: true"
        );
        List<String> bundledLines = lines("SETTING: 2 # new bundled default");

        int changes = mergeBundledDefaults("unit.yml", currentLines, bundledLines);

        assertEquals(0, changes);
        assertEquals(List.of(
                "SETTING: 1 # admin kept the old default",
                "CUSTOM: true"
        ), currentLines);
    }

    @Test
    void mergeBundledDefaultsPreservesUnknownAndRemovedPaths() throws Exception {
        List<String> currentLines = lines(
                "SETTINGS:",
                "  ENABLED: true",
                "  OLD-OPTION: true",
                "  CUSTOM: true"
        );
        List<String> bundledLines = lines(
                "SETTINGS:",
                "  ENABLED: true",
                "  NEW-OPTION: false"
        );

        int changes = mergeBundledDefaults("unit.yml", currentLines, bundledLines);

        assertEquals(1, changes);
        assertTrue(currentLines.contains("  OLD-OPTION: true"));
        assertTrue(currentLines.contains("  CUSTOM: true"));
        assertTrue(currentLines.contains("  NEW-OPTION: false"));
    }

    @Test
    void mergeBundledDefaultsSkipsRuntimeManagedTrees() throws Exception {
        List<String> crateLines = lines(
                "CRATES:",
                "  custom:",
                "    DISPLAY-NAME: \"Custom\"",
                "OTHER: true"
        );
        List<String> crateDefaults = lines(
                "CRATES:",
                "  starter:",
                "    DISPLAY-NAME: \"Starter\"",
                "OTHER: true"
        );

        int crateChanges = mergeBundledDefaults("crates.yml", crateLines, crateDefaults);

        assertEquals(0, crateChanges);
        assertFalse(crateLines.contains("  starter:"));
        assertTrue(crateLines.contains("  custom:"));

        List<String> arenaLines = lines(
                "ARENA_SETTINGS:",
                "  arena1:",
                "    ENABLED: true",
                "SETTINGS:",
                "  ENABLED: true"
        );
        List<String> arenaDefaults = lines(
                "ARENA_SETTINGS:",
                "  example:",
                "    ENABLED: false",
                "SETTINGS:",
                "  ENABLED: true"
        );

        int arenaChanges = mergeBundledDefaults("duels.yml", arenaLines, arenaDefaults);

        assertEquals(0, arenaChanges);
        assertFalse(arenaLines.contains("  example:"));
        assertTrue(arenaLines.contains("  arena1:"));
    }

    @Test
    void mergeBundledDefaultsLeavesCompleteFileByteEquivalent() throws Exception {
        List<String> currentLines = lines(
                "# admin header",
                "SECOND: 2",
                "FIRST: 1 # admin comment"
        );
        List<String> bundledLines = lines(
                "FIRST: 9 # bundled comment",
                "SECOND: 8"
        );
        List<String> before = new ArrayList<>(currentLines);

        int changes = mergeBundledDefaults("unit.yml", currentLines, bundledLines);

        assertEquals(0, changes);
        assertEquals(before, currentLines);
    }

    @Test
    void atomicTextWritePreservesExistingLineEndingAndTrailingNewline() throws Exception {
        Path file = Files.createTempFile("uds-config-sync-", ".yml");
        try {
            Files.writeString(file, "FIRST: 1\r\nSECOND: 2\r\n", StandardCharsets.UTF_8);
            ConfigManager manager = new ConfigManager(null);
            Method read = ConfigManager.class.getDeclaredMethod("readTextFile", File.class);
            read.setAccessible(true);
            Object content = read.invoke(manager, file.toFile());

            Method lines = content.getClass().getDeclaredMethod("lines");
            lines.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> mutableLines = (List<String>) lines.invoke(content);
            mutableLines.add("THIRD: 3");

            Method write = ConfigManager.class.getDeclaredMethod(
                    "writeTextFileAtomically",
                    File.class,
                    content.getClass()
            );
            write.setAccessible(true);
            write.invoke(manager, file.toFile(), content);

            assertEquals(
                    "FIRST: 1\r\nSECOND: 2\r\nTHIRD: 3\r\n",
                    Files.readString(file, StandardCharsets.UTF_8)
            );
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void mergeBundledDefaultsWithSameLevelLists() throws Exception {
        List<String> currentLines = lines(
                "KEY-ALL:",
                "  ENABLED: true",
                "  EVERY: 60",
                "  COMMANDS:",
                "  - \"\"",
                "  TYPE: \"RANDOM\""
        );
        List<String> bundledLines = lines(
                "KEY-ALL:",
                "  ENABLED: true",
                "  EVERY: 60",
                "  COMMANDS:",
                "  - \"\"",
                "  RANDOM-COMMANDS: false",
                "  TYPE: \"RANDOM\""
        );

        int changes = mergeBundledDefaults("unit.yml", currentLines, bundledLines);

        assertEquals(1, changes);

        int commandsIndex = indexOfLine(currentLines, "  COMMANDS:");
        int listItemIndex = indexOfLine(currentLines, "  - \"\"");
        int randomCmdsIndex = indexOfLine(currentLines, "  RANDOM-COMMANDS: false");
        int typeIndex = indexOfLine(currentLines, "  TYPE: \"RANDOM\"");

        assertTrue(commandsIndex < listItemIndex);
        assertTrue(listItemIndex < randomCmdsIndex);
        assertTrue(randomCmdsIndex < typeIndex);

        YamlConfiguration parsed = yaml(currentLines);
        assertFalse(parsed.getBoolean("KEY-ALL.RANDOM-COMMANDS"));
        assertEquals("RANDOM", parsed.getString("KEY-ALL.TYPE"));
    }

    @Test
    void generatedLanguageSyncAddsNewKeysAndPreservesAdminTranslations() throws Exception {
        List<String> currentLines = lines(
                "META:",
                "  LOCALE: id_ID",
                "MENUS:",
                "  COMMON:",
                "    CLOSE:",
                "      NAME: '&cTutup Kustom'",
                "CUSTOM:",
                "  SERVER-TEXT: '&dTetap dipertahankan'"
        );
        List<String> generatedDefaults = lines(
                "META:",
                "  LOCALE: id_ID",
                "MENUS:",
                "  COMMON:",
                "    CLOSE:",
                "      NAME: '&cClose'",
                "      LORE:",
                "      - '&7Close this menu'",
                "CONFIG:",
                "  HIDE:",
                "    MESSAGES:",
                "      ENABLED: '&aHide enabled'"
        );

        int changes = mergeBundledDefaults(
                "languages/id_ID.yml",
                currentLines,
                generatedDefaults
        );
        YamlConfiguration merged = yaml(currentLines);

        assertTrue(changes > 0);
        assertEquals("&cTutup Kustom", merged.getString("MENUS.COMMON.CLOSE.NAME"));
        assertEquals(List.of("&7Close this menu"), merged.getStringList("MENUS.COMMON.CLOSE.LORE"));
        assertEquals("&aHide enabled", merged.getString("CONFIG.HIDE.MESSAGES.ENABLED"));
        assertEquals("&dTetap dipertahankan", merged.getString("CUSTOM.SERVER-TEXT"));
    }

    @Test
    void bundledYamlResourcesExceptPluginParse() throws Exception {
        Path resources = Path.of("src/main/resources");
        try (Stream<Path> paths = Files.list(resources)) {
            for (Path path : paths
                    .filter(ConfigManagerTest::isYamlResource)
                    .filter(candidate -> !candidate.getFileName().toString().equals("plugin.yml"))
                    .toList()) {
                YamlConfiguration configuration = new YamlConfiguration();
                configuration.options().parseComments(true);
                configuration.load(path.toFile());
            }
        }
    }

    private static int mergeBundledDefaults(
            String resourceName,
            List<String> currentLines,
            List<String> bundledLines
    ) throws Exception {
        Method method = ConfigManager.class.getDeclaredMethod(
                "mergeBundledDefaults",
                String.class,
                List.class,
                List.class,
                YamlConfiguration.class,
                YamlConfiguration.class
        );
        method.setAccessible(true);
        return (int) method.invoke(
                new ConfigManager(null),
                resourceName,
                currentLines,
                bundledLines,
                yaml(currentLines),
                yaml(bundledLines)
        );
    }

    private static List<String> lines(String... lines) {
        return new ArrayList<>(List.of(lines));
    }

    private static YamlConfiguration yaml(List<String> lines) throws Exception {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.loadFromString(String.join("\n", lines) + "\n");
        return configuration;
    }

    private static boolean isYamlResource(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".yml") || name.endsWith(".yaml");
    }

    private static int indexOfLine(List<String> lines, String line) {
        int index = lines.indexOf(line);
        assertTrue(index >= 0, () -> "Missing line: " + line);
        return index;
    }
}
