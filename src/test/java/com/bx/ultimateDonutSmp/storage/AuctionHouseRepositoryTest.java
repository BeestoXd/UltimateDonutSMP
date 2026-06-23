package com.bx.ultimateDonutSmp.storage;

import com.bx.ultimateDonutSmp.models.AuctionClaim;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionHouseRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void migratesLegacySchemaWithoutResettingRows() throws Exception {
        Path database = tempDir.resolve("legacy.db");
        try (Connection connection = open(database); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE auction_listings (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      seller_uuid TEXT NOT NULL,
                      seller_name TEXT NOT NULL,
                      buyer_uuid TEXT,
                      status TEXT NOT NULL,
                      price REAL NOT NULL,
                      tax REAL DEFAULT 0,
                      item_data TEXT NOT NULL,
                      created_at INTEGER NOT NULL,
                      expires_at INTEGER NOT NULL,
                      sold_at INTEGER DEFAULT 0,
                      cancelled_at INTEGER DEFAULT 0,
                      expired_at INTEGER DEFAULT 0
                    )
                    """);
            statement.execute("""
                    CREATE TABLE auction_claims (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      owner_uuid TEXT NOT NULL,
                      claim_type TEXT NOT NULL,
                      source_listing_id INTEGER DEFAULT 0,
                      money_amount REAL DEFAULT 0,
                      item_data TEXT,
                      created_at INTEGER NOT NULL,
                      claimed_at INTEGER DEFAULT 0
                    )
                    """);
        }

        AuctionHouseRepository repository = repository(database);
        try {
            repository.initialize().join();
            try (Connection connection = open(database)) {
                assertTrue(columnExists(connection, "auction_listings", "category"));
                assertTrue(columnExists(connection, "auction_claims", "ready_at"));
            }
        } finally {
            repository.shutdown();
        }
    }

    @Test
    void concurrentPurchaseCreatesOneAtomicSaleAndBuyerClaim() throws Exception {
        Path database = tempDir.resolve("concurrent.db");
        AuctionHouseRepository first = repository(database);
        AuctionHouseRepository second = repository(database);
        try {
            first.initialize().join();
            second.initialize().join();

            UUID seller = UUID.randomUUID();
            long now = System.currentTimeMillis();
            AuctionHouseRepository.CreateResult created = first.createListing(
                    seller,
                    "Seller",
                    1_000D,
                    50D,
                    new ItemStack(Material.DIAMOND_SWORD),
                    now,
                    now + 60_000L,
                    "COMBAT",
                    5
            ).join();
            assertTrue(created.created());

            CompletableFuture<Optional<AuctionHouseRepository.PurchaseCommit>> purchaseOne =
                    first.markSold(created.listing().id(), UUID.randomUUID(), now + 1);
            CompletableFuture<Optional<AuctionHouseRepository.PurchaseCommit>> purchaseTwo =
                    second.markSold(created.listing().id(), UUID.randomUUID(), now + 2);
            CompletableFuture.allOf(purchaseOne, purchaseTwo).join();

            int successfulPurchases = (purchaseOne.join().isPresent() ? 1 : 0)
                    + (purchaseTwo.join().isPresent() ? 1 : 0);
            assertEquals(1, successfulPurchases);

            try (Connection connection = open(database);
                 Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(
                        "SELECT status FROM auction_listings WHERE id = " + created.listing().id())) {
                    assertTrue(resultSet.next());
                    assertEquals(AuctionListing.Status.SOLD.name(), resultSet.getString(1));
                }
                try (ResultSet resultSet = statement.executeQuery(
                        "SELECT COUNT(*) FROM auction_claims WHERE source_listing_id = " + created.listing().id())) {
                    assertTrue(resultSet.next());
                    assertEquals(2, resultSet.getInt(1));
                }
            }
        } finally {
            first.shutdown();
            second.shutdown();
        }
    }

    @Test
    void cancelExpiryAndClaimLeaseRemainRecoverable() {
        Path database = tempDir.resolve("claims.db");
        AuctionHouseRepository repository = repository(database);
        try {
            repository.initialize().join();
            UUID owner = UUID.randomUUID();
            long now = System.currentTimeMillis();

            AuctionHouseRepository.CreateResult cancelTarget = repository.createListing(
                    owner,
                    "Owner",
                    500D,
                    25D,
                    new ItemStack(Material.CHEST),
                    now,
                    now + 60_000L,
                    "UTILITIES",
                    5
            ).join();
            assertTrue(repository.cancelListing(cancelTarget.listing().id(), owner, now + 1).join().isPresent());

            AuctionHouseRepository.Snapshot cancelledSnapshot = repository.loadSnapshot().join();
            AuctionClaim returnedItem = cancelledSnapshot.claims().stream()
                    .filter(claim -> claim.sourceListingId() == cancelTarget.listing().id())
                    .findFirst()
                    .orElseThrow();
            AuctionHouseRepository.ClaimLease lease = repository.acquireClaim(
                    returnedItem.id(),
                    owner
            ).join().orElseThrow();
            assertFalse(repository.acquireClaim(returnedItem.id(), owner).join().isPresent());
            assertTrue(repository.completeClaim(lease).join());
            assertTrue(repository.restoreClaim(lease).join());
            assertTrue(repository.acquireClaim(returnedItem.id(), owner).join().isPresent());

            AuctionHouseRepository.CreateResult expiryTarget = repository.createListing(
                    owner,
                    "Owner",
                    250D,
                    10D,
                    new ItemStack(Material.GOLDEN_CARROT),
                    now,
                    now + 5L,
                    "FOOD",
                    5
            ).join();
            assertEquals(1, repository.expireListings(now + 10L).join());
            AuctionListing expired = repository.loadSnapshot().join().listings().stream()
                    .filter(listing -> listing.id() == expiryTarget.listing().id())
                    .findFirst()
                    .orElse(null);
            assertNotNull(expired);
            assertTrue(expired.expired());
        } finally {
            repository.shutdown();
        }
    }

    private AuctionHouseRepository repository(Path database) {
        return new AuctionHouseRepository(
                () -> open(database),
                sql -> sql,
                false,
                (item, owner) -> true,
                new AuctionHouseRepository.ItemCodec() {
                    @Override
                    public String serialize(ItemStack item) {
                        return item.getType().name() + ":" + item.getAmount();
                    }

                    @Override
                    public ItemStack deserialize(String encoded) {
                        String[] parts = encoded.split(":", 2);
                        ItemStack item = new ItemStack(Material.valueOf(parts[0]));
                        item.setAmount(Integer.parseInt(parts[1]));
                        return item;
                    }
                },
                Logger.getLogger("AuctionHouseRepositoryTest")
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

    private boolean columnExists(Connection connection, String table, String column) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet columns = metadata.getColumns(null, null, table, column)) {
            return columns.next();
        }
    }
}
