package com.bx.ultimateDonutSmp.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaintenanceSetLobbyTest {

    @Test
    void leavingTheServerNameOffClearsTheLobby() {
        assertTrue(MaintenanceCommand.clearsLobbyServer(new String[]{"setlobby"}));
        assertTrue(MaintenanceCommand.clearsLobbyServer(new String[]{"setlobby", ""}));
        assertTrue(MaintenanceCommand.clearsLobbyServer(new String[]{"setlobby", "  "}));
    }

    @Test
    void aNamedServerStillSetsIt() {
        assertFalse(MaintenanceCommand.clearsLobbyServer(new String[]{"setlobby", "hub"}));
    }

    @Test
    void statusSpellsOutWhatNoLobbyMeans() {
        assertEquals("&bhub", MaintenanceCommand.describeLobbyServer("hub"));
        assertEquals(
                "&7none, so players without the bypass permission cannot connect",
                MaintenanceCommand.describeLobbyServer("")
        );
        assertEquals(
                "&7none, so players without the bypass permission cannot connect",
                MaintenanceCommand.describeLobbyServer(null)
        );
    }
}
