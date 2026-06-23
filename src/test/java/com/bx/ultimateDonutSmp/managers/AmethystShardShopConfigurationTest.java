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
            assertTrue(slots.add(shardShop.getInt("SLOT")), "Shard-shop slots must be unique");
            assertEquals(1, shardShop.getInt("MIN-QUANTITY"));
            assertEquals(1, shardShop.getInt("MAX-QUANTITY"));
            assertEquals(1, shardShop.getInt("DEFAULT-QUANTITY"));
            assertTrue(shardShop.getBoolean("HIDE-QUANTITY-BUTTONS"));
        }
    }

    private YamlConfiguration loadResource() {
        var stream = getClass().getClassLoader().getResourceAsStream("amethyst-tools.yml");
        assertNotNull(stream, "amethyst-tools.yml");
        return YamlConfiguration.loadConfiguration(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        );
    }
}
