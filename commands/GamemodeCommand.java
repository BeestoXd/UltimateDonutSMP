package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GamemodeCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "ultimatedonutsmp.staff.gamemode";
    private static final String OTHERS_PERMISSION = "ultimatedonutsmp.staff.gamemode.others";
    private static final List<String> MODE_COMPLETIONS = List.of("survival", "creative", "adventure", "spectator");

    private final UltimateDonutSmp plugin;

    public GamemodeCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("GAMEMODE")) {
            send(sender, "GAMEMODE.DISABLED", "&cGamemode commands are currently disabled.");
            return true;
        }

        if (!hasBasePermission(sender)) {
            return true;
        }

        GameMode fixedMode = modeFromLabel(label);
        if (fixedMode != null) {
            return handleFixedModeCommand(sender, label, args, fixedMode);
        }

        return handleMainCommand(sender, label, args);
    }

    private boolean handleFixedModeCommand(CommandSender sender, String label, String[] args, GameMode mode) {
        if (args.length > 1) {
            send(sender, "GAMEMODE.USAGE_SHORT", "&cUsage: /%label% [player]", "%label%", label);
            return true;
        }

        Player target = resolveTarget(sender, args.length == 0 ? null : args[0], label, true);
        if (target == null) {
            return true;
        }

        if (!hasRequiredPermission(sender, target)) {
            return true;
        }

        applyGamemode(sender, target, mode);
        return true;
    }

    private boolean handleMainCommand(CommandSender sender, String label, String[] args) {
        if (args.length < 1 || args.length > 2) {
            send(sender, "GAMEMODE.USAGE", "&cUsage: /%label% <survival|creative|adventure|spectator> [player]", "%label%", label);
            return true;
        }

        GameMode mode = parseMode(args[0]);
        if (mode == null) {
            send(sender, "GAMEMODE.INVALID_MODE", "&cInvalid gamemode. Use survival, creative, adventure, or spectator.");
            return true;
        }

        Player target = resolveTarget(sender, args.length == 1 ? null : args[1], label, false);
        if (target == null) {
            return true;
        }

        if (!hasRequiredPermission(sender, target)) {
            return true;
        }

        applyGamemode(sender, target, mode);
        return true;
    }

    private Player resolveTarget(CommandSender sender, String input, String label, boolean fixedModeCommand) {
        if (input == null || input.isBlank()) {
            if (sender instanceof Player player) {
                return player;
            }

            String fallback = fixedModeCommand
                    ? "&cUsage: /%label% <player>"
                    : "&cUsage: /%label% <survival|creative|adventure|spectator> <player>";
            send(sender, "GAMEMODE.PLAYER_ONLY", fallback, "%label%", label);
            return null;
        }

        Player target = findOnlinePlayer(input);
        if (target == null) {
            send(sender, "GAMEMODE.PLAYER_NOT_ONLINE", "&cPlayer not online.");
            return null;
        }
        return target;
    }

    private boolean hasRequiredPermission(CommandSender sender, Player target) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            send(player, "GAMEMODE.NO_PERMISSION", "&cYou do not have permission.");
            return false;
        }

        if (!player.getUniqueId().equals(target.getUniqueId()) && !player.hasPermission(OTHERS_PERMISSION)) {
            send(player, "GAMEMODE.NO_PERMISSION_OTHERS", "&cYou do not have permission to change other players' gamemode.");
            return false;
        }

        return true;
    }

    private void applyGamemode(CommandSender sender, Player target, GameMode mode) {
        target.setGameMode(mode);

        String modeName = displayName(mode);
        send(sender, "GAMEMODE.MESSAGE", "&d%player% &7is now in &e%mode% mode",
                "%player%", target.getName(),
                "%mode%", modeName,
                "%sender%", senderName(sender));

        if (sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId())) {
            return;
        }

        send(target, "GAMEMODE.TARGET_MESSAGE", "&7Your gamemode has been changed to &e%mode% &7by &d%sender%",
                "%player%", target.getName(),
                "%mode%", modeName,
                "%sender%", senderName(sender));
    }

    private GameMode modeFromLabel(String label) {
        return switch (normalizeLabel(label)) {
            case "gms" -> GameMode.SURVIVAL;
            case "gmc" -> GameMode.CREATIVE;
            case "gma" -> GameMode.ADVENTURE;
            case "gmsp" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    private GameMode parseMode(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        return switch (input.toLowerCase(Locale.ROOT)) {
            case "survival", "s", "0" -> GameMode.SURVIVAL;
            case "creative", "c", "1" -> GameMode.CREATIVE;
            case "adventure", "a", "2" -> GameMode.ADVENTURE;
            case "spectator", "spec", "sp", "3" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    private Player findOnlinePlayer(String input) {
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

    private String displayName(GameMode mode) {
        return mode.name().toLowerCase(Locale.ROOT);
    }

    private String senderName(CommandSender sender) {
        return sender instanceof Player player ? player.getName() : "Console";
    }

    private String normalizeLabel(String label) {
        if (label == null) {
            return "";
        }

        String normalized = label.toLowerCase(Locale.ROOT);
        int namespaceSeparator = normalized.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator + 1 < normalized.length()) {
            normalized = normalized.substring(namespaceSeparator + 1);
        }
        return normalized;
    }

    private void send(CommandSender sender, String path, String fallback, String... placeholders) {
        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault(path, fallback, placeholders)
        ));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!canUseBaseCommand(sender)) {
            return Collections.emptyList();
        }

        if (modeFromLabel(alias) != null) {
            if (args.length == 1 && canTargetOthers(sender)) {
                return partialMatches(args[0], onlinePlayerNames());
            }
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return partialMatches(args[0], MODE_COMPLETIONS);
        }

        if (args.length == 2 && canTargetOthers(sender)) {
            return partialMatches(args[1], onlinePlayerNames());
        }

        return Collections.emptyList();
    }

    private boolean canUseBaseCommand(CommandSender sender) {
        return !(sender instanceof Player player) || player.hasPermission(PERMISSION);
    }

    private boolean hasBasePermission(CommandSender sender) {
        if (sender instanceof Player player && !player.hasPermission(PERMISSION)) {
            send(player, "GAMEMODE.NO_PERMISSION", "&cYou do not have permission.");
            return false;
        }
        return true;
    }

    private boolean canTargetOthers(CommandSender sender) {
        return !(sender instanceof Player player) || player.hasPermission(OTHERS_PERMISSION);
    }

    private List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    private List<String> partialMatches(String token, List<String> completions) {
        List<String> matches = new ArrayList<>();
        StringUtil.copyPartialMatches(token, completions, matches);
        matches.sort(String.CASE_INSENSITIVE_ORDER);
        return matches;
    }
}
