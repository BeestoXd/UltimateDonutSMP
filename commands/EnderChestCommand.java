package com.bx.ultimateDonutSmp.commands;

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
            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                sender.sendMessage(ColorUtils.toComponent(
                        plugin.getEnderChestManager().getMessage("NO-PERMISSION", "&cYou do not have permission.")
                ));
                return true;
            }

            plugin.getConfigManager().reloadEnderChest();
            plugin.getEnderChestManager().reload();
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().getMessage("RELOAD-SUCCESS", "&aEnder Chest config reloaded.")
            ));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (!plugin.getEnderChestManager().isEnabled()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().getMessage(
                            "FEATURE-DISABLED",
                            "&cThe Ender Chest 6 Rows system is disabled."
                    )
            ));
            return true;
        }

        if (!plugin.getEnderChestManager().isCommandAllowed()) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().getMessage(
                            "COMMAND-DISABLED",
                            "&cThe /enderchest command is disabled."
                    )
            ));
            return true;
        }

        if (plugin.getEnderChestManager().commandRequiresPermission()
                && !player.hasPermission(plugin.getEnderChestManager().getCommandPermission())) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getEnderChestManager().getMessage("NO-PERMISSION", "&cYou do not have permission.")
            ));
            return true;
        }

        plugin.getEnderChestManager().open(player);
        return true;
    }
}
