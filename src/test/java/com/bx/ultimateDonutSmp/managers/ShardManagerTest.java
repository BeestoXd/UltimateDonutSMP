package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShardManagerTest {

    @Test
    void fallsBackToFixedLegacyReward() {
        assertEquals(
                new ShardManager.KillRewardRange(7L, 7L),
                ShardManager.normalizeKillRewardRange(null, null, 7L)
        );
    }

    @Test
    void normalizesNegativeAndReversedRanges() {
        assertEquals(
                new ShardManager.KillRewardRange(0L, 8L),
                ShardManager.normalizeKillRewardRange(8L, -5L, 1L)
        );
    }

    @Test
    void rollsBothInclusiveRangeBoundaries() {
        ShardManager.KillRewardRange range = new ShardManager.KillRewardRange(3L, 9L);
        Random minimum = new Random() {
            @Override
            public long nextLong(long origin, long bound) {
                return origin;
            }
        };
        Random maximum = new Random() {
            @Override
            public long nextLong(long origin, long bound) {
                return bound - 1L;
            }
        };

        assertEquals(3L, ShardManager.rollKillReward(range, minimum));
        assertEquals(9L, ShardManager.rollKillReward(range, maximum));
    }
}
