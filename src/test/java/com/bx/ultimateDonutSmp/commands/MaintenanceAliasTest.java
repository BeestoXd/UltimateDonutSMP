package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.MaintenanceManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * /ultimatedonutsmp maintenance is a second door onto /maintenance, and it used to keep its own copy
 * of the body. The copy was not updated when setlobby learned to clear the stored server, so the two
 * doors answered differently.
 */
class MaintenanceAliasTest {

    @Test
    void leavingTheServerNameOffClearsTheOverride() throws Exception {
        Run run = alias("setlobby");

        assertTrue(run.manager.cleared, "the alias has to clear the stored lobby, the same as /maintenance setlobby");
        assertNull(run.manager.lobby);
    }

    @Test
    void aNamedServerStillSetsIt() throws Exception {
        Run run = alias("setlobby", "hub");

        assertEquals("hub", run.manager.lobby, "the alias reads its server name one argument later than /maintenance");
    }

    @Test
    void statusSaysWhatAnEmptyLobbyMeans() throws Exception {
        Run run = alias("status");

        assertTrue(run.sent.stream().anyMatch(line -> line.contains("cannot connect")),
                "an empty lobby has to be spelled out rather than printed as nothing: " + run.sent);
    }

    private Run alias(String... maintenanceArgs) throws Exception {
        UltimateDonutSmp plugin = allocate(UltimateDonutSmp.class);
        RecordingMaintenanceManager manager = allocate(RecordingMaintenanceManager.class);
        // allocating past the constructor leaves the fields at their defaults, so an empty lobby
        // server, the documented single-server setup, has to be set explicitly.
        manager.lobby = "";
        set(UltimateDonutSmp.class, plugin, "maintenanceManager", manager);

        List<String> sent = new ArrayList<>();
        CommandSender sender = sender(sent);

        String[] args = new String[maintenanceArgs.length + 1];
        args[0] = "maintenance";
        System.arraycopy(maintenanceArgs, 0, args, 1, maintenanceArgs.length);

        new UltimateDonutSmpCommand(plugin)
                .onCommand(sender, new StubCommand("ultimatedonutsmp"), "ultimatedonutsmp", args);
        return new Run(manager, sent);
    }

    private record Run(RecordingMaintenanceManager manager, List<String> sent) {
    }

    public static class RecordingMaintenanceManager extends MaintenanceManager {
        String lobby;
        boolean cleared;

        public RecordingMaintenanceManager(UltimateDonutSmp plugin) {
            super(plugin);
        }

        @Override
        public boolean isMaintenanceActive() {
            return false;
        }

        @Override
        public String getLobbyServer() {
            return lobby;
        }

        @Override
        public void setLobbyServer(String lobbyServer) {
            lobby = lobbyServer;
            cleared = lobbyServer == null;
        }
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

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        Constructor<?> constructor = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(type, objectConstructor);
        return (T) constructor.newInstance();
    }

    private static void set(Class<?> owner, Object instance, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }

    private static CommandSender sender(List<String> sent) {
        return (CommandSender) Proxy.newProxyInstance(
                MaintenanceAliasTest.class.getClassLoader(),
                new Class<?>[]{CommandSender.class},
                (instance, method, args) -> switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(instance);
                    case "equals" -> instance == args[0];
                    case "toString" -> "CommandSender-stub";
                    case "hasPermission" -> true;
                    case "getName" -> "CONSOLE";
                    case "sendMessage" -> {
                        if (args != null && args.length > 0) {
                            sent.add(String.valueOf(args[0]));
                        }
                        yield null;
                    }
                    default -> null;
                }
        );
    }
}
