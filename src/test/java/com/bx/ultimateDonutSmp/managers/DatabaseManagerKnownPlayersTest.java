package com.bx.ultimateDonutSmp.managers;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseManagerKnownPlayersTest {

    @Test
    void knownPlayerNamesAreSortedAndBlankNamesAreSkipped() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE players (uuid TEXT PRIMARY KEY, username TEXT)");
            statement.execute("INSERT INTO players VALUES ('1', 'Zed')");
            statement.execute("INSERT INTO players VALUES ('2', '')");
            statement.execute("INSERT INTO players VALUES ('3', NULL)");
            statement.execute("INSERT INTO players VALUES ('4', 'alice')");

            DatabaseManager manager = new DatabaseManager(null);
            Field connectionField = DatabaseManager.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            connectionField.set(manager, connection);

            assertEquals(List.of("alice", "Zed"), manager.loadKnownPlayerNames());
        }
    }
}
