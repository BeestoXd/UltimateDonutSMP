package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class SpawnerBlockListener implements Listener {

    private final UltimateDonutSmp plugin;

    public SpawnerBlockListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getSpawnerManager().isEnabled()) {
            return;
        }
        ItemStack item = event.getItemInHand();
        if (!plugin.getSpawnerManager().isSpawnerItem(item)) {
            return;
        }

        var result = plugin.getSpawnerManager().placeSpawner(event.getPlayer(), event.getBlockPlaced(), item);
        if (!result.success()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ColorUtils.toComponent(result.message()));
            return;
        }

        Player player = event.getPlayer();
        org.bukkit.inventory.EquipmentSlot handSlot = event.getHand();
        final int finalRemaining;
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            int totalPlaced = result.consumedAmount() > 0 ? result.consumedAmount() : (player.isSneaking() ? item.getAmount() : 1);
            int currentAmount = item.getAmount();
            finalRemaining = Math.max(0, currentAmount - totalPlaced);

            if (finalRemaining <= 0) {
                item.setAmount(1);
                if (handSlot == org.bukkit.inventory.EquipmentSlot.OFF_HAND) {
                    player.getInventory().setItemInOffHand(null);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }
            } else {
                item.setAmount(finalRemaining + 1);
            }
        } else {
            finalRemaining = -1;
        }

        plugin.getSpigotScheduler().runEntity(player, () -> {
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE && finalRemaining >= 0) {
                if (handSlot == org.bukkit.inventory.EquipmentSlot.OFF_HAND) {
                    if (finalRemaining <= 0) {
                        player.getInventory().setItemInOffHand(null);
                    } else {
                        ItemStack off = player.getInventory().getItemInOffHand();
                        if (off != null && plugin.getSpawnerManager().isSpawnerItem(off)) {
                            off.setAmount(finalRemaining);
                            player.getInventory().setItemInOffHand(off);
                        }
                    }
                } else {
                    if (finalRemaining <= 0) {
                        player.getInventory().setItemInMainHand(null);
                    } else {
                        ItemStack main = player.getInventory().getItemInMainHand();
                        if (main != null && plugin.getSpawnerManager().isSpawnerItem(main)) {
                            main.setAmount(finalRemaining);
                            player.getInventory().setItemInMainHand(main);
                        }
                    }
                }
            }
            player.updateInventory();
        });

        player.sendMessage(ColorUtils.toComponent(result.message()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getSpawnerManager().isEnabled()) {
            return;
        }
        Block block = event.getBlock();
        if (plugin.getSpawnStashManager() != null && plugin.getSpawnStashManager().isActiveBlock(block)) {
            return;
        }
        if (plugin.getSpawnerManager().getSpawner(block) == null) {
            return;
        }

        Player player = event.getPlayer();
        var result = plugin.getSpawnerManager().breakSpawner(player, block);
        if (!result.success()) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.toComponent(result.message()));
            return;
        }

        event.setDropItems(false);
        event.setExpToDrop(0);
        player.sendMessage(ColorUtils.toComponent(result.message()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (!plugin.getSpawnerManager().isEnabled() || !plugin.getSpawnerManager().isCancelMobSpawn()) {
            return;
        }
        Block spawnerBlock = event.getSpawner() != null ? event.getSpawner().getBlock() : null;
        if (spawnerBlock != null && plugin.getSpawnerManager().getSpawner(spawnerBlock) != null) {
            event.setCancelled(true);
            if (event.getEntity() != null && event.getEntity().isValid()) {
                event.getEntity().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(org.bukkit.event.entity.CreatureSpawnEvent event) {
        if (!plugin.getSpawnerManager().isEnabled() || !plugin.getSpawnerManager().isCancelMobSpawn()) {
            return;
        }
        if (event.getSpawnReason() == org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER) {
            org.bukkit.Location loc = event.getLocation();
            if (loc != null && plugin.getSpawnerManager().isNearManagedSpawner(loc, 12.0D)) {
                event.setCancelled(true);
                if (event.getEntity() != null && event.getEntity().isValid()) {
                    event.getEntity().remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!plugin.getSpawnerManager().isEnabled()) {
            return;
        }
        filterManagedSpawners(event.blockList().iterator());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.getSpawnerManager().isEnabled()) {
            return;
        }
        filterManagedSpawners(event.blockList().iterator());
    }

    private void filterManagedSpawners(Iterator<Block> iterator) {
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (plugin.getSpawnStashManager() != null && plugin.getSpawnStashManager().isActiveBlock(block)) {
                continue;
            }
            if (block.getType() == Material.SPAWNER && plugin.getSpawnerManager().getSpawner(block) != null) {
                iterator.remove();
            }
        }
    }
}
