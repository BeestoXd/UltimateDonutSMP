package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.ShopEditorMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShopEditCommand implements CommandExecutor, TabCompleter {

    private final UltimateDonutSmp plugin;

    public ShopEditCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.colorize("&cOnly players can use /" + label + "."));
            return true;
        }

        if (!PermissionUtils.has(sender, "ultimatedonutsmp.admin.shop")) {
            sender.sendMessage(ColorUtils.colorize("&cYou do not have permission to edit the shop."));
            return true;
        }

        List<String> menus = plugin.getShopManager().getEditableMenuSections();
        if (menus.isEmpty()) {
            player.sendMessage(ColorUtils.toComponent("&cThere are no shop menus to edit."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <menu>"));
            player.sendMessage(ColorUtils.toComponent("&7Menus: &f" + String.join("&7, &f", friendlyNames(menus))));
            return true;
        }

        String menuSection = plugin.getShopManager().resolveMenuSection(args[0]);
        if (menuSection == null) {
            player.sendMessage(ColorUtils.toComponent("&cThere is no shop menu called &f" + args[0] + "&c."));
            player.sendMessage(ColorUtils.toComponent("&7Menus: &f" + String.join("&7, &f", friendlyNames(menus))));
            return true;
        }

        new ShopEditorMenu(plugin, menuSection).open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1 || !PermissionUtils.has(sender, "ultimatedonutsmp.admin.shop")) {
            return List.of();
        }

        String prefix = args[0].toLowerCase(Locale.US);
        List<String> matches = new ArrayList<>();
        for (String name : friendlyNames(plugin.getShopManager().getEditableMenuSections())) {
            if (name.startsWith(prefix)) {
                matches.add(name);
            }
        }
        return matches;
    }

    private List<String> friendlyNames(List<String> menuSections) {
        List<String> names = new ArrayList<>();
        for (String section : menuSections) {
            String trimmed = section.endsWith("-MENU")
                    ? section.substring(0, section.length() - "-MENU".length())
                    : section;
            names.add(trimmed.toLowerCase(Locale.US));
        }
        return names;
    }
}
