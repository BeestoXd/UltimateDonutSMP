package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.StaffChatPayload;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkStaffChatManager {

    private static final String PERMISSION = "ultimatedonutsmp.staff.chat.use";
    private static final String DEFAULT_CHANNEL = "ultimatedonutsmp:staff-chat";
    private static final long SEEN_MESSAGE_TTL_MILLIS = 5 * 60 * 1000L;

    private final UltimateDonutSmp plugin;
    private final Map<String, Long> seenMessageIds = new ConcurrentHashMap<>();
    private final Set<UUID> redisUnavailableWarnedPlayers = ConcurrentHashMap.newKeySet();

    private volatile String channel = DEFAULT_CHANNEL;
    private volatile ScheduledTask cleanupTask;
    private volatile boolean shuttingDown;

    public NetworkStaffChatManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        shuttingDown = false;
        cancelCleanupTask();
        seenMessageIds.clear();

        plugin.getRedisManager().reload();
        channel = getNetworkConfig().getString("NETWORK.REDIS_CHANNEL", DEFAULT_CHANNEL);
        if (channel == null || channel.isBlank()) {
            channel = DEFAULT_CHANNEL;
        }

        if (isNetworkEnabled() && isStaffChatEnabled()) {
            plugin.getRedisManager().subscribe(channel, this::handleIncomingPayload);
            startCleanupTask();
            publishServerStatus("online");
        } else {
            plugin.getRedisManager().unsubscribe(channel);
        }
    }

    public void shutdown() {
        shuttingDown = true;
        publishServerStatusSync("offline");
        cancelCleanupTask();
        plugin.getRedisManager().unsubscribe(channel);
    }

    public void sendStaffChat(CommandSender sender, String rawMessage) {
        if (!isNetworkEnabled() || !isStaffChatEnabled()) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "STAFFCHAT.DISABLED",
                    "&cNetwork staff chat is currently disabled."
            )));
            return;
        }

        String message = rawMessage == null ? "" : rawMessage.trim();
        if (message.isBlank()) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "STAFFCHAT.USAGE",
                    "&cUsage: /staffchat <message>"
            )));
            return;
        }

        int maxLength = Math.max(1, getNetworkConfig().getInt("NETWORK.MAX_MESSAGE_LENGTH", 512));
        if (message.length() > maxLength) {
            sender.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessageOrDefault(
                    "STAFFCHAT.MESSAGE_TOO_LONG",
                    "&cStaff chat message is too long. Max: %max% characters.",
                    "%max%", Integer.toString(maxLength)
            )));
            return;
        }

        String senderUuid = "";
        if (sender instanceof Player player) {
            senderUuid = player.getUniqueId().toString();
        }

        StaffChatPayload payload = StaffChatPayload.staffChat(
                getLocalServerId(),
                getLocalDisplayName(),
                senderUuid,
                sender.getName(),
                message
        );

        markSeen(payload.messageId());
        broadcastPayload(payload);
        publishPayloadAsync(payload, sender);
    }

    public void handleStaffJoin(Player player) {
        if (!isJoinLeaveEnabled() || player == null || shuttingDown) {
            return;
        }

        plugin.getPaperScheduler().runEntityLater(player, () -> {
            if (!player.isOnline() || !player.hasPermission(PERMISSION) || shuttingDown) {
                return;
            }
            publishNotice(StaffChatPayload.TYPE_STAFF_JOIN, player);
        }, 20L);
    }

    public void handleStaffLeave(Player player) {
        if (!isJoinLeaveEnabled() || player == null || !player.hasPermission(PERMISSION) || shuttingDown) {
            return;
        }

        publishNotice(StaffChatPayload.TYPE_STAFF_LEAVE, player);
    }

    public void clearPlayerState(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        redisUnavailableWarnedPlayers.remove(playerUuid);
    }

    public void handleIncomingPayload(String rawPayload) {
        Optional<StaffChatPayload> parsedPayload = StaffChatPayload.deserialize(rawPayload);
        if (parsedPayload.isEmpty()) {
            plugin.getLogger().warning("Ignored invalid network staff payload from Redis.");
            return;
        }

        StaffChatPayload payload = parsedPayload.get();
        if (!markSeen(payload.messageId())) {
            return;
        }

        if (!plugin.isEnabled()) {
            return;
        }

        plugin.getPaperScheduler().runGlobal(() -> broadcastPayload(payload));
    }

    public String getLocalServerId() {
        String configured = getNetworkConfig().getString("NETWORK.LOCAL_SERVER_ID", "");
        if (configured == null || configured.isBlank()) {
            configured = getNetworkConfig().getString("NETWORK-STATUS.LOCAL-SERVER-ID", "local");
        }
        return normalizeServerId(configured);
    }

    public String getLocalDisplayName() {
        String configured = getNetworkConfig().getString("NETWORK.LOCAL_DISPLAY_NAME", "");
        if (configured == null || configured.isBlank()) {
            configured = getNetworkConfig().getString("NETWORK-STATUS.LOCAL-DISPLAY-NAME", "");
        }
        if (configured == null || configured.isBlank()) {
            return prettifyServerId(getLocalServerId());
        }
        return configured.trim();
    }

    private void publishNotice(String type, Player player) {
        StaffChatPayload payload = StaffChatPayload.notice(
                type,
                getLocalServerId(),
                getLocalDisplayName(),
                player.getUniqueId().toString(),
                player.getName(),
                ""
        );

        markSeen(payload.messageId());
        broadcastPayload(payload);
        publishPayloadAsync(payload, null);
    }

    private void publishServerStatus(String status) {
        if (!isServerStatusEnabled()) {
            return;
        }

        StaffChatPayload payload = StaffChatPayload.notice(
                StaffChatPayload.TYPE_SERVER_STATUS,
                getLocalServerId(),
                getLocalDisplayName(),
                "",
                "Server",
                status
        );
        markSeen(payload.messageId());
        publishPayloadAsync(payload, null);
    }

    private void publishServerStatusSync(String status) {
        if (!isNetworkEnabled() || !isServerStatusEnabled() || !plugin.getRedisManager().isEnabled()) {
            return;
        }

        StaffChatPayload payload = StaffChatPayload.notice(
                StaffChatPayload.TYPE_SERVER_STATUS,
                getLocalServerId(),
                getLocalDisplayName(),
                "",
                "Server",
                status
        );
        markSeen(payload.messageId());
        plugin.getRedisManager().publish(channel, payload.serialize());
    }

    private void publishPayloadAsync(StaffChatPayload payload, CommandSender feedbackTarget) {
        if (!isNetworkEnabled() || !plugin.getRedisManager().isEnabled() || shuttingDown || !plugin.isEnabled()) {
            return;
        }

        plugin.getPaperScheduler().runAsync(() -> {
            boolean published = plugin.getRedisManager().publish(channel, payload.serialize());
            if (published || feedbackTarget == null || !shouldWarnSenderOnRedisError()) {
                return;
            }

            if (feedbackTarget instanceof Player player && !redisUnavailableWarnedPlayers.add(player.getUniqueId())) {
                return;
            }

            Runnable feedback = () -> feedbackTarget.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "STAFFCHAT.REDIS_UNAVAILABLE",
                            "&eStaff chat was delivered locally, but Redis is unavailable for cross-server delivery."
                    )
            ));
            if (feedbackTarget instanceof Player player) {
                plugin.getPaperScheduler().runEntity(player, feedback);
            } else {
                plugin.getPaperScheduler().runGlobal(feedback);
            }
        });
    }

    private void broadcastPayload(StaffChatPayload payload) {
        if (StaffChatPayload.TYPE_STAFF_CHAT.equals(payload.type())) {
            broadcastStaffChat(payload);
            return;
        }

        broadcastNotice(payload);
    }

    private void broadcastStaffChat(StaffChatPayload payload) {
        String format = getNetworkConfig().getString(
                "NETWORK.STAFF_CHAT",
                plugin.getConfigManager().getMessageOrDefault(
                        "STAFFCHAT.FORMAT",
                        "&8[&6StaffChat&8] &e%player%&7: %message%"
                )
        );

        String formatted = applyPlaceholders(format, payload);
        broadcastToStaff(formatted, "");
    }

    private void broadcastNotice(StaffChatPayload payload) {
        String formatPath;
        if (StaffChatPayload.TYPE_STAFF_JOIN.equals(payload.type())) {
            formatPath = "NETWORK.STAFF_JOIN";
        } else if (StaffChatPayload.TYPE_STAFF_LEAVE.equals(payload.type())) {
            formatPath = "NETWORK.STAFF_LEAVE";
        } else if (StaffChatPayload.TYPE_SERVER_STATUS.equals(payload.type())) {
            formatPath = "NETWORK.SERVER_STATUS";
        } else {
            return;
        }

        String fallback = switch (payload.type()) {
            case StaffChatPayload.TYPE_STAFF_JOIN -> "&8[&a+&8] &a%player% &7joined &b%server%";
            case StaffChatPayload.TYPE_STAFF_LEAVE -> "&8[&c-&8] &a%player% &7left &b%server%";
            case StaffChatPayload.TYPE_SERVER_STATUS -> "&6%server% &eis now %status%&e.";
            default -> "";
        };

        String formatted = applyPlaceholders(getNetworkConfig().getString(formatPath, fallback), payload);
        broadcastToStaff(formatted, payload.senderUuid());
    }

    private void broadcastToStaff(String formatted, String excludedUuid) {
        String safeExcludedUuid = excludedUuid == null ? "" : excludedUuid;
        plugin.getPaperScheduler().forEachOnlinePlayer(target -> {
            if (!target.hasPermission(PERMISSION)) {
                return;
            }
            if (!safeExcludedUuid.isBlank() && target.getUniqueId().toString().equals(safeExcludedUuid)) {
                return;
            }
            target.sendMessage(ColorUtils.toComponent(formatted));
        });

        if (getNetworkConfig().getBoolean("NETWORK.LOG_TO_CONSOLE", true)) {
            plugin.getLogger().info(ColorUtils.strip(formatted));
        }
    }

    private String applyPlaceholders(String format, StaffChatPayload payload) {
        return safe(format)
                .replace("%server%", payload.sourceServerName())
                .replace("%player%", payload.senderName())
                .replace("%message%", payload.message())
                .replace("%status%", payload.message());
    }

    private boolean markSeen(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return true;
        }
        return seenMessageIds.putIfAbsent(messageId, System.currentTimeMillis()) == null;
    }

    private void startCleanupTask() {
        cleanupTask = plugin.getPaperScheduler().runAsyncTimer(() -> {
            long cutoff = System.currentTimeMillis() - SEEN_MESSAGE_TTL_MILLIS;
            seenMessageIds.entrySet().removeIf(entry -> entry.getValue() < cutoff);
        }, 20L * 60L, 20L * 60L);
    }

    private void cancelCleanupTask() {
        ScheduledTask task = cleanupTask;
        cleanupTask = null;
        if (task != null) {
            task.cancel();
        }
    }

    private boolean isNetworkEnabled() {
        return getNetworkConfig().getBoolean("NETWORK.ENABLED", true);
    }

    private boolean isStaffChatEnabled() {
        return getNetworkConfig().getBoolean("NETWORK.STAFF_CHAT_ENABLED", true);
    }

    private boolean isJoinLeaveEnabled() {
        return isNetworkEnabled()
                && isStaffChatEnabled()
                && getNetworkConfig().getBoolean("NETWORK.STAFF_JOIN_LEAVE_ENABLED", true);
    }

    private boolean isServerStatusEnabled() {
        return getNetworkConfig().getBoolean("NETWORK.SERVER_STATUS_ENABLED", true);
    }

    private boolean shouldWarnSenderOnRedisError() {
        return getNetworkConfig().getBoolean("NETWORK.SEND_LOCAL_FALLBACK_ON_REDIS_ERROR", true);
    }

    private FileConfiguration getNetworkConfig() {
        return plugin.getConfigManager().getNetwork();
    }

    private String normalizeServerId(String value) {
        String normalized = safe(value).trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "local";
        }
        return normalized.replaceAll("[^a-z0-9_-]", "-");
    }

    private String prettifyServerId(String value) {
        String normalized = normalizeServerId(value);
        String[] parts = normalized.split("[-_]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.isEmpty() ? "Local" : builder.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
