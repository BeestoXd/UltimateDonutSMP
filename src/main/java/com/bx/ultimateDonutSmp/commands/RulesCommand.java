package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.RulesMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RulesCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public RulesCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ."); return true; }
        if (!plugin.getConfigManager().isCommandEnabled("RULES")) {
            player.sendMessage(ColorUtils.toComponent("&cʀᴜʟᴇѕ ᴄᴏᴍᴍᴀɴᴅ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."));
            return true;
        }

        RulesMenu menu = new RulesMenu(plugin);
        if (menu.hasValidButtons()) {
            menu.open(player);
            return true;
        }

        sendLegacyRules(player);
        return true;
    }

    private void sendLegacyRules(Player player) {
        player.sendMessage(ColorUtils.toComponent("&7&m---------- &bѕᴇʀᴠᴇʀ ʀᴜʟᴇѕ &7&m----------"));
        player.sendMessage(ColorUtils.toComponent("&71. &fɴᴏ ᴄʜᴇᴀᴛɪɴɢ ᴏʀ ʜᴀᴄᴋɪɴɢ"));
        player.sendMessage(ColorUtils.toComponent("&72. &fʙᴇ ʀᴇѕᴘᴇᴄᴛꜰᴜʟ ᴛᴏ ᴏᴛʜᴇʀ ᴘʟᴀʏᴇʀѕ"));
        player.sendMessage(ColorUtils.toComponent("&73. &fɴᴏ ɢʀɪᴇꜰɪɴɢ ᴀᴛ ѕᴘᴀᴡɴ"));
        player.sendMessage(ColorUtils.toComponent("&74. &fɴᴏ ᴜѕᴇ ᴏꜰ ᴇxᴘʟᴏɪᴛѕ ᴏʀ ᴅᴜᴘʟɪᴄᴀᴛɪᴏɴ"));
        player.sendMessage(ColorUtils.toComponent("&75. &fʜᴀᴠᴇ ꜰᴜɴ!"));
        player.sendMessage(ColorUtils.toComponent("&7&m-----------------------------------"));
    }
}
