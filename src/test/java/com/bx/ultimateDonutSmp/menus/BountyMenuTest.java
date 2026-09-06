package com.bx.ultimateDonutSmp.menus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BountyMenuTest {

    @Test
    void bountyLoreReplacesFormattedPriceBeforeCompactPrice() {
        assertEquals(
                "$1,200.00",
                BountyMenu.replaceBountyPlaceholders("{price_formatted}", "Steve", "1.2k", "$1,200.00")
        );
        assertEquals(
                "1.2k",
                BountyMenu.replaceBountyPlaceholders("{price}", "Steve", "1.2k", "$1,200.00")
        );
        assertEquals(
                "Steve: +$1,200.00 (1.2k)",
                BountyMenu.replaceBountyPlaceholders("{player}: +{price_formatted} ({price})", "Steve", "1.2k", "$1,200.00")
        );
        assertEquals(
                "$1.2k",
                BountyMenu.replaceBountyPlaceholders("${price}", "Steve", "1.2k", "$1,200.00")
        );
    }
}
