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
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.Set;
import java.util.UUID;

public class DuelListener implements Listener {

    private static final Set<String> ALLOWED_DUEL_COMMANDS = Set.of("/duel", "/draw", "/leave", "/queue");

    private final UltimateDonutSmp plugin;

    public DuelListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = resolveAttacker(event);
        boolean attackerInDuel = attacker != null && plugin.getDuelManager().isInDuel(attacker.getUniqueId());
        boolean attackerTransitioning = attacker != null && plugin.getDuelManager().isTransitioning(attacker.getUniqueId());

        if (event.getEntity() instanceof Player victim) {
            boolean victimInDuel = plugin.getDuelManager().isInDuel(victim.getUniqueId());
            boolean victimTransitioning = plugin.getDuelManager().isTransitioning(victim.getUniqueId());
            if (!attackerInDuel && !victimInDuel && !attackerTransitioning && !victimTransitioning) {
                return;
            }

            if (attacker != null
                    && plugin.getDuelManager().areOpponents(attacker.getUniqueId(), victim.getUniqueId())
                    && plugin.getDuelManager().isMatchActive(attacker.getUniqueId())) {
                if (plugin.getDuelManager().shouldHandleAsCustomLethalPvP(attacker, victim, event.getFinalDamage())) {
                    event.setCancelled(true);
                    plugin.getDuelManager().handleLethalPvPHit(attacker, victim);
                    return;
                }
                return;
            }

            event.setCancelled(true);
            return;
        }

        if (attackerInDuel || attackerTransitioning) {
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
                && plugin.getDuelManager().hasArenaSetting(uuid, com.bx.ultimateDonutSmp.managers.DuelManager.ArenaSetting.NO_FALL_DAMAGE)) {
            event.setCancelled(true);
            return;
        }

        if (plugin.getDuelManager().isInCountdown(uuid)
                || plugin.getDuelManager().isTransitioning(uuid)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (!plugin.getDuelManager().hasArenaSetting(uuid, com.bx.ultimateDonutSmp.managers.DuelManager.ArenaSetting.NO_HUNGER)) {
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
    public void onMove(PlayerMoveEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!plugin.getDuelManager().isInCountdown(uuid)) {
            return;
        }

        if (event.getTo() == null) {
            return;
        }

        if (movedPosition(event.getFrom(), event.getTo())) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getDuelManager().isTransitioning(uuid)
                || (plugin.getDuelManager().isInDuel(uuid) && !plugin.getDuelManager().canModifyArena(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getDuelManager().isTransitioning(uuid)
                || (plugin.getDuelManager().isInDuel(uuid) && !plugin.getDuelManager().canModifyArena(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getDuelManager().isTransitioning(uuid)
                || (plugin.getDuelManager().isInDuel(uuid) && !plugin.getDuelManager().canModifyArena(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getDuelManager().isTransitioning(uuid)
                || (plugin.getDuelManager().isInDuel(uuid) && !plugin.getDuelManager().canModifyArena(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getDuelManager().isInDuel(uuid) || plugin.getDuelManager().isTransitioning(uuid)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!plugin.getDuelManager().isInDuel(uuid) && !plugin.getDuelManager().isTransitioning(uuid)) {
            return;
        }

        String raw = event.getMessage().trim().toLowerCase();
        for (String allowed : ALLOWED_DUEL_COMMANDS) {
            if (raw.equals(allowed) || raw.startsWith(allowed + " ")) {
                return;
            }
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ColorUtils.toComponent("&cYou cannot use that command during a duel."));
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (plugin.getDuelManager() != null) {
            plugin.getDuelManager().refreshArenaAvailability();
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (plugin.getDuelManager() != null) {
            plugin.getDuelManager().refreshArenaAvailability();
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

    private boolean movedPosition(org.bukkit.Location from, org.bukkit.Location to) {
        return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
    }
}
