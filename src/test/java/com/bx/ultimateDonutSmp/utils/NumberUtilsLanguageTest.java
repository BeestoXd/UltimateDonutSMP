package com.bx.ultimateDonutSmp.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberUtilsLanguageTest {

    @AfterEach
    void resetFormatter() {
        NumberUtils.setDurationFormatter(null);
    }

    @Test
    void delegatesAllDurationFormatsToTheActiveLanguageFormatter() {
        NumberUtils.setDurationFormatter(new NumberUtils.DurationFormatter() {
            @Override
            public String formatTime(long totalSeconds) {
                return "short:" + totalSeconds;
            }

            @Override
            public String formatTimeLong(long totalSeconds) {
                return "long:" + totalSeconds;
            }

            @Override
            public String formatCountdown(long totalSeconds) {
                return "countdown:" + totalSeconds;
            }
        });

        assertEquals("short:65", NumberUtils.formatTime(65));
        assertEquals("long:90061", NumberUtils.formatTimeLong(90061));
        assertEquals("countdown:5", NumberUtils.formatCountdown(5));
    }

    @Test
    void retainsLegacyEnglishFormattingBeforeLanguageInitialization() {
        NumberUtils.setDurationFormatter(null);

        assertEquals("1h 1m", NumberUtils.formatTime(3665));
        assertEquals("1d 1h 1m", NumberUtils.formatTimeLong(90061));
        assertEquals("1m 5s", NumberUtils.formatCountdown(65));
    }
}
