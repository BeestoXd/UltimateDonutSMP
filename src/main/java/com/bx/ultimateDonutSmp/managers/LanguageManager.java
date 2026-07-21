package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class LanguageManager {

    public static final String DEFAULT_LOCALE = "en_US";
    private static volatile LanguageManager current;

    private static final List<String> BUNDLED_LOCALES = List.of(
            "en_US", "es_ES", "id_ID", "pt_BR", "de_DE", "fr_FR", "ru_RU", "zh_CN"
    );
    private static final DateTimeFormatter BACKUP_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final Map<String, String> LOCALE_ALIASES = createLocaleAliases();
    private static final Set<String> TEXT_KEY_MARKERS = Set.of(
            "TITLE", "NAME", "LORE", "MESSAGE", "TEXT", "LABEL", "DESCRIPTION",
            "STATUS", "STATE", "FORMAT", "LINES", "HEADER", "FOOTER", "HOVER",
            "ANNOUNCE", "PROMPT", "ENABLED", "DISABLED", "USED", "EMPTY",
            "SUBTITLE", "ACTIONBAR"
    );
    private static final Set<String> TECHNICAL_KEY_MARKERS = Set.of(
            "MATERIAL", "PERMISSION", "COMMAND", "SOUND", "URL", "WORLD",
            "SERVER-ID", "CHANNEL", "TOKEN", "HOST", "PATH", "SOURCE",
            "ENTITY_TYPE", "ICON_MATERIAL", "OPEN-TYPE", "TYPE", "MODE",
            "ACTION", "CURRENCY", "START_DATE", "TIME_ZONE", "REDIS", "FOLDER",
            "CUBOID"
    );
    private static final List<String> MENU_EXCLUDED_PATHS = List.of(
            "MEDIA-MENU", "RULES-MENU", "SERVER-INFO-MENU", "SERVERS-MENU",
            "SPAWN-MENU.AREAS", "AFK-MENU.AREAS"
    );

    private final UltimateDonutSmp plugin;
    private final Map<String, YamlConfiguration> languages = new LinkedHashMap<>();
    private final Map<String, YamlConfiguration> bundledLanguages = new HashMap<>();
    private final Map<FileConfiguration, Map<String, FileConfiguration>> localizedConfigurations =
            new IdentityHashMap<>();
    private final Set<String> warnedMissingKeys = new HashSet<>();
    private final Set<String> warnedInvalidLocales = new HashSet<>();
    private final Map<String, String> builtInTranslations = new HashMap<>();

    private String activeLocale = DEFAULT_LOCALE;
    private String fallbackLocale = DEFAULT_LOCALE;

    public LanguageManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void load() {
        languages.clear();
        localizedConfigurations.clear();
        warnedMissingKeys.clear();
        bundledLanguages.clear();

        for (String locale : BUNDLED_LOCALES) {
            bundledLanguages.put(locale.toLowerCase(Locale.ROOT), loadBundledLanguage(locale));
        }

        YamlConfiguration englishDefaults = loadBundledLanguage(DEFAULT_LOCALE);
        mergeCurrentPlayerText(englishDefaults);

        Set<String> loadedLocales = new HashSet<>();

        File folder = new File(plugin.getDataFolder(), "languages");
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.isFile()) {
                        continue;
                    }
                    String name = file.getName();
                    if (name.endsWith(".yml") || name.endsWith(".yaml")) {
                        String locale = name.substring(0, name.lastIndexOf('.'));
                        String resolvedLocale = resolveLocale(locale);
                        if (resolvedLocale != null) {
                            if (loadedLocales.add(resolvedLocale.toLowerCase(Locale.ROOT))) {
                                YamlConfiguration defaults;
                                if (BUNDLED_LOCALES.contains(resolvedLocale)) {
                                    defaults = loadBundledLanguage(resolvedLocale);
                                    mergeMissing(defaults, englishDefaults);
                                } else {
                                    defaults = englishDefaults;
                                }
                                languages.put(resolvedLocale, loadAndSyncLocale(resolvedLocale, file, defaults));
                            }
                        } else {
                            String key = locale;
                            if (loadedLocales.add(key.toLowerCase(Locale.ROOT))) {
                                languages.put(key, loadAndSyncLocale(key, file, englishDefaults));
                            }
                        }
                    }
                }
            }
        }

        for (String locale : BUNDLED_LOCALES) {
            if (loadedLocales.add(locale.toLowerCase(Locale.ROOT))) {
                YamlConfiguration defaults = loadBundledLanguage(locale);
                mergeMissing(defaults, englishDefaults);
                File target = new File(folder, locale + ".yml");
                languages.put(locale, loadAndSyncLocale(locale, target, defaults));
            }
        }

        FileConfiguration config = plugin.getConfigManager().getConfig();
        activeLocale = resolveAvailableLocale(
                config.getString("LANGUAGE.ACTIVE", DEFAULT_LOCALE),
                DEFAULT_LOCALE,
                "active"
        );
        fallbackLocale = resolveAvailableLocale(
                config.getString("LANGUAGE.FALLBACK", DEFAULT_LOCALE),
                DEFAULT_LOCALE,
                "fallback"
        );
        for (java.util.Map.Entry<String, YamlConfiguration> entry : languages.entrySet()) {
            String locale = entry.getKey();
            YamlConfiguration langConfig = entry.getValue();
            if (locale.equalsIgnoreCase(DEFAULT_LOCALE)) {
                mergePlayerLegacyCustomizations(langConfig, locale, false);
            }
        }
        rebuildBuiltInTranslations();
        current = this;

        NumberUtils.setDurationFormatter(new NumberUtils.DurationFormatter() {
            @Override
            public String formatTime(long totalSeconds) {
                return formatDuration(totalSeconds, false);
            }

            @Override
            public String formatTimeLong(long totalSeconds) {
                return formatDuration(totalSeconds, true);
            }

            @Override
            public String formatCountdown(long totalSeconds) {
                return formatCountdownDuration(totalSeconds);
            }
        });

        plugin.getLogger().info("Language loaded: " + activeLocale
                + (activeLocale.equals(fallbackLocale) ? "" : " (fallback: " + fallbackLocale + ")"));
    }

    public void reload() {
        load();
    }

    public String getActiveLocale() {
        return activeLocale;
    }

    public String getFallbackLocale() {
        return fallbackLocale;
    }

    public List<String> getAvailableLocales() {
        return List.copyOf(languages.keySet());
    }

    public static String translateBuiltInText(String text) {
        LanguageManager manager = current;
        return manager == null || text == null ? text : manager.builtInTranslations.getOrDefault(text, text);
    }

    public String text(String path) {
        return text(path, null, null);
    }

    public String text(String path, String legacyFallback) {
        return text(path, legacyFallback, null);
    }

    public String text(String path, String legacyFallback, String javaFallback, String... placeholders) {
        String value = selectTextValue(findString(path), legacyFallback, javaFallback);
        if (value == null) {
            value = "&cmissing language key: " + path;
            if (warnedMissingKeys.add(path)) {
                plugin.getLogger().warning("Missing language key: " + path);
            }
        }
        return replace(value, placeholders);
    }

    public List<String> list(String path, List<String> legacyFallback, String... placeholders) {
        List<String> value = findStringList(path);
        if (value == null) {
            value = legacyFallback == null ? List.of() : legacyFallback;
        }
        List<String> resolved = new ArrayList<>(value.size());
        for (String line : value) {
            resolved.add(replace(line, placeholders));
        }
        return resolved;
    }

    public String component(String path, Player player, String legacyFallback, String... placeholders) {
        return ColorUtils.toComponent(text(path, legacyFallback, null, placeholders), player);
    }

    public List<String> components(String path, Player player, List<String> legacyFallback, String... placeholders) {
        return ColorUtils.toComponentList(list(path, legacyFallback, placeholders), player);
    }

    public String message(String path, String legacyFallback, String... placeholders) {
        return text("MESSAGES." + path, legacyFallback, null, placeholders);
    }

    public String menu(String path, String legacyFallback, String... placeholders) {
        return text("MENUS." + path, legacyFallback, null, placeholders);
    }

    public List<String> menuList(String path, List<String> legacyFallback, String... placeholders) {
        return list("MENUS." + path, legacyFallback, placeholders);
    }

    public String display(String category, String key, String fallback) {
        return text("DISPLAY." + category + "." + normalizeDisplayKey(key), fallback);
    }

    public void clearLocalizedConfigurations() {
        localizedConfigurations.clear();
    }

    public FileConfiguration localize(String rootPath, FileConfiguration legacyConfiguration) {
        if (legacyConfiguration == null || !hasLanguageSection(rootPath)) {
            return legacyConfiguration;
        }
        Map<String, FileConfiguration> byRoot = localizedConfigurations.computeIfAbsent(
                legacyConfiguration,
                ignored -> new HashMap<>()
        );
        return byRoot.computeIfAbsent(rootPath, ignored -> buildLocalizedConfiguration(rootPath, legacyConfiguration));
    }

    public String formatDuration(long totalSeconds, boolean includeDays) {
        long seconds = Math.max(0L, totalSeconds);
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remainingSeconds = seconds % 60L;
        List<String> parts = new ArrayList<>(3);

        if (includeDays && days > 0L) {
            parts.add(days + text("TIME.UNITS.DAY_SHORT", null, "d"));
            parts.add(hours + text("TIME.UNITS.HOUR_SHORT", null, "h"));
            parts.add(minutes + text("TIME.UNITS.MINUTE_SHORT", null, "m"));
        } else if (hours > 0L) {
            parts.add(hours + text("TIME.UNITS.HOUR_SHORT", null, "h"));
            parts.add(minutes + text("TIME.UNITS.MINUTE_SHORT", null, "m"));
        } else if (minutes > 0L) {
            parts.add(minutes + text("TIME.UNITS.MINUTE_SHORT", null, "m"));
            parts.add(remainingSeconds + text("TIME.UNITS.SECOND_SHORT", null, "s"));
        } else {
            parts.add(remainingSeconds + text("TIME.UNITS.SECOND_SHORT", null, "s"));
        }
        return String.join(text("TIME.SEPARATOR", null, " "), parts);
    }

    private String formatCountdownDuration(long totalSeconds) {
        long seconds = Math.max(0L, totalSeconds);
        long minutes = seconds / 60L;
        long remainingSeconds = seconds % 60L;
        if (minutes > 0L) {
            return minutes + text("TIME.UNITS.MINUTE_SHORT", null, "m")
                    + text("TIME.SEPARATOR", null, " ")
                    + remainingSeconds + text("TIME.UNITS.SECOND_SHORT", null, "s");
        }
        return remainingSeconds + text("TIME.UNITS.SECOND_SHORT", null, "s");
    }

    private FileConfiguration buildLocalizedConfiguration(
            String rootPath,
            FileConfiguration legacyConfiguration
    ) {
        YamlConfiguration localized = new YamlConfiguration();
        for (String key : legacyConfiguration.getKeys(true)) {
            if (!legacyConfiguration.isConfigurationSection(key)) {
                localized.set(key, copyValue(legacyConfiguration.get(key)));
            }
        }
        applyLanguageSection(localized, languages.get(fallbackLocale), fallbackLocale, rootPath);
        if (!activeLocale.equals(fallbackLocale)) {
            applyLanguageSection(localized, languages.get(activeLocale), activeLocale, rootPath);
        }
        return localized;
    }

    private void applyLanguageSection(
            YamlConfiguration target,
            YamlConfiguration language,
            String locale,
            String rootPath
    ) {
        if (language == null) {
            return;
        }
        ConfigurationSection section = language.getConfigurationSection(rootPath);
        if (section == null) {
            return;
        }
        YamlConfiguration bundled = bundledLanguages.get(locale.toLowerCase(Locale.ROOT));
        ConfigurationSection bundledSection = bundled != null ? bundled.getConfigurationSection(rootPath) : null;

        for (String key : section.getKeys(true)) {
            if (section.isConfigurationSection(key)) {
                continue;
            }
            Object value = section.get(key);
            if (value instanceof String || isStringList(value)) {
                if (target.contains(key)) {
                    Object existingTargetValue = target.get(key);
                    if (bundledSection != null && bundledSection.contains(key)) {
                        Object bundledValue = bundledSection.get(key);
                        if (existingTargetValue != null && !existingTargetValue.equals(bundledValue)) {
                            continue;
                        }
                        if (value.equals(bundledValue)) {
                            continue;
                        }
                    }
                }
                target.set(key, copyValue(value));
            }
        }
    }

    private boolean hasLanguageSection(String rootPath) {
        YamlConfiguration active = languages.get(activeLocale);
        YamlConfiguration fallback = languages.get(fallbackLocale);
        return (active != null && active.isConfigurationSection(rootPath))
                || (fallback != null && fallback.isConfigurationSection(rootPath));
    }

    private String findString(String path) {
        YamlConfiguration active = languages.get(activeLocale);
        if (active != null && active.isString(path)) {
            return active.getString(path);
        }
        YamlConfiguration fallback = languages.get(fallbackLocale);
        return fallback != null && fallback.isString(path) ? fallback.getString(path) : null;
    }

    private List<String> findStringList(String path) {
        YamlConfiguration active = languages.get(activeLocale);
        if (active != null && active.isList(path)) {
            return active.getStringList(path);
        }
        YamlConfiguration fallback = languages.get(fallbackLocale);
        return fallback != null && fallback.isList(path) ? fallback.getStringList(path) : null;
    }

    private YamlConfiguration loadAndSyncLocale(String locale, File target, YamlConfiguration defaults) {
        try {
            Files.createDirectories(target.getParentFile().toPath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create language directory.", e);
            return defaults;
        }

        if (!target.exists()) {
            try {
                defaults.save(target);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to create language file " + target.getPath(), e);
            }
            return defaults;
        }

        plugin.getConfigManager().syncGeneratedDefaults(
                "languages/" + target.getName(),
                target,
                defaults,
                "language-backups"
        );

        YamlConfiguration current = new YamlConfiguration();
        current.options().parseComments(true);
        try (java.io.Reader reader = new java.io.InputStreamReader(new java.io.FileInputStream(target), StandardCharsets.UTF_8)) {
            current.load(reader);
        } catch (IOException | InvalidConfigurationException e) {
            backupInvalidLanguage(target);
            if (warnedInvalidLocales.add(locale)) {
                plugin.getLogger().log(Level.WARNING,
                        "language file " + target.getPath()
                                + " is invalid. using bundled defaults in memory until it is fixed.",
                        e);
            }
            return defaults;
        }

        if ((current.contains("MENUS.SETTINGS-MENU-LEGACY") && !current.contains("MENUS.SETTINGS-MENU")) || current.contains("MENUS.SETTINGS-MENU.BUTTONS.TP_AUTO")) {
            plugin.getLogger().warning("Detected legacy language file format for " + locale + ". Resetting it to default...");
            backupInvalidLanguage(target);
            target.delete();
            try {
                defaults.save(target);
                try (java.io.Reader reader = new java.io.InputStreamReader(new java.io.FileInputStream(target), StandardCharsets.UTF_8)) {
                    current.load(reader);
                }
            } catch (IOException | InvalidConfigurationException e) {
                // fallback
            }
        }

        mergeMissing(current, defaults);
        return current;
    }

    private void mergeCurrentPlayerText(YamlConfiguration englishDefaults) {
        mergePlayerLegacyCustomizations(englishDefaults, DEFAULT_LOCALE, false);
    }

    private void mergePlayerLegacyCustomizations(YamlConfiguration target, String locale, boolean force) {
        ConfigManager config = plugin.getConfigManager();
        mergeSection(target, "MESSAGES", config.getLegacyMessages(), force);
        mergeSection(target, "DEATH_MESSAGES", config.getLegacyDeathMessages(), force);
        mergeTranslatableSection(target, "MENUS", config.getLegacyMenus(), true, MENU_EXCLUDED_PATHS, force);
        mergeFeature(target, "BILLFORD", config.getBillford(), List.of("BILLFORD", "ACCESS.NPC.DISPLAY_NAME"), force);
        mergeFeature(target, "RTP", config.getRtp(), List.of("DENIED-WORLDS", "WORLD-SETTINGS"), force);
        mergeFeature(target, "AMETHYST_TOOLS", config.getAmethystTools(), List.of(), force);
        mergeFeature(target, "ENDER_CHEST", config.getEnderChest(), List.of(), force);
        mergeFeature(target, "INVSEE", config.getInvsee(), List.of(), force);
        mergeFeature(target, "FREEZE", config.getFreeze(), List.of("FREEZE.SERVER-NAME", "FREEZE.ALLOWED-COMMANDS"), force);
        mergeFeature(target, "AUCTION_HOUSE", config.getAuctionHouse(), List.of(), force);
        mergeFeature(target, "ORDERS", config.getOrders(), List.of("CATEGORY_FILTERS"), force);
        mergeFeature(target, "DUELS", config.getDuels(), List.of("ARENA_SETTINGS", "MAP_SOURCES"), force);
        mergeFeature(target, "FFA", config.getFfa(), List.of("ARENA_SETTINGS"), force);
        mergeFeature(target, "CRATES", config.getCrates(), List.of("CRATES"), force);
        mergeFeature(target, "SPAWNERS", config.getSpawners(), List.of("TYPES"), force);
        mergeFeature(target, "SPAWN_STASH", config.getSpawnStash(), List.of("TYPES"), force);
        mergeFeature(target, "NETWORK", config.getNetwork(), List.of(
                "NETWORK-STATUS.SERVERS", "NETWORK.LOCAL_DISPLAY_NAME", "NETWORK-STATUS.LOCAL-DISPLAY-NAME"), force);
        mergeFeature(target, "STAFF_MODE", config.getStaffMode(), List.of(), force);
        mergeFeature(target, "SERVER_WIPE", config.getServerWipe(), List.of(), force);
        mergeFeature(target, "WORTH", config.getWorth(), List.of("BLOCK-ITEMS"), force);
        mergeShopText(target, config.getLegacyShop(), force);
        mergeFeature(target, "HIDE", config.getHide(), List.of("ALIASES", "SKINS", "STAFF-MARKER"), force);
        mergeFeature(target, "ENCHANTMENTS", config.getEnchantments(), List.of(
                "helmet", "chestplate", "leggings", "boots", "elytra", "bow", "crossbow",
                "sword", "axe", "pickaxe", "shovel", "hoe", "fishing_rod", "trident", "mace"), force);
    }

    private void mergeFeature(YamlConfiguration target, String feature, ConfigurationSection source, List<String> excludedPaths) {
        mergeTranslatableSection(target, "CONFIG." + feature, source, false, excludedPaths, false);
    }

    private void mergeFeature(YamlConfiguration target, String feature, ConfigurationSection source, List<String> excludedPaths, boolean force) {
        mergeTranslatableSection(target, "CONFIG." + feature, source, false, excludedPaths, force);
    }

    static int mergeShopText(YamlConfiguration target, ConfigurationSection shop) {
        return mergeShopText(target, shop, false);
    }

    static int mergeShopText(YamlConfiguration target, ConfigurationSection shop, boolean force) {
        ConfigurationSection shopGui = shop == null ? null : shop.getConfigurationSection("SHOP-GUI");
        if (shopGui == null) {
            return 0;
        }
        int added = 0;
        for (String path : shopGui.getKeys(true)) {
            if (shopGui.isConfigurationSection(path)
                    || path.toUpperCase(Locale.ROOT).endsWith(".MATERIAL")
                    || !isTranslatableValue(shopGui.get(path))) {
                continue;
            }
            String targetPath = "CONFIG.SHOP.SHOP-GUI." + path;
            if (force || !target.contains(targetPath, true)) {
                target.set(targetPath, copyValue(shopGui.get(path)));
                added++;
            }
        }
        return added;
    }

    static int mergeTranslatableSection(YamlConfiguration target, String targetRoot, ConfigurationSection source,
                                        boolean menu, List<String> excludedPaths) {
        return mergeTranslatableSection(target, targetRoot, source, menu, excludedPaths, false);
    }

    static int mergeTranslatableSection(YamlConfiguration target, String targetRoot, ConfigurationSection source,
                                        boolean menu, List<String> excludedPaths, boolean force) {
        if (source == null) return 0;
        int added = 0;
        for (String path : source.getKeys(true)) {
            if (source.isConfigurationSection(path) || isExcludedPath(path, excludedPaths)
                    || !isTranslatableValue(source.get(path)) || !isTranslatablePath(path, menu)) continue;
            String targetPath = targetRoot + "." + path;
            if (force || !target.contains(targetPath, true)) {
                target.set(targetPath, copyValue(source.get(path)));
                added++;
            }
        }
        return added;
    }

    private static boolean isTranslatablePath(String path, boolean menu) {
        String[] parts = path.toUpperCase(Locale.ROOT).split("\\.");
        String leaf = parts[parts.length - 1];
        for (String part : parts) {
            String normalizedPart = part.replace('_', '-');
            for (String marker : TECHNICAL_KEY_MARKERS) {
                String normalizedMarker = marker.replace('_', '-');
                if (normalizedPart.equals(normalizedMarker)
                        || normalizedPart.equals(normalizedMarker + "S")
                        || normalizedPart.startsWith(normalizedMarker + "-")
                        || normalizedPart.endsWith("-" + normalizedMarker)) return false;
            }
        }
        if (!menu) {
            for (String part : parts) if (part.equals("MESSAGES") || part.endsWith("-MESSAGES")) return true;
        }
        for (String marker : TEXT_KEY_MARKERS) if (leaf.contains(marker)) return true;
        return menu && Set.of("NEXT-BUTTON", "BACK-BUTTON", "FIRST-PAGE-BUTTON", "LAST-PAGE-BUTTON").contains(leaf);
    }

    private static boolean isExcludedPath(String path, List<String> excludedPaths) {
        for (String excluded : excludedPaths) {
            if (path.equalsIgnoreCase(excluded)
                    || path.regionMatches(true, 0, excluded + ".", 0, excluded.length() + 1)) return true;
        }
        return false;
    }

    private static boolean isTranslatableValue(Object value) {
        return value instanceof String || isStringList(value);
    }

    private void backupInvalidLanguage(File file) {
        File backupFolder = new File(
                new File(plugin.getDataFolder(), "language-backups"),
                LocalDateTime.now().format(BACKUP_TIMESTAMP)
        );
        try {
            Files.createDirectories(backupFolder.toPath());
            Files.copy(
                    file.toPath(),
                    new File(backupFolder, file.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to back up invalid language file " + file.getPath(), e);
        }
    }

    private YamlConfiguration loadBundledLanguage(String locale) {
        String resourcePath = "languages/" + locale + ".yml";
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        try (InputStream input = plugin.getResource(resourcePath)) {
            if (input == null) {
                plugin.getLogger().warning("Missing bundled language resource: " + resourcePath);
                return configuration;
            }
            configuration.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load bundled language " + resourcePath, e);
        }
        return configuration;
    }

    private void rebuildBuiltInTranslations() {
        builtInTranslations.clear();
        YamlConfiguration english = languages.get(DEFAULT_LOCALE);
        if (english == null) {
            return;
        }

        for (String path : english.getKeys(true)) {
            if (english.isConfigurationSection(path)) {
                continue;
            }
            Object source = english.get(path);
            Object translated = localizedValue(path);
            if (source instanceof String sourceText && translated instanceof String translatedText) {
                builtInTranslations.putIfAbsent(sourceText, translatedText);
            } else if (source instanceof List<?> sourceList && translated instanceof List<?> translatedList) {
                int size = Math.min(sourceList.size(), translatedList.size());
                for (int index = 0; index < size; index++) {
                    Object sourceLine = sourceList.get(index);
                    Object translatedLine = translatedList.get(index);
                    if (sourceLine instanceof String sourceText && translatedLine instanceof String translatedText) {
                        builtInTranslations.putIfAbsent(sourceText, translatedText);
                    }
                }
            }
        }
    }

    private Object localizedValue(String path) {
        YamlConfiguration active = languages.get(activeLocale);
        if (active != null && active.contains(path, true)) {
            return active.get(path);
        }
        YamlConfiguration fallback = languages.get(fallbackLocale);
        return fallback != null && fallback.contains(path, true) ? fallback.get(path) : null;
    }

    private String resolveAvailableLocale(String raw, String fallback, String role) {
        String resolved = resolveLocale(raw);
        if (resolved != null && languages.containsKey(resolved)) {
            return resolved;
        }
        if (raw != null) {
            String normalized = raw.trim()
                    .replace('-', '_')
                    .replace(' ', '_')
                    .toLowerCase(Locale.ROOT);
            for (String locale : languages.keySet()) {
                if (locale.equalsIgnoreCase(normalized) || locale.replace("_", "").equalsIgnoreCase(normalized.replace("_", ""))) {
                    return locale;
                }
            }
        }
        String warningKey = role + ":" + raw;
        if (warnedInvalidLocales.add(warningKey)) {
            plugin.getLogger().warning("Unknown " + role + " language '" + raw
                    + "'. falling back to " + fallback + ".");
        }
        return languages.containsKey(fallback) ? fallback : DEFAULT_LOCALE;
    }

    public static String resolveLocale(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toLowerCase(Locale.ROOT);
        String alias = LOCALE_ALIASES.get(normalized);
        if (alias != null) {
            return alias;
        }
        for (String locale : BUNDLED_LOCALES) {
            if (locale.equalsIgnoreCase(normalized)) {
                return locale;
            }
        }
        LanguageManager manager = current;
        if (manager != null) {
            for (String locale : manager.languages.keySet()) {
                if (locale.equalsIgnoreCase(normalized) || locale.replace("_", "").equalsIgnoreCase(normalized.replace("_", ""))) {
                    return locale;
                }
            }
        }
        return null;
    }

    private static Map<String, String> createLocaleAliases() {
        Map<String, String> aliases = new HashMap<>();
        alias(aliases, "en_US", "en", "english", "english_us", "american_english");
        alias(aliases, "es_ES", "es", "spanish", "espanol", "español", "spanyol");
        alias(aliases, "id_ID", "id", "indonesian", "indonesia", "bahasa", "bahasa_indonesia");
        alias(aliases, "pt_BR", "pt", "portuguese", "portuguese_brazil", "brazilian_portuguese");
        alias(aliases, "de_DE", "de", "german", "deutsch");
        alias(aliases, "fr_FR", "fr", "french", "francais", "français");
        alias(aliases, "ru_RU", "ru", "russian", "русский");
        alias(aliases, "zh_CN", "zh", "chinese", "simplified_chinese", "chinese_simplified", "mandarin");
        alias(aliases, "tr_TR", "tr", "turkish", "turkce", "türkçe");
        alias(aliases, "it_IT", "it", "italian", "italiano");
        alias(aliases, "pl_PL", "pl", "polish", "polski");
        alias(aliases, "nl_NL", "nl", "dutch", "nederlands");
        return Map.copyOf(aliases);
    }

    private static void alias(Map<String, String> aliases, String locale, String... values) {
        aliases.put(locale.toLowerCase(Locale.ROOT), locale);
        for (String value : values) {
            aliases.put(value.toLowerCase(Locale.ROOT), locale);
        }
    }

    static int mergeMissing(YamlConfiguration target, YamlConfiguration defaults) {
        int added = 0;
        for (String path : defaults.getKeys(true)) {
            if (!defaults.isConfigurationSection(path) && !target.contains(path, true)) {
                target.set(path, copyValue(defaults.get(path)));
                added++;
            }
        }
        return added;
    }

    private static void mergeSection(
            YamlConfiguration target,
            String targetRoot,
            ConfigurationSection source,
            boolean force
    ) {
        if (source == null) {
            return;
        }
        for (String path : source.getKeys(true)) {
            if (source.isConfigurationSection(path) || !isTranslatableValue(source.get(path))) {
                continue;
            }
            String targetPath = targetRoot + "." + path;
            if (force || !target.contains(targetPath, true)) {
                target.set(targetPath, copyValue(source.get(path)));
            }
        }
    }

    private static Object copyValue(Object value) {
        if (value instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        if (value instanceof Map<?, ?> map) {
            return new LinkedHashMap<>(map);
        }
        return value;
    }

    private static boolean isStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return false;
        }
        return list.stream().allMatch(String.class::isInstance);
    }

    private static String replace(String value, String... placeholders) {
        String resolved = value == null ? "" : value;
        if (placeholders != null) {
            for (int index = 0; index + 1 < placeholders.length; index += 2) {
                String key = placeholders[index];
                String val = placeholders[index + 1];
                resolved = resolved.replace(key, val);
                if (key.equals("{quantity}")) {
                    resolved = resolved.replace("{Quantity}", val);
                }
            }
        }
        return resolved;
    }

    static String selectTextValue(String localized, String legacyFallback, String javaFallback) {
        if (localized != null) {
            return localized;
        }
        return legacyFallback != null ? legacyFallback : javaFallback;
    }

    private static String normalizeDisplayKey(String value) {
        return value == null || value.isBlank()
                ? "UNKNOWN"
                : value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
    }
}
