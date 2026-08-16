package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.models.PunishmentFilterState;
import com.bx.ultimateDonutSmp.models.PunishmentQuery;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentScope;
import com.bx.ultimateDonutSmp.models.PunishmentSortOrder;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PunishmentListQueryTest {

    private static final long HOUR = 3_600_000L;

    private Connection connection;
    private DatabaseManager dbManager;
    private long now;

    private UUID rodney;
    private UUID rodneyAlt;
    private UUID mara;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = connection.createStatement()) {
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

        now = System.currentTimeMillis();
        rodney = UUID.randomUUID();
        rodneyAlt = UUID.randomUUID();
        mara = UUID.randomUUID();

        // Oldest first so the ids ascend with issued_at.
        insert(mara, "Mara_", PunishmentType.WARN, now - 4 * HOUR, null, false);
        insert(rodneyAlt, "Rodney_Alt", PunishmentType.MUTE, now - 3 * HOUR, now + HOUR, false);
        insert(rodney, "Cuteboyrodney", PunishmentType.BAN, now - 2 * HOUR, now - HOUR, false);
        insert(rodney, "Cuteboyrodney", PunishmentType.BAN, now - HOUR, null, true);
        insert(mara, "Mara_", PunishmentType.BAN, now - 30 * 60_000L, null, false);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void insert(UUID target, String name, PunishmentType type, long issuedAt, Long expiresAt, boolean removed) {
        dbManager.createPunishmentRecord(new PunishmentRecord(
                0L,
                target,
                name,
                type,
                "Testing",
                null,
                "console",
                issuedAt,
                expiresAt,
                null,
                "",
                null,
                "",
                "local",
                PunishmentScope.SERVER
        ));

        if (removed) {
            List<PunishmentRecord> all = dbManager.loadAllPunishments(
                    PunishmentQuery.defaultQuery(), null, 100, 0, now);
            dbManager.markPunishmentRemoved(all.get(0).getId(), null, "console", issuedAt, "appealed");
        }
    }

    private List<String> names(List<PunishmentRecord> records) {
        return records.stream().map(PunishmentRecord::getTargetNameSnapshot).toList();
    }

    @Test
    void listsEveryPlayerNewestFirst() {
        List<PunishmentRecord> records = dbManager.loadAllPunishments(
                PunishmentQuery.defaultQuery(), null, 45, 0, now);

        assertEquals(5, records.size());
        assertEquals(5, dbManager.countAllPunishments(PunishmentQuery.defaultQuery(), null, now));
        assertEquals(
                List.of("Mara_", "Cuteboyrodney", "Cuteboyrodney", "Rodney_Alt", "Mara_"),
                names(records)
        );
    }

    @Test
    void oldestSortOrderReversesTheList() {
        List<PunishmentRecord> records = dbManager.loadAllPunishments(
                new PunishmentQuery(null, PunishmentFilterState.ALL, PunishmentSortOrder.OLDEST), null, 45, 0, now);

        assertEquals(
                List.of("Mara_", "Rodney_Alt", "Cuteboyrodney", "Cuteboyrodney", "Mara_"),
                names(records)
        );
    }

    @Test
    void typeFilterNarrowsTheList() {
        PunishmentQuery bansOnly = new PunishmentQuery(PunishmentType.BAN, PunishmentFilterState.ALL, null);

        assertEquals(3, dbManager.countAllPunishments(bansOnly, null, now));
        assertTrue(dbManager.loadAllPunishments(bansOnly, null, 45, 0, now).stream()
                .allMatch(record -> record.getType() == PunishmentType.BAN));
    }

    @Test
    void activeFilterDropsExpiredAndRemovedRecords() {
        PunishmentQuery active = new PunishmentQuery(null, PunishmentFilterState.ACTIVE, null);
        List<PunishmentRecord> records = dbManager.loadAllPunishments(active, null, 45, 0, now);

        // The expired ban and the removed ban both drop out; the temporary mute is still running.
        assertEquals(List.of("Mara_", "Rodney_Alt", "Mara_"), names(records));
        assertEquals(3, dbManager.countAllPunishments(active, null, now));
    }

    @Test
    void searchMatchesPartialNamesIgnoringCase() {
        List<PunishmentRecord> records = dbManager.loadAllPunishments(
                PunishmentQuery.defaultQuery(), "rodney", 45, 0, now);

        assertEquals(3, records.size());
        assertEquals(3, dbManager.countAllPunishments(PunishmentQuery.defaultQuery(), "rodney", now));
        assertTrue(records.stream().allMatch(record ->
                record.getTargetNameSnapshot().toLowerCase().contains("rodney")));
    }

    @Test
    void searchMatchesAnExactUuid() {
        List<PunishmentRecord> records = dbManager.loadAllPunishments(
                PunishmentQuery.defaultQuery(), mara.toString(), 45, 0, now);

        assertEquals(2, records.size());
        assertTrue(records.stream().allMatch(record -> mara.equals(record.getTargetUuid())));
    }

    @Test
    void searchTreatsLikeWildcardsAsLiteralText() {
        // "%" and "_" would otherwise match everything / any single character.
        assertEquals(0, dbManager.countAllPunishments(PunishmentQuery.defaultQuery(), "%", now));
        assertEquals(0, dbManager.countAllPunishments(PunishmentQuery.defaultQuery(), "Rodney%Alt", now));
        assertEquals(0, dbManager.countAllPunishments(PunishmentQuery.defaultQuery(), "Mara!", now));

        // A real underscore still matches the player actually called "Mara_".
        assertEquals(2, dbManager.countAllPunishments(PunishmentQuery.defaultQuery(), "Mara_", now));
        assertEquals(0, dbManager.countAllPunishments(PunishmentQuery.defaultQuery(), "MaraX", now));
    }

    @Test
    void searchCombinesWithFilters() {
        PunishmentQuery activeBans = new PunishmentQuery(PunishmentType.BAN, PunishmentFilterState.ACTIVE, null);

        assertEquals(1, dbManager.countAllPunishments(activeBans, "mara", now));
        assertEquals(0, dbManager.countAllPunishments(activeBans, "rodney", now));
    }

    @Test
    void paginationWalksTheListWithoutRepeatingRecords() {
        List<PunishmentRecord> firstPage = dbManager.loadAllPunishments(
                PunishmentQuery.defaultQuery(), null, 2, 0, now);
        List<PunishmentRecord> secondPage = dbManager.loadAllPunishments(
                PunishmentQuery.defaultQuery(), null, 2, 2, now);
        List<PunishmentRecord> lastPage = dbManager.loadAllPunishments(
                PunishmentQuery.defaultQuery(), null, 2, 4, now);

        assertEquals(2, firstPage.size());
        assertEquals(2, secondPage.size());
        assertEquals(1, lastPage.size());
        assertTrue(firstPage.stream().noneMatch(first ->
                secondPage.stream().anyMatch(second -> second.getId() == first.getId())));
    }

    @Test
    void blankSearchIsTreatedAsNoSearch() {
        assertEquals(5, dbManager.countAllPunishments(PunishmentQuery.defaultQuery(), "   ", now));
    }
}
