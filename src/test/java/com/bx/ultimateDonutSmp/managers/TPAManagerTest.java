package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TPAManagerTest {

    @Test
    void tpaRequestExpiryDefaultsToOneMinute() {
        TPAManager manager = new TPAManager(null);
        // 1 minute = 60 seconds = 1200 ticks
        assertEquals(60 * 20L, manager.getRequestExpiryTicks());
        // 1 minute = 60,000 milliseconds
        assertEquals(60_000L, manager.getRequestExpiryMillis());
    }

    @Test
    void queuedTpaRequestUsesOneMinuteDuration() {
        UUID requester = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        long now = System.currentTimeMillis();
        long expiry = now + 60_000L;

        TPAManager.QueuedTpaRequest queued = new TPAManager.QueuedTpaRequest(
                requester,
                target,
                false,
                now,
                expiry
        );

        assertEquals(60_000L, queued.expiresAtMillis() - queued.queuedAtMillis());
        TPAManager.TpaRequest request = queued.toRequest();
        assertEquals(requester, request.requester());
        assertEquals(target, request.target());
        assertFalse(request.tpaHere());
    }

    @Test
    void expirePendingRequestRemovesMatchingRequest() throws Exception {
        TPAManager manager = new TPAManager(null);
        UUID requester = UUID.randomUUID();
        UUID target = UUID.randomUUID();

        // Inject request directly into pendingRequests for unit test
        java.lang.reflect.Field field = TPAManager.class.getDeclaredField("pendingRequests");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<UUID, TPAManager.TpaRequest> pending =
                (java.util.Map<UUID, TPAManager.TpaRequest>) field.get(manager);

        TPAManager.TpaRequest request = new TPAManager.TpaRequest(requester, target, false, false);
        pending.put(target, request);

        assertTrue(manager.hasRequest(target));
        assertEquals(request, manager.getRequest(target));

        // Expire without Bukkit online players - should remove cleanly
        boolean expired = manager.expirePendingRequest(target);
        assertTrue(expired);
        assertFalse(manager.hasRequest(target));
        assertNull(manager.getRequest(target));

        // Second expiry attempt returns false because it is already removed
        assertFalse(manager.expirePendingRequest(target));
    }
}