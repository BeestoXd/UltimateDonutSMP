package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.ShopMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public ShopCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!PermissionUtils.has(sender, "ultimatedonutsmp.admin.shop")) {
                sender.sendMessage(ColorUtils.colorize("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ʀᴇʟᴏᴀᴅ ѕʜᴏᴘ ѕᴇᴛᴛɪɴɢѕ."));
                return true;
            }

            plugin.getConfigManager().reloadShop();
            plugin.getConfigManager().reloadMenus();
            plugin.getConfigManager().reloadSounds();
            plugin.getShopManager().reload();
            sender.sendMessage(ColorUtils.colorize("&aѕʜᴏᴘ ᴄᴏɴꜰɪɢ ʀᴇʟᴏᴀᴅᴇᴅ."));
            return true;
        }

        if (!(sender instanceof Player player)) { sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ."); return true; }
        new ShopMenu(plugin).open(player);
        return true;
    }
}
