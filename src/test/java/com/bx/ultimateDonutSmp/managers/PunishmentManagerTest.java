package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentScope;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PunishmentManagerTest {

    private Connection connection;
    private DatabaseManager dbManager;
    private PunishmentManager punishmentManager;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "  uuid TEXT PRIMARY KEY," +
                    "  username TEXT" +
                    ")");
            st.execute("CREATE TABLE IF NOT EXISTS punishments (" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "  target_uuid TEXT NOT NULL," +
                    "  target_name_snapshot TEXT," +
                    "  type TEXT NOT NULL," +
                    "  reason TEXT NOT NULL," +
                    "  issuer_uuid TEXT," +
                    "  issuer_name_snapshot TEXT," +
                    "  issued_at INTEGER NOT NULL," +
                    "  expires_at INTEGER," +
                    "  removed_by_uuid TEXT," +
                    "  removed_by_name_snapshot TEXT," +
                    "  removed_at INTEGER," +
                    "  removal_reason TEXT," +
                    "  source_server TEXT DEFAULT 'local'," +
                    "  scope TEXT DEFAULT 'SERVER'" +
                    ")");
        }

        dbManager = new DatabaseManager(null);
        Field connectionField = DatabaseManager.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(dbManager, connection);

        punishmentManager = new PunishmentManager(null);
        Field dbField = PunishmentManager.class.getDeclaredField("plugin");
        // Create dummy plugin proxy if needed or test dbManager directly
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void unbansPlayerEvenWhenStoredTargetUuidDiffers() throws Exception {
        UUID onlineUuid = UUID.randomUUID();
        UUID offlineUuid = UUID.randomUUID();
        String username = "Cuteboyrodney";

        // Simulate player recorded in players table with onlineUuid
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO players VALUES ('" + onlineUuid + "', '" + username + "')");
        }

        // Create ban record using offlineUuid and username snapshot
        long id = dbManager.createPunishmentRecord(new PunishmentRecord(
                0L,
                offlineUuid,
                username,
                PunishmentType.BAN,
                "Cheating",
                null,
                "console",
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L,
                null,
                "",
                null,
                "",
                "local",
                PunishmentScope.SERVER
        ));
        assertTrue(id > 0L);

        // Verify active punishment exists when querying by onlineUuid AND username
        var active = dbManager.loadPunishmentHistory(
                onlineUuid,
                username,
                new com.bx.ultimateDonutSmp.models.PunishmentQuery(
                        PunishmentType.BAN,
                        com.bx.ultimateDonutSmp.models.PunishmentFilterState.ACTIVE,
                        null
                ),
                1,
                0,
                System.currentTimeMillis()
        );
        assertFalse(active.isEmpty(), "Should find active ban record matching username snapshot");

        // Perform removal/unban using onlineUuid and username
        boolean removed = dbManager.markPunishmentRemoved(
                active.get(0).getId(),
                null,
                "console",
                System.currentTimeMillis(),
                "removed by staff"
        );
        assertTrue(removed, "Should mark active punishment as removed");

        // Verify no active punishment remaining
        var remaining = dbManager.loadPunishmentHistory(
                onlineUuid,
                username,
                new com.bx.ultimateDonutSmp.models.PunishmentQuery(
                        PunishmentType.BAN,
                        com.bx.ultimateDonutSmp.models.PunishmentFilterState.ACTIVE,
                        null
                ),
                1,
                0,
                System.currentTimeMillis()
        );
        assertTrue(remaining.isEmpty(), "Active punishment list should now be empty");
    }
}
