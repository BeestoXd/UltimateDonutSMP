package com.bx.ultimateDonutSmp.menus;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorthMenuTest {

    private static final Pattern PLACEHOLDER = Pattern.compile("[{][a-z_]+[}]");

    // mirrors the substitutions WorthMenu#replaceItemPlaceholders performs
    private static final Set<String> SUPPORTED_PLACEHOLDERS = Set.of(
            "{item}",
            "{category}",
            "{unit_price}",
            "{unit_price_formatted}",
            "{unit_price_compact}",
            "{stack_size}",
            "{stack_price}",
            "{stack_price_formatted}",
            "{stack_price_compact}"
    );

    @Test
    void calculatesConfiguredMaterialStackTotals() {
        assertEquals(640D, WorthMenu.calculateStackPrice(10D, 64));
        assertEquals(160D, WorthMenu.calculateStackPrice(10D, 16));
        assertEquals(10D, WorthMenu.calculateStackPrice(10D, 1));
    }

    @Test
    void browserItemsShipNameAndPricedLore() throws Exception {
        YamlConfiguration worth = loadWorth();

        assertFalse(
                worth.getString("BROWSER.ITEM.NAME", "").isBlank(),
                "worth.yml must ship BROWSER.ITEM.NAME or browser items render without a name"
        );

        List<String> lore = worth.getStringList("BROWSER.ITEM.LORE");
        assertFalse(
                lore.isEmpty(),
                "worth.yml must ship BROWSER.ITEM.LORE or browser items render with no price in their tooltip"
        );
        assertTrue(
                lore.stream().anyMatch(line -> line.contains("{unit_price")),
                "BROWSER.ITEM.LORE must show a unit price"
        );
        assertTrue(
                lore.stream().anyMatch(line -> line.contains("{stack_price")),
                "BROWSER.ITEM.LORE must show a stack price"
        );
    }

    @Test
    void browserItemTextOnlyUsesPlaceholdersTheMenuReplaces() throws Exception {
        YamlConfiguration worth = loadWorth();

        List<String> text = new ArrayList<>(worth.getStringList("BROWSER.ITEM.LORE"));
        text.add(worth.getString("BROWSER.ITEM.NAME", ""));

        for (String line : text) {
            Matcher matcher = PLACEHOLDER.matcher(line);
            while (matcher.find()) {
                assertTrue(
                        SUPPORTED_PLACEHOLDERS.contains(matcher.group()),
                        "BROWSER.ITEM text uses " + matcher.group() + ", which WorthMenu never replaces"
                );
            }
        }
    }

    private static YamlConfiguration loadWorth() throws Exception {
        YamlConfiguration worth = new YamlConfiguration();
        worth.options().parseComments(true);
        worth.load(Path.of("src/main/resources/worth.yml").toFile());
        return worth;
    }
}
