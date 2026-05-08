package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomTeleportCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public RandomTeleportCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can use this command."));
            return true;
        }

        if (!plugin.getStaffModeManager().canUseRandomTeleport(player)) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getStaffModeManager().getMessage("NO-PERMISSION", "&cYou do not have permission.")
            ));
            return true;
        }

        if (plugin.getStaffModeManager().teleportToRandomPlayer(player) == null) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("RANDOMTP.NO_PLAYERS", "&cNo other players available for random teleport")
            ));
        }
        return true;
    }
}
