package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WarpTabCompleter implements TabCompleter {

    private final UltimateDonutSmp plugin;

    public WarpTabCompleter(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String commandName = command.getName().toLowerCase(Locale.ROOT);

        if (commandName.equals("warp")) {
            if (args.length == 1) {
                return partialMatches(args[0], plugin.getWarpManager().getSortedWarpNames());
            }
            return Collections.emptyList();
        }

        if (commandName.equals("delwarp")) {
            if (args.length == 1) {
                return partialMatches(args[0], plugin.getWarpManager().getSortedWarpNames());
            }
            return Collections.emptyList();
        }

        if (commandName.equals("setwarp")) {
            return Collections.emptyList();
        }

        if (!commandName.equals("warpmanager")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return partialMatches(args[0], List.of("create", "delete", "list"));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            return partialMatches(args[1], plugin.getWarpManager().getSortedWarpNames());
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
