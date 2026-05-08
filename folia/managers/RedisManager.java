package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.configuration.file.FileConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Objects;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class RedisManager {

    private static final String ROOT_PATH = "REDIS";

    private final UltimateDonutSmp plugin;
    private final Object lifecycleLock = new Object();
    private final AtomicBoolean subscriberRunning = new AtomicBoolean(false);
    private final Map<String, Consumer<String>> subscriptions = new ConcurrentHashMap<>();

    private volatile JedisPool pool;
    private volatile JedisPubSub subscriber;
    private volatile ExecutorService subscriberExecutor;
    private volatile boolean connected;
    private volatile String lastFailureMessage = "";

    public RedisManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        synchronized (lifecycleLock) {
            stopSubscriber(false);
            subscriptions.clear();
            closePool();
            connected = false;
            lastFailureMessage = "";

            if (!isEnabled()) {
                plugin.getLogger().info("Redis integration is disabled.");
                return;
            }

            pool = createPool();
            testConnection();
        }
    }

    public void shutdown() {
        synchronized (lifecycleLock) {
            stopSubscriber(true);
            closePool();
            connected = false;
        }
    }

    public boolean isEnabled() {
        return getConfig().getBoolean(ROOT_PATH + ".ENABLED", true);
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean publish(String channel, String payload) {
        if (!isEnabled() || channel == null || channel.isBlank() || payload == null) {
            return false;
        }

        JedisPool currentPool = pool;
        if (currentPool == null) {
            return false;
        }

        try (Jedis jedis = currentPool.getResource()) {
            jedis.publish(channel, payload);
            connected = true;
            lastFailureMessage = "";
            return true;
        } catch (Exception exception) {
            connected = false;
            logFailure("Redis publish failed: " + exception.getMessage());
            return false;
        }
    }

    public void subscribe(String channel, Consumer<String> messageConsumer) {
        Objects.requireNonNull(messageConsumer, "messageConsumer");

        synchronized (lifecycleLock) {
            String normalizedChannel = normalizeChannel(channel);
            if (normalizedChannel.isBlank()) {
                return;
            }

            subscriptions.put(normalizedChannel, messageConsumer);
            restartSubscriber();
        }
    }

    public void unsubscribe(String channel) {
        synchronized (lifecycleLock) {
            String normalizedChannel = normalizeChannel(channel);
            if (normalizedChannel.isBlank()) {
                return;
            }

            if (subscriptions.remove(normalizedChannel) != null) {
                restartSubscriber();
            }
        }
    }

    public void stopSubscriber() {
        synchronized (lifecycleLock) {
            stopSubscriber(true);
        }
    }

    private void restartSubscriber() {
        stopSubscriber(false);

        if (!isEnabled() || pool == null || subscriptions.isEmpty()) {
            return;
        }

        subscriberRunning.set(true);
        subscriberExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "UltimateDonutSmp-RedisSubscriber");
            thread.setDaemon(true);
            return thread;
        });
        subscriberExecutor.submit(this::runSubscriber);
    }

    private void stopSubscriber(boolean clearSubscriptions) {
        subscriberRunning.set(false);

        JedisPubSub currentSubscriber = subscriber;
        if (currentSubscriber != null) {
            try {
                currentSubscriber.unsubscribe();
            } catch (Exception ignored) {
            }
        }
        subscriber = null;

        ExecutorService currentExecutor = subscriberExecutor;
        if (currentExecutor != null) {
            currentExecutor.shutdownNow();
            try {
                currentExecutor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        subscriberExecutor = null;

        if (clearSubscriptions) {
            subscriptions.clear();
        }
    }

    private void runSubscriber() {
        while (subscriberRunning.get() && isEnabled()) {
            JedisPool currentPool = pool;
            if (currentPool == null) {
                connected = false;
                sleepBeforeReconnect();
                continue;
            }

            String[] channels = subscriptions.keySet().stream()
                    .filter(channel -> channel != null && !channel.isBlank())
                    .toArray(String[]::new);
            if (channels.length == 0) {
                return;
            }

            JedisPubSub currentSubscriber = new JedisPubSub() {
                @Override
                public void onMessage(String subscribedChannel, String message) {
                    Consumer<String> consumer = subscriptions.get(subscribedChannel);
                    if (consumer == null) {
                        return;
                    }
                    consumer.accept(message);
                }
            };
            subscriber = currentSubscriber;

            try (Jedis jedis = currentPool.getResource()) {
                connected = true;
                lastFailureMessage = "";
                jedis.subscribe(currentSubscriber, channels);
            } catch (Exception exception) {
                connected = false;
                if (subscriberRunning.get()) {
                    logFailure("Redis subscriber failed: " + exception.getMessage());
                    sleepBeforeReconnect();
                }
            } finally {
                if (subscriber == currentSubscriber) {
                    subscriber = null;
                }
            }
        }
    }

    private String normalizeChannel(String channel) {
        return channel == null ? "" : channel.trim();
    }

    private JedisPool createPool() {
        FileConfiguration config = getConfig();
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(Math.max(1, config.getInt(ROOT_PATH + ".MAX-TOTAL", 50)));
        poolConfig.setMaxIdle(Math.max(0, config.getInt(ROOT_PATH + ".MAX-IDLE", 10)));
        poolConfig.setMinIdle(Math.max(0, config.getInt(ROOT_PATH + ".MIN-IDLE", 5)));
        poolConfig.setTestOnBorrow(config.getBoolean(ROOT_PATH + ".TEST-ON-BORROW", false));
        poolConfig.setTestOnReturn(config.getBoolean(ROOT_PATH + ".TEST-ON-RETURN", false));
        poolConfig.setTestWhileIdle(config.getBoolean(ROOT_PATH + ".TEST-WHILE-IDLE", false));

        String host = config.getString(ROOT_PATH + ".HOST", "localhost");
        int port = Math.max(1, Math.min(65535, config.getInt(ROOT_PATH + ".PORT", 6379)));
        int timeout = Math.max(250, config.getInt(ROOT_PATH + ".TIMEOUT", 2000));
        String password = config.getString(ROOT_PATH + ".PASSWORD", "");
        if (password != null && password.isBlank()) {
            password = null;
        }
        int database = Math.max(0, config.getInt(ROOT_PATH + ".DATABASE", 0));

        return new JedisPool(poolConfig, host, port, timeout, password, database);
    }

    private void testConnection() {
        JedisPool currentPool = pool;
        if (currentPool == null) {
            return;
        }

        try (Jedis jedis = currentPool.getResource()) {
            jedis.ping();
            connected = true;
            lastFailureMessage = "";
        } catch (Exception exception) {
            connected = false;
            logFailure("Redis connection failed: " + exception.getMessage());
        }
    }

    private void closePool() {
        JedisPool currentPool = pool;
        pool = null;
        if (currentPool != null) {
            try {
                currentPool.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void sleepBeforeReconnect() {
        try {
            Thread.sleep(getReconnectDelayMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private long getReconnectDelayMillis() {
        return Math.max(1000L, getConfig().getLong(ROOT_PATH + ".RECONNECT-DELAY-MS", 5000L));
    }

    private void logFailure(String message) {
        String safeMessage = message == null ? "Redis operation failed." : message;
        if (safeMessage.equals(lastFailureMessage)) {
            return;
        }
        lastFailureMessage = safeMessage;
        plugin.getLogger().warning(safeMessage);
    }

    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getDatabase();
    }
}
