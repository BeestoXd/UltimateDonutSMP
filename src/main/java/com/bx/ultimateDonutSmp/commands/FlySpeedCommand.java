package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class FlySpeedCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.command.flyspeed";
    private static final String STAFF_PERMISSION = "ultimatedonutsmp.staff.flyspeed";
    private static final String FLY_STAFF_PERMISSION = "ultimatedonutsmp.staff.fly";

    private final UltimateDonutSmp plugin;

    public FlySpeedCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <1-10> [player]"));
            return true;
        }

        // Permission check
        if (sender instanceof Player player) {
            String playerFlyPerm = plugin.getConfigManager().getConfig()
                    .getString("FLY-SYSTEM.PLAYER-FLY-PERMISSION", "ultimatedonutsmp.player.fly");
            boolean hasPerm = PermissionUtils.has(player, PERMISSION)
                    || PermissionUtils.has(player, STAFF_PERMISSION)
                    || PermissionUtils.has(player, FLY_STAFF_PERMISSION)
                    || PermissionUtils.has(player, playerFlyPerm);

            if (!hasPerm) {
                player.sendMessage(ColorUtils.toComponent(
                        plugin.getConfigManager().getMessageOrDefault("STAFF.NO_PERMISSION_OTHERS", "&cYou do not have permission.")
                ));
                return true;
            }
        }

        int minSpeed = plugin.getConfigManager().getConfig().getInt("FLY-SYSTEM.MIN-SPEED", 1);
        int maxSpeed = plugin.getConfigManager().getConfig().getInt("FLY-SYSTEM.MAX-SPEED", 10);

        Double parsedSpeed = null;
        Player target = null;

        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <1-10> <player>"));
                return true;
            }
            target = player;
            parsedSpeed = parseSpeed(args[0]);
        } else { // args.length == 2
            // Try args[0] as speed, args[1] as player
            parsedSpeed = parseSpeed(args[0]);
            if (parsedSpeed != null) {
                target = findOnlinePlayer(args[1]);
            } else {
                // Try args[1] as speed, args[0] as player
                parsedSpeed = parseSpeed(args[1]);
                if (parsedSpeed != null) {
                    target = findOnlinePlayer(args[0]);
                }
            }

            if (target == null && parsedSpeed == null) {
                sendInvalidSpeedMessage(sender, minSpeed, maxSpeed);
                return true;
            }
            if (target == null) {
                sender.sendMessage(ColorUtils.toComponent("&cPlayer not online."));
                return true;
            }
            // Check permissions for setting others' flyspeed
            if (sender instanceof Player player && !player.getUniqueId().equals(target.getUniqueId())) {
                boolean isStaff = PermissionUtils.has(player, STAFF_PERMISSION)
                        || PermissionUtils.has(player, FLY_STAFF_PERMISSION)
                        || PermissionUtils.has(player, "ultimatedonutsmp.command.flyspeed.others");
                if (!isStaff) {
                    player.sendMessage(ColorUtils.toComponent(
                            plugin.getConfigManager().getMessageOrDefault("STAFF.NO_PERMISSION_OTHERS", "&cYou do not have permission.")
                    ));
                    return true;
                }
            }
        }

        if (parsedSpeed == null || parsedSpeed < minSpeed || parsedSpeed > maxSpeed) {
            sendInvalidSpeedMessage(sender, minSpeed, maxSpeed);
            return true;
        }

        // Convert 1-10 scale to Bukkit's fly speed range (-1.0f to 1.0f). Standard default speed in Bukkit is 0.1f.
        float bukkitSpeed = (float) (parsedSpeed / 10.0);
        bukkitSpeed = Math.max(-1.0f, Math.min(1.0f, bukkitSpeed));
        target.setFlySpeed(bukkitSpeed);

        String speedStr = (parsedSpeed == parsedSpeed.intValue())
                ? String.valueOf(parsedSpeed.intValue())
                : String.valueOf(parsedSpeed);

        String setMsg = plugin.getConfigManager().getMessageOrDefault("FLYSPEED.SET", "&aFly speed set to &f{speed}&a.", "{speed}", speedStr, "%speed%", speedStr);
        target.sendMessage(ColorUtils.toComponent(setMsg, target));

        if (!(sender instanceof Player player) || !player.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(ColorUtils.toComponent(
                    "&7Fly speed for &e" + target.getName() + " &7set to &a" + speedStr + "&7."
            ));
        }

        return true;
    }

    private Double parseSpeed(String arg) {
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void sendInvalidSpeedMessage(CommandSender sender, int minSpeed, int maxSpeed) {
        String defaultMsg = "&cInvalid speed. Please enter a number from " + minSpeed + " to " + maxSpeed + ".";
        String invalidMsg = plugin.getConfigManager().getMessageOrDefault("FLYSPEED.INVALID", defaultMsg);
        sender.sendMessage(ColorUtils.toComponent(invalidMsg));
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
