package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageManagerTest {

    private static final Pattern PLACEHOLDER = Pattern.compile(
            "\\{[^{}]+}|%[^%\\s]+%|<[^<>]+>"
    );
    private static final Pattern COLOR_CODE = Pattern.compile(
            "&(?:#[0-9A-Fa-f]{6}|[0-9A-FK-ORa-fk-or])"
    );

    @Test
    void resolvesSupportedLocaleAliases() {
        assertEquals("en_US", LanguageManager.resolveLocale("ENGLISH"));
        assertEquals("es_ES", LanguageManager.resolveLocale("Spanish"));
        assertEquals("es_ES", LanguageManager.resolveLocale("SPANYOL"));
        assertEquals("id_ID", LanguageManager.resolveLocale("Bahasa Indonesia"));
        assertEquals("pt_BR", LanguageManager.resolveLocale("pt-BR"));
        assertEquals("zh_CN", LanguageManager.resolveLocale("simplified chinese"));
        assertEquals("tr_TR", LanguageManager.resolveLocale("Turkish"));
        assertEquals("tr_TR", LanguageManager.resolveLocale("türkçe"));
        assertEquals("it_IT", LanguageManager.resolveLocale("Italian"));
        assertEquals("pl_PL", LanguageManager.resolveLocale("polish"));
        assertEquals("nl_NL", LanguageManager.resolveLocale("Dutch"));
        assertNull(LanguageManager.resolveLocale("klingon"));
    }

    @Test
    void resolvesCustomLocales() throws Exception {
        LanguageManager manager = new LanguageManager(null);
        java.lang.reflect.Field languagesField = LanguageManager.class.getDeclaredField("languages");
        languagesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, YamlConfiguration> languages = (java.util.Map<String, YamlConfiguration>) languagesField.get(manager);
        languages.put("pirate", new YamlConfiguration());
        languages.put("tr_TR", new YamlConfiguration());

        java.lang.reflect.Field currentField = LanguageManager.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(null, manager);

        try {
            assertEquals("pirate", LanguageManager.resolveLocale("pirate"));
            assertEquals("tr_TR", LanguageManager.resolveLocale("tr-TR"));
            assertEquals("tr_TR", LanguageManager.resolveLocale("turkish"));
        } finally {
            currentField.set(null, null);
        }
    }

    @Test
    void bundledLanguageResourcesParseRecursively() throws Exception {
        Path root = Path.of("src/main/resources/languages");
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.filter(LanguageManagerTest::isYaml).toList()) {
                YamlConfiguration configuration = new YamlConfiguration();
                configuration.options().parseComments(true);
                configuration.load(path.toFile());
            }
        }
    }

    @Test
    void bundledLanguagesHaveEnglishKeyPlaceholderAndColorParity() throws Exception {
        YamlConfiguration english = load("en_US");
        for (String locale : List.of("es_ES", "id_ID", "pt_BR", "de_DE", "fr_FR", "ru_RU", "zh_CN")) {
            YamlConfiguration translated = load(locale);
            LanguageManager.mergeMissing(translated, english);

            assertEquals(scalarKeys(english), scalarKeys(translated), locale + " key mismatch");
            for (String path : scalarKeys(english)) {
                assertEquals(
                        matches(english.get(path), PLACEHOLDER),
                        matches(translated.get(path), PLACEHOLDER),
                        locale + " placeholder mismatch at " + path
                );
                assertEquals(
                        matches(english.get(path), COLOR_CODE),
                        matches(translated.get(path), COLOR_CODE),
                        locale + " color-code mismatch at " + path
                );
            }
        }
    }

    @Test
    void bundledUnicodeTranslationsRemainReadable() throws Exception {
        assertTrue(load("ru_RU").getString("META.NAME", "").matches(".*[\\p{IsCyrillic}].*"));
        assertTrue(load("zh_CN").getString("META.NAME", "").matches(".*[\\p{IsHan}].*"));
        assertTrue(load("es_ES").getString("META.NAME", "").contains("ñ"));
    }

    @Test
    void preservesWhitespaceOnlyLanguageValuesForTimeSeparators() throws Exception {
        assertEquals(" ", load("en_US").getString("TIME.SEPARATOR"));
        assertEquals(" ", LanguageManager.selectTextValue(" ", null, "fallback"));
        assertEquals("", LanguageManager.selectTextValue("", null, "fallback"));
        assertEquals("legacy", LanguageManager.selectTextValue(null, "legacy", "fallback"));
        assertEquals("fallback", LanguageManager.selectTextValue(null, null, "fallback"));
    }

    @Test
    void mergeAddsMissingKeysWithoutOverwritingExistingTranslation() throws Exception {
        YamlConfiguration english = load("en_US");
        YamlConfiguration translated = new YamlConfiguration();
        translated.set("MENUS.COMMON.CLOSE.NAME", "&cCustom Close");

        int added = LanguageManager.mergeMissing(translated, english);

        assertTrue(added > 0);
        assertEquals("&cCustom Close", translated.getString("MENUS.COMMON.CLOSE.NAME"));
        assertFalse(translated.getStringList("MENUS.COMMON.CLOSE.LORE").isEmpty());
    }

    @Test
    void selectsPlayerFacingTextWithoutCopyingTechnicalConfiguration() throws Exception {
        YamlConfiguration source = new YamlConfiguration();
        source.set("GUI.TITLE", "&8Example");
        source.set("GUI.BUTTON.NAME", "&aConfirm");
        source.set("GUI.BUTTON.LORE", List.of("&7Click to continue"));
        source.set("GUI.BUTTON.MATERIAL", "LIME_DYE");
        source.set("GUI.BUTTON.COMMAND", "example confirm");
        source.set("GUI.BUTTON.PERMISSION", "example.use");
        source.set("GUI.SOUNDS.CLICK.NAME", "BLOCK_NOTE_BLOCK_PLING");
        source.set("GUI.ENABLED", true);
        source.set("MESSAGES.SUCCESS", "&aDone");
        source.set("MESSAGES.SOUND", "BLOCK_NOTE_BLOCK_PLING");
        source.set("SETTINGS.SERVER-ID", "survival");

        YamlConfiguration target = new YamlConfiguration();
        int menuAdded = LanguageManager.mergeTranslatableSection(
                target,
                "MENUS.TEST",
                source.getConfigurationSection("GUI"),
                true,
                List.of()
        );
        int featureAdded = LanguageManager.mergeTranslatableSection(
                target,
                "CONFIG.TEST",
                source,
                false,
                List.of()
        );

        assertEquals(3, menuAdded);
        assertEquals("&8Example", target.getString("MENUS.TEST.TITLE"));
        assertEquals("&aConfirm", target.getString("MENUS.TEST.BUTTON.NAME"));
        assertEquals(List.of("&7Click to continue"), target.getStringList("MENUS.TEST.BUTTON.LORE"));
        assertFalse(target.contains("MENUS.TEST.BUTTON.MATERIAL"));
        assertFalse(target.contains("MENUS.TEST.BUTTON.COMMAND"));
        assertFalse(target.contains("MENUS.TEST.BUTTON.PERMISSION"));
        assertFalse(target.contains("MENUS.TEST.SOUNDS.CLICK.NAME"));
        assertFalse(target.contains("MENUS.TEST.ENABLED"));

        assertEquals(4, featureAdded);
        assertEquals("&aDone", target.getString("CONFIG.TEST.MESSAGES.SUCCESS"));
        assertEquals("&8Example", target.getString("CONFIG.TEST.GUI.TITLE"));
        assertFalse(target.contains("CONFIG.TEST.MESSAGES.SOUND"));
        assertFalse(target.contains("CONFIG.TEST.SETTINGS.SERVER-ID"));
    }

    @Test
    void bundledEnglishContainsCurrentPlayerFacingResourceText() throws Exception {
        YamlConfiguration english = load("en_US");
        int added = mergeCurrentResourceText(english);

        assertEquals(0, added, "en_US.yml is missing player-facing text from current UDS resources");
    }

    private static int mergeCurrentResourceText(YamlConfiguration target) throws Exception {
        int added = 0;
        added += mergeAll(target, "MESSAGES", "messages.yml");
        added += mergeAll(target, "DEATH_MESSAGES", "death-messages.yml");
        added += mergeText(target, "MENUS", "menus.yml", true, List.of(
                "MEDIA-MENU", "RULES-MENU", "SERVER-INFO-MENU", "SERVERS-MENU",
                "SPAWN-MENU.AREAS", "AFK-MENU.AREAS"
        ));
        added += mergeText(target, "CONFIG.BILLFORD", "billford.yml", false,
                List.of("BILLFORD", "ACCESS.NPC.DISPLAY_NAME"));
        added += mergeText(target, "CONFIG.RTP", "rtp.yml", false,
                List.of("DENIED-WORLDS", "WORLD-SETTINGS"));
        added += mergeText(target, "CONFIG.AMETHYST_TOOLS", "amethyst-tools.yml", false, List.of());
        added += mergeText(target, "CONFIG.ENDER_CHEST", "ender-chest.yml", false, List.of());
        added += mergeText(target, "CONFIG.INVSEE", "invsee.yml", false, List.of());
        added += mergeText(target, "CONFIG.FREEZE", "freeze.yml", false,
                List.of("FREEZE.SERVER-NAME", "FREEZE.ALLOWED-COMMANDS"));
        added += mergeText(target, "CONFIG.AUCTION_HOUSE", "auction-house.yml", false, List.of());
        added += mergeText(target, "CONFIG.ORDERS", "orders.yml", false, List.of("CATEGORY_FILTERS"));
        added += mergeText(target, "CONFIG.DUELS", "duels.yml", false,
                List.of("ARENA_SETTINGS", "MAP_SOURCES"));
        added += mergeText(target, "CONFIG.FFA", "ffa.yml", false, List.of("ARENA_SETTINGS"));
        added += mergeText(target, "CONFIG.CRATES", "crates.yml", false, List.of("CRATES"));
        added += mergeText(target, "CONFIG.SPAWNERS", "spawners.yml", false, List.of("TYPES"));
        added += mergeText(target, "CONFIG.SPAWN_STASH", "spawn-stash.yml", false, List.of("TYPES"));
        added += mergeText(target, "CONFIG.NETWORK", "network.yml", false, List.of(
                "NETWORK-STATUS.SERVERS",
                "NETWORK.LOCAL_DISPLAY_NAME",
                "NETWORK-STATUS.LOCAL-DISPLAY-NAME"
        ));
        added += mergeText(target, "CONFIG.STAFF_MODE", "staff-mode.yml", false, List.of());
        added += mergeText(target, "CONFIG.SERVER_WIPE", "server-wipe.yml", false, List.of());
        added += mergeText(target, "CONFIG.WORTH", "worth.yml", false, List.of("BLOCK-ITEMS"));
        added += LanguageManager.mergeShopText(target, loadResource("shop.yml"));
        added += mergeText(target, "CONFIG.HIDE", "hide.yml", false,
                List.of("ALIASES", "SKINS", "STAFF-MARKER"));
        added += mergeText(target, "CONFIG.ENCHANTMENTS", "enchantments.yml", false, List.of(
                "helmet", "chestplate", "leggings", "boots", "elytra", "bow", "crossbow",
                "sword", "axe", "pickaxe", "shovel", "hoe", "fishing_rod", "trident", "mace"
        ));
        return added;
    }

    private static int mergeAll(YamlConfiguration target, String root, String resource) throws Exception {
        YamlConfiguration source = loadResource(resource);
        int added = 0;
        for (String path : source.getKeys(true)) {
            Object value = source.get(path);
            if (!source.isConfigurationSection(path)
                    && isTextValue(value)
                    && !target.contains(root + "." + path, true)) {
                target.set(root + "." + path, source.get(path));
                added++;
            }
        }
        return added;
    }

    private static int mergeText(
            YamlConfiguration target,
            String root,
            String resource,
            boolean menu,
            List<String> excludedPaths
    ) throws Exception {
        return LanguageManager.mergeTranslatableSection(
                target,
                root,
                loadResource(resource),
                menu,
                excludedPaths
        );
    }

    private static YamlConfiguration loadResource(String name) throws Exception {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.load(Path.of("src/main/resources", name).toFile());
        return configuration;
    }

    private static boolean isTextValue(Object value) {
        return value instanceof String
                || value instanceof List<?> list && list.stream().allMatch(String.class::isInstance);
    }

    private static YamlConfiguration load(String locale) throws Exception {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.load(Path.of("src/main/resources/languages", locale + ".yml").toFile());
        return configuration;
    }

    private static Set<String> scalarKeys(YamlConfiguration configuration) {
        Set<String> keys = new HashSet<>();
        for (String key : configuration.getKeys(true)) {
            if (!configuration.isConfigurationSection(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private static List<String> matches(Object value, Pattern pattern) {
        List<String> matches = new java.util.ArrayList<>();
        if (value instanceof String text) {
            collectMatches(text, pattern, matches);
        } else if (value instanceof List<?> list) {
            for (Object entry : list) {
                if (entry instanceof String text) {
                    collectMatches(text, pattern, matches);
                }
            }
        }
        matches.sort(String::compareTo);
        return matches;
    }

    private static void collectMatches(String text, Pattern pattern, List<String> target) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            target.add(matcher.group());
        }
    }

    private static boolean isYaml(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".yml") || name.endsWith(".yaml");
    }
}
