package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.reflect.ReflectionFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RespawnRtpManagerTest {

    private Server originalServer;
    private List<World> mockWorlds;

    @BeforeEach
    void setUp() throws Exception {
        originalServer = Bukkit.getServer();
        mockWorlds = new ArrayList<>();

        Server mockServer = (Server) Proxy.newProxyInstance(
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
                        return new File(".");
                    }
                    if (method.getName().equals("getOnlinePlayers")) {
                        return List.of();
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

    private void createMockWorld(String name) {
        World mockWorld = (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getName")) {
                        return name;
                    }
                    if (method.getName().equals("getEnvironment")) {
                        return World.Environment.NORMAL;
                    }
                    return null;
                }
        );
        mockWorlds.add(mockWorld);
    }

    private UltimateDonutSmp createMockPlugin(YamlConfiguration config, YamlConfiguration rtpConfig) throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
        Constructor<?> newConstructor =
                reflectionFactory.newConstructorForSerialization(UltimateDonutSmp.class, objectConstructor);
        UltimateDonutSmp plugin = (UltimateDonutSmp) newConstructor.newInstance();

        ConfigManager configManager = new ConfigManager(plugin);
        Field rtpField = ConfigManager.class.getDeclaredField("rtp");
        rtpField.setAccessible(true);
        rtpField.set(configManager, rtpConfig);

        Field configField = ConfigManager.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(configManager, config);

        Field cmField = UltimateDonutSmp.class.getDeclaredField("configManager");
        cmField.setAccessible(true);
        cmField.set(plugin, configManager);

        return plugin;
    }

    private YamlConfiguration rtpConfigWithOverworldBounds() {
        YamlConfiguration rtpConfig = new YamlConfiguration();
        rtpConfig.set("WORLD-SETTINGS.world.MIN-RADIUS", 750);
        rtpConfig.set("WORLD-SETTINGS.world.MAX-RADIUS", 4000);
        rtpConfig.set("WORLD-SETTINGS.world.CENTER-X", 100);
        rtpConfig.set("WORLD-SETTINGS.world.CENTER-Z", -200);
        return rtpConfig;
    }

    @Test
    void bundledConfigShipsRespawnRtpDisabledByDefault() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));
        assertTrue(config.contains("RESPAWN-RTP.ENABLED"));
        assertFalse(config.getBoolean("RESPAWN-RTP.ENABLED"));
        assertEquals("", config.getString("RESPAWN-RTP.WORLD.NAME"));
        assertTrue(config.getBoolean("RESPAWN-RTP.WORLD.USE-RTP-BOUNDS"));
        assertEquals(500, config.getInt("RESPAWN-RTP.WORLD.MIN-RADIUS"));
        assertEquals(5000, config.getInt("RESPAWN-RTP.WORLD.MAX-RADIUS"));
    }

    @Test
    void emptyWorldNameFallsBackToTheWorldThePlayerDiedIn() throws Exception {
        createMockWorld("world");
        UltimateDonutSmp plugin = createMockPlugin(new YamlConfiguration(), rtpConfigWithOverworldBounds());

        RTPManager.SearchSettings settings = new RTPManager(plugin).getRespawnSearchSettings("world");

        assertNotNull(settings);
        assertEquals("world", settings.worldName());
    }

    @Test
    void rtpBoundsAreReusedForTheDeathWorldByDefault() throws Exception {
        createMockWorld("world");
        UltimateDonutSmp plugin = createMockPlugin(new YamlConfiguration(), rtpConfigWithOverworldBounds());

        RTPManager.SearchSettings settings = new RTPManager(plugin).getRespawnSearchSettings("world");

        assertNotNull(settings);
        assertEquals(750, settings.minRadius());
        assertEquals(4000, settings.maxRadius());
        assertEquals(100, settings.centerX());
        assertEquals(-200, settings.centerZ());
    }

    @Test
    void ownBoundsWinWhenRtpBoundsAreTurnedOff() throws Exception {
        createMockWorld("world");
        YamlConfiguration config = new YamlConfiguration();
        config.set("RESPAWN-RTP.WORLD.USE-RTP-BOUNDS", false);
        config.set("RESPAWN-RTP.WORLD.MIN-RADIUS", 1500);
        config.set("RESPAWN-RTP.WORLD.MAX-RADIUS", 9000);
        config.set("RESPAWN-RTP.WORLD.CENTER-X", 64);
        config.set("RESPAWN-RTP.WORLD.CENTER-Z", 32);
        UltimateDonutSmp plugin = createMockPlugin(config, rtpConfigWithOverworldBounds());

        RTPManager.SearchSettings settings = new RTPManager(plugin).getRespawnSearchSettings("world");

        assertNotNull(settings);
        assertEquals(1500, settings.minRadius());
        assertEquals(9000, settings.maxRadius());
        assertEquals(64, settings.centerX());
        assertEquals(32, settings.centerZ());
    }

    @Test
    void aCustomOverworldInheritsTheOverworldRtpBounds() throws Exception {
        createMockWorld("resource");
        UltimateDonutSmp plugin = createMockPlugin(new YamlConfiguration(), rtpConfigWithOverworldBounds());

        RTPManager.SearchSettings settings = new RTPManager(plugin).getRespawnSearchSettings("resource");

        assertNotNull(settings);
        assertEquals("resource", settings.worldName());
        assertEquals(750, settings.minRadius());
        assertEquals(4000, settings.maxRadius());
    }

    @Test
    void ownBoundsAreUsedWhenRtpHasNoWorldSettingsAtAll() throws Exception {
        createMockWorld("resource");
        YamlConfiguration config = new YamlConfiguration();
        config.set("RESPAWN-RTP.WORLD.MIN-RADIUS", 200);
        config.set("RESPAWN-RTP.WORLD.MAX-RADIUS", 800);
        UltimateDonutSmp plugin = createMockPlugin(config, new YamlConfiguration());

        RTPManager.SearchSettings settings = new RTPManager(plugin).getRespawnSearchSettings("resource");

        assertNotNull(settings);
        assertEquals("resource", settings.worldName());
        assertEquals(200, settings.minRadius());
        assertEquals(800, settings.maxRadius());
    }

    @Test
    void aConfiguredWorldOverridesTheDeathWorld() throws Exception {
        createMockWorld("world");
        createMockWorld("resource");
        YamlConfiguration config = new YamlConfiguration();
        config.set("RESPAWN-RTP.WORLD.NAME", "resource");
        UltimateDonutSmp plugin = createMockPlugin(config, rtpConfigWithOverworldBounds());

        RTPManager.SearchSettings settings = new RTPManager(plugin).getRespawnSearchSettings("world");

        assertNotNull(settings);
        assertEquals("resource", settings.worldName());
    }

    @Test
    void deniedAndMissingWorldsAreRefused() throws Exception {
        createMockWorld("world");
        createMockWorld("afk");
        YamlConfiguration rtpConfig = rtpConfigWithOverworldBounds();
        rtpConfig.set("DENIED-WORLDS", List.of("afk"));
        UltimateDonutSmp plugin = createMockPlugin(new YamlConfiguration(), rtpConfig);
        RTPManager rtpManager = new RTPManager(plugin);

        assertNull(rtpManager.getRespawnSearchSettings("afk"));
        assertNull(rtpManager.getRespawnSearchSettings("nowhere"));
    }

    @Test
    void anUnknownDeathWorldFallsBackToTheOverworld() throws Exception {
        createMockWorld("world");
        UltimateDonutSmp plugin = createMockPlugin(new YamlConfiguration(), rtpConfigWithOverworldBounds());

        RTPManager.SearchSettings settings = new RTPManager(plugin).getRespawnSearchSettings(null);

        assertNotNull(settings);
        assertEquals("world", settings.worldName());
    }

    @Test
    void placeholdersUseBlockCoordinatesOfTheDestination() {
        Location destination = new Location(null, 128.9, 71.0, -64.2);
        assertEquals(
                "respawned at X:128 Y:71 Z:-65 in ",
                RespawnRtpManager.applyPlaceholders("respawned at X:{x} Y:{y} Z:{z} in {world}", destination)
        );
    }

    @Test
    void placeholdersAreLeftUntouchedWithoutADestination() {
        assertEquals("still searching {x}", RespawnRtpManager.applyPlaceholders("still searching {x}", null));
        assertEquals("", RespawnRtpManager.applyPlaceholders("", null));
        assertEquals("", RespawnRtpManager.applyPlaceholders(null, null));
    }
}
