package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.amethyst.AmethystToolType;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AmethystToolCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "ultimatedonutsmp.admin.amethysttool";
    private static final List<String> SUBCOMMAND_COMPLETIONS = List.of("give", "reload");
    private static final List<String> TYPE_COMPLETIONS = createTypeCompletions();

    private final UltimateDonutSmp plugin;

    public AmethystToolCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var mgr = plugin.getAmethystToolsManager();

        if (args.length < 1) {
            sender.sendMessage(ColorUtils.toComponent(mgr.getMessage("GIVE-USAGE")));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("reload")) {
            if (!sender.hasPermission(PERMISSION)) {
                sender.sendMessage(ColorUtils.toComponent("&cNo permission."));
                return true;
            }
            plugin.getConfigManager().reloadAmethystTools();
            sender.sendMessage(ColorUtils.toComponent(mgr.getMessage("RELOAD-SUCCESS")));
            return true;
        }

        if (sub.equals("give")) {
            if (!sender.hasPermission(PERMISSION)) {
                sender.sendMessage(ColorUtils.toComponent("&cNo permission."));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ColorUtils.toComponent(mgr.getMessage("GIVE-USAGE")));
                return true;
            }
            String targetName = args[1];
            String typeName   = args[2];
            long duration = args.length >= 4 ? parseLong(args[3]) : -1L;

            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(ColorUtils.toComponent("&cPlayer not found: " + targetName));
                return true;
            }

            AmethystToolType type = AmethystToolType.fromString(typeName);
            if (type == null) {
                sender.sendMessage(ColorUtils.toComponent(mgr.getMessage("GIVE-INVALID-TYPE")));
                return true;
            }

            ItemStack item = mgr.createTool(type, target.getUniqueId(), duration);
            if (item == null) {
                sender.sendMessage(ColorUtils.toComponent("&cFailed to create item (check config)."));
                return true;
            }

            target.getInventory().addItem(item);
            sender.sendMessage(ColorUtils.toComponent(
                    mgr.getMessage("GIVE-SUCCESS",
                            "{type}", type.getDisplayName(),
                            "{player}", target.getName())));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent(mgr.getMessage("GIVE-USAGE")));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return partialMatches(args[0], SUBCOMMAND_COMPLETIONS);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return partialMatches(args[1], onlinePlayerNames());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return partialMatches(args[2], TYPE_COMPLETIONS);
        }

        return Collections.emptyList();
    }

    private static List<String> createTypeCompletions() {
        List<String> completions = new ArrayList<>();
        for (AmethystToolType type : AmethystToolType.values()) {
            completions.add(type.name().toLowerCase(Locale.ROOT).replace('_', '-'));
        }
        return List.copyOf(completions);
    }

    private List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    private List<String> partialMatches(String token, List<String> completions) {
        List<String> matches = new ArrayList<>();
        StringUtil.copyPartialMatches(token, completions, matches);
        matches.sort(String.CASE_INSENSITIVE_ORDER);
        return matches;
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return -1L; }
    }
}
