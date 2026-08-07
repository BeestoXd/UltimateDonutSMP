package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.StaffModeManager;
import com.bx.ultimateDonutSmp.staff.StaffToolType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffModeListenerTest {

    private UUID playerUuid;
    private AtomicInteger vanishToggleCount;
    private StaffModeListener listener;

    public static class TestStaffModeManager extends StaffModeManager {
        private AtomicInteger toggleCount;

        public TestStaffModeManager() {
            super(null);
        }

        @Override
        public boolean isInStaffMode(UUID uuid) {
            return true;
        }

        @Override
        public StaffToolType resolveTool(ItemStack item) {
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

        Field countField = TestStaffModeManager.class.getDeclaredField("toggleCount");
        countField.setAccessible(true);
        countField.set(mockStaffModeManager, vanishToggleCount);

        Field smmField = UltimateDonutSmp.class.getDeclaredField("staffModeManager");
        smmField.setAccessible(true);
        smmField.set(mockPlugin, mockStaffModeManager);

        listener = new StaffModeListener(mockPlugin);
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
}
