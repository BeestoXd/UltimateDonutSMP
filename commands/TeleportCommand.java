package com.bx.ultimateDonutSmp.commands;

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
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(ColorUtils.toComponent("&cYou do not have permission."));
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
            player.sendMessage(ColorUtils.toComponent("&cUsage: /tphere <player>"));
            return true;
        }
        teleportHere(player, args[0]);
        return true;
    }

    private boolean handleTeleportAllAlias(Player player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /tpall"));
            return true;
        }
        teleportAll(player);
        return true;
    }

    private void teleportToPlayer(Player player, String input) {
        Player target = findOnlinePlayer(input);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cPlayer not online."));
            return;
        }

        String targetName = target.getName();
        plugin.getPaperScheduler().teleport(player, target.getLocation()).thenAccept(success ->
                plugin.getPaperScheduler().runEntity(player, () -> {
                    if (Boolean.TRUE.equals(success) && player.isOnline()) {
                        player.sendMessage(ColorUtils.toComponent(
                                message("TELEPORT.TO_PLAYER", "&dTeleported &7to %player%")
                                        .replace("%player%", targetName),
                                player
                        ));
                    }
                }));
    }

    private void teleportHere(Player player, String input) {
        Player target = findOnlinePlayer(input);
        if (target == null) {
            player.sendMessage(ColorUtils.toComponent("&cPlayer not online."));
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(ColorUtils.toComponent("&cYou cannot teleport yourself to yourself."));
            return;
        }

        Location destination = player.getLocation();
        String targetName = target.getName();
        String senderName = player.getName();
        plugin.getPaperScheduler().teleport(target, destination).thenAccept(success -> {
            if (!Boolean.TRUE.equals(success)) {
                return;
            }
            plugin.getPaperScheduler().runEntity(player, () -> {
                if (player.isOnline()) {
                    player.sendMessage(ColorUtils.toComponent(
                            message("TELEPORT.HERE", "&dTeleported &7%player% to your location")
                                    .replace("%player%", targetName),
                            player
                    ));
                }
            });
            plugin.getPaperScheduler().runEntity(target, () -> {
                if (target.isOnline()) {
                    target.sendMessage(ColorUtils.toComponent(
                            message("TELEPORT.HERE_TARGET", "&dYou were teleported to &7%sender%")
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
            plugin.getPaperScheduler().teleport(target, destination).thenAccept(success ->
                    plugin.getPaperScheduler().runEntity(target, () -> {
                        if (Boolean.TRUE.equals(success) && target.isOnline()) {
                            target.sendMessage(ColorUtils.toComponent(
                                    message("TELEPORT.ALL_TARGET", "&dYou were teleported to &7%sender%")
                                            .replace("%sender%", senderName),
                                    target
                            ));
                        }
                    }));
            moved++;
        }

        if (moved == 0) {
            player.sendMessage(ColorUtils.toComponent("&cNo other players online."));
            return;
        }

        player.sendMessage(ColorUtils.toComponent(
                message("TELEPORT.ALL", "&dTeleported &7all players to your location"),
                player
        ));
    }

    private void teleportTop(Player player) {
        Location current = player.getLocation().clone();
        int highestY = current.getWorld().getHighestBlockYAt(current) + 1;
        current.setY(highestY);
        plugin.getPaperScheduler().teleport(player, current).thenAccept(success ->
                plugin.getPaperScheduler().runEntity(player, () -> {
                    if (Boolean.TRUE.equals(success) && player.isOnline()) {
                        player.sendMessage(ColorUtils.toComponent(
                                message("TELEPORT.TOP", "&dTeleported &7to the highest position"),
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
            player.sendMessage(ColorUtils.toComponent("&cCoordinates must be valid numbers."));
            return;
        }

        World world = player.getWorld();
        if (args.length == 4) {
            world = Bukkit.getWorld(args[3]);
            if (world == null) {
                player.sendMessage(ColorUtils.toComponent("&cWorld not found."));
                return;
            }
        }

        Location destination = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
        World destinationWorld = world;
        plugin.getPaperScheduler().teleport(player, destination).thenAccept(success ->
                plugin.getPaperScheduler().runEntity(player, () -> {
                    if (Boolean.TRUE.equals(success) && player.isOnline()) {
                        player.sendMessage(ColorUtils.toComponent(
                                message("TELEPORT.POSITION", "&7Teleported to: &d%x%,%y%,%z% &7(%world%)")
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
                "&cUsage: /" + label + " <player|here <player>|all|top|x y z [world]>"
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
