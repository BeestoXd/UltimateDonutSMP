package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.SpawnManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Set;

public class ItemDropListener implements Listener {

    private final UltimateDonutSmp plugin;

    public ItemDropListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // 1. Check if prevention is configured/enabled
        boolean preventSpawn = plugin.getConfigManager().getConfig().getBoolean("PREVENT-ITEM-DROP.SPAWN", true);
        boolean preventAfk = plugin.getConfigManager().getConfig().getBoolean("PREVENT-ITEM-DROP.AFK", true);

        if (!preventSpawn && !preventAfk) {
            return;
        }

        // 2. Check if player has bypass permission
        String bypassPermission = plugin.getConfigManager().getConfig().getString(
                "PREVENT-ITEM-DROP.BYPASS-PERMISSION", "ultimatedonutsmp.preventdrop.bypass");
        if (PermissionUtils.has(player, bypassPermission)) {
            return;
        }

        boolean block = false;

        // 3. Check spawn prevention
        if (preventSpawn && isInSpawnArea(player)) {
            block = true;
        }

        // 4. Check AFK prevention
        if (!block && preventAfk && isInAfkArea(player)) {
            block = true;
        }

        if (block) {
            event.setCancelled(true);
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    player.updateInventory();
                }
            });
            String message = plugin.getConfigManager().getConfig().getString(
                    "PREVENT-ITEM-DROP.MESSAGE", "&c✗ You are not allowed to drop items in spawn or AFK areas!");
            if (message != null && !message.isBlank()) {
                player.sendMessage(ColorUtils.toComponent(message));
            }
        }
    }

    private boolean isInSpawnArea(Player player) {
        // Check if player is in spawn cuboids
        if (plugin.getAFKManager().isInSpawnCuboid(player)) {
            return true;
        }
        // Fallback or additional check for other Spawn areas configured in SpawnManager
        Set<String> spawnCuboids = plugin.getSpawnManager().getAreaCuboidNames(SpawnManager.AreaType.SPAWN);
        if (!spawnCuboids.isEmpty() && plugin.getCuboidManager().isInAnyCuboid(player, spawnCuboids.toArray(String[]::new))) {
            return true;
        }
        return false;
    }

    private boolean isInAfkArea(Player player) {
        // Check if player is in AFK state
        if (plugin.getAFKManager().isAfk(player.getUniqueId())) {
            return true;
        }
        // Check if player is in AFK cuboids
        Set<String> afkCuboids = plugin.getSpawnManager().getAreaCuboidNames(SpawnManager.AreaType.AFK);
        if (!afkCuboids.isEmpty() && plugin.getCuboidManager().isInAnyCuboid(player, afkCuboids.toArray(String[]::new))) {
            return true;
        }
        // Check legacy AFK cuboid name from config
        String legacyAfk = plugin.getConfigManager().getConfig().getString("AFK-SYSTEM.AFK-CUBOID-NAME");
        if (legacyAfk != null && !legacyAfk.isBlank() && plugin.getCuboidManager().isInCuboid(player, legacyAfk.toLowerCase())) {
            return true;
        }
        return false;
    }
}
