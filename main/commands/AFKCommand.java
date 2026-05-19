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
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }

        if (!plugin.getConfigManager().isCommandEnabled("AFK")) {
            player.sendMessage(ColorUtils.toComponent("&cᴀꜰᴋ ᴄᴏᴍᴍᴀɴᴅ ɪѕ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."));
            return true;
        }

        if (plugin.getCombatManager().isInCombat(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getConfig()
                    .getString("COMBAT-MANAGER.BLOCK-MESSAGE", "&cʏᴏᴜ ᴄᴀɴ'ᴛ ᴜѕᴇ ᴛʜɪѕ ɪɴ ᴄᴏᴍʙᴀᴛ.")));
            return true;
        }

        if (plugin.getSpawnManager().shouldOpenMenu(SpawnManager.AreaType.AFK)) {
            new AfkMenu(plugin).open(player);
            return true;
        }

        Location destination = plugin.getSpawnManager().resolveCommandDestination(SpawnManager.AreaType.AFK);
        if (destination == null) {
            player.sendMessage(ColorUtils.toComponent("&cᴀꜰᴋ ʟᴏᴄᴀᴛɪᴏɴ ɪѕ ɴᴏᴛ ѕᴇᴛ."));
            return true;
        }

        plugin.getTeleportManager().queue(player, destination, "AFK", null);
        return true;
    }
}
