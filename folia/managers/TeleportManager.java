package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PlayerSettingUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TeleportManager {

    private final UltimateDonutSmp plugin;
    private final Map<UUID, ScheduledTask> pendingTasks = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingTypes = new ConcurrentHashMap<>();
    private final Map<UUID, Location> startLocations = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerDistanceState> rtpDistanceStates = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> rtpDistanceRestoreTasks = new ConcurrentHashMap<>();

    private record PlayerDistanceState(
            Integer viewDistance,
            Integer simulationDistance,
            Integer noTickViewDistance,
            Integer sendViewDistance
    ) {
    }

    public TeleportManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void queue(Player player, Location destination, String type,
                      Consumer<Player> onSuccess) {
        int cooldownSecs = getCooldown(type);
        String normalizedType = normalizeType(type);
        cancel(player.getUniqueId());

        if (cooldownSecs <= 0) {
            teleportNow(player, destination, normalizedType, onSuccess);
            return;
        }

        startLocations.put(player.getUniqueId(), player.getLocation().clone());

        sendCountdownFeedback(player, cooldownSecs);
        if (!"RTP".equals(normalizedType)) {
            sendMovementWarning(player, cooldownSecs);
        }

        final int[] remaining = {cooldownSecs};
        ScheduledTask task = plugin.getFoliaScheduler().runEntityTimer(player, () -> {
            remaining[0]--;

            if (!player.isOnline()) {
                cancel(player.getUniqueId());
                return;
            }

            if (remaining[0] <= 0) {
                cancel(player.getUniqueId());
                PlayerSettingUtils.clearActionBar(player);
                teleportNow(player, destination, normalizedType, onSuccess);
            } else {
                sendCountdownFeedback(player, remaining[0]);
            }
        }, 20L, 20L);

        if (task != null) {
            pendingTasks.put(player.getUniqueId(), task);
        }
        pendingTypes.put(player.getUniqueId(), normalizedType);
    }

    private void sendActionBar(Player player, int seconds) {
        String template = plugin.getConfigManager().getMessage("TELEPORT.COUNTDOWN",
                "{seconds}", String.valueOf(seconds));
        PlayerSettingUtils.sendActionBar(plugin, player, ColorUtils.toComponent(template));
    }

    private void sendCountdownFeedback(Player player, int seconds) {
        sendActionBar(player, seconds);
        SoundUtils.play(player, plugin.getConfigManager().getSound("TELEPORT.COUNTDOWN"));
    }

    private void sendMovementWarning(Player player, int seconds) {
        String warning = plugin.getConfigManager().getMessage(
                "TELEPORT.WARNING",
                "{seconds}", String.valueOf(seconds)
        );
        if (warning == null || warning.isBlank()) {
            return;
        }
        player.sendMessage(ColorUtils.toComponent(warning));
    }

    private void teleportNow(Player player, Location destination, String normalizedType, Consumer<Player> onSuccess) {
        boolean rtpThrottleActive = "RTP".equals(normalizedType) && applyRtpChunkThrottle(player);
        plugin.getFoliaScheduler().teleport(player, destination).whenComplete((success, throwable) ->
                plugin.getFoliaScheduler().runEntity(player, () -> {
                    if (!player.isOnline()) {
                        if (rtpThrottleActive) {
                            restoreRtpChunkThrottle(player.getUniqueId());
                        }
                        return;
                    }
                    if (throwable != null || !Boolean.TRUE.equals(success)) {
                        if (rtpThrottleActive) {
                            restoreRtpChunkThrottle(player.getUniqueId());
                        }
                        String failed = plugin.getConfigManager().getMessageOrDefault(
                                "TELEPORT.FAILED",
                                "&cᴛᴇʟᴇᴘᴏʀᴛ ꜰᴀɪʟᴇᴅ. ᴘʟᴇᴀѕᴇ ᴛʀʏ ᴀɢᴀɪɴ."
                        );
                        player.sendMessage(ColorUtils.toComponent(failed));
                        return;
                    }

                    String msg = plugin.getConfigManager().getMessage("TELEPORT.SUCCESS");
                    player.sendMessage(ColorUtils.toComponent(msg));
                    SoundUtils.play(player, plugin.getConfigManager().getSound("TELEPORT.SUCCESS"));
                    if ("RTP".equals(normalizedType)) {
                        scheduleRtpChunkRefresh(player, destination);
                    }
                    if (rtpThrottleActive) {
                        scheduleRtpChunkThrottleRestore(player);
                    }
                    if (onSuccess != null) {
                        onSuccess.accept(player);
                    }
                }));
    }

    public void cancel(UUID uuid) {
        ScheduledTask task = pendingTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        pendingTypes.remove(uuid);
        startLocations.remove(uuid);
        restoreRtpChunkThrottle(uuid);
    }

    public boolean hasPending(UUID uuid) {
        return pendingTasks.containsKey(uuid);
    }

    public boolean hasPendingType(UUID uuid, String type) {
        String pendingType = pendingTypes.get(uuid);
        return pendingType != null && pendingType.equals(normalizeType(type));
    }

    public int countPendingByType(String type) {
        String normalizedType = normalizeType(type);
        int count = 0;
        for (String pendingType : pendingTypes.values()) {
            if (normalizedType.equals(pendingType)) {
                count++;
            }
        }
        return count;
    }

    public void checkMovement(Player player) {
        Location start = startLocations.get(player.getUniqueId());
        if (start == null) {
            return;
        }

        Location now = player.getLocation();
        if (Math.abs(now.getX() - start.getX()) > 0.5
                || Math.abs(now.getZ() - start.getZ()) > 0.5) {
            cancel(player.getUniqueId());
            PlayerSettingUtils.clearActionBar(player);
            String msg = plugin.getConfigManager().getMessage("TELEPORT.CANCELED");
            player.sendMessage(ColorUtils.toComponent(msg));
            SoundUtils.play(player, plugin.getConfigManager().getSound("TELEPORT.CANCELLED"));
        }
    }

    private int getCooldown(String type) {
        return plugin.getConfigManager().getConfig()
                .getInt("TELEPORT-COOLDOWN." + type.toUpperCase(Locale.ROOT), 5);
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
    }

    public void restoreAllRtpChunkThrottles() {
        for (UUID uuid : rtpDistanceStates.keySet().toArray(UUID[]::new)) {
            restoreRtpChunkThrottle(uuid);
        }
    }

    private boolean applyRtpChunkThrottle(Player player) {
        if (!plugin.getConfigManager().getRtp().getBoolean("SETTINGS.POST-TELEPORT-CHUNK-THROTTLE", true)) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        ScheduledTask restoreTask = rtpDistanceRestoreTasks.remove(uuid);
        if (restoreTask != null) {
            restoreTask.cancel();
        }

        int targetViewDistance = getRtpThrottleDistance("SETTINGS.POST-TELEPORT-VIEW-DISTANCE", 4);
        int targetSimulationDistance = getRtpThrottleDistance("SETTINGS.POST-TELEPORT-SIMULATION-DISTANCE", 4);
        Integer currentViewDistance = invokeDistanceGetter(player, "getViewDistance");
        Integer currentSimulationDistance = invokeDistanceGetter(player, "getSimulationDistance");
        Integer currentNoTickViewDistance = invokeDistanceGetter(player, "getNoTickViewDistance");
        Integer currentSendViewDistance = invokeDistanceGetter(player, "getSendViewDistance");
        PlayerDistanceState existingState = rtpDistanceStates.get(uuid);

        boolean shouldChangeView = currentViewDistance != null && currentViewDistance > targetViewDistance;
        boolean shouldChangeSimulation = currentSimulationDistance != null
                && currentSimulationDistance > targetSimulationDistance;
        boolean shouldChangeNoTick = currentNoTickViewDistance != null && currentNoTickViewDistance > targetViewDistance;
        boolean shouldChangeSend = currentSendViewDistance != null && currentSendViewDistance > targetViewDistance;
        if (existingState == null
                && !shouldChangeView
                && !shouldChangeSimulation
                && !shouldChangeNoTick
                && !shouldChangeSend) {
            return false;
        }

        rtpDistanceStates.putIfAbsent(uuid, new PlayerDistanceState(
                currentViewDistance,
                currentSimulationDistance,
                currentNoTickViewDistance,
                currentSendViewDistance
        ));
        if (shouldChangeSend) {
            invokeDistanceSetter(player, "setSendViewDistance", targetViewDistance);
        }
        if (shouldChangeNoTick) {
            invokeDistanceSetter(player, "setNoTickViewDistance", targetViewDistance);
        }
        if (shouldChangeView) {
            invokeDistanceSetter(player, "setViewDistance", targetViewDistance);
        }
        if (shouldChangeSimulation) {
            invokeDistanceSetter(player, "setSimulationDistance", targetSimulationDistance);
        }
        return true;
    }

    private void scheduleRtpChunkThrottleRestore(Player player) {
        UUID uuid = player.getUniqueId();
        int delayTicks = Math.max(20, plugin.getConfigManager().getRtp()
                .getInt("SETTINGS.POST-TELEPORT-THROTTLE-TICKS", 80));
        ScheduledTask existingTask = rtpDistanceRestoreTasks.remove(uuid);
        if (existingTask != null) {
            existingTask.cancel();
        }
        ScheduledTask task = plugin.getFoliaScheduler().runEntityLater(
                player,
                () -> restoreRtpChunkThrottleStep(uuid),
                delayTicks
        );
        if (task != null) {
            rtpDistanceRestoreTasks.put(uuid, task);
        } else {
            restoreRtpChunkThrottle(uuid);
        }
    }

    private void restoreRtpChunkThrottle(UUID uuid) {
        ScheduledTask restoreTask = rtpDistanceRestoreTasks.remove(uuid);
        if (restoreTask != null) {
            restoreTask.cancel();
        }

        PlayerDistanceState state = rtpDistanceStates.remove(uuid);
        if (state == null) {
            return;
        }

        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }
        if (state.viewDistance() != null) {
            invokeDistanceSetter(player, "setViewDistance", state.viewDistance());
        }
        if (state.simulationDistance() != null) {
            invokeDistanceSetter(player, "setSimulationDistance", state.simulationDistance());
        }
        if (state.noTickViewDistance() != null) {
            invokeDistanceSetter(player, "setNoTickViewDistance", state.noTickViewDistance());
        }
        if (state.sendViewDistance() != null) {
            invokeDistanceSetter(player, "setSendViewDistance", state.sendViewDistance());
        }
    }

    private void restoreRtpChunkThrottleStep(UUID uuid) {
        rtpDistanceRestoreTasks.remove(uuid);
        PlayerDistanceState state = rtpDistanceStates.get(uuid);
        if (state == null) {
            return;
        }

        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            rtpDistanceStates.remove(uuid);
            return;
        }

        boolean viewDone = restoreDistanceStep(player, "getViewDistance", "setViewDistance", state.viewDistance());
        boolean simulationDone = restoreDistanceStep(
                player,
                "getSimulationDistance",
                "setSimulationDistance",
                state.simulationDistance()
        );
        boolean noTickDone = restoreDistanceStep(
                player,
                "getNoTickViewDistance",
                "setNoTickViewDistance",
                state.noTickViewDistance()
        );
        boolean sendDone = restoreDistanceStep(
                player,
                "getSendViewDistance",
                "setSendViewDistance",
                state.sendViewDistance()
        );
        if (viewDone && simulationDone && noTickDone && sendDone) {
            rtpDistanceStates.remove(uuid);
            return;
        }

        ScheduledTask task = plugin.getFoliaScheduler().runEntityLater(
                player,
                () -> restoreRtpChunkThrottleStep(uuid),
                20L
        );
        if (task != null) {
            rtpDistanceRestoreTasks.put(uuid, task);
        } else {
            restoreRtpChunkThrottle(uuid);
        }
    }

    private boolean restoreDistanceStep(Player player, String getterName, String setterName, Integer originalDistance) {
        if (originalDistance == null) {
            return true;
        }

        Integer currentDistance = invokeDistanceGetter(player, getterName);
        if (currentDistance == null) {
            invokeDistanceSetter(player, setterName, originalDistance);
            return true;
        }
        if (currentDistance >= originalDistance) {
            if (currentDistance > originalDistance) {
                invokeDistanceSetter(player, setterName, originalDistance);
            }
            return true;
        }

        int nextDistance = Math.min(originalDistance, currentDistance + 2);
        invokeDistanceSetter(player, setterName, nextDistance);
        return nextDistance >= originalDistance;
    }

    private int getRtpThrottleDistance(String path, int fallback) {
        return Math.max(2, plugin.getConfigManager().getRtp().getInt(path, fallback));
    }

    private void scheduleRtpChunkRefresh(Player player, Location destination) {
        if (destination == null || destination.getWorld() == null) {
            return;
        }

        World world = destination.getWorld();
        int centerChunkX = destination.getBlockX() >> 4;
        int centerChunkZ = destination.getBlockZ() >> 4;
        List<int[]> chunks = buildRtpChunkOrder(centerChunkX, centerChunkZ, getRtpChunkStabilizationRadius());
        if (chunks.isEmpty()) {
            return;
        }

        int chunksPerTick = getRtpChunkStabilizationChunksPerTick();
        final int[] nextIndex = {0};
        final ScheduledTask[] taskRef = new ScheduledTask[1];
        taskRef[0] = plugin.getFoliaScheduler().runEntityTimer(player, () -> {
            if (!player.isOnline()) {
                if (taskRef[0] != null) {
                    taskRef[0].cancel();
                }
                return;
            }

            int scheduled = 0;
            while (scheduled < chunksPerTick && nextIndex[0] < chunks.size()) {
                int[] chunk = chunks.get(nextIndex[0]++);
                scheduled++;
                plugin.getFoliaScheduler().runRegion(world, chunk[0], chunk[1], () ->
                        refreshLoadedChunk(world, chunk[0], chunk[1]));
            }

            if (nextIndex[0] >= chunks.size() && taskRef[0] != null) {
                taskRef[0].cancel();
            }
        }, 2L, 1L);
    }

    private List<int[]> buildRtpChunkOrder(int centerChunkX, int centerChunkZ, int radius) {
        List<int[]> chunks = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                chunks.add(new int[]{centerChunkX + dx, centerChunkZ + dz});
            }
        }
        chunks.sort(Comparator.comparingInt(chunk ->
                Math.abs(chunk[0] - centerChunkX) + Math.abs(chunk[1] - centerChunkZ)));
        return chunks;
    }

    private int getRtpChunkStabilizationRadius() {
        int configuredRadius = plugin.getConfigManager().getRtp().getInt("SETTINGS.PRELOAD-RADIUS", 1);
        int radius = Math.max(0, Math.min(4, configuredRadius));
        if (plugin.getConfigManager().getRtp().getBoolean("SETTINGS.POST-TELEPORT-CHUNK-THROTTLE", true)) {
            radius = Math.max(radius, Math.min(4, getRtpThrottleDistance("SETTINGS.POST-TELEPORT-VIEW-DISTANCE", 4)));
        }
        return Math.max(2, radius);
    }

    private int getRtpChunkStabilizationChunksPerTick() {
        return Math.max(2, plugin.getConfigManager().getRtp().getInt("SETTINGS.PRELOAD-CHUNKS-PER-TICK", 1));
    }

    private void refreshLoadedChunk(World world, int chunkX, int chunkZ) {
        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            return;
        }
        try {
            Method method = world.getClass().getMethod("refreshChunk", int.class, int.class);
            method.invoke(world, chunkX, chunkZ);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private Integer invokeDistanceGetter(Player player, String methodName) {
        try {
            Method method = player.getClass().getMethod(methodName);
            Object value = method.invoke(player);
            return value instanceof Number number ? number.intValue() : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private void invokeDistanceSetter(Player player, String methodName, int value) {
        try {
            Method method = player.getClass().getMethod(methodName, int.class);
            method.invoke(player, value);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
}
