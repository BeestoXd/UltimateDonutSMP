package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.Set;
import java.util.UUID;

public class FfaListener implements Listener {

    private static final Set<String> ALLOWED_FFA_COMMANDS = Set.of("/ffa", "/leave", "/ffastats");

    private final UltimateDonutSmp plugin;

    public FfaListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = resolveAttacker(event);

        if (event.getEntity() instanceof Player victim) {
            UUID victimUuid = victim.getUniqueId();
            if (attacker != null
                    && plugin.getFfaManager().isActiveOpponentPair(attacker.getUniqueId(), victimUuid)) {
                event.setCancelled(false);
                plugin.getFfaManager().refreshCombatTag(attacker, victim);
                return;
            }

            if (attacker != null && plugin.getFfaManager().tryStartCombatPair(attacker, victim)) {
                event.setCancelled(false);
                plugin.getFfaManager().refreshCombatTag(attacker, victim);
                return;
            }

            boolean attackerProtected = attacker != null
                    && (plugin.getFfaManager().isInSession(attacker.getUniqueId())
                    || plugin.getFfaManager().isCombatLocked(attacker.getUniqueId()));
            boolean victimProtected = plugin.getFfaManager().isInSession(victimUuid)
                    || plugin.getFfaManager().isCombatLocked(victimUuid);
            if (attackerProtected || victimProtected) {
                event.setCancelled(true);
            }
            return;
        }

        boolean attackerProtected = attacker != null
                && (plugin.getFfaManager().isInSession(attacker.getUniqueId())
                || plugin.getFfaManager().isCombatLocked(attacker.getUniqueId()));
        if (attackerProtected) {
            event.setCancelled(true);
            return;
        }

        if (attacker != null
                && plugin.getFfaManager().isInMatch(attacker.getUniqueId())
                && !plugin.getFfaManager().canModifyArena(attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && plugin.getFfaManager().hasArenaSetting(uuid, com.bx.ultimateDonutSmp.managers.FfaManager.ArenaSetting.NO_FALL_DAMAGE)) {
            event.setCancelled(true);
            return;
        }

        if (plugin.getFfaManager().isInQueue(uuid) || plugin.getFfaManager().isTransitioning(uuid)) {
            event.setCancelled(true);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent) {
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (!plugin.getFfaManager().hasArenaSetting(uuid, com.bx.ultimateDonutSmp.managers.FfaManager.ArenaSetting.NO_HUNGER)) {
            return;
        }

        event.setCancelled(true);
        if (player.getFoodLevel() < 20) {
            player.setFoodLevel(20);
        }
        if (player.getSaturation() < 20F) {
            player.setSaturation(20F);
        }
        player.setExhaustion(0F);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getFfaManager().isInQueue(uuid)
                || plugin.getFfaManager().isTransitioning(uuid)
                || (plugin.getFfaManager().isInMatch(uuid) && !plugin.getFfaManager().canModifyArena(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getFfaManager().isInQueue(uuid)
                || plugin.getFfaManager().isTransitioning(uuid)
                || (plugin.getFfaManager().isInMatch(uuid) && !plugin.getFfaManager().canModifyArena(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getFfaManager().isInQueue(uuid)
                || plugin.getFfaManager().isTransitioning(uuid)
                || (plugin.getFfaManager().isInMatch(uuid) && !plugin.getFfaManager().canModifyArena(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getFfaManager().isInQueue(uuid)
                || plugin.getFfaManager().isTransitioning(uuid)
                || (plugin.getFfaManager().isInMatch(uuid) && !plugin.getFfaManager().canModifyArena(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getFfaManager().isInQueue(uuid)
                || plugin.getFfaManager().isInMatch(uuid)
                || plugin.getFfaManager().isTransitioning(uuid)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (plugin.getFfaManager().isInQueue(uuid)
                || plugin.getFfaManager().isInMatch(uuid)
                || plugin.getFfaManager().isTransitioning(uuid)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getFfaManager().shouldBlockCommands()) {
            return;
        }

        if (event.getPlayer().hasPermission("ultimatedonutsmp.admin.ffa")) {
            return;
        }

        UUID uuid = event.getPlayer().getUniqueId();
        if (!plugin.getFfaManager().isInQueue(uuid)
                && !plugin.getFfaManager().isInMatch(uuid)
                && !plugin.getFfaManager().isTransitioning(uuid)) {
            return;
        }

        String raw = event.getMessage().trim().toLowerCase();
        for (String allowed : ALLOWED_FFA_COMMANDS) {
            if (raw.equals(allowed) || raw.startsWith(allowed + " ")) {
                return;
            }
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ColorUtils.toComponent("&cYou cannot use that command during FFA."));
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (plugin.getFfaManager() != null) {
            plugin.getFfaManager().refreshArenaAvailability();
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (plugin.getFfaManager() != null) {
            plugin.getFfaManager().refreshArenaAvailability();
        }
    }

    private Player resolveAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }

        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }

        return null;
    }

}
