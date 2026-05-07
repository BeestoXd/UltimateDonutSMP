package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;

public class EnderChestListener implements Listener {

    private final UltimateDonutSmp plugin;

    public EnderChestListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
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
        plugin.getPaperScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }
            plugin.getEnderChestManager().open(player);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        plugin.getEnderChestManager().markDirty(event.getView());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        plugin.getEnderChestManager().markDirty(event.getView());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            plugin.getEnderChestManager().handleClose(player, event.getInventory());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getEnderChestManager().handleQuit(event.getPlayer());
    }
}
