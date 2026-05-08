package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.BillfordMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.SoundUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BillfordCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public BillfordCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        String permission = plugin.getConfigManager().getBillford()
                .getString("ACCESS.PERMISSION", "");
        if (!permission.isBlank() && !player.hasPermission(permission)) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessage(
                            "BILLFORD.NO-PERMISSION",
                            "{permission}",
                            permission
                    )
            ));
            return true;
        }

        SoundUtils.play(player, plugin.getConfigManager().getSound("BILLFORD.OPEN"));
        new BillfordMenu(plugin).open(player);
        return true;
    }
}
