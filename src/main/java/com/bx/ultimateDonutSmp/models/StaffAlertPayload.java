package com.bx.ultimateDonutSmp.models;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public record StaffAlertPayload(
        String messageId,
        String type,
        String sourceServerId,
        String sourceServerName,
        String reporterUuid,
        String reporterName,
        String reportedUuid,
        String reportedName,
        String message,
        String world,
        double x,
        double y,
        double z,
        long createdAt
) {

    public static final String TYPE_HELPOP = "HELPOP";
    public static final String TYPE_REPORT = "REPORT";

    public static StaffAlertPayload helpop(
            String sourceServerId,
            String sourceServerName,
            String reporterUuid,
            String reporterName,
            String message,
            String world,
            double x,
            double y,
            double z
    ) {
        return create(TYPE_HELPOP, sourceServerId, sourceServerName, reporterUuid, reporterName,
                "", "", message, world, x, y, z);
    }

    public static StaffAlertPayload report(
            String sourceServerId,
            String sourceServerName,
            String reporterUuid,
            String reporterName,
            String reportedUuid,
            String reportedName,
            String reason,
            String world,
            double x,
            double y,
            double z
    ) {
        return create(TYPE_REPORT, sourceServerId, sourceServerName, reporterUuid, reporterName,
                reportedUuid, reportedName, reason, world, x, y, z);
    }

    public String serialize() {
        Properties properties = new Properties();
        properties.setProperty("messageId", safe(messageId));
        properties.setProperty("type", safe(type));
        properties.setProperty("sourceServerId", safe(sourceServerId));
        properties.setProperty("sourceServerName", safe(sourceServerName));
        properties.setProperty("reporterUuid", safe(reporterUuid));
        properties.setProperty("reporterName", safe(reporterName));
        properties.setProperty("reportedUuid", safe(reportedUuid));
        properties.setProperty("reportedName", safe(reportedName));
        properties.setProperty("message", safe(message));
        properties.setProperty("world", safe(world));
        properties.setProperty("x", Double.toString(x));
        properties.setProperty("y", Double.toString(y));
        properties.setProperty("z", Double.toString(z));
        properties.setProperty("createdAt", Long.toString(createdAt));

        StringWriter writer = new StringWriter();
        try {
            properties.store(writer, "UltimateDonutSmp network staff alert payload");
        } catch (IOException ignored) {
        }
        return writer.toString();
    }

    public static Optional<StaffAlertPayload> deserialize(String rawPayload) {
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
        String reporterUuid = properties.getProperty("reporterUuid", "").trim();
        String reporterName = properties.getProperty("reporterName", "").trim();
        String reportedUuid = properties.getProperty("reportedUuid", "").trim();
        String reportedName = properties.getProperty("reportedName", "").trim();
        String message = properties.getProperty("message", "");

        if (messageId.isBlank()
                || !isKnownType(type)
                || sourceServerId.isBlank()
                || sourceServerName.isBlank()
                || reporterUuid.isBlank()
                || reporterName.isBlank()
                || message.isBlank()) {
            return Optional.empty();
        }

        if (TYPE_REPORT.equals(type) && reportedName.isBlank()) {
            return Optional.empty();
        }

        long createdAt = parseLong(properties.getProperty("createdAt"), System.currentTimeMillis());
        return Optional.of(new StaffAlertPayload(
                messageId,
                type,
                sourceServerId,
                sourceServerName,
                reporterUuid,
                reporterName,
                reportedUuid,
                reportedName,
                message,
                properties.getProperty("world", "").trim(),
                parseDouble(properties.getProperty("x"), 0.0D),
                parseDouble(properties.getProperty("y"), 0.0D),
                parseDouble(properties.getProperty("z"), 0.0D),
                createdAt
        ));
    }

    private static StaffAlertPayload create(
            String type,
            String sourceServerId,
            String sourceServerName,
            String reporterUuid,
            String reporterName,
            String reportedUuid,
            String reportedName,
            String message,
            String world,
            double x,
            double y,
            double z
    ) {
        return new StaffAlertPayload(
                UUID.randomUUID().toString(),
                normalizeType(type),
                safe(sourceServerId).trim(),
                safe(sourceServerName).trim(),
                safe(reporterUuid).trim(),
                safe(reporterName).trim(),
                safe(reportedUuid).trim(),
                safe(reportedName).trim(),
                safe(message),
                safe(world).trim(),
                x,
                y,
                z,
                System.currentTimeMillis()
        );
    }

    private static String normalizeType(String rawType) {
        return safe(rawType).trim().toUpperCase(Locale.ROOT);
    }

    private static boolean isKnownType(String type) {
        return TYPE_HELPOP.equals(type) || TYPE_REPORT.equals(type);
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
