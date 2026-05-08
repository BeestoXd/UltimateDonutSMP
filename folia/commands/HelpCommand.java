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
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only."); return true; }
        if (!plugin.getConfigManager().isCommandEnabled("HELP")) {
            player.sendMessage(ColorUtils.toComponent("&cHelp command is currently disabled."));
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
        player.sendMessage(ColorUtils.toComponent("&7&m-------- &bHelp &7&m--------"));
        sendHelpLine(player, "TEAM", "&b/team &7- Manage your team");
        sendHelpLine(player, "HOME", "&b/home &7- Teleport to your home");
        sendHelpLine(player, "SPAWN", "&b/spawn &7- Teleport to spawn");
        sendHelpLine(player, "RTP", "&b/rtp &7- Random teleport");
        sendHelpLine(player, "TPA", "&b/tpa &7- Request teleport to a player");
        sendHelpLine(player, "SHOP", "&b/shop &7- Open the shop");
        sendHelpLine(player, "SELL", "&b/sell &7- Sell your items");
        sendHelpLine(player, "CRATE", "&b/crates &7- Open the crates menu");
        player.sendMessage(ColorUtils.toComponent("&b/balance &7- Check your balance"));
        sendHelpLine(player, "SHARDS", "&b/shards &7- Check your shards");
        player.sendMessage(ColorUtils.toComponent("&b/bounty &7- View bounties"));
        sendHelpLine(player, "STATS", "&b/stats &7- View your stats");
        sendHelpLine(player, "LEADERBOARDS", "&b/leaderboard &7- View top players");
        sendHelpLine(player, "SETTINGS", "&b/settings &7- Player settings");
        sendHelpLine(player, "BILLFORD", "&b/billford &7- Special trade");
        sendHelpLine(player, "SOCIAL", "&b/discord &7- Discord link");
        sendHelpLine(player, "SOCIAL", "&b/media &7- View media rank requirements");
        sendHelpLine(player, "RULES", "&b/rules &7- View server rules");
        player.sendMessage(ColorUtils.toComponent("&7&m---------------------"));
    }

    private void sendHelpLine(Player player, String commandKey, String line) {
        if (plugin.getConfigManager().isCommandEnabled(commandKey)) {
            player.sendMessage(ColorUtils.toComponent(line));
        }
    }
}
