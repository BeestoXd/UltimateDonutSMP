package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnderChestListener implements Listener {

    private final UltimateDonutSmp plugin;

    public EnderChestListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnderChestInteract(PlayerInteractEvent event) {
        if (!plugin.getEnderChestManager().shouldInterceptVanillaOpen()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != null && event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.ENDER_CHEST) {
            return;
        }

        Player player = event.getPlayer();
        Location sourceLocation = clickedBlock.getLocation();

        event.setCancelled(true);
        plugin.getFoliaScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }
            plugin.getEnderChestManager().open(player, sourceLocation);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (!plugin.getEnderChestManager().shouldInterceptVanillaOpen()) {
            return;
        }

        if (event.getInventory().getType() != InventoryType.ENDER_CHEST) {
            return;
        }

        if (plugin.getEnderChestManager().isCustomEnderChest(event.getInventory())) {
            return;
        }

        event.setCancelled(true);
        plugin.getFoliaScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }
            plugin.getEnderChestManager().open(player);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (plugin.getEnderChestManager().isInspectionView(event.getView())) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            return;
        }
        if (blockUnsafeInsert(event)) {
            return;
        }
        plugin.getEnderChestManager().markDirty(event.getView());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (plugin.getEnderChestManager().isInspectionView(event.getView())) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            return;
        }
        if (blockUnsafeDrag(event)) {
            return;
        }
        plugin.getEnderChestManager().markDirty(event.getView());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            plugin.getEnderChestManager().handleInspectionClose(player, event.getInventory());
            plugin.getEnderChestManager().handleClose(player, event.getInventory());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getEnderChestManager().handleInspectionViewerQuit(event.getPlayer());
        plugin.getEnderChestManager().handleQuit(event.getPlayer());
    }

    private boolean blockUnsafeInsert(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)
                || !plugin.getEnderChestManager().isCustomEnderChestView(event.getView())) {
            return false;
        }

        Inventory topInventory = event.getView().getTopInventory();
        boolean clickedTop = event.getRawSlot() >= 0 && event.getRawSlot() < topInventory.getSize();
        ItemStack candidate = null;

        if (clickedTop) {
            candidate = event.getCursor();
            if ((candidate == null || candidate.getType().isAir()) && event.getClick() == ClickType.NUMBER_KEY) {
                int hotbarButton = event.getHotbarButton();
                if (hotbarButton >= 0) {
                    candidate = player.getInventory().getItem(hotbarButton);
                }
            }
            if ((candidate == null || candidate.getType().isAir()) && event.getClick() == ClickType.SWAP_OFFHAND) {
                candidate = player.getInventory().getItemInOffHand();
            }
        } else if (event.isShiftClick()) {
            candidate = event.getCurrentItem();
        }

        if (candidate == null || candidate.getType().isAir()) {
            return false;
        }

        if (plugin.getCrashProtectionManager()
                .validateOrNotify(player, candidate, com.bx.ultimateDonutSmp.managers.CrashProtectionManager.Context.ENDER_CHEST)
                .allowed()) {
            return false;
        }

        event.setCancelled(true);
        plugin.getFoliaScheduler().runEntity(player, player::updateInventory);
        return true;
    }

    private boolean blockUnsafeDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)
                || !plugin.getEnderChestManager().isCustomEnderChestView(event.getView())) {
            return false;
        }

        Inventory topInventory = event.getView().getTopInventory();
        boolean draggingIntoTop = event.getRawSlots().stream().anyMatch(slot -> slot >= 0 && slot < topInventory.getSize());
        if (!draggingIntoTop) {
            return false;
        }

        ItemStack candidate = event.getOldCursor();
        if (candidate == null || candidate.getType().isAir()) {
            return false;
        }

        if (plugin.getCrashProtectionManager()
                .validateOrNotify(player, candidate, com.bx.ultimateDonutSmp.managers.CrashProtectionManager.Context.ENDER_CHEST)
                .allowed()) {
            return false;
        }

        event.setCancelled(true);
        plugin.getFoliaScheduler().runEntity(player, player::updateInventory);
        return true;
    }
}
