package com.bx.ultimateDonutSmp.utils;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionUtilsTest {

    @Test
    void exactNumberedPermissionReturnsValue() {
        TestPermissible permissible = new TestPermissible()
                .grant("ultimatedonutsmp.homes.5");

        assertEquals(5, PermissionUtils.resolveHighestExactNumberedPermission(
                permissible, "ultimatedonutsmp.homes.", 100));
    }

    @Test
    void wildcardPermissionDoesNotResolveNumberedLimit() {
        TestPermissible permissible = new TestPermissible()
                .grant("ultimatedonutsmp.*")
                .grant("ultimatedonutsmp.homes.*");

        assertEquals(0, PermissionUtils.resolveHighestExactNumberedPermission(
                permissible, "ultimatedonutsmp.homes.", 100));
    }

    @Test
    void exactFalseDenyWinsOverExactAllow() {
        TestPermissible permissible = new TestPermissible()
                .grant("ultimatedonutsmp.homes.5")
                .deny("ultimatedonutsmp.homes.5");

        assertFalse(PermissionUtils.hasExact(permissible, "ultimatedonutsmp.homes.5"));
        assertEquals(0, PermissionUtils.resolveHighestExactNumberedPermission(
                permissible, "ultimatedonutsmp.homes.", 100));
    }

    @Test
    void highestExactNumberedPermissionWins() {
        TestPermissible permissible = new TestPermissible()
                .grant("ultimatedonutsmp.homes.3")
                .grant("ultimatedonutsmp.homes.8");

        assertEquals(8, PermissionUtils.resolveHighestExactNumberedPermission(
                permissible, "ultimatedonutsmp.homes.", 100));
    }

    @Test
    void hasExactIgnoresWildcardAliases() {
        TestPermissible permissible = new TestPermissible()
                .grant("ultimatedonutsmp.auctionhouse.*");

        assertFalse(PermissionUtils.hasExact(permissible, "ultimatedonutsmp.auctionhouse.limit.10"));
    }

    @Test
    void regularHasStillAllowsWildcardAliases() {
        TestPermissible permissible = new TestPermissible()
                .grant("ultimatedonutsmp.auctionhouse.*");

        assertTrue(PermissionUtils.has(permissible, "ultimatedonutsmp.auctionhouse.limit.10"));
    }

    private static final class TestPermissible implements Permissible {
        private final Set<PermissionAttachmentInfo> effectivePermissions = new LinkedHashSet<>();
        private boolean op;

        private TestPermissible grant(String permission) {
            effectivePermissions.add(new PermissionAttachmentInfo(this, permission, null, true));
            return this;
        }

        private TestPermissible deny(String permission) {
            effectivePermissions.add(new PermissionAttachmentInfo(this, permission, null, false));
            return this;
        }

        @Override
        public boolean isPermissionSet(String name) {
            return effectivePermissions.stream()
                    .anyMatch(info -> info.getPermission().equalsIgnoreCase(name));
        }

        @Override
        public boolean isPermissionSet(Permission permission) {
            return permission != null && isPermissionSet(permission.getName());
        }

        @Override
        public boolean hasPermission(String name) {
            boolean matchedTrue = false;
            for (PermissionAttachmentInfo info : effectivePermissions) {
                String granted = info.getPermission().toLowerCase();
                String requested = name.toLowerCase();
                boolean matches = granted.equals(requested)
                        || granted.equals("*")
                        || (granted.endsWith(".*") && requested.startsWith(granted.substring(0, granted.length() - 1)));
                if (!matches) {
                    continue;
                }
                if (!info.getValue()) {
                    return false;
                }
                matchedTrue = true;
            }
            return matchedTrue;
        }

        @Override
        public boolean hasPermission(Permission permission) {
            return permission != null && hasPermission(permission.getName());
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeAttachment(PermissionAttachment attachment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void recalculatePermissions() {
        }

        @Override
        public Set<PermissionAttachmentInfo> getEffectivePermissions() {
            return effectivePermissions;
        }

        @Override
        public boolean isOp() {
            return op;
        }

        @Override
        public void setOp(boolean value) {
            op = value;
        }
    }
}
