package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.VoiceChatConsent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VoiceChatConsentPersistenceTest {

    @Test
    void newPlayersStartUndecided() {
        assertEquals(VoiceChatConsent.UNDECIDED,
                new PlayerData(UUID.randomUUID(), "Bob").getVoiceChatConsent());
    }

    @Test
    void unknownStoredValuesFallBackToUndecided() {
        assertEquals(VoiceChatConsent.UNDECIDED, VoiceChatConsent.fromInt(-1));
        assertEquals(VoiceChatConsent.UNDECIDED, VoiceChatConsent.fromInt(7));
        assertEquals(VoiceChatConsent.UNDECIDED, VoiceChatConsent.fromInt(0));
        assertEquals(VoiceChatConsent.ACCEPTED, VoiceChatConsent.fromInt(1));
        assertEquals(VoiceChatConsent.DECLINED, VoiceChatConsent.fromInt(2));
    }

    @Test
    void nullConsentIsStoredAsUndecided() {
        PlayerData data = new PlayerData(UUID.randomUUID(), "Bob");
        data.setVoiceChatConsent(VoiceChatConsent.ACCEPTED);
        data.setVoiceChatConsent(null);
        assertEquals(VoiceChatConsent.UNDECIDED, data.getVoiceChatConsent());
    }

    @Test
    void consentSurvivesASaveAndLoadRoundTrip() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseManager manager = new DatabaseManager(null);

            Field connectionField = DatabaseManager.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            connectionField.set(manager, connection);

            Method createTables = DatabaseManager.class.getDeclaredMethod("createTables");
            createTables.setAccessible(true);
            createTables.invoke(manager);

            UUID uuid = UUID.randomUUID();
            PlayerData data = new PlayerData(uuid, "Bob");
            data.setVoiceChatConsent(VoiceChatConsent.DECLINED);
            manager.savePlayer(data);

            PlayerData loaded = manager.loadPlayer(uuid);
            assertNotNull(loaded);
            assertEquals(VoiceChatConsent.DECLINED, loaded.getVoiceChatConsent());
        }
    }
}
