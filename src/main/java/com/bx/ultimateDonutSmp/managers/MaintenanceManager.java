package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.TitleUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class MaintenanceManager {

    private static final String REDIS_MAINTENANCE_CHANNEL = "ultimatedonutsmp:maintenance";

    /** Returned by {@link #getRemainingSeconds()} when maintenance has no scheduled end. */
    public static final long NO_DEADLINE = -1L;

    private final UltimateDonutSmp plugin;
    private final File stateFile;
    private boolean maintenanceActive;
    private String customLobbyServer;
    private long maintenanceEndsAt;
    private BukkitTask expiryTask;

    public MaintenanceManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.stateFile = new File(plugin.getDataFolder(), "maintenance-state.yml");
        load();
    }

    public void load() {
        if (!stateFile.exists()) {
            this.maintenanceActive = false;
            this.customLobbyServer = null;
            this.maintenanceEndsAt = 0L;
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(stateFile);
        this.maintenanceActive = config.getBoolean("active", false);
        this.customLobbyServer = config.getString("lobby", null);
        this.maintenanceEndsAt = config.getLong("until", 0L);
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("active", maintenanceActive);
        if (customLobbyServer != null) {
            config.set("lobby", customLobbyServer);
        }
        if (maintenanceEndsAt > 0L) {
            config.set("until", maintenanceEndsAt);
        }

        try {
            config.save(stateFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save maintenance state file", e);
        }
    }

    public boolean isMaintenanceActive() {
        return maintenanceActive;
    }

    public void setMaintenanceActive(boolean active) {
        this.maintenanceActive = active;
        save();
    }

    /**
     * Seconds left before maintenance lifts itself, or {@link #NO_DEADLINE} when there is no end
     * time to count down to.
     */
    public long getRemainingSeconds() {
        return remainingSeconds(maintenanceEndsAt, System.currentTimeMillis());
    }

    static long remainingSeconds(long endsAt, long now) {
        if (endsAt <= 0L) {
            return NO_DEADLINE;
        }
        long remainingMillis = endsAt - now;
        if (remainingMillis <= 0L) {
            return 0L;
        }
        // Rounded up, so a deadline 3.4 seconds out reads as 4. Rounding down would park the
        // countdown on 00:00 for a whole second while the server is still shut.
        return (remainingMillis + 999L) / 1000L;
    }

    /**
     * Renders a countdown the way the multiplayer list shows it: mm:ss, widening to h:mm:ss once
     * more than an hour is left.
     */
    public static String formatCountdown(long totalSeconds) {
        long seconds = Math.max(0L, totalSeconds);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remaining = seconds % 60L;
        if (hours > 0L) {
            return String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, remaining);
        }
        return String.format(Locale.ROOT, "%02d:%02d", minutes, remaining);
    }

    static boolean hasExpired(boolean active, long endsAt, long now) {
        return active && endsAt > 0L && now >= endsAt;
    }

    /**
     * Watches the deadline set by /maintenance on with a duration. Without it the countdown in the
     * server list would run down to 00:00 and the server would stay shut, which is the one thing a
     * countdown promises will not happen.
     */
    public void startExpiryTask() {
        if (expiryTask != null) {
            return;
        }
        expiryTask = plugin.getSpigotScheduler().runGlobalTimer(() -> {
            if (hasExpired(maintenanceActive, maintenanceEndsAt, System.currentTimeMillis())) {
                plugin.getLogger().info("Maintenance mode ended: the scheduled duration ran out.");
                stopMaintenance();
            }
        }, 20L, 20L);
    }

    public String getLobbyServer() {
        if (customLobbyServer != null && !customLobbyServer.isBlank()) {
            return customLobbyServer;
        }
        return plugin.getConfigManager().getNetwork().getString("MAINTENANCE.LOBBY_SERVER", "lobby");
    }

    public boolean isUseProxy() {
        return plugin.getConfigManager().getNetwork().getBoolean("MAINTENANCE.USE_PROXY", true);
    }

    public String getLobbyWorld() {
        return plugin.getConfigManager().getNetwork().getString("MAINTENANCE.LOBBY_WORLD", "WORLD");
    }

    /**
     * Whether maintenance has somewhere to put a player. When it does not, the player is refused at
     * login instead of being allowed into the world and kicked a couple of seconds later.
     */
    public boolean hasLobbyDestination() {
        if (isUseProxy()) {
            return isLobbyServerSet(getLobbyServer());
        }
        return resolveLocalDestination() != null;
    }

    static boolean isLobbyServerSet(String lobbyServer) {
        return isLobbyDestinationSet(lobbyServer);
    }

    /**
     * Blank means the same thing in either lobby key: nothing is set, so maintenance has nowhere
     * to put anyone and falls back to refusing them instead of leaving them in the world.
     */
    static boolean isLobbyDestinationSet(String lobby) {
        return lobby != null && !lobby.isBlank();
    }

    /**
     * Whether players still on the server once the handoff window closes get kicked. Proxy mode
     * always kicks, since the connect packet can go unanswered; a local server only kicks when it
     * has no lobby world to move them to, because otherwise they are already where they belong.
     */
    static boolean kicksLeftoverPlayers(boolean useProxy, boolean hasLocalDestination) {
        return useProxy || !hasLocalDestination;
    }

    /**
     * Blank is stored as no lobby at all, so clearing it falls back to MAINTENANCE.LOBBY_SERVER
     * rather than leaving an empty name in the state file.
     */
    static String normalizeLobbyServer(String lobbyServer) {
        return isLobbyServerSet(lobbyServer) ? lobbyServer : null;
    }

    public Location resolveLocalDestination() {
        String lobbyWorld = getLobbyWorld();
        // An empty LOBBY_WORLD is the local answer to an empty LOBBY_SERVER: the server has nowhere
        // to put players, so it must not quietly drop them at spawn and let them carry on playing.
        if (!isLobbyDestinationSet(lobbyWorld)) {
            return null;
        }
        World world = Bukkit.getWorld(lobbyWorld);
        if (world != null) {
            return world.getSpawnLocation();
        }
        return plugin.getSpawnManager().resolveCommandDestination(SpawnManager.AreaType.SPAWN);
    }

    public void setLobbyServer(String lobbyServer) {
        this.customLobbyServer = normalizeLobbyServer(lobbyServer);
        save();
    }

    public void initializeRedisListener() {
        if (plugin.getRedisManager() != null && plugin.getRedisManager().isEnabled()) {
            plugin.getRedisManager().subscribe(REDIS_MAINTENANCE_CHANNEL, this::handleIncomingRedisPayload);
        }
    }

    public void broadcastOnline() {
        if (plugin.getRedisManager() != null && plugin.getRedisManager().isConnected()) {
            String serverId = plugin.getConfigManager().getNetwork().getString("NETWORK.LOCAL_SERVER_ID", "local");
            plugin.getRedisManager().publish(REDIS_MAINTENANCE_CHANNEL, "online:" + serverId);
        }
    }

    public void startMaintenance() {
        startMaintenance(0L);
    }

    /**
     * @param durationMillis how long maintenance should last, or 0 to stay shut until someone runs
     *                       /maintenance off
     */
    public void startMaintenance(long durationMillis) {
        this.maintenanceEndsAt = durationMillis > 0L ? System.currentTimeMillis() + durationMillis : 0L;
        setMaintenanceActive(true);
        save();

        FileConfiguration config = plugin.getConfigManager().getNetwork();
        String bypassPerm = config.getString("MAINTENANCE.BYPASS_PERMISSION", "ULTIMATEDONUTSMP.ADMIN.MAINTENANCE.BYPASS");
        String enteringMessage = config.getString("MAINTENANCE.MESSAGES.ENTERING", "&d[Maintenance] &7server is entering maintenance. Moving you to the lobby...");
        String lobby = getLobbyServer();
        String localServerId = config.getString("NETWORK.LOCAL_SERVER_ID", "local");
        boolean useProxy = isUseProxy();
        Location localDestination = useProxy ? null : resolveLocalDestination();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(bypassPerm)) {
                String bypassJoinMsg = config.getString("MAINTENANCE.MESSAGES.BYPASS_JOIN", "&d[Maintenance] &7you joined while maintenance mode is active.");
                player.sendMessage(ColorUtils.toComponent(bypassJoinMsg));
                continue;
            }

            // Save player position
            Location loc = player.getLocation();
            if (loc.getWorld() != null) {
                plugin.getDatabaseManager().saveMaintenanceLocation(
                        player.getUniqueId(),
                        localServerId,
                        loc.getWorld().getName(),
                        loc.getX(),
                        loc.getY(),
                        loc.getZ(),
                        loc.getYaw(),
                        loc.getPitch()
                );
            }

            player.sendMessage(ColorUtils.toComponent(enteringMessage));
            if (useProxy) {
                sendToLobby(player, lobby);
            } else if (localDestination != null) {
                plugin.getSpigotScheduler().teleport(player, localDestination);
            }
        }

        // Kick whoever is still here 2 seconds later: a proxy handoff can fail, and a local server
        // with no lobby world set never had anywhere to move them to in the first place
        if (kicksLeftoverPlayers(useProxy, localDestination != null)) {
            plugin.getSpigotScheduler().runGlobalLater(() -> {
                String kickMessage = config.getString("MAINTENANCE.MESSAGES.KICK_FALLBACK", "&cThis server is in maintenance and no lobby is available.");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission(bypassPerm)) {
                        player.kickPlayer(ColorUtils.colorize(kickMessage));
                    }
                }
            }, 40L);
        }
    }

    public void stopMaintenance() {
        this.maintenanceEndsAt = 0L;
        setMaintenanceActive(false);
        save();
        broadcastOnline();
    }

    public void sendToLobby(Player player, String lobby) {
        if (player == null || lobby == null || lobby.isBlank()) {
            return;
        }

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteStream);
            out.writeUTF("Connect");
            out.writeUTF(lobby);
            player.sendPluginMessage(plugin, "BungeeCord", byteStream.toByteArray());
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to send BungeeCord connect packet for " + player.getName(), exception);
        }
    }

    private void handleIncomingRedisPayload(String payload) {
        if (payload == null || !payload.startsWith("online:")) {
            return;
        }

        String targetServerId = payload.substring(7);
        String localServerId = plugin.getConfigManager().getNetwork().getString("NETWORK.LOCAL_SERVER_ID", "local");
        if (localServerId.equalsIgnoreCase(targetServerId)) {
            return; // We are the server that just came online
        }

        // Retrieve players who have saved locations for that target server
        List<UUID> playerUuids = plugin.getDatabaseManager().getMaintenancePlayers(targetServerId);
        if (playerUuids.isEmpty()) {
            return;
        }

        for (UUID uuid : playerUuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                // Reconnect sequence
                startReconnectSequence(player, targetServerId);
            }
        }
    }

    private void startReconnectSequence(Player player, String targetServerId) {
        FileConfiguration config = plugin.getConfigManager().getNetwork();
        int delaySeconds = config.getInt("MAINTENANCE.RECONNECT_DELAY_SECONDS", 5);
        if (delaySeconds <= 0) {
            sendToLobby(player, targetServerId);
            return;
        }

        String titleMsg = config.getString("MAINTENANCE.MESSAGES.RECONNECTING_TITLE", "&a&lServer online");
        String subtitleMsg = config.getString("MAINTENANCE.MESSAGES.RECONNECTING_SUBTITLE", "&7Sending you back in %seconds% seconds...");

        final int[] countdown = {delaySeconds};
        final org.bukkit.scheduler.BukkitTask[] taskRef = new org.bukkit.scheduler.BukkitTask[1];
        taskRef[0] = plugin.getSpigotScheduler().runEntityTimer(player, () -> {
            if (!player.isOnline()) {
                if (taskRef[0] != null) {
                    taskRef[0].cancel();
                }
                return;
            }

            if (countdown[0] <= 0) {
                sendToLobby(player, targetServerId);
                if (taskRef[0] != null) {
                    taskRef[0].cancel();
                }
                return;
            }

            String subtitle = subtitleMsg.replace("%seconds%", String.valueOf(countdown[0]));
            TitleUtils.sendTitle(player, titleMsg, subtitle, 0, 25, 0);

            countdown[0]--;
        }, 0L, 20L);
    }
}
