package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public StatsCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Player only."); return true; }

        OfflinePlayer target = args.length > 0 ? Bukkit.getOfflinePlayer(args[0]) : player;
        PlayerData data = plugin.getPlayerDataManager().get(target.getUniqueId());
        if (data == null) data = plugin.getDatabaseManager().loadPlayer(target.getUniqueId());
        if (data == null) { player.sendMessage(ColorUtils.toComponent("&cPlayer not found.")); return true; }

        String name = target.getName() != null ? target.getName() : args[0];
        player.sendMessage(ColorUtils.toComponent("&7&m------------------"));
        player.sendMessage(ColorUtils.toComponent("&b" + name + "&7's stats:"));
        player.sendMessage(ColorUtils.toComponent("&#00FC00$ &fMoney: &a$" + NumberUtils.formatNice(data.getMoney())));
        player.sendMessage(ColorUtils.toComponent("&#A303F9★ &fShards: &#A303F9" + data.getShards()));
        player.sendMessage(ColorUtils.toComponent("&#FC0000⚔ &fKills: &#FC0000" + data.getKills()));
        player.sendMessage(ColorUtils.toComponent("&#F97603☠ &fDeaths: &#F97603" + data.getDeaths()));
        player.sendMessage(ColorUtils.toComponent("&#FCE300⌚ &fPlaytime: &#FCE300" + NumberUtils.formatTimeLong(data.getTotalPlaytimeSeconds())));

        String teamName = plugin.getTeamManager().getTeam(target.getUniqueId()) != null
                ? plugin.getTeamManager().getTeam(target.getUniqueId()).getName()
                : null;
        player.sendMessage(ColorUtils.toComponent("&#00A4FC⚑ &fTeam: &#00A4FC" + (teamName != null ? teamName.toUpperCase() : "None")));
        player.sendMessage(ColorUtils.toComponent("&7&m------------------"));
        return true;
    }
}
