package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ChatManager;
import com.bx.ultimateDonutSmp.managers.FeatureManager;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final UltimateDonutSmp plugin;

    public ChatListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatManager chatManager = plugin.getChatManager();

        String rawMessage = event.getMessage();

        if (plugin.getHomeManager().hasPendingInput(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getSpigotScheduler().runEntity(player, () ->
                    plugin.getHomeManager().handlePendingInput(player, rawMessage));
            return;
        }

        if (plugin.getTeamManager().hasPendingSearchInput(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getSpigotScheduler().runEntity(player, () ->
                    plugin.getTeamManager().handlePendingSearchInput(player, rawMessage));
            return;
        }

        if (plugin.getOrdersManager() != null && plugin.getOrdersManager().hasPendingInput(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getSpigotScheduler().runEntity(player, () ->
                    plugin.getOrdersManager().handlePendingInput(player, rawMessage));
            return;
        }



        PunishmentRecord activeMute = plugin.getPunishmentManager()
                .getActiveRecord(player.getUniqueId(), player.getName(), PunishmentType.MUTE)
                .orElse(null);
        if (activeMute != null) {
            event.setCancelled(true);
            plugin.getSpigotScheduler().runEntity(player, () ->
                    player.sendMessage(ColorUtils.toComponent(mutedChatMessage(activeMute))));
            return;
        }

        // Team chat check
        if (plugin.getTeamManager().isTeamChatEnabled(player.getUniqueId())) {
            event.setCancelled(true);
            Team team = plugin.getTeamManager().getTeam(player);
            if (team == null) {
                plugin.getTeamManager().setTeamChat(player.getUniqueId(), false);
                plugin.getSpigotScheduler().runEntity(player, () ->
                        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.NO-TEAM"))));
                return;
            }
            if (!plugin.getTeamManager().canUseTeamChat(team, player.getUniqueId())) {
                plugin.getTeamManager().setTeamChat(player.getUniqueId(), false);
                plugin.getSpigotScheduler().runEntity(player, () ->
                        player.sendMessage(ColorUtils.toComponent(
                                plugin.getConfigManager().getMessage("TEAM.NO-TEAM-CHAT-PERMISSION"))));
                return;
            }
            String teamFormat = "&8[&b" + team.getName().toUpperCase() + "&8] &7%player%&8: &f%message%";
            var component = plugin.getHoverStatsManager().buildChatComponent(player, "", rawMessage, teamFormat);
            for (java.util.UUID uuid : team.getMemberUuids()) {
                Player member = Bukkit.getPlayer(uuid);
                if (member != null) {
                    plugin.getSpigotScheduler().runEntity(member, () -> member.spigot().sendMessage(component));
                }
            }
            return;
        }

        if (!plugin.getFeatureManager().isEnabled(FeatureManager.Feature.CHAT)) {
            return;
        }

        if (chatManager.isGlobalChatMuted() && !chatManager.isMuteBypassed(player)) {
            event.setCancelled(true);
            plugin.getSpigotScheduler().runEntity(player, () -> player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "CHAT-MANAGER.GLOBAL-MUTED-BLOCK",
                            "&cɢʟᴏʙᴀʟ ᴄʜᴀᴛ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴍᴜᴛᴇᴅ."
                    )
            )));
            return;
        }

        ChatManager.FilterResult filterResult = chatManager.validateGlobalMessage(player, rawMessage);
        if (!filterResult.allowed()) {
            event.setCancelled(true);
            plugin.getSpigotScheduler().runEntity(player, () -> player.sendMessage(ColorUtils.toComponent(filterResult.blockMessage())));
            return;
        }

        ChatManager.DelayResult delayResult = chatManager.checkAndTrackDelay(player);
        if (!delayResult.allowed()) {
            event.setCancelled(true);
            String delayMessage = plugin.getConfigManager().getMessageOrDefault(
                    "CHAT-MANAGER.GLOBAL-DELAY-BLOCK",
                    "&cʏᴏᴜ ᴍᴜѕᴛ ᴡᴀɪᴛ &f{seconds}ѕ &cʙᴇꜰᴏʀᴇ ᴄʜᴀᴛᴛɪɴɢ ᴀɢᴀɪɴ."
            ).replace("{seconds}", String.valueOf(delayResult.remainingSeconds()))
                    .replace("%seconds%", String.valueOf(delayResult.remainingSeconds()));
            plugin.getSpigotScheduler().runEntity(player, () -> player.sendMessage(ColorUtils.toComponent(delayMessage)));
            return;
        }

        if (!chatManager.isFormatEnabled()) {
            chatManager.trackAcceptedGlobalMessage(player, rawMessage);
            return;
        }

        event.setCancelled(true);

        String chatFormat = chatManager.getChatFormat();
        String prefix = resolvePrefix(player);
        var chatComponent = plugin.getHoverStatsManager()
                .buildChatComponent(player, prefix, rawMessage, chatFormat);

        final var finalMsg = chatComponent;
        plugin.getSpigotScheduler().forEachOnlinePlayer(p -> {
            if (PlayerSettingUtils.notificationEnabled(plugin, p, PlayerSettingUtils.NotificationChannel.PUBLIC_CHAT)) {
                p.spigot().sendMessage(finalMsg);
            }
        });
        chatManager.trackAcceptedGlobalMessage(player, rawMessage);
    }

    private String resolvePrefix(Player player) {
        if (ColorUtils.hasPAPI()) {
            try {
                String prefix = me.clip.placeholderapi.PlaceholderAPI
                        .setPlaceholders(player, "%luckperms_prefix%");
                if (prefix != null && !prefix.isBlank() && !prefix.startsWith("%")) {
                    return prefix;
                }
                prefix = me.clip.placeholderapi.PlaceholderAPI
                        .setPlaceholders(player, "%vault_prefix%");
                if (prefix != null && !prefix.isBlank() && !prefix.startsWith("%")) {
                    return prefix;
                }
                prefix = me.clip.placeholderapi.PlaceholderAPI
                        .setPlaceholders(player, "%prefix%");
                if (prefix != null && !prefix.isBlank() && !prefix.startsWith("%")) {
                    return prefix;
                }
            } catch (Exception ignored) {
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            try {
                org.bukkit.plugin.RegisteredServiceProvider<net.milkbowl.vault.chat.Chat> rsp =
                        Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
                if (rsp != null && rsp.getProvider() != null) {
                    String prefix = rsp.getProvider().getPlayerPrefix(player);
                    if (prefix != null && !prefix.isBlank()) {
                        return prefix;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    private String mutedChatMessage(PunishmentRecord record) {
        return plugin.getConfigManager().getMessageOrDefault(
                "PUNISHMENTS.MUTE",
                "&c&lʏᴏᴜ ʜᴀᴠᴇ ʙᴇᴇɴ ᴍᴜᴛᴇᴅ!\n&8&m----------------------------\n&7ʀᴇᴀѕᴏɴ: &f%reason%\n&7ᴇxᴘɪʀᴇѕ: &f%nicest_expiration%\n&7ᴍᴜᴛᴇᴅ ʙʏ: &f%issuer%\n&8&m----------------------------\n&7ʏᴏᴜ ᴄᴀɴɴᴏᴛ ѕᴘᴇᴀᴋ ɪɴ ᴄʜᴀᴛ",
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
