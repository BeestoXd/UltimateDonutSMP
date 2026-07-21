package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerMoveListener implements Listener {

    private final UltimateDonutSmp plugin;

    public PlayerMoveListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Only care about block-level movement
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
            return;
        }

        // Check flight restrictions for player-fly
        if (player.getAllowFlight()
                && player.getGameMode() != org.bukkit.GameMode.CREATIVE
                && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {

            if (plugin.getConfigManager().getConfig().getBoolean("FLY-SYSTEM.AUTO-DISABLE-OUTSIDE", true)) {
                if (!PermissionUtils.has(player, "ultimatedonutsmp.staff.fly")) {
                    String playerFlyPerm = plugin.getConfigManager().getConfig()
                            .getString("FLY-SYSTEM.PLAYER-FLY-PERMISSION", "ultimatedonutsmp.player.fly");

                    if (PermissionUtils.has(player, playerFlyPerm)) {
                        boolean inSpawn = plugin.getAFKManager().isInSpawnCuboid(player);
                        boolean inCuboid = false;
                        for (String name : plugin.getCuboidManager().getCuboidNames()) {
                            if (plugin.getCuboidManager().isInCuboid(player, name)) {
                                inCuboid = true;
                                break;
                            }
                        }

                        boolean inCombat = plugin.getCombatManager() != null && plugin.getCombatManager().isInCombat(player.getUniqueId());

                        if ((!inSpawn && !inCuboid) || inCombat) {
                            player.setFlying(false);
                            player.setAllowFlight(false);
                            String msg = plugin.getConfigManager().getMessageOrDefault(
                                    "FLY.PLAYER_DISABLED",
                                    "&c✗ &7Flight deactivated because you left the allowed area or entered combat."
                            );
                            player.sendMessage(ColorUtils.toComponent(msg));
                        }
                    } else {
                        // Player doesn't have the player flight permission, disable flight
                        player.setFlying(false);
                        player.setAllowFlight(false);
                    }
                }
            }
        }

        // Reset AFK timer
        plugin.getAFKManager().recordMovement(player.getUniqueId());
        plugin.getShardManager().recordMovement(
                player.getUniqueId(),
                event.getFrom(),
                event.getTo(),
                event instanceof PlayerTeleportEvent
        );

        // Check pending teleport
        if (plugin.getTeleportManager().hasPending(player.getUniqueId())) {
            plugin.getTeleportManager().checkMovement(player);
        }
    }
}
