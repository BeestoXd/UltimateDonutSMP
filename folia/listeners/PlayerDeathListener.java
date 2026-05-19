package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

        if (plugin.getDuelManager() != null && plugin.getDuelManager().handleDuelDeath(event)) {
            return;
        }

        boolean ffaHandled = plugin.getFfaManager() != null && plugin.getFfaManager().handleDeath(event);
        plugin.getStaffModeManager().handleDeath(event);
        Component deathMsg = buildDeathMessage(event, victim, killer);
        event.deathScreenMessageOverride(deathMsg);
        if (ffaHandled) {
            event.deathMessage(deathMsg);
            return;
        }

        PlayerData victimData = plugin.getPlayerDataManager().get(victim);
        if (victimData != null) {
            victimData.addDeath();
            victimData.resetKillStreak();
        }

        if (killer != null && !killer.equals(victim)) {
            PlayerData killerData = plugin.getPlayerDataManager().get(killer);
            if (killerData != null) {
                killerData.addKill();
                killerData.addKillStreak();
                long shardsPerKill = plugin.getConfigManager().getConfig()
                        .getLong("SETTINGS.SHARDS-PER-KILL", 1);
                plugin.getShardManager().giveShards(killer, shardsPerKill, true);
            }

            if (plugin.getBountyManager().hasBounty(victim.getUniqueId()) && !plugin.getBountyManager()
                    .isExcludedWorld(victim.getWorld().getName())) {
                double amount = plugin.getBountyManager().claimBounty(killer, victim.getUniqueId());
                if (amount > 0) {
                    String msg = plugin.getConfigManager().getMessage("BOUNTY.CLAIM-SUCCESS",
                            "{amount}", NumberUtils.format(amount),
                            "{player}", victim.getName());
                    killer.sendMessage(ColorUtils.toComponent(msg));
                }
            }
        }

        if (plugin.getConfigManager().getDeathMessages()
                .getBoolean("MESSAGES.ENABLED", true)) {
            event.deathMessage(deathMsg);
        }

        plugin.getCombatManager().clearTag(victim.getUniqueId());
        plugin.getRtpZoneManager().clearState(victim.getUniqueId());
    }

    private Component buildDeathMessage(PlayerDeathEvent event, Player victim, Player killer) {
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
            case "BLOCK_EXPLOSION" -> cfg.getString("MESSAGES.BLOCK-EXPLOSION", "{player} ᴡᴀѕ ʙʟᴏᴡɴ ᴜᴘ");
            case "CONTACT" -> cfg.getString("MESSAGES.CONTACT", "{player} ᴡᴀѕ ᴘʀɪᴄᴋᴇᴅ");
            case "DROWNING" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.DROWNING.PVP", "{player} ᴅʀᴏᴡɴᴇᴅ ᴇѕᴄᴀᴘɪɴɢ {killer}")
                    : cfg.getString("MESSAGES.DROWNING.NORMAL", "{player} ᴅʀᴏᴡɴᴇᴅ!");
            case "ENTITY_ATTACK" -> cfg.getString("MESSAGES.ENTITY-ATTACK", "{player} ᴡᴀѕ ѕʟᴀɪɴ ʙʏ {killer}");
            case "FALL" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.FALL.PVP", "{player} ᴡᴀѕ ᴅᴏᴏᴍᴇᴅ ᴛᴏ ꜰᴀʟʟ ʙʏ {killer}")
                    : cfg.getString("MESSAGES.FALL.NORMAL", "{player} ʜɪᴛ ᴛʜᴇ ɢʀᴏᴜɴᴅ ᴛᴏᴏ ʜᴀʀᴅ");
            case "FALLING_BLOCK" -> cfg.getString("MESSAGES.FALLING-BLOCK", "{player} ᴡᴀѕ ѕǫᴜᴀѕʜᴇᴅ");
            case "FIRE" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.FIRE.PVP", "{player} ᴡᴀʟᴋᴇᴅ ɪɴᴛᴏ ꜰɪʀᴇ ꜰɪɢʜᴛɪɴɢ {killer}")
                    : cfg.getString("MESSAGES.FIRE.NORMAL", "{player} ᴡᴇɴᴛ ᴜᴘ ɪɴ ꜰʟᴀᴍᴇѕ");
            case "FIRE_TICK" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.FIRE-TICK.PVP", "{player} ʙᴜʀɴᴇᴅ ᴡʜɪʟᴇ ꜰɪɢʜᴛɪɴɢ {killer}")
                    : cfg.getString("MESSAGES.FIRE-TICK.NORMAL", "{player} ʙᴜʀɴᴇᴅ ᴛᴏ ᴅᴇᴀᴛʜ");
            case "LAVA" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.LAVA.PVP", "{player} ᴛʀɪᴇᴅ ᴛᴏ ѕᴡɪᴍ ɪɴ ʟᴀᴠᴀ ᴇѕᴄᴀᴘɪɴɢ {killer}")
                    : cfg.getString("MESSAGES.LAVA.NORMAL", "{player} ᴛʀɪᴇᴅ ᴛᴏ ѕᴡɪᴍ ɪɴ ʟᴀᴠᴀ");
            case "LIGHTNING" -> cfg.getString("MESSAGES.LIGHTNING", "{player} ɢᴏᴛ ѕᴛʀᴜᴄᴋ ʙʏ ʟɪɢʜᴛɴɪɴɢ");
            case "POISON" -> cfg.getString("MESSAGES.POISON", "{player} ᴡᴀѕ ᴘᴏɪѕᴏɴᴇᴅ");
            case "PROJECTILE" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.PROJECTILE.PVP", "{player} ᴡᴀѕ ѕʜᴏᴛ ʙʏ {killer}")
                    : cfg.getString("MESSAGES.PROJECTILE.NORMAL", "{player} ᴡᴀѕ ѕʜᴏᴛ");
            case "STARVATION" -> cfg.getString("MESSAGES.STARVATION", "{player} ѕᴛᴀʀᴠᴇᴅ ᴛᴏ ᴅᴇᴀᴛʜ");
            case "SUFFOCATION" -> cfg.getString("MESSAGES.SUFFOCATION", "{player} ѕᴜꜰꜰᴏᴄᴀᴛᴇᴅ ɪɴ ᴀ ᴡᴀʟʟ");
            case "SUICIDE" -> cfg.getString("MESSAGES.SUICIDE", "{player} ᴛᴏᴏᴋ ᴛʜᴇɪʀ ᴏᴡɴ ʟɪꜰᴇ");
            case "THORNS" -> cfg.getString("MESSAGES.THORNS", "{player} ᴋɪʟʟᴇᴅ ᴛʜᴇᴍѕᴇʟᴠᴇѕ ᴛʀʏɪɴɢ ᴛᴏ ᴋɪʟʟ ѕᴏᴍᴇᴏɴᴇ");
            case "VOID" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.VOID.PVP", "{player} ᴡᴀѕ ᴋɴᴏᴄᴋᴇᴅ ɪɴᴛᴏ ᴛʜᴇ ᴠᴏɪᴅ ʙʏ {killer}")
                    : cfg.getString("MESSAGES.VOID.NORMAL", "{player} ꜰᴇʟʟ ᴏᴜᴛ ᴏꜰ ᴛʜᴇ ᴡᴏʀʟᴅ");
            case "WITHER" -> cfg.getString("MESSAGES.WITHER", "{player} ᴡɪᴛʜᴇʀᴇᴅ ᴀᴡᴀʏ");
            case "ENTITY_EXPLOSION" -> hasNonPlayerKiller
                    ? cfg.getString("MESSAGES.ENTITY-EXPLOSION.PVP", "{player} ᴡᴀѕ ʙʟᴏᴡɴ ᴜᴘ ʙʏ {killer}")
                    : cfg.getString("MESSAGES.ENTITY-EXPLOSION.NORMAL", "{player} ᴡᴀѕ ʙʟᴏᴡɴ ᴜᴘ");
            default -> cfg.getString("MESSAGES.DEFAULT", "{player} ᴅɪᴇᴅ");
        };

        String msg = template
                .replace("{player}", victim.getName())
                .replace("{killer}", killerName != null ? killerName : "ᴜɴᴋɴᴏᴡɴ");

        return ColorUtils.toComponent(prefix + msg);
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
            return "ᴜɴᴋɴᴏᴡɴ";
        }

        String name = entity.getName();
        return name == null || name.isBlank() ? entity.getType().name() : name;
    }

    private Component buildPlayerKillMessage(Player victim, Player killer) {
        String victimName = victim == null ? "ᴜɴᴋɴᴏᴡɴ" : victim.getName();
        String killerName = killer == null ? "ᴜɴᴋɴᴏᴡɴ" : killer.getName();
        return Component.text(
                "☠ " + victimName + " ᴡᴀѕ ѕʟᴀɪɴ ʙʏ " + killerName,
                NamedTextColor.RED
        );
    }
}
