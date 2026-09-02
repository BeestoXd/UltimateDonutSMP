package com.bx.ultimateDonutSmp.menus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleportAreaRandomButtonTest {

    @Test
    void aMenuWithNothingResolvedDrawsNoRandomButton() {
        assertFalse(TeleportAreaMenu.shouldDrawRandomButton(0));
    }

    @Test
    void aSingleResolvedAreaDrawsNoRandomButton() {
        assertFalse(
                TeleportAreaMenu.shouldDrawRandomButton(1),
                "one area makes the random button a duplicate of that area's own icon, so it stays"
                        + " hidden however SPAWN-MENU.RANDOM-BUTTON or AFK-MENU.RANDOM-BUTTON is set"
        );
    }

    @Test
    void twoOrMoreResolvedAreasDrawTheRandomButton() {
        assertTrue(TeleportAreaMenu.shouldDrawRandomButton(2));
        assertTrue(TeleportAreaMenu.shouldDrawRandomButton(3));
        assertTrue(TeleportAreaMenu.shouldDrawRandomButton(12));
    }

    @Test
    void aNegativeCountIsTreatedAsNothingToPickFrom() {
        assertFalse(TeleportAreaMenu.shouldDrawRandomButton(-1));
    }
}
