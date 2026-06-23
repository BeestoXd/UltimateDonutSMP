package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class FeedCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.staff.feed";

    private final UltimateDonutSmp plugin;

    public FeedCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !PermissionUtils.has(player, PERMISSION)) {
            player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        if (args.length > 1) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " [ᴘʟᴀʏᴇʀ]"));
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " <player>"));
                return true;
            }
            target = player;
        } else {
            target = findOnlinePlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ColorUtils.toComponent("&cᴘʟᴀʏᴇʀ ɴᴏᴛ ᴏɴʟɪɴᴇ."));
                return true;
            }
        }

        feed(target);
        if (sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("FEED.SELF", "&7ʏᴏᴜʀ ʜᴜɴɢᴇʀ ʜᴀѕ ʙᴇᴇɴ ѕᴀᴛɪѕꜰɪᴇᴅ!")
            ));
            return true;
        }

        String senderName = sender instanceof Player player ? player.getName() : sender.getName();
        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault("FEED.OTHER", "&7ʏᴏᴜ ꜰᴇᴅ &d%player%", "%player%", target.getName())
        ));
        target.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault("FEED.NOTIFY", "&d%sender% &7ʀᴇѕᴛᴏʀᴇᴅ ʏᴏᴜʀ ʜᴜɴɢᴇʀ", "%sender%", senderName)
        ));
        return true;
    }

    private void feed(Player target) {
        target.setFoodLevel(20);
        target.setSaturation(20F);
        target.setExhaustion(0F);
    }

    private Player findOnlinePlayer(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        Player exact = Bukkit.getPlayerExact(input);
        if (exact != null) {
            return exact;
        }

        String expected = input.toLowerCase(Locale.ROOT);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase(Locale.ROOT).equals(expected)) {
                return player;
            }
        }
        return null;
    }
}
