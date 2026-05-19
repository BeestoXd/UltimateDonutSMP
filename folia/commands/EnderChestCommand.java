package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderChestCommand implements CommandExecutor {

    private static final String ADMIN_PERMISSION = "ultimatedonutsmp.admin.enderchest";

    private final UltimateDonutSmp plugin;

    public EnderChestCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!PermissionUtils.has(sender, ADMIN_PERMISSION)) {
                sender.sendMessage(ColorUtils.toComponent(
                        plugin.getEnderChestManager().getMessage("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
                ));
                return true;
            }

            plugin.getConfigManager().reloadEnderChest();
            plugin.getEnderChestManager().reload();
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().getMessage("RELOAD-SUCCESS", "&aᴇɴᴅᴇʀ ᴄʜᴇѕᴛ ᴄᴏɴꜰɪɢ ʀᴇʟᴏᴀᴅᴇᴅ.")
            ));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ.");
            return true;
        }

        if (!plugin.getEnderChestManager().isEnabled()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().getMessage(
                            "FEATURE-DISABLED",
                            "&cᴛʜᴇ ᴇɴᴅᴇʀ ᴄʜᴇѕᴛ 6 ʀᴏᴡѕ ѕʏѕᴛᴇᴍ ɪѕ ᴅɪѕᴀʙʟᴇᴅ."
                    )
            ));
            return true;
        }

        if (!plugin.getEnderChestManager().isCommandAllowed()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().getMessage(
                            "COMMAND-DISABLED",
                            "&cᴛʜᴇ /enderchest ᴄᴏᴍᴍᴀɴᴅ ɪѕ ᴅɪѕᴀʙʟᴇᴅ."
                    )
            ));
            return true;
        }

        if (plugin.getEnderChestManager().commandRequiresPermission()
                && !PermissionUtils.has(player, plugin.getEnderChestManager().getCommandPermission())) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().getMessage("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
            ));
            return true;
        }

        plugin.getEnderChestManager().open(player);
        return true;
    }
}
