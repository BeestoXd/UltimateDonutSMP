package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.CombatManager;
import com.bx.ultimateDonutSmp.managers.ConfigManager;
import com.bx.ultimateDonutSmp.managers.HomeManager;
import com.bx.ultimateDonutSmp.managers.ShopManager;
import com.bx.ultimateDonutSmp.models.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bukkit registers every command under "plugin:name" as well as its plain name, and hands whichever
 * form was typed to onCommand as the label. A class that decides which command it is by comparing that
 * raw label matches no branch when the namespaced form is used.
 */
class NamespacedCommandLabelTest {

    @Test
    void plainLabelSellsTheHeldStack() throws Exception {
        RecordingShopManager shop = runSell("sellhand");
        assertTrue(shop.sold, "control: /sellhand has to reach ShopManager.sellInventory");
    }

    @Test
    void namespacedLabelSellsTheHeldStackToo() throws Exception {
        RecordingShopManager shop = runSell("ultimatedonutsmp:sellhand");
        assertTrue(shop.sold, "/ultimatedonutsmp:sellhand has to reach ShopManager.sellInventory");
    }

    @Test
    void plainLabelSavesAHome() throws Exception {
        RecordingHomeManager homes = runHome("sethome");
        assertTrue(homes.saved, "control: /sethome has to reach HomeManager.setHome");
    }

    @Test
    void namespacedLabelSavesAHomeToo() throws Exception {
        RecordingHomeManager homes = runHome("ultimatedonutsmp:sethome");
        assertTrue(homes.saved, "/ultimatedonutsmp:sethome has to save a home rather than teleport");
    }

    private RecordingShopManager runSell(String label) throws Exception {
        UltimateDonutSmp plugin = allocate(UltimateDonutSmp.class);
        RecordingShopManager shop = allocate(RecordingShopManager.class);
        set(UltimateDonutSmp.class, plugin, "shopManager", shop);

        Player player = proxy(Player.class);
        Command command = new StubCommand("sellhand");

        new SellCommand(plugin).onCommand(player, command, label, new String[0]);
        return shop;
    }

    private RecordingHomeManager runHome(String label) throws Exception {
        UltimateDonutSmp plugin = allocate(UltimateDonutSmp.class);
        RecordingHomeManager homes = new RecordingHomeManager(plugin);
        ConfigManager configManager = new ConfigManager(plugin);
        set(ConfigManager.class, configManager, "messages", new YamlConfiguration());
        set(UltimateDonutSmp.class, plugin, "configManager", configManager);
        set(UltimateDonutSmp.class, plugin, "combatManager", new CombatManager(plugin));
        set(UltimateDonutSmp.class, plugin, "homeManager", homes);

        Player player = proxy(Player.class);
        Command command = new StubCommand("sethome");

        // The assertion is about which branch ran. When the label reaches the wrong one, the /home
        // block asks for managers this test has no reason to stand up, so let that throw and read the
        // recorder instead.
        try {
            new HomeCommand(plugin).onCommand(player, command, label, new String[]{"base"});
        } catch (Throwable ignored) {
            // wrong branch, and the recorder below says so
        }
        return homes;
    }

    public static class RecordingShopManager extends ShopManager {
        boolean sold;

        public RecordingShopManager(UltimateDonutSmp plugin) {
            super(plugin);
        }

        @Override
        public double sellInventory(Player player, boolean handOnly) {
            sold = true;
            return 1.0D;
        }
    }

    public static class RecordingHomeManager extends HomeManager {
        boolean saved;

        public RecordingHomeManager(UltimateDonutSmp plugin) {
            super(plugin);
        }

        @Override
        public boolean setHome(Player player, String name) {
            saved = true;
            return true;
        }

        @Override
        public List<Home> getHomes(UUID uuid) {
            return List.of();
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

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type) {
        UUID uuid = UUID.randomUUID();
        return (T) Proxy.newProxyInstance(
                NamespacedCommandLabelTest.class.getClassLoader(),
                new Class<?>[]{type},
                (instance, method, args) -> switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(instance);
                    case "equals" -> instance == args[0];
                    case "toString" -> type.getSimpleName() + "-stub";
                    case "getUniqueId" -> uuid;
                    case "getName" -> "Tester";
                    default -> null;
                }
        );
    }
}
