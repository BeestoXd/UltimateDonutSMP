package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenusMigrationPreservationTest {

    @Test
    void spawnAndAfkAreasAreProtectedFromBundledDefaultOverwrites() {
        assertTrue(ConfigManager.isUserManagedBundledPath("menus.yml", "SPAWN-MENU.AREAS.1.SLOT"));
        assertTrue(ConfigManager.isUserManagedBundledPath("menus.yml", "SPAWN-MENU.AREAS.1.LOCATION"));
        assertTrue(ConfigManager.isUserManagedBundledPath("menus.yml", "SPAWN-MENU.AREAS.custom_spawn.DISPLAY-NAME"));

        assertTrue(ConfigManager.isUserManagedBundledPath("menus.yml", "AFK-MENU.AREAS.1.SLOT"));
        assertTrue(ConfigManager.isUserManagedBundledPath("menus.yml", "AFK-MENU.AREAS.1.LOCATION"));
        assertTrue(ConfigManager.isUserManagedBundledPath("menus.yml", "AFK-MENU.AREAS.custom_afk.CUBOID"));
    }

    @Test
    void detectsOnlyPlaceholderAreasInFreshBundledMenuConfig() throws Exception {
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.load(Path.of("src/main/resources/menus.yml").toFile());

        assertTrue(ConfigManager.hasOnlyPlaceholderAreas(bundled, "SPAWN-MENU"),
                "Bundled default SPAWN-MENU should only have placeholders");
        assertTrue(ConfigManager.hasOnlyPlaceholderAreas(bundled, "AFK-MENU"),
                "Bundled default AFK-MENU should only have placeholders");
    }

    @Test
    void detectsCustomConfiguredAreasWhenValidLocationsArePresent() throws Exception {
        String yamlContent = """
                SPAWN-MENU:
                  AREAS:
                    '1':
                      SLOT: 0
                      CUBOID: main_spawn
                      LOCATION: world,100.0,65.0,200.0,0.0,0.0
                AFK-MENU:
                  AREAS:
                    '1':
                      SLOT: 0
                      CUBOID: afk_zone
                      LOCATION: afk_world,0.5,70.0,0.5,90.0,0.0
                """;

        YamlConfiguration config = new YamlConfiguration();
        config.load(new StringReader(yamlContent));

        assertFalse(ConfigManager.hasOnlyPlaceholderAreas(config, "SPAWN-MENU"),
                "SPAWN-MENU with real coordinates should not be marked as placeholder only");
        assertFalse(ConfigManager.hasOnlyPlaceholderAreas(config, "AFK-MENU"),
                "AFK-MENU with real coordinates should not be marked as placeholder only");
        assertTrue(ConfigManager.hasCustomAreas(config, "SPAWN-MENU"));
        assertTrue(ConfigManager.hasCustomAreas(config, "AFK-MENU"));
    }
}
