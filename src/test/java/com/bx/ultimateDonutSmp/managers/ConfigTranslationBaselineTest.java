package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every feature yml and the bundled English catalogue have to say the same thing word for word.
 *
 * <p>{@code LanguageManager.customizedLegacyKeys} compares the two and reads any difference as an
 * admin who edited that key by hand, which makes {@code applyLanguageSection} leave it alone so the
 * edit survives. On a stock install nothing has been edited, so a key that differs only because the
 * two files were written at different times gets the same treatment: it is pinned to English on
 * every translated server, silently. One capital letter is enough, and 111 keys were stuck this way.
 *
 * <p>Only the roots whose {@code ConfigManager} getter runs through {@code localized(...)} are
 * listed. {@code getMessages()} is one of them: {@code messages.yml} is compared with
 * {@code MESSAGES} in the English catalogue, not with a {@code CONFIG.*} block. A root left off
 * this list can drift forever, and the first day it starts going through {@code localized(...)}
 * every stock install looks hand-edited.
 */
class ConfigTranslationBaselineTest {

    private static final Map<String, String> LOCALIZED_ROOTS = new LinkedHashMap<>();

    static {
        LOCALIZED_ROOTS.put("BILLFORD", "billford.yml");
        LOCALIZED_ROOTS.put("RTP", "rtp.yml");
        LOCALIZED_ROOTS.put("AMETHYST_TOOLS", "amethyst-tools.yml");
        LOCALIZED_ROOTS.put("ENDER_CHEST", "ender-chest.yml");
        LOCALIZED_ROOTS.put("INVSEE", "invsee.yml");
        LOCALIZED_ROOTS.put("FREEZE", "freeze.yml");
        LOCALIZED_ROOTS.put("AUCTION_HOUSE", "auction-house.yml");
        LOCALIZED_ROOTS.put("ORDERS", "orders.yml");
        LOCALIZED_ROOTS.put("DUELS", "duels.yml");
        LOCALIZED_ROOTS.put("FFA", "ffa.yml");
        LOCALIZED_ROOTS.put("PVP", "pvp.yml");
        LOCALIZED_ROOTS.put("CRATES", "crates.yml");
        LOCALIZED_ROOTS.put("SPAWNERS", "spawners.yml");
        LOCALIZED_ROOTS.put("SPAWN_STASH", "spawn-stash.yml");
        LOCALIZED_ROOTS.put("NETWORK", "network.yml");
        LOCALIZED_ROOTS.put("STAFF_MODE", "staff-mode.yml");
        LOCALIZED_ROOTS.put("SERVER_WIPE", "server-wipe.yml");
        LOCALIZED_ROOTS.put("WORTH", "worth.yml");
        LOCALIZED_ROOTS.put("HIDE", "hide.yml");
        LOCALIZED_ROOTS.put("ENCHANTMENTS", "enchantments.yml");
    }

    private static final List<String> MAINTENANCE_MESSAGES = List.of(
            "MAINTENANCE.MESSAGES.ENTERING",
            "MAINTENANCE.MESSAGES.BYPASS_JOIN",
            "MAINTENANCE.MESSAGES.NOT_ALLOWED",
            "MAINTENANCE.MESSAGES.KICK_FALLBACK",
            "MAINTENANCE.MESSAGES.RECONNECTING_TITLE",
            "MAINTENANCE.MESSAGES.RECONNECTING_SUBTITLE"
    );

    private static YamlConfiguration resource(String name) {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/" + name));
    }

    private static ConfigurationSection catalogue(String root) {
        ConfigurationSection section = resource("languages/en_US.yml").getConfigurationSection("CONFIG." + root);
        assertNotNull(section, "en_US.yml lost its CONFIG." + root + " section");
        return section;
    }

    private static int compare(String cataloguePath, String resourceName, List<String> drifted) {
        ConfigurationSection catalogue = resource("languages/en_US.yml").getConfigurationSection(cataloguePath);
        assertNotNull(catalogue, "en_US.yml lost its " + cataloguePath + " section");
        YamlConfiguration source = resource(resourceName);
        int compared = 0;
        for (String path : catalogue.getKeys(true)) {
            if (catalogue.isConfigurationSection(path) || !source.contains(path)) {
                continue;
            }
            compared++;
            if (!catalogue.get(path).equals(source.get(path))) {
                drifted.add(cataloguePath + "." + path
                        + "\n      " + resourceName + ": " + source.get(path)
                        + "\n      en_US.yml: " + catalogue.get(path));
            }
        }
        return compared;
    }

    @Test
    void noLocalizedConfigKeyIsPinnedToEnglishByADriftingCatalogue() {
        List<String> drifted = new ArrayList<>();
        int compared = 0;

        for (Map.Entry<String, String> entry : LOCALIZED_ROOTS.entrySet()) {
            compared += compare("CONFIG." + entry.getKey(), entry.getValue(), drifted);
        }
        // messages.yml is the stock text for /discord and the rest of getMessages().
        // A case change here is enough for customizedLegacyKeys to treat the key as a
        // hand edit and leave every language on the English file.
        compared += compare("MESSAGES", "messages.yml", drifted);
        // death-messages.yml is the stock text for getDeathMessages(). The same case
        // check applies: one capital letter pins the line to English on every language.
        compared += compare("DEATH_MESSAGES", "death-messages.yml", drifted);
        // menus.yml is the stock text for getMenus(). A capital, or a renamed button,
        // is enough to pin that line to the English file on every language.
        compared += compare("MENUS", "menus.yml", drifted);

        assertTrue(compared > 100, "expected the catalogue to share plenty of keys, saw " + compared);
        assertEquals(List.of(), drifted,
                "these keys differ from their feature yml, so no translation of them is ever applied");
    }

    @Test
    void stockSocialLinksMatchTheEnglishCatalogue() {
        YamlConfiguration messages = resource("messages.yml");
        ConfigurationSection social = resource("languages/en_US.yml").getConfigurationSection("MESSAGES.SOCIAL");
        assertNotNull(social, "en_US.yml lost MESSAGES.SOCIAL");

        for (String key : List.of("DISCORD", "TWITTER", "STORE")) {
            assertEquals(messages.getStringList("SOCIAL." + key), social.getStringList(key), key);
        }
    }

    @Test
    void theMaintenanceMessagesAreCoveredByThatCheck() {
        YamlConfiguration network = resource("network.yml");
        ConfigurationSection catalogue = catalogue("NETWORK");

        // #392 shipped this block with two words capitalised differently from the catalogue, which
        // stopped ENTERING and NOT_ALLOWED translating. Naming the paths keeps the sweep above from
        // quietly skipping them if either file is restructured.
        for (String path : MAINTENANCE_MESSAGES) {
            assertTrue(network.isString(path), path + " is missing from network.yml");
            assertTrue(catalogue.isString(path), path + " is missing from CONFIG.NETWORK in en_US.yml");
            assertEquals(network.getString(path), catalogue.getString(path), path + " drifted");
        }
    }

    @Test
    void technicalValuesStayOutOfTheCatalogueEntirely() {
        // Material names and a proxy server id are not text. They were translated anyway
        // (DIRT became Schmutz, and the server id became Lokal), so they are excluded from the
        // catalogue rather than kept in step with it.
        assertFalse(catalogue("AMETHYST_TOOLS").contains("AMETHYST-TOOLS.DRILL.DISABLED-BLOCKS"),
                "material names must not be translatable");
        assertFalse(catalogue("DUELS").contains("CROSS_SERVER.PROXY_SERVER_NAME"),
                "the cross-server proxy id must not be translatable");
    }
}
