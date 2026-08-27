package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.FarmingMetaManager;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MetaCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public MetaCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FarmingMetaManager farmingMetaManager = plugin.getFarmingMetaManager();

        if (!farmingMetaManager.isActive()) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessage("WORTH.META-INACTIVE")));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent(
                farmingMetaManager.formatMetaMessage("WORTH.META-CURRENT")));
        return true;
    }
}
