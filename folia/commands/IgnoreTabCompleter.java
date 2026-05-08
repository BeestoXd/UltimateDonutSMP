package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.IgnoreEntry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IgnoreTabCompleter implements TabCompleter {

    private final UltimateDonutSmp plugin;

    public IgnoreTabCompleter(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || args.length != 1) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        if (alias.equalsIgnoreCase("unignore")) {
            for (IgnoreEntry entry : plugin.getIgnoreManager().getIgnoredPlayers(player.getUniqueId())) {
                completions.add(entry.ignoredNameSnapshot());
            }
            return partialMatches(args[0], completions);
        }

        completions.add("list");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                completions.add(onlinePlayer.getName());
            }
        }
        return partialMatches(args[0], completions);
    }

    private List<String> partialMatches(String token, List<String> completions) {
        List<String> matches = new ArrayList<>();
        StringUtil.copyPartialMatches(token, completions, matches);
        matches.sort(String.CASE_INSENSITIVE_ORDER);
        return matches;
    }
}
