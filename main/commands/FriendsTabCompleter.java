package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.FollowEntry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendsTabCompleter implements TabCompleter {

    private final UltimateDonutSmp plugin;

    public FriendsTabCompleter(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            subcommands.add("list");
            subcommands.add("follow");
            subcommands.add("unfollow");
            subcommands.add("search");
            subcommands.add("following");
            subcommands.add("followers");
            subcommands.add("friends");
            if (player.hasPermission("donutfriends.admin")) {
                subcommands.add("reload");
            }
            return partialMatches(args[0], subcommands);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("follow") || sub.equals("add")) {
                List<String> players = new ArrayList<>();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!online.getUniqueId().equals(player.getUniqueId())) {
                        players.add(plugin.getHideManager().plainPublicName(online));
                    }
                }
                return partialMatches(args[1], players);
            }

            if (sub.equals("unfollow") || sub.equals("remove")) {
                List<String> following = new ArrayList<>();
                for (FollowEntry entry : plugin.getFriendsManager().getFollowing(player.getUniqueId())) {
                    following.add(entry.followedNameSnapshot());
                }
                return partialMatches(args[1], following);
            }
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
