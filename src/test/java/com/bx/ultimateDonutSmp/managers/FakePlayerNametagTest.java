package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakePlayerNametagTest {

    @Test
    void hiddenProfileNameIsUniqueAndLegal() {
        UUID fakeUuid = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        String name = FakePlayerManager.hiddenProfileName(fakeUuid);

        assertEquals(16, name.length());
        assertTrue(name.startsWith("fp"));
        assertTrue(name.matches("[A-Za-z0-9_]+"));
        assertEquals(name, FakePlayerManager.hiddenProfileName(fakeUuid));
        assertNotEquals(name, FakePlayerManager.hiddenProfileName(UUID.randomUUID()));
    }

    @Test
    void bundledStaffModeHidesNametagsByDefault() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.load(Path.of("src/main/resources", "staff-mode.yml").toFile());

        assertTrue(config.getBoolean("FAKE-PLAYER.HIDE-NAMETAG", false),
                "staff-mode.yml must ship HIDE-NAMETAG as true so a config restore keeps prefix and money off the bait");
    }
}
