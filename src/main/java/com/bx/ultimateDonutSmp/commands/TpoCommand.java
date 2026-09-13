package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public class TpoCommand extends Command implements CommandExecutor {

    private static final String PERMISSION = "ultimatedonutsmp.staff.teleport.offline";

    private final UltimateDonutSmp plugin;
    private final TeleportCommand teleportCommand;

    public TpoCommand(UltimateDonutSmp plugin, TeleportCommand teleportCommand) {
        super("tpo",
                "teleport to an offline player's last known location",
                "/tpo <player>",
                List.of("tpoffline"));
        this.plugin = plugin;
        this.teleportCommand = teleportCommand;
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cOnly players can use this command."));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ColorUtils.toComponent("&cUsage: /" + label + " <player>"));
            return true;
        }

        teleportCommand.teleportToOfflinePlayer(player, args[0]);
        return true;
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
