package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ConfigManager.getRtp() hands back LanguageManager.localize("CONFIG.RTP", rtp), which is a freshly
 * built copy whenever the active or fallback language file carries a CONFIG.RTP section - and every
 * bundled language file does. saveRtp() writes the raw field, so a set() through getRtp() is dropped
 * and the command that made it still reports success.
 */
class LocalizedConfigWriteTest {

    private static final Path CONFIG_MANAGER =
            Path.of("src/main/java/com/bx/ultimateDonutSmp/managers/ConfigManager.java");

    @Test
    void aWriteThroughGetRtpNeverReachesTheConfigurationSaveRtpPersists() throws Exception {
        UltimateDonutSmp plugin = newPlugin();
        YamlConfiguration rtp = (YamlConfiguration) rawRtp(plugin);

        plugin.getConfigManager().getRtp().set("QUEUE.CUBOID", "arena");

        assertEquals("", rtp.getString("QUEUE.CUBOID"),
                "getRtp() is a localized copy on every install, which is why writers must not use it");
    }

    @Test
    void getOriginalRtpIsTheConfigurationSaveRtpPersists() throws Exception {
        UltimateDonutSmp plugin = newPlugin();

        plugin.getConfigManager().getOriginalRtp().set("QUEUE.CUBOID", "arena");

        assertEquals("arena", rawRtp(plugin).getString("QUEUE.CUBOID"),
                "the accessor the bind uses has to be the field saveRtp() writes");
    }

    /**
     * Both known instances of this bug wrote through a different getter in a different file, so the
     * guard covers every localized getter rather than the two that were caught: #350 for
     * CuboidCommand and getRtp(), #376 for UltimateDonutSmpCommand and getNetwork().
     */
    @Test
    void noSourceWritesThroughALocalizedConfigurationGetter() throws IOException {
        Set<String> getters = localizedGetterNames();
        assertFalse(getters.isEmpty(), "the localized getters have to be discoverable from ConfigManager");

        Set<String> offenders = new TreeSet<>();
        for (Path source : javaSources()) {
            String text = Files.readString(source);
            String file = source.getFileName().toString();
            for (String getter : getters) {
                if (text.contains(getter + "().set(")) {
                    offenders.add(file + ": " + getter + "().set(");
                }

                // The #376 shape: the copy is parked in a local first, then written several lines down.
                Matcher assigned = Pattern
                        .compile("(?:FileConfiguration|var)\\s+(\\w+)\\s*=[^;]*\\b" + getter + "\\(\\)")
                        .matcher(text);
                while (assigned.find()) {
                    String local = assigned.group(1);
                    // Only within the method holding the assignment. These names are reused across
                    // methods in the same file, where the write is usually the getOriginal* one.
                    String scope = restOfEnclosingMethod(text, assigned.end());
                    if (Pattern.compile("\\b" + local + "\\s*\\.set\\(").matcher(scope).find()) {
                        offenders.add(file + ": " + local + " = " + getter + "() then " + local + ".set(");
                    }
                }
            }
        }

        assertTrue(offenders.isEmpty(),
                "a localized getter returns a copy that no save() persists, so a write through one is"
                        + " dropped while the command still reports success: " + offenders);
    }

    /**
     * Everything from {@code from} up to the brace that closes the method containing it.
     */
    private static String restOfEnclosingMethod(String text, int from) {
        int depth = 0;
        for (int index = from; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '{') {
                depth++;
            } else if (character == '}') {
                if (depth == 0) {
                    return text.substring(from, index);
                }
                depth--;
            }
        }
        return text.substring(from);
    }

    private static Set<String> localizedGetterNames() throws IOException {
        Matcher matcher = Pattern
                .compile("public FileConfiguration (\\w+)\\(\\)\\s*\\{\\s*return localized\\(")
                .matcher(Files.readString(CONFIG_MANAGER));
        Set<String> names = new TreeSet<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private static List<Path> javaSources() throws IOException {
        try (Stream<Path> paths = Files.walk(Path.of("src/main/java"))) {
            return paths.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    private static FileConfiguration rawRtp(UltimateDonutSmp plugin) throws Exception {
        Field field = ConfigManager.class.getDeclaredField("rtp");
        field.setAccessible(true);
        return (FileConfiguration) field.get(plugin.getConfigManager());
    }

    private static UltimateDonutSmp newPlugin() throws Exception {
        UltimateDonutSmp plugin = allocate(UltimateDonutSmp.class);

        YamlConfiguration language = new YamlConfiguration();
        // Any string under CONFIG.RTP makes it a section, which is all localize() checks for.
        language.set("CONFIG.RTP.MESSAGES.DISABLED", "&cRTP is disabled.");

        LanguageManager languageManager = allocate(LanguageManager.class);
        Map<String, YamlConfiguration> languages = new LinkedHashMap<>();
        languages.put("en_US", language);
        set(LanguageManager.class, languageManager, "languages", languages);
        set(LanguageManager.class, languageManager, "bundledLanguages", new LinkedHashMap<String, YamlConfiguration>());
        set(LanguageManager.class, languageManager, "localizedConfigurations",
                new IdentityHashMap<FileConfiguration, Map<String, FileConfiguration>>());
        set(LanguageManager.class, languageManager, "activeLocale", "en_US");
        set(LanguageManager.class, languageManager, "fallbackLocale", "en_US");
        set(UltimateDonutSmp.class, plugin, "languageManager", languageManager);

        YamlConfiguration rtp = new YamlConfiguration();
        rtp.set("QUEUE.CUBOID", "");

        ConfigManager configManager = new ConfigManager(plugin);
        set(ConfigManager.class, configManager, "rtp", rtp);
        set(UltimateDonutSmp.class, plugin, "configManager", configManager);
        return plugin;
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        Constructor<?> constructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(type, objectConstructor);
        return (T) constructor.newInstance();
    }

    private static void set(Class<?> owner, Object instance, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }
}
