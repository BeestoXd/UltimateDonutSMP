package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class PhantomListener implements Listener {

    private final UltimateDonutSmp plugin;

    public PhantomListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPhantomTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof Phantom)) return;
        if (!(event.getTarget() instanceof Player player)) return;

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data != null && !data.isPhantomEnabled()) {
            event.setCancelled(true);
        }
    }
}
