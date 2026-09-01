package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.FeatureManager;
import com.bx.ultimateDonutSmp.managers.ShardManager;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final UltimateDonutSmp plugin;

    public PlayerDeathListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (plugin.getHideManager() != null) {
            plugin.getHideManager().clearNametag(victim.getUniqueId());
        }

        PlayerData victimData = plugin.getPlayerDataManager().get(victim);
        if (victimData != null) {
            if (victimData.isDestroyPearlOnDeath()) {
                for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
                    for (org.bukkit.entity.EnderPearl pearl : world.getEntitiesByClass(org.bukkit.entity.EnderPearl.class)) {
                        if (victim.equals(pearl.getShooter())) {
                            pearl.remove();
                        }
                    }
                }
            } else if (plugin.getEnderPearlManager() != null) {
                plugin.getEnderPearlManager().handlePlayerDeath(victim);
            }
        }

        if (plugin.getDuelManager() != null && plugin.getDuelManager().handleDuelDeath(event)) {
            return;
        }

        boolean ffaHandled = plugin.getFfaManager() != null && plugin.getFfaManager().handleDeath(event);
        plugin.getStaffModeManager().handleDeath(event);
        String deathMsg = buildDeathMessage(event, victim, killer);
        if (ffaHandled) {
            event.setDeathMessage(deathMsg);
            return;
        }

        if (victimData != null) {
            victimData.addDeath();
            victimData.resetKillStreak();
        }

        String locationStr = String.format("%s (%d, %d, %d)",
                victim.getWorld().getName(),
                victim.getLocation().getBlockX(),
                victim.getLocation().getBlockY(),
                victim.getLocation().getBlockZ());
        String cleanDeathMsg = deathMsg != null ? ColorUtils.strip(deathMsg) : "";

        if (killer != null && !killer.equals(victim)) {
            PlayerData killerData = plugin.getPlayerDataManager().get(killer);
            if (killerData != null) {
                killerData.addKill();
                killerData.addKillStreak();
                if (plugin.getFeatureManager().isEnabled(FeatureManager.Feature.SHARDS)) {
                    if (plugin.getShardManager().tryClaimKillReward(killer.getUniqueId(), victim.getUniqueId())) {
                        long multiplier = plugin.getShardManager().getKillMultiplier(killer.getUniqueId());
                        long shardsPerKill = ShardManager.applyMultiplier(
                                plugin.getShardManager().rollKillReward(), multiplier);
                        plugin.getShardManager().giveShards(killer, shardsPerKill, false);
                        plugin.getShardManager().sendKillRewardFeedback(killer, shardsPerKill, multiplier);
                    } else {
                        plugin.getShardManager().sendKillRewardCooldownFeedback(killer, victim.getUniqueId());
                    }
                }
            }

            plugin.getPlayerLogsManager().log(
                    victim.getUniqueId(),
                    victim.getName(),
                    "deaths",
                    "PVP_DEATH",
                    "Killed by " + killer.getName() + " at " + locationStr + (cleanDeathMsg.isEmpty() ? "" : " | " + cleanDeathMsg)
            );

            plugin.getPlayerLogsManager().log(
                    killer.getUniqueId(),
                    killer.getName(),
                    "deaths",
                    "PVP_KILL",
                    "Killed " + victim.getName() + " at " + locationStr + (cleanDeathMsg.isEmpty() ? "" : " | " + cleanDeathMsg)
            );

            if (plugin.getFeatureManager().isEnabled(FeatureManager.Feature.BOUNTY)
                    && plugin.getBountyManager().hasBounty(victim.getUniqueId()) && !plugin.getBountyManager()
                    .isExcludedWorld(victim.getWorld().getName())) {
                double amount = plugin.getBountyManager().claimBounty(killer, victim.getUniqueId());
                if (amount > 0) {
                    String msg = plugin.getConfigManager().getMessage("BOUNTY.CLAIM-SUCCESS",
                            "{amount}", NumberUtils.format(amount),
                            "{amount_formatted}", plugin.getCurrencyManager().formatMoney(amount),
                            "{player}", plugin.getHideManager().publicName(victim));
                    killer.sendMessage(ColorUtils.toComponent(msg));
                }
            }
        } else {
            EntityDamageEvent damageCause = victim.getLastDamageCause();
            String causeName = damageCause != null ? damageCause.getCause().name() : "UNKNOWN";
            plugin.getPlayerLogsManager().log(
                    victim.getUniqueId(),
                    victim.getName(),
                    "deaths",
                    "DEATH",
                    "Died from " + causeName + " at " + locationStr + (cleanDeathMsg.isEmpty() ? "" : " | " + cleanDeathMsg)
            );
        }

        event.setDeathMessage(null);
        if (plugin.getConfigManager().getDeathMessages()
                .getBoolean("MESSAGES.ENABLED", true)) {
            final String finalDeathMsg = deathMsg;
            plugin.getSpigotScheduler().forEachOnlinePlayer(p -> {
                if (shouldReceiveDeathMessage(plugin.getPlayerDataManager().get(p))) {
                    p.sendMessage(ColorUtils.toComponent(finalDeathMsg));
                }
            });
            // Clearing the vanilla message above also takes the death out of the server log, which
            // is where an owner looks when nobody was online to read the chat line.
            org.bukkit.Bukkit.getConsoleSender().sendMessage(finalDeathMsg);
        }

        plugin.getCombatManager().clearTag(victim.getUniqueId());
        plugin.getRtpZoneManager().clearState(victim);
    }

    /**
     * Everyone online reads the death feed unless they muted it, and a player the store has no row
     * for yet is treated as not having muted it. Narrowing the feed to players who follow the
     * victim, the way join and leave messages are narrowed, emptied it completely: a fresh server
     * has nobody following anybody, and the victim does not follow themselves either.
     */
    static boolean shouldReceiveDeathMessage(PlayerData receiverData) {
        return receiverData == null || receiverData.isDeathMessagesEnabled();
    }

    private String buildDeathMessage(PlayerDeathEvent event, Player victim, Player killer) {
        if (killer != null && !killer.equals(victim)) {
            return buildPlayerKillMessage(victim, killer);
        }

        FileConfiguration cfg = plugin.getConfigManager().getDeathMessages();
        String prefix = cfg.getString("MESSAGES.PREFIX", "&c\u2620 ");
        EntityDamageEvent damageCause = event.getEntity().getLastDamageCause();
        String cause = damageCause != null
                ? damageCause.getCause().name()
                : "DEFAULT";
        String killerName = resolveNonPlayerKillerName(damageCause, victim);
        boolean hasNonPlayerKiller = killerName != null;

        String template = switch (cause) {
            case "BLOCK_EXPLOSION" -> cfg.getString("MESSAGES.BLOCK-EXPLOSION", "{player} was blown up");
            case "CONTACT" -> cfg.getString("MESSAGES.CONTACT", "{player} was pricked");
            case "DROWNING" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.DROWNING.PVP", "{player} drowned escaping {killer}")
                    : cfg.getString("MESSAGES.DROWNING.NORMAL", "{player} drowned!");
            case "ENTITY_ATTACK" -> cfg.getString("MESSAGES.ENTITY-ATTACK", "{player} was slain by {killer}");
            case "FALL" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.FALL.PVP", "{player} was doomed to fall by {killer}")
                    : cfg.getString("MESSAGES.FALL.NORMAL", "{player} hit the ground too hard");
            case "FALLING_BLOCK" -> cfg.getString("MESSAGES.FALLING-BLOCK", "{player} was squashed");
            case "FIRE" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.FIRE.PVP", "{player} walked into fire fighting {killer}")
                    : cfg.getString("MESSAGES.FIRE.NORMAL", "{player} went up in flames");
            case "FIRE_TICK" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.FIRE-TICK.PVP", "{player} burned while fighting {killer}")
                    : cfg.getString("MESSAGES.FIRE-TICK.NORMAL", "{player} burned to death");
            case "LAVA" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.LAVA.PVP", "{player} tried to swim in lava escaping {killer}")
                    : cfg.getString("MESSAGES.LAVA.NORMAL", "{player} tried to swim in lava");
            case "LIGHTNING" -> cfg.getString("MESSAGES.LIGHTNING", "{player} got struck by lightning");
            case "POISON" -> cfg.getString("MESSAGES.POISON", "{player} was poisoned");
            case "PROJECTILE" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.PROJECTILE.PVP", "{player} was shot by {killer}")
                    : cfg.getString("MESSAGES.PROJECTILE.NORMAL", "{player} was shot");
            case "STARVATION" -> cfg.getString("MESSAGES.STARVATION", "{player} starved to death");
            case "SUFFOCATION" -> cfg.getString("MESSAGES.SUFFOCATION", "{player} suffocated in a wall");
            case "SUICIDE" -> cfg.getString("MESSAGES.SUICIDE", "{player} took their own life");
            case "THORNS" -> cfg.getString("MESSAGES.THORNS", "{player} killed themselves trying to kill someone");
            case "VOID" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.VOID.PVP", "{player} was knocked into the void by {killer}")
                    : cfg.getString("MESSAGES.VOID.NORMAL", "{player} fell out of the world");
            case "WITHER" -> cfg.getString("MESSAGES.WITHER", "{player} withered away");
            case "ENTITY_EXPLOSION" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.ENTITY-EXPLOSION.PVP", "{player} was blown up by {killer}")
                    : cfg.getString("MESSAGES.ENTITY-EXPLOSION.NORMAL", "{player} was blown up");
            default -> cfg.getString("MESSAGES.DEFAULT", "{player} died");
        };

        String msg = template
                .replace("{player}", plugin.getHideManager().publicName(victim))
                .replace("{killer}", killerName != null ? killerName : "unknown");

        return ColorUtils.colorize(prefix + msg);
    }

    private String resolveNonPlayerKillerName(EntityDamageEvent damageCause, Player victim) {
        if (!(damageCause instanceof EntityDamageByEntityEvent entityDamage)) {
            return null;
        }

        Entity damager = entityDamage.getDamager();
        if (damager instanceof Player) {
            return null;
        }

        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Entity shooter
                    && !(shooter instanceof Player)
                    && !shooter.equals(victim)) {
                return safeEntityName(shooter);
            }
            return null;
        }

        if (!damager.equals(victim)) {
            return safeEntityName(damager);
        }

        return null;
    }

    private String safeEntityName(Entity entity) {
        if (entity == null) {
            return "unknown";
        }

        String name = entity.getName();
        return name == null || name.isBlank() ? entity.getType().name() : name;
    }

    private String buildPlayerKillMessage(Player victim, Player killer) {
        String victimName = victim == null ? "unknown" : plugin.getHideManager().publicName(victim);
        String killerName = killer == null ? "unknown" : plugin.getHideManager().publicName(killer);
        return ColorUtils.colorize("&c\u2620 " + victimName + " was slain by " + killerName);
    }
}
