package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class KeyAllManager {

    private final UltimateDonutSmp plugin;
    private final Random random = new Random();

    public KeyAllManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("KEY-ALL.ENABLED", true);
    }

    public int getEveryMinutes() {
        return Math.max(1, plugin.getConfigManager().getConfig().getInt("KEY-ALL.EVERY", 60));
    }

    public long getCycleSeconds() {
        return getEveryMinutes() * 60L;
    }

    public void reload() {
        long cycleSeconds = getCycleSeconds();
        for (PlayerData data : plugin.getPlayerDataManager().getAll()) {
            initializeCountdown(data, cycleSeconds, true);
        }
    }

    public void handleJoin(Player player) {
        if (player == null) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            return;
        }

        initializeCountdown(data, getCycleSeconds(), true);
    }

    public void tickOnlinePlayers() {
        if (!isEnabled()) {
            return;
        }

        long cycleSeconds = getCycleSeconds();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerDataManager().get(player);
            if (data == null) {
                continue;
            }

            initializeCountdown(data, cycleSeconds, true);
            long remaining = data.getKeyAllRemainingSeconds();
            if (remaining > 1L) {
                data.setKeyAllRemainingSeconds(remaining - 1L);
                continue;
            }

            SelectedKeyReward reward = plugin.getCrateManager() == null ? null : selectConfiguredReward();
            data.setKeyAllRemainingSeconds(cycleSeconds);
            if (reward != null) {
                plugin.getCrateManager().addKeys(player.getUniqueId(), reward.crateId(), reward.amount());
            }

            notifyPlayer(player, reward);
            executeConfiguredCommands(player, reward);
        }
    }

    public int grantCrateKeys(String crateId, int amount, boolean resetTimerAfter) {
        if (plugin.getCrateManager() == null || amount <= 0) {
            return 0;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null) {
            return 0;
        }

        SelectedKeyReward reward = new SelectedKeyReward(
                crate.id(),
                Math.max(1, amount),
                1,
                plugin.getCrateManager().getReadableCrateName(crate)
        );

        int granted = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getCrateManager().addKeys(player.getUniqueId(), reward.crateId(), reward.amount());
            if (resetTimerAfter) {
                PlayerData data = plugin.getPlayerDataManager().get(player);
                if (data != null) {
                    data.setKeyAllRemainingSeconds(getCycleSeconds());
                }
            }
            notifyPlayer(player, reward);
            executeConfiguredCommands(player, reward);
            granted++;
        }
        return granted;
    }

    public long getRemainingSeconds(UUID uuid) {
        long cycleSeconds = getCycleSeconds();
        if (uuid == null) {
            return cycleSeconds;
        }

        PlayerData loaded = plugin.getPlayerDataManager().get(uuid);
        if (loaded != null) {
            initializeCountdown(loaded, cycleSeconds, true);
            return loaded.getKeyAllRemainingSeconds();
        }

        PlayerData stored = plugin.getDatabaseManager().loadPlayer(uuid);
        if (stored == null) {
            return cycleSeconds;
        }

        long remaining = stored.getKeyAllRemainingSeconds();
        if (remaining < 0L) {
            return cycleSeconds;
        }
        return Math.min(remaining, cycleSeconds);
    }

    public String getFormattedCountdown(UUID uuid) {
        return NumberUtils.formatCountdown(getRemainingSeconds(uuid));
    }

    private void initializeCountdown(PlayerData data, long cycleSeconds, boolean clampToCycle) {
        if (data == null) {
            return;
        }

        if (data.getKeyAllRemainingSeconds() < 0L) {
            data.setKeyAllRemainingSeconds(cycleSeconds);
            return;
        }

        if (clampToCycle && data.getKeyAllRemainingSeconds() > cycleSeconds) {
            data.setKeyAllRemainingSeconds(cycleSeconds);
        }
    }

    private SelectedKeyReward selectConfiguredReward() {
        String configuredType = plugin.getConfigManager().getConfig().getString("KEY-ALL.TYPE");
        if (configuredType == null) {
            configuredType = "RANDOM";
        } else if (configuredType.isBlank()) {
            return null;
        }

        String type = configuredType.trim().toUpperCase(Locale.US);

        return switch (type) {
            case "ONE_KEY_ONLY" -> loadOneKeyOnlyReward();
            case "RANDOM" -> loadRandomReward();
            default -> {
                SelectedKeyReward reward = loadRandomReward();
                yield reward != null ? reward : loadOneKeyOnlyReward();
            }
        };
    }

    private SelectedKeyReward loadRandomReward() {
        ConfigurationSection keysSection = plugin.getConfigManager().getConfig()
                .getConfigurationSection("KEY-ALL.RANDOM.KEYS");
        if (keysSection == null) {
            return loadLegacyReward();
        }

        List<SelectedKeyReward> rewards = new ArrayList<>();
        for (String crateId : keysSection.getKeys(false)) {
            int weight = keysSection.getInt(crateId, 0);
            if (weight <= 0) {
                continue;
            }

            CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(crateId);
            if (crate == null) {
                plugin.getLogger().warning("Skipping KEY-ALL random crate '" + crateId + "' because it does not exist.");
                continue;
            }

            rewards.add(new SelectedKeyReward(
                    crate.id(),
                    1,
                    weight,
                    plugin.getCrateManager().getReadableCrateName(crate)
            ));
        }

        if (rewards.isEmpty()) {
            return loadLegacyReward();
        }

        return selectWeightedRandom(rewards);
    }

    private SelectedKeyReward loadOneKeyOnlyReward() {
        String crateId = plugin.getConfigManager().getConfig().getString("KEY-ALL.ONE-KEY-ONLY.KEY", "");
        if (crateId == null || crateId.isBlank()) {
            return loadLegacyReward();
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null) {
            plugin.getLogger().warning("Skipping KEY-ALL one-key-only crate '" + crateId + "' because it does not exist.");
            return loadLegacyReward();
        }

        return new SelectedKeyReward(
                crate.id(),
                1,
                1,
                plugin.getCrateManager().getReadableCrateName(crate)
        );
    }

    private SelectedKeyReward loadLegacyReward() {
        ConfigurationSection rewardsSection = plugin.getConfigManager().getConfig().getConfigurationSection("KEY-ALL.REWARDS");
        if (rewardsSection != null) {
            List<SelectedKeyReward> rewards = new ArrayList<>();
            for (String key : rewardsSection.getKeys(false)) {
                ConfigurationSection section = rewardsSection.getConfigurationSection(key);
                if (section == null || !section.getBoolean("ENABLED", true)) {
                    continue;
                }

                String crateId = section.getString("CRATE-ID", key);
                CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(crateId);
                if (crate == null) {
                    continue;
                }

                rewards.add(new SelectedKeyReward(
                        crate.id(),
                        1,
                        Math.max(1, section.getInt("WEIGHT", 1)),
                        plugin.getCrateManager().getReadableCrateName(crate)
                ));
            }

            if (!rewards.isEmpty()) {
                return selectWeightedRandom(rewards);
            }
        }

        String crateId = plugin.getConfigManager().getConfig().getString("KEY-ALL.REWARD.CRATE-ID", "");
        if (crateId == null || crateId.isBlank()) {
            return null;
        }

        CrateManager.CrateDefinition crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null) {
            return null;
        }

        return new SelectedKeyReward(
                crate.id(),
                1,
                1,
                plugin.getCrateManager().getReadableCrateName(crate)
        );
    }

    private SelectedKeyReward selectWeightedRandom(List<SelectedKeyReward> rewards) {
        int totalWeight = rewards.stream().mapToInt(reward -> Math.max(1, reward.weight())).sum();
        if (totalWeight <= 0) {
            return rewards.getFirst();
        }

        int pick = random.nextInt(totalWeight) + 1;
        int cursor = 0;
        for (SelectedKeyReward reward : rewards) {
            cursor += Math.max(1, reward.weight());
            if (pick <= cursor) {
                return reward;
            }
        }

        return rewards.getFirst();
    }

    private void executeConfiguredCommands(Player player, SelectedKeyReward reward) {
        if (player == null || !player.isOnline()) {
            return;
        }

        for (String command : readCommandRewards()) {
            if (command == null || command.isBlank()) {
                continue;
            }

            String resolved = resolveCommandReward(command, player, reward);
            if (resolved.isBlank()) {
                continue;
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
        }
    }

    private String resolveCommandReward(String command, Player player, SelectedKeyReward reward) {
        String resolved = command.trim();
        if (resolved.startsWith("/")) {
            resolved = resolved.substring(1).trim();
        }

        return resolved
                .replace("{player}", player.getName())
                .replace("{username}", player.getName())
                .replace("{uuid}", player.getUniqueId().toString())
                .replace("{crate}", reward == null ? "" : reward.crateId())
                .replace("{crate_id}", reward == null ? "" : reward.crateId())
                .replace("{crate_name}", reward == null ? "crate" : reward.displayName())
                .replace("{amount}", String.valueOf(reward == null ? 1 : reward.amount()));
    }

    private List<String> readCommandRewards() {
        List<String> commands = plugin.getConfigManager().getConfig().getStringList("KEY-ALL.COMMANDS");
        if (!commands.isEmpty()) {
            return commands;
        }

        String command = plugin.getConfigManager().getConfig().getString("KEY-ALL.COMMANDS", "");
        if (command == null || command.isBlank()) {
            return List.of();
        }
        return List.of(command);
    }

    private void notifyPlayer(Player player, SelectedKeyReward reward) {
        if (player == null || !player.isOnline()) {
            return;
        }

        if (!plugin.getConfigManager().getConfig().getBoolean("KEY-ALL.NOTIFICATION.ENABLED", true)) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data != null && !data.isKeyAllNotificationsEnabled()) {
            return;
        }

        for (String message : readNotificationMessages()) {
            if (message == null || message.isBlank()) {
                continue;
            }

            player.sendMessage(ColorUtils.toComponent(
                    message
                            .replace("{crate}", reward == null ? "" : reward.displayName())
                            .replace("{crate_id}", reward == null ? "" : reward.crateId())
                            .replace("{amount}", String.valueOf(reward == null ? 0 : reward.amount()))
            ));
        }

        String sound = plugin.getConfigManager().getSound("KEY-ALL.REWARD");
        if (sound == null || sound.isBlank()) {
            sound = plugin.getConfigManager().getConfig().getString(
                    "KEY-ALL.NOTIFICATION.SOUND",
                    "minecraft:entity.player.levelup|1.0|1.1"
            );
        }

        SoundUtils.play(player, sound);
    }

    private List<String> readNotificationMessages() {
        List<String> messages = plugin.getConfigManager().getConfig().getStringList("KEY-ALL.NOTIFICATION.MESSAGE");
        if (!messages.isEmpty()) {
            return messages;
        }
        return plugin.getConfigManager().getConfig().getStringList("KEY-ALL.MESSAGE");
    }

    private record SelectedKeyReward(String crateId, int amount, int weight, String displayName) {
    }
}
