package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.DuelManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class DuelListener implements Listener {

    private static final String DUEL_CRYSTAL_OWNER_KEY = "duel_crystal_owner";

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

            if (attacker != null
                    && event.getDamager() instanceof EnderCrystal
                    && attacker.getUniqueId().equals(victim.getUniqueId())
                    && victimInDuel
                    && !victimTransitioning
                    && plugin.getDuelManager().isMatchActive(victim.getUniqueId())) {
                return;
            }

            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof EnderCrystal && attackerInDuel) {
            if (!attackerTransitioning && plugin.getDuelManager().canModifyArena(attacker)) {
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
        plugin.getDuelManager().handleArenaBorderMove(event);

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
    public void onCrystalPlace(EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal crystal)) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (plugin.getDuelManager().isTransitioning(uuid)
                || plugin.getDuelManager().isInCountdown(uuid)
                || (plugin.getDuelManager().isInDuel(uuid) && !plugin.getDuelManager().canModifyArena(player))) {
            event.setCancelled(true);
            return;
        }

        if (plugin.getDuelManager().isInDuel(uuid)) {
            crystal.getPersistentDataContainer().set(
                    plugin.getKey(DUEL_CRYSTAL_OWNER_KEY),
                    PersistentDataType.STRING,
                    uuid.toString()
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (shouldCancelArenaModify(event.getPlayer(), DuelManager.ArenaSetting.ALLOW_BLOCK_PLACE)) {
            event.setCancelled(true);
            return;
        }
        plugin.getDuelManager().recordGeneratedBlockChange(event.getPlayer(), event.getBlockPlaced());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (shouldCancelArenaModify(event.getPlayer(), DuelManager.ArenaSetting.ALLOW_BLOCK_BREAK)) {
            event.setCancelled(true);
            return;
        }
        plugin.getDuelManager().recordGeneratedBlockChange(event.getPlayer(), event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (shouldCancelArenaModify(event.getPlayer(), DuelManager.ArenaSetting.ALLOW_BUCKET_USE)) {
            event.setCancelled(true);
            return;
        }
        if (event.getBlockClicked() != null) {
            Block target = event.getBlockClicked().getRelative(event.getBlockFace());
            plugin.getDuelManager().recordGeneratedBlockChange(event.getPlayer(), target);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (shouldCancelArenaModify(event.getPlayer(), DuelManager.ArenaSetting.ALLOW_BUCKET_USE)) {
            event.setCancelled(true);
            return;
        }
        if (event.getBlockClicked() != null) {
            plugin.getDuelManager().recordGeneratedBlockChange(event.getPlayer(), event.getBlockClicked());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            plugin.getDuelManager().recordGeneratedBlockChange(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            plugin.getDuelManager().recordGeneratedBlockChange(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        plugin.getDuelManager().recordGeneratedBlockChange(event.getToBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.getDuelManager().isTransitioning(uuid)
                || plugin.getDuelManager().isInCountdown(uuid)
                || (plugin.getDuelManager().isInDuel(uuid)
                && !plugin.getDuelManager().hasArenaSetting(uuid, DuelManager.ArenaSetting.ALLOW_ITEM_DROP))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!plugin.getDuelManager().isInDuel(uuid) && !plugin.getDuelManager().isTransitioning(uuid)) {
            return;
        }

        if (plugin.getDuelManager().isCommandAllowedDuringMatch(event.getMessage())) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ColorUtils.toComponent(plugin.getDuelManager().getCommandBlockedMessage()));
        if (plugin.getDuelManager() != null) {
            return;
        }
        event.getPlayer().sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴜѕᴇ ᴛʜᴀᴛ ᴄᴏᴍᴍᴀɴᴅ ᴅᴜʀɪɴɢ ᴀ ᴅᴜᴇʟ."));
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

        if (event.getDamager() instanceof EnderCrystal crystal) {
            return resolveCrystalOwner(crystal);
        }

        return null;
    }

    private Player resolveCrystalOwner(EnderCrystal crystal) {
        String raw = crystal.getPersistentDataContainer().get(
                plugin.getKey(DUEL_CRYSTAL_OWNER_KEY),
                PersistentDataType.STRING
        );
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return plugin.getServer().getPlayer(UUID.fromString(raw));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean shouldCancelArenaModify(Player player, DuelManager.ArenaSetting setting) {
        UUID uuid = player.getUniqueId();
        return plugin.getDuelManager().isTransitioning(uuid)
                || (plugin.getDuelManager().isInDuel(uuid)
                && (!plugin.getDuelManager().canModifyArena(player)
                || !plugin.getDuelManager().hasArenaSetting(uuid, setting)));
    }

    private boolean movedPosition(org.bukkit.Location from, org.bukkit.Location to) {
        return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
    }
}
