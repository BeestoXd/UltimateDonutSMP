package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {

    private final UltimateDonutSmp plugin;
    /** UUID → expiry time in millis */
    private final Map<UUID, Long> combatMap = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    public CombatManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void tag(Player player) {
        if (!isEnabled()) return;
        long cooldownMillis = getCooldownSeconds() * 1000L;
        combatMap.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMillis);
        startCountdown(player);
    }

    private void startCountdown(Player player) {
        UUID uuid = player.getUniqueId();
        ScheduledTask old = tasks.remove(uuid);
        if (old != null) old.cancel();

        ScheduledTask task = plugin.getPaperScheduler().runEntityTimer(player, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !isInCombat(uuid)) {
                ScheduledTask t = tasks.remove(uuid);
                if (t != null) t.cancel();
                return;
            }
            long remaining = getRemainingSeconds(uuid);
            String format = plugin.getConfigManager().getConfig()
                    .getString("COMBAT-MANAGER.ACTION-BAR", "&fCombat: &b${time}s")
                    .replace("${time}", String.valueOf(remaining));
            PlayerSettingUtils.sendActionBar(plugin, p, ColorUtils.toComponent(format));
        }, 0L, 20L);
        if (task != null) {
            tasks.put(uuid, task);
        }
    }

    public void clearTag(UUID uuid) {
        combatMap.remove(uuid);
        ScheduledTask task = tasks.remove(uuid);
        if (task != null) task.cancel();
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) PlayerSettingUtils.clearActionBar(p);
    }

    public boolean isInCombat(UUID uuid) {
        Long expiry = combatMap.get(uuid);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            clearTag(uuid);
            return false;
        }
        return true;
    }

    public long getRemainingSeconds(UUID uuid) {
        Long expiry = combatMap.get(uuid);
        if (expiry == null) return 0;
        return Math.max(0, (expiry - System.currentTimeMillis()) / 1000L);
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getConfig()
                .getBoolean("COMBAT-MANAGER.ENABLED", true);
    }

    public int getCooldownSeconds() {
        return plugin.getConfigManager().getConfig()
                .getInt("COMBAT-MANAGER.COOLDOWN", 16);
    }

    public boolean isBlockedCommand(String command) {
        for (String blocked : plugin.getConfigManager().getConfig()
                .getStringList("COMBAT-MANAGER.BLOCK-COMMANDS")) {
            if (blocked.equalsIgnoreCase(command) ||
                command.toLowerCase().startsWith(blocked.toLowerCase() + " ")) {
                return true;
            }
        }
        return false;
    }

    public boolean isExcludedWorld(String worldName) {
        return plugin.getConfigManager().getConfig()
                .getStringList("COMBAT-MANAGER.EXCLUDED-WORLDS")
                .contains(worldName);
    }

    public boolean isAntiStasisEnabled() {
        return plugin.getConfigManager().getConfig()
                .getBoolean("COMBAT-MANAGER.ANTI-STASIS-CHAMBER.ENABLED", true);
    }

    public int getMaxStasisDistance() {
        return plugin.getConfigManager().getConfig()
                .getInt("COMBAT-MANAGER.ANTI-STASIS-CHAMBER.MAX-DISTANCE", 500);
    }
}
