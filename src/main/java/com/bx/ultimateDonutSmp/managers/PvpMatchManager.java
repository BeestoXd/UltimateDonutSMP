package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PvpKit;
import com.bx.ultimateDonutSmp.models.PvpMatch;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * The ranked 1v1 side of the arena: the queue, the matches it produces, and their history.
 *
 * <p>This sits beside the open arena rather than inside it. Both use the same Elo, ranks and kits
 * from {@link PvpManager}, but a queued match is a fight between two named players with a result,
 * which is what makes a history entry worth keeping. Kills in the open arena are not matches and
 * never appear here.</p>
 */
public class PvpMatchManager {

    private final UltimateDonutSmp plugin;

    private final Map<UUID, QueueEntry> queue = new LinkedHashMap<>();
    private final Map<UUID, PvpMatch> active = new ConcurrentHashMap<>();
    private final AtomicLong localIds = new AtomicLong();

    public PvpMatchManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        ensureTables();
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getPvp();
    }

    private PvpManager pvp() {
        return plugin.getPvpManager();
    }

    public boolean isEnabled() {
        return pvp() != null && pvp().isEnabled() && config().getBoolean("MATCH.ENABLED", true);
    }

    public void reload() {
        // Nothing cached from the config, but a reload has to clear a queue that may now point at
        // kits or an arena that no longer exist.
        clearQueue();
    }

    public void shutdown() {
        for (PvpMatch match : new ArrayList<>(active.values())) {
            endMatch(match, null, PvpMatch.Result.ABORTED, true);
        }
        queue.clear();
    }

    // ── Queue ─────────────────────────────────────────────────────────────────

    public boolean isQueued(UUID uuid) {
        synchronized (queue) {
            return queue.containsKey(uuid);
        }
    }

    public int getQueueSize() {
        synchronized (queue) {
            return queue.size();
        }
    }

    public String getQueuedKit(UUID uuid) {
        synchronized (queue) {
            QueueEntry entry = queue.get(uuid);
            return entry == null ? null : entry.kitId();
        }
    }

    /** Seconds a player has been waiting, or -1 when they are not queued. */
    public long getQueuedSeconds(UUID uuid) {
        synchronized (queue) {
            QueueEntry entry = queue.get(uuid);
            return entry == null ? -1L : (System.currentTimeMillis() - entry.queuedAt()) / 1000L;
        }
    }

    /**
     * Puts a player in the ranked queue with the kit they picked, and starts a match as soon as
     * somebody else is waiting.
     *
     * @return false when the queue refused them, with the reason already sent
     */
    public boolean joinQueue(Player player, PvpKit kit) {
        if (player == null || kit == null) {
            return false;
        }
        if (!isEnabled()) {
            send(player, message("DISABLED", "&cThe PvP arena is not enabled."));
            return false;
        }
        if (!pvp().isConfigured()) {
            send(player, message("NOT_SET_UP", "&cThe PvP arena has not been set up yet."));
            return false;
        }
        if (isInMatch(player.getUniqueId())) {
            send(player, message("MATCH_ALREADY_IN", "&cYou are already in a ranked match."));
            return false;
        }
        if (pvp().isInArena(player.getUniqueId())) {
            send(player, message("MATCH_LEAVE_ARENA_FIRST", "&cLeave the arena before queueing."));
            return false;
        }
        if (!pvp().canUseKit(player, kit)) {
            send(player, message("KIT_NO_PERMISSION", "&cYou do not have access to that kit."));
            return false;
        }

        UUID opponent;
        synchronized (queue) {
            if (queue.containsKey(player.getUniqueId())) {
                send(player, message("QUEUE_ALREADY_IN", "&cYou are already in the queue."));
                return false;
            }
            queue.put(player.getUniqueId(), new QueueEntry(kit.getId(), System.currentTimeMillis()));
            opponent = findOpponent(player.getUniqueId(), kit.getId());
        }

        send(player, message("QUEUE_JOINED", "&aYou joined the ranked queue. Waiting for an opponent...")
                .replace("{kit}", kit.getId()));

        if (opponent == null) {
            return true;
        }

        Player other = Bukkit.getPlayer(opponent);
        if (other == null) {
            leaveQueue(opponent);
            return true;
        }

        String kitId = kit.getId();
        synchronized (queue) {
            queue.remove(player.getUniqueId());
            queue.remove(opponent);
        }
        startMatch(other, player, kitId, null);
        return true;
    }

    /**
     * The player who has waited longest for the same kit.
     *
     * <p>Only the same kit counts. Both fighters use one loadout, so pairing across kits would hand
     * whoever waited the gear the other one picked, which is the opposite of what choosing a kit in
     * the queue menu is for.</p>
     *
     * <p>Within a kit, pairing is by wait time rather than by rating. The queue on a survival server
     * is rarely more than a handful of people, and holding a high rated player back to look for a
     * closer match would mostly mean nobody gets a fight at all.</p>
     */
    private UUID findOpponent(UUID self, String kitId) {
        for (Map.Entry<UUID, QueueEntry> entry : queue.entrySet()) {
            if (entry.getKey().equals(self) || !entry.getValue().kitId().equals(kitId)) {
                continue;
            }
            if (Bukkit.getPlayer(entry.getKey()) != null) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean leaveQueue(UUID uuid) {
        synchronized (queue) {
            return queue.remove(uuid) != null;
        }
    }

    private void clearQueue() {
        synchronized (queue) {
            queue.clear();
        }
    }

    // ── Matches ───────────────────────────────────────────────────────────────

    public boolean isInMatch(UUID uuid) {
        return uuid != null && active.containsKey(uuid);
    }

    public PvpMatch getMatch(UUID uuid) {
        return uuid == null ? null : active.get(uuid);
    }

    public int getActiveMatchCount() {
        return active.size() / 2;
    }

    /**
     * Starts a ranked match between two players.
     *
     * @param initiator the tester who assigned it, or null when the queue paired them
     * @return false when either player could not be put in, with the reason already sent
     */
    public boolean startMatch(Player first, Player second, String kitId, org.bukkit.command.CommandSender initiator) {
        if (first == null || second == null || first.getUniqueId().equals(second.getUniqueId())) {
            send(initiator, message("MATCH_NEEDS_TWO", "&cA ranked match needs two different players."));
            return false;
        }
        if (!isEnabled() || !pvp().isConfigured()) {
            send(initiator, message("NOT_SET_UP", "&cThe PvP arena has not been set up yet."));
            return false;
        }
        if (isInMatch(first.getUniqueId()) || isInMatch(second.getUniqueId())) {
            send(initiator, message("MATCH_ALREADY_IN", "&cYou are already in a ranked match."));
            return false;
        }

        // An assigned match must not pull somebody out of an open arena fight they are in the
        // middle of. The queue already refuses arena players, so this only bites on /pvp assign.
        Player busy = pvp().isInArena(first.getUniqueId())
                ? first
                : (pvp().isInArena(second.getUniqueId()) ? second : null);
        if (busy != null) {
            send(initiator, message("MATCH_BUSY", "&c{player} is already in the arena.")
                    .replace("{player}", busy.getName()));
            return false;
        }

        PvpKit kit = pvp().getKit(kitId);
        if (kit == null) {
            List<PvpKit> kits = pvp().getKits();
            if (kits.isEmpty()) {
                send(initiator, message("NO_KITS", "&cThere are no PvP kits to choose from yet."));
                return false;
            }
            kit = kits.get(0);
        }

        leaveQueue(first.getUniqueId());
        leaveQueue(second.getUniqueId());

        long now = System.currentTimeMillis();
        PvpMatch match = new PvpMatch(
                localIds.incrementAndGet(),
                first.getUniqueId(), first.getName(),
                second.getUniqueId(), second.getName(),
                kit.getId(),
                now
        );
        match.setEloBefore(
                pvp().getStats(first.getUniqueId()).getElo(),
                pvp().getStats(second.getUniqueId()).getElo()
        );
        match.setCountdownEndsAt(now + Math.max(0, config().getInt("MATCH.COUNTDOWN_SECONDS", 5)) * 1000L);

        active.put(first.getUniqueId(), match);
        active.put(second.getUniqueId(), match);

        prepare(first, kit, pvp().getSpawn());
        prepare(second, kit, pvp().getSpawn2());

        String announcement = message("MATCH_STARTED", "&8[&cPVP&8] &f{first} &7vs &f{second}")
                .replace("{first}", first.getName())
                .replace("{second}", second.getName());
        send(first, announcement);
        send(second, announcement);
        if (initiator != null && !(initiator instanceof Player player && match.involves(player.getUniqueId()))) {
            send(initiator, announcement);
        }
        return true;
    }

    /** Drops a player onto their side of the arena with the match kit in hand. */
    private void prepare(Player player, PvpKit kit, Location spawn) {
        pvp().startSession(player);
        if (spawn == null) {
            equip(player, kit);
            return;
        }

        // The kit has to land after the teleport rather than beside it. Moving a player is
        // asynchronous, so handing the kit over here writes it into the inventory they are still
        // standing in - the survival one - and whatever keeps inventories apart per world then
        // files the kit away as everything they queued with.
        plugin.getSpigotScheduler().teleport(player, spawn).thenRun(() ->
                plugin.getSpigotScheduler().runEntity(player, () -> equip(player, kit)));
    }

    /**
     * Hands the match kit over once the player is standing in the arena.
     *
     * <p>The match can already be over by the time the teleport lands - a disconnect during those
     * few ticks ends it - so this checks it is still running before clearing anything.</p>
     */
    private void equip(Player player, PvpKit kit) {
        if (player == null || !player.isOnline() || !isInMatch(player.getUniqueId())) {
            return;
        }
        if (config().getBoolean("MATCH.HEAL_ON_START", true)) {
            pvp().healPlayer(player);
        }
        pvp().giveKit(player, kit);
        pvp().markKitGiven(player, kit);
    }

    /** Called when a player in a ranked match dies. The other one wins. */
    public void handleDeath(Player victim) {
        PvpMatch match = getMatch(victim.getUniqueId());
        if (match == null) {
            return;
        }
        endMatch(match, match.opponentOf(victim.getUniqueId()), PvpMatch.Result.DECIDED, false);
    }

    /** A player who disconnects mid-match hands the win to their opponent. */
    public void handleQuit(Player player) {
        leaveQueue(player.getUniqueId());
        PvpMatch match = getMatch(player.getUniqueId());
        if (match != null) {
            endMatch(match, match.opponentOf(player.getUniqueId()), PvpMatch.Result.DECIDED, false);
        }
    }

    /** Called when a player leaves the arena boundary during a match. */
    public void handleBoundaryExit(Player player) {
        PvpMatch match = getMatch(player.getUniqueId());
        if (match != null) {
            endMatch(match, match.opponentOf(player.getUniqueId()), PvpMatch.Result.DECIDED, false);
        }
    }

    public void handleHit(Player attacker, Player victim) {
        PvpMatch match = getMatch(attacker.getUniqueId());
        if (match != null && match.involves(victim.getUniqueId())) {
            match.addHit(attacker.getUniqueId());
        }
    }

    public void handleCrystal(Player attacker, Player victim) {
        PvpMatch match = getMatch(attacker.getUniqueId());
        if (match != null && match.involves(victim.getUniqueId())) {
            match.addCrystal(attacker.getUniqueId());
        }
    }

    /**
     * Finishes a match, moves the Elo, writes the record, and sends both players home.
     *
     * @param winner the winner, or null for a draw
     * @param silent true during shutdown, where no message would reach anyone
     */
    private void endMatch(PvpMatch match, UUID winner, PvpMatch.Result result, boolean silent) {
        if (match == null || active.get(match.getFirstUuid()) != match) {
            return;
        }

        active.remove(match.getFirstUuid());
        active.remove(match.getSecondUuid());
        match.setEndedAt(System.currentTimeMillis());
        captureFinalHealth(match);
        match.setWinnerUuid(winner);
        match.setResult(result);

        int win = config().getInt("MATCH.ELO_WIN", 20);
        int loss = config().getInt("MATCH.ELO_LOSS", 15);
        int draw = config().getInt("MATCH.ELO_DRAW", 0);

        int firstDelta;
        int secondDelta;
        if (result != PvpMatch.Result.DECIDED || winner == null) {
            firstDelta = draw;
            secondDelta = draw;
        } else if (match.isFirst(winner)) {
            firstDelta = win;
            secondDelta = -loss;
        } else {
            firstDelta = -loss;
            secondDelta = win;
        }

        // The stored delta is what the Elo floor and ceiling actually allowed, so the history never
        // shows a player losing points they did not have.
        int appliedFirst = pvp().applyMatchElo(match.getFirstUuid(), firstDelta);
        int appliedSecond = pvp().applyMatchElo(match.getSecondUuid(), secondDelta);
        match.setEloDelta(appliedFirst, appliedSecond);

        if (result == PvpMatch.Result.DECIDED && winner != null) {
            pvp().recordMatchOutcome(winner, match.opponentOf(winner));
        }

        insertMatch(match);

        Player first = Bukkit.getPlayer(match.getFirstUuid());
        Player second = Bukkit.getPlayer(match.getSecondUuid());
        if (!silent) {
            announceResult(match, first);
            announceResult(match, second);
        }
        if (first != null) {
            pvp().removeFromArena(first, silent);
        }
        if (second != null) {
            pvp().removeFromArena(second, silent);
        }
    }

    /**
     * Reads the health both fighters ended on, before either is cleaned up.
     *
     * <p>A player who is offline or already gone counts as zero, which is what a disconnect
     * forfeit should look like in the record.</p>
     */
    private void captureFinalHealth(PvpMatch match) {
        Player first = Bukkit.getPlayer(match.getFirstUuid());
        Player second = Bukkit.getPlayer(match.getSecondUuid());
        match.setFinalHealth(healthOf(first), healthOf(second));
        Player reference = first != null ? first : second;
        if (reference != null) {
            match.setMaxHealth(com.bx.ultimateDonutSmp.utils.AttributeUtils.getMaxHealth(reference));
        }
    }

    private double healthOf(Player player) {
        return player == null || player.isDead() ? 0.0D : player.getHealth();
    }

    private void announceResult(PvpMatch match, Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        UUID opponent = match.opponentOf(uuid);
        boolean won = match.getWinnerUuid() != null && match.getWinnerUuid().equals(uuid);
        String key = match.getResult() == PvpMatch.Result.DECIDED ? (won ? "MATCH_WIN" : "MATCH_LOSS") : "MATCH_DRAW";
        String fallback = switch (key) {
            case "MATCH_WIN" -> "&aYou beat &f{opponent} &8(&a{elo} elo&8)";
            case "MATCH_LOSS" -> "&cYou lost to &f{opponent} &8(&c{elo} elo&8)";
            default -> "&7The match against &f{opponent} &7ended in a draw.";
        };

        send(player, message(key, fallback)
                .replace("{opponent}", opponent == null ? "?" : resolveName(opponent))
                .replace("{elo}", formatDelta(match.getEloDelta(uuid)))
                .replace("{hits}", String.valueOf(match.getHits(uuid)))
                .replace("{crystals}", String.valueOf(match.getCrystals(uuid))));
    }

    /** Runs once a second alongside the arena tick: countdown lines and the duration cap. */
    public void tick() {
        long now = System.currentTimeMillis();
        long cap = PvpManager.parseDuration(config().getString("MATCH.MAX_DURATION", "5m"));

        for (PvpMatch match : new ArrayList<>(active.values())) {
            if (match.isCountingDown(now)) {
                long seconds = Math.max(1L, (match.getCountdownEndsAt() - now + 999L) / 1000L);
                String text = message("MATCH_COUNTDOWN", "&fStarting in &c{seconds}&f...")
                        .replace("{seconds}", String.valueOf(seconds));
                send(Bukkit.getPlayer(match.getFirstUuid()), text);
                send(Bukkit.getPlayer(match.getSecondUuid()), text);
                continue;
            }

            if (cap > 0 && now - match.getStartedAt() > cap) {
                endMatch(match, null, PvpMatch.Result.DRAW, false);
            }
        }
    }

    // ── History ───────────────────────────────────────────────────────────────

    public int countHistory(UUID uuid) {
        Connection connection = connection();
        if (uuid == null || connection == null) {
            return 0;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "select count(*) from pvp_matches where player_one_uuid = ? or player_two_uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to count PvP matches for " + uuid, exception);
            return 0;
        }
    }

    /** A player's matches, most recent first. */
    public List<PvpMatch> getHistory(UUID uuid, int limit, int offset) {
        List<PvpMatch> history = new ArrayList<>();
        Connection connection = connection();
        if (uuid == null || connection == null) {
            return history;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "select id, player_one_uuid, player_one_name, player_two_uuid, player_two_name, kit_id,"
                        + " winner_uuid, result, started_at, ended_at, one_hits, two_hits, one_crystals,"
                        + " two_crystals, one_elo_before, two_elo_before, one_elo_delta, two_elo_delta,"
                        + " one_final_health, two_final_health, max_health"
                        + " from pvp_matches where player_one_uuid = ? or player_two_uuid = ?"
                        + " order by ended_at desc limit ? offset ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, uuid.toString());
            ps.setInt(3, Math.max(1, limit));
            ps.setInt(4, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PvpMatch match = readMatch(rs);
                    if (match != null) {
                        history.add(match);
                    }
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to read PvP matches for " + uuid, exception);
        }
        return history;
    }

    private PvpMatch readMatch(ResultSet rs) throws SQLException {
        UUID first = parseUuid(rs.getString("player_one_uuid"));
        UUID second = parseUuid(rs.getString("player_two_uuid"));
        if (first == null || second == null) {
            return null;
        }

        PvpMatch match = new PvpMatch(
                rs.getLong("id"),
                first, rs.getString("player_one_name"),
                second, rs.getString("player_two_name"),
                rs.getString("kit_id"),
                rs.getLong("started_at")
        );
        match.setEndedAt(rs.getLong("ended_at"));
        match.setWinnerUuid(parseUuid(rs.getString("winner_uuid")));
        match.setHits(rs.getInt("one_hits"), rs.getInt("two_hits"));
        match.setCrystals(rs.getInt("one_crystals"), rs.getInt("two_crystals"));
        match.setEloBefore(rs.getInt("one_elo_before"), rs.getInt("two_elo_before"));
        match.setEloDelta(rs.getInt("one_elo_delta"), rs.getInt("two_elo_delta"));
        match.setFinalHealth(rs.getDouble("one_final_health"), rs.getDouble("two_final_health"));
        match.setMaxHealth(rs.getDouble("max_health"));
        try {
            match.setResult(PvpMatch.Result.valueOf(rs.getString("result")));
        } catch (IllegalArgumentException | NullPointerException exception) {
            match.setResult(PvpMatch.Result.ABORTED);
        }
        return match;
    }

    // ── Formatting helpers used by the menus ──────────────────────────────────

    /** Formats a stored match timestamp with the configured date pattern. */
    public String formatDate(long epochMillis) {
        String pattern = config().getString("MATCH.DATE_FORMAT", "dd/MM/yyyy HH:mm");
        try {
            return DateTimeFormatter.ofPattern(pattern)
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.ofEpochMilli(epochMillis));
        } catch (IllegalArgumentException exception) {
            return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.ofEpochMilli(epochMillis));
        }
    }

    /** Renders a match length as mm:ss, or hh:mm:ss once it runs past an hour. */
    public static String formatMatchDuration(long seconds) {
        long safe = Math.max(0L, seconds);
        long hours = safe / 3600L;
        long minutes = (safe % 3600L) / 60L;
        long remainder = safe % 60L;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, remainder);
        }
        return String.format("%02d:%02d", minutes, remainder);
    }

    /** Writes an Elo change the way the history reads it: {@code +20}, {@code -15}, {@code 0}. */
    public static String formatDelta(int delta) {
        return delta > 0 ? "+" + delta : String.valueOf(delta);
    }

    public String resolveName(UUID uuid) {
        if (uuid == null) {
            return "?";
        }
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            return online.getName();
        }
        String known = plugin.getDatabaseManager() == null
                ? null
                : plugin.getDatabaseManager().getLastKnownUsername(uuid);
        if (known != null && !known.isBlank()) {
            return known;
        }
        String offline = Bukkit.getOfflinePlayer(uuid).getName();
        return offline == null ? uuid.toString().substring(0, 8) : offline;
    }

    public String message(String key, String fallback) {
        return pvp() == null ? fallback : pvp().message(key, fallback);
    }

    private void send(org.bukkit.command.CommandSender target, String text) {
        if (target != null && text != null && !text.isBlank()) {
            target.sendMessage(ColorUtils.toComponent(text));
        }
    }

    private static UUID parseUuid(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return null;
        }
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    private Connection connection() {
        return plugin.getDatabaseManager() == null ? null : plugin.getDatabaseManager().getConnection();
    }

    private void ensureTables() {
        Connection connection = connection();
        if (connection == null) {
            return;
        }

        try (Statement st = connection.createStatement()) {
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS pvp_matches (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      player_one_uuid TEXT NOT NULL,
                      player_one_name TEXT,
                      player_two_uuid TEXT NOT NULL,
                      player_two_name TEXT,
                      kit_id TEXT,
                      winner_uuid TEXT,
                      result TEXT,
                      started_at INTEGER DEFAULT 0,
                      ended_at INTEGER DEFAULT 0,
                      one_hits INTEGER DEFAULT 0,
                      two_hits INTEGER DEFAULT 0,
                      one_crystals INTEGER DEFAULT 0,
                      two_crystals INTEGER DEFAULT 0,
                      one_elo_before INTEGER DEFAULT 0,
                      two_elo_before INTEGER DEFAULT 0,
                      one_elo_delta INTEGER DEFAULT 0,
                      two_elo_delta INTEGER DEFAULT 0,
                      one_final_health REAL DEFAULT 0,
                      two_final_health REAL DEFAULT 0,
                      max_health REAL DEFAULT 20
                    )
                    """);
            plugin.getDatabaseManager().executeSchema(st, """
                    CREATE TABLE IF NOT EXISTS pvp_sync_codes (
                      code TEXT PRIMARY KEY,
                      player_uuid TEXT NOT NULL,
                      player_name TEXT,
                      created_at INTEGER DEFAULT 0,
                      expires_at INTEGER DEFAULT 0
                    )
                    """);

            // The match table shipped before the health columns existed, so a server updating from
            // that build gets them added rather than a create that quietly does nothing.
            addColumn(st, "one_final_health", "REAL DEFAULT 0");
            addColumn(st, "two_final_health", "REAL DEFAULT 0");
            addColumn(st, "max_health", "REAL DEFAULT 20");
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to create the PvP match tables", exception);
        }
    }

    private void addColumn(Statement statement, String column, String definition) throws SQLException {
        if (plugin.getDatabaseManager().hasColumn("pvp_matches", column)) {
            return;
        }
        statement.execute(plugin.getDatabaseManager().adaptSchemaSql(
                "ALTER TABLE pvp_matches ADD COLUMN " + column + " " + definition));
    }

    private void insertMatch(PvpMatch match) {
        Connection connection = connection();
        if (connection == null) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "insert into pvp_matches (player_one_uuid, player_one_name, player_two_uuid, player_two_name,"
                        + " kit_id, winner_uuid, result, started_at, ended_at, one_hits, two_hits, one_crystals,"
                        + " two_crystals, one_elo_before, two_elo_before, one_elo_delta, two_elo_delta,"
                        + " one_final_health, two_final_health, max_health)"
                        + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, match.getFirstUuid().toString());
            ps.setString(2, match.getFirstName());
            ps.setString(3, match.getSecondUuid().toString());
            ps.setString(4, match.getSecondName());
            ps.setString(5, match.getKitId());
            ps.setString(6, match.getWinnerUuid() == null ? null : match.getWinnerUuid().toString());
            ps.setString(7, match.getResult() == null ? PvpMatch.Result.ABORTED.name() : match.getResult().name());
            ps.setLong(8, match.getStartedAt());
            ps.setLong(9, match.getEndedAt());
            ps.setInt(10, match.getFirstHits());
            ps.setInt(11, match.getSecondHits());
            ps.setInt(12, match.getFirstCrystals());
            ps.setInt(13, match.getSecondCrystals());
            ps.setInt(14, match.getFirstEloBefore());
            ps.setInt(15, match.getSecondEloBefore());
            ps.setInt(16, match.getFirstEloDelta());
            ps.setInt(17, match.getSecondEloDelta());
            ps.setDouble(18, match.getFirstFinalHealth());
            ps.setDouble(19, match.getSecondFinalHealth());
            ps.setDouble(20, match.getMaxHealth());
            ps.executeUpdate();
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to store a PvP match", exception);
        }
    }

    /** One player waiting in the ranked queue, and the kit they will fight with. */
    private record QueueEntry(String kitId, long queuedAt) {
    }
}
