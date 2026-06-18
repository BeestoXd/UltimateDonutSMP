package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.DuelCreateMenu;
import com.bx.ultimateDonutSmp.models.DuelMapSelection;
import com.bx.ultimateDonutSmp.models.DuelPrivacyMode;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public CreateCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (!plugin.getDuelManager().isEnabled()) {
            player.sendMessage(ColorUtils.toComponent("&cDuels are currently disabled."));
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        DuelPrivacyMode privacyMode = DuelPrivacyMode.INVITE_ONLY;
        int targetIndex = 0;
        String mode = args[0].toLowerCase();
        if ("invite".equals(mode) || "invites".equals(mode)) {
            privacyMode = DuelPrivacyMode.INVITE_ONLY;
            targetIndex = 1;
        } else if ("friends".equals(mode) || "friend".equals(mode)) {
            privacyMode = DuelPrivacyMode.FRIENDS_ONLY;
            targetIndex = 1;
        }

        if (args.length <= targetIndex) {
            sendUsage(player);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[targetIndex]);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cThat player is not online."));
            return true;
        }

        if (args.length > targetIndex + 1) {
            DuelMapSelection selection = plugin.getDuelManager().parseMapSelection(args[targetIndex + 1]);
            plugin.getDuelManager().sendChallenge(player, target, selection, privacyMode);
            return true;
        }

        new DuelCreateMenu(plugin, target.getUniqueId(), privacyMode).open(player);
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ColorUtils.toComponent("&cUsage: /create invite <player> [map]"));
        player.sendMessage(ColorUtils.toComponent("&cUsage: /create friends <player> [map]"));
    }
}
