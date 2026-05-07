package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.OptimizationManager;
import com.bx.ultimateDonutSmp.managers.StatsWipeManager;
import com.bx.ultimateDonutSmp.menus.StatsWipeMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

public class UltimateDonutSmpCommand implements CommandExecutor {

    private static final String RELOAD_PERMISSION = "ultimatedonutsmp.admin.reload";
    private static final String STATS_WIPE_PERMISSION = "ultimatedonutsmp.admin.statswipe";
    private static final String OPTIMIZE_PERMISSION = "ultimatedonutsmp.admin.optimize";

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
            default -> sendUsage(sender, label);
        }
        return true;
    }

    private void handleReload(CommandSender sender, String label) {
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
        sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <reload|statswipe|optimize>"));
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
}
