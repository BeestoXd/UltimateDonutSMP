package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.enderchest.EnderChestHolder;
import com.bx.ultimateDonutSmp.enderchest.EnderChestInspectionHolder;
import com.bx.ultimateDonutSmp.enderchest.EnderChestInspectionSession;
import com.bx.ultimateDonutSmp.enderchest.EnderChestSession;
import com.bx.ultimateDonutSmp.utils.SpigotScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnderChestInspectionSyncTest {

    private Server originalServer;

    @BeforeEach
    void setUp() throws Exception {
        originalServer = Bukkit.getServer();
        installServer();
    }

    @AfterEach
    void tearDown() throws Exception {
        setServer(originalServer);
    }

    private void installServer() throws Exception {
        BukkitScheduler scheduler = proxy(BukkitScheduler.class, (method, args) -> {
            if ("runTask".equals(method.getName()) && args[1] instanceof Runnable runnable) {
                runnable.run();
            }
            return null;
        });

        org.bukkit.inventory.meta.ItemMeta itemMeta = (org.bukkit.inventory.meta.ItemMeta) Proxy.newProxyInstance(
                org.bukkit.inventory.meta.ItemMeta.class.getClassLoader(),
                new Class<?>[]{org.bukkit.inventory.meta.ItemMeta.class},
                (proxy, method, args) -> switch (method.getName()) {
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
                    case "equals" -> args.length == 2 ? java.util.Objects.equals(args[0], args[1]) : proxy == args[0];
                    default -> defaultValue(method);
                });

        Object[] registry = new Object[1];

        setServer(proxy(Server.class, (method, args) -> switch (method.getName()) {
            case "getScheduler" -> scheduler;
            case "getItemFactory" -> itemFactory;
            case "getLogger" -> java.util.logging.Logger.getLogger("EnderChestInspectionSyncTest");
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
        }));
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

    private static void setServer(Server server) throws Exception {
        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, server);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private interface Handler {
        Object handle(Method method, Object[] args) throws Throwable;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Handler handler) {
        return (T) Proxy.newProxyInstance(
                EnderChestInspectionSyncTest.class.getClassLoader(),
                new Class<?>[]{type},
                (instance, method, args) -> switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(instance);
                    case "equals" -> instance == args[0];
                    case "toString" -> type.getSimpleName() + "-stub";
                    default -> handler.handle(method, args);
                }
        );
    }

    private EnderChestManager createTestManager(
            DatabaseManager dbManager,
            AtomicReference<ItemStack[]> savedContentsRef
    ) throws Exception {
        Constructor<Object> objectConstructor = Object.class.getConstructor();
        ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

        Constructor<?> pluginConstructor = reflectionFactory
                .newConstructorForSerialization(UltimateDonutSmp.class, objectConstructor);
        UltimateDonutSmp plugin = (UltimateDonutSmp) pluginConstructor.newInstance();

        Field descField = JavaPlugin.class.getDeclaredField("description");
        descField.setAccessible(true);
        PluginDescriptionFile pdf = new PluginDescriptionFile(
                "UltimateDonutSmp", "1.0", "com.bx.ultimateDonutSmp.UltimateDonutSmp"
        );
        descField.set(plugin, pdf);

        ConfigManager configManager = new ConfigManager(plugin);
        YamlConfiguration enderChestConfig = new YamlConfiguration();
        enderChestConfig.set("ENDER-CHEST.ENABLED", true);
        enderChestConfig.set("ENDER-CHEST.DEFAULT-ROWS", 6);
        enderChestConfig.set("ENDER-CHEST.ECSEE.ENABLED", true);
        enderChestConfig.set("ENDER-CHEST.ECSEE.EDITABLE", true);

        Field enderChestField = ConfigManager.class.getDeclaredField("enderChest");
        enderChestField.setAccessible(true);
        enderChestField.set(configManager, enderChestConfig);

        set(plugin, "configManager", configManager);
        set(plugin, "databaseManager", dbManager);

        WorthManager worthManager = new WorthManager(plugin) {
            @Override
            public ItemStack stripWorthDisplay(ItemStack item) {
                return item;
            }
        };
        set(plugin, "worthManager", worthManager);

        CrashProtectionManager crashProtectionManager = new CrashProtectionManager(plugin) {
            @Override
            public boolean isEnabled() {
                return false;
            }
        };
        set(plugin, "crashProtectionManager", crashProtectionManager);

        set(plugin, "SpigotScheduler", new SpigotScheduler(plugin));

        Constructor<?> managerConstructor = reflectionFactory
                .newConstructorForSerialization(EnderChestManager.class, objectConstructor);
        EnderChestManager manager = (EnderChestManager) managerConstructor.newInstance();

        set(manager, "plugin", plugin);
        set(manager, "activeSessions", new ConcurrentHashMap<>());
        set(manager, "inspectionSessionsByViewer", new ConcurrentHashMap<>());
        set(manager, "inspectionViewersByTarget", new ConcurrentHashMap<>());

        return manager;
    }

    private Inventory createMockInventory(Object holder, ItemStack[] items) {
        return (Inventory) Proxy.newProxyInstance(
                Inventory.class.getClassLoader(),
                new Class<?>[]{Inventory.class},
                (p, method, args) -> {
                    if (method.getName().equals("getHolder")) return holder;
                    if (method.getName().equals("getSize")) return items.length;
                    if (method.getName().equals("getContents")) return items;
                    if (method.getName().equals("setContents")) {
                        ItemStack[] newContents = (ItemStack[]) args[0];
                        System.arraycopy(newContents, 0, items, 0, Math.min(items.length, newContents.length));
                        return null;
                    }
                    return null;
                }
        );
    }

    @Test
    void syncInspectionBackToTargetSavesToDatabaseWhenTargetNotOpen() throws Exception {
        UUID viewerUuid = UUID.randomUUID();
        UUID targetUuid = UUID.randomUUID();

        AtomicBoolean saved = new AtomicBoolean(false);
        AtomicReference<ItemStack[]> savedContentsRef = new AtomicReference<>();
        DatabaseManager dbManager = new DatabaseManager(null) {
            @Override
            public boolean saveEnderChest(UUID uuid, int rows, ItemStack[] contents) {
                if (uuid.equals(targetUuid)) {
                    saved.set(true);
                    savedContentsRef.set(contents);
                    return true;
                }
                return false;
            }
        };

        EnderChestManager manager = createTestManager(dbManager, savedContentsRef);

        ItemStack[] items = new ItemStack[54];
        items[0] = new ItemStack(Material.DIAMOND, 32); // e.g. 32 diamonds left after admin extracted half
        EnderChestInspectionHolder holder = new EnderChestInspectionHolder(viewerUuid, targetUuid);
        Inventory inspectionInv = createMockInventory(holder, items);
        holder.bind(inspectionInv);

        manager.syncInspectionBackToTarget(inspectionInv);

        assertTrue(saved.get(), "DatabaseManager.saveEnderChest must be called when target chest is closed");
        assertNotNull(savedContentsRef.get());
        assertEquals(Material.DIAMOND, savedContentsRef.get()[0].getType());
        assertEquals(32, savedContentsRef.get()[0].getAmount());
    }

    @Test
    void syncInspectionBackToTargetUpdatesTargetSessionWhenTargetIsOpen() throws Exception {
        UUID viewerUuid = UUID.randomUUID();
        UUID targetUuid = UUID.randomUUID();

        DatabaseManager dbManager = new DatabaseManager(null) {
            @Override
            public boolean saveEnderChest(UUID uuid, int rows, ItemStack[] contents) {
                return true;
            }
        };

        EnderChestManager manager = createTestManager(dbManager, new AtomicReference<>());

        Field activeSessionsField = EnderChestManager.class.getDeclaredField("activeSessions");
        activeSessionsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, EnderChestSession> activeSessions = (Map<UUID, EnderChestSession>) activeSessionsField.get(manager);

        ItemStack[] targetItems = new ItemStack[54];
        targetItems[0] = new ItemStack(Material.DIAMOND, 64);
        Inventory targetInv = createMockInventory(new EnderChestHolder(targetUuid, 6), targetItems);
        EnderChestSession targetSession = new EnderChestSession(targetUuid, targetInv, 6);
        activeSessions.put(targetUuid, targetSession);

        ItemStack[] inspectionItems = new ItemStack[54];
        inspectionItems[0] = new ItemStack(Material.EMERALD, 16);
        EnderChestInspectionHolder holder = new EnderChestInspectionHolder(viewerUuid, targetUuid);
        Inventory inspectionInv = createMockInventory(holder, inspectionItems);
        holder.bind(inspectionInv);

        manager.syncInspectionBackToTarget(inspectionInv);

        assertEquals(Material.EMERALD, targetItems[0].getType());
        assertEquals(16, targetItems[0].getAmount());
        assertTrue(targetSession.isDirty());
    }

    @Test
    void getActiveInspectionSessionFindsActiveInspectionForTarget() throws Exception {
        UUID viewerUuid = UUID.randomUUID();
        UUID targetUuid = UUID.randomUUID();

        Player mockViewer = proxy(Player.class, (method, args) -> switch (method.getName()) {
            case "getUniqueId" -> viewerUuid;
            case "isOnline" -> true;
            default -> null;
        });

        BukkitScheduler scheduler = proxy(BukkitScheduler.class, (method, args) -> null);
        setServer(proxy(Server.class, (method, args) -> switch (method.getName()) {
            case "getScheduler" -> scheduler;
            case "getLogger" -> java.util.logging.Logger.getLogger("EnderChestInspectionSyncTest");
            case "getPlayer" -> ((UUID) args[0]).equals(viewerUuid) ? mockViewer : null;
            default -> null;
        }));

        DatabaseManager dbManager = new DatabaseManager(null);
        EnderChestManager manager = createTestManager(dbManager, new AtomicReference<>());

        assertNull(manager.getActiveInspectionSession(targetUuid));

        Field inspectionSessionsField = EnderChestManager.class.getDeclaredField("inspectionSessionsByViewer");
        inspectionSessionsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, EnderChestInspectionSession> inspectionSessions =
                (Map<UUID, EnderChestInspectionSession>) inspectionSessionsField.get(manager);

        Field inspectionViewersField = EnderChestManager.class.getDeclaredField("inspectionViewersByTarget");
        inspectionViewersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, Set<UUID>> inspectionViewers =
                (Map<UUID, Set<UUID>>) inspectionViewersField.get(manager);

        ItemStack[] items = new ItemStack[54];
        EnderChestInspectionHolder holder = new EnderChestInspectionHolder(viewerUuid, targetUuid);
        Inventory inspectionInv = createMockInventory(holder, items);
        holder.bind(inspectionInv);

        EnderChestInspectionSession session = new EnderChestInspectionSession(viewerUuid, targetUuid, "TargetPlayer", inspectionInv);
        inspectionSessions.put(viewerUuid, session);
        inspectionViewers.computeIfAbsent(targetUuid, k -> ConcurrentHashMap.newKeySet()).add(viewerUuid);

        EnderChestInspectionSession retrieved = manager.getActiveInspectionSession(targetUuid);
        assertNotNull(retrieved);
        assertEquals(targetUuid, retrieved.getTargetUuid());
        assertEquals(viewerUuid, retrieved.getViewerUuid());
    }
}
