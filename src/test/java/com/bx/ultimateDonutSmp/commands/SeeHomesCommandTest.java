package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.managers.ProfileViewerManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeeHomesCommandTest {

    @Test
    void theDedicatedPermissionOpensTheHomeList() {
        assertTrue(SeeHomesCommand.canSeeHomes(new TestPermissible().grant(SeeHomesCommand.PERMISSION)));
    }

    @Test
    void staffWhoAlreadyHaveTheProfileViewerKeepWorkingWithoutASecondNode() {
        assertTrue(SeeHomesCommand.canSeeHomes(new TestPermissible().grant(ProfileViewerManager.VIEW_PERMISSION)));
    }

    @Test
    void everybodyElseIsTurnedAway() {
        assertFalse(SeeHomesCommand.canSeeHomes(new TestPermissible()));
        assertFalse(SeeHomesCommand.canSeeHomes(new TestPermissible().grant("ultimatedonutsmp.command.homes")));
        assertFalse(SeeHomesCommand.canSeeHomes(null));
    }

    @Test
    void pluginMetadataDeclaresTheCommandAndBothPermissions() {
        YamlConfiguration pluginYaml = YamlConfiguration.loadConfiguration(new File("src/main/resources/plugin.yml"));

        assertEquals("ultimatedonutsmp.command.seehomes", pluginYaml.getString("commands.seehomes.permission"));
        assertTrue(pluginYaml.getStringList("commands.seehomes.aliases").contains("homesee"));
        assertEquals("op", pluginYaml.getString("permissions.ultimatedonutsmp.command.seehomes.default"));
        assertEquals("op", pluginYaml.getString("permissions." + SeeHomesCommand.PERMISSION + ".default"));
        assertTrue(pluginYaml.getBoolean(
                "permissions.ultimatedonutsmp.command.*.children.ultimatedonutsmp.command.seehomes"));
        assertTrue(pluginYaml.getBoolean(
                "permissions.ultimatedonutsmp.admin.children." + SeeHomesCommand.PERMISSION));
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
