package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class FfaCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;
    private final FfaArenaCommand arenaCommand;

    public FfaCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
        this.arenaCommand = new FfaArenaCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sendUsage(sender, label);
                return true;
            }
            plugin.getFfaManager().joinArena(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        if (subcommand.equals("join")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
                return true;
            }
            plugin.getFfaManager().joinArena(player);
            return true;
        }
        if (subcommand.equals("reload")) {
            if (!PermissionUtils.has(sender, "ultimatedonutsmp.admin.ffa")) {
                sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʀᴇʟᴏᴀᴅ ꜰꜰᴀ."));
                return true;
            }
            plugin.getConfigManager().reloadFfa();
            plugin.getFfaManager().reload();
            sender.sendMessage(ColorUtils.toComponent("&aꜰꜰᴀ ᴄᴏɴꜰɪɢ ʀᴇʟᴏᴀᴅᴇᴅ."));
            return true;
        }
        if (subcommand.equals("arena")) {
            return arenaCommand.handle(sender, label + " arena", Arrays.copyOfRange(args, 1, args.length));
        }
        if (subcommand.equals("help")) {
            sendUsage(sender, label);
            return true;
        }

        sendUsage(sender, label);
        return true;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ColorUtils.toComponent("&e/" + label));
        sender.sendMessage(ColorUtils.toComponent("&e/leave"));
        sender.sendMessage(ColorUtils.toComponent("&e/ffastats [ᴘʟᴀʏᴇʀ]"));
        if (PermissionUtils.has(sender, "ultimatedonutsmp.admin.ffa")) {
            sender.sendMessage(ColorUtils.toComponent("&e/" + label + " ʀᴇʟᴏᴀᴅ"));
            sender.sendMessage(ColorUtils.toComponent("&e/" + label + " ᴀʀᴇɴᴀ <subcommand>"));
        }
    }
}
