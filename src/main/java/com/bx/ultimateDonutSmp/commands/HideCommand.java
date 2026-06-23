package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.HideManager;
import com.bx.ultimateDonutSmp.menus.DisguiseAliasMenu;
import com.bx.ultimateDonutSmp.menus.DisguiseSkinMenu;
import com.bx.ultimateDonutSmp.menus.HideListMenu;
import com.bx.ultimateDonutSmp.menus.HideMenu;
import com.bx.ultimateDonutSmp.models.HideState;
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
import java.util.Set;

public class HideCommand implements CommandExecutor, TabCompleter {

    private final UltimateDonutSmp plugin;

    public HideCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        HideManager manager = plugin.getHideManager();
        if (label.equalsIgnoreCase("disguise")) {
            return handleDisguise(sender, args, manager);
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                send(sender, manager.message("PLAYER-ONLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
                return true;
            }
            new HideMenu(plugin).open(player);
            return true;
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "status", "check" -> handleStatus(sender, args, manager);
            case "scramble" -> handleScramble(sender, manager);
            case "remove", "removal" -> handleRemove(sender, args, manager);
            case "list" -> handleList(sender, manager);
            default -> {
                send(sender, "&cᴜѕᴀɢᴇ: /hide [ѕᴛᴀᴛᴜѕ|ѕᴄʀᴀᴍʙʟᴇ|ʀᴇᴍᴏᴠᴇ|ᴄʜᴇᴄᴋ <player>|ʟɪѕᴛ]");
                yield true;
            }
        };
    }

    private boolean handleDisguise(CommandSender sender, String[] args, HideManager manager) {
        if (!(sender instanceof Player player)) {
            send(sender, manager.message("PLAYER-ONLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
            return true;
        }
        if (args.length == 0) {
            new DisguiseAliasMenu(plugin, 0).open(player);
            return true;
        }
        if (args.length == 1) {
            send(player, manager.message("SKIN-SEARCHING", "&7ѕᴇᴀʀᴄʜɪɴɢ ѕᴋɪɴ ꜰᴏʀ &f{skin}&7...",
                    "{skin}", args[0]));
            if (HideManager.isSkinUrl(args[0])) {
                manager.disguiseWithScrambledAlias(player, args[0], result -> sendResult(player, result));
            } else {
                manager.disguise(player, args[0], args[0], result -> sendResult(player, result));
            }
            return true;
        }
        send(player, manager.message("SKIN-SEARCHING", "&7ѕᴇᴀʀᴄʜɪɴɢ ѕᴋɪɴ ꜰᴏʀ &f{skin}&7...",
                "{skin}", args[1]));
        manager.disguise(player, args[0], args[1], result -> sendResult(player, result));
        return true;
    }

    private boolean handleStatus(CommandSender sender, String[] args, HideManager manager) {
        if (args.length > 1) {
            if (!PermissionUtils.has(sender, HideManager.ADMIN_PERMISSION)) {
                send(sender, manager.message("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
                return true;
            }
            HideState state = manager.findState(args[1]);
            if (state == null) {
                send(sender, manager.message("NOT-HIDDEN", "&cᴛʜᴀᴛ ᴘʟᴀʏᴇʀ ɪѕ ɴᴏᴛ ʜɪᴅᴅᴇɴ."));
                return true;
            }
            send(sender, manager.message(
                    "CHECK",
                    "&bʜɪᴅᴇ ᴄʜᴇᴄᴋ\n&7ʀᴇᴀʟ ɴᴀᴍᴇ: &f{real}\n&7ᴀʟɪᴀѕ: &f{alias}\n&7ᴍᴏᴅᴇ: &f{mode}\n&7ѕᴋɪɴ: &f{skin}",
                    "{real}", state.realNameSnapshot(),
                    "{alias}", state.alias(),
                    "{mode}", state.mode().name(),
                    "{skin}", state.skinUsername().isBlank() ? "ᴏʀɪɢɪɴᴀʟ" : state.skinUsername()
            ));
            return true;
        }

        if (!(sender instanceof Player player)) {
            send(sender, "&cᴜѕᴀɢᴇ: /hide ᴄʜᴇᴄᴋ <player>");
            return true;
        }
        HideState state = manager.getState(player.getUniqueId());
        if (state == null) {
            send(player, manager.message("STATUS-NONE", "&7ʜɪᴅᴇ ѕᴛᴀᴛᴜѕ: &cɪɴᴀᴄᴛɪᴠᴇ"));
        } else {
            send(player, manager.message(
                    "STATUS-ACTIVE",
                    "&7ʜɪᴅᴇ ѕᴛᴀᴛᴜѕ: &a{mode} &8- &f{alias}",
                    "{mode}", state.mode().name(),
                    "{alias}", manager.publicName(state)
            ));
        }
        return true;
    }

    private boolean handleScramble(CommandSender sender, HideManager manager) {
        if (!(sender instanceof Player player)) {
            send(sender, manager.message("PLAYER-ONLY", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀѕ ᴄᴀɴ ᴜѕᴇ ᴛʜɪѕ ᴄᴏᴍᴍᴀɴᴅ."));
            return true;
        }
        sendResult(player, manager.scramble(player));
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args, HideManager manager) {
        if (args.length > 1) {
            if (!PermissionUtils.has(sender, HideManager.ADMIN_PERMISSION)) {
                send(sender, manager.message("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
                return true;
            }
            HideState state = manager.findState(args[1]);
            if (state == null) {
                send(sender, manager.message("NOT-HIDDEN", "&cᴛʜᴀᴛ ᴘʟᴀʏᴇʀ ɪѕ ɴᴏᴛ ʜɪᴅᴅᴇɴ."));
                return true;
            }
            HideManager.Result result = manager.remove(state.playerUuid());
            if (result.success()) {
                send(sender, manager.message(
                        "ADMIN-REMOVED",
                        "&aѕᴜᴄᴄᴇѕѕꜰᴜʟʟʏ ʀᴇᴍᴏᴠᴇᴅ ʜɪᴅᴇ ꜰʀᴏᴍ &f{player}&a.",
                        "{player}", state.realNameSnapshot()
                ));
                Player online = plugin.getServer().getPlayer(state.playerUuid());
                if (online != null) {
                    send(online, manager.message("REMOVED-BY-ADMIN",
                            "&cʏᴏᴜʀ ʜɪᴅᴇ ѕᴛᴀᴛᴇ ʜᴀѕ ʙᴇᴇɴ ʀᴇᴍᴏᴠᴇᴅ ʙʏ ᴀɴ ᴀᴅᴍɪɴɪѕᴛʀᴀᴛᴏʀ."));
                }
            } else {
                sendResult(sender, result);
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            send(sender, "&cᴜѕᴀɢᴇ: /hide ʀᴇᴍᴏᴠᴇ <player>");
            return true;
        }
        sendResult(player, manager.remove(player, false));
        return true;
    }

    private boolean handleList(CommandSender sender, HideManager manager) {
        if (!PermissionUtils.has(sender, HideManager.ADMIN_PERMISSION)) {
            send(sender, manager.message("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            return true;
        }
        if (!(sender instanceof Player player)) {
            for (HideState state : manager.getStates()) {
                sender.sendMessage(state.realNameSnapshot() + " -> " + state.alias()
                        + " (" + state.mode().name() + ")");
            }
            return true;
        }
        new HideListMenu(plugin, 0).open(player);
        return true;
    }

    public void sendResult(CommandSender sender, HideManager.Result result) {
        HideManager manager = plugin.getHideManager();
        switch (result.type()) {
            case SUCCESS -> {
                if (result.state() == null) {
                    return;
                }
                String key = result.state().mode() == com.bx.ultimateDonutSmp.models.HideMode.SCRAMBLE
                        ? "SCRAMBLED"
                        : "DISGUISED";
                if (!manager.isHidden(result.state().playerUuid())) {
                    key = "REMOVED";
                }
                send(sender, manager.message(
                        key,
                        key.equals("removed")
                                ? "&aʏᴏᴜʀ ʜɪᴅᴇ ѕᴛᴀᴛᴇ ʜᴀѕ ʙᴇᴇɴ ʀᴇᴍᴏᴠᴇᴅ."
                                : "&aʏᴏᴜʀ ɪᴅᴇɴᴛɪᴛʏ ɪѕ ɴᴏᴡ &f{alias}&a.",
                        "{alias}", manager.publicName(result.state())
                ));
            }
            case DISABLED -> send(sender, manager.message("DISABLED", "&cᴛʜᴇ ʜɪᴅᴇ ꜰᴇᴀᴛᴜʀᴇ ɪѕ ᴅɪѕᴀʙʟᴇᴅ."));
            case DEPENDENCY_MISSING -> send(sender, manager.message(
                    "DEPENDENCY-MISSING", "&cʜɪᴅᴇ ʀᴇǫᴜɪʀᴇѕ ᴘʀᴏᴛᴏᴄᴏʟʟɪʙ."));
            case NO_PERMISSION -> send(sender, manager.message("NO-PERMISSION", "&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ."));
            case IN_COMBAT -> send(sender, manager.message("IN-COMBAT", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴄʜᴀɴɢᴇ ʜɪᴅᴇ ɪɴ ᴄᴏᴍʙᴀᴛ."));
            case COOLDOWN -> send(sender, manager.message(
                    "COOLDOWN", "&cᴡᴀɪᴛ &f{seconds}ѕ &cʙᴇꜰᴏʀᴇ ᴄʜᴀɴɢɪɴɢ ʜɪᴅᴇ ᴀɢᴀɪɴ.",
                    "{seconds}", String.valueOf(result.remainingSeconds())));
            case INVALID_ALIAS -> send(sender, manager.message("INVALID-ALIAS", "&cɪɴᴠᴀʟɪᴅ ᴀʟɪᴀѕ."));
            case INVALID_SKIN -> send(sender, manager.message("INVALID-SKIN", "&cɪɴᴠᴀʟɪᴅ ѕᴋɪɴ."));
            case ALIAS_IN_USE -> send(sender, manager.message("ALIAS-IN-USE", "&cᴛʜᴀᴛ ᴀʟɪᴀѕ ɪѕ ᴀʟʀᴇᴀᴅʏ ɪɴ ᴜѕᴇ."));
            case NOT_HIDDEN -> send(sender, manager.message("NOT-HIDDEN", "&cᴛʜᴀᴛ ᴘʟᴀʏᴇʀ ɪѕ ɴᴏᴛ ʜɪᴅᴅᴇɴ."));
            case DATABASE_ERROR -> send(sender, "&cᴜɴᴀʙʟᴇ ᴛᴏ ѕᴀᴠᴇ ʜɪᴅᴇ ѕᴛᴀᴛᴇ.");
        }
    }

    private void send(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            player.sendMessage(ColorUtils.toComponent(message, player));
        } else {
            sender.sendMessage(ColorUtils.colorize(message));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        HideManager manager = plugin.getHideManager();
        if (command.getName().equalsIgnoreCase("disguise")) {
            if (args.length == 1) {
                List<String> values = new ArrayList<>(manager.aliases().keySet());
                values.addAll(manager.skins().values().stream()
                        .map(HideManager.SkinOption::username)
                        .toList());
                return filter(values, args[0]);
            }
            if (args.length == 2) {
                List<String> values = new ArrayList<>(manager.skins().keySet());
                values.addAll(manager.skins().values().stream()
                        .map(HideManager.SkinOption::username)
                        .toList());
                return filter(values, args[1]);
            }
            return List.of();
        }

        if (args.length == 1) {
            List<String> values = new ArrayList<>(List.of("status", "scramble", "remove"));
            if (PermissionUtils.has(sender, HideManager.ADMIN_PERMISSION)) {
                values.add("check");
                values.add("list");
                values.add("removal");
            }
            return filter(values, args[0]);
        }
        if (args.length == 2 && PermissionUtils.has(sender, HideManager.ADMIN_PERMISSION)
                && Set.of("check", "remove", "removal").contains(args[0].toLowerCase(Locale.ROOT))) {
            List<String> values = manager.getStates().stream()
                    .flatMap(state -> java.util.stream.Stream.of(state.alias(), state.realNameSnapshot()))
                    .toList();
            return filter(values, args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String input) {
        String lower = input == null ? "" : input.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lower))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private String normalizeKey(String value) {
        return HideManager.normalize(value).replace('-', '_').replace(' ', '_');
    }
}
