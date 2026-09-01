package com.bx.ultimateDonutSmp.listeners;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaintenanceLoginGateTest {

    @Test
    void maintenanceWithNowhereToSendThemRefusesTheLogin() {
        assertTrue(PlayerJoinQuitListener.deniesMaintenanceLogin(true, false, false));
    }

    @Test
    void everyoneElseIsStillLetThrough() {
        assertFalse(PlayerJoinQuitListener.deniesMaintenanceLogin(false, false, false));
        assertFalse(PlayerJoinQuitListener.deniesMaintenanceLogin(true, true, false));
        assertFalse(PlayerJoinQuitListener.deniesMaintenanceLogin(true, false, true));
    }
}
