package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.RulesMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RulesCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public RulesCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only."); return true; }
        if (!plugin.getConfigManager().isCommandEnabled("RULES")) {
            player.sendMessage(ColorUtils.toComponent("&cRules command is currently disabled."));
            return true;
        }

        RulesMenu menu = new RulesMenu(plugin);
        if (menu.hasValidButtons()) {
            menu.open(player);
            return true;
        }

        sendLegacyRules(player);
        return true;
    }

    private void sendLegacyRules(Player player) {
        player.sendMessage(ColorUtils.toComponent("&7&m---------- &bServer Rules &7&m----------"));
        player.sendMessage(ColorUtils.toComponent("&71. &fNo cheating or hacking"));
        player.sendMessage(ColorUtils.toComponent("&72. &fBe respectful to other players"));
        player.sendMessage(ColorUtils.toComponent("&73. &fNo griefing at spawn"));
        player.sendMessage(ColorUtils.toComponent("&74. &fNo use of exploits or duplication"));
        player.sendMessage(ColorUtils.toComponent("&75. &fHave fun!"));
        player.sendMessage(ColorUtils.toComponent("&7&m-----------------------------------"));
    }
}
