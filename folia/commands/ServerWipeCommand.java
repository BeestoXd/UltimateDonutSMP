package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.DatabaseManager;
import com.bx.ultimateDonutSmp.managers.ServerWipeManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServerWipeCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "ultimatedonutsmp.admin.serverwipe";
    private static final List<String> SUBCOMMANDS = List.of("preview", "prepare", "confirm", "cancel", "status");

    private final UltimateDonutSmp plugin;

    public ServerWipeCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtils.has(sender, PERMISSION)) {
            send(sender, "&cYou do not have permission to inspect server wipes.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "preview" -> handlePreview(sender);
            case "status" -> send(sender, "&7Server wipe: &f" + plugin.getServerWipeManager().describeStatus());
            case "prepare" -> {
                if (!requireConsole(sender)) return true;
                sendResult(sender, plugin.getServerWipeManager().prepare());
            }
            case "confirm" -> {
                if (!requireConsole(sender)) return true;
                if (args.length < 2) {
                    send(sender, "&cUsage: /" + label + " confirm <token>");
                    return true;
                }
                sendResult(sender, plugin.getServerWipeManager().confirm(args[1]));
            }
            case "cancel" -> {
                if (!requireConsole(sender)) return true;
                sendResult(sender, plugin.getServerWipeManager().cancel());
            }
            default -> sendUsage(sender, label);
        }
        return true;
    }

    private void handlePreview(CommandSender sender) {
        ServerWipeManager.Preview preview = plugin.getServerWipeManager().preview();
        send(sender, "&6Server wipe preview");
        send(sender, "&7Worlds: &f" + (preview.worlds().isEmpty() ? "(none)" : String.join(", ", preview.worlds())));
        DatabaseManager.ServerWipePreview database = preview.database();
        send(sender, "&7Players: &f" + database.count("players")
                + " &8| &7Homes: &f" + database.count("homes")
                + " &8| &7Teams: &f" + database.count("teams"));
        send(sender, "&7Keys: &f" + database.count("crate_keys")
                + " &8| &7Ender chests: &f" + database.count("ender_chests")
                + " &8| &7Bounties: &f" + database.count("bounties"));
        send(sender, "&7Auctions: &f" + database.count("auctions")
                + " &8| &7Orders: &f" + database.count("orders")
                + " &8| &7PvP records: &f" + (database.count("duels") + database.count("ffa")));
        send(sender, "&7Reset-world spawners: &f" + database.count("spawners")
                + " &8| &7Crate blocks: &f" + database.count("crate_blocks"));
        if (preview.valid()) {
            send(sender, "&aValidation passed. Console can run /serverwipe prepare.");
            return;
        }
        for (String error : preview.errors()) send(sender, "&c- " + error);
    }

    private boolean requireConsole(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) return true;
        send(sender, "&cOnly the server console can run this destructive action.");
        return false;
    }

    private void sendResult(CommandSender sender, ServerWipeManager.OperationResult result) {
        send(sender, (result.success() ? "&a" : "&c") + result.message());
    }

    private void sendUsage(CommandSender sender, String label) {
        send(sender, "&cUsage: /" + label + " <preview|prepare|confirm <token>|cancel|status>");
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.toComponent(message));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!PermissionUtils.has(sender, PERMISSION)) return List.of();
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (String subcommand : SUBCOMMANDS) {
                if (subcommand.startsWith(input)) matches.add(subcommand);
            }
            return matches;
        }
        return List.of();
    }
}
