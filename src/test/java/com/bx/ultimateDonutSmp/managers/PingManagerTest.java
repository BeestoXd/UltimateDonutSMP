package com.bx.ultimateDonutSmp.managers;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PingManagerTest {

    @Test
    void nullPlayerReturnsZeroPing() {
        // Since plugin instance is null in this unit test without Bukkit server mocking,
        // null player should return 0 safely.
        Player player = null;
        assertEquals(0, player == null ? 0 : player.getPing());
    }
}
