package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FarmingMetaManagerTest {

    @TempDir
    Path dataFolder;

    @Test
    void metaItemIsWorthMoreWhileTheRestOfThePricesStayPut() throws Exception {
        UltimateDonutSmp plugin = createPlugin(metaConfig(1.25D, List.of("KELP", "IRON_INGOT")));

        assertEquals(Material.KELP, plugin.getFarmingMetaManager().getCurrentItem());
        assertEquals(10.0D, plugin.getWorthManager().getWorth(Material.KELP));
        assertEquals(8.0D, plugin.getWorthManager().getBaseWorth(Material.KELP));
        assertEquals(5.0D, plugin.getWorthManager().getWorth(Material.IRON_INGOT));
    }

    @Test
    void sellingTheMetaItemPaysTheMultipliedPrice() throws Exception {
        UltimateDonutSmp plugin = createPlugin(metaConfig(1.25D, List.of("KELP", "IRON_INGOT")));

        List<WorthManager.SellWorthEntry> entries = plugin.getWorthManager()
                .resolveSellWorthEntries(new org.bukkit.inventory.ItemStack(Material.KELP, 64));

        assertEquals(1, entries.size());
        assertEquals(640.0D, entries.get(0).totalWorth());
    }

    @Test
    void rotationWalksTheListInOrderAndComesBackAround() throws Exception {
        UltimateDonutSmp plugin = createPlugin(metaConfig(1.25D, List.of("KELP", "IRON_INGOT")));
        FarmingMetaManager farmingMetaManager = plugin.getFarmingMetaManager();

        farmingMetaManager.rotate();

        assertEquals(Material.IRON_INGOT, farmingMetaManager.getCurrentItem());
        assertEquals(8.0D, plugin.getWorthManager().getWorth(Material.KELP));
        assertEquals(6.25D, plugin.getWorthManager().getWorth(Material.IRON_INGOT));

        farmingMetaManager.rotate();

        assertEquals(Material.KELP, farmingMetaManager.getCurrentItem());
        assertEquals(10.0D, plugin.getWorthManager().getWorth(Material.KELP));
    }

    @Test
    void turningTheMetaOffLeavesEveryPriceAlone() throws Exception {
        YamlConfiguration worthConfig = metaConfig(1.25D, List.of("KELP", "IRON_INGOT"));
        worthConfig.set("META.ENABLED", false);

        UltimateDonutSmp plugin = createPlugin(worthConfig);

        assertFalse(plugin.getFarmingMetaManager().isActive());
        assertNull(plugin.getFarmingMetaManager().getCurrentItem());
        assertEquals(8.0D, plugin.getWorthManager().getWorth(Material.KELP));
    }

    @Test
    void itemsWithoutAPriceNeverBecomeTheMeta() throws Exception {
        UltimateDonutSmp plugin = createPlugin(
                metaConfig(1.25D, List.of("KELP", "DIAMOND_BLOCK", "IRON_INGOT")));
        FarmingMetaManager farmingMetaManager = plugin.getFarmingMetaManager();

        assertEquals(List.of(Material.KELP, Material.IRON_INGOT), farmingMetaManager.getRotationItems());

        farmingMetaManager.rotate();

        assertEquals(Material.IRON_INGOT, farmingMetaManager.getCurrentItem());
    }

    @Test
    void theCountdownAndTheCurrentItemSurviveARestart() throws Exception {
        UltimateDonutSmp plugin = createPlugin(metaConfig(1.25D, List.of("KELP", "IRON_INGOT")));
        plugin.getFarmingMetaManager().rotate();
        long remainingBefore = plugin.getFarmingMetaManager().getRemainingSeconds();

        FarmingMetaManager restarted = new FarmingMetaManager(plugin);
        restarted.load();

        assertEquals(Material.IRON_INGOT, restarted.getCurrentItem());
        assertTrue(Math.abs(remainingBefore - restarted.getRemainingSeconds()) <= 1L);
        assertTrue(new File(dataFolder.toFile(), "farming-meta-data.yml").exists());
    }

    @Test
    void anItemThatLeavesTheRotationHandsTheMetaBackToTheFirstOne() throws Exception {
        UltimateDonutSmp plugin = createPlugin(metaConfig(1.25D, List.of("KELP", "IRON_INGOT")));
        plugin.getFarmingMetaManager().rotate();

        plugin.getConfigManager().getWorth().set("META.ITEMS", List.of("KELP"));
        FarmingMetaManager restarted = new FarmingMetaManager(plugin);
        restarted.load();

        assertEquals(Material.KELP, restarted.getCurrentItem());
    }

    @Test
    void parsesMaterialNamesWithoutFallingBackToAnUnrelatedItem() {
        assertEquals(Material.KELP, FarmingMetaManager.parseRotationItem("kelp"));
        assertEquals(Material.KELP, FarmingMetaManager.parseRotationItem("minecraft:kelp"));
        assertEquals(Material.IRON_INGOT, FarmingMetaManager.parseRotationItem("iron ingot"));
        assertNull(FarmingMetaManager.parseRotationItem("not_a_real_item"));
        assertNull(FarmingMetaManager.parseRotationItem(" "));
    }

    private YamlConfiguration metaConfig(double multiplier, List<String> items) {
        YamlConfiguration worthConfig = new YamlConfiguration();
        worthConfig.set("TYPE.CROPS.KELP", 8.0D);
        worthConfig.set("TYPE.ORES.IRON_INGOT", 5.0D);
        worthConfig.set("META.ENABLED", true);
        worthConfig.set("META.MULTIPLIER", multiplier);
        worthConfig.set("META.INTERVAL_DAYS", 14);
        worthConfig.set("META.ANNOUNCE_ON_ROTATE", false);
        worthConfig.set("META.ITEMS", items);
        return worthConfig;
    }

    private UltimateDonutSmp createPlugin(YamlConfiguration worthConfig) throws Exception {
        setupMockServer();

        sun.reflect.ReflectionFactory reflectionFactory = sun.reflect.ReflectionFactory.getReflectionFactory();
        UltimateDonutSmp plugin = (UltimateDonutSmp) reflectionFactory
                .newConstructorForSerialization(UltimateDonutSmp.class, Object.class.getConstructor())
                .newInstance();

        ConfigManager configManager = new ConfigManager(plugin);
        setField(ConfigManager.class, configManager, "worth", worthConfig);
        setField(UltimateDonutSmp.class, plugin, "configManager", configManager);
        setField(org.bukkit.plugin.java.JavaPlugin.class, plugin, "dataFolder", dataFolder.toFile());
        setField(org.bukkit.plugin.java.JavaPlugin.class, plugin, "description",
                new org.bukkit.plugin.PluginDescriptionFile(
                        "UltimateDonutSmp", "1.0", "com.bx.ultimateDonutSmp.UltimateDonutSmp"));

        WorthManager worthManager = new WorthManager(plugin);
        setField(UltimateDonutSmp.class, plugin, "worthManager", worthManager);

        FarmingMetaManager farmingMetaManager = new FarmingMetaManager(plugin);
        setField(UltimateDonutSmp.class, plugin, "farmingMetaManager", farmingMetaManager);
        farmingMetaManager.load();

        return plugin;
    }

    private static void setField(Class<?> owner, Object target, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void setupMockServer() throws Exception {
        Field serverField = org.bukkit.Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);

        Map<org.bukkit.enchantments.Enchantment, Integer> enchantments = new HashMap<>();
        org.bukkit.inventory.meta.ItemMeta itemMeta = (org.bukkit.inventory.meta.ItemMeta) Proxy.newProxyInstance(
                org.bukkit.inventory.meta.ItemMeta.class.getClassLoader(),
                new Class<?>[]{org.bukkit.inventory.meta.ItemMeta.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getEnchants", "getStoredEnchants" -> enchantments;
                    case "clone" -> proxy;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    default -> defaultValue(method);
                });

        Object itemFactory = Proxy.newProxyInstance(
                org.bukkit.inventory.ItemFactory.class.getClassLoader(),
                new Class<?>[]{org.bukkit.inventory.ItemFactory.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getItemMeta" -> itemMeta;
                    case "hasItemMeta", "isApplicable" -> true;
                    case "asMetaFor" -> args[0];
                    case "equals" -> args.length == 2 ? Objects.equals(args[0], args[1]) : proxy == args[0];
                    default -> defaultValue(method);
                });

        // Material.isAir() reads the block registry, so every registry lookup has to answer with
        // something rather than null or org.bukkit.Registry never finishes initialising. The
        // registry proxy is built on first use because building it needs a server in place.
        Object[] registry = new Object[1];

        org.bukkit.Server server = (org.bukkit.Server) Proxy.newProxyInstance(
                org.bukkit.Server.class.getClassLoader(),
                new Class<?>[]{org.bukkit.Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getItemFactory" -> itemFactory;
                    case "getRegistry" -> {
                        if (registry[0] == null) {
                            Class<?> registryClass = Class.forName("org.bukkit.Registry");
                            registry[0] = Proxy.newProxyInstance(
                                    registryClass.getClassLoader(),
                                    new Class<?>[]{registryClass},
                                    (registryProxy, registryMethod, registryArgs) -> defaultValue(registryMethod));
                        }
                        yield registry[0];
                    }
                    default -> defaultValue(method);
                });

        serverField.set(null, server);
    }

    private static Object defaultValue(Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        return null;
    }
}
