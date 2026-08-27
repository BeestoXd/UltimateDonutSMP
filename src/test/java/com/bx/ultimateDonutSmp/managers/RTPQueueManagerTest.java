package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RTPQueueManagerTest {

    private Server originalServer;
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

        Server mockServer = (Server) Proxy.newProxyInstance(
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

    private YamlConfiguration rtpConfig(int matchSize) {
        YamlConfiguration rtpConfig = new YamlConfiguration();
        rtpConfig.set("ENABLED", true);
        rtpConfig.set("QUEUE.ENABLED", true);
        rtpConfig.set("QUEUE.MATCH-SIZE", matchSize);
        rtpConfig.set("QUEUE.WORLD", "world");
        rtpConfig.set("WORLD-SETTINGS.world.MIN-RADIUS", 500);
        rtpConfig.set("WORLD-SETTINGS.world.MAX-RADIUS", 5000);
        return rtpConfig;
    }

    private RTPQueueManager createQueueManager(YamlConfiguration rtpConfig) throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
        Constructor<?> newConstructor =
                reflectionFactory.newConstructorForSerialization(UltimateDonutSmp.class, objectConstructor);
        UltimateDonutSmp plugin = (UltimateDonutSmp) newConstructor.newInstance();

        ConfigManager configManager = new ConfigManager(plugin);
        setField(ConfigManager.class, configManager, "rtp", rtpConfig);
        setField(ConfigManager.class, configManager, "config", new YamlConfiguration());
        setField(ConfigManager.class, configManager, "sounds", new YamlConfiguration());
        setField(UltimateDonutSmp.class, plugin, "configManager", configManager);
        setField(UltimateDonutSmp.class, plugin, "featureManager", new FeatureManager(plugin));
        setField(UltimateDonutSmp.class, plugin, "teleportManager", new TeleportManager(plugin));
        setField(UltimateDonutSmp.class, plugin, "rtpManager", new RTPManager(plugin));

        RTPQueueManager queueManager = new RTPQueueManager(plugin);
        setField(UltimateDonutSmp.class, plugin, "rtpQueueManager", queueManager);
        return queueManager;
    }

    private static void setField(Class<?> owner, Object target, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Player createMockPlayer(UUID uuid, String name) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> uuid;
                    case "getName" -> name;
                    case "isOnline" -> true;
                    case "hasPermission" -> false;
                    default -> null;
                }
        );
    }

    @Test
    void queueKeepsJoinOrderAndReportsPositions() throws Exception {
        RTPQueueManager queueManager = createQueueManager(rtpConfig(4));

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        UUID thirdId = UUID.randomUUID();

        assertTrue(queueManager.join(createMockPlayer(firstId, "First")));
        assertTrue(queueManager.join(createMockPlayer(secondId, "Second")));
        assertTrue(queueManager.join(createMockPlayer(thirdId, "Third")));

        assertEquals(3, queueManager.getQueueSize());
        assertEquals(1, queueManager.getQueuePosition(firstId));
        assertEquals(2, queueManager.getQueuePosition(secondId));
        assertEquals(3, queueManager.getQueuePosition(thirdId));
        assertTrue(queueManager.isInQueue(secondId));
    }

    @Test
    void joiningTwiceKeepsTheOriginalPlace() throws Exception {
        RTPQueueManager queueManager = createQueueManager(rtpConfig(4));

        UUID playerId = UUID.randomUUID();
        Player player = createMockPlayer(playerId, "First");

        assertTrue(queueManager.join(player));
        assertFalse(queueManager.join(player));
        assertEquals(1, queueManager.getQueueSize());
        assertEquals(1, queueManager.getQueuePosition(playerId));
    }

    @Test
    void leavingRemovesOnlyThatPlayer() throws Exception {
        RTPQueueManager queueManager = createQueueManager(rtpConfig(4));

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        Player first = createMockPlayer(firstId, "First");

        queueManager.join(first);
        queueManager.join(createMockPlayer(secondId, "Second"));

        assertTrue(queueManager.leave(first));
        assertFalse(queueManager.leave(first));
        assertFalse(queueManager.isInQueue(firstId));
        assertEquals(1, queueManager.getQueueSize());
        assertEquals(1, queueManager.getQueuePosition(secondId));
    }

    @Test
    void disconnectingDropsPlayerFromTheQueue() throws Exception {
        RTPQueueManager queueManager = createQueueManager(rtpConfig(4));

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        queueManager.join(createMockPlayer(firstId, "First"));
        queueManager.join(createMockPlayer(secondId, "Second"));
        queueManager.handleQuit(firstId);

        assertFalse(queueManager.isInQueue(firstId));
        assertEquals(1, queueManager.getQueueSize());
    }

    @Test
    void matchPartyOnlyDrainsOnceTheQueueIsLongEnough() throws Exception {
        YamlConfiguration rtpConfig = rtpConfig(4);
        RTPQueueManager queueManager = createQueueManager(rtpConfig);

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        UUID thirdId = UUID.randomUUID();

        queueManager.join(createMockPlayer(firstId, "First"));
        queueManager.join(createMockPlayer(secondId, "Second"));
        queueManager.join(createMockPlayer(thirdId, "Third"));

        assertTrue(queueManager.pollMatchParty().isEmpty());
        assertEquals(3, queueManager.getQueueSize());

        rtpConfig.set("QUEUE.MATCH-SIZE", 2);
        assertEquals(List.of(firstId, secondId), queueManager.pollMatchParty());
        assertEquals(1, queueManager.getQueueSize());
        assertEquals(1, queueManager.getQueuePosition(thirdId));
    }

    @Test
    void disabledQueueRefusesToJoin() throws Exception {
        YamlConfiguration rtpConfig = rtpConfig(2);
        rtpConfig.set("QUEUE.ENABLED", false);
        RTPQueueManager queueManager = createQueueManager(rtpConfig);

        assertFalse(queueManager.isEnabled());
        assertFalse(queueManager.join(createMockPlayer(UUID.randomUUID(), "First")));
        assertEquals(0, queueManager.getQueueSize());
    }

    @Test
    void queueWithoutWorldSettingsRefusesToJoin() throws Exception {
        YamlConfiguration rtpConfig = rtpConfig(2);
        rtpConfig.set("WORLD-SETTINGS", null);
        RTPQueueManager queueManager = createQueueManager(rtpConfig);

        assertFalse(queueManager.join(createMockPlayer(UUID.randomUUID(), "First")));
        assertEquals(0, queueManager.getQueueSize());
    }

    @Test
    void matchSizeAndSpreadRadiusStayInsideTheirLimits() throws Exception {
        YamlConfiguration rtpConfig = rtpConfig(1);
        rtpConfig.set("QUEUE.SPREAD-RADIUS", -5);
        RTPQueueManager queueManager = createQueueManager(rtpConfig);

        assertEquals(2, queueManager.getMatchSize());
        assertEquals(0, queueManager.getSpreadRadius());

        rtpConfig.set("QUEUE.MATCH-SIZE", 999);
        rtpConfig.set("QUEUE.SPREAD-RADIUS", 9999);
        assertEquals(32, queueManager.getMatchSize());
        assertEquals(512, queueManager.getSpreadRadius());
    }
}
