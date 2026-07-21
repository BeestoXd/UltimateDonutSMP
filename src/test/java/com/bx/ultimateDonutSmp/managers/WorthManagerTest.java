package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.WorthResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorthManagerTest {

    @Test
    void displaysSingleItemTotalWorth() {
        WorthResult result = directWorth(100.0, 100.0);

        assertEquals(100.0, WorthManager.getDisplayWorth(result));
    }

    @Test
    void displaysStackTotalWorthInsteadOfUnitWorth() {
        WorthResult result = directWorth(100.0, 500.0);

        assertEquals(500.0, WorthManager.getDisplayWorth(result));
    }

    @Test
    void displaysContainerStackTotalWorth() {
        WorthResult result = new WorthResult(
                true,
                true,
                250.0,
                500.0,
                50.0,
                200.0,
                "CONTAINER",
                "SHULKER_BOX",
                "BLOCKS"
        );

        assertEquals(500.0, WorthManager.getDisplayWorth(result));
    }

    @Test
    void enchantedItemAddsEnchantmentValueToTotalWorth() throws Exception {
        setupMockServer();

        org.bukkit.configuration.file.YamlConfiguration worthConfig = new org.bukkit.configuration.file.YamlConfiguration();
        worthConfig.set("TYPE.ARMOR_AND_TOOLS.DIAMOND_SWORD", 50.0);
        worthConfig.set("TYPE.BOOK.ENCHANTED_BOOK:SHARPNESS:5", 1500.0);

        UltimateDonutSmp plugin = createMockPlugin(worthConfig);
        WorthManager worthManager = new WorthManager(plugin);

        org.bukkit.inventory.ItemStack sword = new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_SWORD);
        org.bukkit.enchantments.Enchantment sharpness = new TestEnchantment(org.bukkit.NamespacedKey.minecraft("sharpness"));
        sword.addUnsafeEnchantment(sharpness, 5);



        WorthResult result = worthManager.resolveWorth(sword);
        assertEquals(1550.0, result.totalWorth());
    }

    private void setupMockServer() throws Exception {
        java.lang.reflect.Field serverField = org.bukkit.Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);

        final Object[] registryMockHolder = new Object[1];
        final Object[] factoryMockHolder = new Object[1];

        org.bukkit.Server mockServer = (org.bukkit.Server) java.lang.reflect.Proxy.newProxyInstance(
                org.bukkit.Server.class.getClassLoader(),
                new Class<?>[]{org.bukkit.Server.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getRegistry")) {
                        Class<?> registryType = (Class<?>) args[0];
                        Class<?> registryClass = Class.forName("org.bukkit.Registry");
                        if (registryType.getName().endsWith("Enchantment")) {
                            if (registryMockHolder[0] == null) {
                                registryMockHolder[0] = java.lang.reflect.Proxy.newProxyInstance(
                                        registryClass.getClassLoader(),
                                        new Class<?>[]{registryClass},
                                        (rProxy, rMethod, rArgs) -> {
                                            if (rMethod.getName().equals("get")) {
                                                org.bukkit.NamespacedKey key = (org.bukkit.NamespacedKey) rArgs[0];
                                                return new TestEnchantment(key);
                                            }
                                            return null;
                                        }
                                );
                            }
                            return registryMockHolder[0];
                        }
                        return java.lang.reflect.Proxy.newProxyInstance(
                                registryClass.getClassLoader(),
                                new Class<?>[]{registryClass},
                                (rProxy, rMethod, rArgs) -> null
                        );
                    }
                    if (method.getName().equals("getItemFactory")) {
                        if (factoryMockHolder[0] == null) {
                            java.util.Map<org.bukkit.enchantments.Enchantment, Integer> enchantsMap = new java.util.HashMap<>();
                            org.bukkit.inventory.meta.ItemMeta mockMeta = (org.bukkit.inventory.meta.ItemMeta) java.lang.reflect.Proxy.newProxyInstance(
                                    org.bukkit.inventory.meta.ItemMeta.class.getClassLoader(),
                                    new Class<?>[]{org.bukkit.inventory.meta.ItemMeta.class},
                                    (mProxy, mMethod, mArgs) -> {
                                        if (mMethod.getName().equals("addEnchant") || mMethod.getName().equals("addStoredEnchant")) {
                                            enchantsMap.put((org.bukkit.enchantments.Enchantment) mArgs[0], (Integer) mArgs[1]);
                                            return true;
                                        }
                                        if (mMethod.getName().equals("getEnchants") || mMethod.getName().equals("getStoredEnchants")) {
                                            return enchantsMap;
                                        }
                                        if (mMethod.getName().equals("hasEnchant") || mMethod.getName().equals("hasStoredEnchant")) {
                                            return enchantsMap.containsKey(mArgs[0]);
                                        }
                                        if (mMethod.getName().equals("clone")) {
                                            return mProxy;
                                        }
                                        if (mMethod.getName().equals("equals")) {
                                            return mProxy == mArgs[0];
                                        }
                                        if (mMethod.getName().equals("hashCode")) {
                                            return System.identityHashCode(mProxy);
                                        }
                                        if (mMethod.getName().equals("toString")) {
                                            return "MockMeta";
                                        }
                                        return null;
                                    }
                            );
                            factoryMockHolder[0] = java.lang.reflect.Proxy.newProxyInstance(
                                    org.bukkit.inventory.ItemFactory.class.getClassLoader(),
                                    new Class<?>[]{org.bukkit.inventory.ItemFactory.class},
                                    (fProxy, fMethod, fArgs) -> {
                                        if (fMethod.getName().equals("getItemMeta")) {
                                            return mockMeta;
                                        }
                                        if (fMethod.getName().equals("hasItemMeta")) {
                                            return true;
                                        }
                                        if (fMethod.getName().equals("isApplicable")) {
                                            return true;
                                        }
                                        if (fMethod.getName().equals("asMetaFor")) {
                                            return fArgs[0];
                                        }
                                        if (fMethod.getName().equals("equals") && fArgs.length == 2) {
                                            return java.util.Objects.equals(fArgs[0], fArgs[1]);
                                        }
                                        return null;
                                    }
                            );
                        }
                        return factoryMockHolder[0];
                    }
                    return null;
                }
        );

        serverField.set(null, mockServer);
    }

    private UltimateDonutSmp createMockPlugin(org.bukkit.configuration.file.YamlConfiguration worthConfig) throws Exception {
        java.lang.reflect.Constructor<Object> objectConstructor = Object.class.getConstructor();
        sun.reflect.ReflectionFactory reflectionFactory = sun.reflect.ReflectionFactory.getReflectionFactory();
        java.lang.reflect.Constructor<?> newConstructor = reflectionFactory.newConstructorForSerialization(UltimateDonutSmp.class, objectConstructor);
        UltimateDonutSmp plugin = (UltimateDonutSmp) newConstructor.newInstance();

        ConfigManager configManager = new ConfigManager(plugin);
        java.lang.reflect.Field worthField = ConfigManager.class.getDeclaredField("worth");
        worthField.setAccessible(true);
        worthField.set(configManager, worthConfig);

        java.lang.reflect.Field cmField = UltimateDonutSmp.class.getDeclaredField("configManager");
        cmField.setAccessible(true);
        cmField.set(plugin, configManager);

        java.lang.reflect.Field descField = org.bukkit.plugin.java.JavaPlugin.class.getDeclaredField("description");
        descField.setAccessible(true);
        org.bukkit.plugin.PluginDescriptionFile pdf = new org.bukkit.plugin.PluginDescriptionFile("UltimateDonutSmp", "1.0", "com.bx.ultimateDonutSmp.UltimateDonutSmp");
        descField.set(plugin, pdf);

        return plugin;
    }

    private WorthResult directWorth(double unitWorth, double totalWorth) {
        return new WorthResult(
                true,
                false,
                unitWorth,
                totalWorth,
                unitWorth,
                0.0,
                "DIRECT",
                "DIAMOND",
                "ORES"
        );
    }

    private static class TestEnchantment extends org.bukkit.enchantments.Enchantment {
        private final org.bukkit.NamespacedKey key;

        public TestEnchantment(org.bukkit.NamespacedKey key) {
            this.key = key;
        }

        @Override
        public org.bukkit.NamespacedKey getKey() {
            return key;
        }

        @Override
        public String getName() {
            return key.getKey().toUpperCase(java.util.Locale.US);
        }

        @Override
        public int getMaxLevel() {
            return 10;
        }

        @Override
        public int getStartLevel() {
            return 1;
        }

        @Override
        public org.bukkit.enchantments.EnchantmentTarget getItemTarget() {
            return null;
        }

        @Override
        public boolean isTreasure() {
            return false;
        }

        @Override
        public boolean isCursed() {
            return false;
        }

        @Override
        public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
            return false;
        }

        @Override
        public boolean canEnchantItem(org.bukkit.inventory.ItemStack item) {
            return true;
        }

        @Override
        public String getTranslationKey() {
            return key.getKey();
        }

        @Override
        public boolean isRegistered() {
            return false;
        }

        @Override
        public org.bukkit.NamespacedKey getKeyOrNull() {
            return key;
        }

        @Override
        public org.bukkit.NamespacedKey getKeyOrThrow() {
            return key;
        }
    }
}
