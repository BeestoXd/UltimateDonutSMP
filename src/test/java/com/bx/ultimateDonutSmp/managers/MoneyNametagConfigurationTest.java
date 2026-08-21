package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.PlayerSettingDefaults;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyNametagConfigurationTest {

    @Test
    void balancesRenderWithThousandSeparatorsByDefault() {
        assertEquals("&a$1,250,000", MoneyNametagManager.render("&a${balance}", 1_250_000D, false));
        assertEquals("&a$0", MoneyNametagManager.render("&a${balance}", 0D, false));
        assertEquals("&a$-500", MoneyNametagManager.render("&a${balance}", -500D, false));
    }

    @Test
    void shortFormatCollapsesLargeBalancesToASuffix() {
        assertEquals("&a$1.25M", MoneyNametagManager.render("&a${balance}", 1_250_000D, true));
        assertEquals("&a$2.5B", MoneyNametagManager.render("&a${balance}", 2_500_000_000D, true));
        assertEquals("&a$750", MoneyNametagManager.render("&a${balance}", 750D, true));
    }

    @Test
    void compactBalancesClimbThroughEverySuffix() {
        assertEquals("&a$1.1K", MoneyNametagManager.render("&a${balance}", 1_100D, true));
        assertEquals("&a$1.1M", MoneyNametagManager.render("&a${balance}", 1_100_000D, true));
        assertEquals("&a$1.1B", MoneyNametagManager.render("&a${balance}", 1_100_000_000D, true));
        assertEquals("&a$1.1T", MoneyNametagManager.render("&a${balance}", 1_100_000_000_000D, true));
        assertEquals("&a$2.3M", MoneyNametagManager.render("&a${balance}", 2_300_000D, true));
    }

    @Test
    void aFormatWithoutThePlaceholderIsLeftAlone() {
        assertEquals("&7Balance hidden", MoneyNametagManager.render("&7Balance hidden", 1_000D, false));
        assertEquals("1,000", MoneyNametagManager.render(null, 1_000D, false));
    }

    @Test
    void bundledConfigShipsTheDocumentedDefaults() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.load(Path.of("src/main/resources", "config.yml").toFile());

        assertTrue(config.isConfigurationSection("MONEY-NAMETAGS"));
        assertTrue(config.getBoolean("MONEY-NAMETAGS.ENABLED"));
        assertEquals("&a${balance}", config.getString("MONEY-NAMETAGS.FORMAT"));
        assertTrue(config.getBoolean("MONEY-NAMETAGS.SHORT-FORMAT"));
        assertEquals(10, config.getInt("MONEY-NAMETAGS.UPDATE-INTERVAL-TICKS"));
        assertEquals(0.05D, config.getDouble("MONEY-NAMETAGS.LINE-GAP"));
        assertEquals(32.0D, config.getDouble("MONEY-NAMETAGS.VIEW-RANGE"));
        assertTrue(config.getBoolean("MONEY-NAMETAGS.HIDE-WHILE-SNEAKING"));
    }

    @Test
    void playersStartWithoutMoneyNametagsUntilAnAdminSaysOtherwise() throws Exception {
        assertFalse(new PlayerData(UUID.randomUUID(), "Tester").isMoneyNametagsEnabled());

        YamlConfiguration menus = new YamlConfiguration();
        menus.load(Path.of("src/main/resources", "menus.yml").toFile());
        ConfigurationSection buttons = menus.getConfigurationSection(PlayerSettingDefaults.BUTTONS_PATH);
        assertNotNull(buttons);
        assertNotNull(buttons.getConfigurationSection("MONEY_NAMETAGS"));

        buttons.set("MONEY_NAMETAGS.DEFAULT", true);
        PlayerData data = new PlayerData(UUID.randomUUID(), "Tester");
        PlayerSettingDefaults.applyDefaults(buttons, data);

        assertTrue(data.isMoneyNametagsEnabled());
    }
}
