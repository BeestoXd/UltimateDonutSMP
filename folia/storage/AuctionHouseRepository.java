package com.bx.ultimateDonutSmp.storage;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CrashProtectionManager;
import com.bx.ultimateDonutSmp.models.AuctionClaim;
import com.bx.ultimateDonutSmp.models.AuctionListing;
import com.bx.ultimateDonutSmp.models.PlayerPreference;
import com.bx.ultimateDonutSmp.utils.ItemSerializationUtils;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AuctionHouseRepository {

    public record Snapshot(List<AuctionListing> listings, List<AuctionClaim> claims) {
        public Snapshot {
            listings = List.copyOf(listings);
            claims = List.copyOf(claims);
        }
    }

    public record CreateResult(boolean created, boolean limitReached, AuctionListing listing) {
        public static CreateResult limitReachedResult() {
            return new CreateResult(false, true, null);
        }

        public static CreateResult failed() {
            return new CreateResult(false, false, null);
        }
    }

    public record ClaimLease(AuctionClaim claim, long token) {
    }

    public record PurchaseCommit(AuctionListing listing, long buyerClaimId) {
    }

    private static final long STALE_LEASE_MILLIS = TimeUnit.MINUTES.toMillis(5);
    private static final long PURCHASE_CLAIM_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(15);

    private final ConnectionFactory connectionFactory;
    private final UnaryOperator<String> schemaAdapter;
    private final boolean mySql;
    private final BiPredicate<ItemStack, String> itemValidator;
    private final ItemCodec itemCodec;
    private final Logger logger;
    private final ExecutorService worker;
    private volatile Connection connection;
    private volatile boolean closed;

    public AuctionHouseRepository(UltimateDonutSmp plugin) {
        this(
                () -> plugin.getDatabaseManager().openDedicatedConnection(),
                plugin.getDatabaseManager()::adaptSchemaSql,
                plugin.getDatabaseManager().isMySql(),
                (item, owner) -> {
                    CrashProtectionManager.ValidationResult validation = plugin.getCrashProtectionManager()
                            .validateForStorage(item, CrashProtectionManager.Context.DATABASE_LOAD);
                    if (validation.allowed()) {
                        return true;
                    }
                    plugin.getCrashProtectionManager().logBlockedItem(
                            owner,
                            item,
                            CrashProtectionManager.Context.DATABASE_LOAD,
                            validation
                    );
                    return false;
                },
                new ItemCodec() {
                    @Override
                    public String serialize(ItemStack item) throws Exception {
                        return ItemSerializationUtils.serialize(item);
                    }

                    @Override
                    public ItemStack deserialize(String encoded) throws Exception {
                        return ItemSerializationUtils.deserialize(encoded);
                    }
                },
                plugin.getLogger()
        );
    }

    AuctionHouseRepository(
            ConnectionFactory connectionFactory,
            UnaryOperator<String> schemaAdapter,
            boolean mySql,
            BiPredicate<ItemStack, String> itemValidator,
            ItemCodec itemCodec,
            Logger logger
    ) {
        this.connectionFactory = connectionFactory;
        this.schemaAdapter = schemaAdapter;
        this.mySql = mySql;
        this.itemValidator = itemValidator;
        this.itemCodec = itemCodec;
        this.logger = logger;
        this.worker = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "UltimateDonutSmp-AuctionDB");
            thread.setDaemon(true);
            return thread;
        });
    }

    public CompletableFuture<Void> initialize() {
        return submit(() -> {
            connection = connectionFactory.open();
            ensureTables();
            resetStaleClaimLeases();
            return null;
        });
    }

    public CompletableFuture<Snapshot> loadSnapshot() {
        return submit(() -> {
            resetStaleClaimLeases();
            List<AuctionListing> listings = new ArrayList<>();
            try (PreparedStatement statement = connection().prepareStatement(
                    "SELECT * FROM auction_listings ORDER BY id DESC");
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    AuctionListing listing = mapListing(resultSet);
                    if (listing != null) {
                        listings.add(listing);
                    }
                }
            }

            List<AuctionClaim> claims = new ArrayList<>();
            try (PreparedStatement statement = connection().prepareStatement(
                    "SELECT * FROM auction_claims WHERE claimed_at = 0 AND ready_at <= ? ORDER BY created_at DESC, id DESC")) {
                statement.setLong(1, System.currentTimeMillis());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        AuctionClaim claim = mapClaim(resultSet);
                        if (claim != null) {
                            claims.add(claim);
                        }
                    }
                }
            }
            return new Snapshot(listings, claims);
        });
    }

    public CompletableFuture<PlayerPreference> loadPreference(UUID playerId) {
        return submit(() -> {
            try (PreparedStatement statement = connection().prepareStatement(
                    "SELECT * FROM player_auction_preferences WHERE player_uuid = ? LIMIT 1")) {
                statement.setString(1, playerId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new PlayerPreference(
                                playerId,
                                resultSet.getInt("fast_buy_enabled") != 0,
                                resultSet.getInt("fast_sell_enabled") != 0,
                                resultSet.getInt("last_duration_hours"),
                                resultSet.getString("last_category"),
                                resultSet.getDouble("last_price")
                        );
                    }
                }
            }
            return new PlayerPreference(playerId);
        });
    }

    public CompletableFuture<Void> savePreference(PlayerPreference preference) {
        UUID playerId = preference.playerId();
        boolean fastBuy = preference.fastBuyEnabled();
        boolean fastSell = preference.fastSellEnabled();
        int duration = preference.lastDurationHours();
        String category = preference.lastCategory();
        double price = preference.lastPrice();
        return submit(() -> {
            String sql = mySql
                    ? "INSERT INTO player_auction_preferences " +
                    "(player_uuid, fast_buy_enabled, fast_sell_enabled, last_duration_hours, last_category, last_price) " +
                    "VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "fast_buy_enabled = VALUES(fast_buy_enabled), " +
                    "fast_sell_enabled = VALUES(fast_sell_enabled), " +
                    "last_duration_hours = VALUES(last_duration_hours), " +
                    "last_category = VALUES(last_category), " +
                    "last_price = VALUES(last_price)"
                    : "INSERT INTO player_auction_preferences " +
                    "(player_uuid, fast_buy_enabled, fast_sell_enabled, last_duration_hours, last_category, last_price) " +
                    "VALUES (?,?,?,?,?,?) ON CONFLICT(player_uuid) DO UPDATE SET " +
                    "fast_buy_enabled = excluded.fast_buy_enabled, " +
                    "fast_sell_enabled = excluded.fast_sell_enabled, " +
                    "last_duration_hours = excluded.last_duration_hours, " +
                    "last_category = excluded.last_category, " +
                    "last_price = excluded.last_price";
            try (PreparedStatement insert = connection().prepareStatement(sql)) {
                    insert.setString(1, playerId.toString());
                    insert.setInt(2, fastBuy ? 1 : 0);
                    insert.setInt(3, fastSell ? 1 : 0);
                    insert.setInt(4, duration);
                    insert.setString(5, category);
                    insert.setDouble(6, price);
                    insert.executeUpdate();
            }
            return null;
        });
    }

    public CompletableFuture<CreateResult> createListing(
            UUID sellerId,
            String sellerName,
            double price,
            double tax,
            ItemStack item,
            long createdAt,
            long expiresAt,
            String category,
            int maximumActiveListings
    ) {
        ItemStack storedItem = item.clone();
        return submit(() -> inTransaction(() -> {
            String limitSql = mySql
                    ? "SELECT id FROM auction_listings " +
                    "WHERE seller_uuid = ? AND status = ? AND expires_at > ? FOR UPDATE"
                    : "SELECT id FROM auction_listings " +
                    "WHERE seller_uuid = ? AND status = ? AND expires_at > ?";
            try (PreparedStatement count = connection().prepareStatement(limitSql)) {
                count.setString(1, sellerId.toString());
                count.setString(2, AuctionListing.Status.ACTIVE.name());
                count.setLong(3, createdAt);
                try (ResultSet resultSet = count.executeQuery()) {
                    int active = 0;
                    while (resultSet.next() && active < maximumActiveListings) {
                        active++;
                    }
                    if (active >= maximumActiveListings) {
                        return CreateResult.limitReachedResult();
                    }
                }
            }

            try (PreparedStatement insert = connection().prepareStatement(
                    "INSERT INTO auction_listings " +
                            "(seller_uuid, seller_name, buyer_uuid, status, price, tax, item_data, created_at, expires_at, sold_at, cancelled_at, expired_at, category) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                insert.setString(1, sellerId.toString());
                insert.setString(2, sellerName);
                insert.setNull(3, Types.VARCHAR);
                insert.setString(4, AuctionListing.Status.ACTIVE.name());
                insert.setDouble(5, price);
                insert.setDouble(6, tax);
                insert.setString(7, serializeItem(storedItem));
                insert.setLong(8, createdAt);
                insert.setLong(9, expiresAt);
                insert.setLong(10, 0L);
                insert.setLong(11, 0L);
                insert.setLong(12, 0L);
                insert.setString(13, category);
                if (insert.executeUpdate() != 1) {
                    return CreateResult.failed();
                }
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (!keys.next()) {
                        return CreateResult.failed();
                    }
                    long id = keys.getLong(1);
                    return new CreateResult(true, false, new AuctionListing(
                            id,
                            sellerId,
                            sellerName,
                            null,
                            AuctionListing.Status.ACTIVE,
                            price,
                            tax,
                            storedItem,
                            createdAt,
                            expiresAt,
                            0L,
                            0L,
                            0L,
                            category
                    ));
                }
            }
        }));
    }

    public CompletableFuture<Optional<PurchaseCommit>> markSold(
            long listingId,
            UUID buyerId,
            long soldAt
    ) {
        return submit(() -> inTransaction(() -> {
            AuctionListing listing = findListing(listingId);
            if (listing == null || !listing.active() || listing.expiresAt() <= soldAt) {
                return Optional.empty();
            }

            try (PreparedStatement update = connection().prepareStatement(
                    "UPDATE auction_listings SET status = ?, buyer_uuid = ?, sold_at = ? " +
                            "WHERE id = ? AND status = ? AND expires_at > ?")) {
                update.setString(1, AuctionListing.Status.SOLD.name());
                update.setString(2, buyerId.toString());
                update.setLong(3, soldAt);
                update.setLong(4, listingId);
                update.setString(5, AuctionListing.Status.ACTIVE.name());
                update.setLong(6, soldAt);
                if (update.executeUpdate() != 1) {
                    return Optional.empty();
                }
            }

            try (PreparedStatement claim = connection().prepareStatement(
                    "INSERT INTO auction_claims " +
                            "(owner_uuid, claim_type, source_listing_id, money_amount, item_data, created_at, claimed_at, ready_at) " +
                            "VALUES (?,?,?,?,?,?,?,?)")) {
                claim.setString(1, listing.sellerUuid().toString());
                claim.setString(2, AuctionClaim.ClaimType.MONEY.name());
                claim.setLong(3, listing.id());
                claim.setDouble(4, listing.sellerPayout());
                claim.setNull(5, Types.VARCHAR);
                claim.setLong(6, soldAt);
                claim.setLong(7, 0L);
                claim.setLong(8, soldAt + PURCHASE_CLAIM_DELAY_MILLIS);
                claim.executeUpdate();
            }

            long buyerClaimId;
            try (PreparedStatement claim = connection().prepareStatement(
                    "INSERT INTO auction_claims " +
                            "(owner_uuid, claim_type, source_listing_id, money_amount, item_data, created_at, claimed_at, ready_at) " +
                            "VALUES (?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                claim.setString(1, buyerId.toString());
                claim.setString(2, AuctionClaim.ClaimType.ITEM.name());
                claim.setLong(3, listing.id());
                claim.setDouble(4, 0D);
                claim.setString(5, serializeItem(listing.item()));
                claim.setLong(6, soldAt);
                claim.setLong(7, 0L);
                claim.setLong(8, soldAt);
                if (claim.executeUpdate() != 1) {
                    throw new SQLException("Failed to create buyer item claim for listing #" + listing.id());
                }
                try (ResultSet keys = claim.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Missing buyer claim ID for listing #" + listing.id());
                    }
                    buyerClaimId = keys.getLong(1);
                }
            }

            AuctionListing soldListing = new AuctionListing(
                    listing.id(),
                    listing.sellerUuid(),
                    listing.sellerName(),
                    buyerId,
                    AuctionListing.Status.SOLD,
                    listing.price(),
                    listing.tax(),
                    listing.item(),
                    listing.createdAt(),
                    listing.expiresAt(),
                    soldAt,
                    listing.cancelledAt(),
                    listing.expiredAt(),
                    listing.category()
            );
            return Optional.of(new PurchaseCommit(soldListing, buyerClaimId));
        }));
    }

    public CompletableFuture<Boolean> makeSellerClaimReady(long listingId) {
        return submit(() -> {
            try (PreparedStatement statement = connection().prepareStatement(
                    "UPDATE auction_claims SET ready_at = ? " +
                            "WHERE source_listing_id = ? AND claim_type = ? AND claimed_at = 0")) {
                statement.setLong(1, System.currentTimeMillis());
                statement.setLong(2, listingId);
                statement.setString(3, AuctionClaim.ClaimType.MONEY.name());
                return statement.executeUpdate() == 1;
            }
        });
    }

    public CompletableFuture<Optional<AuctionListing>> cancelListing(long listingId, UUID ownerId, long cancelledAt) {
        return submit(() -> inTransaction(() -> moveToItemClaim(
                listingId,
                ownerId,
                AuctionListing.Status.CANCELLED,
                cancelledAt
        )));
    }

    public CompletableFuture<Integer> expireListings(long now) {
        return submit(() -> {
            List<Long> ids = new ArrayList<>();
            try (PreparedStatement statement = connection().prepareStatement(
                    "SELECT id FROM auction_listings WHERE status = ? AND expires_at <= ?")) {
                statement.setString(1, AuctionListing.Status.ACTIVE.name());
                statement.setLong(2, now);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        ids.add(resultSet.getLong(1));
                    }
                }
            }

            int expired = 0;
            for (long id : ids) {
                Optional<AuctionListing> result = inTransaction(() -> moveToItemClaim(
                        id,
                        null,
                        AuctionListing.Status.EXPIRED,
                        now
                ));
                if (result.isPresent()) {
                    expired++;
                }
            }
            return expired;
        });
    }

    public CompletableFuture<Optional<ClaimLease>> acquireClaim(long claimId, UUID ownerId) {
        return submit(() -> inTransaction(() -> {
            AuctionClaim claim = findClaim(claimId);
            if (claim == null || claim.claimed() || !claim.ownerUuid().equals(ownerId)) {
                return Optional.empty();
            }
            long token = Math.max(1L, System.currentTimeMillis());
            try (PreparedStatement update = connection().prepareStatement(
                    "UPDATE auction_claims SET claimed_at = ? " +
                            "WHERE id = ? AND owner_uuid = ? AND claimed_at = 0 AND ready_at <= ?")) {
                update.setLong(1, -token);
                update.setLong(2, claimId);
                update.setString(3, ownerId.toString());
                update.setLong(4, token);
                if (update.executeUpdate() != 1) {
                    return Optional.empty();
                }
            }
            return Optional.of(new ClaimLease(claim, token));
        }));
    }

    public CompletableFuture<Void> createPendingItemClaim(
            UUID ownerId,
            long sourceListingId,
            ItemStack item,
            long createdAt
    ) {
        ItemStack storedItem = item.clone();
        return submit(() -> {
            try (PreparedStatement claim = connection().prepareStatement(
                    "INSERT INTO auction_claims " +
                            "(owner_uuid, claim_type, source_listing_id, money_amount, item_data, created_at, claimed_at, ready_at) " +
                            "VALUES (?,?,?,?,?,?,?,?)")) {
                claim.setString(1, ownerId.toString());
                claim.setString(2, AuctionClaim.ClaimType.ITEM.name());
                claim.setLong(3, sourceListingId);
                claim.setDouble(4, 0D);
                claim.setString(5, serializeItem(storedItem));
                claim.setLong(6, createdAt);
                claim.setLong(7, 0L);
                claim.setLong(8, createdAt);
                claim.executeUpdate();
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> completeClaim(ClaimLease lease) {
        return submit(() -> {
            try (PreparedStatement update = connection().prepareStatement(
                    "UPDATE auction_claims SET claimed_at = ? WHERE id = ? AND claimed_at = ?")) {
                update.setLong(1, lease.token());
                update.setLong(2, lease.claim().id());
                update.setLong(3, -lease.token());
                return update.executeUpdate() == 1;
            }
        });
    }

    public CompletableFuture<Boolean> restoreClaim(ClaimLease lease) {
        return submit(() -> {
            try (PreparedStatement update = connection().prepareStatement(
                    "UPDATE auction_claims SET claimed_at = 0 " +
                            "WHERE id = ? AND (claimed_at = ? OR claimed_at = ?)")) {
                update.setLong(1, lease.claim().id());
                update.setLong(2, -lease.token());
                update.setLong(3, lease.token());
                return update.executeUpdate() == 1;
            }
        });
    }

    public CompletableFuture<Void> releaseClaim(ClaimLease lease) {
        return submit(() -> {
            try (PreparedStatement update = connection().prepareStatement(
                    "UPDATE auction_claims SET claimed_at = 0 WHERE id = ? AND claimed_at = ?")) {
                update.setLong(1, lease.claim().id());
                update.setLong(2, -lease.token());
                update.executeUpdate();
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
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            worker.shutdownNow();
        }
        Connection current = connection;
        if (current != null) {
            try {
                current.close();
            } catch (SQLException exception) {
                logger.log(Level.WARNING, "Failed to close Auction House database connection", exception);
            }
        }
    }

    private Optional<AuctionListing> moveToItemClaim(
            long listingId,
            UUID expectedOwner,
            AuctionListing.Status nextStatus,
            long timestamp
    ) throws SQLException {
        AuctionListing listing = findListing(listingId);
        if (listing == null || !listing.active()) {
            return Optional.empty();
        }
        if (expectedOwner != null && !listing.sellerUuid().equals(expectedOwner)) {
            return Optional.empty();
        }

        String timestampColumn = nextStatus == AuctionListing.Status.CANCELLED ? "cancelled_at" : "expired_at";
        String ownerClause = expectedOwner == null ? "" : " AND seller_uuid = ?";
        try (PreparedStatement update = connection().prepareStatement(
                "UPDATE auction_listings SET status = ?, " + timestampColumn + " = ? " +
                        "WHERE id = ? AND status = ?" + ownerClause)) {
            update.setString(1, nextStatus.name());
            update.setLong(2, timestamp);
            update.setLong(3, listingId);
            update.setString(4, AuctionListing.Status.ACTIVE.name());
            if (expectedOwner != null) {
                update.setString(5, expectedOwner.toString());
            }
            if (update.executeUpdate() != 1) {
                return Optional.empty();
            }
        }

        try (PreparedStatement claim = connection().prepareStatement(
                "INSERT INTO auction_claims " +
                        "(owner_uuid, claim_type, source_listing_id, money_amount, item_data, created_at, claimed_at, ready_at) " +
                        "VALUES (?,?,?,?,?,?,?,?)")) {
            claim.setString(1, listing.sellerUuid().toString());
            claim.setString(2, AuctionClaim.ClaimType.ITEM.name());
            claim.setLong(3, listing.id());
            claim.setDouble(4, 0D);
            claim.setString(5, serializeItem(listing.item()));
            claim.setLong(6, timestamp);
            claim.setLong(7, 0L);
            claim.setLong(8, timestamp);
            claim.executeUpdate();
        }

        return Optional.of(new AuctionListing(
                listing.id(),
                listing.sellerUuid(),
                listing.sellerName(),
                listing.buyerUuid(),
                nextStatus,
                listing.price(),
                listing.tax(),
                listing.item(),
                listing.createdAt(),
                listing.expiresAt(),
                listing.soldAt(),
                nextStatus == AuctionListing.Status.CANCELLED ? timestamp : listing.cancelledAt(),
                nextStatus == AuctionListing.Status.EXPIRED ? timestamp : listing.expiredAt(),
                listing.category()
        ));
    }

    private AuctionListing findListing(long listingId) throws SQLException {
        try (PreparedStatement statement = connection().prepareStatement(
                "SELECT * FROM auction_listings WHERE id = ? LIMIT 1")) {
            statement.setLong(1, listingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapListing(resultSet) : null;
            }
        }
    }

    private AuctionClaim findClaim(long claimId) throws SQLException {
        try (PreparedStatement statement = connection().prepareStatement(
                "SELECT * FROM auction_claims WHERE id = ? AND ready_at <= ? LIMIT 1")) {
            statement.setLong(1, claimId);
            statement.setLong(2, System.currentTimeMillis());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapClaim(resultSet) : null;
            }
        }
    }

    private void ensureTables() throws SQLException {
        try (Statement statement = connection().createStatement()) {
            statement.execute(schemaAdapter.apply("""
                    CREATE TABLE IF NOT EXISTS auction_listings (
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
                      expired_at INTEGER DEFAULT 0,
                      category TEXT DEFAULT 'ALL'
                    )
                    """));
            statement.execute(schemaAdapter.apply("""
                    CREATE TABLE IF NOT EXISTS player_auction_preferences (
                      player_uuid TEXT PRIMARY KEY,
                      fast_buy_enabled INTEGER DEFAULT 0,
                      fast_sell_enabled INTEGER DEFAULT 0,
                      last_duration_hours INTEGER DEFAULT 48,
                      last_category TEXT DEFAULT 'ALL',
                      last_price REAL DEFAULT 0
                    )
                    """));
            statement.execute(schemaAdapter.apply("""
                    CREATE TABLE IF NOT EXISTS auction_claims (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      owner_uuid TEXT NOT NULL,
                      claim_type TEXT NOT NULL,
                      source_listing_id INTEGER DEFAULT 0,
                      money_amount REAL DEFAULT 0,
                      item_data TEXT,
                      created_at INTEGER NOT NULL,
                      claimed_at INTEGER DEFAULT 0,
                      ready_at INTEGER DEFAULT 0
                    )
                    """));
        }

        addColumnIfMissing("auction_listings", "category", "TEXT DEFAULT 'ALL'");
        addColumnIfMissing("auction_claims", "ready_at", "INTEGER DEFAULT 0");
        createIndexIfMissing("idx_auction_listings_status_expires", "auction_listings", "status, expires_at");
        createIndexIfMissing("idx_auction_listings_seller_status", "auction_listings", "seller_uuid, status");
        createIndexIfMissing("idx_auction_claims_owner_claimed", "auction_claims", "owner_uuid, claimed_at");
        createIndexIfMissing("idx_auction_claims_source_type", "auction_claims", "source_listing_id, claim_type");
    }

    private void addColumnIfMissing(String table, String column, String definition) throws SQLException {
        if (columnExists(table, column)) {
            return;
        }
        try (Statement statement = connection().createStatement()) {
            statement.execute(schemaAdapter.apply(
                    "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition
            ));
        }
    }

    private boolean columnExists(String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection().getMetaData();
        try (ResultSet columns = metadata.getColumns(connection().getCatalog(), null, table, column)) {
            if (columns.next()) {
                return true;
            }
        }
        try (ResultSet columns = metadata.getColumns(connection().getCatalog(), null, table.toUpperCase(), column.toUpperCase())) {
            return columns.next();
        }
    }

    private void createIndexIfMissing(String index, String table, String columns) throws SQLException {
        DatabaseMetaData metadata = connection().getMetaData();
        try (ResultSet indexes = metadata.getIndexInfo(connection().getCatalog(), null, table, false, false)) {
            while (indexes.next()) {
                if (index.equalsIgnoreCase(indexes.getString("INDEX_NAME"))) {
                    return;
                }
            }
        }
        String prefix = mySql ? "CREATE INDEX " : "CREATE INDEX IF NOT EXISTS ";
        try (Statement statement = connection().createStatement()) {
            statement.execute(prefix + index + " ON " + table + " (" + columns + ")");
        }
    }

    private void resetStaleClaimLeases() throws SQLException {
        long cutoff = System.currentTimeMillis() - STALE_LEASE_MILLIS;
        try (PreparedStatement statement = connection().prepareStatement(
                "UPDATE auction_claims SET claimed_at = 0 WHERE claimed_at < 0 AND claimed_at > ?")) {
            statement.setLong(1, -cutoff);
            statement.executeUpdate();
        }
    }

    private AuctionListing mapListing(ResultSet resultSet) throws SQLException {
        ItemStack item = deserializeItem(resultSet.getString("item_data"));
        if (item == null || !isSafe(item, "auction listing #" + resultSet.getLong("id"))) {
            return null;
        }
        String buyer = resultSet.getString("buyer_uuid");
        return new AuctionListing(
                resultSet.getLong("id"),
                UUID.fromString(resultSet.getString("seller_uuid")),
                resultSet.getString("seller_name"),
                buyer == null || buyer.isBlank() ? null : UUID.fromString(buyer),
                AuctionListing.Status.fromDatabase(resultSet.getString("status")),
                resultSet.getDouble("price"),
                resultSet.getDouble("tax"),
                item,
                resultSet.getLong("created_at"),
                resultSet.getLong("expires_at"),
                resultSet.getLong("sold_at"),
                resultSet.getLong("cancelled_at"),
                resultSet.getLong("expired_at"),
                resultSet.getString("category")
        );
    }

    private AuctionClaim mapClaim(ResultSet resultSet) throws SQLException {
        AuctionClaim.ClaimType type = AuctionClaim.ClaimType.fromDatabase(resultSet.getString("claim_type"));
        ItemStack item = deserializeItem(resultSet.getString("item_data"));
        if (type == AuctionClaim.ClaimType.ITEM && (item == null || !isSafe(item, "auction claim #" + resultSet.getLong("id")))) {
            return null;
        }
        return new AuctionClaim(
                resultSet.getLong("id"),
                UUID.fromString(resultSet.getString("owner_uuid")),
                type,
                resultSet.getLong("source_listing_id"),
                resultSet.getDouble("money_amount"),
                item,
                resultSet.getLong("created_at"),
                resultSet.getLong("claimed_at")
        );
    }

    private boolean isSafe(ItemStack item, String owner) {
        return itemValidator.test(item, owner);
    }

    private String serializeItem(ItemStack item) throws SQLException {
        try {
            return itemCodec.serialize(item);
        } catch (Exception exception) {
            throw new SQLException("Failed to serialize auction item", exception);
        }
    }

    private ItemStack deserializeItem(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }
        try {
            return itemCodec.deserialize(encoded);
        } catch (Exception exception) {
            logger.log(Level.WARNING, "Failed to deserialize Auction House item", exception);
            return null;
        }
    }

    private <T> T inTransaction(SqlCallable<T> callable) throws SQLException {
        Connection current = connection();
        boolean originalAutoCommit = current.getAutoCommit();
        current.setAutoCommit(false);
        try {
            T result = callable.call();
            current.commit();
            return result;
        } catch (SQLException | RuntimeException exception) {
            current.rollback();
            throw exception;
        } finally {
            current.setAutoCommit(originalAutoCommit);
        }
    }

    private Connection connection() throws SQLException {
        Connection current = connection;
        if (current == null || current.isClosed()) {
            throw new SQLException("Auction House database connection is not available");
        }
        return current;
    }

    private <T> CompletableFuture<T> submit(Callable<T> task) {
        if (closed) {
            return CompletableFuture.failedFuture(new IllegalStateException("Auction House repository is closed"));
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
    private interface SqlCallable<T> {
        T call() throws SQLException;
    }

    @FunctionalInterface
    interface ConnectionFactory {
        Connection open() throws SQLException;
    }

    interface ItemCodec {
        String serialize(ItemStack item) throws Exception;

        ItemStack deserialize(String encoded) throws Exception;
    }
}
