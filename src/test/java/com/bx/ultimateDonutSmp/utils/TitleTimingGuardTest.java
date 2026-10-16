package com.bx.ultimateDonutSmp.utils;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TitleTimingGuardTest {

    @Test
    void customTimingsStayRestorableUntilAnotherPluginSendsATitle() {
        UUID player = UUID.randomUUID();
        int token = TitleTimingGuard.own(player);
        try {
            assertTrue(TitleTimingGuard.mayRestore(player, token));

            TitleUtils.noteForeignTitlePacket(player);

            assertFalse(TitleTimingGuard.mayRestore(player, token));
        } finally {
            TitleTimingGuard.release(player);
        }
    }

    @Test
    void ourOwnTitlePacketsDoNotCancelTheRestore() {
        UUID player = UUID.randomUUID();
        int token = TitleTimingGuard.own(player);
        try {
            TitleUtils.whileSendingOwnTitle(() -> TitleUtils.noteForeignTitlePacket(player));

            assertTrue(TitleTimingGuard.mayRestore(player, token));
        } finally {
            TitleTimingGuard.release(player);
        }
    }

    @Test
    void aNewerCustomTitleInvalidatesTheEarlierRestore() {
        UUID player = UUID.randomUUID();
        int first = TitleTimingGuard.own(player);
        int second = TitleTimingGuard.own(player);
        try {
            assertFalse(TitleTimingGuard.mayRestore(player, first));
            assertTrue(TitleTimingGuard.mayRestore(player, second));
        } finally {
            TitleTimingGuard.release(player);
        }
    }

    @Test
    void anotherPlayersTitleDoesNotDropOurs() {
        UUID us = UUID.randomUUID();
        UUID them = UUID.randomUUID();
        int token = TitleTimingGuard.own(us);
        try {
            TitleUtils.noteForeignTitlePacket(them);

            assertTrue(TitleTimingGuard.mayRestore(us, token));
        } finally {
            TitleTimingGuard.release(us);
            TitleTimingGuard.release(them);
        }
    }

    @Test
    void leavingTheServerDropsThePendingRestore() {
        UUID player = UUID.randomUUID();
        int token = TitleTimingGuard.own(player);

        TitleUtils.cancelPendingReset(player);

        assertFalse(TitleTimingGuard.mayRestore(player, token));
    }
}
