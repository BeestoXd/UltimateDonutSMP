package com.bx.ultimateDonutSmp.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RtpChunkPreloadPolicyTest {

    private static final String RADIUS = "SETTINGS.PRELOAD-RADIUS";
    private static final String CHUNKS_PER_TICK = "SETTINGS.PRELOAD-CHUNKS-PER-TICK";
    private static final String THROTTLE = "SETTINGS.POST-TELEPORT-CHUNK-THROTTLE";
    private static final String VIEW_DISTANCE = "SETTINGS.POST-TELEPORT-VIEW-DISTANCE";

    @Test
    void anUntouchedConfigGetsTheSizesTheDocsPromise() {
        YamlConfiguration rtp = new YamlConfiguration();

        // The throttle is on by default and holds the player at 4, so the radius follows it there
        // rather than staying at the 2 in PRELOAD-RADIUS.
        assertEquals(4, RtpChunkPreloadPolicy.radius(rtp));
        assertEquals(2, RtpChunkPreloadPolicy.chunksPerTick(rtp));
    }

    @Test
    void aMissingConfigFallsBackToTheFloorsRatherThanFailing() {
        assertEquals(2, RtpChunkPreloadPolicy.radius(null));
        assertEquals(2, RtpChunkPreloadPolicy.chunksPerTick(null));
    }

    @Test
    void withTheThrottleOffTheRadiusIsTheAdminsBetweenTwoAndFour() {
        YamlConfiguration rtp = new YamlConfiguration();
        rtp.set(THROTTLE, false);
        assertEquals(2, RtpChunkPreloadPolicy.radius(rtp));

        // A radius under 2 drops the player into terrain nothing has read yet, which is the whole
        // thing preloading exists to stop, so the floor wins over the configured value.
        rtp.set(RADIUS, 1);
        assertEquals(2, RtpChunkPreloadPolicy.radius(rtp));
        rtp.set(RADIUS, -5);
        assertEquals(2, RtpChunkPreloadPolicy.radius(rtp));

        rtp.set(RADIUS, 3);
        assertEquals(3, RtpChunkPreloadPolicy.radius(rtp));

        rtp.set(RADIUS, 9);
        assertEquals(4, RtpChunkPreloadPolicy.radius(rtp));
    }

    @Test
    void withTheThrottleOnTheViewDistanceRaisesTheRadiusButNeverLowersIt() {
        YamlConfiguration rtp = new YamlConfiguration();
        rtp.set(THROTTLE, true);
        rtp.set(RADIUS, 2);

        // This is why the config comment calls PRELOAD-RADIUS a floor rather than a cap.
        assertEquals(4, RtpChunkPreloadPolicy.radius(rtp));

        rtp.set(VIEW_DISTANCE, 2);
        assertEquals(2, RtpChunkPreloadPolicy.radius(rtp));

        // A bigger PRELOAD-RADIUS still wins when it asks for more than the throttle does.
        rtp.set(RADIUS, 3);
        assertEquals(3, RtpChunkPreloadPolicy.radius(rtp));

        // Neither side can push past 4 chunks.
        rtp.set(VIEW_DISTANCE, 32);
        assertEquals(4, RtpChunkPreloadPolicy.radius(rtp));

        // A view distance below 2 is treated as 2, so it cannot shrink the radius either.
        rtp.set(RADIUS, 2);
        rtp.set(VIEW_DISTANCE, 0);
        assertEquals(2, RtpChunkPreloadPolicy.radius(rtp));
    }

    @Test
    void chunksPerTickNeverDropsBelowTwo() {
        YamlConfiguration rtp = new YamlConfiguration();

        rtp.set(CHUNKS_PER_TICK, 1);
        assertEquals(2, RtpChunkPreloadPolicy.chunksPerTick(rtp));
        rtp.set(CHUNKS_PER_TICK, 0);
        assertEquals(2, RtpChunkPreloadPolicy.chunksPerTick(rtp));
        rtp.set(CHUNKS_PER_TICK, -8);
        assertEquals(2, RtpChunkPreloadPolicy.chunksPerTick(rtp));

        rtp.set(CHUNKS_PER_TICK, 6);
        assertEquals(6, RtpChunkPreloadPolicy.chunksPerTick(rtp));
    }

    @Test
    void chunkOrderCoversTheSquareOnceAndStartsInTheMiddle() {
        List<int[]> chunks = RtpChunkPreloadPolicy.chunkOrder(10, -4, 2);

        assertEquals(25, chunks.size());
        assertEquals(10, chunks.get(0)[0]);
        assertEquals(-4, chunks.get(0)[1]);

        Set<String> seen = new HashSet<>();
        for (int[] chunk : chunks) {
            assertTrue(seen.add(chunk[0] + ":" + chunk[1]), "chunk visited twice");
            assertTrue(Math.abs(chunk[0] - 10) <= 2 && Math.abs(chunk[1] + 4) <= 2);
        }

        // Nearest first is what makes a run that gives up early still leave the ground under the
        // player ready, so the distances have to come back in order.
        int previous = -1;
        for (int[] chunk : chunks) {
            int distance = Math.abs(chunk[0] - 10) + Math.abs(chunk[1] + 4);
            assertTrue(distance >= previous, "chunk order jumped back towards the centre");
            previous = distance;
        }
    }

    @Test
    void aZeroRadiusIsJustTheChunkUnderTheDestination() {
        List<int[]> chunks = RtpChunkPreloadPolicy.chunkOrder(0, 0, 0);

        assertEquals(1, chunks.size());
        assertEquals(0, chunks.get(0)[0]);
        assertEquals(0, chunks.get(0)[1]);
    }
}
