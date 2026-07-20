package com.bx.ultimateDonutSmp.models;

import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RandomizedCoordsTest {

    @Test
    void testRealCoordsWhenDisabled() {
        PlayerData data = new PlayerData(UUID.randomUUID(), "TestPlayer");
        data.setRandomizedCoords(false);

        assertEquals(100, data.getDisplayX(100));
        assertEquals(64, data.getDisplayY(64));
        assertEquals(-200, data.getDisplayZ(-200));
    }

    @Test
    void testRandomizedCoordsWhenEnabled() {
        PlayerData data = new PlayerData(UUID.randomUUID(), "TestPlayer");
        data.setRandomizedCoords(true);
        data.setRandomOffsetForTesting(500, -300);

        assertEquals(600, data.getDisplayX(100));
        assertEquals(64, data.getDisplayY(64));
        assertEquals(-500, data.getDisplayZ(-200));
    }
}
