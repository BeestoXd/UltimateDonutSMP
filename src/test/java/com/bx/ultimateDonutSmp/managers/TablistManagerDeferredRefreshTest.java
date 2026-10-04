package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TablistManagerDeferredRefreshTest {

    @Test
    void deferredTablistNameRefreshUsesTwoJoinSafeDelays() {
        assertEquals(2, TablistManager.DEFERRED_TABLIST_NAME_REFRESH_DELAYS.size());
        assertEquals(8L, TablistManager.DEFERRED_TABLIST_NAME_REFRESH_DELAYS.get(0));
        assertEquals(20L, TablistManager.DEFERRED_TABLIST_NAME_REFRESH_DELAYS.get(1));
    }
}
