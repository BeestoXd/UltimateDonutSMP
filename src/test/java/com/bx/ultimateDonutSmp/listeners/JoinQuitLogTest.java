package com.bx.ultimateDonutSmp.listeners;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Join and leave chat is resent per player, which never reaches latest.log. These pin the
 * console lines that replace that missing record, and the switch that can silence them.
 */
class JoinQuitLogTest {

    @Test
    void bundledConfigShipsJoinQuitLoggingOn() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));
        assertTrue(config.contains(PlayerJoinQuitListener.JOIN_QUIT_LOG_PATH));
        assertTrue(config.getBoolean(PlayerJoinQuitListener.JOIN_QUIT_LOG_PATH));
    }

    @Test
    void aJoinLineNamesThePlayer() {
        assertEquals("[JOIN] Steve", PlayerJoinQuitListener.joinQuitLogLine("JOIN", "Steve"));
    }

    @Test
    void aQuitLineNamesThePlayer() {
        assertEquals("[QUIT] Alex", PlayerJoinQuitListener.joinQuitLogLine("QUIT", "Alex"));
    }

    @Test
    void aBlankNameIsNotLogged() {
        assertNull(PlayerJoinQuitListener.joinQuitLogLine("JOIN", " "));
        assertNull(PlayerJoinQuitListener.joinQuitLogLine("QUIT", null));
        assertNull(PlayerJoinQuitListener.joinQuitLogLine("", "Steve"));
    }
}
