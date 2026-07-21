package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.WorthResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorthManagerTest {

    @Test
    void displaysSingleItemTotalWorth() {
        WorthResult result = directWorth(100.0, 100.0);

        assertEquals(100.0, WorthManager.getDisplayWorth(result));
    }

    @Test
    void displaysStackTotalWorthInsteadOfUnitWorth() {
        WorthResult result = directWorth(100.0, 500.0);

        assertEquals(500.0, WorthManager.getDisplayWorth(result));
    }

    @Test
    void displaysContainerStackTotalWorth() {
        WorthResult result = new WorthResult(
                true,
                true,
                250.0,
                500.0,
                50.0,
                200.0,
                "CONTAINER",
                "SHULKER_BOX",
                "BLOCKS"
        );

        assertEquals(500.0, WorthManager.getDisplayWorth(result));
    }

    private WorthResult directWorth(double unitWorth, double totalWorth) {
        return new WorthResult(
                true,
                false,
                unitWorth,
                totalWorth,
                unitWorth,
                0.0,
                "DIRECT",
                "DIAMOND",
                "ORES"
        );
    }
}
