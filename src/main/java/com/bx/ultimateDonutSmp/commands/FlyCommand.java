package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class FlyCommand implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.staff.fly";

    private final UltimateDonutSmp plugin;

    public FlyCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " [ᴘʟᴀʏᴇʀ]"));
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + label + " <player>"));
                return true;
            }

            boolean isStaff = PermissionUtils.has(player, PERMISSION);
            String playerFlyPerm = plugin.getConfigManager().getConfig()
                    .getString("FLY-SYSTEM.PLAYER-FLY-PERMISSION", "ultimatedonutsmp.player.fly");
            boolean isPlayerFly = PermissionUtils.has(player, playerFlyPerm);

            if (!isStaff && !isPlayerFly) {
                player.sendMessage(ColorUtils.toComponent(
                        plugin.getConfigManager().getMessageOrDefault("STAFF.NO_PERMISSION_OTHERS", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
                ));
                return true;
            }

            if (!isStaff) {
                // Check combat
                if (plugin.getCombatManager() != null && plugin.getCombatManager().isInCombat(player.getUniqueId())) {
                    player.sendMessage(ColorUtils.toComponent(
                            plugin.getConfigManager().getMessageOrDefault("FLY.PLAYER_IN_COMBAT", "&cYou cannot fly while in combat.")
                    ));
                    return true;
                }

                // Check location (spawn or cuboids)
                boolean inSpawn = plugin.getAFKManager().isInSpawnCuboid(player);
                boolean inCuboid = false;
                for (String name : plugin.getCuboidManager().getCuboidNames()) {
                    if (plugin.getCuboidManager().isInCuboid(player, name)) {
                        inCuboid = true;
                        break;
                    }
                }

                if (!inSpawn && !inCuboid) {
                    player.sendMessage(ColorUtils.toComponent(
                            plugin.getConfigManager().getMessageOrDefault("FLY.PLAYER_RESTRICTED", "&cYou can only use flight in spawn or cuboids.")
                    ));
                    return true;
                }
            }

            target = player;
        } else {
            // Target player specified - staff only
            if (sender instanceof Player player && !PermissionUtils.has(player, PERMISSION)) {
                player.sendMessage(ColorUtils.toComponent(
                        plugin.getConfigManager().getMessageOrDefault("STAFF.NO_PERMISSION_OTHERS", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.")
                ));
                return true;
            }

            target = findOnlinePlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ColorUtils.toComponent("&cᴘʟᴀʏᴇʀ ɴᴏᴛ ᴏɴʟɪɴᴇ."));
                return true;
            }
        }

        boolean enabled = toggleFlight(target);
        String path = enabled ? "FLY.ENABLED" : "FLY.DISABLED";
        String fallback = enabled ? "&aflight mode activated" : "&cflight mode deactivated";
        String targetMessage = plugin.getConfigManager().getMessageOrDefault(path, fallback);
        target.sendMessage(ColorUtils.toComponent(targetMessage, target));

        if (!(sender instanceof Player player) || !player.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(ColorUtils.toComponent(
                    "&7ꜰʟɪɢʜᴛ ꜰᴏʀ &e" + target.getName() + " &7ᴡᴀѕ " + (enabled ? "&aᴇɴᴀʙʟᴇᴅ" : "&cᴅɪѕᴀʙʟᴇᴅ") + "&7."
            ));
        }
        return true;
    }

    private boolean toggleFlight(Player target) {
        boolean currentlyEnabled = isFlightEnabled(target);
        boolean nextEnabled = !currentlyEnabled;
        GameMode gameMode = target.getGameMode();
        boolean nativeFlightMode = gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;

        if (nextEnabled) {
            target.setAllowFlight(true);
            target.setFlying(true);
            return true;
        }

        target.setFlying(false);
        if (!nativeFlightMode) {
            target.setAllowFlight(false);
        }
        return false;
    }

    private boolean isFlightEnabled(Player target) {
        if (target == null) {
            return false;
        }

        GameMode gameMode = target.getGameMode();
        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) {
            return target.isFlying();
        }
        return target.getAllowFlight() || target.isFlying();
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
