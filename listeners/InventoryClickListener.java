package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.BaseMenu;
import com.bx.ultimateDonutSmp.menus.CrateEditorMenu;
import com.bx.ultimateDonutSmp.menus.RTPMenu;
import com.bx.ultimateDonutSmp.menus.SellMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    private final UltimateDonutSmp plugin;

    public InventoryClickListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof BaseMenu menu)) return;
        Inventory topInventory = event.getView().getTopInventory();

        if (menu instanceof SellMenu sellMenu) {
            sellMenu.handleInventoryClick(event);
            return;
        }

        if (menu instanceof RTPMenu) {
            handleRtpMenuClick(event, player, menu);
            return;
        }

        if (menu instanceof CrateEditorMenu crateEditorMenu) {
            crateEditorMenu.handleInventoryClick(event);
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(topInventory)) return;
        if (event.getCurrentItem() == null) return;
        menu.handleClick(event.getSlot(), player, event.getClick());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof BaseMenu menu)) return;

        if (menu instanceof SellMenu sellMenu) {
            sellMenu.handleInventoryDrag(event);
            return;
        }

        if (menu instanceof RTPMenu && event.getWhoClicked() instanceof Player player) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
            syncInventory(player);
            return;
        }

        if (menu instanceof CrateEditorMenu crateEditorMenu) {
            crateEditorMenu.handleInventoryDrag(event);
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getInventory().getHolder() instanceof BaseMenu menu) {
            menu.onClose(player);
        }
    }

    private void handleRtpMenuClick(InventoryClickEvent event, Player player, BaseMenu menu) {
        Inventory topInventory = event.getView().getTopInventory();
        ItemStack originalCursor = event.getCursor() == null ? null : event.getCursor().clone();
        int rawSlot = event.getRawSlot();
        ClickType clickType = event.getClick();
        boolean validTopClick = event.getClickedInventory() != null
                && event.getClickedInventory().equals(topInventory)
                && event.getCurrentItem() != null
                && !event.getCurrentItem().getType().isAir();

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

        plugin.getPaperScheduler().runEntity(player, () -> {
            if (!player.isOnline()) {
                return;
            }

            player.setItemOnCursor(originalCursor);
            player.closeInventory();
            player.updateInventory();

            if (validTopClick) {
                menu.handleClick(rawSlot, player, clickType);
            }
        });
    }

    private void syncInventory(Player player) {
        plugin.getPaperScheduler().runEntity(player, player::updateInventory);
    }
}
