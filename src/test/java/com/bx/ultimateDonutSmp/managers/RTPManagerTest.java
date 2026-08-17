package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
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
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @SuppressWarnings("unchecked")
    private void seedLocationCache(RTPManager rtpManager, String worldName, Location location, long cachedAtMillis)
            throws Exception {
        Class<?> cachedLocationClass = Class.forName("com.bx.ultimateDonutSmp.managers.RTPManager$CachedLocation");
        Constructor<?> cachedLocationConstructor = cachedLocationClass.getDeclaredConstructor(Location.class, long.class);
        cachedLocationConstructor.setAccessible(true);
        Object entry = cachedLocationConstructor.newInstance(location, cachedAtMillis);

        Field cacheField = RTPManager.class.getDeclaredField("locationPreCache");
        cacheField.setAccessible(true);
        Map<String, Queue<Object>> cache = (Map<String, Queue<Object>>) cacheField.get(rtpManager);
        Queue<Object> queue = new ConcurrentLinkedQueue<>();
        queue.add(entry);
        cache.put(worldName.toLowerCase(java.util.Locale.ROOT), queue);
    }

    private Location pollCachedLocation(RTPManager rtpManager, String worldName) throws Exception {
        Method pollPreCachedLocation = RTPManager.class.getDeclaredMethod("pollPreCachedLocation", String.class);
        pollPreCachedLocation.setAccessible(true);
        return (Location) pollPreCachedLocation.invoke(rtpManager, worldName);
    }

    private YamlConfiguration overworldRtpConfig() {
        YamlConfiguration rtpConfig = new YamlConfiguration();
        rtpConfig.set("WORLD-SETTINGS.world.MIN-RADIUS", 500);
        rtpConfig.set("WORLD-SETTINGS.world.MAX-RADIUS", 5000);
        return rtpConfig;
    }

    @Test
    void testSearchAttemptsPerTickReadsConfiguredValue() throws Exception {
        YamlConfiguration rtpConfig = new YamlConfiguration();
        RTPManager rtpManager = new RTPManager(createMockPlugin(rtpConfig));
        assertEquals(1, rtpManager.getSearchAttemptsPerTick());

        rtpConfig.set("SETTINGS.SEARCH-ATTEMPTS-PER-TICK", 8);
        assertEquals(8, rtpManager.getSearchAttemptsPerTick());

        rtpConfig.set("SETTINGS.SEARCH-ATTEMPTS-PER-TICK", 0);
        assertEquals(1, rtpManager.getSearchAttemptsPerTick());
    }

    @Test
    void testPreCacheSizeIsClampedAndRespectsToggle() throws Exception {
        YamlConfiguration rtpConfig = new YamlConfiguration();
        RTPManager rtpManager = new RTPManager(createMockPlugin(rtpConfig));
        assertEquals(3, rtpManager.getPreCacheSize());

        rtpConfig.set("SETTINGS.LOCATION-CACHE.SIZE", 64);
        assertEquals(16, rtpManager.getPreCacheSize());

        rtpConfig.set("SETTINGS.LOCATION-CACHE.SIZE", -4);
        assertEquals(0, rtpManager.getPreCacheSize());

        rtpConfig.set("SETTINGS.LOCATION-CACHE.SIZE", 5);
        rtpConfig.set("SETTINGS.LOCATION-CACHE.ENABLED", false);
        assertEquals(0, rtpManager.getPreCacheSize());
    }

    @Test
    void testPollPreCachedLocationReturnsUsableEntry() throws Exception {
        World world = createMockWorld("world", World.Environment.NORMAL);
        RTPManager rtpManager = new RTPManager(createMockPlugin(overworldRtpConfig()));

        Location cached = new Location(world, 1000.5, 70.0, 1000.5);
        seedLocationCache(rtpManager, "world", cached, System.currentTimeMillis());

        Location polled = pollCachedLocation(rtpManager, "world");
        assertEquals(cached, polled);
        assertSame(world, polled.getWorld());
        assertNull(pollCachedLocation(rtpManager, "world"));
    }

    @Test
    void testPollPreCachedLocationDropsEntryOutsideRadius() throws Exception {
        World world = createMockWorld("world", World.Environment.NORMAL);
        RTPManager rtpManager = new RTPManager(createMockPlugin(overworldRtpConfig()));

        seedLocationCache(rtpManager, "world", new Location(world, 100.5, 70.0, 100.5), System.currentTimeMillis());

        assertNull(pollCachedLocation(rtpManager, "world"));
    }

    @Test
    void testPollPreCachedLocationDropsExpiredEntry() throws Exception {
        World world = createMockWorld("world", World.Environment.NORMAL);
        YamlConfiguration rtpConfig = overworldRtpConfig();
        rtpConfig.set("SETTINGS.LOCATION-CACHE.MAX-AGE-SECONDS", 60);
        RTPManager rtpManager = new RTPManager(createMockPlugin(rtpConfig));

        Location cached = new Location(world, 1000.5, 70.0, 1000.5);
        seedLocationCache(rtpManager, "world", cached, System.currentTimeMillis() - 120_000L);

        assertNull(pollCachedLocation(rtpManager, "world"));
    }

    @Test
    void testPollPreCachedLocationIgnoresCacheWhenDisabled() throws Exception {
        World world = createMockWorld("world", World.Environment.NORMAL);
        YamlConfiguration rtpConfig = overworldRtpConfig();
        rtpConfig.set("SETTINGS.LOCATION-CACHE.ENABLED", false);
        RTPManager rtpManager = new RTPManager(createMockPlugin(rtpConfig));

        seedLocationCache(rtpManager, "world", new Location(world, 1000.5, 70.0, 1000.5), System.currentTimeMillis());

        assertNull(pollCachedLocation(rtpManager, "world"));
    }

    private Chunk createMockChunk(World world, int x, int z) {
        return (Chunk) Proxy.newProxyInstance(
                Chunk.class.getClassLoader(),
                new Class<?>[]{Chunk.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getX" -> x;
                    case "getZ" -> z;
                    case "getWorld" -> world;
                    default -> null;
                }
        );
    }

    /**
     * A world that reports the given loaded chunks and records every column the search probes, so a
     * test can prove the probe never reads outside the chunk it was handed. The recorded height sits
     * below the minimum so the probe stops before it reaches any block.
     */
    private World createProbeMockWorld(String name, List<int[]> loadedChunkCoords, List<int[]> probes) {
        List<Chunk> chunks = new ArrayList<>();
        World mockWorld = (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getName":
                            return name;
                        case "getEnvironment":
                            return World.Environment.NORMAL;
                        case "getLoadedChunks":
                            return chunks.toArray(new Chunk[0]);
                        case "getMinHeight":
                            return 0;
                        case "getHighestBlockYAt":
                            if (args != null && args.length == 2 && args[0] instanceof Integer x
                                    && args[1] instanceof Integer z) {
                                probes.add(new int[]{x, z});
                            }
                            return -1;
                        default:
                            return null;
                    }
                }
        );
        for (int[] coords : loadedChunkCoords) {
            chunks.add(createMockChunk(mockWorld, coords[0], coords[1]));
        }
        mockWorlds.add(mockWorld);
        return mockWorld;
    }

    @Test
    void testNextLoadedChunkSampleReturnsNullWhenNothingIsLoaded() throws Exception {
        World world = createProbeMockWorld("world", List.of(), new ArrayList<>());
        RTPManager rtpManager = new RTPManager(createMockPlugin(overworldRtpConfig()));

        assertNull(rtpManager.nextLoadedChunkSample(world));
    }

    @Test
    void testNextLoadedChunkSampleReportsTheChunkCoordinates() throws Exception {
        World world = createProbeMockWorld("world", List.of(new int[]{666, 339}), new ArrayList<>());
        RTPManager rtpManager = new RTPManager(createMockPlugin(overworldRtpConfig()));

        int[] sample = rtpManager.nextLoadedChunkSample(world);

        assertNotNull(sample);
        assertEquals(666, sample[0]);
        assertEquals(339, sample[1]);
    }

    @Test
    void testLoadedChunkProbeNeverLeavesTheChunkItWasGiven() throws Exception {
        List<int[]> probes = new ArrayList<>();
        // A second loaded chunk sitting in a different region. The probe must never touch it.
        createProbeMockWorld("world", List.of(new int[]{666, 339}, new int[]{10, 10}), probes);
        RTPManager rtpManager = new RTPManager(createMockPlugin(overworldRtpConfig()));

        Method probe = RTPManager.class.getDeclaredMethod(
                "tryLoadedChunkLocationAttempt", RTPManager.SearchSettings.class, int.class, int.class);
        probe.setAccessible(true);
        RTPManager.SearchSettings settings = rtpManager.getWorldSearchSettings("world");

        for (int run = 0; run < 200; run++) {
            probe.invoke(rtpManager, settings, 666, 339);
        }

        assertEquals(200, probes.size());
        for (int[] probed : probes) {
            assertTrue(probed[0] >= 666 * 16 && probed[0] < 666 * 16 + 16,
                    "probe read x " + probed[0] + ", which is outside chunk 666");
            assertTrue(probed[1] >= 339 * 16 && probed[1] < 339 * 16 + 16,
                    "probe read z " + probed[1] + ", which is outside chunk 339");
        }
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
