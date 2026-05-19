package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.ServerInfoMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public HelpCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ."); return true; }
        if (!plugin.getConfigManager().isCommandEnabled("HELP")) {
            player.sendMessage(ColorUtils.toComponent("&cʜᴇʟᴘ ᴄᴏᴍᴍᴀɴᴅ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."));
            return true;
        }

        ServerInfoMenu menu = new ServerInfoMenu(plugin);
        if (menu.hasValidButtons()) {
            menu.open(player);
            return true;
        }

        sendLegacyHelp(player);
        return true;
    }

    private void sendLegacyHelp(Player player) {
        player.sendMessage(ColorUtils.toComponent("&7&m-------- &bʜᴇʟᴘ &7&m--------"));
        sendHelpLine(player, "TEAM", "&b/team &7- ᴍᴀɴᴀɢᴇ ʏᴏᴜʀ ᴛᴇᴀᴍ");
        sendHelpLine(player, "HOME", "&b/home &7- ᴛᴇʟᴇᴘᴏʀᴛ ᴛᴏ ʏᴏᴜʀ ʜᴏᴍᴇ");
        sendHelpLine(player, "SPAWN", "&b/spawn &7- ᴛᴇʟᴇᴘᴏʀᴛ ᴛᴏ ѕᴘᴀᴡɴ");
        sendHelpLine(player, "RTP", "&b/rtp &7- ʀᴀɴᴅᴏᴍ ᴛᴇʟᴇᴘᴏʀᴛ");
        sendHelpLine(player, "TPA", "&b/tpa &7- ʀᴇǫᴜᴇѕᴛ ᴛᴇʟᴇᴘᴏʀᴛ ᴛᴏ ᴀ ᴘʟᴀʏᴇʀ");
        sendHelpLine(player, "SHOP", "&b/shop &7- ᴏᴘᴇɴ ᴛʜᴇ ѕʜᴏᴘ");
        sendHelpLine(player, "SELL", "&b/sell &7- ѕᴇʟʟ ʏᴏᴜʀ ɪᴛᴇᴍѕ");
        sendHelpLine(player, "CRATE", "&b/crates &7- ᴏᴘᴇɴ ᴛʜᴇ ᴄʀᴀᴛᴇѕ ᴍᴇɴᴜ");
        player.sendMessage(ColorUtils.toComponent("&b/balance &7- ᴄʜᴇᴄᴋ ʏᴏᴜʀ ʙᴀʟᴀɴᴄᴇ"));
        sendHelpLine(player, "SHARDS", "&b/shards &7- ᴄʜᴇᴄᴋ ʏᴏᴜʀ ѕʜᴀʀᴅѕ");
        player.sendMessage(ColorUtils.toComponent("&b/bounty &7- ᴠɪᴇᴡ ʙᴏᴜɴᴛɪᴇѕ"));
        sendHelpLine(player, "STATS", "&b/stats &7- ᴠɪᴇᴡ ʏᴏᴜʀ ѕᴛᴀᴛѕ");
        sendHelpLine(player, "LEADERBOARDS", "&b/leaderboard &7- ᴠɪᴇᴡ ᴛᴏᴘ ᴘʟᴀʏᴇʀѕ");
        sendHelpLine(player, "SETTINGS", "&b/settings &7- ᴘʟᴀʏᴇʀ ѕᴇᴛᴛɪɴɢѕ");
        sendHelpLine(player, "BILLFORD", "&b/billford &7- ѕᴘᴇᴄɪᴀʟ ᴛʀᴀᴅᴇ");
        sendHelpLine(player, "SOCIAL", "&b/discord &7- ᴅɪѕᴄᴏʀᴅ ʟɪɴᴋ");
        sendHelpLine(player, "SOCIAL", "&b/media &7- ᴠɪᴇᴡ ᴍᴇᴅɪᴀ ʀᴀɴᴋ ʀᴇǫᴜɪʀᴇᴍᴇɴᴛѕ");
        sendHelpLine(player, "RULES", "&b/rules &7- ᴠɪᴇᴡ ѕᴇʀᴠᴇʀ ʀᴜʟᴇѕ");
        player.sendMessage(ColorUtils.toComponent("&7&m---------------------"));
    }

    private void sendHelpLine(Player player, String commandKey, String line) {
        if (plugin.getConfigManager().isCommandEnabled(commandKey)) {
            player.sendMessage(ColorUtils.toComponent(line));
        }
    }
}
