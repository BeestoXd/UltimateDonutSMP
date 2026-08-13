package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void normalizesKillRewardCooldownToMillis() {
        assertEquals(600_000L, ShardManager.normalizeKillRewardCooldownMillis(600L));
        assertEquals(0L, ShardManager.normalizeKillRewardCooldownMillis(0L));
        assertEquals(0L, ShardManager.normalizeKillRewardCooldownMillis(-30L));
    }

    @Test
    void blocksRepeatKillRewardsUntilCooldownExpires() {
        long cooldown = 600_000L;
        long firstKill = 1_000_000L;

        assertFalse(ShardManager.isKillRewardOnCooldown(null, firstKill, cooldown));
        assertTrue(ShardManager.isKillRewardOnCooldown(firstKill, firstKill + 1L, cooldown));
        assertTrue(ShardManager.isKillRewardOnCooldown(firstKill, firstKill + cooldown - 1L, cooldown));
        assertFalse(ShardManager.isKillRewardOnCooldown(firstKill, firstKill + cooldown, cooldown));
    }

    @Test
    void ignoresCooldownWhenDisabledOrClockRewinds() {
        long firstKill = 1_000_000L;

        assertFalse(ShardManager.isKillRewardOnCooldown(firstKill, firstKill + 1L, 0L));
        assertFalse(ShardManager.isKillRewardOnCooldown(firstKill, firstKill - 5_000L, 600_000L));
    }

    @Test
    void reportsRemainingCooldownRoundedUpToWholeSeconds() {
        long cooldown = 600_000L;
        long firstKill = 1_000_000L;

        assertEquals(600L, ShardManager.killRewardCooldownRemainingSeconds(firstKill, firstKill, cooldown));
        assertEquals(599L, ShardManager.killRewardCooldownRemainingSeconds(firstKill, firstKill + 1_000L, cooldown));
        assertEquals(1L, ShardManager.killRewardCooldownRemainingSeconds(firstKill, firstKill + cooldown - 1L, cooldown));
        assertEquals(0L, ShardManager.killRewardCooldownRemainingSeconds(firstKill, firstKill + cooldown, cooldown));
    }
}
