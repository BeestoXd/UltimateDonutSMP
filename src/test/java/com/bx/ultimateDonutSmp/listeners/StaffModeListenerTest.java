package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.StaffModeManager;
import com.bx.ultimateDonutSmp.staff.StaffToolType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffModeListenerTest {

    private static final Material STAFF_TOOL = Material.NETHER_STAR;
    private static final Material NORMAL_ITEM = Material.STONE;

    private UUID playerUuid;
    private AtomicInteger vanishToggleCount;
    private AtomicInteger toolLockedMessageCount;
    private TestStaffModeManager staffModeManager;
    private StaffModeListener listener;
    private Server originalServer;

    public static class TestStaffModeManager extends StaffModeManager {
        private AtomicInteger toggleCount;
        private AtomicInteger lockedMessageCount;
        private Material staffToolMaterial;

        public TestStaffModeManager() {
            super(null);
        }

        @Override
        public boolean isInStaffMode(UUID uuid) {
            return true;
        }

        @Override
        public boolean shouldLockTools() {
            return true;
        }

        @Override
        public void sendToolLockedMessage(Player player) {
            if (lockedMessageCount != null) {
                lockedMessageCount.incrementAndGet();
            }
        }

        @Override
        public StaffToolType resolveTool(ItemStack item) {
            if (staffToolMaterial == null) {
                return StaffToolType.VANISH;
            }
            if (item == null || item.getType() != staffToolMaterial) {
                return null;
            }
            return StaffToolType.VANISH;
        }

        @Override
        public boolean canUseVanish(Player player) {
            return true;
        }

        @Override
        public boolean toggleVanish(Player player) {
            if (toggleCount != null) {
                toggleCount.incrementAndGet();
            }
            return true;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        playerUuid = UUID.randomUUID();
        vanishToggleCount = new AtomicInteger(0);
        toolLockedMessageCount = new AtomicInteger(0);
        originalServer = Bukkit.getServer();
        installMockServer();

        Constructor<Object> objectConstructor = Object.class.getConstructor();
        ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

        Constructor<?> pluginConstructor = reflectionFactory.newConstructorForSerialization(
                UltimateDonutSmp.class, objectConstructor
        );
        UltimateDonutSmp mockPlugin = (UltimateDonutSmp) pluginConstructor.newInstance();

        Constructor<?> smmConstructor = reflectionFactory.newConstructorForSerialization(
                TestStaffModeManager.class, objectConstructor
        );
        TestStaffModeManager mockStaffModeManager = (TestStaffModeManager) smmConstructor.newInstance();

        setManagerField(mockStaffModeManager, "toggleCount", vanishToggleCount);
        setManagerField(mockStaffModeManager, "lockedMessageCount", toolLockedMessageCount);

        Field smmField = UltimateDonutSmp.class.getDeclaredField("staffModeManager");
        smmField.setAccessible(true);
        smmField.set(mockPlugin, mockStaffModeManager);

        staffModeManager = mockStaffModeManager;
        listener = new StaffModeListener(mockPlugin);
    }

    @AfterEach
    void tearDown() throws Exception {
        setBukkitServer(originalServer);
    }

    @Test
    void interactDebouncesRapidClicks() throws Exception {
        Player mockPlayer = (Player) Proxy.newProxyInstance(
                StaffModeListenerTest.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getUniqueId")) {
                        return playerUuid;
                    }
                    return null;
                }
        );

        ItemStack mockItem = new ItemStack(Material.FEATHER);

        PlayerInteractEvent event1 = new PlayerInteractEvent(
                mockPlayer,
                Action.RIGHT_CLICK_BLOCK,
                mockItem,
                null,
                null,
                EquipmentSlot.HAND
        );

        PlayerInteractEvent event2 = new PlayerInteractEvent(
                mockPlayer,
                Action.RIGHT_CLICK_AIR,
                mockItem,
                null,
                null,
                EquipmentSlot.HAND
        );

        listener.onInteract(event1);
        assertTrue(event1.isCancelled(), "First event should be cancelled");
        assertEquals(1, vanishToggleCount.get(), "First click should trigger vanish toggle");

        listener.onInteract(event2);
        assertTrue(event2.isCancelled(), "Second event should still be cancelled");
        assertEquals(1, vanishToggleCount.get(), "Rapid second click within 200ms should be debounced");

        Thread.sleep(210);

        PlayerInteractEvent event3 = new PlayerInteractEvent(
                mockPlayer,
                Action.RIGHT_CLICK_AIR,
                mockItem,
                null,
                null,
                EquipmentSlot.HAND
        );

        listener.onInteract(event3);
        assertTrue(event3.isCancelled(), "Third event should be cancelled");
        assertEquals(2, vanishToggleCount.get(), "Click after cooldown expires should trigger vanish toggle again");
    }

    @Test
    void dropIsCancelledForStaffToolsOnly() throws Exception {
        onlyStaffToolsAreTools();
        Player player = mockPlayer(mockPlayerInventory(Map.of(), null));

        PlayerDropItemEvent toolDrop = new PlayerDropItemEvent(player, mockItemEntity(new ItemStack(STAFF_TOOL)));
        listener.onDrop(toolDrop);
        assertTrue(toolDrop.isCancelled(), "Dropping a staff tool should be blocked");
        assertEquals(1, toolLockedMessageCount.get(), "Blocking a staff tool drop should explain why");

        PlayerDropItemEvent normalDrop = new PlayerDropItemEvent(player, mockItemEntity(new ItemStack(NORMAL_ITEM)));
        listener.onDrop(normalDrop);
        assertFalse(normalDrop.isCancelled(), "Dropping a normal item should still be allowed");
        assertEquals(1, toolLockedMessageCount.get(), "An allowed drop should not send the locked message");
    }

    @Test
    void offhandSwapIsCancelledOnlyWhenAStaffToolIsInvolved() throws Exception {
        onlyStaffToolsAreTools();
        Player player = mockPlayer(mockPlayerInventory(Map.of(), null));

        PlayerSwapHandItemsEvent toolInMainHand = new PlayerSwapHandItemsEvent(
                player, new ItemStack(NORMAL_ITEM), new ItemStack(STAFF_TOOL));
        listener.onSwap(toolInMainHand);
        assertTrue(toolInMainHand.isCancelled(), "Swapping a staff tool into the off hand should be blocked");

        PlayerSwapHandItemsEvent toolInOffHand = new PlayerSwapHandItemsEvent(
                player, new ItemStack(STAFF_TOOL), new ItemStack(NORMAL_ITEM));
        listener.onSwap(toolInOffHand);
        assertTrue(toolInOffHand.isCancelled(), "Swapping a staff tool out of the off hand should be blocked");

        PlayerSwapHandItemsEvent normalSwap = new PlayerSwapHandItemsEvent(
                player, new ItemStack(NORMAL_ITEM), new ItemStack(NORMAL_ITEM));
        listener.onSwap(normalSwap);
        assertFalse(normalSwap.isCancelled(), "Swapping two normal items should still be allowed");
    }

    @Test
    void inventoryClickIsCancelledOnlyWhenAStaffToolIsInvolved() throws Exception {
        onlyStaffToolsAreTools();
        Player player = mockPlayer(mockPlayerInventory(Map.of(), null));

        InventoryClickEvent toolClick = mockClick(
                player, Map.of(9, new ItemStack(STAFF_TOOL)), null, 9, ClickType.LEFT,
                InventoryAction.PICKUP_ALL, -1);
        listener.onInventoryClick(toolClick);
        assertTrue(toolClick.isCancelled(), "Picking a staff tool up in the inventory should be blocked");

        InventoryClickEvent normalClick = mockClick(
                player, Map.of(9, new ItemStack(NORMAL_ITEM)), null, 9, ClickType.LEFT,
                InventoryAction.PICKUP_ALL, -1);
        listener.onInventoryClick(normalClick);
        assertFalse(normalClick.isCancelled(), "Moving a normal item around the inventory should be allowed");

        InventoryClickEvent cursorClick = mockClick(
                player, Map.of(9, new ItemStack(NORMAL_ITEM)), new ItemStack(STAFF_TOOL), 9, ClickType.LEFT,
                InventoryAction.PLACE_ALL, -1);
        listener.onInventoryClick(cursorClick);
        assertTrue(cursorClick.isCancelled(), "Placing a staff tool held on the cursor should be blocked");
    }

    @Test
    void hotbarAndOffhandSwapClicksSeeTheToolTheyWouldMove() throws Exception {
        onlyStaffToolsAreTools();
        PlayerInventory inventory = mockPlayerInventory(Map.of(3, new ItemStack(STAFF_TOOL)), new ItemStack(STAFF_TOOL));
        Player player = mockPlayer(inventory);

        InventoryClickEvent hotbarSwap = mockClick(
                player, Map.of(9, new ItemStack(NORMAL_ITEM)), null, 9, ClickType.NUMBER_KEY,
                InventoryAction.HOTBAR_SWAP, 3);
        listener.onInventoryClick(hotbarSwap);
        assertTrue(hotbarSwap.isCancelled(), "A number key that would move a staff tool should be blocked");

        InventoryClickEvent emptyHotbarSwap = mockClick(
                player, Map.of(9, new ItemStack(NORMAL_ITEM)), null, 9, ClickType.NUMBER_KEY,
                InventoryAction.HOTBAR_SWAP, 5);
        listener.onInventoryClick(emptyHotbarSwap);
        assertFalse(emptyHotbarSwap.isCancelled(), "A number key aimed at a normal slot should be allowed");

        InventoryClickEvent offhandSwap = mockClick(
                player, Map.of(9, new ItemStack(NORMAL_ITEM)), null, 9, ClickType.SWAP_OFFHAND,
                InventoryAction.HOTBAR_SWAP, -1);
        listener.onInventoryClick(offhandSwap);
        assertTrue(offhandSwap.isCancelled(), "An off hand swap holding a staff tool should be blocked");
    }

    @Test
    void dragIsCancelledOnlyWhenItTouchesAStaffTool() throws Exception {
        onlyStaffToolsAreTools();
        Player player = mockPlayer(mockPlayerInventory(Map.of(), null));

        InventoryDragEvent toolDrag = new InventoryDragEvent(
                mockView(player, Map.of(), null),
                null,
                new ItemStack(STAFF_TOOL),
                false,
                Map.of(9, new ItemStack(STAFF_TOOL))
        );
        listener.onInventoryDrag(toolDrag);
        assertTrue(toolDrag.isCancelled(), "Dragging a staff tool should be blocked");

        InventoryDragEvent overToolDrag = new InventoryDragEvent(
                mockView(player, Map.of(9, new ItemStack(STAFF_TOOL)), null),
                null,
                new ItemStack(NORMAL_ITEM),
                false,
                Map.of(9, new ItemStack(NORMAL_ITEM))
        );
        listener.onInventoryDrag(overToolDrag);
        assertTrue(overToolDrag.isCancelled(), "Dragging onto a slot that holds a staff tool should be blocked");

        InventoryDragEvent normalDrag = new InventoryDragEvent(
                mockView(player, Map.of(), null),
                null,
                new ItemStack(NORMAL_ITEM),
                false,
                Map.of(9, new ItemStack(NORMAL_ITEM))
        );
        listener.onInventoryDrag(normalDrag);
        assertFalse(normalDrag.isCancelled(), "Dragging normal items around should be allowed");
    }

    private void onlyStaffToolsAreTools() throws Exception {
        setManagerField(staffModeManager, "staffToolMaterial", STAFF_TOOL);
    }

    private void setManagerField(TestStaffModeManager manager, String name, Object value) throws Exception {
        Field field = TestStaffModeManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(manager, value);
    }

    private void installMockServer() throws Exception {
        Object scheduler = Proxy.newProxyInstance(
                BukkitScheduler.class.getClassLoader(),
                new Class<?>[]{BukkitScheduler.class},
                (proxy, method, args) -> null
        );
        Server server = (Server) Proxy.newProxyInstance(
                Server.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getScheduler")) {
                        return scheduler;
                    }
                    if (method.getName().equals("getLogger")) {
                        return java.util.logging.Logger.getLogger("StaffModeListenerTest");
                    }
                    return null;
                }
        );
        setBukkitServer(server);
    }

    private void setBukkitServer(Server server) throws Exception {
        Field serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        serverField.set(null, server);
    }

    private Player mockPlayer(PlayerInventory inventory) {
        return (Player) Proxy.newProxyInstance(
                StaffModeListenerTest.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getUniqueId")) {
                        return playerUuid;
                    }
                    if (method.getName().equals("getInventory")) {
                        return inventory;
                    }
                    return null;
                }
        );
    }

    private PlayerInventory mockPlayerInventory(Map<Integer, ItemStack> slots, ItemStack offhand) {
        Map<Integer, ItemStack> contents = new HashMap<>(slots);
        return (PlayerInventory) Proxy.newProxyInstance(
                StaffModeListenerTest.class.getClassLoader(),
                new Class<?>[]{PlayerInventory.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getItem") && args != null && args.length == 1
                            && args[0] instanceof Integer slot) {
                        return contents.get(slot);
                    }
                    if (method.getName().equals("getItemInOffHand")) {
                        return offhand;
                    }
                    return null;
                }
        );
    }

    private Item mockItemEntity(ItemStack stack) {
        return (Item) Proxy.newProxyInstance(
                StaffModeListenerTest.class.getClassLoader(),
                new Class<?>[]{Item.class},
                (proxy, method, args) -> method.getName().equals("getItemStack") ? stack : null
        );
    }

    private InventoryView mockView(Player player, Map<Integer, ItemStack> rawSlots, ItemStack cursor) {
        Map<Integer, ItemStack> contents = new HashMap<>(rawSlots);
        return (InventoryView) Proxy.newProxyInstance(
                StaffModeListenerTest.class.getClassLoader(),
                new Class<?>[]{InventoryView.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getPlayer":
                            return player;
                        case "getCursor":
                            return cursor;
                        case "getItem":
                            return contents.get((Integer) args[0]);
                        case "convertSlot":
                            return args[0];
                        default:
                            return method.getReturnType() == int.class ? 0 : null;
                    }
                }
        );
    }

    private InventoryClickEvent mockClick(Player player,
                                          Map<Integer, ItemStack> rawSlots,
                                          ItemStack cursor,
                                          int rawSlot,
                                          ClickType click,
                                          InventoryAction action,
                                          int hotbarButton) {
        return new InventoryClickEvent(
                mockView(player, rawSlots, cursor),
                InventoryType.SlotType.CONTAINER,
                rawSlot,
                click,
                action,
                hotbarButton
        );
    }
}
