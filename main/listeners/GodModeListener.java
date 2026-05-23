package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class GodModeListener implements Listener {

    private static final double BASE_ENTITY_KNOCKBACK = 0.4D;
    private static final double BASE_PROJECTILE_KNOCKBACK = 0.35D;
    private static final double EXTRA_KNOCKBACK_PER_LEVEL = 0.2D;
    private static final double SPRINT_KNOCKBACK_BONUS = 0.15D;
    private static final double VERTICAL_KNOCKBACK = 0.36D;
    private static final double MIN_DIRECTION_LENGTH = 0.0001D;

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

        if (event.isCancelled()) {
            return;
        }

        double originalDamage = event.getDamage();
        event.setDamage(0D);

        if (originalDamage > 0D) {
            playDamageFeedback(player, event);
            applyKnockback(player, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getGodModeManager().clear(event.getPlayer().getUniqueId());
    }

    private void playDamageFeedback(Player player, EntityDamageEvent event) {
        player.playHurtAnimation(resolveHurtYaw(player, event));

        Sound hurtSound = player.getHurtSound();
        if (hurtSound != null) {
            player.getWorld().playSound(player.getLocation(), hurtSound, SoundCategory.PLAYERS, 1F, 1F);
        }
    }

    private void applyKnockback(Player player, EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent damageByEntity)) {
            return;
        }

        Entity damager = damageByEntity.getDamager();
        Vector direction = resolveKnockbackDirection(player, damager);
        if (direction == null) {
            return;
        }

        double horizontalStrength = damager instanceof Projectile
                ? BASE_PROJECTILE_KNOCKBACK
                : BASE_ENTITY_KNOCKBACK;
        horizontalStrength += resolveKnockbackLevel(damager) * EXTRA_KNOCKBACK_PER_LEVEL;

        if (damager instanceof Player attacker && attacker.isSprinting()) {
            horizontalStrength += SPRINT_KNOCKBACK_BONUS;
        }

        Vector velocity = direction.multiply(horizontalStrength);
        velocity.setY(Math.max(player.getVelocity().getY(), VERTICAL_KNOCKBACK));
        player.setVelocity(velocity);
    }

    private Vector resolveKnockbackDirection(Player player, Entity damager) {
        if (damager instanceof Projectile projectile) {
            Vector projectileVelocity = projectile.getVelocity();
            Vector projectileDirection = new Vector(projectileVelocity.getX(), 0D, projectileVelocity.getZ());
            if (projectileDirection.lengthSquared() > MIN_DIRECTION_LENGTH) {
                return projectileDirection.normalize();
            }

            if (projectile.getShooter() instanceof Entity shooter) {
                return directionFrom(player, shooter);
            }
        }

        return directionFrom(player, damager);
    }

    private Vector directionFrom(Player player, Entity source) {
        Vector direction = player.getLocation().toVector().subtract(source.getLocation().toVector());
        direction.setY(0D);
        if (direction.lengthSquared() <= MIN_DIRECTION_LENGTH) {
            direction = player.getLocation().getDirection().setY(0D).multiply(-1D);
        }

        return direction.lengthSquared() > MIN_DIRECTION_LENGTH ? direction.normalize() : null;
    }

    private int resolveKnockbackLevel(Entity damager) {
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity shooter) {
            return getEnchantmentLevel(shooter, Enchantment.PUNCH);
        }

        if (damager instanceof LivingEntity livingEntity) {
            return getEnchantmentLevel(livingEntity, Enchantment.KNOCKBACK);
        }

        return 0;
    }

    private int getEnchantmentLevel(LivingEntity entity, Enchantment enchantment) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return 0;
        }

        ItemStack item = equipment.getItemInMainHand();
        return item == null ? 0 : item.getEnchantmentLevel(enchantment);
    }

    private float resolveHurtYaw(Player player, EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent damageByEntity)) {
            return player.getLocation().getYaw();
        }

        Entity damager = damageByEntity.getDamager();
        double x = damager.getLocation().getX() - player.getLocation().getX();
        double z = damager.getLocation().getZ() - player.getLocation().getZ();
        return (float) Math.toDegrees(Math.atan2(-x, z));
    }
}
