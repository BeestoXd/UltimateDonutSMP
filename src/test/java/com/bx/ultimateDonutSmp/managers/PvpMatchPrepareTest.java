package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PvpKit;
import com.bx.ultimateDonutSmp.models.PvpMatch;
import com.bx.ultimateDonutSmp.utils.SpigotScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.reflect.ReflectionFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The order a ranked match assembles a fighter in.
 *
 * <p>The kit overwrites whatever the player is carrying, so it must not land until the teleport
 * into the arena has. Handing it over beside the teleport writes it into the survival inventory
 * the player is still standing in, and everything they queued with goes down with it.</p>
 */
class PvpMatchPrepareTest {

    private static final List<String> CALLS = new ArrayList<>();

    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID OPPONENT = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

    private Server originalServer;
    private World arenaWorld;
    private Location arenaSpawn;
    private AtomicBoolean teleportRequested;
    private CompletableFuture<Boolean> arrival;

    private UltimateDonutSmp plugin;
    private PvpMatchManager matches;
    private Map<UUID, PvpMatch> active;
    private Player player;

    @BeforeEach
    void setUp() throws Exception {
        CALLS.clear();
        originalServer = Bukkit.getServer();
        installServer();

        plugin = build(UltimateDonutSmp.class);
        set(plugin, "configManager", arenaConfig());
        set(plugin, "SpigotScheduler", new SpigotScheduler(plugin));
        set(plugin, "pvpManager", build(RecordingPvpManager.class));

        matches = build(PvpMatchManager.class);
        set(matches, "plugin", plugin);
        active = new ConcurrentHashMap<>();
        set(matches, "active", active);
        active.put(PLAYER, new PvpMatch(1L, PLAYER, "Tester", OPPONENT, "Rival", "warrior", 0L));

        teleportRequested = new AtomicBoolean();
        arrival = new CompletableFuture<>();
        arenaWorld = proxy(World.class, (method, args) -> "getName".equals(method.getName()) ? "arena" : null);
        arenaSpawn = new Location(arenaWorld, 0.0D, 64.0D, 0.0D);
        player = proxy(Player.class, AsyncTeleport.class, (method, args) -> switch (method.getName()) {
            case "getUniqueId" -> PLAYER;
            case "getName" -> "Tester";
            case "isOnline" -> true;
            case "teleportAsync" -> {
                teleportRequested.set(true);
                yield arrival;
            }
            default -> null;
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        setServer(originalServer);
    }

    @Test
    void theKitIsHeldBackUntilTheTeleportIntoTheArenaLands() throws Exception {
        prepare();

        assertTrue(teleportRequested.get(), "the fighter has to be sent to the arena");
        assertEquals(List.of("startSession"), CALLS,
                "nothing may touch the inventory while the player is still standing in survival");

        arrival.complete(true);

        assertEquals(List.of("startSession", "heal", "giveKit", "markKitGiven"), CALLS,
                "the kit belongs on the arena side of the teleport");
    }

    @Test
    void aFighterWhoseMatchEndedMidTeleportIsLeftAlone() throws Exception {
        prepare();
        active.clear();

        arrival.complete(true);

        assertEquals(List.of("startSession"), CALLS,
                "a match that ended during the teleport must not clear anyone out");
    }

    private void prepare() throws Exception {
        Method prepare = PvpMatchManager.class
                .getDeclaredMethod("prepare", Player.class, PvpKit.class, Location.class);
        prepare.setAccessible(true);
        prepare.invoke(matches, player, new PvpKit("warrior"), arenaSpawn);
    }

    // ── Harness ───────────────────────────────────────────────────────────────

    /** The asynchronous teleport Paper and Purpur hand back, which spigot-api does not declare. */
    public interface AsyncTeleport {
        CompletableFuture<Boolean> teleportAsync(Location location, PlayerTeleportEvent.TeleportCause cause);
    }

    /** An arena manager that only writes down what the match asked it to do. */
    public static class RecordingPvpManager extends PvpManager {

        public RecordingPvpManager() {
            super(null);
        }

        @Override
        public void startSession(Player player) {
            CALLS.add("startSession");
        }

        @Override
        public void healPlayer(Player player) {
            CALLS.add("heal");
        }

        @Override
        public void giveKit(Player player, PvpKit kit) {
            CALLS.add("giveKit");
        }

        @Override
        public void markKitGiven(Player player, PvpKit kit) {
            CALLS.add("markKitGiven");
        }
    }

    private ConfigManager arenaConfig() throws Exception {
        ConfigManager configManager = build(ConfigManager.class);
        YamlConfiguration pvp = new YamlConfiguration();
        pvp.load(new File("src/main/resources/pvp.yml"));
        set(configManager, "pvp", pvp);
        return configManager;
    }

    /** Builds an instance without running any constructor, so nothing reaches for a live server. */
    @SuppressWarnings("unchecked")
    private static <T> T build(Class<T> type) throws Exception {
        Constructor<?> constructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(type, Object.class.getConstructor());
        return (T) constructor.newInstance();
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private interface Handler {
        Object handle(Method method, Object[] args) throws Throwable;
    }

    private static <T> T proxy(Class<T> type, Handler handler) {
        return proxy(type, null, handler);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Class<?> extra, Handler handler) {
        Class<?>[] interfaces = extra == null ? new Class<?>[]{type} : new Class<?>[]{type, extra};
        return (T) Proxy.newProxyInstance(
                PvpMatchPrepareTest.class.getClassLoader(),
                interfaces,
                (instance, method, args) -> switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(instance);
                    case "equals" -> instance == args[0];
                    case "toString" -> type.getSimpleName() + "-stub";
                    default -> handler.handle(method, args);
                }
        );
    }

    private void installServer() throws Exception {
        BukkitScheduler scheduler = proxy(BukkitScheduler.class, (method, args) -> {
            if ("runTask".equals(method.getName()) && args[1] instanceof Runnable runnable) {
                runnable.run();
            }
            return null;
        });
        setServer(proxy(Server.class, (method, args) -> switch (method.getName()) {
            case "getScheduler" -> scheduler;
            case "getLogger" -> java.util.logging.Logger.getLogger("PvpMatchPrepareTest");
            default -> null;
        }));
    }

    private static void setServer(Server server) throws Exception {
        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, server);
    }
}
