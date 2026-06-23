package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageTabCompleter implements TabCompleter {

    private final UltimateDonutSmp plugin;

    public MessageTabCompleter(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (alias.equalsIgnoreCase("reply") || alias.equalsIgnoreCase("r") || args.length != 1) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        for (String name : plugin.getHideManager().onlineNames(sender)) {
            Player player = plugin.getHideManager().findOnlinePlayer(sender, name);
            if (player != null
                    && sender instanceof Player senderPlayer
                    && player.getUniqueId().equals(senderPlayer.getUniqueId())) {
                continue;
            }
            completions.add(name);
        }
        List<String> matches = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], completions, matches);
        matches.sort(String.CASE_INSENSITIVE_ORDER);
        return matches;
    }
}
