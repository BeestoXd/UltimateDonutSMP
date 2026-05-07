package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class OptimizationManager {

    public enum LoadState {
        NORMAL(0, "&aNormal"),
        WARN(1, "&eWarning"),
        CRITICAL(2, "&cCritical");

        private final int severity;
        private final String display;

        LoadState(int severity, String display) {
            this.severity = severity;
            this.display = display;
        }

        public int severity() {
            return severity;
        }

        public String display() {
            return display;
        }
    }

    public enum OptimizedTask {
        SCOREBOARD("SCOREBOARD", "Scoreboard"),
        TABLIST("TABLIST", "Tablist"),
        LUNAR_TEAMMATES("LUNAR-TEAMMATES", "Lunar Team View");

        private final String configKey;
        private final String displayName;

        OptimizedTask(String configKey, String displayName) {
            this.configKey = configKey;
            this.displayName = displayName;
        }

        public String configKey() {
            return configKey;
        }

        public String displayName() {
            return displayName;
        }
    }

    private static final String CONFIG_PATH = "OPTIMIZATION";
    private static final long MILLIS_PER_TICK = 50L;

    private final UltimateDonutSmp plugin;
    private final Map<OptimizedTask, TaskThrottle> throttles = new EnumMap<>(OptimizedTask.class);
    private final Map<OptimizedTask, Long> lastRunMillis = new EnumMap<>(OptimizedTask.class);
    private final Map<OptimizedTask, Long> skippedRuns = new EnumMap<>(OptimizedTask.class);
    private final Map<String, Method> serverMetricMethods = new HashMap<>();
    private final Set<String> unavailableMetricMethods = new HashSet<>();

    private ScheduledTask monitorTask;
    private boolean enabled;
    private boolean logStateChanges;
    private long monitorIntervalTicks;
    private double warnTps;
    private double criticalTps;
    private double warnMspt;
    private double criticalMspt;
    private int recoverySamples;
    private int recoverySamplesSeen;
    private double lastTps = -1.0D;
    private double lastMspt = -1.0D;
    private long lastSampleMillis;
    private LoadState loadState = LoadState.NORMAL;

    public OptimizationManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        reload();
    }

    public void start() {
        shutdown();
        if (!enabled) {
            return;
        }

        monitorTask = plugin.getPaperScheduler().runGlobalTimer(
                this::sampleServerLoad,
                monitorIntervalTicks,
                monitorIntervalTicks
        );
        sampleServerLoad();
    }

    public void shutdown() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
    }

    public void reload() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        enabled = config.getBoolean(CONFIG_PATH + ".ENABLED", true);
        logStateChanges = config.getBoolean(CONFIG_PATH + ".LOG-STATE-CHANGES", true);
        monitorIntervalTicks = Math.max(20L, config.getLong(CONFIG_PATH + ".MONITOR-INTERVAL-TICKS", 100L));
        warnTps = config.getDouble(CONFIG_PATH + ".TPS-WARN-THRESHOLD", 18.5D);
        criticalTps = config.getDouble(CONFIG_PATH + ".TPS-CRITICAL-THRESHOLD", 16.0D);
        warnMspt = config.getDouble(CONFIG_PATH + ".MSPT-WARN-THRESHOLD", 45.0D);
        criticalMspt = config.getDouble(CONFIG_PATH + ".MSPT-CRITICAL-THRESHOLD", 55.0D);
        recoverySamples = Math.max(1, config.getInt(CONFIG_PATH + ".RECOVERY-SAMPLES", 3));
        recoverySamplesSeen = 0;

        throttles.clear();
        for (OptimizedTask task : OptimizedTask.values()) {
            throttles.put(task, readThrottle(config, task));
        }

        if (!enabled) {
            loadState = LoadState.NORMAL;
        }

        if (monitorTask != null) {
            start();
        }
    }

    public boolean shouldRun(OptimizedTask task) {
        if (task == null || !enabled || loadState == LoadState.NORMAL) {
            return true;
        }

        TaskThrottle throttle = throttles.get(task);
        if (throttle == null || !throttle.enabled()) {
            return true;
        }

        long minIntervalMillis = loadState == LoadState.CRITICAL
                ? throttle.criticalMinIntervalMillis()
                : throttle.warnMinIntervalMillis();
        if (minIntervalMillis <= 0L) {
            return true;
        }

        long now = System.currentTimeMillis();
        long lastRun = lastRunMillis.getOrDefault(task, 0L);
        if (lastRun <= 0L || now - lastRun >= minIntervalMillis) {
            lastRunMillis.put(task, now);
            return true;
        }

        skippedRuns.merge(task, 1L, Long::sum);
        return false;
    }

    public void resetStats() {
        skippedRuns.clear();
        lastRunMillis.clear();
        recoverySamplesSeen = 0;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LoadState getLoadState() {
        return loadState;
    }

    public double getLastTps() {
        return lastTps;
    }

    public double getLastMspt() {
        return lastMspt;
    }

    public long getLastSampleMillis() {
        return lastSampleMillis;
    }

    public long getSkippedRuns(OptimizedTask task) {
        return skippedRuns.getOrDefault(task, 0L);
    }

    public long getTotalSkippedRuns() {
        long total = 0L;
        for (long skipped : skippedRuns.values()) {
            total += skipped;
        }
        return total;
    }

    public long getUsedMemoryMb() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
    }

    public long getMaxMemoryMb() {
        return Runtime.getRuntime().maxMemory() / (1024L * 1024L);
    }

    public String formatMetric(double value) {
        if (value < 0.0D || Double.isNaN(value) || Double.isInfinite(value)) {
            return "n/a";
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private void sampleServerLoad() {
        if (!enabled) {
            lastTps = -1.0D;
            lastMspt = -1.0D;
            lastSampleMillis = System.currentTimeMillis();
            setLoadState(LoadState.NORMAL);
            return;
        }

        lastTps = readTps();
        lastMspt = readMspt();
        lastSampleMillis = System.currentTimeMillis();

        LoadState requestedState = classify(lastTps, lastMspt);
        updateLoadState(requestedState);
    }

    private LoadState classify(double tps, double mspt) {
        boolean critical = (tps > 0.0D && tps < criticalTps)
                || (mspt > 0.0D && mspt > criticalMspt);
        if (critical) {
            return LoadState.CRITICAL;
        }

        boolean warn = (tps > 0.0D && tps < warnTps)
                || (mspt > 0.0D && mspt > warnMspt);
        return warn ? LoadState.WARN : LoadState.NORMAL;
    }

    private void updateLoadState(LoadState requestedState) {
        if (requestedState == loadState) {
            recoverySamplesSeen = 0;
            return;
        }

        if (requestedState.severity() > loadState.severity()) {
            setLoadState(requestedState);
            recoverySamplesSeen = 0;
            return;
        }

        recoverySamplesSeen++;
        if (recoverySamplesSeen >= recoverySamples) {
            setLoadState(requestedState);
            recoverySamplesSeen = 0;
        }
    }

    private void setLoadState(LoadState newState) {
        if (newState == loadState) {
            return;
        }

        LoadState previous = loadState;
        loadState = newState;
        if (logStateChanges) {
            plugin.getLogger().info("Optimization state changed from " + previous.name()
                    + " to " + newState.name()
                    + " (TPS=" + formatMetric(lastTps)
                    + ", MSPT=" + formatMetric(lastMspt) + ").");
        }
    }

    private TaskThrottle readThrottle(FileConfiguration config, OptimizedTask task) {
        String path = CONFIG_PATH + ".ADAPTIVE-TASKS." + task.configKey();
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return TaskThrottle.disabled();
        }

        boolean taskEnabled = section.getBoolean("ENABLED", true);
        long warnInterval = ticksToMillis(section.getLong("WARN-MIN-INTERVAL-TICKS", 0L));
        long criticalInterval = ticksToMillis(section.getLong("CRITICAL-MIN-INTERVAL-TICKS", 0L));
        return new TaskThrottle(taskEnabled, warnInterval, criticalInterval);
    }

    private long ticksToMillis(long ticks) {
        return Math.max(0L, ticks) * MILLIS_PER_TICK;
    }

    private double readTps() {
        Object result = invokeNoArgs("getTPS");
        if (result instanceof double[] values && values.length > 0) {
            return values[0];
        }
        if (result instanceof float[] values && values.length > 0) {
            return values[0];
        }
        if (result instanceof Number number) {
            return number.doubleValue();
        }
        return -1.0D;
    }

    private double readMspt() {
        Object averageTickTime = invokeNoArgs("getAverageTickTime");
        if (averageTickTime instanceof Number number) {
            return number.doubleValue();
        }

        Object tickTimes = invokeNoArgs("getTickTimes");
        if (tickTimes instanceof long[] values && values.length > 0) {
            long total = 0L;
            int samples = 0;
            for (long value : values) {
                if (value <= 0L) {
                    continue;
                }
                total += value;
                samples++;
            }
            if (samples > 0) {
                return (total / (double) samples) / 1_000_000.0D;
            }
        }

        return -1.0D;
    }

    private Object invokeNoArgs(String methodName) {
        if (unavailableMetricMethods.contains(methodName)) {
            return null;
        }

        try {
            Method method = serverMetricMethods.get(methodName);
            if (method == null) {
                method = Bukkit.getServer().getClass().getMethod(methodName);
                serverMetricMethods.put(methodName, method);
            }
            return method.invoke(Bukkit.getServer());
        } catch (NoSuchMethodException exception) {
            unavailableMetricMethods.add(methodName);
            plugin.getLogger().log(Level.FINEST, "Server metric method is unavailable: " + methodName, exception);
            return null;
        } catch (ReflectiveOperationException | SecurityException exception) {
            unavailableMetricMethods.add(methodName);
            plugin.getLogger().log(Level.FINEST, "Server metric method is unavailable: " + methodName, exception);
            return null;
        }
    }

    private record TaskThrottle(
            boolean enabled,
            long warnMinIntervalMillis,
            long criticalMinIntervalMillis
    ) {
        private static TaskThrottle disabled() {
            return new TaskThrottle(false, 0L, 0L);
        }
    }
}
