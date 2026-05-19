package com.bx.ultimateDonutSmp.utils;

import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.Permissible;

import java.util.Locale;

public final class PermissionUtils {

    private PermissionUtils() {
    }

    public static boolean has(Permissible permissible, String permission) {
        if (permissible == null || permission == null || permission.isBlank()) {
            return false;
        }

        String normalized = normalizePermissionNode(permission);
        if (permissible.hasPermission(permission)) {
            return true;
        }
        if (!normalized.equals(permission) && permissible.hasPermission(normalized)) {
            return true;
        }

        return hasEffectivePermissionAlias(permissible, normalized);
    }

    public static boolean hasOrUnset(Permissible permissible, String permission) {
        return permission == null || permission.isBlank() || has(permissible, permission);
    }

    public static boolean hasAny(Permissible permissible, String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        for (String permission : permissions) {
            if (has(permissible, permission)) {
                return true;
            }
        }
        return false;
    }

    public static String normalizePermissionNode(String permission) {
        if (permission == null || permission.isBlank()) {
            return "";
        }

        String value = permission.trim().toLowerCase(Locale.ROOT);
        StringBuilder normalized = new StringBuilder(value.length());
        value.codePoints().forEach(codePoint -> normalized.appendCodePoint(normalizeCodePoint(codePoint)));
        return normalized.toString();
    }

    private static boolean hasEffectivePermissionAlias(Permissible permissible, String normalizedPermission) {
        boolean matchedTrue = false;
        for (PermissionAttachmentInfo info : permissible.getEffectivePermissions()) {
            String normalizedGranted = normalizePermissionNode(info.getPermission());
            if (!matches(normalizedGranted, normalizedPermission)) {
                continue;
            }
            if (!info.getValue()) {
                return false;
            }
            matchedTrue = true;
        }
        return matchedTrue;
    }

    private static boolean matches(String granted, String requested) {
        if (granted.equals(requested) || granted.equals("*")) {
            return true;
        }
        if (!granted.endsWith(".*")) {
            return false;
        }
        String prefix = granted.substring(0, granted.length() - 1);
        return requested.startsWith(prefix);
    }

    private static int normalizeCodePoint(int codePoint) {
        return switch (codePoint) {
            case '\u1D00', '\u0430' -> 'a';
            case '\u0299' -> 'b';
            case '\u1D04', '\u0441' -> 'c';
            case '\u1D05' -> 'd';
            case '\u1D07', '\u0435' -> 'e';
            case '\u0493', '\uA730' -> 'f';
            case '\u0262', '\u0261' -> 'g';
            case '\u029C' -> 'h';
            case '\u026A', '\u0456' -> 'i';
            case '\u1D0A', '\u0458' -> 'j';
            case '\u1D0B' -> 'k';
            case '\u029F' -> 'l';
            case '\u1D0D' -> 'm';
            case '\u0274' -> 'n';
            case '\u1D0F', '\u043E' -> 'o';
            case '\u1D18', '\u0440' -> 'p';
            case '\u01EB' -> 'q';
            case '\u0280' -> 'r';
            case '\u0455' -> 's';
            case '\u1D1B' -> 't';
            case '\u1D1C' -> 'u';
            case '\u1D20' -> 'v';
            case '\u1D21' -> 'w';
            case '\u0445' -> 'x';
            case '\u028F', '\u0443' -> 'y';
            case '\u1D22' -> 'z';
            case '\u00A0', '\u2007', '\u202F' -> ' ';
            default -> codePoint;
        };
    }
}
