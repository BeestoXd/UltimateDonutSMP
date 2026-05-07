package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.StaffAlertPayload;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkStaffAlertManager {

    private static final String DEFAULT_CHANNEL = "ultimatedonutsmp:staff-alerts";
    private static final String PERMISSION_HELPOP_RECEIVE = "ultimatedonutsmp.staff.helpop.receive";
    private static final String PERMISSION_REPORT_RECEIVE = "ultimatedonutsmp.staff.report.receive";
    private static final String PERMISSION_ALERTS_RECEIVE = "ultimatedonutsmp.staff.alerts.receive";
    private static final String PERMISSION_BYPASS_COOLDOWN = "ultimatedonutsmp.staff.alerts.bypass-cooldown";
    private static final long SEEN_MESSAGE_TTL_MILLIS = 5 * 60 * 1000L;

    private final UltimateDonutSmp plugin;
    private final Map<String, Long> seenMessageIds = new ConcurrentHashMap<>();
    private final Map<UUID, Long> helpopCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> reportCooldowns = new ConcurrentHashMap<>();
    private final Set<UUID> redisUnavailableWarnedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<String> subscribedChannels = ConcurrentHashMap.newKeySet();

    private volatile String helpopChannel = DEFAULT_CHANNEL;
    private volatile String reportChannel = DEFAULT_CHANNEL;
    private volatile ScheduledTask cleanupTask;
    private volatile boolean shuttingDown;

    public NetworkStaffAlertManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        shuttingDown = false;
        cancelCleanupTask();
        seenMessageIds.clear();
        redisUnavailableWarnedPlayers.clear();
        unsubscribeAll();

        helpopChannel = channel("NETWORK.HELPOP_REDIS_CHANNEL");
        reportChannel = channel("NETWORK.REPORT_REDIS_CHANNEL");

        if (isNetworkEnabled() && (isHelpopEnabled() || isReportEnabled())) {
            subscribe(helpopChannel);
            subscribe(reportChannel);
            startCleanupTask();
        }
    }

    public void shutdown() {
        shuttingDown = true;
        cancelCleanupTask();
        unsubscribeAll();
    }

    public void sendHelpop(Player sender, String rawMessage) {
        if (!isHelpopEnabled()) {
            sender.sendMessage(ColorUtils.toComponent(message("HELPOP.DISABLED", "&cHelpop is currently disabled.")));
            return;
        }

        String message = sanitizeUserMessage(rawMessage);
        if (message.isBlank()) {
            sender.sendMessage(ColorUtils.toComponent(message("HELPOP.USAGE", "&cUsage: /helpop <message>")));
            return;
        }

        int maxLength = maxLength("NETWORK.HELPOP_MAX_MESSAGE_LENGTH");
        if (message.length() > maxLength) {
            sender.sendMessage(ColorUtils.toComponent(message(
                    "HELPOP.MESSAGE_TOO_LONG",
                    "&cYour request is too long. Max: %max% characters.",
                    "%max%", Integer.toString(maxLength)
            )));
            return;
        }

        if (!checkCooldown(sender, helpopCooldowns, cooldownSeconds("NETWORK.HELPOP_COOLDOWN_SECONDS", 30),
                "HELPOP.COOLDOWN", "&cPlease wait %seconds%s before using helpop again.")) {
            return;
        }

        Location location = sender.getLocation();
        StaffAlertPayload payload = StaffAlertPayload.helpop(
                getLocalServerId(),
                getLocalDisplayName(),
                sender.getUniqueId().toString(),
                sender.getName(),
                message,
                worldName(location),
                location.getX(),
                location.getY(),
                location.getZ()
        );

        markSeen(payload.messageId());
        broadcastPayload(payload);
        publishPayloadAsync(payload, helpopChannel, sender, "HELPOP.REDIS_UNAVAILABLE",
                "&eYour request was delivered locally, but Redis is unavailable for cross-server delivery.");

        sender.sendMessage(ColorUtils.toComponent(message(
                "HELPOP.CONFIRMATION",
                "&aYour request has been sent to all staff members."
        )));
    }

    public void sendReport(Player reporter, Player reported, String rawReason) {
        if (!isReportEnabled()) {
            reporter.sendMessage(ColorUtils.toComponent(message("REPORT.DISABLED", "&cReports are currently disabled.")));
            return;
        }

        if (reported == null || !reported.isOnline()) {
            reporter.sendMessage(ColorUtils.toComponent(message("REPORT.PLAYER_NOT_FOUND", "&cPlayer not found.")));
            return;
        }

        if (reporter.getUniqueId().equals(reported.getUniqueId())) {
            reporter.sendMessage(ColorUtils.toComponent(message("REPORT.CANNOT_REPORT_SELF", "&cYou can't report yourself!")));
            return;
        }

        String reason = sanitizeUserMessage(rawReason);
        if (reason.isBlank()) {
            reporter.sendMessage(ColorUtils.toComponent(message("REPORT.USAGE", "&cUsage: /report <player> <reason>")));
            return;
        }

        int maxLength = maxLength("NETWORK.REPORT_MAX_REASON_LENGTH");
        if (reason.length() > maxLength) {
            reporter.sendMessage(ColorUtils.toComponent(message(
                    "REPORT.MESSAGE_TOO_LONG",
                    "&cYour report reason is too long. Max: %max% characters.",
                    "%max%", Integer.toString(maxLength)
            )));
            return;
        }

        if (!checkCooldown(reporter, reportCooldowns, cooldownSeconds("NETWORK.REPORT_COOLDOWN_SECONDS", 60),
                "REPORT.COOLDOWN", "&cPlease wait %seconds%s before reporting again.")) {
            return;
        }

        Location location = reporter.getLocation();
        StaffAlertPayload payload = StaffAlertPayload.report(
                getLocalServerId(),
                getLocalDisplayName(),
                reporter.getUniqueId().toString(),
                reporter.getName(),
                reported.getUniqueId().toString(),
                reported.getName(),
                reason,
                worldName(location),
                location.getX(),
                location.getY(),
                location.getZ()
        );

        markSeen(payload.messageId());
        broadcastPayload(payload);
        publishPayloadAsync(payload, reportChannel, reporter, "REPORT.REDIS_UNAVAILABLE",
                "&eYour report was delivered locally, but Redis is unavailable for cross-server delivery.");

        reporter.sendMessage(ColorUtils.toComponent(message(
                "REPORT.CONFIRMATION",
                "&aYour report has been sent to all staff members."
        )));
    }

    public void handleIncomingPayload(String rawPayload) {
        Optional<StaffAlertPayload> parsedPayload = StaffAlertPayload.deserialize(rawPayload);
        if (parsedPayload.isEmpty()) {
            plugin.getLogger().warning("Ignored invalid network staff alert payload from Redis.");
            return;
        }

        StaffAlertPayload payload = parsedPayload.get();
        if (!markSeen(payload.messageId()) || !plugin.isEnabled()) {
            return;
        }

        plugin.getPaperScheduler().runGlobal(() -> broadcastPayload(payload));
    }

    public void clearPlayerState(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        redisUnavailableWarnedPlayers.remove(playerUuid);
    }

    private void broadcastPayload(StaffAlertPayload payload) {
        if (StaffAlertPayload.TYPE_HELPOP.equals(payload.type())) {
            if (!isHelpopEnabled()) {
                return;
            }
            broadcastToStaff(formatLines("HELPOP.FORMAT", defaultHelpopFormat(), payload), PERMISSION_HELPOP_RECEIVE);
            return;
        }

        if (StaffAlertPayload.TYPE_REPORT.equals(payload.type())) {
            if (!isReportEnabled()) {
                return;
            }
            broadcastToStaff(formatLines("REPORT.FORMAT", defaultReportFormat(), payload), PERMISSION_REPORT_RECEIVE);
        }
    }

    private void broadcastToStaff(List<String> lines, String typePermission) {
        plugin.getPaperScheduler().forEachOnlinePlayer(target -> {
            if (!canReceive(target, typePermission)) {
                return;
            }
            for (String line : lines) {
                target.sendMessage(ColorUtils.toComponent(line));
            }
        });

        if (getNetworkConfig().getBoolean("NETWORK.STAFF_ALERTS_LOG_TO_CONSOLE",
                getNetworkConfig().getBoolean("NETWORK.LOG_TO_CONSOLE", true))) {
            for (String line : lines) {
                String stripped = ColorUtils.strip(line);
                if (!stripped.isBlank()) {
                    plugin.getLogger().info(stripped);
                }
            }
        }
    }

    private void publishPayloadAsync(StaffAlertPayload payload, String channel, Player feedbackTarget,
                                     String redisMessagePath, String redisMessageFallback) {
        if (!isNetworkEnabled() || !plugin.getRedisManager().isEnabled() || shuttingDown || !plugin.isEnabled()) {
            return;
        }

        plugin.getPaperScheduler().runAsync(() -> {
            boolean published = plugin.getRedisManager().publish(channel, payload.serialize());
            if (published || !shouldWarnSenderOnRedisError()) {
                return;
            }

            if (!redisUnavailableWarnedPlayers.add(feedbackTarget.getUniqueId())) {
                return;
            }

            plugin.getPaperScheduler().runEntity(feedbackTarget, () -> {
                if (feedbackTarget.isOnline()) {
                    feedbackTarget.sendMessage(ColorUtils.toComponent(message(redisMessagePath, redisMessageFallback)));
                }
            });
        });
    }

    private boolean checkCooldown(Player player, Map<UUID, Long> cooldowns, int cooldownSeconds,
                                  String messagePath, String fallback) {
        if (cooldownSeconds <= 0 || player.hasPermission(PERMISSION_BYPASS_COOLDOWN)) {
            return true;
        }

        long now = System.currentTimeMillis();
        long availableAt = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (availableAt > now) {
            long remainingSeconds = Math.max(1L, (long) Math.ceil((availableAt - now) / 1000.0D));
            player.sendMessage(ColorUtils.toComponent(message(
                    messagePath,
                    fallback,
                    "%seconds%", Long.toString(remainingSeconds)
            )));
            return false;
        }

        cooldowns.put(player.getUniqueId(), now + (cooldownSeconds * 1000L));
        return true;
    }

    private List<String> formatLines(String path, List<String> fallback, StaffAlertPayload payload) {
        List<String> configured = getMessagesConfig().getStringList(path);
        if (configured.isEmpty() && getMessagesConfig().isString(path)) {
            configured = List.of(getMessagesConfig().getString(path, ""));
        }
        if (configured.isEmpty()) {
            configured = fallback;
        }

        List<String> formatted = new ArrayList<>();
        for (String line : configured) {
            formatted.add(applyPlaceholders(line, payload));
        }
        return formatted;
    }

    private String applyPlaceholders(String line, StaffAlertPayload payload) {
        return safe(line)
                .replace("%server%", payload.sourceServerName())
                .replace("%player%", payload.reporterName())
                .replace("%message%", payload.message())
                .replace("%reporter%", payload.reporterName())
                .replace("%reported%", payload.reportedName())
                .replace("%reason%", payload.message())
                .replace("%reporter_uuid%", payload.reporterUuid())
                .replace("%reported_uuid%", payload.reportedUuid())
                .replace("%world%", payload.world())
                .replace("%x%", formatCoordinate(payload.x()))
                .replace("%y%", formatCoordinate(payload.y()))
                .replace("%z%", formatCoordinate(payload.z()));
    }

    private boolean markSeen(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return true;
        }
        return seenMessageIds.putIfAbsent(messageId, System.currentTimeMillis()) == null;
    }

    private void subscribe(String channel) {
        if (channel == null || channel.isBlank() || !subscribedChannels.add(channel)) {
            return;
        }
        plugin.getRedisManager().subscribe(channel, this::handleIncomingPayload);
    }

    private void unsubscribeAll() {
        for (String channel : List.copyOf(subscribedChannels)) {
            plugin.getRedisManager().unsubscribe(channel);
        }
        subscribedChannels.clear();
    }

    private void startCleanupTask() {
        cleanupTask = plugin.getPaperScheduler().runAsyncTimer(() -> {
            long now = System.currentTimeMillis();
            long seenCutoff = now - SEEN_MESSAGE_TTL_MILLIS;
            seenMessageIds.entrySet().removeIf(entry -> entry.getValue() < seenCutoff);
            helpopCooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);
            reportCooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);
        }, 20L * 60L, 20L * 60L);
    }

    private void cancelCleanupTask() {
        ScheduledTask task = cleanupTask;
        cleanupTask = null;
        if (task != null) {
            task.cancel();
        }
    }

    private boolean canReceive(Player player, String typePermission) {
        return player.hasPermission(PERMISSION_ALERTS_RECEIVE) || player.hasPermission(typePermission);
    }

    private boolean isNetworkEnabled() {
        return getNetworkConfig().getBoolean("NETWORK.ENABLED", true);
    }

    private boolean isHelpopEnabled() {
        return isNetworkEnabled() && getNetworkConfig().getBoolean("NETWORK.HELPOP_ENABLED", true);
    }

    private boolean isReportEnabled() {
        return isNetworkEnabled() && getNetworkConfig().getBoolean("NETWORK.REPORT_ENABLED", true);
    }

    private boolean shouldWarnSenderOnRedisError() {
        return getNetworkConfig().getBoolean("NETWORK.STAFF_ALERTS_WARN_SENDER_ON_REDIS_ERROR", false);
    }

    private int maxLength(String path) {
        int fallback = getNetworkConfig().getInt("NETWORK.STAFF_ALERTS_MAX_REASON_LENGTH", 256);
        return Math.max(1, getNetworkConfig().getInt(path, fallback));
    }

    private int cooldownSeconds(String path, int fallback) {
        return Math.max(0, getNetworkConfig().getInt(path, fallback));
    }

    private String channel(String path) {
        String configured = getNetworkConfig().getString(path, DEFAULT_CHANNEL);
        if (configured == null || configured.isBlank()) {
            return DEFAULT_CHANNEL;
        }
        return configured.trim();
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

    private String message(String path, String fallback, String... placeholders) {
        String message = plugin.getConfigManager().getMessageOrDefault(path, fallback);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        return message;
    }

    private FileConfiguration getNetworkConfig() {
        return plugin.getConfigManager().getNetwork();
    }

    private FileConfiguration getMessagesConfig() {
        return plugin.getConfigManager().getMessages();
    }

    private String sanitizeUserMessage(String value) {
        return safe(value)
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("\\p{Cntrl}", "")
                .trim();
    }

    private String worldName(Location location) {
        return location.getWorld() == null ? "" : location.getWorld().getName();
    }

    private String formatCoordinate(double coordinate) {
        return String.format(Locale.US, "%.1f", coordinate);
    }

    private List<String> defaultHelpopFormat() {
        return List.of(
                "",
                "&9[ʀᴇǫᴜᴇѕᴛ] &7[%server%] &a%player% &bʜᴀѕ ʀᴇǫᴜᴇѕᴛᴇᴅ ᴀѕѕɪѕᴛᴀɴᴄᴇ",
                "     &9ʀᴇᴀѕᴏɴ: &b%message%",
                ""
        );
    }

    private List<String> defaultReportFormat() {
        return List.of(
                "",
                "&9[ʀᴇᴘᴏʀᴛ] &7[%server%] &c%reported% &bʜᴀѕ ʙᴇᴇɴ ʀᴇᴘᴏʀᴛᴇᴅ ʙʏ &a%reporter%",
                "     &9ʀᴇᴀѕᴏɴ: &b%reason%",
                ""
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
