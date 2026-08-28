package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PlayerData;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatManagerTest {

    private static final long TAG_APPLIED = 1_000L;
    private static final long TWENTY_SECOND_EXPIRY = TAG_APPLIED + 20_000L;

    @Test
    void twentySecondTagOpensOnTwentyRatherThanNineteen() {
        assertEquals(20L, CombatManager.remainingSeconds(TWENTY_SECOND_EXPIRY, TAG_APPLIED + 50L));
        assertEquals(20L, CombatManager.remainingSeconds(TWENTY_SECOND_EXPIRY, TAG_APPLIED));
    }

    @Test
    void everyLaterFrameDropsByExactlyOne() {
        assertEquals(19L, CombatManager.remainingSeconds(TWENTY_SECOND_EXPIRY, TAG_APPLIED + 1_050L));
        assertEquals(18L, CombatManager.remainingSeconds(TWENTY_SECOND_EXPIRY, TAG_APPLIED + 2_050L));
        assertEquals(1L, CombatManager.remainingSeconds(TWENTY_SECOND_EXPIRY, TAG_APPLIED + 19_050L));
    }

    @Test
    void aWholeSecondLeftReadsAsThatSecondAndNotTheOneAbove() {
        assertEquals(19L, CombatManager.remainingSeconds(TWENTY_SECOND_EXPIRY, TAG_APPLIED + 1_000L));
        assertEquals(1L, CombatManager.remainingSeconds(TWENTY_SECOND_EXPIRY, TAG_APPLIED + 19_000L));
    }

    @Test
    void aTaggedPlayerIsNeverShownZero() {
        assertEquals(1L, CombatManager.remainingSeconds(TWENTY_SECOND_EXPIRY, TWENTY_SECOND_EXPIRY - 1L));
    }

    @Test
    void anExpiredTagReadsZero() {
        assertEquals(0L, CombatManager.remainingSeconds(TWENTY_SECOND_EXPIRY, TWENTY_SECOND_EXPIRY));
        assertEquals(0L, CombatManager.remainingSeconds(TWENTY_SECOND_EXPIRY, TWENTY_SECOND_EXPIRY + 5_000L));
    }

    @Test
    void theCountdownIsDrawnUntilThePlayerTurnsItOff() {
        PlayerData data = new PlayerData(UUID.randomUUID(), "Tester");
        assertTrue(CombatManager.showsCombatTimer(data));

        data.setCombatTimerEnabled(false);
        assertFalse(CombatManager.showsCombatTimer(data));
    }

    @Test
    void aPlayerWithNoProfileKeepsSeeingTheCountdown() {
        assertTrue(CombatManager.showsCombatTimer(null));
    }
}
