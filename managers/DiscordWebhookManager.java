package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PunishmentRecord;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DiscordWebhookManager {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z")
            .withZone(ZoneId.systemDefault());
    private static final String DEFAULT_WEBHOOK_PLACEHOLDER = "https://discord.com/api/webhooks/your_webhook_here";

    private final UltimateDonutSmp plugin;
    private final HttpClient httpClient;

    public DiscordWebhookManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder().build();
    }

    public void sendPunishment(PunishmentRecord record) {
        if (record == null) {
            return;
        }

        String messageKey = plugin.getPunishmentManager().getDisplayType(record);
        if (messageKey.startsWith("TEMP")) {
            messageKey = messageKey.substring(4);
        }

        Map<String, String> placeholders = basePunishmentPlaceholders(record);
        placeholders.put("%type%", plugin.getPunishmentManager().getDisplayType(record));
        sendMessage(messageKey, placeholders, record.getTargetUuid());
    }

    public void reload() {
        // Configuration is read on each send so reload only exists for lifecycle symmetry.
    }

    private Map<String, String> basePunishmentPlaceholders(PunishmentRecord record) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", fallback(record.getTargetNameSnapshot(), "Unknown"));
        placeholders.put("%uuid%", record.getTargetUuid() == null ? "" : record.getTargetUuid().toString());
        placeholders.put("%staff%", fallback(record.getIssuerNameSnapshot(), "Unknown"));
        placeholders.put("%reason%", fallback(record.getReason(), "No reason specified"));
        placeholders.put("%duration%", formatDuration(record));
        placeholders.put("%date%", formatDate(record.getIssuedAt()));
        placeholders.put("%id%", String.valueOf(record.getId()));
        placeholders.put("%server%", fallback(record.getSourceServer(), "local"));
        placeholders.put("%scope%", record.getScope() == null ? "SERVER" : record.getScope().name());
        return placeholders;
    }

    private void sendMessage(String key, Map<String, String> placeholders, UUID avatarUuid) {
        ConfigurationSection root = plugin.getConfigManager().getDiscord().getConfigurationSection("WEBHOOKS");
        if (root == null || !root.getBoolean("ENABLED", true)) {
            return;
        }

        String url = root.getString("URL", "");
        if (url.isBlank() || DEFAULT_WEBHOOK_PLACEHOLDER.equalsIgnoreCase(url)) {
            return;
        }

        ConfigurationSection message = root.getConfigurationSection("MESSAGES." + key.toUpperCase(Locale.ROOT));
        if (message == null || !message.getBoolean("ENABLED", true)) {
            return;
        }

        addSkinPlaceholders(root, placeholders, avatarUuid);

        String payload = buildPayload(
                applyPlaceholders(message.getString("TITLE", key), placeholders),
                parseColor(message.getString("COLOR", "#00A4FC")),
                applyPlaceholders(message.getString("DESCRIPTION", ""), placeholders),
                applyPlaceholders(message.getString("AUTHOR_NAME", ""), placeholders),
                applyPlaceholders(message.getString("AUTHOR_ICON", ""), placeholders),
                applyPlaceholders(message.getString("FOOTER", ""), placeholders),
                applyPlaceholders(message.getString("THUMBNAIL", ""), placeholders),
                applyPlaceholders(message.getString("IMAGE", ""), placeholders),
                message.getBoolean("TIMESTAMP", false),
                message.getMapList("FIELDS"),
                placeholders
        );

        plugin.getPaperScheduler().runAsync(() -> post(url, payload));
    }

    private void post(String url, String payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                plugin.getLogger().warning("Discord webhook failed with HTTP " + response.statusCode() + ": " + response.body());
            }
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            plugin.getLogger().log(Level.WARNING, "Failed to send Discord webhook.", e);
        }
    }

    private String buildPayload(String title,
                                int color,
                                String description,
                                String authorName,
                                String authorIcon,
                                String footer,
                                String thumbnail,
                                String image,
                                boolean timestamp,
                                List<Map<?, ?>> fields,
                                Map<String, String> placeholders) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"embeds\":[{");
        appendField(builder, "title", title, false);
        builder.append(",\"color\":").append(color);
        if (description != null && !description.isBlank()) {
            appendField(builder, "description", description, true);
        }
        if (authorName != null && !authorName.isBlank()) {
            builder.append(",\"author\":{");
            appendField(builder, "name", authorName, false);
            if (authorIcon != null && !authorIcon.isBlank()) {
                appendField(builder, "icon_url", authorIcon, true);
            }
            builder.append('}');
        }
        if (thumbnail != null && !thumbnail.isBlank()) {
            builder.append(",\"thumbnail\":{");
            appendField(builder, "url", thumbnail, false);
            builder.append('}');
        }
        if (image != null && !image.isBlank()) {
            builder.append(",\"image\":{");
            appendField(builder, "url", image, false);
            builder.append('}');
        }
        appendFields(builder, fields, placeholders);
        if (timestamp) {
            appendField(builder, "timestamp", Instant.now().toString(), true);
        }
        if (footer != null && !footer.isBlank()) {
            builder.append(",\"footer\":{");
            appendField(builder, "text", footer, false);
            builder.append('}');
        }
        builder.append("}]}");
        return builder.toString();
    }

    private void appendFields(StringBuilder builder, List<Map<?, ?>> fields, Map<String, String> placeholders) {
        if (fields == null || fields.isEmpty()) {
            return;
        }

        boolean hasField = false;
        StringBuilder fieldsBuilder = new StringBuilder();
        fieldsBuilder.append(",\"fields\":[");
        for (Map<?, ?> field : fields) {
            Object nameObject = field.get("NAME");
            Object valueObject = field.get("VALUE");
            Object inlineObject = field.get("INLINE");
            String name = applyPlaceholders(nameObject == null ? "" : String.valueOf(nameObject), placeholders);
            String value = applyPlaceholders(valueObject == null ? "" : String.valueOf(valueObject), placeholders);
            boolean inline = Boolean.parseBoolean(inlineObject == null ? "false" : String.valueOf(inlineObject));
            if (name.isBlank() || value.isBlank()) {
                continue;
            }

            if (hasField) {
                fieldsBuilder.append(',');
            }
            fieldsBuilder.append('{');
            appendField(fieldsBuilder, "name", name, false);
            appendField(fieldsBuilder, "value", value, true);
            fieldsBuilder.append(",\"inline\":").append(inline);
            fieldsBuilder.append('}');
            hasField = true;
        }
        fieldsBuilder.append(']');

        if (hasField) {
            builder.append(fieldsBuilder);
        }
    }

    private void addSkinPlaceholders(ConfigurationSection root, Map<String, String> placeholders, UUID uuid) {
        if (uuid == null) {
            placeholders.put("%skin_avatar%", "");
            placeholders.put("%skin_model%", "");
            placeholders.put("%skin_bust%", "");
            return;
        }

        placeholders.put("%skin_avatar%", applyUuid(root.getString("AVATAR_API", "https://visage.surgeplay.com/face/128/%uuid_no_dash%"), uuid));
        placeholders.put("%skin_model%", applyUuid(root.getString("MODEL_API", "https://visage.surgeplay.com/full/384/%uuid_no_dash%"), uuid));
        placeholders.put("%skin_bust%", applyUuid(root.getString("BUST_API", "https://visage.surgeplay.com/bust/384/%uuid_no_dash%"), uuid));
    }

    private String applyUuid(String input, UUID uuid) {
        return input == null ? "" : input
                .replace("%uuid%", uuid.toString())
                .replace("%uuid_no_dash%", uuid.toString().replace("-", ""));
    }

    private void appendField(StringBuilder builder, String key, String value, boolean prefixComma) {
        if (prefixComma) {
            builder.append(',');
        }
        builder.append('"').append(jsonEscape(key)).append("\":\"").append(jsonEscape(value)).append('"');
    }

    private String applyPlaceholders(String input, Map<String, String> placeholders) {
        String output = input == null ? "" : input;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replace(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
        }
        return output;
    }

    private int parseColor(String input) {
        if (input == null || input.isBlank()) {
            return 0x00A4FC;
        }

        String normalized = input.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        try {
            return Integer.parseInt(normalized, 16);
        } catch (NumberFormatException ignored) {
            return 0x00A4FC;
        }
    }

    private String formatDuration(PunishmentRecord record) {
        if (record == null || record.getExpiresAt() == null) {
            return "Permanent";
        }
        long seconds = Math.max(0L, (record.getExpiresAt() - System.currentTimeMillis()) / 1000L);
        return NumberUtils.formatCountdown(seconds);
    }

    private String formatDate(long millis) {
        long timestamp = millis <= 0L ? System.currentTimeMillis() : millis;
        return DATE_FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder(value.length() + 16);
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (character < 0x20) {
                        builder.append(String.format("\\u%04x", (int) character));
                    } else {
                        builder.append(character);
                    }
                }
            }
        }
        return builder.toString();
    }
}
