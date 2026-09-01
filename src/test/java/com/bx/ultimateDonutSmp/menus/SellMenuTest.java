package com.bx.ultimateDonutSmp.menus;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SellMenuTest {

    @Test
    void autoSellOnlyControlsSellingWhileTheMenuIsOpen() {
        assertTrue(SellMenu.sellsWhileOpen("instant", true));
        assertFalse(
                SellMenu.sellsWhileOpen("instant", false),
                "AUTO-SELL false must stop items selling the moment they land in the grid"
        );
        assertFalse(
                SellMenu.sellsWhileOpen("confirm", true),
                "confirm mode sells through its button, never on item movement"
        );
    }

    @Test
    void closeModeHoldsEverythingUntilTheMenuIsShut() {
        assertFalse(
                SellMenu.sellsWhileOpen("close", true),
                "close mode waits for the close even with AUTO-SELL left on"
        );
        assertFalse(SellMenu.sellsWhileOpen("close", false));
        assertTrue(
                SellMenu.sellsOnClose("close"),
                "close mode has no other way to pay the player"
        );
    }

    @Test
    void instantModeStillSellsWhenTheMenuIsClosed() {
        assertTrue(
                SellMenu.sellsOnClose("instant"),
                "instant mode with AUTO-SELL false would otherwise have no way to sell at all"
        );
        assertFalse(
                SellMenu.sellsOnClose("confirm"),
                "confirm mode hands items back when the player closes without confirming"
        );
    }

    @Test
    void modeNamesAreReadCaseInsensitively() {
        assertTrue(SellMenu.isConfirmMode("confirm"));
        assertTrue(SellMenu.isConfirmMode("CONFIRM"));
        assertFalse(SellMenu.isConfirmMode("instant"));
        assertFalse(SellMenu.isConfirmMode(null));

        assertTrue(SellMenu.isCloseMode("close"));
        assertTrue(SellMenu.isCloseMode("Close"));
        assertFalse(SellMenu.isCloseMode("instant"));
        assertFalse(SellMenu.isCloseMode(null));
    }

    @Test
    void anUnknownModeFallsBackToInstant() {
        assertTrue(SellMenu.sellsWhileOpen("colse", true), "a typo must not silently disable selling");
        assertTrue(SellMenu.sellsOnClose("colse"));
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
