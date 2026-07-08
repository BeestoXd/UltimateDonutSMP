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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// worth is added only to the outgoing item packets, the real items are never touched
public class WorthPacketDisplay implements Listener {

    private final UltimateDonutSmp plugin;
    private final ProtocolManager protocolManager;
    private final Set<UUID> inPluginMenu = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingRefresh = ConcurrentHashMap.newKeySet();
    private final Set<UUID> suppressed = ConcurrentHashMap.newKeySet();

    public WorthPacketDisplay(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        registerPacketListener();
    }

    private void registerPacketListener() {
        final UltimateDonutSmp uds = plugin; // packetadapter has its own plugin field
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Server.SET_SLOT,
                PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null || player.getGameMode() == GameMode.CREATIVE) {
                    return;
                }
                if (inPluginMenu.contains(player.getUniqueId())) {
                    return; // menus render their own worth
                }
                if (suppressed.contains(player.getUniqueId())) {
                    return; // holding an item, keep items plain so native drag/stack works
                }
                if (!uds.getWorthManager().isWorthDisplayEnabledFor(player)) {
                    return;
                }

                PacketContainer packet = event.getPacket();
                int windowId = readWindowId(packet);
                // editing the held/offhand item replays the equip animation, so skip them
                int heldSlot = 36 + player.getInventory().getHeldItemSlot();
                int offhandSlot = 45;

                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    if (windowId == 0) {
                        int slot = readSlotIndex(packet);
                        if (slot == heldSlot || slot == offhandSlot) {
                            return;
                        }
                    }
                    ItemStack item = packet.getItemModifier().read(0);
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
                    if (windowId == 0 && (i == heldSlot || i == offhandSlot)) {
                        updated.add(item);
                        continue;
                    }
                    ItemStack rendered = uds.getWorthManager().renderClientWorthDisplay(item);
                    if (rendered != item) {
                        changed = true;
                    }
                    updated.add(rendered);
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
        if (event.getInventory().getHolder() instanceof BaseMenu) {
            inPluginMenu.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        inPluginMenu.remove(uuid);
        suppressed.remove(uuid);
        if (event.getPlayer() instanceof Player player) {
            scheduleCursorEval(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        suppressed.remove(uuid);
        inPluginMenu.remove(uuid);
        plugin.getWorthManager().clearWorthDisplay(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        suppressed.remove(uuid);
        inPluginMenu.remove(uuid);
        pendingRefresh.remove(uuid);
    }

    // strip any leftover worth nbt so the real item stays clean
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        ItemStack current = event.getItem().getItemStack();
        ItemStack stripped = plugin.getWorthManager().stripWorthDisplay(current);
        if (stripped != current) {
            event.getItem().setItemStack(stripped);
        }
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
        boolean involvesItem = (cursor != null && !cursor.getType().isAir())
                || (current != null && !current.getType().isAir());
        if (involvesItem) {
            suppressed.add(player.getUniqueId());
        }
        scheduleCursorEval(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            suppressed.add(player.getUniqueId());
            scheduleCursorEval(player);
        }
    }

    // resend a tick later so worth reappears and matches whether the cursor is holding an item
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
                suppressed.remove(uuid);
                return;
            }
            ItemStack onCursor = player.getItemOnCursor();
            if (onCursor != null && !onCursor.getType().isAir()) {
                suppressed.add(uuid);
            } else {
                suppressed.remove(uuid);
            }
            player.updateInventory();
        }, 1L);
    }
}
