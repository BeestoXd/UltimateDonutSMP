package com.bx.ultimateDonutSmp.storage;

import com.bx.ultimateDonutSmp.models.ShopPreference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopPreferenceRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void createsSchemaAndPersistsFavorites() throws Exception {
        Path database = tempDir.resolve("shop.db");
        try (Connection connection = open(database); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE existing_data (id INTEGER PRIMARY KEY, value TEXT)");
            statement.execute("INSERT INTO existing_data (id, value) VALUES (1, 'keep')");
        }

        ShopPreferenceRepository repository = repository(database);
        UUID playerId = UUID.randomUUID();
        try {
            repository.initialize().join();

            ShopPreference initial = repository.load(playerId).join();
            assertTrue(initial.favorites().isEmpty());

            repository.setFavorite(playerId, "GEAR-MENU:TOTEM", true).join();
            repository.setFavorite(playerId, "FOOD-MENU:GOLDEN_APPLE", true).join();

            ShopPreference saved = repository.load(playerId).join();
            assertEquals(2, saved.favorites().size());
            assertTrue(saved.favorites().contains("GEAR-MENU:TOTEM"));

            repository.setFavorite(playerId, "GEAR-MENU:TOTEM", false).join();
            ShopPreference updated = repository.load(playerId).join();
            assertFalse(updated.favorites().contains("GEAR-MENU:TOTEM"));

            try (Connection connection = open(database);
                ResultSet resultSet = connection.createStatement()
                         .executeQuery("SELECT value FROM existing_data WHERE id = 1")) {
                assertTrue(resultSet.next());
                assertEquals("keep", resultSet.getString(1));
            }
        } finally {
            repository.shutdown();
        }
    }

    private ShopPreferenceRepository repository(Path database) {
        return new ShopPreferenceRepository(
                () -> open(database),
                sql -> sql,
                false,
                Logger.getLogger("ShopPreferenceRepositoryTest")
        );
    }

    private Connection open(Path database) throws java.sql.SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database.toAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("PRAGMA busy_timeout=10000");
        }
        return connection;
    }
}
