package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class OfflineLocationManager {

    public record OfflineLocationRecord(
            UUID uuid,
            String username,
            String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            long timestampMillis
    ) {
    }

    private final UltimateDonutSmp plugin;
    private final Map<UUID, OfflineLocationRecord> recordsByUuid = new ConcurrentHashMap<>();
    private final Map<String, UUID> uuidByUsername = new ConcurrentHashMap<>();
    private final File dataFile;
    private volatile boolean dirty = false;

    public OfflineLocationManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.dataFile = plugin != null ? new File(plugin.getDataFolder(), "offline-locations.yml") : new File("offline-locations.yml");
    }

    public void load() {
        recordsByUuid.clear();
        uuidByUsername.clear();
        dirty = false;

        if (!dataFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection root = config.getConfigurationSection("locations");
        if (root == null) {
            return;
        }

        for (String uuidStr : root.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            ConfigurationSection section = root.getConfigurationSection(uuidStr);
            if (section == null) {
                continue;
            }

            String username = section.getString("username");
            String worldName = section.getString("world");
            if (worldName == null || worldName.isBlank()) {
                continue;
            }

            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float) section.getDouble("yaw", 0.0);
            float pitch = (float) section.getDouble("pitch", 0.0);
            long timestamp = section.getLong("timestamp", 0L);

            OfflineLocationRecord record = new OfflineLocationRecord(
                    uuid, username, worldName, x, y, z, yaw, pitch, timestamp
            );
            recordsByUuid.put(uuid, record);
            if (username != null && !username.isBlank()) {
                uuidByUsername.put(username.toLowerCase(Locale.ROOT), uuid);
            }
        }
    }

    public void save() {
        if (!dirty) {
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection root = config.createSection("locations");

        for (OfflineLocationRecord record : recordsByUuid.values()) {
            ConfigurationSection section = root.createSection(record.uuid().toString());
            if (record.username() != null) {
                section.set("username", record.username());
            }
            section.set("world", record.worldName());
            section.set("x", record.x());
            section.set("y", record.y());
            section.set("z", record.z());
            section.set("yaw", record.yaw());
            section.set("pitch", record.pitch());
            section.set("timestamp", record.timestampMillis());
        }

        try {
            config.save(dataFile);
            dirty = false;
        } catch (IOException e) {
            if (plugin != null) {
                plugin.getLogger().log(Level.WARNING, "Failed to save offline-locations.yml", e);
            }
        }
    }

    public void recordDisconnect(Player player) {
        if (player == null) {
            return;
        }

        Location location = player.getLocation();
        if (location.getWorld() == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        String username = player.getName();
        OfflineLocationRecord record = new OfflineLocationRecord(
                uuid,
                username,
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                System.currentTimeMillis()
        );

        recordsByUuid.put(uuid, record);
        if (username != null && !username.isBlank()) {
            uuidByUsername.put(username.toLowerCase(Locale.ROOT), uuid);
        }
        dirty = true;
    }

    public Optional<Location> getLastKnownLocation(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }

        OfflineLocationRecord record = recordsByUuid.get(uuid);
        if (record != null) {
            World world = Bukkit.getWorld(record.worldName());
            if (world != null) {
                return Optional.of(new Location(
                        world,
                        record.x(),
                        record.y(),
                        record.z(),
                        record.yaw(),
                        record.pitch()
                ));
            }
        }

        // Fallback to CraftOfflinePlayer/Paper offline player data
        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer != null) {
                Location loc = offlinePlayer.getLocation();
                if (loc != null && loc.getWorld() != null) {
                    return Optional.of(loc);
                }
            }
        } catch (Exception ignored) {
        }

        return Optional.empty();
    }

    public UUID findUuid(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        UUID cached = uuidByUsername.get(name.toLowerCase(Locale.ROOT));
        if (cached != null) {
            return cached;
        }

        if (plugin.getDatabaseManager() != null) {
            UUID dbUuid = plugin.getDatabaseManager().findPlayerUuidByUsername(name);
            if (dbUuid != null) {
                return dbUuid;
            }
        }

        try {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
            if (offline != null && (offline.hasPlayedBefore() || offline.isOnline())) {
                return offline.getUniqueId();
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public List<String> getKnownPlayerNames() {
        List<String> names = new ArrayList<>();
        for (OfflineLocationRecord record : recordsByUuid.values()) {
            if (record.username() != null && !record.username().isBlank()) {
                names.add(record.username());
            }
        }
        return names;
    }
}
