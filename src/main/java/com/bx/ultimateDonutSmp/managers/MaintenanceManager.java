package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class MaintenanceManager {

    private static final String REDIS_MAINTENANCE_CHANNEL = "ultimatedonutsmp:maintenance";

    private final UltimateDonutSmp plugin;
    private final File stateFile;
    private boolean maintenanceActive;
    private String customLobbyServer;

    public MaintenanceManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.stateFile = new File(plugin.getDataFolder(), "maintenance-state.yml");
        load();
    }

    public void load() {
        if (!stateFile.exists()) {
            this.maintenanceActive = false;
            this.customLobbyServer = null;
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(stateFile);
        this.maintenanceActive = config.getBoolean("active", false);
        this.customLobbyServer = config.getString("lobby", null);
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("active", maintenanceActive);
        if (customLobbyServer != null) {
            config.set("lobby", customLobbyServer);
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

    public void setLobbyServer(String lobbyServer) {
        this.customLobbyServer = lobbyServer;
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
        setMaintenanceActive(true);
        save();

        FileConfiguration config = plugin.getConfigManager().getNetwork();
        String bypassPerm = config.getString("MAINTENANCE.BYPASS_PERMISSION", "ULTIMATEDONUTSMP.ADMIN.MAINTENANCE.BYPASS");
        String enteringMessage = config.getString("MAINTENANCE.MESSAGES.ENTERING", "&d[ᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ] &7ѕᴇʀᴠᴇʀ ɪѕ ᴇɴᴛᴇʀɪɴɢ ᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ. ᴍᴏᴠɪɴɢ ʏᴏᴜ ᴛᴏ ᴛʜᴇ ʟᴏʙʙʏ...");
        String lobby = getLobbyServer();
        String localServerId = config.getString("NETWORK.LOCAL_SERVER_ID", "local");
        boolean useProxy = isUseProxy();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(bypassPerm)) {
                String bypassJoinMsg = config.getString("MAINTENANCE.MESSAGES.BYPASS_JOIN", "&d[ᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ] &7ʏᴏᴜ ᴊᴏɪɴᴇᴅ ᴡʜɪʟᴇ ᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ ᴍᴏᴅᴇ ɪѕ ᴀᴄᴛɪᴠᴇ.");
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
            } else {
                String worldName = getLobbyWorld();
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    plugin.getSpigotScheduler().teleport(player, world.getSpawnLocation());
                } else {
                    Location defaultSpawn = plugin.getSpawnManager().resolveCommandDestination(SpawnManager.AreaType.SPAWN);
                    if (defaultSpawn != null) {
                        plugin.getSpigotScheduler().teleport(player, defaultSpawn);
                    }
                }
            }
        }

        // Kick players who failed to transfer after 2 seconds (only in proxy mode)
        if (useProxy) {
            plugin.getSpigotScheduler().runGlobalLater(() -> {
                String kickMessage = config.getString("MAINTENANCE.MESSAGES.KICK_FALLBACK", "&cᴛʜɪѕ ѕᴇʀᴠᴇʀ ɪѕ ɪɴ ᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ ᴀɴᴅ ɴᴏ ʟᴏʙʙʏ ɪѕ ᴀᴠᴀɪʟᴀʙʟᴇ.");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission(bypassPerm)) {
                        player.kickPlayer(ColorUtils.colorize(kickMessage));
                    }
                }
            }, 40L);
        }
    }

    public void stopMaintenance() {
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

        String titleMsg = config.getString("MAINTENANCE.MESSAGES.RECONNECTING_TITLE", "&a&lѕᴇʀᴠᴇʀ ᴏɴʟɪɴᴇ");
        String subtitleMsg = config.getString("MAINTENANCE.MESSAGES.RECONNECTING_SUBTITLE", "&7ѕᴇɴᴅɪɴɢ ʏᴏᴜ ʙᴀᴄᴋ ɪɴ %seconds% ѕᴇᴄᴏɴᴅѕ...");

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
            player.sendTitle(
                    ColorUtils.colorize(titleMsg),
                    ColorUtils.colorize(subtitle),
                    0, 25, 0
            );

            countdown[0]--;
        }, 0L, 20L);
    }
}
