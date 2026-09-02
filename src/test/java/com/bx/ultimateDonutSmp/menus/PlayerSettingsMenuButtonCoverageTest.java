package com.bx.ultimateDonutSmp.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * PlayerSettingsMenu#shouldRenderButton drops any key missing from VALID_SETTINGS, and a key with
 * no entry under SETTINGS-MENU.BUTTONS has nothing to draw. Either half going missing leaves a
 * setting that players can never reach even though its click handler and stored flag still work,
 * which is how the RTP coordinates toggle stopped being reachable.
 */
class PlayerSettingsMenuButtonCoverageTest {

    @Test
    void everySettingsButtonIsRenderableAndEveryRenderableSettingHasAButton() throws Exception {
        Set<String> buttons = bundledButtons();
        Set<String> renderable = validSettings();

        assertEquals(renderable, buttons,
                "SETTINGS-MENU.BUTTONS and VALID_SETTINGS must list the same keys");
    }

    /**
     * Buttons carrying COMMAND or STATUS-PLACEHOLDER are admin-authored redirects to other plugins.
     * shouldRenderButton lets those through without a VALID_SETTINGS entry, so they are not part of
     * the comparison above. The bundled file ships none of them.
     */
    private static Set<String> bundledButtons() throws Exception {
        YamlConfiguration menus = new YamlConfiguration();
        menus.load(Path.of("src/main/resources", "menus.yml").toFile());

        ConfigurationSection buttons = menus.getConfigurationSection("SETTINGS-MENU.BUTTONS");
        assertNotNull(buttons);

        Set<String> keys = new TreeSet<>();
        for (String key : buttons.getKeys(false)) {
            ConfigurationSection section = buttons.getConfigurationSection(key);
            if (section != null && (section.contains("COMMAND") || section.contains("STATUS-PLACEHOLDER"))) {
                continue;
            }
            keys.add(key);
        }
        return keys;
    }

    @SuppressWarnings("unchecked")
    private static Set<String> validSettings() throws Exception {
        Field field = PlayerSettingsMenu.class.getDeclaredField("VALID_SETTINGS");
        field.setAccessible(true);
        return new TreeSet<>((Set<String>) field.get(null));
    }
}
