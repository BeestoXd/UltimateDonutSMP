package com.bx.ultimateDonutSmp.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Remembers which custom title timings still belong to this plugin.
 *
 * <p>Title timings stick on the client until something sends new ones. After a countdown title
 * with a short stay, {@code TitleUtils} puts the usual 10/70/20 back so the next title is not
 * stuck with that short stay. The packet that does it also changes the timings of whatever title
 * is on screen at that moment, which cuts off an animation another plugin started in between.
 *
 * <p>A restore is allowed only for the token issued when the custom title was sent, and only
 * while no later title packet from anywhere else has been seen for that player.
 */
final class TitleTimingGuard {

    private static final Map<UUID, Integer> OWNED = new ConcurrentHashMap<>();
    private static final AtomicInteger NEXT_TOKEN = new AtomicInteger();

    private TitleTimingGuard() {
    }

    static int own(UUID player) {
        int token = NEXT_TOKEN.incrementAndGet();
        OWNED.put(player, token);
        return token;
    }

    static void release(UUID player) {
        if (player != null) {
            OWNED.remove(player);
        }
    }

    static boolean mayRestore(UUID player, int token) {
        if (player == null || token == 0) {
            return false;
        }
        Integer owned = OWNED.get(player);
        return owned != null && owned.intValue() == token;
    }
}
