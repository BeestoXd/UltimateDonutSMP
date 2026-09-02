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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * ConfigManager.getRtp() hands back LanguageManager.localize("CONFIG.RTP", rtp), which is a freshly
 * built copy whenever the active or fallback language file carries a CONFIG.RTP section - and every
 * bundled language file does. saveRtp() writes the raw field, so a set() through getRtp() is dropped
 * and the command that made it still reports success.
 */
class LocalizedConfigWriteTest {

    private static final Path CUBOID_COMMAND =
            Path.of("src/main/java/com/bx/ultimateDonutSmp/commands/CuboidCommand.java");

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

    @Test
    void cuboidCommandDoesNotBindTheRtpQueueThroughTheLocalizedGetter() throws IOException {
        String source = Files.readString(CUBOID_COMMAND);

        assertFalse(source.contains("getRtp().set("),
                "CuboidCommand has to write QUEUE.CUBOID through the raw accessor saveRtp() persists,"
                        + " otherwise the bind is silently dropped and the command still reports success");
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
