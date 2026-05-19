package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class HealCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.staff.heal";

    private final UltimateDonutSmp plugin;

    public HealCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.hasPermission(PERMISSION)) {
            player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        if (args.length > 1) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " [player]"));
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

        heal(target);
        if (sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("HEAL.SELF", "&aʏᴏᴜʀ ʜᴇᴀʟᴛʜ ʜᴀѕ ʙᴇᴇɴ ʀᴇѕᴛᴏʀᴇᴅ!")
            ));
            return true;
        }

        String senderName = sender instanceof Player player ? player.getName() : sender.getName();
        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault("HEAL.OTHER", "&7ʏᴏᴜ ʜᴇᴀʟᴇᴅ &d%player%", "%player%", target.getName())
        ));
        target.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault("HEAL.NOTIFY", "&d%sender% &7ʀᴇѕᴛᴏʀᴇᴅ ʏᴏᴜʀ ʜᴇᴀʟᴛʜ", "%sender%", senderName)
        ));
        return true;
    }

    private void heal(Player target) {
        AttributeInstance maxHealth = target.getAttribute(Attribute.MAX_HEALTH);
        target.setHealth(maxHealth == null ? 20D : maxHealth.getValue());
        target.setFireTicks(0);
        target.setFallDistance(0F);
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
