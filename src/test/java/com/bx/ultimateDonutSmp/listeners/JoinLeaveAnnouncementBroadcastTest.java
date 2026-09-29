package com.bx.ultimateDonutSmp.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JoinLeaveAnnouncementBroadcastTest {

    @Test
    void configuredAnnouncementBroadcastsEvenWhenServerMessageIsNull() {
        assertEquals(
                "&8[&a+&8] &aSteve &7joined the server.",
                PlayerJoinQuitListener.resolveMessageToBroadcast(
                        "&8[&a+&8] &aSteve &7joined the server.",
                        null
                )
        );
        assertEquals(
                "&8[&a+&8] &aSteve &7joined the server.",
                PlayerJoinQuitListener.resolveMessageToBroadcast(
                        "&8[&a+&8] &aSteve &7joined the server.",
                        ""
                )
        );
    }

    @Test
    void configuredAnnouncementTakesPrecedenceOverServerMessage() {
        assertEquals(
                "&8[&a+&8] &aSteve &7joined the server.",
                PlayerJoinQuitListener.resolveMessageToBroadcast(
                        "&8[&a+&8] &aSteve &7joined the server.",
                        "Steve joined the game"
                )
        );
    }

    @Test
    void relaysServerMessageWhenAnnouncementIsOff() {
        assertEquals(
                "Steve joined the game",
                PlayerJoinQuitListener.resolveMessageToBroadcast(null, "Steve joined the game")
        );
    }

    @Test
    void broadcastsNothingWhenBothAnnouncementAndServerMessageAreMissing() {
        assertNull(PlayerJoinQuitListener.resolveMessageToBroadcast(null, null));
        assertNull(PlayerJoinQuitListener.resolveMessageToBroadcast(null, ""));
    }

    @Test
    void onJoinRunsAtNormalPriorityToMatchOnQuit() throws Exception {
        Method onJoin = PlayerJoinQuitListener.class.getMethod("onJoin", PlayerJoinEvent.class);
        Method onQuit = PlayerJoinQuitListener.class.getMethod("onQuit", PlayerQuitEvent.class);

        EventHandler joinAnnotation = onJoin.getAnnotation(EventHandler.class);
        EventHandler quitAnnotation = onQuit.getAnnotation(EventHandler.class);

        assertEquals(EventPriority.NORMAL, joinAnnotation.priority());
        assertEquals(joinAnnotation.priority(), quitAnnotation.priority());
    }
}
