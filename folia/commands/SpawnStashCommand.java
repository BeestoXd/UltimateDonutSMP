package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.SpawnStashManager;
import com.bx.ultimateDonutSmp.models.SpawnStashInstance;
import com.bx.ultimateDonutSmp.models.SpawnStashTypeDefinition;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import com.bx.ultimateDonutSmp.utils.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnStashCommand implements CommandExecutor, TabCompleter {

    private final UltimateDonutSmp plugin;

    public SpawnStashCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return handleRandomSpawn(sender, label);
        }

        String sub = args[0].toLowerCase(Locale.US);
        return switch (sub) {
            case "spawn" -> handleSpawn(sender, label, args.length >= 2 ? args[1] : null);
            case "list" -> handleList(sender);
            case "remove" -> handleRemove(sender, label, args);
            case "reload" -> handleReload(sender);
            default -> handleSpawn(sender, label, args[0]);
        };
    }

    private boolean handleSpawn(CommandSender sender, String label, String typeKey) {
        if (!PermissionUtils.has(sender, SpawnStashManager.USE_PERMISSION)) {
            send(sender, noPermission());
            return true;
        }
        if (!(sender instanceof Player player)) {
            send(sender, plugin.getSpawnStashManager().publicMessage("PLAYER-ONLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
            return true;
        }
        if (typeKey == null || typeKey.isBlank()) {
            plugin.getSpawnStashManager().sendUsage(sender, label);
            return true;
        }

        SpawnStashManager.SpawnResult result = plugin.getSpawnStashManager().spawn(player, typeKey);
        send(sender, result.message());
        return true;
    }

    private boolean handleRandomSpawn(CommandSender sender, String label) {
        if (!PermissionUtils.has(sender, SpawnStashManager.USE_PERMISSION)) {
            send(sender, noPermission());
            return true;
        }
        if (!(sender instanceof Player)) {
            send(sender, plugin.getSpawnStashManager().publicMessage("PLAYER-ONLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
            return true;
        }

        List<String> typeKeys = plugin.getSpawnStashManager().getTypeKeys();
        if (typeKeys.isEmpty()) {
            send(sender, plugin.getSpawnStashManager().publicMessage(
                    "INVALID-CONFIG",
                    "&cѕᴘᴀᴡɴѕᴛᴀѕʜ ᴄᴏɴꜰɪɢ ɪѕ ɪɴᴠᴀʟɪᴅ: &f{reason}&c.",
                    "{reason}", "ɴᴏ ᴇɴᴀʙʟᴇᴅ ѕᴛᴀѕʜ ᴛʏᴘᴇѕ"
            ));
            return true;
        }

        String randomType = typeKeys.get(ThreadLocalRandom.current().nextInt(typeKeys.size()));
        return handleSpawn(sender, label, randomType);
    }

    private boolean handleList(CommandSender sender) {
        if (!PermissionUtils.has(sender, SpawnStashManager.USE_PERMISSION)) {
            send(sender, noPermission());
            return true;
        }

        send(sender, "&8&m----------- &dѕᴘᴀᴡɴѕᴛᴀѕʜ &8&m-----------");
        send(sender, "&7ᴄᴏɴꜰɪɢᴜʀᴇᴅ ᴛʏᴘᴇѕ:");
        for (SpawnStashTypeDefinition definition : plugin.getSpawnStashManager().getTypeDefinitions()) {
            send(sender, "&f- &d" + definition.key() + " &8| &7" + ColorUtils.strip(definition.displayName())
                    + " &8| &7ᴛᴛʟ &f" + definition.ttlSeconds() + "s"
                    + " &8| &7ʀᴀᴅɪᴜѕ &f" + trim(definition.alertRadius()));
        }

        List<SpawnStashInstance> active = plugin.getSpawnStashManager().getActiveStashes();
        send(sender, "&7ᴀᴄᴛɪᴠᴇ ѕᴛᴀѕʜᴇѕ: &f" + active.size());
        for (SpawnStashInstance instance : active) {
            send(sender, "&f#" + instance.id()
                    + " &8| &dᴛʏᴘᴇ " + instance.typeKey()
                    + " &8| &7" + instance.worldName() + " "
                    + instance.originX() + ", " + instance.originY() + ", " + instance.originZ()
                    + " &8| &7ᴛᴛʟ &f" + plugin.getSpawnStashManager().remainingSeconds(instance) + "s"
                    + " &8| &7ʙʏ &f" + instance.creatorName());
        }
        return true;
    }

    private boolean handleRemove(CommandSender sender, String label, String[] args) {
        if (!PermissionUtils.has(sender, SpawnStashManager.USE_PERMISSION)) {
            send(sender, noPermission());
            return true;
        }
        if (args.length < 2) {
            plugin.getSpawnStashManager().sendUsage(sender, label);
            return true;
        }

        String target = args[1].toLowerCase(Locale.US);
        SpawnStashManager.RemovalResult result;
        if (target.equals("all")) {
            if (!PermissionUtils.has(sender, SpawnStashManager.ADMIN_PERMISSION)) {
                send(sender, noPermission());
                return true;
            }
            result = plugin.getSpawnStashManager().removeAll();
        } else if (target.equals("nearest")) {
            if (!(sender instanceof Player player)) {
                send(sender, plugin.getSpawnStashManager().publicMessage("PLAYER-ONLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
                return true;
            }
            result = plugin.getSpawnStashManager().removeNearest(player);
        } else {
            try {
                result = plugin.getSpawnStashManager().removeById(Long.parseLong(target));
            } catch (NumberFormatException exception) {
                result = SpawnStashManager.RemovalResult.fail(plugin.getSpawnStashManager()
                        .publicMessage("NO-ACTIVE", "&cɴᴏ ᴀᴄᴛɪᴠᴇ ѕᴛᴀѕʜ ꜰᴏᴜɴᴅ."));
            }
        }

        send(sender, result.message());
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!PermissionUtils.has(sender, SpawnStashManager.ADMIN_PERMISSION)) {
            send(sender, noPermission());
            return true;
        }

        plugin.getConfigManager().reloadSpawnStash();
        plugin.getSpawnStashManager().reload();
        send(sender, plugin.getSpawnStashManager().publicMessage("RELOADED", "&aѕᴘᴀᴡɴѕᴛᴀѕʜ ѕᴇᴛᴛɪɴɢѕ ʀᴇʟᴏᴀᴅᴇᴅ."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!PermissionUtils.has(sender, SpawnStashManager.USE_PERMISSION)
                && !PermissionUtils.has(sender, SpawnStashManager.ADMIN_PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("spawn", "list", "remove"));
            if (PermissionUtils.has(sender, SpawnStashManager.ADMIN_PERMISSION)) {
                options.add("reload");
            }
            options.addAll(plugin.getSpawnStashManager().getTypeKeys());
            return partial(args[0], options);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            return partial(args[1], plugin.getSpawnStashManager().getTypeKeys());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            List<String> options = new ArrayList<>();
            options.add("nearest");
            if (PermissionUtils.has(sender, SpawnStashManager.ADMIN_PERMISSION)) {
                options.add("all");
            }
            for (SpawnStashInstance instance : plugin.getSpawnStashManager().getActiveStashes()) {
                options.add(String.valueOf(instance.id()));
            }
            return partial(args[1], options);
        }

        return List.of();
    }

    private List<String> partial(String token, List<String> options) {
        String lower = token == null ? "" : token.toLowerCase(Locale.US);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.US).startsWith(lower))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private String noPermission() {
        return plugin.getSpawnStashManager().publicMessage("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ.");
    }

    private String trim(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.toComponent(message));
    }
}
