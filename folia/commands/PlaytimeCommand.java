package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.PlayerData;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;

public class PlaytimeCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public PlaytimeCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " [player]"));
            return true;
        }

        Player target = null;
        String requestedName = null;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <player>"));
                return true;
            }
            target = player;
            requestedName = player.getName();
        } else {
            requestedName = args[0];
            target = findOnlinePlayer(requestedName);
        }

        PlayerData data = resolvePlayerData(target, requestedName);
        if (data == null) {
            sender.sendMessage(ColorUtils.toComponent("&cPlayer not found."));
            return true;
        }

        String targetName = target != null
                ? target.getName()
                : resolveStoredName(data, requestedName);
        long seconds = target != null
                ? data.getTotalPlaytimeSeconds()
                : data.getPlaytimeSeconds();
        String time = NumberUtils.formatTimeLong(seconds);
        String server = resolveServerName();

        boolean selfRequest = args.length == 0 || (sender instanceof Player player
                && target != null
                && player.getUniqueId().equals(target.getUniqueId()));
        String path = selfRequest ? "PLAYTIME.MESSAGE" : "PLAYTIME.OTHER";
        String fallback = selfRequest
                ? "&a%time% &eplaying on &a%server%"
                : "&e%player% &7has played for &a%time% &7on &a%server%";

        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault(
                        path,
                        fallback,
                        "%player%",
                        targetName,
                        "%time%",
                        time,
                        "%server%",
                        server
                )
        ));
        return true;
    }

    private PlayerData resolvePlayerData(Player target, String requestedName) {
        if (target != null) {
            PlayerData cached = plugin.getPlayerDataManager().get(target);
            return cached != null ? cached : plugin.getDatabaseManager().loadPlayer(target.getUniqueId());
        }

        UUID uuid = plugin.getDatabaseManager().findPlayerUuidByUsername(requestedName);
        return uuid == null ? null : plugin.getDatabaseManager().loadPlayer(uuid);
    }

    private String resolveStoredName(PlayerData data, String requestedName) {
        String username = data.getUsername();
        if (username != null && !username.isBlank()) {
            return username;
        }
        return requestedName == null || requestedName.isBlank() ? "Unknown" : requestedName;
    }

    private String resolveServerName() {
        String displayName = plugin.getConfigManager().getNetwork()
                .getString("NETWORK-STATUS.LOCAL-DISPLAY-NAME", "");
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }

        String serverId = plugin.getConfigManager().getNetwork()
                .getString("NETWORK-STATUS.LOCAL-SERVER-ID", "");
        if (serverId != null && !serverId.isBlank()) {
            return serverId;
        }

        return "server";
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
