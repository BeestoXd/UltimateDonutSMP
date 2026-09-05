package com.bx.ultimateDonutSmp.menus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShopMenuTest {

    @Test
    void shopLoreReplacesDollarFormattedPriceBeforeBraceFormattedPrice() {
        assertEquals(
                "$1,200.00",
                ShopMenu.replacePricePlaceholders("${price_formatted}", "1.2k", "$1,200.00")
        );
        assertEquals(
                "$1,200.00",
                ShopMenu.replacePricePlaceholders("{price_formatted}", "1.2k", "$1,200.00")
        );
        assertEquals(
                "1.2k",
                ShopMenu.replacePricePlaceholders("{price}", "1.2k", "$1,200.00")
        );
        assertEquals(
                "$1,200.00",
                ShopMenu.replacePricePlaceholders("${price}", "1.2k", "$1,200.00")
        );
        assertEquals(
                "$1,200.00",
                ShopMenu.replacePricePlaceholders("%price_formatted%", "1.2k", "$1,200.00")
        );
    }
}
