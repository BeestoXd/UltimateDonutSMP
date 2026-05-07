package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.DatabaseManager;
import com.bx.ultimateDonutSmp.models.ProfileSnapshot;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AltsCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.staff.alts";

    private final UltimateDonutSmp plugin;

    public AltsCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.hasPermission(PERMISSION)) {
            player.sendMessage(ColorUtils.toComponent("&cYou do not have permission."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("ALTS.USAGE", "&cUsage: /alts <player>")
            ));
            return true;
        }

        ProfileSnapshot snapshot = plugin.getProfileViewerManager().resolveProfile(args[0]).orElse(null);
        if (snapshot == null) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("ALTS.NOT_FOUND", "&cPlayer not found.")
            ));
            return true;
        }

        List<String> knownIps = plugin.getDatabaseManager().loadKnownIpAddresses(snapshot.getUuid());
        if (knownIps.isEmpty()) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("ALTS.NO_DATA", "&cNo IP history found for that player.")
            ));
            return true;
        }

        List<DatabaseManager.AltAccountMatch> matches = plugin.getDatabaseManager().loadAltAccounts(snapshot.getUuid());
        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault("ALTS.HEADER", "&8[&6Alts&8] &e%player%")
                        .replace("%player%", snapshot.getUsername())
        ));
        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault("ALTS.KNOWN_IPS", "&7Known IPs: &f%ips%")
                        .replace("%ips%", String.join(", ", knownIps))
        ));

        if (matches.isEmpty()) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("ALTS.NONE", "&7No alternate accounts found.")
            ));
            return true;
        }

        for (DatabaseManager.AltAccountMatch match : matches) {
            boolean online = Bukkit.getPlayer(match.uuid()) != null;
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                                    "ALTS.ENTRY",
                                    "&8- &e%player% &7[%status%&7] &fshared: %ips%"
                            )
                            .replace("%player%", match.username())
                            .replace("%status%", online ? "&aOnline" : "&cOffline")
                            .replace("%ips%", String.join(", ", match.sharedIps()))
            ));
        }
        return true;
    }
}
