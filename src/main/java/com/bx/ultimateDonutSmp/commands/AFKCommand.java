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

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

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

        if (args.length > 0 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("setafk") || args[0].equalsIgnoreCase("setup"))) {
            if (!PermissionUtils.has(player, "ultimatedonutsmp.command.setafk") && !PermissionUtils.has(player, "ultimatedonutsmp.admin.setup")) {
                player.sendMessage(ColorUtils.toComponent("&cYou do not have permission to set AFK."));
                return true;
            }

            Location location = player.getLocation();
            SpawnManager.SetupLocationResult result = plugin.getSpawnManager().setAfkLocation(location);
            if (!result.success()) {
                player.sendMessage(ColorUtils.toComponent("&cAFK location could not be saved: &f" + result.message()));
                return true;
            }

            player.sendMessage(ColorUtils.toComponent("&aAFK location saved. &7(World: &f" + (location.getWorld() == null ? "unknown" : location.getWorld().getName())
                    + "&7, X: &f" + String.format("%.1f", location.getX())
                    + "&7, Y: &f" + String.format("%.1f", location.getY())
                    + "&7, Z: &f" + String.format("%.1f", location.getZ()) + "&7)"));
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
