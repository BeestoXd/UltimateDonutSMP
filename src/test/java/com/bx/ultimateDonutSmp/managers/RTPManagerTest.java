package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RTPManagerTest {

    private Server originalServer;
    private Server mockServer;
    private List<World> mockWorlds;

    @BeforeEach
    void setUp() throws Exception {
        originalServer = Bukkit.getServer();
        mockWorlds = new ArrayList<>();

        mockServer = (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getWorlds")) {
                        return mockWorlds;
                    }
                    if (method.getName().equals("getWorld")) {
                        String name = (String) args[0];
                        for (World world : mockWorlds) {
                            if (world.getName().equalsIgnoreCase(name)) {
                                return world;
                            }
                        }
                        return null;
                    }
                    if (method.getName().equals("getWorldContainer")) {
                        return new java.io.File(".");
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

    private World createMockWorld(String name, World.Environment environment) {
        World mockWorld = (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getName")) {
                        return name;
                    }
                    if (method.getName().equals("getEnvironment")) {
                        return environment;
                    }
                    return null;
                }
        );
        mockWorlds.add(mockWorld);
        return mockWorld;
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

        Field cmField = UltimateDonutSmp.class.getDeclaredField("configManager");
        cmField.setAccessible(true);
        cmField.set(plugin, configManager);

        return plugin;
    }

    @Test
    void testGetLoadedNormalWorldNameExcludesSpawnHubDenied() throws Exception {
        createMockWorld("afk", World.Environment.NORMAL);
        createMockWorld("spawn", World.Environment.NORMAL);
        createMockWorld("hub", World.Environment.NORMAL);
        createMockWorld("lobby", World.Environment.NORMAL);
        createMockWorld("survival", World.Environment.NORMAL);

        YamlConfiguration rtpConfig = new YamlConfiguration();
        rtpConfig.set("DENIED-WORLDS", List.of("lobby", "afk"));
        UltimateDonutSmp plugin = createMockPlugin(rtpConfig);

        RTPManager rtpManager = new RTPManager(plugin);

        Method getLoadedNormalWorldName = RTPManager.class.getDeclaredMethod("getLoadedNormalWorldName");
        getLoadedNormalWorldName.setAccessible(true);

        String normalWorld = (String) getLoadedNormalWorldName.invoke(rtpManager);
        assertEquals("survival", normalWorld);
    }

    @Test
    void testGetLoadedNormalWorldNameWithDeniedNormalWorld() throws Exception {
        createMockWorld("world", World.Environment.NORMAL); // denied world
        createMockWorld("smp", World.Environment.NORMAL); // normal world

        YamlConfiguration rtpConfig = new YamlConfiguration();
        rtpConfig.set("DENIED-WORLDS", List.of("world"));
        UltimateDonutSmp plugin = createMockPlugin(rtpConfig);

        RTPManager rtpManager = new RTPManager(plugin);

        Method getLoadedNormalWorldName = RTPManager.class.getDeclaredMethod("getLoadedNormalWorldName");
        getLoadedNormalWorldName.setAccessible(true);

        String normalWorld = (String) getLoadedNormalWorldName.invoke(rtpManager);
        assertEquals("smp", normalWorld);
    }
}
