package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OptimizationManager;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LunarTeammatesTask implements Runnable {

    private static final double MAX_DISTANCE = 96.0;
    private static final double MAX_DISTANCE_SQUARED = MAX_DISTANCE * MAX_DISTANCE;

    private final UltimateDonutSmp plugin;

    public LunarTeammatesTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getOptimizationManager() != null
                && !plugin.getOptimizationManager().shouldRun(OptimizationManager.OptimizedTask.LUNAR_TEAMMATES)) {
            return;
        }
        if (!plugin.getConfigManager().getConfig().getBoolean("LUNAR-CLIENT.TEAM-VIEW.ENABLED", true)) {
            return;
        }

        plugin.getPaperScheduler().forEachOnlinePlayer(this::tickViewer);
    }

    private void tickViewer(Player viewer) {
        PlayerData viewerData = plugin.getPlayerDataManager().get(viewer);
        if (viewerData == null || !viewerData.isLunarTeammatesEnabled()) {
            return;
        }

        Team team = plugin.getTeamManager().getTeam(viewer);
        if (team == null || team.getMemberCount() <= 1) {
            return;
        }

        for (UUID teammateUuid : team.getMemberUuids()) {
            if (teammateUuid.equals(viewer.getUniqueId())) {
                continue;
            }

            Player teammate = Bukkit.getPlayer(teammateUuid);
            if (teammate == null || !teammate.isOnline() || !Bukkit.isOwnedByCurrentRegion(teammate)) {
                continue;
            }
            if (!viewer.getWorld().equals(teammate.getWorld())) {
                continue;
            }
            if (viewer.getLocation().distanceSquared(teammate.getLocation()) > MAX_DISTANCE_SQUARED) {
                continue;
            }

            Location marker = teammate.getLocation().clone().add(0, teammate.getHeight() + 0.4, 0);
            viewer.spawnParticle(Particle.GLOW, marker, 3, 0.20, 0.30, 0.20, 0.0);
            viewer.spawnParticle(Particle.END_ROD, marker.clone().add(0, 0.18, 0), 1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    public static void start(UltimateDonutSmp plugin) {
        long period = Math.max(10L, plugin.getConfigManager().getConfig().getLong("LUNAR-CLIENT.TEAM-VIEW.UPDATE", 20));
        plugin.getPaperScheduler().runGlobalTimer(new LunarTeammatesTask(plugin), period, period);
    }
}
