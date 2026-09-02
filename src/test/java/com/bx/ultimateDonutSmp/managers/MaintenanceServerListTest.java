package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The text that lands on the multiplayer list entry while maintenance mode is on.
 */
class MaintenanceServerListTest {

    private static final List<String> LINES =
            List.of("&cCurrently under maintenance", "&bCome back in: &d%time%");

    @Test
    void theCountdownLandsOnTheSecondLineOfTheMotd() {
        assertEquals(
                "&cCurrently under maintenance\n&bCome back in: &d03:29",
                MaintenanceProtocolLibBridge.renderMotd(LINES, 209L)
        );
    }

    @Test
    void everyConfiguredLineIsKeptSoTheEntryLooksAsItWasWritten() {
        assertEquals("one\ntwo\nthree", MaintenanceProtocolLibBridge.renderMotd(List.of("one", "two", "three"), 30L));
        assertEquals("alone", MaintenanceProtocolLibBridge.renderMotd(List.of("alone"), 30L));
        assertEquals("", MaintenanceProtocolLibBridge.renderMotd(List.of(), 30L));
        assertEquals("", MaintenanceProtocolLibBridge.renderMotd(null, 30L));
    }

    @Test
    void withNoDeadlineThePlaceholderIsEmptiedRatherThanShownToPlayers() {
        String motd = MaintenanceProtocolLibBridge.renderMotd(LINES, MaintenanceManager.NO_DEADLINE);

        assertFalse(motd.contains("%time%"), "an unresolved placeholder would be printed on the server list as it is");
        assertEquals("&cCurrently under maintenance\n&bCome back in: &d", motd);
    }

    @Test
    void aLineWithoutThePlaceholderIsLeftAlone() {
        assertEquals("untouched", MaintenanceProtocolLibBridge.applyCountdown("untouched", 30L));
        assertEquals("back soon: 00:30", MaintenanceProtocolLibBridge.applyCountdown("back soon: %time%", 30L));
        assertEquals("", MaintenanceProtocolLibBridge.applyCountdown(null, 30L));
    }

    @Test
    void bundledConfigShipsTheServerListBlock() {
        YamlConfiguration network = YamlConfiguration.loadConfiguration(new File("src/main/resources/network.yml"));

        assertTrue(network.getBoolean("MAINTENANCE.SERVER_LIST.ENABLED"));
        assertEquals(LINES, network.getStringList("MAINTENANCE.SERVER_LIST.LINES"));
        assertEquals(
                List.of("&cCurrently under maintenance", "&7come back later"),
                network.getStringList("MAINTENANCE.SERVER_LIST.LINES_NO_TIMER")
        );
        assertEquals("&cMaintenance", network.getString("MAINTENANCE.SERVER_LIST.VERSION_LABEL"));
        assertEquals(List.of("&cCurrently under maintenance"), network.getStringList("MAINTENANCE.SERVER_LIST.HOVER"));
    }

    @Test
    void theBundledDefaultsProduceTheEntryTheRequestAskedFor() {
        YamlConfiguration network = YamlConfiguration.loadConfiguration(new File("src/main/resources/network.yml"));

        assertEquals(
                "&cCurrently under maintenance\n&bCome back in: &d03:29",
                MaintenanceProtocolLibBridge.renderMotd(network.getStringList("MAINTENANCE.SERVER_LIST.LINES"), 209L)
        );
    }
}
