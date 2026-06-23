package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class TeleportCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.staff.teleport";

    private final UltimateDonutSmp plugin;

    public TeleportCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
            return true;
        }

        if (!PermissionUtils.has(player, PERMISSION)) {
            player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }

        String normalizedLabel = label.toLowerCase(Locale.ROOT);
        if ("tphere".equals(normalizedLabel)) {
            return handleTeleportHereAlias(player, args);
        }
        if ("tpall".equals(normalizedLabel)) {
            return handleTeleportAllAlias(player, args);
        }

        if (args.length == 0) {
            sendUsage(player, label);
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("all")) {
                teleportAll(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("top")) {
                teleportTop(player);
                return true;
            }
            teleportToPlayer(player, args[0]);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("here")) {
            teleportHere(player, args[1]);
            return true;
        }

        if (args.length == 3 || args.length == 4) {
            teleportToPosition(player, args);
            return true;
        }

        sendUsage(player, label);
        return true;
    }

    private boolean handleTeleportHereAlias(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /tphere <player>"));
            return true;
        }
        teleportHere(player, args[0]);
        return true;
    }

    private boolean handleTeleportAllAlias(Player player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /tpall"));
            return true;
        }
        teleportAll(player);
        return true;
    }

    private void teleportToPlayer(Player player, String input) {
        Player target = findOnlinePlayer(input);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cᴘʟᴀʏᴇʀ ɴᴏᴛ ᴏɴʟɪɴᴇ."));
            return;
        }

        String targetName = target.getName();
        plugin.getFoliaScheduler().teleport(player, target.getLocation()).thenAccept(success ->
                plugin.getFoliaScheduler().runEntity(player, () -> {
                    if (Boolean.TRUE.equals(success) && player.isOnline()) {
                        player.sendMessage(ColorUtils.toComponent(
                                message("TELEPORT.TO_PLAYER", "&dᴛᴇʟᴇᴘᴏʀᴛᴇᴅ &7ᴛᴏ %player%")
                                        .replace("%player%", targetName),
                                player
                        ));
                    }
                }));
    }

    private void teleportHere(Player player, String input) {
        Player target = findOnlinePlayer(input);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cᴘʟᴀʏᴇʀ ɴᴏᴛ ᴏɴʟɪɴᴇ."));
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴛᴇʟᴇᴘᴏʀᴛ ʏᴏᴜʀѕᴇʟꜰ ᴛᴏ ʏᴏᴜʀѕᴇʟꜰ."));
            return;
        }

        Location destination = player.getLocation();
        String targetName = target.getName();
        String senderName = player.getName();
        plugin.getFoliaScheduler().teleport(target, destination).thenAccept(success -> {
            if (!Boolean.TRUE.equals(success)) {
                return;
            }
            plugin.getFoliaScheduler().runEntity(player, () -> {
                if (player.isOnline()) {
                    player.sendMessage(ColorUtils.toComponent(
                            message("TELEPORT.HERE", "&dᴛᴇʟᴇᴘᴏʀᴛᴇᴅ &7%player% ᴛᴏ ʏᴏᴜʀ ʟᴏᴄᴀᴛɪᴏɴ")
                                    .replace("%player%", targetName),
                            player
                    ));
                }
            });
            plugin.getFoliaScheduler().runEntity(target, () -> {
                if (target.isOnline()) {
                    target.sendMessage(ColorUtils.toComponent(
                            message("TELEPORT.HERE_TARGET", "&dʏᴏᴜ ᴡᴇʀᴇ ᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ &7%sender%")
                                    .replace("%sender%", senderName),
                            target
                    ));
                }
            });
        });
    }

    private void teleportAll(Player player) {
        int moved = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().equals(target.getUniqueId())) {
                continue;
            }
            String senderName = player.getName();
            Location destination = player.getLocation();
            plugin.getFoliaScheduler().teleport(target, destination).thenAccept(success ->
                    plugin.getFoliaScheduler().runEntity(target, () -> {
                        if (Boolean.TRUE.equals(success) && target.isOnline()) {
                            target.sendMessage(ColorUtils.toComponent(
                                    message("TELEPORT.ALL_TARGET", "&dʏᴏᴜ ᴡᴇʀᴇ ᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ &7%sender%")
                                            .replace("%sender%", senderName),
                                    target
                            ));
                        }
                    }));
            moved++;
        }

        if (moved == 0) {
            player.sendMessage(ColorUtils.toComponent("&cɴᴏ ᴏᴛʜᴇʀ ᴘʟᴀʏᴇʀѕ ᴏɴʟɪɴᴇ."));
            return;
        }

        player.sendMessage(ColorUtils.toComponent(
                message("TELEPORT.ALL", "&dᴛᴇʟᴇᴘᴏʀᴛᴇᴅ &7ᴀʟʟ ᴘʟᴀʏᴇʀѕ ᴛᴏ ʏᴏᴜʀ ʟᴏᴄᴀᴛɪᴏɴ"),
                player
        ));
    }

    private void teleportTop(Player player) {
        Location current = player.getLocation().clone();
        int highestY = current.getWorld().getHighestBlockYAt(current) + 1;
        current.setY(highestY);
        plugin.getFoliaScheduler().teleport(player, current).thenAccept(success ->
                plugin.getFoliaScheduler().runEntity(player, () -> {
                    if (Boolean.TRUE.equals(success) && player.isOnline()) {
                        player.sendMessage(ColorUtils.toComponent(
                                message("TELEPORT.TOP", "&dᴛᴇʟᴇᴘᴏʀᴛᴇᴅ &7ᴛᴏ ᴛʜᴇ ʜɪɢʜᴇѕᴛ ᴘᴏѕɪᴛɪᴏɴ"),
                                player
                        ));
                    }
                }));
    }

    private void teleportToPosition(Player player, String[] args) {
        Double x = parseCoordinate(args[0]);
        Double y = parseCoordinate(args[1]);
        Double z = parseCoordinate(args[2]);
        if (x == null || y == null || z == null) {
            player.sendMessage(ColorUtils.toComponent("&cᴄᴏᴏʀᴅɪɴᴀᴛᴇѕ ᴍᴜѕᴛ ʙᴇ ᴠᴀʟɪᴅ ɴᴜᴍʙᴇʀѕ."));
            return;
        }

        World world = player.getWorld();
        if (args.length == 4) {
            world = Bukkit.getWorld(args[3]);
            if (world == null) {
                player.sendMessage(ColorUtils.toComponent("&cᴡᴏʀʟᴅ ɴᴏᴛ ꜰᴏᴜɴᴅ."));
                return;
            }
        }

        Location destination = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
        World destinationWorld = world;
        plugin.getFoliaScheduler().teleport(player, destination).thenAccept(success ->
                plugin.getFoliaScheduler().runEntity(player, () -> {
                    if (Boolean.TRUE.equals(success) && player.isOnline()) {
                        player.sendMessage(ColorUtils.toComponent(
                                message("TELEPORT.POSITION", "&7ᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ: &d%x%,%y%,%z% &7(%world%)")
                                        .replace("%x%", formatCoordinate(x))
                                        .replace("%y%", formatCoordinate(y))
                                        .replace("%z%", formatCoordinate(z))
                                        .replace("%world%", destinationWorld.getName()),
                                player
                        ));
                    }
                }));
    }

    private void sendUsage(Player player, String label) {
        player.sendMessage(ColorUtils.toComponent(
                "&cᴜѕᴀɢᴇ: /" + label + " <player|here <player>|ᴀʟʟ|ᴛᴏᴘ|x ʏ ᴢ [ᴡᴏʀʟᴅ]>"
        ));
    }

    private String message(String path, String fallback) {
        return plugin.getConfigManager().getMessageOrDefault(path, fallback);
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

    private Double parseCoordinate(String input) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String formatCoordinate(double value) {
        if (Math.rint(value) == value) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.US, "%.2f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }
}
