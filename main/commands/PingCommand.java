package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public PingCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " [ᴘʟᴀʏᴇʀ]"));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " <player>"));
                return true;
            }

            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                            "PING.SELF",
                            "&7ʏᴏᴜʀ ᴘɪɴɢ ɪѕ &b%ping%ᴍѕ",
                            "%ping%",
                            String.valueOf(player.getPing())
                    ),
                    player
            ));
            return true;
        }

        Player target = plugin.getHideManager().findOnlinePlayer(sender, args[0]);
        if (target == null) {
            sender.sendMessage(ColorUtils.toComponent("&cᴘʟᴀʏᴇʀ ɴᴏᴛ ᴏɴʟɪɴᴇ."));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault(
                        "PING.OTHER",
                        "&e%player%'ѕ &7ᴘɪɴɢ ɪѕ &b%ping%ᴍѕ",
                        "%player%",
                        plugin.getHideManager().publicName(target),
                        "%ping%",
                        String.valueOf(target.getPing())
                )
        ));
        return true;
    }

}
