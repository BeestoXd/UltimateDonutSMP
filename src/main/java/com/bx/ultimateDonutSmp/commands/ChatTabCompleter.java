package com.bx.ultimateDonutSmp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return partialMatches(args[0], List.of("help", "mute", "unmute", "delay", "clear"));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("delay")) {
            return partialMatches(args[1], List.of("0", "3", "5", "10", "off"));
        }

        return Collections.emptyList();
    }

    private List<String> partialMatches(String token, List<String> completions) {
        List<String> matches = new ArrayList<>();
        StringUtil.copyPartialMatches(token, completions, matches);
        matches.sort(String.CASE_INSENSITIVE_ORDER);
        return matches;
    }
}
