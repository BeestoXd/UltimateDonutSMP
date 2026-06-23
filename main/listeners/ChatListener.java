package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ChatManager;
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
                .getActiveRecord(player.getUniqueId(), PunishmentType.MUTE)
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
                if (member != null
                        && (member.getUniqueId().equals(player.getUniqueId())
                        || PlayerSettingUtils.notificationEnabled(
                        plugin,
                        member,
                        PlayerSettingUtils.NotificationChannel.TEAM_CHAT
                ))) {
                    plugin.getSpigotScheduler().runEntity(member, () -> member.spigot().sendMessage(component));
                }
            }
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
        String prefix = getLuckPermsPrefix(player);
        var chatComponent = plugin.getHoverStatsManager()
                .buildChatComponent(player, prefix, rawMessage, chatFormat);

        final var finalMsg = chatComponent;
        plugin.getSpigotScheduler().forEachOnlinePlayer(recipient -> {
            if (recipient.getUniqueId().equals(player.getUniqueId())
                    || PlayerSettingUtils.notificationEnabled(
                    plugin,
                    recipient,
                    PlayerSettingUtils.NotificationChannel.PUBLIC_CHAT
            )) {
                recipient.spigot().sendMessage(finalMsg);
            }
        });
        chatManager.trackAcceptedGlobalMessage(player, rawMessage);
    }

    private String getLuckPermsPrefix(Player player) {
        if (!ColorUtils.hasPAPI()) return "";
        try {
            String prefix = me.clip.placeholderapi.PlaceholderAPI
                    .setPlaceholders(player, "%luckperms_prefix%");
            return prefix.startsWith("%") ? "" : prefix;
        } catch (Exception e) {
            return "";
        }
    }

    private String mutedChatMessage(PunishmentRecord record) {
        return plugin.getConfigManager().getMessageOrDefault(
                "PUNISHMENTS.MUTE",
                "&c&lʏᴏᴜ ʜᴀᴠᴇ ʙᴇᴇɴ ᴍᴜᴛᴇᴅ!\n&8&m----------------------------\n&7ʀᴇᴀѕᴏɴ: &f%reason%\n&7ᴇxᴘɪʀᴇѕ: &f%nicest_expiration%\n&7ᴍᴜᴛᴇᴅ ʙʏ: &f%issuer%\n&8&m----------------------------\n&7ʏᴏᴜ ᴄᴀɴɴᴏᴛ ѕᴘᴇᴀᴋ ɪɴ ᴄʜᴀᴛ",
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
        return issuer == null || issuer.isBlank() ? "unknown" : issuer;
    }
}
