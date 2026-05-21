package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GodModeListener implements Listener {

    private final UltimateDonutSmp plugin;

    public GodModeListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!plugin.getGodModeManager().isInGodMode(player.getUniqueId())) {
            return;
        }

        event.setDamage(0D);
        if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            refreshAir(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAirChange(EntityAirChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!plugin.getGodModeManager().isInGodMode(player.getUniqueId())) {
            return;
        }

        int maximumAir = player.getMaximumAir();
        if (event.getAmount() < maximumAir) {
            event.setAmount(maximumAir);
        }
        refreshAir(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getGodModeManager().clear(event.getPlayer().getUniqueId());
    }

    private void refreshAir(Player player) {
        int maximumAir = player.getMaximumAir();
        if (player.getRemainingAir() < maximumAir) {
            player.setRemainingAir(maximumAir);
        }
    }
}
