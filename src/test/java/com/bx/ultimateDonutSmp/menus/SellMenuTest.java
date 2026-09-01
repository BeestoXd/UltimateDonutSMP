package com.bx.ultimateDonutSmp.menus;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SellMenuTest {

    @Test
    void autoSellOnlyControlsSellingWhileTheMenuIsOpen() {
        assertTrue(SellMenu.sellsWhileOpen(false, true));
        assertFalse(
                SellMenu.sellsWhileOpen(false, false),
                "AUTO-SELL false must stop items selling the moment they land in the grid"
        );
        assertFalse(
                SellMenu.sellsWhileOpen(true, true),
                "confirm mode sells through its button, never on item movement"
        );
    }

    @Test
    void instantModeStillSellsWhenTheMenuIsClosed() {
        assertTrue(
                SellMenu.sellsOnClose(false),
                "instant mode with AUTO-SELL false would otherwise have no way to sell at all"
        );
        assertFalse(
                SellMenu.sellsOnClose(true),
                "confirm mode hands items back when the player closes without confirming"
        );
    }

    @Test
    void modeKeyIsReadCaseInsensitively() {
        assertTrue(SellMenu.isConfirmMode("confirm"));
        assertTrue(SellMenu.isConfirmMode("CONFIRM"));
        assertFalse(SellMenu.isConfirmMode("instant"));
        assertFalse(SellMenu.isConfirmMode(null));
    }

    @Test
    void shippedMenusYmlOpensTheSellMenuInConfirmMode() throws Exception {
        YamlConfiguration menus = new YamlConfiguration();
        menus.load(Path.of("src/main/resources/menus.yml").toFile());

        assertTrue(
                SellMenu.isConfirmMode(menus.getString("SELL-MENU.MODE", "instant")),
                "menus.yml ships MODE: confirm, so the default sell menu keeps its Confirm Sell button"
        );
    }
}
