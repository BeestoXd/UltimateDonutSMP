package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public class GodModeCommand extends Command implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.staff.god";

    private final UltimateDonutSmp plugin;

    public GodModeCommand(UltimateDonutSmp plugin) {
        super("god",
                "toggle god mode for yourself or another online player",
                "/god [player]",
                List.of("godmode"));
        this.plugin = plugin;
        setPermission(PERMISSION);
        registerPermission();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return handle(sender, label, args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return handle(sender, label, args);
    }

    public boolean registerDynamically() {
        CommandMap commandMap = resolveCommandMap();
        return commandMap != null && commandMap.register(plugin.getDescription().getName().toLowerCase(Locale.ROOT), this);
    }

    private void registerPermission() {
        if (plugin.getServer().getPluginManager().getPermission(PERMISSION) != null) {
            return;
        }

        plugin.getServer().getPluginManager().addPermission(new Permission(PERMISSION, PermissionDefault.OP));
    }

    private boolean handle(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            send(sender, "GOD.USAGE", "&cUsage: /%label% [player]", "%label%", label);
            return true;
        }

        if (sender instanceof Player player && !PermissionUtils.has(player, PERMISSION)) {
            send(player, "GOD.NO_PERMISSION", "&cYou do not have permission.");
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                send(sender, "GOD.CONSOLE_USAGE", "&cUsage: /%label% <player>", "%label%", label);
                return true;
            }
            target = player;
        } else {
            target = findOnlinePlayer(args[0]);
            if (target == null) {
                send(sender, "GOD.PLAYER_NOT_ONLINE", "&cPlayer not online.");
                return true;
            }
        }

        boolean enabled = plugin.getGodModeManager().toggle(target.getUniqueId());
        notifyTarget(sender, target, enabled);
        return true;
    }

    private void notifyTarget(CommandSender sender, Player target, boolean enabled) {
        String status = plugin.getConfigManager().getMessageOrDefault(
                enabled ? "GOD.STATUS_ENABLED" : "GOD.STATUS_DISABLED",
                enabled ? "&aEnabled" : "&cDisabled"
        );

        if (sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId())) {
            send(target,
                    enabled ? "GOD.ENABLED" : "GOD.DISABLED",
                    enabled ? "&aGod mode enabled." : "&cGod mode disabled.");
            return;
        }

        send(sender,
                "GOD.OTHER",
                "&7God mode for &e%player% &7was %status%&7.",
                "%player%", target.getName(),
                "%status%", status);
        send(target,
                "GOD.NOTIFY",
                "&e%sender% &7set your god mode to %status%&7.",
                "%sender%", senderName(sender),
                "%status%", status);
    }

    private void send(CommandSender sender, String path, String fallback, String... placeholders) {
        sender.sendMessage(ColorUtils.toComponent(
                plugin.getConfigManager().getMessageOrDefault(path, fallback, placeholders)
        ));
    }

    private String senderName(CommandSender sender) {
        return sender instanceof Player player ? player.getName() : sender.getName();
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

    private CommandMap resolveCommandMap() {
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getCommandMap");
            Object commandMap = method.invoke(Bukkit.getServer());
            return commandMap instanceof CommandMap map ? map : null;
        } catch (ReflectiveOperationException ignored) {
            try {
                Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                field.setAccessible(true);
                Object commandMap = field.get(Bukkit.getServer());
                return commandMap instanceof CommandMap map ? map : null;
            } catch (ReflectiveOperationException ignoredAgain) {
                return null;
            }
        }
    }
}
