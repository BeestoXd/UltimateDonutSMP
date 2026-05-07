package com.bx.ultimateDonutSmp.amethyst;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AmethystToolsTask implements Runnable {

    private final UltimateDonutSmp plugin;
    private final AmethystToolsManager manager;

    private AmethystToolsTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.manager = plugin.getAmethystToolsManager();
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getPaperScheduler().runGlobalTimer(new AmethystToolsTask(plugin), 20L, 20L); // every second
    }

    @Override
    public void run() {
        plugin.getPaperScheduler().forEachOnlinePlayer(this::checkInventory);
    }

    private void checkInventory(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (!manager.isAmethystTool(item)) {
                continue;
            }

            if (manager.sanitizeInventorySlot(player, slot, true)) {
                continue;
            }
        }

        if (manager.isVisualSyncSuppressed(player.getUniqueId())) {
            return;
        }

        syncHeldCountdown(player);
        syncOffHandCountdown(player);
        syncCursorCountdown(player);
    }

    private void syncHeldCountdown(Player player) {
        int heldSlot = player.getInventory().getHeldItemSlot();
        if (manager.sanitizeInventorySlot(player, heldSlot, true)) {
            return;
        }

        ItemStack held = player.getInventory().getItem(heldSlot);
        if (manager.isAmethystTool(held) && manager.updateLoreCountdown(held)) {
            player.getInventory().setItem(heldSlot, held);
        }
    }

    private void syncOffHandCountdown(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (!manager.isAmethystTool(offHand)) {
            return;
        }

        if (manager.sanitizeInventorySlot(player, 40, true)) {
            return;
        }

        offHand = player.getInventory().getItemInOffHand();
        if (manager.isAmethystTool(offHand) && manager.updateLoreCountdown(offHand)) {
            player.getInventory().setItemInOffHand(offHand);
        }
    }

    private void syncCursorCountdown(Player player) {
        if (manager.sanitizeCursorItem(player, true)) {
            return;
        }

        ItemStack cursor = player.getItemOnCursor();
        if (manager.isAmethystTool(cursor) && manager.updateLoreCountdown(cursor)) {
            player.setItemOnCursor(cursor);
        }
    }
}
