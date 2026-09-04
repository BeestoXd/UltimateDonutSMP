package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PortalDefinition;
import com.bx.ultimateDonutSmp.models.ServerStatusSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalHologramPlaceholderTest {

    private Server originalServer;
    private Server mockServer;
    private World overworld;
    private World nether;
    private World end;
    private final List<Player> overworldPlayers = new ArrayList<>();
    private final List<Player> netherPlayers = new ArrayList<>();
    private final List<Player> endPlayers = new ArrayList<>();
    private final List<Player> allPlayers = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        originalServer = Bukkit.getServer();

        overworld = createMockWorld("world", World.Environment.NORMAL, overworldPlayers);
        nether = createMockWorld("world_nether", World.Environment.NETHER, netherPlayers);
        end = createMockWorld("world_the_end", World.Environment.THE_END, endPlayers);

        mockServer = (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> {
                    return switch (method.getName()) {
                        case "getWorlds" -> List.of(overworld, nether, end);
                        case "getWorld" -> {
                            String name = (String) args[0];
                            if ("world".equalsIgnoreCase(name)) yield overworld;
                            if ("world_nether".equalsIgnoreCase(name) || "nether".equalsIgnoreCase(name)) yield nether;
                            if ("world_the_end".equalsIgnoreCase(name) || "end".equalsIgnoreCase(name)) yield end;
                            yield null;
                        }
                        case "getOnlinePlayers" -> allPlayers;
                        case "getMaxPlayers" -> 200;
                        case "getLogger" -> Logger.getLogger("Minecraft");
                        default -> null;
                    };
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

    @Test
    void bundledConfigLinesUseDestinationPlayersPlaceholder() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("src/main/resources/config.yml"));
        List<String> lines = config.getStringList("PORTAL-SYSTEM.HOLOGRAM.LINES");

        assertFalse(lines.isEmpty(), "config.yml should define PORTAL-SYSTEM.HOLOGRAM.LINES");
        assertTrue(lines.contains("&f<players> Players"),
                "Portal hologram lines should use <players> Players to show destination players instead of total online");
        assertFalse(lines.contains("&f<total_player> Players"),
                "Portal hologram lines must not default to <total_player> Players");
    }

    @Test
    void portalHologramShowsSpecificWorldPlayerCountForRtpPortals() throws Exception {
        Player p1 = createMockPlayer(UUID.randomUUID(), overworld);
        Player p2 = createMockPlayer(UUID.randomUUID(), overworld);
        Player p3 = createMockPlayer(UUID.randomUUID(), overworld);
        Player p4 = createMockPlayer(UUID.randomUUID(), nether);
        Player p5 = createMockPlayer(UUID.randomUUID(), nether);
        Player p6 = createMockPlayer(UUID.randomUUID(), end);

        overworldPlayers.addAll(List.of(p1, p2, p3));
        netherPlayers.addAll(List.of(p4, p5));
        endPlayers.add(p6);
        allPlayers.addAll(List.of(p1, p2, p3, p4, p5, p6));

        UltimateDonutSmp plugin = createMockPlugin();
        PortalManager portalManager = new PortalManager(plugin);

        PortalDefinition worldPortal = new PortalDefinition(
                "world_portal", "Overworld", "cuboid1", "RTP", "overworld",
                true, "", 0, 1500L, "", "", 0D, 0D, 0D
        );
        PortalDefinition netherPortal = new PortalDefinition(
                "nether_portal", "Nether", "cuboid2", "RTP", "nether",
                true, "", 0, 1500L, "", "", 0D, 0D, 0D
        );
        PortalDefinition endPortal = new PortalDefinition(
                "end_portal", "The End", "cuboid3", "RTP", "the_end",
                true, "", 0, 1500L, "", "", 0D, 0D, 0D
        );

        String linePattern = "&f<players> Players (Total: <total_player>)";

        String worldResolved = portalManager.resolveHologramLine(linePattern, worldPortal);
        assertEquals("&f3 Players (Total: 6)", worldResolved,
                "Overworld portal should show overworld players in <players> and server total in <total_player>");

        String netherResolved = portalManager.resolveHologramLine(linePattern, netherPortal);
        assertEquals("&f2 Players (Total: 6)", netherResolved,
                "Nether portal should show nether players in <players> and server total in <total_player>");

        String endResolved = portalManager.resolveHologramLine(linePattern, endPortal);
        assertEquals("&f1 Players (Total: 6)", endResolved,
                "End portal should show end players in <players> and server total in <total_player>");
    }

    @Test
    void portalHologramShowsAfkPlayerCountForAfkPortals() throws Exception {
        Player p1 = createMockPlayer(UUID.randomUUID(), overworld);
        Player p2 = createMockPlayer(UUID.randomUUID(), overworld);
        Player p3 = createMockPlayer(UUID.randomUUID(), overworld);
        Player p4 = createMockPlayer(UUID.randomUUID(), overworld);

        overworldPlayers.addAll(List.of(p1, p2, p3, p4));
        allPlayers.addAll(List.of(p1, p2, p3, p4));

        UltimateDonutSmp plugin = createMockPlugin();
        AFKManager afkManager = plugin.getAFKManager();
        Field afkSetField = AFKManager.class.getDeclaredField("afkPlayers");
        afkSetField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<UUID> afkSet = (Set<UUID>) afkSetField.get(afkManager);
        afkSet.add(p1.getUniqueId());
        afkSet.add(p2.getUniqueId());

        PortalManager portalManager = new PortalManager(plugin);
        PortalDefinition afkPortal = new PortalDefinition(
                "afk_portal", "AFK", "cuboid_afk", "AFK", "",
                true, "", 0, 1500L, "", "", 0D, 0D, 0D
        );

        String linePattern = "&f<players> Players";
        String resolved = portalManager.resolveHologramLine(linePattern, afkPortal);

        assertEquals("&f2 Players", resolved,
                "AFK portal should show AFK players count (2), not the overworld player count (4)");
        assertEquals(2, portalManager.getDestinationPlayerCount(afkPortal));
    }

    @Test
    void portalHologramShowsRemoteServerPlayerCountWhenConfigured() throws Exception {
        UltimateDonutSmp plugin = createMockPlugin();

        NetworkStatusManager networkStatusManager = allocateInstance(NetworkStatusManager.class);
        Field nsmPluginField = NetworkStatusManager.class.getDeclaredField("plugin");
        nsmPluginField.setAccessible(true);
        nsmPluginField.set(networkStatusManager, plugin);

        Field netField = ConfigManager.class.getDeclaredField("network");
        netField.setAccessible(true);
        YamlConfiguration netConfig = new YamlConfiguration();
        netConfig.set("NETWORK-STATUS.ENABLED", true);
        netField.set(plugin.getConfigManager(), netConfig);

        Field snapField = NetworkStatusManager.class.getDeclaredField("snapshots");
        snapField.setAccessible(true);
        Map<String, ServerStatusSnapshot> snapMap = new ConcurrentHashMap<>();
        snapMap.put("survival-2", new ServerStatusSnapshot("survival-2", "Survival 2", true, 42, "Paper", "20.0", 0L, 5L));
        snapField.set(networkStatusManager, snapMap);

        Field defsField = NetworkStatusManager.class.getDeclaredField("serverDefinitions");
        defsField.setAccessible(true);
        Map<String, Object> defsMap = new ConcurrentHashMap<>();
        defsMap.put("survival-2", "defined");
        defsField.set(networkStatusManager, defsMap);

        Field nsmField = UltimateDonutSmp.class.getDeclaredField("networkStatusManager");
        nsmField.setAccessible(true);
        nsmField.set(plugin, networkStatusManager);

        FileConfiguration config = plugin.getConfigManager().getConfig();
        config.set("PORTAL-SYSTEM.HOLOGRAM.PORTALS.remote_portal.SERVER-ID", "survival-2");

        PortalManager portalManager = new PortalManager(plugin);
        PortalDefinition remotePortal = new PortalDefinition(
                "remote_portal", "Survival 2", "cuboid_remote", "RTP", "overworld",
                true, "", 0, 1500L, "", "", 0D, 0D, 0D
        );

        assertEquals(42, portalManager.getDestinationPlayerCount(remotePortal));
        String resolved = portalManager.resolveHologramLine("&f<players> Players on {server}", remotePortal);
        assertEquals("&f42 Players on Survival 2", resolved);
    }

    private World createMockWorld(String name, World.Environment environment, List<Player> playersInWorld) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    return switch (method.getName()) {
                        case "getName" -> name;
                        case "getPlayers" -> playersInWorld;
                        case "getEnvironment" -> environment;
                        default -> null;
                    };
                }
        );
    }

    private Player createMockPlayer(UUID uuid, World world) {
        Location loc = new Location(world, 100, 64, 100);
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    return switch (method.getName()) {
                        case "getUniqueId" -> uuid;
                        case "getName" -> "Player_" + uuid.toString().substring(0, 4);
                        case "getWorld" -> world;
                        case "getLocation" -> loc;
                        case "isOnline" -> true;
                        default -> null;
                    };
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocateInstance(Class<T> clazz) throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        Constructor<?> constructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(clazz, objectConstructor);
        return (T) constructor.newInstance();
    }

    private UltimateDonutSmp createMockPlugin() throws Exception {
        UltimateDonutSmp plugin = allocateInstance(UltimateDonutSmp.class);

        ConfigManager configManager = new ConfigManager(plugin);
        Field configField = ConfigManager.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(configManager, new YamlConfiguration());

        Field rtpConfigField = ConfigManager.class.getDeclaredField("rtp");
        rtpConfigField.setAccessible(true);
        rtpConfigField.set(configManager, new YamlConfiguration());

        Field cmField = UltimateDonutSmp.class.getDeclaredField("configManager");
        cmField.setAccessible(true);
        cmField.set(plugin, configManager);

        FeatureManager featureManager = new FeatureManager(plugin);
        Field fmField = UltimateDonutSmp.class.getDeclaredField("featureManager");
        fmField.setAccessible(true);
        fmField.set(plugin, featureManager);

        RTPManager rtpManager = allocateInstance(RTPManager.class);
        Field rtpPluginField = RTPManager.class.getDeclaredField("plugin");
        rtpPluginField.setAccessible(true);
        rtpPluginField.set(rtpManager, plugin);

        Field cdField = RTPManager.class.getDeclaredField("configuredDestinations");
        cdField.setAccessible(true);
        cdField.set(rtpManager, List.of());

        Field rtpField = UltimateDonutSmp.class.getDeclaredField("rtpManager");
        rtpField.setAccessible(true);
        rtpField.set(plugin, rtpManager);

        AFKManager afkManager = new AFKManager(plugin);
        Field afkField = UltimateDonutSmp.class.getDeclaredField("afkManager");
        afkField.setAccessible(true);
        afkField.set(plugin, afkManager);

        SpawnManager spawnManager = allocateInstance(SpawnManager.class);
        Field smPluginField = SpawnManager.class.getDeclaredField("plugin");
        smPluginField.setAccessible(true);
        smPluginField.set(spawnManager, plugin);
        Field smConfiguredAfkField = SpawnManager.class.getDeclaredField("configuredAfkAreas");
        smConfiguredAfkField.setAccessible(true);
        smConfiguredAfkField.set(spawnManager, List.of());

        Field smConfiguredSpawnField = SpawnManager.class.getDeclaredField("configuredSpawnAreas");
        smConfiguredSpawnField.setAccessible(true);
        smConfiguredSpawnField.set(spawnManager, List.of());

        Field spawnField = UltimateDonutSmp.class.getDeclaredField("spawnManager");
        spawnField.setAccessible(true);
        spawnField.set(plugin, spawnManager);

        CuboidManager cuboidManager = allocateInstance(CuboidManager.class);
        Field cuboidsField = CuboidManager.class.getDeclaredField("cuboids");
        cuboidsField.setAccessible(true);
        cuboidsField.set(cuboidManager, new ConcurrentHashMap<>());

        Field cuboidField = UltimateDonutSmp.class.getDeclaredField("cuboidManager");
        cuboidField.setAccessible(true);
        cuboidField.set(plugin, cuboidManager);

        return plugin;
    }
}
