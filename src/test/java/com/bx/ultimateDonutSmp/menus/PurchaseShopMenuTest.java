package com.bx.ultimateDonutSmp.menus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseShopMenuTest {

    @Test
    void purchaseTextReplacesFormattedPriceBeforeCompactPrice() {
        assertEquals(
                "$1,200.00",
                PurchaseShopMenu.replacePricePlaceholders("{price_formatted}", "1.2k", "$1,200.00")
        );
        assertEquals(
                "1.2k",
                PurchaseShopMenu.replacePricePlaceholders("{price}", "1.2k", "$1,200.00")
        );
        assertEquals(
                "+$1,200.00 (1.2k)",
                PurchaseShopMenu.replacePricePlaceholders("+{price_formatted} ({price})", "1.2k", "$1,200.00")
        );
        assertEquals(
                "$1,200.00",
                PurchaseShopMenu.replacePricePlaceholders("%price_formatted%", "1.2k", "$1,200.00")
        );
        assertEquals(
                "$1,200.00",
                PurchaseShopMenu.replacePricePlaceholders("${price_formatted}", "1.2k", "$1,200.00")
        );
        assertEquals(
                "$1,200.00",
                PurchaseShopMenu.replacePricePlaceholders("${price}", "1.2k", "$1,200.00")
        );
    }
}
