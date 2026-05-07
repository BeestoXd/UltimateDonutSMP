package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.Team;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {

    private final UltimateDonutSmp plugin;

    public CombatListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getCombatManager().isEnabled()) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;

        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj) {
            if (proj.getShooter() instanceof Player p) attacker = p;
        } else if (event.getDamager() instanceof EnderCrystal) {
            // Crystal hit - tag victim only
        }

        if (plugin.getDuelManager() != null && plugin.getDuelManager().shouldBypassGlobalCombat(attacker, victim)) {
            return;
        }
        if (plugin.getFfaManager() != null && plugin.getFfaManager().shouldBypassGlobalCombat(attacker, victim)) {
            return;
        }

        if (plugin.getCombatManager().isExcludedWorld(victim.getWorld().getName())) return;

        if (attacker != null && !attacker.getUniqueId().equals(victim.getUniqueId())
                && plugin.getTeamManager().areTeammates(attacker.getUniqueId(), victim.getUniqueId())) {
            Team team = plugin.getTeamManager().getTeam(attacker);
            if (team != null && !team.isFriendlyFireEnabled()) {
                event.setCancelled(true);
                return;
            }
        }

        plugin.getCombatManager().tag(victim);
        if (attacker != null) plugin.getCombatManager().tag(attacker);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getCombatManager().isEnabled()) return;
        Player player = event.getPlayer();
        if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) return;
        if (plugin.getCombatManager().isExcludedWorld(player.getWorld().getName())) return;

        String cmd = event.getMessage().split(" ")[0].toLowerCase();
        if (plugin.getCombatManager().isBlockedCommand(cmd)) {
            event.setCancelled(true);
            String msg = plugin.getConfigManager().getConfig()
                    .getString("COMBAT-MANAGER.BLOCK-MESSAGE",
                            "&cYou can't use this command in your current status.");
            player.sendMessage(ColorUtils.toComponent(msg));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Kill player if they disconnect during combat (optional, can be configured)
        Player player = event.getPlayer();
        if (plugin.getCombatManager().isInCombat(player.getUniqueId())) {
            plugin.getCombatManager().clearTag(player.getUniqueId());
            // Optionally kill: player.setHealth(0);
        }
    }
}
