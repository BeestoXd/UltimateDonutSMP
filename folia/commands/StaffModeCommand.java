package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.StaffModeManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffModeCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public StaffModeCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        StaffModeManager manager = plugin.getStaffModeManager();

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!manager.canAdmin(sender)) {
                sender.sendMessage(ColorUtils.toComponent(
                        manager.getMessage("NO-PERMISSION", "&cYou do not have permission.")
                ));
                return true;
            }

            plugin.getConfigManager().reload();
            manager.reload();
            sender.sendMessage(ColorUtils.toComponent(
                    manager.getMessage("RELOAD-SUCCESS", "&aStaff mode config reloaded.")
            ));
            return true;
        }

        if (args.length > 0) {
            if (sender instanceof Player player && args[0].equalsIgnoreCase(player.getName())) {
                return toggleSelf(player, manager);
            }

            if (!manager.canManageOthers(sender)) {
                sender.sendMessage(ColorUtils.toComponent(
                        manager.getStaffMessage(
                                "NO_PERMISSION_OTHERS",
                                "&c&lERROR &7>> &cYou don't have permission to manage other players' staff mode!"
                        )
                ));
                return true;
            }

            Player target = plugin.getServer().getPlayerExact(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(ColorUtils.toComponent("&cPlayer not online."));
                return true;
            }

            if (sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId())) {
                return toggleSelf(player, manager);
            }

            StaffModeManager.StaffModeToggleResult result = manager.isInStaffMode(target.getUniqueId())
                    ? manager.disable(target, false)
                    : manager.enable(target, false);
            if (!result.success()) {
                sender.sendMessage(ColorUtils.toComponent(
                        manager.getStaffMessage("TOGGLE_ERROR", "&c&lERROR &7>> &cFailed to toggle staff mode!")
                ));
                return true;
            }

            manager.notifyExternalToggle(sender, target, result.active());
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent(
                    manager.getMessage("PLAYER-ONLY", "&cOnly players can use this command.")
            ));
            return true;
        }

        return toggleSelf(player, manager);
    }

    private boolean toggleSelf(Player player, StaffModeManager manager) {
        if (!manager.isInStaffMode(player.getUniqueId()) && !manager.isEnabled()) {
            player.sendMessage(ColorUtils.toComponent(
                    manager.getMessage("FEATURE-DISABLED", "&cStaff mode is currently disabled.")
            ));
            return true;
        }

        if (!manager.canUse(player)) {
            player.sendMessage(ColorUtils.toComponent(
                    manager.getMessage("NO-PERMISSION", "&cYou do not have permission.")
            ));
            return true;
        }

        StaffModeManager.StaffModeToggleResult result = manager.toggle(player);
        if (!result.message().isBlank() && !result.success()) {
            player.sendMessage(ColorUtils.toComponent(result.message(), player));
        }
        return true;
    }
}
