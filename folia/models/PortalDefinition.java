package com.bx.ultimateDonutSmp.models;

import java.util.Locale;

public record PortalDefinition(
        String id,
        String displayName,
        String cuboidName,
        String destinationType,
        String destinationValue,
        boolean enabled,
        String permission,
        int priority,
        long triggerCooldownMillis,
        String enterMessage,
        String hologramWorld,
        double hologramX,
        double hologramY,
        double hologramZ
) {

    public PortalDefinition {
        id = normalizeId(id);
        displayName = normalizeText(displayName);
        cuboidName = normalizeToken(cuboidName);
        destinationType = normalizeToken(destinationType).toUpperCase(Locale.ROOT);
        destinationValue = normalizeToken(destinationValue);
        permission = normalizeText(permission);
        triggerCooldownMillis = Math.max(0L, triggerCooldownMillis);
        enterMessage = normalizeText(enterMessage);
        hologramWorld = normalizeToken(hologramWorld);
    }

    public String effectiveDisplayName() {
        return displayName.isBlank() ? id : displayName;
    }

    public PortalDefinition withDisplayName(String value) {
        return new PortalDefinition(id, value, cuboidName, destinationType, destinationValue, enabled,
                permission, priority, triggerCooldownMillis, enterMessage, hologramWorld, hologramX, hologramY, hologramZ);
    }

    public PortalDefinition withCuboidName(String value) {
        return new PortalDefinition(id, displayName, value, destinationType, destinationValue, enabled,
                permission, priority, triggerCooldownMillis, enterMessage, hologramWorld, hologramX, hologramY, hologramZ);
    }

    public PortalDefinition withDestination(String type, String value) {
        return new PortalDefinition(id, displayName, cuboidName, type, value, enabled,
                permission, priority, triggerCooldownMillis, enterMessage, hologramWorld, hologramX, hologramY, hologramZ);
    }

    public PortalDefinition withEnabled(boolean value) {
        return new PortalDefinition(id, displayName, cuboidName, destinationType, destinationValue, value,
                permission, priority, triggerCooldownMillis, enterMessage, hologramWorld, hologramX, hologramY, hologramZ);
    }

    public PortalDefinition withPriority(int value) {
        return new PortalDefinition(id, displayName, cuboidName, destinationType, destinationValue, enabled,
                permission, value, triggerCooldownMillis, enterMessage, hologramWorld, hologramX, hologramY, hologramZ);
    }

    public PortalDefinition withPermission(String value) {
        return new PortalDefinition(id, displayName, cuboidName, destinationType, destinationValue, enabled,
                value, priority, triggerCooldownMillis, enterMessage, hologramWorld, hologramX, hologramY, hologramZ);
    }

    public PortalDefinition withTriggerCooldownMillis(long value) {
        return new PortalDefinition(id, displayName, cuboidName, destinationType, destinationValue, enabled,
                permission, priority, value, enterMessage, hologramWorld, hologramX, hologramY, hologramZ);
    }

    public PortalDefinition withEnterMessage(String value) {
        return new PortalDefinition(id, displayName, cuboidName, destinationType, destinationValue, enabled,
                permission, priority, triggerCooldownMillis, value, hologramWorld, hologramX, hologramY, hologramZ);
    }

    public PortalDefinition withHologramLocation(String world, double x, double y, double z) {
        return new PortalDefinition(id, displayName, cuboidName, destinationType, destinationValue, enabled,
                permission, priority, triggerCooldownMillis, enterMessage, world, x, y, z);
    }

    public boolean hasCustomHologramLocation() {
        return !hologramWorld.isBlank();
    }

    private static String normalizeId(String value) {
        String normalized = normalizeToken(value);
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static String normalizeToken(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}
