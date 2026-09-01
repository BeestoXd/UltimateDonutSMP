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

        List<String> shopLines = lines(
                "CATEGORIES:",
                "  CUSTOM:",
                "    MATERIAL: DIAMOND",
                "    SLOT: 10",
                "CUSTOM-MENU:",
                "  TITLE: \"Custom\"",
                "SHOP-GUI:",
                "  SHOW-AUCTION-PRICE: true"
        );
        List<String> shopDefaults = lines(
                "CATEGORIES:",
                "  END:",
                "    MATERIAL: END_STONE",
                "END-MENU:",
                "  TITLE: \"End\"",
                "SHOP-GUI:",
                "  SHOW-AUCTION-PRICE: true"
        );

        int shopChanges = mergeBundledDefaults("shop.yml", shopLines, shopDefaults);

        assertEquals(0, shopChanges);
        assertFalse(shopLines.contains("  END:"));
        assertFalse(shopLines.contains("END-MENU:"));
        assertTrue(shopLines.contains("CUSTOM-MENU:"));

        List<String> billfordLines = lines(
                "BILLFORD:",
                "  1:",
                "    DISPLAY_NAME: \"Custom Billford\"",
                "    INPUTS:",
                "      1:",
                "        MATERIAL: DIAMOND_SHOVEL",
                "        QUANTITY: 1"
        );
        List<String> billfordDefaults = lines(
                "BILLFORD:",
                "  1:",
                "    DISPLAY_NAME: \"Custom Billford\"",
                "    INPUTS:",
                "      1:",
                "        MATERIAL: DIAMOND_SHOVEL",
                "        QUANTITY: 1",
                "      2:",
                "        MATERIAL: BLAZE_ROD",
                "        QUANTITY: 8"
        );

        int billfordChanges = mergeBundledDefaults("billford.yml", billfordLines, billfordDefaults);

        assertEquals(0, billfordChanges);
        assertFalse(billfordLines.contains("        MATERIAL: BLAZE_ROD"));
        assertTrue(billfordLines.contains("        MATERIAL: DIAMOND_SHOVEL"));
    }

    @Test
    void mergeBundledDefaultsKeepsRanksMenuButtonsAdminsRenamed() throws Exception {
        List<String> currentLines = lines(
                "RANKS-MENU:",
                "  TITLE: '&8Ranks'",
                "  SIZE: 27",
                "  BUTTONS:",
                "    OWNER:",
                "      MATERIAL: PLAYER_HEAD",
                "      SLOT: 11",
                "    MVP:",
                "      MATERIAL: PLAYER_HEAD",
                "      SLOT: 13"
        );
        List<String> bundledLines = lines(
                "RANKS-MENU:",
                "  TITLE: '&8Ranks'",
                "  SIZE: 27",
                "  BUTTONS:",
                "    DEFAULT:",
                "      MATERIAL: PLAYER_HEAD",
                "      SLOT: 11",
                "    DONUT_PLUS:",
                "      MATERIAL: PLAYER_HEAD",
                "      SLOT: 13"
        );

        int changes = mergeBundledDefaults("menus.yml", currentLines, bundledLines);

        assertEquals(0, changes);
        assertFalse(currentLines.contains("    DEFAULT:"),
                "a renamed bundled rank must not come back on the slot its replacement now uses");
        assertFalse(currentLines.contains("    DONUT_PLUS:"));
        assertTrue(currentLines.contains("    OWNER:"));
        assertTrue(currentLines.contains("    MVP:"));
    }

    @Test
    void mergeBundledDefaultsStillInstallsTheRanksMenuOnConfigsThatPredateIt() throws Exception {
        List<String> currentLines = lines(
                "GLOBAL:",
                "  ENABLED: true"
        );
        List<String> bundledLines = lines(
                "GLOBAL:",
                "  ENABLED: true",
                "RANKS-MENU:",
                "  TITLE: '&8Ranks'",
                "  BUTTONS:",
                "    DEFAULT:",
                "      SLOT: 11"
        );

        int changes = mergeBundledDefaults("menus.yml", currentLines, bundledLines);

        assertTrue(changes > 0);
        assertTrue(currentLines.contains("RANKS-MENU:"));
        assertTrue(currentLines.contains("    DEFAULT:"),
                "the first install of the ranks menu still needs its bundled buttons");
    }

    @Test
    void mergeBundledDefaultsKeepsRulesButtonsAdminsRenamed() throws Exception {
        List<String> currentLines = lines(
                "RULES-MENU:",
                "  TITLE: '&8Rules'",
                "  SIZE: 27",
                "  BUTTONS:",
                "    HOUSE_RULES:",
                "      MATERIAL: KNOWLEDGE_BOOK",
                "      SLOT: 12"
        );
        List<String> bundledLines = lines(
                "RULES-MENU:",
                "  TITLE: '&8Rules'",
                "  SIZE: 27",
                "  BUTTONS:",
                "    RULE_1:",
                "      MATERIAL: KNOWLEDGE_BOOK",
                "      SLOT: 12",
                "    RULE_2:",
                "      MATERIAL: KNOWLEDGE_BOOK",
                "      SLOT: 14"
        );

        int changes = mergeBundledDefaults("menus.yml", currentLines, bundledLines);

        assertEquals(0, changes);
        assertFalse(currentLines.contains("    RULE_1:"),
                "a renamed rules page must not come back on the slot its replacement now uses");
        assertFalse(currentLines.contains("    RULE_2:"),
                "a deleted rules page must not come back carrying the bundled server's rules");
        assertTrue(currentLines.contains("    HOUSE_RULES:"));
    }

    @Test
    void mergeBundledDefaultsKeepsServersMenuEntriesAdminsRenamed() throws Exception {
        List<String> currentLines = lines(
                "SERVERS-MENU:",
                "  TITLE: '&8Ongoing Servers'",
                "  SIZE: 27",
                "  SERVERS:",
                "    survival:",
                "      SLOT: 13"
        );
        List<String> bundledLines = lines(
                "SERVERS-MENU:",
                "  TITLE: '&8Ongoing Servers'",
                "  SIZE: 27",
                "  SERVERS:",
                "    crystal:",
                "      SLOT: 13"
        );

        int changes = mergeBundledDefaults("menus.yml", currentLines, bundledLines);

        assertEquals(0, changes);
        assertFalse(currentLines.contains("    crystal:"),
                "a renamed network id must not come back and render as a permanently offline server");
        assertTrue(currentLines.contains("    survival:"));
    }

    @Test
    void mergeBundledDefaultsStillInstallsTheRulesAndServersMenusOnConfigsThatPredateThem() throws Exception {
        List<String> currentLines = lines(
                "GLOBAL:",
                "  ENABLED: true"
        );
        List<String> bundledLines = lines(
                "GLOBAL:",
                "  ENABLED: true",
                "RULES-MENU:",
                "  BUTTONS:",
                "    RULE_1:",
                "      SLOT: 12",
                "SERVERS-MENU:",
                "  SERVERS:",
                "    crystal:",
                "      SLOT: 13"
        );

        int changes = mergeBundledDefaults("menus.yml", currentLines, bundledLines);

        assertTrue(changes > 0);
        assertTrue(currentLines.contains("    RULE_1:"),
                "the first install of the rules menu still needs its bundled pages");
        assertTrue(currentLines.contains("    crystal:"),
                "the first install of the servers menu still needs its bundled example");
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

    @Test
    void mergeBundledDefaultsKeepsDeletedKeyAllCratesDeleted() throws Exception {
        List<String> currentLines = lines(
                "KEY-ALL:",
                "  ENABLED: true",
                "  RANDOM:",
                "    KEYS:",
                "      # The numerical value for Common. Available options: Any valid integer",
                "      common: 90",
                "      gold: 10"
        );
        List<String> bundledLines = lines(
                "KEY-ALL:",
                "  ENABLED: true",
                "  RANDOM:",
                "    KEYS:",
                "      # The numerical value for Common. Available options: Any valid integer",
                "      common: 60",
                "      # The numerical value for Rare. Available options: Any valid integer",
                "      rare: 30",
                "      # The numerical value for Epic. Available options: Any valid integer",
                "      epic: 10"
        );

        int changes = mergeBundledDefaults("config.yml", currentLines, bundledLines);

        assertEquals(0, changes);
        assertFalse(currentLines.contains("      rare: 30"));
        assertFalse(currentLines.contains("      epic: 10"));
        assertTrue(currentLines.contains("      common: 90"));
        assertTrue(currentLines.contains("      gold: 10"));
    }

    @Test
    void mergeBundledDefaultsStillAddsMissingKeyAllRandomSection() throws Exception {
        List<String> currentLines = lines(
                "KEY-ALL:",
                "  ENABLED: true"
        );
        List<String> bundledLines = lines(
                "KEY-ALL:",
                "  ENABLED: true",
                "  RANDOM:",
                "    KEYS:",
                "      common: 60",
                "      rare: 30",
                "      epic: 10"
        );

        int changes = mergeBundledDefaults("config.yml", currentLines, bundledLines);

        assertTrue(changes > 0);
        assertTrue(currentLines.contains("      common: 60"));
        assertTrue(currentLines.contains("      rare: 30"));
        assertTrue(currentLines.contains("      epic: 10"));
    }

    @Test
    void repairsMiscasedPlaceholdersLeftInExistingMenus() throws Exception {
        List<String> currentLines = lines(
                "SELL-MENU:",
                "  ORES-BUTTON:",
                "    LORE:",
                "    - '&7Progress to &f{neXt_multiplier}'",
                "    - '{porcentage_level} &#6BF18D{porcentage}%'",
                "PROGRESS-MENU:",
                "  WORKING-BUTTON:",
                "    LORE:",
                "    - '&7{current_earned}/{neXt_goal}'",
                "SPAWNER-STORAGE-MENU:",
                "  NEXT-PAGE:",
                "    LORE:",
                "    - '&7Go to page &f{neXt_page}&7.' # admin note",
                "    - '&7Location: &f{world} ({X}, {y}, {z})'"
        );

        int repaired = repairMiscasedPlaceholders("menus.yml", currentLines);
        YamlConfiguration merged = yaml(currentLines);

        assertEquals(4, repaired);
        assertEquals(
                List.of("&7Progress to &f{next_multiplier}", "{porcentage_level} &#6BF18D{porcentage}%"),
                merged.getStringList("SELL-MENU.ORES-BUTTON.LORE")
        );
        assertEquals(
                List.of("&7{current_earned}/{next_goal}"),
                merged.getStringList("PROGRESS-MENU.WORKING-BUTTON.LORE")
        );
        assertEquals(
                List.of("&7Go to page &f{next_page}&7.", "&7Location: &f{world} ({x}, {y}, {z})"),
                merged.getStringList("SPAWNER-STORAGE-MENU.NEXT-PAGE.LORE")
        );
        assertTrue(currentLines.contains("    - '&7Go to page &f{next_page}&7.' # admin note"));
    }

    @Test
    void repairsMiscasedPlaceholdersInGeneratedLanguageFiles() throws Exception {
        List<String> currentLines = lines(
                "MENUS:",
                "  SELL-MENU:",
                "    ORES-BUTTON:",
                "      LORE:",
                "      - '&7Progress to &f{neXt_multiplier}'"
        );

        int repaired = repairMiscasedPlaceholders("languages/id_ID.yml", currentLines);

        assertEquals(1, repaired);
        assertEquals(
                List.of("&7Progress to &f{next_multiplier}"),
                yaml(currentLines).getStringList("MENUS.SELL-MENU.ORES-BUTTON.LORE")
        );
    }

    @Test
    void repairMiscasedPlaceholdersLeavesCleanFilesAndOtherConfigsAlone() throws Exception {
        List<String> cleanMenus = lines(
                "SELL-MENU:",
                "  ORES-BUTTON:",
                "    LORE:",
                "    - '&7Progress to &f{next_multiplier}'"
        );
        List<String> cleanBefore = new ArrayList<>(cleanMenus);

        assertEquals(0, repairMiscasedPlaceholders("menus.yml", cleanMenus));
        assertEquals(cleanBefore, cleanMenus);

        List<String> otherConfig = lines(
                "SETTINGS:",
                "  TEXT: '&7Page {neXt_page}'"
        );
        List<String> otherBefore = new ArrayList<>(otherConfig);

        assertEquals(0, repairMiscasedPlaceholders("config.yml", otherConfig));
        assertEquals(otherBefore, otherConfig);
    }

    @Test
    void bundledMenusAndLanguagesShipNoMiscasedPlaceholders() throws Exception {
        List<Path> targets = new ArrayList<>();
        targets.add(Path.of("src/main/resources/menus.yml"));
        try (Stream<Path> paths = Files.list(Path.of("src/main/resources/languages"))) {
            paths.filter(ConfigManagerTest::isYamlResource).forEach(targets::add);
        }

        for (Path target : targets) {
            String fileName = target.getFileName().toString();
            String resourceName = "menus.yml".equals(fileName) ? fileName : "languages/" + fileName;
            List<String> fileLines = new ArrayList<>(Files.readAllLines(target, StandardCharsets.UTF_8));

            assertEquals(
                    0,
                    repairMiscasedPlaceholders(resourceName, fileLines),
                    () -> "Mis-cased placeholder names shipped in " + target
            );
        }
    }

    @Test
    void bundledSettingsMenuIsNotMistakenForTheOldLayout() throws Exception {
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.options().parseComments(true);
        bundled.load(Path.of("src/main/resources/menus.yml").toFile());

        assertFalse(hasLegacyButtons(bundled));
    }

    @Test
    void theScatteredSettingsMenuIsRegenerated() throws Exception {
        YamlConfiguration scattered = yaml(lines(
                "SETTINGS-MENU:",
                "  BUTTONS:",
                "    JOIN_LEAVE_MESSAGES:",
                "      SLOT: 31",
                "    PAY_ALERTS:",
                "      SLOT: 32",
                "    MONEY_NAMETAGS:",
                "      SLOT: 33"
        ));

        assertTrue(hasLegacyButtons(scattered));
    }

    @Test
    void theFirstGroupedSettingsMenuIsRegeneratedToo() throws Exception {
        // Servers that picked up the interim layout would otherwise keep it forever.
        YamlConfiguration interim = yaml(lines(
                "SETTINGS-MENU:",
                "  BUTTONS:",
                "    NOTIFICATION_SOUNDS:",
                "      SLOT: 16",
                "    TPA_CONFIRM_MENUS:",
                "      SLOT: 36"
        ));

        assertTrue(hasLegacyButtons(interim));
    }

    @Test
    void theLayoutThatStillHadLunarTeammatesAtTwentyTwoIsRegeneratedToo() throws Exception {
        YamlConfiguration previous = yaml(lines(
                "SETTINGS-MENU:",
                "  BUTTONS:",
                "    LUNAR_TEAMMATES:",
                "      SLOT: 22"
        ));

        assertTrue(hasLegacyButtons(previous));
    }

    @Test
    void aMenuWithoutSettingsButtonsIsLeftAlone() throws Exception {
        assertFalse(hasLegacyButtons(yaml(lines("OTHER-MENU:", "  SIZE: 27"))));
    }

    private static boolean hasLegacyButtons(YamlConfiguration configuration) throws Exception {
        Method method = ConfigManager.class.getDeclaredMethod("hasLegacyButtons", YamlConfiguration.class);
        method.setAccessible(true);
        return (boolean) method.invoke(new ConfigManager(null), configuration);
    }

    private static int repairMiscasedPlaceholders(
            String resourceName,
            List<String> lines
    ) throws Exception {
        Method method = ConfigManager.class.getDeclaredMethod(
                "repairMiscasedPlaceholders",
                String.class,
                List.class
        );
        method.setAccessible(true);
        return (int) method.invoke(new ConfigManager(null), resourceName, lines);
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
