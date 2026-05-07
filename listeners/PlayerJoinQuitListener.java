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

        // Load player data
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

        // Load homes
        plugin.getHomeManager().loadHomes(player);

        // Setup scoreboard
        plugin.getScoreboardManager().setupPlayer(player);

        // Update tablist name
        plugin.getTablistManager().updateTablistName(player);
        plugin.getTablistManager().update(player);

        // Track for AFK
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

        // Hide join message (optional, uncomment to suppress)
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

        // Clear combat tag
        plugin.getCombatManager().clearTag(player.getUniqueId());

        // Cancel any pending teleport
        plugin.getTeleportManager().cancel(player.getUniqueId());

        // Remove pending TPA requests
        plugin.getTPAManager().removeRequest(player.getUniqueId());
        plugin.getTPAManager().cancelRequestsByRequester(player.getUniqueId());

        // Remove temporary worth lore before the inventory is persisted by the server
        plugin.getWorthManager().clearWorthDisplay(player);

        // Save and unload player data
        plugin.getPlayerDataManager().unload(player.getUniqueId());

        // Unload homes
        plugin.getHomeManager().unloadHomes(player.getUniqueId());

        // Remove scoreboard
        plugin.getScoreboardManager().removePlayer(player.getUniqueId());

        // Remove AFK tracking
        plugin.getAFKManager().removePlayer(player.getUniqueId());

        // Clean up cuboid-shard countdown state
        plugin.getShardManager().removeCountdown(player.getUniqueId());
        plugin.getShardManager().clearBoosterCache(player.getUniqueId());
        plugin.getRtpZoneManager().clearState(player.getUniqueId());
        plugin.getRtpManager().clearSearch(player.getUniqueId());
        plugin.getPortalManager().clearPlayerState(player.getUniqueId());
        plugin.getPortalManager().refreshHologramsSoon();
        plugin.getCrateManager().clearSession(player.getUniqueId());
        plugin.getCrateManager().clearPendingBind(player.getUniqueId());
        plugin.getCrateVisualManager().handleQuit(player.getUniqueId());
        plugin.getFreezeManager().handleQuit(player);
        plugin.getStaffModeManager().handleQuit(player);
        plugin.getChatManager().clearPlayerState(player.getUniqueId());
        plugin.getPrivateMessageManager().clearPlayer(player.getUniqueId());
        plugin.getIgnoreManager().unloadPlayer(player.getUniqueId());

        // Remove team chat
        plugin.getTeamManager().setTeamChat(player.getUniqueId(), false);
        plugin.getTeamManager().clearSearchState(player.getUniqueId());
    }

    private String kickMessage(PunishmentRecord record) {
        return plugin.getConfigManager().getMessageOrDefault(
                record.getType() == PunishmentType.BLACKLIST ? "PUNISHMENTS.BLACKLIST" : "PUNISHMENTS.BAN",
                record.getType() == PunishmentType.BLACKLIST
                        ? "&4&lYOU HAVE BEEN BLACKLISTED!\n&8&m----------------------------\n&7Reason: &f%reason%\n&7Blacklisted by: &f%issuer%\n&8&m----------------------------\n&4You cannot join the server"
                        : "&c&lYou have been banned!\n&8&m----------------------------\n&7Reason: &f%reason%\n&7Expires: &f%nicest_expiration%\n&7Banned by: &f%issuer%\n&8&m----------------------------\n&7Appeal at: &fdiscord.example.space",
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
        return issuer == null || issuer.isBlank() ? "Unknown" : issuer;
    }
}
