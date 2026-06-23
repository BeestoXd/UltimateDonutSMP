package com.bx.ultimateDonutSmp.menus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorthMenuTest {

    @Test
    void calculatesConfiguredMaterialStackTotals() {
        assertEquals(640D, WorthMenu.calculateStackPrice(10D, 64));
        assertEquals(160D, WorthMenu.calculateStackPrice(10D, 16));
        assertEquals(10D, WorthMenu.calculateStackPrice(10D, 1));
    }
}
