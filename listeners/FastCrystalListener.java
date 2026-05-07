package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.FastCrystalManager;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class FastCrystalListener implements Listener {

    private final UltimateDonutSmp plugin;

    public FastCrystalListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCrystalUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;
        if (event.getItem() == null || event.getItem().getType() != Material.END_CRYSTAL) return;

        FastCrystalManager fastCrystalManager = plugin.getFastCrystalManager();
        Player player = event.getPlayer();

        if (!fastCrystalManager.isEnabledFor(player)) {
            scheduleCooldownApply(player);
            return;
        }

        if (!fastCrystalManager.tryBeginPlace(player, event.getClickedBlock())) {
            return;
        }

        scheduleCooldownApply(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCrystalBreak(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal)) return;

        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) return;

        FastCrystalManager fastCrystalManager = plugin.getFastCrystalManager();
        if (!fastCrystalManager.isEnabledFor(attacker)) return;
        if (!fastCrystalManager.shouldClearCooldownAfterHit()) return;

        plugin.getPaperScheduler().runEntity(attacker, () -> fastCrystalManager.clearCrystalCooldown(attacker));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getFastCrystalManager().clearState(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getFastCrystalManager().clearState(event.getPlayer().getUniqueId());
    }

    private void scheduleCooldownApply(Player player) {
        plugin.getPaperScheduler().runEntity(player, () -> {
            if (player.isOnline()) {
                plugin.getFastCrystalManager().applyCrystalCooldown(player);
            }
        });
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }
}
