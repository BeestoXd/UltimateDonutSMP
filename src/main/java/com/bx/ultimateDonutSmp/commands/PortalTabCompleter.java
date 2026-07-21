package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.SpawnManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PortalTabCompleter implements TabCompleter {

    private final UltimateDonutSmp plugin;

    public PortalTabCompleter(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("portalmanager")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return partialMatches(args[0], List.of(
                    "list", "info", "create", "delete", "setcuboid", "setdestination", "setdisplay", "toggle", "setpriority", "sethologramhere"
            ));
        }

        String subcommand = args[0].toLowerCase();
        if (args.length == 2) {
            return switch (subcommand) {
                case "info", "delete", "setcuboid", "setdestination", "setdisplay", "toggle", "setpriority", "sethologramhere" ->
                        partialMatches(args[1], plugin.getPortalManager().getSortedPortalIds());
                default -> Collections.emptyList();
            };
        }

        if (args.length == 3) {
            return switch (subcommand) {
                case "create", "setcuboid" -> partialMatches(args[2], new ArrayList<>(plugin.getCuboidManager().getCuboidNames()));
                case "setdestination" -> {
                    List<String> list = new ArrayList<>();
                    list.add("RTP");
                    list.add("AFK");
                    list.addAll(plugin.getRtpManager().getPortalSelectorSuggestions());
                    yield partialMatches(args[2], list);
                }
                default -> Collections.emptyList();
            };
        }

        if (args.length == 4) {
            if (subcommand.equals("create")) {
                List<String> list = new ArrayList<>();
                list.add("RTP");
                list.add("AFK");
                list.addAll(plugin.getRtpManager().getPortalSelectorSuggestions());
                return partialMatches(args[3], list);
            }
            if (subcommand.equals("setdestination")) {
                String type = args[2].toUpperCase(Locale.ROOT);
                if (type.equals("RTP")) {
                    return partialMatches(args[3], plugin.getRtpManager().getPortalSelectorSuggestions());
                } else if (type.equals("AFK")) {
                    List<String> areas = plugin.getSpawnManager().getValidAreas(SpawnManager.AreaType.AFK)
                            .stream().map(SpawnManager.TeleportArea::id).toList();
                    return partialMatches(args[3], areas);
                }
            }
        }

        if (args.length == 5) {
            if (subcommand.equals("create")) {
                String type = args[3].toUpperCase(Locale.ROOT);
                if (type.equals("RTP")) {
                    return partialMatches(args[4], plugin.getRtpManager().getPortalSelectorSuggestions());
                } else if (type.equals("AFK")) {
                    List<String> areas = plugin.getSpawnManager().getValidAreas(SpawnManager.AreaType.AFK)
                            .stream().map(SpawnManager.TeleportArea::id).toList();
                    return partialMatches(args[4], areas);
                }
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
