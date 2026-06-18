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
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }

        if (plugin.getCombatManager().isInCombat(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getConfig()
                    .getString("COMBAT-MANAGER.BLOCK-MESSAGE", "&cʏᴏᴜ ᴄᴀɴ'ᴛ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ ɪɴ ᴄᴏᴍʙᴀᴛ.")));
            return true;
        }

        if (plugin.getSpawnManager().shouldOpenMenu(SpawnManager.AreaType.SPAWN)) {
            new SpawnMenu(plugin).open(player);
            return true;
        }

        Location destination = plugin.getSpawnManager().resolveCommandDestination(SpawnManager.AreaType.SPAWN);
        if (destination == null) {
            player.sendMessage(ColorUtils.toComponent("&cѕᴘᴀᴡɴ ʟᴏᴄᴀᴛɪᴏɴ ɪѕ ɴᴏᴛ ѕᴇᴛ."));
            return true;
        }

        plugin.getTeleportManager().queue(player, destination, "SPAWN", null);
        return true;
    }
}
