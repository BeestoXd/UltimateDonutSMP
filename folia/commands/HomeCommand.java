package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.HomeMenu;
import com.bx.ultimateDonutSmp.models.Home;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public HomeCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ."); return true; }

        if (plugin.getCombatManager().isInCombat(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getConfig()
                    .getString("COMBAT-MANAGER.BLOCK-MESSAGE", "&cʏᴏᴜ ᴄᴀɴ'ᴛ ᴜѕᴇ ᴛʜɪѕ ɪɴ ᴄᴏᴍʙᴀᴛ.")));
            return true;
        }

        String sub = label.toLowerCase();

        if (sub.equals("homes")) {
            new HomeMenu(plugin).open(player);
            return true;
        }

        if (sub.equals("sethome")) {
            String name = args.length > 0 ? args[0] : "home";
            boolean success = plugin.getHomeManager().setHome(player, name);
            if (success) {
                player.sendMessage(ColorUtils.toComponent(
                        plugin.getConfigManager().getMessage("HOME.SET")));
            } else {
                player.sendMessage(ColorUtils.toComponent(
                        "&cʏᴏᴜ'ᴠᴇ ʀᴇᴀᴄʜᴇᴅ ʏᴏᴜʀ ʜᴏᴍᴇ ʟɪᴍɪᴛ ᴏʀ ᴛʜᴇ ʜᴏᴍᴇ ᴀʟʀᴇᴀᴅʏ ᴇxɪѕᴛѕ ᴀᴛ ᴛʜɪѕ ɴᴀᴍᴇ."));
            }
            return true;
        }

        if (sub.equals("delhome")) {
            if (args.length == 0) { player.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /delhome <name>")); return true; }
            boolean removed = plugin.getHomeManager().deleteHome(player.getUniqueId(), args[0]);
            player.sendMessage(ColorUtils.toComponent(removed
                    ? plugin.getConfigManager().getMessage("HOME.DELETED")
                    : "&cʜᴏᴍᴇ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
            return true;
        }

        if (sub.equals("renamehome")) {
            if (args.length < 2) { player.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /renamehome <old> <new>")); return true; }
            boolean ok = plugin.getHomeManager().renameHome(player.getUniqueId(), args[0], args[1]);
            player.sendMessage(ColorUtils.toComponent(ok
                    ? plugin.getConfigManager().getMessage("HOME.RENAME-SUCCESS", "{name}", args[1])
                    : "&cꜰᴀɪʟᴇᴅ ᴛᴏ ʀᴇɴᴀᴍᴇ ʜᴏᴍᴇ."));
            return true;
        }

        // /home [name]
        String homeName = args.length > 0 ? args[0] : "home";
        Home home = plugin.getHomeManager().getHome(player.getUniqueId(), homeName);
        if (home == null) {
            if (plugin.getHomeManager().getHomeCount(player.getUniqueId()) == 0) {
                new HomeMenu(plugin).open(player);
            } else {
                player.sendMessage(ColorUtils.toComponent("&cʜᴏᴍᴇ '&e" + homeName + "&c' ɴᴏᴛ ꜰᴏᴜɴᴅ."));
            }
            return true;
        }

        plugin.getTeleportManager().queue(player, home.getLocation(), "HOME", null);
        return true;
    }
}
