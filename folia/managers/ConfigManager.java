package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class ConfigManager {

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
        saveDefaults();
        reload();
    }

    public void reload() {
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

    private void saveDefaults() {
        plugin.saveDefaultConfig();
        saveDefault("messages.yml");
        saveDefault("death-messages.yml");
        saveDefault("menus.yml");
        saveDefault("scoreboard.yml");
        saveDefault("shop.yml");
        saveDefault("sounds.yml");
        saveDefault("billford.yml");
        saveDefault("rtp.yml");
        saveDefault("worth.yml");
        saveDefault("amethyst-tools.yml");
        saveDefault("ender-chest.yml");
        saveDefault("invsee.yml");
        saveDefault("freeze.yml");
        saveDefault("auction-house.yml");
        saveDefault("orders.yml");
        saveDefault("duels.yml");
        saveDefault("ffa.yml");
        saveDefault("crates.yml");
        saveDefault("spawners.yml");
        saveDefault("network.yml");
        saveDefault("staff-mode.yml");
        saveDefault("database.yml");
        saveDefault("discord.yml");
    }

    private void saveDefault(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            try {
                plugin.saveResource(name, false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, "Resource not found in jar: " + name);
            }
        }
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
                plugin.saveResource(name, true);
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

    // ── Convenience helpers ────────────────────────────────────────────────────

    public String getMessage(String path) {
        return messages.getString(path, "&cMessage not found: " + path);
    }

    public String getMessage(String path, String... placeholders) {
        String msg = getMessage(path);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        return msg;
    }

    public String getMessageOrDefault(String path, String fallback) {
        return messages.getString(path, fallback);
    }

    public String getMessageOrDefault(String path, String fallback, String... placeholders) {
        String msg = getMessageOrDefault(path, fallback);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        return msg;
    }

    public String getSound(String path) {
        return sounds.getString(path, "");
    }

    public boolean isCommandEnabled(String key) {
        return config.getBoolean("COMMANDS." + key, true);
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
}
