package com.bx.ultimateDonutSmp.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RanksMenu renders straight off menus.yml, so a bad slot, a missing material, or a button the
 * player cannot see is a config problem the bundled defaults have to avoid.
 */
class RanksMenuTest {

    private static final String MENU_PATH = "RANKS-MENU";
    private static final String BUTTONS_PATH = MENU_PATH + ".BUTTONS";

    @Test
    void bundledMenuShipsATitleAndAUsableSize() throws Exception {
        YamlConfiguration menus = loadMenus();

        assertFalse(
                menus.getString(MENU_PATH + ".TITLE", "").isBlank(),
                "menus.yml must ship " + MENU_PATH + ".TITLE or the menu opens with an empty title"
        );

        int size = menus.getInt(MENU_PATH + ".SIZE", -1);
        assertTrue(size >= 9 && size <= 54 && size % 9 == 0,
                MENU_PATH + ".SIZE must be a multiple of 9 between 9 and 54 but was " + size);
    }

    @Test
    void bundledButtonsAreRenderable() throws Exception {
        YamlConfiguration menus = loadMenus();
        int size = menus.getInt(MENU_PATH + ".SIZE", 27);
        ConfigurationSection buttons = menus.getConfigurationSection(BUTTONS_PATH);
        assertNotNull(buttons, "menus.yml must ship " + BUTTONS_PATH + " or the menu renders a barrier");

        List<Integer> usedSlots = new ArrayList<>();
        for (String key : buttons.getKeys(false)) {
            ConfigurationSection button = buttons.getConfigurationSection(key);
            assertNotNull(button, BUTTONS_PATH + "." + key + " must be a section");

            int slot = button.getInt("SLOT", -1);
            assertTrue(slot >= 0 && slot < size,
                    button.getCurrentPath() + " slot " + slot + " is outside menu size " + size);
            assertFalse(usedSlots.contains(slot),
                    button.getCurrentPath() + " reuses slot " + slot + ", so one button never renders");
            usedSlots.add(slot);

            String material = button.getString("MATERIAL", "");
            assertFalse(material.isBlank(), button.getCurrentPath() + " must ship a MATERIAL");
            assertNotNull(org.bukkit.Material.getMaterial(material.trim().toUpperCase(java.util.Locale.ROOT)),
                    button.getCurrentPath() + " material '" + material + "' is not a valid material");

            assertFalse(button.getString("DISPLAY-NAME", "").isBlank(),
                    button.getCurrentPath() + " must ship a DISPLAY-NAME");
            assertFalse(button.getStringList("LORE").isEmpty(),
                    button.getCurrentPath() + " must ship LORE or the rank lists no perks");
        }

        assertFalse(usedSlots.isEmpty(), BUTTONS_PATH + " must ship at least one button");
    }

    @Test
    void blankCommandsStayInformational() {
        assertNull(RanksMenu.sanitizeCommand(null));
        assertNull(RanksMenu.sanitizeCommand(""));
        assertNull(RanksMenu.sanitizeCommand("   "));
        assertNull(RanksMenu.sanitizeCommand("/"));
        assertEquals("shop", RanksMenu.sanitizeCommand("/shop"));
        assertEquals("shop ranks", RanksMenu.sanitizeCommand("  shop ranks  "));
    }

    @Test
    void buttonKeysFallBackToAReadableName() {
        assertEquals("Donut Plus Plus", RanksMenu.prettifyKey("DONUT_PLUS_PLUS"));
        assertEquals("Default", RanksMenu.prettifyKey("DEFAULT"));
        assertEquals("Rank", RanksMenu.prettifyKey("_"));
    }

    @Test
    void commandAndPermissionAreRegistered() throws Exception {
        YamlConfiguration plugin = new YamlConfiguration();
        plugin.load(Path.of("src/main/resources/plugin.yml").toFile());

        assertTrue(plugin.isConfigurationSection("commands.ranks"),
                "plugin.yml must declare the ranks command or the executor never binds");
        assertEquals("ultimatedonutsmp.command.ranks", plugin.getString("commands.ranks.permission"));
        assertTrue(plugin.isConfigurationSection("permissions.ultimatedonutsmp.command.ranks"),
                "plugin.yml must declare the ranks permission node");

        YamlConfiguration config = new YamlConfiguration();
        config.options().parseComments(true);
        config.load(Path.of("src/main/resources/config.yml").toFile());
        assertTrue(config.contains("COMMANDS.RANKS"),
                "config.yml must ship COMMANDS.RANKS so the feature toggle can disable the menu");
    }

    private static YamlConfiguration loadMenus() throws Exception {
        YamlConfiguration menus = new YamlConfiguration();
        menus.options().parseComments(true);
        menus.load(Path.of("src/main/resources/menus.yml").toFile());
        return menus;
    }
}
