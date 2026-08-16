package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RTPRankCooldownTest {

    private Server originalServer;
    private Server mockServer;
    private org.bukkit.World mockWorld;

    @BeforeEach
    void setUp() throws Exception {
        originalServer = Bukkit.getServer();

        mockWorld = (org.bukkit.World) Proxy.newProxyInstance(
                org.bukkit.World.class.getClassLoader(),
                new Class<?>[]{org.bukkit.World.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getName")) {
                        return "world";
                    }
                    if (method.getName().equals("getEnvironment")) {
                        return org.bukkit.World.Environment.NORMAL;
                    }
                    return null;
                }
        );

        mockServer = (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getWorlds")) {
                        return List.of(mockWorld);
                    }
                    if (method.getName().equals("getWorld")) {
                        return mockWorld;
                    }
                    if (method.getName().equals("getLogger")) {
                        return java.util.logging.Logger.getLogger("Minecraft");
                    }
                    return null;
                }
        );

        Field serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        serverField.set(null, mockServer);
    }

    @AfterEach
    void tearDown() throws Exception {
        Field serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        serverField.set(null, originalServer);
    }

    private UltimateDonutSmp createMockPlugin(YamlConfiguration rtpConfig) throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
        Constructor<?> newConstructor = reflectionFactory.newConstructorForSerialization(UltimateDonutSmp.class, objectConstructor);
        UltimateDonutSmp plugin = (UltimateDonutSmp) newConstructor.newInstance();

        ConfigManager configManager = new ConfigManager(plugin);
        Field rtpField = ConfigManager.class.getDeclaredField("rtp");
        rtpField.setAccessible(true);
        rtpField.set(configManager, rtpConfig);

        Field configField = ConfigManager.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(configManager, new YamlConfiguration());

        Field soundsField = ConfigManager.class.getDeclaredField("sounds");
        soundsField.setAccessible(true);
        soundsField.set(configManager, new YamlConfiguration());

        Field cmField = UltimateDonutSmp.class.getDeclaredField("configManager");
        cmField.setAccessible(true);
        cmField.set(plugin, configManager);

        FeatureManager featureManager = new FeatureManager(plugin);
        Field fmField = UltimateDonutSmp.class.getDeclaredField("featureManager");
        fmField.setAccessible(true);
        fmField.set(plugin, featureManager);

        TeleportManager teleportManager = new TeleportManager(plugin);
        Field tmField = UltimateDonutSmp.class.getDeclaredField("teleportManager");
        tmField.setAccessible(true);
        tmField.set(plugin, teleportManager);

        return plugin;
    }

    private Player createMockPlayer(UUID uuid, String name, Set<String> permissions) {
        final Player[] playerHolder = new Player[1];
        Player playerProxy = (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getUniqueId")) {
                        return uuid;
                    }
                    if (method.getName().equals("getName")) {
                        return name;
                    }
                    if (method.getName().equals("hasPermission")) {
                        String perm = (String) args[0];
                        return permissions.contains(perm.toLowerCase(Locale.ROOT));
                    }
                    if (method.getName().equals("getEffectivePermissions")) {
                        Set<PermissionAttachmentInfo> effective = new HashSet<>();
                        for (String perm : permissions) {
                            effective.add(new PermissionAttachmentInfo(playerHolder[0], perm, null, true));
                        }
                        return effective;
                    }
                    if (method.getName().equals("sendMessage")) {
                        return null;
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
        YamlConfiguration rtpConfig = new YamlConfiguration();
        rtpConfig.set("WORLD-SETTINGS.world.MAX-RADIUS", 5000);
        rtpConfig.set("WORLD-SETTINGS.world.COOLDOWN", 30);
        rtpConfig.set("SETTINGS.RANK-COOLDOWNS.ENABLED", true);
        rtpConfig.set("SETTINGS.RANK-COOLDOWNS.PERMISSIONS.ultimatedonutsmp.rtp.cooldown.vip++", 3);
        rtpConfig.set("SETTINGS.RANK-COOLDOWNS.PERMISSIONS.ultimatedonutsmp.rtp.cooldown.vip+", 10);
        rtpConfig.set("SETTINGS.RANK-COOLDOWNS.PERMISSIONS.ultimatedonutsmp.rtp.cooldown.vip", 15);
        return rtpConfig;
    }

    @Test
    void testWorldCooldownUsedWithoutPermissions() throws Exception {
        RTPManager rtpManager = new RTPManager(createMockPlugin(baseConfig()));
        Player regular = createMockPlayer(UUID.randomUUID(), "Regular", Set.of());

        assertEquals(30, rtpManager.getPlayerCooldownSeconds(regular, "world"));
        assertEquals(30, rtpManager.getWorldCooldownSeconds("world"));
    }

    @Test
    void testCooldownResolvedFromConfiguredPermissionMap() throws Exception {
        RTPManager rtpManager = new RTPManager(createMockPlugin(baseConfig()));

        Player vip = createMockPlayer(UUID.randomUUID(), "VIP", Set.of("ultimatedonutsmp.rtp.cooldown.vip"));
        Player vipPlus = createMockPlayer(UUID.randomUUID(), "VIPPlus", Set.of("ultimatedonutsmp.rtp.cooldown.vip+"));
        Player vipPlusPlus = createMockPlayer(UUID.randomUUID(), "VIPPlusPlus", Set.of("ultimatedonutsmp.rtp.cooldown.vip++"));

        assertEquals(15, rtpManager.getPlayerCooldownSeconds(vip, "world"));
        assertEquals(10, rtpManager.getPlayerCooldownSeconds(vipPlus, "world"));
        assertEquals(3, rtpManager.getPlayerCooldownSeconds(vipPlusPlus, "world"));
    }

    @Test
    void testCooldownResolvedFromNumberedPermission() throws Exception {
        RTPManager rtpManager = new RTPManager(createMockPlugin(baseConfig()));

        Player numeric = createMockPlayer(UUID.randomUUID(), "Numeric", Set.of("ultimatedonutsmp.rtp.cooldown.7"));
        assertEquals(7, rtpManager.getPlayerCooldownSeconds(numeric, "world"));
    }

    @Test
    void testLowestValueWinsAcrossStackedPermissions() throws Exception {
        RTPManager rtpManager = new RTPManager(createMockPlugin(baseConfig()));

        Player stacked = createMockPlayer(UUID.randomUUID(), "Stacked", Set.of(
                "ultimatedonutsmp.rtp.cooldown.vip",
                "ultimatedonutsmp.rtp.cooldown.vip+",
                "ultimatedonutsmp.rtp.cooldown.5"
        ));
        assertEquals(5, rtpManager.getPlayerCooldownSeconds(stacked, "world"));
    }

    @Test
    void testPermissionOverridesWorldCooldownInBothDirections() throws Exception {
        RTPManager rtpManager = new RTPManager(createMockPlugin(baseConfig()));

        Player slower = createMockPlayer(UUID.randomUUID(), "Slower", Set.of("ultimatedonutsmp.rtp.cooldown.60"));
        assertEquals(60, rtpManager.getPlayerCooldownSeconds(slower, "world"));

        Player faster = createMockPlayer(UUID.randomUUID(), "Faster", Set.of("ultimatedonutsmp.rtp.cooldown.5"));
        assertEquals(5, rtpManager.getPlayerCooldownSeconds(faster, "world"));
    }

    @Test
    void testZeroPermissionRemovesCooldownEntirely() throws Exception {
        RTPManager rtpManager = new RTPManager(createMockPlugin(baseConfig()));

        Player bypass = createMockPlayer(UUID.randomUUID(), "Bypass", Set.of("ultimatedonutsmp.rtp.cooldown.0"));
        assertEquals(0, rtpManager.getPlayerCooldownSeconds(bypass, "world"));
    }

    @Test
    void testMalformedNumberedPermissionIsIgnored() throws Exception {
        RTPManager rtpManager = new RTPManager(createMockPlugin(baseConfig()));

        Player malformed = createMockPlayer(UUID.randomUUID(), "Malformed", Set.of(
                "ultimatedonutsmp.rtp.cooldown.abc",
                "ultimatedonutsmp.rtp.cooldown.-5",
                "ultimatedonutsmp.rtp.cooldown."
        ));
        assertEquals(30, rtpManager.getPlayerCooldownSeconds(malformed, "world"));
    }

    @Test
    void testDisabledRankCooldownsFallBackToWorldSetting() throws Exception {
        YamlConfiguration rtpConfig = baseConfig();
        rtpConfig.set("SETTINGS.RANK-COOLDOWNS.ENABLED", false);
        RTPManager rtpManager = new RTPManager(createMockPlugin(rtpConfig));

        Player vipPlusPlus = createMockPlayer(UUID.randomUUID(), "VIPPlusPlus", Set.of(
                "ultimatedonutsmp.rtp.cooldown.vip++",
                "ultimatedonutsmp.rtp.cooldown.1"
        ));
        assertFalse(rtpManager.isRankCooldownsEnabled());
        assertEquals(30, rtpManager.getPlayerCooldownSeconds(vipPlusPlus, "world"));
    }

    @Test
    void testNullPlayerFallsBackToWorldSetting() throws Exception {
        RTPManager rtpManager = new RTPManager(createMockPlugin(baseConfig()));
        assertEquals(30, rtpManager.getPlayerCooldownSeconds(null, "world"));
    }
}
