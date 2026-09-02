package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PunishmentFilterState;
import com.bx.ultimateDonutSmp.models.PunishmentQuery;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentScope;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A voice mute is stored as its own punishment type, so the queries the microphone gate relies on
 * have to keep it apart from an ordinary chat mute and stop reporting it once it expires.
 */
class VoiceMutePunishmentTest {

    private static final String NAME = "Loris";

    private Connection connection;
    private DatabaseManager dbManager;
    private UUID target;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        dbManager = new DatabaseManager(null);

        Field connectionField = DatabaseManager.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(dbManager, connection);

        Method createTables = DatabaseManager.class.getDeclaredMethod("createTables");
        createTables.setAccessible(true);
        createTables.invoke(dbManager);

        target = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void theTypeSurvivesTheRoundTripThroughTheTextColumn() {
        record(PunishmentType.VOICE_MUTE, null);

        List<PunishmentRecord> stored = active(PunishmentType.VOICE_MUTE);
        assertEquals(1, stored.size());
        assertSame(PunishmentType.VOICE_MUTE, stored.get(0).getType());
        assertSame(PunishmentType.VOICE_MUTE, PunishmentType.fromString("VOICE_MUTE", PunishmentType.WARN));
    }

    @Test
    void aVoiceMuteIsNotAChatMuteAndAChatMuteIsNotAVoiceMute() {
        record(PunishmentType.VOICE_MUTE, null);

        assertEquals(1, active(PunishmentType.VOICE_MUTE).size());
        assertTrue(active(PunishmentType.MUTE).isEmpty(), "a voice mute must not silence chat as well");

        record(PunishmentType.MUTE, null);

        assertEquals(1, active(PunishmentType.MUTE).size());
        assertEquals(1, active(PunishmentType.VOICE_MUTE).size());
    }

    @Test
    void anExpiredVoiceMuteStopsBeingActive() {
        record(PunishmentType.VOICE_MUTE, System.currentTimeMillis() - 1_000L);

        assertTrue(active(PunishmentType.VOICE_MUTE).isEmpty());
    }

    @Test
    void aRemovedVoiceMuteStopsBeingActive() {
        long id = record(PunishmentType.VOICE_MUTE, null);

        assertTrue(dbManager.markPunishmentRemoved(id, null, "console", System.currentTimeMillis(), "vcunmute"));
        assertTrue(active(PunishmentType.VOICE_MUTE).isEmpty());
    }

    @Test
    void deletingTheRecordFromTheGuiAlsoEndsTheMute() {
        long id = record(PunishmentType.VOICE_MUTE, null);
        assertNotNull(dbManager.loadPunishmentRecord(id));

        assertTrue(dbManager.deletePunishmentRecord(id));
        assertTrue(active(PunishmentType.VOICE_MUTE).isEmpty());

        // The row is unreadable once it is gone, so anything that reacts to the delete has to
        // capture the record first. PunishmentManager.deleteRecord loads it before deleting.
        assertNull(dbManager.loadPunishmentRecord(id));
    }

    private long record(PunishmentType type, Long expiresAt) {
        long id = dbManager.createPunishmentRecord(new PunishmentRecord(
                0L,
                target,
                NAME,
                type,
                "shouting",
                null,
                "console",
                System.currentTimeMillis(),
                expiresAt,
                null,
                "",
                null,
                "",
                "local",
                PunishmentScope.SERVER
        ));
        assertTrue(id > 0L);
        return id;
    }

    private List<PunishmentRecord> active(PunishmentType type) {
        return dbManager.loadPunishmentHistory(
                target,
                NAME,
                new PunishmentQuery(type, PunishmentFilterState.ACTIVE, null),
                10,
                0,
                System.currentTimeMillis()
        );
    }
}
