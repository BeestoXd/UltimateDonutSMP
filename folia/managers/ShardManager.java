package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.LocationUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShardManager {

    public enum EverywhereEligibilityResult {
        ELIGIBLE,
        DISABLED,
        NO_PERMISSION,
        EXCLUDED_WORLD,
        AFK,
        NO_RECENT_MOVEMENT,
        IN_SHARD_CUBOID
    }

    public record ShardCuboidConfig(
            String id,
            String cuboidName,
            String world,
            int priority,
            int intervalSeconds,
            long amountPerInterval,
            String countdownMessage,
            String rewardMessage,
            String boostedRewardMessage,
            String leaveMessage,
            int afkTimeoutSeconds,
            String afkCuboidName,
            Location afkLocation,
            String afkMessage,
            boolean teleportOnAfk,
            boolean resetOnLeave,
            int recentMovementWindowSeconds,
            int minimumMovementBlocks,
            String pausedMessage,
            String afkPausedMessage,
            String excludedWorldMessage,
            Set<String> excludedWorlds
    ) {
        public boolean matches(Player player, CuboidManager cuboidManager) {
            if (player.getWorld() == null) {
                return false;
            }
            if (world != null && !world.isBlank() && !player.getWorld().getName().equalsIgnoreCase(world)) {
                return false;
            }
            CuboidManager.Cuboid cuboid = cuboidManager.getCuboid(cuboidName);
            return cuboid != null && cuboid.contains(player.getLocation());
        }

        public boolean isWorldExcluded(String worldName) {
            if (worldName == null) {
                return false;
            }
            return excludedWorlds.contains(worldName.toLowerCase(Locale.ROOT));
        }
    }

    public static final class ShardCuboidProgress {
        private int remainingSeconds;
        private int movementThisCycle;

        public ShardCuboidProgress(int intervalSeconds) {
            this.remainingSeconds = Math.max(0, intervalSeconds);
        }

        public int getRemainingSeconds() {
            return remainingSeconds;
        }

        public int getMovementThisCycle() {
            return movementThisCycle;
        }

        public void decrement() {
            remainingSeconds = Math.max(0, remainingSeconds - 1);
        }

        public void addMovement(int blocks) {
            movementThisCycle = Math.max(0, movementThisCycle + blocks);
        }

        public void reset(int intervalSeconds) {
            remainingSeconds = Math.max(0, intervalSeconds);
            movementThisCycle = 0;
        }
    }

    public record ShardCuboidHudState(
            String cuboidName,
            String status,
            String display,
            int remainingSeconds,
            boolean visible
    ) {}

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Long> boosterExpiry = new HashMap<>();
    private final List<ShardCuboidConfig> shardCuboidConfigs = new ArrayList<>();
    private final Map<String, ShardCuboidConfig> shardCuboidConfigById = new LinkedHashMap<>();
    private final Map<UUID, Map<String, ShardCuboidProgress>> cuboidProgress = new HashMap<>();
    private final Map<UUID, Integer> pendingMovementBlocks = new HashMap<>();
    private final Map<UUID, String> lastMatchedCuboid = new HashMap<>();
    private final Map<UUID, ShardCuboidHudState> hudStates = new HashMap<>();

    public ShardManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        loadShardCuboidConfigs();
    }

    public void giveShards(Player player, long amount, boolean showMessage) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            return;
        }

        data.addShards(amount);
        if (showMessage) {
            String msg = plugin.getConfigManager().getConfig()
                    .getString("SETTINGS.SHARDS-KILL-MESSAGE", "&#A303F9+{shards} Shard")
                    .replace("{shards}", String.valueOf(amount));
            PlayerSettingUtils.sendActionBar(plugin, player, msg);
        }
    }

    public boolean hasBooster(UUID uuid) {
        Long expiry = boosterExpiry.get(uuid);
        if (expiry == null) {
            return false;
        }
        if (System.currentTimeMillis() >= expiry) {
            expireBooster(uuid, true);
            return false;
        }
        return true;
    }

    public boolean activateBooster(Player player) {
        return activateBooster(player, 24L * 60 * 60 * 1000);
    }

    public boolean activateBooster(Player player, long durationMillis) {
        if (hasBooster(player.getUniqueId())) {
            return false;
        }
        long expiry = System.currentTimeMillis() + Math.max(1000L, durationMillis);
        boosterExpiry.put(player.getUniqueId(), expiry);

        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data != null) {
            data.setShardBoosterExpiryMillis(expiry);
        }
        return true;
    }

    public long getBoosterRemainingSeconds(UUID uuid) {
        Long expiry = boosterExpiry.get(uuid);
        if (expiry == null) {
            return 0;
        }
        if (System.currentTimeMillis() >= expiry) {
            expireBooster(uuid, true);
            return 0;
        }
        return Math.max(0, (expiry - System.currentTimeMillis()) / 1000L);
    }

    public int getMultiplier(UUID uuid) {
        if (hasBooster(uuid)) {
            return plugin.getConfigManager().getConfig().getInt("SHARDS.BOOSTER-MULTIPLIER", 4);
        }
        return 1;
    }

    public void syncBooster(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data == null) {
            boosterExpiry.remove(player.getUniqueId());
            return;
        }

        long expiry = data.getShardBoosterExpiryMillis();
        if (expiry <= System.currentTimeMillis()) {
            if (expiry > 0L) {
                data.setShardBoosterExpiryMillis(0L);
            }
            boosterExpiry.remove(player.getUniqueId());
            return;
        }

        boosterExpiry.put(player.getUniqueId(), expiry);
    }

    public void clearBoosterCache(UUID uuid) {
        boosterExpiry.remove(uuid);
    }

    private void expireBooster(UUID uuid, boolean notifyPlayer) {
        boosterExpiry.remove(uuid);

        PlayerData data = plugin.getPlayerDataManager().get(uuid);
        if (data != null && data.getShardBoosterExpiryMillis() != 0L) {
            data.setShardBoosterExpiryMillis(0L);
        }

        if (!notifyPlayer) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        String expiredMessage = plugin.getConfigManager().getMessage("SHARD-BOOSTER.EXPIRED");
        player.sendMessage(com.bx.ultimateDonutSmp.utils.ColorUtils.toComponent(expiredMessage));
        SoundUtils.play(player, plugin.getConfigManager().getSound("AMETHYST.EXPIRED"));
    }

    public void initCountdown(UUID uuid) {
        cuboidProgress.remove(uuid);
        pendingMovementBlocks.put(uuid, 0);
        lastMatchedCuboid.remove(uuid);
        hudStates.put(uuid, new ShardCuboidHudState("None", "OUTSIDE", "-", 0, false));
    }

    public void removeCountdown(UUID uuid) {
        cuboidProgress.remove(uuid);
        pendingMovementBlocks.remove(uuid);
        lastMatchedCuboid.remove(uuid);
        hudStates.remove(uuid);
    }

    public void recordMovement(UUID uuid, Location from, Location to, boolean teleport) {
        if (uuid == null || from == null || to == null || from.getWorld() == null || to.getWorld() == null) {
            return;
        }
        if (teleport || !from.getWorld().equals(to.getWorld())) {
            return;
        }

        int dx = Math.abs(to.getBlockX() - from.getBlockX());
        int dy = Math.abs(to.getBlockY() - from.getBlockY());
        int dz = Math.abs(to.getBlockZ() - from.getBlockZ());
        int movedBlocks = Math.max(dx, Math.max(dy, dz));
        if (movedBlocks <= 0) {
            return;
        }

        pendingMovementBlocks.merge(uuid, Math.min(movedBlocks, 8), Integer::sum);
    }

    public int drainPendingMovement(UUID uuid) {
        int moved = pendingMovementBlocks.getOrDefault(uuid, 0);
        pendingMovementBlocks.remove(uuid);
        return moved;
    }

    public List<ShardCuboidConfig> getShardCuboidConfigs() {
        return shardCuboidConfigs;
    }

    public ShardCuboidConfig getShardCuboidConfig(String id) {
        if (id == null) {
            return null;
        }
        return shardCuboidConfigById.get(id.toLowerCase(Locale.ROOT));
    }

    public ShardCuboidConfig findMatchingShardCuboid(Player player) {
        for (ShardCuboidConfig config : shardCuboidConfigs) {
            if (config.matches(player, plugin.getCuboidManager())) {
                return config;
            }
        }
        return null;
    }

    public ShardCuboidProgress getOrCreateProgress(UUID uuid, ShardCuboidConfig config) {
        Map<String, ShardCuboidProgress> perPlayer = cuboidProgress.computeIfAbsent(uuid, ignored -> new HashMap<>());
        return perPlayer.computeIfAbsent(config.id(), ignored -> new ShardCuboidProgress(config.intervalSeconds()));
    }

    public void resetProgress(UUID uuid, ShardCuboidConfig config) {
        getOrCreateProgress(uuid, config).reset(config.intervalSeconds());
    }

    public void addMovementToProgress(UUID uuid, ShardCuboidConfig config, int movedBlocks) {
        if (movedBlocks <= 0) {
            return;
        }
        getOrCreateProgress(uuid, config).addMovement(movedBlocks);
    }

    public String getLastMatchedCuboid(UUID uuid) {
        return lastMatchedCuboid.get(uuid);
    }

    public void setLastMatchedCuboid(UUID uuid, String cuboidId) {
        if (cuboidId == null) {
            lastMatchedCuboid.remove(uuid);
            return;
        }
        lastMatchedCuboid.put(uuid, cuboidId);
    }

    public void setHudState(UUID uuid, ShardCuboidHudState state) {
        hudStates.put(uuid, state);
    }

    public ShardCuboidHudState getHudState(UUID uuid) {
        return hudStates.getOrDefault(uuid, new ShardCuboidHudState("None", "OUTSIDE", "-", 0, false));
    }

    public boolean shouldShowShardCuboidLine(UUID uuid) {
        return getHudState(uuid).visible();
    }

    public String getShardCuboidDisplay(UUID uuid) {
        return getHudState(uuid).display();
    }

    public String getShardCuboidStatus(UUID uuid) {
        return getHudState(uuid).status();
    }

    public String getShardCuboidName(UUID uuid) {
        return getHudState(uuid).cuboidName();
    }

    public Location resolveAfkLocation(ShardCuboidConfig config) {
        if (config == null) {
            return plugin.getSpawnManager().getAfkLocation();
        }
        if (config.afkLocation() != null) {
            return config.afkLocation();
        }
        if (config.afkCuboidName() != null && !config.afkCuboidName().isBlank()) {
            Location cuboidDestination = plugin.getCuboidManager().getCuboidTeleportLocation(config.afkCuboidName());
            if (cuboidDestination != null) {
                return cuboidDestination;
            }
        }
        return plugin.getSpawnManager().getAfkLocation();
    }

    public String formatCountdown(ShardCuboidProgress progress) {
        return NumberUtils.formatCountdown(progress.getRemainingSeconds());
    }

    public String formatRewardMessage(Player player, ShardCuboidConfig config, long amount, long multiplier) {
        String base = replaceRewardPlaceholders(player, config.rewardMessage(), config, amount, multiplier);
        if (multiplier > 1) {
            return config.boostedRewardMessage()
                    .replace("%base%", base)
                    .replace("%multiplier%", String.valueOf(multiplier))
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%cuboid%", config.cuboidName())
                    .replace("%total%", String.valueOf(getTotalShardsOrZero(player)));
        }
        return base;
    }

    public void sendCuboidRewardFeedback(Player player, ShardCuboidConfig config, long amount, long multiplier) {
        PlayerSettingUtils.sendActionBar(plugin, player, formatRewardMessage(player, config, amount, multiplier));
        String soundPath = multiplier > 1 ? "SHARDS.REWARD-BOOSTED" : "SHARDS.REWARD";
        String sound = plugin.getConfigManager().getSound(soundPath);
        if (sound == null || sound.isBlank()) {
            sound = multiplier > 1
                    ? "minecraft:entity.player.levelup|0.85|1.45"
                    : "minecraft:entity.experience_orb.pickup|0.85|1.35";
        }
        SoundUtils.play(player, sound);
    }

    public void sendCuboidLeaveFeedback(Player player, ShardCuboidConfig config) {
        if (player == null || config == null) {
            return;
        }

        PlayerSettingUtils.sendActionBar(
                plugin,
                player,
                replaceCommonPlaceholders(config.leaveMessage(), config, 0, 0, 1, null)
        );

        String sound = plugin.getConfigManager().getSound("SHARDS.CANCELLED");
        if (sound == null || sound.isBlank()) {
            sound = "minecraft:entity.villager.no|0.8|1.1";
        }
        SoundUtils.play(player, sound);
    }

    public String formatCountdownMessage(ShardCuboidConfig config, ShardCuboidProgress progress, long multiplier) {
        return replaceCommonPlaceholders(
                config.countdownMessage(),
                config,
                progress.getRemainingSeconds(),
                config.amountPerInterval() * Math.max(1, multiplier),
                multiplier,
                progress
        );
    }

    public String replaceCommonPlaceholders(
            String message,
            ShardCuboidConfig config,
            int remainingSeconds,
            long amount,
            long multiplier,
            ShardCuboidProgress progress
    ) {
        if (message == null) {
            return "";
        }

        int movementProgress = progress != null ? progress.getMovementThisCycle() : 0;
        return message
                .replace("%cuboid%", config.cuboidName())
                .replace("%time%", NumberUtils.formatCountdown(Math.max(0, remainingSeconds)))
                .replace("%seconds%", String.valueOf(Math.max(0, remainingSeconds)))
                .replace("%amount%", String.valueOf(amount))
                .replace("%multiplier%", String.valueOf(Math.max(1, multiplier)))
                .replace("%required_movement%", String.valueOf(config.minimumMovementBlocks()))
                .replace("%movement%", String.valueOf(movementProgress));
    }

    private String replaceRewardPlaceholders(Player player, String message, ShardCuboidConfig config, long amount, long multiplier) {
        return replaceCommonPlaceholders(message, config, 0, amount, multiplier, null)
                .replace("%total%", String.valueOf(getTotalShardsOrZero(player)));
    }

    private long getTotalShardsOrZero(Player player) {
        if (player == null) {
            return 0L;
        }
        PlayerData data = plugin.getPlayerDataManager().get(player);
        return data != null ? data.getShards() : 0L;
    }

    public boolean isEverywhereEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("SHARDS.EVERYWHERE.ENABLED", true);
    }

    public int getEverywhereEveryMinutes() {
        return plugin.getConfigManager().getConfig().getInt("SHARDS.EVERYWHERE.EVERY", 3);
    }

    public long getEverywhereAmount() {
        return plugin.getConfigManager().getConfig().getLong("SHARDS.EVERYWHERE.AMOUNT", 1);
    }

    public String getEverywhereRequiredPermission() {
        return emptyToNull(plugin.getConfigManager().getConfig()
                .getString("SHARDS.EVERYWHERE.REQUIRED-PERMISSION", "ultimatedonutsmp.shards.everywhere"));
    }

    public boolean hasEverywherePermission(Player player) {
        if (player == null) {
            return false;
        }

        String permission = getEverywhereRequiredPermission();
        return permission == null || player.hasPermission(permission);
    }

    public int getEverywhereRecentMovementWindowSeconds() {
        return Math.max(1, plugin.getConfigManager().getConfig()
                .getInt("SHARDS.EVERYWHERE.RECENT-MOVEMENT-WINDOW", 15));
    }

    public boolean isEverywhereExcludedWorld(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return false;
        }

        return plugin.getConfigManager().getConfig()
                .getStringList("SHARDS.EVERYWHERE.EXCLUDED-WORLDS")
                .stream()
                .anyMatch(world -> world.equalsIgnoreCase(worldName));
    }

    public boolean isEverywhereDisabledWhileInShardCuboid() {
        return plugin.getConfigManager().getConfig()
                .getBoolean("SHARDS.EVERYWHERE.DISABLE-WHILE-IN-SHARD-CUBOID", false);
    }

    public boolean isInShardCuboid(Player player) {
        return player != null && findMatchingShardCuboid(player) != null;
    }

    public EverywhereEligibilityResult getEverywhereEligibility(Player player) {
        if (player == null || !isEverywhereEnabled()) {
            return EverywhereEligibilityResult.DISABLED;
        }
        if (!hasEverywherePermission(player)) {
            return EverywhereEligibilityResult.NO_PERMISSION;
        }
        if (isEverywhereExcludedWorld(player.getWorld().getName())) {
            return EverywhereEligibilityResult.EXCLUDED_WORLD;
        }
        if (plugin.getAFKManager().isAfk(player.getUniqueId())) {
            return EverywhereEligibilityResult.AFK;
        }
        if (!plugin.getAFKManager().hasRecentMovement(
                player.getUniqueId(),
                getEverywhereRecentMovementWindowSeconds())) {
            return EverywhereEligibilityResult.NO_RECENT_MOVEMENT;
        }
        if (isEverywhereDisabledWhileInShardCuboid() && isInShardCuboid(player)) {
            return EverywhereEligibilityResult.IN_SHARD_CUBOID;
        }
        return EverywhereEligibilityResult.ELIGIBLE;
    }

    public String formatEverywhereRewardMessage(Player player, long amount, long multiplier) {
        boolean boosted = multiplier > 1;
        String path = boosted
                ? "SHARDS.EVERYWHERE.RECEIVED-BOOSTED"
                : "SHARDS.EVERYWHERE.RECEIVED";
        String fallback = boosted
                ? "&#A303F9You received %amount% Shards &7(&ax%multiplier%&7) &8[Everywhere] &7(Total: &#A303F9%total%&7)"
                : "&#A303F9You received %amount% Shard &8[Everywhere] &7(Total: &#A303F9%total%&7)";

        return plugin.getConfigManager().getConfig()
                .getString(path, fallback)
                .replace("%amount%", String.valueOf(amount))
                .replace("%multiplier%", String.valueOf(Math.max(1L, multiplier)))
                .replace("%total%", String.valueOf(getTotalShardsOrZero(player)));
    }

    public void sendEverywhereRewardFeedback(Player player, long amount, long multiplier) {
        if (player == null) {
            return;
        }

        PlayerSettingUtils.sendActionBar(plugin, player, formatEverywhereRewardMessage(player, amount, multiplier));

        String soundPath = multiplier > 1 ? "SHARDS.REWARD-BOOSTED" : "SHARDS.REWARD";
        String sound = plugin.getConfigManager().getSound(soundPath);
        if (sound == null || sound.isBlank()) {
            sound = multiplier > 1
                    ? "minecraft:entity.player.levelup|0.85|1.45"
                    : "minecraft:entity.experience_orb.pickup|0.85|1.35";
        }
        SoundUtils.play(player, sound);
    }

    private Location parseExplicitLocation(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocationUtils.parse(raw);
    }

    public void reloadSettings() {
        loadShardCuboidConfigs();
        cuboidProgress.clear();
        lastMatchedCuboid.clear();
        hudStates.clear();
        pendingMovementBlocks.clear();
    }

    private void loadShardCuboidConfigs() {
        shardCuboidConfigs.clear();
        shardCuboidConfigById.clear();

        FileConfiguration cfg = plugin.getConfigManager().getConfig();
        ConfigurationSection regions = cfg.getConfigurationSection("SHARDS.CUBOIDS.REGIONS");

        if (regions == null || regions.getKeys(false).isEmpty()) {
            ShardCuboidConfig legacy = buildLegacyConfig(cfg);
            shardCuboidConfigs.add(legacy);
            shardCuboidConfigById.put(legacy.id(), legacy);
            return;
        }

        for (String key : regions.getKeys(false)) {
            ConfigurationSection section = regions.getConfigurationSection(key);
            if (section == null || !section.getBoolean("ENABLED", true)) {
                continue;
            }

            boolean bound = section.getBoolean("BOUND", false);
            if (!bound) {
                plugin.getLogger().warning("[ShardManager] Ignoring shard cuboid region '" + key
                        + "' because it is not bound yet. Use /cuboid bind <cuboid> shard true.");
                continue;
            }

            ShardCuboidConfig config = new ShardCuboidConfig(
                    key.toLowerCase(Locale.ROOT),
                    section.getString("CUBOID", key),
                    emptyToNull(section.getString("WORLD")),
                    section.getInt("PRIORITY", 0),
                    Math.max(1, section.getInt("INTERVAL", 60)),
                    Math.max(1L, section.getLong("AMOUNT", 1L)),
                    section.getString("COUNTDOWN-MESSAGE", "&7Next shard in &#A303F9%time%"),
                    section.getString("REWARD-MESSAGE", "&#A303F9You received %amount% Shard &7(Total: &#A303F9%total%&7)"),
                    section.getString("BOOSTED-REWARD-MESSAGE", "&#A303F9You received %amount% Shards &7(&ax%multiplier%&7) &7(Total: &#A303F9%total%&7)"),
                    section.getString("LEAVE-MESSAGE", "&cShard reward cancelled &7(Left %cuboid% zone)"),
                    Math.max(1, section.getInt("AFK-TIME", cfg.getInt("AFK-SYSTEM.TIME", 180))),
                    emptyToNull(section.getString("AFK-CUBOID")),
                    parseExplicitLocation(section.getString("AFK-LOCATION")),
                    section.getString("AFK-MESSAGE", cfg.getString("AFK-SYSTEM.MESSAGE",
                            "&7You have been moved to the AFK area for being inactive in the shard zone.")),
                    section.getBoolean("TELEPORT-ON-AFK", true),
                    section.getBoolean("RESET-ON-LEAVE", cfg.getBoolean("SHARDS.RESET-ON-LEAVE", true)),
                    Math.max(1, section.getInt("RECENT-MOVEMENT-WINDOW", 15)),
                    Math.max(1, section.getInt("MIN-MOVEMENT-BLOCKS", 5)),
                    section.getString("PAUSED-MESSAGE", "&eMove to keep earning shards"),
                    section.getString("AFK-PAUSED-MESSAGE", "&cYou are AFK. Move to resume shard gain"),
                    section.getString("EXCLUDED-WORLD-MESSAGE", "&cShards are disabled in this world"),
                    section.getStringList("EXCLUDED-WORLDS").stream()
                            .map(world -> world.toLowerCase(Locale.ROOT))
                            .collect(Collectors.toSet())
            );

            shardCuboidConfigs.add(config);
            shardCuboidConfigById.put(config.id(), config);
        }

        shardCuboidConfigs.sort(Comparator.comparingInt(ShardCuboidConfig::priority).reversed());
    }

    private ShardCuboidConfig buildLegacyConfig(FileConfiguration cfg) {
        return new ShardCuboidConfig(
                "legacy-spawn",
                cfg.getString("AFK-SYSTEM.SPAWN-CUBOID-NAME", "spawn"),
                null,
                0,
                Math.max(1, cfg.getInt("SHARDS.EVERY", 1) * 60),
                Math.max(1L, cfg.getLong("SHARDS.AMOUNT", 1L)),
                cfg.getString("SHARDS.COUNTDOWN", "&7Next shard in &#A303F9%time%"),
                cfg.getString("SHARDS.RECEIVED", "&#A303F9You received %amount% Shard &7(Total: &#A303F9%total%&7)"),
                cfg.getString("SHARDS.RECEIVED-BOOSTED", "&#A303F9You received %amount% Shards &7(&ax%multiplier%&7) &7(Total: &#A303F9%total%&7)"),
                cfg.getString("SHARDS.CANCELLED-MESSAGE", "&cShard reward cancelled &7(Left %cuboid% zone)"),
                Math.max(1, cfg.getInt("AFK-SYSTEM.TIME", 180)),
                emptyToNull(cfg.getString("AFK-SYSTEM.AFK-CUBOID-NAME")),
                plugin.getSpawnManager().getAfkLocation(),
                cfg.getString("AFK-SYSTEM.MESSAGE",
                        "&7You have been moved to the AFK area for being inactive in the spawn."),
                cfg.getBoolean("AFK-SYSTEM.ENABLED", true),
                cfg.getBoolean("SHARDS.RESET-ON-LEAVE", true),
                15,
                5,
                "&eMove to keep earning shards",
                "&cYou are AFK. Move to resume shard gain",
                "&cShards are disabled in this world",
                Set.of()
        );
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
