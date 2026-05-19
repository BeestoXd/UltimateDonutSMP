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
            sender.sendMessage(ColorUtils.toComponent("&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
            return true;
        }

        if (!plugin.getStaffModeManager().canUseRandomTeleport(player)) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getStaffModeManager().getMessage("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
            ));
            return true;
        }

        if (plugin.getStaffModeManager().teleportToRandomPlayer(player) == null) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("RANDOMTP.NO_PLAYERS", "&cɴᴏ ᴏᴛʜᴇʀ ᴘʟᴀʏᴇʀѕ ᴀᴠᴀɪʟᴀʙʟᴇ ꜰᴏʀ ʀᴀɴᴅᴏᴍ ᴛᴇʟᴇᴘᴏʀᴛ")
            ));
        }
        return true;
    }
}
