package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilModerationListener implements Listener {

    private final UltimateDonutSmp plugin;

    public AnvilModerationListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        if (!(event.getView().getTopInventory() instanceof AnvilInventory)) {
            return;
        }

        // Slot 2 is the result slot of the anvil
        if (event.getRawSlot() != 2) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = clickedItem.getItemMeta().getDisplayName();
        String matchedWord = plugin.getAnvilModerationManager().findInappropriateWord(displayName);

        if (matchedWord != null) {
            if (player.hasPermission("anvilmod.admin")) {
                return; // Bypass moderation
            }

            event.setCancelled(true);
            player.closeInventory();

            String strippedInput = ColorUtils.strip(displayName);
            plugin.getAnvilModerationManager().punish(player, matchedWord, strippedInput);
        }
    }
}
