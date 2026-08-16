package com.bx.ultimateDonutSmp.utils;

import org.junit.jupiter.api.Test;

import static com.bx.ultimateDonutSmp.utils.SpigotScheduler.PlayerCommandRoute;
import static com.bx.ultimateDonutSmp.utils.SpigotScheduler.playerCommandRoute;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SpigotSchedulerCommandRouteTest {

    @Test
    void paperRunsInlineOnTheMainThread() {
        assertEquals(PlayerCommandRoute.INLINE, playerCommandRoute(false, true, true));
    }

    @Test
    void paperHandsAnAsyncCallerToTheMainThread() {
        assertEquals(PlayerCommandRoute.GLOBAL_SCHEDULER, playerCommandRoute(false, false, false));
    }

    @Test
    void foliaRunsInlineWhenTheCallerAlreadyOwnsThePlayer() {
        // A staff member clicking their own hotbar item is handled on the region that owns them.
        assertEquals(PlayerCommandRoute.INLINE, playerCommandRoute(true, true, true));
    }

    @Test
    void foliaNeverSendsAPlayerCommandToTheGlobalRegion() {
        // The global region owns no players, so a dispatch there fails a thread check and the
        // command silently never runs.
        assertEquals(PlayerCommandRoute.ENTITY_SCHEDULER, playerCommandRoute(true, true, false));
        assertEquals(PlayerCommandRoute.ENTITY_SCHEDULER, playerCommandRoute(true, false, false));
        assertEquals(PlayerCommandRoute.ENTITY_SCHEDULER, playerCommandRoute(true, false, true));
    }
}
