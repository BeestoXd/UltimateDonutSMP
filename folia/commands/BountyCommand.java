package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.menus.BountyConfirmMenu;
import com.bx.ultimateDonutSmp.menus.BountyMenu;
import com.bx.ultimateDonutSmp.models.Bounty;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BountyCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public BountyCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            new BountyMenu(plugin).open(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add", "set" -> handleAdd(player, args);
            case "info" -> handleInfo(player, args);
            default -> player.sendMessage(ColorUtils.toComponent("&cUsage: /bounty <add|info|list> [player] [amount]"));
        }
        return true;
    }

    private void handleAdd(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /bounty add <player> <amount>"));
            return;
        }

        UUID targetUuid = plugin.getBountyManager().resolvePlayerUuid(args[1]);
        if (targetUuid == null) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BOUNTY.PLAYER-NOT-EXIST")));
            return;
        }

        if (targetUuid.equals(player.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BOUNTY.CANT-SELF-BOUNTY")));
            return;
        }

        double amount;
        try {
            amount = NumberUtils.parse(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BALANCE.PAY.INVALID-AMOUNT")));
            return;
        }

        if (amount < 1) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BOUNTY.MINIMUM-PRICE")));
            return;
        }

        new BountyConfirmMenu(
                plugin,
                targetUuid,
                plugin.getBountyManager().getDisplayName(targetUuid),
                amount
        ).open(player);
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /bounty info <player>"));
            return;
        }

        UUID targetUuid = plugin.getBountyManager().resolvePlayerUuid(args[1]);
        if (targetUuid == null) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BOUNTY.PLAYER-NOT-EXIST")));
            return;
        }

        Bounty bounty = plugin.getBountyManager().getBounty(targetUuid);
        if (bounty == null) {
            player.sendMessage(ColorUtils.toComponent(plugin.getConfigManager().getMessage("BOUNTY.NO-BOUNTY")));
            return;
        }

        String msg = plugin.getConfigManager().getMessage("BOUNTY.PLAYER-HAS-BOUNTY",
                "{player}", plugin.getBountyManager().getDisplayName(targetUuid),
                "{amount}", NumberUtils.format(bounty.getAmount()));
        player.sendMessage(ColorUtils.toComponent(msg));
    }
}
