package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The MOTD a server writes for itself, and what reaches the multiplayer list entry from it.
 */
class ServerListMotdTest {

    private static final List<String> LINES =
            List.of("&d&lUltimateDonutSMP", "&7%online%&8/&7%max_players% &7online");

    @Test
    void bothCountsLandOnTheLineThatAsksForThem() {
        assertEquals(
                "&d&lUltimateDonutSMP\n&712&8/&760 &7online",
                ServerListProtocolLibBridge.renderMotd(LINES, 12, 60)
        );
    }

    @Test
    void everyConfiguredLineIsKeptSoTheEntryLooksAsItWasWritten() {
        assertEquals("one\ntwo\nthree", ServerListProtocolLibBridge.renderMotd(List.of("one", "two", "three"), 0, 20));
        assertEquals("alone", ServerListProtocolLibBridge.renderMotd(List.of("alone"), 0, 20));
        assertEquals("", ServerListProtocolLibBridge.renderMotd(List.of(), 0, 20));
        assertEquals("", ServerListProtocolLibBridge.renderMotd(null, 0, 20));
    }

    @Test
    void aLineWithoutThePlaceholdersIsLeftAlone() {
        assertEquals("untouched", ServerListProtocolLibBridge.applyCounts("untouched", 12, 60));
        assertEquals("", ServerListProtocolLibBridge.applyCounts(null, 12, 60));
        assertEquals("", ServerListProtocolLibBridge.applyCounts("", 12, 60));
    }

    @Test
    void anEmptyServerStillReadsAsAnEmptyServerRatherThanAsAToken() {
        String motd = ServerListProtocolLibBridge.renderMotd(LINES, 0, 60);

        assertFalse(motd.contains("%online%"), "an unresolved placeholder would be printed on the server list as it is");
        assertEquals("&d&lUltimateDonutSMP\n&70&8/&760 &7online", motd);
    }

    @Test
    void theBundledBlockShipsSwitchedOffSoUpdatingTheJarLeavesTheEntryAlone() {
        YamlConfiguration config = bundledConfig();

        assertFalse(config.getBoolean("SERVER-LIST.ENABLED"));
        assertEquals(LINES, config.getStringList("SERVER-LIST.MOTD"));
    }

    @Test
    void theBundledLinesAreReadyToTurnOnWithoutBeingRewritten() {
        assertEquals(
                "&d&lUltimateDonutSMP\n&73&8/&720 &7online",
                ServerListProtocolLibBridge.renderMotd(bundledConfig().getStringList("SERVER-LIST.MOTD"), 3, 20)
        );
    }

    private static YamlConfiguration bundledConfig() {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));
    }
}
