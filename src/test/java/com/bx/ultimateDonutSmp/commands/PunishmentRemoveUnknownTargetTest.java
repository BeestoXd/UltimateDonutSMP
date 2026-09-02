package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ConfigManager;
import com.bx.ultimateDonutSmp.managers.PunishmentManager;
import com.bx.ultimateDonutSmp.models.PunishmentType;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * /unban on a name that resolves to nothing.
 *
 * <p>handleRemove builds targetName defensively ("target != null ? target.name() : args[0]") and then
 * dereferences target.name() anyway when it reports the outcome.</p>
 */
class PunishmentRemoveUnknownTargetTest {

    private Server originalServer;
    private final List<String> sent = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        originalServer = Bukkit.getServer();
        setServer(proxy(Server.class, (method, args) -> switch (method.getName()) {
            case "getOnlinePlayers" -> List.of();
            case "getPlayerExact", "getPlayer" -> null;
            case "getLogger" -> Logger.getLogger("PunishmentRemoveUnknownTargetTest");
            default -> null;
        }));
    }

    @AfterEach
    void tearDown() throws Exception {
        setServer(originalServer);
    }

    @Test
    void unbanningAnUnresolvableNameReportsInsteadOfThrowing() throws Exception {
        UltimateDonutSmp plugin = newPlugin();

        ConfigManager configManager = new ConfigManager(plugin);
        set(ConfigManager.class, configManager, "messages", new YamlConfiguration());
        set(UltimateDonutSmp.class, plugin, "configManager", configManager);

        // Nothing online, nothing stored, and the name fails the ^[A-Za-z0-9_]{3,16}$ check that gates
        // resolveTargetUuid's offline fallback, so PunishmentCommand.resolveTarget returns null.
        set(UltimateDonutSmp.class, plugin, "punishmentManager", new PunishmentManager(plugin) {
            @Override
            public Optional<UUID> resolveTargetUuid(String username, boolean allowOfflineFallback) {
                return Optional.empty();
            }

            @Override
            public boolean markActiveRecordsRemoved(UUID targetUuid,
                                                    String targetName,
                                                    PunishmentType type,
                                                    PunishmentRemovalRequest request) {
                return false;
            }
        });

        CommandSender console = proxy(CommandSender.class, (method, args) -> {
            if (method.getName().equals("sendMessage") && args != null && args.length > 0) {
                sent.add(String.valueOf(args[0]));
            }
            if (method.getName().equals("getName")) {
                return "CONSOLE";
            }
            return null;
        });
        Command command = new StubCommand("unban");

        new PunishmentCommand(plugin).onCommand(console, command, "unban", new String[]{"Bad.Name"});

        assertFalse(sent.isEmpty(), "the staff member should be told the player was not found");
    }

    private static UltimateDonutSmp newPlugin() throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        Constructor<?> pluginConstructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(UltimateDonutSmp.class, objectConstructor);
        return (UltimateDonutSmp) pluginConstructor.newInstance();
    }

    private static void set(Class<?> owner, Object instance, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }

    private static void setServer(Server server) throws Exception {
        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, server);
    }

    private static final class StubCommand extends Command {
        private StubCommand(String name) {
            super(name);
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            return true;
        }
    }

    private interface Handler {
        Object handle(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Handler handler) {
        return (T) Proxy.newProxyInstance(
                PunishmentRemoveUnknownTargetTest.class.getClassLoader(),
                new Class<?>[]{type},
                (instance, method, args) -> switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(instance);
                    case "equals" -> instance == args[0];
                    case "toString" -> type.getSimpleName() + "-stub";
                    default -> handler.handle(method, args);
                }
        );
    }
}
