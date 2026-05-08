package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.SpawnManager;
import com.bx.ultimateDonutSmp.menus.SpawnMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public SpawnCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (plugin.getCombatManager().isInCombat(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getConfig()
                    .getString("COMBAT-MANAGER.BLOCK-MESSAGE", "&cYou can't use this command in combat.")));
            return true;
        }

        if (plugin.getSpawnManager().hasMenuDefinition(SpawnManager.AreaType.SPAWN)) {
            new SpawnMenu(plugin).open(player);
            return true;
        }

        Location destination = plugin.getSpawnManager().resolveCommandDestination(SpawnManager.AreaType.SPAWN);
        if (destination == null) {
            player.sendMessage(ColorUtils.toComponent("&cSpawn location is not set."));
            return true;
        }

        plugin.getTeleportManager().queue(player, destination, "SPAWN", null);
        return true;
    }
}
