package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.SpigotScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitScheduler;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RTPManagerTest {

    private Server originalServer;
    private Server mockServer;
    private List<World> mockWorlds;
    private AtomicInteger scheduledTasks;

    @BeforeEach
    void setUp() throws Exception {
        originalServer = Bukkit.getServer();
        mockWorlds = new ArrayList<>();
        scheduledTasks = new AtomicInteger();

        Object mockScheduler = Proxy.newProxyInstance(
                BukkitScheduler.class.getClassLoader(),
                new Class<?>[]{BukkitScheduler.class},
                (proxy, method, args) -> {
                    if (method.getName().startsWith("runTask")) {
                        scheduledTasks.incrementAndGet();
                    }
                    return null;
                }
        );

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
                    if (method.getName().equals("getOnlinePlayers")) {
                        return List.of();
                    }
                    if (method.getName().equals("getScheduler")) {
                        return mockScheduler;
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

    /** Gives the mock plugin the two collaborators the background pre-cache needs to run. */
    private void attachSchedulerAndFeatures(UltimateDonutSmp plugin) throws Exception {
        Field featureField = UltimateDonutSmp.class.getDeclaredField("featureManager");
        featureField.setAccessible(true);
        featureField.set(plugin, new FeatureManager(plugin));

        Field schedulerField = UltimateDonutSmp.class.getDeclaredField("SpigotScheduler");
        schedulerField.setAccessible(true);
        schedulerField.set(plugin, new SpigotScheduler(plugin));
    }

    private YamlConfiguration preCacheRtpConfig() {
        YamlConfiguration rtpConfig = overworldRtpConfig();
        rtpConfig.set("SETTINGS.SEARCH-ATTEMPTS-PER-TICK", 25);
        rtpConfig.set("SETTINGS.LOCATION-CACHE.ENABLED", true);
        rtpConfig.set("SETTINGS.LOCATION-CACHE.SIZE", 5);
        rtpConfig.set("RTP-MENU.BUTTONS.OVERWORLD.SLOT", 11);
        rtpConfig.set("RTP-MENU.BUTTONS.OVERWORLD.WORLD", "world");
        rtpConfig.set("RTP-MENU.BUTTONS.OVERWORLD.ENABLED", true);
        return rtpConfig;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Queue<Long>> preCacheInFlight(RTPManager rtpManager) throws Exception {
        Field field = RTPManager.class.getDeclaredField("preCacheInFlight");
        field.setAccessible(true);
        return (Map<String, Queue<Long>>) field.get(rtpManager);
    }

    private Queue<Long> searchesInFlight(RTPManager rtpManager, String worldKey) throws Exception {
        Queue<Long> running = preCacheInFlight(rtpManager).get(worldKey);
        return running == null ? new ConcurrentLinkedQueue<>() : running;
    }

    private void seedSearchesInFlight(RTPManager rtpManager, String worldKey, long... startedAt) throws Exception {
        Queue<Long> running = new ConcurrentLinkedQueue<>();
        for (long value : startedAt) {
            running.add(value);
        }
        preCacheInFlight(rtpManager).put(worldKey, running);
    }

    @Test
    void testPreCacheSearchesToStartFillsTheCacheWithinTheConcurrencyLimit() {
        assertEquals(2, RTPManager.preCacheSearchesToStart(5, 0, 0, 2));
        assertEquals(1, RTPManager.preCacheSearchesToStart(5, 0, 1, 2));
        assertEquals(0, RTPManager.preCacheSearchesToStart(5, 0, 2, 2));
        assertEquals(1, RTPManager.preCacheSearchesToStart(2, 1, 0, 2));
        assertEquals(0, RTPManager.preCacheSearchesToStart(2, 2, 0, 2));
        assertEquals(0, RTPManager.preCacheSearchesToStart(0, 0, 0, 2));
    }

    @Test
    void testPreCacheWarmsUpWhileTheServerIsEmpty() throws Exception {
        createMockWorld("world", World.Environment.NORMAL);
        UltimateDonutSmp plugin = createMockPlugin(preCacheRtpConfig());
        attachSchedulerAndFeatures(plugin);

        RTPManager rtpManager = new RTPManager(plugin);

        assertEquals(0, Bukkit.getOnlinePlayers().size());
        assertEquals(2, searchesInFlight(rtpManager, "world").size());
        assertTrue(scheduledTasks.get() > 0);

        rtpManager.refillPreCacheAllWorlds();
        assertEquals(2, searchesInFlight(rtpManager, "world").size());
    }

    @Test
    void testPreCacheReleasesSearchesThatNeverFinished() throws Exception {
        createMockWorld("world", World.Environment.NORMAL);
        UltimateDonutSmp plugin = createMockPlugin(preCacheRtpConfig());
        attachSchedulerAndFeatures(plugin);
        RTPManager rtpManager = new RTPManager(plugin);

        long stale = System.currentTimeMillis() - 120_000L;
        seedSearchesInFlight(rtpManager, "world", stale, stale);

        rtpManager.refillPreCacheAllWorlds();

        Queue<Long> running = searchesInFlight(rtpManager, "world");
        assertEquals(2, running.size());
        for (long startedAt : running) {
            assertTrue(startedAt > stale, "a stale search should have been replaced by a fresh one");
        }
    }

    @Test
    void testPreCacheLeavesRunningSearchesAlone() throws Exception {
        createMockWorld("world", World.Environment.NORMAL);
        UltimateDonutSmp plugin = createMockPlugin(preCacheRtpConfig());
        attachSchedulerAndFeatures(plugin);
        RTPManager rtpManager = new RTPManager(plugin);

        long now = System.currentTimeMillis();
        seedSearchesInFlight(rtpManager, "world", now, now);

        rtpManager.refillPreCacheAllWorlds();

        assertEquals(List.of(now, now), new ArrayList<>(searchesInFlight(rtpManager, "world")));
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
