package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.ServerStatusSnapshot;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkStatusManager {

    private static final String ROOT_PATH = "NETWORK-STATUS";
    private static final String SERVERS_PATH = ROOT_PATH + ".SERVERS";
    private static final String LOCAL_SERVER_ID_PATH = ROOT_PATH + ".LOCAL-SERVER-ID";
    private static final String LOCAL_DISPLAY_NAME_PATH = ROOT_PATH + ".LOCAL-DISPLAY-NAME";
    private static final String REFRESH_SECONDS_PATH = ROOT_PATH + ".REFRESH-SECONDS";
    private static final String TIMEOUT_MS_PATH = ROOT_PATH + ".TIMEOUT-MS";
    private static final String ENDPOINT_PATH = ROOT_PATH + ".ENDPOINT";

    private final UltimateDonutSmp plugin;
    private HttpClient httpClient;
    private final Map<String, ServerDefinition> serverDefinitions = new ConcurrentHashMap<>();
    private final Map<String, ServerStatusSnapshot> snapshots = new ConcurrentHashMap<>();
    private final AtomicBoolean remoteRefreshInProgress = new AtomicBoolean(false);

    private volatile List<String> orderedServerIds = List.of();
    private volatile ServerStatusSnapshot localEndpointSnapshot;

    private ScheduledTask localRefreshTask;
    private ScheduledTask remoteRefreshTask;
    private HttpServer endpointServer;
    private ExecutorService endpointExecutor;

    public NetworkStatusManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.httpClient = buildHttpClient();
        this.localEndpointSnapshot = ServerStatusSnapshot.offline(getLocalServerId(), getLocalDisplayName());
        reload();
    }

    public void reload() {
        stopRefreshTasks();
        stopEndpointServer();
        serverDefinitions.clear();
        snapshots.clear();
        orderedServerIds = List.of();
        localEndpointSnapshot = ServerStatusSnapshot.offline(getLocalServerId(), getLocalDisplayName());
        httpClient = buildHttpClient();

        if (!isEnabled()) {
            return;
        }

        loadDefinitions();
        refreshLocalSnapshots();
        startEndpointServer();
        startRefreshTasks();
        requestImmediateRefreshAll();
    }

    public void shutdown() {
        stopRefreshTasks();
        stopEndpointServer();
        serverDefinitions.clear();
        snapshots.clear();
        orderedServerIds = List.of();
        localEndpointSnapshot = ServerStatusSnapshot.offline(getLocalServerId(), getLocalDisplayName());
    }

    public boolean isEnabled() {
        return getConfig().getBoolean(ROOT_PATH + ".ENABLED", true);
    }

    public boolean hasConfiguredServers() {
        return !orderedServerIds.isEmpty();
    }

    public List<String> getOrderedServerIds() {
        return orderedServerIds;
    }

    public List<ServerStatusSnapshot> getOrderedSnapshots() {
        List<ServerStatusSnapshot> orderedSnapshots = new ArrayList<>();
        for (String serverId : orderedServerIds) {
            orderedSnapshots.add(getSnapshot(serverId));
        }
        return orderedSnapshots;
    }

    public ServerStatusSnapshot getSnapshot(String serverId) {
        String normalizedId = normalizeId(serverId);
        if (normalizedId.isEmpty()) {
            return ServerStatusSnapshot.offline("unknown", "Unknown");
        }

        ServerStatusSnapshot snapshot = snapshots.get(normalizedId);
        if (snapshot != null) {
            return snapshot;
        }

        return ServerStatusSnapshot.offline(normalizedId, resolveDisplayName(normalizedId));
    }

    public String resolveDisplayName(String serverId) {
        ServerDefinition definition = serverDefinitions.get(normalizeId(serverId));
        if (definition != null) {
            return definition.displayName();
        }

        return prettifyId(serverId);
    }

    public boolean isKnownServer(String serverId) {
        return serverDefinitions.containsKey(normalizeId(serverId));
    }

    public void requestImmediateRefresh(String serverId) {
        ServerDefinition definition = serverDefinitions.get(normalizeId(serverId));
        if (definition == null || !isEnabled()) {
            return;
        }

        if (definition.sourceType() == SourceType.LOCAL) {
            plugin.getPaperScheduler().runGlobal(this::refreshLocalSnapshots);
            return;
        }

        plugin.getPaperScheduler().runAsync(() -> refreshRemoteSnapshot(definition));
    }

    public void requestImmediateRefreshAll() {
        if (!isEnabled()) {
            return;
        }

        plugin.getPaperScheduler().runGlobal(this::refreshLocalSnapshots);
        plugin.getPaperScheduler().runAsync(this::refreshRemoteSnapshots);
    }

    private void loadDefinitions() {
        ConfigurationSection serversSection = getConfig().getConfigurationSection(SERVERS_PATH);
        if (serversSection == null || serversSection.getKeys(false).isEmpty()) {
            return;
        }

        Map<String, ServerDefinition> loadedDefinitions = new LinkedHashMap<>();
        for (String rawId : serversSection.getKeys(false)) {
            ConfigurationSection serverSection = serversSection.getConfigurationSection(rawId);
            if (serverSection == null) {
                plugin.getLogger().warning("Skipping " + SERVERS_PATH + "." + rawId + " because it is not a section.");
                continue;
            }

            String serverId = normalizeId(rawId);
            if (serverId.isEmpty()) {
                plugin.getLogger().warning("Skipping empty network server id under " + SERVERS_PATH + ".");
                continue;
            }

            String displayName = serverSection.getString("DISPLAY", prettifyId(serverId));
            ConfigurationSection sourceSection = serverSection.getConfigurationSection("SOURCE");
            if (sourceSection == null) {
                plugin.getLogger().warning("Skipping " + serverSection.getCurrentPath()
                        + " because SOURCE is missing.");
                continue;
            }

            SourceType sourceType = SourceType.from(sourceSection.getString("TYPE"));
            if (sourceType == null) {
                plugin.getLogger().warning("Skipping " + sourceSection.getCurrentPath()
                        + " because TYPE is invalid.");
                continue;
            }

            String url = "";
            if (sourceType == SourceType.HTTP) {
                url = sourceSection.getString("URL", "").trim();
                if (url.isBlank()) {
                    plugin.getLogger().warning("Skipping " + sourceSection.getCurrentPath()
                            + " because URL is required for HTTP sources.");
                    continue;
                }

                try {
                    URI.create(url);
                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning("Skipping " + sourceSection.getCurrentPath()
                            + " because URL '" + url + "' is invalid.");
                    continue;
                }
            }

            loadedDefinitions.put(serverId, new ServerDefinition(
                    serverId,
                    displayName,
                    sourceType,
                    url,
                    sourceSection.getString("TOKEN", getEndpointToken())
            ));
        }

        serverDefinitions.putAll(loadedDefinitions);
        orderedServerIds = List.copyOf(loadedDefinitions.keySet());

        for (ServerDefinition definition : loadedDefinitions.values()) {
            snapshots.put(definition.id(), ServerStatusSnapshot.offline(definition.id(), definition.displayName()));
        }
    }

    private void refreshLocalSnapshots() {
        if (!isEnabled()) {
            return;
        }

        if (!Bukkit.isGlobalTickThread()) {
            plugin.getPaperScheduler().runGlobal(this::refreshLocalSnapshots);
            return;
        }

        ServerStatusSnapshot baseSnapshot = createLocalSnapshot(getLocalServerId(), getLocalDisplayName());
        localEndpointSnapshot = baseSnapshot;

        for (ServerDefinition definition : serverDefinitions.values()) {
            if (definition.sourceType() != SourceType.LOCAL) {
                continue;
            }

            snapshots.put(
                    definition.id(),
                    baseSnapshot.withIdentity(definition.id(), definition.displayName())
            );
        }
    }

    private void refreshRemoteSnapshots() {
        if (!isEnabled() || !hasRemoteServers()) {
            return;
        }

        if (!remoteRefreshInProgress.compareAndSet(false, true)) {
            return;
        }

        try {
            for (String serverId : orderedServerIds) {
                ServerDefinition definition = serverDefinitions.get(serverId);
                if (definition == null || definition.sourceType() != SourceType.HTTP) {
                    continue;
                }

                refreshRemoteSnapshot(definition);
            }
        } finally {
            remoteRefreshInProgress.set(false);
        }
    }

    private void refreshRemoteSnapshot(ServerDefinition definition) {
        long startedAt = System.nanoTime();

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(definition.url()))
                    .GET()
                    .timeout(Duration.ofMillis(getTimeoutMillis()));

            if (!definition.token().isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + definition.token());
            }

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                snapshots.put(definition.id(), ServerStatusSnapshot.offline(definition.id(), definition.displayName()));
                return;
            }

            long latencyMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            snapshots.put(definition.id(), parseStatusPayload(response.body(), definition, latencyMs));
        } catch (Exception ignored) {
            snapshots.put(definition.id(), ServerStatusSnapshot.offline(definition.id(), definition.displayName()));
        }
    }

    private ServerStatusSnapshot parseStatusPayload(String payload, ServerDefinition definition, long latencyMs)
            throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(payload == null ? "" : payload));

        boolean online = Boolean.parseBoolean(properties.getProperty("online", "false"));
        int players = parseInt(properties.getProperty("players"), 0);
        String software = properties.getProperty("software", "N/A");
        String performance = properties.getProperty("performance", "N/A");
        long timestamp = parseLong(properties.getProperty("timestamp"), System.currentTimeMillis());

        if (!online) {
            players = 0;
            software = "N/A";
            performance = "N/A";
        }

        return new ServerStatusSnapshot(
                definition.id(),
                definition.displayName(),
                online,
                players,
                software,
                performance,
                timestamp,
                latencyMs
        );
    }

    private void startRefreshTasks() {
        if (!isEnabled()) {
            return;
        }

        long periodTicks = Math.max(20L, getRefreshSeconds() * 20L);
        if (hasLocalConsumers()) {
            localRefreshTask = plugin.getPaperScheduler().runGlobalTimer(
                    this::refreshLocalSnapshots,
                    periodTicks,
                    periodTicks
            );
        }

        if (hasRemoteServers()) {
            remoteRefreshTask = plugin.getPaperScheduler().runAsyncTimer(
                    this::refreshRemoteSnapshots,
                    periodTicks,
                    periodTicks
            );
        }
    }

    private void stopRefreshTasks() {
        if (localRefreshTask != null) {
            localRefreshTask.cancel();
            localRefreshTask = null;
        }

        if (remoteRefreshTask != null) {
            remoteRefreshTask.cancel();
            remoteRefreshTask = null;
        }
    }

    private void startEndpointServer() {
        if (!isEndpointEnabled()) {
            return;
        }

        int port = getConfig().getInt(ENDPOINT_PATH + ".PORT", 8123);
        if (port <= 0 || port > 65535) {
            plugin.getLogger().warning("Skipping network status endpoint because port '" + port + "' is invalid.");
            return;
        }

        String host = getConfig().getString(ENDPOINT_PATH + ".HOST", "0.0.0.0").trim();
        if (host.isBlank()) {
            host = "0.0.0.0";
        }
        String path = normalizePath(getConfig().getString(ENDPOINT_PATH + ".PATH", "/status"));

        try {
            endpointServer = HttpServer.create(new InetSocketAddress(host, port), 0);
            endpointServer.createContext(path, this::handleStatusEndpointRequest);
            endpointExecutor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "UltimateDonutSmp-NetworkStatusEndpoint");
                thread.setDaemon(true);
                return thread;
            });
            endpointServer.setExecutor(endpointExecutor);
            endpointServer.start();
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to start network status endpoint on "
                    + host + ":" + port + path + " (" + exception.getMessage() + ").");
            stopEndpointServer();
        }
    }

    private void stopEndpointServer() {
        if (endpointServer != null) {
            endpointServer.stop(0);
            endpointServer = null;
        }

        if (endpointExecutor != null) {
            endpointExecutor.shutdownNow();
            endpointExecutor = null;
        }
    }

    private void handleStatusEndpointRequest(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendEndpointResponse(exchange, 405, "method=not_allowed\n");
                return;
            }

            if (!isAuthorized(exchange)) {
                sendEndpointResponse(exchange, 401, "error=unauthorized\n");
                return;
            }

            sendEndpointResponse(exchange, 200, serializeSnapshot(localEndpointSnapshot));
        } finally {
            exchange.close();
        }
    }

    private boolean isAuthorized(HttpExchange exchange) {
        String expectedToken = getEndpointToken();
        if (expectedToken.isBlank()) {
            return true;
        }

        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        return authorization != null && authorization.equals("Bearer " + expectedToken);
    }

    private void sendEndpointResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private String serializeSnapshot(ServerStatusSnapshot snapshot) {
        ServerStatusSnapshot safeSnapshot = snapshot == null
                ? ServerStatusSnapshot.offline(getLocalServerId(), getLocalDisplayName())
                : snapshot;

        return new StringBuilder()
                .append("serverId=").append(safeSnapshot.serverId()).append('\n')
                .append("displayName=").append(safeSnapshot.displayName()).append('\n')
                .append("online=").append(safeSnapshot.online()).append('\n')
                .append("players=").append(safeSnapshot.playerCount()).append('\n')
                .append("software=").append(safeSnapshot.softwareLabel()).append('\n')
                .append("performance=").append(safeSnapshot.performanceLabel()).append('\n')
                .append("timestamp=").append(safeSnapshot.lastUpdatedAt()).append('\n')
                .toString();
    }

    private ServerStatusSnapshot createLocalSnapshot(String serverId, String displayName) {
        String software = plugin.getServer().getName()
                + " " + plugin.getServer().getBukkitVersion()
                + " (MC: " + plugin.getServer().getMinecraftVersion() + ")";
        double[] tpsValues = plugin.getServer().getTPS();

        return new ServerStatusSnapshot(
                serverId,
                displayName,
                true,
                plugin.getServer().getOnlinePlayers().size(),
                software,
                formatPerformance(tpsValues),
                System.currentTimeMillis(),
                0L
        );
    }

    private String formatPerformance(double[] tpsValues) {
        if (tpsValues == null || tpsValues.length < 3) {
            return "N/A";
        }

        return String.format(
                Locale.US,
                "%.2f, %.2f, %.2f",
                normalizeTps(tpsValues[0]),
                normalizeTps(tpsValues[1]),
                normalizeTps(tpsValues[2])
        );
    }

    private double normalizeTps(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0D;
        }

        return Math.max(0.0D, Math.min(20.0D, value));
    }

    private boolean hasLocalConsumers() {
        return isEndpointEnabled() || serverDefinitions.values().stream().anyMatch(definition -> definition.sourceType() == SourceType.LOCAL);
    }

    private boolean hasRemoteServers() {
        return serverDefinitions.values().stream().anyMatch(definition -> definition.sourceType() == SourceType.HTTP);
    }

    private boolean isEndpointEnabled() {
        return getConfig().getBoolean(ENDPOINT_PATH + ".ENABLED", false);
    }

    private String getEndpointToken() {
        return getConfig().getString(ENDPOINT_PATH + ".TOKEN", "").trim();
    }

    private long getRefreshSeconds() {
        return Math.max(2L, getConfig().getLong(REFRESH_SECONDS_PATH, 5L));
    }

    private int getTimeoutMillis() {
        return Math.max(250, getConfig().getInt(TIMEOUT_MS_PATH, 1500));
    }

    private HttpClient buildHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(getTimeoutMillis()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    private String getLocalServerId() {
        return normalizeId(getConfig().getString(LOCAL_SERVER_ID_PATH, "local"));
    }

    private String getLocalDisplayName() {
        String configuredDisplay = getConfig().getString(LOCAL_DISPLAY_NAME_PATH, "");
        if (!configuredDisplay.isBlank()) {
            return configuredDisplay;
        }

        for (ServerDefinition definition : serverDefinitions.values()) {
            if (definition.sourceType() == SourceType.LOCAL) {
                return definition.displayName();
            }
        }

        return prettifyId(getLocalServerId());
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/status";
        }

        return path.startsWith("/") ? path : "/" + path;
    }

    private String normalizeId(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String prettifyId(String value) {
        String normalized = normalizeId(value);
        if (normalized.isEmpty()) {
            return "Unknown";
        }

        String[] parts = normalized.split("[-_\\s]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.isEmpty() ? "Unknown" : builder.toString();
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getNetwork();
    }

    private enum SourceType {
        LOCAL,
        HTTP;

        private static SourceType from(String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                return null;
            }

            try {
                return SourceType.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }
    }

    private record ServerDefinition(
            String id,
            String displayName,
            SourceType sourceType,
            String url,
            String token
    ) {}
}
