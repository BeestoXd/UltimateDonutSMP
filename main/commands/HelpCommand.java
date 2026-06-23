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
        sendHelpLine(player, "team", "&b/team &7- manage your team");
        sendHelpLine(player, "home", "&b/home &7- teleport to your home");
        sendHelpLine(player, "spawn", "&b/spawn &7- teleport to spawn");
        sendHelpLine(player, "rtp", "&b/rtp &7- random teleport");
        sendHelpLine(player, "tpa", "&b/tpa &7- request teleport to a player");
        sendHelpLine(player, "shop", "&b/shop &7- open the shop");
        sendHelpLine(player, "sell", "&b/sell &7- sell your items");
        sendHelpLine(player, "crate", "&b/crates &7- open the crates menu");
        player.sendMessage(ColorUtils.toComponent("&b/balance &7- ᴄʜᴇᴄᴋ ʏᴏᴜʀ ʙᴀʟᴀɴᴄᴇ"));
        sendHelpLine(player, "shards", "&b/shards &7- check your "
                + plugin.getCurrencyManager().plural(com.bx.ultimateDonutSmp.managers.CurrencyManager.CurrencyType.SHARDS));
        sendHelpLine(player, "bounty", "&b/bounty &7- view bounties");
        sendHelpLine(player, "stats", "&b/stats &7- view your stats");
        sendHelpLine(player, "leaderboards", "&b/leaderboard &7- view top players");
        sendHelpLine(player, "settings", "&b/settings &7- player settings");
        sendHelpLine(player, "billford", "&b/billford &7- special trade");
        sendHelpLine(player, "social", "&b/discord &7- discord link");
        sendHelpLine(player, "social", "&b/media &7- view media rank requirements");
        sendHelpLine(player, "rules", "&b/rules &7- view server rules");
        player.sendMessage(ColorUtils.toComponent("&7&m---------------------"));
    }

    private void sendHelpLine(Player player, String commandKey, String line) {
        if (plugin.getConfigManager().isCommandEnabled(commandKey)) {
            player.sendMessage(ColorUtils.toComponent(line));
        }
    }
}
