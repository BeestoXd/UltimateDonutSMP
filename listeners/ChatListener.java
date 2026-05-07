package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ChatManager;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final UltimateDonutSmp plugin;

    public ChatListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        ChatManager chatManager = plugin.getChatManager();

        // Extract plain text
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (plugin.getHomeManager().hasPendingInput(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getPaperScheduler().runEntity(player, () ->
                    plugin.getHomeManager().handlePendingInput(player, rawMessage));
            return;
        }

        if (plugin.getTeamManager().hasPendingSearchInput(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getPaperScheduler().runEntity(player, () ->
                    plugin.getTeamManager().handlePendingSearchInput(player, rawMessage));
            return;
        }

        if (plugin.getOrdersManager() != null && plugin.getOrdersManager().hasPendingInput(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getPaperScheduler().runEntity(player, () ->
                    plugin.getOrdersManager().handlePendingInput(player, rawMessage));
            return;
        }

        PunishmentRecord activeMute = plugin.getPunishmentManager()
                .getActiveRecord(player.getUniqueId(), PunishmentType.MUTE)
                .orElse(null);
        if (activeMute != null) {
            event.setCancelled(true);
            plugin.getPaperScheduler().runEntity(player, () ->
                    player.sendMessage(ColorUtils.toComponent(mutedChatMessage(activeMute))));
            return;
        }

        // Team chat check
        if (plugin.getTeamManager().isTeamChatEnabled(player.getUniqueId())) {
            event.setCancelled(true);
            Team team = plugin.getTeamManager().getTeam(player);
            if (team == null) {
                plugin.getTeamManager().setTeamChat(player.getUniqueId(), false);
                plugin.getPaperScheduler().runEntity(player, () ->
                        player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("TEAM.NO-TEAM"))));
                return;
            }
            if (!plugin.getTeamManager().canUseTeamChat(team, player.getUniqueId())) {
                plugin.getTeamManager().setTeamChat(player.getUniqueId(), false);
                plugin.getPaperScheduler().runEntity(player, () ->
                        player.sendMessage(ColorUtils.toComponent(
                                plugin.getConfigManager().getMessage("TEAM.NO-TEAM-CHAT-PERMISSION"))));
                return;
            }
            String teamFormat = "&8[&b" + team.getName().toUpperCase() + "&8] &7%player%&8: &f%message%";
            var component = plugin.getHoverStatsManager().buildChatComponent(player, "", rawMessage, teamFormat);
            for (java.util.UUID uuid : team.getMemberUuids()) {
                Player member = Bukkit.getPlayer(uuid);
                if (member != null) {
                    plugin.getPaperScheduler().runEntity(member, () -> member.sendMessage(component));
                }
            }
            return;
        }

        if (chatManager.isGlobalChatMuted() && !chatManager.isMuteBypassed(player)) {
            event.setCancelled(true);
            plugin.getPaperScheduler().runEntity(player, () -> player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "CHAT-MANAGER.GLOBAL-MUTED-BLOCK",
                            "&cGlobal chat is currently muted."
                    )
            )));
            return;
        }

        ChatManager.FilterResult filterResult = chatManager.validateGlobalMessage(player, rawMessage);
        if (!filterResult.allowed()) {
            event.setCancelled(true);
            plugin.getPaperScheduler().runEntity(player, () -> player.sendMessage(ColorUtils.toComponent(filterResult.blockMessage())));
            return;
        }

        ChatManager.DelayResult delayResult = chatManager.checkAndTrackDelay(player);
        if (!delayResult.allowed()) {
            event.setCancelled(true);
            String delayMessage = plugin.getConfigManager().getMessageOrDefault(
                    "CHAT-MANAGER.GLOBAL-DELAY-BLOCK",
                    "&cYou must wait &f{seconds}s &cbefore chatting again."
            ).replace("{seconds}", String.valueOf(delayResult.remainingSeconds()))
                    .replace("%seconds%", String.valueOf(delayResult.remainingSeconds()));
            plugin.getPaperScheduler().runEntity(player, () -> player.sendMessage(ColorUtils.toComponent(delayMessage)));
            return;
        }

        event.setCancelled(true);

        String chatFormat = chatManager.isFormatEnabled()
                ? chatManager.getChatFormat()
                : "%player%: %message%";
        String prefix = getLuckPermsPrefix(player);
        var chatComponent = plugin.getHoverStatsManager()
                .buildChatComponent(player, prefix, rawMessage, chatFormat);

        final var finalMsg = chatComponent;
        plugin.getPaperScheduler().forEachOnlinePlayer(p -> p.sendMessage(finalMsg));
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
                "&c&lYou have been muted!\n&8&m----------------------------\n&7Reason: &f%reason%\n&7Expires: &f%nicest_expiration%\n&7Muted by: &f%issuer%\n&8&m----------------------------\n&7You cannot speak in chat",
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
