package com.bx.ultimateDonutSmp.commands;

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
        if (!sender.hasPermission("ultimatedonutsmp.admin.ffa")) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to manage FFA arenas."));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender, baseLabel);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        if (subcommand.equals("list")) {
            if (plugin.getFfaManager().getArenas().isEmpty()) {
                sender.sendMessage(ColorUtils.toComponent("&cNo FFA arenas configured."));
                return true;
            }
            sender.sendMessage(ColorUtils.toComponent("&eFFA Arenas:"));
            for (FfaArena arena : plugin.getFfaManager().getArenas()) {
                sender.sendMessage(ColorUtils.toComponent(
                        "&7- &f" + arena.getId()
                                + " &8(" + arena.getDisplayName() + "&8)"
                                + " &7state=&f" + arena.getState().name()
                                + " &7configured=&f" + arena.isConfigured()
                                + " &7region=&f" + arena.hasRollbackRegion()
                                + " &7ready=&f" + arena.isReady()
                                + " &7enabled=&f" + arena.isEnabled()
                ));
            }
            return true;
        }
        if (subcommand.equals("reload")) {
            plugin.getConfigManager().reloadFfa();
            plugin.getFfaManager().reload();
            sender.sendMessage(ColorUtils.toComponent("&aReloaded FFA arenas and config."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cYou must specify an arena id."));
            return true;
        }

        String id = args[1];
        if (subcommand.equals("create")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getFfaManager().createArena(id)
                            ? "&aCreated FFA arena &f" + id + "&a."
                            : "&cCould not create that FFA arena."
            ));
            return true;
        }
        if (subcommand.equals("delete")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getFfaManager().deleteArena(id)
                            ? "&aDeleted FFA arena &f" + id + "&a."
                            : "&cCould not delete that FFA arena."
            ));
            return true;
        }
        if (subcommand.equals("enable")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getFfaManager().setArenaEnabled(id, true)
                            ? "&aEnabled FFA arena &f" + id + "&a."
                            : "&cCould not enable that FFA arena."
            ));
            return true;
        }
        if (subcommand.equals("disable")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getFfaManager().setArenaEnabled(id, false)
                            ? "&eDisabled FFA arena &f" + id + "&e."
                            : "&cCould not disable that FFA arena."
            ));
            return true;
        }
        if (subcommand.equals("setdisplay")) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + baseLabel + " setdisplay <id> <name>"));
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
                            ? "&aUpdated display name for FFA arena &f" + id + "&a."
                            : "&cCould not update that FFA arena display name."
            ));
            return true;
        }
        if (subcommand.equals("settings")) {
            FfaArena arena = plugin.getFfaManager().getArena(id);
            if (arena == null) {
                sender.sendMessage(ColorUtils.toComponent("&cThat FFA arena does not exist."));
                return true;
            }

            if (args.length == 2) {
                sendSettingsOverview(sender, arena);
                sender.sendMessage(ColorUtils.toComponent("&7Usage: &f/" + baseLabel + " settings <id> <nohunger|noweather|alwaysmorning|nofalldamage> <on|off>"));
                return true;
            }

            FfaManager.ArenaSetting setting = parseArenaSetting(args[2]);
            if (setting == null) {
                sender.sendMessage(ColorUtils.toComponent("&cUnknown FFA arena setting. Use: nohunger, noweather, alwaysmorning, nofalldamage"));
                return true;
            }

            if (args.length < 4) {
                sender.sendMessage(ColorUtils.toComponent("&cUsage: /" + baseLabel + " settings <id> <nohunger|noweather|alwaysmorning|nofalldamage> <on|off>"));
                return true;
            }

            Boolean enabled = parseToggle(args[3]);
            if (enabled == null) {
                sender.sendMessage(ColorUtils.toComponent("&cValue must be &fon &cor &foff&c."));
                return true;
            }

            if (!plugin.getFfaManager().setArenaSetting(id, setting, enabled)) {
                sender.sendMessage(ColorUtils.toComponent("&cCould not update that FFA arena setting."));
                return true;
            }

            sender.sendMessage(ColorUtils.toComponent(
                    "&aFFA arena &f" + arena.getId() + " &a" + (enabled ? "enabled " : "disabled ") + "&f" + setting.getDisplayName() + "&a."
            ));
            sendSettingsOverview(sender, plugin.getFfaManager().getArena(id));
            return true;
        }
        if (subcommand.equals("setspawn1") || subcommand.equals("setspawn2") || subcommand.equals("setreturn")) {
            sender.sendMessage(ColorUtils.toComponent(
                    "&eFFA arena sekarang tidak pakai spawn manual. Gunakan &f/" + baseLabel + " setpos <id> &euntuk set pusat arena combat."
            ));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cThis subcommand requires a player sender."));
            return true;
        }
        if (subcommand.equals("setpos") || subcommand.equals("setpos1") || subcommand.equals("setpos2")) {
            if (!plugin.getFfaManager().setArenaRegionPos(id, player.getLocation())) {
                sender.sendMessage(ColorUtils.toComponent("&cCould not set FFA region for that arena."));
                return true;
            }
            sender.sendMessage(ColorUtils.toComponent(
                    "&aSet FFA arena center for &f" + id + "&a. &7Combat area dan rollback akan dibangun otomatis dari titik ini."
            ));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent("&cUnknown FFA arena subcommand."));
        return true;
    }

    private void sendUsage(CommandSender sender, String baseLabel) {
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " create <id>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " delete <id>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " setpos <id>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " setdisplay <id> <name>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " settings <id> [setting] [on|off]"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " enable <id>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " disable <id>"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " list"));
        sender.sendMessage(ColorUtils.toComponent("&e/" + baseLabel + " reload"));
    }

    private void sendSettingsOverview(CommandSender sender, FfaArena arena) {
        if (sender == null || arena == null) {
            return;
        }

        sender.sendMessage(ColorUtils.toComponent("&eFFA Arena Settings &7(" + arena.getId() + "&7)"));
        sender.sendMessage(ColorUtils.toComponent("&7- &fno hunger: &a" + arena.isNoHunger()));
        sender.sendMessage(ColorUtils.toComponent("&7- &fno weather: &a" + arena.isNoWeather()));
        sender.sendMessage(ColorUtils.toComponent("&7- &falways morning: &a" + arena.isAlwaysMorning()));
        sender.sendMessage(ColorUtils.toComponent("&7- &fno fall damage: &a" + arena.isNoFallDamage()));
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
