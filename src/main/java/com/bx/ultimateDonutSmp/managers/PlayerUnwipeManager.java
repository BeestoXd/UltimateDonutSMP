package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerWipeArchive;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The other half of {@link PlayerWipeManager}: every wipe leaves a backup of the rows it removed in
 * a folder beside the plugin's configuration, and this puts one of them back. Backups are kept
 * indefinitely and never overwritten, so a player wiped twice can be taken back to either point.
 */
public class PlayerUnwipeManager {

    private static final String BACKUP_FOLDER = "player-wipe-backups";
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter READABLE =
            DateTimeFormatter.ofPattern("d MMM yyyy 'at' HH:mm").withZone(ZoneId.systemDefault());

    /**
     * A backup filename, read without opening the file. Tab completion runs on every keystroke, so
     * the name and uuid live in the filename rather than behind a parse of every backup on disk.
     */
    private static final Pattern FILE_NAME = Pattern.compile(
            "^(.*)_([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})_(.+)\\.yml$");

    private static final Pattern UNSAFE_NAME_CHARACTER = Pattern.compile("[^A-Za-z0-9_.-]");

    public record RestoreResult(
            boolean success,
            boolean busy,
            DatabaseManager.PlayerWipeResult counts,
            String errorMessage
    ) {
        public static RestoreResult alreadyRunning() {
            return new RestoreResult(false, true, null, null);
        }

        public static RestoreResult failure(String errorMessage) {
            return new RestoreResult(false, false, null, errorMessage);
        }
    }

    /** One backup on disk, described by its filename alone. */
    public record ArchiveFile(File file, UUID playerUuid, String playerName, String stamp) {
    }

    private final UltimateDonutSmp plugin;
    private final AtomicBoolean restoreInProgress = new AtomicBoolean(false);

    public PlayerUnwipeManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public static String formatWipeTime(long epochMillis) {
        return READABLE.format(Instant.ofEpochMilli(epochMillis));
    }

    public boolean isRestoreInProgress() {
        return restoreInProgress.get();
    }

    public File backupDirectory() {
        return new File(plugin.getDataFolder(), BACKUP_FOLDER);
    }

    /**
     * Writes the backup a wipe just captured. The wipe has not run yet when this is called, so a
     * failure here stops the wipe rather than losing the only copy of the player's data.
     */
    public File write(PlayerWipeArchive archive) throws IOException {
        File directory = backupDirectory();
        String base = sanitize(archive.playerName()) + "_" + archive.playerUuid() + "_"
                + STAMP.format(Instant.ofEpochMilli(archive.wipedAt()));

        File file = new File(directory, base + ".yml");
        for (int attempt = 2; file.exists(); attempt++) {
            file = new File(directory, base + "-" + attempt + ".yml");
        }

        archive.save(file);
        return file;
    }

    /** The most recent backup for one player, or null when they have never been wiped. */
    public PlayerWipeArchive findLatest(UUID playerUuid) {
        ArchiveFile latest = null;
        for (ArchiveFile candidate : listArchiveFiles()) {
            if (!candidate.playerUuid().equals(playerUuid)) {
                continue;
            }
            if (latest == null || candidate.stamp().compareTo(latest.stamp()) > 0) {
                latest = candidate;
            }
        }
        if (latest == null) {
            return null;
        }

        try {
            return PlayerWipeArchive.load(latest.file());
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not read wipe backup " + latest.file().getName(), exception);
            return null;
        }
    }

    /** Every player with a backup on disk, newest name first, for tab completion. */
    public List<String> backedUpPlayerNames() {
        Set<String> names = new LinkedHashSet<>();
        for (ArchiveFile archive : listArchiveFiles()) {
            names.add(archive.playerName());
        }
        return names.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public List<ArchiveFile> listArchiveFiles() {
        File[] files = backupDirectory().listFiles();
        if (files == null) {
            return List.of();
        }

        List<ArchiveFile> archives = new ArrayList<>();
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            Matcher matcher = FILE_NAME.matcher(file.getName());
            if (!matcher.matches()) {
                continue;
            }
            try {
                archives.add(new ArchiveFile(
                        file,
                        UUID.fromString(matcher.group(2)),
                        matcher.group(1),
                        matcher.group(3)
                ));
            } catch (IllegalArgumentException ignored) {
                // A file that only looks like a backup. Leave it where it is.
            }
        }
        return archives;
    }

    /**
     * Resolves a name to a player who has a backup. Unlike a wipe this cannot fall back to the
     * server's player list: the whole point is that the target may be long gone.
     */
    public UUID resolveBackedUpPlayer(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String trimmed = input.trim();
        ArchiveFile latest = null;
        for (ArchiveFile candidate : listArchiveFiles()) {
            if (!candidate.playerName().equalsIgnoreCase(trimmed)) {
                continue;
            }
            if (latest == null || candidate.stamp().compareTo(latest.stamp()) > 0) {
                latest = candidate;
            }
        }
        if (latest != null) {
            return latest.playerUuid();
        }

        Player online = Bukkit.getPlayerExact(trimmed);
        if (online != null) {
            return online.getUniqueId();
        }
        return plugin.getDatabaseManager().findPlayerUuidByUsername(trimmed);
    }

    public RestoreResult restore(PlayerWipeArchive archive, String actorName) {
        if (archive == null) {
            return RestoreResult.failure("no backup selected.");
        }
        if (plugin.getPlayerWipeManager().isWipeInProgress()) {
            return RestoreResult.alreadyRunning();
        }
        if (!restoreInProgress.compareAndSet(false, true)) {
            return RestoreResult.alreadyRunning();
        }

        UUID playerUuid = archive.playerUuid();
        try {
            discardOpenState(playerUuid);
            DatabaseManager.PlayerWipeResult counts = plugin.getDatabaseManager()
                    .restorePlayerWipeArchive(archive);
            reloadCaches(playerUuid);
            refreshDisplays(playerUuid);

            plugin.getLogger().info("Player wipe restored by " + actorName + " for "
                    + archive.playerName() + " (" + playerUuid + ").");
            return new RestoreResult(true, false, counts, null);
        } catch (SQLException | RuntimeException exception) {
            plugin.getLogger().log(Level.SEVERE, "player unwipe failed for " + playerUuid, exception);
            return RestoreResult.failure(exception.getMessage());
        } finally {
            restoreInProgress.set(false);
        }
    }

    /** Closes anything showing the player's current contents before those rows are replaced. */
    private void discardOpenState(UUID playerUuid) {
        if (plugin.getEnderChestManager() != null) {
            plugin.getEnderChestManager().discardForPlayerWipe(playerUuid);
        }
        plugin.getCrateManager().clearSession(playerUuid);
        plugin.getCrateManager().clearPendingBind(playerUuid);
    }

    /**
     * Reads the restored rows back into memory. This mirrors the cache clearing a wipe does, with
     * the player's own data loaded again afterwards so nothing writes the wiped state back over it.
     */
    private void reloadCaches(UUID playerUuid) {
        plugin.getPlayerDataManager().discardWithoutSaving(playerUuid);
        plugin.getCrateManager().unloadKeyBalanceCache(playerUuid);
        plugin.getShopManager().cleanupPlayer(playerUuid);
        plugin.getHomeManager().unloadHomes(playerUuid);
        plugin.getIgnoreManager().unloadPlayer(playerUuid);
        plugin.getDuelManager().forgetStats(playerUuid);
        plugin.getFfaManager().forgetStats(playerUuid);
        plugin.getTeamManager().loadAll();
        plugin.getBountyManager().loadAll();
        if (plugin.getFriendsManager() != null) {
            plugin.getFriendsManager().unloadPlayer(playerUuid);
        }
        if (plugin.getAuctionHouseManager() != null) {
            plugin.getAuctionHouseManager().cleanupPlayer(playerUuid);
        }
        if (plugin.getOrdersManager() != null) {
            plugin.getOrdersManager().forgetUiState(playerUuid);
        }
        if (plugin.getLeaderboardManager() != null) {
            plugin.getLeaderboardManager().invalidateAll();
        }

        Player online = Bukkit.getPlayer(playerUuid);
        if (online != null) {
            plugin.getPlayerDataManager().loadOrCreate(online);
            plugin.getHomeManager().loadHomes(online);
            plugin.getIgnoreManager().loadPlayer(playerUuid);
            if (plugin.getFriendsManager() != null) {
                plugin.getFriendsManager().loadPlayer(playerUuid);
            }
        }
    }

    private void refreshDisplays(UUID playerUuid) {
        plugin.getScoreboardManager().updateAll();
        plugin.getTablistManager().updateAll();

        Player online = Bukkit.getPlayer(playerUuid);
        if (online != null) {
            plugin.getTablistManager().updateTablistName(online);
        }
    }

    private static String sanitize(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return "unknown";
        }
        String cleaned = UNSAFE_NAME_CHARACTER.matcher(playerName.trim()).replaceAll("");
        return cleaned.isBlank() ? "unknown" : cleaned;
    }
}
