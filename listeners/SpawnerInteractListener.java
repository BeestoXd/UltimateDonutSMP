package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.SpawnerInstance;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SpawnerInteractListener implements Listener {

    private final UltimateDonutSmp plugin;

    public SpawnerInteractListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        SpawnerInstance instance = plugin.getSpawnerManager().getSpawner(block);
        if (instance == null) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        event.setCancelled(true);

        if (plugin.getSpawnerManager().isSpawnerItem(held)) {
            var result = plugin.getSpawnerManager().stackSpawner(player, block, held);
            player.sendMessage(ColorUtils.toComponent(result.message()));
            if (result.success()) {
                plugin.getSpawnerManager().consumeHeldSpawnerItem(player);
            }
            return;
        }

        if (!plugin.getSpawnerManager().canOpen(player, instance)) {
            player.sendMessage(ColorUtils.toComponent("&cYou do not have access to that spawner."));
            return;
        }

        plugin.getSpawnerManager().openStorage(player, instance, 1);
        plugin.getAntiEspManager().updatePlayer(player);
    }
}
