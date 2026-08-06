package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaPermissionPluginYamlTest {

    @Test
    void mediaPermissionsAreConfiguredWithDefaultFalseInPluginYaml() throws Exception {
        YamlConfiguration plugin = new YamlConfiguration();
        plugin.load(new File("src/main/resources/plugin.yml"));

        assertTrue(plugin.isConfigurationSection("permissions.rank.media"));
        assertEquals("false", plugin.getString("permissions.rank.media.default"));

        assertTrue(plugin.isConfigurationSection("permissions.rank.media.plus"));
        assertEquals("false", plugin.getString("permissions.rank.media.plus.default"));

        assertTrue(plugin.isConfigurationSection("permissions.rank.media.include"));
        assertEquals("false", plugin.getString("permissions.rank.media.include.default"));
    }
}
