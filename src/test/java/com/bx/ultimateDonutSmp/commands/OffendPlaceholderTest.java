package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentScope;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OffendPlaceholderTest {

    @Test
    void testPunishmentPlaceholdersContainAllExpiryVariants() {
        PunishmentRecord record = new PunishmentRecord(
                101L,
                UUID.randomUUID(),
                "TargetPlayer",
                PunishmentType.BAN,
                "Hacking / Cheating",
                UUID.randomUUID(),
                "StaffAdmin",
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L,
                null,
                null,
                null,
                null,
                "local",
                PunishmentScope.SERVER
        );

        String[] placeholdersArray = new PunishmentMessages(null, false).placeholders(record);

        assertNotNull(placeholdersArray);
        assertTrue(placeholdersArray.length >= 10);

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i + 1 < placeholdersArray.length; i += 2) {
            map.put(placeholdersArray[i], placeholdersArray[i + 1]);
        }

        // Verify all expiry date placeholder keys are mapped and non-empty
        String[] expiryKeys = new String[]{
                "%nicest_expiration%", "{nicest_expiration}",
                "%expires%", "{expires}",
                "%expires_at%", "{expires_at}",
                "%expiration%", "{expiration}",
                "%expiry%", "{expiry}",
                "%duration%", "{duration}"
        };

        for (String key : expiryKeys) {
            assertTrue(map.containsKey(key), "Missing key in placeholders: " + key);
            assertNotNull(map.get(key), "Null value for key: " + key);
            assertFalse(map.get(key).isBlank(), "Blank value for key: " + key);
            assertNotEquals(key, map.get(key), "Placeholder not replaced for key: " + key);
        }

        // Verify reason, issuer, player, id
        assertEquals("Hacking / Cheating", map.get("%reason%"));
        assertEquals("Hacking / Cheating", map.get("{reason}"));
        assertEquals("StaffAdmin", map.get("%issuer%"));
        assertEquals("StaffAdmin", map.get("{issuer}"));
        assertEquals("TargetPlayer", map.get("%player%"));
        assertEquals("TargetPlayer", map.get("{player}"));
        assertEquals("101", map.get("%id%"));
        assertEquals("101", map.get("{id}"));
    }

    @Test
    void testPermanentPunishmentExpirationFormat() {
        PunishmentRecord record = new PunishmentRecord(
                102L,
                UUID.randomUUID(),
                "TargetPlayer",
                PunishmentType.BAN,
                "Permanent Ban Reason",
                UUID.randomUUID(),
                "Console",
                System.currentTimeMillis(),
                null, // null expiresAt = Permanent
                null,
                null,
                null,
                null,
                "local",
                PunishmentScope.SERVER
        );

        String[] placeholdersArray = new PunishmentMessages(null, false).placeholders(record);

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i + 1 < placeholdersArray.length; i += 2) {
            map.put(placeholdersArray[i], placeholdersArray[i + 1]);
        }

        assertEquals("Permanent", map.get("%expires%"));
        assertEquals("Permanent", map.get("{expires_at}"));
        assertEquals("Permanent", map.get("%nicest_expiration%"));
    }
}
