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
            send(sender, "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ɪɴѕᴘᴇᴄᴛ ѕᴇʀᴠᴇʀ ᴡɪᴘᴇѕ.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "preview" -> handlePreview(sender);
            case "status" -> send(sender, "&7ѕᴇʀᴠᴇʀ ᴡɪᴘᴇ: &f" + plugin.getServerWipeManager().describeStatus());
            case "prepare" -> {
                if (!requireConsole(sender)) {
                    return true;
                }
                sendResult(sender, plugin.getServerWipeManager().prepare());
            }
            case "confirm" -> {
                if (!requireConsole(sender)) {
                    return true;
                }
                if (args.length < 2) {
                    send(sender, "&cᴜѕᴀɢᴇ: /" + label + " ᴄᴏɴꜰɪʀᴍ <token>");
                    return true;
                }
                sendResult(sender, plugin.getServerWipeManager().confirm(args[1]));
            }
            case "cancel" -> {
                if (!requireConsole(sender)) {
                    return true;
                }
                sendResult(sender, plugin.getServerWipeManager().cancel());
            }
            default -> sendUsage(sender, label);
        }
        return true;
    }

    private void handlePreview(CommandSender sender) {
        ServerWipeManager.Preview preview = plugin.getServerWipeManager().preview();
        send(sender, "&6ѕᴇʀᴠᴇʀ ᴡɪᴘᴇ ᴘʀᴇᴠɪᴇᴡ");
        send(sender, "&7ᴡᴏʀʟᴅѕ: &f" + (preview.worlds().isEmpty() ? "(none)" : String.join(", ", preview.worlds())));

        DatabaseManager.ServerWipePreview database = preview.database();
        send(sender, "&7ᴘʟᴀʏᴇʀѕ: &f" + database.count("players")
                + " &8| &7ʜᴏᴍᴇѕ: &f" + database.count("homes")
                + " &8| &7ᴛᴇᴀᴍѕ: &f" + database.count("teams"));
        send(sender, "&7ᴋᴇʏѕ: &f" + database.count("crate_keys")
                + " &8| &7ᴇɴᴅᴇʀ ᴄʜᴇѕᴛѕ: &f" + database.count("ender_chests")
                + " &8| &7ʙᴏᴜɴᴛɪᴇѕ: &f" + database.count("bounties"));
        send(sender, "&7ᴀᴜᴄᴛɪᴏɴѕ: &f" + database.count("auctions")
                + " &8| &7ᴏʀᴅᴇʀѕ: &f" + database.count("orders")
                + " &8| &7ᴘᴠᴘ ʀᴇᴄᴏʀᴅѕ: &f" + (database.count("duels") + database.count("ffa")));
        send(sender, "&7ʀᴇѕᴇᴛ-ᴡᴏʀʟᴅ ѕᴘᴀᴡɴᴇʀѕ: &f" + database.count("spawners")
                + " &8| &7ᴄʀᴀᴛᴇ ʙʟᴏᴄᴋѕ: &f" + database.count("crate_blocks"));

        if (preview.valid()) {
            send(sender, "&aᴠᴀʟɪᴅᴀᴛɪᴏɴ ᴘᴀѕѕᴇᴅ. ᴄᴏɴѕᴏʟᴇ ᴄᴀɴ ʀᴜɴ /serverwipe ᴘʀᴇᴘᴀʀᴇ.");
            return;
        }
        for (String error : preview.errors()) {
            send(sender, "&c- " + error);
        }
    }

    private boolean requireConsole(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }
        send(sender, "&cᴏɴʟʏ ᴛʜᴇ ѕᴇʀᴠᴇʀ ᴄᴏɴѕᴏʟᴇ ᴄᴀɴ ʀᴜɴ ᴛʜɪѕ ᴅᴇѕᴛʀᴜᴄᴛɪᴠᴇ ᴀᴄᴛɪᴏɴ.");
        return false;
    }

    private void sendResult(CommandSender sender, ServerWipeManager.OperationResult result) {
        send(sender, (result.success() ? "&a" : "&c") + result.message());
    }

    private void sendUsage(CommandSender sender, String label) {
        send(sender, "&cᴜѕᴀɢᴇ: /" + label + " <preview|prepare|confirm <token>|ᴄᴀɴᴄᴇʟ|ѕᴛᴀᴛᴜѕ>");
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.toComponent(message));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!PermissionUtils.has(sender, PERMISSION)) {
            return List.of();
        }
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (String subcommand : SUBCOMMANDS) {
                if (subcommand.startsWith(input)) {
                    matches.add(subcommand);
                }
            }
            return matches;
        }
        return List.of();
    }
}
