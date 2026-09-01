package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.amethyst.AmethystToolType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmethystShardShopConfigurationTest {

    @Test
    void everyAmethystToolCanBeConfiguredForShardShop() {
        YamlConfiguration configuration = loadResource();
        Set<Integer> slots = new HashSet<>();

        for (AmethystToolType type : AmethystToolType.values()) {
            String path = "AMETHYST-TOOLS." + type.getConfigKey();
            ConfigurationSection tool = configuration.getConfigurationSection(path);
            assertNotNull(tool, path);

            ConfigurationSection shardShop = tool.getConfigurationSection("SHARD-SHOP");
            assertNotNull(shardShop, path + ".SHARD-SHOP");
            assertFalse(shardShop.getBoolean("ENABLED"), "Default must not change the live economy");
            assertEquals("SHARD", shardShop.getString("CURRENCY"), path + ".SHARD-SHOP.CURRENCY");
            // A tool with no usable price is skipped when the menu loads, so enabling the
            // section would leave an empty slot behind.
            assertTrue(shardShop.getDouble("PRICE-PER-UNIT") > 0D, path + ".SHARD-SHOP.PRICE-PER-UNIT");
            assertTrue(slots.add(shardShop.getInt("SLOT")), "Shard-shop slots must be unique");
            assertEquals(1, shardShop.getInt("MIN-QUANTITY"));
            assertEquals(1, shardShop.getInt("MAX-QUANTITY"));
            assertEquals(1, shardShop.getInt("DEFAULT-QUANTITY"));
            assertTrue(shardShop.getBoolean("HIDE-QUANTITY-BUTTONS"));
        }
    }

    @Test
    void everyShardShopSectionBelongsToAToolTheCodeReads() {
        YamlConfiguration configuration = loadResource();
        ConfigurationSection tools = configuration.getConfigurationSection("AMETHYST-TOOLS");
        assertNotNull(tools, "AMETHYST-TOOLS");

        Set<String> known = new HashSet<>();
        for (AmethystToolType type : AmethystToolType.values()) {
            known.add(type.getConfigKey());
        }

        for (String key : tools.getKeys(false)) {
            ConfigurationSection section = tools.getConfigurationSection(key);
            if (section == null || !section.isConfigurationSection("SHARD-SHOP")) {
                continue;
            }
            // Both the loader and the validator walk AmethystToolType, so a shard shop under
            // any other key is config nobody ever reads.
            assertTrue(known.contains(key), "AMETHYST-TOOLS." + key + " has a SHARD-SHOP but no tool type reads it");
        }
    }

    private YamlConfiguration loadResource() {
        var file = new java.io.File("src/main/resources/amethyst-tools.yml");
        return YamlConfiguration.loadConfiguration(file);
    }
}
