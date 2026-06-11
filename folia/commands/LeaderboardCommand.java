package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.LeaderboardManager;
import com.bx.ultimateDonutSmp.menus.LeaderboardMenu;
import com.bx.ultimateDonutSmp.menus.LeaderboardTypeMenu;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.stream.Collectors;

public class LeaderboardCommand implements CommandExecutor, Listener {

    private final UltimateDonutSmp plugin;

    public LeaderboardCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBaltop(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().trim().equalsIgnoreCase("/baltop")) {
            return;
        }

        event.setCancelled(true);
        onCommand(event.getPlayer(), null, "baltop", new String[0]);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("ᴘʟᴀʏᴇʀ ᴏɴʟʏ."); return true; }
        if (!plugin.getConfigManager().isCommandEnabled("LEADERBOARDS")) {
            player.sendMessage(ColorUtils.toComponent("&cʟᴇᴀᴅᴇʀʙᴏᴀʀᴅѕ ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ ᴅɪѕᴀʙʟᴇᴅ."));
            return true;
        }

        if (args.length == 0 && label.equalsIgnoreCase("baltop")) {
            new LeaderboardTypeMenu(plugin, LeaderboardManager.LeaderboardType.MONEY).open(player);
            return true;
        }

        if (args.length == 0) {
            new LeaderboardMenu(plugin).open(player);
            return true;
        }

        var type = plugin.getLeaderboardManager().parseType(args[0]).orElse(null);
        if (type == null) {
            String available = plugin.getLeaderboardManager().getTypes().stream()
                    .map(leaderboardType -> leaderboardType.getConfigKey())
                    .collect(Collectors.joining(", "));
            player.sendMessage(ColorUtils.toComponent("&cᴛɪᴘᴇ ʟᴇᴀᴅᴇʀʙᴏᴀʀᴅ ᴛɪᴅᴀᴋ ᴠᴀʟɪᴅ. &7ᴀᴠᴀɪʟᴀʙʟᴇ: &f" + available));
            return true;
        }

        new LeaderboardTypeMenu(plugin, type).open(player);
        return true;
    }
}
