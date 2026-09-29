package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnCuboidBindingTest {

    @Test
    void recognizesOnlyGenuineLocationStringsAsConfiguredLocations() {
        assertFalse(SpawnManager.isConfiguredLocation(null));
        assertFalse(SpawnManager.isConfiguredLocation(""));
        assertFalse(SpawnManager.isConfiguredLocation("   "));
        assertFalse(SpawnManager.isConfiguredLocation("1"));
        assertFalse(SpawnManager.isConfiguredLocation("2"));
        assertFalse(SpawnManager.isConfiguredLocation("  42  "));

        assertTrue(SpawnManager.isConfiguredLocation("world,0.5,70.0,0.5,90.0,0.0"));
        assertTrue(SpawnManager.isConfiguredLocation("lobby,100.0,65.0,-200.0"));
    }
}
