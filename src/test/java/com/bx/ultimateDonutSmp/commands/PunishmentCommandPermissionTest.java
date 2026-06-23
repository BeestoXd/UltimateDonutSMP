package com.bx.ultimateDonutSmp.commands;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PunishmentCommandPermissionTest {

    private static final String PREFIX = "ultimatedonutsmp.staff.punishments.";

    @Test
    void mapsCommandsAndAliasesToExpectedPermissions() {
        Map<String, String> expected = Map.ofEntries(
                Map.entry("ban", PREFIX + "ban"),
                Map.entry("tempban", PREFIX + "ban"),
                Map.entry("unban", PREFIX + "unban"),
                Map.entry("pardon", PREFIX + "unban"),
                Map.entry("mute", PREFIX + "mute"),
                Map.entry("tempmute", PREFIX + "mute"),
                Map.entry("unmute", PREFIX + "unmute"),
                Map.entry("blacklist", PREFIX + "blacklist"),
                Map.entry("unblacklist", PREFIX + "unblacklist"),
                Map.entry("warn", PREFIX + "create"),
                Map.entry("kick", PREFIX + "create")
        );

        expected.forEach((action, permission) ->
                assertEquals(permission, PunishmentCommand.permissionForAction(action), action));
        assertNull(PunishmentCommand.permissionForAction("unknown"));
    }

    @Test
    void granularPermissionOnlyAllowsItsMappedActions() {
        TestPermissible permissible = new TestPermissible().grant(PREFIX + "ban");

        assertTrue(PunishmentCommand.hasPermissionForAction(permissible, "ban"));
        assertTrue(PunishmentCommand.hasPermissionForAction(permissible, "tempban"));
        assertFalse(PunishmentCommand.hasPermissionForAction(permissible, "mute"));
        assertFalse(PunishmentCommand.hasPermissionForAction(permissible, "blacklist"));
        assertFalse(PunishmentCommand.hasPermissionForAction(permissible, "unban"));
    }

    @Test
    void pluginMetadataUsesGranularCommandsAndLegacyParentPermissions() throws Exception {
        YamlConfiguration pluginYaml = loadPluginYaml();

        assertEquals(PREFIX + "ban", pluginYaml.getString("commands.ban.permission"));
        assertEquals(PREFIX + "ban", pluginYaml.getString("commands.tempban.permission"));
        assertEquals(PREFIX + "mute", pluginYaml.getString("commands.mute.permission"));
        assertEquals(PREFIX + "mute", pluginYaml.getString("commands.tempmute.permission"));
        assertEquals(PREFIX + "blacklist", pluginYaml.getString("commands.blacklist.permission"));
        assertEquals(PREFIX + "unban", pluginYaml.getString("commands.unban.permission"));
        assertEquals(PREFIX + "unmute", pluginYaml.getString("commands.unmute.permission"));
        assertEquals(PREFIX + "unblacklist", pluginYaml.getString("commands.unblacklist.permission"));
        assertEquals(PREFIX + "create", pluginYaml.getString("commands.warn.permission"));
        assertEquals(PREFIX + "create", pluginYaml.getString("commands.kick.permission"));
        assertTrue(pluginYaml.getStringList("commands.unban.aliases").contains("pardon"));

        assertTrue(pluginYaml.getBoolean("permissions." + PREFIX + "create.children." + PREFIX + "ban"));
        assertTrue(pluginYaml.getBoolean("permissions." + PREFIX + "create.children." + PREFIX + "mute"));
        assertTrue(pluginYaml.getBoolean("permissions." + PREFIX + "create.children." + PREFIX + "blacklist"));
        assertTrue(pluginYaml.getBoolean("permissions." + PREFIX + "remove.children." + PREFIX + "unban"));
        assertTrue(pluginYaml.getBoolean("permissions." + PREFIX + "remove.children." + PREFIX + "unmute"));
        assertTrue(pluginYaml.getBoolean("permissions." + PREFIX + "remove.children." + PREFIX + "unblacklist"));

        for (String action : Set.of("ban", "mute", "blacklist", "unban", "unmute", "unblacklist")) {
            String path = "permissions." + PREFIX + action + ".default";
            assertEquals(Boolean.FALSE, pluginYaml.get(path), action);
        }
    }

    private YamlConfiguration loadPluginYaml() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/plugin.yml")) {
            assertNotNull(input);
            return YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
        }
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
