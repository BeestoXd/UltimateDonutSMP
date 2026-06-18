package com.bx.ultimateDonutSmp.tasks;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.ShardManager;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ShardCuboidTask implements Runnable {

    private final UltimateDonutSmp plugin;

    public ShardCuboidTask(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ShardManager shardManager = plugin.getShardManager();
        if (shardManager == null || !shardManager.isEnabled()) {
            return;
        }

        plugin.getSpigotScheduler().forEachOnlinePlayer(player -> tickPlayer(player, shardManager));
    }

    private void tickPlayer(Player player, ShardManager shardManager) {
        UUID uuid = player.getUniqueId();
        int movedBlocks = shardManager.drainPendingMovement(uuid);
        ShardManager.ShardCuboidConfig current = shardManager.findMatchingShardCuboid(player);
        String previousId = shardManager.getLastMatchedCuboid(uuid);

        if (current == null) {
            if (previousId != null) {
                ShardManager.ShardCuboidConfig previous = shardManager.getShardCuboidConfig(previousId);
                shardManager.sendCuboidLeaveFeedback(player, previous);
            }
            handleLeave(uuid, previousId, shardManager);
            shardManager.setLastMatchedCuboid(uuid, null);
            shardManager.setHudState(uuid, new ShardManager.ShardCuboidHudState(
                    "None",
                    "Outside",
                    "-",
                    0,
                    false
            ));
            return;
        }

        if (previousId != null && !previousId.equalsIgnoreCase(current.id())) {
            handleLeave(uuid, previousId, shardManager);
        }

        shardManager.setLastMatchedCuboid(uuid, current.id());
        ShardManager.ShardCuboidProgress progress = shardManager.getOrCreateProgress(uuid, current);
        shardManager.addMovementToProgress(uuid, current, movedBlocks);

        if (current.isWorldExcluded(player.getWorld().getName())) {
            PlayerSettingUtils.sendActionBar(plugin, player, current.excludedWorldMessage());
            shardManager.setHudState(uuid, new ShardManager.ShardCuboidHudState(
                    current.cuboidName(),
                    "EXCLUDED_WORLD",
                    "Disabled",
                    progress.getRemainingSeconds(),
                    true
            ));
            return;
        }

        if (plugin.getAFKManager() != null && plugin.getAFKManager().isAfk(uuid)) {
            PlayerSettingUtils.sendActionBar(plugin, player,
                    shardManager.replaceCommonPlaceholders(current.afkPausedMessage(), current, progress.getRemainingSeconds(), 0, 1, progress));
            shardManager.setHudState(uuid, new ShardManager.ShardCuboidHudState(
                    current.cuboidName(),
                    "AFK",
                    "AFK",
                    progress.getRemainingSeconds(),
                    true
            ));
            return;
        }

        if (progress.getRemainingSeconds() <= 1 && progress.getMovementThisCycle() < current.minimumMovementBlocks()) {
            PlayerSettingUtils.sendActionBar(plugin, player,
                    shardManager.replaceCommonPlaceholders(current.pausedMessage(), current, progress.getRemainingSeconds(), 0, 1, progress));
            shardManager.setHudState(uuid, new ShardManager.ShardCuboidHudState(
                    current.cuboidName(),
                    "PAUSED",
                    "Paused",
                    progress.getRemainingSeconds(),
                    true
            ));
            return;
        }

        progress.decrement();

        if (progress.getRemainingSeconds() > 0) {
            PlayerSettingUtils.sendActionBar(plugin, player,
                    shardManager.formatCountdownMessage(current, progress, shardManager.getMultiplier(uuid)));
            shardManager.setHudState(uuid, new ShardManager.ShardCuboidHudState(
                    current.cuboidName(),
                    "ACTIVE",
                    shardManager.formatCountdown(progress),
                    progress.getRemainingSeconds(),
                    true
            ));
            return;
        }

        long multiplier = shardManager.getMultiplier(uuid);
        long amount = current.amountPerInterval() * multiplier;
        shardManager.giveShards(player, amount, false);
        shardManager.sendCuboidRewardFeedback(player, current, amount, multiplier);

        progress.reset(current.intervalSeconds());
        shardManager.setHudState(uuid, new ShardManager.ShardCuboidHudState(
                current.cuboidName(),
                "REWARDED",
                shardManager.formatCountdown(progress),
                progress.getRemainingSeconds(),
                true
        ));
    }

    private void handleLeave(UUID uuid, String previousId, ShardManager shardManager) {
        if (previousId == null) {
            return;
        }

        ShardManager.ShardCuboidConfig previous = shardManager.getShardCuboidConfig(previousId);
        if (previous != null && previous.resetOnLeave()) {
            shardManager.resetProgress(uuid, previous);
        }
    }

    public static void start(UltimateDonutSmp plugin) {
        plugin.getSpigotScheduler().runGlobalTimer(new ShardCuboidTask(plugin), 20L, 20L);
    }
}
