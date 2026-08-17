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

class HomePermissionTest {

    private HomeManager createManager(YamlConfiguration mainConfig) throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

        Constructor<?> pluginConstructor = reflectionFactory
                .newConstructorForSerialization(UltimateDonutSmp.class, objectConstructor);
        UltimateDonutSmp plugin = (UltimateDonutSmp) pluginConstructor.newInstance();

        ConfigManager configManager = new ConfigManager(plugin);
        Field configField = ConfigManager.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(configManager, mainConfig);

        Field configManagerField = UltimateDonutSmp.class.getDeclaredField("configManager");
        configManagerField.setAccessible(true);
        configManagerField.set(plugin, configManager);

        return new HomeManager(plugin);
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
        config.set("SETTINGS.HOME-DEFAULT", 2);
        config.set("SETTINGS.HOME-PERMISSIONS.ENABLED", true);
        config.set("SETTINGS.HOME-PERMISSIONS.PERMISSIONS.ultimatedonutsmp.homes.vip++", 15);
        config.set("SETTINGS.HOME-PERMISSIONS.PERMISSIONS.ultimatedonutsmp.homes.vip+", 10);
        config.set("SETTINGS.HOME-PERMISSIONS.PERMISSIONS.ultimatedonutsmp.homes.vip", 5);
        return config;
    }

    @Test
    void defaultHomesUsedWithoutPermissions() throws Exception {
        HomeManager manager = createManager(baseConfig());
        Player regular = createMockPlayer(Set.of());

        assertEquals(0, manager.getPermissionHomes(regular));
        assertEquals(2, manager.getMaxHomes(regular));
    }

    @Test
    void homesResolvedFromConfiguredPermissionMap() throws Exception {
        HomeManager manager = createManager(baseConfig());

        assertEquals(5, manager.getMaxHomes(
                createMockPlayer(Set.of("ultimatedonutsmp.homes.vip"))));
        assertEquals(10, manager.getMaxHomes(
                createMockPlayer(Set.of("ultimatedonutsmp.homes.vip+"))));
        assertEquals(15, manager.getMaxHomes(
                createMockPlayer(Set.of("ultimatedonutsmp.homes.vip++"))));
    }

    @Test
    void homesResolvedFromNumberedPermission() throws Exception {
        HomeManager manager = createManager(baseConfig());
        Player numbered = createMockPlayer(Set.of("ultimatedonutsmp.homes.7"));

        assertEquals(7, manager.getMaxHomes(numbered));
    }

    @Test
    void pagePermissionGrantsFiveHomesEach() throws Exception {
        HomeManager manager = createManager(baseConfig());
        Player paged = createMockPlayer(Set.of("ultimatedonutsmp.homes.page.3"));

        assertEquals(15, manager.getMaxHomes(paged));
        assertEquals(3, manager.getMaxHomePages(paged));
    }

    @Test
    void highestValueWinsAcrossStackedPermissions() throws Exception {
        HomeManager manager = createManager(baseConfig());
        Player stacked = createMockPlayer(Set.of(
                "ultimatedonutsmp.homes.3",
                "ultimatedonutsmp.homes.vip",
                "ultimatedonutsmp.homes.page.2"
        ));

        assertEquals(10, manager.getMaxHomes(stacked));
    }

    @Test
    void permissionHomesCanSitBelowTheDefault() throws Exception {
        HomeManager manager = createManager(baseConfig());
        Player limited = createMockPlayer(Set.of("ultimatedonutsmp.homes.1"));

        assertEquals(1, manager.getMaxHomes(limited));
    }

    @Test
    void malformedNumberedPermissionIsIgnored() throws Exception {
        HomeManager manager = createManager(baseConfig());
        Player malformed = createMockPlayer(Set.of(
                "ultimatedonutsmp.homes.abc",
                "ultimatedonutsmp.homes.-2",
                "ultimatedonutsmp.homes."
        ));

        assertEquals(2, manager.getMaxHomes(malformed));
    }

    @Test
    void wildcardDoesNotGrantAHomeTier() throws Exception {
        HomeManager manager = createManager(baseConfig());
        Player wildcard = createMockPlayer(Set.of("ultimatedonutsmp.*"));

        assertEquals(2, manager.getMaxHomes(wildcard));
    }

    @Test
    void disabledHomePermissionsFallBackToTheDefault() throws Exception {
        YamlConfiguration config = baseConfig();
        config.set("SETTINGS.HOME-PERMISSIONS.ENABLED", false);
        HomeManager manager = createManager(config);
        Player vip = createMockPlayer(Set.of(
                "ultimatedonutsmp.homes.vip++",
                "ultimatedonutsmp.homes.20"
        ));

        assertFalse(manager.isHomePermissionsEnabled());
        assertEquals(2, manager.getMaxHomes(vip));
    }

    @Test
    void nullPlayerFallsBackToTheDefault() throws Exception {
        HomeManager manager = createManager(baseConfig());

        assertEquals(0, manager.getPermissionHomes(null));
        assertEquals(2, manager.getMaxHomes(null));
    }

    @Test
    void defaultHomesNeverDropBelowOne() throws Exception {
        YamlConfiguration config = baseConfig();
        config.set("SETTINGS.HOME-DEFAULT", 0);
        HomeManager manager = createManager(config);

        assertEquals(1, manager.getMaxHomes(createMockPlayer(Set.of())));
    }

    @Test
    void bundledConfigShipsTheHomePermissionSection() throws Exception {
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.load(new File("src/main/resources/config.yml"));

        assertTrue(bundled.getBoolean("SETTINGS.HOME-PERMISSIONS.ENABLED"));
        assertTrue(bundled.isConfigurationSection("SETTINGS.HOME-PERMISSIONS.PERMISSIONS"));
        assertEquals(2, bundled.getInt("SETTINGS.HOME-DEFAULT"));
    }
}
