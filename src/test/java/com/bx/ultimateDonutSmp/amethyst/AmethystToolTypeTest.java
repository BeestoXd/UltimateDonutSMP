package com.bx.ultimateDonutSmp.amethyst;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AmethystToolTypeTest {

    @Test
    void tabCompletionNamesParseWhenTheJvmIsTurkish() {
        Locale previous = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        try {
            for (AmethystToolType type : AmethystToolType.values()) {
                String completion = type.name().toLowerCase(Locale.ROOT).replace('_', '-');
                assertEquals(type, AmethystToolType.fromString(completion), completion);
            }
        } finally {
            Locale.setDefault(previous);
        }
    }
}
