package com.bx.ultimateDonutSmp.menus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SellHistoryMenuTest {

    @Test
    void historyLoreReplacesFormattedPriceBeforeCompactPrice() {
        assertEquals(
                "$1,200.00",
                SellHistoryMenu.replaceHistoryPlaceholders("{price_formatted}", "1.2k", "$1,200.00", "64")
        );
        assertEquals(
                "1.2k",
                SellHistoryMenu.replaceHistoryPlaceholders("{price}", "1.2k", "$1,200.00", "64")
        );
        assertEquals(
                "+$1,200.00 (1.2k)",
                SellHistoryMenu.replaceHistoryPlaceholders("+{price_formatted} ({price})", "1.2k", "$1,200.00", "64")
        );
        assertEquals(
                "64",
                SellHistoryMenu.replaceHistoryPlaceholders("{amount}", "1.2k", "$1,200.00", "64")
        );
    }
}
