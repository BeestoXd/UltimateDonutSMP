package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

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
        if (sender instanceof Player player && !PermissionUtils.has(player, PERMISSION)) {
            player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("ALTS.USAGE", "&cᴜѕᴀɢᴇ: /alts <player>")
            ));
            return true;
        }

        ProfileSnapshot snapshot = plugin.getProfileViewerManager().resolveProfile(args[0]).orElse(null);
        if (snapshot == null) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("ALTS.NOT_FOUND", "&cᴘʟᴀʏᴇʀ ɴᴏᴛ ꜰᴏᴜɴᴅ.")
            ));
            return true;
        }

        List<String> knownIps = plugin.getDatabaseManager().loadKnownIpAddresses(snapshot.getUuid());
        if (knownIps.isEmpty()) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("ALTS.NO_DATA", "&cɴᴏ ɪᴘ ʜɪѕᴛᴏʀʏ ꜰᴏᴜɴᴅ ꜰᴏʀ ᴛʜᴀᴛ ᴘʟᴀʏᴇʀ.")
            ));
            return true;
        }

        List<DatabaseManager.AltAccountMatch> matches = plugin.getDatabaseManager().loadAltAccounts(snapshot.getUuid());
        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault("ALTS.HEADER", "&8[&6ᴀʟᴛѕ&8] &e%player%")
                        .replace("%player%", snapshot.getUsername())
        ));
        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault("ALTS.KNOWN_IPS", "&7ᴋɴᴏᴡɴ ɪᴘѕ: &f%ips%")
                        .replace("%ips%", String.join(", ", knownIps))
        ));

        if (matches.isEmpty()) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault("ALTS.NONE", "&7ɴᴏ ᴀʟᴛᴇʀɴᴀᴛᴇ ᴀᴄᴄᴏᴜɴᴛѕ ꜰᴏᴜɴᴅ.")
            ));
            return true;
        }

        for (DatabaseManager.AltAccountMatch match : matches) {
            boolean online = Bukkit.getPlayer(match.uuid()) != null;
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getConfigManager().getMessageOrDefault(
                                    "ALTS.ENTRY",
                                    "&8- &e%player% &7[%status%&7] &fѕʜᴀʀᴇᴅ: %ips%"
                            )
                            .replace("%player%", match.username())
                            .replace("%status%", online ? "&aᴏɴʟɪɴᴇ" : "&cᴏꜰꜰʟɪɴᴇ")
                            .replace("%ips%", String.join(", ", match.sharedIps()))
            ));
        }
        return true;
    }
}
