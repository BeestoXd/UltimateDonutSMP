package com.bx.ultimateDonutSmp.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * network.yml used to ship a STAFF_ALERTS_LOCAL_FALLBACK_ON_REDIS_ERROR key that no code read,
 * beside a staff chat key whose comment described local delivery it does not decide. Helpop and
 * report alerts reach the staff on this server before Redis is attempted at all, so the dead key
 * could never have changed anything either way. These keep the shipped file honest about which
 * knobs actually exist.
 */
class NetworkRedisFallbackConfigurationTest {

    private static YamlConfiguration network() {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/network.yml"));
    }

    @Test
    void theAlertLocalFallbackKeyIsNoLongerShipped() {
        assertFalse(
                network().contains("NETWORK.STAFF_ALERTS_LOCAL_FALLBACK_ON_REDIS_ERROR"),
                "no code reads this key, so shipping it advertises a setting that does nothing"
        );
    }

    @Test
    void theStaffChatWarningKeyStillShipsUnderTheNameTheCodeReads() {
        YamlConfiguration network = network();

        assertTrue(network.contains("NETWORK.SEND_LOCAL_FALLBACK_ON_REDIS_ERROR"),
                "NetworkStaffChatManager reads this exact path, and renaming it would silently reset"
                        + " servers that had turned the warning off");
        assertTrue(network.getBoolean("NETWORK.SEND_LOCAL_FALLBACK_ON_REDIS_ERROR"));
    }

    @Test
    void theAlertWarningKeyIsTheOneThatStaysOffByDefault() {
        YamlConfiguration network = network();

        assertTrue(network.contains("NETWORK.STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR"));
        assertFalse(network.getBoolean("NETWORK.STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR"));
    }
}
