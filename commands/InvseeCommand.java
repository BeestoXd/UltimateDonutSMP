package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvseeCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public InvseeCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!plugin.getInvseeManager().canAdmin(sender)) {
                sender.sendMessage(ColorUtils.toComponent(
                        plugin.getInvseeManager().getMessage("NO-PERMISSION", "&cYou do not have permission.")
                ));
                return true;
            }

            plugin.getConfigManager().reloadInvsee();
            plugin.getInvseeManager().reload();
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getInvseeManager().getMessage("RELOAD-SUCCESS", "&aInvsee config reloaded.")
            ));
            return true;
        }

        if (!(sender instanceof Player viewer)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (!plugin.getInvseeManager().isEnabled()) {
            viewer.sendMessage(ColorUtils.toComponent(
                    plugin.getInvseeManager().getMessage("FEATURE-DISABLED", "&cThe Invsee system is disabled.")
            ));
            return true;
        }

        if (!plugin.getInvseeManager().canView(viewer)) {
            viewer.sendMessage(ColorUtils.toComponent(
                    plugin.getInvseeManager().getMessage("NO-PERMISSION", "&cYou do not have permission.")
            ));
            return true;
        }

        if (args.length == 0) {
            viewer.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <player>"));
            return true;
        }

        Player target = plugin.getInvseeManager().findOnlineTarget(args[0]);
        if (target == null) {
            boolean knownPlayer = plugin.getInvseeManager().hasKnownPlayer(args[0]);
            String path = plugin.getInvseeManager().requiresOnlineTarget() || knownPlayer
                    ? "PLAYER-NOT-ONLINE"
                    : "PLAYER-NOT-FOUND";
            String fallback = plugin.getInvseeManager().requiresOnlineTarget() || knownPlayer
                    ? "&cThat player must be online."
                    : "&cPlayer not found.";
            viewer.sendMessage(ColorUtils.toComponent(
                    plugin.getInvseeManager().formatMessage(path, fallback, "{player}", args[0], "{target}", args[0])
            ));
            return true;
        }

        if (!plugin.getInvseeManager().allowSelfView()
                && viewer.getUniqueId().equals(target.getUniqueId())) {
            viewer.sendMessage(ColorUtils.toComponent(
                    plugin.getInvseeManager().getMessage("SELF-VIEW-DISABLED", "&cYou cannot invsee yourself.")
            ));
            return true;
        }

        plugin.getInvseeManager().open(viewer, target);
        return true;
    }
}
