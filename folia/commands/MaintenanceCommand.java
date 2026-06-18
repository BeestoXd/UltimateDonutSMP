package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
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

    private final UltimateDonutSmp plugin;

    public MaintenanceCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimatedonutsmp.admin.maintenance")) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴍᴀɴᴀɢᴇ ᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ ᴍᴏᴅᴇ."));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " <on|off|status|setlobby [server]>"));
            return true;
        }

        var mm = plugin.getMaintenanceManager();
        if (mm == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ ᴍᴀɴᴀɢᴇʀ ɪѕ ɴᴏᴛ ᴀᴠᴀɪʟᴀʙʟᴇ."));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "on", "start", "enable" -> {
                if (mm.isMaintenanceActive()) {
                    sender.sendMessage(ColorUtils.toComponent("&eᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ ᴍᴏᴅᴇ ɪѕ ᴀʟʀᴇᴀᴅʏ ᴀᴄᴛɪᴠᴇ."));
                    return true;
                }
                mm.startMaintenance();
                sender.sendMessage(ColorUtils.toComponent("&aᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ ᴍᴏᴅᴇ ʜᴀѕ ʙᴇᴇɴ ᴇɴᴀʙʟᴇᴅ. ᴘʟᴀʏᴇʀѕ ᴀʀᴇ ʙᴇɪɴɢ ʀᴇᴅɪʀᴇᴄᴛᴇᴅ."));
            }
            case "off", "stop", "disable" -> {
                if (!mm.isMaintenanceActive()) {
                    sender.sendMessage(ColorUtils.toComponent("&eᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ ᴍᴏᴅᴇ ɪѕ ɴᴏᴛ ᴀᴄᴛɪᴠᴇ."));
                    return true;
                }
                mm.stopMaintenance();
                sender.sendMessage(ColorUtils.toComponent("&aᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ ᴍᴏᴅᴇ ʜᴀѕ ʙᴇᴇɴ ᴅɪѕᴀʙʟᴇᴅ. ʀᴇᴄᴏɴɴᴇᴄᴛ ѕɪɢɴᴀʟ ѕᴇɴᴛ."));
            }
            case "status" -> {
                boolean active = mm.isMaintenanceActive();
                String lobby = mm.getLobbyServer();
                sender.sendMessage(ColorUtils.toComponent("&d&lᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ ѕᴛᴀᴛᴜѕ:"));
                sender.sendMessage(ColorUtils.toComponent("  &fᴀᴄᴛɪᴠᴇ: " + (active ? "&aʏᴇѕ" : "&cɴᴏ")));
                sender.sendMessage(ColorUtils.toComponent("  &fʟᴏʙʙʏ ѕᴇʀᴠᴇʀ: &b" + lobby));
            }
            case "setlobby" -> {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " setlobby <server>"));
                    return true;
                }
                String lobby = args[1];
                mm.setLobbyServer(lobby);
                sender.sendMessage(ColorUtils.toComponent("&aʟᴏʙʙʏ ѕᴇʀᴠᴇʀ ѕᴇᴛ ᴛᴏ &b" + lobby + "&a."));
            }
            default -> sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " <on|off|status|setlobby [server]>"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ultimatedonutsmp.admin.maintenance")) {
            return List.of();
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("on", "off", "status", "setlobby"), new ArrayList<>());
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
