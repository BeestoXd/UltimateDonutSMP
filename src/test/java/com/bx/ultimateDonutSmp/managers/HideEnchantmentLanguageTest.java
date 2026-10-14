package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Hide messages and the enchantment picker used to read hide.yml and enchantments.yml directly,
 * so LANGUAGE.ACTIVE never reached them. Worth and freeze already go through localize(), which
 * keeps a line the admin actually changed.
 */
class HideEnchantmentLanguageTest {

    @Test
    void activeLocaleReachesHideMessagesAndTheEnchantmentPicker() throws Exception {
        PluginFixture fixture = fixture();
        YamlConfiguration hide = stockHide();
        YamlConfiguration enchantments = stockEnchantments();
        set(ConfigManager.class, fixture.configManager, "hide", hide);
        set(ConfigManager.class, fixture.configManager, "enchantments", enchantments);

        FileConfiguration localizedHide = fixture.configManager.getHide();
        FileConfiguration localizedEnchantments = fixture.configManager.getEnchantments();

        assertEquals("&cFitur sembunyi sedang dimatikan.", localizedHide.getString("MESSAGES.DISABLED"));
        assertEquals("&8sembunyi", localizedHide.getString("GUI.MAIN.TITLE"));
        assertEquals("&#444444pilih enchant", localizedEnchantments.getString("gui.title"));
        assertEquals("&fKlik untuk memilih", localizedEnchantments.getString("messages.select"));
        assertEquals("&cthe hide feature is currently disabled.", hide.getString("MESSAGES.DISABLED"));
        assertEquals("&#444444pick enchantments", enchantments.getString("gui.title"));
    }

    @Test
    void anEditedHideOrEnchantmentLineBeatsTheTranslation() throws Exception {
        PluginFixture fixture = fixture();
        YamlConfiguration hide = stockHide();
        hide.set("MESSAGES.DISABLED", "&cHide is closed tonight.");
        YamlConfiguration enchantments = stockEnchantments();
        enchantments.set("gui.title", "&#444444our enchants");
        set(ConfigManager.class, fixture.configManager, "hide", hide);
        set(ConfigManager.class, fixture.configManager, "enchantments", enchantments);

        FileConfiguration localizedHide = fixture.configManager.getHide();
        FileConfiguration localizedEnchantments = fixture.configManager.getEnchantments();

        assertEquals("&cHide is closed tonight.", localizedHide.getString("MESSAGES.DISABLED"));
        assertEquals("&8sembunyi", localizedHide.getString("GUI.MAIN.TITLE"));
        assertEquals("&#444444our enchants", localizedEnchantments.getString("gui.title"));
        assertEquals("&fKlik untuk memilih", localizedEnchantments.getString("messages.select"));
    }

    @Test
    void namesSlotsAndEnchantmentOptionsStayOnTheFeatureFile() throws Exception {
        PluginFixture fixture = fixture();
        YamlConfiguration hide = stockHide();
        YamlConfiguration enchantments = stockEnchantments();
        set(ConfigManager.class, fixture.configManager, "hide", hide);
        set(ConfigManager.class, fixture.configManager, "enchantments", enchantments);

        FileConfiguration localizedHide = fixture.configManager.getHide();
        FileConfiguration localizedEnchantments = fixture.configManager.getEnchantments();

        assertEquals(true, localizedHide.getBoolean("ENABLED"));
        assertEquals(30, localizedHide.getInt("COOLDOWN-SECONDS"));
        assertEquals("Dream", localizedHide.getString("ALIASES.dream.NAME"));
        assertEquals("&8[&cH&8] ", localizedHide.getString("STAFF-MARKER"));
        assertEquals(6, localizedEnchantments.getInt("gui.rows"));
        assertEquals(52, localizedEnchantments.getInt("gui.slots.confirm"));
        assertEquals("protection;1", localizedEnchantments.getString("helmet.protection1.enchantment"));
    }

    private static YamlConfiguration stockHide() {
        YamlConfiguration hide = new YamlConfiguration();
        hide.set("ENABLED", true);
        hide.set("COOLDOWN-SECONDS", 30);
        hide.set("STAFF-MARKER", "&8[&cH&8] ");
        hide.set("ALIASES.dream.NAME", "Dream");
        hide.set("GUI.MAIN.TITLE", "&8hide");
        hide.set("MESSAGES.DISABLED", "&cthe hide feature is currently disabled.");
        return hide;
    }

    private static YamlConfiguration stockEnchantments() {
        YamlConfiguration enchantments = new YamlConfiguration();
        enchantments.set("messages.select", "&fClick to select");
        enchantments.set("gui.title", "&#444444pick enchantments");
        enchantments.set("gui.rows", 6);
        enchantments.set("gui.slots.confirm", 52);
        enchantments.set("helmet.protection1.enchantment", "protection;1");
        return enchantments;
    }

    private static PluginFixture fixture() throws Exception {
        UltimateDonutSmp plugin = allocate(UltimateDonutSmp.class);
        LanguageManager languageManager = new LanguageManager(null);
        YamlConfiguration english = new YamlConfiguration();
        english.set("CONFIG.HIDE.GUI.MAIN.TITLE", "&8hide");
        english.set("CONFIG.HIDE.MESSAGES.DISABLED", "&cthe hide feature is currently disabled.");
        english.set("CONFIG.ENCHANTMENTS.gui.title", "&#444444pick enchantments");
        english.set("CONFIG.ENCHANTMENTS.messages.select", "&fClick to select");
        YamlConfiguration indonesian = new YamlConfiguration();
        indonesian.set("CONFIG.HIDE.GUI.MAIN.TITLE", "&8sembunyi");
        indonesian.set("CONFIG.HIDE.MESSAGES.DISABLED", "&cFitur sembunyi sedang dimatikan.");
        indonesian.set("CONFIG.ENCHANTMENTS.gui.title", "&#444444pilih enchant");
        indonesian.set("CONFIG.ENCHANTMENTS.messages.select", "&fKlik untuk memilih");

        Map<String, YamlConfiguration> languages = new LinkedHashMap<>();
        languages.put("en_US", english);
        languages.put("id_ID", indonesian);
        Map<String, YamlConfiguration> bundled = new LinkedHashMap<>();
        bundled.put("en_us", english);
        set(LanguageManager.class, languageManager, "languages", languages);
        set(LanguageManager.class, languageManager, "bundledLanguages", bundled);
        set(LanguageManager.class, languageManager, "localizedConfigurations",
                new IdentityHashMap<FileConfiguration, Map<String, FileConfiguration>>());
        set(LanguageManager.class, languageManager, "activeLocale", "id_ID");
        set(LanguageManager.class, languageManager, "fallbackLocale", "en_US");
        set(UltimateDonutSmp.class, plugin, "languageManager", languageManager);

        ConfigManager configManager = new ConfigManager(plugin);
        set(UltimateDonutSmp.class, plugin, "configManager", configManager);
        return new PluginFixture(configManager);
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

    private record PluginFixture(ConfigManager configManager) {
    }
}
