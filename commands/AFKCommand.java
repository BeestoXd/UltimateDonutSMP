package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.SpawnManager;
import com.bx.ultimateDonutSmp.menus.AfkMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AFKCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public AFKCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (!plugin.getConfigManager().isCommandEnabled("AFK")) {
            player.sendMessage(ColorUtils.toComponent("&cAFK command is currently disabled."));
            return true;
        }

        if (plugin.getCombatManager().isInCombat(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getConfig()
                    .getString("COMBAT-MANAGER.BLOCK-MESSAGE", "&cYou can't use this in combat.")));
            return true;
        }

        if (plugin.getSpawnManager().hasMenuDefinition(SpawnManager.AreaType.AFK)) {
            new AfkMenu(plugin).open(player);
            return true;
        }

        Location destination = plugin.getSpawnManager().resolveCommandDestination(SpawnManager.AreaType.AFK);
        if (destination == null) {
            player.sendMessage(ColorUtils.toComponent("&cAFK location is not set."));
            return true;
        }

        plugin.getTeleportManager().queue(player, destination, "AFK", null);
        return true;
    }
}
