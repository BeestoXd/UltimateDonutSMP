package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.ItemUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class RTPManager {

    public record SearchSettings(
            String worldName,
            int minRadius,
            int maxRadius,
            int centerX,
            int centerZ,
            int maxAttempts
    ) {}

    public record RTPDestination(
            String id,
            int slot,
            org.bukkit.Material material,
            String displayName,
            List<String> lore,
            String worldName,
            boolean enabled
    ) {
        public RTPDestination {
            lore = List.copyOf(lore == null ? List.of() : lore);
        }
    }

    private static final long SEARCH_ACTIONBAR_REFRESH_TICKS = 1L;
    private static final long SEARCH_ATTEMPT_INTERVAL_TICKS = 2L;
    private static final long MIN_SEARCH_DISPLAY_TICKS = 60L;
    private static final int SEARCH_ATTEMPTS_PER_TICK = 1;
    private static final long FOUND_ACTIONBAR_DELAY_TICKS = 20L;
    private static final int NETHER_ROOF_PADDING_BLOCKS = 8;
    private static final int PLAYER_CLEARANCE_BLOCKS = 2;

    private static final class SearchProgress {
        private final String worldName;
        private final SearchSettings settings;
        private long elapsedTicks;
        private int attemptsUsed;
        private long lastElapsedSecond;
        private boolean attemptInFlight;
        private Location pendingFoundLocation;

        private SearchProgress(String worldName, SearchSettings settings) {
            this.worldName = worldName;
            this.settings = settings;
        }
    }

    private final UltimateDonutSmp plugin;
    private final Map<UUID, Map<String, Long>> cooldownsByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> activeSearchTasks = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> activeResultTasks = new ConcurrentHashMap<>();
    private final Map<UUID, SearchProgress> activeSearches = new ConcurrentHashMap<>();
    private List<RTPDestination> configuredDestinations = List.of();
    private List<RTPDestination> menuDestinations = List.of();

    public RTPManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        clearAllSearches();
        cooldownsByPlayer.clear();
        configuredDestinations = loadConfiguredDestinations();
        menuDestinations = buildMenuDestinations(configuredDestinations);
    }

    public void clearSearch(UUID playerId) {
        stopSearch(playerId, true);
    }

    private void stopSearch(UUID playerId, boolean clearActionBar) {
        ScheduledTask task = activeSearchTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
        ScheduledTask resultTask = activeResultTasks.remove(playerId);
        if (resultTask != null) {
            resultTask.cancel();
        }
        activeSearches.remove(playerId);

        if (!clearActionBar) {
            return;
        }

        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            PlayerSettingUtils.clearActionBar(player);
        }
    }

    public List<RTPDestination> getMenuDestinations() {
        return menuDestinations;
    }

    public boolean hasMenuDestinations() {
        return !menuDestinations.isEmpty();
    }

    public RTPDestination getDestinationBySlot(int slot) {
        for (RTPDestination destination : menuDestinations) {
            if (destination.slot() == slot) {
                return destination;
            }
        }
        return null;
    }

    public int getPlayersInWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        return world == null ? 0 : world.getPlayers().size();
    }

    public int getWorldCooldownSeconds(String worldName) {
        return Math.max(0, plugin.getConfigManager().getRtp()
                .getInt("WORLD-SETTINGS." + worldName + ".COOLDOWN", 0));
    }

    public SearchSettings getWorldSearchSettings(String worldName) {
        if (!hasWorldSearchSettings(worldName)) {
            return null;
        }

        int minRadius = plugin.getConfigManager().getRtp().getInt("WORLD-SETTINGS." + worldName + ".MIN-RADIUS", 500);
        int maxRadius = plugin.getConfigManager().getRtp().getInt("WORLD-SETTINGS." + worldName + ".MAX-RADIUS", 5000);
        int centerX = plugin.getConfigManager().getRtp().getInt("WORLD-SETTINGS." + worldName + ".CENTER-X", 0);
        int centerZ = plugin.getConfigManager().getRtp().getInt("WORLD-SETTINGS." + worldName + ".CENTER-Z", 0);
        int maxAttempts = plugin.getConfigManager().getRtp().getInt("SETTINGS.MAX-ATTEMPTS", 16);

        return new SearchSettings(
                worldName,
                minRadius,
                Math.max(minRadius, maxRadius),
                centerX,
                centerZ,
                Math.max(1, maxAttempts)
        );
    }

    public SearchSettings getZoneSearchSettings() {
        int minRadius = plugin.getConfigManager().getConfig().getInt("RTP-ZONE.WORLD.MIN-RADIUS", 500);
        int maxRadius = plugin.getConfigManager().getConfig().getInt("RTP-ZONE.WORLD.MAX-RADIUS", 2000);
        int centerX = plugin.getConfigManager().getConfig().getInt("RTP-ZONE.WORLD.CENTER-X", 0);
        int centerZ = plugin.getConfigManager().getConfig().getInt("RTP-ZONE.WORLD.CENTER-Z", 0);
        int maxAttempts = plugin.getConfigManager().getRtp().getInt("SETTINGS.MAX-ATTEMPTS", 16);

        return new SearchSettings(
                plugin.getConfigManager().getConfig().getString("RTP-ZONE.WORLD.NAME", "world"),
                minRadius,
                Math.max(minRadius, maxRadius),
                centerX,
                centerZ,
                Math.max(1, maxAttempts)
        );
    }

    public String describeWorld(String worldName) {
        for (RTPDestination destination : configuredDestinations) {
            if (destination.worldName().equalsIgnoreCase(worldName)) {
                String displayName = ColorUtils.strip(destination.displayName()).trim();
                if (!displayName.isBlank()) {
                    return displayName;
                }
            }
        }

        return switch (worldName.toLowerCase(Locale.ROOT)) {
            case "world" -> "Overworld";
            case "world_nether" -> "Nether";
            case "world_the_end" -> "The End";
            default -> worldName;
        };
    }

    public boolean queueMenuTeleport(Player player, RTPDestination destination) {
        if (destination == null) {
            return false;
        }
        return queueTeleport(player, destination.worldName());
    }

    public boolean queueCommandTeleport(Player player, String selector) {
        String worldName = resolveWorldSelector(selector);
        if (worldName == null || worldName.isBlank()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp().getString("MESSAGES.WORLD-NOT-EXIST", "&cWorld not found.")
            ));
            return false;
        }
        return queueTeleport(player, worldName);
    }

    public boolean isPortalDestinationAvailable(String selector) {
        String worldName = resolveWorldSelector(selector);
        if (worldName == null || worldName.isBlank()) {
            return false;
        }
        if (isDeniedWorld(worldName) || isConfiguredDestinationDisabled(worldName)) {
            return false;
        }
        if (!hasWorldSearchSettings(worldName)) {
            return false;
        }
        return Bukkit.getWorld(worldName) != null;
    }

    public List<String> getPortalSelectorSuggestions() {
        Set<String> selectors = new LinkedHashSet<>();

        for (RTPDestination destination : configuredDestinations) {
            if (!destination.enabled()) {
                continue;
            }
            if (!hasWorldSearchSettings(destination.worldName())) {
                continue;
            }
            if (Bukkit.getWorld(destination.worldName()) == null) {
                continue;
            }

            selectors.add(destination.id());
            selectors.add(destination.worldName());
        }

        if (Bukkit.getWorld("world") != null
                && hasWorldSearchSettings("world")
                && !isDeniedWorld("world")
                && !isConfiguredDestinationDisabled("world")) {
            selectors.add("overworld");
        }
        if (Bukkit.getWorld("world_nether") != null
                && hasWorldSearchSettings("world_nether")
                && !isDeniedWorld("world_nether")
                && !isConfiguredDestinationDisabled("world_nether")) {
            selectors.add("nether");
        }
        if (Bukkit.getWorld("world_the_end") != null
                && hasWorldSearchSettings("world_the_end")
                && !isDeniedWorld("world_the_end")
                && !isConfiguredDestinationDisabled("world_the_end")) {
            selectors.add("end");
        }

        List<String> list = new ArrayList<>(selectors);
        list.sort(String.CASE_INSENSITIVE_ORDER);
        return List.copyOf(list);
    }

    public Location findSafeLocation(SearchSettings settings) {
        if (settings == null || settings.worldName() == null || settings.worldName().isBlank()) {
            return null;
        }

        for (int attempt = 0; attempt < settings.maxAttempts(); attempt++) {
            Location found = tryFindSafeLocationAttempt(settings);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private boolean queueTeleport(Player player, String worldName) {
        if (isDeniedWorld(worldName)) {
            player.sendMessage(ColorUtils.toComponent("&cYou cannot RTP in this world."));
            return false;
        }

        if (isConfiguredDestinationDisabled(worldName)) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp()
                            .getString("MESSAGES.DESTINATION-DISABLED", "&cThis destination is currently disabled.")
            ));
            return false;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp().getString("MESSAGES.WORLD-NOT-EXIST", "&cWorld not found.")
            ));
            return false;
        }

        SearchSettings settings = getWorldSearchSettings(worldName);
        if (settings == null) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp()
                            .getString("MESSAGES.DESTINATION-DISABLED", "&cThis destination is currently disabled.")
            ));
            return false;
        }

        if (hasActiveRtpFlow(player.getUniqueId()) || plugin.getTeleportManager().hasPendingType(player.getUniqueId(), "RTP")) {
            player.sendMessage(ColorUtils.toComponent("&cYour RTP is already in progress."));
            return false;
        }

        long cooldownRemaining = getCooldownRemainingMillis(player.getUniqueId(), worldName);
        if (cooldownRemaining > 0L) {
            long remainingSeconds = Math.max(1L, (long) Math.ceil(cooldownRemaining / 1000.0D));
            String message = plugin.getConfigManager().getRtp()
                    .getString("MESSAGES.COOLDOWN", "&cYou can't rtp for another {remaining}s.");
            message = message.replace("{remaining}", String.valueOf(remainingSeconds))
                    .replace("%remaining%", String.valueOf(remainingSeconds));
            player.sendMessage(ColorUtils.toComponent(message));
            return false;
        }

        if (isQueueFull(player)) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getRtp()
                            .getString("MESSAGES.MAX-PLAYERS", "&cToo many players are using RTP right now. Please try again later.")
            ));
            return false;
        }

        startSearch(player, worldName, settings);
        return true;
    }

    private void startSearch(Player player, String worldName, SearchSettings settings) {
        clearSearch(player.getUniqueId());

        String worldLabel = describeWorld(worldName);
        String searching = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.SEARCHING", "&aSearching for safe location in {world}...")
                .replace("{world}", worldLabel);
        player.sendMessage(ColorUtils.toComponent(searching));

        SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-START"));

        SearchProgress progress = new SearchProgress(worldName, settings);
        activeSearches.put(player.getUniqueId(), progress);

        ScheduledTask task = plugin.getPaperScheduler().runEntityTimer(
                player,
                () -> tickSearch(player.getUniqueId()),
                0L,
                SEARCH_ACTIONBAR_REFRESH_TICKS
        );
        if (task != null) {
            activeSearchTasks.put(player.getUniqueId(), task);
        }
    }

    private void tickSearch(UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        SearchProgress progress = activeSearches.get(playerId);
        if (player == null || !player.isOnline() || progress == null) {
            clearSearch(playerId);
            return;
        }

        progress.elapsedTicks++;
        sendSearchActionBar(player, progress);

        if (progress.pendingFoundLocation != null) {
            if (progress.elapsedTicks >= MIN_SEARCH_DISPLAY_TICKS) {
                stopSearch(playerId, false);
                finishSearch(player, progress.worldName, progress.pendingFoundLocation);
            }
            return;
        }

        if (progress.attemptInFlight || progress.elapsedTicks % SEARCH_ATTEMPT_INTERVAL_TICKS != 0L) {
            return;
        }

        for (int i = 0; i < SEARCH_ATTEMPTS_PER_TICK && progress.attemptsUsed < progress.settings.maxAttempts(); i++) {
            beginAsyncLocationAttempt(playerId, progress);
        }
    }

    private void beginAsyncLocationAttempt(UUID playerId, SearchProgress progress) {
        World world = Bukkit.getWorld(progress.worldName);
        if (world == null) {
            failSearch(playerId, progress);
            return;
        }

        int minRadius = Math.max(0, progress.settings.minRadius());
        int maxRadius = Math.max(minRadius, progress.settings.maxRadius());

        double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
        double distance = minRadius;
        if (maxRadius > minRadius) {
            distance += ThreadLocalRandom.current().nextDouble(0, maxRadius - minRadius);
        }

        int x = progress.settings.centerX() + (int) Math.round(Math.cos(angle) * distance);
        int z = progress.settings.centerZ() + (int) Math.round(Math.sin(angle) * distance);
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        progress.attemptsUsed++;
        progress.attemptInFlight = true;

        world.getChunkAtAsync(chunkX, chunkZ, true).whenComplete((chunk, throwable) ->
                plugin.getPaperScheduler().runRegion(world, chunkX, chunkZ,
                        () -> completeAsyncLocationAttempt(playerId, progress, x, z, throwable))
        );
    }

    private void completeAsyncLocationAttempt(UUID playerId, SearchProgress progress, int x, int z, Throwable throwable) {
        SearchProgress activeProgress = activeSearches.get(playerId);
        if (activeProgress != progress) {
            return;
        }

        progress.attemptInFlight = false;
        if (throwable != null) {
            plugin.getLogger().warning("[RTPManager] Async RTP chunk load failed: " + throwable.getMessage());
            if (progress.attemptsUsed >= progress.settings.maxAttempts()) {
                failSearch(playerId, progress);
            }
            return;
        }

        World world = Bukkit.getWorld(progress.worldName);
        if (world == null) {
            failSearch(playerId, progress);
            return;
        }

        Location found = resolveSafeLocation(world, x, z);
        if (found != null) {
            progress.pendingFoundLocation = found;
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline() && progress.elapsedTicks >= MIN_SEARCH_DISPLAY_TICKS) {
                stopSearch(playerId, false);
                finishSearch(player, progress.worldName, found);
            }
            return;
        }

        if (progress.attemptsUsed >= progress.settings.maxAttempts()) {
            failSearch(playerId, progress);
        }
    }

    private void failSearch(UUID playerId, SearchProgress progress) {
        Player player = plugin.getServer().getPlayer(playerId);
        clearSearch(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }

        String attempts = String.valueOf(progress.settings.maxAttempts());
        String maxAttemptsMessage = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.MAX-ATTEMPTS", "&cCould not find a safe location after %attempts% attempts.")
                .replace("%attempts%", attempts)
                .replace("{attempts}", attempts);
        player.sendMessage(ColorUtils.toComponent(maxAttemptsMessage));
        SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-FAIL"));
    }

    private void finishSearch(Player player, String worldName, Location found) {
        applyCooldown(player.getUniqueId(), worldName);

        String foundMessage = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.SAFE-LOCATION-FOUND", "&aSafe location found at: X:{x} Y:{y} Z:{z}")
                .replace("{x}", String.valueOf(found.getBlockX()))
                .replace("{y}", String.valueOf(found.getBlockY()))
                .replace("{z}", String.valueOf(found.getBlockZ()));
        player.sendMessage(ColorUtils.toComponent(foundMessage));
        sendTeleportWarning(player, worldName);
        SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-FOUND"));
        UUID playerId = player.getUniqueId();

        final long[] shownTicks = {0L};
        final ScheduledTask[] resultTaskRef = new ScheduledTask[1];
        ScheduledTask resultTask = plugin.getPaperScheduler().runEntityTimer(player, () -> {
            if (!player.isOnline()) {
                activeResultTasks.remove(playerId);
                if (resultTaskRef[0] != null) {
                    resultTaskRef[0].cancel();
                }
                return;
            }

            sendFoundActionBar(player, worldName, found, shownTicks[0]);
            shownTicks[0]++;

            if (shownTicks[0] >= FOUND_ACTIONBAR_DELAY_TICKS) {
                activeResultTasks.remove(playerId);
                if (resultTaskRef[0] != null) {
                    resultTaskRef[0].cancel();
                }
                plugin.getTeleportManager().queue(player, found, "RTP", null);
            }
        }, 1L, SEARCH_ACTIONBAR_REFRESH_TICKS);
        resultTaskRef[0] = resultTask;
        if (resultTask != null) {
            activeResultTasks.put(playerId, resultTask);
        }
    }

    private void sendSearchActionBar(Player player, SearchProgress progress) {
        long displayedSeconds = getDisplayedSearchSeconds(progress.elapsedTicks);

        String actionBar = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.SEARCH-ACTIONBAR", "&7Searching {world}... &b{elapsed}s")
                .replace("{world}", describeWorld(progress.worldName))
                .replace("{elapsed}", formatElapsedSeconds(progress.elapsedTicks))
                .replace("{attempts}", String.valueOf(progress.attemptsUsed))
                .replace("{max_attempts}", String.valueOf(progress.settings.maxAttempts()));

        sendPersistentActionBar(player, actionBar, progress.elapsedTicks);
        if (displayedSeconds > progress.lastElapsedSecond) {
            progress.lastElapsedSecond = displayedSeconds;
            SoundUtils.play(player, plugin.getConfigManager().getSound("RTP.SEARCH-TICK"));
        }
    }

    private void sendFoundActionBar(Player player, String worldName, Location found) {
        sendFoundActionBar(player, worldName, found, 0L);
    }

    private void sendFoundActionBar(Player player, String worldName, Location found, long refreshTick) {
        String actionBar = plugin.getConfigManager().getRtp()
                .getString("MESSAGES.SEARCH-FOUND-ACTIONBAR", "&aSafe location found in {world}! &7Preparing teleport...")
                .replace("{world}", describeWorld(worldName))
                .replace("{x}", String.valueOf(found.getBlockX()))
                .replace("{y}", String.valueOf(found.getBlockY()))
                .replace("{z}", String.valueOf(found.getBlockZ()));
        sendPersistentActionBar(player, actionBar, refreshTick);
    }

    private Location tryFindSafeLocationAttempt(SearchSettings settings) {
        if (settings == null || settings.worldName() == null || settings.worldName().isBlank()) {
            return null;
        }

        World world = Bukkit.getWorld(settings.worldName());
        if (world == null) {
            return null;
        }

        int minRadius = Math.max(0, settings.minRadius());
        int maxRadius = Math.max(minRadius, settings.maxRadius());

        double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
        double distance = minRadius;
        if (maxRadius > minRadius) {
            distance += ThreadLocalRandom.current().nextDouble(0, maxRadius - minRadius);
        }

        int x = settings.centerX() + (int) Math.round(Math.cos(angle) * distance);
        int z = settings.centerZ() + (int) Math.round(Math.sin(angle) * distance);
        return resolveSafeLocation(world, x, z);
    }

    private Location resolveSafeLocation(World world, int x, int z) {
        if (world.getEnvironment() == World.Environment.NETHER) {
            return resolveNetherSafeLocation(world, x, z);
        }

        if (!isSafe(world, x, z)) {
            return null;
        }

        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x + 0.5, y + 1.5, z + 0.5);
    }

    private Location resolveNetherSafeLocation(World world, int x, int z) {
        int minGroundY = world.getMinHeight();
        int logicalTopY = Math.min(world.getLogicalHeight(), world.getMaxHeight()) - 1;
        int maxGroundY = Math.min(
                world.getMaxHeight() - 1 - PLAYER_CLEARANCE_BLOCKS,
                logicalTopY - NETHER_ROOF_PADDING_BLOCKS
        );

        if (maxGroundY < minGroundY) {
            maxGroundY = world.getMaxHeight() - 1 - PLAYER_CLEARANCE_BLOCKS;
        }

        for (int groundY = maxGroundY; groundY >= minGroundY; groundY--) {
            if (isSafeStandLocation(world, x, groundY, z)) {
                return new Location(world, x + 0.5, groundY + 1.0, z + 0.5);
            }
        }

        return null;
    }

    private List<RTPDestination> loadConfiguredDestinations() {
        FileConfiguration rtp = plugin.getConfigManager().getRtp();
        ConfigurationSection buttons = rtp.getConfigurationSection("RTP-MENU.BUTTONS");
        if (buttons == null) {
            return List.of();
        }

        List<RTPDestination> loaded = new ArrayList<>();
        for (String key : buttons.getKeys(false)) {
            ConfigurationSection button = buttons.getConfigurationSection(key);
            if (button == null) {
                continue;
            }

            String worldName = button.getString("WORLD", "").trim();
            if (worldName.isBlank()) {
                warn("RTP-MENU.BUTTONS." + key + " is missing WORLD.");
                continue;
            }

            loaded.add(new RTPDestination(
                    key,
                    button.getInt("SLOT", -1),
                    ItemUtils.parseMaterial(button.getString("MATERIAL", "GRASS_BLOCK")),
                    button.getString("DISPLAY-NAME", key),
                    button.getStringList("LORE"),
                    worldName,
                    button.getBoolean("ENABLED", true)
            ));
        }

        return List.copyOf(loaded);
    }

    private List<RTPDestination> buildMenuDestinations(List<RTPDestination> configured) {
        FileConfiguration rtp = plugin.getConfigManager().getRtp();
        int menuSize = normalizeSize(rtp.getInt("RTP-MENU.SIZE", 27));
        Set<Integer> usedSlots = new HashSet<>();
        List<RTPDestination> visible = new ArrayList<>();

        for (RTPDestination destination : configured) {
            if (!destination.enabled()) {
                continue;
            }

            if (destination.slot() < 0 || destination.slot() >= menuSize) {
                warn("RTP destination '" + destination.id() + "' uses invalid slot " + destination.slot()
                        + " for menu size " + menuSize + ".");
                continue;
            }

            if (!usedSlots.add(destination.slot())) {
                warn("RTP destination '" + destination.id() + "' collides with another RTP button on slot "
                        + destination.slot() + ".");
                continue;
            }

            if (!hasWorldSearchSettings(destination.worldName())) {
                warn("RTP destination '" + destination.id() + "' points to world '" + destination.worldName()
                        + "' without WORLD-SETTINGS.");
                continue;
            }

            if (Bukkit.getWorld(destination.worldName()) == null) {
                warn("RTP destination '" + destination.id() + "' points to missing world '" + destination.worldName() + "'.");
                continue;
            }

            visible.add(destination);
        }

        visible.sort(Comparator.comparingInt(RTPDestination::slot));
        return List.copyOf(visible);
    }

    public String resolveWorldSelector(String selector) {
        if (selector == null) {
            return null;
        }

        String trimmed = selector.trim();
        if (trimmed.isBlank()) {
            return null;
        }

        for (RTPDestination destination : configuredDestinations) {
            if (destination.id().equalsIgnoreCase(trimmed)) {
                return destination.worldName();
            }
        }

        return switch (trimmed.toLowerCase(Locale.ROOT)) {
            case "overworld" -> "world";
            case "nether" -> "world_nether";
            case "end", "the_end", "the-end" -> "world_the_end";
            default -> trimmed;
        };
    }

    private boolean isConfiguredDestinationDisabled(String worldName) {
        boolean hasConfiguredDestination = false;
        for (RTPDestination destination : configuredDestinations) {
            if (!destination.worldName().equalsIgnoreCase(worldName)) {
                continue;
            }
            hasConfiguredDestination = true;
            if (destination.enabled()) {
                return false;
            }
        }
        return hasConfiguredDestination;
    }

    private boolean isDeniedWorld(String worldName) {
        for (String deniedWorld : plugin.getConfigManager().getRtp().getStringList("DENIED-WORLDS")) {
            if (deniedWorld.equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasWorldSearchSettings(String worldName) {
        return plugin.getConfigManager().getRtp().isConfigurationSection("WORLD-SETTINGS." + worldName);
    }

    private long getCooldownRemainingMillis(UUID playerId, String worldName) {
        Map<String, Long> cooldowns = cooldownsByPlayer.get(playerId);
        if (cooldowns == null) {
            return 0L;
        }

        String key = normalizeWorldKey(worldName);
        Long expiresAt = cooldowns.get(key);
        if (expiresAt == null) {
            return 0L;
        }

        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining <= 0L) {
            cooldowns.remove(key);
            if (cooldowns.isEmpty()) {
                cooldownsByPlayer.remove(playerId);
            }
            return 0L;
        }
        return remaining;
    }

    private void applyCooldown(UUID playerId, String worldName) {
        int cooldownSeconds = getWorldCooldownSeconds(worldName);
        if (cooldownSeconds <= 0) {
            return;
        }

        cooldownsByPlayer.computeIfAbsent(playerId, ignored -> new HashMap<>())
                .put(normalizeWorldKey(worldName), System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }

    private boolean isQueueFull(Player player) {
        int maxPlayers = plugin.getConfigManager().getRtp().getInt("SETTINGS.PLAYERS-IN-RTP", 0);
        if (maxPlayers <= 0) {
            return false;
        }

        int inProgress = countActiveSearches() + plugin.getTeleportManager().countPendingByType("RTP");
        if (isSearching(player.getUniqueId())) {
            inProgress = Math.max(0, inProgress - 1);
        }
        if (plugin.getTeleportManager().hasPendingType(player.getUniqueId(), "RTP")) {
            inProgress = Math.max(0, inProgress - 1);
        }
        return inProgress >= maxPlayers;
    }

    private int countActiveSearches() {
        return activeSearchTasks.size() + activeResultTasks.size();
    }

    private boolean isSearching(UUID playerId) {
        return activeSearchTasks.containsKey(playerId);
    }

    private boolean hasActiveRtpFlow(UUID playerId) {
        return activeSearchTasks.containsKey(playerId) || activeResultTasks.containsKey(playerId);
    }

    private boolean isSafe(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z);
        if (y <= world.getMinHeight()) {
            return false;
        }

        Block top = world.getBlockAt(x, y, z);
        String typeName = top.getType().name();
        return !typeName.contains("WATER")
                && !typeName.contains("LAVA")
                && !typeName.contains("VOID");
    }

    private boolean isSafeStandLocation(World world, int x, int groundY, int z) {
        if (groundY < world.getMinHeight() || groundY + PLAYER_CLEARANCE_BLOCKS >= world.getMaxHeight()) {
            return false;
        }

        Block ground = world.getBlockAt(x, groundY, z);
        Block feet = world.getBlockAt(x, groundY + 1, z);
        Block head = world.getBlockAt(x, groundY + 2, z);

        return isSafeGround(ground.getType())
                && isSafeBodySpace(feet)
                && isSafeBodySpace(head);
    }

    private boolean isSafeGround(Material material) {
        return material != null
                && material.isSolid()
                && material != Material.BEDROCK
                && !isHazardous(material);
    }

    private boolean isSafeBodySpace(Block block) {
        return block.isPassable() && !isHazardous(block.getType());
    }

    private boolean isHazardous(Material material) {
        if (material == null) {
            return true;
        }

        String typeName = material.name();
        return typeName.contains("LAVA")
                || typeName.contains("FIRE")
                || typeName.contains("MAGMA")
                || typeName.contains("CACTUS")
                || typeName.contains("CAMPFIRE")
                || typeName.contains("SWEET_BERRY_BUSH")
                || typeName.contains("POWDER_SNOW")
                || typeName.contains("VOID");
    }

    private int normalizeSize(int size) {
        int normalized = Math.max(9, ((size + 8) / 9) * 9);
        return Math.min(54, normalized);
    }

    private String normalizeWorldKey(String worldName) {
        return worldName.toLowerCase(Locale.ROOT);
    }

    private String formatElapsedSeconds(long elapsedTicks) {
        return String.valueOf(getDisplayedSearchSeconds(elapsedTicks));
    }

    private long getDisplayedSearchSeconds(long elapsedTicks) {
        return Math.max(1L, (long) Math.ceil(Math.max(0D, elapsedTicks / 20.0D)));
    }

    private void sendTeleportWarning(Player player, String worldName) {
        int teleportCountdown = plugin.getConfigManager().getConfig().getInt("TELEPORT-COOLDOWN.RTP", 5);
        String warning = plugin.getConfigManager().getRtp()
                .getString(
                        "MESSAGES.TP-WARNING",
                        "&eDo not move for &b{countdown}&e seconds or the teleport will be canceled."
                )
                .replace("{world}", describeWorld(worldName))
                .replace("{countdown}", String.valueOf(teleportCountdown));
        if (!warning.isBlank()) {
            player.sendMessage(ColorUtils.toComponent(warning));
        }
    }

    private void sendPersistentActionBar(Player player, String text, long refreshTick) {
        PlayerSettingUtils.sendActionBar(plugin, player, ColorUtils.toComponent(text));
    }

    private void clearAllSearches() {
        for (UUID playerId : List.copyOf(activeSearchTasks.keySet())) {
            clearSearch(playerId);
        }
    }

    private void warn(String message) {
        plugin.getLogger().warning("[RTPManager] " + message);
    }
}
