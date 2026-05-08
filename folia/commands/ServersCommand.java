package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.ServerStatusSnapshot;
import com.bx.ultimateDonutSmp.menus.ServersMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServersCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.servers";

    private final UltimateDonutSmp plugin;

    public ServersCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to use /servers."));
            return true;
        }

        if (!plugin.getConfigManager().isCommandEnabled("SERVERS")) {
            sender.sendMessage(ColorUtils.toComponent("&cServers menu is currently disabled."));
            return true;
        }

        if (!plugin.getNetworkStatusManager().isEnabled()) {
            sender.sendMessage(ColorUtils.toComponent("&cNetwork server status is currently disabled."));
            return true;
        }

        if (sender instanceof Player player) {
            ServersMenu menu = new ServersMenu(plugin);
            if (!menu.hasRenderableServers()) {
                player.sendMessage(ColorUtils.toComponent("&cNo servers are configured for the Servers Menu."));
                return true;
            }

            menu.open(player);
            return true;
        }

        if (!plugin.getNetworkStatusManager().hasConfiguredServers()) {
            sender.sendMessage("No servers are configured for network status.");
            return true;
        }

        sender.sendMessage("Network server status:");
        for (ServerStatusSnapshot snapshot : plugin.getNetworkStatusManager().getOrderedSnapshots()) {
            sender.sendMessage("- " + snapshot.displayName()
                    + " | " + (snapshot.online() ? "ONLINE" : "OFFLINE")
                    + " | players=" + snapshot.playerCount()
                    + " | software=" + snapshot.softwareLabel()
                    + " | performance=" + snapshot.performanceLabel());
        }
        return true;
    }
}
