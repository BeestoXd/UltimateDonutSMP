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
        if (!sender.hasPermission(STATS_WIPE_PERMISSION)) {
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
        if (!sender.hasPermission(OPTIMIZE_PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴜѕᴇ ᴏᴘᴛɪᴍɪᴢᴀᴛɪᴏɴ ᴛᴏᴏʟѕ."));
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
        sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " <reload|statswipe|optimize>"));
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
}
