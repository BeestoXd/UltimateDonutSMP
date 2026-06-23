package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.utils.PermissionUtils;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.managers.FfaManager;
import com.bx.ultimateDonutSmp.models.FfaArena;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FfaArenaCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public FfaArenaCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return handle(sender, label, args);
    }

    public boolean handle(CommandSender sender, String baseLabel, String[] args) {
        if (!PermissionUtils.has(sender, "ultimatedonutsmp.admin.ffa")) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴅᴏ ɴᴏᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪѕѕɪᴏɴ ᴛᴏ ᴍᴀɴᴀɢᴇ ꜰꜰᴀ ᴀʀᴇɴᴀѕ."));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender, baseLabel);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        if (subcommand.equals("list")) {
            if (plugin.getFfaManager().getArenas().isEmpty()) {
                sender.sendMessage(ColorUtils.toComponent("&cɴᴏ ꜰꜰᴀ ᴀʀᴇɴᴀѕ ᴄᴏɴꜰɪɢᴜʀᴇᴅ."));
                return true;
            }
            sender.sendMessage(ColorUtils.toComponent("&eꜰꜰᴀ ᴀʀᴇɴᴀѕ:"));
            for (FfaArena arena : plugin.getFfaManager().getArenas()) {
                sender.sendMessage(ColorUtils.toComponent(
                        "&7- &f" + arena.getId()
                                + " &8(" + arena.getDisplayName() + "&8)"
                                + " &7ѕᴛᴀᴛᴇ=&f" + arena.getState().name()
                                + " &7ᴄᴏɴꜰɪɢᴜʀᴇᴅ=&f" + arena.isConfigured()
                                + " &7ʀᴇɢɪᴏɴ=&f" + arena.hasRollbackRegion()
                                + " &7ʀᴇᴀᴅʏ=&f" + arena.isReady()
                                + " &7ᴇɴᴀʙʟᴇᴅ=&f" + arena.isEnabled()
                ));
            }
            return true;
        }
        if (subcommand.equals("reload")) {
            plugin.getConfigManager().reloadFfa();
            plugin.getFfaManager().reload();
            sender.sendMessage(ColorUtils.toComponent("&aʀᴇʟᴏᴀᴅᴇᴅ ꜰꜰᴀ ᴀʀᴇɴᴀѕ ᴀɴᴅ ᴄᴏɴꜰɪɢ."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cʏᴏᴜ ᴍᴜѕᴛ ѕᴘᴇᴄɪꜰʏ ᴀɴ ᴀʀᴇɴᴀ ɪᴅ."));
            return true;
        }

        String id = args[1];
        if (subcommand.equals("create")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getFfaManager().createArena(id)
                            ? "&aᴄʀᴇᴀᴛᴇᴅ ꜰꜰᴀ ᴀʀᴇɴᴀ &f" + id + "&a."
                            : "&cᴄᴏᴜʟᴅ ɴᴏᴛ ᴄʀᴇᴀᴛᴇ ᴛʜᴀᴛ ꜰꜰᴀ ᴀʀᴇɴᴀ."
            ));
            return true;
        }
        if (subcommand.equals("delete")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getFfaManager().deleteArena(id)
                            ? "&aᴅᴇʟᴇᴛᴇᴅ ꜰꜰᴀ ᴀʀᴇɴᴀ &f" + id + "&a."
                            : "&cᴄᴏᴜʟᴅ ɴᴏᴛ ᴅᴇʟᴇᴛᴇ ᴛʜᴀᴛ ꜰꜰᴀ ᴀʀᴇɴᴀ."
            ));
            return true;
        }
        if (subcommand.equals("enable")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getFfaManager().setArenaEnabled(id, true)
                            ? "&aᴇɴᴀʙʟᴇᴅ ꜰꜰᴀ ᴀʀᴇɴᴀ &f" + id + "&a."
                            : "&cᴄᴏᴜʟᴅ ɴᴏᴛ ᴇɴᴀʙʟᴇ ᴛʜᴀᴛ ꜰꜰᴀ ᴀʀᴇɴᴀ."
            ));
            return true;
        }
        if (subcommand.equals("disable")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getFfaManager().setArenaEnabled(id, false)
                            ? "&eᴅɪѕᴀʙʟᴇᴅ ꜰꜰᴀ ᴀʀᴇɴᴀ &f" + id + "&e."
                            : "&cᴄᴏᴜʟᴅ ɴᴏᴛ ᴅɪѕᴀʙʟᴇ ᴛʜᴀᴛ ꜰꜰᴀ ᴀʀᴇɴᴀ."
            ));
            return true;
        }
        if (subcommand.equals("setdisplay")) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + baseLabel + " ѕᴇᴛᴅɪѕᴘʟᴀʏ <id> <name>"));
                return true;
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) {
                    builder.append(' ');
                }
                builder.append(args[i]);
            }
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getFfaManager().setArenaDisplayName(id, builder.toString())
                            ? "&aᴜᴘᴅᴀᴛᴇᴅ ᴅɪѕᴘʟᴀʏ ɴᴀᴍᴇ ꜰᴏʀ ꜰꜰᴀ ᴀʀᴇɴᴀ &f" + id + "&a."
                            : "&cᴄᴏᴜʟᴅ ɴᴏᴛ ᴜᴘᴅᴀᴛᴇ ᴛʜᴀᴛ ꜰꜰᴀ ᴀʀᴇɴᴀ ᴅɪѕᴘʟᴀʏ ɴᴀᴍᴇ."
            ));
            return true;
        }
        if (subcommand.equals("settings")) {
            FfaArena arena = plugin.getFfaManager().getArena(id);
            if (arena == null) {
                sender.sendMessage(ColorUtils.toComponent("&cᴛʜᴀᴛ ꜰꜰᴀ ᴀʀᴇɴᴀ ᴅᴏᴇѕ ɴᴏᴛ ᴇxɪѕᴛ."));
                return true;
            }

            if (args.length == 2) {
                sendSettingsOverview(sender, arena);
                sender.sendMessage(ColorUtils.toComponent("&7ᴜѕᴀɢᴇ: &f/" + baseLabel + " ѕᴇᴛᴛɪɴɢѕ <id> <nohunger|noweather|alwaysmorning|nofalldamage> <on|off>"));
                return true;
            }

            FfaManager.ArenaSetting setting = parseArenaSetting(args[2]);
            if (setting == null) {
                sender.sendMessage(ColorUtils.toComponent("&cᴜɴᴋɴᴏᴡɴ ꜰꜰᴀ ᴀʀᴇɴᴀ ѕᴇᴛᴛɪɴɢ. ᴜѕᴇ: ɴᴏʜᴜɴɢᴇʀ, ɴᴏᴡᴇᴀᴛʜᴇʀ, ᴀʟᴡᴀʏѕᴍᴏʀɴɪɴɢ, ɴᴏꜰᴀʟʟᴅᴀᴍᴀɢᴇ"));
                return true;
            }

            if (args.length < 4) {
                sender.sendMessage(ColorUtils.toComponent("&cᴜѕᴀɢᴇ: /" + baseLabel + " ѕᴇᴛᴛɪɴɢѕ <id> <nohunger|noweather|alwaysmorning|nofalldamage> <on|off>"));
                return true;
            }

            Boolean enabled = parseToggle(args[3]);
            if (enabled == null) {
                sender.sendMessage(ColorUtils.toComponent("&cᴠᴀʟᴜᴇ ᴍᴜѕᴛ ʙᴇ &fᴏɴ &cᴏʀ &fᴏꜰꜰ&c."));
                return true;
            }

            if (!plugin.getFfaManager().setArenaSetting(id, setting, enabled)) {
                sender.sendMessage(ColorUtils.toComponent("&cᴄᴏᴜʟᴅ ɴᴏᴛ ᴜᴘᴅᴀᴛᴇ ᴛʜᴀᴛ ꜰꜰᴀ ᴀʀᴇɴᴀ ѕᴇᴛᴛɪɴɢ."));
                return true;
            }

            sender.sendMessage(ColorUtils.toComponent(
                    "&aꜰꜰᴀ ᴀʀᴇɴᴀ &f" + arena.getId() + " &a" + (enabled ? "ᴇɴᴀʙʟᴇᴅ " : "ᴅɪѕᴀʙʟᴇᴅ ") + "&f" + setting.getDisplayName() + "&a."
            ));
            sendSettingsOverview(sender, plugin.getFfaManager().getArena(id));
            return true;
        }
        if (subcommand.equals("setspawn1") || subcommand.equals("setspawn2") || subcommand.equals("setreturn")) {
            sender.sendMessage(ColorUtils.toComponent(
                    "&eꜰꜰᴀ ᴀʀᴇɴᴀ ѕᴇᴋᴀʀᴀɴɢ ᴛɪᴅᴀᴋ ᴘᴀᴋᴀɪ ѕᴘᴀᴡɴ ᴍᴀɴᴜᴀʟ. ɢᴜɴᴀᴋᴀɴ &f/" + baseLabel + " ѕᴇᴛᴘᴏѕ <id> &eᴜɴᴛᴜᴋ ѕᴇᴛ ᴘᴜѕᴀᴛ ᴀʀᴇɴᴀ ᴄᴏᴍʙᴀᴛ."
            ));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cᴛʜɪѕ ѕᴜʙᴄᴏᴍᴍᴀɴᴅ ʀᴇǫᴜɪʀᴇѕ ᴀ ᴘʟᴀʏᴇʀ ѕᴇɴᴅᴇʀ."));
            return true;
        }
        if (subcommand.equals("setpos") || subcommand.equals("setpos1") || subcommand.equals("setpos2")) {
            if (!plugin.getFfaManager().setArenaRegionPos(id, player.getLocation())) {
                sender.sendMessage(ColorUtils.toComponent("&cᴄᴏᴜʟᴅ ɴᴏᴛ ѕᴇᴛ ꜰꜰᴀ ʀᴇɢɪᴏɴ ꜰᴏʀ ᴛʜᴀᴛ ᴀʀᴇɴᴀ."));
                return true;
            }
            sender.sendMessage(ColorUtils.toComponent(
                    "&aѕᴇᴛ ꜰꜰᴀ ᴀʀᴇɴᴀ ᴄᴇɴᴛᴇʀ ꜰᴏʀ &f" + id + "&a. &7ᴄᴏᴍʙᴀᴛ ᴀʀᴇᴀ ᴅᴀɴ ʀᴏʟʟʙᴀᴄᴋ ᴀᴋᴀɴ ᴅɪʙᴀɴɢᴜɴ ᴏᴛᴏᴍᴀᴛɪѕ ᴅᴀʀɪ ᴛɪᴛɪᴋ ɪɴɪ."
            ));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent("&cᴜɴᴋɴᴏᴡɴ ꜰꜰᴀ ᴀʀᴇɴᴀ ѕᴜʙᴄᴏᴍᴍᴀɴᴅ."));
        return true;
    }

    private void sendUsage(CommandSender sender, String baseLabel) {
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " create <id>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " delete <id>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " ѕᴇᴛᴘᴏѕ <id>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " ѕᴇᴛᴅɪѕᴘʟᴀʏ <id> <name>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " ѕᴇᴛᴛɪɴɢѕ <id> [ѕᴇᴛᴛɪɴɢ] [ᴏɴ|ᴏꜰꜰ]"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " ᴇɴᴀʙʟᴇ <id>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " ᴅɪѕᴀʙʟᴇ <id>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " ʟɪѕᴛ"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " ʀᴇʟᴏᴀᴅ"));
    }

    private void sendSettingsOverview(CommandSender sender, FfaArena arena) {
        if (sender == null || arena == null) {
            return;
        }

        sender.sendMessage(ColorUtils.toComponent("&eꜰꜰᴀ ᴀʀᴇɴᴀ ѕᴇᴛᴛɪɴɢѕ &7(" + arena.getId() + "&7)"));
        sender.sendMessage(ColorUtils.toComponent("&7- &fɴᴏ ʜᴜɴɢᴇʀ: &a" + arena.isNoHunger()));
        sender.sendMessage(ColorUtils.toComponent("&7- &fɴᴏ ᴡᴇᴀᴛʜᴇʀ: &a" + arena.isNoWeather()));
        sender.sendMessage(ColorUtils.toComponent("&7- &fᴀʟᴡᴀʏѕ ᴍᴏʀɴɪɴɢ: &a" + arena.isAlwaysMorning()));
        sender.sendMessage(ColorUtils.toComponent("&7- &fɴᴏ ꜰᴀʟʟ ᴅᴀᴍᴀɢᴇ: &a" + arena.isNoFallDamage()));
    }

    private FfaManager.ArenaSetting parseArenaSetting(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        return switch (input.trim().toLowerCase()) {
            case "nohunger", "no-hunger", "no_hunger" -> FfaManager.ArenaSetting.NO_HUNGER;
            case "noweather", "no-weather", "no_weather" -> FfaManager.ArenaSetting.NO_WEATHER;
            case "alwaysmorning", "always-morning", "always_morning" -> FfaManager.ArenaSetting.ALWAYS_MORNING;
            case "nofalldamage", "no-fall-damage", "no_fall_damage" -> FfaManager.ArenaSetting.NO_FALL_DAMAGE;
            default -> null;
        };
    }

    private Boolean parseToggle(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        return switch (input.trim().toLowerCase()) {
            case "on", "true", "enable", "enabled", "yes" -> true;
            case "off", "false", "disable", "disabled", "no" -> false;
            default -> null;
        };
    }
}
