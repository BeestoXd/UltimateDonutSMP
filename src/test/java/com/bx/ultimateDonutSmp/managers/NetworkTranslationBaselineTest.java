package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * network.yml and the bundled English catalogue have to say the same thing word for word.
 *
 * <p>{@code LanguageManager.customizedLegacyKeys} compares the two and reads any difference as an
 * admin who edited that key by hand, which makes {@code applyLanguageSection} leave it alone so the
 * edit survives. On a stock install nothing has been edited, so a key that differs only because the
 * two files were written at different times gets the same treatment: it is pinned to English on
 * every translated server, silently. One capital letter is enough to do it.
 */
class NetworkTranslationBaselineTest {

    private static final List<String> MAINTENANCE_MESSAGES = List.of(
            "MAINTENANCE.MESSAGES.ENTERING",
            "MAINTENANCE.MESSAGES.BYPASS_JOIN",
            "MAINTENANCE.MESSAGES.NOT_ALLOWED",
            "MAINTENANCE.MESSAGES.KICK_FALLBACK",
            "MAINTENANCE.MESSAGES.RECONNECTING_TITLE",
            "MAINTENANCE.MESSAGES.RECONNECTING_SUBTITLE"
    );

    private static YamlConfiguration network() {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/network.yml"));
    }

    private static ConfigurationSection englishCatalogue() {
        YamlConfiguration english =
                YamlConfiguration.loadConfiguration(new File("src/main/resources/languages/en_US.yml"));
        ConfigurationSection catalogue = english.getConfigurationSection("CONFIG.NETWORK");
        assertNotNull(catalogue, "en_US.yml lost its CONFIG.NETWORK section");
        return catalogue;
    }

    @Test
    void nothingInNetworkYmlIsPinnedToEnglishByADriftingCatalogue() {
        YamlConfiguration network = network();
        ConfigurationSection catalogue = englishCatalogue();

        List<String> drifted = new ArrayList<>();
        int compared = 0;
        for (String path : catalogue.getKeys(true)) {
            if (catalogue.isConfigurationSection(path) || !network.contains(path)) {
                continue;
            }
            compared++;
            if (!catalogue.get(path).equals(network.get(path))) {
                drifted.add(path
                        + "\n      network.yml: " + network.get(path)
                        + "\n      en_US.yml  : " + catalogue.get(path));
            }
        }

        assertTrue(compared > 0, "the two files stopped sharing any key, so this check covers nothing");
        assertEquals(List.of(), drifted,
                "these keys differ between network.yml and en_US.yml, so no translation of them is ever applied");
    }

    @Test
    void theMaintenanceMessagesAreCoveredByThatCheck() {
        YamlConfiguration network = network();
        ConfigurationSection catalogue = englishCatalogue();

        // #392 added this block to network.yml with two words capitalised differently from the
        // catalogue, which stopped ENTERING and NOT_ALLOWED translating at all. Naming the paths
        // here keeps the sweep above from quietly skipping them if either file is restructured.
        for (String path : MAINTENANCE_MESSAGES) {
            assertTrue(network.isString(path), path + " is missing from network.yml");
            assertTrue(catalogue.isString(path), path + " is missing from CONFIG.NETWORK in en_US.yml");
            assertEquals(network.getString(path), catalogue.getString(path), path + " drifted");
        }
    }
}
