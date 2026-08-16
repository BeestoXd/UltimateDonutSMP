package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnderChestRowPermissionTest {

    private EnderChestManager createManager(YamlConfiguration enderChestConfig) throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

        Constructor<?> pluginConstructor = reflectionFactory
                .newConstructorForSerialization(UltimateDonutSmp.class, objectConstructor);
        UltimateDonutSmp plugin = (UltimateDonutSmp) pluginConstructor.newInstance();

        ConfigManager configManager = new ConfigManager(plugin);
        Field enderChestField = ConfigManager.class.getDeclaredField("enderChest");
        enderChestField.setAccessible(true);
        enderChestField.set(configManager, enderChestConfig);

        Field configManagerField = UltimateDonutSmp.class.getDeclaredField("configManager");
        configManagerField.setAccessible(true);
        configManagerField.set(plugin, configManager);

        // The real constructor calls reload(), which needs a live server; only the config reading
        // helpers are under test here.
        Constructor<?> managerConstructor = reflectionFactory
                .newConstructorForSerialization(EnderChestManager.class, objectConstructor);
        EnderChestManager manager = (EnderChestManager) managerConstructor.newInstance();

        Field pluginField = EnderChestManager.class.getDeclaredField("plugin");
        pluginField.setAccessible(true);
        pluginField.set(manager, plugin);

        return manager;
    }

    private Player createMockPlayer(Set<String> permissions) {
        final Player[] playerHolder = new Player[1];
        Player playerProxy = (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getUniqueId")) {
                        return UUID.randomUUID();
                    }
                    if (method.getName().equals("hasPermission")) {
                        return permissions.contains(((String) args[0]).toLowerCase(Locale.ROOT));
                    }
                    if (method.getName().equals("getEffectivePermissions")) {
                        Set<PermissionAttachmentInfo> effective = new HashSet<>();
                        for (String permission : permissions) {
                            effective.add(new PermissionAttachmentInfo(playerHolder[0], permission, null, true));
                        }
                        return effective;
                    }
                    if (method.getName().equals("isOnline")) {
                        return true;
                    }
                    return null;
                }
        );
        playerHolder[0] = playerProxy;
        return playerProxy;
    }

    private YamlConfiguration baseConfig() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("ENDER-CHEST.DEFAULT-ROWS", 3);
        config.set("ENDER-CHEST.ROW-PERMISSIONS.ENABLED", true);
        config.set("ENDER-CHEST.ROW-PERMISSIONS.ON-DOWNGRADE", "KEEP-SIZE");
        config.set("ENDER-CHEST.ROW-PERMISSIONS.PERMISSIONS.ultimatedonutsmp.enderchest.rows.vip++", 6);
        config.set("ENDER-CHEST.ROW-PERMISSIONS.PERMISSIONS.ultimatedonutsmp.enderchest.rows.vip+", 5);
        config.set("ENDER-CHEST.ROW-PERMISSIONS.PERMISSIONS.ultimatedonutsmp.enderchest.rows.vip", 4);
        return config;
    }

    @Test
    void defaultRowsUsedWithoutPermissions() throws Exception {
        EnderChestManager manager = createManager(baseConfig());
        Player regular = createMockPlayer(Set.of());

        assertEquals(0, manager.getPermissionRows(regular));
        assertEquals(3, manager.getEntitledRows(regular));
    }

    @Test
    void rowsResolvedFromConfiguredPermissionMap() throws Exception {
        EnderChestManager manager = createManager(baseConfig());

        assertEquals(4, manager.getEntitledRows(
                createMockPlayer(Set.of("ultimatedonutsmp.enderchest.rows.vip"))));
        assertEquals(5, manager.getEntitledRows(
                createMockPlayer(Set.of("ultimatedonutsmp.enderchest.rows.vip+"))));
        assertEquals(6, manager.getEntitledRows(
                createMockPlayer(Set.of("ultimatedonutsmp.enderchest.rows.vip++"))));
    }

    @Test
    void rowsResolvedFromNumberedPermission() throws Exception {
        EnderChestManager manager = createManager(baseConfig());
        Player numbered = createMockPlayer(Set.of("ultimatedonutsmp.enderchest.rows.5"));

        assertEquals(5, manager.getEntitledRows(numbered));
    }

    @Test
    void highestValueWinsAcrossStackedPermissions() throws Exception {
        EnderChestManager manager = createManager(baseConfig());
        Player stacked = createMockPlayer(Set.of(
                "ultimatedonutsmp.enderchest.rows.2",
                "ultimatedonutsmp.enderchest.rows.vip",
                "ultimatedonutsmp.enderchest.rows.4"
        ));

        assertEquals(4, manager.getEntitledRows(stacked));
    }

    @Test
    void permissionRowsCanSitBelowTheDefault() throws Exception {
        EnderChestManager manager = createManager(baseConfig());
        Player limited = createMockPlayer(Set.of("ultimatedonutsmp.enderchest.rows.1"));

        assertEquals(1, manager.getEntitledRows(limited));
    }

    @Test
    void rowsAreClampedToASingleChest() throws Exception {
        YamlConfiguration config = baseConfig();
        config.set("ENDER-CHEST.ROW-PERMISSIONS.PERMISSIONS.ultimatedonutsmp.enderchest.rows.owner", 99);
        EnderChestManager manager = createManager(config);

        assertEquals(6, manager.getEntitledRows(
                createMockPlayer(Set.of("ultimatedonutsmp.enderchest.rows.owner"))));
        assertEquals(6, manager.getEntitledRows(
                createMockPlayer(Set.of("ultimatedonutsmp.enderchest.rows.9"))));
    }

    @Test
    void malformedNumberedPermissionIsIgnored() throws Exception {
        EnderChestManager manager = createManager(baseConfig());
        Player malformed = createMockPlayer(Set.of(
                "ultimatedonutsmp.enderchest.rows.abc",
                "ultimatedonutsmp.enderchest.rows.-2",
                "ultimatedonutsmp.enderchest.rows."
        ));

        assertEquals(3, manager.getEntitledRows(malformed));
    }

    @Test
    void wildcardDoesNotGrantARowTier() throws Exception {
        EnderChestManager manager = createManager(baseConfig());
        Player wildcard = createMockPlayer(Set.of("ultimatedonutsmp.*"));

        assertEquals(3, manager.getEntitledRows(wildcard));
    }

    @Test
    void disabledRowPermissionsFallBackToTheDefault() throws Exception {
        YamlConfiguration config = baseConfig();
        config.set("ENDER-CHEST.ROW-PERMISSIONS.ENABLED", false);
        EnderChestManager manager = createManager(config);
        Player vip = createMockPlayer(Set.of(
                "ultimatedonutsmp.enderchest.rows.vip++",
                "ultimatedonutsmp.enderchest.rows.6"
        ));

        assertFalse(manager.isRowPermissionsEnabled());
        assertEquals(3, manager.getEntitledRows(vip));
    }

    @Test
    void nullPlayerFallsBackToTheDefault() throws Exception {
        EnderChestManager manager = createManager(baseConfig());

        assertEquals(0, manager.getPermissionRows(null));
        assertEquals(3, manager.getEntitledRows(null));
    }

    @Test
    void downgradeModeDefaultsToKeepingTheLargerSize() throws Exception {
        EnderChestManager manager = createManager(baseConfig());
        assertFalse(manager.returnsOverflowOnDowngrade());

        YamlConfiguration returning = baseConfig();
        returning.set("ENDER-CHEST.ROW-PERMISSIONS.ON-DOWNGRADE", "RETURN-ITEMS");
        assertTrue(createManager(returning).returnsOverflowOnDowngrade());
    }

    @Test
    void bundledConfigShipsTheRowPermissionSection() throws Exception {
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.load(new File("src/main/resources/ender-chest.yml"));

        assertTrue(bundled.getBoolean("ENDER-CHEST.ROW-PERMISSIONS.ENABLED"));
        assertEquals("KEEP-SIZE", bundled.getString("ENDER-CHEST.ROW-PERMISSIONS.ON-DOWNGRADE"));
        assertTrue(bundled.isConfigurationSection("ENDER-CHEST.ROW-PERMISSIONS.PERMISSIONS"));
        assertTrue(bundled.getString("MESSAGES.ROWS-DOWNGRADED", "").contains("{rows}"));
    }
}
