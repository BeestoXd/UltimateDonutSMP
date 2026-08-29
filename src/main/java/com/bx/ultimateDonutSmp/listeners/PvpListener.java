package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.PvpManager;
import com.bx.ultimateDonutSmp.models.PvpSession;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Set;

/**
 * Everything the ranked arena has to react to in the world: deaths, respawns, the boundary wand,
 * the command block list, and the two protections that keep a freshly spawned player alive long
 * enough to actually fight.
 */
public class PvpListener implements Listener {

    private static final Set<String> ALWAYS_ALLOWED = Set.of("pvp", "arena");

    private final UltimateDonutSmp plugin;

    public PvpListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    private PvpManager pvp() {
        return plugin.getPvpManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (pvp() == null || !pvp().isInArena(victim.getUniqueId())) {
            return;
        }

        if (!plugin.getConfigManager().getPvp().getBoolean("SETTINGS.DROP_KIT_ON_DEATH", false)) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            event.setKeepLevel(true);
        }

        pvp().handleDeath(victim, victim.getKiller());

        // Sitting on the death screen would stall the respawn countdown, so the arena takes the
        // choice away and puts the player straight into spectator for it.
        plugin.getSpigotScheduler().runEntityLater(victim, () -> {
            if (victim.isOnline() && victim.isDead()) {
                victim.spigot().respawn();
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (pvp() == null || !pvp().isInArena(player.getUniqueId())) {
            return;
        }

        Location spawn = pvp().getSpawn();
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }

        plugin.getSpigotScheduler().runEntityLater(player, () -> {
            if (!player.isOnline() || !pvp().isInArena(player.getUniqueId())) {
                return;
            }
            player.setGameMode(GameMode.SPECTATOR);
            if (spawn != null) {
                plugin.getSpigotScheduler().teleport(player, spawn);
            }
        }, 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (pvp() != null) {
            pvp().handleQuit(event.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (pvp() != null) {
            pvp().handleJoin(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (pvp() == null || !pvp().isInArena(player.getUniqueId()) || !pvp().shouldBlockCommands()) {
            return;
        }
        if (PermissionUtils.has(player, "ultimatedonutsmp.admin.pvp")) {
            return;
        }

        String typed = event.getMessage().trim().toLowerCase();
        String label = typed.startsWith("/") ? typed.substring(1) : typed;
        int space = label.indexOf(' ');
        if (space >= 0) {
            label = label.substring(0, space);
        }
        if (ALWAYS_ALLOWED.contains(label)) {
            return;
        }

        if (pvp().isBlockedCommand(event.getMessage())) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.toComponent(
                    pvp().message("BLOCKED_COMMAND", "&cYou cannot use that command inside the PvP arena.")));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnderChestOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (pvp() == null || !pvp().isInArena(player.getUniqueId()) || !pvp().shouldBlockEnderChestBlock()) {
            return;
        }
        if (PermissionUtils.has(player, "ultimatedonutsmp.admin.pvp")) {
            return;
        }

        if (event.getInventory().getType() == InventoryType.ENDER_CHEST) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.toComponent(
                    pvp().message("BLOCKED_COMMAND", "&cYou cannot use that command inside the PvP arena.")));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (pvp() != null && pvp().isInArena(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || pvp() == null) {
            return;
        }

        PvpSession session = pvp().getSession(player.getUniqueId());
        if (session == null) {
            return;
        }

        // A player who owes the arena a kit choice is standing there empty handed, and one inside
        // the spawn window has not had a chance to move yet. Neither is a fair fight.
        if (session.isAwaitingKit() || isSpawnProtected(session)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (pvp() == null || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player attacker = resolveAttacker(event);
        if (attacker == null) {
            return;
        }

        PvpSession attackerSession = pvp().getSession(attacker.getUniqueId());
        if (attackerSession != null && (attackerSession.isAwaitingKit() || isSpawnProtected(attackerSession))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWandUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (pvp() == null || event.getClickedBlock() == null || !pvp().isWand(event.getItem())) {
            return;
        }
        if (!PermissionUtils.has(player, "ultimatedonutsmp.admin.pvp")) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);
        int corner = action == Action.LEFT_CLICK_BLOCK ? 1 : 2;
        Location location = event.getClickedBlock().getLocation();
        pvp().setWandSelection(player.getUniqueId(), corner, location);
        player.sendMessage(ColorUtils.toComponent("&aCorner &f" + corner + " &aset to &f"
                + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnderChestBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (pvp() == null
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getClickedBlock() == null
                || event.getClickedBlock().getType() != Material.ENDER_CHEST) {
            return;
        }
        if (!pvp().isInArena(player.getUniqueId()) || !pvp().shouldBlockEnderChestBlock()) {
            return;
        }
        if (PermissionUtils.has(player, "ultimatedonutsmp.admin.pvp")) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(ColorUtils.toComponent(
                pvp().message("BLOCKED_COMMAND", "&cYou cannot use that command inside the PvP arena.")));
    }

    private boolean isSpawnProtected(PvpSession session) {
        return session.isSpawnProtected(
                System.currentTimeMillis(),
                plugin.getConfigManager().getPvp().getInt("SETTINGS.SPAWN_PROTECTION_SECONDS", 3)
        );
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
