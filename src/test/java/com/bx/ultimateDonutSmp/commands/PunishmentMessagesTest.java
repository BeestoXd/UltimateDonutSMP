package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.models.PunishmentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This text used to be copied into two command classes, and adding a punishment type meant editing
 * both. These walk every constant instead, so the next type cannot reach a player with nothing to
 * say.
 */
class PunishmentMessagesTest {

    private final PunishmentMessages withAdvice = new PunishmentMessages(null, true);
    private final PunishmentMessages withoutAdvice = new PunishmentMessages(null, false);

    @Test
    void everyTypeHasAMessagePathUnderPunishments() {
        for (PunishmentType type : PunishmentType.values()) {
            String path = PunishmentMessages.messagePath(type);
            assertNotNull(path, type.name());
            assertTrue(path.startsWith("PUNISHMENTS."), type + " maps to " + path);
        }
    }

    @Test
    void everyTypeHasSomethingToSayInBothForms() {
        for (PunishmentType type : PunishmentType.values()) {
            assertFalse(PunishmentMessages.messageBody(type).isBlank(), type.name());
            assertFalse(withAdvice.defaultMessage(type).isBlank(), type.name());
            assertFalse(withoutAdvice.defaultMessage(type).isBlank(), type.name());
        }
    }

    @Test
    void theClosingAdviceIsAddedToTheBodyRatherThanReplacingIt() {
        for (PunishmentType type : PunishmentType.values()) {
            String body = PunishmentMessages.messageBody(type);
            assertEquals(body, withoutAdvice.defaultMessage(type), type.name());
            assertTrue(withAdvice.defaultMessage(type).startsWith(body), type.name());
        }
    }

    @Test
    void everyTypeButAWarningCarriesClosingAdvice() {
        for (PunishmentType type : PunishmentType.values()) {
            String advice = PunishmentMessages.closingAdvice(type);
            assertNotNull(advice, type.name());

            if (type == PunishmentType.WARN) {
                assertTrue(advice.isEmpty(), "a warning needs no closing line");
                assertEquals(withoutAdvice.defaultMessage(type), withAdvice.defaultMessage(type));
            } else {
                assertFalse(advice.isBlank(), type.name());
                assertNotEquals(withoutAdvice.defaultMessage(type), withAdvice.defaultMessage(type), type.name());
            }
        }
    }
}
