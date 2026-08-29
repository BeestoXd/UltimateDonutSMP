package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PvpRank;
import com.bx.ultimateDonutSmp.models.PvpStats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The arena maths that runs without a server: durations, the level curve, and the rank ladder. */
class PvpProgressionTest {

    private static final List<PvpRank> LADDER = List.of(
            new PvpRank("LT5", "&7LT5", 0),
            new PvpRank("LT1", "&eLT1", 400),
            new PvpRank("HT5", "&eHT5", 600),
            new PvpRank("HT1", "&cHT1", 1500)
    );

    @Test
    void durationsReadEveryUnitAndCombination() {
        assertEquals(86_400_000L, PvpManager.parseDuration("24h"));
        assertEquals(1_800_000L, PvpManager.parseDuration("30m"));
        assertEquals(90_000L, PvpManager.parseDuration("90s"));
        assertEquals(
                86_400_000L + 10 * 3_600_000L + 15 * 60_000L,
                PvpManager.parseDuration("1d10h15m")
        );
    }

    @Test
    void aBareNumberIsReadAsSecondsAndJunkIsZero() {
        assertEquals(45_000L, PvpManager.parseDuration("45"));
        assertEquals(0L, PvpManager.parseDuration(""));
        assertEquals(0L, PvpManager.parseDuration(null));
        assertEquals(0L, PvpManager.parseDuration("soon"));
    }

    @Test
    void theResetCountdownFillsBothPaddedAndUnpaddedPlaceholders() {
        long remaining = PvpManager.parseDuration("1d10h15m1s");

        assertEquals("01D:10H:15M:01S", PvpManager.formatDuration(remaining, "{d}D:{h}H:{m}M:{s}S"));
        assertEquals("1d 10h 15m 1s", PvpManager.formatDuration(remaining, "{D}d {H}h {M}m {S}s"));
    }

    @Test
    void theLevelCurveGrowsByOneIncrementPerLevel() {
        assertEquals(100, PvpManager.xpForLevel(1, 100, 50));
        assertEquals(150, PvpManager.xpForLevel(2, 100, 50));
        assertEquals(300, PvpManager.xpForLevel(5, 100, 50));

        // A flat curve is a legitimate setting, and no level may ever cost nothing.
        assertEquals(100, PvpManager.xpForLevel(9, 100, 0));
        assertTrue(PvpManager.xpForLevel(3, 0, 0) > 0);
    }

    @Test
    void aPlayerHoldsTheHighestRankTheirEloReaches() {
        assertEquals("LT5", PvpRank.resolve(LADDER, 0).getId());
        assertEquals("LT5", PvpRank.resolve(LADDER, 399).getId());
        assertEquals("LT1", PvpRank.resolve(LADDER, 400).getId());
        assertEquals("HT5", PvpRank.resolve(LADDER, 1499).getId());
        assertEquals("HT1", PvpRank.resolve(LADDER, 9999).getId());
    }

    @Test
    void anEloDropTakesTheRankBackDownWithIt() {
        assertEquals("HT5", PvpRank.resolve(LADDER, 620).getId());
        assertEquals("LT1", PvpRank.resolve(LADDER, 580).getId());
    }

    @Test
    void theBottomRankIsTheFloorAndAnEmptyLadderHasNone() {
        assertEquals("LT5", PvpRank.resolve(LADDER, -50).getId());
        assertNull(PvpRank.resolve(List.of(), 100));
        assertNull(PvpRank.resolve(null, 100));
    }

    @Test
    void killsAndDeathsMoveTheStreakAndTheRatio() {
        PvpStats stats = PvpStats.starting(250).recordKill().recordKill().recordKill();

        assertEquals(250, stats.getElo());
        assertEquals(3, stats.getKills());
        assertEquals(3, stats.getStreak());
        assertEquals(3, stats.getBestStreak());
        assertEquals(3.0D, stats.getKillDeathRatio());

        PvpStats afterDeath = stats.recordDeath();
        assertEquals(0, afterDeath.getStreak());
        assertEquals(3, afterDeath.getBestStreak(), "the best streak is a record, not a counter");
        assertEquals(3.0D, afterDeath.getKillDeathRatio());
    }

    @Test
    void eloNeverGoesNegativeAndTheLevelNeverDropsBelowOne() {
        assertEquals(0, PvpStats.starting(0).withElo(-40).getElo());
        assertEquals(1, PvpStats.starting(0).withProgress(0, 10).getLevel());
    }
}
