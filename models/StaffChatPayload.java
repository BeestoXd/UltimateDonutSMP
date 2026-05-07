package com.bx.ultimateDonutSmp.models;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public record StaffChatPayload(
        String messageId,
        String type,
        String sourceServerId,
        String sourceServerName,
        String senderUuid,
        String senderName,
        String message,
        long createdAt
) {

    public static final String TYPE_STAFF_CHAT = "STAFF_CHAT";
    public static final String TYPE_STAFF_JOIN = "STAFF_JOIN";
    public static final String TYPE_STAFF_LEAVE = "STAFF_LEAVE";
    public static final String TYPE_SERVER_STATUS = "SERVER_STATUS";

    public static StaffChatPayload staffChat(
            String sourceServerId,
            String sourceServerName,
            String senderUuid,
            String senderName,
            String message
    ) {
        return create(TYPE_STAFF_CHAT, sourceServerId, sourceServerName, senderUuid, senderName, message);
    }

    public static StaffChatPayload notice(
            String type,
            String sourceServerId,
            String sourceServerName,
            String senderUuid,
            String senderName,
            String message
    ) {
        return create(type, sourceServerId, sourceServerName, senderUuid, senderName, message);
    }

    public String serialize() {
        Properties properties = new Properties();
        properties.setProperty("messageId", safe(messageId));
        properties.setProperty("type", safe(type));
        properties.setProperty("sourceServerId", safe(sourceServerId));
        properties.setProperty("sourceServerName", safe(sourceServerName));
        properties.setProperty("senderUuid", safe(senderUuid));
        properties.setProperty("senderName", safe(senderName));
        properties.setProperty("message", safe(message));
        properties.setProperty("createdAt", Long.toString(createdAt));

        StringWriter writer = new StringWriter();
        try {
            properties.store(writer, "UltimateDonutSmp network staff payload");
        } catch (IOException ignored) {
        }
        return writer.toString();
    }

    public static Optional<StaffChatPayload> deserialize(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return Optional.empty();
        }

        Properties properties = new Properties();
        try {
            properties.load(new StringReader(rawPayload));
        } catch (IOException exception) {
            return Optional.empty();
        }

        String messageId = properties.getProperty("messageId", "").trim();
        String type = normalizeType(properties.getProperty("type", ""));
        String sourceServerId = properties.getProperty("sourceServerId", "").trim();
        String sourceServerName = properties.getProperty("sourceServerName", "").trim();
        String senderName = properties.getProperty("senderName", "").trim();

        if (messageId.isBlank()
                || !isKnownType(type)
                || sourceServerId.isBlank()
                || sourceServerName.isBlank()
                || senderName.isBlank()) {
            return Optional.empty();
        }

        long createdAt = parseLong(properties.getProperty("createdAt"), System.currentTimeMillis());
        return Optional.of(new StaffChatPayload(
                messageId,
                type,
                sourceServerId,
                sourceServerName,
                properties.getProperty("senderUuid", "").trim(),
                senderName,
                properties.getProperty("message", ""),
                createdAt
        ));
    }

    private static StaffChatPayload create(
            String type,
            String sourceServerId,
            String sourceServerName,
            String senderUuid,
            String senderName,
            String message
    ) {
        return new StaffChatPayload(
                UUID.randomUUID().toString(),
                normalizeType(type),
                safe(sourceServerId).trim(),
                safe(sourceServerName).trim(),
                safe(senderUuid).trim(),
                safe(senderName).trim(),
                safe(message),
                System.currentTimeMillis()
        );
    }

    private static String normalizeType(String rawType) {
        return safe(rawType).trim().toUpperCase(Locale.ROOT);
    }

    private static boolean isKnownType(String type) {
        return TYPE_STAFF_CHAT.equals(type)
                || TYPE_STAFF_JOIN.equals(type)
                || TYPE_STAFF_LEAVE.equals(type)
                || TYPE_SERVER_STATUS.equals(type);
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
