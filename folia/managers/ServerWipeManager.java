package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PortalDefinition;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

public class ServerWipeManager {

    public enum State {
        IDLE,
        PREPARED,
        RUNNING,
        FAILED,
        SHUTDOWN_PENDING
    }

    public record OperationResult(boolean success, String message, String token) {
        public static OperationResult success(String message) {
            return new OperationResult(true, message, null);
        }

        public static OperationResult prepared(String message, String token) {
            return new OperationResult(true, message, token);
        }

        public static OperationResult failure(String message) {
            return new OperationResult(false, message, null);
        }
    }

    public record Preview(List<String> worlds, List<String> errors, DatabaseManager.ServerWipePreview database) {
        public boolean valid() {
            return errors.isEmpty();
        }
    }

    private record Validation(
            List<String> worlds,
            Map<String, World.Environment> environments,
            List<String> errors
    ) {
        private boolean valid() {
            return errors.isEmpty();
        }
    }

    private record MovedPath(Path original, Path backup, boolean world, World.Environment environment) {
    }

    private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PENDING_FILE_NAME = "server-wipe-pending.yml";

    private final UltimateDonutSmp plugin;
    private State state = State.IDLE;
    private boolean maintenanceMode;
    private boolean suppressShutdownSaves;
    private String confirmationToken;
    private long preparedAtMillis;
    private String lastError = "";
    private Path activeBackupDirectory;

    public ServerWipeManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public synchronized State getState() {
        return state;
    }

    public synchronized boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public synchronized boolean shouldSuppressShutdownSaves() {
        return suppressShutdownSaves;
    }

    public String getMaintenanceMessage() {
        return config().getString(
                "MESSAGES.MAINTENANCE",
                "&cᴛʜᴇ ѕᴇʀᴠᴇʀ ɪѕ ᴘʀᴇᴘᴀʀɪɴɢ ᴀ ѕᴇᴀѕᴏɴ ʀᴇѕᴇᴛ. ᴛʀʏ ᴀɢᴀɪɴ ᴀꜰᴛᴇʀ ᴛʜᴇ ʀᴇѕᴛᴀʀᴛ."
        );
    }

    public synchronized String describeStatus() {
        StringBuilder status = new StringBuilder("state=").append(state.name().toLowerCase(Locale.ROOT));
        if (state == State.PREPARED) {
            long remaining = Math.max(0L, tokenTtlMillis() - (System.currentTimeMillis() - preparedAtMillis));
            status.append(", token_expires_in=").append((remaining + 999L) / 1000L).append('s');
        }
        if (activeBackupDirectory != null) {
            status.append(", backup=").append(activeBackupDirectory);
        }
        if (!lastError.isBlank()) {
            status.append(", error=").append(lastError);
        }
        return status.toString();
    }

    public Preview preview() {
        Validation validation = validateConfiguredWorlds();
        DatabaseManager.ServerWipePreview databasePreview = plugin.getDatabaseManager()
                .previewServerWipe(new LinkedHashSet<>(validation.worlds()));
        return new Preview(validation.worlds(), validation.errors(), databasePreview);
    }

    public synchronized OperationResult prepare() {
        if (!config().getBoolean("ENABLED", true)) {
            return OperationResult.failure("Server wipe is disabled in server-wipe.yml.");
        }
        if (state == State.RUNNING || state == State.SHUTDOWN_PENDING) {
            return OperationResult.failure("A server wipe is already running.");
        }

        Validation validation = validateConfiguredWorlds();
        if (!validation.valid()) {
            return OperationResult.failure(String.join(" ", validation.errors()));
        }

        confirmationToken = generateToken();
        preparedAtMillis = System.currentTimeMillis();
        state = State.PREPARED;
        lastError = "";
        return OperationResult.prepared(
                "Prepared a wipe for " + String.join(", ", validation.worlds())
                        + ". Confirm within " + (tokenTtlMillis() / 1000L)
                        + "s using /serverwipe confirm " + confirmationToken + ".",
                confirmationToken
        );
    }

    public synchronized OperationResult cancel() {
        if (state != State.PREPARED) {
            return OperationResult.failure("There is no prepared wipe to cancel.");
        }
        clearPreparation();
        state = State.IDLE;
        return OperationResult.success("Prepared server wipe cancelled.");
    }

    public synchronized OperationResult confirm(String token) {
        if (state != State.PREPARED) {
            return OperationResult.failure("Run /serverwipe prepare first.");
        }
        if (isTokenExpired(preparedAtMillis, System.currentTimeMillis(), tokenTtlMillis())) {
            clearPreparation();
            state = State.IDLE;
            return OperationResult.failure("The confirmation token expired. Prepare the wipe again.");
        }
        if (token == null || !token.equalsIgnoreCase(confirmationToken)) {
            return OperationResult.failure("Invalid confirmation token.");
        }

        Validation validation = validateConfiguredWorlds();
        if (!validation.valid()) {
            clearPreparation();
            state = State.FAILED;
            lastError = String.join(" ", validation.errors());
            return OperationResult.failure(lastError);
        }

        state = State.RUNNING;
        maintenanceMode = true;
        clearPreparation();

        try {
            prepareRuntimeForWipe();
        } catch (Exception exception) {
            failBeforeCriticalStage("failed to prepare runtime state: " + exception.getMessage(), exception);
            return OperationResult.failure(lastError);
        }

        String kickMessage = config().getString(
                "MESSAGES.KICK",
                "&cᴀ ѕᴇᴀѕᴏɴ ʀᴇѕᴇᴛ ɪѕ ѕᴛᴀʀᴛɪɴɢ. ᴛʜᴇ ѕᴇʀᴠᴇʀ ᴡɪʟʟ ʀᴇѕᴛᴀʀᴛ ѕʜᴏʀᴛʟʏ."
        );
        for (Player player : List.copyOf(Bukkit.getOnlinePlayers())) {
            player.kickPlayer(ColorUtils.toComponent(kickMessage));
        }

        plugin.getFoliaScheduler().runGlobalLater(() -> executeCriticalWipe(validation), 2L);
        return OperationResult.success("Server wipe started. Players are being removed and the server will shut down.");
    }

    public void recoverOrRecreatePendingWorlds() {
        Path pendingFile = pendingFile();
        if (!Files.isRegularFile(pendingFile)) {
            return;
        }

        YamlConfiguration pending = YamlConfiguration.loadConfiguration(pendingFile.toFile());
        Path backupDirectory = readAbsolutePath(pending.getString("BACKUP-DIRECTORY", ""));
        String wipeId = pending.getString("WIPE-ID", "");
        if (backupDirectory == null) {
            plugin.getLogger().severe("Server wipe pending marker has no valid backup directory.");
            synchronized (this) {
                state = State.FAILED;
                maintenanceMode = true;
                lastError = "Invalid pending wipe marker.";
            }
            return;
        }

        try {
            if (!plugin.getDatabaseManager().isServerWipeCommitted(wipeId)) {
                restoreStagedFiles(pending, backupDirectory);
                Files.deleteIfExists(pendingFile);
                plugin.getLogger().warning("Recovered files from an interrupted server wipe staging phase.");
                return;
            }

            List<String> worlds = normalizeWorldNames(pending.getStringList("WORLDS"));
            for (String worldName : worlds) {
                World.Environment environment = parseEnvironment(
                        pending.getString("ENVIRONMENTS." + worldName, inferEnvironment(worldName).name())
                );
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    world = WorldCreator.name(worldName).environment(environment).createWorld();
                }
                if (world == null) {
                    throw new IOException("Failed to recreate world " + worldName + ".");
                }
                world.setKeepSpawnInMemory(false);
            }

            Files.deleteIfExists(pendingFile);
            plugin.getLogger().info("Recreated server-wipe worlds: " + String.join(", ", worlds) + ".");
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to recover pending server wipe state", exception);
            synchronized (this) {
                state = State.FAILED;
                maintenanceMode = true;
                lastError = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            }
        }
    }

    private void executeCriticalWipe(Validation validation) {
        List<MovedPath> movedPaths = new ArrayList<>();
        Path backupDirectory = null;
        Path pendingFile = pendingFile();
        boolean databaseCommitted = false;
        try {
            backupDirectory = createBackupDirectory();
            String wipeId = backupDirectory.getFileName().toString();
            synchronized (this) {
                activeBackupDirectory = backupDirectory;
            }

            List<Path> playerDataDirectories = findPlayerDataDirectories(validation.worlds());
            writePendingMarker(pendingFile, backupDirectory, wipeId, validation, playerDataDirectories);
            writeManifest(backupDirectory, validation, "BACKING_UP", null);
            plugin.getDatabaseManager().writePortableBackup(backupDirectory.resolve("database.sql"));

            movePlayerDataDirectories(playerDataDirectories, backupDirectory, movedPaths);
            moveResetWorlds(validation, backupDirectory, movedPaths);

            double startingMoney = plugin.getConfigManager().getConfig()
                    .getDouble("SETTINGS.MONEY-PER-DEFAULT", 1000D);
            DatabaseManager.ServerWipeResult result = plugin.getDatabaseManager()
                    .resetForServerWipe(startingMoney, new LinkedHashSet<>(validation.worlds()), wipeId);
            databaseCommitted = true;

            try {
                clearRuntimeAfterCommit();
            } catch (RuntimeException runtimeError) {
                plugin.getLogger().log(Level.SEVERE,
                        "Runtime cleanup failed after the server wipe database commit; shutdown will continue",
                        runtimeError);
            }

            try {
                writeManifest(backupDirectory, validation, "COMPLETED", result.affectedCounts());
            } catch (IOException manifestError) {
                plugin.getLogger().log(Level.WARNING, "Failed to finalize server wipe manifest", manifestError);
            }

            synchronized (this) {
                suppressShutdownSaves = true;
                state = State.SHUTDOWN_PENDING;
                lastError = "";
            }
            plugin.getLogger().warning("Server wipe completed. Backup: " + backupDirectory);
            Bukkit.shutdown();
        } catch (Exception exception) {
            if (databaseCommitted) {
                plugin.getLogger().log(Level.SEVERE,
                        "A post-commit server wipe step failed; preserving reset files and forcing shutdown",
                        exception);
                synchronized (this) {
                    suppressShutdownSaves = true;
                    state = State.SHUTDOWN_PENDING;
                    lastError = exception.getMessage() == null
                            ? exception.getClass().getSimpleName()
                            : exception.getMessage();
                }
                Bukkit.shutdown();
                return;
            }
            plugin.getLogger().log(Level.SEVERE, "Server wipe failed; restoring staged filesystem data", exception);
            restoreMovedPaths(movedPaths);
            try {
                Files.deleteIfExists(pendingFile);
                if (backupDirectory != null) {
                    writeManifest(backupDirectory, validation, "FAILED", Map.of());
                }
            } catch (IOException recoveryError) {
                plugin.getLogger().log(Level.SEVERE, "Failed to update wipe recovery files", recoveryError);
            }
            resumeAfterFailure(exception);
        }
    }

    private void prepareRuntimeForWipe() {
        if (!plugin.getEnderChestManager().flushAndDiscardForServerWipe()) {
            throw new IllegalStateException("An open Ender Chest could not be saved for backup.");
        }
        plugin.getCrateManager().prepareForServerWipe();
        plugin.getAuctionHouseManager().prepareForServerWipe();
        plugin.getOrdersManager().prepareForServerWipe();
        plugin.getDuelManager().shutdown();
        plugin.getFfaManager().shutdown();
        plugin.getSpawnerManager().setServerWipeMode(true);
    }

    private void clearRuntimeAfterCommit() {
        plugin.getPlayerDataManager().discardAllForServerWipe();
        plugin.getEnderChestManager().discardAllForServerWipe();
        plugin.getHomeManager().clearAllCaches();
        plugin.getTeamManager().resetRuntimeState();
        plugin.getBountyManager().clearAll();
        plugin.getCrateManager().prepareForServerWipe();
        plugin.getLeaderboardManager().invalidateAll();
    }

    private void failBeforeCriticalStage(String message, Exception exception) {
        plugin.getLogger().log(Level.SEVERE, message, exception);
        synchronized (this) {
            state = State.FAILED;
            maintenanceMode = false;
            lastError = message;
        }
        plugin.getSpawnerManager().setServerWipeMode(false);
        plugin.getEnderChestManager().reload();
    }

    private void resumeAfterFailure(Exception exception) {
        plugin.getSpawnerManager().setServerWipeMode(false);
        plugin.getEnderChestManager().reload();
        try {
            plugin.getDuelManager().reload();
            plugin.getFfaManager().reload();
        } catch (Exception reloadError) {
            plugin.getLogger().log(Level.WARNING, "Failed to restore PvP managers after wipe failure", reloadError);
        }
        synchronized (this) {
            state = State.FAILED;
            maintenanceMode = false;
            suppressShutdownSaves = false;
            lastError = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        }
    }

    private Validation validateConfiguredWorlds() {
        List<String> worlds = normalizeWorldNames(config().getStringList("RESET-WORLDS"));
        List<String> errors = new ArrayList<>();
        Map<String, World.Environment> environments = new LinkedHashMap<>();

        if (worlds.isEmpty()) {
            errors.add("RESET-WORLDS is empty in server-wipe.yml.");
            return new Validation(worlds, environments, errors);
        }

        Set<String> protectedWorlds = collectProtectedWorlds();
        Path worldContainer = Bukkit.getWorldContainer().toPath().toAbsolutePath().normalize();
        for (String worldName : worlds) {
            if (!isSafeWorldName(worldName)) {
                errors.add("Unsafe world name: " + worldName + ".");
                continue;
            }
            if (protectedWorlds.contains(worldName.toLowerCase(Locale.ROOT))) {
                errors.add("World " + worldName + " is protected.");
                continue;
            }
            if (!isConfiguredRtpWorld(worldName)) {
                errors.add("World " + worldName + " is not configured as an RTP world.");
                continue;
            }

            Path worldFolder = worldContainer.resolve(worldName).normalize();
            if (!worldFolder.getParent().equals(worldContainer)) {
                errors.add("World " + worldName + " is not a direct child of the server world directory.");
                continue;
            }
            World loaded = Bukkit.getWorld(worldName);
            if (loaded == null && !Files.isDirectory(worldFolder)) {
                errors.add("World folder does not exist: " + worldName + ".");
                continue;
            }
            environments.put(worldName, loaded == null ? inferEnvironment(worldName) : loaded.getEnvironment());
        }

        return new Validation(List.copyOf(worlds), Map.copyOf(environments), List.copyOf(errors));
    }

    private Set<String> collectProtectedWorlds() {
        LinkedHashSet<String> protectedWorlds = new LinkedHashSet<>();
        for (String configured : config().getStringList("PROTECTED-WORLDS")) {
            addWorldName(protectedWorlds, configured);
        }

        if (!Bukkit.getWorlds().isEmpty()) {
            String primary = Bukkit.getWorlds().getFirst().getName();
            addWorldName(protectedWorlds, primary);
            addWorldName(protectedWorlds, primary + "_nether");
            addWorldName(protectedWorlds, primary + "_the_end");
        }

        if (plugin.getSpawnManager() != null) {
            addLocationWorld(protectedWorlds, plugin.getSpawnManager().getSpawnLocation());
            addLocationWorld(protectedWorlds, plugin.getSpawnManager().getAfkLocation());
            for (SpawnManager.TeleportArea area : plugin.getSpawnManager().getSpawnAreas()) {
                addLocationWorld(protectedWorlds, plugin.getSpawnManager().resolveDestination(area));
            }
            for (SpawnManager.TeleportArea area : plugin.getSpawnManager().getAfkAreas()) {
                addLocationWorld(protectedWorlds, plugin.getSpawnManager().resolveDestination(area));
            }
        }

        if (plugin.getWarpManager() != null) {
            for (Location location : plugin.getWarpManager().getWarpLocations()) {
                addLocationWorld(protectedWorlds, location);
            }
        }

        if (plugin.getPortalManager() != null && plugin.getCuboidManager() != null) {
            for (PortalDefinition portal : plugin.getPortalManager().getPortals()) {
                CuboidManager.Cuboid cuboid = plugin.getCuboidManager().getCuboid(portal.cuboidName());
                if (cuboid != null) {
                    addWorldName(protectedWorlds, cuboid.world());
                }
                addWorldName(protectedWorlds, portal.hologramWorld());
            }
        }

        if (plugin.getCrateManager() != null) {
            for (CrateManager.CrateBlockKey key : plugin.getCrateManager().getBoundBlockIds().keySet()) {
                addWorldName(protectedWorlds, key.world());
            }
        }
        return Set.copyOf(protectedWorlds);
    }

    private boolean isConfiguredRtpWorld(String worldName) {
        FileConfiguration rtp = plugin.getConfigManager().getRtp();
        if (rtp.isConfigurationSection("WORLD-SETTINGS." + worldName)) {
            return true;
        }
        ConfigurationSection buttons = rtp.getConfigurationSection("RTP-MENU.BUTTONS");
        if (buttons == null) {
            return false;
        }
        for (String key : buttons.getKeys(false)) {
            if (worldName.equalsIgnoreCase(buttons.getString(key + ".WORLD", ""))) {
                return true;
            }
        }
        return false;
    }

    private Path createBackupDirectory() throws IOException {
        String configured = config().getString("BACKUP-DIRECTORY", "server-wipe-backups");
        Path root = Path.of(configured == null || configured.isBlank() ? "server-wipe-backups" : configured);
        if (!root.isAbsolute()) {
            root = plugin.getDataFolder().toPath().resolve(root);
        }
        root = root.toAbsolutePath().normalize();
        Files.createDirectories(root);

        String suffix = BACKUP_TIME.format(LocalDateTime.now()) + "-" + Integer.toHexString(RANDOM.nextInt(0x10000));
        Path backup = root.resolve(suffix).normalize();
        if (!backup.getParent().equals(root)) {
            throw new IOException("Invalid backup directory.");
        }
        Files.createDirectory(backup);
        return backup;
    }

    private List<Path> findPlayerDataDirectories(List<String> resetWorlds) {
        Set<String> reset = new LinkedHashSet<>();
        for (String world : resetWorlds) {
            reset.add(world.toLowerCase(Locale.ROOT));
        }

        LinkedHashSet<Path> directories = new LinkedHashSet<>();
        Path worldContainer = Bukkit.getWorldContainer().toPath().toAbsolutePath().normalize();
        for (World world : Bukkit.getWorlds()) {
            if (reset.contains(world.getName().toLowerCase(Locale.ROOT))) {
                continue;
            }
            Path playerData = world.getWorldFolder().toPath().toAbsolutePath().normalize().resolve("playerdata");
            if (playerData.startsWith(worldContainer) && Files.isDirectory(playerData)) {
                directories.add(playerData);
            }
        }
        return List.copyOf(directories);
    }

    private void movePlayerDataDirectories(
            List<Path> playerDataDirectories,
            Path backupDirectory,
            List<MovedPath> movedPaths
    ) throws IOException {
        Path worldContainer = Bukkit.getWorldContainer().toPath().toAbsolutePath().normalize();
        for (Path original : playerDataDirectories) {
            Path relative = worldContainer.relativize(original);
            Path backup = backupDirectory.resolve("playerdata").resolve(relative).normalize();
            Files.createDirectories(backup.getParent());
            moveDirectory(original, backup);
            movedPaths.add(new MovedPath(original, backup, false, null));
            Files.createDirectories(original);
        }
    }

    private void moveResetWorlds(
            Validation validation,
            Path backupDirectory,
            List<MovedPath> movedPaths
    ) throws IOException {
        Path worldContainer = Bukkit.getWorldContainer().toPath().toAbsolutePath().normalize();
        for (String worldName : validation.worlds()) {
            World loaded = Bukkit.getWorld(worldName);
            World.Environment environment = validation.environments().getOrDefault(worldName, inferEnvironment(worldName));
            if (loaded != null) {
                loaded.save();
                if (!Bukkit.unloadWorld(loaded, true)) {
                    throw new IOException("Failed to unload world " + worldName + ".");
                }
            }

            Path original = worldContainer.resolve(worldName).normalize();
            Path backup = backupDirectory.resolve("worlds").resolve(worldName).normalize();
            Files.createDirectories(backup.getParent());
            moveDirectory(original, backup);
            movedPaths.add(new MovedPath(original, backup, true, environment));
        }
    }

    private void moveDirectory(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target);
        }
    }

    private void restoreMovedPaths(List<MovedPath> movedPaths) {
        List<MovedPath> reverse = new ArrayList<>(movedPaths);
        reverse.sort(Comparator.comparing((MovedPath moved) -> moved.world() ? 0 : 1));
        for (MovedPath moved : reverse) {
            try {
                deleteRecursively(moved.original());
                copyDirectory(moved.backup(), moved.original());
                if (moved.world() && Bukkit.getWorld(moved.original().getFileName().toString()) == null) {
                    WorldCreator.name(moved.original().getFileName().toString())
                            .environment(moved.environment())
                            .createWorld();
                }
            } catch (Exception exception) {
                plugin.getLogger().log(Level.SEVERE, "Failed to restore " + moved.original(), exception);
            }
        }
    }

    private void writePendingMarker(
            Path pendingFile,
            Path backupDirectory,
            String wipeId,
            Validation validation,
            List<Path> playerDataDirectories
    ) throws IOException {
        YamlConfiguration pending = new YamlConfiguration();
        pending.set("BACKUP-DIRECTORY", backupDirectory.toAbsolutePath().normalize().toString());
        pending.set("WIPE-ID", wipeId);
        pending.set("WORLDS", validation.worlds());
        for (Map.Entry<String, World.Environment> entry : validation.environments().entrySet()) {
            pending.set("ENVIRONMENTS." + entry.getKey(), entry.getValue().name());
        }

        Path worldContainer = Bukkit.getWorldContainer().toPath().toAbsolutePath().normalize();
        List<String> relativePlayerData = new ArrayList<>();
        for (Path path : playerDataDirectories) {
            relativePlayerData.add(worldContainer.relativize(path).toString().replace('\\', '/'));
        }
        pending.set("PLAYERDATA-PATHS", relativePlayerData);
        pending.save(pendingFile.toFile());
    }

    private void restoreStagedFiles(YamlConfiguration pending, Path backupDirectory) throws IOException {
        Path worldContainer = Bukkit.getWorldContainer().toPath().toAbsolutePath().normalize();
        for (String worldName : normalizeWorldNames(pending.getStringList("WORLDS"))) {
            Path backup = backupDirectory.resolve("worlds").resolve(worldName).normalize();
            Path original = worldContainer.resolve(worldName).normalize();
            if (Files.isDirectory(backup) && !Files.exists(original)) {
                copyDirectory(backup, original);
            }
        }
        for (String relativeValue : pending.getStringList("PLAYERDATA-PATHS")) {
            Path relative = Path.of(relativeValue).normalize();
            Path original = worldContainer.resolve(relative).normalize();
            Path backup = backupDirectory.resolve("playerdata").resolve(relative).normalize();
            if (original.startsWith(worldContainer) && Files.isDirectory(backup)) {
                deleteRecursively(original);
                copyDirectory(backup, original);
            }
        }
    }

    private void writeManifest(
            Path backupDirectory,
            Validation validation,
            String status,
            Map<String, Integer> affected
    ) throws IOException {
        YamlConfiguration manifest = new YamlConfiguration();
        manifest.set("STATUS", status);
        manifest.set("CREATED-AT", System.currentTimeMillis());
        manifest.set("DATABASE-TYPE", plugin.getDatabaseManager().getDatabaseType().name());
        manifest.set("WORLDS", validation.worlds());
        if (affected != null) {
            for (Map.Entry<String, Integer> entry : affected.entrySet()) {
                manifest.set("AFFECTED." + entry.getKey(), entry.getValue());
            }
        }
        manifest.save(backupDirectory.resolve("manifest.yml").toFile());
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path current : paths.toList()) {
                Path relative = source.relativize(current);
                Path destination = target.resolve(relative);
                if (Files.isDirectory(current)) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(Objects.requireNonNull(destination.getParent()));
                    Files.copy(current, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
        }
    }

    private void deleteRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private Path pendingFile() {
        return plugin.getDataFolder().toPath().resolve(PENDING_FILE_NAME).toAbsolutePath().normalize();
    }

    private Path readAbsolutePath(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Path.of(value).toAbsolutePath().normalize();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private FileConfiguration config() {
        return plugin.getConfigManager().getServerWipe();
    }

    private long tokenTtlMillis() {
        return Math.max(30L, config().getLong("TOKEN-TTL-SECONDS", 300L)) * 1000L;
    }

    private void clearPreparation() {
        confirmationToken = null;
        preparedAtMillis = 0L;
    }

    private String generateToken() {
        return String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
    }

    private void addLocationWorld(Set<String> worlds, Location location) {
        if (location != null && location.getWorld() != null) {
            addWorldName(worlds, location.getWorld().getName());
        }
    }

    private void addWorldName(Set<String> worlds, String world) {
        if (world != null && !world.isBlank()) {
            worlds.add(world.trim().toLowerCase(Locale.ROOT));
        }
    }

    static boolean isSafeWorldName(String worldName) {
        return worldName != null && worldName.matches("[A-Za-z0-9._-]+") && !worldName.contains("..");
    }

    static List<String> normalizeWorldNames(Collection<String> values) {
        LinkedHashMap<String, String> worlds = new LinkedHashMap<>();
        if (values != null) {
            for (String value : values) {
                if (value == null || value.isBlank()) {
                    continue;
                }
                String trimmed = value.trim();
                worlds.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed);
            }
        }
        return List.copyOf(worlds.values());
    }

    static boolean isTokenExpired(long preparedAt, long now, long ttlMillis) {
        return preparedAt <= 0L || ttlMillis <= 0L || now - preparedAt > ttlMillis;
    }

    private static World.Environment inferEnvironment(String worldName) {
        String normalized = worldName.toLowerCase(Locale.ROOT);
        if (normalized.endsWith("_nether") || normalized.contains("nether")) {
            return World.Environment.NETHER;
        }
        if (normalized.endsWith("_the_end") || normalized.endsWith("_end") || normalized.contains("the_end")) {
            return World.Environment.THE_END;
        }
        return World.Environment.NORMAL;
    }

    private static World.Environment parseEnvironment(String raw) {
        try {
            return World.Environment.valueOf(raw == null ? "NORMAL" : raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return World.Environment.NORMAL;
        }
    }
}
