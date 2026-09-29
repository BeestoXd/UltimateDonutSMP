package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.WorthManager;
import com.bx.ultimateDonutSmp.menus.BaseMenu;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// worth is added only to the outgoing item packets, the real items are never touched
public class WorthPacketDisplay implements Listener {

    private final UltimateDonutSmp plugin;
    private final ProtocolManager protocolManager;
    private final Set<UUID> inPluginMenu = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingRefresh = ConcurrentHashMap.newKeySet();
    private final Map<UUID, org.bukkit.Material> suppressedMaterials = new ConcurrentHashMap<>();
    private final Set<UUID> openInventories = ConcurrentHashMap.newKeySet();
    private final Map<UUID, ForeignMenu> foreignMenus = new ConcurrentHashMap<>();

    // window contents are the menu rows, then the 36 slots of the player's storage. armor stays on window 0
    static final int PLAYER_STORAGE_SLOTS = 36;

    // 0 until the first lookup, 1 when the fields are usable, 2 when this server build has neither
    private static volatile int containerIdState;
    private static Method playerGetHandle;
    private static Field containerMenuField;
    private static Field containerIdField;

    public WorthPacketDisplay(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        registerPacketListener();
        registerAttemptPickupListener();
    }

    private void registerAttemptPickupListener() {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends org.bukkit.event.Event> eventClass = 
                (Class<? extends org.bukkit.event.Event>) Class.forName("org.bukkit.event.player.PlayerAttemptPickupItemEvent");
            
            plugin.getServer().getPluginManager().registerEvent(
                eventClass,
                this,
                EventPriority.LOWEST,
                (listener, event) -> {
                    try {
                        Player player = (Player) event.getClass().getMethod("getPlayer").invoke(event);
                        org.bukkit.entity.Item itemEntity = (org.bukkit.entity.Item) event.getClass().getMethod("getItem").invoke(event);
                        ItemStack current = itemEntity.getItemStack();
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            plugin.getWorthManager().stripStorageWorthDisplayForNativePickup(player, current);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                },
                plugin,
                true
            );
        } catch (ClassNotFoundException e) {
            // Not on Paper/Folia, ignore
        }
    }

    // an interactive workbench or enchanting table holds functional crafting slots rather than storage.
    // pricing an item being enchanted in slot 0 sends worth lore to bedrock clients, which wipes the
    // enchantment offers on geyser. treating screens that hold client state as menus skips their top
    // rows while keeping worth lines on the player inventory below
    static boolean isMenuInventory(boolean isInPluginMenu, org.bukkit.inventory.Inventory topInv) {
        if (isInPluginMenu) {
            return true;
        }
        if (topInv == null) {
            return false;
        }
        try {
            if (WorthManager.holdsClientState(topInv.getType())) {
                return true;
            }
        } catch (Exception ignored) {}
        return isMenuHolder(topInv.getHolder());
    }

    private boolean isMenuInventory(Player player, org.bukkit.inventory.Inventory topInv) {
        return isMenuInventory(player != null && inPluginMenu.contains(player.getUniqueId()), topInv);
    }

    // a chest belongs to the player, so its contents are worth something and get the line. anything
    // held by a plugin is a screen made of items, and pricing its buttons is nonsense: the sell menu
    // confirms with a lime pane, which worth.yml prices at 3, so the button read "Worth: $3"
    static boolean isMenuHolder(org.bukkit.inventory.InventoryHolder holder) {
        if (holder == null) {
            return false;
        }
        if (holder instanceof BaseMenu) {
            return true;
        }
        return !(holder instanceof org.bukkit.block.Container) && !(holder instanceof org.bukkit.entity.Player);
    }

    // window 0 is the player's own inventory and always keeps the line. above that, only the menu's
    // own rows are skipped; the player rows underneath still show what their items are worth. a
    // topSize the server would not give us means skip the window rather than guess at the boundary
    static boolean shouldSkipSlot(boolean isMenu, int windowId, int slot, int topSize) {
        if (!isMenu || windowId <= 0) {
            return false;
        }
        return topSize == 0 || slot == -1 || slot < topSize;
    }

    // -1 means the packet is not a menu, so every slot keeps its price. 0 means it is a menu whose
    // height we could not measure, so none of it is priced. anything higher is the menu's own row
    // count: those slots are buttons, and the storage after them still belongs to the player.
    //
    // a plugin that draws the screen itself (TrMenu's packet windows) never opens a Bukkit inventory,
    // so the player is still on their crafting grid while the buttons are on screen. a chest the
    // server opened has already taken that grid over, and its window id is the container they are
    // actually in. pricing those buttons is how a menu icon ends up reading "Worth: $3"
    static int packetMenuTopSize(boolean bukkitMenu,
                                 int bukkitTopSize,
                                 int windowId,
                                 boolean viewingOwnCrafting,
                                 int openContainerId,
                                 int itemCount,
                                 int rememberedTopSize) {
        if (windowId <= 0) {
            return -1;
        }
        boolean sameContainer = openContainerId >= 0 && windowId == openContainerId;
        boolean foreign = false;
        if (!sameContainer) {
            foreign = viewingOwnCrafting || (openContainerId >= 0 && windowId != openContainerId);
            if (!foreign && itemCount > PLAYER_STORAGE_SLOTS) {
                foreign = itemCount - PLAYER_STORAGE_SLOTS != bukkitTopSize;
            }
        }
        if (!foreign) {
            return bukkitMenu ? bukkitTopSize : -1;
        }
        if (itemCount > PLAYER_STORAGE_SLOTS) {
            return itemCount - PLAYER_STORAGE_SLOTS;
        }
        return rememberedTopSize > 0 ? rememberedTopSize : 0;
    }

    private void registerPacketListener() {
        final UltimateDonutSmp uds = plugin; // packetadapter has its own plugin field
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Server.SET_SLOT,
                PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isPlayerTemporary()) {
                    return;
                }
                Player player = event.getPlayer();
                if (player == null || player.getGameMode() == GameMode.CREATIVE) {
                    return;
                }
                org.bukkit.Material suppressedMat = suppressedMaterials.get(player.getUniqueId());
                if (!uds.getWorthManager().isWorthDisplayEnabledFor(player)) {
                    return;
                }

                PacketContainer packet = event.getPacket();
                int windowId = readWindowId(packet);
                int topSize = 0;
                boolean isMenu = false;
                int bukkitTopSize = 0;
                boolean bukkitMenu = false;
                boolean viewingOwnCrafting = false;

                if (windowId > 0) {
                    try {
                        org.bukkit.inventory.InventoryView openView = player.getOpenInventory();
                        if (openView != null) {
                            org.bukkit.inventory.Inventory topInv = openView.getTopInventory();
                            if (topInv != null) {
                                bukkitTopSize = topInv.getSize();
                                viewingOwnCrafting = topInv.getType() == org.bukkit.event.inventory.InventoryType.CRAFTING;
                                bukkitMenu = isMenuInventory(player, topInv);
                            }
                        }
                    } catch (Exception ignored) {}
                    if (!bukkitMenu && inPluginMenu.contains(player.getUniqueId())) {
                        bukkitMenu = true;
                    }
                }

                boolean windowItems = event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS;
                List<ItemStack> windowItemsList = null;
                int itemCount = -1;
                if (windowItems) {
                    windowItemsList = packet.getItemListModifier().read(0);
                    itemCount = windowItemsList == null ? 0 : windowItemsList.size();
                    if (itemCount == 0) {
                        return;
                    }
                }

                if (windowId > 0) {
                    ForeignMenu remembered = foreignMenus.get(player.getUniqueId());
                    int rememberedTopSize = remembered != null && remembered.windowId == windowId
                            ? remembered.topSize
                            : -1;
                    int menuTop = packetMenuTopSize(
                            bukkitMenu,
                            bukkitTopSize,
                            windowId,
                            viewingOwnCrafting,
                            readOpenContainerId(player),
                            itemCount,
                            rememberedTopSize
                    );
                    isMenu = menuTop >= 0;
                    topSize = isMenu ? menuTop : 0;
                    if (isMenu && itemCount > PLAYER_STORAGE_SLOTS) {
                        foreignMenus.put(player.getUniqueId(), new ForeignMenu(windowId, topSize));
                    }
                }

                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    int slot = readSlotIndex(packet);
                    if (shouldSkipSlot(isMenu, windowId, slot, topSize)) {
                        return;
                    }
                    ItemStack item = packet.getItemModifier().read(0);
                    if (item != null && item.getType() == suppressedMat) {
                        return;
                    }
                    ItemStack rendered = uds.getWorthManager().renderClientWorthDisplay(item);
                    if (rendered != item) {
                        packet.getItemModifier().write(0, rendered);
                    }
                    return;
                }

                List<ItemStack> items = windowItemsList;
                if (items == null || items.isEmpty()) {
                    return;
                }
                List<ItemStack> updated = new ArrayList<>(items.size());
                boolean changed = false;
                for (int i = 0; i < items.size(); i++) {
                    ItemStack item = items.get(i);
                    if (shouldSkipSlot(isMenu, windowId, i, topSize)) {
                        updated.add(item);
                        continue;
                    }
                    if (item != null && item.getType() == suppressedMat) {
                        updated.add(item);
                        continue;
                    }
                    ItemStack rendered = uds.getWorthManager().renderClientWorthDisplay(item);
                    if (rendered != item) {
                        changed = true;
                        updated.add(rendered);
                    } else {
                        updated.add(item);
                    }
                }
                if (changed) {
                    packet.getItemListModifier().write(0, updated);
                }
            }
        });
    }

    private static int readWindowId(PacketContainer packet) {
        if (packet.getIntegers().size() > 0) {
            return packet.getIntegers().read(0);
        }
        return -1;
    }

    private static int readSlotIndex(PacketContainer packet) {
        if (packet.getShorts().size() > 0) {
            return packet.getShorts().read(0);
        }
        if (packet.getIntegers().size() > 2) {
            return packet.getIntegers().read(2);
        }
        return -1;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        openInventories.add(event.getPlayer().getUniqueId());
        if (event.getInventory().getHolder() instanceof BaseMenu) {
            inPluginMenu.add(event.getPlayer().getUniqueId());
        } else {
            plugin.getWorthManager().sanitizeInventory(event.getInventory());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        inPluginMenu.remove(uuid);
        suppressedMaterials.remove(uuid);
        openInventories.remove(uuid);
        foreignMenus.remove(uuid);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        suppressedMaterials.remove(uuid);
        inPluginMenu.remove(uuid);
        openInventories.remove(uuid);
        foreignMenus.remove(uuid);
        plugin.getWorthManager().clearWorthDisplay(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        suppressedMaterials.remove(uuid);
        inPluginMenu.remove(uuid);
        openInventories.remove(uuid);
        pendingRefresh.remove(uuid);
        foreignMenus.remove(uuid);
    }

    // strip any leftover worth nbt so the real item stays clean
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack current = event.getItem().getItemStack();
        ItemStack stripped = plugin.getWorthManager().stripWorthDisplay(current);
        if (stripped != current) {
            event.getItem().setItemStack(stripped);
        }
        plugin.getWorthManager().stripStorageWorthDisplayForNativePickup(player, current);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        if (current != null) {
            ItemStack stripped = plugin.getWorthManager().stripWorthDisplay(current);
            if (stripped != current) {
                event.setCurrentItem(stripped);
            }
        }
        ItemStack cursor = event.getCursor();
        if (cursor != null && !cursor.getType().isAir()) {
            ItemStack stripped = plugin.getWorthManager().stripWorthDisplay(cursor);
            if (stripped != cursor) {
                event.setCursor(stripped);
            }
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        openInventories.add(player.getUniqueId());
        if (cursor != null && !cursor.getType().isAir()) {
            suppressedMaterials.put(player.getUniqueId(), cursor.getType());
        } else if (current != null && !current.getType().isAir()) {
            suppressedMaterials.put(player.getUniqueId(), current.getType());
        }
        scheduleCursorEval(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            ItemStack oldCursor = event.getOldCursor();
            if (oldCursor != null && !oldCursor.getType().isAir()) {
                suppressedMaterials.put(player.getUniqueId(), oldCursor.getType());
            }
            scheduleCursorEval(player);
        }
    }

    // resend a tick later so worth reappears and matches whether the cursor is holding an item,
    // except on the screens whose client-side state a resend would wipe
    private void scheduleCursorEval(Player player) {
        if (inPluginMenu.contains(player.getUniqueId())) {
            return;
        }
        if (!plugin.getWorthManager().isWorthDisplayEnabledFor(player)) {
            return;
        }
        if (!pendingRefresh.add(player.getUniqueId())) {
            return;
        }
        plugin.getSpigotScheduler().runEntityLater(player, () -> {
            UUID uuid = player.getUniqueId();
            pendingRefresh.remove(uuid);
            if (!player.isOnline() || player.getGameMode() == GameMode.CREATIVE) {
                suppressedMaterials.remove(uuid);
                return;
            }
            ItemStack onCursor = player.getItemOnCursor();
            if (onCursor != null && !onCursor.getType().isAir()) {
                suppressedMaterials.put(uuid, onCursor.getType());
            } else {
                suppressedMaterials.remove(uuid);
            }
            if (openInventories.contains(uuid) && plugin.getWorthManager().canResendOpenInventory(player)) {
                player.updateInventory();
            }
        }, 1L);
    }

    // the container id of whatever Bukkit currently has open. -1 when this server build does not
    // expose it; the crafting-view check still catches a packet menu in that case
    private static int readOpenContainerId(Player player) {
        if (player == null || containerIdState == 2) {
            return -1;
        }
        try {
            ensureContainerIdAccess(player);
            if (containerIdState != 1 || playerGetHandle == null || containerMenuField == null || containerIdField == null) {
                return -1;
            }
            Object handle = playerGetHandle.invoke(player);
            if (handle == null) {
                return -1;
            }
            Object menu = containerMenuField.get(handle);
            if (menu == null) {
                return -1;
            }
            return containerIdField.getInt(menu);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    private static void ensureContainerIdAccess(Player player) {
        if (containerIdState != 0) {
            return;
        }
        synchronized (WorthPacketDisplay.class) {
            if (containerIdState != 0) {
                return;
            }
            try {
                Method handleMethod = player.getClass().getMethod("getHandle");
                Object handle = handleMethod.invoke(player);
                if (handle == null) {
                    return;
                }
                Field menuField = findInstanceField(handle.getClass(), "containerMenu");
                Object menu = menuField.get(handle);
                if (menu == null) {
                    return;
                }
                Field idField = findInstanceField(menu.getClass(), "containerId");
                playerGetHandle = handleMethod;
                containerMenuField = menuField;
                containerIdField = idField;
                containerIdState = 1;
            } catch (Throwable ignored) {
                containerIdState = 2;
            }
        }
    }

    private static Field findInstanceField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static final class ForeignMenu {
        private final int windowId;
        private final int topSize;

        private ForeignMenu(int windowId, int topSize) {
            this.windowId = windowId;
            this.topSize = topSize;
        }
    }
}
