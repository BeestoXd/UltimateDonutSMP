package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class ConfigManager {

    private static final List<String> CONFIGURATION_RESOURCES = List.of(
            "config.yml",
            "messages.yml",
            "death-messages.yml",
            "menus.yml",
            "scoreboard.yml",
            "shop.yml",
            "sounds.yml",
            "billford.yml",
            "rtp.yml",
            "worth.yml",
            "amethyst-tools.yml",
            "ender-chest.yml",
            "invsee.yml",
            "freeze.yml",
            "auction-house.yml",
            "orders.yml",
            "duels.yml",
            "ffa.yml",
            "crates.yml",
            "spawners.yml",
            "network.yml",
            "staff-mode.yml",
            "database.yml",
            "discord.yml"
    );

    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final UltimateDonutSmp plugin;

    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration deathMessages;
    private FileConfiguration menus;
    private FileConfiguration scoreboard;
    private FileConfiguration shop;
    private FileConfiguration sounds;
    private FileConfiguration billford;
    private FileConfiguration rtp;
    private FileConfiguration worth;
    private FileConfiguration amethystTools;
    private FileConfiguration enderChest;
    private FileConfiguration invsee;
    private FileConfiguration freeze;
    private FileConfiguration auctionHouse;
    private FileConfiguration orders;
    private FileConfiguration duels;
    private FileConfiguration ffa;
    private FileConfiguration crates;
    private FileConfiguration spawners;
    private FileConfiguration network;
    private FileConfiguration staffMode;
    private FileConfiguration database;
    private FileConfiguration discord;

    public ConfigManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        syncBundledConfigurations();
        reloadLoadedConfigurations();
    }

    public void reload() {
        syncBundledConfigurations();
        reloadLoadedConfigurations();
    }

    private void reloadLoadedConfigurations() {
        plugin.reloadConfig();
        config       = plugin.getConfig();
        messages     = load("messages.yml");
        deathMessages= load("death-messages.yml");
        menus        = load("menus.yml");
        scoreboard   = load("scoreboard.yml");
        shop         = load("shop.yml");
        sounds       = load("sounds.yml");
        billford     = load("billford.yml");
        rtp          = load("rtp.yml");
        worth        = load("worth.yml");
        amethystTools = load("amethyst-tools.yml");
        enderChest   = load("ender-chest.yml");
        invsee       = load("invsee.yml");
        freeze       = load("freeze.yml");
        auctionHouse = load("auction-house.yml");
        orders       = load("orders.yml");
        duels        = load("duels.yml");
        ffa          = load("ffa.yml");
        crates       = load("crates.yml");
        spawners     = load("spawners.yml");
        network      = load("network.yml");
        staffMode    = load("staff-mode.yml");
        database     = load("database.yml");
        discord      = load("discord.yml");
    }

    private void syncBundledConfigurations() {
        File backupDirectory = new File(
                new File(plugin.getDataFolder(), "config-backups"),
                LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT)
        );

        int created = 0;
        int updated = 0;
        int restored = 0;
        int skipped = 0;
        int snapshots = 0;

        for (String name : CONFIGURATION_RESOURCES) {
            SyncResult result = syncBundledConfiguration(name, backupDirectory);
            if (result.created) {
                created++;
            }
            if (result.updated) {
                updated++;
            }
            if (result.restored) {
                restored++;
            }
            if (result.skipped) {
                skipped++;
            }
            if (result.snapshotUpdated) {
                snapshots++;
            }
        }

        plugin.getLogger().info("Configuration sync complete: "
                + created + " created, "
                + updated + " updated, "
                + restored + " restored, "
                + snapshots + " default snapshots refreshed"
                + (skipped > 0 ? ", " + skipped + " skipped" : "")
                + ".");
    }

    private SyncResult syncBundledConfiguration(String name, File backupDirectory) {
        SyncResult result = new SyncResult();
        File targetFile = new File(plugin.getDataFolder(), name);

        YamlConfiguration bundledDefault;
        try {
            bundledDefault = loadBundledYaml(name);
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException e) {
            result.skipped = true;
            plugin.getLogger().log(Level.WARNING, "Skipping configuration sync for missing or invalid bundled resource: " + name, e);
            return result;
        }

        if (!targetFile.exists()) {
            if (copyBundledResource(name, targetFile, false)) {
                result.created = true;
                result.snapshotUpdated = refreshDefaultSnapshot(name);
            } else {
                result.skipped = true;
            }
            return result;
        }

        YamlConfiguration current;
        try {
            current = loadYamlFile(targetFile);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load " + targetFile.getPath() + ", restoring default copy.", e);
            backupExistingFile(targetFile, backupDirectory);
            if (copyBundledResource(name, targetFile, true)) {
                result.restored = true;
                result.snapshotUpdated = refreshDefaultSnapshot(name);
            } else {
                result.skipped = true;
            }
            return result;
        }

        YamlConfiguration previousDefault = loadPreviousDefaultSnapshot(name);
        int mergedPaths = mergeBundledDefaults(name, current, bundledDefault, previousDefault);
        if (mergedPaths > 0) {
            backupExistingFile(targetFile, backupDirectory);
            try {
                current.save(targetFile);
                result.updated = true;
                result.snapshotUpdated = refreshDefaultSnapshot(name);
                plugin.getLogger().info("Updated " + name + " with " + mergedPaths + " bundled default path(s).");
            } catch (IOException e) {
                result.skipped = true;
                plugin.getLogger().log(Level.WARNING, "Failed to save synced configuration " + targetFile.getPath(), e);
            }
            return result;
        }

        result.snapshotUpdated = refreshDefaultSnapshot(name);
        return result;
    }

    private int mergeBundledDefaults(
            String resourceName,
            YamlConfiguration current,
            YamlConfiguration bundledDefault,
            YamlConfiguration previousDefault
    ) {
        int changes = 0;

        for (String path : bundledDefault.getKeys(true)) {
            if (isUserManagedBundledPath(resourceName, path)) {
                continue;
            }

            if (bundledDefault.isConfigurationSection(path)) {
                if (!current.contains(path, true) && !hasScalarParent(current, path)) {
                    current.createSection(path);
                    changes++;
                }
                continue;
            }

            if (!current.contains(path, true)) {
                if (!hasScalarParent(current, path)) {
                    current.set(path, copyConfigValue(bundledDefault.get(path)));
                    changes++;
                }
                continue;
            }

            if (previousDefault == null
                    || !previousDefault.contains(path, true)
                    || previousDefault.isConfigurationSection(path)) {
                continue;
            }

            Object currentValue = current.get(path);
            Object previousValue = previousDefault.get(path);
            Object bundledValue = bundledDefault.get(path);

            if (valuesEquivalent(currentValue, previousValue)
                    && !valuesEquivalent(currentValue, bundledValue)) {
                current.set(path, copyConfigValue(bundledValue));
                changes++;
            }
        }

        return changes;
    }

    private boolean isUserManagedBundledPath(String resourceName, String path) {
        // Crate definitions are live server content. The bundled CRATES tree is only
        // an initial example and must not be merged back after admins edit/delete it.
        return "crates.yml".equals(resourceName) && path.startsWith("CRATES.");
    }

    private boolean hasScalarParent(ConfigurationSection configuration, String path) {
        int dotIndex = path.indexOf('.');
        while (dotIndex > 0) {
            String parentPath = path.substring(0, dotIndex);
            if (configuration.contains(parentPath, true)
                    && !configuration.isConfigurationSection(parentPath)) {
                return true;
            }
            dotIndex = path.indexOf('.', dotIndex + 1);
        }
        return false;
    }

    private YamlConfiguration loadPreviousDefaultSnapshot(String name) {
        File snapshot = new File(defaultSnapshotsFolder(), name);
        if (!snapshot.exists()) {
            return null;
        }

        try {
            return loadYamlFile(snapshot);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING, "Ignoring invalid default configuration snapshot: " + snapshot.getPath(), e);
            return null;
        }
    }

    private boolean refreshDefaultSnapshot(String name) {
        byte[] bundledBytes;
        try {
            bundledBytes = readBundledResourceBytes(name);
        } catch (IOException | IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to read bundled configuration snapshot for " + name, e);
            return false;
        }

        File snapshot = new File(defaultSnapshotsFolder(), name);
        try {
            if (snapshot.exists()) {
                byte[] existingBytes = Files.readAllBytes(snapshot.toPath());
                if (Arrays.equals(existingBytes, bundledBytes)) {
                    return false;
                }
            }

            Files.createDirectories(snapshot.getParentFile().toPath());
            Files.write(snapshot.toPath(), bundledBytes);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to refresh default configuration snapshot: " + snapshot.getPath(), e);
            return false;
        }
    }

    private File defaultSnapshotsFolder() {
        return new File(plugin.getDataFolder(), ".default-configs");
    }

    private YamlConfiguration loadBundledYaml(String name) throws IOException, InvalidConfigurationException {
        try (InputStream input = plugin.getResource(name)) {
            if (input == null) {
                throw new IllegalArgumentException("Resource not found in jar: " + name);
            }

            try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                YamlConfiguration configuration = new YamlConfiguration();
                configuration.load(reader);
                return configuration;
            }
        }
    }

    private YamlConfiguration loadYamlFile(File file) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(file);
        return configuration;
    }

    private boolean copyBundledResource(String name, File target, boolean replace) {
        try (InputStream input = plugin.getResource(name)) {
            if (input == null) {
                plugin.getLogger().warning("Resource not found in jar: " + name);
                return false;
            }

            Files.createDirectories(target.getParentFile().toPath());
            if (replace) {
                Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(input, target.toPath());
            }
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to copy bundled resource " + name + " to " + target.getPath(), e);
            return false;
        }
    }

    private byte[] readBundledResourceBytes(String name) throws IOException {
        try (InputStream input = plugin.getResource(name)) {
            if (input == null) {
                throw new IllegalArgumentException("Resource not found in jar: " + name);
            }
            return input.readAllBytes();
        }
    }

    private void backupExistingFile(File file, File backupDirectory) {
        if (!file.exists()) {
            return;
        }

        File backup = new File(backupDirectory, file.getName());
        try {
            Files.createDirectories(backupDirectory.toPath());
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to back up " + file.getPath(), e);
        }
    }

    private Object copyConfigValue(Object value) {
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            for (Object entry : list) {
                copy.add(copyConfigValue(entry));
            }
            return copy;
        }

        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                copy.put(entry.getKey(), copyConfigValue(entry.getValue()));
            }
            return copy;
        }

        return value;
    }

    private boolean valuesEquivalent(Object first, Object second) {
        return Objects.equals(first, second);
    }

    private FileConfiguration load(String name) {
        File file = new File(plugin.getDataFolder(), name);
        YamlConfiguration configuration = new YamlConfiguration();

        try {
            configuration.load(file);
            return configuration;
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load " + file.getPath() + ", restoring default copy.", e);
            backupBrokenFile(file);

            try {
                copyBundledResource(name, file, true);
                configuration.load(file);
            } catch (IOException | InvalidConfigurationException | IllegalArgumentException restoreException) {
                plugin.getLogger().log(Level.SEVERE, "Failed to restore default resource " + name, restoreException);
            }
            return configuration;
        }
    }

    private void backupBrokenFile(File file) {
        if (!file.exists()) {
            return;
        }

        File backup = new File(file.getParentFile(), file.getName() + ".broken");
        try {
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to back up broken file " + file.getPath(), e);
        }
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public FileConfiguration getConfig()        { return config; }
    public FileConfiguration getMessages()      { return messages; }
    public FileConfiguration getDeathMessages() { return deathMessages; }
    public FileConfiguration getMenus()         { return menus; }
    public FileConfiguration getScoreboard()    { return scoreboard; }
    public FileConfiguration getShop()          { return shop; }
    public FileConfiguration getSounds()        { return sounds; }
    public FileConfiguration getBillford()      { return billford; }
    public FileConfiguration getRtp()           { return rtp; }
    public FileConfiguration getWorth()         { return worth; }
    public FileConfiguration getAmethystTools() { return amethystTools; }
    public FileConfiguration getEnderChest()    { return enderChest; }
    public FileConfiguration getInvsee()        { return invsee; }
    public FileConfiguration getFreeze()        { return freeze; }
    public FileConfiguration getAuctionHouse()  { return auctionHouse; }
    public FileConfiguration getOrders()        { return orders; }
    public FileConfiguration getDuels()         { return duels; }
    public FileConfiguration getFfa()           { return ffa; }
    public FileConfiguration getCrates()        { return crates; }
    public FileConfiguration getSpawners()      { return spawners; }
    public FileConfiguration getNetwork()       { return network; }
    public FileConfiguration getStaffMode()     { return staffMode; }
    public FileConfiguration getDatabase()      { return database; }
    public FileConfiguration getDiscord()       { return discord; }

    public void reloadShop() { shop = load("shop.yml"); }
    public void reloadMenus() { menus = load("menus.yml"); }
    public void reloadSounds() { sounds = load("sounds.yml"); }
    public void reloadWorth() { worth = load("worth.yml"); }
    public void reloadAmethystTools() { amethystTools = load("amethyst-tools.yml"); }
    public void reloadEnderChest() { enderChest = load("ender-chest.yml"); }
    public void reloadInvsee() { invsee = load("invsee.yml"); }
    public void reloadFreeze() { freeze = load("freeze.yml"); }
    public void reloadAuctionHouse() { auctionHouse = load("auction-house.yml"); }
    public void reloadOrders() { orders = load("orders.yml"); }
    public void reloadDuels() { duels = load("duels.yml"); }
    public void reloadFfa() { ffa = load("ffa.yml"); }
    public void reloadCrates() { crates = load("crates.yml"); }
    public void reloadSpawners() { spawners = load("spawners.yml"); }
    public void reloadNetwork() { network = load("network.yml"); }
    public void reloadStaffMode() { staffMode = load("staff-mode.yml"); }
    public void reloadDatabase() { database = load("database.yml"); }
    public void reloadDiscord() { discord = load("discord.yml"); }
    public boolean saveDuels() { return save("duels.yml", duels); }
    public boolean saveFfa() { return save("ffa.yml", ffa); }
    public boolean saveCrates() { return save("crates.yml", crates); }
    public boolean saveMenus() { return save("menus.yml", menus); }
    public boolean saveDatabase() { return save("database.yml", database); }
    public boolean saveNetwork() { return save("network.yml", network); }
    public boolean saveDiscord() { return save("discord.yml", discord); }

    // ── Convenience helpers ────────────────────────────────────────────────────

    public String getMessage(String path) {
        return applyCurrencyPlaceholders(messages.getString(path, "&cMessage not found: " + path));
    }

    public String getMessage(String path, String... placeholders) {
        String msg = getMessage(path);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        return applyCurrencyPlaceholders(msg);
    }

    public String getMessageOrDefault(String path, String fallback) {
        return applyCurrencyPlaceholders(messages.getString(path, fallback));
    }

    public String getMessageOrDefault(String path, String fallback, String... placeholders) {
        String msg = getMessageOrDefault(path, fallback);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        return applyCurrencyPlaceholders(msg);
    }

    private String applyCurrencyPlaceholders(String text) {
        if (text == null || plugin.getCurrencyManager() == null) {
            return text;
        }
        return plugin.getCurrencyManager().applyStaticPlaceholders(text);
    }

    public String getSound(String path) {
        return sounds.getString(path, "");
    }

    public boolean isCommandEnabled(String key) {
        return FeatureManager.isCommandEnabled(config, key);
    }

    private boolean save(String name, FileConfiguration configuration) {
        if (configuration == null) {
            return false;
        }

        File file = new File(plugin.getDataFolder(), name);
        try {
            configuration.save(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save " + file.getPath(), e);
        }
        return false;
    }

    private static final class SyncResult {
        private boolean created;
        private boolean updated;
        private boolean restored;
        private boolean skipped;
        private boolean snapshotUpdated;
    }
}
