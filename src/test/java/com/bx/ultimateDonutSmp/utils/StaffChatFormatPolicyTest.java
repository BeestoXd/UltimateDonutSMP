package com.bx.ultimateDonutSmp.utils;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A placeholder written into the staff chat format used to survive into the printed line as its own
 * name, because the only substitution the format ever got was the four literal tokens below and a
 * PlaceholderAPI pass that ran with no player attached. These pin down who the format is resolved
 * against, and that the sender's own text is not resolved at all.
 */
class StaffChatFormatPolicyTest {

    private static final String FORMAT = "&8[&6StaffChat&8] %luckperms_prefix%&e%player%&7: %message%";

    /** Stands in for PlaceholderAPI resolving against the staff member who sent the message. */
    private static UnaryOperator<String> sender(String prefix) {
        return text -> text.replace("%luckperms_prefix%", prefix);
    }

    @Test
    void aPlaceholderInTheFormatIsFilledInFromTheSender() {
        String line = StaffChatFormatPolicy.render(
                FORMAT, sender("&c[Admin] "), "Crystal", "Notch", "anyone on?");

        assertEquals("&8[&6StaffChat&8] &c[Admin] &eNotch&7: anyone on?", line);
    }

    @Test
    void theFormatReachesTheResolverBeforeAnyTokenIsSubstituted() {
        AtomicReference<String> seen = new AtomicReference<>();

        StaffChatFormatPolicy.render(FORMAT, text -> {
            seen.set(text);
            return text;
        }, "Crystal", "Notch", "anyone on?");

        assertEquals(FORMAT, seen.get());
    }

    @Test
    void aPlaceholderSomebodyTypesIntoStaffChatStaysTheTextTheyTyped() {
        String line = StaffChatFormatPolicy.render(
                FORMAT, sender(""), "Crystal", "Notch", "why is %luckperms_prefix% empty?");

        assertTrue(line.endsWith("why is %luckperms_prefix% empty?"), line);
    }

    @Test
    void theSendersNameIsNotSubstitutedIntoASecondTime() {
        String line = StaffChatFormatPolicy.render(
                "%player%: %message%", null, "Crystal", "Notch", "%player% is me");

        assertEquals("Notch: %player% is me", line);
    }

    @Test
    void aServerStatusNoticeStillFillsInItsStatusWord() {
        String line = StaffChatFormatPolicy.render(
                "&6%server% &eis now %status%&e.", null, "Crystal", "server", "online");

        assertEquals("&6Crystal &eis now online&e.", line);
    }

    @Test
    void aMissingFormatAndMissingValuesRenderAsEmptyText() {
        assertEquals("", StaffChatFormatPolicy.render(null, null, null, null, null));
        assertEquals(": ", StaffChatFormatPolicy.render("%player%: %message%", null, null, null, null));
    }
}
