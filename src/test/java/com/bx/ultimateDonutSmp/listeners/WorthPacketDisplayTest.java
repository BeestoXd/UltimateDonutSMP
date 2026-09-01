package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.menus.BaseMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorthPacketDisplayTest {

    @Test
    void theSellMenuConfirmButtonIsLeftAlone() throws Exception {
        Object previousServer = installServerThatCreatesInventories();
        try {
            assertTrue(
                    WorthPacketDisplay.isMenuHolder(new TestMenu()),
                    "a plugin menu is a screen built out of items, so none of it is for sale"
            );
        } finally {
            restoreServer(previousServer);
        }

        assertTrue(
                WorthPacketDisplay.shouldSkipSlot(true, 1, 53, 54),
                "slot 53 of the 54 slot sell menu holds Confirm Sell, a lime pane worth.yml prices at 3"
        );
    }

    @Test
    void thePlayerRowsBelowAMenuStillShowWhatTheirItemsAreWorth() {
        assertFalse(
                WorthPacketDisplay.shouldSkipSlot(true, 1, 54, 54),
                "slot 54 is the first of the player's own rows, drawn underneath the menu"
        );
        assertFalse(WorthPacketDisplay.shouldSkipSlot(true, 1, 89, 54));
    }

    @Test
    void aChestIsNotAMenuSoItsContentsKeepTheirPrices() {
        assertFalse(WorthPacketDisplay.isMenuHolder(holderProxy(org.bukkit.block.Container.class)));
    }

    @Test
    void aPlayerHoldingTheirOwnInventoryIsNotAMenu() {
        assertFalse(WorthPacketDisplay.isMenuHolder(holderProxy(Player.class)));
    }

    @Test
    void anInventoryWithNoHolderIsTreatedAsStorage() {
        assertFalse(
                WorthPacketDisplay.isMenuHolder(null),
                "guessing menu here would strip prices off anything the server hands over without an owner"
        );
    }

    @Test
    void aMenuBelongingToAnotherPluginIsLeftAloneToo() {
        assertTrue(WorthPacketDisplay.isMenuHolder(holderProxy(InventoryHolder.class)));
    }

    @Test
    void thePlayersOwnWindowAndPlainChestsAreNeverSkipped() {
        assertFalse(
                WorthPacketDisplay.shouldSkipSlot(true, 0, 5, 54),
                "window 0 is the player's own inventory, which is where the worth line belongs"
        );
        assertFalse(WorthPacketDisplay.shouldSkipSlot(false, 1, 5, 54));
    }

    @Test
    void aWindowWeCannotMeasureIsSkippedRatherThanGuessedAt() {
        assertTrue(
                WorthPacketDisplay.shouldSkipSlot(true, 1, 0, 0),
                "topSize 0 means the menu's last slot is unknown, so pricing any of it risks the button again"
        );
        assertTrue(
                WorthPacketDisplay.shouldSkipSlot(true, 1, -1, 54),
                "-1 is the packet's marker for no particular slot"
        );
    }

    private static InventoryHolder holderProxy(Class<? extends InventoryHolder> type) {
        return (InventoryHolder) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> defaultValue(method)
        );
    }

    private static final class TestMenu extends BaseMenu {

        private TestMenu() {
            super(null, "&8Test menu", 54);
        }

        @Override
        public void build(Player player) {
        }
    }

    // BaseMenu builds its inventory in the constructor, so it needs a server to exist. the previous
    // one is handed back so the next test class gets whatever it was expecting
    private static Object installServerThatCreatesInventories() throws Exception {
        Field serverField = org.bukkit.Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        Object previous = serverField.get(null);

        org.bukkit.Server mockServer = (org.bukkit.Server) Proxy.newProxyInstance(
                org.bukkit.Server.class.getClassLoader(),
                new Class<?>[]{org.bukkit.Server.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("createInventory")) {
                        return Proxy.newProxyInstance(
                                org.bukkit.inventory.Inventory.class.getClassLoader(),
                                new Class<?>[]{org.bukkit.inventory.Inventory.class},
                                (iProxy, iMethod, iArgs) -> defaultValue(iMethod)
                        );
                    }
                    return defaultValue(method);
                }
        );

        serverField.set(null, mockServer);
        return previous;
    }

    private static void restoreServer(Object previous) throws Exception {
        Field serverField = org.bukkit.Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        serverField.set(null, previous);
    }

    private static Object defaultValue(Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == double.class) {
            return 0.0;
        }
        if (returnType == float.class) {
            return 0.0f;
        }
        return null;
    }
}
