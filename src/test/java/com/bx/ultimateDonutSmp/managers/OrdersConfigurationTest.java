package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdersConfigurationTest {

    @Test
    void donutStyleDefaultsAndOptionalIntegrationsParse() throws Exception {
        YamlConfiguration orders = new YamlConfiguration();
        orders.load(Path.of("src/main/resources/orders.yml").toFile());

        assertEquals("DEPOSIT_GUI", orders.getString("DELIVERY.MODE"));
        assertEquals(2304, orders.getInt("DELIVERY.MAX_DELIVER_PER_TRANSACTION"));
        assertEquals(54, orders.getInt("GUI.MAIN.SIZE"));
        assertEquals(45, orders.getInt("GUI.MAIN.ITEMS_PER_PAGE"));
        assertEquals(27, orders.getInt("GUI.MY_ORDERS.SIZE"));
        assertEquals(26, orders.getInt("GUI.MY_ORDERS.BUTTONS.NEW.SLOT"));
        assertEquals(35, orders.getInt("GUI.DELIVERY_DEPOSIT.BUTTONS.CONFIRM.SLOT"));
        assertTrue(orders.getBoolean("BEDROCK.ENABLED"));
        assertTrue(orders.getBoolean("NETWORK.ENABLED"));
        assertEquals("ultimate-donut-smp:orders", orders.getString("NETWORK.REDIS_CHANNEL"));
    }

    @Test
    void testRootLevelFallback() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(
                "BOTS:\n" +
                "  ENABLED: true\n" +
                "ITEMS:\n" +
                "  - MATERIAL: COBBLESTONE\n" +
                "    MIN_AMOUNT: 64\n"
        );
        org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection("BOTS");
        java.util.List<java.util.Map<?, ?>> itemsList = section.getMapList("ITEMS");
        if (itemsList == null || itemsList.isEmpty()) {
            itemsList = config.getMapList("ITEMS");
        }
        assertEquals(1, itemsList.size());
        assertEquals("COBBLESTONE", itemsList.get(0).get("MATERIAL"));
    }

    @Test
    void everyBundledLocaleProvidesOrdersOverridesAndEnglishFallbackCompletesIt() throws Exception {
        YamlConfiguration english = load("en_US");
        for (String locale : List.of("en_US", "id_ID", "de_DE", "es_ES", "fr_FR", "pt_BR", "ru_RU", "zh_CN")) {
            YamlConfiguration language = load(locale);
            assertTrue(language.isConfigurationSection("ORDERS"), locale);
            LanguageManager.mergeMissing(language, english);
            assertTrue(language.isString("ORDERS.GUI.MAIN.TITLE"), locale);
            assertTrue(language.isString("ORDERS.BEDROCK.CANCEL.CONTENT"), locale);
            assertTrue(language.isList("ORDERS.GUI.CONFIRM.SUMMARY.LORE"), locale);
        }
    }

    private static YamlConfiguration load(String locale) throws Exception {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(Path.of("src/main/resources/languages", locale + ".yml").toFile());
        return configuration;
    }
}
