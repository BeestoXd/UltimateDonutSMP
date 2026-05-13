package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OptimizationManager;
import com.bx.ultimateDonutSmp.managers.StatsWipeManager;
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
    private static final String DEFAULT_WEBHOOK_PLACEHOLDER = "https://discord.com/api/webhooks/your_webhook_here";
    private static final int COMMANDS_PER_PAGE = 8;

    private static final List<String> ROOT_COMPLETIONS = List.of("reload", "statswipe", "optimize", "setup");
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

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> handleReload(sender);
            case "statswipe" -> handleStatsWipe(sender, label, args);
            case "optimize", "optimization" -> handleOptimize(sender, label, args);
            case "setup" -> handleSetup(sender, label, args);
            default -> sendUsage(sender, label);
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to reload plugin settings."));
            return;
        }

        try {
            plugin.reloadAllPluginConfigurations();
            sender.sendMessage(ColorUtils.toComponent("&aUltimateDonutSmp configuration reloaded."));
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload UltimateDonutSmp configuration.", exception);
            sender.sendMessage(ColorUtils.toComponent("&cFailed to reload configuration. Check console for details."));
        }
    }

    private void handleStatsWipe(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(STATS_WIPE_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent(message("NO-PERMISSION",
                    "&cYou do not have permission to use Stats Wipe.")));
            return;
        }

        if (args.length == 1 || isGuiAlias(args[1])) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtils.toComponent(message("PLAYER-ONLY-GUI",
                        "&cOpen the Stats Wipe GUI in-game, or use /" + label + " statswipe <target> confirm.")));
                return;
            }

            new StatsWipeMenu(plugin).open(player);
            return;
        }

        StatsWipeManager.WipeTarget target = StatsWipeManager.WipeTarget.fromInput(args[1]).orElse(null);
        if (target == null) {
            sender.sendMessage(ColorUtils.toComponent(message("INVALID-TARGET",
                    "&cInvalid Stats Wipe target. Available: {targets}")
                    .replace("{targets}", availableTargets())));
            return;
        }

        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            sender.sendMessage(ColorUtils.toComponent(message("DIRECT-USAGE",
                    "&cUse /" + label + " statswipe <target> confirm to run directly, or /" + label + " statswipe to open the GUI.")));
            return;
        }

        StatsWipeManager.WipeResult result = plugin.getStatsWipeManager().wipeTarget(target, sender.getName());
        if (result.busy()) {
            sender.sendMessage(ColorUtils.toComponent(message("BUSY", "&cA wipe is already in progress.")));
            return;
        }
        if (!result.success()) {
            String error = result.errorMessage() == null || result.errorMessage().isBlank()
                    ? "Unknown error"
                    : result.errorMessage();
            sender.sendMessage(ColorUtils.toComponent(message("FAILED",
                    "&cStats Wipe failed: {error}")
                    .replace("{error}", error)));
            return;
        }

        sender.sendMessage(ColorUtils.toComponent(message("SUCCESS",
                "&aWipe complete: &f{target}&a. Affected records: &f{count}&a.")
                .replace("{target}", target.getDisplayName())
                .replace("{count}", String.valueOf(result.affectedCount(target)))));
    }

    private void handleOptimize(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(OPTIMIZE_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to use optimization tools."));
            return;
        }

        OptimizationManager optimizationManager = plugin.getOptimizationManager();
        if (optimizationManager == null) {
            sender.sendMessage(ColorUtils.toComponent("&cOptimization manager is not available."));
            return;
        }

        if (args.length == 1 || args[1].equalsIgnoreCase("status")) {
            sendOptimizationStatus(sender, label, optimizationManager);
            return;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                optimizationManager.reload();
                sender.sendMessage(ColorUtils.toComponent("&aOptimization settings reloaded."));
            }
            case "reset" -> {
                optimizationManager.resetStats();
                sender.sendMessage(ColorUtils.toComponent("&aOptimization runtime counters reset."));
            }
            default -> sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " optimize [status|reload|reset]"));
        }
    }

    private void handleSetup(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(SETUP_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to use setup tools."));
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
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " setup apply single-paper confirm"));
            return;
        }

        try {
            FileConfiguration config = plugin.getConfigManager().getConfig();
            FileConfiguration database = plugin.getConfigManager().getDatabase();
            FileConfiguration network = plugin.getConfigManager().getNetwork();
            FileConfiguration discord = plugin.getConfigManager().getDiscord();

            config.set("SETTINGS.SPAWN-MENU", false);
            config.set("SETTINGS.AFK-MENU", false);
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
                sender.sendMessage(ColorUtils.toComponent("&cSingle Paper preset was applied in memory, but one or more files failed to save. Check console."));
                return;
            }

            plugin.reloadAllPluginConfigurations();
            sender.sendMessage(ColorUtils.toComponent("&aSingle Paper setup preset applied."));
            sender.sendMessage(ColorUtils.toComponent("&7Next: use &f/" + label + " setup setspawn &7and &f/" + label + " setup setafk&7."));
            sender.sendMessage(ColorUtils.toComponent("&eRestart the server before going live so storage changes are fully applied."));
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to apply Single Paper setup preset.", exception);
            sender.sendMessage(ColorUtils.toComponent("&cFailed to apply setup preset. Check console for details."));
        }
    }

    private void handleSetupLocation(CommandSender sender, boolean spawn) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can save setup locations."));
            return;
        }

        Location location = player.getLocation();
        if (spawn) {
            if (!plugin.getSpawnManager().setSpawnLocation(location)) {
                plugin.getLogger().warning("Failed to save setup spawn location at " + describeLocation(location) + ".");
                sender.sendMessage(ColorUtils.toComponent("&cSpawn location could not be saved. Check console for details."));
                return;
            }
            sender.sendMessage(ColorUtils.toComponent("&aSpawn location saved at &f" + describeLocation(location) + "&a."));
            return;
        }

        if (!plugin.getSpawnManager().setAfkLocation(location)) {
            plugin.getLogger().warning("Failed to save setup AFK location at " + describeLocation(location) + ".");
            sender.sendMessage(ColorUtils.toComponent("&cAFK location could not be saved. Check console for details."));
            return;
        }
        sender.sendMessage(ColorUtils.toComponent("&aAFK location saved at &f" + describeLocation(location) + "&a."));
    }

    private void sendSetupStatus(CommandSender sender, String label) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        FileConfiguration database = plugin.getConfigManager().getDatabase();
        FileConfiguration discord = plugin.getConfigManager().getDiscord();

        sender.sendMessage(ColorUtils.toComponent("&8&m---------- &bUltimateDonutSmp Setup &8&m----------"));
        sendCheck(sender, isJava21OrNewer(), "Java",
                System.getProperty("java.version", "unknown") + " (Java 21+ required)");
        sendCheck(sender, isCompatibleServer(), "Server",
                plugin.getServer().getName() + " " + plugin.getServer().getBukkitVersion());
        sendCheck(sender, isDatabaseConnected(), "Storage",
                database.getString("DATABASE.TYPE", "SQLITE").toUpperCase(Locale.ROOT) + " configured");
        sendCheck(sender, isRedisReadyForSingleServer(database), "Redis",
                redisDetail(database));
        sendCheck(sender, isDiscordWebhookReady(discord), "Discord webhook",
                discordWebhookDetail(discord));
        sendCheck(sender, plugin.getSpawnManager().hasSpawn(), "Spawn",
                plugin.getSpawnManager().hasSpawn() ? "ready" : "use /" + label + " setup setspawn");
        sendCheck(sender, plugin.getSpawnManager().hasAfk(), "AFK",
                plugin.getSpawnManager().hasAfk() ? "ready" : "use /" + label + " setup setafk");
        List<String> rtpWorlds = availableRtpWorlds();
        sendCheck(sender, config.getBoolean("COMMANDS.RTP", true)
                        && plugin.getConfigManager().getRtp().getBoolean("ENABLED", true)
                        && !rtpWorlds.isEmpty(),
                "RTP worlds",
                rtpWorlds.isEmpty() ? "no configured RTP worlds are loaded" : String.join(", ", rtpWorlds));
        sendCheck(sender, true, "Optional integrations", optionalIntegrationDetail());
        sender.sendMessage(ColorUtils.toComponent("&7Preset: &f/" + label + " setup apply single-paper confirm"));
        sender.sendMessage(ColorUtils.toComponent("&7Commands: &f/" + label + " setup commands [category] [page]"));
    }

    private void handleSetupCommands(CommandSender sender, String label, String[] args) {
        String category = args.length >= 3 ? args[2].toLowerCase(Locale.ROOT) : "all";
        if (!COMMAND_CATEGORIES.contains(category)) {
            sender.sendMessage(ColorUtils.toComponent("&cUnknown command category. Available: &f" + String.join(", ", COMMAND_CATEGORIES)));
            return;
        }

        int page = args.length >= 4 ? parsePage(args[3]) : 1;
        List<CommandEntry> entries = commandEntries(category, label);
        if (entries.isEmpty()) {
            sender.sendMessage(ColorUtils.toComponent("&cNo commands found for category: &f" + category));
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) COMMANDS_PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));
        int start = (page - 1) * COMMANDS_PER_PAGE;
        int end = Math.min(entries.size(), start + COMMANDS_PER_PAGE);

        sender.sendMessage(ColorUtils.toComponent("&8&m---------- &bUDS Commands &8(&f" + category + " " + page + "/" + totalPages + "&8) &8&m----------"));
        for (int index = start; index < end; index++) {
            CommandEntry entry = entries.get(index);
            sender.sendMessage(ColorUtils.toComponent("&f" + entry.usage() + " &8- &7" + entry.description()));
        }
        sender.sendMessage(ColorUtils.toComponent("&7Categories: &f" + String.join("&7, &f", COMMAND_CATEGORIES)));
    }

    private List<CommandEntry> commandEntries(String category, String label) {
        if (category.equals("setup")) {
            return List.of(
                    new CommandEntry("/" + label + " setup [status]", "Show setup checklist"),
                    new CommandEntry("/" + label + " setup apply single-paper confirm", "Apply the single Paper preset"),
                    new CommandEntry("/" + label + " setup setspawn", "Save your current location as spawn"),
                    new CommandEntry("/" + label + " setup setafk", "Save your current location as AFK"),
                    new CommandEntry("/" + label + " setup commands [category] [page]", "Browse command usage")
            );
        }

        Map<String, Map<String, Object>> commandMap = plugin.getDescription().getCommands();
        List<String> commandNames = commandNamesForCategory(category, commandMap.keySet());
        List<CommandEntry> entries = new ArrayList<>();
        for (String commandName : commandNames) {
            Map<String, Object> details = commandMap.get(commandName);
            if (details == null) {
                continue;
            }

            String usage = String.valueOf(details.getOrDefault("usage", "/" + commandName));
            String description = String.valueOf(details.getOrDefault("description", "No description"));
            String aliases = formatAliases(details.get("aliases"));
            if (!aliases.isBlank()) {
                description += " (aliases: " + aliases + ")";
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
        String state = ok ? "&aOK" : "&eCHECK";
        sender.sendMessage(ColorUtils.toComponent("&8- " + state + " &f" + label + " &8- &7" + detail));
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

    private boolean isCompatibleServer() {
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
            return "disabled for single-server setup";
        }
        return plugin.getRedisManager() != null && plugin.getRedisManager().isConnected()
                ? "enabled and connected"
                : "enabled but not connected";
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
            return "disabled";
        }

        String url = root.getString("URL", "");
        if (url.isBlank() || DEFAULT_WEBHOOK_PLACEHOLDER.equalsIgnoreCase(url)) {
            return "enabled with placeholder URL";
        }
        return "enabled";
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

        String installedText = installed.isEmpty() ? "none installed" : "installed: " + String.join(", ", installed);
        if (missing.isEmpty()) {
            return installedText;
        }
        return installedText + "; optional missing: " + String.join(", ", missing);
    }

    private String describeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return "unknown";
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

    private void sendOptimizationStatus(
            CommandSender sender,
            String label,
            OptimizationManager optimizationManager
    ) {
        sender.sendMessage(ColorUtils.toComponent("&8&m---------- &bOptimization &8&m----------"));
        sender.sendMessage(ColorUtils.toComponent("&7Enabled: &f" + optimizationManager.isEnabled()
                + " &8| &7State: " + optimizationManager.getLoadState().display()));
        sender.sendMessage(ColorUtils.toComponent("&7TPS: &f" + optimizationManager.formatMetric(optimizationManager.getLastTps())
                + " &8| &7MSPT: &f" + optimizationManager.formatMetric(optimizationManager.getLastMspt())));
        sender.sendMessage(ColorUtils.toComponent("&7Memory: &f" + optimizationManager.getUsedMemoryMb()
                + "MB&8/&f" + optimizationManager.getMaxMemoryMb() + "MB"));
        sender.sendMessage(ColorUtils.toComponent("&7Skipped task runs: &f"
                + optimizationManager.getTotalSkippedRuns()
                + " &8(&7scoreboard=&f" + optimizationManager.getSkippedRuns(OptimizationManager.OptimizedTask.SCOREBOARD)
                + "&8, &7tablist=&f" + optimizationManager.getSkippedRuns(OptimizationManager.OptimizedTask.TABLIST)
                + "&8, &7lunar=&f" + optimizationManager.getSkippedRuns(OptimizationManager.OptimizedTask.LUNAR_TEAMMATES)
                + "&8)"));
        sender.sendMessage(ColorUtils.toComponent("&7Usage: &f/" + label + " optimize [status|reload|reset]"));
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <reload|statswipe|optimize|setup>"));
    }

    private void sendSetupUsage(CommandSender sender, String label) {
        sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " setup <status|apply|setspawn|setafk|commands>"));
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
                .orElse("Player Stats");
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

    private List<String> partialMatches(String input, Collection<String> options) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(normalized))
                .toList();
    }

    private record CommandEntry(String usage, String description) {
    }
}
