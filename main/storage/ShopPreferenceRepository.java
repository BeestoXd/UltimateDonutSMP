package com.bx.ultimateDonutSmp.storage;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.ShopPreference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

public final class ShopPreferenceRepository {

    private final ConnectionFactory connectionFactory;
    private final UnaryOperator<String> schemaAdapter;
    private final boolean mySql;
    private final Logger logger;
    private final ExecutorService worker;
    private volatile Connection connection;
    private volatile boolean closed;

    public ShopPreferenceRepository(UltimateDonutSmp plugin) {
        this(
                () -> plugin.getDatabaseManager().openDedicatedConnection(),
                plugin.getDatabaseManager()::adaptSchemaSql,
                plugin.getDatabaseManager().isMySql(),
                plugin.getLogger()
        );
    }

    ShopPreferenceRepository(
            ConnectionFactory connectionFactory,
            UnaryOperator<String> schemaAdapter,
            boolean mySql,
            Logger logger
    ) {
        this.connectionFactory = connectionFactory;
        this.schemaAdapter = schemaAdapter;
        this.mySql = mySql;
        this.logger = logger;
        this.worker = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "UltimateDonutSmp-ShopDB");
            thread.setDaemon(true);
            return thread;
        });
    }

    public CompletableFuture<Void> initialize() {
        return submit(() -> {
            connection = connectionFactory.open();
            ensureTables();
            return null;
        });
    }

    public CompletableFuture<ShopPreference> load(UUID playerId) {
        return submit(() -> {
            Set<String> favorites = new LinkedHashSet<>();
            try (PreparedStatement statement = connection().prepareStatement(
                    "SELECT favorite_id FROM shop_favorites WHERE player_uuid = ? ORDER BY favorite_id ASC")) {
                statement.setString(1, playerId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String favoriteId = resultSet.getString("favorite_id");
                        if (favoriteId != null && !favoriteId.isBlank()) {
                            favorites.add(favoriteId);
                        }
                    }
                }
            }
            return new ShopPreference(playerId, favorites);
        });
    }

    public CompletableFuture<Void> setFavorite(UUID playerId, String favoriteId, boolean favorite) {
        return submit(() -> {
            if (favorite) {
                String sql = mySql
                        ? "INSERT IGNORE INTO shop_favorites (player_uuid, favorite_id) VALUES (?,?)"
                        : "INSERT OR IGNORE INTO shop_favorites (player_uuid, favorite_id) VALUES (?,?)";
                try (PreparedStatement statement = connection().prepareStatement(sql)) {
                    statement.setString(1, playerId.toString());
                    statement.setString(2, favoriteId);
                    statement.executeUpdate();
                }
            } else {
                try (PreparedStatement statement = connection().prepareStatement(
                        "DELETE FROM shop_favorites WHERE player_uuid = ? AND favorite_id = ?")) {
                    statement.setString(1, playerId.toString());
                    statement.setString(2, favoriteId);
                    statement.executeUpdate();
                }
            }
            return null;
        });
    }

    public void shutdown() {
        closed = true;
        worker.shutdown();
        try {
            if (!worker.awaitTermination(5, TimeUnit.SECONDS)) {
                worker.shutdownNow();
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            worker.shutdownNow();
        }
        Connection current = connection;
        if (current != null) {
            try {
                current.close();
            } catch (SQLException exception) {
                logger.warning("Failed to close shop preference database connection: " + exception.getMessage());
            }
        }
    }

    private void ensureTables() throws SQLException {
        try (Statement statement = connection().createStatement()) {
            statement.execute(schemaAdapter.apply("""
                    CREATE TABLE IF NOT EXISTS shop_favorites (
                      player_uuid VARCHAR(191) NOT NULL,
                      favorite_id VARCHAR(191) NOT NULL,
                      PRIMARY KEY (player_uuid, favorite_id)
                    )
                    """));
        }
    }

    private Connection connection() throws SQLException {
        Connection current = connection;
        if (current == null || current.isClosed()) {
            throw new SQLException("Shop preference database connection is not available");
        }
        return current;
    }

    private <T> CompletableFuture<T> submit(Callable<T> task) {
        if (closed) {
            return CompletableFuture.failedFuture(new IllegalStateException("Shop preference repository is closed"));
        }
        return CompletableFuture.supplyAsync(() -> {
            for (int attempt = 0; ; attempt++) {
                try {
                    return task.call();
                } catch (SQLException exception) {
                    if (attempt >= 4 || !isTransientDatabaseContention(exception)) {
                        throw new CompletionException(exception);
                    }
                    try {
                        Thread.sleep(25L * (attempt + 1L));
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new CompletionException(interrupted);
                    }
                } catch (Exception exception) {
                    throw new CompletionException(exception);
                }
            }
        }, worker);
    }

    private boolean isTransientDatabaseContention(SQLException exception) {
        String state = exception.getSQLState();
        String message = exception.getMessage();
        return "40001".equals(state)
                || exception.getErrorCode() == 5
                || exception.getErrorCode() == 1205
                || exception.getErrorCode() == 1213
                || message != null && (
                message.toLowerCase().contains("database is locked")
                        || message.toLowerCase().contains("database is busy")
                        || message.toLowerCase().contains("deadlock")
        );
    }

    @FunctionalInterface
    interface ConnectionFactory {
        Connection open() throws SQLException;
    }
}
