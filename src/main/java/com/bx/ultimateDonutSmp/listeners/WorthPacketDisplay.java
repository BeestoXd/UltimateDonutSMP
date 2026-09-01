package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
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

    private boolean isMenuInventory(Player player, org.bukkit.inventory.Inventory topInv) {
        if (player != null && inPluginMenu.contains(player.getUniqueId())) {
            return true;
        }
        return topInv != null && isMenuHolder(topInv.getHolder());
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

                if (windowId > 0) {
                    try {
                        org.bukkit.inventory.InventoryView openView = player.getOpenInventory();
                        if (openView != null) {
                            org.bukkit.inventory.Inventory topInv = openView.getTopInventory();
                            if (topInv != null) {
                                topSize = topInv.getSize();
                                isMenu = isMenuInventory(player, topInv);
                            }
                        }
                    } catch (Exception ignored) {}
                    if (!isMenu && inPluginMenu.contains(player.getUniqueId())) {
                        isMenu = true;
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

                List<ItemStack> items = packet.getItemListModifier().read(0);
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
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        suppressedMaterials.remove(uuid);
        inPluginMenu.remove(uuid);
        openInventories.remove(uuid);
        plugin.getWorthManager().clearWorthDisplay(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        suppressedMaterials.remove(uuid);
        inPluginMenu.remove(uuid);
        openInventories.remove(uuid);
        pendingRefresh.remove(uuid);
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
}
