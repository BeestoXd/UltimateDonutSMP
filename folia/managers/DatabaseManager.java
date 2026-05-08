package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Bounty;
import com.bx.ultimateDonutSmp.models.FreezeState;
import com.bx.ultimateDonutSmp.models.Home;
import com.bx.ultimateDonutSmp.models.IgnoreEntry;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.PunishmentFilterState;
import com.bx.ultimateDonutSmp.models.PunishmentQuery;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentScope;
import com.bx.ultimateDonutSmp.models.PunishmentSortOrder;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import com.bx.ultimateDonutSmp.models.SellCategory;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.models.SpawnerLootEntry;
import com.bx.ultimateDonutSmp.models.StaffModeState;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.staff.StaffInventorySnapshot;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseManager {

    public record SellHistoryEntry(String itemName, int amount, double price, long timestamp) {}

    public record AltAccountMatch(UUID uuid, String username, List<String> sharedIps, long lastSeenAt) {}

    public enum DatabaseType {
        SQLITE,
        MYSQL,
        MONGODB;

        public static DatabaseType fromConfig(String raw) {
            if (raw == null || raw.isBlank()) {
                return SQLITE;
            }

            String normalized = raw.trim().replace("-", "_").toUpperCase(Locale.ROOT);
            if ("SQLLITE".equals(normalized)) {
                return SQLITE;
            }

            try {
                return DatabaseType.valueOf(normalized);
            } catch (IllegalArgumentException ignored) {
                return SQLITE;
            }
        }
    }

    private static final Pattern CREATE_INDEX_PATTERN = Pattern.compile(
            "^CREATE\\s+INDEX\\s+IF\\s+NOT\\s+EXISTS\\s+([A-Za-z0-9_]+)\\s+ON\\s+([A-Za-z0-9_]+)\\s*\\((.+)\\)\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final String MONGO_SCHEMA_COLLECTION = "_schema";

    private final UltimateDonutSmp plugin;
    private Connection connection;
    private DatabaseType databaseType = DatabaseType.SQLITE;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private boolean mongoBridgeActive;

    public DatabaseManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            databaseType = DatabaseType.fromConfig(getDatabaseConfig().getString("DATABASE.TYPE", "SQLITE"));

            switch (databaseType) {
                case MYSQL -> initializeMySqlConnection();
                case MONGODB -> initializeMongoBridgeConnection();
                case SQLITE -> initializeSqliteConnection(resolveConfiguredFile("DATABASE.SQLITE.FILE", "data/data.db"));
            }

            createTables();
            ensurePlayerColumns();
            ensurePortalColumns();
            ensureTeamColumns();
            ensureStaffModeColumns();

            if (mongoBridgeActive) {
                importMongoSnapshotIntoSqlite();
            }

            plugin.getLogger().info("Database connected successfully using " + databaseType.name() + ".");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }

    private void initializeSqliteConnection(File dbFile) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Files.createDirectories(dbFile.getParentFile().toPath());

        File legacyDbFile = new File(plugin.getDataFolder(), "data.db");
        migrateLegacyDatabase(legacyDbFile, dbFile);

        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
        }
    }

    private void initializeMySqlConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        FileConfiguration config = getDatabaseConfig();
        String host = config.getString("DATABASE.MYSQL.HOST", "localhost");
        int port = Math.max(1, Math.min(65535, config.getInt("DATABASE.MYSQL.PORT", 3306)));
        String database = config.getString("DATABASE.MYSQL.DATABASE", "ultimatedonutsmp");
        String username = config.getString("DATABASE.MYSQL.USERNAME", "root");
        String password = config.getString("DATABASE.MYSQL.PASSWORD", "");
        String parameters = config.getString("DATABASE.MYSQL.PARAMETERS",
                "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8");

        if (config.getBoolean("DATABASE.MYSQL.CREATE-DATABASE", true)) {
            String serverUrl = "jdbc:mysql://" + host + ":" + port + "/" + appendJdbcParameters(parameters);
            try (Connection setupConnection = DriverManager.getConnection(serverUrl, username, password);
                 Statement statement = setupConnection.createStatement()) {
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + escapeMySqlIdentifier(database) + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + appendJdbcParameters(parameters);
        connection = DriverManager.getConnection(url, username, password);
    }

    private void initializeMongoBridgeConnection() throws Exception {
        FileConfiguration config = getDatabaseConfig();
        String uri = config.getString("DATABASE.MONGODB.URI", "mongodb://localhost:27017");
        String database = config.getString("DATABASE.MONGODB.DATABASE", "ultimatedonutsmp");

        mongoClient = MongoClients.create(uri);
        mongoDatabase = mongoClient.getDatabase(database);
        mongoDatabase.runCommand(new Document("ping", 1));

        mongoBridgeActive = true;
        initializeSqliteConnection(resolveConfiguredFile("DATABASE.MONGODB.CACHE-FILE", "data/mongodb-cache.db"));
    }

    private void migrateLegacyDatabase(File legacyDbFile, File dbFile) throws IOException {
        if (!legacyDbFile.exists() || dbFile.exists()) {
            return;
        }

        Files.move(legacyDbFile.toPath(), dbFile.toPath());
        moveLegacySqliteCompanionFile(legacyDbFile, dbFile, "-wal");
        moveLegacySqliteCompanionFile(legacyDbFile, dbFile, "-shm");
        plugin.getLogger().info("Moved legacy database to " + dbFile.getPath());
    }

    private void moveLegacySqliteCompanionFile(File legacyDbFile, File dbFile, String suffix) throws IOException {
        File legacyFile = new File(legacyDbFile.getParentFile(), legacyDbFile.getName() + suffix);
        File newFile = new File(dbFile.getParentFile(), dbFile.getName() + suffix);
        if (legacyFile.exists() && !newFile.exists()) {
            Files.move(legacyFile.toPath(), newFile.toPath());
        }
    }

    private File resolveConfiguredFile(String path, String fallback) {
        String configuredPath = getDatabaseConfig().getString(path, fallback);
        File file = new File(configuredPath == null || configuredPath.isBlank() ? fallback : configuredPath);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(plugin.getDataFolder(), configuredPath == null || configuredPath.isBlank() ? fallback : configuredPath);
    }

    private String appendJdbcParameters(String parameters) {
        if (parameters == null || parameters.isBlank()) {
            return "";
        }
        return parameters.trim().startsWith("?") ? parameters.trim() : "?" + parameters.trim();
    }

    private String escapeMySqlIdentifier(String identifier) {
        return identifier == null ? "" : identifier.replace("`", "``");
    }

    private FileConfiguration getDatabaseConfig() {
        return plugin.getConfigManager().getDatabase();
    }

    private void createTables() throws SQLException {
        execute(
            "CREATE TABLE IF NOT EXISTS players (" +
            "  uuid TEXT PRIMARY KEY," +
            "  username TEXT," +
            "  money REAL DEFAULT 0," +
            "  shards INTEGER DEFAULT 0," +
            "  kills INTEGER DEFAULT 0," +
            "  deaths INTEGER DEFAULT 0," +
            "  playtime_seconds INTEGER DEFAULT 0," +
            "  blocks_placed INTEGER DEFAULT 0," +
            "  blocks_broken INTEGER DEFAULT 0," +
            "  mobs_killed INTEGER DEFAULT 0," +
            "  kill_streak INTEGER DEFAULT 0," +
            "  highest_kill_streak INTEGER DEFAULT 0," +
            "  money_spent REAL DEFAULT 0," +
            "  money_made REAL DEFAULT 0," +
            "  tpauto INTEGER DEFAULT 0," +
            "  phantom_enabled INTEGER DEFAULT 1," +
            "  payments_enabled INTEGER DEFAULT 1," +
            "  scoreboard_visible INTEGER DEFAULT 1," +
            "  pay_alerts_enabled INTEGER DEFAULT 1," +
            "  hotbar_messages_enabled INTEGER DEFAULT 1," +
            "  worth_display_enabled INTEGER DEFAULT 1," +
            "  clear_entities_messages_enabled INTEGER DEFAULT 1," +
            "  bounty_alerts_enabled INTEGER DEFAULT 1," +
            "  tpa_confirm_menu_enabled INTEGER DEFAULT 1," +
            "  chainmail_on_respawn_enabled INTEGER DEFAULT 1," +
            "  lunar_teammates_enabled INTEGER DEFAULT 1," +
            "  tpa_requests_enabled INTEGER DEFAULT 1," +
            "  auto_tpahere_enabled INTEGER DEFAULT 0," +
            "  tpahere_requests_enabled INTEGER DEFAULT 1," +
            "  team_invites_enabled INTEGER DEFAULT 1," +
            "  mob_spawn_enabled INTEGER DEFAULT 1," +
              "  pay_confirm_menu_enabled INTEGER DEFAULT 1," +
              "  totem_particles_enabled INTEGER DEFAULT 1," +
              "  fast_crystals_enabled INTEGER DEFAULT 1," +
              "  amethyst_break_messages_enabled INTEGER DEFAULT 1," +
              "  private_messages_enabled INTEGER DEFAULT 1," +
              "  keyall_notifications_enabled INTEGER DEFAULT 1," +
              "  keyall_remaining_seconds INTEGER DEFAULT -1," +
              "  shard_booster_expiry INTEGER DEFAULT 0" +
              ")"
          );
        execute(
            "CREATE TABLE IF NOT EXISTS player_ip_history (" +
            "  player_uuid TEXT NOT NULL," +
            "  ip_address TEXT NOT NULL," +
            "  first_seen INTEGER NOT NULL DEFAULT 0," +
            "  last_seen INTEGER NOT NULL DEFAULT 0," +
            "  PRIMARY KEY (player_uuid, ip_address)" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS player_ignores (" +
            "  owner_uuid TEXT NOT NULL," +
            "  ignored_uuid TEXT NOT NULL," +
            "  ignored_name_snapshot TEXT," +
            "  created_at INTEGER NOT NULL," +
            "  PRIMARY KEY (owner_uuid, ignored_uuid)" +
            ")"
        );
        execute("CREATE INDEX IF NOT EXISTS idx_player_ignores_owner ON player_ignores(owner_uuid)");
        execute("CREATE INDEX IF NOT EXISTS idx_player_ignores_ignored ON player_ignores(ignored_uuid)");
        execute(
            "CREATE TABLE IF NOT EXISTS teams (" +
            "  name TEXT PRIMARY KEY," +
            "  leader_uuid TEXT," +
            "  home_world TEXT," +
            "  home_x REAL," +
            "  home_y REAL," +
            "  home_z REAL," +
            "  home_yaw REAL," +
            "  home_pitch REAL," +
            "  friendly_fire_enabled INTEGER DEFAULT 0" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS team_members (" +
            "  player_uuid TEXT," +
            "  team_name TEXT," +
            "  can_edit_home INTEGER DEFAULT 0," +
            "  can_manage_teammates INTEGER DEFAULT 0," +
            "  can_toggle_pvp INTEGER DEFAULT 0," +
            "  can_visit_home INTEGER DEFAULT 1," +
            "  can_use_team_chat INTEGER DEFAULT 1," +
            "  PRIMARY KEY (player_uuid)" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS homes (" +
            "  player_uuid TEXT," +
            "  home_name TEXT," +
            "  world TEXT," +
            "  x REAL, y REAL, z REAL," +
            "  yaw REAL, pitch REAL," +
            "  PRIMARY KEY (player_uuid, home_name)" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS bounties (" +
            "  target_uuid TEXT PRIMARY KEY," +
            "  amount REAL," +
            "  placer_uuid TEXT" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS warps (" +
            "  name TEXT PRIMARY KEY," +
            "  world TEXT," +
            "  x REAL, y REAL, z REAL," +
            "  yaw REAL, pitch REAL" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS portals (" +
            "  id TEXT PRIMARY KEY," +
            "  display_name TEXT," +
            "  cuboid_name TEXT," +
            "  destination_type TEXT," +
            "  destination_value TEXT," +
            "  enabled INTEGER DEFAULT 1," +
            "  permission TEXT," +
            "  priority INTEGER DEFAULT 0," +
            "  trigger_cooldown_ms INTEGER DEFAULT 1500," +
            "  enter_message TEXT," +
            "  hologram_world TEXT," +
            "  hologram_x REAL DEFAULT 0," +
            "  hologram_y REAL DEFAULT 0," +
            "  hologram_z REAL DEFAULT 0" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS sell_history (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  player_uuid TEXT," +
            "  item_name TEXT," +
            "  amount INTEGER," +
            "  price REAL," +
            "  timestamp INTEGER" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS punishments (" +
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
            ")"
        );
        execute("CREATE INDEX IF NOT EXISTS idx_punishments_target_issued ON punishments(target_uuid, issued_at DESC)");
        execute("CREATE INDEX IF NOT EXISTS idx_punishments_target_type ON punishments(target_uuid, type)");
        execute(
            "CREATE TABLE IF NOT EXISTS freeze_states (" +
            "  target_uuid TEXT PRIMARY KEY," +
            "  target_name_snapshot TEXT," +
            "  frozen_by_uuid TEXT," +
            "  frozen_by_name_snapshot TEXT," +
            "  frozen_at INTEGER NOT NULL," +
            "  source_server TEXT DEFAULT 'local'," +
            "  active INTEGER NOT NULL DEFAULT 1" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS staff_mode_states (" +
            "  staff_uuid TEXT PRIMARY KEY," +
            "  staff_name_snapshot TEXT," +
            "  enabled_at INTEGER NOT NULL," +
            "  source_server TEXT DEFAULT 'local'," +
            "  vanish_active INTEGER NOT NULL DEFAULT 0," +
            "  better_view_active INTEGER NOT NULL DEFAULT 0," +
            "  snapshot_present INTEGER NOT NULL DEFAULT 0," +
            "  previous_allow_flight INTEGER NOT NULL DEFAULT 0," +
            "  previous_flying INTEGER NOT NULL DEFAULT 0," +
            "  previous_selected_slot INTEGER NOT NULL DEFAULT 0," +
            "  night_vision_owned INTEGER NOT NULL DEFAULT 0," +
            "  previous_game_mode TEXT DEFAULT 'SURVIVAL'" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS staff_mode_snapshot_items (" +
            "  staff_uuid TEXT NOT NULL," +
            "  section TEXT NOT NULL," +
            "  slot INTEGER NOT NULL," +
            "  item_data TEXT," +
            "  PRIMARY KEY (staff_uuid, section, slot)" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS sell_progress (" +
            "  player_uuid TEXT," +
            "  category TEXT," +
            "  earned REAL DEFAULT 0," +
            "  PRIMARY KEY (player_uuid, category)" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS cuboids (" +
            "  name TEXT PRIMARY KEY," +
            "  world TEXT," +
            "  x1 INTEGER, y1 INTEGER, z1 INTEGER," +
            "  x2 INTEGER, y2 INTEGER, z2 INTEGER" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS ender_chest_profiles (" +
            "  player_uuid TEXT PRIMARY KEY," +
            "  rows INTEGER DEFAULT 6," +
            "  updated_at INTEGER DEFAULT 0" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS ender_chest_items (" +
            "  player_uuid TEXT," +
            "  slot INTEGER," +
            "  item_data TEXT," +
            "  PRIMARY KEY (player_uuid, slot)" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS player_crate_keys (" +
            "  player_uuid TEXT," +
            "  crate_id TEXT," +
            "  amount INTEGER DEFAULT 0," +
            "  updated_at INTEGER DEFAULT 0," +
            "  PRIMARY KEY (player_uuid, crate_id)" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS crate_blocks (" +
            "  world TEXT," +
            "  x INTEGER," +
            "  y INTEGER," +
            "  z INTEGER," +
            "  crate_id TEXT," +
            "  updated_at INTEGER DEFAULT 0," +
            "  PRIMARY KEY (world, x, y, z)" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS spawners (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  world TEXT NOT NULL," +
            "  x INTEGER NOT NULL," +
            "  y INTEGER NOT NULL," +
            "  z INTEGER NOT NULL," +
            "  owner_uuid TEXT NOT NULL," +
            "  owner_name TEXT NOT NULL," +
            "  mob_type TEXT NOT NULL," +
            "  stack_amount INTEGER NOT NULL," +
            "  access_mode TEXT NOT NULL," +
            "  last_processed_at INTEGER NOT NULL," +
            "  created_at INTEGER NOT NULL," +
            "  updated_at INTEGER NOT NULL," +
            "  UNIQUE(world, x, y, z)" +
            ")"
        );
        execute(
            "CREATE TABLE IF NOT EXISTS spawner_loot (" +
            "  spawner_id INTEGER NOT NULL," +
            "  loot_key TEXT NOT NULL," +
            "  material TEXT NOT NULL," +
            "  amount INTEGER NOT NULL," +
            "  PRIMARY KEY (spawner_id, loot_key)" +
            ")"
        );
    }

    private void ensurePlayerColumns() throws SQLException {
        ensureColumnExists("players", "blocks_placed", "INTEGER DEFAULT 0");
        ensureColumnExists("players", "blocks_broken", "INTEGER DEFAULT 0");
        ensureColumnExists("players", "mobs_killed", "INTEGER DEFAULT 0");
        ensureColumnExists("players", "kill_streak", "INTEGER DEFAULT 0");
        ensureColumnExists("players", "highest_kill_streak", "INTEGER DEFAULT 0");
        ensureColumnExists("players", "money_spent", "REAL DEFAULT 0");
        ensureColumnExists("players", "money_made", "REAL DEFAULT 0");
        ensureColumnExists("players", "scoreboard_visible", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "pay_alerts_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "hotbar_messages_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "worth_display_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "clear_entities_messages_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "bounty_alerts_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "tpa_confirm_menu_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "chainmail_on_respawn_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "lunar_teammates_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "tpa_requests_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "auto_tpahere_enabled", "INTEGER DEFAULT 0");
        ensureColumnExists("players", "tpahere_requests_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "team_invites_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "mob_spawn_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "pay_confirm_menu_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "totem_particles_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "fast_crystals_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "amethyst_break_messages_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "private_messages_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "keyall_notifications_enabled", "INTEGER DEFAULT 1");
        ensureColumnExists("players", "keyall_remaining_seconds", "INTEGER DEFAULT -1");
        ensureColumnExists("players", "shard_booster_expiry", "INTEGER DEFAULT 0");
    }

    private void ensurePortalColumns() throws SQLException {
        ensureColumnExists("portals", "hologram_world", "TEXT");
        ensureColumnExists("portals", "hologram_x", "REAL DEFAULT 0");
        ensureColumnExists("portals", "hologram_y", "REAL DEFAULT 0");
        ensureColumnExists("portals", "hologram_z", "REAL DEFAULT 0");
    }

    private void ensureTeamColumns() throws SQLException {
        ensureColumnExists("teams", "friendly_fire_enabled", "INTEGER DEFAULT 0");

        boolean legacyPvpColumnExists = hasColumn("team_members", "pvp_enabled");
        boolean toggleColumnExists = hasColumn("team_members", "can_toggle_pvp");

        ensureColumnExists("team_members", "can_toggle_pvp", "INTEGER DEFAULT 0");
        ensureColumnExists("team_members", "can_visit_home", "INTEGER DEFAULT 1");
        ensureColumnExists("team_members", "can_use_team_chat", "INTEGER DEFAULT 1");

        if (legacyPvpColumnExists && !toggleColumnExists) {
            execute("UPDATE team_members SET can_toggle_pvp = COALESCE(pvp_enabled, 0)");
        }
    }

    private void ensureStaffModeColumns() throws SQLException {
        ensureColumnExists("staff_mode_states", "previous_game_mode", "TEXT DEFAULT 'SURVIVAL'");
    }

    private void ensureColumnExists(String table, String column, String definition) throws SQLException {
        if (hasColumn(table, column)) return;
        try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    public boolean hasColumn(String table, String column) throws SQLException {
        if (!isMySql()) {
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
                while (rs.next()) {
                    if (column.equalsIgnoreCase(rs.getString("name"))) {
                        return true;
                    }
                }
            }
            return false;
        }

        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, table, null)) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    public PlayerData loadPlayer(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPlayerRow(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load player " + uuid, e);
        }
        return null;
    }

    public List<PlayerData> loadAllPlayers() {
        List<PlayerData> players = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM players")) {
            while (rs.next()) {
                players.add(mapPlayerRow(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load all players", e);
        }
        return players;
    }

    private PlayerData mapPlayerRow(ResultSet rs) throws SQLException {
        PlayerData data = new PlayerData(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("username")
        );
        data.setMoney(rs.getDouble("money"));
        data.setShards(rs.getLong("shards"));
        data.setKills(rs.getInt("kills"));
        data.setDeaths(rs.getInt("deaths"));
        data.setPlaytimeSeconds(rs.getLong("playtime_seconds"));
        data.setBlocksPlaced(rs.getLong("blocks_placed"));
        data.setBlocksBroken(rs.getLong("blocks_broken"));
        data.setMobsKilled(rs.getLong("mobs_killed"));
        data.setKillStreak(rs.getInt("kill_streak"));
        data.setHighestKillStreak(rs.getInt("highest_kill_streak"));
        data.setMoneySpent(rs.getDouble("money_spent"));
        data.setMoneyMade(rs.getDouble("money_made"));
        data.setTpauto(rs.getInt("tpauto") == 1);
        data.setPhantomEnabled(rs.getInt("phantom_enabled") == 1);
        data.setPaymentsEnabled(rs.getInt("payments_enabled") == 1);
        data.setScoreboardVisible(rs.getInt("scoreboard_visible") != 0);
        data.setPayAlertsEnabled(rs.getInt("pay_alerts_enabled") != 0);
        data.setHotbarMessagesEnabled(rs.getInt("hotbar_messages_enabled") != 0);
        data.setWorthDisplayEnabled(rs.getInt("worth_display_enabled") != 0);
        data.setClearEntitiesMessagesEnabled(rs.getInt("clear_entities_messages_enabled") != 0);
        data.setBountyAlertsEnabled(rs.getInt("bounty_alerts_enabled") != 0);
        data.setTpaConfirmMenuEnabled(rs.getInt("tpa_confirm_menu_enabled") != 0);
        data.setChainmailOnRespawnEnabled(rs.getInt("chainmail_on_respawn_enabled") != 0);
        data.setLunarTeammatesEnabled(rs.getInt("lunar_teammates_enabled") != 0);
        data.setTpaRequestsEnabled(rs.getInt("tpa_requests_enabled") != 0);
        data.setAutoTpaHereEnabled(rs.getInt("auto_tpahere_enabled") != 0);
        data.setTpaHereRequestsEnabled(rs.getInt("tpahere_requests_enabled") != 0);
        data.setTeamInvitesEnabled(rs.getInt("team_invites_enabled") != 0);
        data.setMobSpawnEnabled(rs.getInt("mob_spawn_enabled") != 0);
        data.setPayConfirmMenuEnabled(rs.getInt("pay_confirm_menu_enabled") != 0);
        data.setTotemParticlesEnabled(rs.getInt("totem_particles_enabled") != 0);
        data.setFastCrystalsEnabled(rs.getInt("fast_crystals_enabled") != 0);
        data.setAmethystBreakMessagesEnabled(rs.getInt("amethyst_break_messages_enabled") != 0);
        data.setPrivateMessagesEnabled(rs.getInt("private_messages_enabled") != 0);
        data.setKeyAllNotificationsEnabled(rs.getInt("keyall_notifications_enabled") != 0);
        data.setKeyAllRemainingSeconds(rs.getLong("keyall_remaining_seconds"));
        data.setShardBoosterExpiryMillis(rs.getLong("shard_booster_expiry"));
        data.setDirty(false);
        return data;
    }

    public UUID findPlayerUuidByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT uuid FROM players WHERE LOWER(username) = LOWER(?) LIMIT 1")) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to resolve player uuid for " + username, e);
        }
        return null;
    }

    public UUID findPunishmentTargetUuidByName(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT target_uuid FROM punishments " +
                "WHERE LOWER(target_name_snapshot) = LOWER(?) " +
                "ORDER BY issued_at DESC, id DESC LIMIT 1")) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("target_uuid"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to resolve punishment target uuid for " + username, e);
        }
        return null;
    }

    public String getLastKnownUsername(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT username FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get username for " + uuid, e);
        }
        return null;
    }

    public List<IgnoreEntry> loadIgnoredPlayers(UUID ownerUuid) {
        List<IgnoreEntry> ignoredPlayers = new ArrayList<>();
        if (ownerUuid == null) {
            return ignoredPlayers;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT owner_uuid, ignored_uuid, ignored_name_snapshot, created_at " +
                "FROM player_ignores WHERE owner_uuid = ? " +
                "ORDER BY LOWER(COALESCE(ignored_name_snapshot, ignored_uuid)) ASC")) {
            ps.setString(1, ownerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ignoredPlayers.add(new IgnoreEntry(
                            UUID.fromString(rs.getString("owner_uuid")),
                            UUID.fromString(rs.getString("ignored_uuid")),
                            rs.getString("ignored_name_snapshot"),
                            rs.getLong("created_at")
                    ));
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load ignored players for " + ownerUuid, e);
        }
        return ignoredPlayers;
    }

    public boolean addIgnoredPlayer(UUID ownerUuid, UUID ignoredUuid, String ignoredNameSnapshot, long createdAt) {
        if (ownerUuid == null || ignoredUuid == null) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO player_ignores " +
                "(owner_uuid, ignored_uuid, ignored_name_snapshot, created_at) VALUES (?,?,?,?)")) {
            ps.setString(1, ownerUuid.toString());
            ps.setString(2, ignoredUuid.toString());
            ps.setString(3, ignoredNameSnapshot);
            ps.setLong(4, createdAt);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add ignored player " + ignoredUuid + " for " + ownerUuid, e);
        }
        return false;
    }

    public boolean removeIgnoredPlayer(UUID ownerUuid, UUID ignoredUuid) {
        if (ownerUuid == null || ignoredUuid == null) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM player_ignores WHERE owner_uuid = ? AND ignored_uuid = ?")) {
            ps.setString(1, ownerUuid.toString());
            ps.setString(2, ignoredUuid.toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove ignored player " + ignoredUuid + " for " + ownerUuid, e);
        }
        return false;
    }

    public String getLatestPunishmentTargetName(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT target_name_snapshot FROM punishments " +
                "WHERE target_uuid = ? AND target_name_snapshot IS NOT NULL AND target_name_snapshot != '' " +
                "ORDER BY issued_at DESC, id DESC LIMIT 1")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("target_name_snapshot");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to resolve punishment target name for " + uuid, e);
        }
        return null;
    }

    public void savePlayerIpAddress(UUID playerUuid, String ipAddress, long seenAt) {
        if (playerUuid == null || ipAddress == null || ipAddress.isBlank()) {
            return;
        }

        String normalizedIp = ipAddress.trim();
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE player_ip_history SET last_seen = ? WHERE player_uuid = ? AND ip_address = ?")) {
            update.setLong(1, seenAt);
            update.setString(2, playerUuid.toString());
            update.setString(3, normalizedIp);
            if (update.executeUpdate() > 0) {
                return;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update IP history for " + playerUuid, e);
            return;
        }

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO player_ip_history (player_uuid, ip_address, first_seen, last_seen) VALUES (?,?,?,?)")) {
            insert.setString(1, playerUuid.toString());
            insert.setString(2, normalizedIp);
            insert.setLong(3, seenAt);
            insert.setLong(4, seenAt);
            insert.executeUpdate();
        } catch (SQLException e) {
            try (PreparedStatement retryUpdate = connection.prepareStatement(
                    "UPDATE player_ip_history SET last_seen = ? WHERE player_uuid = ? AND ip_address = ?")) {
                retryUpdate.setLong(1, seenAt);
                retryUpdate.setString(2, playerUuid.toString());
                retryUpdate.setString(3, normalizedIp);
                retryUpdate.executeUpdate();
            } catch (SQLException retryException) {
                plugin.getLogger().log(Level.WARNING, "Failed to save IP history for " + playerUuid, retryException);
            }
        }
    }

    public List<String> loadKnownIpAddresses(UUID playerUuid) {
        if (playerUuid == null) {
            return List.of();
        }

        List<String> ipAddresses = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT ip_address FROM player_ip_history WHERE player_uuid = ? ORDER BY last_seen DESC, ip_address ASC")) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ipAddress = rs.getString("ip_address");
                    if (ipAddress != null && !ipAddress.isBlank()) {
                        ipAddresses.add(ipAddress);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load IP history for " + playerUuid, e);
        }
        return ipAddresses;
    }

    public List<AltAccountMatch> loadAltAccounts(UUID playerUuid) {
        if (playerUuid == null) {
            return List.of();
        }

        Map<String, AltAccountAccumulator> matchesByUuid = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT hist.player_uuid, p.username, hist.ip_address, hist.last_seen " +
                        "FROM player_ip_history target_hist " +
                        "JOIN player_ip_history hist ON hist.ip_address = target_hist.ip_address " +
                        "LEFT JOIN players p ON p.uuid = hist.player_uuid " +
                        "WHERE target_hist.player_uuid = ? AND hist.player_uuid <> ? " +
                        "ORDER BY hist.last_seen DESC, LOWER(COALESCE(p.username, hist.player_uuid)) ASC, hist.ip_address ASC")) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String altUuidRaw = rs.getString("player_uuid");
                    if (altUuidRaw == null || altUuidRaw.isBlank()) {
                        continue;
                    }

                    String username = rs.getString("username");
                    if (username == null || username.isBlank()) {
                        username = altUuidRaw.substring(0, Math.min(8, altUuidRaw.length()));
                    }
                    final String resolvedUsername = username;

                    AltAccountAccumulator accumulator = matchesByUuid.computeIfAbsent(
                            altUuidRaw,
                            ignored -> new AltAccountAccumulator(UUID.fromString(altUuidRaw), resolvedUsername)
                    );
                    String sharedIp = rs.getString("ip_address");
                    if (sharedIp != null && !sharedIp.isBlank()) {
                        accumulator.sharedIps.add(sharedIp);
                    }
                    accumulator.lastSeenAt = Math.max(accumulator.lastSeenAt, rs.getLong("last_seen"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load alt accounts for " + playerUuid, e);
        }

        List<AltAccountMatch> matches = new ArrayList<>();
        for (AltAccountAccumulator accumulator : matchesByUuid.values()) {
            matches.add(new AltAccountMatch(
                    accumulator.uuid,
                    accumulator.username,
                    List.copyOf(accumulator.sharedIps),
                    accumulator.lastSeenAt
            ));
        }
        return matches;
    }

    private static final class AltAccountAccumulator {
        private final UUID uuid;
        private final String username;
        private final LinkedHashSet<String> sharedIps = new LinkedHashSet<>();
        private long lastSeenAt;

        private AltAccountAccumulator(UUID uuid, String username) {
            this.uuid = uuid;
            this.username = username;
        }
    }

    public void savePlayer(PlayerData data) {
        try (PreparedStatement ps = connection.prepareStatement("""
                REPLACE INTO players
                (uuid, username, money, shards, kills, deaths, playtime_seconds, blocks_placed, blocks_broken, mobs_killed,
                 kill_streak, highest_kill_streak, money_spent, money_made, tpauto, phantom_enabled, payments_enabled,
                 scoreboard_visible, pay_alerts_enabled, hotbar_messages_enabled, worth_display_enabled,
                 clear_entities_messages_enabled, bounty_alerts_enabled, tpa_confirm_menu_enabled,
                 chainmail_on_respawn_enabled, lunar_teammates_enabled, tpa_requests_enabled, auto_tpahere_enabled,
                 tpahere_requests_enabled, team_invites_enabled, mob_spawn_enabled, pay_confirm_menu_enabled,
                 totem_particles_enabled, fast_crystals_enabled, amethyst_break_messages_enabled,
                 private_messages_enabled, keyall_notifications_enabled, keyall_remaining_seconds, shard_booster_expiry)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """)) {
            ps.setString(1, data.getUuid().toString());
            ps.setString(2, data.getUsername());
            ps.setDouble(3, data.getMoney());
            ps.setLong(4, data.getShards());
            ps.setInt(5, data.getKills());
            ps.setInt(6, data.getDeaths());
            ps.setLong(7, data.getTotalPlaytimeSeconds());
            ps.setLong(8, data.getBlocksPlaced());
            ps.setLong(9, data.getBlocksBroken());
            ps.setLong(10, data.getMobsKilled());
            ps.setInt(11, data.getKillStreak());
            ps.setInt(12, data.getHighestKillStreak());
            ps.setDouble(13, data.getMoneySpent());
            ps.setDouble(14, data.getMoneyMade());
            ps.setInt(15, data.isTpauto() ? 1 : 0);
            ps.setInt(16, data.isPhantomEnabled() ? 1 : 0);
            ps.setInt(17, data.isPaymentsEnabled() ? 1 : 0);
            ps.setInt(18, data.isScoreboardVisible() ? 1 : 0);
            ps.setInt(19, data.isPayAlertsEnabled() ? 1 : 0);
            ps.setInt(20, data.isHotbarMessagesEnabled() ? 1 : 0);
            ps.setInt(21, data.isWorthDisplayEnabled() ? 1 : 0);
            ps.setInt(22, data.isClearEntitiesMessagesEnabled() ? 1 : 0);
            ps.setInt(23, data.isBountyAlertsEnabled() ? 1 : 0);
            ps.setInt(24, data.isTpaConfirmMenuEnabled() ? 1 : 0);
            ps.setInt(25, data.isChainmailOnRespawnEnabled() ? 1 : 0);
            ps.setInt(26, data.isLunarTeammatesEnabled() ? 1 : 0);
            ps.setInt(27, data.isTpaRequestsEnabled() ? 1 : 0);
            ps.setInt(28, data.isAutoTpaHereEnabled() ? 1 : 0);
            ps.setInt(29, data.isTpaHereRequestsEnabled() ? 1 : 0);
            ps.setInt(30, data.isTeamInvitesEnabled() ? 1 : 0);
            ps.setInt(31, data.isMobSpawnEnabled() ? 1 : 0);
            ps.setInt(32, data.isPayConfirmMenuEnabled() ? 1 : 0);
            ps.setInt(33, data.isTotemParticlesEnabled() ? 1 : 0);
            ps.setInt(34, data.isFastCrystalsEnabled() ? 1 : 0);
            ps.setInt(35, data.isAmethystBreakMessagesEnabled() ? 1 : 0);
            ps.setInt(36, data.isPrivateMessagesEnabled() ? 1 : 0);
            ps.setInt(37, data.isKeyAllNotificationsEnabled() ? 1 : 0);
            ps.setLong(38, data.getKeyAllRemainingSeconds());
            ps.setLong(39, data.getShardBoosterExpiryMillis());
            ps.executeUpdate();
            data.setDirty(false);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save player " + data.getUuid(), e);
        }
    }

    public int countPlayersWithTrackedStats() {
        String sql = """
                SELECT COUNT(*) FROM players
                WHERE kills != 0
                   OR deaths != 0
                   OR playtime_seconds != 0
                   OR blocks_placed != 0
                   OR blocks_broken != 0
                   OR mobs_killed != 0
                   OR kill_streak != 0
                   OR highest_kill_streak != 0
                   OR money_spent != 0
                   OR money_made != 0
                """;
        return countQuery(sql);
    }

    public int resetPlayerStats() {
        String sql = """
                UPDATE players SET
                    kills = 0,
                    deaths = 0,
                    playtime_seconds = 0,
                    blocks_placed = 0,
                    blocks_broken = 0,
                    mobs_killed = 0,
                    kill_streak = 0,
                    highest_kill_streak = 0,
                    money_spent = 0,
                    money_made = 0
                """;
        return executeUpdate(sql);
    }

    public List<PlayerData> getTopByMoney(int limit) {
        return getTop("money", limit);
    }

    public List<PlayerData> getTopByKills(int limit) {
        return getTop("kills", limit);
    }

    public List<PlayerData> getTopByShards(int limit) {
        return getTop("shards", limit);
    }

    public List<PlayerData> getTopByDeaths(int limit) {
        return getTop("deaths", limit);
    }

    public List<PlayerData> getTopByPlaytime(int limit) {
        return getTop("playtime_seconds", limit);
    }

    private List<PlayerData> getTop(String column, int limit) {
        List<PlayerData> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM players ORDER BY " + column + " DESC LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPlayerRow(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get top list", e);
        }
        return list;
    }

    public int loadEnderChestRows(UUID uuid, int fallbackRows) {
        if (connection == null) {
            return Math.max(1, fallbackRows);
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT rows FROM ender_chest_profiles WHERE player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Math.max(1, rs.getInt("rows"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load Ender Chest rows for " + uuid, e);
        }
        return Math.max(1, fallbackRows);
    }

    public ItemStack[] loadEnderChestContents(UUID uuid, int size) {
        ItemStack[] contents = new ItemStack[Math.max(9, size)];
        if (connection == null) {
            return contents;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT slot, item_data FROM ender_chest_items WHERE player_uuid = ? ORDER BY slot ASC")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int slot = rs.getInt("slot");
                    if (slot < 0 || slot >= contents.length) {
                        continue;
                    }

                    ItemStack item = deserializeItemStack(rs.getString("item_data"));
                    if (item == null || item.getType().isAir()) {
                        continue;
                    }

                    contents[slot] = item;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load Ender Chest contents for " + uuid, e);
        }

        return contents;
    }

    public Map<String, Integer> loadCrateKeyBalances(UUID uuid) {
        Map<String, Integer> balances = new LinkedHashMap<>();
        if (connection == null || uuid == null) {
            return balances;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT crate_id, amount FROM player_crate_keys WHERE player_uuid = ? ORDER BY crate_id ASC")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    balances.put(rs.getString("crate_id"), Math.max(0, rs.getInt("amount")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load crate key balances for " + uuid, e);
        }
        return balances;
    }

    public int getCrateKeyAmount(UUID uuid, String crateId) {
        if (connection == null || uuid == null || crateId == null || crateId.isBlank()) {
            return 0;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT amount FROM player_crate_keys WHERE player_uuid = ? AND crate_id = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, crateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Math.max(0, rs.getInt("amount"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load crate key amount for " + uuid + "/" + crateId, e);
        }
        return 0;
    }

    public int setCrateKeyAmount(UUID uuid, String crateId, int amount) {
        if (connection == null || uuid == null || crateId == null || crateId.isBlank()) {
            return 0;
        }

        int normalizedAmount = Math.max(0, amount);
        if (normalizedAmount == 0) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM player_crate_keys WHERE player_uuid = ? AND crate_id = ?")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, crateId);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to delete crate key balance for " + uuid + "/" + crateId, e);
            }
            return 0;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO player_crate_keys (player_uuid, crate_id, amount, updated_at) VALUES (?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, crateId);
            ps.setInt(3, normalizedAmount);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save crate key balance for " + uuid + "/" + crateId, e);
        }

        return normalizedAmount;
    }

    public int addCrateKeys(UUID uuid, String crateId, int amount) {
        if (amount <= 0) {
            return getCrateKeyAmount(uuid, crateId);
        }

        int newAmount = getCrateKeyAmount(uuid, crateId) + amount;
        return setCrateKeyAmount(uuid, crateId, newAmount);
    }

    public boolean removeCrateKeys(UUID uuid, String crateId, int amount) {
        if (amount <= 0) {
            return true;
        }

        int current = getCrateKeyAmount(uuid, crateId);
        if (current < amount) {
            return false;
        }

        setCrateKeyAmount(uuid, crateId, current - amount);
        return true;
    }

    public record CrateBlockData(String world, int x, int y, int z, String crateId) {
    }

    public List<CrateBlockData> loadCrateBlocks() {
        List<CrateBlockData> blocks = new ArrayList<>();
        if (connection == null) {
            return blocks;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT world, x, y, z, crate_id FROM crate_blocks ORDER BY world ASC, y ASC, x ASC, z ASC")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    blocks.add(new CrateBlockData(
                            rs.getString("world"),
                            rs.getInt("x"),
                            rs.getInt("y"),
                            rs.getInt("z"),
                            rs.getString("crate_id")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load crate block bindings", e);
        }
        return blocks;
    }

    public boolean saveCrateBlock(String world, int x, int y, int z, String crateId) {
        if (connection == null || world == null || world.isBlank() || crateId == null || crateId.isBlank()) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO crate_blocks (world, x, y, z, crate_id, updated_at) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            ps.setString(5, crateId);
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save crate block binding for "
                    + world + " " + x + "," + y + "," + z, e);
        }
        return false;
    }

    public boolean deleteCrateBlock(String world, int x, int y, int z) {
        if (connection == null || world == null || world.isBlank()) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM crate_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete crate block binding for "
                    + world + " " + x + "," + y + "," + z, e);
        }
        return false;
    }

    public int deleteCrateKeyBalances(String crateId) {
        if (connection == null || crateId == null || crateId.isBlank()) {
            return 0;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM player_crate_keys WHERE crate_id = ?")) {
            ps.setString(1, crateId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete crate key balances for " + crateId, e);
        }
        return 0;
    }

    public int deleteCrateBlocksByCrateId(String crateId) {
        if (connection == null || crateId == null || crateId.isBlank()) {
            return 0;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM crate_blocks WHERE crate_id = ?")) {
            ps.setString(1, crateId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete crate block bindings for " + crateId, e);
        }
        return 0;
    }

    public boolean saveEnderChest(UUID uuid, int rows, ItemStack[] contents) {
        if (connection == null) {
            return false;
        }

        boolean originalAutoCommit;
        try {
            originalAutoCommit = connection.getAutoCommit();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to inspect auto-commit state before Ender Chest save", e);
            return false;
        }

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement profileStatement = connection.prepareStatement(
                    "REPLACE INTO ender_chest_profiles (player_uuid, rows, updated_at) VALUES (?,?,?)")) {
                profileStatement.setString(1, uuid.toString());
                profileStatement.setInt(2, Math.max(1, rows));
                profileStatement.setLong(3, System.currentTimeMillis());
                profileStatement.executeUpdate();
            }

            try (PreparedStatement deleteStatement = connection.prepareStatement(
                    "DELETE FROM ender_chest_items WHERE player_uuid = ?")) {
                deleteStatement.setString(1, uuid.toString());
                deleteStatement.executeUpdate();
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO ender_chest_items (player_uuid, slot, item_data) VALUES (?,?,?)")) {
                for (int slot = 0; slot < contents.length; slot++) {
                    ItemStack item = contents[slot];
                    if (item == null || item.getType().isAir()) {
                        continue;
                    }

                    insertStatement.setString(1, uuid.toString());
                    insertStatement.setInt(2, slot);
                    insertStatement.setString(3, serializeItemStack(item));
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            }

            connection.commit();
            return true;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                plugin.getLogger().log(Level.WARNING, "Failed to roll back Ender Chest save for " + uuid, rollbackException);
            }
            plugin.getLogger().log(Level.WARNING, "Failed to save Ender Chest for " + uuid, e);
            return false;
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to restore auto-commit after Ender Chest save", e);
            }
        }
    }

    // â”€â”€ Homes â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public List<Home> loadHomes(UUID uuid) {
        List<Home> homes = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM homes WHERE player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    World w = Bukkit.getWorld(rs.getString("world"));
                    if (w == null) continue;
                    Location loc = new Location(w,
                            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch"));
                    homes.add(new Home(uuid, rs.getString("home_name"), loc));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load homes for " + uuid, e);
        }
        return homes;
    }

    public void saveHome(Home home) {
        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO homes (player_uuid, home_name, world, x, y, z, yaw, pitch)" +
                " VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, home.getOwnerUuid().toString());
            ps.setString(2, home.getName());
            Location l = home.getLocation();
            ps.setString(3, l.getWorld().getName());
            ps.setDouble(4, l.getX());
            ps.setDouble(5, l.getY());
            ps.setDouble(6, l.getZ());
            ps.setFloat(7, l.getYaw());
            ps.setFloat(8, l.getPitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save home", e);
        }
    }

    public void deleteHome(UUID uuid, String name) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM homes WHERE player_uuid = ? AND home_name = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete home", e);
        }
    }

    public int countHomes() {
        return countRows("homes");
    }

    public int clearHomes() {
        return executeUpdate("DELETE FROM homes");
    }

    // â”€â”€ Teams â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public int countTeams() {
        return countRows("teams");
    }

    public int clearTeams() {
        executeUpdate("DELETE FROM team_members");
        return executeUpdate("DELETE FROM teams");
    }

    public List<Team> loadAllTeams() {
        List<Team> teams = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM teams")) {
            while (rs.next()) {
                Team team = new Team(rs.getString("name"),
                        UUID.fromString(rs.getString("leader_uuid")));
                team.setFriendlyFireEnabled(rs.getInt("friendly_fire_enabled") == 1);
                String worldName = rs.getString("home_world");
                if (worldName != null) {
                    World w = Bukkit.getWorld(worldName);
                    if (w != null) {
                        team.setHome(new Location(w,
                                rs.getDouble("home_x"), rs.getDouble("home_y"), rs.getDouble("home_z"),
                                rs.getFloat("home_yaw"), rs.getFloat("home_pitch")));
                    }
                }
                teams.add(team);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load teams", e);
        }
        // Load members
        for (Team team : teams) {
            loadTeamMembers(team);
        }
        return teams;
    }

    private void loadTeamMembers(Team team) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM team_members WHERE team_name = ?")) {
            ps.setString(1, team.getName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID memberUuid = UUID.fromString(rs.getString("player_uuid"));
                    team.addMember(memberUuid);
                    Team.TeamMember member = team.getMember(memberUuid);
                    if (member != null) {
                        member.setCanEditHome(rs.getInt("can_edit_home") == 1);
                        member.setCanManageTeammates(rs.getInt("can_manage_teammates") == 1);
                        member.setCanTogglePvp(rs.getInt("can_toggle_pvp") == 1);
                        member.setCanVisitHome(rs.getInt("can_visit_home") == 1);
                        member.setCanUseTeamChat(rs.getInt("can_use_team_chat") == 1);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load team members for " + team.getName(), e);
        }
    }

    public void saveTeam(Team team) {
        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO teams (name, leader_uuid, home_world, home_x, home_y, home_z, home_yaw, home_pitch, friendly_fire_enabled)" +
                " VALUES (?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, team.getName());
            ps.setString(2, team.getLeaderUuid().toString());
            Location h = team.getHome();
            if (h != null) {
                ps.setString(3, h.getWorld().getName());
                ps.setDouble(4, h.getX());
                ps.setDouble(5, h.getY());
                ps.setDouble(6, h.getZ());
                ps.setFloat(7, h.getYaw());
                ps.setFloat(8, h.getPitch());
            } else {
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.REAL); ps.setNull(5, Types.REAL);
                ps.setNull(6, Types.REAL); ps.setNull(7, Types.REAL);
                ps.setNull(8, Types.REAL);
            }
            ps.setInt(9, team.isFriendlyFireEnabled() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save team " + team.getName(), e);
        }
        // Save members
        saveTeamMembers(team);
    }

    private void saveTeamMembers(Team team) {
        try (PreparedStatement del = connection.prepareStatement(
                "DELETE FROM team_members WHERE team_name = ?")) {
            del.setString(1, team.getName());
            del.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to clear team members", e);
        }
        for (Team.TeamMember member : team.getMembers().values()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO team_members (player_uuid, team_name, can_edit_home, can_manage_teammates, can_toggle_pvp, can_visit_home, can_use_team_chat)" +
                    " VALUES (?,?,?,?,?,?,?)")) {
                ps.setString(1, member.getUuid().toString());
                ps.setString(2, team.getName());
                ps.setInt(3, member.canEditHome() ? 1 : 0);
                ps.setInt(4, member.canManageTeammates() ? 1 : 0);
                ps.setInt(5, member.canTogglePvp() ? 1 : 0);
                ps.setInt(6, member.canVisitHome() ? 1 : 0);
                ps.setInt(7, member.canUseTeamChat() ? 1 : 0);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save team member", e);
            }
        }
    }

    public void deleteTeam(String name) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM teams WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete team", e);
        }
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM team_members WHERE team_name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete team members", e);
        }
    }

    // â”€â”€ Bounties â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public List<Bounty> loadAllBounties() {
        List<Bounty> bounties = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM bounties")) {
            while (rs.next()) {
                bounties.add(new Bounty(
                        UUID.fromString(rs.getString("target_uuid")),
                        rs.getDouble("amount"),
                        UUID.fromString(rs.getString("placer_uuid"))
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load bounties", e);
        }
        return bounties;
    }

    public int countBounties() {
        return countRows("bounties");
    }

    public int clearBounties() {
        return executeUpdate("DELETE FROM bounties");
    }

    public void saveBounty(Bounty bounty) {
        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO bounties (target_uuid, amount, placer_uuid) VALUES (?,?,?)")) {
            ps.setString(1, bounty.getTargetUuid().toString());
            ps.setDouble(2, bounty.getAmount());
            ps.setString(3, bounty.getPlacerUuid().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save bounty", e);
        }
    }

    public void deleteBounty(UUID targetUuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM bounties WHERE target_uuid = ?")) {
            ps.setString(1, targetUuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete bounty", e);
        }
    }

    // â”€â”€ Warps â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public Map<String, Location> loadWarps() {
        Map<String, Location> warps = new LinkedHashMap<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM warps")) {
            while (rs.next()) {
                World w = Bukkit.getWorld(rs.getString("world"));
                if (w == null) continue;
                warps.put(rs.getString("name").toLowerCase(),
                        new Location(w, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                                rs.getFloat("yaw"), rs.getFloat("pitch")));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load warps", e);
        }
        return warps;
    }

    public void saveWarp(String name, Location loc) {
        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO warps (name, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?)")) {
            ps.setString(1, name.toLowerCase());
            ps.setString(2, loc.getWorld().getName());
            ps.setDouble(3, loc.getX());
            ps.setDouble(4, loc.getY());
            ps.setDouble(5, loc.getZ());
            ps.setFloat(6, loc.getYaw());
            ps.setFloat(7, loc.getPitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save warp " + name, e);
        }
    }

    public void deleteWarp(String name) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM warps WHERE name = ?")) {
            ps.setString(1, name.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete warp " + name, e);
        }
    }

    // â”€â”€ Sell history â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public record PortalData(
            String displayName,
            String cuboidName,
            String destinationType,
            String destinationValue,
            boolean enabled,
            String permission,
            int priority,
            long triggerCooldownMs,
            String enterMessage,
            String hologramWorld,
            double hologramX,
            double hologramY,
            double hologramZ
    ) {}

    public Map<String, PortalData> loadPortals() {
        Map<String, PortalData> portals = new LinkedHashMap<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM portals ORDER BY LOWER(id)")) {
            while (rs.next()) {
                portals.put(rs.getString("id"), new PortalData(
                        rs.getString("display_name"),
                        rs.getString("cuboid_name"),
                        rs.getString("destination_type"),
                        rs.getString("destination_value"),
                        rs.getInt("enabled") != 0,
                        rs.getString("permission"),
                        rs.getInt("priority"),
                        rs.getLong("trigger_cooldown_ms"),
                        rs.getString("enter_message"),
                        rs.getString("hologram_world"),
                        rs.getDouble("hologram_x"),
                        rs.getDouble("hologram_y"),
                        rs.getDouble("hologram_z")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load portals", e);
        }
        return portals;
    }

    public void savePortal(String id, String displayName, String cuboidName, String destinationType,
                           String destinationValue, boolean enabled, String permission, int priority,
                           long triggerCooldownMs, String enterMessage, String hologramWorld,
                           double hologramX, double hologramY, double hologramZ) {
        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO portals " +
                        "(id, display_name, cuboid_name, destination_type, destination_value, enabled, permission, priority, trigger_cooldown_ms, enter_message, hologram_world, hologram_x, hologram_y, hologram_z) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, id);
            ps.setString(2, displayName);
            ps.setString(3, cuboidName);
            ps.setString(4, destinationType);
            ps.setString(5, destinationValue);
            ps.setInt(6, enabled ? 1 : 0);
            ps.setString(7, permission);
            ps.setInt(8, priority);
            ps.setLong(9, triggerCooldownMs);
            ps.setString(10, enterMessage);
            ps.setString(11, hologramWorld);
            ps.setDouble(12, hologramX);
            ps.setDouble(13, hologramY);
            ps.setDouble(14, hologramZ);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save portal " + id, e);
        }
    }

    public void deletePortal(String id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM portals WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete portal " + id, e);
        }
    }

    public void addSellHistory(UUID uuid, String itemName, int amount, double price) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO sell_history (player_uuid, item_name, amount, price, timestamp) VALUES (?,?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, itemName);
            ps.setInt(3, amount);
            ps.setDouble(4, price);
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add sell history", e);
        }
    }

    public void addSellProgress(UUID uuid, SellCategory category, double amount) {
        if (amount <= 0) {
            return;
        }

        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE sell_progress SET earned = earned + ? WHERE player_uuid = ? AND category = ?")) {
            update.setDouble(1, amount);
            update.setString(2, uuid.toString());
            update.setString(3, category.name());
            if (update.executeUpdate() > 0) {
                return;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update sell progress", e);
            return;
        }

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO sell_progress (player_uuid, category, earned) VALUES (?,?,?)")) {
            insert.setString(1, uuid.toString());
            insert.setString(2, category.name());
            insert.setDouble(3, amount);
            insert.executeUpdate();
        } catch (SQLException e) {
            try (PreparedStatement retryUpdate = connection.prepareStatement(
                    "UPDATE sell_progress SET earned = earned + ? WHERE player_uuid = ? AND category = ?")) {
                retryUpdate.setDouble(1, amount);
                retryUpdate.setString(2, uuid.toString());
                retryUpdate.setString(3, category.name());
                retryUpdate.executeUpdate();
            } catch (SQLException retryException) {
                plugin.getLogger().log(Level.WARNING, "Failed to add sell progress", retryException);
            }
        }
    }

    public Map<SellCategory, Double> getSellProgress(UUID uuid) {
        Map<SellCategory, Double> progress = new EnumMap<>(SellCategory.class);

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT category, earned FROM sell_progress WHERE player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SellCategory category = SellCategory.fromConfigKey(rs.getString("category")).orElse(null);
                    if (category != null) {
                        progress.put(category, rs.getDouble("earned"));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load sell progress", e);
        }

        return progress;
    }

    public int countSellHistory(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM sell_history WHERE player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to count sell history", e);
        }
        return 0;
    }

    public List<SellHistoryEntry> getSellHistoryEntries(UUID uuid, int limit, int offset, boolean sortByPrice) {
        List<SellHistoryEntry> list = new ArrayList<>();
        String orderBy = sortByPrice ? "price DESC, timestamp DESC, id DESC" : "timestamp DESC, id DESC";

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT item_name, amount, price, timestamp FROM sell_history " +
                "WHERE player_uuid = ? ORDER BY " + orderBy + " LIMIT ? OFFSET ?")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SellHistoryEntry(
                            rs.getString("item_name"),
                            rs.getInt("amount"),
                            rs.getDouble("price"),
                            rs.getLong("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get sell history entries", e);
        }

        return list;
    }

    public List<String[]> getSellHistory(UUID uuid, int limit) {
        List<String[]> list = new ArrayList<>();
        for (SellHistoryEntry entry : getSellHistoryEntries(uuid, limit, 0, false)) {
            list.add(new String[]{
                    entry.itemName(),
                    String.valueOf(entry.amount()),
                    String.valueOf(entry.price()),
                    String.valueOf(entry.timestamp())
            });
        }
        return list;
    }

    public int countSellDocuments() {
        return countRows("sell_history") + countRows("sell_progress");
    }

    public int clearSellHistoryAndProgress() {
        int historyDeleted = executeUpdate("DELETE FROM sell_history");
        int progressDeleted = executeUpdate("DELETE FROM sell_progress");
        return historyDeleted + progressDeleted;
    }

    public long createPunishmentRecord(PunishmentRecord record) {
        if (record == null || record.getTargetUuid() == null) {
            return -1L;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO punishments (" +
                        "target_uuid, target_name_snapshot, type, reason, issuer_uuid, issuer_name_snapshot, " +
                        "issued_at, expires_at, removed_by_uuid, removed_by_name_snapshot, removed_at, " +
                        "removal_reason, source_server, scope" +
                        ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, record.getTargetUuid().toString());
            ps.setString(2, record.getTargetNameSnapshot());
            ps.setString(3, record.getType().name());
            ps.setString(4, record.getReason());
            ps.setString(5, record.getIssuerUuid() == null ? null : record.getIssuerUuid().toString());
            ps.setString(6, record.getIssuerNameSnapshot());
            ps.setLong(7, record.getIssuedAt());
            if (record.getExpiresAt() == null) {
                ps.setNull(8, Types.BIGINT);
            } else {
                ps.setLong(8, record.getExpiresAt());
            }
            ps.setString(9, record.getRemovedByUuid() == null ? null : record.getRemovedByUuid().toString());
            ps.setString(10, record.getRemovedByNameSnapshot());
            if (record.getRemovedAt() == null) {
                ps.setNull(11, Types.BIGINT);
            } else {
                ps.setLong(11, record.getRemovedAt());
            }
            ps.setString(12, record.getRemovalReason());
            ps.setString(13, record.getSourceServer());
            ps.setString(14, record.getScope().name());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create punishment record for " + record.getTargetUuid(), e);
        }

        return -1L;
    }

    public PunishmentRecord loadPunishmentRecord(long punishmentId) {
        if (punishmentId <= 0L) {
            return null;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM punishments WHERE id = ? LIMIT 1")) {
            ps.setLong(1, punishmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPunishmentRow(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load punishment record #" + punishmentId, e);
        }

        return null;
    }

    public boolean markPunishmentRemoved(long punishmentId,
                                         UUID removedByUuid,
                                         String removedByNameSnapshot,
                                         long removedAt,
                                         String removalReason) {
        if (punishmentId <= 0L) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE punishments SET removed_by_uuid = ?, removed_by_name_snapshot = ?, removed_at = ?, removal_reason = ? " +
                        "WHERE id = ?")) {
            ps.setString(1, removedByUuid == null ? null : removedByUuid.toString());
            ps.setString(2, removedByNameSnapshot);
            ps.setLong(3, removedAt);
            ps.setString(4, removalReason);
            ps.setLong(5, punishmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to mark punishment #" + punishmentId + " as removed", e);
        }

        return false;
    }

    public boolean deletePunishmentRecord(long punishmentId) {
        if (punishmentId <= 0L) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM punishments WHERE id = ?")) {
            ps.setLong(1, punishmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete punishment #" + punishmentId, e);
        }

        return false;
    }

    public List<FreezeState> loadActiveFreezeStates() {
        List<FreezeState> states = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM freeze_states WHERE active = 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    states.add(mapFreezeRow(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load active freeze states", e);
        }
        return states;
    }

    public void saveFreezeState(FreezeState state) {
        if (state == null || state.getTargetUuid() == null) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO freeze_states " +
                        "(target_uuid, target_name_snapshot, frozen_by_uuid, frozen_by_name_snapshot, frozen_at, source_server, active) " +
                        "VALUES (?,?,?,?,?,?,1)")) {
            ps.setString(1, state.getTargetUuid().toString());
            ps.setString(2, state.getTargetNameSnapshot());
            ps.setString(3, state.getFrozenByUuid() == null ? null : state.getFrozenByUuid().toString());
            ps.setString(4, state.getFrozenByNameSnapshot());
            ps.setLong(5, state.getFrozenAt());
            ps.setString(6, state.getSourceServer());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save freeze state for " + state.getTargetUuid(), e);
        }
    }

    public boolean deleteFreezeState(UUID targetUuid) {
        if (targetUuid == null) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM freeze_states WHERE target_uuid = ?")) {
            ps.setString(1, targetUuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete freeze state for " + targetUuid, e);
        }
        return false;
    }

    public List<StaffModeState> loadActiveStaffModeStates() {
        List<StaffModeState> states = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM staff_mode_states")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    states.add(mapStaffModeStateRow(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load active staff mode states", e);
        }
        return states;
    }

    public void saveStaffModeState(StaffModeState state) {
        if (state == null || state.getStaffUuid() == null) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO staff_mode_states " +
                        "(staff_uuid, staff_name_snapshot, enabled_at, source_server, vanish_active, better_view_active, snapshot_present, previous_allow_flight, previous_flying, previous_selected_slot, night_vision_owned, previous_game_mode) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, state.getStaffUuid().toString());
            ps.setString(2, state.getStaffNameSnapshot());
            ps.setLong(3, state.getEnabledAt());
            ps.setString(4, state.getSourceServer());
            ps.setInt(5, state.isVanishActive() ? 1 : 0);
            ps.setInt(6, state.isBetterViewActive() ? 1 : 0);
            ps.setInt(7, state.isSnapshotPresent() ? 1 : 0);
            ps.setInt(8, state.isPreviousAllowFlight() ? 1 : 0);
            ps.setInt(9, state.isPreviousFlying() ? 1 : 0);
            ps.setInt(10, state.getPreviousSelectedSlot());
            ps.setInt(11, state.isNightVisionOwned() ? 1 : 0);
            ps.setString(12, state.getPreviousGameMode().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save staff mode state for " + state.getStaffUuid(), e);
        }
    }

    public StaffInventorySnapshot loadStaffModeSnapshot(UUID staffUuid) {
        if (staffUuid == null) {
            return null;
        }

        ItemStack[] storageContents = new ItemStack[36];
        ItemStack[] armorContents = new ItemStack[4];
        ItemStack offhand = null;

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT section, slot, item_data FROM staff_mode_snapshot_items WHERE staff_uuid = ? ORDER BY section ASC, slot ASC")) {
            ps.setString(1, staffUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String section = rs.getString("section");
                    int slot = rs.getInt("slot");
                    ItemStack item = deserializeItemStack(rs.getString("item_data"));
                    if ("STORAGE".equalsIgnoreCase(section)) {
                        if (slot >= 0 && slot < storageContents.length) {
                            storageContents[slot] = item;
                        }
                        continue;
                    }
                    if ("ARMOR".equalsIgnoreCase(section)) {
                        if (slot >= 0 && slot < armorContents.length) {
                            armorContents[slot] = item;
                        }
                        continue;
                    }
                    if ("OFFHAND".equalsIgnoreCase(section) && slot == 0) {
                        offhand = item;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load staff mode snapshot for " + staffUuid, e);
            return null;
        }

        return new StaffInventorySnapshot(storageContents, armorContents, offhand);
    }

    public boolean saveStaffModeSnapshot(UUID staffUuid, StaffInventorySnapshot snapshot) {
        if (staffUuid == null || snapshot == null || connection == null) {
            return false;
        }

        boolean originalAutoCommit;
        try {
            originalAutoCommit = connection.getAutoCommit();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to inspect auto-commit state before Staff Mode snapshot save", e);
            return false;
        }

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteStatement = connection.prepareStatement(
                    "DELETE FROM staff_mode_snapshot_items WHERE staff_uuid = ?")) {
                deleteStatement.setString(1, staffUuid.toString());
                deleteStatement.executeUpdate();
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO staff_mode_snapshot_items (staff_uuid, section, slot, item_data) VALUES (?,?,?,?)")) {
                ItemStack[] storageContents = snapshot.getStorageContents();
                for (int slot = 0; slot < storageContents.length; slot++) {
                    insertSnapshotItem(insertStatement, staffUuid, "STORAGE", slot, storageContents[slot]);
                }

                ItemStack[] armorContents = snapshot.getArmorContents();
                for (int slot = 0; slot < armorContents.length; slot++) {
                    insertSnapshotItem(insertStatement, staffUuid, "ARMOR", slot, armorContents[slot]);
                }

                insertSnapshotItem(insertStatement, staffUuid, "OFFHAND", 0, snapshot.getOffhandItem());
                insertStatement.executeBatch();
            }

            connection.commit();
            return true;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                plugin.getLogger().log(Level.WARNING, "Failed to roll back Staff Mode snapshot save for " + staffUuid, rollbackException);
            }
            plugin.getLogger().log(Level.WARNING, "Failed to save Staff Mode snapshot for " + staffUuid, e);
            return false;
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to restore auto-commit after Staff Mode snapshot save", e);
            }
        }
    }

    public boolean deleteStaffModeSnapshot(UUID staffUuid) {
        if (staffUuid == null) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM staff_mode_snapshot_items WHERE staff_uuid = ?")) {
            ps.setString(1, staffUuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete staff mode snapshot for " + staffUuid, e);
        }
        return false;
    }

    public boolean deleteStaffModeState(UUID staffUuid) {
        if (staffUuid == null) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM staff_mode_states WHERE staff_uuid = ?")) {
            ps.setString(1, staffUuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete staff mode state for " + staffUuid, e);
        }
        return false;
    }

    public int countPunishmentHistory(UUID targetUuid, PunishmentQuery query, long now) {
        if (targetUuid == null) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM punishments");
        List<Object> parameters = new ArrayList<>();
        appendPunishmentFilters(sql, parameters, targetUuid, query, now);

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bindParameters(ps, parameters);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to count punishment history for " + targetUuid, e);
        }

        return 0;
    }

    public List<PunishmentRecord> loadPunishmentHistory(UUID targetUuid, PunishmentQuery query, int limit, int offset, long now) {
        List<PunishmentRecord> records = new ArrayList<>();
        if (targetUuid == null) {
            return records;
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM punishments");
        List<Object> parameters = new ArrayList<>();
        appendPunishmentFilters(sql, parameters, targetUuid, query, now);

        PunishmentSortOrder sortOrder = query == null ? PunishmentSortOrder.NEWEST : query.sortOrder();
        sql.append(sortOrder == PunishmentSortOrder.OLDEST
                ? " ORDER BY issued_at ASC, id ASC"
                : " ORDER BY issued_at DESC, id DESC");
        sql.append(" LIMIT ? OFFSET ?");
        parameters.add(Math.max(1, limit));
        parameters.add(Math.max(0, offset));

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bindParameters(ps, parameters);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapPunishmentRow(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load punishment history for " + targetUuid, e);
        }

        return records;
    }

    // â”€â”€ Cuboids â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public record CuboidData(String world, int x1, int y1, int z1, int x2, int y2, int z2) {}

    public Map<String, CuboidData> loadCuboids() {
        Map<String, CuboidData> cuboids = new LinkedHashMap<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM cuboids")) {
            while (rs.next()) {
                cuboids.put(rs.getString("name"), new CuboidData(
                        rs.getString("world"),
                        rs.getInt("x1"),
                        rs.getInt("y1"),
                        rs.getInt("z1"),
                        rs.getInt("x2"),
                        rs.getInt("y2"),
                        rs.getInt("z2")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load cuboids", e);
        }
        return cuboids;
    }

    public void saveCuboid(String name, String world, int x1, int y1, int z1, int x2, int y2, int z2) {
        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO cuboids (name, world, x1, y1, z1, x2, y2, z2) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, name);
            ps.setString(2, world);
            ps.setInt(3, x1); ps.setInt(4, y1); ps.setInt(5, z1);
            ps.setInt(6, x2); ps.setInt(7, y2); ps.setInt(8, z2);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save cuboid " + name, e);
        }
    }

    public void deleteCuboid(String name) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM cuboids WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete cuboid " + name, e);
        }
    }

    // â”€â”€ Misc â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public List<SpawnerInstance> loadAllSpawners() {
        List<SpawnerInstance> spawners = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM spawners ORDER BY id ASC")) {
            while (rs.next()) {
                spawners.add(new SpawnerInstance(
                        rs.getLong("id"),
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        UUID.fromString(rs.getString("owner_uuid")),
                        rs.getString("owner_name"),
                        rs.getString("mob_type"),
                        rs.getLong("stack_amount"),
                        SpawnerInstance.AccessMode.fromString(rs.getString("access_mode"), SpawnerInstance.AccessMode.OWNER_ONLY),
                        rs.getLong("last_processed_at"),
                        rs.getLong("created_at"),
                        rs.getLong("updated_at")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load managed spawners", e);
        }
        return spawners;
    }

    public Map<Long, List<SpawnerLootEntry>> loadAllSpawnerLoot() {
        Map<Long, List<SpawnerLootEntry>> lootBySpawnerId = new HashMap<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM spawner_loot ORDER BY spawner_id ASC, loot_key ASC")) {
            while (rs.next()) {
                long spawnerId = rs.getLong("spawner_id");
                lootBySpawnerId.computeIfAbsent(spawnerId, ignored -> new ArrayList<>()).add(
                        new SpawnerLootEntry(
                                rs.getString("loot_key"),
                                Material.matchMaterial(rs.getString("material")),
                                rs.getLong("amount")
                        )
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load managed spawner loot", e);
        }
        return lootBySpawnerId;
    }

    public long createSpawner(SpawnerInstance instance) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO spawners (world, x, y, z, owner_uuid, owner_name, mob_type, stack_amount, access_mode, last_processed_at, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, instance.getWorld());
            ps.setInt(2, instance.getX());
            ps.setInt(3, instance.getY());
            ps.setInt(4, instance.getZ());
            ps.setString(5, instance.getOwnerUuid().toString());
            ps.setString(6, instance.getOwnerNameSnapshot());
            ps.setString(7, instance.getMobTypeKey());
            ps.setLong(8, instance.getStackAmount());
            ps.setString(9, instance.getAccessMode().name());
            ps.setLong(10, instance.getLastProcessedAt());
            ps.setLong(11, instance.getCreatedAt());
            ps.setLong(12, instance.getUpdatedAt());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create managed spawner", e);
        }
        return -1L;
    }

    public void saveSpawner(SpawnerInstance instance) {
        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO spawners " +
                        "(id, world, x, y, z, owner_uuid, owner_name, mob_type, stack_amount, access_mode, last_processed_at, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            ps.setLong(1, instance.getId());
            ps.setString(2, instance.getWorld());
            ps.setInt(3, instance.getX());
            ps.setInt(4, instance.getY());
            ps.setInt(5, instance.getZ());
            ps.setString(6, instance.getOwnerUuid().toString());
            ps.setString(7, instance.getOwnerNameSnapshot());
            ps.setString(8, instance.getMobTypeKey());
            ps.setLong(9, instance.getStackAmount());
            ps.setString(10, instance.getAccessMode().name());
            ps.setLong(11, instance.getLastProcessedAt());
            ps.setLong(12, instance.getCreatedAt());
            ps.setLong(13, instance.getUpdatedAt());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save managed spawner " + instance.getId(), e);
        }
    }

    public void replaceSpawnerLoot(long spawnerId, Collection<SpawnerLootEntry> lootEntries) {
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM spawner_loot WHERE spawner_id = ?")) {
                delete.setLong(1, spawnerId);
                delete.executeUpdate();
            }

            if (lootEntries != null && !lootEntries.isEmpty()) {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO spawner_loot (spawner_id, loot_key, material, amount) VALUES (?,?,?,?)")) {
                    for (SpawnerLootEntry entry : lootEntries) {
                        if (entry == null || entry.getAmount() <= 0L) {
                            continue;
                        }

                        insert.setLong(1, spawnerId);
                        insert.setString(2, entry.getKey());
                        insert.setString(3, entry.getMaterial().name());
                        insert.setLong(4, entry.getAmount());
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                plugin.getLogger().log(Level.WARNING, "Failed to roll back spawner loot transaction", rollbackException);
            }
            plugin.getLogger().log(Level.WARNING, "Failed to replace managed spawner loot for spawner " + spawnerId, e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public void deleteSpawner(long spawnerId) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM spawners WHERE id = ?")) {
            ps.setLong(1, spawnerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete managed spawner " + spawnerId, e);
        }

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM spawner_loot WHERE spawner_id = ?")) {
            ps.setLong(1, spawnerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete managed spawner loot " + spawnerId, e);
        }
    }

    private void execute(String sql) throws SQLException {
        if (handleCreateIndexIfNeeded(sql)) {
            return;
        }
        try (Statement st = connection.createStatement()) {
            st.execute(adaptSchemaSql(sql));
        }
    }

    public void executeSchema(Statement statement, String sql) throws SQLException {
        if (handleCreateIndexIfNeeded(sql)) {
            return;
        }
        statement.execute(adaptSchemaSql(sql));
    }

    public String adaptSchemaSql(String sql) {
        if (!isMySql() || sql == null || sql.isBlank()) {
            return sql;
        }

        String adapted = sql.replace("INTEGER PRIMARY KEY AUTOINCREMENT", "BIGINT PRIMARY KEY AUTO_INCREMENT");
        adapted = adapted.replaceAll("(?i)\\bINTEGER\\b", "BIGINT");
        adapted = adapted.replaceAll("(?i)\\bREAL\\b", "DOUBLE");
        adapted = adapted.replaceAll("(?i)\\b([a-z0-9_]*uuid|[a-z0-9_]*name|ip_address|id|world|section|category|crate_id|loot_key)\\s+TEXT", "$1 VARCHAR(191)");
        adapted = adapted.replaceAll("(?i)\\b(source_server|scope|previous_game_mode|type|status|claim_type|match_type|arena_id|mob_type|access_mode|material)\\s+TEXT", "$1 VARCHAR(191)");
        return adapted;
    }

    private boolean handleCreateIndexIfNeeded(String sql) throws SQLException {
        if (sql == null || sql.isBlank()) {
            return false;
        }

        Matcher matcher = CREATE_INDEX_PATTERN.matcher(sql.trim());
        if (!matcher.matches()) {
            return false;
        }

        createIndexIfMissing(matcher.group(1), matcher.group(2), matcher.group(3));
        return true;
    }

    public void createIndexIfMissing(String indexName, String table, String columns) throws SQLException {
        if (indexName == null || indexName.isBlank() || table == null || table.isBlank() || columns == null || columns.isBlank()) {
            return;
        }

        if (isMySql()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getIndexInfo(connection.getCatalog(), null, table, false, false)) {
                while (rs.next()) {
                    String existingName = rs.getString("INDEX_NAME");
                    if (indexName.equalsIgnoreCase(existingName)) {
                        return;
                    }
                }
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE INDEX " + indexName + " ON " + table + " (" + columns + ")");
            }
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE INDEX IF NOT EXISTS " + indexName + " ON " + table + " (" + columns + ")");
        }
    }

    private PunishmentRecord mapPunishmentRow(ResultSet rs) throws SQLException {
        String targetUuidRaw = rs.getString("target_uuid");
        String issuerUuidRaw = rs.getString("issuer_uuid");
        String removedByUuidRaw = rs.getString("removed_by_uuid");
        long expiresAtValue = rs.getLong("expires_at");
        boolean expiresAtWasNull = rs.wasNull();
        long removedAtValue = rs.getLong("removed_at");
        boolean removedAtWasNull = rs.wasNull();

        return new PunishmentRecord(
                rs.getLong("id"),
                targetUuidRaw == null || targetUuidRaw.isBlank() ? null : UUID.fromString(targetUuidRaw),
                rs.getString("target_name_snapshot"),
                PunishmentType.fromString(rs.getString("type"), PunishmentType.WARN),
                rs.getString("reason"),
                issuerUuidRaw == null || issuerUuidRaw.isBlank() ? null : UUID.fromString(issuerUuidRaw),
                rs.getString("issuer_name_snapshot"),
                rs.getLong("issued_at"),
                expiresAtWasNull ? null : expiresAtValue,
                removedByUuidRaw == null || removedByUuidRaw.isBlank() ? null : UUID.fromString(removedByUuidRaw),
                rs.getString("removed_by_name_snapshot"),
                removedAtWasNull ? null : removedAtValue,
                rs.getString("removal_reason"),
                rs.getString("source_server"),
                PunishmentScope.fromString(rs.getString("scope"), PunishmentScope.SERVER)
        );
    }

    private FreezeState mapFreezeRow(ResultSet rs) throws SQLException {
        String targetUuidRaw = rs.getString("target_uuid");
        String frozenByUuidRaw = rs.getString("frozen_by_uuid");
        return new FreezeState(
                targetUuidRaw == null || targetUuidRaw.isBlank() ? null : UUID.fromString(targetUuidRaw),
                rs.getString("target_name_snapshot"),
                frozenByUuidRaw == null || frozenByUuidRaw.isBlank() ? null : UUID.fromString(frozenByUuidRaw),
                rs.getString("frozen_by_name_snapshot"),
                rs.getLong("frozen_at"),
                rs.getString("source_server")
        );
    }

    private StaffModeState mapStaffModeStateRow(ResultSet rs) throws SQLException {
        String staffUuidRaw = rs.getString("staff_uuid");
        return new StaffModeState(
                staffUuidRaw == null || staffUuidRaw.isBlank() ? null : UUID.fromString(staffUuidRaw),
                rs.getString("staff_name_snapshot"),
                rs.getLong("enabled_at"),
                rs.getString("source_server"),
                rs.getInt("vanish_active") != 0,
                rs.getInt("better_view_active") != 0,
                rs.getInt("snapshot_present") != 0,
                rs.getInt("previous_allow_flight") != 0,
                rs.getInt("previous_flying") != 0,
                rs.getInt("previous_selected_slot"),
                rs.getInt("night_vision_owned") != 0,
                parseGameMode(rs.getString("previous_game_mode"))
        );
    }

    private void insertSnapshotItem(PreparedStatement insertStatement,
                                    UUID staffUuid,
                                    String section,
                                    int slot,
                                    ItemStack item) throws SQLException {
        if (item == null || item.getType().isAir()) {
            return;
        }

        insertStatement.setString(1, staffUuid.toString());
        insertStatement.setString(2, section);
        insertStatement.setInt(3, slot);
        insertStatement.setString(4, serializeItemStack(item));
        insertStatement.addBatch();
    }

    private GameMode parseGameMode(String raw) {
        if (raw == null || raw.isBlank()) {
            return GameMode.SURVIVAL;
        }

        try {
            return GameMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return GameMode.SURVIVAL;
        }
    }

    private void appendPunishmentFilters(StringBuilder sql,
                                         List<Object> parameters,
                                         UUID targetUuid,
                                         PunishmentQuery query,
                                         long now) {
        sql.append(" WHERE target_uuid = ?");
        parameters.add(targetUuid.toString());

        if (query == null) {
            return;
        }

        if (query.typeFilter() != null) {
            sql.append(" AND type = ?");
            parameters.add(query.typeFilter().name());
        }

        if (query.stateFilter() == PunishmentFilterState.ACTIVE) {
            sql.append(" AND removed_at IS NULL AND (expires_at IS NULL OR expires_at > ?)");
            parameters.add(now);
        } else if (query.stateFilter() == PunishmentFilterState.INACTIVE) {
            sql.append(" AND (removed_at IS NOT NULL OR (expires_at IS NOT NULL AND expires_at <= ?))");
            parameters.add(now);
        }
    }

    private void bindParameters(PreparedStatement ps, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object value = parameters.get(index);
            int parameterIndex = index + 1;
            if (value == null) {
                ps.setObject(parameterIndex, null);
            } else if (value instanceof String stringValue) {
                ps.setString(parameterIndex, stringValue);
            } else if (value instanceof Integer integerValue) {
                ps.setInt(parameterIndex, integerValue);
            } else if (value instanceof Long longValue) {
                ps.setLong(parameterIndex, longValue);
            } else if (value instanceof UUID uuidValue) {
                ps.setString(parameterIndex, uuidValue.toString());
            } else {
                ps.setObject(parameterIndex, value);
            }
        }
    }

    private int countRows(String table) {
        return countQuery("SELECT COUNT(*) FROM " + table);
    }

    private int countQuery(String sql) {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to execute count query: " + sql, e);
        }
        return 0;
    }

    private int executeUpdate(String sql) {
        try (Statement st = connection.createStatement()) {
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to execute update: " + sql, e);
        }
        return 0;
    }

    private String serializeItemStack(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    private ItemStack deserializeItemStack(String encodedData) {
        if (encodedData == null || encodedData.isBlank()) {
            return null;
        }

        try {
            byte[] itemBytes = Base64.getDecoder().decode(encodedData);
            return ItemStack.deserializeBytes(itemBytes);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to deserialize Ender Chest item data", e);
            return null;
        }
    }

    private void importMongoSnapshotIntoSqlite() {
        Set<String> collectionNames = new LinkedHashSet<>();
        try (MongoCursor<String> cursor = mongoDatabase.listCollectionNames().iterator()) {
            while (cursor.hasNext()) {
                String collectionName = cursor.next();
                if (isSafeIdentifier(collectionName) && !MONGO_SCHEMA_COLLECTION.equals(collectionName)) {
                    collectionNames.add(collectionName);
                }
            }
        }

        if (collectionNames.isEmpty()) {
            return;
        }

        try {
            createTablesFromMongoSchema();
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                for (String table : collectionNames) {
                    importMongoCollection(table);
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
            plugin.getLogger().info("Imported MongoDB snapshot into SQL runtime cache.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to import MongoDB snapshot; keeping local SQL cache", e);
        }
    }

    private void createTablesFromMongoSchema() throws SQLException {
        MongoCollection<Document> schemaCollection = mongoDatabase.getCollection(MONGO_SCHEMA_COLLECTION);
        try (MongoCursor<Document> cursor = schemaCollection.find().iterator();
             Statement statement = connection.createStatement()) {
            while (cursor.hasNext()) {
                Document schema = cursor.next();
                String table = schema.getString("_id");
                String createSql = schema.getString("create_sql");
                if (!isSafeIdentifier(table) || createSql == null || createSql.isBlank() || tableExists(table)) {
                    continue;
                }
                statement.execute(ensureCreateTableIfNotExists(createSql));
            }
        }
    }

    private void importMongoCollection(String table) throws SQLException {
        if (!tableExists(table)) {
            return;
        }

        List<String> columns = getSqliteTableColumns(table);
        if (columns.isEmpty()) {
            return;
        }

        try (Statement delete = connection.createStatement()) {
            delete.executeUpdate("DELETE FROM " + table);
        }

        MongoCollection<Document> collection = mongoDatabase.getCollection(table);
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                List<String> presentColumns = columns.stream()
                        .filter(document::containsKey)
                        .toList();
                if (presentColumns.isEmpty()) {
                    continue;
                }

                String placeholders = String.join(",", Collections.nCopies(presentColumns.size(), "?"));
                String sql = "INSERT INTO " + table + " (" + String.join(",", presentColumns) + ") VALUES (" + placeholders + ")";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    for (int index = 0; index < presentColumns.size(); index++) {
                        Object value = document.get(presentColumns.get(index));
                        if (value instanceof Boolean booleanValue) {
                            ps.setInt(index + 1, booleanValue ? 1 : 0);
                        } else {
                            ps.setObject(index + 1, value);
                        }
                    }
                    ps.executeUpdate();
                }
            }
        }
    }

    private void exportSqliteSnapshotToMongo() throws SQLException {
        MongoCollection<Document> schemaCollection = mongoDatabase.getCollection(MONGO_SCHEMA_COLLECTION);
        for (String table : listSqliteTables()) {
            String createSql = getCreateTableSql(table);
            if (createSql != null && !createSql.isBlank()) {
                Document schema = new Document("_id", table)
                        .append("create_sql", createSql)
                        .append("updated_at", System.currentTimeMillis());
                schemaCollection.replaceOne(new Document("_id", table), schema, new ReplaceOptions().upsert(true));
            }

            MongoCollection<Document> collection = mongoDatabase.getCollection(table);
            collection.deleteMany(new Document());
            List<Document> documents = readTableDocuments(table);
            if (!documents.isEmpty()) {
                collection.insertMany(documents);
            }
        }
    }

    private List<Document> readTableDocuments(String table) throws SQLException {
        List<Document> documents = new ArrayList<>();
        List<String> primaryKeyColumns = getPrimaryKeyColumns(table);

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM " + table)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            int rowIndex = 0;
            while (rs.next()) {
                Document document = new Document();
                for (int index = 1; index <= columnCount; index++) {
                    String columnName = metaData.getColumnName(index);
                    document.append(columnName, rs.getObject(index));
                }
                document.append("_id", buildMongoDocumentId(table, document, primaryKeyColumns, rowIndex++));
                documents.add(document);
            }
        }
        return documents;
    }

    private String buildMongoDocumentId(String table, Document document, List<String> primaryKeyColumns, int rowIndex) {
        if (primaryKeyColumns == null || primaryKeyColumns.isEmpty()) {
            return table + ":" + rowIndex;
        }

        StringBuilder id = new StringBuilder(table);
        for (String primaryKeyColumn : primaryKeyColumns) {
            id.append(':').append(String.valueOf(document.get(primaryKeyColumn)));
        }
        return id.toString();
    }

    private List<String> listSqliteTables() throws SQLException {
        List<String> tables = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name ASC")) {
            while (rs.next()) {
                String table = rs.getString("name");
                if (isSafeIdentifier(table)) {
                    tables.add(table);
                }
            }
        }
        return tables;
    }

    private String getCreateTableSql(String table) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT sql FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("sql");
                }
            }
        }
        return null;
    }

    private boolean tableExists(String table) throws SQLException {
        if (!isSafeIdentifier(table)) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<String> getSqliteTableColumns(String table) throws SQLException {
        List<String> columns = new ArrayList<>();
        if (!isSafeIdentifier(table)) {
            return columns;
        }

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                String column = rs.getString("name");
                if (isSafeIdentifier(column)) {
                    columns.add(column);
                }
            }
        }
        return columns;
    }

    private List<String> getPrimaryKeyColumns(String table) throws SQLException {
        Map<Integer, String> orderedColumns = new TreeMap<>();
        if (!isSafeIdentifier(table)) {
            return List.of();
        }

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                int order = rs.getInt("pk");
                if (order > 0) {
                    orderedColumns.put(order, rs.getString("name"));
                }
            }
        }
        return List.copyOf(orderedColumns.values());
    }

    private String ensureCreateTableIfNotExists(String createSql) {
        String trimmed = createSql.trim();
        if (trimmed.toUpperCase(Locale.ROOT).startsWith("CREATE TABLE IF NOT EXISTS")) {
            return trimmed;
        }
        return trimmed.replaceFirst("(?i)^CREATE\\s+TABLE\\s+", "CREATE TABLE IF NOT EXISTS ");
    }

    private boolean isSafeIdentifier(String identifier) {
        return identifier != null && identifier.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    public void close() {
        flush();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to close database", e);
        }
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to close MongoDB client", e);
            }
        }
    }

    public void flush() {
        if (!mongoBridgeActive || mongoDatabase == null || connection == null) {
            return;
        }

        try {
            exportSqliteSnapshotToMongo();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to sync SQL cache to MongoDB", e);
        }
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public boolean isMySql() {
        return databaseType == DatabaseType.MYSQL;
    }

    public boolean isMongoDb() {
        return databaseType == DatabaseType.MONGODB;
    }

    public Connection getConnection() { return connection; }
}

