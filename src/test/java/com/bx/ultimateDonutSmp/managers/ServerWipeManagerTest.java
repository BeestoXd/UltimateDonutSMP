package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerWipeManagerTest {

    @Test
    void acceptsOnlyDirectSafeWorldNames() {
        assertTrue(ServerWipeManager.isSafeWorldName("resource_world"));
        assertTrue(ServerWipeManager.isSafeWorldName("resource-nether.2"));
        assertFalse(ServerWipeManager.isSafeWorldName("../world"));
        assertFalse(ServerWipeManager.isSafeWorldName("folder/world"));
        assertFalse(ServerWipeManager.isSafeWorldName("world\\nether"));
    }

    @Test
    void normalizesAndDeduplicatesWorldNames() {
        assertEquals(
                List.of("Resource", "resource_nether"),
                ServerWipeManager.normalizeWorldNames(List.of(" Resource ", "resource", "resource_nether", ""))
        );
    }

    @Test
    void expiresConfirmationTokensUsingConfiguredTtl() {
        assertFalse(ServerWipeManager.isTokenExpired(1_000L, 5_999L, 5_000L));
        assertTrue(ServerWipeManager.isTokenExpired(1_000L, 6_001L, 5_000L));
        assertTrue(ServerWipeManager.isTokenExpired(0L, 1_000L, 5_000L));
    }
}
