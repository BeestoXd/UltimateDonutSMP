package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.commands.TeleportCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfflineLocationManagerTest {

    @Test
    void canTeleportOfflineAllowsStaffAndOfflinePermissions() {
        TestPermissible staff = new TestPermissible().grant(TeleportCommand.PERMISSION);
        TestPermissible offlineOnly = new TestPermissible().grant(TeleportCommand.OFFLINE_PERMISSION);
        TestPermissible regularUser = new TestPermissible();

        assertTrue(TeleportCommand.canTeleportOffline(staff));
        assertTrue(TeleportCommand.canTeleportOffline(offlineOnly));
        assertFalse(TeleportCommand.canTeleportOffline(regularUser));
        assertFalse(TeleportCommand.canTeleportOffline(null));
    }

    @Test
    void recordsAndPersistsOfflineLocations(@TempDir Path tempDir) throws Exception {
        File dataFile = tempDir.resolve("offline-locations.yml").toFile();

        OfflineLocationManager manager = new OfflineLocationManager(null);
        setField(manager, "dataFile", dataFile);

        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        OfflineLocationManager.OfflineLocationRecord record1 = new OfflineLocationManager.OfflineLocationRecord(
                uuid1, "Alice", "world", 100.5, 64.0, -200.5, 90.0f, 0.0f, 1000L
        );
        OfflineLocationManager.OfflineLocationRecord record2 = new OfflineLocationManager.OfflineLocationRecord(
                uuid2, "Bob", "world_nether", 50.0, 120.0, 50.0, 180.0f, -10.0f, 2000L
        );

        @SuppressWarnings("unchecked")
        java.util.Map<UUID, OfflineLocationManager.OfflineLocationRecord> map =
                (java.util.Map<UUID, OfflineLocationManager.OfflineLocationRecord>) getField(manager, "recordsByUuid");
        @SuppressWarnings("unchecked")
        java.util.Map<String, UUID> nameMap =
                (java.util.Map<String, UUID>) getField(manager, "uuidByUsername");

        map.put(uuid1, record1);
        nameMap.put("alice", uuid1);
        map.put(uuid2, record2);
        nameMap.put("bob", uuid2);
        setField(manager, "dirty", true);

        manager.save();
        assertTrue(dataFile.exists());

        // Load into a fresh manager
        OfflineLocationManager reloadedManager = new OfflineLocationManager(null);
        setField(reloadedManager, "dataFile", dataFile);
        reloadedManager.load();

        assertEquals(uuid1, reloadedManager.findUuid("Alice"));
        assertEquals(uuid1, reloadedManager.findUuid("alice"));
        assertEquals(uuid2, reloadedManager.findUuid("Bob"));

        List<String> names = reloadedManager.getKnownPlayerNames();
        assertTrue(names.contains("Alice"));
        assertTrue(names.contains("Bob"));

        @SuppressWarnings("unchecked")
        java.util.Map<UUID, OfflineLocationManager.OfflineLocationRecord> reloadedMap =
                (java.util.Map<UUID, OfflineLocationManager.OfflineLocationRecord>) getField(reloadedManager, "recordsByUuid");
        OfflineLocationManager.OfflineLocationRecord loadedRecord = reloadedMap.get(uuid1);
        assertEquals("world", loadedRecord.worldName());
        assertEquals(100.5, loadedRecord.x(), 0.001);
        assertEquals(64.0, loadedRecord.y(), 0.001);
        assertEquals(-200.5, loadedRecord.z(), 0.001);
        assertEquals(90.0f, loadedRecord.yaw(), 0.001f);
        assertEquals(0.0f, loadedRecord.pitch(), 0.001f);
        assertEquals(1000L, loadedRecord.timestampMillis());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static final class TestPermissible implements Permissible {
        private final Set<PermissionAttachmentInfo> effectivePermissions = new LinkedHashSet<>();

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
        public boolean isOp() {
            return false;
        }

        @Override
        public void setOp(boolean value) {
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
    }
}
