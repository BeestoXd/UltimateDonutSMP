package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.LeaderboardMenu;
import com.bx.ultimateDonutSmp.menus.LeaderboardTypeMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class LeaderboardCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public LeaderboardCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only."); return true; }
        if (!plugin.getConfigManager().isCommandEnabled("LEADERBOARDS")) {
            player.sendMessage(ColorUtils.toComponent("&cLeaderboards are currently disabled."));
            return true;
        }

        if (args.length == 0) {
            new LeaderboardMenu(plugin).open(player);
            return true;
        }

        var type = plugin.getLeaderboardManager().parseType(args[0]).orElse(null);
        if (type == null) {
            String available = plugin.getLeaderboardManager().getTypes().stream()
                    .map(leaderboardType -> leaderboardType.getConfigKey())
                    .collect(Collectors.joining(", "));
            player.sendMessage(ColorUtils.toComponent("&cTipe leaderboard tidak valid. &7Available: &f" + available));
            return true;
        }

        new LeaderboardTypeMenu(plugin, type).open(player);
        return true;
    }
}
