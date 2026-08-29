package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PvpMatch;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The ranked match record and the two formats the history menu reads it through. */
class PvpMatchTest {

    private static final UUID FIRST = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID SECOND = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    private static final UUID STRANGER = UUID.fromString("00000000-0000-0000-0000-0000000000c3");

    private static PvpMatch match() {
        return new PvpMatch(1L, FIRST, "SamirSpider", SECOND, "Nexf", "warrior", 1_000L);
    }

    @Test
    void hitsAndCrystalsAreCountedPerPlayer() {
        PvpMatch match = match();
        match.addHit(SECOND);
        match.addHit(SECOND);
        match.addCrystal(FIRST);

        assertEquals(0, match.getHits(FIRST));
        assertEquals(2, match.getHits(SECOND));
        assertEquals(1, match.getCrystals(FIRST));
        assertEquals(0, match.getCrystals(SECOND));
    }

    @Test
    void aStrangerCannotAddToEitherSideOfTheMatch() {
        PvpMatch match = match();
        match.addHit(STRANGER);
        match.addCrystal(STRANGER);

        assertEquals(0, match.getFirstHits());
        assertEquals(0, match.getSecondHits());
        assertEquals(0, match.getFirstCrystals());
        assertEquals(0, match.getSecondCrystals());
        assertFalse(match.involves(STRANGER));
        assertNull(match.opponentOf(STRANGER));
    }

    @Test
    void theOpponentIsWhicheverSideTheOtherPlayerIsOn() {
        PvpMatch match = match();

        assertEquals(SECOND, match.opponentOf(FIRST));
        assertEquals(FIRST, match.opponentOf(SECOND));
        assertTrue(match.involves(FIRST));
        assertTrue(match.involves(SECOND));
    }

    @Test
    void theCountdownHoldsUntilItsDeadlineAndNotAfter() {
        PvpMatch match = match();
        match.setCountdownEndsAt(5_000L);

        assertTrue(match.isCountingDown(4_999L));
        assertFalse(match.isCountingDown(5_000L));

        // A match with no countdown set is live from the first tick.
        assertFalse(match().isCountingDown(0L));
    }

    @Test
    void theDurationRunsToTheEndTimeOnceThereIsOne() {
        PvpMatch match = match();
        assertEquals(17L, match.getDurationSeconds(18_000L));

        match.setEndedAt(11_000L);
        assertEquals(10L, match.getDurationSeconds(99_000L), "a finished match stops counting");
    }

    @Test
    void eloDeltasAreStoredPerSideAndReadBackBySide() {
        PvpMatch match = match();
        match.setEloBefore(1000, 1000);
        match.setEloDelta(-15, 20);

        assertEquals(-15, match.getEloDelta(FIRST));
        assertEquals(20, match.getEloDelta(SECOND));
        assertEquals(1000, match.getEloBefore(FIRST));
    }

    @Test
    void matchDurationsAreWrittenAsClockTime() {
        assertEquals("00:17", PvpMatchManager.formatMatchDuration(17L));
        assertEquals("05:00", PvpMatchManager.formatMatchDuration(300L));
        assertEquals("01:00:05", PvpMatchManager.formatMatchDuration(3605L));
        assertEquals("00:00", PvpMatchManager.formatMatchDuration(-5L));
    }

    @Test
    void eloChangesCarryTheirSign() {
        assertEquals("+20", PvpMatchManager.formatDelta(20));
        assertEquals("-15", PvpMatchManager.formatDelta(-15));
        assertEquals("0", PvpMatchManager.formatDelta(0));
    }
}
