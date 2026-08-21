package com.bx.ultimateDonutSmp.menus;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These menus read their lore straight off the bundled config with no java fallback, so a missing
 * key renders the buttons with no lore at all rather than falling back to something readable.
 */
class MenuLoreDefaultsTest {

    private static final Pattern PLACEHOLDER = Pattern.compile("[{][a-z_]+[}]");

    private static final List<String> RTP_BUTTONS = List.of("OVERWORLD", "NETHER", "THE_END");

    // mirrors RTPMenu#replacePlaceholders
    private static final Set<String> RTP_PLACEHOLDERS = Set.of(
            "{players}",
            "{ping}",
            "{world}",
            "{min_radius}",
            "{max_radius}",
            "{required_playtime}",
            "{cooldown}",
            "{status}"
    );

    // mirrors ShopMenu#replaceShopGuiPlaceholders
    private static final Set<String> SHOP_PLACEHOLDERS = Set.of(
            "{shop_price}",
            "{shop_unit_price}",
            "{auction_line}",
            "{auction_action}",
            "{favorite_line}",
            "{favorite_action}",
            "{item}"
    );

    @Test
    void rtpMenuButtonsShipLore() throws Exception {
        YamlConfiguration rtp = load("rtp.yml");
        for (String button : RTP_BUTTONS) {
            String path = "RTP-MENU.BUTTONS." + button + ".LORE";
            assertFalse(
                    rtp.getStringList(path).isEmpty(),
                    "rtp.yml must ship " + path + " or the button renders with no lore"
            );
        }
    }

    @Test
    void rtpMenuLoreOnlyUsesPlaceholdersTheMenuReplaces() throws Exception {
        YamlConfiguration rtp = load("rtp.yml");
        for (String button : RTP_BUTTONS) {
            String path = "RTP-MENU.BUTTONS." + button + ".LORE";
            assertPlaceholdersSupported(rtp.getStringList(path), RTP_PLACEHOLDERS, path);
        }
    }

    @Test
    void shopItemsShipLore() throws Exception {
        YamlConfiguration shop = load("shop.yml");
        List<String> lore = shop.getStringList("SHOP-GUI.ITEM.LORE");
        assertFalse(
                lore.isEmpty(),
                "shop.yml must ship SHOP-GUI.ITEM.LORE or shop items render without a price"
        );
        assertTrue(
                lore.stream().anyMatch(line -> line.contains("{shop_price}")),
                "SHOP-GUI.ITEM.LORE must show the shop price"
        );
    }

    @Test
    void shopItemLoreOnlyUsesPlaceholdersTheMenuReplaces() throws Exception {
        YamlConfiguration shop = load("shop.yml");
        assertPlaceholdersSupported(
                shop.getStringList("SHOP-GUI.ITEM.LORE"),
                SHOP_PLACEHOLDERS,
                "SHOP-GUI.ITEM.LORE"
        );
    }

    private static void assertPlaceholdersSupported(List<String> lore, Set<String> supported, String path) {
        for (String line : lore) {
            Matcher matcher = PLACEHOLDER.matcher(line);
            while (matcher.find()) {
                assertTrue(
                        supported.contains(matcher.group()),
                        path + " uses " + matcher.group() + ", which the menu never replaces"
                );
            }
        }
    }

    private static YamlConfiguration load(String name) throws Exception {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().parseComments(true);
        configuration.load(Path.of("src/main/resources", name).toFile());
        return configuration;
    }
}
