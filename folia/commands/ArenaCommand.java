package com.bx.ultimateDonutSmp.commands;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import com.bx.ultimateDonutSmp.models.DuelArena;
import com.bx.ultimateDonutSmp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand implements CommandExecutor {

    private final UltimateDonutSmp plugin;

    public ArenaCommand(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimatedonutsmp.admin.duels")) {
            sender.sendMessage(ColorUtils.toComponent("&cYou do not have permission to manage duel arenas."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtils.toComponent("&e/arena create <id>"));
            sender.sendMessage(ColorUtils.toComponent("&e/arena delete <id>"));
            sender.sendMessage(ColorUtils.toComponent("&e/arena setpos1 <id>"));
            sender.sendMessage(ColorUtils.toComponent("&e/arena setpos2 <id>"));
            sender.sendMessage(ColorUtils.toComponent("&e/arena setreturn <id>"));
            sender.sendMessage(ColorUtils.toComponent("&e/arena setdisplay <id> <name>"));
            sender.sendMessage(ColorUtils.toComponent("&e/arena enable <id>"));
            sender.sendMessage(ColorUtils.toComponent("&e/arena disable <id>"));
            sender.sendMessage(ColorUtils.toComponent("&e/arena queue <id> <true|false>"));
            sender.sendMessage(ColorUtils.toComponent("&e/arena list"));
            sender.sendMessage(ColorUtils.toComponent("&e/arena reload"));
            return true;
        }

        String subcommand = args[0].toLowerCase();
        if (subcommand.equals("list")) {
            if (plugin.getDuelManager().getArenas().isEmpty()) {
                sender.sendMessage(ColorUtils.toComponent("&cNo duel arenas configured."));
                return true;
            }
            sender.sendMessage(ColorUtils.toComponent("&eDuel Arenas:"));
            for (DuelArena arena : plugin.getDuelManager().getArenas()) {
                sender.sendMessage(ColorUtils.toComponent(
                        "&7- &f" + arena.getId()
                                + " &8(" + arena.getDisplayName() + "&8)"
                                + " &7ready=&f" + arena.isReady()
                                + " &7rollback=&f" + arena.hasRollbackRegion()
                                + " &7pos1=&f" + (arena.getSpawn1() != null)
                                + " &7pos2=&f" + (arena.getSpawn2() != null)
                                + " &7return=&f" + (arena.getReturnLocation() != null)
                                + " &7enabled=&f" + arena.isEnabled()
                                + " &7queue=&f" + arena.isQueueEnabled()
                ));
            }
            return true;
        }
        if (subcommand.equals("reload")) {
            plugin.getConfigManager().reloadDuels();
            plugin.getDuelManager().reload();
            sender.sendMessage(ColorUtils.toComponent("&aReloaded duel arenas and config."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.toComponent("&cYou must specify an arena id."));
            return true;
        }

        String id = args[1];
        if (subcommand.equals("create")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getDuelManager().createArena(id)
                            ? "&aCreated duel arena &f" + id + "&a."
                            : "&cCould not create that duel arena."
            ));
            return true;
        }
        if (subcommand.equals("delete")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getDuelManager().deleteArena(id)
                            ? "&aDeleted duel arena &f" + id + "&a."
                            : "&cCould not delete that duel arena."
            ));
            return true;
        }
        if (subcommand.equals("enable")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getDuelManager().setArenaEnabled(id, true)
                            ? "&aEnabled duel arena &f" + id + "&a."
                            : "&cCould not enable that duel arena."
            ));
            return true;
        }
        if (subcommand.equals("disable")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getDuelManager().setArenaEnabled(id, false)
                            ? "&eDisabled duel arena &f" + id + "&e."
                            : "&cCould not disable that duel arena."
            ));
            return true;
        }
        if (subcommand.equals("queue")) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtils.toComponent("&cUsage: /arena queue <id> <true|false>"));
                return true;
            }
            boolean enabled = Boolean.parseBoolean(args[2]);
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getDuelManager().setArenaQueueEnabled(id, enabled)
                            ? "&aUpdated queue status for &f" + id + "&a."
                            : "&cCould not update queue status for that arena."
            ));
            return true;
        }
        if (subcommand.equals("setdisplay")) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtils.toComponent("&cUsage: /arena setdisplay <id> <name>"));
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
                    plugin.getDuelManager().setArenaDisplayName(id, builder.toString())
                            ? "&aUpdated display name for arena &f" + id + "&a."
                            : "&cCould not update that arena display name."
            ));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.toComponent("&cThis subcommand requires a player sender."));
            return true;
        }
        if (subcommand.equals("setpos1")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getDuelManager().setArenaRegionPos(id, 1, player.getLocation())
                            ? "&aSet pos 1 for arena &f" + id + "&a. This now acts as spawn 1 and rollback anchor."
                            : "&cCould not set pos 1 for that arena."
            ));
            return true;
        }
        if (subcommand.equals("setpos2")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getDuelManager().setArenaRegionPos(id, 2, player.getLocation())
                            ? "&aSet pos 2 for arena &f" + id + "&a. This now acts as spawn 2 and rollback anchor."
                            : "&cCould not set pos 2 for that arena."
            ));
            return true;
        }
        if (subcommand.equals("setreturn")) {
            sender.sendMessage(ColorUtils.toComponent(
                    plugin.getDuelManager().setArenaReturn(id, player.getLocation())
                            ? "&aSet return location for arena &f" + id + "&a."
                            : "&cCould not set return location for that arena."
            ));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent("&cUnknown arena subcommand."));
        return true;
    }
}
