package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NightVisionUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.TitleUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class PlayerJoinQuitListener implements Listener {

    static final long DEFAULT_FIRST_JOIN_SPAWN_DELAY_TICKS = 20L;
    static final long MAX_FIRST_JOIN_SPAWN_DELAY_TICKS = 1200L;
    private static final long FIRST_JOIN_SPAWN_RETRY_DELAY_TICKS = 20L;

    private final UltimateDonutSmp plugin;

    public PlayerJoinQuitListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(PlayerLoginEvent event) {
        if (plugin.getServerWipeManager() != null && plugin.getServerWipeManager().isMaintenanceMode()) {
            event.disallow(
                    PlayerLoginEvent.Result.KICK_OTHER,
                    ColorUtils.colorize(plugin.getServerWipeManager().getMaintenanceMessage())
            );
            return;
        }
        if (plugin.getMaintenanceManager() != null) {
            String bypassPerm = plugin.getConfigManager().getNetwork().getString("MAINTENANCE.BYPASS_PERMISSION", "ULTIMATEDONUTSMP.ADMIN.MAINTENANCE.BYPASS");
            if (deniesMaintenanceLogin(
                    plugin.getMaintenanceManager().isMaintenanceActive(),
                    event.getPlayer().hasPermission(bypassPerm),
                    plugin.getMaintenanceManager().hasLobbyDestination()
            )) {
                String kickMessage = plugin.getConfigManager().getNetwork().getString("MAINTENANCE.MESSAGES.KICK_FALLBACK", "&cThis server is in maintenance and no lobby is available.");
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ColorUtils.colorize(kickMessage));
                return;
            }
        }
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }

        UUID uuid = event.getPlayer().getUniqueId();
        if (uuid == null) {
            return;
        }

        String playerName = event.getPlayer().getName();
        PunishmentRecord blacklist = plugin.getPunishmentManager()
                .getActiveRecord(uuid, playerName, PunishmentType.BLACKLIST)
                .orElse(null);
        if (blacklist != null) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, ColorUtils.colorize(kickMessage(blacklist)));
            return;
        }

        PunishmentRecord ban = plugin.getPunishmentManager()
                .getActiveRecord(uuid, playerName, PunishmentType.BAN)
                .orElse(null);
        if (ban != null) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, ColorUtils.colorize(kickMessage(ban)));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String joinMsg = event.getJoinMessage();
        event.setJoinMessage(null);

        // 1. Maintenance checkmode
        if (plugin.getMaintenanceManager() != null && plugin.getMaintenanceManager().isMaintenanceActive()) {
            String bypassPerm = plugin.getConfigManager().getNetwork().getString("MAINTENANCE.BYPASS_PERMISSION", "ULTIMATEDONUTSMP.ADMIN.MAINTENANCE.BYPASS");
            if (!player.hasPermission(bypassPerm)) {
                boolean useProxy = plugin.getConfigManager().getNetwork().getBoolean("MAINTENANCE.USE_PROXY", true);
                String notAllowedMsg = plugin.getConfigManager().getNetwork().getString("MAINTENANCE.MESSAGES.NOT_ALLOWED", "&d[Maintenance] &cthis server is currently in maintenance. Redirecting to lobby...");
                player.sendMessage(ColorUtils.toComponent(notAllowedMsg));

                if (useProxy) {
                    String lobby = plugin.getMaintenanceManager().getLobbyServer();
                    plugin.getMaintenanceManager().sendToLobby(player, lobby);
                    event.setJoinMessage(null);

                    plugin.getSpigotScheduler().runEntityLater(player, () -> {
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

                    Location destination = plugin.getMaintenanceManager().resolveLocalDestination();
                    if (destination != null) {
                        plugin.getSpigotScheduler().runEntityLater(player, () -> {
                            if (player.isOnline()) {
                                plugin.getSpigotScheduler().teleport(player, destination);
                            }
                        }, 1L);
                    }
                }
                return;
            } else {
                String bypassJoinMsg = plugin.getConfigManager().getNetwork().getString("MAINTENANCE.MESSAGES.BYPASS_JOIN", "&d[Maintenance] &7you joined while maintenance mode is active.");
                player.sendMessage(ColorUtils.toComponent(bypassJoinMsg));
            }
        } else if (plugin.getMaintenanceManager() != null) {
            String localServerId = plugin.getConfigManager().getNetwork().getString("NETWORK.LOCAL_SERVER_ID", "local");
            org.bukkit.Location savedLoc = plugin.getDatabaseManager().getMaintenanceLocation(player.getUniqueId(), localServerId);
            if (savedLoc != null) {
                plugin.getSpigotScheduler().runEntityLater(player, () -> {
                    if (player.isOnline()) {
                        plugin.getSpigotScheduler().teleport(player, savedLoc).thenAccept(success -> {
                            plugin.getSpigotScheduler().runEntity(player, () -> {
                                if (player.isOnline()) {
                                    plugin.getDatabaseManager().deleteMaintenanceLocation(player.getUniqueId(), localServerId);
                                }
                            });
                        });
                    }
                }, 1L);
            }
        }

        plugin.getPlayerDataManager().loadOrCreate(player);
        NightVisionUtils.restoreIfEnabled(plugin, player);
        plugin.getShopManager().loadPreference(player.getUniqueId());
        plugin.getIgnoreManager().loadPlayer(player.getUniqueId());
        if (plugin.getFriendsManager() != null) {
            plugin.getFriendsManager().handleJoin(player);
        }
        if (player.getAddress() != null && player.getAddress().getAddress() != null) {
            plugin.getDatabaseManager().savePlayerIpAddressAsync(
                    player.getUniqueId(),
                    player.getAddress().getAddress().getHostAddress(),
                    System.currentTimeMillis()
            );
        }
        plugin.getKeyAllManager().handleJoin(player);
        if (plugin.getHideManager() != null) {
            plugin.getHideManager().handleJoin(player,
                    message -> player.sendMessage(ColorUtils.toComponent(message, player)));
        }

        // Load homes
        plugin.getHomeManager().loadHomes(player);

        // Setup scoreboard
        plugin.getScoreboardManager().setupPlayer(player);

        // Give the joining player a money nametag and show them the ones already out there
        plugin.getMoneyNametagManager().refreshViewer(player);
        plugin.getMoneyNametagManager().update(player);

        // Update tablist name
        plugin.getTablistManager().updateTablistName(player);
        plugin.getTablistManager().update(player);
        plugin.getTablistManager().refreshSkinHeads(player);

        // Track for AFK
        plugin.getAFKManager().trackPlayer(player);
        plugin.getShardManager().syncBooster(player);
        plugin.getAmethystToolsManager().sanitizePlayerInventory(player, true);
        plugin.getCrateVisualManager().handleJoin(player);
        plugin.getPortalManager().refreshHologramsSoon();
        plugin.getFreezeManager().handleJoin(player);
        plugin.getVoiceChatConsentManager().handleJoin(player);
        plugin.getStaffModeManager().handleJoin(player);
        plugin.getNetworkStaffChatManager().handleStaffJoin(player);
        if (plugin.getLunarRichPresenceManager() != null) {
            plugin.getLunarRichPresenceManager().handleJoin(player);
        }

        // Initialize cuboid-shard countdown so the player cannot receive shards
        // the instant they join – they must wait the full interval first.
        plugin.getShardManager().initCountdown(player.getUniqueId());
        plugin.getRtpZoneManager().clearState(player.getUniqueId());
        if (plugin.getFfaManager() != null) {
            plugin.getFfaManager().handleJoin(player);
        }
        if (plugin.getDuelManager() != null) {
            plugin.getDuelManager().handleJoin(player);
        }
        if (player.isInvulnerable()) {
            boolean inGodMode = plugin.getGodModeManager() != null && plugin.getGodModeManager().isInGodMode(player.getUniqueId());
            boolean inStaffMode = plugin.getStaffModeManager() != null && plugin.getStaffModeManager().isInStaffMode(player.getUniqueId());
            boolean inDuel = plugin.getDuelManager() != null && (plugin.getDuelManager().isTransitioning(player.getUniqueId()) || plugin.getDuelManager().isInDuel(player.getUniqueId()));
            boolean inFfa = plugin.getFfaManager() != null && plugin.getFfaManager().isInSession(player.getUniqueId());

            if (!inGodMode && !inStaffMode && !inDuel && !inFfa) {
                player.setInvulnerable(false);
            }
        }
        plugin.getPlayerVisibilityManager().handleJoin(player);

        boolean firstJoin = !player.hasPlayedBefore();
        if (firstJoin) {
            boolean randomSpawnStarted = plugin.getFirstJoinSpawnManager() != null
                    && plugin.getFirstJoinSpawnManager().handleFirstJoin(player);
            boolean spawnOnFirstJoin = plugin.getConfigManager().getConfig().getBoolean("SETTINGS.TELEPORT-SPAWN-ON-FIRST-JOIN", true);
            if (!randomSpawnStarted && spawnOnFirstJoin) {
                scheduleFirstJoinSpawnTeleport(player);
            }
        }
        if (plugin.getOrdersManager() != null) {
            plugin.getSpigotScheduler().runEntity(player, () -> {
                plugin.getOrdersManager().processAutoClaims(player);
            });
        }
        if (plugin.getAuctionHouseManager() != null) {
            plugin.getSpigotScheduler().runEntityLater(player, () -> {
                if (player.isOnline()) {
                    plugin.getAuctionHouseManager().processAutoClaims(player);
                }
            }, 20L);
        }

        if (plugin.getUpdateManager() != null && plugin.getUpdateManager().isUpdateAvailable()) {
            if (player.isOp() || player.hasPermission("ultimatedonutsmp.admin") || player.hasPermission("ultimatedonutsmp.updatechecker")) {
                plugin.getSpigotScheduler().runEntityLater(player, () -> {
                    if (player.isOnline()) {
                        String currentVer = plugin.getDescription().getVersion();
                        String latestVer = plugin.getUpdateManager().getLatestVersion();
                        player.sendMessage(ColorUtils.colorize("&8&m--------------------------------------------------", player));
                        player.sendMessage(ColorUtils.colorize("&9&lUltimateDonutSmp &7» &c&lA new update is available!", player));
                        player.sendMessage(ColorUtils.colorize("&7Current version: &c" + currentVer + " &8| &7Latest version: &a" + latestVer, player));
                        player.sendMessage(ColorUtils.colorize("&cPlease download the update from the official repository:", player));
                        player.sendMessage(ColorUtils.colorize("&bhttps://github.com/BeestoXd/UltimateDonutSMP", player));
                        player.sendMessage(ColorUtils.colorize("&8&m--------------------------------------------------", player));
                    }
                }, 40L);
            }
        }

        if (joinMsg != null && !joinMsg.isEmpty()) {
            String announcement = plugin.getServerNotificationManager() == null
                    ? null
                    : plugin.getServerNotificationManager().joinAnnouncement(player, firstJoin);
            broadcastJoinLeave(player, announcement == null ? joinMsg : announcement);
        }
    }

    /**
     * Sends a join or leave line to everyone allowed to see it. A configured announcement takes
     * the place of the server's own message and travels the same route, so a player who turned
     * join and leave messages off in /settings stays quiet either way. Nothing is sent when the
     * server had no message to begin with, which is how other plugins suppress a join.
     */
    private void broadcastJoinLeave(Player subject, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        plugin.getSpigotScheduler().forEachOnlinePlayer(p -> {
            if (shouldReceiveJoinLeaveMessage(p, subject)) {
                p.sendMessage(ColorUtils.toComponent(message));
            }
        });
    }

    /**
     * Sends a brand new player to the spawn location. The teleport waits for the player's
     * own scheduler instead of running inside the join event, because the server is still
     * placing the player in the world while that event runs and a teleport issued there is
     * dropped rather than applied.
     */
    private void scheduleFirstJoinSpawnTeleport(Player player) {
        long delay = firstJoinSpawnDelayTicks(plugin.getConfigManager().getConfig()
                .getLong("SETTINGS.FIRST-JOIN-SPAWN-DELAY-TICKS", DEFAULT_FIRST_JOIN_SPAWN_DELAY_TICKS));
        plugin.getSpigotScheduler().runEntityLater(player, () -> firstJoinSpawnTeleport(player, true), delay);
    }

    private void firstJoinSpawnTeleport(Player player, boolean retryOnFailure) {
        if (!player.isOnline()) {
            return;
        }

        String name = player.getName();
        Location spawn = plugin.getSpawnManager() == null ? null : plugin.getSpawnManager().getSpawnLocation();
        if (spawn == null) {
            plugin.getLogger().warning("SETTINGS.TELEPORT-SPAWN-ON-FIRST-JOIN is on but there is no spawn to send "
                    + name + " to. Run /setspawn, or check that the world named in LOCATIONS.SPAWN-LOCATION is loaded.");
            return;
        }

        plugin.getSpigotScheduler().teleport(player, spawn).thenAccept(success -> {
            if (Boolean.TRUE.equals(success)) {
                return;
            }
            if (retryOnFailure) {
                plugin.getSpigotScheduler().runEntityLater(player, () -> firstJoinSpawnTeleport(player, false),
                        FIRST_JOIN_SPAWN_RETRY_DELAY_TICKS);
                return;
            }
            plugin.getLogger().warning("Could not send " + name + " to the spawn location on first join.");
        });
    }

    static long firstJoinSpawnDelayTicks(long configured) {
        return Math.max(1L, Math.min(MAX_FIRST_JOIN_SPAWN_DELAY_TICKS, configured));
    }

    static boolean deniesMaintenanceLogin(boolean maintenanceActive, boolean hasBypass, boolean hasLobbyDestination) {
        return maintenanceActive && !hasBypass && !hasLobbyDestination;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (plugin.getHideManager() != null) {
            plugin.getHideManager().clearNametag(event.getPlayer().getUniqueId());
        }
        refreshHiddenNametag(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        refreshHiddenNametag(event.getPlayer());
    }

    private void refreshHiddenNametag(Player player) {
        if (plugin.getHideManager() == null) {
            return;
        }
        var state = plugin.getHideManager().getState(player.getUniqueId());
        if (plugin.getHideManager().usesObfuscatedText(state)) {
            plugin.getHideManager().refreshNametag(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String quitMsg = event.getQuitMessage();
        event.setQuitMessage(null);

        TitleUtils.cancelPendingReset(player.getUniqueId());
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

        // Clear combat tag
        plugin.getCombatManager().clearTag(player.getUniqueId());
        plugin.getVoiceChatConsentManager().handleQuit(player.getUniqueId());

        // Cancel any pending teleport
        plugin.getTeleportManager().cancel(player.getUniqueId());

        // Remove pending TPA requests
        plugin.getTPAManager().removeRequest(player.getUniqueId());
        plugin.getTPAManager().clearQueuedRequestsForTarget(player.getUniqueId());
        plugin.getTPAManager().cancelRequestsByRequester(player.getUniqueId());

        // Remove temporary worth lore before the inventory is persisted by the server
        plugin.getWorthManager().clearWorthDisplay(player);

        // Save and unload player data
        plugin.getPlayerDataManager().unload(player.getUniqueId());

        // Unload homes
        plugin.getHomeManager().unloadHomes(player.getUniqueId());

        // Remove the money nametag before the player entity goes away
        plugin.getMoneyNametagManager().remove(player.getUniqueId());

        // Remove scoreboard
        plugin.getScoreboardManager().removePlayer(player.getUniqueId());
        plugin.getTablistManager().removePlayer(player.getUniqueId());

        // Remove AFK tracking
        plugin.getAFKManager().removePlayer(player.getUniqueId());

        // Clean up cuboid-shard countdown state
        plugin.getShardManager().removeCountdown(player.getUniqueId());
        plugin.getShardManager().clearBoosterCache(player.getUniqueId());
        plugin.getRtpZoneManager().clearState(player);
        plugin.getRtpManager().clearSearch(player.getUniqueId());
        if (plugin.getRtpQueueManager() != null) {
            plugin.getRtpQueueManager().handleQuit(player.getUniqueId());
        }
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
        if (plugin.getFriendsManager() != null) {
            plugin.getFriendsManager().handleQuit(player);
        }
        if (plugin.getHideManager() != null) {
            plugin.getHideManager().handleQuit(player.getUniqueId());
        }
        if (plugin.getAuctionHouseManager() != null) {
            plugin.getAuctionHouseManager().cleanupPlayer(player.getUniqueId());
        }
        plugin.getShopManager().cleanupPlayer(player.getUniqueId());
        if (plugin.getOrdersManager() != null) {
            plugin.getOrdersManager().forgetUiState(player.getUniqueId());
        }
        plugin.getPlayerVisibilityManager().clearPlayer(player.getUniqueId());

        // Remove team chat
        plugin.getTeamManager().setTeamChat(player.getUniqueId(), false);
        plugin.getTeamManager().clearSearchState(player.getUniqueId());

        if (quitMsg != null && !quitMsg.isEmpty()) {
            String announcement = plugin.getServerNotificationManager() == null
                    ? null
                    : plugin.getServerNotificationManager().leaveAnnouncement(player);
            broadcastJoinLeave(player, announcement == null ? quitMsg : announcement);
        }
    }

    /**
     * A vanished joiner stays hidden from anyone without the permission to see them. Past that,
     * everyone online reads the feed unless they muted it in /settings. Narrowing it to players who
     * follow the joiner emptied it instead: the server's own join and leave message is cleared and
     * resent through here, so on a server where nobody has followed anybody it reached no one.
     */
    private boolean shouldReceiveJoinLeaveMessage(Player receiver, Player joiner) {
        if (plugin.getStaffModeManager() != null && plugin.getStaffModeManager().isVanished(joiner.getUniqueId())) {
            if (!PermissionUtils.has(receiver, plugin.getStaffModeManager().getSeeVanishedPermission())) {
                return false;
            }
        }
        PlayerData receiverData = plugin.getPlayerDataManager().get(receiver);
        return receiverData == null || receiverData.isJoinLeaveMessagesEnabled();
    }

    private String kickMessage(PunishmentRecord record) {
        return plugin.getConfigManager().getMessageOrDefault(
                record.getType() == PunishmentType.BLACKLIST ? "PUNISHMENTS.BLACKLIST" : "PUNISHMENTS.BAN",
                record.getType() == PunishmentType.BLACKLIST
                        ? "&4&lYou have been blacklisted!\n&8&m----------------------------\n&7Reason: &f%reason%\n&7Blacklisted by: &f%issuer%\n&8&m----------------------------\n&4You cannot join the server"
                        : "&c&lYou have been banned!\n&8&m----------------------------\n&7Reason: &f%reason%\n&7Expires: &f%nicest_expiration%\n&7Banned by: &f%issuer%\n&8&m----------------------------\n&7Appeal at: &fDiscord.example.space",
                punishmentPlaceholders(record)
        );
    }

    private String[] punishmentPlaceholders(PunishmentRecord record) {
        String expires = formatExpires(record);
        String issuer = formatIssuer(record);
        String reason = record == null || record.getReason() == null ? "" : record.getReason();
        String player = record == null || record.getTargetNameSnapshot() == null ? "" : record.getTargetNameSnapshot();
        String id = record == null ? "" : String.valueOf(record.getId());
        String type = record == null || record.getType() == null ? "" : record.getType().name();

        return new String[]{
                "%reason%", reason,
                "{reason}", reason,

                "%nicest_expiration%", expires,
                "{nicest_expiration}", expires,
                "%expires%", expires,
                "{expires}", expires,
                "%expires_at%", expires,
                "{expires_at}", expires,
                "%expiration%", expires,
                "{expiration}", expires,
                "%expiry%", expires,
                "{expiry}", expires,
                "%duration%", expires,
                "{duration}", expires,

                "%issuer%", issuer,
                "{issuer}", issuer,
                "%staff%", issuer,
                "{staff}", issuer,
                "%by%", issuer,
                "{by}", issuer,

                "%player%", player,
                "{player}", player,
                "%target%", player,
                "{target}", player,

                "%id%", id,
                "{id}", id,

                "%type%", type,
                "{type}", type
        };
    }

    private String formatExpires(PunishmentRecord record) {
        if (record == null || record.getExpiresAt() == null) {
            return "Permanent";
        }
        long remainingSeconds = Math.max(0L, (record.getExpiresAt() - System.currentTimeMillis()) / 1000L);
        if (remainingSeconds <= 0L) {
            return "Expired";
        }
        if (plugin != null && plugin.getLanguageManager() != null) {
            return plugin.getLanguageManager().formatDuration(remainingSeconds, true);
        }
        return NumberUtils.formatCountdown(remainingSeconds);
    }

    private String formatIssuer(PunishmentRecord record) {
        if (record == null) return "unknown";
        String issuer = record.getIssuerNameSnapshot();
        return issuer == null || issuer.isBlank() ? "unknown" : issuer;
    }
}
