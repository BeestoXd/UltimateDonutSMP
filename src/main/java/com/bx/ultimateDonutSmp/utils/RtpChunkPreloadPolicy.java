package com.bx.ultimateDonutSmp.utils;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sizes the chunk work on either side of an RTP teleport.
 *
 * <p>Two managers do that work at different moments. {@code RTPManager} loads the terrain around
 * the destination before the player lands on it, and {@code TeleportManager} settles the same
 * terrain again afterwards. Different moments, one area: both are sized from the single pair of
 * config keys {@code PRELOAD-RADIUS} and {@code PRELOAD-CHUNKS-PER-TICK}, and both widen the
 * radius to cover whatever the post teleport throttle is about to let the player see.</p>
 *
 * <p>The sizing lived as line-for-line copies in the two managers until a fix to the
 * {@code PRELOAD-RADIUS} fallback had to be made in four places at once. Nothing broke, because
 * the copies were still identical, and nothing would have broken loudly if only one of them had
 * been changed either. Giving one side a size of its own means adding a config key for it, which
 * is a deliberate decision, and the deliberate place to split this apart again.</p>
 */
public final class RtpChunkPreloadPolicy {

    private static final String RADIUS_SETTING = "SETTINGS.PRELOAD-RADIUS";
    private static final String CHUNKS_PER_TICK_SETTING = "SETTINGS.PRELOAD-CHUNKS-PER-TICK";
    private static final String CHUNK_THROTTLE_SETTING = "SETTINGS.POST-TELEPORT-CHUNK-THROTTLE";
    private static final String VIEW_DISTANCE_SETTING = "SETTINGS.POST-TELEPORT-VIEW-DISTANCE";

    private static final int DEFAULT_RADIUS = 2;
    private static final int MIN_RADIUS = 2;
    private static final int MAX_RADIUS = 4;
    private static final int DEFAULT_CHUNKS_PER_TICK = 2;
    private static final int MIN_CHUNKS_PER_TICK = 2;
    private static final int DEFAULT_VIEW_DISTANCE = 4;
    private static final int MIN_VIEW_DISTANCE = 2;

    private RtpChunkPreloadPolicy() {
    }

    /**
     * Chunk radius to prepare around an RTP destination.
     *
     * <p>{@code PRELOAD-RADIUS} sets the floor rather than the ceiling. While the post teleport
     * throttle is on, the radius is raised to the distance the player is about to be held at, so
     * that the terrain they can see is the terrain that was made ready. On stock settings that
     * lands on 4 whatever the key says.</p>
     *
     * @param rtp the rtp.yml configuration, or null while it is unavailable
     */
    public static int radius(ConfigurationSection rtp) {
        if (rtp == null) {
            return MIN_RADIUS;
        }
        int radius = Math.max(0, Math.min(MAX_RADIUS, rtp.getInt(RADIUS_SETTING, DEFAULT_RADIUS)));
        if (rtp.getBoolean(CHUNK_THROTTLE_SETTING, true)) {
            radius = Math.max(radius, Math.min(MAX_RADIUS, throttledViewDistance(rtp)));
        }
        return Math.max(MIN_RADIUS, radius);
    }

    /**
     * Chunks to prepare per tick. Anything below {@value #MIN_CHUNKS_PER_TICK} is treated as that,
     * since one chunk a tick stretches a full radius 4 warm-up past four seconds.
     *
     * @param rtp the rtp.yml configuration, or null while it is unavailable
     */
    public static int chunksPerTick(ConfigurationSection rtp) {
        if (rtp == null) {
            return MIN_CHUNKS_PER_TICK;
        }
        return Math.max(MIN_CHUNKS_PER_TICK, rtp.getInt(CHUNKS_PER_TICK_SETTING, DEFAULT_CHUNKS_PER_TICK));
    }

    /**
     * The square of chunks around a centre, nearest first.
     *
     * <p>Ordering by walking distance from the middle matters when the work is spread over several
     * ticks: the ground under the player is ready first, and a run that gives up early has still
     * prepared the part they are standing on.</p>
     */
    public static List<int[]> chunkOrder(int centerChunkX, int centerChunkZ, int radius) {
        List<int[]> chunks = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                chunks.add(new int[]{centerChunkX + dx, centerChunkZ + dz});
            }
        }
        chunks.sort(Comparator.comparingInt(chunk ->
                Math.abs(chunk[0] - centerChunkX) + Math.abs(chunk[1] - centerChunkZ)));
        return chunks;
    }

    /** The view distance the post teleport throttle holds a player at. */
    private static int throttledViewDistance(ConfigurationSection rtp) {
        return Math.max(MIN_VIEW_DISTANCE, rtp.getInt(VIEW_DISTANCE_SETTING, DEFAULT_VIEW_DISTANCE));
    }
}
