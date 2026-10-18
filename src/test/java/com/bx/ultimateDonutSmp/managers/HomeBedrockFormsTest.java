package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bedrock /home used to open a Floodgate form on every server. The chest is the menu now,
 * and the form only comes back when HOME-MENU.BEDROCK-FORMS is set.
 */
class HomeBedrockFormsTest {

    @Test
    void aMissingSwitchKeepsTheChest() {
        assertFalse(HomeBedrockManager.formsRequested(null));
        assertFalse(HomeBedrockManager.formsRequested(new YamlConfiguration()));
    }

    @Test
    void theSwitchHasToBeTurnedOn() {
        YamlConfiguration menus = new YamlConfiguration();
        menus.set("HOME-MENU.BEDROCK-FORMS", false);
        assertFalse(HomeBedrockManager.formsRequested(menus));

        menus.set("HOME-MENU.BEDROCK-FORMS", true);
        assertTrue(HomeBedrockManager.formsRequested(menus));
    }

    @Test
    void theShippedMenuUsesTheChest() {
        YamlConfiguration menus = YamlConfiguration.loadConfiguration(new File("src/main/resources/menus.yml"));

        assertTrue(menus.contains("HOME-MENU.BEDROCK-FORMS"));
        assertFalse(menus.getBoolean("HOME-MENU.BEDROCK-FORMS"));
    }

    @Test
    void chestClicksDoNotOpenAFormOnTheirOwn() throws Exception {
        String source = Files.readString(
                Path.of("src/main/java/com/bx/ultimateDonutSmp/menus/HomeMenu.java"),
                StandardCharsets.UTF_8);

        assertTrue(source.contains("getHomeBedrockManager().useForms(player)"));
        assertFalse(source.contains("isBedrockPlayer("));
    }
}
