package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SpawnStashListener implements Listener {

    private final UltimateDonutSmp plugin;

    public SpawnStashListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.getSpawnStashManager().isEnabled()
                || event.getHand() != EquipmentSlot.HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block != null) {
            plugin.getSpawnStashManager().triggerBlockAlert(event.getPlayer(), block, "interact");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!plugin.getSpawnStashManager().isEnabled()) {
            return;
        }
        if (!(event.getPlayer() instanceof org.bukkit.entity.Player player)) {
            return;
        }

        Location location = event.getInventory().getLocation();
        if (location != null) {
            plugin.getSpawnStashManager().triggerBlockAlert(player, location.getBlock(), "open");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getSpawnStashManager().isEnabled()) {
            return;
        }
        if (!plugin.getSpawnStashManager().triggerBlockAlert(event.getPlayer(), event.getBlock(), "break")) {
            return;
        }
        if (!plugin.getSpawnStashManager().isProtectionEnabled()) {
            if (plugin.getSpawnStashManager().handleBrokenBlock(event.getPlayer(), event.getBlock())) {
                event.setDropItems(false);
                event.setExpToDrop(0);
            }
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ColorUtils.toComponent(plugin.getSpawnStashManager()
                .publicMessage("BLOCKED-BREAK", "&cᴛʜɪѕ ѕᴛᴀѕʜ ɪѕ ᴘʀᴏᴛᴇᴄᴛᴇᴅ.")));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!plugin.getSpawnStashManager().isEnabled()) {
            return;
        }
        if (!plugin.getSpawnStashManager().isProtectionEnabled()) {
            event.blockList().forEach(block -> plugin.getSpawnStashManager().releaseDestroyedBlock(block));
            return;
        }
        event.blockList().removeIf(block -> plugin.getSpawnStashManager().isActiveBlock(block));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.getSpawnStashManager().isEnabled()) {
            return;
        }
        if (!plugin.getSpawnStashManager().isProtectionEnabled()) {
            event.blockList().forEach(block -> plugin.getSpawnStashManager().releaseDestroyedBlock(block));
            return;
        }
        event.blockList().removeIf(block -> plugin.getSpawnStashManager().isActiveBlock(block));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (plugin.getSpawnStashManager().isActiveSpawner(event.getSpawner().getBlock())) {
            event.setCancelled(true);
        }
    }
}
