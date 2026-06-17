package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerConnection;
import io.papermc.paper.connection.PlayerLoginConnection;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerJoinQuitListener implements Listener {

    private final UltimateDonutSmp plugin;

    public PlayerJoinQuitListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onValidateLogin(PlayerConnectionValidateLoginEvent event) {
        if (!event.isAllowed()) {
            return;
        }

        UUID uuid = resolveLoginUuid(event.getConnection());
        if (uuid == null) {
            return;
        }

        PunishmentRecord blacklist = plugin.getPunishmentManager()
                .getActiveRecord(uuid, PunishmentType.BLACKLIST)
                .orElse(null);
        if (blacklist != null) {
            event.kickMessage(ColorUtils.toComponent(kickMessage(blacklist)));
            return;
        }

        PunishmentRecord ban = plugin.getPunishmentManager()
                .getActiveRecord(uuid, PunishmentType.BAN)
                .orElse(null);
        if (ban != null) {
            event.kickMessage(ColorUtils.toComponent(kickMessage(ban)));
        }
    }

    private UUID resolveLoginUuid(PlayerConnection connection) {
        if (connection instanceof PlayerConfigurationConnection configurationConnection) {
            return configurationConnection.getProfile().getId();
        }
        if (connection instanceof PlayerLoginConnection loginConnection) {
            PlayerProfile profile = loginConnection.getAuthenticatedProfile();
            if (profile == null) {
                profile = loginConnection.getUnsafeProfile();
            }
            return profile == null ? null : profile.getId();
        }
        return null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check maintenance mode
        if (plugin.getMaintenanceManager() != null && plugin.getMaintenanceManager().isMaintenanceActive()) {
            String bypassPerm = plugin.getConfigManager().getNetwork().getString("MAINTENANCE.BYPASS_PERMISSION", "ultimatedonutsmp.admin.maintenance.bypass");
            if (!player.hasPermission(bypassPerm)) {
                boolean useProxy = plugin.getConfigManager().getNetwork().getBoolean("MAINTENANCE.USE_PROXY", true);
                String notAllowedMsg = plugin.getConfigManager().getNetwork().getString("MAINTENANCE.MESSAGES.NOT_ALLOWED", "&d[Maintenance] &cThis server is currently in maintenance. Redirecting to lobby...");
                player.sendMessage(ColorUtils.toComponent(notAllowedMsg));
                
                if (useProxy) {
                    String lobby = plugin.getMaintenanceManager().getLobbyServer();
                    plugin.getMaintenanceManager().sendToLobby(player, lobby);
                    event.setJoinMessage(null);
                    
                    plugin.getFoliaScheduler().runEntityLater(player, () -> {
                        if (player.isOnline()) {
                            String kickMessage = plugin.getConfigManager().getNetwork().getString("MAINTENANCE.MESSAGES.KICK_FALLBACK", "&cThis server is in maintenance and no lobby is available.");
                            player.kickPlayer(ColorUtils.colorize(kickMessage));
                        }
                    }, 40L);
                } else {
                    // Local server: Teleport them to the lobby world spawn
                    String localServerId = plugin.getConfigManager().getNetwork().getString("NETWORK.LOCAL_SERVER_ID", "local");
                    if (plugin.getDatabaseManager().getMaintenanceLocation(player.getUniqueId(), localServerId) == null) {
                        org.bukkit.Location loc = player.getLocation();
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
                    }
                    
                    String lobbyWorld = plugin.getConfigManager().getNetwork().getString("MAINTENANCE.LOBBY_WORLD", "world");
                    org.bukkit.World world = Bukkit.getWorld(lobbyWorld);
                    if (world != null) {
                        plugin.getFoliaScheduler().teleport(player, world.getSpawnLocation());
                    }
                }
                return;
            } else {
                String bypassJoinMsg = plugin.getConfigManager().getNetwork().getString("MAINTENANCE.MESSAGES.BYPASS_JOIN", "&d[Maintenance] &7You joined while maintenance mode is active.");
                player.sendMessage(ColorUtils.toComponent(bypassJoinMsg));
            }
        } else if (plugin.getMaintenanceManager() != null) {
            String localServerId = plugin.getConfigManager().getNetwork().getString("NETWORK.LOCAL_SERVER_ID", "local");
            Location savedLoc = plugin.getDatabaseManager().getMaintenanceLocation(player.getUniqueId(), localServerId);
            if (savedLoc != null) {
                plugin.getFoliaScheduler().teleport(player, savedLoc).thenAccept(success -> {
                    if (success) {
                        plugin.getFoliaScheduler().runAsync(() -> {
                            plugin.getDatabaseManager().deleteMaintenanceLocation(player.getUniqueId(), localServerId);
                        });
                    }
                });
            }
        }

        // load player data
        plugin.getPlayerDataManager().loadOrCreate(player);
        plugin.getIgnoreManager().loadPlayer(player.getUniqueId());
        if (player.getAddress() != null && player.getAddress().getAddress() != null) {
            plugin.getDatabaseManager().savePlayerIpAddress(
                    player.getUniqueId(),
                    player.getAddress().getAddress().getHostAddress(),
                    System.currentTimeMillis()
            );
        }
        plugin.getKeyAllManager().handleJoin(player);

        // load homes
        plugin.getHomeManager().loadHomes(player);

        // setup scoreboard
        plugin.getScoreboardManager().setupPlayer(player);

        // update tablist name
        plugin.getTablistManager().updateTablistName(player);
        plugin.getTablistManager().update(player);

        // track for AFK
        plugin.getAFKManager().trackPlayer(player);
        plugin.getShardManager().syncBooster(player);
        plugin.getAmethystToolsManager().sanitizePlayerInventory(player, true);
        plugin.getCrateVisualManager().handleJoin(player);
        plugin.getPortalManager().refreshHologramsSoon();
        plugin.getFreezeManager().handleJoin(player);
        plugin.getStaffModeManager().handleJoin(player);
        plugin.getNetworkStaffChatManager().handleStaffJoin(player);
        if (plugin.getLunarRichPresenceManager() != null) {
            plugin.getLunarRichPresenceManager().handleJoin(player);
        }

        // initialize cuboid-shard countdown so the player cannot receive shards
        // the instant they join – they must wait the full interval first.
        plugin.getShardManager().initCountdown(player.getUniqueId());
        plugin.getRtpZoneManager().clearState(player.getUniqueId());
        if (plugin.getFfaManager() != null) {
            plugin.getFfaManager().handleJoin(player);
        }
        if (plugin.getDuelManager() != null) {
            plugin.getDuelManager().handleJoin(player);
        }

        if (!player.hasPlayedBefore()) {
            boolean spawnOnFirstJoin = plugin.getConfigManager().getConfig().getBoolean("SETTINGS.TELEPORT-SPAWN-ON-FIRST-JOIN", true);
            if (spawnOnFirstJoin && plugin.getSpawnManager().hasSpawn()) {
                Location spawn = plugin.getSpawnManager().getSpawnLocation();
                if (spawn != null) {
                    plugin.getFoliaScheduler().teleport(player, spawn);
                }
            }
        }

        // hide join message (optional, uncomment to suppress)
        // event.joinMessage(null);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getNetworkStaffChatManager().handleStaffLeave(player);
        plugin.getNetworkStaffChatManager().clearPlayerState(player.getUniqueId());
        plugin.getNetworkStaffAlertManager().clearPlayerState(player.getUniqueId());
        if (plugin.getLunarRichPresenceManager() != null) {
            plugin.getLunarRichPresenceManager().handleQuit(player);
        }

        if (plugin.getDuelManager() != null) {
            plugin.getDuelManager().handleQuit(player);
        }
        if (plugin.getFfaManager() != null) {
            plugin.getFfaManager().handleQuit(player);
        }

        // clear combat tag
        plugin.getCombatManager().clearTag(player.getUniqueId());

        // cancel any pending teleport
        plugin.getTeleportManager().cancel(player.getUniqueId());

        // remove pending tpa requests
        plugin.getTPAManager().removeRequest(player.getUniqueId());
        plugin.getTPAManager().clearQueuedRequestsForTarget(player.getUniqueId());
        plugin.getTPAManager().cancelRequestsByRequester(player.getUniqueId());

        // remove temporary worth lore before the inventory is persisted by the server
        plugin.getWorthManager().clearWorthDisplay(player);

        // save and unload player data
        plugin.getPlayerDataManager().unload(player.getUniqueId());

        // unload homes
        plugin.getHomeManager().unloadHomes(player.getUniqueId());

        // remove scoreboard
        plugin.getScoreboardManager().removePlayer(player.getUniqueId());

        // remove afk tracking
        plugin.getAFKManager().removePlayer(player.getUniqueId());

        // clean up cuboid-shard countdown state
        plugin.getShardManager().removeCountdown(player.getUniqueId());
        plugin.getShardManager().clearBoosterCache(player.getUniqueId());
        plugin.getRtpZoneManager().clearState(player.getUniqueId());
        plugin.getRtpManager().clearSearch(player.getUniqueId());
        plugin.getPortalManager().clearPlayerState(player.getUniqueId());
        plugin.getPortalManager().refreshHologramsSoon();
        plugin.getCrateManager().clearSession(player.getUniqueId());
        plugin.getCrateManager().clearPendingBind(player.getUniqueId());
        plugin.getCrateManager().unloadKeyBalanceCache(player.getUniqueId());
        plugin.getCrateVisualManager().handleQuit(player.getUniqueId());
        plugin.getFreezeManager().handleQuit(player);
        plugin.getStaffModeManager().handleQuit(player);
        plugin.getChatManager().clearPlayerState(player.getUniqueId());
        plugin.getPrivateMessageManager().clearPlayer(player.getUniqueId());
        plugin.getIgnoreManager().unloadPlayer(player.getUniqueId());

        // remove team chat
        plugin.getTeamManager().setTeamChat(player.getUniqueId(), false);
        plugin.getTeamManager().clearSearchState(player.getUniqueId());
    }

    private String kickMessage(PunishmentRecord record) {
        return plugin.getConfigManager().getMessageOrDefault(
                record.getType() == PunishmentType.BLACKLIST ? "PUNISHMENTS.BLACKLIST" : "PUNISHMENTS.BAN",
                record.getType() == PunishmentType.BLACKLIST
                        ? "&4&lʏᴏᴜ ʜᴀᴠᴇ ʙᴇᴇɴ ʙʟᴀᴄᴋʟɪѕᴛᴇᴅ!\n&8&m----------------------------\n&7ʀᴇᴀѕᴏɴ: &f%reason%\n&7ʙʟᴀᴄᴋʟɪѕᴛᴇᴅ ʙʏ: &f%issuer%\n&8&m----------------------------\n&4ʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴊᴏɪɴ ᴛʜᴇ ѕᴇʀᴠᴇʀ"
                        : "&c&lʏᴏᴜ ʜᴀᴠᴇ ʙᴇᴇɴ ʙᴀɴɴᴇᴅ!\n&8&m----------------------------\n&7ʀᴇᴀѕᴏɴ: &f%reason%\n&7ᴇxᴘɪʀᴇѕ: &f%nicest_expiration%\n&7ʙᴀɴɴᴇᴅ ʙʏ: &f%issuer%\n&8&m----------------------------\n&7ᴀᴘᴘᴇᴀʟ ᴀᴛ: &fdiscord.example.space",
                "%reason%", record.getReason(),
                "%nicest_expiration%", formatExpires(record),
                "%issuer%", formatIssuer(record),
                "{reason}", record.getReason(),
                "{expires}", formatExpires(record),
                "{issuer}", formatIssuer(record)
        );
    }

    private String formatExpires(PunishmentRecord record) {
        if (record.getExpiresAt() == null) {
            return "Never";
        }

        long remainingSeconds = Math.max(0L, (record.getExpiresAt() - System.currentTimeMillis()) / 1000L);
        return NumberUtils.formatCountdown(remainingSeconds);
    }

    private String formatIssuer(PunishmentRecord record) {
        String issuer = record.getIssuerNameSnapshot();
        return issuer == null || issuer.isBlank() ? "ᴜɴᴋɴᴏᴡɴ" : issuer;
    }
}
