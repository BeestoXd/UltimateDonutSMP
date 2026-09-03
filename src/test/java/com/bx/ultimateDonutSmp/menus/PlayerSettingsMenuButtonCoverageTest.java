package com.bx.ultimateDonutSmp.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A setting reaches a player through three lists that have to agree: a {@code case} label in
 * PlayerSettingsMenu to act on the click, an entry in VALID_SETTINGS because shouldRenderButton
 * drops anything else, and a block under SETTINGS-MENU.BUTTONS to draw. Drop any one of them and
 * the setting silently stops being reachable while its stored flag carries on driving behaviour.
 *
 * <p>That is not hypothetical. A settings layout rebuild left nine keys holding a handler and a
 * live flag with no button and no VALID_SETTINGS entry, and nothing noticed until a player asked
 * why they could not turn the RTP coordinates message off.</p>
 */
class PlayerSettingsMenuButtonCoverageTest {

    private static final Path MENU_SOURCE = Path.of(
            "src", "main", "java", "com", "bx", "ultimateDonutSmp", "menus", "PlayerSettingsMenu.java");

    /** A {@code case} arm, including multi-label arms such as {@code case "A", "B" ->}. */
    private static final Pattern CASE_ARM =
            Pattern.compile("case\\s+((?:\"[A-Z_]+\"\\s*,\\s*)*\"[A-Z_]+\")\\s*->");

    private static final Pattern QUOTED_LABEL = Pattern.compile("\"([A-Z_]+)\"");

    @Test
    void everySettingsButtonIsRenderableAndEveryRenderableSettingHasAButton() throws Exception {
        Set<String> buttons = bundledButtons();
        Set<String> renderable = validSettings();

        assertEquals(renderable, buttons,
                "SETTINGS-MENU.BUTTONS and VALID_SETTINGS must list the same keys");
    }

    @Test
    void everyHandledSettingIsRenderableAndEveryRenderableSettingIsHandled() throws Exception {
        assertEquals(validSettings(), handledSettings(),
                "every case label in PlayerSettingsMenu must appear in VALID_SETTINGS and back");
    }

    /**
     * Both switches are read together, so a key handled on the click side but missing from the
     * status side still has to be in VALID_SETTINGS. Enum arms carry no quotes and are skipped.
     */
    private static Set<String> handledSettings() throws Exception {
        String source = Files.readString(MENU_SOURCE, StandardCharsets.UTF_8);
        Set<String> labels = new TreeSet<>();
        Matcher arms = CASE_ARM.matcher(source);
        while (arms.find()) {
            Matcher label = QUOTED_LABEL.matcher(arms.group(1));
            while (label.find()) {
                labels.add(label.group(1));
            }
        }
        assertFalse(labels.isEmpty(),
                "found no case labels in " + MENU_SOURCE + "; is the working directory the module root?");
        return labels;
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
