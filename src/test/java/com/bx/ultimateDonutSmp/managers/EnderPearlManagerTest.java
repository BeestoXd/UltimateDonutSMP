package com.bx.ultimateDonutSmp.managers;

import org.bukkit.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EnderPearlManagerTest {

    private EnderPearlManager enderPearlManager;

    @BeforeEach
    void setUp() {
        enderPearlManager = new EnderPearlManager(null);
    }

    @Test
    void testDirectTrackingAndPendingTeleport() {
        UUID pearlUuid = UUID.randomUUID();
        UUID playerUuid = UUID.randomUUID();

        assertFalse(enderPearlManager.isPearlTracked(pearlUuid));
        assertFalse(enderPearlManager.hasPendingTeleport(playerUuid));

        enderPearlManager.trackPearlDirectly(pearlUuid, playerUuid);
        assertTrue(enderPearlManager.isPearlTracked(pearlUuid));

        Location testLocation = new Location(null, 100, 64, 200);
        enderPearlManager.setPendingTeleportDirectly(playerUuid, testLocation);
        assertTrue(enderPearlManager.hasPendingTeleport(playerUuid));

        Location consumed = enderPearlManager.consumePendingTeleport(playerUuid);
        assertEquals(testLocation, consumed);
        assertFalse(enderPearlManager.hasPendingTeleport(playerUuid));
    }

    @Test
    void testClear() {
        UUID pearlUuid = UUID.randomUUID();
        UUID playerUuid = UUID.randomUUID();

        enderPearlManager.trackPearlDirectly(pearlUuid, playerUuid);
        enderPearlManager.setPendingTeleportDirectly(playerUuid, new Location(null, 0, 0, 0));

        enderPearlManager.clear();

        assertFalse(enderPearlManager.isPearlTracked(pearlUuid));
        assertFalse(enderPearlManager.hasPendingTeleport(playerUuid));
        assertNull(enderPearlManager.consumePendingTeleport(playerUuid));
    }
}
