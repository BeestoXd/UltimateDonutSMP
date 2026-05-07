package com.bx.ultimateDonutSmp.listeners;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

public class PlayerSettingEffectsListener implements Listener {

    private final UltimateDonutSmp plugin;

    public PlayerSettingEffectsListener(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTotemUse(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data != null && !data.isTotemParticlesEnabled()) {
            return;
        }

        playTotemBurst(player);
    }

    private void playTotemBurst(Player player) {
        spawnTotemWave(player, 0.35, 0.20, 18, 90);

        for (int stage = 1; stage <= 3; stage++) {
            int currentStage = stage;
            plugin.getPaperScheduler().runEntityLater(player, () -> {
                if (!player.isOnline()) {
                    return;
                }

                double radius = 0.35 + (currentStage * 0.30);
                double heightOffset = 0.20 + (currentStage * 0.28);
                int points = 18 + (currentStage * 4);
                int burstCount = 90 + (currentStage * 20);
                spawnTotemWave(player, radius, heightOffset, points, burstCount);
            }, stage * 2L);
        }
    }

    private void spawnTotemWave(Player player, double radius, double heightOffset, int points, int burstCount) {
        World world = player.getWorld();
        Location base = player.getLocation().add(0, 1.0 + heightOffset, 0);

        world.spawnParticle(
                Particle.TOTEM_OF_UNDYING,
                base,
                burstCount,
                0.45 + radius,
                0.35,
                0.45 + radius,
                0.04
        );
        world.spawnParticle(
                Particle.GLOW,
                base,
                Math.max(20, burstCount / 3),
                0.35 + radius,
                0.25,
                0.35 + radius,
                0.01
        );

        for (int point = 0; point < points; point++) {
            double angle = (Math.PI * 2 * point) / points;
            double x = base.getX() + (Math.cos(angle) * radius);
            double z = base.getZ() + (Math.sin(angle) * radius);
            double y = base.getY() + (Math.sin(angle * 2) * 0.10);

            world.spawnParticle(Particle.TOTEM_OF_UNDYING, x, y, z, 1, 0, 0, 0, 0);
            if (point % 2 == 0) {
                world.spawnParticle(Particle.GLOW, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }
}
