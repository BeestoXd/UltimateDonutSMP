package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public VanishCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getStaffModeManager().getMessage("PLAYER-ONLY", "&cOnly players can use this command.")
            ));
            return true;
        }

        if (!plugin.getStaffModeManager().canUseVanish(player)) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getStaffModeManager().getMessage("NO-PERMISSION", "&cYou do not have permission.")
            ));
            return true;
        }

        if (!plugin.getStaffModeManager().isInStaffMode(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cYou must be in Staff Mode to use /vanish."));
            return true;
        }

        plugin.getStaffModeManager().toggleVanish(player);
        return true;
    }
}
