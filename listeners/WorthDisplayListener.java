package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.BaseMenu;
import com.bx.ultimateDonutSmp.menus.SellMenu;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorthDisplayListener implements Listener {

    private static final long AMETHYST_REFRESH_DELAY_TICKS = 20L;
    private final UltimateDonutSmp plugin;
    private final Set<UUID> dirtyPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingRefreshes = ConcurrentHashMap.newKeySet();

    public WorthDisplayListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        startDirtySyncTask();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getWorthManager().clearWorthDisplay(event.getPlayer());
        queueRefresh(event.getPlayer(), 2L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        pendingRefreshes.remove(uuid);
        dirtyPlayers.remove(uuid);
        plugin.getWorthManager().clearWorthDisplay(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() == GameMode.CREATIVE) {
            queueClear(event.getPlayer(), 1L);
            return;
        }

        queueRefresh(event.getPlayer(), 2L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (isPlayerInventoryView(event.getInventory())) {
            return;
        }

        if (plugin.getInvseeManager().isInvseeInventory(event.getInventory())) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof SellMenu)
                && !(event.getInventory().getHolder() instanceof BaseMenu)) {
            plugin.getWorthManager().sanitizeInventory(event.getInventory());
        }

        queueRefresh(player, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (isAmethystItem(event.getCurrentItem()) || isAmethystItem(event.getCursor())) {
            return;
        }

        queueRefresh(player, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (isAmethystItem(event.getOldCursor())) {
            return;
        }

        queueRefresh(player, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (isPlayerInventoryView(event.getInventory())) {
            return;
        }

        if (plugin.getInvseeManager().isInvseeInventory(event.getInventory())) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof SellMenu)
                && !(event.getInventory().getHolder() instanceof BaseMenu)) {
            plugin.getWorthManager().sanitizeInventory(event.getInventory());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        event.getItemDrop().setItemStack(
                plugin.getWorthManager().stripWorthDisplay(event.getItemDrop().getItemStack())
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack current = event.getItem().getItemStack();
            ItemStack updated = plugin.getWorthManager().applyWorthDisplayForPlayer(player, current);
            if (updated != current) {
                event.getItem().setItemStack(updated);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        List<org.bukkit.inventory.ItemStack> drops = event.getDrops();
        for (int index = 0; index < drops.size(); index++) {
            drops.set(index, plugin.getWorthManager().stripWorthDisplay(drops.get(index)));
        }

        UUID uuid = event.getEntity().getUniqueId();
        pendingRefreshes.remove(uuid);
        dirtyPlayers.remove(uuid);
        plugin.getWorthManager().clearWorthDisplay(event.getEntity());
    }

    private void queueRefresh(Player player, long delayTicks) {
        long effectiveDelay = delayTicks;
        if (plugin.getAmethystToolsManager().isVisualSyncSuppressed(player.getUniqueId())) {
            effectiveDelay = Math.max(effectiveDelay, AMETHYST_REFRESH_DELAY_TICKS);
        }

        UUID uuid = player.getUniqueId();
        if (!pendingRefreshes.add(uuid)) {
            return;
        }

        plugin.getPaperScheduler().runEntityLater(player, () -> {
            pendingRefreshes.remove(uuid);
            if (!player.isOnline()) {
                return;
            }
            dirtyPlayers.add(uuid);
        }, effectiveDelay);
    }

    private void queueClear(Player player, long delayTicks) {
        plugin.getPaperScheduler().runEntityLater(player, () -> {
            if (!player.isOnline()) {
                return;
            }

            pendingRefreshes.remove(player.getUniqueId());
            dirtyPlayers.remove(player.getUniqueId());
            plugin.getWorthManager().clearWorthDisplay(player);
        }, delayTicks);
    }

    private void startDirtySyncTask() {
        plugin.getPaperScheduler().runGlobalTimer(this::syncDirtyPlayers, 1L, 1L);
    }

    private void syncDirtyPlayers() {
        if (dirtyPlayers.isEmpty()) {
            return;
        }

        for (UUID uuid : Set.copyOf(dirtyPlayers)) {
            Player player = plugin.getServer().getPlayer(uuid);
            dirtyPlayers.remove(uuid);

            if (player == null || !player.isOnline()) {
                continue;
            }

            plugin.getPaperScheduler().runEntity(player, () -> {
                if (!player.isOnline()) {
                    return;
                }

                if (player.getGameMode() == GameMode.CREATIVE) {
                    plugin.getWorthManager().clearWorthDisplay(player);
                    return;
                }

                plugin.getWorthManager().syncWorthDisplay(player);
            });
        }
    }

    private boolean isPlayerInventoryView(Inventory inventory) {
        return inventory != null
                && (inventory.getHolder() instanceof Player
                || inventory.getType() == InventoryType.CRAFTING
                || inventory.getType() == InventoryType.CREATIVE);
    }

    private boolean isAmethystItem(ItemStack item) {
        return plugin.getAmethystToolsManager().isAmethystTool(item);
    }
}
