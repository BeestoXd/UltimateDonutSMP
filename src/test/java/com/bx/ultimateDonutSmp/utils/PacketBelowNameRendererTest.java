package com.bx.ultimateDonutSmp.utils;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The score packet carries two optionals and the constructor sees both as bare {@link Optional},
 * so the balance can only be put in the right one by reading what the field declares. These stand a
 * record in for the packet, in the shapes Minecraft has shipped it in.
 */
class PacketBelowNameRendererTest {

    /** Stand-in for a number format, named so the generic type reads the way Minecraft's does. */
    private interface NumberFormat {
    }

    private record ScorePacket(String owner, String objectiveName, int score,
                               Optional<String> display, Optional<NumberFormat> numberFormat) {
    }

    private record ReorderedScorePacket(String owner, String objectiveName, int score,
                                        Optional<NumberFormat> numberFormat, Optional<String> display) {
    }

    private record DisplayOnlyScorePacket(String owner, String objectiveName, int score,
                                          Optional<String> display) {
    }

    @Test
    void theBalanceGoesInTheOptionalThatDeclaresANumberFormat() {
        assertEquals(1, PacketBelowNameRenderer.numberFormatOptional(ScorePacket.class, 2));
    }

    @Test
    void reorderingTheOptionalsMovesTheBalanceWithThem() {
        assertEquals(0, PacketBelowNameRenderer.numberFormatOptional(ReorderedScorePacket.class, 2));
    }

    @Test
    void aPacketWithNoNumberFormatFallsBackToTheLastOptional() {
        assertEquals(0, PacketBelowNameRenderer.numberFormatOptional(DisplayOnlyScorePacket.class, 1));
    }

    @Test
    void aPacketThatIsNotARecordFallsBackToTheLastOptional() {
        assertEquals(1, PacketBelowNameRenderer.numberFormatOptional(String.class, 2));
    }
}
