package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.FeatureManager;
import com.bx.ultimateDonutSmp.managers.OptimizationManager;
import com.bx.ultimateDonutSmp.managers.SpawnManager;
import com.bx.ultimateDonutSmp.managers.StatsWipeManager;
import com.bx.ultimateDonutSmp.menus.FeatureToggleMenu;
import com.bx.ultimateDonutSmp.menus.StatsWipeMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class UltimateDonutSmpCommand implements CommandExecutor, TabCompleter {

    private static final String RELOAD_PERMISSION = "ultimatedonutsmp.admin.reload";
    private static final String STATS_WIPE_PERMISSION = "ultimatedonutsmp.admin.statswipe";
    private static final String OPTIMIZE_PERMISSION = "ultimatedonutsmp.admin.optimize";
    private static final String SETUP_PERMISSION = "ultimatedonutsmp.admin.setup";
    private static final String FEATURES_PERMISSION = "ultimatedonutsmp.admin.features";
    private static final String DEFAULT_WEBHOOK_PLACEHOLDER = "https://discord.com/api/webhooks/your_webhook_here";
    private static final int COMMANDS_PER_PAGE = 8;

    private static final List<String> ROOT_COMPLETIONS = List.of("reload", "statswipe", "optimize", "setup", "features");
    private static final List<String> SETUP_COMPLETIONS = List.of("status", "apply", "setspawn", "setafk", "commands");
    private static final List<String> COMMAND_CATEGORIES = List.of("all", "starter", "economy", "market", "pvp", "staff", "admin", "setup");

    private final UltimateDonutSmp plugin;

    public UltimateDonutSmpCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender, label);
            case "statswipe" -> handleStatsWipe(sender, label, args);
            case "optimize", "optimization" -> handleOptimize(sender, label, args);
            case "setup" -> handleSetup(sender, label, args);
            case "features" -> handleFeatures(sender, label, args);
            default -> sendUsage(sender, label);
        }
        return true;
    }

    private void handleReload(CommandSender sender, String label) {
        if (!PermissionUtils.has(sender, RELOAD_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʀᴇʟᴏᴀᴅ ᴘʟᴜɢɪɴ ѕᴇᴛᴛɪɴɢѕ."));
            return;
        }

        try {
            plugin.reloadAllPluginConfigurations();
            sender.sendMessage(ColorUtils.toComponent("&aᴜʟᴛɪᴍᴀᴛᴇᴅᴏɴᴜᴛѕᴍᴘ ᴄᴏɴꜰɪɢᴜʀᴀᴛɪᴏɴ ʀᴇʟᴏᴀᴅᴇᴅ."));
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "ꜰᴀɪʟᴇᴅ ᴛᴏ ʀᴇʟᴏᴀᴅ ᴜʟᴛɪᴍᴀᴛᴇᴅᴏɴᴜᴛѕᴍᴘ ᴄᴏɴꜰɪɢᴜʀᴀᴛɪᴏɴ.", exception);
            sender.sendMessage(ColorUtils.toComponent("&cꜰᴀɪʟᴇᴅ ᴛᴏ ʀᴇʟᴏᴀᴅ ᴄᴏɴꜰɪɢᴜʀᴀᴛɪᴏɴ. ᴄʜᴇᴄᴋ ᴄᴏɴѕᴏʟᴇ ꜰᴏʀ ᴅᴇᴛᴀɪʟѕ."));
        }
    }

    private void handleStatsWipe(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, STATS_WIPE_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent(message("NO-PERMISSION",
                    "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴜѕᴇ ѕᴛᴀᴛѕ ᴡɪᴘᴇ.")));
            return;
        }

        if (args.length == 1 || isGuiAlias(args[1])) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtils.toComponent(message("PLAYER-ONLY-GUI",
                        "&cᴏᴘᴇɴ ᴛʜᴇ ѕᴛᴀᴛѕ ᴡɪᴘᴇ ɢᴜɪ ɪɴ-ɢᴀᴍᴇ, ᴏʀ ᴜѕᴇ /" + label + " ѕᴛᴀᴛѕᴡɪᴘᴇ <target> ᴄᴏɴꜰɪʀᴍ.")));
                return;
            }

            new StatsWipeMenu(plugin).open(player);
            return;
        }

        StatsWipeManager.WipeTarget target = StatsWipeManager.WipeTarget.fromInput(args[1]).orElse(null);
        if (target == null) {
            sender.sendMessage(ColorUtils.toComponent(message("INVALID-TARGET",
                    "&cɪɴᴠᴀʟɪᴅ ѕᴛᴀᴛѕ ᴡɪᴘᴇ ᴛᴀʀɢᴇᴛ. ᴀᴠᴀɪʟᴀʙʟᴇ: {targets}")
                    .replace("{targets}", availableTargets())));
            return;
        }

        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            sender.sendMessage(ColorUtils.toComponent(message("DIRECT-USAGE",
                    "&cᴜѕᴇ /" + label + " ѕᴛᴀᴛѕᴡɪᴘᴇ <target> ᴄᴏɴꜰɪʀᴍ ᴛᴏ ʀᴜɴ ᴅɪʀᴇᴄᴛʟʏ, ᴏʀ /" + label + " ѕᴛᴀᴛѕᴡɪᴘᴇ ᴛᴏ ᴏᴘᴇɴ ᴛʜᴇ ɢᴜɪ.")));
            return;
        }

        StatsWipeManager.WipeResult result = plugin.getStatsWipeManager().wipeTarget(target, sender.getName());
        if (result.busy()) {
            sender.sendMessage(ColorUtils.toComponent(message("BUSY", "&cᴀ ᴡɪᴘᴇ ɪѕ ᴀʟʀᴇᴀᴅʏ ɪɴ ᴘʀᴏɢʀᴇѕѕ.")));
            return;
        }
        if (!result.success()) {
            String error = result.errorMessage() == null || result.errorMessage().isBlank()
                    ? "ᴜɴᴋɴᴏᴡɴ ᴇʀʀᴏʀ"
                    : result.errorMessage();
            sender.sendMessage(ColorUtils.toComponent(message("FAILED",
                    "&cѕᴛᴀᴛѕ ᴡɪᴘᴇ ꜰᴀɪʟᴇᴅ: {error}")
                    .replace("{error}", error)));
            return;
        }

        sender.sendMessage(ColorUtils.toComponent(message("SUCCESS",
                "&aᴡɪᴘᴇ ᴄᴏᴍᴘʟᴇᴛᴇ: &f{target}&a. ᴀꜰꜰᴇᴄᴛᴇᴅ ʀᴇᴄᴏʀᴅѕ: &f{count}&a.")
                .replace("{target}", target.getDisplayName())
                .replace("{count}", String.valueOf(result.affectedCount(target)))));
    }

    private void handleOptimize(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, OPTIMIZE_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪꜱꜱɪᴏɴ ᴛᴏ ᴜꜱᴇ ᴏᴘᴛɪᴍɪᴢᴀᴛɪᴏɴ ᴛᴏᴏʟꜱ."));
            return;
        }

        OptimizationManager optimizationManager = plugin.getOptimizationManager();
        if (optimizationManager == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴏᴘᴛɪᴍɪᴢᴀᴛɪᴏɴ ᴍᴀɴᴀɢᴇʀ ɪѕ ɴᴏᴛ ᴀᴠᴀɪʟᴀʙʟᴇ."));
            return;
        }

        if (args.length == 1 || args[1].equalsIgnoreCase("status")) {
            sendOptimizationStatus(sender, label, optimizationManager);
            return;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                optimizationManager.reload();
                sender.sendMessage(ColorUtils.toComponent("&aᴏᴘᴛɪᴍɪᴢᴀᴛɪᴏɴ ѕᴇᴛᴛɪɴɢѕ ʀᴇʟᴏᴀᴅᴇᴅ."));
            }
            case "reset" -> {
                optimizationManager.resetStats();
                sender.sendMessage(ColorUtils.toComponent("&aᴏᴘᴛɪᴍɪᴢᴀᴛɪᴏɴ ʀᴜɴᴛɪᴍᴇ ᴄᴏᴜɴᴛᴇʀѕ ʀᴇѕᴇᴛ."));
            }
            default -> sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " ᴏᴘᴛɪᴍɪᴢᴇ [status|reload|reset]"));
        }
    }

    private void handleSetup(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, SETUP_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴜѕᴇ ѕᴇᴛᴜᴘ ᴛᴏᴏʟѕ."));
            return;
        }

        if (args.length == 1 || args[1].equalsIgnoreCase("status")) {
            sendSetupStatus(sender, label);
            return;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "apply" -> handleSetupApply(sender, label, args);
            case "setspawn" -> handleSetupLocation(sender, true);
            case "setafk" -> handleSetupLocation(sender, false);
            case "commands" -> handleSetupCommands(sender, label, args);
            default -> sendSetupUsage(sender, label);
        }
    }

    private void handleSetupApply(CommandSender sender, String label, String[] args) {
        if (args.length < 4 || !args[2].equalsIgnoreCase("single-paper") || !args[3].equalsIgnoreCase("confirm")) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " setup apply single-paper confirm"));
            return;
        }

        try {
            FileConfiguration config = plugin.getConfigManager().getConfig();
            FileConfiguration database = plugin.getConfigManager().getDatabase();
            FileConfiguration network = plugin.getConfigManager().getNetwork();
            FileConfiguration discord = plugin.getConfigManager().getDiscord();

            config.set("SETTINGS.SPAWN-MENU", true);
            config.set("SETTINGS.AFK-MENU", true);
            config.set("LUNAR-CLIENT.RICH-PRESENCE.ENABLED", false);

            database.set("DATABASE.TYPE", "SQLITE");
            database.set("DATABASE.SQLITE.FILE", "data/data.db");
            database.set("REDIS.ENABLED", false);

            network.set("NETWORK.LOCAL_SERVER_ID", "local");
            network.set("NETWORK.LOCAL_DISPLAY_NAME", "Local");
            network.set("NETWORK-STATUS.LOCAL-SERVER-ID", "local");
            network.set("NETWORK-STATUS.LOCAL-DISPLAY-NAME", "Local");
            network.set("NETWORK-STATUS.SERVERS", null);
            network.set("NETWORK-STATUS.SERVERS.local.DISPLAY", "Local");
            network.set("NETWORK-STATUS.SERVERS.local.SOURCE.TYPE", "LOCAL");

            discord.set("WEBHOOKS.ENABLED", false);

            plugin.saveConfig();
            boolean saved = plugin.getConfigManager().saveDatabase()
                    & plugin.getConfigManager().saveNetwork()
                    & plugin.getConfigManager().saveDiscord();
            if (!saved) {
                sender.sendMessage(ColorUtils.toComponent("&cѕɪɴɢʟᴇ-ᴘᴀᴘᴇʀ ᴘʀᴇѕᴇᴛ ᴡᴀѕ ᴀᴘᴘʟɪᴇᴅ ɪɴ ᴍᴇᴍᴏʀʏ, ʙᴜᴛ ᴏɴᴇ ᴏʀ ᴍᴏʀᴇ ꜰɪʟᴇѕ ꜰᴀɪʟᴇᴅ ᴛᴏ ѕᴀᴠᴇ. ᴄʜᴇᴄᴋ ᴄᴏɴѕᴏʟᴇ."));
                return;
            }

            plugin.reloadAllPluginConfigurations();
            sender.sendMessage(ColorUtils.toComponent("&aѕɪɴɢʟᴇ-ᴘᴀᴘᴇʀ ѕᴇᴛᴜᴘ ᴘʀᴇѕᴇᴛ ᴀᴘᴘʟɪᴇᴅ."));
            sender.sendMessage(ColorUtils.toComponent("&7ɴᴇxᴛ: ᴜѕᴇ &f/" + label + " setup setspawn &7ᴀɴᴅ &f/" + label + " setup setafk&7."));
            sender.sendMessage(ColorUtils.toComponent("&eʀᴇѕᴛᴀʀᴛ ᴛʜᴇ ѕᴇʀᴠᴇʀ ʙᴇꜰᴏʀᴇ ɢᴏɪɴɢ ʟɪᴠᴇ ѕᴏ ѕᴛᴏʀᴀɢᴇ ᴄʜᴀɴɢᴇѕ ᴀʀᴇ ꜰᴜʟʟʏ ᴀᴘᴘʟɪᴇᴅ."));
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to apply single-paper setup preset.", exception);
            sender.sendMessage(ColorUtils.toComponent("&cꜰᴀɪʟᴇᴅ ᴛᴏ ᴀᴘᴘʟʏ ѕᴇᴛᴜᴘ ᴘʀᴇѕᴇᴛ. ᴄʜᴇᴄᴋ ᴄᴏɴѕᴏʟᴇ ꜰᴏʀ ᴅᴇᴛᴀɪʟѕ."));
        }
    }

    private void handleSetupLocation(CommandSender sender, boolean spawn) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ѕᴀᴠᴇ ѕᴇᴛᴜᴘ ʟᴏᴄᴀᴛɪᴏɴѕ."));
            return;
        }

        Location location = player.getLocation();
        try {
            if (spawn) {
                SpawnManager.SetupLocationResult result = plugin.getSpawnManager().setSpawnLocation(location);
                if (!result.success()) {
                    plugin.getLogger().warning("Failed to save setup spawn location at "
                            + describeLocation(location) + ": " + result.message());
                    sender.sendMessage(ColorUtils.toComponent("&cꜰᴀɪʟᴇᴅ ᴛᴏ ѕᴀᴠᴇ ѕᴘᴀᴡɴ ʟᴏᴄᴀᴛɪᴏɴ: &f" + result.message()));
                    return;
                }
                sender.sendMessage(ColorUtils.toComponent("&aѕᴘᴀᴡɴ ʟᴏᴄᴀᴛɪᴏɴ ѕᴀᴠᴇᴅ ᴀᴛ &f" + describeLocation(location) + "&a."));
            } else {
                SpawnManager.SetupLocationResult result = plugin.getSpawnManager().setAfkLocation(location);
                if (!result.success()) {
                    plugin.getLogger().warning("Failed to save setup AFK location at "
                            + describeLocation(location) + ": " + result.message());
                    sender.sendMessage(ColorUtils.toComponent("&cꜰᴀɪʟᴇᴅ ᴛᴏ ѕᴀᴠᴇ ᴀꜰᴋ ʟᴏᴄᴀᴛɪᴏɴ: &f" + result.message()));
                    return;
                }
                sender.sendMessage(ColorUtils.toComponent("&aᴀꜰᴋ ʟᴏᴄᴀᴛɪᴏɴ ѕᴀᴠᴇᴅ ᴀᴛ &f" + describeLocation(location) + "&a."));
            }
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to save setup location at " + describeLocation(location), exception);
            sender.sendMessage(ColorUtils.toComponent("&cꜰᴀɪʟᴇᴅ ᴛᴏ ѕᴀᴠᴇ ʟᴏᴄᴀᴛɪᴏɴ. ᴄʜᴇᴄᴋ ᴄᴏɴѕᴏʟᴇ ꜰᴏʀ ᴅᴇᴛᴀɪʟѕ."));
        }
    }

    private void sendSetupStatus(CommandSender sender, String label) {
        FileConfiguration database = plugin.getConfigManager().getDatabase();
        FileConfiguration discord = plugin.getConfigManager().getDiscord();

        sender.sendMessage(ColorUtils.toComponent("&8&m---------- &bᴜᴅѕ ѕᴇᴛᴜᴘ ѕᴛᴀᴛᴜѕ &8&m----------"));
        sendCheck(sender, isJava21OrNewer(), "ᴊᴀᴠᴀ 21+", System.getProperty("java.version", "ᴜɴᴋɴᴏᴡɴ"));
        sendCheck(sender, isFoliaOrPaperLikeServer(), "ꜰᴏʟɪᴀ/ᴘᴀᴘᴇʀ ѕᴇʀᴠᴇʀ", plugin.getServer().getName());
        sendCheck(sender, isDatabaseConnected(), "ᴅᴀᴛᴀʙᴀѕᴇ", database.getString("DATABASE.TYPE", "SQLITE"));
        sendCheck(sender, isRedisReadyForSingleServer(database), "ʀᴇᴅɪѕ", redisDetail(database));
        sendCheck(sender, isDiscordWebhookReady(discord), "ᴅɪѕᴄᴏʀᴅ ᴡᴇʙʜᴏᴏᴋ", discordWebhookDetail(discord));
        sendCheck(sender, plugin.getSpawnManager().hasSpawn(), "ѕᴘᴀᴡɴ", "ᴜѕᴇ /" + label + " setup setspawn");
        sendCheck(sender, plugin.getSpawnManager().hasAfk(), "ᴀꜰᴋ", "ᴜѕᴇ /" + label + " setup setafk");
        sendCheck(sender, !availableRtpWorlds().isEmpty(), "ʀᴛᴘ ᴡᴏʀʟᴅѕ", String.join(", ", availableRtpWorlds()));
        sendCheck(sender, true, "ᴏᴘᴛɪᴏɴᴀʟ ɪɴᴛᴇɢʀᴀᴛɪᴏɴѕ", optionalIntegrationDetail());
        sender.sendMessage(ColorUtils.toComponent("&7ᴘʀᴇѕᴇᴛ: &f/" + label + " setup apply single-paper confirm"));
        sender.sendMessage(ColorUtils.toComponent("&7ᴄᴏᴍᴍᴀɴᴅѕ: &f/" + label + " setup commands [category] [page]"));
    }

    private void handleSetupCommands(CommandSender sender, String label, String[] args) {
        String category = args.length >= 3 ? args[2].toLowerCase(Locale.ROOT) : "all";
        if (!COMMAND_CATEGORIES.contains(category)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜɴᴋɴᴏᴡɴ ᴄᴏᴍᴍᴀɴᴅ ᴄᴀᴛᴇɢᴏʀʏ. ᴀᴠᴀɪʟᴀʙʟᴇ: &f" + String.join(", ", COMMAND_CATEGORIES)));
            return;
        }

        int page = args.length >= 4 ? parsePage(args[3]) : 1;
        List<CommandEntry> entries = commandEntries(category, label);
        if (entries.isEmpty()) {
            sender.sendMessage(ColorUtils.toComponent("&cɴᴏ ᴄᴏᴍᴍᴀɴᴅѕ ꜰᴏᴜɴᴅ ꜰᴏʀ ᴄᴀᴛᴇɢᴏʀʏ: &f" + category));
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) COMMANDS_PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));
        int start = (page - 1) * COMMANDS_PER_PAGE;
        int end = Math.min(entries.size(), start + COMMANDS_PER_PAGE);

        sender.sendMessage(ColorUtils.toComponent("&8&m---------- &bᴜᴅѕ ᴄᴏᴍᴍᴀɴᴅѕ &8(&f" + category + " " + page + "/" + totalPages + "&8) &8&m----------"));
        for (int index = start; index < end; index++) {
            CommandEntry entry = entries.get(index);
            sender.sendMessage(ColorUtils.toComponent("&f" + entry.usage() + " &8- &7" + entry.description()));
        }
        sender.sendMessage(ColorUtils.toComponent("&7ᴄᴀᴛᴇɢᴏʀɪᴇѕ: &f" + String.join("&7, &f", COMMAND_CATEGORIES)));
    }

    private List<CommandEntry> commandEntries(String category, String label) {
        if (category.equals("setup")) {
            return List.of(
                    new CommandEntry("/" + label + " setup [status]", "ѕʜᴏᴡ ѕᴇᴛᴜᴘ ᴄʜᴇᴄᴋʟɪѕᴛ"),
                    new CommandEntry("/" + label + " setup apply single-paper confirm", "ᴀᴘᴘʟʏ ᴛʜᴇ ѕɪɴɢʟᴇ-ѕᴇʀᴠᴇʀ ᴘʀᴇѕᴇᴛ"),
                    new CommandEntry("/" + label + " setup setspawn", "ѕᴀᴠᴇ ʏᴏᴜʀ ᴄᴜʀʀᴇɴᴛ ʟᴏᴄᴀᴛɪᴏɴ ᴀѕ ѕᴘᴀᴡɴ"),
                    new CommandEntry("/" + label + " setup setafk", "ѕᴀᴠᴇ ʏᴏᴜʀ ᴄᴜʀʀᴇɴᴛ ʟᴏᴄᴀᴛɪᴏɴ ᴀѕ ᴀꜰᴋ"),
                    new CommandEntry("/" + label + " setup commands [category] [page]", "ʙʀᴏᴡѕᴇ ᴄᴏᴍᴍᴀɴᴅ ᴜѕᴀɢᴇ")
            );
        }

        Map<String, Map<String, Object>> commandMap = plugin.getDescription().getCommands();
        List<String> commandNames = commandNamesForCategory(category, commandMap.keySet());
        List<CommandEntry> entries = new ArrayList<>();
        for (String commandName : commandNames) {
            Map<String, Object> details = commandMap.get(commandName);
            if (details == null || !plugin.getFeatureManager().isCommandFeatureEnabled(commandName)) {
                continue;
            }

            String usage = String.valueOf(details.getOrDefault("usage", "/" + commandName));
            String description = String.valueOf(details.getOrDefault("description", "ɴᴏ ᴅᴇѕᴄʀɪᴘᴛɪᴏɴ"));
            String aliases = formatAliases(details.get("aliases"));
            if (!aliases.isBlank()) {
                description += " (ᴀʟɪᴀѕᴇѕ: " + aliases + ")";
            }
            entries.add(new CommandEntry(usage, description));
        }
        return entries;
    }

    private List<String> commandNamesForCategory(String category, Collection<String> allCommandNames) {
        if (category.equals("all")) {
            return new ArrayList<>(allCommandNames);
        }

        return switch (category) {
            case "starter" -> List.of(
                    "spawn", "afk", "home", "homes", "sethome", "delhome", "renamehome",
                    "rtp", "warp", "tpa", "tpahere", "tpaccept", "tpadeny", "tpacancel",
                    "settings", "stats", "ping", "playtime", "social", "discord", "twitter",
                    "store", "rules", "help", "servers"
            );
            case "economy" -> List.of(
                    "balance", "pay", "addmoney", "removemoney", "setmoney", "shards", "shardpay",
                    "addshards", "removeshards", "setshards",
                    "shop", "sell", "sellhand", "sellall", "sellhistory", "worth"
            );
            case "market" -> List.of(
                    "auctionhouse", "orders", "billford", "bounty", "crate", "crates", "keys", "spawner"
            );
            case "pvp" -> List.of(
                    "duel", "queue", "leave", "draw", "arena", "ffa", "ffastats", "ffaarena"
            );
            case "staff" -> List.of(
                    "staffmode", "stafflist", "staffchat", "helpop", "report", "freeze", "fly",
                    "heal", "feed", "gamemode", "randomteleport", "teleport", "alts", "vanish",
                    "invsee", "profileviewer", "punishments", "ban", "tempban", "mute", "tempmute",
                    "warn", "kick", "blacklist", "unban", "unmute", "unblacklist", "findplayer", "rename"
            );
            case "admin" -> List.of(
                    "ultimatedonutsmp", "clearlag", "cuboid", "setwarp", "delwarp", "warpmanager",
                    "portalmanager", "amethysttool", "arena", "ffaarena", "crate", "spawner",
                    "shop", "orders", "auctionhouse", "enderchest", "freeze", "staffmode", "invsee", "worth"
            );
            default -> List.of();
        };
    }

    private String formatAliases(Object rawAliases) {
        if (rawAliases instanceof Collection<?> aliases) {
            return aliases.stream()
                    .map(String::valueOf)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
        }
        if (rawAliases instanceof String alias && !alias.isBlank()) {
            return alias;
        }
        return "";
    }

    private void sendCheck(CommandSender sender, boolean ok, String label, String detail) {
        String state = ok ? "&aᴏᴋ" : "&eᴄʜᴇᴄᴋ";
        String value = detail == null || detail.isBlank() ? "-" : detail;
        sender.sendMessage(ColorUtils.toComponent("&8- " + state + " &f" + label + " &8- &7" + value));
    }

    private boolean isJava21OrNewer() {
        String version = System.getProperty("java.version", "");
        String normalized = version.startsWith("1.") ? version.substring(2) : version;
        int dotIndex = normalized.indexOf('.');
        String major = dotIndex >= 0 ? normalized.substring(0, dotIndex) : normalized;
        try {
            return Integer.parseInt(major) >= 21;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private boolean isFoliaOrPaperLikeServer() {
        String serverName = plugin.getServer().getName().toLowerCase(Locale.ROOT);
        return serverName.contains("folia")
                || serverName.contains("paper")
                || serverName.contains("purpur")
                || serverName.contains("pufferfish");
    }

    private boolean isDatabaseConnected() {
        Connection connection = plugin.getDatabaseManager() == null ? null : plugin.getDatabaseManager().getConnection();
        if (connection == null) {
            return false;
        }

        try {
            return !connection.isClosed();
        } catch (SQLException ignored) {
            return false;
        }
    }

    private boolean isRedisReadyForSingleServer(FileConfiguration database) {
        return !database.getBoolean("REDIS.ENABLED", false)
                || (plugin.getRedisManager() != null && plugin.getRedisManager().isConnected());
    }

    private String redisDetail(FileConfiguration database) {
        if (!database.getBoolean("REDIS.ENABLED", false)) {
            return "ᴅɪѕᴀʙʟᴇᴅ ꜰᴏʀ ѕɪɴɢʟᴇ-ѕᴇʀᴠᴇʀ ѕᴇᴛᴜᴘ";
        }
        return plugin.getRedisManager() != null && plugin.getRedisManager().isConnected()
                ? "ᴇɴᴀʙʟᴇᴅ ᴀɴᴅ ᴄᴏɴɴᴇᴄᴛᴇᴅ"
                : "ᴇɴᴀʙʟᴇᴅ ʙᴜᴛ ɴᴏᴛ ᴄᴏɴɴᴇᴄᴛᴇᴅ";
    }

    private boolean isDiscordWebhookReady(FileConfiguration discord) {
        ConfigurationSection root = discord.getConfigurationSection("WEBHOOKS");
        if (root == null || !root.getBoolean("ENABLED", true)) {
            return true;
        }

        String url = root.getString("URL", "");
        return !url.isBlank() && !DEFAULT_WEBHOOK_PLACEHOLDER.equalsIgnoreCase(url);
    }

    private String discordWebhookDetail(FileConfiguration discord) {
        ConfigurationSection root = discord.getConfigurationSection("WEBHOOKS");
        if (root == null || !root.getBoolean("ENABLED", true)) {
            return "ᴅɪѕᴀʙʟᴇᴅ";
        }

        String url = root.getString("URL", "");
        if (url.isBlank() || DEFAULT_WEBHOOK_PLACEHOLDER.equalsIgnoreCase(url)) {
            return "ᴇɴᴀʙʟᴇᴅ ᴡɪᴛʜ ᴘʟᴀᴄᴇʜᴏʟᴅᴇʀ ᴜʀʟ";
        }
        return "ᴇɴᴀʙʟᴇᴅ";
    }

    private List<String> availableRtpWorlds() {
        ConfigurationSection worlds = plugin.getConfigManager().getRtp().getConfigurationSection("WORLD-SETTINGS");
        if (worlds == null) {
            return List.of();
        }

        List<String> loaded = new ArrayList<>();
        for (String worldName : worlds.getKeys(false)) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                loaded.add(world.getName());
            }
        }
        loaded.sort(String.CASE_INSENSITIVE_ORDER);
        return loaded;
    }

    private String optionalIntegrationDetail() {
        List<String> installed = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String pluginName : List.of("PlaceholderAPI", "LuckPerms", "Vault", "ProtocolLib", "Apollo", "NickPlus")) {
            if (plugin.getServer().getPluginManager().getPlugin(pluginName) == null) {
                missing.add(pluginName);
            } else {
                installed.add(pluginName);
            }
        }

        String installedText = installed.isEmpty() ? "ɴᴏɴᴇ ɪɴѕᴛᴀʟʟᴇᴅ" : "ɪɴѕᴛᴀʟʟᴇᴅ: " + String.join(", ", installed);
        if (missing.isEmpty()) {
            return installedText;
        }
        return installedText + "; ᴏᴘᴛɪᴏɴᴀʟ ᴍɪѕѕɪɴɢ: " + String.join(", ", missing);
    }

    private String describeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return "ᴜɴᴋɴᴏᴡɴ";
        }
        return location.getWorld().getName()
                + " " + location.getBlockX()
                + ", " + location.getBlockY()
                + ", " + location.getBlockZ();
    }

    private int parsePage(String raw) {
        try {
            return Math.max(1, Integer.parseInt(raw));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private void handleFeatures(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, FEATURES_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "FEATURES.NO-PERMISSION",
                            "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪꜱꜱɪᴏɴ ᴛᴏ ᴍᴀɴᴀɢᴇ ꜰᴇᴀᴛᴜʀᴇ ᴛᴏɢɢʟᴇꜱ."
                    )
            ));
            return;
        }

        if (args.length == 1 || (args.length >= 2 && isGuiAlias(args[1]))) {
            if (sender instanceof Player player) {
                new FeatureToggleMenu(plugin).open(player);
                return;
            }
            sendFeatureList(sender);
            return;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        if (action.equals("list")) {
            sendFeatureList(sender);
            return;
        }

        if (!action.equals("toggle") && !action.equals("enable") && !action.equals("disable")) {
            sendFeatureUsage(sender, label);
            return;
        }

        if (args.length < 3) {
            sendFeatureUsage(sender, label);
            return;
        }

        FeatureManager.Feature feature = FeatureManager.Feature.fromInput(args[2]).orElse(null);
        if (feature == null) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "FEATURES.UNKNOWN",
                            "&cᴜɴᴋɴᴏᴡɴ ꜰᴇᴀᴛᴜʀᴇ: &f{feature}",
                            "{feature}", args[2]
                    )
            ));
            return;
        }

        boolean success;
        if (action.equals("toggle")) {
            success = plugin.getFeatureManager().toggle(feature);
        } else {
            success = plugin.getFeatureManager().setEnabled(feature, action.equals("enable"));
        }

        if (!success) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "FEATURES.TOGGLE-FAILED",
                            "&cꜰᴀɪʟᴇᴅ ᴛᴏ ᴜᴘᴅᴀᴛᴇ {feature}.",
                            "{feature}", feature.displayName(),
                            "{feature_key}", feature.configKey()
                    )
            ));
            return;
        }

        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault(
                        "FEATURES.TOGGLED",
                        "&a{feature} ɪꜱ ɴᴏᴡ {state}.",
                        "{feature}", feature.displayName(),
                        "{feature_key}", feature.configKey(),
                        "{state}", plugin.getFeatureManager().statusText(feature)
                )
        ));
    }

    private void sendFeatureUsage(CommandSender sender, String label) {
        sender.sendMessage(ColorUtils.toComponent("&cᴜꜱᴀɢᴇ: /" + label + " features [list|toggle|enable|disable] [feature]"));
    }

    private void sendFeatureList(CommandSender sender) {
        sender.sendMessage(ColorUtils.toComponent("&8&m---------- &bꜰᴇᴀᴛᴜʀᴇ ᴛᴏɢɢʟᴇꜱ &8&m----------"));
        for (FeatureManager.Feature feature : plugin.getFeatureManager().getFeatures()) {
            sender.sendMessage(ColorUtils.toComponent("&f" + feature.configKey()
                    + " &8- " + plugin.getFeatureManager().statusText(feature)
                    + " &8- &7" + feature.displayName()));
        }
    }

    private void sendOptimizationStatus(
            CommandSender sender,
            String label,
            OptimizationManager optimizationManager
    ) {
        sender.sendMessage(ColorUtils.toComponent("&8&m---------- &bᴏᴘᴛɪᴍɪᴢᴀᴛɪᴏɴ &8&m----------"));
        sender.sendMessage(ColorUtils.toComponent("&7ᴇɴᴀʙʟᴇᴅ: &f" + optimizationManager.isEnabled()
                + " &8| &7ѕᴛᴀᴛᴇ: " + optimizationManager.getLoadState().display()));
        sender.sendMessage(ColorUtils.toComponent("&7ᴛᴘѕ: &f" + optimizationManager.formatMetric(optimizationManager.getLastTps())
                + " &8| &7ᴍѕᴘᴛ: &f" + optimizationManager.formatMetric(optimizationManager.getLastMspt())));
        sender.sendMessage(ColorUtils.toComponent("&7ᴍᴇᴍᴏʀʏ: &f" + optimizationManager.getUsedMemoryMb()
                + "ᴍʙ&8/&f" + optimizationManager.getMaxMemoryMb() + "MB"));
        sender.sendMessage(ColorUtils.toComponent("&7ѕᴋɪᴘᴘᴇᴅ ᴛᴀѕᴋ ʀᴜɴѕ: &f"
                + optimizationManager.getTotalSkippedRuns()
                + " &8(&7ѕᴄᴏʀᴇʙᴏᴀʀᴅ=&f" + optimizationManager.getSkippedRuns(OptimizationManager.OptimizedTask.SCOREBOARD)
                + "&8, &7ᴛᴀʙʟɪѕᴛ=&f" + optimizationManager.getSkippedRuns(OptimizationManager.OptimizedTask.TABLIST)
                + "&8, &7ʟᴜɴᴀʀ=&f" + optimizationManager.getSkippedRuns(OptimizationManager.OptimizedTask.LUNAR_TEAMMATES)
                + "&8)"));
        sender.sendMessage(ColorUtils.toComponent("&7ᴜѕᴀɢᴇ: &f/" + label + " ᴏᴘᴛɪᴍɪᴢᴇ [status|reload|reset]"));
    }

    private void sendUsage(CommandSender sender, String label) {
        if (ROOT_COMPLETIONS.contains("setup")) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " <reload|statswipe|optimize|setup|features>"));
            return;
        }
        if (label != null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜꜱᴀɢᴇ: /" + label + " <reload|statswipe|optimize|features>"));
            return;
        }
        sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " <reload|statswipe|optimize>"));
    }

    private void sendSetupUsage(CommandSender sender, String label) {
        sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " setup <status|apply|setspawn|setafk|commands>"));
    }

    private String message(String key, String fallback) {
        return plugin.getConfigManager().getMessages().getString("STATS-WIPE." + key, fallback);
    }

    private boolean isGuiAlias(String input) {
        return input.equalsIgnoreCase("menu") || input.equalsIgnoreCase("gui");
    }

    private String availableTargets() {
        return Arrays.stream(StatsWipeManager.WipeTarget.values())
                .map(StatsWipeManager.WipeTarget::getDisplayName)
                .reduce((left, right) -> left + ", " + right)
                .orElse("ᴘʟᴀʏᴇʀ ѕᴛᴀᴛѕ");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return partialMatches(args[0], ROOT_COMPLETIONS);
        }

        String root = args[0].toLowerCase(Locale.ROOT);
        if (root.equals("optimize") || root.equals("optimization")) {
            return args.length == 2 ? partialMatches(args[1], List.of("status", "reload", "reset")) : List.of();
        }

        if (root.equals("features")) {
            return completeFeatures(args);
        }

        if (root.equals("statswipe")) {
            if (args.length == 2) {
                List<String> targets = Arrays.stream(StatsWipeManager.WipeTarget.values())
                        .map(target -> target.getDisplayName().toLowerCase(Locale.ROOT).replace(" ", "-"))
                        .toList();
                return partialMatches(args[1], targets);
            }
            return args.length == 3 ? partialMatches(args[2], List.of("confirm")) : List.of();
        }

        if (!root.equals("setup")) {
            return List.of();
        }

        if (args.length == 2) {
            return partialMatches(args[1], SETUP_COMPLETIONS);
        }

        String setupSub = args[1].toLowerCase(Locale.ROOT);
        if (setupSub.equals("apply")) {
            if (args.length == 3) {
                return partialMatches(args[2], List.of("single-paper"));
            }
            if (args.length == 4 && args[2].equalsIgnoreCase("single-paper")) {
                return partialMatches(args[3], List.of("confirm"));
            }
        }

        if (setupSub.equals("commands")) {
            if (args.length == 3) {
                return partialMatches(args[2], COMMAND_CATEGORIES);
            }
            if (args.length == 4) {
                return partialMatches(args[3], List.of("1", "2", "3", "4", "5"));
            }
        }

        return List.of();
    }

    private List<String> completeFeatures(String[] args) {
        if (args.length == 2) {
            return partialMatches(args[1], List.of("list", "toggle", "enable", "disable"));
        }

        if (args.length == 3 && List.of("toggle", "enable", "disable").contains(args[1].toLowerCase(Locale.ROOT))) {
            List<String> featureKeys = plugin.getFeatureManager().getFeatures().stream()
                    .map(feature -> feature.configKey().toLowerCase(Locale.ROOT).replace('_', '-'))
                    .toList();
            return partialMatches(args[2], featureKeys);
        }

        return List.of();
    }

    private List<String> partialMatches(String input, Collection<String> options) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(normalized))
                .toList();
    }

    private record CommandEntry(String usage, String description) {
    }
}
