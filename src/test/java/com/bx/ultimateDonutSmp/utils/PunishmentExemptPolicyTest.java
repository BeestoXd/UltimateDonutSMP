package com.bx.ultimateDonutSmp.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A rank handed the offend or ban permission used to be able to punish anybody on the server, the
 * owner included.
 */
class PunishmentExemptPolicyTest {

    private static final String OFFEND = "ultimatedonutsmp.staff.punishments.offend";

    @Test
    void staffCannotPunishAnExemptPlayer() {
        TestPermissible staff = new TestPermissible().grant(OFFEND);
        TestPermissible owner = new TestPermissible().grant(PunishmentExemptPolicy.EXEMPT_PERMISSION);

        assertFalse(PunishmentExemptPolicy.canPunish(staff, owner));
    }

    @Test
    void staffCanStillPunishEveryoneElse() {
        TestPermissible staff = new TestPermissible().grant(OFFEND);

        assertTrue(PunishmentExemptPolicy.canPunish(staff, new TestPermissible()));
    }

    @Test
    void theBypassGetsThroughTheExemption() {
        TestPermissible admin = new TestPermissible()
                .grant(OFFEND)
                .grant(PunishmentExemptPolicy.BYPASS_PERMISSION);
        TestPermissible owner = new TestPermissible().grant(PunishmentExemptPolicy.EXEMPT_PERMISSION);

        assertTrue(PunishmentExemptPolicy.canPunish(admin, owner));
    }

    @Test
    void theStaffPunishmentWildcardDoesNotHandOutTheExemption() {
        TestPermissible staff = new TestPermissible().grant("ultimatedonutsmp.staff.punishments.*");
        TestPermissible owner = new TestPermissible().grant(PunishmentExemptPolicy.EXEMPT_PERMISSION);

        assertFalse(PunishmentExemptPolicy.isExempt(staff),
                "a wildcard over the staff nodes must not make every moderator unpunishable");
        assertFalse(PunishmentExemptPolicy.canPunish(staff, owner));
    }

    @Test
    void anAdminWildcardCarriesBothNodes() {
        TestPermissible admin = new TestPermissible().grant("ultimatedonutsmp.admin.*");
        TestPermissible owner = new TestPermissible().grant(PunishmentExemptPolicy.EXEMPT_PERMISSION);

        assertTrue(PunishmentExemptPolicy.isExempt(admin));
        assertTrue(PunishmentExemptPolicy.canPunish(admin, owner));
    }

    @Test
    void anOfflineTargetIsLeftAlone() {
        TestPermissible staff = new TestPermissible().grant(OFFEND);

        assertTrue(PunishmentExemptPolicy.canPunish(staff, null),
                "an offline player has no permissions to read, so the punishment goes through");
    }

    @Test
    void pluginMetadataDeclaresBothNodes() {
        YamlConfiguration pluginYaml = YamlConfiguration.loadConfiguration(
                new java.io.File("src/main/resources/plugin.yml"));

        assertEquals("op", pluginYaml.getString(
                "permissions." + PunishmentExemptPolicy.EXEMPT_PERMISSION + ".default"));
        assertEquals("op", pluginYaml.getString(
                "permissions." + PunishmentExemptPolicy.BYPASS_PERMISSION + ".default"));
    }

    private static final class TestPermissible implements Permissible {
        private final Set<PermissionAttachmentInfo> effectivePermissions = new LinkedHashSet<>();
        private boolean op;

        private TestPermissible grant(String permission) {
            effectivePermissions.add(new PermissionAttachmentInfo(this, permission, null, true));
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
            return effectivePermissions.stream()
                    .anyMatch(info -> info.getValue() && info.getPermission().equalsIgnoreCase(name));
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
