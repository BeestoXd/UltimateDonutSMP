package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.MaintenanceManager;
import com.bx.ultimateDonutSmp.managers.OffenseManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MaintenanceCommand implements CommandExecutor, TabCompleter {

    static final String ARGUMENTS = "<on [duration]|off|status|setlobby [server]>";

    /** Offered after /maintenance on. Any duration the punishment commands accept works too. */
    static final List<String> DURATION_SUGGESTIONS = List.of("15m", "30m", "1h", "2h", "6h");

    private final UltimateDonutSmp plugin;

    public MaintenanceCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        handle(plugin, sender, "/" + label, args);
        return true;
    }

    /**
     * The body of /maintenance. The /ultimatedonutsmp maintenance alias calls this with its own
     * subcommand stripped off, so the two entry points cannot drift apart the way they did when
     * setlobby learned to clear the stored server.
     *
     * @param usage what the calling entry point calls itself in a usage line
     * @param args  the arguments from the maintenance subcommand onwards
     */
    static void handle(UltimateDonutSmp plugin, CommandSender sender, String usage, String[] args) {
        if (!sender.hasPermission("ultimatedonutsmp.admin.maintenance")) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to manage maintenance mode."));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: " + usage + " " + ARGUMENTS));
            return;
        }

        var mm = plugin.getMaintenanceManager();
        if (mm == null) {
            sender.sendMessage(ColorUtils.toComponent("&cMaintenance manager is not available."));
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "on", "start", "enable" -> {
                if (mm.isMaintenanceActive()) {
                    sender.sendMessage(ColorUtils.toComponent("&eMaintenance mode is already active."));
                    return;
                }
                long durationMillis = 0L;
                if (args.length >= 2 && !args[1].isBlank()) {
                    Long parsed = OffenseManager.parseDurationToMillis(args[1]);
                    if (parsed == null || parsed <= 0L) {
                        sender.sendMessage(ColorUtils.toComponent(
                                "&cCould not read &b" + args[1] + "&c as a duration. Try something like 30m, 2h or 1d."));
                        return;
                    }
                    durationMillis = parsed;
                }
                mm.startMaintenance(durationMillis);
                if (durationMillis > 0L) {
                    sender.sendMessage(ColorUtils.toComponent(
                            "&aMaintenance mode has been enabled for &b"
                                    + plugin.getLanguageManager().formatDuration(durationMillis / 1000L, true)
                                    + "&a. Players are being redirected."));
                    return;
                }
                sender.sendMessage(ColorUtils.toComponent("&aMaintenance mode has been enabled. Players are being redirected."));
            }
            case "off", "stop", "disable" -> {
                if (!mm.isMaintenanceActive()) {
                    sender.sendMessage(ColorUtils.toComponent("&eMaintenance mode is not active."));
                    return;
                }
                mm.stopMaintenance();
                sender.sendMessage(ColorUtils.toComponent("&aMaintenance mode has been disabled. Reconnect signal sent."));
            }
            case "status" -> {
                boolean active = mm.isMaintenanceActive();
                String lobby = mm.getLobbyServer();
                sender.sendMessage(ColorUtils.toComponent("&d&lMaintenance status:"));
                sender.sendMessage(ColorUtils.toComponent("  &fActive: " + (active ? "&aYes" : "&cNo")));
                sender.sendMessage(ColorUtils.toComponent("  &fLobby server: " + describeLobbyServer(lobby)));
                sender.sendMessage(ColorUtils.toComponent("  &fEnds in: " + describeRemaining(mm.getRemainingSeconds())));
            }
            case "setlobby" -> {
                if (clearsLobbyServer(args)) {
                    mm.setLobbyServer(null);
                    sender.sendMessage(ColorUtils.toComponent(
                            "&aLobby server cleared. network.yml decides it now: "
                                    + describeLobbyServer(mm.getLobbyServer()) + "&a."));
                    return;
                }
                String lobby = args[1];
                mm.setLobbyServer(lobby);
                sender.sendMessage(ColorUtils.toComponent("&aLobby server set to &b" + lobby + "&a."));
            }
            default -> sender.sendMessage(ColorUtils.toComponent("&cUsage: " + usage + " " + ARGUMENTS));
        }
    }

    /**
     * Leaving the server name off clears the stored lobby, which is the only way back to the
     * MAINTENANCE.LOBBY_SERVER value in network.yml once one has been set in game.
     */
    static boolean clearsLobbyServer(String[] args) {
        return args.length < 2 || args[1].isBlank();
    }

    /** The three spellings of /maintenance on, all of which may be followed by a duration. */
    static boolean isEnableArgument(String argument) {
        return argument.equalsIgnoreCase("on")
                || argument.equalsIgnoreCase("start")
                || argument.equalsIgnoreCase("enable");
    }

    /**
     * Maintenance started without a duration has nothing to count down to, and saying so beats
     * printing a zero that reads like the server is about to reopen.
     */
    static String describeRemaining(long remainingSeconds) {
        if (remainingSeconds == MaintenanceManager.NO_DEADLINE) {
            return "&7nothing scheduled, it stays on until &f/maintenance off";
        }
        return "&b" + MaintenanceManager.formatCountdown(remainingSeconds);
    }

    static String describeLobbyServer(String lobby) {
        if (lobby == null || lobby.isBlank()) {
            return "&7none, so players without the bypass permission cannot connect";
        }
        return "&b" + lobby;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ultimatedonutsmp.admin.maintenance")) {
            return List.of();
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("on", "off", "status", "setlobby"), new ArrayList<>());
        }

        if (args.length == 2 && isEnableArgument(args[0])) {
            return StringUtil.copyPartialMatches(args[1], DURATION_SUGGESTIONS, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setlobby")) {
            List<String> servers = new ArrayList<>();
            ConfigurationSection sec = plugin.getConfigManager().getNetwork().getConfigurationSection("NETWORK-STATUS.SERVERS");
            if (sec != null) {
                servers.addAll(sec.getKeys(false));
            }
            return StringUtil.copyPartialMatches(args[1], servers, new ArrayList<>());
        }

        return List.of();
    }
}
