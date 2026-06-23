package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RTPZoneManagerTest {

    @Test
    void bundledConfigDefinesDefaultTitleFadeOut() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/config.yml")) {
            assertNotNull(input);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(input, StandardCharsets.UTF_8)
            );
            assertEquals(10, config.getInt("RTP-ZONE.TITLE-FADE-OUT-TICKS"));
        }
    }

    @Test
    void titleFadeOutIsClampedToAtLeastOneTick() {
        assertEquals(1, RTPZoneManager.normalizeTitleFadeOutTicks(-10));
        assertEquals(1, RTPZoneManager.normalizeTitleFadeOutTicks(0));
        assertEquals(10, RTPZoneManager.normalizeTitleFadeOutTicks(10));
    }
}
