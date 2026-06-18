package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.DuelClaimMenu;
import com.bx.ultimateDonutSmp.menus.DuelCreateMenu;
import com.bx.ultimateDonutSmp.menus.DuelQueueMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public DuelCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }

        if (!plugin.getDuelManager().isEnabled() && (args.length == 0 || !"reload".equalsIgnoreCase(args[0]))) {
            player.sendMessage(ColorUtils.toComponent("&cᴅᴜᴇʟѕ ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."));
            return true;
        }

        if (args.length == 0) {
            new DuelQueueMenu(plugin).open(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        if (subcommand.equals("claims")) {
            new DuelClaimMenu(plugin, 1).open(player);
            return true;
        }
        if (subcommand.equals("reload")) {
            if (!PermissionUtils.has(player, "ultimatedonutsmp.admin.duels")) {
                player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʀᴇʟᴏᴀᴅ ᴅᴜᴇʟѕ."));
                return true;
            }
            plugin.getConfigManager().reloadDuels();
            plugin.getDuelManager().reload();
            player.sendMessage(ColorUtils.toComponent("&aᴅᴜᴇʟѕ ᴄᴏɴꜰɪɢ ʀᴇʟᴏᴀᴅᴇᴅ."));
            return true;
        }
        if (subcommand.equals("accept")) {
            plugin.getDuelManager().acceptChallenge(player, args.length > 1 ? args[1] : null);
            return true;
        }
        if (subcommand.equals("deny")) {
            plugin.getDuelManager().denyChallenge(player, args.length > 1 ? args[1] : null);
            return true;
        }

        Player target = plugin.getHideManager().findOnlinePlayer(player, args[0]);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ᴘʟᴀʏᴇʀ ɪѕ ɴᴏᴛ ᴏɴʟɪɴᴇ."));
            return true;
        }

        new DuelCreateMenu(plugin, target.getUniqueId()).open(player);
        return true;
    }
}
