package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AreaDeletionLogLineTest {

    private static final String SPAWN_PATH = "LOCATIONS.SPAWN-LOCATION";
    private static final String AFK_PATH = "LOCATIONS.AFK-LOCATION";

    @Test
    void aDeletionThatTookTheSavedSpawnWithItSaysSo() {
        assertEquals(
                "[SpawnManager] Notch deleted spawn area 1 from slot 22, which also cleared "
                        + SPAWN_PATH + ".",
                SpawnManager.describeAreaDeletion("Notch", "spawn", "1", 22, SPAWN_PATH)
        );
    }

    @Test
    void aDeletionThatLeftTheSavedSpawnAloneDoesNotMentionIt() {
        String line = SpawnManager.describeAreaDeletion("Notch", "spawn", "2", 24, null);

        assertEquals("[SpawnManager] Notch deleted spawn area 2 from slot 24.", line);
        assertFalse(line.contains(SPAWN_PATH));
    }

    @Test
    void theAfkMenuIsReportedAgainstItsOwnKey() {
        assertEquals(
                "[SpawnManager] Notch deleted afk area 1 from slot 13, which also cleared "
                        + AFK_PATH + ".",
                SpawnManager.describeAreaDeletion("Notch", "afk", "1", 13, AFK_PATH)
        );
    }

    @Test
    void aDeletionWithNoNameBehindItStillGetsLogged() {
        for (String missing : new String[]{null, "", "   "}) {
            String line = SpawnManager.describeAreaDeletion(missing, "spawn", "1", 22, SPAWN_PATH);
            assertTrue(line.startsWith("[SpawnManager] An unnamed sender deleted spawn area 1"), line);
            assertTrue(line.endsWith("cleared " + SPAWN_PATH + "."), line);
        }
    }
}
