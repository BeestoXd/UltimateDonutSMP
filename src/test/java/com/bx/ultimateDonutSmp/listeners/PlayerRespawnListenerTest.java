package com.bx.ultimateDonutSmp.listeners;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PlayerRespawnListenerTest {

    @Test
    void testScheduleChainmailKitWithNullsDoesNotThrow() {
        assertDoesNotThrow(() -> PlayerRespawnListener.scheduleChainmailKit(null, null, 0L));
    }
}
