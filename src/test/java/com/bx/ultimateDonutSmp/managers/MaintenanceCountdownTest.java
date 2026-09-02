package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The clock behind /maintenance on with a duration. The server list promises players a time, so
 * the arithmetic behind that promise is worth pinning down.
 */
class MaintenanceCountdownTest {

    @Test
    void maintenanceStartedWithoutADurationHasNothingToCountDown() {
        assertEquals(MaintenanceManager.NO_DEADLINE, MaintenanceManager.remainingSeconds(0L, 1_000L));
        assertEquals(MaintenanceManager.NO_DEADLINE, MaintenanceManager.remainingSeconds(-1L, 1_000L));
    }

    @Test
    void aDeadlineAlreadyGoneReadsAsZeroRatherThanACountUp() {
        assertEquals(0L, MaintenanceManager.remainingSeconds(1_000L, 1_000L));
        assertEquals(0L, MaintenanceManager.remainingSeconds(1_000L, 5_000L));
    }

    @Test
    void partSecondsRoundUpSoTheCountdownNeverRestsOnZeroWhileTheServerIsShut() {
        assertEquals(4L, MaintenanceManager.remainingSeconds(3_400L, 0L));
        assertEquals(1L, MaintenanceManager.remainingSeconds(1L, 0L));
        assertEquals(30L, MaintenanceManager.remainingSeconds(30_000L, 0L));
    }

    @Test
    void theCountdownIsWrittenTheWayTheMultiplayerListShowsIt() {
        assertEquals("03:29", MaintenanceManager.formatCountdown(209L));
        assertEquals("00:00", MaintenanceManager.formatCountdown(0L));
        assertEquals("00:09", MaintenanceManager.formatCountdown(9L));
        assertEquals("59:59", MaintenanceManager.formatCountdown(3_599L));
    }

    @Test
    void anHourOrMoreGrowsTheClockRatherThanRunningTheMinutesPastSixty() {
        assertEquals("1:00:00", MaintenanceManager.formatCountdown(3_600L));
        assertEquals("2:03:04", MaintenanceManager.formatCountdown(7_384L));
    }

    @Test
    void aNegativeCountIsClampedInsteadOfPrintingAMinusSign() {
        assertEquals("00:00", MaintenanceManager.formatCountdown(MaintenanceManager.NO_DEADLINE));
    }

    @Test
    void onlyAnActiveMaintenanceWithItsDeadlinePassedLiftsItself() {
        assertTrue(MaintenanceManager.hasExpired(true, 1_000L, 1_000L));
        assertTrue(MaintenanceManager.hasExpired(true, 1_000L, 2_000L));
        assertFalse(MaintenanceManager.hasExpired(true, 1_000L, 999L));
        assertFalse(MaintenanceManager.hasExpired(true, 0L, 5_000L), "no duration means it waits for /maintenance off");
        assertFalse(MaintenanceManager.hasExpired(false, 1_000L, 5_000L));
    }
}
