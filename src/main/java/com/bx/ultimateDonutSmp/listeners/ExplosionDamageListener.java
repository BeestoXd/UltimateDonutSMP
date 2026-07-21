package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class ExplosionDamageListener implements Listener {

    private final UltimateDonutSmp plugin;

    public ExplosionDamageListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        DamageCause cause = event.getCause();
        if (cause != DamageCause.ENTITY_EXPLOSION && cause != DamageCause.BLOCK_EXPLOSION) {
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getConfig();

        if (event.getDamager() instanceof EnderCrystal) {
            boolean enabled = config.getBoolean("END-CRYSTAL.ENABLED", false);
            if (!enabled) {
                return;
            }
            double multiplier = config.getDouble("END-CRYSTAL.DAMAGE", 2.0D);
            event.setDamage(event.getDamage() * multiplier);
            return;
        }

        if (event.getDamager() instanceof TNTPrimed) {
            boolean enabled = config.getBoolean("TNT.ENABLED", true);
            if (!enabled) {
                event.setCancelled(true);
            } else {
                double multiplier = config.getDouble("TNT.DAMAGE", 1.0D);
                event.setDamage(event.getDamage() * multiplier);
            }
            return;
        }

        if (event.getDamager() instanceof ExplosiveMinecart) {
            boolean enabled = config.getBoolean("TNT-MINECART.ENABLED", true);
            if (!enabled) {
                event.setCancelled(true);
            } else {
                double multiplier = config.getDouble("TNT-MINECART.DAMAGE", 1.0D);
                event.setDamage(event.getDamage() * multiplier);
            }
            return;
        }

        // Other entity explosions (e.g. creepers, wither)
        if (cause == DamageCause.ENTITY_EXPLOSION) {
            boolean enabled = config.getBoolean("OTHER-EXPLOSIONS.ENABLED", true);
            if (!enabled) {
                event.setCancelled(true);
            } else {
                double multiplier = config.getDouble("OTHER-EXPLOSIONS.DAMAGE", 1.0D);
                event.setDamage(event.getDamage() * multiplier);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        DamageCause cause = event.getCause();
        if (cause != DamageCause.BLOCK_EXPLOSION) {
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getConfig();

        if (isRespawnAnchorDamage(event)) {
            boolean enabled = config.getBoolean("RESPAWN-ANCHOR.ENABLED", false);
            if (!enabled) {
                return;
            }
            double multiplier = config.getDouble("RESPAWN-ANCHOR.DAMAGE", 2.0D);
            event.setDamage(event.getDamage() * multiplier);
            return;
        }

        // Other block explosions (e.g. beds)
        boolean enabled = config.getBoolean("OTHER-EXPLOSIONS.ENABLED", true);
        if (!enabled) {
            event.setCancelled(true);
        } else {
            double multiplier = config.getDouble("OTHER-EXPLOSIONS.DAMAGE", 1.0D);
            event.setDamage(event.getDamage() * multiplier);
        }
    }

    private boolean isRespawnAnchorDamage(EntityDamageByBlockEvent event) {
        Block damager = event.getDamager();
        if (damager != null && damager.getType() == Material.RESPAWN_ANCHOR) {
            return true;
        }
        BlockState state = event.getDamagerBlockState();
        return state != null && state.getType() == Material.RESPAWN_ANCHOR;
    }
}
