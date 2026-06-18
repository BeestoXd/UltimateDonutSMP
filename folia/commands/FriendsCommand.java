package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.menus.FriendsMenu;
import com.bx.ultimateDonutSmp.models.FollowEntry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FriendsCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.friends";
    private static final String ADMIN_PERMISSION = "donutfriends.admin";

    private final UltimateDonutSmp plugin;

    public FriendsCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission(ADMIN_PERMISSION)) {
                    plugin.getConfigManager().reload();
                    sender.sendMessage(ColorUtils.colorize("&a[DonutFriends] Config reloaded successfully."));
                } else {
                    sender.sendMessage(ColorUtils.colorize("&cYou do not have permission."));
                }
                return true;
            }
            sender.sendMessage("Player only command.");
            return true;
        }

        if (!PermissionUtils.has(player, PERMISSION)) {
            player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        if (args.length == 0) {
            new FriendsMenu(plugin).open(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                if (PermissionUtils.has(player, ADMIN_PERMISSION)) {
                    plugin.getConfigManager().reload();
                    player.sendMessage(ColorUtils.toComponent("&a[DonutFriends] Config reloaded successfully."));
                } else {
                    player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
                }
            }
            case "list" -> new FriendsMenu(plugin, 0, null, FriendsMenu.FilterType.ALL).open(player);
            case "following" -> new FriendsMenu(plugin, 0, null, FriendsMenu.FilterType.FOLLOWING).open(player);
            case "followers" -> new FriendsMenu(plugin, 0, null, FriendsMenu.FilterType.FOLLOWERS).open(player);
            case "friends" -> new FriendsMenu(plugin, 0, null, FriendsMenu.FilterType.FRIENDS).open(player);
            case "add", "follow" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtils.toComponent("&cUsage: /friends follow <player>"));
                    return true;
                }
                handleFollow(player, args[1]);
            }
            case "remove", "unfollow" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtils.toComponent("&cUsage: /friends remove <player>"));
                    return true;
                }
                handleUnfollow(player, args[1]);
            }
            case "search" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtils.toComponent("&cUsage: /friends search <query>"));
                    return true;
                }
                new FriendsMenu(plugin, 0, args[1], FriendsMenu.FilterType.ALL).open(player);
            }
            default -> {
                player.sendMessage(ColorUtils.toComponent("&cUnknown subcommand. Usage: /friends [list|follow|remove|search|following|followers|friends]"));
            }
        }

        return true;
    }

    private void handleFollow(Player player, String targetName) {
        ResolvedTarget target = resolveTarget(player, targetName);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cPlayer not found."));
            return;
        }

        if (player.getUniqueId().equals(target.uuid())) {
            player.sendMessage(ColorUtils.toComponent("&cYou cannot follow yourself."));
            return;
        }

        if (plugin.getFriendsManager().isFollowing(player.getUniqueId(), target.uuid())) {
            player.sendMessage(ColorUtils.toComponent("&7You are already following &f" + target.name() + "&7."));
            return;
        }

        boolean success = plugin.getFriendsManager().followPlayer(player, target.uuid(), target.name());
        if (success) {
            player.sendMessage(ColorUtils.toComponent("&7You followed &f" + target.name() + "&7."));
        } else {
            player.sendMessage(ColorUtils.toComponent("&cCould not follow player."));
        }
    }

    private void handleUnfollow(Player player, String targetName) {
        ResolvedTarget target = resolveTarget(player, targetName);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cPlayer not found in your follows database."));
            return;
        }

        if (!plugin.getFriendsManager().isFollowing(player.getUniqueId(), target.uuid())) {
            player.sendMessage(ColorUtils.toComponent("&7You are not following &f" + target.name() + "&7."));
            return;
        }

        boolean success = plugin.getFriendsManager().unfollowPlayer(player, target.uuid());
        if (success) {
            player.sendMessage(ColorUtils.toComponent("&7You unfollowed &f" + target.name() + "&7."));
        } else {
            player.sendMessage(ColorUtils.toComponent("&cCould not unfollow player."));
        }
    }

    private ResolvedTarget resolveTarget(Player player, String input) {
        if (input == null || input.isBlank()) return null;

        Player online = plugin.getHideManager().findOnlinePlayer(player, input);
        if (online != null) {
            return new ResolvedTarget(online.getUniqueId(), plugin.getHideManager().plainPublicName(online));
        }

        for (FollowEntry entry : plugin.getFriendsManager().getFollowing(player.getUniqueId())) {
            if (entry.followedNameSnapshot().equalsIgnoreCase(input) || entry.followedUuid().toString().equalsIgnoreCase(input)) {
                return new ResolvedTarget(entry.followedUuid(), entry.followedNameSnapshot());
            }
        }

        UUID uuid = plugin.getHideManager().findKnownPlayerUuid(player, input);
        if (uuid == null) return null;

        String name = plugin.getDatabaseManager().getLastKnownUsername(uuid);
        String fallback = name == null || name.isBlank() ? input : name;
        return new ResolvedTarget(uuid, plugin.getHideManager().plainPublicName(uuid, fallback));
    }

    private record ResolvedTarget(UUID uuid, String name) {}
}
